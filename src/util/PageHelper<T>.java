package com.sankuai.mpmctleads.common.util.page;

import com.dianping.cat.Cat;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.collections.CollectionUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * 分页助手
 *
 * @author xkun
 * @date 2024/03/19
 */
@Slf4j
public class PageHelper<T> {
    private PageDataCollection<T> dataCollection;
    private int currentPageNo;
    private int pageSize;
    private int countPageNo;
    private int dataCount;

    private PageHelper(PageDataCollection<T> dataCollection, int pageSize) {
        this.dataCollection = dataCollection;
        this.currentPageNo = 1;
        this.pageSize = pageSize;
        if (null == dataCollection) {
            throw new IllegalArgumentException("illegalCollectionParameter");
        }
        this.dataCount = dataCollection.getCount();
        this.countPageNo = dataCount % pageSize == 0 ? dataCount / pageSize : dataCount / pageSize + 1;
    }

    /**
     * 构建
     *
     * @param dataCollection 数据收集
     * @param pageSize       分大小
     * @return {@link PageHelper}
     */
    public static PageHelper build(PageDataCollection dataCollection, int pageSize) {
        return new PageHelper<>(dataCollection, pageSize);
    }

    /**
     * 获取分页数据
     *
     * @param pageNo 页码
     * @return {@link List}<{@link T}>
     */
    public List<T> getPage(int pageNo) {
        if (pageNo > countPageNo) {
            return Collections.EMPTY_LIST;
        }
        List<T> dataList = dataCollection.getData(pageNo);
        if (CollectionUtils.isEmpty(dataList)) {
            log.warn("PageHelperError getPage data is empty data name is {}  , pageNo is {}", dataCollection.getDataName(), pageNo);
            Cat.logEvent("PageHelperError", "getPageEmpty");
        }
        return dataList;
    }

    /**
     * 下一个页面
     *
     * @return {@link List}<{@link T}>
     */
    public List<T> nextPage() {
        if (currentPageNo > countPageNo) {
            return Collections.EMPTY_LIST;
        }
        List<T> dataList = dataCollection.getData(currentPageNo);
        if (CollectionUtils.isEmpty(dataList)) {
            log.warn("PageHelperError nextPage data is empty data name is {} , currentPageNo is {}", dataCollection.getDataName(), currentPageNo);
            Cat.logEvent("PageHelperError", "nextPageEmpty");
        }
        currentPageNo = currentPageNo + 1;
        return dataList;
    }

    /**
     * 是否有下一个页面
     *
     * @return boolean
     */
    public boolean hasNextPage() {
        return currentPageNo <= countPageNo;
    }

    /**
     * 是否有前一页
     *
     * @return boolean
     */
    public boolean hasPreviousPage() {
        return currentPageNo == 1;
    }

    /**
     * 得到当前分页数
     *
     * @return int
     */
    public int getCurrentPageNo() {
        return this.currentPageNo;
    }

    /**
     * 获取分页总数
     *
     * @return int
     */
    public int getPageNoCount() {
        return this.countPageNo;
    }

    /**
     * 获取数据总数
     *
     * @return int
     */
    public int getDataCount() {
        return this.dataCount;
    }

    /**
     * 获取分页大小
     *
     * @return int
     */
    public int getPageSize() {
        return pageSize;
    }

}
