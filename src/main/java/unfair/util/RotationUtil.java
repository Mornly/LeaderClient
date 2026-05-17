package unfair.util;

import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.util.*;
import org.lwjgl.util.vector.Vector2f;
import unfair.mixin.IAccessorEntity;
import unfair.module.modules.combat.KillAura;

import java.util.List;

public class RotationUtil {
    private static final Minecraft mc = Minecraft.getMinecraft();

    public static float wrapAngleDiff(float angle, float target) {
        return target + MathHelper.wrapAngleTo180_float(angle - target);
    }
    public static boolean hasVisiblePoint(AxisAlignedBB boundingBox) {
        Vec3 eyePos = RotationUtil.mc.thePlayer.getPositionEyes(1.0f);
        double centerX = (boundingBox.minX + boundingBox.maxX) / 2.0;
        double centerZ = (boundingBox.minZ + boundingBox.maxZ) / 2.0;
        double height = boundingBox.maxY - boundingBox.minY;
        double[] yRatios = new double[]{0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9};

        for (double ratio : yRatios) {
            double targetY = boundingBox.minY + ratio * height;
            Vec3 targetPoint = new Vec3(centerX, targetY, centerZ);
            if (RotationUtil.mc.theWorld.rayTraceBlocks(eyePos, targetPoint) == null) {
                return true;
            }
        }
        return false;
    }
    public static float clampAngle(float angle, float maxAngle) {
        maxAngle = Math.max(0.0f, Math.min(180.0f, maxAngle));
        if (angle > maxAngle) {
            angle = maxAngle;
        } else if (angle < -maxAngle) {
            angle = -maxAngle;
        }
        return angle;
    }
    public static KillAura.RotationData getRotationDataToEntity(Entity target, float hitbox, List<Double> eyeHeights) {
        double minDistance = Double.MAX_VALUE;
        Vec3 bestEye = null, bestHitVec = null;
        Vector2f bestRotation = null;

        for (double eye : eyeHeights) {
            Vec3 thePlayerPosition = new Vec3(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ);
            Vec3 eyePos = thePlayerPosition.addVector(0, eye, 0);

            AxisAlignedBB targetBox = getTargetBoundingBox(target, hitbox);

            Vec3 closestPoint = getClosestPoint(eyePos, targetBox);

            Vector2f basicRotation = getRotationsVector(new Vec3(target.posX, closestPoint.yCoord, target.posZ));
            MovingObjectPosition basicRotationIntercept = getIntercept(targetBox, basicRotation, eyePos);

            if (isVecInside(targetBox, eyePos)) {
                minDistance = 0;
                bestEye = eyePos;
                bestRotation = basicRotation;
                bestHitVec = basicRotationIntercept.hitVec;
                break;
            }

            if (basicRotationIntercept != null && basicRotationIntercept.hitVec != null) {
                if (basicRotationIntercept.hitVec.distanceTo(eyePos) < 3) {
                    minDistance = basicRotationIntercept.hitVec.distanceTo(eyePos);
                    bestEye = eyePos;
                    bestRotation = basicRotation;
                    bestHitVec = basicRotationIntercept.hitVec;
                    break;
                }
            }

            Vector2f rotations = getRotations(eyePos, closestPoint);
            MovingObjectPosition position = getIntercept(targetBox, rotations, eyePos);

            if (position == null) {
                continue;
            }

            Vec3 intercept = position.hitVec;

            if (isVecInside(targetBox, eyePos)) {
                minDistance = 0;
                bestEye = eyePos;
                bestRotation = rotations;
                bestHitVec = intercept;
                break;
            }

            if (intercept != null) {
                if (intercept.distanceTo(eyePos) < minDistance) {
                    minDistance = intercept.distanceTo(eyePos);
                    bestEye = eyePos;
                    bestRotation = rotations;
                    bestHitVec = intercept;
                }
            }
        }

        return new KillAura.RotationData(bestEye, bestHitVec, minDistance, bestRotation);
    }
    public static Vector2f getRotationsVector(Vec3 vec) {
        Vec3 thePlayerVector = new Vec3(mc.thePlayer.posX, mc.thePlayer.posY + mc.thePlayer.getEyeHeight(), mc.thePlayer.posZ);

        double x = vec.xCoord - thePlayerVector.xCoord;
        double y = vec.yCoord - thePlayerVector.yCoord;
        double z = vec.zCoord - thePlayerVector.zCoord;
        return diffCalcVector(x, y, z);
    }
    public static Vector2f diffCalcVector(double diffX, double diffY, double diffZ) {
        final double diffXZ = Math.sqrt(diffX * diffX + diffZ * diffZ);
        final float yaw = (float) Math.toDegrees(Math.atan2(diffZ, diffX)) - 90.0f;
        final float pitch = (float) (-Math.toDegrees(Math.atan2(diffY, diffXZ)));
        return new Vector2f(MathHelper.wrapAngleTo180_float(yaw), MathHelper.wrapAngleTo180_float(pitch));
    }
    public static Vec3 getClosestPoint(Vec3 vec, AxisAlignedBB aabb) {
        double closestX = Math.max(aabb.minX, Math.min(vec.xCoord, aabb.maxX));
        double closestY = Math.max(aabb.minY, Math.min(vec.yCoord, aabb.maxY));
        double closestZ = Math.max(aabb.minZ, Math.min(vec.zCoord, aabb.maxZ));

        return new Vec3(closestX, closestY, closestZ);
    }
    private static AxisAlignedBB getTargetBoundingBox(Entity entity, float hitbox) {
        return entity.getEntityBoundingBox().expand(hitbox, hitbox, hitbox);
    }
    public static Vector2f getRotations(Vec3 eye, Vec3 target) {
        double x = target.xCoord - eye.xCoord;
        double y = target.yCoord - eye.yCoord;
        double z = target.zCoord - eye.zCoord;

        double diffXZ = Math.sqrt(x * x + z * z);
        float yaw = (float) Math.toDegrees(Math.atan2(z, x)) - 90.0f;
        float pitch = (float) (-Math.toDegrees(Math.atan2(y, diffXZ)));

        return new Vector2f(MathHelper.wrapAngleTo180_float(yaw), MathHelper.wrapAngleTo180_float(pitch));
    }

