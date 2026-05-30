package unfair.module.modules.combat;

import com.google.common.base.CaseFormat;
import net.minecraft.client.Minecraft;
<<<<<<< HEAD
import net.minecraft.client.settings.KeyBinding;
=======
>>>>>>> 839a5315ef498d98d4be72e8b3f4e7cc0c660d5c
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.client.*;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraft.network.play.server.S19PacketEntityStatus;
import net.minecraft.network.play.server.S27PacketExplosion;
import net.minecraft.potion.Potion;
import unfair.Unfair;
import unfair.enums.DelayModules;
import unfair.event.EventManager;
import unfair.event.EventTarget;
import unfair.event.types.EventType;
import unfair.events.*;
import unfair.management.RotationState;
import unfair.mixin.IAccessorEntity;
import unfair.module.Module;
import unfair.module.modules.movement.LongJump;
import unfair.property.properties.BooleanProperty;
import unfair.property.properties.IntProperty;
import unfair.property.properties.ModeProperty;
import unfair.property.properties.PercentProperty;
<<<<<<< HEAD
import unfair.util.*;
=======
import unfair.util.ChatUtil;
import unfair.util.MoveUtil;
import unfair.util.RayCastUtil;
import unfair.util.RotationUtil;
>>>>>>> 839a5315ef498d98d4be72e8b3f4e7cc0c660d5c

import java.util.Objects;

import static unfair.config.Config.mc;

public class Velocity extends Module {
    private static final Minecraft mc = Minecraft.getMinecraft();
<<<<<<< HEAD
    public final ModeProperty mode = new ModeProperty("mode", 0, new String[]{"VANILLA", "Prediction","Reduce"});
    private final BooleanProperty reduceWhenCanAttack = new BooleanProperty("ReduceWhenCanAttack",true, () -> mode.getValue() != 0);
    public final IntProperty attackTimes = new IntProperty("AttackTimes", 1, 1, 5, () -> this.mode.getValue() == 1 && this.reduce.getValue());
    public final BooleanProperty reduce = new BooleanProperty("reduce", true, () -> mode.getValue() == 1);
    public final BooleanProperty jump = new BooleanProperty("Jump", true, () -> mode.getValue() == 1);
    public final BooleanProperty delay = new BooleanProperty("delay", false, () -> mode.getValue() == 1);
=======
    private static boolean attack = false;
    private static boolean inventory = false;
    public final ModeProperty mode = new ModeProperty("mode", 0, new String[]{"VANILLA", "Prediction","Reduce"});
    private final BooleanProperty noBlink = new BooleanProperty("NoBlinking",true, () -> mode.getValue() != 0);
    private final BooleanProperty noBlocking = new BooleanProperty("NoBlocking",true, () -> mode.getValue() != 0);
    public final IntProperty attackTimes = new IntProperty("AttackTimes", 1, 1, 5, () -> this.mode.getValue() == 1 && this.reduce.getValue());
    public final BooleanProperty reduce = new BooleanProperty("reduce", true, () -> mode.getValue() == 1);
    public final BooleanProperty jump = new BooleanProperty("Jump", true, () -> mode.getValue() == 1);
    public final BooleanProperty delay = new BooleanProperty("delay", false, () -> mode.getValue() == 1 && !this.airBuffer.getValue());
>>>>>>> 839a5315ef498d98d4be72e8b3f4e7cc0c660d5c
    public final IntProperty delayTicks = new IntProperty("delay-ticks", 1, 1, 5, () -> mode.getValue() == 1 && delay.getValue() && !this.airBuffer.getValue());
    public final BooleanProperty rotate = new BooleanProperty("Rotate", false, () -> this.mode.getValue() == 1);
    public final IntProperty rotateTick = new IntProperty("Rotate Tick", 3, 1, 12, () -> this.mode.getValue() == 1 && this.rotate.getValue());
    public final BooleanProperty autoMove = new BooleanProperty("Auto Move", false, () -> this.mode.getValue() == 1 && this.rotate.getValue());
<<<<<<< HEAD
    public final BooleanProperty airBuffer = new BooleanProperty("DelayTillOnGround", true, () -> mode.getValue() == 1 && delay.getValue());
    public final BooleanProperty groundDelay = new BooleanProperty("GroundDelay",false, () -> mode.getValue() == 1 && delay.getValue() && !airBuffer.getValue());
=======
    public final BooleanProperty airBuffer = new BooleanProperty("air-buffer", true, () -> mode.getValue() == 1 && !delay.getValue());
>>>>>>> 839a5315ef498d98d4be72e8b3f4e7cc0c660d5c
    public final PercentProperty chance = new PercentProperty("chance", 100, () -> mode.getValue() == 0);
    public final PercentProperty horizontal = new PercentProperty("horizontal", 100, () -> mode.getValue() == 0);
    public final PercentProperty vertical = new PercentProperty("vertical", 100, () -> mode.getValue() == 0);
    public final PercentProperty explosionHorizontal = new PercentProperty("explosions-horizontal", 100, () -> mode.getValue() == 0);
    public final PercentProperty explosionVertical = new PercentProperty("explosions-vertical", 100, () -> mode.getValue() == 0);
    public final BooleanProperty fakeCheck = new BooleanProperty("fake-check", true);
    public final BooleanProperty debug = new BooleanProperty("debug", false);
    public boolean knockback = false;
    private int chanceCounter = 0;
    private int rotatoTickCounter = 0;
    private boolean pendingExplosion = false;
    private boolean allowNext = true;
    private boolean delayFlag = false;
    private boolean isFallDamage;
    private boolean jumpFlag = false;
    public static boolean hasReceivedVelocity;
<<<<<<< HEAD
    private int ticksSinceVelocity = -1;
    private boolean handleReset = false;

