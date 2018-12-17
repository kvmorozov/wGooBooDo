package ru.kmorozov.gbd.desktop.gui

import javax.swing.*
import java.awt.*

/**
 * Created by km on 05.12.2015.
 */
class MainFrame : JFrame("wGooBooDo") {
    init {
        instance = this

        defaultCloseOperation = WindowConstants.EXIT_ON_CLOSE

        contentPane = MainBookForm().mainPanel
        pack()

        val dimension = Toolkit.getDefaultToolkit().screenSize
        val x = ((dimension.getWidth() - width.toDouble()) / 2.0).toInt()
        val y = ((dimension.getHeight() - height.toDouble()) / 2.0).toInt()
        setLocation(x, y)
    }

    fun setVisible() {
        isVisible = true
    }

    companion object {

        lateinit var instance: MainFrame
    }
}
