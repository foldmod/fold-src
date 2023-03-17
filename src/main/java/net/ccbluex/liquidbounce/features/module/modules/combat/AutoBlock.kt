package net.ccbluex.liquidbounce.features.module.modules.combat

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.TickEvent
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.utils.CPSCounter.MouseButton
import org.lwjgl.input.Keyboard
import org.lwjgl.input.Mouse

@ModuleInfo(
    name = "AutoBlock",
    description = "Holds right click while using killaura to autoblock on most servers.",
    category = ModuleCategory.COMBAT
)
class AutoBlock : Module() {
    val killAura = LiquidBounce.moduleManager.getModule(KillAura::class.java)!! as KillAura

    @EventTarget
    fun onTick(event: TickEvent) {
        mc.gameSettings.keyBindUseItem.pressed = killAura.currentTarget != null
    }
}