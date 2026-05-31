package unfair.module.modules.player;

import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C0APacketAnimation;
import net.minecraft.potion.Potion;
import net.minecraft.util.*;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.world.WorldSettings.GameType;
import unfair.Unfair;
import unfair.event.EventTarget;
import unfair.event.types.EventType;
import unfair.event.types.Priority;
import unfair.events.*;
import unfair.management.RotationState;
import unfair.module.Module;
import unfair.module.modules.misc.BedNuker;
import unfair.module.modules.movement.LongJump;
import unfair.property.properties.*;
import unfair.util.*;

import java.util.ArrayList;
import java.util.Comparator;

public class Scaffold extends Module {
    private static final Minecraft mc = Minecraft.getMinecraft();
    private static final double[] placeOffsets = new double[]{
            0.03125,
            0.09375,
            0.15625,
            0.21875,
            0.28125,
            0.34375,
            0.40625,
            0.46875,
            0.53125,
            0.59375,
            0.65625,
            0.71875,
            0.78125,
            0.84375,
            0.90625,
            0.96875
    };
    public final ModeProperty rotationMode = new ModeProperty("Rotate Mode", 1, new String[]{"None", "Vanilla", "Backwards"});
    public final ModeProperty moveFix = new ModeProperty("Move Fix", 1, new String[]{"None", "Silent"});
    public final IntProperty jumpDelay = new IntProperty("Jump Delay", 2, 0, 5);
    public final FloatProperty startRotSpeed = new FloatProperty("Start Rotate Speed", 92.5F, 1.0F, 180.0F);
    public final FloatProperty normalRotSpeed = new FloatProperty("Normal Rotate Speed", 32.5F, 1.0F, 180.0F);
    public final BooleanProperty swing = new BooleanProperty("swing", true);
    public final BooleanProperty itemSpoof = new BooleanProperty("item-spoof", false);
    private int rotationTick = 0;
    private int lastSlot = -1;
    private int blockCount = -1;
    private float yaw = -180.0F;
    private float pitch = 0.0F;
    private boolean canRotate = false;
    private int tellyJumpDelayTimer = 0;
    private boolean wasInAir = false;
    private int stage = 0;
    private int startY = 256;
    private boolean shouldKeepY = false;
    private boolean towering = false;
    private EnumFacing targetFacing = null;
    public static int count = 0;

    public Scaffold() {
        super("Scaffold", false);
    }

    private boolean shouldStopSprint() {
        if (this.isTowering()) {
            return false;
        } else {
            return this.stage <= 0;
        }
    }

    private boolean canPlace() {
        BedNuker bedNuker = (BedNuker) Unfair.moduleManager.modules.get(BedNuker.class);
        if (bedNuker.isEnabled() && bedNuker.isReady()) {
            return false;
        } else {
            LongJump longJump = (LongJump) Unfair.moduleManager.modules.get(LongJump.class);
            return !longJump.isEnabled() || !longJump.isAutoMode() || longJump.isJumping();
        }
    }

    private EnumFacing getBestFacing(BlockPos blockPos1, BlockPos blockPos3) {
        double offset = 0.0;
        EnumFacing enumFacing = null;
        for (EnumFacing facing : EnumFacing.VALUES) {
            if (facing != EnumFacing.DOWN) {
                BlockPos pos = blockPos1.offset(facing);
                if (pos.getY() <= blockPos3.getY()) {
                    double distance = pos.distanceSqToCenter((double) blockPos3.getX() + 0.5, (double) blockPos3.getY() + 0.5, (double) blockPos3.getZ() + 0.5);
                    if (enumFacing == null || distance < offset || distance == offset && facing == EnumFacing.UP) {
                        offset = distance;
                        enumFacing = facing;
                    }
                }
            }
        }
        return enumFacing;
    }