    public static MovingObjectPosition getIntercept(AxisAlignedBB targetBox, Vector2f rotations, Vec3 eyePos) {
        return getIntercept(targetBox, rotations, eyePos, 6);
    }


    public static MovingObjectPosition getIntercept(AxisAlignedBB targetBox, Vector2f rotations, Vec3 eyePos, double reach) {
        Vec3 lookVec = getLook(rotations.x, rotations.y);
        Vec3 endReachPos = eyePos.add(new Vec3(lookVec.xCoord * reach, lookVec.yCoord * reach, lookVec.zCoord * reach));
        return targetBox.calculateIntercept(eyePos, endReachPos);
    }
    public static Vec3 getLook(float yaw, float pitch) {
            float f = pitch * ((float) Math.PI / 180F);
            float f1 = -yaw * ((float) Math.PI / 180F);
            float f2 = MathHelper.cos(f1);
            float f3 = MathHelper.sin(f1);
            float f4 = MathHelper.cos(f);
            float f5 = MathHelper.sin(f);
            return new Vec3(f3 * f4, -f5, f2 * f4);
    }
    public static boolean isVecInside(AxisAlignedBB self, Vec3 vec) {
        return vec.xCoord > self.minX && vec.xCoord < self.maxX && (vec.yCoord > self.minY && vec.yCoord < self.maxY && vec.zCoord > self.minZ && vec.zCoord < self.maxZ);
    }
    public static float smoothAngle(float angle, float smoothFactor) {
        return angle * (0.5f + 0.5f * (1.0f - Math.max(0.0f, Math.min(1.0f, smoothFactor + RandomUtil.nextFloat(-0.1f, 0.1f)))));
    }

    public static float quantizeAngle(float angle) {
        return (float) ((double) angle - (double) angle % (double) 0.0096f);
    }
    public static float[] getRotationsTo(double targetX, double targetY, double targetZ, float currentYaw, float currentPitch) {
        return RotationUtil.getRotations(targetX, targetY, targetZ, currentYaw, currentPitch, 180.0f, 0.0f);
    }

