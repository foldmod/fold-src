
package net.ccbluex.liquidbounce.features.command.commands

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.EventTarget
import net.ccbluex.liquidbounce.event.Listenable
import net.ccbluex.liquidbounce.event.Render2DEvent
import net.ccbluex.liquidbounce.event.UpdateEvent
import net.ccbluex.liquidbounce.features.command.Command
import net.ccbluex.liquidbounce.utils.ClientUtils
import net.ccbluex.liquidbounce.utils.render.RenderUtils
import net.minecraft.client.gui.ScaledResolution
import net.minecraft.util.ResourceLocation

class TacoCommand : Command("taco", emptyArray()), Listenable {
    private var toggle = false
    private var image = 0
    private var running = 0f
    private val tacoTextures = arrayOf(
            ResourceLocation("fold/taco/1.png"),
            ResourceLocation("fold/taco/2.png"),
            ResourceLocation("fold/taco/3.png"),
            ResourceLocation("fold/taco/4.png"),
            ResourceLocation("fold/taco/5.png"),
            ResourceLocation("fold/taco/6.png"),
            ResourceLocation("fold/taco/7.png"),
            ResourceLocation("fold/taco/8.png"),
            ResourceLocation("fold/taco/9.png"),
            ResourceLocation("fold/taco/10.png"),
            ResourceLocation("fold/taco/11.png"),
            ResourceLocation("fold/taco/12.png")
    )

    init {
        LiquidBounce.eventManager.registerListener(this)
    }

    /**
     * Execute commands with provided [args]
     */
    override fun execute(args: Array<String>) {
        toggle = !toggle
        ClientUtils.displayChatMessage(if (toggle) "§aTACO TACO TACO. :)" else "§cYou made the little taco sad! :(")
    }

    @EventTarget
    fun onRender2D(event: Render2DEvent) {
        if (!toggle)
            return

        running += 0.15f * RenderUtils.deltaTime
        val scaledResolution = ScaledResolution(mc)
        RenderUtils.drawImage(tacoTextures[image], running.toInt(), scaledResolution.scaledHeight - 60, 64, 32)
        if (scaledResolution.scaledWidth <= running)
            running = -64f
    }

    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        if (!toggle) {
            image = 0
            return
        }

        image++
        if (image >= tacoTextures.size) image = 0
    }

    override fun handleEvents() = true

    override fun tabComplete(args: Array<String>): List<String> {
        return listOf("TACO")
    }
}