package ru.kmorozov.library.data.server.options

import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.stereotype.Component

@Component
@Qualifier("inMemory")
class InMemoryOptions : AbstractServerGBDOptions()