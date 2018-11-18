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
public final class ProxyBlacklistHolder {

    public static final ProxyBlacklistHolder BLACKLIST = new ProxyBlacklistHolder();
    private static final String BL_FILE_NAME = "black.lst";
    private static final int DEAD_PROXY_TREASHOLD = 5000;
    private File blacklistFile;
    private final Collection<HttpHostExt> storedHosts = new CopyOnWriteArrayList<>();

    private ProxyBlacklistHolder() {
        init();
    }

    private void init() {
        blacklistFile = new File(System.getProperty("java.io.tmpdir") + File.separator + BL_FILE_NAME);
        if (!blacklistFile.exists()) try {
            blacklistFile.createNewFile();
        } catch (final IOException e) {
            e.printStackTrace();
        }

        final Charset charset = Charset.forName("UTF-8");
        try (BufferedReader reader = Files.newBufferedReader(blacklistFile.toPath(), charset)) {
            String line;
            while (null != (line = reader.readLine())) {
                storedHosts.add(HttpHostExt.getProxyFromString(line));
            }
        } catch (final IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isProxyInBlacklist(final String proxyStr) {
        final Optional<HttpHostExt> host = storedHosts.parallelStream().filter(httpHostExt -> httpHostExt.isSameAsStr(proxyStr)).findFirst();
        return host.filter(httpHostExt -> !httpHostExt.isAvailable()).isPresent();
    }

    public void updateBlacklist(final Iterable<HttpHostExt> currentProxyList) {
        for (final HttpHostExt proxy : currentProxyList) {
            final Optional<HttpHostExt> proxyInListOpt = storedHosts.parallelStream().filter(httpHostExt -> httpHostExt.equals(proxy)).findFirst();
            if (proxyInListOpt.isPresent()) {
                proxyInListOpt.get().update(proxy);
            }
            else {
                storedHosts.add(proxy);
            }
        }

        final List<HttpHostExt> deadProxyList = storedHosts.parallelStream().filter(HttpHostExt::isNotAvailable).limit((long) DEAD_PROXY_TREASHOLD).collect(Collectors.toList());
        final List<HttpHostExt> liveProxyList = storedHosts.parallelStream().filter(HttpHostExt::isAvailable).limit((long) DEAD_PROXY_TREASHOLD).collect(Collectors.toList());

        try (PrintWriter out = new PrintWriter(blacklistFile)) {
            for (final HttpHostExt host : liveProxyList)
                out.write(host.getProxyString() + System.getProperty("line.separator"));
            for (final HttpHostExt host : deadProxyList)
                out.write(host.getProxyString() + System.getProperty("line.separator"));

            out.flush();
        } catch (final FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public Collection<String> getWhiteList() {
        return storedHosts.parallelStream().filter(HttpHostExt::isAvailable).map(HttpHostExt::getProxyStringShort).collect(Collectors.toList());
    }
}
