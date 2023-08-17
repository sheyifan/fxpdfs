package cn.sheyifan.components

import cn.sheyifan.utils.DraggableGroup
import cn.sheyifan.utils.FxPDFsAPP
import cn.sheyifan.utils.file.SProperties
import cn.sheyifan.utils.nativecall.NativeAPPs
import com.jfoenix.controls.JFXButton
import com.jfoenix.controls.JFXCheckBox
import com.jfoenix.controls.JFXChipView
import javafx.collections.ListChangeListener
import javafx.event.ActionEvent
import javafx.event.EventHandler
import javafx.fxml.FXMLLoader
import javafx.scene.Node
import javafx.scene.Scene
import javafx.scene.control.Label
import javafx.scene.control.TextField
import javafx.scene.image.Image
import javafx.scene.input.MouseEvent
import javafx.scene.layout.HBox
import javafx.scene.layout.Pane
import javafx.scene.paint.Color
import javafx.scene.text.Font
import javafx.stage.DirectoryChooser
import javafx.stage.Stage
import javafx.stage.StageStyle
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.net.URL
import java.nio.file.Files
import java.nio.file.Paths
import java.util.*

var fileTypeList: Array<String>? = null
var ZH_FONT: Font = Font.font("Microsoft YaHei")

lateinit var primaryView: FxPDFsPrimaryView

class FxPDFsPrimaryView : FxPDFsView {
    private lateinit var primaryViewResourceBundle: ResourceBundle
    // 文件类型选择框
    private var filetypesBox: JFXChipView<String>? = null
    private var pdfUrlBox: TextField? = null
    private var closeAppButton: Pane? = null
    private var minimizeAppButton: Pane? = null
    private var openSettingsButton: Pane? = null
    private var startRetrieveButton: JFXButton? = null
    private var filetypesAlertBox: HBox? = null
    private var pdfUrlAlertBox: HBox? = null
    private var alertInfos: Set<Node>? = null
    private var feedbackLink: Label? = null
    private var dirIcon: Label? = null
    private var dirChooser: DirectoryChooser? = null
    private var webIcon: Label? = null

    constructor(parentStage: Stage?) : super(parentStage) {}
    constructor(parentStage: Stage?, stageName: String?) : super(parentStage, stageName) {}

    override fun loadComponents() {
        root = FXMLLoader.load(FxPDFsPrimaryView::class.java.getResource("/cn/sheyifan/view/main.fxml"))
        content = root!!.children[0] as Pane
//        filetypesBox = JFXChipView()
//        filetypesBox!!.autoCompletePopup.suggestions.addAll("PDF", "Word", "Excel", "PPT")
//        filetypesBox!!.layoutX = 70.0
//        filetypesBox!!.layoutY = 235.0
//        filetypesBox!!.chips.addAll("PDF", "Word", "Excel", "PPT")
        filetypesBox?.let { content!!.children.add(it) }
        closeAppButton = root!!.lookup("#fxpdfs-app-close-icon-box") as Pane
        minimizeAppButton = root!!.lookup("#fxpdfs-app-minimize-icon-box") as Pane
        openSettingsButton = root!!.lookup("#titlebar-settings-item-box") as Pane
        startRetrieveButton = root!!.lookup("#start-retrieve") as JFXButton
        alertInfos = root!!.lookupAll(".alert-info")
        feedbackLink = root!!.lookup("#feedback-link") as Label
        pdfUrlBox = root!!.lookup("#pdf-url-box") as TextField
        filetypesAlertBox = root!!.lookup("#filetypes-alert-box") as HBox
        pdfUrlAlertBox = root!!.lookup("#url-alert-box") as HBox
        dirIcon = root!!.lookup("#dir-icon") as Label
        dirChooser = DirectoryChooser()
        dirChooser!!.title = "Select directory"
        webIcon = root!!.lookup("#web-icon") as Label
    }

