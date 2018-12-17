package ru.kmorozov.library.data.server.condition

import org.springframework.context.annotation.Condition
import org.springframework.context.annotation.ConditionContext
import org.springframework.core.type.AnnotatedTypeMetadata

class StorageEnabledCondition : Condition {

    override fun matches(conditionContext: ConditionContext, annotatedTypeMetadata: AnnotatedTypeMetadata): Boolean {
        return java.lang.Boolean.valueOf(conditionContext.environment.getProperty("rest.controllers.storage.enabled"))
    }
}
