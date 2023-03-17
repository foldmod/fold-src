package net.ccbluex.liquidbounce.features.module.modules.movement

import net.ccbluex.liquidbounce.LiquidBounce
import net.ccbluex.liquidbounce.event.*
import net.ccbluex.liquidbounce.features.module.Module
import net.ccbluex.liquidbounce.features.module.ModuleCategory
import net.ccbluex.liquidbounce.features.module.ModuleInfo
import net.ccbluex.liquidbounce.features.module.modules.combat.KillAura
import net.ccbluex.liquidbounce.features.module.modules.world.Scaffold
import net.ccbluex.liquidbounce.utils.MovementUtils
import net.ccbluex.liquidbounce.utils.PacketUtils
import net.ccbluex.liquidbounce.utils.PosLookInstance
import net.ccbluex.liquidbounce.utils.timer.MSTimer
import net.ccbluex.liquidbounce.value.BoolValue
import net.ccbluex.liquidbounce.value.ListValue
import net.minecraft.client.settings.GameSettings
import net.minecraft.network.Packet
import net.minecraft.network.play.INetHandlerPlayServer
import net.minecraft.network.play.client.*
import net.minecraft.network.play.server.S12PacketEntityVelocity
import net.minecraft.util.BlockPos
import net.minecraft.util.EnumFacing
import java.util.*
import java.util.concurrent.LinkedBlockingQueue
import kotlin.math.abs

@ModuleInfo(name = "Speed", description = "Allows you to move faster.", category = ModuleCategory.MOVEMENT)
class NewSpeed : Module() {
    // Jartex Speed Shit
    private val linkedQueue: Queue<Packet<*>> = LinkedBlockingQueue()
    private var rotatingSpeed = 0F
    private val keepAlives = arrayListOf<C00PacketKeepAlive>()
    private val transactions = arrayListOf<C0FPacketConfirmTransaction>()
    private val packetQueue = LinkedList<C0FPacketConfirmTransaction>()
    private val anotherQueue = LinkedList<C00PacketKeepAlive>()
    private val playerQueue = LinkedList<C03PacketPlayer>()
    private val packetBus = hashMapOf<Long, Packet<INetHandlerPlayServer>>()
    private val queueBus = LinkedList<Packet<INetHandlerPlayServer>>()
    private val packetBuffer = LinkedList<Packet<INetHandlerPlayServer>>()
    private val posLookInstance = PosLookInstance()
    private val msTimer = MSTimer()
    private val wdTimer = MSTimer()
    private val benTimer = MSTimer()
    private val pulseTimer = MSTimer()
    private var disableLogger = false
    private var alrSendY = false
    private var alrSprint = false
    private var expectedSetback = false
    private var sendDelay = 0
    private var shouldActive = false
    private var benHittingLean = false
    private var transCount = 0
    private var counter = 0
    private var canModify = false
    var shouldModifyRotation = false
    private var lastTick = 0
    private var s08count = 0
    private var ticking = 0
    private var lastYaw = 0F
    private var lastUid = 0
    private var currentTrans = 0
    private var currentDelay = 5000
    private var currentBuffer = 4
    private var currentDec = -1
    private val lagTimer = MSTimer()
    private val decTimer = MSTimer()
    private var runReset = false

    @JvmField
    val modeValue = ListValue(
            "Mode", arrayOf(
            "Hypixel",
            "NCP",
            "Jartex",
            "BlocksMC"
    ), "Hypixel"
    )
    private val hypixel = BoolValue("Hypixel", false){ modeValue.get().equals("ncp", true)}

    override val tag: String
        get() = modeValue.get()

