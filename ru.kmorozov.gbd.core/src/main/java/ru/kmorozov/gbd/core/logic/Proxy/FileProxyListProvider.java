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
        buildList();
    }

    private void buildList() {
        final String proxyListFileName = GBDOptions.getProxyListFile();
        if (null == proxyListFileName || proxyListFileName.isEmpty()) return;

        final File proxyListFile = new File(proxyListFileName);
        if (!proxyListFile.exists() && !proxyListFile.canRead()) return;

        try (InputStream is = new FileInputStream(proxyListFile)) {
            this.proxyItems = new HashSet(IOUtils.readLines(is, "UTF-8"));
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateProxyList() {
        final String proxyListFileName = GBDOptions.getProxyListFile();
        if (null == proxyListFileName || proxyListFileName.isEmpty()) return;

        final File proxyListFile = new File(proxyListFileName);
        if (!proxyListFile.exists() && !proxyListFile.canRead()) return;

        try (OutputStream os = new FileOutputStream(proxyListFile, false)) {
            for (final HttpHostExt proxy : proxyList)
                if (!proxy.isLocal() && proxy.isAvailable()) IOUtils.write(String.format("%s %s%n", proxy.getHost().getHostName(), proxy.getHost().getPort()), os, "UTF-8");
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }
}
