package cn.sheyifan.components

import cn.sheyifan.utils.file.OfficeFileType
import cn.sheyifan.utils.file.SFile
import cn.sheyifan.utils.file.SProperties
import javafx.application.Platform
import javafx.scene.control.TextField
import javafx.stage.Stage
import javafx.stage.Window
import org.apache.log4j.Logger
import java.net.URL
import java.nio.file.Path
import java.nio.file.Paths
import java.text.DecimalFormat
import java.util.*

class FxPDFsListFilesProgressView : FxPDFsProgressView {
    private lateinit var listFilesProgressViewResourceBundle: ResourceBundle

    constructor(parentStage: Stage?) : super(parentStage) {}
    constructor(parentStage: Stage?, stageName: String?) : super(parentStage, stageName) {}

    override fun initStyle() {
        super.initStyle()

        if (SProperties.readProperty("/cn/sheyifan/fxpdfs_profile.properties", "lang") == "ZH") {
            listFilesProgressViewResourceBundle = ResourceBundle.getBundle("cn/sheyifan/lang/foundview", Locale.CHINA)
        }
        else if (SProperties.readProperty("/cn/sheyifan/fxpdfs_profile.properties", "lang") == "EN") {
            listFilesProgressViewResourceBundle = ResourceBundle.getBundle("cn/sheyifan/lang/foundview", Locale.US)
        }
        else {
            logger.error("Unrecognized region")
            return
        }

        progressDesc!!.style = "-fx-font-family: ${listFilesProgressViewResourceBundle.getString("main.font")}"
        progressDesc!!.text = listFilesProgressViewResourceBundle.getString("progressbar.listfiles.statusdesc.pre")
    }

    override fun show() { // 清除上一次查询的文件列表记录
        fun newFileTypes(): Boolean {
            if (FxPDFsPrimaryView.lastFileTypes == null) {
                return true
            }

            fileTypeList?.forEach {
                if (!FxPDFsPrimaryView.lastFileTypes!!.contains(it)) {
                    return true
                }
            }

            if (fileTypeList!!.size != FxPDFsPrimaryView.lastFileTypes!!.size) {
                return true
            }
            return false
        }

        val dirUrlBox = parentStage!!.scene.lookup("#pdf-url-box") as TextField
        println(newFileTypes())
        if ((URL(dirUrlBox.text) != FxPDFsPrimaryView.lastRetrieveURL) || newFileTypes()) {
            files = null
            System.gc()
            super.show()
            walkFileTree()
        }
        else {
            FxPDFsSearchResultView(parentStage!!, files, "Found files window").show()
        }
    }

    private fun walkFileTree() {
        val startProgressThread = Thread {
            continueFlag.set(true)
            val dirUrlBox = parentStage!!.scene.root.lookup("#pdf-url-box") as TextField
            val logger = Logger.getLogger(FxPDFsProgressView::class.java)
            val startTime = System.currentTimeMillis()
            val pdfURL = URL(dirUrlBox.text)
            val pdfPath = Paths.get(pdfURL.toURI())
            val sfile = SFile(pdfPath)
            val total = sfile.getFileCountByRoboCopy(continueFlag)
            if (total == 0) {
                Thread.currentThread().stop()
            }
            logger.info("Total: $total")
            var currentCount: Int = 0;
            Platform.runLater {
                progressBar!!.progress = 0.0
                progressValue!!.text = "0.00 %"
                progressDesc!!.text = listFilesProgressViewResourceBundle.getString("progressbar.listfiles.statusdesc.during")
            }
            val estimatingTime = System.currentTimeMillis()
            logger.info("Walking dir: " + pdfPath.toAbsolutePath().toString() + ". Used estimating time: " + (estimatingTime - startTime))

            files = sfile.walkDir(Runnable {
                currentCount++
                Platform.runLater {
                    val currentProgress = currentCount.toDouble() / total
                    if (currentProgress <= 1.0) {
                        val decimalFormat = DecimalFormat("0.00")
                        progressBar!!.progress = Math.min(currentProgress, 1.0)
                        progressValue!!.text = decimalFormat.format(currentProgress * 100) + " %"
                    }
                }
            }, continueFlag)
            val endTime = System.currentTimeMillis()
            logger.info("Walking dir: " + pdfPath.toAbsolutePath().toString() + ". Used walking time: " + (endTime - estimatingTime))
            Platform.runLater {
                currentStage.close()
                if (currentCount == total) {
                    FxPDFsSearchResultView(parentStage!!, files, "Found files window").show()
                    continueFlag.set(false)
                } else {
                    FxPDFsSearchResultView(parentStage!!, files, "Found files window").show()
                    continueFlag.set(false)
                    logger.info(
                            "Total file " + total +
                                    " count not equals to actually handled file count " + currentCount + ".")
                }
                // 记录本次查询的URL。重复查询相同的URL，将会使用缓存。
                FxPDFsPrimaryView.lastRetrieveURL = pdfURL
                FxPDFsPrimaryView.lastFileTypes = fileTypeList?.clone()
                Window.getWindows().forEach {
                    if (it is Stage) {
                        if (it.owner == null) {
                            it.isIconified = false
                        }
                    }
                }
            }
            continueFlag.set(false)
        }
        startProgressThread.isDaemon = true
        startProgressThread.start()
    }

    companion object {
        var files: HashMap<Path, OfficeFileType>? = null
    }
}