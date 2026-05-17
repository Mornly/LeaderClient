package unfair.module.modules.movement;

import com.google.common.base.CaseFormat;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraft.util.*;
import unfair.Unfair;
import unfair.enums.BlinkModules;
import unfair.event.EventTarget;
import unfair.event.types.EventType;
import unfair.event.types.Priority;
import unfair.events.PacketEvent;
import unfair.events.TickEvent;
import unfair.events.UpdateEvent;
import unfair.mixin.IAccessorC03PacketPlayer;
import unfair.mixin.IAccessorMinecraft;
import unfair.mixin.IAccessorPlayerControllerMP;
import unfair.module.Module;
import unfair.property.properties.BooleanProperty;
import unfair.property.properties.FloatProperty;
import unfair.property.properties.IntProperty;
import unfair.property.properties.ModeProperty;
import unfair.util.*;

public class NoFall extends Module {
    private static final Minecraft mc = Minecraft.getMinecraft();
    public final ModeProperty mode = new ModeProperty("mode", 0, new String[]{"PACKET", "BLINK", "NO_GROUND", "SPOOF","WATER_BUCKET"});
    public final FloatProperty distance = new FloatProperty("distance", 3.0F, 0.0F, 20.0F);
    public final IntProperty delay = new IntProperty("delay", 0, 0, 10000);
    public  final BooleanProperty silent = new BooleanProperty("Rotation",false,() -> this.mode.getValue() == 4);
    private final TimerUtil packetDelayTimer = new TimerUtil();
    private final TimerUtil scoreboardResetTimer = new TimerUtil();
    private boolean slowFalling = false;
    private boolean lastOnGround = false;
    private int lastSlot = -1;
    private final long PLACE_DELAY = 500L;
    private long lastPlace = 0L;
    private final long PICKUP_WAIT = 150L;

    public NoFall() {
        super("NoFall", false);
    }

    private boolean canTrigger() {
        return this.scoreboardResetTimer.hasTimeElapsed(3000) && this.packetDelayTimer.hasTimeElapsed(this.delay.getValue().longValue());
    }

    @EventTarget(Priority.HIGH)
    public void onPacket(PacketEvent event) {
        if (event.getType() == EventType.RECEIVE && event.getPacket() instanceof S08PacketPlayerPosLook) {
            this.onDisabled();
        } else if (this.isEnabled() && event.getType() == EventType.SEND && !event.isCancelled()) {
            if (event.getPacket() instanceof C03PacketPlayer) {
                C03PacketPlayer packet = (C03PacketPlayer) event.getPacket();
                switch (this.mode.getValue()) {
                    case 0:
                        if (this.slowFalling) {
                            this.slowFalling = false;
                            ((IAccessorMinecraft) mc).getTimer().timerSpeed = 1.0F;
                        } else if (!packet.isOnGround()) {
                            AxisAlignedBB aabb = mc.thePlayer.getEntityBoundingBox().expand(2.0, 0.0, 2.0);
                            if (PlayerUtil.canFly(this.distance.getValue())
                                    && !PlayerUtil.checkInWater(aabb)
                                    && this.canTrigger()) {
                                this.packetDelayTimer.reset();
                                this.slowFalling = true;
                                ((IAccessorMinecraft) mc).getTimer().timerSpeed = 0.5F;
                            }
                        }
                        break;
                    case 1:
                        boolean allowed = !mc.thePlayer.isOnLadder() && !mc.thePlayer.capabilities.allowFlying && mc.thePlayer.hurtTime == 0;
                        if (Unfair.blinkManager.getBlinkingModule() != BlinkModules.NO_FALL) {
                            if (this.lastOnGround
                                    && !packet.isOnGround()
                                    && allowed
                                    && PlayerUtil.canFly(this.distance.getValue().intValue())
                                    && mc.thePlayer.motionY < 0.0) {
                                Unfair.blinkManager.setBlinkState(false, Unfair.blinkManager.getBlinkingModule());
                                Unfair.blinkManager.setBlinkState(true, BlinkModules.NO_FALL);
                            }
                        } else if (!allowed) {
                            Unfair.blinkManager.setBlinkState(false, BlinkModules.NO_FALL);
                            ChatUtil.sendFormatted(String.format("%s%s: &cFailed player check!&r", Unfair.clientName, this.getName()));
                        } else if (PlayerUtil.checkInWater(mc.thePlayer.getEntityBoundingBox().expand(2.0, 0.0, 2.0))) {
                            Unfair.blinkManager.setBlinkState(false, BlinkModules.NO_FALL);
                            ChatUtil.sendFormatted(String.format("%s%s: &cFailed void check!&r", Unfair.clientName, this.getName()));
                        } else if (packet.isOnGround()) {
                            for (Packet<?> blinkedPacket : Unfair.blinkManager.blinkedPackets) {
                                if (blinkedPacket instanceof C03PacketPlayer) {
                                    ((IAccessorC03PacketPlayer) blinkedPacket).setOnGround(true);
                                }
                            }
                            Unfair.blinkManager.setBlinkState(false, BlinkModules.NO_FALL);
                            this.packetDelayTimer.reset();
                        }
                        this.lastOnGround = packet.isOnGround() && allowed && this.canTrigger();
                        break;
                    case 2:
                        ((IAccessorC03PacketPlayer) packet).setOnGround(false);
                        break;
                    case 3:
                        if (!packet.isOnGround()) {
                            AxisAlignedBB aabb = mc.thePlayer.getEntityBoundingBox().expand(2.0, 0.0, 2.0);
                            if (PlayerUtil.canFly(this.distance.getValue())
                                    && !PlayerUtil.checkInWater(aabb)
                                    && this.canTrigger()) {
                                this.packetDelayTimer.reset();
                                ((IAccessorC03PacketPlayer) packet).setOnGround(true);
                                mc.thePlayer.fallDistance = 0.0F;
                            }
                        }
                }
            }
        }
    }

