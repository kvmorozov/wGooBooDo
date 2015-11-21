package ru.kmorozov.App.Logic.DataModel;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by km on 21.11.2015.
 */

public class PageInfo {

    private String pid, flags, title, src, uf;
    private int order, h;
    private Object links;

    public AtomicBoolean sigRequestStarted = new AtomicBoolean(false);
    public AtomicBoolean imgRequestStarted = new AtomicBoolean(false);

    public String getPid() {
        return pid;
    }

    public void setPid(String pid) {
        this.pid = pid;
    }

    public String getFlags() {
        return flags;
    }

    public void setFlags(String flags) {
        this.flags = flags;
    }

    public int getOrder() {
        return order;
    }

    public void setOrder(int order) {
        this.order = order;
    }

    public int getH() {
        return h;
    }

    public void setH(int h) {
        this.h = h;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getSrc() {
        return src;
    }

    public void setSrc(String src) {
        this.src = src;
    }

    public String getUf() {
        return uf;
    }

    public void setUf(String uf) {
        this.uf = uf;
    }

    public Object getLinks() {
        return links;
    }

    public void setLinks(Object links) {
        this.links = links;
    }

    public String getSig() {
        return src == null ? null : src.substring(src.indexOf("sig=") + 4);
    }
}
