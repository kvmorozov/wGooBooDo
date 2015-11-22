package ru.kmorozov.App.Logic.DataModel;

import ru.kmorozov.App.Logic.ExecutionContext;
import ru.kmorozov.App.Logic.Runtime.ImageExtractor;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by km on 21.11.2015.
 */

public class PageInfo {

    private String pid, flags, title, src, uf, sig;
    private int order, h;
    private Object links;

    public Lock sigRequestLock = new ReentrantLock();
    public Lock imgRequestLock = new ReentrantLock();

    private boolean sigChecked = false;

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

    public synchronized String getSrc() {
        return src;
    }

    public synchronized void setSrc(String src) {
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

    public boolean isSigChecked() {
        return sigChecked;
    }

    public void setSigChecked(boolean sigChecked) {
        this.sigChecked = sigChecked;
    }

    public String getSig() {
        return src == null ? null : sig == null ? sig = src.substring(src.indexOf("sig=") + 4) : sig;
    }

    public String getPageUrl() {
        return ExecutionContext.baseUrl + ImageExtractor.IMG_REQUEST_TEMPLATE
                .replace(ImageExtractor.RQ_PG_PLACEHOLED, getPid())
                .replace(ImageExtractor.RQ_SIG_PLACEHOLED, getSig());
    }
}