    private BlockData getBlockData() {
        int startY = MathHelper.floor_double(mc.thePlayer.posY);
        BlockPos targetPos = new BlockPos(
                MathHelper.floor_double(mc.thePlayer.posX),
                (this.stage != 0 && !this.shouldKeepY ? Math.min(startY, this.startY) : startY) - 1,
                MathHelper.floor_double(mc.thePlayer.posZ)
        );
        if (!BlockUtil.isReplaceable(targetPos)) {
            return null;
        } else {
            ArrayList<BlockPos> positions = new ArrayList<>();
            for (int x = -4; x <= 4; x++) {
                for (int y = -4; y <= 0; y++) {
                    for (int z = -4; z <= 4; z++) {
                        BlockPos pos = targetPos.add(x, y, z);
                        if (!BlockUtil.isReplaceable(pos)
                                && !BlockUtil.isInteractable(pos)
                                && !(
                                mc.thePlayer.getDistance((double) pos.getX() + 0.5, (double) pos.getY() + 0.5, (double) pos.getZ() + 0.5)
                                        > (double) mc.playerController.getBlockReachDistance()
                        )
                                && (this.stage == 0 || this.shouldKeepY || pos.getY() < this.startY)) {
                            for (EnumFacing facing : EnumFacing.VALUES) {
                                if (facing != EnumFacing.DOWN) {
                                    BlockPos blockPos = pos.offset(facing);
                                    if (BlockUtil.isReplaceable(blockPos)) {
                                        positions.add(pos);
                                    }
                                }
                            }
                        }
                    }
                }
            }
            if (positions.isEmpty()) {
                return null;
            } else {
                positions.sort(
                        Comparator.comparingDouble(
                                o -> o.distanceSqToCenter((double) targetPos.getX() + 0.5, (double) targetPos.getY() + 0.5, (double) targetPos.getZ() + 0.5)
                        )
                );
                BlockPos blockPos = positions.get(0);
                EnumFacing facing = this.getBestFacing(blockPos, targetPos);
                return facing == null ? null : new BlockData(blockPos, facing);
            }
        }
    }

    private void place(BlockPos blockPos, EnumFacing enumFacing, Vec3 vec3) {
        if (ItemUtil.isHoldingBlock() && this.blockCount > 0) {
            if (mc.playerController.onPlayerRightClick(mc.thePlayer, mc.theWorld, mc.thePlayer.inventory.getCurrentItem(), blockPos, enumFacing, vec3)) {
                if (mc.playerController.getCurrentGameType() != GameType.CREATIVE) {
                    this.blockCount--;
                }
                if (this.swing.getValue()) {
                    mc.thePlayer.swingItem();
                } else {
                    PacketUtil.sendPacket(new C0APacketAnimation());
                }
            }
        }
    }

    private float getCurrentYaw() {
        return MoveUtil.adjustYaw(
                mc.thePlayer.rotationYaw, (float) MoveUtil.getForwardValue(), (float) MoveUtil.getLeftValue()
        );
    }

    private boolean isDiagonal(float yaw) {
        float absYaw = Math.abs(yaw % 90.0F);
        return absYaw > 20.0F && absYaw < 70.0F;
    }

    private boolean isTowering() {
        if (!MoveUtil.isForwardPressed()) return false;
        if (PlayerUtil.isAirAbove()) return false;
        if (mc.thePlayer.onGround) {
            if (this.stage > 0 || mc.gameSettings.keyBindJump.isKeyDown()) return true;
        }
        return this.tellyJumpDelayTimer > 0;
    }

    public int getSlot() {
        return this.lastSlot;
    }

