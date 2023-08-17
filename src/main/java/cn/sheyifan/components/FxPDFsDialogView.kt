package cn.sheyifan.components

import com.jfoenix.controls.JFXButton
import javafx.scene.control.TextArea
import javafx.stage.Stage

abstract class FxPDFsDialogView : FxPDFsView {
    var diagReturnValue: Int
        protected set
    @JvmField
    protected var dialogText: TextArea? = null
    protected var contentPath: String? = null
    @JvmField
    protected var continueButton: JFXButton? = null

    private constructor(parentStage: Stage) : super(parentStage) {
        diagReturnValue = -1
    }

    private constructor(parentStage: Stage?, stageName: String?) : super(parentStage, stageName) {
        diagReturnValue = -1
    }

    protected constructor(parentStage: Stage?, stageName: String?, contentPath: String?) : this(parentStage, stageName) {
        this.contentPath = contentPath
    }

    override fun loadComponents() {}
    override fun initStyle() {}
    override fun registerEvents() {}
    override fun initWindow() {
        super.initWindow()
        if (parentStage != null) {
            alignCenter()
        }
    }

    override fun show() {
        loadComponents()
        initStyle()
        registerEvents()
        initWindow()
        currentStage.showAndWait()
    }

}