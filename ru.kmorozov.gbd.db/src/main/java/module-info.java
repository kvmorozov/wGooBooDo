module ru.kmorozov.gbd.db {
    exports ru.kmorozov.db.core.logic.model.book.rfbr;
    exports ru.kmorozov.db.core.logic.model.book;
    exports ru.kmorozov.db.core.config;
    exports ru.kmorozov.db.utils;
    exports ru.kmorozov.db.core.logic.model.book.google;
    exports ru.kmorozov.db.core.logic.model.book.shpl;

    requires gson;
    requires org.apache.commons.lang3;

    requires ru.kmorozov.gbd.core.config;
    requires ru.kmorozov.gbd.core.logger;
}