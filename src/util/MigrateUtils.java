package xxx.xxx.xxx;

import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;

import java.util.Comparator;
import java.util.concurrent.*;


/**
 * @author kunxu
 */
@Slf4j
public class MigrateUtils {
    /**
     * 读比对最大重试次数
     */
    private static final int MAX_READING_COMPARISON_RETRY_TIMES = 3;

    /**
     * 重试时间间隔,单位毫秒
     */
    private static final int RETRY_TIME_INTERVAL = 1000;

    /**
     * 默认超时时间，单位毫秒
     */
    public static final long DEFAULT_TIMEOUT = 1000;

    /**
     * 切读
     * 注意：未配置灰度目标、配置参数、线程池的情况，兜底执行oldMethod
     * 切读工具是为了提供读接口平滑切换的能力。使用的时候需要通过实现Callable接口使新老接口返回的结果类型一致，通过实现Comparator比较器，可以提供对于新老接口读结果的比对能力。
     * 建议切读的顺序是上线时走老的读接口、开启读比对，发现没问题后逐步放量走新的读接口，等到全量后一段时间，观察读比对结果一直没问题后关闭读比对。
     *
     */
    @SneakyThrows
    public static <T> T switchRead(Long target, String switchMethodName, String readingComparisonGraySwitchkey, Callable<T> newMethod, Callable<T> oldMethod, Comparator<T> comparator, ExecutorService migrateExecutor, Runnable inconsistentExtensionMethod) {
        if (null == target || target <= 0) {
            log.info("[MigrateUtils switchRead] 缺失切读灰度目标，兜底执行oldMethod");
            return oldMethod.call();
        }
        if (StringUtils.isBlank(switchMethodName) || StringUtils.isBlank(readingComparisonGraySwitchkey)) {
            log.info("[MigrateUtils switchRead] 缺失配置参数，兜底执行oldMethod");
            return oldMethod.call();
        }
        if (null == migrateExecutor) {
            log.info("[MigrateUtils switchRead] 未配置线程池，兜底执行oldMethod");
            return oldMethod.call();
        }
        if (GraySwitchUtil.isSwitchedAdvanced(target, readingComparisonGraySwitchkey + "." + switchMethodName, "switchRead-compare")) {
            // 开启读比对,若是数据迁移，则此时老数据源未下线
            migrateExecutor.submit(() -> readingComparison(target, switchMethodName, newMethod, oldMethod, comparator, inconsistentExtensionMethod, MAX_READING_COMPARISON_RETRY_TIMES));
        }
        if (GraySwitchUtil.isSwitchedAdvanced(target, readingComparisonGraySwitchkey + "." + switchMethodName, "switchRead-new")) {
            log.info("[MigrateUtils switchRead]命中切读灰度开关,target:{},switchMethodName:{}", target, switchMethodName);
            return newMethod.call();
        } else {
            log.info("[MigrateUtils switchRead]未命中切读灰度开关,target:{},switchMethodName:{}", target, switchMethodName);
            return oldMethod.call();
        }
    }

    
    private static <T> void readingComparison(Long target, String switchMethodName, Callable<T> newMethod, Callable<T> oldMethod, Comparator<T> comparator, Runnable inconsistentExtensionMethod, int maxRetryTimes) {
        T newResult = null;
        T oldResult = null;
        int retryTimes = 0;
        boolean consistent = false;
        while (retryTimes < maxRetryTimes) {
            retryTimes++;
            newResult = null;
            oldResult = null;
            try {
                oldResult = oldMethod.call();
                newResult = newMethod.call();
            } catch (Exception e) {
                log.error("[MigrateUtils readingComparison]读比对时出现异常", e);
                try {
                    Thread.sleep(RETRY_TIME_INTERVAL);
                } catch (Exception exp) {
                    log.error("[MigrateUtils readingComparison]线程睡眠出现异常", exp);
                }
                continue;
            }
            if (oldResult == null && newResult == null) {
                log.info("[MigrateUtils readingComparison]读比对结果一致,oldResult:{},newResult:{},target:{},switchMethodName:{},retryTimes:{}", GsonUtil.toJson(oldResult), GsonUtil.toJson(newResult), target, switchMethodName, retryTimes);
                consistent = true;
                break;
            }
            if (oldResult != null && newResult == null) {
                readingComparisonWhenInconsistent(target, switchMethodName, oldResult, newResult, retryTimes);
                continue;
            }
            if (oldResult != null && 0 == comparator.compare(oldResult, newResult)) {
                log.info("[MigrateUtils readingComparison]读比对结果一致,oldResult:{},newResult:{},target:{},switchMethodName:{},retryTimes:{}", GsonUtil.toJson(oldResult), GsonUtil.toJson(newResult), target, switchMethodName, retryTimes);
                consistent = true;
                break;
            }
            readingComparisonWhenInconsistent(target, switchMethodName, oldResult, newResult, retryTimes);
        }
        if (!consistent) {
            log.info("[MigrateUtils readingComparison]读比对结果不一致,oldResult:{},newResult:{},target:{},switchMethodName:{}", GsonUtil.toJson(oldResult), GsonUtil.toJson(newResult), target, switchMethodName);
            if (null != inconsistentExtensionMethod) {
                try {
                    inconsistentExtensionMethod.run();
                } catch (Exception e) {
                    log.info("[MigrateUtils readingComparison]执行读比对不一致扩展点时出现异常", e);
                }
            }
        }
    }

    private static <T> void readingComparisonWhenInconsistent(Long target, String switchMethodName, T oldResult, T newResult, int retryTimes) {
        log.info("[MigrateUtils readingComparison]读比对结果不一致,等待重试，oldResult:{},newResult:{},target:{},switchMethodName:{},retryTimes:{}", GsonUtil.toJson(oldResult), GsonUtil.toJson(newResult), target, switchMethodName, retryTimes);
        try {
            Thread.sleep(RETRY_TIME_INTERVAL);
        } catch (Exception e) {
            log.info("[MigrateUtils readingComparison]线程睡眠出现异常", e);
        }
    }

}

