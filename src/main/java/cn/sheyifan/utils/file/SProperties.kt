package cn.sheyifan.utils.file

import java.io.InputStream
import java.util.*

object SProperties {
    fun readProperty(src: String, key: String): String? {
        val pro = Properties()
        val proIn: InputStream? = SProperties::class.java.getResourceAsStream(src)
        pro.load(proIn)
        val ret = pro.getProperty(key)
        proIn!!.close()
        return ret
    }
}