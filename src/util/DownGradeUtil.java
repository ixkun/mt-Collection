import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.message.FormattedMessageFactory;

import java.util.concurrent.Callable;

/**
 * @author xkun
 * @create 2024/3/18 10:46 上午
 */
@Slf4j
public class DownGradeUtil {

    private final static FormattedMessageFactory FORMATTED_MESSAGE_FACTORY = new FormattedMessageFactory();

    /**
     * 降级调用
     *
     * @param runnable  runnable
     * @param eventName 打点事件
     */
    public static void downGradeRun(Runnable runnable, String eventName) {
        try {
            runnable.run();
        } catch (Exception e) {
            ExceptionMonitorUtil.logForException(e, FORMATTED_MESSAGE_FACTORY.newMessage("downGradeException eventName = {}", eventName).getFormattedMessage());
            log.error("[downGradeRunException]降级调用代码执行出错，自动降级", e);
        }
    }


    /**
     * 降级调用
     *
     * @param callable  callable
     * @param defaultV  降级值
     * @param eventName 打点事件
     * @param <V>       返回值
     * @return 返回结果
     */
    public static <V> V downGradeCall(Callable<V> callable, V defaultV, String eventName) {
        try {
            return callable.call();
        } catch (Exception e) {
            ExceptionMonitorUtil.logForException(e, FORMATTED_MESSAGE_FACTORY.newMessage("downGradeException eventName = {} defaultValue = {}", eventName, GsonUtil.toJson(defaultV)).getFormattedMessage());
            log.error("[downGradeCallException]降级调用代码执行出错，自动降级", e);
            return defaultV;
        }
    }

}
