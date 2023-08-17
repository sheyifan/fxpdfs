package cn.sheyifan.utils

import javafx.scene.layout.Region

class FxStrucAnalysis(region: Any) {
    private var region: Region? = null
    fun resolveStructure() {
        if (region != null) {
            _walkStructure(region, 0)
        }
    }

    private fun _walkStructure(currentComponent: Any?, depth: Int) {
        if (currentComponent is Region) {
            val currentRegion = currentComponent
            if (currentRegion.childrenUnmodifiable.size == 0) {
                val padding = String(CharArray(depth)).replace("\u0000", "\t")
                println(padding + currentRegion)
                println("width:" + currentRegion.maxWidth)
                return
            }
            for (c in currentRegion.childrenUnmodifiable) {
                val padding = String(CharArray(depth)).replace("\u0000", "\t")
                println(padding + c)
                println("width:" + currentRegion.maxWidth)
                _walkStructure(c, depth + 1)
            }
        }
    }

    init {
        if (region is Region) {
            this.region = region
        } else {
            System.err.println("Error: $region is not instance of Region.")
        }
    }
}