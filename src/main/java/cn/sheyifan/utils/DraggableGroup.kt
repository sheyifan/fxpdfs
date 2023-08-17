package cn.sheyifan.utils

import javafx.event.EventHandler
import javafx.scene.Node
import javafx.scene.input.MouseEvent
import javafx.stage.Window
import java.util.*

class DraggableGroup(var handler: Node, vararg targets: Window) {
    var targets: Array<out Window>
    private val pressEvents: ArrayList<EventHandler<MouseEvent>>
    private val dragEvents: ArrayList<EventHandler<MouseEvent>>
    fun setDraggable(draggable: Boolean) {
        if (draggable) {
            for (i in targets.indices) {
                handler.addEventHandler(MouseEvent.MOUSE_PRESSED, pressEvents[i])
                handler.addEventHandler(MouseEvent.MOUSE_DRAGGED, dragEvents[i])
            }
        } else {
            for (i in targets.indices) {
                handler.removeEventHandler(MouseEvent.MOUSE_PRESSED, pressEvents[i])
                handler.removeEventHandler(MouseEvent.MOUSE_DRAGGED, dragEvents[i])
            }
        }
    }

    internal inner class Delta {
        var x = 0.0
        var y = 0.0
    }

    init {
        this.targets = targets
        pressEvents = ArrayList(targets.size)
        dragEvents = ArrayList(targets.size)
        val deltas = arrayOfNulls<Delta>(targets.size)
        for (i in targets.indices) {
            deltas[i] = Delta()
            pressEvents.add(EventHandler { event: MouseEvent ->
                deltas[i]!!.x = targets[i].x - event.screenX
                deltas[i]!!.y = targets[i].y - event.screenY
            })
            dragEvents.add(EventHandler { event: MouseEvent ->
                targets[i].x = event.screenX + deltas[i]!!.x
                targets[i].y = event.screenY + deltas[i]!!.y
            })
        }
    }
}