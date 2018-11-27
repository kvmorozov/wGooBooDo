package ru.kmorozov.library.data.server.condition;

import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

public class LibraryEnabledCondition implements Condition {

    @Override
    public boolean matches(final ConditionContext conditionContext, final AnnotatedTypeMetadata annotatedTypeMetadata) {
        return Boolean.valueOf(conditionContext.getEnvironment().getProperty("rest.controllers.library.enabled"));
    }
}