    private var wasTimer = false
    private var damagedTicks = 0
    @EventTarget
    fun onUpdate(event: UpdateEvent) {
        if(modeValue.get() == "Hypixel"){
            if (wasTimer) {
                mc.timer.timerSpeed = 1.00f
                wasTimer = false
            }
            if (abs(mc.thePlayer.movementInput.moveStrafe) < 0.1f) {
                mc.thePlayer.jumpMovementFactor = 0.026499f
            } else {
                mc.thePlayer.jumpMovementFactor = 0.0244f
            }
            mc.gameSettings.keyBindJump.pressed = GameSettings.isKeyDown(mc.gameSettings.keyBindJump)

            if (MovementUtils.getSpeed() < 0.215f && !mc.thePlayer.onGround) {
                MovementUtils.strafe(0.215f)
            }
            if (mc.thePlayer.onGround && MovementUtils.isMoving()) {
                mc.gameSettings.keyBindJump.pressed = false
                mc.thePlayer.jump()
                mc.timer.timerSpeed = 1.0f
                wasTimer = true
                MovementUtils.strafe()
                if (MovementUtils.getSpeed() < 0.5f) {
                    MovementUtils.strafe(0.4849f)
                }
            } else if (!MovementUtils.isMoving()) {
                mc.timer.timerSpeed = 1.00f
            }
            if (damagedTicks > 2) {
                MovementUtils.strafe(MovementUtils.getSpeed() * 0.95f)
            }
            damagedTicks -= 1
        }
        else if(modeValue.get() == "NCP"){
            if (MovementUtils.isMoving()) {
                if (mc.thePlayer.onGround) {
                    mc.thePlayer.jump()
                    mc.thePlayer.speedInAir = 0.0223f
                }
                if(hypixel.get()){
                    if (damagedTicks > 2) {
                        MovementUtils.strafe(MovementUtils.getSpeed() * 0.95f)
                    }
                    damagedTicks -= 1
                } else {
                    MovementUtils.strafe()
                }

            } else {
                mc.thePlayer.motionX = 0.0
                mc.thePlayer.motionZ = 0.0
            }
        }
        else if(modeValue.get() == "Jartex"){
            if (mc.thePlayer.ticksExisted % 5 == 0)
                mc.netHandler.addToSendQueue(
                        C07PacketPlayerDigging(
                                C07PacketPlayerDigging.Action.STOP_DESTROY_BLOCK,
                                BlockPos(-1, -1, -1),
                                EnumFacing.UP
                        )
                )

            if (MovementUtils.isMoving()) {
                if (mc.thePlayer.onGround) {
                    mc.thePlayer.jump()
                    mc.thePlayer.speedInAir = 0.0223f
                }
                MovementUtils.strafe()
            }
        }
        else if (modeValue.get().equals("BlocksMC", true)) {
            if (!mc.thePlayer.isInWeb && !mc.thePlayer.isInLava && !mc.thePlayer.isInWater && !mc.thePlayer.isOnLadder && mc.thePlayer.ridingEntity == null) {
                if (MovementUtils.isMoving()) {
                    mc.gameSettings.keyBindJump.pressed = false
                    if (mc.thePlayer.onGround) {
                        mc.thePlayer.jump()
                        MovementUtils.strafe(0.48f)
                    }
                    MovementUtils.strafe()
                }
            }
        }
    }
    @EventTarget
    fun onPacket(event: PacketEvent) {
        val packet = event.packet
        if(modeValue.get() == "Hypixel"){
            if (packet is S12PacketEntityVelocity) {
                if (mc.thePlayer == null || (mc.theWorld?.getEntityByID(packet.entityID) ?: return) != mc.thePlayer) {
                    return
                }

                if (packet.motionY / 8000.0 > 0.1) {
                    damagedTicks = 15
                }
            }
        }
        else if(modeValue.get() == "Jartex"){
            if (mc.thePlayer.ticksExisted % 2 == 0) {
                if (packet is C03PacketPlayer.C04PacketPlayerPosition || packet is C03PacketPlayer.C06PacketPlayerPosLook ||
                        packet is C08PacketPlayerBlockPlacement ||
                        packet is C0APacketAnimation ||
                        packet is C0BPacketEntityAction || packet is C02PacketUseEntity
                ) {
                    event.cancelEvent()
                    packetBuffer.add(packet as Packet<INetHandlerPlayServer>)
                }
            }
        }
        else if(modeValue.get().equals("NCP", true)){
            if(hypixel.get()){
                if (packet is S12PacketEntityVelocity) {
                    if (mc.thePlayer == null || (mc.theWorld?.getEntityByID(packet.entityID) ?: return) != mc.thePlayer) {
                        return
                    }

                    if (packet.motionY / 8000.0 > 0.1) {
                        damagedTicks = 15
                    }
                }
            }
        }
    }
    @EventTarget
    fun onWorld(event: WorldEvent) {
        if(modeValue.get() == "Jartex"){
            packetBuffer.clear()
            transactions.clear()
            keepAlives.clear()
            packetQueue.clear()
            anotherQueue.clear()
            playerQueue.clear()
            packetBus.clear()
            queueBus.clear()
            canModify = false

            s08count = 0

            msTimer.reset()
            wdTimer.reset()
            benTimer.reset()
            expectedSetback = false
            shouldActive = false
            alrSendY = false
            alrSprint = false
            benHittingLean = false
            transCount = 0
            counter = 0
            lastTick = 0
            ticking = 0
            lastUid = 0
            posLookInstance.reset()

            rotatingSpeed = 0F
        }
    }
    @EventTarget(priority = 2)
    fun onMotion(event: MotionEvent) {
        if(modeValue.get() == "Jartex") {
            val killAura = LiquidBounce.moduleManager.getModule(KillAura::class.java)!!
            val speed = LiquidBounce.moduleManager.getModule(Speed::class.java)!!
            val fly = LiquidBounce.moduleManager.getModule(Flight::class.java)!!
            if (event.eventState == EventState.PRE)
                shouldModifyRotation = false
        }
    }
    override fun onDisable() {
        val scaffoldModule = LiquidBounce.moduleManager.getModule(Scaffold::class.java)
        if(modeValue.get() == "Hypixel"){
            if (!mc.thePlayer.isSneaking && !scaffoldModule!!.state)
                MovementUtils.strafe(0.2f)
        }
        else if(modeValue.get() == "NCP"){
            mc.thePlayer.speedInAir = 0.02f
            mc.timer.timerSpeed = 1f
        }
        else if(modeValue.get() == "Jartex") {
            mc.thePlayer.speedInAir = 0.02f
            mc.timer.timerSpeed = 1f

            val scaffold = LiquidBounce.moduleManager.getModule(Scaffold::class.java)

            if (!mc.thePlayer.isSneaking && !scaffold!!.state) MovementUtils.strafe(0.2f)

            keepAlives.forEach {
                PacketUtils.sendPacketNoEvent(it)
            }
            transactions.forEach {
                PacketUtils.sendPacketNoEvent(it)
            }

            packetBuffer.clear()
            keepAlives.clear()
            transactions.clear()
            packetQueue.clear()
            anotherQueue.clear()
            packetBus.clear()
            queueBus.clear()

            msTimer.reset()

            mc.thePlayer.motionY = 0.0
            MovementUtils.strafe(0F)
            mc.timer.timerSpeed = 1F

            shouldModifyRotation = false
        }
    }

    override fun onEnable() {
        if(modeValue.get() == "NCP"){
            mc.timer.timerSpeed = 1.0865f
        }
        else if(modeValue.get() == "Jartex"){
            mc.timer.timerSpeed = 1.0865f
            packetBuffer.clear()
            keepAlives.clear()
            transactions.clear()
            packetQueue.clear()
            anotherQueue.clear()
            playerQueue.clear()
            packetBus.clear()
            queueBus.clear()

            s08count = 0

            pulseTimer.reset()
            msTimer.reset()
            wdTimer.reset()
            benTimer.reset()
            canModify = false
            expectedSetback = false
            shouldActive = false
            alrSendY = false
            alrSprint = false
            transCount = 0
            lastTick = 0
            ticking = 0

            lastUid = 0
            posLookInstance.reset()

            shouldModifyRotation = false
            benHittingLean = false

            rotatingSpeed = 0F
        }
        super.onEnable()
    }
}