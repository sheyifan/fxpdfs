package cn.sheyifan.components

import cn.sheyifan.utils.DraggableGroup
import cn.sheyifan.utils.file.SProperties
import com.jfoenix.controls.JFXButton
import com.jfoenix.controls.JFXRadioButton
import javafx.event.ActionEvent
import javafx.fxml.FXMLLoader
import javafx.scene.Scene
import javafx.scene.control.Label
import javafx.scene.control.ToggleGroup
import javafx.scene.layout.Pane
import javafx.scene.paint.Color
import javafx.stage.Modality
import javafx.stage.Stage
import javafx.stage.StageStyle
import java.util.*

class FxPDFsSettingsView : FxPDFsView {
    private lateinit var settingsResourceBundle: ResourceBundle

    private var closeSettingsButton: JFXButton? = null
    private var saveSettingsButton: JFXButton? = null
    private var editUserButton: JFXButton? = null
    private var themeRadioGroup: ToggleGroup? = null
    private var lightThemeRadioButton: JFXRadioButton? = null
    private var darkThemeRadioButton: JFXRadioButton? = null

    constructor(parentStage: Stage?) : super(parentStage) {}
    constructor(parentStage: Stage?, stageName: String?) : super(parentStage, stageName) {}

    override fun loadComponents() {
        root = FXMLLoader.load(FxPDFsSettingsView::class.java.getResource("/cn/sheyifan/view/settings.fxml"))
        content = root!!.children[0] as Pane
        closeSettingsButton = root!!.lookup("#cancel-save-settings-button") as JFXButton
        themeRadioGroup = ToggleGroup()
        lightThemeRadioButton = root!!.lookup("#light-theme-radio-button") as JFXRadioButton
        lightThemeRadioButton!!.toggleGroup = themeRadioGroup
        darkThemeRadioButton = root!!.lookup("#dark-theme-radio-button") as JFXRadioButton
        darkThemeRadioButton!!.toggleGroup = themeRadioGroup

        saveSettingsButton = root!!.lookup("#save-settings-button") as JFXButton
        editUserButton = root!!.lookup("#edit-usr-button") as JFXButton
    }

    override fun initStyle() {
        if (SProperties.readProperty("/cn/sheyifan/fxpdfs_profile.properties", "lang") == "ZH") {
            settingsResourceBundle = ResourceBundle.getBundle("cn/sheyifan/lang/settingsview", Locale.CHINA)
        }
        else if (SProperties.readProperty("/cn/sheyifan/fxpdfs_profile.properties", "lang") == "EN") {
            settingsResourceBundle = ResourceBundle.getBundle("cn/sheyifan/lang/settingsview", Locale.US)
        }
        else {
            logger.error("Unrecognized region")
            return
        }

        val settingsVersion = root!!.lookup("#settings-app-version") as Label
        settingsVersion.text = settingsResourceBundle.getString("top.version")
        settingsVersion.style = "-fx-font-family: ${settingsResourceBundle.getString("main.font")}"

        val settingsRelease = root!!.lookup("#settings-app-release") as Label
        settingsRelease.text = "${settingsResourceBundle.getString("top.release")} ${SProperties.readProperty("/cn/sheyifan/fxpdfs_profile.properties", "relasedate")}"
        settingsRelease.style = "-fx-font-family: ${settingsResourceBundle.getString("main.font")}"

        val supportedFileTypes = root!!.lookup("#settings-supported-file-types-label") as Label
        supportedFileTypes.text = settingsResourceBundle.getString("body.filetypes")
        supportedFileTypes.style = "-fx-font-family: ${settingsResourceBundle.getString("main.font")}"

        val bodyPrompt = root!!.lookup("#settings-body-prompt") as Label
        bodyPrompt.text = settingsResourceBundle.getString("body.prompt")
        bodyPrompt.style = "-fx-font-family: ${settingsResourceBundle.getString("main.font")}"

        val licenseSign = root!!.lookup("#settings-license-label") as Label
        licenseSign.text = settingsResourceBundle.getString("body.license")
        licenseSign.style = "-fx-font-family: ${settingsResourceBundle.getString("main.font")}"

        val licenseDetail = root!!.lookup("#settings-license-detail") as Label
        licenseDetail.text = "${settingsResourceBundle.getString("body.license.detail")} ${
            when (SProperties.readProperty("/cn/sheyifan/fxpdfs_profile.properties", "deadline")) {
                "" -> "Infinity"
                else -> SProperties.readProperty("/cn/sheyifan/fxpdfs_profile.properties", "deadline")
            }
        }"
        licenseDetail.style = "-fx-font-family: ${settingsResourceBundle.getString("main.font")}"

        val themeSign = root!!.lookup("#settings-theme-label") as Label
        themeSign.text = settingsResourceBundle.getString("tail.theme.desc")
        themeSign.style = "-fx-font-family: ${settingsResourceBundle.getString("main.font")}"

        lightThemeRadioButton!!.text = settingsResourceBundle.getString("tail.theme.light")
        lightThemeRadioButton!!.style = "-fx-font-family: ${settingsResourceBundle.getString("main.font")}"
        darkThemeRadioButton!!.text = settingsResourceBundle.getString("tail.theme.dark")
        darkThemeRadioButton!!.style = "-fx-font-family: ${settingsResourceBundle.getString("main.font")}"

        val userSign = root!!.lookup("#settings-username-label") as Label
        userSign.text = settingsResourceBundle.getString("tail.usr.desc")
        userSign.style = "-fx-font-family: ${settingsResourceBundle.getString("main.font")}"

        editUserButton!!.text = settingsResourceBundle.getString("tail.usr.editbutton")
        editUserButton!!.style = "-fx-font-family: ${settingsResourceBundle.getString("main.font")}"

        closeSettingsButton!!.text = settingsResourceBundle.getString("tail.cancel")
        closeSettingsButton!!.style = "-fx-font-family: ${settingsResourceBundle.getString("main.font")}"

        saveSettingsButton!!.text = settingsResourceBundle.getString("tail.save")
        saveSettingsButton!!.style = "-fx-font-family: ${settingsResourceBundle.getString("main.font")}"
    }
    override fun registerEvents() {
        closeSettingsButton!!.addEventHandler(ActionEvent.ACTION) { currentStage.close() }
    }

    override fun initWindow() {
        super.initWindow()
        scene = Scene(root, content!!.prefWidth + 8, content!!.prefHeight + 8, Color.valueOf("#ffffff01"))
        currentStage.scene = scene
        currentStage.initStyle(StageStyle.TRANSPARENT)
        currentStage.initModality(Modality.APPLICATION_MODAL)
        currentStage.initOwner(parentStage)
        DraggableGroup(content!!, currentStage).setDraggable(true)
    }
}