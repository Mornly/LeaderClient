package unfair.module.modules.movement;

import com.google.common.base.CaseFormat;
import io.netty.buffer.Unpooled;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.network.PacketBuffer;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.client.C09PacketHeldItemChange;
import net.minecraft.network.play.client.C17PacketCustomPayload;
import net.minecraft.util.BlockPos;
import unfair.Unfair;
import unfair.enums.BlinkModules;
import unfair.enums.FloatModules;
import unfair.event.EventTarget;
import unfair.event.types.EventType;
import unfair.event.types.Priority;
import unfair.events.*;
import unfair.module.Module;
import unfair.module.modules.combat.KillAura;
import unfair.property.properties.BooleanProperty;
import unfair.property.properties.IntProperty;
import unfair.property.properties.ModeProperty;
import unfair.property.properties.PercentProperty;
import unfair.util.*;

import java.util.Random;

public class NoSlow extends Module {
    private static final Minecraft mc = Minecraft.getMinecraft();
    public final ModeProperty swordMode = new ModeProperty("Sword Mode", 1, new String[]{"None", "Vanilla", "PredictionSemi", "Prediction"});
    public final IntProperty swapDelay = new IntProperty("Swap Delay", 0, 0, 3, () -> swordMode.getValue() == 3);
    public final BooleanProperty test = new BooleanProperty("Test", false, () -> swordMode.getValue() == 3);
    public final BooleanProperty c17 = new BooleanProperty("C17 Packet", false, () -> swordMode.getValue() == 3);
    public final BooleanProperty noAttack = new BooleanProperty("No Attack", false, () -> swordMode.getValue() == 3);
    public final IntProperty cancelTick = new IntProperty("Cancel Tick", 1, 0, 2, () -> swordMode.getValue() == 2);
    public final IntProperty cancelTick2 = new IntProperty("Cancel Tick 2", 1, 0, 2, () -> swordMode.getValue() == 2);
    public final PercentProperty swordMotion = new PercentProperty("Sword Motion", 100, () -> this.swordMode.getValue() != 0);
    public final BooleanProperty swordSprint = new BooleanProperty("Sword Sprint", true, () -> this.swordMode.getValue() != 0);
    public final BooleanProperty onlyKillAuraAutoBlock = new BooleanProperty("Only Kill Aura Auto Block", false, () -> this.swordMode.getValue() != 0);
    public final ModeProperty foodMode = new ModeProperty("Food Mode", 0, new String[]{"None", "Vanilla", "Float"});
    public final PercentProperty foodMotion = new PercentProperty("Food Motion", 100, () -> this.foodMode.getValue() != 0);
    public final BooleanProperty foodSprint = new BooleanProperty("Food Sprint", true, () -> this.foodMode.getValue() != 0);
    public final ModeProperty bowMode = new ModeProperty("Bow Mode", 0, new String[]{"None", "Vanilla", "Float"});
    public final PercentProperty bowMotion = new PercentProperty("Bow Motion", 100, () -> this.bowMode.getValue() != 0);
    public final BooleanProperty bowSprint = new BooleanProperty("Bow Sprint", true, () -> this.bowMode.getValue() != 0);
    private int lastSlot = -1;
    private int delay = 0;
    private boolean post = false;

    public NoSlow() {
        super("NoSlow", false);
    }

    public boolean isSwordActive() {
        return this.swordMode.getValue() != 0 && ItemUtil.isHoldingSword() && (!this.onlyKillAuraAutoBlock.getValue() || this.isKillAuraAutoBlocking());
    }

    public boolean isFoodActive() {
        return this.foodMode.getValue() != 0 && ItemUtil.isEating();
    }

    public boolean isBowActive() {
        return this.bowMode.getValue() != 0 && ItemUtil.isUsingBow();
    }

    public boolean isFloatMode() {
        return this.foodMode.getValue() == 2 && ItemUtil.isEating()
                || this.bowMode.getValue() == 2 && ItemUtil.isUsingBow();
    }

    private boolean isKillAuraAutoBlocking() {
       KillAura aura = (KillAura) Unfair.moduleManager.modules.get(KillAura.class);
        if (!aura.isPlayerBlocking() || !aura.isEnabled()) {
            return false;
        }
        return aura.isBlocking();
    }
    public boolean isAnyActive() {
        if (this.swordMode.getValue() != 2 && this.swordMode.getValue() != 3) {
            return mc.thePlayer.isUsingItem() && (this.isSwordActive() || this.isFoodActive() || this.isBowActive());
        }
        else if (this.swordMode.getValue() == 2 && isSwordActive()){
            KillAura killAura = (KillAura) Unfair.moduleManager.getModule(KillAura.class);
           return killAura.isEnabled() && killAura.shouldAutoBlock() && (killAura.blockTick == cancelTick.getValue() || killAura.blockTick == cancelTick2.getValue());
        }
        else if (swordMode.getValue() == 3 && isSwordActive()){
            KillAura killAura = (KillAura) Unfair.moduleManager.getModule(KillAura.class);
            if (!noAttack.getValue() || !((killAura.blockTick == 0 && killAura.autoBlock.getValue() == 2) || (killAura.autoBlock.getValue() == 6 && killAura.blockTick == killAura.attackTick.getValue()) || (killAura.autoBlock.getValue() != 6 && killAura.autoBlock.getValue() != 2) || (killAura.autoBlock.getValue() == 5 && killAura.blockTick == 0) && killAura.isEnabled() && killAura.isPlayerBlocking())) {
                return delay == 0;
            }
        }
        return false;
    }

    public boolean canSprint() {
        return this.isSwordActive() && this.swordSprint.getValue()
                || this.isFoodActive() && this.foodSprint.getValue()
                || this.isBowActive() && this.bowSprint.getValue();
    }

