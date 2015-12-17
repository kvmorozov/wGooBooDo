package ru.simpleGBD.App.Logic.model.book;

/**
 * Created by km on 12.12.2015.
 */
public enum Resolutions {

    _350px(350),
    _410px(410),
    _495px(495),
    _575px(575),
    _685px(685),
    _800px(800),
    _910px(910),
    _1042px(1042),
    _1280px(1280);

    private final int resolution;

    Resolutions(int resolution) {
        this.resolution = resolution;
    }

    @Override
    public String toString() {
        return String.format("%d px", resolution);
    }

    public int getResolution() {
        return resolution;
    }

    public static Resolutions getEnum(int value) {
        for (Resolutions v : values())
            if (v.getResolution() == value) return v;

        throw new IllegalArgumentException();
    }
}