    @EventTarget(Priority.HIGH)
    public void onUpdate(UpdateEvent event) {
        if (this.isEnabled() && event.getType() == EventType.PRE) {
            if (this.rotationTick > 0) {
                this.rotationTick--;
            }
            if (mc.thePlayer.onGround) {
                if (this.stage > 0) {
                    this.stage--;
                }
                if (this.stage < 0) {
                    this.stage++;
                }
                this.startY = this.shouldKeepY ? this.startY : MathHelper.floor_double(mc.thePlayer.posY);
                this.shouldKeepY = false;
                this.towering = false;
                if (this.wasInAir) {
                    this.tellyJumpDelayTimer = jumpDelay.getValue();
                    this.wasInAir = false;
                }
                if (this.tellyJumpDelayTimer > 0) this.tellyJumpDelayTimer--;
            } else {
                this.wasInAir = true;
            }
            if (mc.thePlayer.onGround && MoveUtil.isForwardPressed() && !mc.gameSettings.keyBindJump.isKeyDown() && this.stage == 0) {
                this.stage = 1;
            }
            if (mc.gameSettings.keyBindJump.isKeyDown()) {
                this.tellyJumpDelayTimer = 2;
            }
            if (this.canPlace()) {
                ItemStack stack = mc.thePlayer.getHeldItem();
                int count = ItemUtil.isBlock(stack) ? stack.stackSize : 0;
                this.blockCount = Math.min(this.blockCount, count);
                if (this.blockCount <= 0) {
                    int slot = mc.thePlayer.inventory.currentItem;
                    if (this.blockCount == 0) {
                        slot--;
                    }
                    for (int i = slot; i > slot - 9; i--) {
                        int hotbarSlot = (i % 9 + 9) % 9;
                        ItemStack candidate = mc.thePlayer.inventory.getStackInSlot(hotbarSlot);
                        if (ItemUtil.isBlock(candidate)) {
                            mc.thePlayer.inventory.currentItem = hotbarSlot;
                            this.blockCount = candidate.stackSize;
                            break;
                        }
                    }
                }
                float currentYaw = this.getCurrentYaw();
                float yawDiffTo180 = RotationUtil.wrapAngleDiff(currentYaw - 180.0F, event.getYaw());
                float diagonalYaw = this.isDiagonal(currentYaw)
                        ? yawDiffTo180
                        : RotationUtil.wrapAngleDiff(currentYaw - 135.0F * ((currentYaw + 180.0F) % 90.0F < 45.0F ? 1.0F : -1.0F), event.getYaw());
                    if (!this.canRotate) {
                        switch (this.rotationMode.getValue()) {
                            case 1:
                                if (this.yaw == -180.0F && this.pitch == 0.0F) {
                                    this.yaw = RotationUtil.quantizeAngle(diagonalYaw);
                                    this.pitch = RotationUtil.quantizeAngle(85.0F);
                                } else {
                                    this.yaw = RotationUtil.quantizeAngle(diagonalYaw);
                                }
                                break;
                            case 2:
                                if (this.yaw == -180.0F && this.pitch == 0.0F) {
                                    this.yaw = RotationUtil.quantizeAngle(yawDiffTo180);
                                    this.pitch = RotationUtil.quantizeAngle(85.0F);
                                } else {
                                    this.yaw = RotationUtil.quantizeAngle(yawDiffTo180);
                                }
                                break;
                        }
                }
                BlockData blockData = this.getBlockData();
                Vec3 hitVec = null;
                if (blockData != null) {
                    double[] x = placeOffsets;
                    double[] y = placeOffsets;
                    double[] z = placeOffsets;
                    switch (blockData.facing()) {
                        case NORTH:
                            z = new double[]{0.0};
                            break;
                        case EAST:
                            x = new double[]{1.0};
                            break;
                        case SOUTH:
                            z = new double[]{1.0};
                            break;
                        case WEST:
                            x = new double[]{0.0};
                            break;
                        case DOWN:
                            y = new double[]{0.0};
                            break;
                        case UP:
                            y = new double[]{1.0};
                    }
                    float bestYaw = -180.0F;
                    float bestPitch = 0.0F;
                    float bestDiff = 0.0F;
                    for (double dx : x) {
                        for (double dy : y) {
                            for (double dz : z) {
                                double relX = (double) blockData.blockPos().getX() + dx - mc.thePlayer.posX;
                                double relY = (double) blockData.blockPos().getY() + dy - mc.thePlayer.posY - (double) mc.thePlayer.getEyeHeight();
                                double relZ = (double) blockData.blockPos().getZ() + dz - mc.thePlayer.posZ;
                                float baseYaw = RotationUtil.wrapAngleDiff(this.yaw, event.getYaw());
                                float[] rotations = RotationUtil.getRotationsTo(relX, relY, relZ, baseYaw, this.pitch);
                                MovingObjectPosition mop = RotationUtil.rayTrace(rotations[0], rotations[1], mc.playerController.getBlockReachDistance(), 1.0F);
                                if (mop != null
                                        && mop.typeOfHit == MovingObjectType.BLOCK
                                        && mop.getBlockPos().equals(blockData.blockPos())
                                        && mop.sideHit == blockData.facing()) {
                                    float totalDiff = Math.abs(rotations[0] - baseYaw) + Math.abs(rotations[1] - this.pitch);
                                    if (bestYaw == -180.0F && bestPitch == 0.0F || totalDiff < bestDiff) {
                                        bestYaw = rotations[0];
                                        bestPitch = rotations[1];
                                        bestDiff = totalDiff;
                                        hitVec = mop.hitVec;
                                    }
                                }
                            }
                        }
                    }
                    if (bestYaw != -180.0F || bestPitch != 0.0F) {
                        this.yaw = bestYaw;
                        this.pitch = bestPitch;
                        this.canRotate = true;
                    }
                }
                if (this.canRotate && MoveUtil.isForwardPressed() && Math.abs(MathHelper.wrapAngleTo180_float(yawDiffTo180 - this.yaw)) < 90.0F) {
                    if (this.rotationMode.getValue() == 2) {
                        this.yaw = RotationUtil.quantizeAngle(yawDiffTo180);
                    }
                }
                if (this.rotationMode.getValue() != 0) {
                    float targetYaw = this.yaw;
                    float targetPitch = this.pitch;
                    if (this.towering && (mc.thePlayer.motionY > 0.0 || mc.thePlayer.posY > (double) (this.startY + 1))) {
                        float yawDiff = MathHelper.wrapAngleTo180_float(this.yaw - event.getYaw());
                        float tolerance = this.rotationTick >= 2 ? startRotSpeed.getValue() : normalRotSpeed.getValue();
                        if (Math.abs(yawDiff) > tolerance) {
                            float clampedYaw = RotationUtil.clampAngle(yawDiff, tolerance);
                            targetYaw = RotationUtil.quantizeAngle(event.getYaw() + clampedYaw);
                            this.rotationTick = Math.max(this.rotationTick, 1);
                        }
                    }
                    if (this.isTowering() && this.tellyJumpDelayTimer <= 0) {
                        float yawDelta = MathHelper.wrapAngleTo180_float(mc.thePlayer.rotationYaw - event.getYaw());
                        targetYaw = RotationUtil.quantizeAngle(event.getYaw() + yawDelta * RandomUtil.nextFloat(0.98F, 0.99F));
                        targetPitch = RotationUtil.quantizeAngle(RandomUtil.nextFloat(30.0F, 80.0F));
                        this.rotationTick = 3;
                        this.towering = true;
                    } else if (this.tellyJumpDelayTimer > 0) {
                        targetYaw = this.yaw != -180.0F ? this.yaw : RotationUtil.quantizeAngle(
                                MathHelper.wrapAngleTo180_float(mc.thePlayer.rotationYaw - event.getYaw()) + event.getYaw()
                        );
                        targetPitch = this.pitch > 10 || this.pitch < -10 ? this.pitch : 60.0F;
                    }
                    event.setRotation(targetYaw, targetPitch, 3);
                    if (this.moveFix.getValue() == 1) {
                        event.setPervRotation(targetYaw, 3);
                    }
                }
                if (blockData != null && hitVec != null && this.rotationTick <= 0) {
                    this.place(blockData.blockPos(), blockData.facing(), hitVec);
                }
                if (this.targetFacing != null) {
                    if (this.rotationTick <= 0) {
                        int playerBlockX = MathHelper.floor_double(mc.thePlayer.posX);
                        int playerBlockY = MathHelper.floor_double(mc.thePlayer.posY);
                        int playerBlockZ = MathHelper.floor_double(mc.thePlayer.posZ);
                        BlockPos belowPlayer = new BlockPos(playerBlockX, playerBlockY - 1, playerBlockZ);
                        hitVec = BlockUtil.getHitVec(belowPlayer, this.targetFacing, this.yaw, this.pitch);
                        this.place(belowPlayer, this.targetFacing, hitVec);
                    }
                    this.targetFacing = null;
                }
            }
        }
    }
    @EventTarget
    public void onStrafe(StrafeEvent event) {
        if (this.isEnabled()) {
            if (!mc.thePlayer.isCollidedHorizontally
                    && mc.thePlayer.hurtTime <= 5
                    && !mc.thePlayer.isPotionActive(Potion.jump)
                    && mc.gameSettings.keyBindJump.isKeyDown()
                    && ItemUtil.isHoldingBlock()) {
                if (mc.thePlayer.onGround && this.tellyJumpDelayTimer <= 0 && PlayerUtil.isAirBelow()) {
                    this.startY = MathHelper.floor_double(mc.thePlayer.posY);
                    mc.thePlayer.motionY = 0.42F;
                    if (!MoveUtil.isForwardPressed()) {
                        MoveUtil.setSpeed(0.0);
                        event.setForward(0.0F);
                        event.setStrafe(0.0F);
                    } else {
                        MoveUtil.setSpeed(MoveUtil.getSpeed(), MoveUtil.getMoveYaw());
                    }
                }
            }
        }
    }

