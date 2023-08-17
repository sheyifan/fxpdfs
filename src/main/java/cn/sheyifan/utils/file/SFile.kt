package cn.sheyifan.utils.file

import cn.sheyifan.components.fileTypeList
import org.apache.log4j.Logger
import org.apache.pdfbox.pdmodel.PDDocument
import org.apache.pdfbox.text.PDFTextStripper
import org.apache.poi.extractor.POITextExtractor
import org.apache.poi.ooxml.extractor.ExtractorFactory
import org.apache.tika.config.TikaConfig
import org.apache.tika.io.TikaInputStream
import org.apache.tika.metadata.TikaCoreProperties.RESOURCE_NAME_KEY
import java.io.*
import java.nio.file.Files
import java.nio.file.Path
import java.nio.file.Paths
import java.util.*
import java.util.concurrent.atomic.AtomicBoolean

import org.apache.tika.metadata.Metadata as TikaMetaData

val logger: Logger = Logger.getLogger(SFile::class.java)

fun main() {
    recurseDir(File("C:\\workdir\\projects\\wingtech\\W63021AA1"))
}

fun recurseDir(src: File) {
    val srcPath = src.toPath()
    if (!Files.isDirectory(srcPath)) {
        if (getTypeByExtension(src) != OfficeFileType.UNKNOWN) {
            logger.info("Content: ${getTextFromOffice(src)?.length}")
        }
    }
    else {
        Files.list(srcPath).forEach {
            if (!Files.isDirectory(it)) {
                if (getTypeByExtension(it.toFile()) != OfficeFileType.UNKNOWN) {
                    logger.info("Content: ${getTextFromOffice(it.toFile())?.length}")
                }
            }
            else {
                recurseDir(it.toFile())
            }
        }
    }
}

enum class OfficeFileType {
    WORD, EXCEL, PPT, PDF, UNKNOWN
}

fun getType(file: File): OfficeFileType {
    try {
        val tikaConfig = TikaConfig()
        val metaData = TikaMetaData()
        metaData.set(RESOURCE_NAME_KEY, file.toString())
        val mimeType = tikaConfig.detector.detect(TikaInputStream.get(file.toPath()), metaData)

        val officeFileType: OfficeFileType
        officeFileType = if (mimeType.toString().contains("powerpoint")
                || mimeType.toString().contains("presentation")) {
            OfficeFileType.PPT
        }
        else if (mimeType.toString().contains("excel")
                || mimeType.toString().contains("text/csv")
                || mimeType.toString().contains("spreadsheet")) {
            OfficeFileType.EXCEL
        }
        else if (mimeType.toString().contains("word")) {
            OfficeFileType.WORD
        }
        else if (file.toPath().toAbsolutePath().toString().endsWith(".pdf")) {
            OfficeFileType.PDF
        }
        else {
            OfficeFileType.UNKNOWN
        }

        return officeFileType
    } catch (configReadException: IOException) {
        logger.error("Fail to read file type from $file")
    }
    return OfficeFileType.UNKNOWN
}

fun getTypeByExtension(file: File): OfficeFileType {
    val filePathSrc: String = file.toPath().toAbsolutePath().toString()

    if (filePathSrc.endsWith(".xlsx")
            || filePathSrc.endsWith(".xlsm")
            || filePathSrc.endsWith(".xls")
            || filePathSrc.endsWith(".csv")
            || filePathSrc.endsWith(".xltx")
            || filePathSrc.endsWith(".xltm")
            || filePathSrc.endsWith(".xlt")) {
        return OfficeFileType.EXCEL
    }
    else if (filePathSrc.endsWith(".docx")
            || filePathSrc.endsWith(".docm")
            || filePathSrc.endsWith(".doc")
            || filePathSrc.endsWith(".dotx")
            || filePathSrc.endsWith(".dotm")
            || filePathSrc.endsWith(".dot")
            || filePathSrc.endsWith(".rtf")) {
        return OfficeFileType.WORD
    }
    else if (filePathSrc.endsWith(".pptx")
            || filePathSrc.endsWith(".pptm")
            || filePathSrc.endsWith(".ppt")
            || filePathSrc.endsWith(".potx")
            || filePathSrc.endsWith(".potm")
            || filePathSrc.endsWith(".pot")
            || filePathSrc.endsWith(".ppsx")
            || filePathSrc.endsWith(".ppsm")
            || filePathSrc.endsWith(".pps") || filePathSrc.endsWith(".pptx")) {
        return OfficeFileType.PPT
    }
    else if (filePathSrc.endsWith(".pdf")) {
        return OfficeFileType.PDF
    }
    else {
        return OfficeFileType.UNKNOWN
    }
}

fun getTextFromOffice(file: File): String? {
    fun getOfficeFileText(): String? {
        var textExtractor: POITextExtractor? = null
        try {
            textExtractor = ExtractorFactory.createExtractor(file)
        } catch (readFileException: Exception) {
            logger.error("Fail to create extractor from $file")
        }

        try {
            val text = textExtractor?.text
            textExtractor?.close()
            return text
        } catch (e: Exception) {
            logger.error("Fail to get text from $file")
        }
        textExtractor?.close()
        return null
    }

    fun getPDFText() : String {
        var pdfDoc: PDDocument? = null
        try {
            pdfDoc = PDDocument.load(file)
            if (pdfDoc != null) {
                val content = PDFTextStripper().getText(pdfDoc)
                pdfDoc.close()
                return content
            }
        } catch (e: IOException) {
            logger.error("Fail to read doc from $file")
        }
        pdfDoc?.close()
        return ""
    }

    return when (getTypeByExtension(file)) {
        OfficeFileType.WORD -> getOfficeFileText()
        OfficeFileType.EXCEL -> getOfficeFileText()
        OfficeFileType.PPT -> getOfficeFileText()
        OfficeFileType.PDF -> getPDFText()
        else -> null
    }
}