    public static float[] getRotationsToBox(AxisAlignedBB boundingBox, float yaw, float pitch, float maxAngle, float smoothFactor) {
        Vec3 eyePos = RotationUtil.mc.thePlayer.getPositionEyes(1.0f);
        double centerX = (boundingBox.minX + boundingBox.maxX) / 2.0;
        double centerZ = (boundingBox.minZ + boundingBox.maxZ) / 2.0;
        double height = boundingBox.maxY - boundingBox.minY;
        double[] yRatios = new double[]{0.1, 0.2, 0.3, 0.4, 0.5, 0.6, 0.7, 0.8, 0.9};
        Vec3 bestPoint = null;
        double bestDistSq = Double.MAX_VALUE;

        for (double ratio : yRatios) {
            double targetY = boundingBox.minY + ratio * height;
            Vec3 targetPoint = new Vec3(centerX, targetY, centerZ);
            if (RotationUtil.mc.theWorld.rayTraceBlocks(eyePos, targetPoint) == null) {
                double dx = centerX - eyePos.xCoord;
                double dy = targetY - eyePos.yCoord;
                double dz = centerZ - eyePos.zCoord;
                double distSq = dx * dx + dy * dy + dz * dz;
                if (distSq < bestDistSq) {
                    bestDistSq = distSq;
                    bestPoint = targetPoint;
                }
            }
        }

        if (bestPoint != null) {
            double deltaX = bestPoint.xCoord - eyePos.xCoord;
            double deltaY = bestPoint.yCoord - eyePos.yCoord;
            double deltaZ = bestPoint.zCoord - eyePos.zCoord;
            return RotationUtil.getRotations(deltaX, deltaY, deltaZ, yaw, pitch, maxAngle, smoothFactor);
        }

        double minTargetY = boundingBox.minY + 0.05 * height;
        double maxTargetY = boundingBox.minY + 0.75 * height;
        double deltaX = centerX - eyePos.xCoord;
        double deltaY = eyePos.yCoord >= maxTargetY ? maxTargetY - eyePos.yCoord : (eyePos.yCoord <= minTargetY ? minTargetY - eyePos.yCoord : 0.0);
        double deltaZ = centerZ - eyePos.zCoord;
        return RotationUtil.getRotations(deltaX, deltaY, deltaZ, yaw, pitch, maxAngle, smoothFactor);
    }

