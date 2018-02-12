package ru.kmorozov.gbd.core.config.options;

public class CtxOptions {

    public static final CtxOptions DEFAULT_CTX_OPTIONS = new CtxOptions(CtxMode.FILE, "books.ctx");

    public enum CtxMode {
        FILE,
        MONGO
    }

    private CtxOptions.CtxMode ctxMode;
    private String connectionParams;

    public CtxOptions(String ctxMode, String connectionParams) {
        this.ctxMode = CtxMode.valueOf(ctxMode);
        this.connectionParams = connectionParams;
    }

    CtxOptions(CtxMode ctxMode, String connectionParams) {
        this.ctxMode = ctxMode;
        this.connectionParams = connectionParams;
    }

    public CtxOptions.CtxMode getCtxMode() {
        return this.ctxMode;
    }

    public String getConnectionParams() {
        return this.connectionParams;
    }
}