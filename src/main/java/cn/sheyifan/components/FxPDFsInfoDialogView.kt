package cn.sheyifan.components

import cn.sheyifan.utils.DraggableGroup
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

class FxPDFsInfoDialogView(parentStage: Stage?, stageName: String?, contentPath: String?) : FxPDFsDialogView(parentStage, stageName, contentPath) {
    override fun loadComponents() {
        super.loadComponents()
        root = FXMLLoader.load(FxPDFsInfoDialogView::class.java.getResource("/cn/sheyifan/view/dialog/info.fxml"))
        continueButton = root!!.lookup("#continue-progress-button") as JFXButton
        dialogText = root!!.lookup("#info-info") as TextArea
        val errorReader = BufferedReader(InputStreamReader(FxPDFsInfoDialogView::class.java.getResourceAsStream(contentPath)))
        dialogText!!.text = errorReader.lines().collect(Collectors.joining("\n"))
        errorReader.close()
    }

    override fun initStyle() {
        super.initStyle()
        dialogText!!.isEditable = false
    }

    override fun registerEvents() {
        super.registerEvents()
        continueButton!!.onAction = EventHandler { _: ActionEvent? ->
            diagReturnValue = 0
            currentStage.close()
        }
    }

    override fun initWindow() {
        super.initWindow()
        currentStage.initStyle(StageStyle.TRANSPARENT)
        currentStage.initModality(Modality.APPLICATION_MODAL)
        if (parentStage != null) {
            currentStage.initOwner(parentStage)
        }
        scene = Scene(root, root!!.width, root!!.height, Color.valueOf("#ffffff01"))
        currentStage.scene = scene
        val draggableInfoDialog = parentStage?.let { DraggableGroup(root!!, currentStage, it) }
        draggableInfoDialog!!.setDraggable(true)
    }
}