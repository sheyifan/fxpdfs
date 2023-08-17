package cn.sheyifan.components

import cn.sheyifan.utils.DraggableGroup
import cn.sheyifan.utils.FxBackgroundTask
import cn.sheyifan.utils.SPDF
import cn.sheyifan.utils.file.OfficeFileType
import cn.sheyifan.utils.file.SProperties
import com.jfoenix.controls.*
import com.jfoenix.controls.cells.editors.base.JFXTreeTableCell
import com.jfoenix.controls.datamodels.treetable.RecursiveTreeObject
import javafx.application.Platform
import javafx.beans.property.IntegerProperty
import javafx.beans.property.SimpleIntegerProperty
import javafx.beans.property.SimpleStringProperty
import javafx.beans.property.StringProperty
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import javafx.event.EventHandler
import javafx.fxml.FXMLLoader
import javafx.scene.Cursor
import javafx.scene.Scene
import javafx.scene.control.Label
import javafx.scene.control.Tooltip
import javafx.scene.control.TreeTableColumn
import javafx.scene.image.Image
import javafx.scene.input.MouseEvent
import javafx.scene.layout.Pane
import javafx.scene.paint.Color
import javafx.stage.Modality
import javafx.stage.Stage
import javafx.stage.StageStyle
import javafx.stage.Window
import javafx.util.Callback
import javafx.util.Duration
import org.apache.log4j.Logger
import java.awt.Desktop
import java.io.File
import java.net.URL
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.text.DecimalFormat
import java.util.*
import kotlin.collections.HashMap

class FxPDFsSearchResultView : FxPDFsView {
    private lateinit var searchResultResourceBundle: ResourceBundle

    private var files: HashMap<Path, OfficeFileType>? = null
    private var treeTableView: JFXTreeTableView<SearchItem>? = null
    private var idColumn: JFXTreeTableColumn<SearchItem, String>? = null
    private var matchesColumn: JFXTreeTableColumn<SearchItem, Int>? = null
    private var pathColumn: JFXTreeTableColumn<SearchItem, String>? = null
    // 搜索出的所有PDF文件，尚未按照关键词检索
    private var items: ObservableList<SearchItem?>? = null
    private var closeAppButton: Pane? = null
    private var minimizeAppButton: Pane? = null
    private var openSettingsButton: Pane? = null
    private var filesCountLabel: Label? = null
    private var startRetrievingButton: JFXButton? = null
    private var keywordsBox: JFXChipView<String>? = null
    private var keywordsAlert: Pane? = null

    private constructor(parentStage: Stage) : super(parentStage) {}
    public constructor(parentStage: Stage, stageName: String) : super(parentStage, stageName) {}
    constructor(parentStage: Stage, files: HashMap<Path, OfficeFileType>?) : this(parentStage) {
        this.files = files
    }

    constructor(parentStage: Stage, files: HashMap<Path, OfficeFileType>?, stageName: String) : this(parentStage, stageName) {
        this.files = files
    }

