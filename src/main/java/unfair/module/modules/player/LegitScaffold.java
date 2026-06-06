package unfair.module.modules.player;

import net.minecraft.client.Minecraft;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import unfair.Unfair;
import unfair.event.EventTarget;
import unfair.event.types.EventType;
import unfair.event.types.Priority;
import unfair.events.MoveInputEvent;
import unfair.events.UpdateEvent;
import unfair.management.RotationState;
import unfair.module.Module;
import unfair.module.modules.movement.Eagle;
import unfair.property.properties.BooleanProperty;
import unfair.property.properties.ModeProperty;
import unfair.util.*;

import java.util.ArrayList;
import java.util.Comparator;

public class LegitScaffold extends Module {
    private static final Minecraft mc = Minecraft.getMinecraft();
    private static final double[] placeOffsets = new double[]{0.03125, 0.09375, 0.15625, 0.21875, 0.28125, 0.34375, 0.40625, 0.46875, 0.53125, 0.59375, 0.65625, 0.71875, 0.78125, 0.84375, 0.90625, 0.96875};
    private float yaw = -180.0F;
    private float pitch = 0.0F;
    private boolean canRotate = false;
    private boolean eagleSneaking = false;
    public final ModeProperty rotationMode = new ModeProperty("Rotations", 2, new String[]{"None", "Default", "Backwards", "Sideways"});
    public final ModeProperty moveFix = new ModeProperty("Move Fix", 1, new String[]{"None", "Silent"});
    public final BooleanProperty eagle = new BooleanProperty("Eagle", true);
    public final BooleanProperty blocksOnly = new BooleanProperty("Blocks Only", true);

    public LegitScaffold() {
        super("LegitScaffold", false);
    }

    private boolean shouldAssist() {
        return mc.thePlayer != null
                && mc.theWorld != null
                && mc.currentScreen == null
                && (!this.blocksOnly.getValue() || ItemUtil.isHoldingBlock())
                && !this.isModuleEnabled(Scaffold.class);
    }

    private boolean isModuleEnabled(Class<? extends Module> moduleClass) { //写出来这个东西的人或者ai可以进场了
        Module module = Unfair.moduleManager.modules.get(moduleClass);
        return module != null && module.isEnabled();
    }

    private boolean canEagleMoveSafely() {
        double[] offset = MoveUtil.predictMovement();
        return PlayerUtil.canMove(mc.thePlayer.motionX + offset[0], mc.thePlayer.motionZ + offset[1]);
    }

