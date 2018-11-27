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
        this.init();
    }

    private void init() {
        this.blacklistFile = new File(System.getProperty("java.io.tmpdir") + File.separator + ProxyBlacklistHolder.BL_FILE_NAME);
        if (!this.blacklistFile.exists()) try {
            this.blacklistFile.createNewFile();
        } catch (IOException e) {
            e.printStackTrace();
        }

        Charset charset = Charset.forName("UTF-8");
        try (final BufferedReader reader = Files.newBufferedReader(this.blacklistFile.toPath(), charset)) {
            String line;
            while (null != (line = reader.readLine())) {
                this.storedHosts.add(HttpHostExt.getProxyFromString(line));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean isProxyInBlacklist(String proxyStr) {
        Optional<HttpHostExt> host = this.storedHosts.parallelStream().filter(httpHostExt -> httpHostExt.isSameAsStr(proxyStr)).findFirst();
        return host.filter(httpHostExt -> !httpHostExt.isAvailable()).isPresent();
    }

    public void updateBlacklist(Iterable<HttpHostExt> currentProxyList) {
        for (HttpHostExt proxy : currentProxyList) {
            Optional<HttpHostExt> proxyInListOpt = this.storedHosts.parallelStream().filter(httpHostExt -> httpHostExt.equals(proxy)).findFirst();
            if (proxyInListOpt.isPresent()) {
                proxyInListOpt.get().update(proxy);
            }
            else {
                this.storedHosts.add(proxy);
            }
        }

        List<HttpHostExt> deadProxyList = this.storedHosts.parallelStream().filter(HttpHostExt::isNotAvailable).limit((long) ProxyBlacklistHolder.DEAD_PROXY_TREASHOLD).collect(Collectors.toList());
        List<HttpHostExt> liveProxyList = this.storedHosts.parallelStream().filter(HttpHostExt::isAvailable).limit((long) ProxyBlacklistHolder.DEAD_PROXY_TREASHOLD).collect(Collectors.toList());

        try (final PrintWriter out = new PrintWriter(this.blacklistFile)) {
            for (HttpHostExt host : liveProxyList)
                out.write(host.getProxyString() + System.getProperty("line.separator"));
            for (HttpHostExt host : deadProxyList)
                out.write(host.getProxyString() + System.getProperty("line.separator"));

            out.flush();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    public Collection<String> getWhiteList() {
        return this.storedHosts.parallelStream().filter(HttpHostExt::isAvailable).map(HttpHostExt::getProxyStringShort).collect(Collectors.toList());
    }
}
