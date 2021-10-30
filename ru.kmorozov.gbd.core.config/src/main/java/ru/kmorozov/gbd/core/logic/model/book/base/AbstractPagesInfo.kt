package ru.kmorozov.gbd.core.logic.model.book.base

import java.util.*

abstract class AbstractPagesInfo : IPagesInfo {

    override fun getPageByPid(pid: String): IPage {
        val opPage = Arrays.asList(*pages).stream().filter { page -> page.pid.equals(pid) }.findFirst()
        if (opPage.isPresent)
            return opPage.get()
        else
            throw PageNotFoundException()
    }
}