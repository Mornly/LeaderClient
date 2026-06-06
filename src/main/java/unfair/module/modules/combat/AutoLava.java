package unfair.module.modules.combat;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import unfair.Unfair;
import unfair.event.EventTarget;
import unfair.event.types.EventType;
import unfair.event.types.Priority;
import unfair.events.MoveInputEvent;
import unfair.events.UpdateEvent;
import unfair.management.RotationState;
import unfair.mixin.IAccessorMinecraft;
import unfair.module.Module;
import unfair.property.properties.BooleanProperty;
import unfair.property.properties.FloatProperty;
import unfair.property.properties.IntProperty;
import unfair.property.properties.ModeProperty;
import unfair.util.MoveUtil;
import unfair.util.TeamUtil;

import java.util.ArrayList;
import java.util.Comparator;

public class AutoLava extends Module {
    private static final Minecraft mc = Minecraft.getMinecraft();
    public final FloatProperty range = new FloatProperty("Range", 4.5F, 3.0F, 6.0F);
    public final IntProperty placeDelay = new IntProperty("Place Delay", 200, 50, 1000);
    public final BooleanProperty rotations = new BooleanProperty("Rotations", true);
    public final ModeProperty moveFix = new ModeProperty("Move Fix", 1, new String[]{"NONE", "SILENT"});
    public final BooleanProperty autoSwitch = new BooleanProperty("Auto Switch", true);
    public final BooleanProperty onlyGround = new BooleanProperty("Only Ground", true);
    public final BooleanProperty teams = new BooleanProperty("Teams", true);

    final Item bucket = Items.lava_bucket;

    private EntityPlayer target = null;
    private int lastSlot = -1;
    private long lastPlaceTime = 0L;
    private int state = 0;
    private boolean hasRotated = false;

    public AutoLava() {
        super("AutoLava", false);
    }

    @Override
    public void onEnabled() {
        this.target = null;
        this.lastSlot = -1;
        this.state = 0;
        this.hasRotated = false;
        this.lastPlaceTime = 0L;
    }

    @Override
    public void onDisabled() {
        switchBack();
        this.target = null;
        this.state = 0;
        this.hasRotated = false;
    }

    private boolean isValidTarget(EntityLivingBase entity) {
        if (entity == mc.thePlayer || entity.deathTime > 0) return false;
        if (!(entity instanceof EntityOtherPlayerMP)) return false;
        if (mc.thePlayer.getDistanceToEntity(entity) > this.range.getValue()) return false;
        if (this.onlyGround.getValue() && !entity.onGround) return false;
        EntityPlayer player = (EntityPlayer) entity;
        if (TeamUtil.isFriend(player)) return false;
        return !this.teams.getValue() || !TeamUtil.isSameTeam(player);
    }

    private EntityPlayer getTarget() {
        ArrayList<EntityPlayer> targets = new ArrayList<>();
        for (Object obj : mc.theWorld.loadedEntityList) {
            if (obj instanceof EntityOtherPlayerMP) {
                EntityPlayer player = (EntityPlayer) obj;
                if (isValidTarget(player)) {
                    targets.add(player);
                }
            }
        }
        if (targets.isEmpty()) return null;
        targets.sort(Comparator.comparingDouble(entity -> mc.thePlayer.getDistanceToEntity(entity)));
        return targets.get(0);
    }

    private boolean hasBucket() {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.thePlayer.inventory.getStackInSlot(i);
            if (stack != null && stack.getItem() == bucket) {
                return true;
            }
        }
        return false;
    }

    private int getSlot() {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.thePlayer.inventory.getStackInSlot(i);
            if (stack != null && stack.getItem() == bucket) {
                return i;
            }
        }
        return -1;
    }

    private void switchTo() {
        int slot = getSlot();
        if (slot != -1) {
            this.lastSlot = mc.thePlayer.inventory.currentItem;
            mc.thePlayer.inventory.currentItem = slot;
        }
    }

    private void switchBack() {
        if (this.lastSlot != -1) {
            mc.thePlayer.inventory.currentItem = this.lastSlot;
            this.lastSlot = -1;
        }
    }

    private float[] calculateRotations(BlockPos blockPos) {
        double x = blockPos.getX() + 0.5 - (mc.thePlayer.posX);
        double y = blockPos.getY() + 0.5 - (mc.thePlayer.posY + mc.thePlayer.getEyeHeight());
        double z = blockPos.getZ() + 0.5 - (mc.thePlayer.posZ);

        double dist = Math.sqrt(x * x + z * z);
        float yaw = (float) Math.toDegrees(Math.atan2(z, x)) - 90.0F;
        float pitch = (float) -Math.toDegrees(Math.atan2(y, dist));

        return new float[]{
                MathHelper.wrapAngleTo180_float(yaw),
                MathHelper.wrapAngleTo180_float(pitch)
        };
    }

    private void place() {
        int slot = getSlot();
        if (slot == -1) return;

        ItemStack stack = mc.thePlayer.inventory.getStackInSlot(slot);
        if (stack == null || stack.getItem() != bucket) return;

        ((IAccessorMinecraft) mc).setRightClickDelayTimer(0);
        ((IAccessorMinecraft) mc).invokeRightClickMouse();
    }

    @EventTarget(Priority.HIGH)
    public void onUpdate(UpdateEvent event) {
        if (!this.isEnabled() || event.getType() != EventType.PRE) return;

        if (!hasBucket()) {
            reset();
            switchBack();
            return;
        }

        switch (this.state) {
            case 0:
                if (System.currentTimeMillis() - lastPlaceTime < placeDelay.getValue()) return;

                target = getTarget();
                if (target == null) return;

                KillAura aura = (KillAura) Unfair.moduleManager.modules.get(KillAura.class);
                if (aura != null && aura.isEnabled() &&
                        mc.thePlayer.getDistanceToEntity(target) <= aura.autoBlockRange.getValue()) {
                    return;
                }

                this.state = 1;
                break;

            case 1:
                if (target == null) {
                    reset();
                    return;
                }

                if (autoSwitch.getValue()) {
                    switchTo();
                }
                this.state = 2;
                break;

            case 2:
                if (rotations.getValue()) {
                    BlockPos lavaPos = new BlockPos(target.posX, target.posY - 0.01, target.posZ);
                    float[] rots = calculateRotations(lavaPos);
                    event.setRotation(rots[0], rots[1], 2);
                    event.setPervRotation(rots[0], 2);
                    hasRotated = true;
                }
                this.state = 3;
                break;

            case 3:
                if (target == null) {
                    reset();
                    return;
                }

                place();

                lastPlaceTime = System.currentTimeMillis();
                this.state = 4;
                break;

            case 4:
                switchBack();
                reset();
                break;
        }
    }

    @EventTarget
    public void onMoveInput(MoveInputEvent event) {
        if (isEnabled() && hasRotated
                && RotationState.isActived()
                && RotationState.getPriority() == 2.0F
                && moveFix.getValue() == 1
                && MoveUtil.isForwardPressed()) {
            MoveUtil.fixStrafe(RotationState.getSmoothedYaw());
        }
    }

    private void reset() {
        this.target = null;
        this.hasRotated = false;
        this.state = 0;
    }
}
