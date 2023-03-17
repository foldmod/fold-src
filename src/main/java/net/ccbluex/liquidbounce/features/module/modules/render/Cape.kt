
package net.ccbluex.liquidbounce.features.module.modules.render

import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.value.ListValue

import net.minecraft.util.ResourceLocation

@ModuleInfo(name = "Cape", description = "fold  capes.", category = ModuleCategory.RENDER)
class Cape : Module() {

    val styleValue = ListValue("Style", arrayOf("Dark", "Darker", "Light", "Special1", "Special2"), "Dark")

    private val capeCache = hashMapOf<String, CapeStyle>()

    fun getCapeLocation(value: String): ResourceLocation {
        if (capeCache[value.toUpperCase()] == null) {
            try {
                capeCache[value.toUpperCase()] = CapeStyle.valueOf(value.toUpperCase())
            } catch (e: Exception) {
                capeCache[value.toUpperCase()] = CapeStyle.DARK
            }
        }
        return capeCache[value.toUpperCase()]!!.location
    }

    enum class CapeStyle(val location: ResourceLocation) {
        DARK(ResourceLocation("fold/cape/dark.png")),
        DARKER(ResourceLocation("fold/cape/darker.png")),
        LIGHT(ResourceLocation("fold/cape/light.png")),
        SPECIAL1(ResourceLocation("fold/cape/special1.png")),
        SPECIAL2(ResourceLocation("fold/cape/special2.png"))
    }

    override val tag: String
        get() = styleValue.get()

}