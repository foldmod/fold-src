package net.ccbluex.liquidbounce.features.module.modules.movement;

import kotlin.jvm.JvmField;
import net.ccbluex.liquidbounce.LiquidBounce;
import net.ccbluex.liquidbounce.event.EventTarget;
import net.ccbluex.liquidbounce.event.UpdateEvent;
import net.ccbluex.liquidbounce.features.module.Module;
import net.ccbluex.liquidbounce.features.module.ModuleCategory;
import net.ccbluex.liquidbounce.features.module.ModuleInfo;
import net.ccbluex.liquidbounce.value.ListValue;
import org.lwjgl.input.Keyboard;

@ModuleInfo(name = "Fly", description = "Fly in survival mode.", category = ModuleCategory.MOVEMENT)
public class Fly extends Module {
    public boolean isMoving() {
        return mc.thePlayer != null && (mc.thePlayer.movementInput.moveForward != 0F || mc.thePlayer.movementInput.moveStrafe != 0F);
    }
    public void strafe(final float speed) {
        if(!isMoving())
            return;

        final double yaw = getDirection();
        mc.thePlayer.motionX = -Math.sin(yaw) * speed;
        mc.thePlayer.motionZ = Math.cos(yaw) * speed;
    }
    public double getDirection() {
        return getDirectionRotation(mc.thePlayer.rotationYaw, mc.thePlayer.moveStrafing, mc.thePlayer.moveForward);
    }
    public double getDirectionRotation(float yaw, float pStrafe, float pForward) {
        float rotationYaw = yaw;

        if(pForward < 0F)
            rotationYaw += 180F;

        float forward = 1F;
        if(pForward < 0F)
            forward = -0.5F;
        else if(pForward > 0F)
            forward = 0.5F;

        if(pStrafe > 0F)
            rotationYaw -= 90F * forward;

        if(pStrafe < 0F)
            rotationYaw += 90F * forward;

        return Math.toRadians(rotationYaw);
    }
    @EventTarget
    public void onUpdate(UpdateEvent event){
        mc.thePlayer.capabilities.isFlying = false;
        mc.thePlayer.motionY = 0.0;
        mc.thePlayer.motionX = 0.0;
        mc.thePlayer.motionZ = 0.0;
        if(mc.gameSettings.keyBindJump.isKeyDown()){
            mc.thePlayer.motionY += 1.0;
        }
        if(Keyboard.isKeyDown(Keyboard.KEY_LSHIFT)){
            mc.thePlayer.motionY -= 1.0;
            mc.gameSettings.keyBindSneak.pressed = false;
        }
    }
    @Override
    public void onDisable() {
        mc.thePlayer.motionY = 0.0;
        mc.thePlayer.motionX = 0.0;
        mc.thePlayer.motionZ = 0.0;
    }
}