    private double knockbackX = 0;
    private float[] targetRotation = null;
    private double knockbackZ = 0;
    private int reduceTick = -1;
=======
    public static boolean attacking;
    private double knockbackX = 0;
    private float[] targetRotation = null;
    private double knockbackZ = 0;
>>>>>>> 839a5315ef498d98d4be72e8b3f4e7cc0c660d5c

    public Velocity() {
        super("Velocity", false, false);
    }

    private boolean isInLiquidOrWeb() {
        return mc.thePlayer.isInWater() || mc.thePlayer.isInLava() || ((IAccessorEntity) mc.thePlayer).getIsInWeb();
    }


    @EventTarget
    public void onKnockback(KnockbackEvent event) {
            if (!allowNext || !(Boolean) fakeCheck.getValue()) {
                    allowNext = true;
                if (pendingExplosion) {
                    if (mode.getValue() == 0) {
                        pendingExplosion = false;
                        if (explosionHorizontal.getValue() > 0) {
                            event.setX(event.getX() * (double) explosionHorizontal.getValue() / 100.0);
                            event.setZ(event.getZ() * (double) explosionHorizontal.getValue() / 100.0);
                        } else {
                            event.setX(mc.thePlayer.motionX);
                            event.setZ(mc.thePlayer.motionZ);
                        }
                        if (explosionVertical.getValue() > 0) {
                            event.setY(event.getY() * (double) explosionVertical.getValue() / 100.0);
                        } else {
                            event.setY(mc.thePlayer.motionY);
                        }
                    }
                } else {
                    if (!isEnabled() || event.isCancelled()) {
                        pendingExplosion = false;
                        allowNext = true;
                        return;
                    }
                    if (this.mode.getValue() == 1 && this.rotate.getValue() && event.getY() > 0.0) {
                        this.knockbackX = event.getX();
                        this.knockbackZ = event.getZ();
                        if (Math.abs(this.knockbackX) > 0.01 || Math.abs(this.knockbackZ) > 0.01) {
                            this.rotatoTickCounter = 1;
                        }
                    }
<<<<<<< HEAD
                    if (!delay.getValue()) {
                        ticksSinceVelocity = 0;
=======
                    double packetDirection = 0.0;
                    double motionX = event.getX();
                    double motionZ = event.getZ();

                    packetDirection = Math.atan2(motionX, motionZ);
                    double degreePlayer = getDirection();

                    double degreePacket = Math.floorMod((int) Math.toDegrees(packetDirection), 360);


                    double angle = Math.abs(degreePacket + degreePlayer);
                    angle = Math.floorMod((int) angle, 360);

                    double threshold = 120.0;
                    boolean inRange = angle >= (180.0 - threshold / 2.0) && angle <= (180.0 + threshold / 2.0);
                    if (inRange) {
                        this.jumpFlag = (this.mode.getValue() == 1 && jump.getValue()) && event.getY() > 0.0;
>>>>>>> 839a5315ef498d98d4be72e8b3f4e7cc0c660d5c
                    }
                    chanceCounter = chanceCounter % 100 + chance.getValue();
                    if (chanceCounter >= 100) {
                        if (mode.getValue() == 0) {
                            if (horizontal.getValue() > 0) {
                                event.setX(event.getX() * (double) horizontal.getValue() / 100.0);
                                event.setZ(event.getZ() * (double) horizontal.getValue() / 100.0);
                            } else {
                                event.setX(mc.thePlayer.motionX);
                                event.setZ(mc.thePlayer.motionZ);
                            }
                            if (vertical.getValue() > 0) {
                                event.setY(event.getY() * (double) vertical.getValue() / 100.0);
                            } else {
                                event.setY(mc.thePlayer.motionY);
                            }
                        }
                    }
                }
            }

    }

<<<<<<< HEAD

=======
    private boolean badPackets() {
        return (attack) || (inventory);
    }

    private void resetBadPackets() {
        attack = false;
        inventory = false;
    }
>>>>>>> 839a5315ef498d98d4be72e8b3f4e7cc0c660d5c
    @EventTarget
    public void onLivingUpdate(LivingUpdateEvent event) {
        if (this.isEnabled() && this.jumpFlag) {
            if (mc.thePlayer.onGround && MoveUtil.isForwardPressed() && !mc.thePlayer.isPotionActive(Potion.jump) && !this.isInLiquidOrWeb()) {
                mc.thePlayer.movementInput.jump = true;
            }
            this.jumpFlag = false;
        }
    }
    @EventTarget
<<<<<<< HEAD
    public void onTick(TickEvent event){
        if (this.isEnabled()){
            if (ticksSinceVelocity >= 0) {
                ticksSinceVelocity++;
            }
            if (ticksSinceVelocity >= 10) {
                ticksSinceVelocity = -1;
            }
            if (jump.getValue() && this.mode.getValue() == 1){
                handleJumpReset();
            }
        }
    }
        private void handleJumpReset() {
            if (mc.thePlayer == null) return;
            if (ticksSinceVelocity >= 0) {
                handleReset = true;
                if (ticksSinceVelocity <= 2 && mc.thePlayer.onGround) {
                    KeyBindUtil.setKeyBindState(mc.gameSettings.keyBindJump.getKeyCode(),true);
                }
            }
            if (ticksSinceVelocity >= 4 && ticksSinceVelocity <= 9) {
                KeyBindUtil.setKeyBindState(mc.gameSettings.keyBindJump.getKeyCode(),false);
                handleReset = false;
            }
        }
    @EventTarget
=======
>>>>>>> 839a5315ef498d98d4be72e8b3f4e7cc0c660d5c
    public void onUpdate(UpdateEvent event) {
        if (!isEnabled()) return;
        if (mode.getValue() == 1) {
            if (event.getType() == EventType.PRE) {
                int maxTick = this.rotateTick.getValue();
                if (this.rotatoTickCounter > 0 && this.rotatoTickCounter <= maxTick) {
                    if (this.rotatoTickCounter == 1) {
                        double deltaX = -this.knockbackX;
                        double deltaZ = -this.knockbackZ;
                        this.targetRotation = RotationUtil.getRotationsTo(deltaX, 0, deltaZ, event.getYaw(), event.getPitch());
                    }
                    if (this.targetRotation != null) {
                        event.setRotation(this.targetRotation[0], this.targetRotation[1], 2);
                        event.setPervRotation(this.targetRotation[0], 2);
                    }
                }
            }
            if (event.getType() == EventType.POST) {
                int maxTick = this.rotateTick.getValue();
                if (this.rotatoTickCounter > 0 && this.rotatoTickCounter <= maxTick) {
                    this.rotatoTickCounter++;
                    if (this.rotatoTickCounter > maxTick) {
                        this.rotatoTickCounter = 0;
                        this.targetRotation = null;
                        this.knockbackX = 0;
                        this.knockbackZ = 0;
                    }
                }
                if (delayFlag && ((delay.getValue()
<<<<<<< HEAD
                        && (isInLiquidOrWeb() || Unfair.delayManager.getDelay() >= (long) delayTicks.getValue() && !airBuffer.getValue()) || (mc.thePlayer.onGround && !groundDelay.getValue() && !airBuffer.getValue()))
                        || (airBuffer.getValue() && mc.thePlayer.onGround && delayFlag))) {
                    ticksSinceVelocity = 0;
                    hasReceivedVelocity = true;
=======
                        && (isInLiquidOrWeb() || Unfair.delayManager.getDelay() >= (long) delayTicks.getValue()))
                        || (airBuffer.getValue() && mc.thePlayer.onGround && delayFlag))) {
>>>>>>> 839a5315ef498d98d4be72e8b3f4e7cc0c660d5c
                    dbg(Unfair.clientName + "Delay/Buffer " + Unfair.delayManager.getDelay() + " Ticks");
                    Unfair.delayManager.setDelayState(false, DelayModules.VELOCITY);
                    delayFlag = false;
                }
            }
            if (reduce.getValue()) {
                if (event.getType() != EventType.PRE) return;
                if (hasReceivedVelocity){
<<<<<<< HEAD
                    if (reduceTick >= attackTimes.getValue()){
                        reduceTick = 0;
                        hasReceivedVelocity = false;
                    }
=======
>>>>>>> 839a5315ef498d98d4be72e8b3f4e7cc0c660d5c
                    RayCastUtil.RayCastResult targetA = RayCastUtil.rayCast(new RotationUtil.RotationVec(event.getYaw(),event.getPitch()),3.2);
                    if (targetA != null) {
                        if (targetA.entityHit instanceof EntityPlayer && targetA.entityHit != mc.thePlayer) {
                            if (mc.thePlayer.isSprinting()) {
<<<<<<< HEAD
                                KillAura killAura = (KillAura) Unfair.moduleManager.getModule(KillAura.class);
                                if (killAura.getTarget() != null) {
                                    if (!reduceWhenCanAttack.getValue() || (killAura.blockTick == 0 && killAura.autoBlock.getValue() == 2) || (killAura.autoBlock.getValue() == 6 && killAura.blockTick == killAura.attackTick.getValue()) || (killAura.autoBlock.getValue() != 6 && killAura.autoBlock.getValue() != 2) || (killAura.autoBlock.getValue() == 5 && killAura.blockTick == 0)) {
                                        EventManager.call(new AttackEvent(killAura.getTarget()));
=======
                                attacking = true;
                                KillAura killAura = (KillAura) Unfair.moduleManager.getModule(KillAura.class);
                                if (killAura.getTarget() != null) {
                                    if (!noBlink.getValue() || !Unfair.blinkManager.isBlinking()) {
                                        if (!noBlocking.getValue() || !mc.thePlayer.isBlocking()) {
                                            for (int i = 1; i <= attackTimes.getValue(); ++i) {
                                                EventManager.call(new AttackEvent(killAura.getTarget()));
>>>>>>> 839a5315ef498d98d4be72e8b3f4e7cc0c660d5c
                                                mc.getNetHandler().addToSendQueue(new C0APacketAnimation());
                                                if (killAura.getTarget() != mc.thePlayer) {
                                                    mc.getNetHandler().addToSendQueue(new C02PacketUseEntity(killAura.getTarget(), C02PacketUseEntity.Action.ATTACK));
                                                } else {
                                                    mc.getNetHandler().addToSendQueue(new C02PacketUseEntity(Objects.requireNonNull(killAura.getTarget()), C02PacketUseEntity.Action.ATTACK));
                                                }
                                                mc.thePlayer.motionX *= 0.6D;
                                                mc.thePlayer.motionZ *= 0.6D;
                                                mc.thePlayer.setSprinting(false);
<<<<<<< HEAD
                                    }
                                } else {
                                    EventManager.call(new AttackEvent(targetA.entityHit));
                                    mc.getNetHandler().addToSendQueue(new C0APacketAnimation());
                                    if (targetA.entityHit != mc.thePlayer) {
                                                    mc.getNetHandler().addToSendQueue(new C02PacketUseEntity(targetA.entityHit, C02PacketUseEntity.Action.ATTACK));
                                    } else {
                                                    mc.getNetHandler().addToSendQueue(new C02PacketUseEntity(Objects.requireNonNull(targetA.entityHit), C02PacketUseEntity.Action.ATTACK));
                                                }
                                    mc.thePlayer.motionX *= 0.6D;
                                    mc.thePlayer.motionZ *= 0.6D;
                                    mc.thePlayer.setSprinting(false);
=======
                                            }
                                        }
                                    }
                                    attacking = false;
                                } else {
                                    if (!noBlink.getValue() || !Unfair.blinkManager.isBlinking()) {
                                        if (!noBlocking.getValue() || !mc.thePlayer.isBlocking()) {
                                            for (int i = 1; i <= attackTimes.getValue(); ++i) {
                                                EventManager.call(new AttackEvent(targetA.entityHit));
                                                mc.getNetHandler().addToSendQueue(new C0APacketAnimation());
                                                if (targetA.entityHit != mc.thePlayer) {
                                                    mc.getNetHandler().addToSendQueue(new C02PacketUseEntity(targetA.entityHit, C02PacketUseEntity.Action.ATTACK));
                                                } else {
                                                    mc.getNetHandler().addToSendQueue(new C02PacketUseEntity(Objects.requireNonNull(targetA.entityHit), C02PacketUseEntity.Action.ATTACK));
                                                }
                                                mc.thePlayer.motionX *= 0.6D;
                                                mc.thePlayer.motionZ *= 0.6D;
                                                mc.thePlayer.setSprinting(false);
                                            }
                                        }
                                        attacking = false;
                                    }
>>>>>>> 839a5315ef498d98d4be72e8b3f4e7cc0c660d5c
                                }
                            }
                        }
                    }
<<<<<<< HEAD
                    reduceTick++;
                }
=======
                }
                hasReceivedVelocity = false;
>>>>>>> 839a5315ef498d98d4be72e8b3f4e7cc0c660d5c
            }
        }
        if (mode.getValue() == 2){
            if (event.getType() == EventType.PRE){
                if (hasReceivedVelocity){
<<<<<<< HEAD
=======

>>>>>>> 839a5315ef498d98d4be72e8b3f4e7cc0c660d5c
                    RayCastUtil.RayCastResult targetA = RayCastUtil.rayCast(new RotationUtil.RotationVec(event.getYaw(),event.getPitch()),3.2);
                    if (targetA != null) {
                        if (targetA.entityHit instanceof EntityPlayer && targetA.entityHit != mc.thePlayer) {
                            if (mc.thePlayer.isSprinting()) {
<<<<<<< HEAD
                                KillAura killAura = (KillAura) Unfair.moduleManager.getModule(KillAura.class);
                                if (killAura.getTarget() != null) {
                                    if (!reduceWhenCanAttack.getValue() || (killAura.blockTick == 0 && killAura.autoBlock.getValue() == 2) || (killAura.autoBlock.getValue() == 6 && killAura.blockTick == killAura.attackTick.getValue()) || (killAura.autoBlock.getValue() != 6 && killAura.autoBlock.getValue() != 2) || (killAura.autoBlock.getValue() == 5 && killAura.blockTick == 0)) {
                                        EventManager.call(new AttackEvent(killAura.getTarget()));
=======
                                attacking = true;
                                KillAura killAura = (KillAura) Unfair.moduleManager.getModule(KillAura.class);
                                if (killAura.getTarget() != null) {
                                    if (!noBlink.getValue() || !Unfair.blinkManager.isBlinking()) {
                                        if (!noBlocking.getValue() || !mc.thePlayer.isBlocking()) {
                                            EventManager.call(new AttackEvent(killAura.getTarget()));
>>>>>>> 839a5315ef498d98d4be72e8b3f4e7cc0c660d5c
                                            mc.getNetHandler().addToSendQueue(new C0APacketAnimation());
                                            if (killAura.getTarget() != mc.thePlayer) {
                                                mc.getNetHandler().addToSendQueue(new C02PacketUseEntity(killAura.getTarget(), C02PacketUseEntity.Action.ATTACK));
                                            } else {
                                                mc.getNetHandler().addToSendQueue(new C02PacketUseEntity(Objects.requireNonNull(killAura.getTarget()), C02PacketUseEntity.Action.ATTACK));
                                            }
                                            mc.thePlayer.motionX *= 0.6D;
                                            mc.thePlayer.motionZ *= 0.6D;
                                            mc.thePlayer.setSprinting(false);
<<<<<<< HEAD
                                    }
                                } else {
=======
                                            ChatUtil.sendFormatted("Reduced");
                                        }

                                    }
                                    attacking = false;
                                } else {
                                    if (!noBlink.getValue() || !Unfair.blinkManager.isBlinking()) {
                                        if (!noBlocking.getValue() || !mc.thePlayer.isBlocking()) {
>>>>>>> 839a5315ef498d98d4be72e8b3f4e7cc0c660d5c
                                            EventManager.call(new AttackEvent(targetA.entityHit));
                                            mc.getNetHandler().addToSendQueue(new C0APacketAnimation());
                                            if (targetA.entityHit!= mc.thePlayer) {
                                                mc.getNetHandler().addToSendQueue(new C02PacketUseEntity(targetA.entityHit, C02PacketUseEntity.Action.ATTACK));
                                            } else {
                                                mc.getNetHandler().addToSendQueue(new C02PacketUseEntity(Objects.requireNonNull(targetA.entityHit), C02PacketUseEntity.Action.ATTACK));
                                            }
                                            mc.thePlayer.motionX *= 0.6D;
                                            mc.thePlayer.motionZ *= 0.6D;
                                            mc.thePlayer.setSprinting(false);
<<<<<<< HEAD
=======
                                            ChatUtil.sendFormatted("Reduced");
                                        }
                                        attacking = false;
                                    }
>>>>>>> 839a5315ef498d98d4be72e8b3f4e7cc0c660d5c
                                }
                            }
                        }
                    }
                    hasReceivedVelocity = false;
                }
            }
        }
    }