    public int getMotionMultiplier() {
        if (ItemUtil.isHoldingSword()) {
            return this.swordMotion.getValue();
        } else if (ItemUtil.isEating()) {
            return this.foodMotion.getValue();
        } else {
            return ItemUtil.isUsingBow() ? this.bowMotion.getValue() : 100;
        }
    }
    @EventTarget
    public void onUpdate(UpdateEvent event) {
        if (!this.isEnabled()) return;
        if (ItemUtil.isHoldingSword() && mc.thePlayer.isUsingItem()) {
            if (isSwordActive()) {
                if (this.swordMode.getValue() == 3) {
                    if (event.getType() == EventType.PRE) {
                        delay--;
                        if (delay < 0) {
                            KillAura killAura = (KillAura) Unfair.moduleManager.getModule(KillAura.class);
                            if (!noAttack.getValue() || !((killAura.blockTick == 0 && killAura.autoBlock.getValue() == 2) || (killAura.autoBlock.getValue() == 6 && killAura.blockTick == killAura.attackTick.getValue()) || (killAura.autoBlock.getValue() != 6 && killAura.autoBlock.getValue() != 2) || (killAura.autoBlock.getValue() == 5 && killAura.blockTick == 0) && killAura.isEnabled() && killAura.isPlayerBlocking())) {
                                int randomSlot = new Random().nextInt(9);
                                while (randomSlot == mc.thePlayer.inventory.currentItem) {
                                    randomSlot = new Random().nextInt(9);
                                }
                                if (test.getValue()) {
                                    Unfair.blinkManager.setBlinkState(true, BlinkModules.NO_SLOW);
                                }
                                PacketUtil.sendPacket(new C09PacketHeldItemChange(randomSlot));
                                if (c17.getValue()) {
                                    PacketUtil.sendPacket(new C17PacketCustomPayload("woshijiejue", new PacketBuffer(Unpooled.buffer())));
                                }
                                PacketUtil.sendPacket(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem));
                            }
                            post = true;
                            delay = swapDelay.getValue();
                        }
                    }
                }
            }
        }else {
            if (post){
                if (test.getValue()) {
                    int randomSlot = new Random().nextInt(9);
                    while (randomSlot == mc.thePlayer.inventory.currentItem) {
                        randomSlot = new Random().nextInt(9);
                    }
                    Unfair.blinkManager.setBlinkState(false, BlinkModules.NO_SLOW);
                    PacketUtil.sendPacket(new C09PacketHeldItemChange(randomSlot));
                    if (c17.getValue()) {
                        PacketUtil.sendPacket(new C17PacketCustomPayload("woshijiejue", new PacketBuffer(Unpooled.buffer())));
                    }
                    PacketUtil.sendPacket(new C09PacketHeldItemChange(mc.thePlayer.inventory.currentItem));
                }
                post = false;
            }
        }
    }
    @EventTarget
    public void onMotion(PostMotionEvent event){
        if (!this.isEnabled()) return;
        if (!ItemUtil.isHoldingSword() || !mc.thePlayer.isUsingItem()) return;
        if (isSwordActive()) {
            if (this.swordMode.getValue() == 3) {
                if (post) {
                    post = false;
                    if (test.getValue()) {
                        PacketUtil.sendPacket(new C08PacketPlayerBlockPlacement(mc.thePlayer.getHeldItem()));
                        Unfair.blinkManager.setBlinkState(false, BlinkModules.NO_SLOW);
                    }
                }
            }
        }
    }

    @EventTarget
    public void onLivingUpdate(LivingUpdateEvent event) {
        if (this.isEnabled() && this.isAnyActive()) {
            float multiplier = (float) this.getMotionMultiplier() / 100.0F;
            mc.thePlayer.movementInput.moveForward *= multiplier;
            mc.thePlayer.movementInput.moveStrafe *= multiplier;
            if (!this.canSprint()) {
                mc.thePlayer.setSprinting(false);
            }
        }
    }

    @EventTarget(Priority.LOW)
    public void onPlayerUpdate(PlayerUpdateEvent event) {

        if (this.isEnabled() && this.isFloatMode()) {
            int item = mc.thePlayer.inventory.currentItem;
            if (this.lastSlot != item && PlayerUtil.isUsingItem()) {
                this.lastSlot = item;
                Unfair.floatManager.setFloatState(true, FloatModules.NO_SLOW);
            }
        } else {
            this.lastSlot = -1;
            Unfair.floatManager.setFloatState(false, FloatModules.NO_SLOW);
        }
    }

    @EventTarget
    public void onRightClick(RightClickMouseEvent event) {
        if (this.isEnabled()) {
            if (mc.objectMouseOver != null) {
                switch (mc.objectMouseOver.typeOfHit) {
                    case BLOCK:
                        BlockPos blockPos = mc.objectMouseOver.getBlockPos();
                        if (BlockUtil.isInteractable(blockPos) && !PlayerUtil.isSneaking()) {
                            return;
                        }
                        break;
                    case ENTITY:
                        Entity entityHit = mc.objectMouseOver.entityHit;
                        if (entityHit instanceof EntityVillager) {
                            return;
                        }
                        if (entityHit instanceof EntityLivingBase && TeamUtil.isShop((EntityLivingBase) entityHit)) {
                            return;
                        }
                }
            }
            if (this.isFloatMode() && !Unfair.floatManager.isPredicted() && mc.thePlayer.onGround) {
                event.setCancelled(true);
                mc.thePlayer.motionY = 0.42F;
            }
        }
    }

    @Override
    public String[] getSuffix() {
        return new String[]{CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, this.swordMode.getModeString())};
    }
}
