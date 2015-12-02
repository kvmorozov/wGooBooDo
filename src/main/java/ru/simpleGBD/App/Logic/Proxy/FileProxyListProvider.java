package ru.simpleGBD.App.Logic.Proxy;

import org.apache.commons.io.IOUtils;
import ru.simpleGBD.App.Config.GBDOptions;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

/**
 * Created by sbt-morozov-kv on 02.12.2015.
 */
public class FileProxyListProvider extends AbstractProxyPistProvider {

    FileProxyListProvider() {
        buildList();
    }

    private void buildList() {
        String proxyListFileName = GBDOptions.getGBDOptions().getProxyListFile();
        if (proxyListFileName == null || proxyListFileName.length() == 0)
            return;

        File proxyListFile = new File(proxyListFileName);
        if (!proxyListFile.exists() && !proxyListFile.canRead())
            return;

        try (InputStream is = new FileInputStream(proxyListFile)) {
            List<String> lines = IOUtils.readLines(is, "UTF-8");
            buildFromList(lines);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
