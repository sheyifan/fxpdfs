package cn.sheyifan

import cn.sheyifan.components.FxPDFsPrimaryView
import cn.sheyifan.components.primaryView
import javafx.application.Application
import javafx.stage.Stage
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.*

fun main(args: Array<String>) {
    Application.launch(FxPDFsMain("placeholder")::class.java, *args)
}

class FxPDFsMain : Application {
    /**
     * The main entry point for all JavaFX applications.
     * The start method is called after the init method has returned,
     * and after the system is ready for the application to begin running.
     *
     *
     *
     * NOTE: This method is called on the JavaFX Application Thread.
     *
     *
     * @param primaryStage the primary stage for this application, onto which
     * the application scene can be set.
     * Applications may create other stages, if needed, but they will not be
     * primary stages.
     * @throws Exception if something goes wrong
     */
    @Throws(Exception::class)
    override fun start(primaryStage: Stage) {
        System.setProperty("prism.lcdtext", "false");
        primaryView = FxPDFsPrimaryView(primaryStage, "FxPDFs primary window")
        primaryView.show()
    }

    constructor() : super()
    constructor(tString: String): super() {
        println("Load main constructor by ${tString}")
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            launch(FxPDFsMain::class.java)
        }
    }
}

enum class Theme {
    DARK, LIGHT
}

fun getTheme(): Theme {
    val file = File("userdata.properties")
    if (!file.exists()) {
        file.createNewFile()
    }
    val pro = Properties()
    val proIn = FileInputStream(file)
    pro.load(proIn)

    if (pro.getProperty("theme") != null) {
        return Theme.valueOf(pro.getProperty("theme"))
    }

    pro.setProperty("theme", "LIGHT")
    val proOut = FileOutputStream(file)
    pro.store(proOut, "utf-8")

    proIn.close()
    proOut.close()

    return Theme.LIGHT
}