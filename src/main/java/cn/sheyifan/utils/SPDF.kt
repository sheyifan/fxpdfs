package cn.sheyifan.utils

import cn.sheyifan.components.FxPDFsSearchResultView.SearchItem
import cn.sheyifan.utils.file.getTextFromOffice
import javafx.application.Platform
import javafx.collections.FXCollections
import javafx.collections.ObservableList
import org.apache.log4j.Logger
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.text.PDFTextStripper
import java.net.URL
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*
import java.util.concurrent.atomic.AtomicInteger
import java.util.function.Consumer

class SPDF {
    fun walkFileContent(
            items: ObservableList<SearchItem?>?,
            keywords: Array<String?>,
            backgroundTask: FxBackgroundTask): ObservableList<SearchItem>  {
        Platform.runLater(backgroundTask.preFxAction)

        val addSearchItemLock= ""
        val retItems: ObservableList<SearchItem> = FXCollections.observableArrayList()
        Platform.runLater(backgroundTask.preFxAction)
        val logger = Logger.getLogger(SPDF::class.java)

        val fileCount = AtomicInteger(0)

        items!!.stream().parallel().forEach { item: SearchItem? ->
            if (!backgroundTask.continueFlag.get()) {
                return@forEach
            }
            fileCount.incrementAndGet()

            val pdfPath: Path? = Paths.get(URL(item?.path?.value).toURI())
            var matchCount = 0
            getTextFromOffice(pdfPath!!.toFile())?.lines()?.forEach { e: String ->
                for (keyword in keywords) {
                    matchCount += keyword!!.toLowerCase().toRegex().findAll(e.toLowerCase()).count()
                }
            }

            synchronized(addSearchItemLock) {
                retItems.add(SearchItem(item!!.id.get(), item.path.get(), matchCount))
            }

            backgroundTask.currentProgress.incrementAndGet()
        }

        backgroundTask.continueFlag.set(false)
        logger.info("Finish resolving file content.")
        Platform.runLater(backgroundTask.sufFxAction)
        return retItems
    }

    companion object {
        fun find(pdfSrc: Path, matchCount: Int, vararg targets: String): ArrayList<String> {
            matchCount.minus(matchCount)
            val caught = ArrayList<String>()
            val pdfDoc = PDDocument.load(pdfSrc.toFile())
            PDFTextStripper().getText(pdfDoc).lines().forEach(Consumer { e: String ->
                var lineMatchCount = 0
                for (target in targets) {
                    lineMatchCount += target.toLowerCase().toRegex().findAll(e.toLowerCase()).count()
                }
                matchCount.plus(lineMatchCount)
                if (lineMatchCount > 0) {
                    caught.add(e)
                }
            })
            pdfDoc.close()
            return caught
        }

        fun calculateLines(pdfSrc: Path): Long {
            val pdfDoc = PDDocument.load(pdfSrc.toFile())
            val count: Long = PDFTextStripper().getText(pdfDoc).lines().count().toLong()
            pdfDoc.close()
            return count
        }
    }
}