    @EventTarget
    public void onPacket(PacketEvent event) {
        if (isEnabled() && event.getType() == EventType.RECEIVE && !event.isCancelled()) {
            if (event.getPacket() instanceof S12PacketEntityVelocity) {
                S12PacketEntityVelocity packet = (S12PacketEntityVelocity) event.getPacket();
                if (packet.getEntityID() == mc.thePlayer.getEntityId()) {
                    double packetDirection = 0.0;
                    S12PacketEntityVelocity s12 = (S12PacketEntityVelocity) event.getPacket();
                    double motionX = s12.getMotionX();
                    double motionZ = s12.getMotionZ();

                    packetDirection = Math.atan2(motionX, motionZ);
                    double degreePlayer = getDirection();

                    double degreePacket = Math.floorMod((int) Math.toDegrees(packetDirection), 360);


                    double angle = Math.abs(degreePacket + degreePlayer);
                    angle = Math.floorMod((int) angle, 360);

                    double threshold = 120.0;
                    boolean inRange = angle >= (180.0 - threshold / 2.0) && angle <= (180.0 + threshold / 2.0);

                    if (inRange) {
                       isFallDamage = false;
                    }
<<<<<<< HEAD
                    if (!delay.getValue()) {
                        hasReceivedVelocity = true;
                    }
=======
                    hasReceivedVelocity = true;
>>>>>>> 839a5315ef498d98d4be72e8b3f4e7cc0c660d5c
                    LongJump longJump = (LongJump) Unfair.moduleManager.modules.get(LongJump.class);
                    if (mode.getValue() == 1
                            && !delayFlag
                            && !isInLiquidOrWeb()
                            && !pendingExplosion
                            && (!allowNext || !(Boolean) fakeCheck.getValue())
                            && (!longJump.isEnabled() || !longJump.canStartJump())) {
<<<<<<< HEAD
                        if ((airBuffer.getValue() && !mc.thePlayer.onGround) || (delay.getValue() && !mc.thePlayer.onGround) || (delay.getValue() && groundDelay.getValue()) && !airBuffer.getValue()) {
=======
                        if ((airBuffer.getValue() && !mc.thePlayer.onGround) || (delay.getValue() && !mc.thePlayer.onGround)) {
>>>>>>> 839a5315ef498d98d4be72e8b3f4e7cc0c660d5c
                            Unfair.delayManager.setDelayState(true, DelayModules.VELOCITY);
                            dbg(Unfair.clientName + "Delay/Buffer Active");
                            Unfair.delayManager.delayedPacket.offer(packet);
                            event.setCancelled(true);
                            delayFlag = true;
                        }
                    }
                }
            } else if (!(event.getPacket() instanceof S27PacketExplosion)) {
                if (event.getPacket() instanceof S19PacketEntityStatus) {
                    S19PacketEntityStatus packet = (S19PacketEntityStatus) event.getPacket();
                    Entity entity = packet.getEntity(mc.theWorld);
                    if (entity != null && entity.equals(mc.thePlayer) && packet.getOpCode() == 2) {
                        allowNext = false;
                    }
                }
            } else if (mode.getValue() == 0) {
                S27PacketExplosion packet = (S27PacketExplosion) event.getPacket();
                if (packet.func_149149_c() != 0.0F || packet.func_149144_d() != 0.0F || packet.func_149147_e() != 0.0F) {
                    pendingExplosion = true;
                    if (explosionHorizontal.getValue() == 0 || explosionVertical.getValue() == 0) {
                        event.setCancelled(true);
                    }
                }
            }
        }
        if (event.getType() == EventType.RECEIVE && !event.isCancelled()) {
            if (event.getPacket() instanceof S12PacketEntityVelocity) {
                S12PacketEntityVelocity velocityPacket = (S12PacketEntityVelocity) event.getPacket();
                if (velocityPacket.getEntityID() == mc.thePlayer.getEntityId()) {
                    knockback = true;
                }
            }
        }
<<<<<<< HEAD
    }
    @EventTarget
    public void onMove(MoveInputEvent event) {
        if (this.isEnabled()){
                if (handleReset) {
                    mc.thePlayer.movementInput.moveForward = 1.0F;
                }
        }
=======
        if (event.getType() == EventType.SEND && !event.isCancelled()) {
            if (event.getPacket() instanceof C02PacketUseEntity) {
                C02PacketUseEntity useEntity = (C02PacketUseEntity) event.getPacket();
                if (useEntity.getAction() == C02PacketUseEntity.Action.ATTACK) {
                    attack = true;
                }
            } else if (event.getPacket() instanceof C0DPacketCloseWindow || event.getPacket() instanceof C0EPacketClickWindow ||
                    (event.getPacket() instanceof C16PacketClientStatus && ((C16PacketClientStatus) event.getPacket()).getStatus() == C16PacketClientStatus.EnumState.OPEN_INVENTORY_ACHIEVEMENT)) {
                inventory = true;
            } else if (event.getPacket() instanceof C03PacketPlayer) {
                resetBadPackets();
            }
        }
    }
    @EventTarget
    public void onMove(MoveInputEvent event) {
>>>>>>> 839a5315ef498d98d4be72e8b3f4e7cc0c660d5c
        if (this.isEnabled() && this.rotatoTickCounter > 0 && this.rotatoTickCounter <= this.rotateTick.getValue()) {
            if (this.autoMove.getValue()) {
                mc.thePlayer.movementInput.moveForward = 1.0F;
            }
            if (this.targetRotation != null && RotationState.isActived() && RotationState.getPriority() == 2.0F && MoveUtil.isForwardPressed()) {
                MoveUtil.fixStrafe(RotationState.getSmoothedYaw());
            }
        }
    }
    @EventTarget
    public void onStrafe(StrafeEvent event) {
        if (mode.getValue() == 2) {
            boolean shouldJump;
            shouldJump = mc.thePlayer.hurtTime == 9 && mc.thePlayer.isSprinting() &&
                    !isFallDamage;
            if (shouldJump && mc.thePlayer.onGround && !mc.gameSettings.keyBindJump.isKeyDown() && !isInLiquidOrWeb()) {
                mc.thePlayer.jump();
            }
        }
    }
    @EventTarget
    public void onLoadWorld(LoadWorldEvent event) {
        onDisabled();
    }

    public void dbg(String msg) {
        if (debug.getValue()) ChatUtil.sendFormatted(msg);
    }

    @Override
    public void onEnabled() {
        knockback = false;
        hasReceivedVelocity = false;
<<<<<<< HEAD
=======
        attacking = false;
>>>>>>> 839a5315ef498d98d4be72e8b3f4e7cc0c660d5c
        this.rotatoTickCounter = 0;
        this.targetRotation = null;
        this.knockbackX = 0;
        this.knockbackZ = 0;
    }

    @Override
    public void onDisabled() {
        pendingExplosion = false;
        allowNext = true;
        hasReceivedVelocity = false;
<<<<<<< HEAD
=======
        attacking = false;
>>>>>>> 839a5315ef498d98d4be72e8b3f4e7cc0c660d5c
        knockback = false;
    }
    @Override
    public String[] getSuffix() {
        if (mode.getValue() == 0) {
            return new String[]{
                    String.format("%d%%", horizontal.getValue()),
                    String.format("%d%%", vertical.getValue())
            };
        } else {
            return new String[]{CaseFormat.UPPER_UNDERSCORE.to(CaseFormat.UPPER_CAMEL, mode.getModeString())};
        }
    }
    private double getDirection() {
        float moveYaw = mc.thePlayer.rotationYaw;

        if (mc.thePlayer.moveForward != 0f && mc.thePlayer.moveStrafing == 0f) {
            moveYaw += (mc.thePlayer.moveForward > 0) ? 0 : 180;
        } else if (mc.thePlayer.moveForward != 0f && mc.thePlayer.moveStrafing != 0f) {
            if (mc.thePlayer.moveForward > 0) {
                moveYaw += (mc.thePlayer.moveStrafing > 0) ? -45 : 45;
            } else {
                moveYaw -= (mc.thePlayer.moveStrafing > 0) ? -45 : 45;
            }
            moveYaw += (mc.thePlayer.moveForward > 0) ? 0 : 180;
        } else if (mc.thePlayer.moveStrafing != 0f && mc.thePlayer.moveForward == 0f) {
            moveYaw += (mc.thePlayer.moveStrafing > 0) ? -90 : 90;
        }

        return (double) Math.floorMod((int) moveYaw, 360);
    }
}