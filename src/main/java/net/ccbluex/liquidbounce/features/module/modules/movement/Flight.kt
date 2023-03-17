package net.ccbluex.liquidbounce.features.module.modules.movement

import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.FloatValue
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.network.play.client.C03PacketPlayer
import net.minecraft.network.play.server.S08PacketPlayerPosLook
import org.lwjgl.input.Keyboard

@ModuleInfo(name = "Flight", description = "Fly in survival mode.", category = ModuleCategory.MOVEMENT)
class Flight : Module() {
    @JvmField
    val modeValue = ListValue(
            "Mode", arrayOf(
            "Vanilla",
            "Jartex"
    ), "Vanilla"
    )
    private val vanillaSpeedValue = FloatValue("Speed", 1f, 0f, 10f) {
        modeValue.get().equals("vanilla", ignoreCase = true)
    }
    private val canClipValue = BoolValue("CanClip", true) {
        modeValue.get().equals("jartex", ignoreCase = true)
    }
    private var waitFlag = false
    private var canGlide = false
    private var ticks = 0

    @EventTarget
    fun onUpdate(event: UpdateEvent?) {
        val vanillaSpeed = vanillaSpeedValue.get()
        val vanillaVSpeed = vanillaSpeedValue.get()
        mc.thePlayer.noClip = false
        if(modeValue.get().equals("vanilla", ignoreCase = true)){
            mc.thePlayer.capabilities.isFlying = false
            mc.thePlayer.motionY = 0.0
            mc.thePlayer.motionX = 0.0
            mc.thePlayer.motionZ = 0.0
            if (mc.gameSettings.keyBindJump.isKeyDown) {
                mc.thePlayer.motionY += vanillaVSpeed.toDouble()
            }
            if (Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)) {
                mc.thePlayer.motionY -= vanillaVSpeed.toDouble()
                mc.gameSettings.keyBindSneak.pressed = false
            }
            MovementUtils.strafe(vanillaSpeed)
        }
    }

    @EventTarget
    fun onMotion(event: MotionEvent) {
        if(modeValue.get().equals("jartex", ignoreCase = true)){
            if (event.eventState == EventState.PRE && canGlide) {
                mc.timer.timerSpeed = 1f
                mc.thePlayer.motionY = -if(ticks % 2 == 0) {
                    0.17
                } else {
                    0.10
                }
                if(ticks == 0) {
                    mc.thePlayer.motionY = -0.07
                }
                ticks++
            }
        }

    }
    @EventTarget
    fun onPacket(event: PacketEvent) {
        val packet = event.packet
        if(modeValue.get().equals("jartex", ignoreCase = true)){
            if(packet is S08PacketPlayerPosLook && waitFlag) {
                waitFlag = false
                mc.thePlayer.setPosition(packet.x, packet.y, packet.z)
                mc.netHandler.addToSendQueue(C03PacketPlayer.C06PacketPlayerPosLook(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, mc.thePlayer.rotationYaw, mc.thePlayer.rotationPitch, false))
                event.cancelEvent()
                mc.thePlayer.jump()
                clip(0.127318f, 0f)
                clip(3.425559f, 3.7f)
                clip(3.14285f, 3.54f)
                clip(2.88522f, 3.4f)
                canGlide = true
            }
        }

    }

    override fun onDisable() {
        if(modeValue.get().equals("vanilla", ignoreCase = true)){
            mc.thePlayer.motionY = 0.0
            mc.thePlayer.motionX = 0.0
            mc.thePlayer.motionZ = 0.0
        }
        super.onDisable()
    }
    override fun onEnable() {
        if(modeValue.get().equals("jartex", ignoreCase = true)){
            if(mc.thePlayer.onGround && canClipValue.get()) {
                clip(0f, -0.1f)
                waitFlag = true
                canGlide = false
                ticks = 0
                mc.timer.timerSpeed = 0.1f
            } else {
                waitFlag = false
                canGlide = true
            }
        }
        super.onEnable()
    }
    override val tag: String
        get() = modeValue.get()

    private fun clip(dist: Float, y: Float) {
        val yaw = Math.toRadians(mc.thePlayer.rotationYaw.toDouble())
        val x = -Math.sin(yaw) * dist
        val z = Math.cos(yaw) * dist
        mc.thePlayer.setPosition(mc.thePlayer.posX + x, mc.thePlayer.posY + y, mc.thePlayer.posZ + z)
        mc.netHandler.addToSendQueue(C03PacketPlayer.C04PacketPlayerPosition(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ, false))
    }
}