    override fun initStyle() {
        super.initStyle()

        for (alertInfo in alertInfos!!) {
            alertInfo.isVisible = false
        }
        pdfUrlBox!!.isEditable = false

        if (SProperties.readProperty("/cn/sheyifan/fxpdfs_profile.properties", "lang") == "ZH") {
            primaryViewResourceBundle = ResourceBundle.getBundle("cn/sheyifan/lang/primaryview", Locale.CHINA)
        }
        else if (SProperties.readProperty("/cn/sheyifan/fxpdfs_profile.properties", "lang") == "EN") {
            primaryViewResourceBundle = ResourceBundle.getBundle("cn/sheyifan/lang/primaryview", Locale.US)
        }
        else {
            logger.error("Unrecognized region")
            return
        }

        val title = root!!.lookup("#title") as Label
        title.text = primaryViewResourceBundle.getString("titlebar.appdesc.short")
        title.style = "-fx-font-family: ${primaryViewResourceBundle.getString("main.font")}"
        val titleSign = root!!.lookup("#title-sign") as Label
        titleSign.text = primaryViewResourceBundle.getString("titlebar.appdesc.long")
        titleSign.style = "-fx-font-family: ${primaryViewResourceBundle.getString("main.font")}"
        val bodySep = root!!.lookup("#edit-condition-sign") as Label
        bodySep.text = primaryViewResourceBundle.getString("body.topbar")
        bodySep.style = "-fx-font-family: ${primaryViewResourceBundle.getString("main.font")}"
        val fileTypesSign = root!!.lookup("#filetypes-sign") as Label
        fileTypesSign.text = primaryViewResourceBundle.getString("body.filetypes.desc")
        fileTypesSign.style = "-fx-font-family: ${primaryViewResourceBundle.getString("main.font")}"
        val fileTypesAlertSign = root!!.lookup("#filetypes-alert-sign") as Label
        fileTypesAlertSign.text = primaryViewResourceBundle.getString("body.filetypes.alert")
        fileTypesAlertSign.style = "-fx-font-family: ${primaryViewResourceBundle.getString("main.font")}"
        val pdfSrcSign = root!!.lookup("#pdf-dir-sign") as Label
        pdfSrcSign.text = primaryViewResourceBundle.getString("body.pdfsrc.desc")
        pdfSrcSign.style = "-fx-font-family: ${primaryViewResourceBundle.getString("main.font")}"
        val pdfSrcAlert = root!!.lookup("#url-alert-sign") as Label
        pdfSrcAlert.text = primaryViewResourceBundle.getString("body.pdfsrc.alert")
        pdfSrcAlert.style = "-fx-font-family: ${primaryViewResourceBundle.getString("main.font")}"
        val webLink = root!!.lookup("#find-web-resource-link") as Label
        webLink.text = primaryViewResourceBundle.getString("body.pdfsrc.web.link")
        webLink.style = "-fx-font-family: ${primaryViewResourceBundle.getString("main.font")}"
        startRetrieveButton!!.text = primaryViewResourceBundle.getString("body.submit")
        startRetrieveButton!!.style = "-fx-font-family: ${primaryViewResourceBundle.getString("main.font")}"
        val historySign = root!!.lookup("#check-history-prompt") as Label
        historySign.text = primaryViewResourceBundle.getString("body.history.desc")
        historySign.style = "-fx-font-family: ${primaryViewResourceBundle.getString("main.font")}"
        val checkHistoryLink = root!!.lookup("#check-history-link") as Label
        checkHistoryLink.text = primaryViewResourceBundle.getString("body.history.link")
        checkHistoryLink.style = "-fx-font-family: ${primaryViewResourceBundle.getString("main.font")}"
        val feedbackDesc = root!!.lookup("#feedback-prompt") as Label
        feedbackDesc.text = primaryViewResourceBundle.getString("tail.feedback.desc")
        feedbackDesc.style = "-fx-font-family: ${primaryViewResourceBundle.getString("main.font")}"
        val feedbackLink = root!!.lookup("#feedback-link") as Label
        feedbackLink.text = primaryViewResourceBundle.getString("tail.feedback.link")
        feedbackLink.style = "-fx-font-family: ${primaryViewResourceBundle.getString("main.font")}"
    }


