package cn.sheyifan

import cn.sheyifan.components.FxPDFsProgressView
import cn.sheyifan.components.FxPDFsSearchContentPDFsProgressView
import javafx.application.Application
import javafx.stage.Stage

class FxPDFsProgressBarMain : Application() {
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
        FxPDFsSearchContentPDFsProgressView(primaryStage).show()
    }

    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            launch(*args)
        }
    }
}