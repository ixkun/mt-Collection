//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package com.sankuai.mpmctleads.common.util;

import com.google.common.collect.Lists;
import com.sankuai.mpmctleads.common.util.exception.BizException;
import com.sankuai.mpmctleads.common.util.exception.ServiceTimeOutException;
import com.sankuai.mpmctleads.common.util.model.PageModel;
import com.sankuai.mpmctleads.common.util.model.PageReqModel;
import com.sankuai.mpmctleads.common.util.monitor.NonIgnorableErrorMonitorUtil;
import com.sankuai.mpmctmvacommon.resource.response.constant.CommonRespCodeEnum;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.function.BiFunction;
import java.util.function.Function;
import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.collections.MapUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConcurrentUtil {
    private static final Logger log = LoggerFactory.getLogger(ConcurrentUtil.class);

    public ConcurrentUtil() {
    }

    public static <T> T getFromFutureIfExist(Future<T> future, long milliSeconds) {
        if (future == null) {
            return null;
        } else {
            try {
                return future.get(milliSeconds, TimeUnit.MILLISECONDS);
            } catch (TimeoutException var4) {
                throw new ServiceTimeOutException("并行执行超时", var4);
            } catch (ExecutionException var5) {
                throw new BizException("并行执行失败", var5);
            } catch (InterruptedException var6) {
                throw new BizException("并行执行中断", var6);
            }
        }
    }

    public static <T, V> List<V> parallelPaginateAsList(T queryCondition, Long pageSize, ExecutorService executorService, Function<PageReqModel<T>, PageModel<V>> func) {
        return parallelPaginateAsList(queryCondition, pageSize, executorService, (Integer)null, func);
    }

    public static <T, V> List<V> parallelPaginateAsList(T queryCondition, Long pageSize, ExecutorService executorService, Integer milliSeconds, Function<PageReqModel<T>, PageModel<V>> func) {
        AssertUtil.assertTrue(Objects.nonNull(pageSize), CommonRespCodeEnum.CODE_217, "pageSize不能为空", new Object[0]);
        PageReqModel<T> pageReqModelForTotalSize = new PageReqModel(1L, 1L, queryCondition);
        PageModel<V> pageModelForTotalSize = (PageModel)func.apply(pageReqModelForTotalSize);
        AssertUtil.assertTrue(Objects.nonNull(pageModelForTotalSize), CommonRespCodeEnum.CODE_217, "查询结果为空", new Object[0]);
        AssertUtil.assertTrue(Objects.nonNull(pageModelForTotalSize.getTotal()), CommonRespCodeEnum.CODE_217, "查询结果总数为空", new Object[0]);
        long totalSize = pageModelForTotalSize.getTotal();
        if (totalSize <= 0L) {
            return new ArrayList();
        } else {
            long totalPageNum = totalSize / pageSize;
            if (totalSize % pageSize != 0L) {
                ++totalPageNum;
            }

            List<Callable<PageModel<V>>> tasks = new ArrayList();

            for(long pageItr = 0L; pageItr < totalPageNum; ++pageItr) {
                long pageNum = pageItr + 1L;
                PageReqModel<T> pageReqModel = new PageReqModel(pageSize, pageNum, queryCondition);
                tasks.add(() -> {
                    return (PageModel)func.apply(pageReqModel);
                });
            }

            try {
                List futures;
                if (Objects.nonNull(milliSeconds)) {
                    futures = executorService.invokeAll(tasks, (long)milliSeconds, TimeUnit.MILLISECONDS);
                } else {
                    futures = executorService.invokeAll(tasks);
                }

                List<V> result = new ArrayList();
                Iterator var19 = futures.iterator();

                while(var19.hasNext()) {
                    Future<PageModel<V>> future = (Future)var19.next();
                    if (!future.isDone()) {
                        log.error("[parallelPaginateAsListTimeOut]并行处理执行超时未完成");
                        NonIgnorableErrorMonitorUtil.logEvent("parallelPaginateAsListTimeOut");
                        throw new BizException("并行处理执行超时未完成");
                    }

                    PageModel<V> pageModel = (PageModel)future.get();
                    if (Objects.nonNull(pageModel.getPageData())) {
                        result.addAll(pageModel.getPageData());
                    }
                }

                return result;
            } catch (Exception var17) {
                log.error("[parallelPaginateAsListException]并行处理失败", var17);
                NonIgnorableErrorMonitorUtil.logEvent("parallelPaginateAsListException");
                throw new BizException("并行处理失败", var17);
            }
        }
    }

    public static <T, V> List<V> parallelGetAsList(List<T> sourceList, Integer eachSize, ExecutorService executorService, Function<List<T>, List<V>> func) {
        return parallelGetAsList(sourceList, eachSize, executorService, (Integer)null, func);
    }

    public static <T, V> List<V> parallelGetAsList(List<T> sourceList, Integer eachSize, ExecutorService executorService, Integer milliSeconds, Function<List<T>, List<V>> func) {
        if (CollectionUtils.isEmpty(sourceList)) {
            return new ArrayList();
        } else if (sourceList.size() <= eachSize) {
            return (List)func.apply(sourceList);
        } else {
            List<List<T>> partitions = Lists.partition(sourceList, eachSize);
            List<Callable<List<V>>> tasks = new ArrayList();
            Iterator futures = partitions.iterator();

            while(futures.hasNext()) {
                List<T> partition = (List)futures.next();
                tasks.add(() -> {
                    return (List)func.apply(partition);
                });
            }

            try {
                futures = null;
                List futures;
                if (Objects.nonNull(milliSeconds)) {
                    futures = executorService.invokeAll(tasks, (long)milliSeconds, TimeUnit.MILLISECONDS);
                } else {
                    futures = executorService.invokeAll(tasks);
                }

                List<V> result = new ArrayList();
                Iterator var9 = futures.iterator();

                while(var9.hasNext()) {
                    Future<List<V>> future = (Future)var9.next();
                    if (!future.isDone()) {
                        log.error("[parallelGetAsListTimeOut]并行处理执行超时未完成");
                        NonIgnorableErrorMonitorUtil.logEvent("parallelGetAsListTimeOut");
                        throw new BizException("并行处理执行超时未完成");
                    }

                    List<V> futureResult = (List)future.get();
                    if (CollectionUtils.isNotEmpty(futureResult)) {
                        result.addAll(futureResult);
                    }
                }

                return result;
            } catch (Exception var12) {
                log.error("[parallelGetAsListException]并行处理失败", var12);
                NonIgnorableErrorMonitorUtil.logEvent("parallelGetAsListException");
                throw new BizException("并行处理失败", var12);
            }
        }
    }

    public static <T, V> Map<T, V> parallelGetAsMap(List<T> sourceList, Integer eachSize, ExecutorService executorService, Function<List<T>, Map<T, V>> func) {
        return parallelGetAsMap(sourceList, eachSize, executorService, (Integer)null, func);
    }

    public static <T, V> Map<T, V> parallelGetAsMap(List<T> sourceList, Integer eachSize, ExecutorService executorService, Integer milliSeconds, Function<List<T>, Map<T, V>> func) {
        if (CollectionUtils.isEmpty(sourceList)) {
            return new HashMap();
        } else if (sourceList.size() <= eachSize) {
            return (Map)func.apply(sourceList);
        } else {
            List<List<T>> partitions = Lists.partition(sourceList, eachSize);
            List<Callable<Map<T, V>>> tasks = new ArrayList();
            Iterator futures = partitions.iterator();

            while(futures.hasNext()) {
                List<T> partition = (List)futures.next();
                tasks.add(() -> {
                    return (Map)func.apply(partition);
                });
            }

            try {
                futures = null;
                List futures;
                if (Objects.nonNull(milliSeconds)) {
                    futures = executorService.invokeAll(tasks, (long)milliSeconds, TimeUnit.MILLISECONDS);
                } else {
                    futures = executorService.invokeAll(tasks);
                }

                Map<T, V> result = new HashMap();
                Iterator var9 = futures.iterator();

                while(var9.hasNext()) {
                    Future<Map<T, V>> future = (Future)var9.next();
                    if (!future.isDone()) {
                        log.error("[taskTimeOut]并行处理执行超时未完成");
                        NonIgnorableErrorMonitorUtil.logEvent("parallelGetAsMapTimeOut");
                        throw new BizException("并行处理执行超时未完成");
                    }

                    Map<T, V> futureResult = (Map)future.get();
                    if (MapUtils.isNotEmpty(futureResult)) {
                        result.putAll(futureResult);
                    }
                }

                return result;
            } catch (Exception var12) {
                log.error("[parallelGetAsMapException]并行处理失败", var12);
                NonIgnorableErrorMonitorUtil.logEvent("parallelGetAsMapException");
                throw new BizException("并行处理失败", var12);
            }
        }
    }

    public static <V> V futureGet(Future<V> future, String getAction, Long timeOut, V defaultVal) {
        try {
            timeOut = Objects.isNull(timeOut) ? 1000L : timeOut;
            return future.get(timeOut, TimeUnit.MILLISECONDS);
        } catch (Exception var5) {
            log.error(String.format("%s ,%s , 获取异常 ", getAction, timeOut), var5);
            NonIgnorableErrorMonitorUtil.logEvent("futureGetException");
            return defaultVal;
        }
    }

    public static <T, V, R> R parallelGetTaskResults(List<Callable<T>> tasks, Function<T, V> func, BiFunction<V, R, R> merger, ExecutorService executorService, long milliSeconds) {
        if (!CollectionUtils.isEmpty(tasks) && func != null && merger != null && executorService != null && milliSeconds > 0L) {
            try {
                List<Future<T>> futures = executorService.invokeAll(tasks, milliSeconds, TimeUnit.MILLISECONDS);
                R result = null;

                Object midResult;
                for(Iterator var8 = futures.iterator(); var8.hasNext(); result = merger.apply(midResult, result)) {
                    Future<T> future = (Future)var8.next();
                    if (!future.isDone()) {
                        log.error("[taskTimeOut]并行处理执行超时未完成");
                        NonIgnorableErrorMonitorUtil.logEvent("parallelGetTaskResultsTimeOut");
                        throw new BizException("并行处理执行超时未完成");
                    }

                    T futureResult = future.get();
                    midResult = func.apply(futureResult);
                }

                return result;
            } catch (Exception var12) {
                log.error("[parallelGetTaskResultsException]并行处理失败", var12);
                NonIgnorableErrorMonitorUtil.logEvent("parallelGetTaskResultsException");
                throw new BizException("并行处理失败", var12);
            }
        } else {
            return null;
        }
    }
}
