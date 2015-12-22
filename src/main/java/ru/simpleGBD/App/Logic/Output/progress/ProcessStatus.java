package ru.simpleGBD.App.Logic.Output.progress;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by km on 22.12.2015.
 */
public class ProcessStatus {

    private AtomicInteger maxValue;

    public ProcessStatus(int maxValue) {
        this.maxValue = new AtomicInteger(maxValue);
    }

    public int inc() {
        return maxValue.incrementAndGet();
    }
}
