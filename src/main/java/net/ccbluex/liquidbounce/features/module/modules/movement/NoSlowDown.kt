package net.ccbluex.liquidbounce.features.module.modules.movement

import com.sun.xml.internal.bind.v2.runtime.reflect.Lister.Pack
import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.module.modules.combat.KillAura
import net.ccbluex.liquidbounce.utils.ClientUtils
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.utils.PacketUtils
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.entity.player.InventoryPlayer
import net.minecraft.item.*
import net.minecraft.network.play.client.C07PacketPlayerDigging
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement
import net.minecraft.network.play.client.C09PacketHeldItemChange
import net.minecraft.network.play.server.S30PacketWindowItems
import net.minecraft.util.BlockPos
import net.minecraft.util.EnumFacing

@ModuleInfo(name = "NoSlow", description = "Stop slowing down!", category = ModuleCategory.MOVEMENT)
class NoSlowDown : Module() {
    private val msTimer = MSTimer()

    @JvmField
    val modeValue = ListValue(
            "Mode", arrayOf(
            "NCP",
            "OldNCP",
            "Vanilla"
    ), "Vanilla"
    )
    private val swordOnly = BoolValue("Sword Only", false)
    override val tag: String
        get() = modeValue.get()

    @EventTarget
    fun onPacket(event: PacketEvent) {
        val packet = event.packet
        val killAura = LiquidBounce.moduleManager[KillAura::class.java]!! as KillAura

        if (modeValue.get().equals("ncp", true) && packet is S30PacketWindowItems && (mc.thePlayer.isUsingItem || mc.thePlayer.isBlocking)) {
            event.cancelEvent()
        }
    }

    @EventTarget
    fun onMotion(event: MotionEvent) {
        if (!MovementUtils.isMoving())
            return

        val heldItem = mc.thePlayer.heldItem
        val killAura = LiquidBounce.moduleManager[KillAura::class.java]!! as KillAura
        fun sendPacket(event: MotionEvent, sendC07: Boolean, sendC08: Boolean, delay: Boolean, delayValue: Long, onGround: Boolean, watchDog: Boolean = false) {
            val digging = C07PacketPlayerDigging(C07PacketPlayerDigging.Action.RELEASE_USE_ITEM, BlockPos(-1, -1, -1), EnumFacing.DOWN)
            val blockPlace = C08PacketPlayerBlockPlacement(mc.thePlayer.inventory.getCurrentItem())
            val blockMent = C08PacketPlayerBlockPlacement(BlockPos(-1, -1, -1), 255, mc.thePlayer.inventory.getCurrentItem(), 0f, 0f, 0f)
            if (onGround && !mc.thePlayer.onGround) {
                return
            }
            if (sendC07 && event.eventState == EventState.PRE) {
                if (delay && msTimer.hasTimePassed(delayValue)) {
                    mc.netHandler.addToSendQueue(digging)
                } else if (!delay) {
                    mc.netHandler.addToSendQueue(digging)
                }
            }
            if (sendC08 && event.eventState == EventState.POST) {
                if (delay && msTimer.hasTimePassed(delayValue) && !watchDog) {
                    mc.netHandler.addToSendQueue(blockPlace)
                    msTimer.reset()
                } else if (!delay && !watchDog) {
                    mc.netHandler.addToSendQueue(blockPlace)
                } else if (watchDog) {
                    mc.netHandler.addToSendQueue(blockMent)
                }
            }
        }
        if (modeValue.get().equals("OldNCP", ignoreCase = true)) {
            if (mc.thePlayer.ticksExisted % 2 == 0)
                sendPacket(event, true, false, false, 50, true)
            else
                sendPacket(event, false, true, false, 0, true, false)
        } else if (modeValue.get().equals("NCP", ignoreCase = true)) {
            if ((!killAura.state || !killAura.blockingStatus)
                    && event.eventState == EventState.PRE
                    && mc.thePlayer.itemInUse != null && mc.thePlayer.itemInUse.item != null) {
                val item = mc.thePlayer.itemInUse.item
                if (mc.thePlayer.isUsingItem && (item is ItemFood || item is ItemBucketMilk || item is ItemPotion) && mc.thePlayer.getItemInUseCount() >= 1) {
                    PacketUtils.sendPacketNoEvent(C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem % 8 + 1))
                    PacketUtils.sendPacketNoEvent(C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem))
                }
            }
        }

    }
    @EventTarget
    fun onSlowDown(event: SlowDownEvent) {
        val heldItem = mc.thePlayer.heldItem?.item
        if (swordOnly.get()) {
            if(heldItem is ItemFood || heldItem is ItemBucketMilk || heldItem is ItemPotion || heldItem is ItemBow) {
                return
            } else {
                event.forward = 1.0F
                event.strafe = 1.0F
            }
        }
        else {
            event.forward = 1.0F
            event.strafe = 1.0F
        }
    }

}