    @EventTarget
    public void onMoveInput(MoveInputEvent event) {
        if (this.isEnabled()) {
            if (this.moveFix.getValue() == 1
                    && RotationState.isActived()
                    && RotationState.getPriority() == 3.0F
                    && MoveUtil.isForwardPressed()) {
                MoveUtil.fixStrafe(RotationState.getSmoothedYaw());
            }
            if (mc.thePlayer.onGround && this.stage > 0 && MoveUtil.isForwardPressed() && this.tellyJumpDelayTimer <= 0) {
                mc.thePlayer.movementInput.jump = true;
            }
        }
    }

    @EventTarget
    public void onLivingUpdate(LivingUpdateEvent event) {
        if (this.isEnabled()) {
            if (this.shouldStopSprint()) {
                mc.thePlayer.setSprinting(false);
            }
        }
    }

    @EventTarget
    public void onRender(Render2DEvent event) {
        if (this.isEnabled()) {
                int count = 0;
                for (int i = 0; i < 9; i++) {
                    ItemStack stack = mc.thePlayer.inventory.getStackInSlot(i);
                    if (stack != null && stack.stackSize > 0) {
                        Item item = stack.getItem();
                        if (item instanceof ItemBlock) {
                            Block block = ((ItemBlock) item).getBlock();
                            if (!BlockUtil.isInteractable(block) && BlockUtil.isSolid(block)) {
                                count += stack.stackSize;
                            }
                        }
                    }
                }
                Scaffold.count = count;
        }
    }

