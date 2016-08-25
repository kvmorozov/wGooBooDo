package ru.simpleGBD.App.Utils;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Created by km on 21.11.2015.
 */
public class Pools {

    public final static ExecutorService sigExecutor = Executors.newFixedThreadPool(50);
    public final static ExecutorService imgExecutor = Executors.newFixedThreadPool(50);
}
