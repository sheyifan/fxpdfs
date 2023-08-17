package cn.sheyifan.components

import cn.sheyifan.utils.DraggableGroup
import com.jfoenix.controls.JFXButton
import com.jfoenix.controls.JFXProgressBar
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.fxml.FXMLLoader
import javafx.scene.Scene
import javafx.scene.control.Label
import javafx.scene.input.MouseEvent
import javafx.scene.layout.Pane
import javafx.scene.paint.Color
import javafx.stage.Modality
import javafx.stage.Stage
import javafx.stage.StageStyle
import javafx.stage.Window
import java.util.concurrent.atomic.AtomicBoolean

public abstract class FxPDFsProgressView : FxPDFsView {
    @JvmField
    var continueFlag: AtomicBoolean = AtomicBoolean(true)
    protected var startProgressThread: Thread? = null
    private var cancelButton: JFXButton? = null
    @JvmField
    protected var progressBar: JFXProgressBar? = null
    @JvmField
    protected var progressValue: Label? = null
    @JvmField
    protected var progressDesc: Label? = null
    @JvmField
    protected var minimizeButton: Pane? = null

    constructor(parentStage: Stage?) : super(parentStage) {}
    constructor(parentStage: Stage?, stageName: String?) : super(parentStage, stageName) {}

    override fun loadComponents() {
        root = FXMLLoader.load(FxPDFsProgressView::class.java.getResource("/cn/sheyifan/view/progress.fxml"))
        progressBar = root!!.lookup("#indexing-progress-bar") as JFXProgressBar
        progressBar!!.progress = JFXProgressBar.INDETERMINATE_PROGRESS
        progressValue = root!!.lookup("#progress-value") as Label
        progressDesc = root!!.lookup("#progress-desc") as Label
        cancelButton = root!!.lookup("#cancel-progress-button") as JFXButton
        minimizeButton = root!!.lookup("#fxpdfs-app-minimize-icon-box") as Pane
    }

    override fun initStyle() {
        super.initStyle()
    }
    override fun registerEvents() {
        cancelButton!!.onAction = EventHandler { _: ActionEvent? ->
            currentStage.close()
            continueFlag.set(false)
            logger.info("User interrupts progress by clicking cancel button.")
        }

        minimizeButton!!.addEventHandler(MouseEvent.MOUSE_PRESSED) {
            Window.getWindows().forEach {
                if (it is Stage) {
                    if (it.owner == null) {
                        it.isIconified = true
                    }
                }
            }
        }
    }

    override fun initWindow() {
        super.initWindow()
        scene = Scene(root, root!!.width, root!!.height, Color.valueOf("#ffffff01"))
        currentStage.scene = scene
        currentStage.initStyle(StageStyle.TRANSPARENT)
        currentStage.initModality(Modality.APPLICATION_MODAL)
        currentStage.initOwner(parentStage)
        val draggableProgress = parentStage?.let { root?.let { it1 -> DraggableGroup(it1, currentStage, it) } }
        currentStage.xProperty().addListener { _, _, newValue ->
            if (newValue.toDouble() < parentStage!!.x) {
                currentStage.x = parentStage!!.x
            }
        }
        draggableProgress!!.setDraggable(true)
    }

    override fun show() {
        super.show()
        alignCenter()
    }

    fun startProgress(startProgressThread: Thread?) {
        this.startProgressThread = startProgressThread
        this.startProgressThread!!.isDaemon = true
        this.startProgressThread!!.start()
    }
}