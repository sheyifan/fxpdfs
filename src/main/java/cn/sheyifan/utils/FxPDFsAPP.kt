package cn.sheyifan.utils

import cn.sheyifan.utils.file.SProperties
import java.text.SimpleDateFormat
import java.util.*

object FxPDFsAPP {
    fun checkLicense(): Boolean {
        val deadline: Date? = getDeadline()
        return if (deadline == null) {
            true
        }
        else {
            val currentDate = Date(System.currentTimeMillis())
            deadline.after(currentDate)
        }
    }

    fun getDeadline(): Date? {
        val deadlineStr = SProperties.readProperty("/cn/sheyifan/fxpdfs_profile.properties", "deadline")
        if (deadlineStr == "") {
            return null
        }
        val formatter = SimpleDateFormat("yyyy-MM-dd")
        return formatter.parse(deadlineStr)
    }
}