    override fun loadComponents() {
        if (SProperties.readProperty("/cn/sheyifan/fxpdfs_profile.properties", "lang") == "ZH") {
            searchResultResourceBundle = ResourceBundle.getBundle("cn/sheyifan/lang/foundview", Locale.CHINA)
        }
        else if (SProperties.readProperty("/cn/sheyifan/fxpdfs_profile.properties", "lang") == "EN") {
            searchResultResourceBundle = ResourceBundle.getBundle("cn/sheyifan/lang/foundview", Locale.US)
        }
        else {
            logger.error("Unrecognized region")
            return
        }

        root = FXMLLoader.load(FxPDFsPrimaryView::class.java.getResource("/cn/sheyifan/view/found.fxml"))
        keywordsBox = JFXChipView()
        keywordsBox!!.id = "keywords"
        keywordsBox?.layoutX = 10.0
        keywordsBox?.layoutY = 85.0
        content = root!!.children[0] as Pane
        keywordsAlert = root!!.lookup("#keywords-alert-box") as Pane
        filesCountLabel = root!!.lookup("#files-count") as Label
        closeAppButton = root!!.lookup("#fxpdfs-app-close-icon-box") as Pane
        minimizeAppButton = root!!.lookup("#fxpdfs-app-minimize-icon-box") as Pane
        openSettingsButton = root!!.lookup("#titlebar-settings-item-box") as Pane
        startRetrievingButton = root!!.lookup("#start") as JFXButton
        idColumn = JFXTreeTableColumn(searchResultResourceBundle.getString("table.columns.filetype.desc"))
        idColumn!!.prefWidth = 30.0
        idColumn!!.styleClass.add("left-display")
        idColumn!!.setCellValueFactory { param: TreeTableColumn.CellDataFeatures<SearchItem, String> ->
            if (idColumn!!.validateValue(param)) {
                return@setCellValueFactory param.value.value.id
            } else {
                return@setCellValueFactory idColumn!!.getComputedValue(param)
            }
        }
        pathColumn = JFXTreeTableColumn(searchResultResourceBundle.getString("table.columns.filepath.desc"))
        pathColumn!!.prefWidth = 140.0
        pathColumn!!.setCellValueFactory { param: TreeTableColumn.CellDataFeatures<SearchItem, String> ->
            if (pathColumn!!.validateValue(param)) {
                return@setCellValueFactory param.value.value.path
            } else {
                return@setCellValueFactory pathColumn!!.getComputedValue(param)
            }
        }
        pathColumn!!.setCellFactory { _: TreeTableColumn<SearchItem, String>? ->
            object : JFXTreeTableCell<SearchItem?, String?>() {
                private var lastOpenFileByDefaultAppEvent: EventHandler<MouseEvent>? = null
                private var hoverEvent: EventHandler<MouseEvent>? = null
                private var exitEvent: EventHandler<MouseEvent>? = null
                override fun updateItem(item: String?, empty: Boolean) {
                    super.updateItem(item, empty)
                    text = item
                    if (item != null) {
                        val filePath = Paths.get(URL(item).toURI()).toAbsolutePath()
                        val tooltip = Tooltip(filePath.fileName.toString())
                        tooltip.showDelay = Duration.millis(300.0)
                        tooltip.style = "-fx-background-color: #2481e3"
                        setTooltip(tooltip)
                        if (lastOpenFileByDefaultAppEvent != null) {
                            removeEventHandler(MouseEvent.MOUSE_CLICKED, lastOpenFileByDefaultAppEvent)
                        }
                        lastOpenFileByDefaultAppEvent = EventHandler { event: MouseEvent ->
                            if (event.clickCount == 2) {
                                if (Files.exists(filePath)) {
                                    Desktop.getDesktop().open(File(filePath.toString()))
                                } else {
                                    val alertDiag = FxPDFsInfoDialogView(
                                            currentStage,
                                            "File not found info dialog",
                                            "/cn/sheyifan/view/dialog/file_notfound_info")
                                    alertDiag.show()
                                }
                            }
                        }
                        addEventHandler(MouseEvent.MOUSE_CLICKED, lastOpenFileByDefaultAppEvent)
                        if (hoverEvent == null || exitEvent == null) {
                            hoverEvent = EventHandler { this.cursor = Cursor.HAND }
                            exitEvent = EventHandler { this.cursor = Cursor.DEFAULT }
                            addEventHandler(MouseEvent.MOUSE_ENTERED, hoverEvent)
                            addEventHandler(MouseEvent.MOUSE_EXITED, exitEvent)
                        }
                    }
                }
            }
        }
        matchesColumn = JFXTreeTableColumn(searchResultResourceBundle.getString("table.columns.matchcount.desc"))
        matchesColumn!!.styleClass.add("center-display")
        matchesColumn!!.setCellValueFactory { param: TreeTableColumn.CellDataFeatures<SearchItem, Int> ->
            if (matchesColumn!!.validateValue(param)) {
                return@setCellValueFactory SimpleIntegerProperty(param.value.value.matches.intValue()).asObject()
            } else {
                return@setCellValueFactory matchesColumn!!.getComputedValue(param)
            }
        }
        items = FXCollections.observableArrayList()
        fillTableData()
        val dataRoot = RecursiveTreeItem(items) { obj: RecursiveTreeObject<SearchItem?> -> obj.children }
        treeTableView = root!!.lookup("#found-items-table") as JFXTreeTableView<SearchItem>
        treeTableView!!.setRoot(dataRoot)
        treeTableView!!.setShowRoot(false)
        treeTableView!!.setEditable(true)
        treeTableView!!.getColumns().setAll(idColumn, pathColumn, matchesColumn)
        treeTableView!!.prefWidthProperty().bind(root!!.widthProperty().subtract(104))

        content?.children?.addAll(keywordsBox)
    }

    override fun initStyle() {
        super.initStyle()

        idColumn!!.prefWidth = 50.0

        keywordsAlert!!.isVisible = false
        val keywordAlertText = (keywordsAlert!!.children[1] as Pane).children[0] as Label
        keywordAlertText.text = searchResultResourceBundle.getString("keywords.alert")
        keywordAlertText.style = "-fx-font-family: ${searchResultResourceBundle.getString("main.font")}"
        keywordsBox?.maxWidth = 250.0
        keywordsBox?.layoutXProperty()?.bind(
                content?.prefWidthProperty()?.subtract(keywordsBox?.maxWidthProperty())?.divide(2))

        val keywordsSign = root!!.lookup("#keywords-sign") as Label
        keywordsSign.text = searchResultResourceBundle.getString("keywords.sign")
        keywordsSign.style = "-fx-font-family: ${searchResultResourceBundle.getString("main.font")}"

        val foundItemsTablePlaceHolder = treeTableView!!.getPlaceholder() as Label
        foundItemsTablePlaceHolder.text = searchResultResourceBundle.getString("table.placeholder")
        foundItemsTablePlaceHolder.style = "-fx-font-family: ${searchResultResourceBundle.getString("main.font")}"

        startRetrievingButton!!.text = searchResultResourceBundle.getString("forms.submit")
        startRetrievingButton!!.style = "-fx-font-family: ${searchResultResourceBundle.getString("main.font")}"
    }

