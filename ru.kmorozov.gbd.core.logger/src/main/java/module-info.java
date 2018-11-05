module ru.kmorozov.gbd.core.logger {
    exports ru.kmorozov.gbd.logger.model;
    exports ru.kmorozov.gbd.logger;
    exports ru.kmorozov.gbd.logger.output;
    exports ru.kmorozov.gbd.logger.progress;
    exports ru.kmorozov.gbd.logger.consumers;
    exports ru.kmorozov.gbd.logger.events;

    requires java.desktop;
    requires java.logging;

}