    @EventTarget
    public void onLeftClick(LeftClickMouseEvent event) {
        if (this.isEnabled()) {
            event.setCancelled(true);
        }
    }

    @EventTarget
    public void onRightClick(RightClickMouseEvent event) {
        if (this.isEnabled()) {
            event.setCancelled(true);
        }
    }

    @EventTarget
    public void onHitBlock(HitBlockEvent event) {
        if (this.isEnabled()) {
            event.setCancelled(true);
        }
    }

    @EventTarget
    public void onSwap(SwapItemEvent event) {
        if (this.isEnabled()) {
            this.lastSlot = event.setSlot(this.lastSlot);
            event.setCancelled(true);
        }
    }

    @Override
    public void onEnabled() {
        if (mc.thePlayer != null) {
            this.lastSlot = mc.thePlayer.inventory.currentItem;
        } else {
            this.lastSlot = -1;
        }
        this.blockCount = -1;
        this.rotationTick = 3;
        this.yaw = -180.0F;
        this.pitch = 0.0F;
        this.canRotate = false;
        this.towering = false;
    }

    @Override
    public void onDisabled() {
        if (mc.thePlayer != null && this.lastSlot != -1) {
            mc.thePlayer.inventory.currentItem = this.lastSlot;
        }
    }

    public static class BlockData {
        private final BlockPos blockPos;
        private final EnumFacing facing;

        public BlockData(BlockPos blockPos, EnumFacing enumFacing) {
            this.blockPos = blockPos;
            this.facing = enumFacing;
        }

        public BlockPos blockPos() {
            return this.blockPos;
        }

        public EnumFacing facing() {
            return this.facing;
        }
    }
}
