package cn.sheyifan.components

import com.jfoenix.controls.JFXButton
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.fxml.FXMLLoader
import javafx.scene.Scene
import javafx.scene.control.TextArea
import javafx.scene.paint.Color
import javafx.stage.Modality
import javafx.stage.Stage
import javafx.stage.StageStyle
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.stream.Collectors

class FxPDFsAlertDialogView(parentStage: Stage?, stageName: String?, contentPath: String?) : FxPDFsDialogView(parentStage!!, stageName!!, contentPath) {
    private var cancelButton: JFXButton? = null
    override fun loadComponents() {
        super.loadComponents()
        root = FXMLLoader.load(FxPDFsAlertDialogView::class.java.getResource("/cn/sheyifan/view/dialog/error.fxml"))
        continueButton = currentStage.scene.root.lookup("#continue-progress-button") as JFXButton
        cancelButton = currentStage.scene.root.lookup("#cancel-progress-button") as JFXButton
        dialogText = root!!.lookup("#alert-info") as TextArea
        val dialogTextReader = BufferedReader(InputStreamReader(FxPDFsAlertDialogView::class.java.getResourceAsStream(contentPath)))
        dialogText!!.text = dialogTextReader.lines().collect(Collectors.joining("\n"))
        dialogText!!.isEditable = false
        dialogTextReader.close()
    }

    override fun initStyle() {
        super.initStyle()
    }

    override fun registerEvents() {
        super.registerEvents()
        continueButton!!.onAction = EventHandler { _: ActionEvent? ->
            diagReturnValue = 0
            currentStage.close()
        }
        cancelButton!!.onAction = EventHandler { _: ActionEvent? ->
            diagReturnValue = 1
            currentStage.close()
        }
    }

    override fun initWindow() {
        super.initWindow()
        scene = Scene(root, root!!.width, root!!.height, Color.valueOf("#ffffff01"))
        currentStage.scene = scene
        currentStage.initStyle(StageStyle.TRANSPARENT)
        currentStage.initModality(Modality.APPLICATION_MODAL)
        if (parentStage != null) {
            currentStage.initOwner(parentStage)
        }
    }

    override fun show() {
        super.show()
    }
}