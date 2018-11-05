module ru.kmorozov.gbd.core {
    requires java.net.http;
    requires com.google.api.client;
    requires org.apache.commons.io;
    requires org.jsoup;
    requires org.apache.commons.lang3;

    requires ru.kmorozov.gbd.db;
    requires ru.kmorozov.gbd.core.config;
    requires ru.kmorozov.gbd.core.logger;
    requires httpcore5;
    requires com.google.common;
    requires java.desktop;
    requires gson;
}