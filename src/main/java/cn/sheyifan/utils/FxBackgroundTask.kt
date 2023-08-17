package cn.sheyifan.utils

import java.util.concurrent.atomic.AtomicBoolean
import java.util.concurrent.atomic.AtomicInteger

class FxBackgroundTask(// Actions to run in javafx thread
        @JvmField var continueFlag: AtomicBoolean, maxProgress: Int, preFxAction: Runnable, sufFxAction: Runnable) {
    @JvmField
    var preFxAction: Runnable
    @JvmField
    var sufFxAction: Runnable
    @JvmField
    var currentProgress: AtomicInteger = AtomicInteger(0)
    private var maxProgress = 0
    val progressPercentage: Double
        get() = currentProgress.toDouble() / maxProgress

    init {
        this.maxProgress = maxProgress
        this.preFxAction = preFxAction
        this.sufFxAction = sufFxAction
    }
}