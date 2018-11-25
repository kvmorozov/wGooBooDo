package ru.kmorozov.library.data.server.condition;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class StorageEnabledCondition implements Condition {

    @Override
    public boolean matches(ConditionContext conditionContext, AnnotatedTypeMetadata annotatedTypeMetadata) {
        return Boolean.valueOf(conditionContext.getEnvironment().getProperty("rest.controllers.storage.enabled"));
    }
}
