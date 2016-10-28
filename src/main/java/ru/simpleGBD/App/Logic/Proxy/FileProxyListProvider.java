package ru.simpleGBD.App.Logic.Proxy;

import org.apache.commons.io.IOUtils;
import ru.simpleGBD.App.Config.GBDOptions;

import java.io.*;

/**
 * Created by sbt-morozov-kv on 02.12.2015.
 */
public class FileProxyListProvider extends AbstractProxyListProvider {

    FileProxyListProvider() {
        buildList();
    }

    private void buildList() {
        String proxyListFileName = GBDOptions.getProxyListFile();
        if (proxyListFileName == null || proxyListFileName.length() == 0)
            return;

        File proxyListFile = new File(proxyListFileName);
        if (!proxyListFile.exists() && !proxyListFile.canRead())
            return;

        try (InputStream is = new FileInputStream(proxyListFile)) {
            this.proxyItems = IOUtils.readLines(is, "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void updateProxyList() {
        String proxyListFileName = GBDOptions.getProxyListFile();
        if (proxyListFileName == null || proxyListFileName.length() == 0)
            return;

        File proxyListFile = new File(proxyListFileName);
        if (!proxyListFile.exists() && !proxyListFile.canRead())
            return;

        try (OutputStream os = new FileOutputStream(proxyListFile, false)) {
            for (HttpHostExt proxy : proxyList)
                if (!proxy.isLocal() && proxy.isAvailable())
                    IOUtils.write(String.format("%s %s%n", proxy.getHost().getHostName(), proxy.getHost().getPort()), os, "UTF-8");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