    private static Vec3 findVisiblePointFromHead(Vec3 eyePos, AxisAlignedBB bb) {
        double headX = (bb.minX + bb.maxX) / 2.0;
        double headY = bb.minY + (bb.maxY - bb.minY) * 0.75;
        double headZ = (bb.minZ + bb.maxZ) / 2.0;
        double step = 0.2;
        for (double xOff = 0; xOff <= 0.5; xOff += step) {
            for (double yOff = 0; yOff <= 0.5; yOff += step) {
                for (double zOff = 0; zOff <= 0.5; zOff += step) {
                    double[] xOffsets = {xOff, -xOff};
                    double[] yOffsets = {yOff, -yOff};
                    double[] zOffsets = {zOff, -zOff};
                    for (double x : xOffsets) {
                        for (double y : yOffsets) {
                            for (double z : zOffsets) {
                                double targetX = headX + x;
                                double targetY = headY + y;
                                double targetZ = headZ + z;
                                if (targetX >= bb.minX && targetX <= bb.maxX &&
                                        targetY >= bb.minY && targetY <= bb.maxY &&
                                        targetZ >= bb.minZ && targetZ <= bb.maxZ) {
                                    Vec3 testPoint = new Vec3(targetX, targetY, targetZ);
                                    if (isPointVisible(eyePos, testPoint)) {
                                        return testPoint;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    private static boolean isPointVisible(Vec3 start, Vec3 end) {
        MovingObjectPosition rayTrace = RotationUtil.mc.theWorld.rayTraceBlocks(start, end, false, true, false);
        return rayTrace == null;
    }

    public static float[] getRotations(double targetX, double targetY, double targetZ, float currentYaw, float currentPitch, float maxAngle, float smoothFactor) {
        double horizontalDistance = Math.sqrt(targetX * targetX + targetZ * targetZ);
        float yawDelta = MathHelper.wrapAngleTo180_float((float) (Math.atan2(targetZ, targetX) * 180.0 / Math.PI) - 90.0f - currentYaw);
        float pitchDelta = MathHelper.wrapAngleTo180_float((float) (-Math.atan2(targetY, horizontalDistance) * 180.0 / Math.PI) - currentPitch);
        yawDelta = Math.abs(yawDelta) <= 1.0f ? 0.0f : RotationUtil.smoothAngle(RotationUtil.clampAngle(yawDelta, maxAngle), smoothFactor);
        pitchDelta = Math.abs(pitchDelta) <= 1.0f ? 0.0f : RotationUtil.smoothAngle(RotationUtil.clampAngle(pitchDelta, maxAngle), smoothFactor);
        return new float[]{RotationUtil.quantizeAngle(currentYaw + yawDelta), RotationUtil.quantizeAngle(currentPitch + pitchDelta)};
    }

    public static Vec3 getClosestPointOnBox(Vec3 point, AxisAlignedBB bb) {
        double x = MathHelper.clamp_double(point.xCoord, bb.minX, bb.maxX);
        double y = MathHelper.clamp_double(point.yCoord, bb.minY, bb.maxY);
        double z = MathHelper.clamp_double(point.zCoord, bb.minZ, bb.maxZ);
        return new Vec3(x, y, z);
    }

    public static double distanceToEntity(Entity entity) {
        float borderSize = entity.getCollisionBorderSize();
        AxisAlignedBB boundingBox = entity.getEntityBoundingBox().expand(borderSize, borderSize, borderSize);
        return RotationUtil.distanceToBox(boundingBox);
    }

    public static double distanceToBox(Entity entity, Vec3 point) {
        float borderSize = entity.getCollisionBorderSize();
        return RotationUtil.getDistanceToBox(entity.getEntityBoundingBox().expand(borderSize, borderSize, borderSize), point);
    }

    public static double distanceToBox(AxisAlignedBB boundingBox) {
        return RotationUtil.getDistanceToBox(boundingBox, RotationUtil.mc.thePlayer.getPositionEyes(1.0f));
    }

    public static double getDistanceToBox(AxisAlignedBB bb, Vec3 point) {
        if (bb.isVecInside(point)) {
            return 0.0;
        }
        Vec3 closestPoint = getClosestPointOnBox(point, bb);
        return point.distanceTo(closestPoint);
    }

    public static float angleToEntity(Entity entity) {
        Vec3 eyePos = RotationUtil.mc.thePlayer.getPositionEyes(1.0f);
        float borderSize = entity.getCollisionBorderSize();
        AxisAlignedBB boundingBox = entity.getEntityBoundingBox().expand(borderSize, borderSize, borderSize);
        if (boundingBox.isVecInside(eyePos)) {
            return 0.0f;
        }
        double deltaX = entity.posX - eyePos.xCoord;
        double deltaZ = entity.posZ - eyePos.zCoord;
        return Math.abs(MathHelper.wrapAngleTo180_float((float) (Math.atan2(deltaZ, deltaX) * 180.0 / Math.PI) - 90.0f - RotationUtil.mc.thePlayer.rotationYaw)) * 2.0f;
    }

    public static float getYawBetween(double x1, double z1, double x2, double z2) {
        return MathHelper.wrapAngleTo180_float((float) (Math.atan2(z2 - z1, x2 - x1) * 180.0 / Math.PI) - 90.0f - RotationUtil.mc.thePlayer.rotationYaw);
    }

    public static MovingObjectPosition rayTrace(float yaw, float pitch, double distance, float partialTicks) {
        Vec3 eyePos = RotationUtil.mc.thePlayer.getPositionEyes(partialTicks);
        Vec3 lookVec = ((IAccessorEntity) RotationUtil.mc.thePlayer).callGetVectorForRotation(pitch, yaw);
        Vec3 targetPos = eyePos.addVector(lookVec.xCoord * distance, lookVec.yCoord * distance, lookVec.zCoord * distance);
        return RotationUtil.mc.theWorld.rayTraceBlocks(eyePos, targetPos);
    }

    public static MovingObjectPosition rayTrace(Entity entity) {
        Vec3 eyePos = RotationUtil.mc.thePlayer.getPositionEyes(1.0f);
        float borderSize = entity.getCollisionBorderSize();
        Vec3 targetPos = RotationUtil.getClosestPointOnBox(eyePos, entity.getEntityBoundingBox().expand(borderSize, borderSize, borderSize));
        return RotationUtil.mc.theWorld.rayTraceBlocks(eyePos, targetPos);
    }

    public static MovingObjectPosition rayTrace(AxisAlignedBB boundingBox, float yaw, float pitch, double distance) {
        Vec3 eyePos = RotationUtil.mc.thePlayer.getPositionEyes(1.0f);
        Vec3 lookVec = ((IAccessorEntity) RotationUtil.mc.thePlayer).callGetVectorForRotation(pitch, yaw);
        Vec3 targetPos = eyePos.addVector(lookVec.xCoord * distance, lookVec.yCoord * distance, lookVec.zCoord * distance);
        return boundingBox.calculateIntercept(eyePos, targetPos);
    }
    public static float[] getRotations(Vec3 vec) {
        return getRotations(vec.xCoord, vec.yCoord, vec.zCoord);
    }
    public static float[] getRotations(BlockPos blockPos) {
        return getRotations(blockPos.getX() + 0.5, blockPos.getY() + 0.5, blockPos.getZ() + 0.5, mc.thePlayer.posX, mc.thePlayer.posY + (double)mc.thePlayer.getEyeHeight(), mc.thePlayer.posZ);
    }
    public static float[] getRotations(double posX, double posY, double posZ) {
        return getRotations(posX, posY, posZ, mc.thePlayer.posX, mc.thePlayer.posY + (double) mc.thePlayer.getEyeHeight(), mc.thePlayer.posZ);
    }
    public static float[] getRotations(double rotX, double rotY, double rotZ, double startX, double startY, double startZ) {
        double x = rotX - startX;
        double y = rotY - startY;
        double z = rotZ - startZ;
        double dist = MathHelper.sqrt_double(x * x + z * z);
        float yaw = (float) (Math.atan2(z, x) * 180.0 / Math.PI) - 90.0F;
        float pitch = (float) (-(Math.atan2(y, dist) * 180.0 / Math.PI));
        return new float[]{yaw, pitch};
    }

    public static float[] getRotations(BlockPos blockPos, EnumFacing enumFacing) {
        double d = (double) blockPos.getX() + 0.5 - mc.thePlayer.posX + (double) enumFacing.getFrontOffsetX() * 0.25;
        double d2 = (double) blockPos.getZ() + 0.5 - mc.thePlayer.posZ + (double) enumFacing.getFrontOffsetZ() * 0.25;
        double d3 = mc.thePlayer.posY + (double) mc.thePlayer.getEyeHeight() - blockPos.getY() - (double) enumFacing.getFrontOffsetY() * 0.25;
        double d4 = MathHelper.sqrt_double(d * d + d2 * d2);
        float f = (float) (Math.atan2(d2, d) * 180.0 / Math.PI) - 90.0f;
        float f2 = (float) (Math.atan2(d3, d4) * 180.0 / Math.PI);
        return new float[]{MathHelper.wrapAngleTo180_float(f), f2};
    }
    public static class RotationVec {
        public float x;
        public float y;

        public RotationVec(float x, float y) {
            this.x = x;
            this.y = y;
        }

        public RotationVec add(float x, float y) {
            return new RotationVec(this.x + x, this.y + y);
        }

        public float getX() {
            return this.x;
        }

        public void setX(float x) {
            this.x = x;
        }

        public float getY() {
            return this.y;
        }

        public void setY(float y) {
            this.y = y;
        }
    }
}