    override fun registerEvents() {
        closeAppButton!!.addEventHandler(MouseEvent.MOUSE_CLICKED) { currentStage.close() }
        minimizeAppButton!!.addEventHandler(MouseEvent.MOUSE_CLICKED) {
            Window.getWindows().forEach {
                if (it is Stage) {
                    if (it.owner == null) {
                        it.isIconified = true
                    }
                }
            }
        }
        openSettingsButton!!.addEventHandler(MouseEvent.MOUSE_CLICKED) { FxPDFsSettingsView(currentStage, "File search result window").show() }
        startRetrievingButton!!.onAction = EventHandler {
            if (keywordsBox!!.chips.size == 0) {
                keywordsAlert!!.isVisible = true
                return@EventHandler
            }
            keywordsAlert!!.isVisible = false

            val progressView: FxPDFsProgressView = FxPDFsSearchContentPDFsProgressView(currentStage, "window for searching file content")
            progressView.show()
            val progressRoot = progressView.root
            val progressBar = progressRoot!!.lookup("#indexing-progress-bar") as JFXProgressBar
            val progressValue = progressRoot.lookup("#progress-value") as Label
            val keywords = arrayOfNulls<String>(keywordsBox?.chips?.toTypedArray()?.size ?: 0)
            keywordsBox?.chips?.forEachIndexed { i, chip ->
                keywords[i] = chip
            }

            val walkFileFxBackTask = FxBackgroundTask(
                    progressView.continueFlag,
                    items!!.size,
                    Runnable {}, Runnable { progressView.close() })
            val fileContentWalkingThread = Thread {
                val logger = Logger.getLogger(FxPDFsSearchResultView::class.java)
                val startTime = System.currentTimeMillis()
                val retItems = SPDF().walkFileContent(items, keywords, walkFileFxBackTask)
                retItems.sortWith(java.util.Comparator { o1: SearchItem, o2: SearchItem -> o2.matches.get() - o1.matches.get() })
                val stopTime = System.currentTimeMillis()
                Platform.runLater {
                    val dataRootUpdated = RecursiveTreeItem(retItems, Callback { obj: RecursiveTreeObject<SearchItem?> -> obj.children })
                    treeTableView!!.setRoot(dataRootUpdated)
                }
                logger.info("Finish retrieving all file content in " + (stopTime - startTime) + " ms.")
            }
            val fileContentWalkingObserver = Thread {
                val logger = Logger.getLogger(FxPDFsSearchResultView::class.java)
                var oldProgress: Int = walkFileFxBackTask.currentProgress.get()
                logger.info("start walking file content")
                while (true) {
                    if (!walkFileFxBackTask.continueFlag.get()) {
                        logger.info("break from walking file content")
                        break
                    }
                    if (walkFileFxBackTask.currentProgress.get() != oldProgress) {
                        Platform.runLater {
                            val currentProgress = walkFileFxBackTask.progressPercentage
                            val decimalFormat = DecimalFormat("0.00")
                            progressBar.progress = currentProgress
                            progressValue.text = decimalFormat.format(currentProgress * 100) + " %"
                        }
                        oldProgress = walkFileFxBackTask.currentProgress.get()
                    }
                    Thread.sleep(10)
                }
                Platform.runLater {
                    Window.getWindows().forEach {
                        if (it is Stage) {
                            if (it.owner == null) {
                                it.isIconified = false
                            }
                        }
                    }
                }
            }

            val garbageCollectThread = Thread {
                while (true) {
                    Thread.sleep(5000)

                    if (!progressView.continueFlag.get()) {
                        break
                    }

                    System.gc()
                }
            }
            progressView.startProgress(fileContentWalkingThread)
            progressView.startProgress(fileContentWalkingObserver)
            progressView.startProgress(garbageCollectThread)
        }
    }

    override fun initWindow() {
        super.initWindow()
        scene = Scene(root, content!!.prefWidth + 20, content!!.prefHeight + 20, Color.valueOf("#ffffff01"))
        currentStage.scene = scene
        currentStage.initStyle(StageStyle.TRANSPARENT)
        currentStage.isResizable = false
        currentStage.initOwner(parentStage)
        currentStage.icons.add(Image("/cn/sheyifan/view/images/fxpdfs_icon.png"))
        currentStage.initModality(Modality.APPLICATION_MODAL)
        DraggableGroup(root!!, currentStage).setDraggable(true)
    }

    private fun fillTableData() {
        files?.entries?.forEach {
            items!!.add(SearchItem(it.value.toString(), it.key.toUri().toURL().toExternalForm(), 0))
        }
        filesCountLabel!!.text = files?.size.toString()
    }

    class SearchItem(id: String, path: String?, matches: Int) : RecursiveTreeObject<SearchItem?>() {
        @JvmField
        val id: StringProperty
        @JvmField
        val path: StringProperty
        @JvmField
        val matches: IntegerProperty

        init {
            this.id = SimpleStringProperty(id)
            this.path = SimpleStringProperty(path)
            this.matches = SimpleIntegerProperty(matches)
        }
    }
}