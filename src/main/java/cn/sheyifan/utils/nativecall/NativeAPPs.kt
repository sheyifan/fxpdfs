package cn.sheyifan.utils.nativecall

import org.apache.log4j.Logger
import java.awt.Desktop
import java.io.IOException
import java.net.URI
import java.net.URISyntaxException

object NativeAPPs {
    fun sendMail() {
        val logger = Logger.getLogger(NativeAPPs::class.java)
        if (Desktop.isDesktopSupported()) {
            val desktop = Desktop.getDesktop()
            if (desktop.isSupported(Desktop.Action.MAIL)) {
                try {
                    val mail = URI("mailto:1506870670@qq.com?subject=FxPDFs%20feedback")
                    desktop.mail(mail)
                } catch (e: URISyntaxException) {
                    e.printStackTrace()
                } catch (e: IOException) {
                    e.printStackTrace()
                }
            } else {
                logger.error("Mail is not supported.")
            }
        } else {
            logger.error("Native desktop is not supported.")
        }
    }
}