    private boolean shouldEagleSneak() {
        return this.eagle.getValue()
                && this.shouldAssist()
                && !this.isModuleEnabled(Eagle.class)
                && mc.thePlayer.onGround
                && this.canEagleMoveSafely();
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

    private float getModeYaw(float currentYaw, float eventYaw) {
        float yawDiffTo180 = RotationUtil.wrapAngleDiff(currentYaw - 180.0F, eventYaw);
        if (this.rotationMode.getValue() == 2) {
            return yawDiffTo180;
        }
        return this.isDiagonal(currentYaw)
                ? yawDiffTo180
                : RotationUtil.wrapAngleDiff(currentYaw - 135.0F * ((currentYaw + 180.0F) % 90.0F < 45.0F ? 1.0F : -1.0F), eventYaw);
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
        BlockPos targetPos = new BlockPos(
                MathHelper.floor_double(mc.thePlayer.posX),
                MathHelper.floor_double(mc.thePlayer.posY) - 1,
                MathHelper.floor_double(mc.thePlayer.posZ)
        );
        if (!BlockUtil.isReplaceable(targetPos)) {
            return null;
        }

        ArrayList<BlockPos> positions = new ArrayList<>();
        for (int x = -4; x <= 4; x++) {
            for (int y = -4; y <= 0; y++) {
                for (int z = -4; z <= 4; z++) {
                    BlockPos pos = targetPos.add(x, y, z);
                    if (this.isValidSupport(pos)) {
                        positions.add(pos);
                    }
                }
            }
        }
        if (positions.isEmpty()) {
            return null;
        }

        positions.sort(
                Comparator.comparingDouble(
                        o -> o.distanceSqToCenter((double) targetPos.getX() + 0.5, (double) targetPos.getY() + 0.5, (double) targetPos.getZ() + 0.5)
                )
        );
        BlockPos blockPos = positions.get(0);
        EnumFacing facing = this.getBestFacing(blockPos, targetPos);
        return facing == null ? null : new BlockData(blockPos, facing);
    }

    private boolean isValidSupport(BlockPos pos) {
        if (BlockUtil.isReplaceable(pos)
                || BlockUtil.isInteractable(pos)
                || mc.thePlayer.getDistance((double) pos.getX() + 0.5, (double) pos.getY() + 0.5, (double) pos.getZ() + 0.5) > (double) mc.playerController.getBlockReachDistance()) {
            return false;
        }
        for (EnumFacing facing : EnumFacing.VALUES) {
            if (facing != EnumFacing.DOWN && BlockUtil.isReplaceable(pos.offset(facing))) {
                return true;
            }
        }
        return false;
    }

    @EventTarget(Priority.HIGH)
    public void onUpdate(UpdateEvent event) {
        if (!this.isEnabled() || event.getType() != EventType.PRE) {
            return;
        }
        if (!this.shouldAssist()) {
            this.canRotate = false;
            return;
        }

        BlockData blockData = this.getBlockData();
        if (blockData == null) {
            this.canRotate = false;
            return;
        }

        double[] x = placeOffsets;
        double[] y = placeOffsets;
        double[] z = placeOffsets;//?
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

        float currentYaw = this.getCurrentYaw();
        float modeYaw = this.getModeYaw(currentYaw, event.getYaw());
        if (!this.canRotate) {
            switch (this.rotationMode.getValue()) {
                case 1:
                case 2:
                case 3:
                    this.yaw = RotationUtil.quantizeAngle(modeYaw);
                    this.pitch = RotationUtil.quantizeAngle(85.0F);
            }
        }

        float baseYaw = RotationUtil.wrapAngleDiff(this.yaw, event.getYaw());
        float basePitch = this.pitch;
        float bestYaw = -180.0F;
        float bestPitch = 0.0F;
        float bestDiff = 0.0F;
        for (double dx : x) {
            for (double dy : y) {
                for (double dz : z) {
                    double relX = (double) blockData.blockPos().getX() + dx - mc.thePlayer.posX;
                    double relY = (double) blockData.blockPos().getY() + dy - mc.thePlayer.posY - (double) mc.thePlayer.getEyeHeight();
                    double relZ = (double) blockData.blockPos().getZ() + dz - mc.thePlayer.posZ;
                    float[] rotations = RotationUtil.getRotationsTo(relX, relY, relZ, baseYaw, basePitch);
                    MovingObjectPosition mop = RotationUtil.rayTrace(rotations[0], rotations[1], mc.playerController.getBlockReachDistance(), 1.0F);
                    if (mop != null
                            && mop.typeOfHit == MovingObjectType.BLOCK
                            && mop.getBlockPos().equals(blockData.blockPos())
                            && mop.sideHit == blockData.facing()) {
                        float totalDiff = Math.abs(rotations[0] - baseYaw) + Math.abs(rotations[1] - basePitch);
                        if (bestYaw == -180.0F && bestPitch == 0.0F || totalDiff < bestDiff) {
                            bestYaw = rotations[0];
                            bestPitch = rotations[1];
                            bestDiff = totalDiff;
                        }
                    }
                }
            }
        }

        if (bestYaw == -180.0F && bestPitch == 0.0F) {
            this.canRotate = false;
            return;
        }

        this.yaw = bestYaw;
        this.pitch = bestPitch;
        this.canRotate = true;
        if (this.canRotate && MoveUtil.isForwardPressed()) {
            switch (this.rotationMode.getValue()) {
                case 2:
                case 3:
                    this.yaw = RotationUtil.quantizeAngle(modeYaw);
            }
        }
        if (this.rotationMode.getValue() != 0) {
            event.setRotation(this.yaw, this.pitch, 3);
            if (this.moveFix.getValue() == 1) {
                event.setPervRotation(this.yaw, 3);
            }
        }
    }

    @EventTarget(Priority.LOWEST)
    public void onMoveInput(MoveInputEvent event) {
        if (!this.isEnabled()) {
            return;
        }
        if (this.moveFix.getValue() == 1
                && RotationState.isActived()
                && RotationState.getPriority() == 3.0F
                && MoveUtil.isForwardPressed()) {
            MoveUtil.fixStrafe(RotationState.getSmoothedYaw());
        }
        this.eagleSneaking = this.shouldEagleSneak();
        if (this.eagleSneaking && !mc.thePlayer.movementInput.sneak) {
            mc.thePlayer.movementInput.sneak = true;
            mc.thePlayer.movementInput.moveStrafe *= 0.3F;
            mc.thePlayer.movementInput.moveForward *= 0.3F;
        }
    }

    @Override
    public void onDisabled() {
        this.yaw = -180.0F;
        this.pitch = 0.0F;
        this.canRotate = false;
        this.eagleSneaking = false;
    }

    public static class BlockData {
        private final BlockPos blockPos;
        private final EnumFacing facing;

        public BlockData(BlockPos blockPos, EnumFacing facing) {
            this.blockPos = blockPos;
            this.facing = facing;
        }

        public BlockPos blockPos() {
            return this.blockPos;
        }

        public EnumFacing facing() {
            return this.facing;
        }
    }
}
