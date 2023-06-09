
package net.ccbluex.liquidbounce.features.module.modules.movement

import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo

@ModuleInfo(name = "Freeze", description = "Allows you to stay stuck in mid air.", category = ModuleCategory.MOVEMENT)
class Freeze : Module() {
    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        mc.thePlayer.isDead = true
        mc.thePlayer.cameraYaw = mc.thePlayer.rotationYaw
        mc.thePlayer.cameraPitch = mc.thePlayer.rotationPitch
    }

    override fun onDisable() {
        mc.thePlayer?.isDead = false
    }
}
