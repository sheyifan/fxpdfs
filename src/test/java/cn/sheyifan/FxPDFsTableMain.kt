package cn.sheyifan

import cn.sheyifan.components.FxPDFsSearchResultView
import com.jfoenix.controls.JFXButton
import javafx.application.Application
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.fxml.FXMLLoader
import javafx.scene.Scene
import javafx.scene.layout.Pane
import javafx.stage.Stage

fun main(args: Array<String>) {
    Application.launch(FxPDFsTableMain::class.java, *args)
}

class FxPDFsTableMain: Application() {
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
     * @throws java.lang.Exception if something goes wrong
     */
    override fun start(primaryStage: Stage?) {
        FxPDFsSearchResultView(primaryStage!!, "").show()
    }
}