    override fun registerEvents() {
        closeAppButton!!.addEventHandler(MouseEvent.MOUSE_CLICKED) { currentStage.close() }
        minimizeAppButton!!.addEventHandler(MouseEvent.MOUSE_CLICKED) { currentStage.isIconified = true }
        openSettingsButton!!.addEventHandler(MouseEvent.MOUSE_CLICKED) { FxPDFsSettingsView(currentStage, "Settings window").show() }
        feedbackLink!!.addEventHandler(MouseEvent.MOUSE_PRESSED) { NativeAPPs.sendMail() }
        filetypesBox?.chips?.addListener { c: ListChangeListener.Change<out String> ->
            fun validateFileType(type: String): Boolean {
                return when (type) {
                    "PDF" -> true
                    "Excel" -> true
                    "PPT" -> true
                    "Word" -> true
                    else -> false
                }
            }

            fun contains(item: String): Boolean {
                filetypesBox?.chips?.forEach {
                    if (it == item) {
                        return true
                    }
                }

                return false
            }

            c.next()
            if (c.wasAdded()) {
                val added = c.addedSubList[0]
                if (filetypesBox!!.chips.slice(0 until filetypesBox!!.chips!!.size - 1).contains(added)
                        || !validateFileType(added)) {
                    filetypesBox?.chips?.removeAt(filetypesBox?.chips?.size?.minus(1)!!)
                }
            }
        }

        startRetrieveButton!!.onAction = EventHandler { _: ActionEvent? ->
            if (!FxPDFsAPP.checkLicense()) {
                val fxInfoView = FxPDFsInfoDialogView(currentStage, "License info dialog", "/cn/sheyifan/view/dialog/license_status")
                fxInfoView.show()
                if (fxInfoView.diagReturnValue == 0) {
                }
            } else {
                if (checkForm()) {
                    val fileItems = root!!.lookupAll(".file-type-item-checkbox").toTypedArray() as Array<*>
                    fileTypeList = fileItems.map {
                        if (it is JFXCheckBox) {
                            if (it.isSelected) {
                                it.text
                            }
                            else {
                                ""
                            }
                        }
                        else {
                            ""
                        }
                    }.filter { it != "" }.toTypedArray()

                    val progressView = FxPDFsListFilesProgressView(currentStage,
                            "Progress dialog for counting files")
                    progressView.show()
                }
            }
        }
        dirIcon!!.addEventHandler(MouseEvent.MOUSE_PRESSED) { _: MouseEvent? ->
            val file = File("userdata.properties")
            if (!file.exists()) {
                file.createNewFile()
            }
            val pro = Properties()
            val proIn = FileInputStream(file)
            pro.load(proIn)

            if (pro.getProperty("pdf_dir") != null && Files.exists(Paths.get(pro.getProperty("pdf_dir")))) {
                dirChooser!!.initialDirectory = File(pro.getProperty("pdf_dir"))
            }
            val dir = dirChooser!!.showDialog(currentStage) ?: return@addEventHandler

            pro.setProperty("pdf_dir", dir.path)
            val proOut = FileOutputStream(file)
            pro.store(proOut, "utf-8")

            proIn.close()
            proOut.close()
            pdfUrlBox!!.text = dir.toURI().toURL().toExternalForm()
        }
        webIcon!!.addEventHandler(MouseEvent.MOUSE_PRESSED) {

        }
    }

    override fun initWindow() {
        super.initWindow()
        scene = Scene(root, root!!.width, root!!.height, Color.valueOf("#ffffff01"))
        currentStage.scene = scene
        currentStage.width = content!!.prefWidth + 20
        currentStage.height = content!!.prefHeight + 20
        currentStage.icons.add(Image(javaClass.getResource("/cn/sheyifan/view/images/fxpdfs_icon.png")!!.toExternalForm()))
        currentStage.isResizable = false
        currentStage.initStyle(StageStyle.TRANSPARENT)
        DraggableGroup(root!!, currentStage).setDraggable(true)
    }

    override fun show() {
        super.show()
    }

    private fun checkForm(): Boolean {
        var filetypesIntegrity: Boolean
        val selected = (root!!.lookupAll(".file-type-item-checkbox").toTypedArray() as Array<*>).map {
            if (it is JFXCheckBox) {
                it.isSelected
            } else {
                false
            }
        }.groupBy { it }
        filetypesIntegrity = !(selected.size == 1 && selected.containsKey(false))
        val pdfUrlIntegrity = pdfUrlBox!!.text.isNotEmpty()
        filetypesAlertBox!!.isVisible = !filetypesIntegrity
        pdfUrlAlertBox!!.isVisible = !pdfUrlIntegrity
        return filetypesIntegrity && pdfUrlIntegrity
    }

    companion object {
        // Flag to judge: if app needs
        @JvmField
        var lastRetrieveURL: URL? = null
        @JvmField
        var lastFileTypes: Array<String>? = null
    }
}

fun main() {
    val ss = Font.loadFont(
            File("C:\\workdir\\projects\\sheyifan\\fxpdf1.0\\src\\main\\resources\\view\\font_icon\\fonts\\No.83-ShangShouCuKaiTi-2.ttf").toURI().toURL().toString(), 12.0)
    println(ss.name)
}