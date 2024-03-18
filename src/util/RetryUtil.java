
import lombok.extern.slf4j.Slf4j;
import java.util.concurrent.Callable;

/**
 * @author xkun
 * @create 2024/3/18 9:43 上午
 */
@Slf4j
public class RetryUtil {

    /**
     * 简单的重试方法
     *
     * @param callable        回调方法
     * @param retryTimeLimit  重试次数
     * @param sleepTimeMillis 重试时间间隔，0表示无间隔
     */
    public static <V> V retryCallable(Callable<V> callable, int retryTimeLimit, long sleepTimeMillis) {
        int retryTimes = 0;
        while (retryTimes++ < retryTimeLimit) {
            try {
                return callable.call();
            } catch (Exception e) {
                if (retryTimes == retryTimeLimit) {
                    log.error("[retryTimeOut]重试超次后依然失败", e);
                    throw new RunTimeException("retryTimeOut", e); //业务异常
                } else {
                    log.info("[retrying]执行失败，进行重试", e);
                }
            }
            if (sleepTimeMillis > 0) {
                try {
                    Thread.sleep(sleepTimeMillis);
                } catch (Exception e) {
                    log.error("线程睡眠异常", e);
                }
            }
        }
        log.error("[retryTimeOut]重试超次后依然失败");
        throw new RuntimeException("retryTimeOut");
    }

}
