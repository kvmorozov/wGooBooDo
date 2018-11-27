package ru.kmorozov.gbd.core.logic.Proxy;

import org.apache.commons.io.IOUtils;
import ru.kmorozov.gbd.core.config.GBDOptions;

import java.io.*;
import java.util.HashSet;

/**
 * Created by sbt-morozov-kv on 02.12.2015.
 */
public class FileProxyListProvider extends AbstractProxyListProvider {

    FileProxyListProvider() {
        this.buildList();
    }

    private void buildList() {
        String proxyListFileName = GBDOptions.getProxyListFile();
        if (null == proxyListFileName || proxyListFileName.isEmpty()) return;

        File proxyListFile = new File(proxyListFileName);
        if (!proxyListFile.exists() && !proxyListFile.canRead()) return;

        try (final InputStream is = new FileInputStream(proxyListFile)) {
            proxyItems = new HashSet(IOUtils.readLines(is, "UTF-8"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateProxyList() {
        String proxyListFileName = GBDOptions.getProxyListFile();
        if (null == proxyListFileName || proxyListFileName.isEmpty()) return;

        File proxyListFile = new File(proxyListFileName);
        if (!proxyListFile.exists() && !proxyListFile.canRead()) return;

        try (final OutputStream os = new FileOutputStream(proxyListFile, false)) {
            for (HttpHostExt proxy : this.proxyList)
                if (!proxy.isLocal() && proxy.isAvailable()) IOUtils.write(String.format("%s %s%n", proxy.getHost().getHostName(), proxy.getHost().getPort()), os, "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
