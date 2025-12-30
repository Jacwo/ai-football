package cn.xingxing.dto;

import lombok.Getter;
import lombok.ToString;

import java.io.Serializable;
import java.util.Collections;
import java.util.List;

/**
 * 通用分页插件
 * @author xyb
 * @date 2025年11月20 10:08
 */
@Getter
@ToString
public class PageVO<E> implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long pageSize = 10L;

    private Long pageNo = 1L;

    private Long pages = 0L;

    private Long total = 0L;

    private List<E> list;

    public PageVO(long pageNo, long pageSize) {
        this(pageNo,pageSize,0L,null);
    }

    public PageVO(int pageNo, int pageSize) {
        this(pageNo,pageSize,0L,null);
    }

    public PageVO(){
        this(1L,10L);
    }

    public PageVO(long pageNo, long pageSize, long total, List<E> list) {
        if(pageNo <= 0){
            pageNo = 1L;
        }
        if (pageSize <= 0) {
            pageSize = 10L;
        }
        if(total < 0){
            total = 0L;
        }
        if(list == null){
            list = Collections.emptyList();
        }
        this.pageNo = pageNo;
        this.pageSize = pageSize;
        this.total = total;
        this.list = list;
        this.pages = 0L;
        if (total > 0) {
            this.pages = (total - 1) / pageSize + 1;
        }
    }

    public void setTotal(long total) {
        this.total = total;
        this.pages = 0L;
        if(total > 0){
            this.pages = (total - 1) / pageSize + 1;
        }
    }

    public void setList(List<E> list) {
        this.list = list;
    }
}