    @EventTarget(Priority.HIGHEST)
    public void onTick(TickEvent event) {
        if (this.isEnabled() && event.getType() == EventType.PRE) {
            if (this.mode.getValue() == 4){
                if (mc.isGamePaused() || mc.thePlayer.capabilities.isFlying || mc.thePlayer.capabilities.isCreativeMode) {
                    return;
                }
                if (!fallCheck()) {
                    return;
                }
                MovingObjectPosition mop = getTarget(mc.playerController.getBlockReachDistance(), mc.thePlayer.rotationYaw, silent.getValue() ? 90.0f : mc.thePlayer.rotationPitch);
                if (mop == null || mop.typeOfHit != MovingObjectPosition.MovingObjectType.BLOCK || mop.sideHit != EnumFacing.UP) {
                    return;
                }
                long now = System.currentTimeMillis();
                if (timeBetween(lastPlace, now) < PLACE_DELAY) {
                    return;
                }
                if (!isItem(mc.thePlayer.getHeldItem(), Items.water_bucket)) {
                    this.attemptSwitch();
                }
                if (silent.getValue() && mc.thePlayer.rotationPitch < 85.0f) {
                    return;
                }
                lastPlace = now;
                this.useCurrentItem();
            }
            if (ServerUtil.hasPlayerCountInfo()) {
                this.scoreboardResetTimer.reset();
            }
            if (this.mode.getValue() == 0 && this.slowFalling) {
                PacketUtil.sendPacketNoEvent(new C03PacketPlayer(true));
                mc.thePlayer.fallDistance = 0.0F;
            }
        }
    }
    @EventTarget
    public void onUpdate(UpdateEvent e){
        if (this.isEnabled() && e.getType() == EventType.PRE){
            if (this.mode.getValue() == 4) {
                if (silent.getValue() && (fallCheck() || timeBetween(lastPlace, System.currentTimeMillis()) < PLACE_DELAY) && getWaterBucketSlot() != -1) {
                    mc.thePlayer.rotationPitch = 90f;
                }
                if (timeBetween(lastPlace, System.currentTimeMillis()) > PICKUP_WAIT && isItem(mc.thePlayer.getHeldItem(), Items.bucket)) {
                    this.useCurrentItem();
                    if (this.lastSlot != -1) {
                        mc.thePlayer.inventory.currentItem = lastSlot;
                        ((IAccessorPlayerControllerMP) mc.playerController).callSyncCurrentPlayItem();
                        this.lastSlot = -1;
                    }
                }
            }
        }
    }

    @Override
    public void onDisabled() {
        this.lastOnGround = false;
        Unfair.blinkManager.setBlinkState(false, BlinkModules.NO_FALL);
        if (this.slowFalling) {
            this.slowFalling = false;
            ((IAccessorMinecraft) mc).getTimer().timerSpeed = 1.0F;
        }
        this.lastPlace = 0L;
        this.lastSlot = -1;
    }

    @Override
    public void verifyValue(String mode) {
        if (this.isEnabled()) {
            this.onDisabled();
        }
    }
    private void attemptSwitch() {
        int slot = getWaterBucketSlot();
        if (slot != -1) {
            this.lastSlot = mc.thePlayer.inventory.currentItem;
            mc.thePlayer.inventory.currentItem = slot;
            ((IAccessorPlayerControllerMP) mc.playerController).callSyncCurrentPlayItem();
        }
    }

    private int getWaterBucketSlot() {
        for (int slot = 0; slot < InventoryPlayer.getHotbarSize(); ++slot) {
            if (isItem(mc.thePlayer.inventory.getStackInSlot(slot), Items.water_bucket)) {
                return slot;
            }
        }
        return -1;
    }
    public static MovingObjectPosition getTarget(final double reach, final float yaw, final float pitch) {
        Vec3 eyeVec = mc.thePlayer.getPositionEyes(1.0f);
        float y = -yaw * 0.017453292f;
        float p = -pitch * 0.017453292f;
        float f = MathHelper.cos(y - 3.1415927f);
        float f2 = MathHelper.sin(y - 3.1415927f);
        float f3 = -MathHelper.cos(p);
        float f4 = MathHelper.sin(p);
        Vec3 lookVec = new Vec3(f2 * f3, f4, f * f3);
        Vec3 sumVec = eyeVec.addVector(lookVec.xCoord * reach, lookVec.yCoord * reach, lookVec.zCoord * reach);
        return mc.theWorld.rayTraceBlocks(eyeVec, sumVec, false, false, false);
    }
    private void useCurrentItem() {
        mc.getNetHandler().addToSendQueue(new C08PacketPlayerBlockPlacement(mc.thePlayer.getHeldItem()));
    }

    private boolean isItem(ItemStack itemStack, Item item) {
        return itemStack != null && itemStack.getItem() == item;
    }
    public static long timeBetween(long val, long val2) {
        return Math.abs(val2 - val);
    }
    private boolean fallCheck() {
        return !mc.thePlayer.onGround && mc.thePlayer.fallDistance >= distance.getValue();
    }
    @Override
    public String[] getSuffix() {
        return new String[]{CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, this.mode.getModeString())};
    }
}
