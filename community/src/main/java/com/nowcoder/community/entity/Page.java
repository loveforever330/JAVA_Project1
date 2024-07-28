package com.nowcoder.community.entity;

/**
 * 封装分页相关的信息
 */
public class Page {
    //当前页码
    private int current =1;
    //显示上限
    private int limit =10;
    //数据总数(计算总共的页数)
    private int rows;
    //查询路径，返回给页面即可，复用分页的链接
    private String path;

    public int getCurrent() {
        return current;
    }

    /**
     *
     * @param current: 需要去做一个判断，对其的要求是>=1,才认为是合法的
     */
    public void setCurrent(int current) {
        if(current>=1) {
            this.current = current;
        }
        }

    public int getLimit() {
        return limit;
    }

    public void setLimit(int limit) {
        if(limit>=1&&limit<=100) {//防止过大或者过小
            this.limit = limit;
        }
    }

    public int getRows() {
        return rows;
    }

    public void setRows(int rows) {
        if(rows>=0) {
            this.rows = rows;
        }
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }

    /**
     * 获取当前页面的起始符，当前页面的起始行
     * @return
     */
    public int getOffset(){
            //current*limit-limit
        return  (current-1)*limit;
    }

    /**
     * 获取总页数,z总的行数/每页的限制条数
     * @return
     */
    public int getTotal(){
        //rows/limit
        if(rows%limit==0){//可以被整除
            return rows/limit;
        }
        else {//不可以被整除
            return rows/limit+1;
        }
    }
    /**
     * 获取起始页码
     *当前页面的前面两页
     */
    public int getFrom(){
        int from=current-2;
        return from<1?1:from;
    }
    /**
     * 获取结束页码
     * 当前页面的后两页
     */
    public int getTo(){
        int to =current+2;
        int total=getTotal();//最后一页
        return to> total ? total:to;
    }
}
