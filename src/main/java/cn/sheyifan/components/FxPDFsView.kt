package cn.sheyifan.components

import cn.sheyifan.Theme
import cn.sheyifan.getTheme
import javafx.beans.value.ObservableValue
import javafx.scene.Scene
import javafx.scene.layout.Pane
import javafx.scene.layout.Region
import javafx.stage.Stage
import org.apache.log4j.Logger

abstract class FxPDFsView : FxPDFsAbstractView {
    protected var logger = Logger.getLogger(this.javaClass)
    var parentStage: Stage? = null
    var currentStage: Stage
    protected var scene: Scene? = null
    // 根节点的容器，用于显示阴影
    var root: Pane? = null
    // 根节点
    protected var content: Pane? = null
    protected var stageName = "FxPDFs view"

    constructor() {
        currentStage = Stage()
    }

    constructor(parentStage: Stage?) {
        this.parentStage = parentStage
        currentStage = Stage()
    }

    constructor(parentStage: Stage?, stageName: String?) : this(parentStage) {
        if (stageName != null) {
            this.stageName = stageName
        }
    }

    // 加载组件对象
    protected abstract fun loadComponents()

    // 设置组件样式
    protected open fun initStyle() {
        loadTheme()
    }

    // 注册组件的事件
    protected abstract fun registerEvents()

    // 加载窗口对象、设置窗口样式、注册窗口事件
    protected open fun initWindow() {
        currentStage.showingProperty().addListener { _: ObservableValue<out Boolean>?, _: Boolean?, newValue: Boolean ->
            if (newValue) {
                logger.info("<$stageName> shows.")
            } else {
                logger.info("<$stageName> closes.")
            }
        }
    }

    override fun show() {
        loadComponents()
        initStyle()
        registerEvents()
        initWindow()
        currentStage.show()
    }

    override fun close() {
        currentStage.close()
    }

    protected fun alignCenter() {
        val content = root!!.childrenUnmodifiable[0] as Region
        val contentWidth = content.prefWidth + 20
        val contentHeight = content.prefHeight + 20
        currentStage.width = contentWidth
        currentStage.height = contentHeight
        val xBias = (parentStage!!.width - currentStage.width) / 2
        val yBias = (parentStage!!.height - currentStage.height) / 2
        currentStage.x = parentStage!!.x + xBias
        currentStage.y = parentStage!!.y + yBias
    }

    private fun loadTheme() {
        root!!.stylesheets.filter {
            it.contains("/cn/sheyifan/view/style/light_theme/") || it.contains("/cn/sheyifan/view/style/dark_theme")
        }.forEach {
            root!!.stylesheets.remove(it)
        }

        when (getTheme()) {
            Theme.DARK -> {
                root!!.stylesheets.addAll(
                        FxPDFsPrimaryView::class.java.getResource("/cn/sheyifan/view/style/dark_theme/container.css").toExternalForm(),
                        FxPDFsPrimaryView::class.java.getResource("/cn/sheyifan/view/style/dark_theme/input.css").toExternalForm(),
                        FxPDFsPrimaryView::class.java.getResource("/cn/sheyifan/view/style/dark_theme/text.css").toExternalForm(),
                        FxPDFsPrimaryView::class.java.getResource("/cn/sheyifan/view/style/dark_theme/icons.css").toExternalForm(),
                        FxPDFsPrimaryView::class.java.getResource("/cn/sheyifan/view/style/dark_theme/table.css").toExternalForm())
            }

            else -> {
                root!!.stylesheets.addAll(
                        FxPDFsPrimaryView::class.java.getResource("/cn/sheyifan/view/style/light_theme/container.css").toExternalForm(),
                        FxPDFsPrimaryView::class.java.getResource("/cn/sheyifan/view/style/light_theme/input.css").toExternalForm(),
                        FxPDFsPrimaryView::class.java.getResource("/cn/sheyifan/view/style/light_theme/text.css").toExternalForm(),
                        FxPDFsPrimaryView::class.java.getResource("/cn/sheyifan/view/style/light_theme/icons.css").toExternalForm(),
                        FxPDFsPrimaryView::class.java.getResource("/cn/sheyifan/view/style/light_theme/table.css").toExternalForm())
            }
        }
    }
}