class SFile(private val root: Path) {
    fun walkDir(findFileAction: Runnable, go: AtomicBoolean): HashMap<Path, OfficeFileType> {
        val result = HashMap<Path, OfficeFileType>()
        val logger = Logger.getLogger(SFile::class.java)
        _walkDir(root, result, logger, findFileAction, go)
        logger.info("Finish counting files. Returns array containing found files, size: ${result.size}")
        return result
    }

    private fun _walkDir(currentRoot: Path, result: HashMap<Path, OfficeFileType>, logger: Logger, findFileAction: Runnable, go: AtomicBoolean) {
        fun addOrNot(type: OfficeFileType): Boolean {
            if ((type == OfficeFileType.WORD && fileTypeList?.contains("Word") == true) ||
                    (type == OfficeFileType.EXCEL && fileTypeList?.contains("Excel") == true) ||
                    (type == OfficeFileType.PPT && fileTypeList?.contains("PPT") == true) ||
                    (type == OfficeFileType.PDF && fileTypeList?.contains("PDF") == true)) {
                return true
            }

            return false
        }

        if (!go.get()) {
            logger.error("Stopping from walking file directory tree.")
            Thread.currentThread().stop()
            return
        }
        if (!Files.isDirectory(currentRoot)) {
            val type = getTypeByExtension(currentRoot.toFile())
            if (addOrNot(type)) {
                result[currentRoot] = type
            }
            findFileAction.run()
        } else {
            try {
                Files.list(currentRoot).forEach { e: Path ->
                    if (!Files.isDirectory(e)) {
                        val type = getTypeByExtension(e.toFile())
                        if (addOrNot(type)) {
                            result[e] = type
                        }
                        findFileAction.run()
                    } else {
                        _walkDir(e, result, logger, findFileAction, go)
                    }
                }
            } catch (e: IOException) {
                logger.error("Fail to list file in $currentRoot")
            }
        }
    }

    private fun getFileCount(go: AtomicBoolean): Int {
        val count = 0
        getFile(root.toAbsolutePath().toString(), count, go)
        return count
    }

    fun getFileCountByRoboCopy(go: AtomicBoolean): Int {
        Logger.getLogger(SFile::class.java).info("Try to count files by robocopy.")
        val runtime = Runtime.getRuntime()
        val execPath = appRootPath.resolve("lib").resolve("win").resolve("cmd").resolve("robocopy").resolve("count.bat")
        var count = 0
        var cmdRun = false
        lateinit var p: Process
        try {
            val cmd =
                """
                "${execPath.toAbsolutePath()}" "${root.toAbsolutePath().toString().replace('\\', '/')}"
                """.trim()
            Logger.getLogger(SFile::class.java).info("Execute command: $cmd")
            cmdRun = true
            p = runtime.exec(cmd)
        } catch (e: IOException) {
            Logger.getLogger(SFile::class.java).info("Robocopy not found. Try to count files by pure java.")
            // Use pure java to get recursive file count instead of using robocopy.exe
            return getFileCount(go)
        }

        val readCmdOutThread = Thread {
            val output = BufferedReader(InputStreamReader(p.inputStream))
            var line: String? = null
            var caught: String? = null
            while (true) {
                if (!go.get()) {
                    p.destroy()
                    break
                }
                // fill file count when cmd app finish running
                if (!cmdRun) {
                    count = caught!!.split(":".toRegex()).toTypedArray()[1].strip().split(" ".toRegex()).toTypedArray()[0].toInt()
                    break
                }
                if (output.readLine().also { line = it } != null) {
                    println(line)
                    if (line!!.contains("Files")) {
                        caught = line
                    }
                } else {

                }
            }
            output.close()
        }
        readCmdOutThread.isDaemon = true
        readCmdOutThread.start()
        val ret = p.waitFor()
//            if (ret != 0) {
//                Logger.getLogger(SFile.class).error("Robocopy returns " + ret + ". Try to count files by pure java.");
//                return getFileCount(go);
//            }
        cmdRun = false
        // Wait for thread to get total file count
        readCmdOutThread.join()
        return count
    }

    private fun getFile(dirPath: String, count: Int, go: AtomicBoolean) {
        var count = count;
        if (!go.get()) {
            Thread.currentThread().stop()
            return
        }
        val f = File(dirPath)
        val files = f.listFiles()
        if (files != null) for (i in files.indices) {
            val file = files[i]
            if (file.isDirectory) {
                getFile(file.absolutePath, count, go)
            } else {
                count++
            }
        }
    }

    companion object {
        val appRootPath: Path
            get() = Paths.get(System.getProperty("user.dir"))

        @JvmStatic
        fun main(args: Array<String>) {
            println(appRootPath.toAbsolutePath().toString())
        }
    }
}