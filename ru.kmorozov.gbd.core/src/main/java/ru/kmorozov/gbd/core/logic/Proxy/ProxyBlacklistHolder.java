package ru.kmorozov.gbd.core.logic.Proxy;

import java.io.*;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.stream.Collectors;

/**
 * Created by km on 29.10.2016.
 */
public class ProxyBlacklistHolder {

    public static final ProxyBlacklistHolder BLACKLIST = new ProxyBlacklistHolder();
    private static final String BL_FILE_NAME = "black.lst";
    private static final int DEAD_PROXY_TREASHOLD = 5000;
    private File blacklistFile;
    private List<HttpHostExt> storedHosts = new CopyOnWriteArrayList<>();

    private ProxyBlacklistHolder() {
        init();
    }

    private void init() {
        blacklistFile = new File(System.getProperty("java.io.tmpdir") + File.separator + BL_FILE_NAME);
        if (!blacklistFile.exists()) try {
            blacklistFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Charset charset = Charset.forName("UTF-8");
        try (BufferedReader reader = Files.newBufferedReader(blacklistFile.toPath(), charset)) {
            String line;
            while ((line = reader.readLine()) != null) {
                storedHosts.add(HttpHostExt.getProxyFromString(line));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isProxyInBlacklist(final String proxyStr) {
        Optional<HttpHostExt> host = storedHosts.parallelStream().filter(httpHostExt -> httpHostExt.isSameAsStr(proxyStr)).findFirst();
        return host.filter(httpHostExt -> !httpHostExt.isAvailable()).isPresent();
    }

    public void updateBlacklist(Collection<HttpHostExt> currentProxyList) {
        for (HttpHostExt proxy : currentProxyList) {
            Optional<HttpHostExt> proxyInListOpt = storedHosts.parallelStream().filter(httpHostExt -> httpHostExt.equals(proxy)).findFirst();
            if (proxyInListOpt.isPresent()) {
                proxyInListOpt.get().update(proxy);
            }
            else {
                storedHosts.add(proxy);
            }
        }

        List<HttpHostExt> deadProxyList = storedHosts.parallelStream().filter(HttpHostExt::isNotAvailable).limit(DEAD_PROXY_TREASHOLD).collect(Collectors.toList());
        List<HttpHostExt> liveProxyList = storedHosts.parallelStream().filter(HttpHostExt::isAvailable).limit(DEAD_PROXY_TREASHOLD).collect(Collectors.toList());

        try (PrintWriter out = new PrintWriter(blacklistFile)) {
            for (HttpHostExt host : liveProxyList)
                out.write(host.getProxyString() + System.getProperty("line.separator"));
            for (HttpHostExt host : deadProxyList)
                out.write(host.getProxyString() + System.getProperty("line.separator"));

            out.flush();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public List<String> getWhiteList() {
        return storedHosts.parallelStream().filter(HttpHostExt::isAvailable).map(HttpHostExt::getProxyStringShort).collect(Collectors.toList());
    }
}
