module ru.kmorozov.gbd.core {
    requires java.net.http;
    requires com.google.api.client;
    requires org.jsoup;
    requires org.apache.commons.lang3;

    requires ru.kmorozov.gbd.db;
    requires ru.kmorozov.gbd.core.config;
    requires ru.kmorozov.gbd.core.logger;
    requires httpcore5;
    requires com.google.common;
    requires java.desktop;
    requires gson;
    requires httpclient5;
    requires async.http.client;
    requires io.netty.codec.http;
    requires io.netty.common;
    requires io.netty.transport;
    requires org.apache.commons.io;
    requires io.netty.handler;
    requires okhttp3;
    requires selenium.api;
    requires selenium.chrome.driver;
    requires selenium.support;
}