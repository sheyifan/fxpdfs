package cn.sheyifan.components

import cn.sheyifan.utils.file.SProperties
import javafx.stage.Stage
import java.util.*

class FxPDFsSearchContentPDFsProgressView : FxPDFsProgressView {
    private lateinit var retrieveProgressResourceBundle: ResourceBundle

    constructor(parentStage: Stage?) : super(parentStage) {}
    constructor(parentStage: Stage?, stageName: String?) : super(parentStage, stageName) {}

    override fun initStyle() {
        super.initStyle()

        if (SProperties.readProperty("/cn/sheyifan/fxpdfs_profile.properties", "lang") == "ZH") {
            retrieveProgressResourceBundle = ResourceBundle.getBundle("cn/sheyifan/lang/foundview", Locale.CHINA)
        }
        else if (SProperties.readProperty("/cn/sheyifan/fxpdfs_profile.properties", "lang") == "EN") {
            retrieveProgressResourceBundle = ResourceBundle.getBundle("cn/sheyifan/lang/foundview", Locale.US)
        }
        else {
            logger.error("Unrecognized region")
            return
        }

        progressDesc!!.style = "-fx-font-family: ${retrieveProgressResourceBundle.getString("main.font")}"
        progressDesc!!.text = retrieveProgressResourceBundle.getString("progressbar.retrieve.statusbar.during")
    }
}