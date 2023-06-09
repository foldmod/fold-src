
package net.ccbluex.liquidbounce.features.module.modules.render

import net.ccbluex.liquidbounce.features.module.*
import net.ccbluex.liquidbounce.value.FloatValue

@ModuleInfo(name = "ItemPhysics", spacedName = "Item Physics", description = "newton hits", category = ModuleCategory.RENDER)
class ItemPhysics : Module() {
    val itemWeight = FloatValue("Weight", 0.5F, 0F, 1F, "x")
    override val tag: String?
        get() = "${itemWeight.get()}"
}
