module ru.kmorozov.gbd.core.config {
    requires org.apache.commons.lang3;
    requires java.prefs;

    exports ru.kmorozov.gbd.core.config;
    exports ru.kmorozov.gbd.core.logic.model.book.base;
    exports ru.kmorozov.gbd.core.config.constants;
    exports ru.kmorozov.gbd.core.config.options;
}