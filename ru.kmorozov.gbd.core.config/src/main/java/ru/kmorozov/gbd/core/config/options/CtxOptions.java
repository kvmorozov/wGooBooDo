package ru.kmorozov.gbd.core.config.options;

public class CtxOptions {

    public static final CtxOptions DEFAULT_CTX_OPTIONS = new CtxOptions(CtxOptions.CtxMode.FILE, "books.ctx");

    public enum CtxMode {
        FILE,
        MONGO
    }

    private final CtxOptions.CtxMode ctxMode;
    private final String connectionParams;

    public CtxOptions(final String ctxMode, final String connectionParams) {
        this.ctxMode = CtxOptions.CtxMode.valueOf(ctxMode);
        this.connectionParams = connectionParams;
    }

    CtxOptions(final CtxOptions.CtxMode ctxMode, final String connectionParams) {
        this.ctxMode = ctxMode;
        this.connectionParams = connectionParams;
    }

    public CtxOptions.CtxMode getCtxMode() {
        return ctxMode;
    }

    public String getConnectionParams() {
        return connectionParams;
    }
}
