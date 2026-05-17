package unfair.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.EnumAction;
import net.minecraft.item.ItemMap;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import unfair.Unfair;
import unfair.module.modules.render.Animations;

@Mixin(ItemRenderer.class)
public abstract class MixinItemRenderer {

    @Shadow
    private float prevEquippedProgress;

    @Shadow
    private float equippedProgress;

    @Shadow
    @Final
    private Minecraft mc;

    @Shadow
    private ItemStack itemToRender;

    @Unique
    private float myau$astolfoSpinDelay = 0.0F;

    @Unique
    private long myau$lastUpdateTime = System.currentTimeMillis();

    @Shadow
    protected abstract void rotateArroundXAndY(float angle, float angleY);

    @Shadow
    protected abstract void setLightMapFromPlayer(AbstractClientPlayer clientPlayer);

    @Shadow
    protected abstract void rotateWithPlayerRotations(EntityPlayerSP entityplayerspIn, float partialTicks);

    @Shadow
    protected abstract void renderItemMap(AbstractClientPlayer clientPlayer, float pitch, float equipmentProgress, float swingProgress);

    @Shadow
    protected abstract void performDrinking(AbstractClientPlayer clientPlayer, float partialTicks);

    @Shadow
    protected abstract void doBlockTransformations();

    @Shadow
    protected abstract void doBowTransformations(float partialTicks, AbstractClientPlayer clientPlayer);

    @Shadow
    protected abstract void doItemUsedTransformations(float swingProgress);

    @Shadow
    public abstract void renderItem(EntityLivingBase entityIn, ItemStack heldStack, ItemCameraTransforms.TransformType transform);

    @Shadow
    protected abstract void renderPlayerArm(AbstractClientPlayer clientPlayer, float equipProgress, float swingProgress);

    @Unique
    private void myau$transformFirstPersonItem(float equipProgress, float swingProgress) {
        GlStateManager.translate(0.56F, -0.52F, -0.72F);
        GlStateManager.translate(0.0F, equipProgress * -0.6F, 0.0F);
        GlStateManager.rotate(45.0F, 0.0F, 1.0F, 0.0F);
        float f = MathHelper.sin(swingProgress * swingProgress * (float) Math.PI);
        float f1 = MathHelper.sin(MathHelper.sqrt_float(swingProgress) * (float) Math.PI);
        GlStateManager.rotate(f * -20.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(f1 * -20.0F, 0.0F, 0.0F, 1.0F);
        GlStateManager.rotate(f1 * -80.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.scale(0.4F, 0.4F, 0.4F);
    }

    @Unique
    private void myau$func_178105_d(final float swingProgress) {
        final float f = -0.4F * MathHelper.sin(MathHelper.sqrt_float(swingProgress) * (float) Math.PI);
        final float f1 = 0.2F * MathHelper.sin(MathHelper.sqrt_float(swingProgress) * (float) Math.PI * 2.0F);
        final float f2 = -0.2F * MathHelper.sin(swingProgress * (float) Math.PI);
        GlStateManager.translate(f, f1, f2);
    }

    @Unique
    private void myau$tap1(final float equipProgress, final float swingProgress) {
        GlStateManager.translate(0.56F, -0.52F, -0.71999997F);
        GlStateManager.translate(0.0F, equipProgress * -0.15F, 0.0F);
        GlStateManager.rotate(45.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate((swingProgress * 0.8f - (swingProgress * swingProgress) * 0.8f) * -90.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.scale(0.37F, 0.37F, 0.37F);
    }

    @Unique
    private void myau$tap2(final float equipProgress, final float swingProgress) {
        GlStateManager.translate(0.56F, -0.42F, -0.71999997F);
        GlStateManager.translate(0.0F, equipProgress * -0.15F, 0.0F);
        GlStateManager.rotate(30.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(MathHelper.sin(MathHelper.sqrt_float(swingProgress) * (float) Math.PI) * -30.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.scale(0.4F, 0.4F, 0.4F);
    }

    @Unique
    private void myau$slideSwing(float equipProgress, float swingProgress) {
        GlStateManager.translate(0.56F, -0.52F, -0.72F);
        GlStateManager.translate(0.0F, equipProgress * -0.6F, 0.0F);
        GlStateManager.rotate(45.0F, 0.0F, 1.0F, 0.0F);
        float f = MathHelper.sin(swingProgress * swingProgress * (float) Math.PI);
        float f1 = MathHelper.sin(MathHelper.sqrt_float(swingProgress) * (float) Math.PI);
        GlStateManager.rotate(f * -0.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(f1 * -0.0F, 0.0F, 0.0F, 1.0F);
        GlStateManager.rotate(f1 * -80.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.scale(0.4F, 0.4F, 0.4F);
    }

    @Unique
    private void myau$avatar(float equipProgress, float swingProgress) {
        GlStateManager.translate(0.56F, -0.52F, -0.72F);
        GlStateManager.translate(0.0F, equipProgress * -0.6F, 0.0F);
        GlStateManager.rotate(45.0F, 0.0F, 1.0F, 0.0F);
        float f = MathHelper.sin(swingProgress * swingProgress * (float) Math.PI);
        float f1 = MathHelper.sin(MathHelper.sqrt_float(swingProgress) * (float) Math.PI);
        GlStateManager.rotate(f * -20.0F, 0.0F, 1.0F, 0.0F);
        GlStateManager.rotate(f1 * -20.0F, 0.0F, 0.0F, 1.0F);
        GlStateManager.rotate(f1 * -40.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.scale(0.4F, 0.4F, 0.4F);
    }

    @Unique
    private void myau$smallPush(float equipProgress, float swingProgress) {
        GlStateManager.translate(0.56F, -0.52F, -0.72F);
        GlStateManager.translate(0.0F, equipProgress * -0.6F, 0.0F);
        GlStateManager.rotate(45.0F, 0.0F, 1.0F, 0.0F);
        float f = MathHelper.sin(swingProgress * swingProgress * (float) Math.PI);
        float f1 = MathHelper.sin(MathHelper.sqrt_float(swingProgress) * (float) Math.PI);
        GlStateManager.rotate(f * -10.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.rotate(f1 * -10.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.rotate(f1 * -10.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.scale(0.4F, 0.4F, 0.4F);
    }

    @Inject(method = "renderItemInFirstPerson", at = @At("HEAD"), cancellable = true)
    private void myau$renderItemInFirstPerson(float partialTicks, CallbackInfo ci) {
        if (Unfair.moduleManager == null) {
            return;
        }

        Animations animations = (Animations) Unfair.moduleManager.modules.get(Animations.class);
        if (animations == null || !animations.isEnabled()) {
            return;
        }

        final float equipProgress = 1.0F - (this.prevEquippedProgress + (this.equippedProgress - this.prevEquippedProgress) * partialTicks);
        final EntityPlayerSP player = this.mc.thePlayer;
        if (player == null) {
            return;
        }

        final float swingProgress = player.getSwingProgress(partialTicks);
        final float pitch = player.prevRotationPitch + (player.rotationPitch - player.prevRotationPitch) * partialTicks;
        final float yaw = player.prevRotationYaw + (player.rotationYaw - player.prevRotationYaw) * partialTicks;
        final float f4 = MathHelper.sin(MathHelper.sqrt_float(swingProgress) * (float) Math.PI);

        GL11.glTranslated(animations.itemPosX.getValue().doubleValue(), animations.itemPosY.getValue().doubleValue(), animations.itemPosZ.getValue().doubleValue());
        this.rotateArroundXAndY(pitch, yaw);
        this.setLightMapFromPlayer(player);
        this.rotateWithPlayerRotations(player, partialTicks);
        GlStateManager.scale(1.0F, 1.0F, -animations.itemFov.getValue() + 1.0F);
        GlStateManager.enableRescaleNormal();
        GlStateManager.pushMatrix();

        GL11.glTranslated(animations.itemPosX.getValue().doubleValue(), animations.itemPosY.getValue().doubleValue(), animations.itemPosZ.getValue().doubleValue());

        if (this.itemToRender != null) {
            if (this.itemToRender.getItem() instanceof ItemMap) {
                this.renderItemMap(player, pitch, equipProgress, swingProgress);
            } else {
                boolean isUsingItem = player.getItemInUseCount() > 0;
                EnumAction action = isUsingItem ? this.itemToRender.getItemUseAction() : EnumAction.NONE;

                boolean isBlocking = isUsingItem && action == EnumAction.BLOCK;
                boolean cancelEquip = animations.cancelEquip.getValue() && (!animations.cancelEquipBlockingOnly.getValue() || isBlocking);
                float equip = cancelEquip ? 0.0F : equipProgress;

                if (action == EnumAction.NONE) {
                    this.myau$func_178105_d(swingProgress);
                    this.myau$transformFirstPersonItem(equip, swingProgress);
                    GlStateManager.scale(animations.itemSize.getValue() + 1.0F, animations.itemSize.getValue() + 1.0F, animations.itemSize.getValue() + 1.0F);
                } else if (action == EnumAction.EAT || action == EnumAction.DRINK) {
                    this.performDrinking(player, partialTicks);
                    this.myau$transformFirstPersonItem(equip, 0.0F);
                    GlStateManager.scale(animations.itemSize.getValue() + 1.0F, animations.itemSize.getValue() + 1.0F, animations.itemSize.getValue() + 1.0F);
                } else if (action == EnumAction.BLOCK) {
                    String z = animations.mode.getModeString();
                    switch (z) {
                        case "Astolfo": {
                            GL11.glTranslated(animations.blockPosX.getValue().doubleValue(), animations.blockPosY.getValue().doubleValue(), animations.blockPosZ.getValue().doubleValue());
                            final float var9 = MathHelper.sin(MathHelper.sqrt_float(this.mc.thePlayer.getSwingProgress(partialTicks)) * (float) Math.PI);
                            GL11.glTranslated(0.0D, 0.0D, 0.0D);
                            this.myau$transformFirstPersonItem(cancelEquip ? 0.0F : (equipProgress / 2.5f), 0.0f);
                            GlStateManager.rotate(-var9 * 58.0F / 2.0F, var9 / 2.0F, 1.0F, 0.5F);
                            GlStateManager.rotate(-var9 * 43.0F, 1.0F, var9 / 3.0F, -0.0F);
                            doBlockTransformations();
                            break;
                        }
                        case "Scale": {
                            GL11.glTranslated(animations.blockPosX.getValue().doubleValue() + 0.84, animations.blockPosY.getValue().doubleValue() - 0.77, animations.blockPosZ.getValue().doubleValue() - 1.1);
                            GlStateManager.translate(0.56F, -0.52F, -0.71999997F);
                            GlStateManager.translate(0.0F, cancelEquip ? 0.0f : (equipProgress / 0.8f * -0.8F), 0.0F);
                            GlStateManager.rotate(45.0F, 0.0F, 1.0F, 0.0F);
                            final float var3 = MathHelper.sin(swingProgress * swingProgress * (float) Math.PI);
                            final float var4 = MathHelper.sin(MathHelper.sqrt_float(swingProgress) * (float) Math.PI);
                            GlStateManager.rotate(var3 * -27.0F, 0.0F, 0.0F, 0.0F);
                            GlStateManager.rotate(var4 * -27.0F, 0.0F, 0.0F, 0.0F);
                            GlStateManager.rotate(var4 * -27.0F, 0.0F, 0.0F, 0.0F);
                            doBlockTransformations();
                            break;
                        }
                        case "Leaked": {
                            GL11.glTranslated(animations.blockPosX.getValue().doubleValue() + 0.08, animations.blockPosY.getValue().doubleValue() + 0.02, animations.blockPosZ.getValue().doubleValue());
                            final float var = MathHelper.sin(MathHelper.sqrt_float(swingProgress) * (float) Math.PI);
                            this.myau$transformFirstPersonItem(cancelEquip ? 0.0F : (equipProgress / 1.4F), 0.0f);
                            doBlockTransformations();
                            GlStateManager.rotate(-var * 41F, 1.1F, 0.8F, -0.3F);
                            break;
                        }
                        case "Tap1": {
                            GL11.glTranslated(animations.blockPosX.getValue().doubleValue(), animations.blockPosY.getValue().doubleValue(), animations.blockPosZ.getValue().doubleValue());
                            this.myau$tap1(cancelEquip ? 0.0F : equipProgress, swingProgress);
                            doBlockTransformations();
                            break;
                        }
                        case "Tap2": {
                            GL11.glTranslated(animations.blockPosX.getValue().doubleValue(), animations.blockPosY.getValue().doubleValue() - 0.1f, animations.blockPosZ.getValue().doubleValue());
                            this.myau$tap2(cancelEquip ? 0.0F : equipProgress, swingProgress);
                            doBlockTransformations();
                            break;
                        }
                        case "AstolfoSpin": {
                            GL11.glTranslated(animations.blockPosX.getValue().doubleValue(), animations.blockPosY.getValue().doubleValue(), animations.blockPosZ.getValue().doubleValue());
                            GlStateManager.rotate(this.myau$astolfoSpinDelay, 0.0F, 0.0F, -0.1F);
                            this.myau$transformFirstPersonItem(cancelEquip ? 0.0F : (equipProgress / 1.5F), 0.0F);

                            long currentTime = System.currentTimeMillis();
                            long elapsedTime = currentTime - myau$lastUpdateTime;
                            this.myau$astolfoSpinDelay += elapsedTime * 360.0F / 850.0F;
                            this.myau$lastUpdateTime = currentTime;
                            if (this.myau$astolfoSpinDelay > 360.0F) {
                                this.myau$astolfoSpinDelay = 0.0F;
                            }

                            doBlockTransformations();
                            break;
                        }
                        case "Astro": {
                            GL11.glTranslated(animations.blockPosX.getValue().doubleValue(), animations.blockPosY.getValue().doubleValue(), animations.blockPosZ.getValue().doubleValue());
                            this.myau$transformFirstPersonItem(cancelEquip ? 0.0F : (equipProgress / 2.3F), swingProgress);
                            final float var9 = MathHelper.sin(MathHelper.sqrt_float(swingProgress) * (float) Math.PI);
                            GlStateManager.rotate(var9 * 50.0F / 9.0F, -var9, -0.0F, 90.0F);
                            GlStateManager.rotate(var9 * 50.0F, 200.0F, -var9 / 2.0F, -0.0F);
                            doBlockTransformations();
                            break;
                        }
                        case "Hide": {
                            GL11.glTranslated(animations.blockPosX.getValue().doubleValue(), animations.blockPosY.getValue().doubleValue(), animations.blockPosZ.getValue().doubleValue());
                            this.doItemUsedTransformations(swingProgress);
                            this.myau$transformFirstPersonItem(cancelEquip ? 0.0F : equipProgress, swingProgress);
                            break;
                        }
                        case "Slash": {
                            GL11.glTranslated(animations.blockPosX.getValue().doubleValue() + 0.08, animations.blockPosY.getValue().doubleValue() + 0.08, animations.blockPosZ.getValue().doubleValue());
                            final float var = MathHelper.sin(MathHelper.sqrt_float(swingProgress) * (float) Math.PI);
                            this.myau$transformFirstPersonItem(cancelEquip ? 0.0F : (equipProgress / 1.4F), 0.0f);
                            doBlockTransformations();
                            GlStateManager.rotate(-var * 70F, 5F, 13F, 50F);
                            break;
                        }
                        case "Reverse": {
                            GL11.glTranslated(animations.blockPosX.getValue().doubleValue(), animations.blockPosY.getValue().doubleValue() + 0.1, animations.blockPosZ.getValue().doubleValue() - 0.12);
                            this.myau$transformFirstPersonItem(cancelEquip ? 0.0F : (equipProgress / 1.2f), swingProgress);
                            this.doBlockTransformations();
                            GL11.glTranslated(0.08D, -0.1D, -0.3D);
                            break;
                        }
                        case "Smooth": {
                            GL11.glTranslated(animations.blockPosX.getValue().doubleValue() + 0.14, animations.blockPosY.getValue().doubleValue() - 0.1, animations.blockPosZ.getValue().doubleValue() - 0.24);
                            this.myau$transformFirstPersonItem(cancelEquip ? 0.0F : (equipProgress / 1.4f), 0.0f);
                            final float var91 = MathHelper.sin(MathHelper.sqrt_float(swingProgress) * (float) Math.PI);
                            doBlockTransformations();
                            GlStateManager.translate(-0.36f, 0.25f, -0.06f);
                            GlStateManager.rotate(-var91 * 35.0f, -8.0f, -0.0f, 9.0f);
                            GlStateManager.rotate(-var91 * 70.0f, 1.0f, 0.4f, -0.0f);
                            break;
                        }
                        case "Rhys": {
                            GL11.glTranslated(animations.blockPosX.getValue().doubleValue(), animations.blockPosY.getValue().doubleValue() + 0.19, animations.blockPosZ.getValue().doubleValue());
                            this.myau$transformFirstPersonItem(cancelEquip ? 0.0F : (equipProgress / 1.4f), 0.0f);
                            GlStateManager.translate(0.41F, -0.25F, -0.5555557F);
                            GlStateManager.rotate(35.0F, 0f, 1.5F, 0.0F);
                            final float swingSin = MathHelper.sin(swingProgress * swingProgress / 64 * (float) Math.PI);
                            GlStateManager.rotate(swingSin * -5.0F, 0.0F, 0.0F, 0.0F);
                            GlStateManager.rotate(f4 * -12.0F, 0.0F, 0.0F, 1.0F);
                            GlStateManager.rotate(f4 * -65.0F, 1.0F, 0.0F, 0.0F);
                            this.doBlockTransformations();
                            break;
                        }
                        case "Stab": {
                            GL11.glTranslated(animations.blockPosX.getValue().doubleValue() - 0.25, animations.blockPosY.getValue().doubleValue() + 0.45, animations.blockPosZ.getValue().doubleValue() + 0.8);
                            this.myau$transformFirstPersonItem(cancelEquip ? 0.0F : (equipProgress / 1.5f), 0.0f);
                            final float spin = MathHelper.sin(MathHelper.sqrt_float(swingProgress) * (float) Math.PI);
                            GlStateManager.translate(0.6f, 0.3f, -0.6f + -spin * 0.7f);
                            GlStateManager.rotate(6090, 0.0f, 0.0f, 0.1f);
                            GlStateManager.rotate(6085, 0.0f, 0.1f, 0.0f);
                            GlStateManager.rotate(6110, 0.1f, 0.0f, 0.0f);
                            this.myau$transformFirstPersonItem(0.0F, 0.0f);
                            this.doBlockTransformations();
                            break;
                        }
                        case "Winter": {
                            GL11.glTranslated(animations.blockPosX.getValue().doubleValue(), animations.blockPosY.getValue().doubleValue() - 0.16, animations.blockPosZ.getValue().doubleValue());
                            this.myau$transformFirstPersonItem(cancelEquip ? 0.0F : (equipProgress / 1.5f), swingProgress);
                            doBlockTransformations();
                            GL11.glTranslatef(-0.35F, 0.1F, 0.0F);
                            GL11.glTranslatef(-0.05F, -0.1F, 0.1F);
                            break;
                        }
                        case "Slide": {
                            GL11.glTranslated(animations.blockPosX.getValue().doubleValue() + 0.08, animations.blockPosY.getValue().doubleValue() - 0.11, animations.blockPosZ.getValue().doubleValue() - 0.07);
                            this.myau$transformFirstPersonItem(cancelEquip ? 0.0F : (equipProgress / 1.5F), 0.0F);
                            final float var91 = MathHelper.sin(MathHelper.sqrt_float(swingProgress) * (float) Math.PI);
                            doBlockTransformations();
                            GlStateManager.translate(-0.4f, 0.28f, 0.0f);
                            GlStateManager.rotate(-var91 * 35.0f, -8.0f, -0.0f, 9.0f);
                            GlStateManager.rotate(-var91 * 70.0f, 1.0f, -0.4f, -0.0f);
                            break;
                        }
                        case "Sigma4": {
                            GL11.glTranslated(animations.blockPosX.getValue().doubleValue() - 0.6, animations.blockPosY.getValue().doubleValue() - 0.17, animations.blockPosZ.getValue().doubleValue() + 0.11);
                            final float var = MathHelper.sin(MathHelper.sqrt_float(swingProgress) * (float) Math.PI);
                            this.myau$transformFirstPersonItem(cancelEquip ? 0.0F : (equipProgress / 1.8F), 0.0F);
                            GlStateManager.rotate(-var * 55 / 2.0F, -8.0F, -0.0F, 9.0F);
                            GlStateManager.rotate(-var * 45, 1.0F, var / 2, 0.0F);
                            doBlockTransformations();
                            GL11.glTranslated(-0.08, -1.25, 1.25);
                            break;
                        }
                        case "Old": {
                            GL11.glTranslated(animations.blockPosX.getValue().doubleValue() + 0.08, animations.blockPosY.getValue().doubleValue() - 0.14, animations.blockPosZ.getValue().doubleValue() - 0.05);
                            this.myau$transformFirstPersonItem(cancelEquip ? 0.0F : equipProgress, swingProgress);
                            this.doBlockTransformations();
                            GlStateManager.translate(-0.35F, 0.2F, 0.0F);
                            break;
                        }
                        case "Jigsaw": {
                            GL11.glTranslated(animations.blockPosX.getValue().doubleValue(), animations.blockPosY.getValue().doubleValue() - 0.18, animations.blockPosZ.getValue().doubleValue() - 0.1);
                            this.myau$transformFirstPersonItem(cancelEquip ? 0.0F : equipProgress, swingProgress);
                            doBlockTransformations();
                            GlStateManager.translate(-0.5D, 0.0D, 0.0D);
                            break;
                        }
                        case "Small": {
                            GL11.glTranslated(animations.blockPosX.getValue().doubleValue() - 0.01, animations.blockPosY.getValue().doubleValue() + 0.03, animations.blockPosZ.getValue().doubleValue() - 0.24);
                            this.myau$transformFirstPersonItem(cancelEquip ? 0.0F : equipProgress, swingProgress);
                            this.doBlockTransformations();
                            break;
                        }
                        case "Dash": {
                            GL11.glTranslated(animations.blockPosX.getValue().doubleValue(), animations.blockPosY.getValue().doubleValue(), animations.blockPosZ.getValue().doubleValue());
                            final float var9 = MathHelper.sin(MathHelper.sqrt_float(swingProgress) * (float) Math.PI);
                            this.myau$transformFirstPersonItem(cancelEquip ? 0.0F : (equipProgress / 2.4f), 0.0f);
                            GL11.glRotated(-var9 * 22.0F, var9 / 2, 0.0F, 9.0F);
                            GL11.glRotated(-var9 * 50.0F, 0.8F, var9 / 2, 0F);
                            doBlockTransformations();
                            break;
                        }
                        case "Remix": {
                            GL11.glTranslated(animations.blockPosX.getValue().doubleValue(), animations.blockPosY.getValue().doubleValue(), animations.blockPosZ.getValue().doubleValue());
                            final float var = MathHelper.sin(MathHelper.sqrt_float(swingProgress) * (float) Math.PI);
                            this.myau$transformFirstPersonItem(cancelEquip ? 0.0F : (equipProgress / 1.4f), 1.0f);
                            doBlockTransformations();
                            GlStateManager.rotate(0.0F, -2.0F, 0.0F, 10.0F);
                            GlStateManager.rotate(-var * 25.0F, 0.5F, 0F, 1F);
                            break;
                        }
                        case "Xiv": {
                            GL11.glTranslated(animations.blockPosX.getValue().doubleValue(), animations.blockPosY.getValue().doubleValue(), animations.blockPosZ.getValue().doubleValue());
                            final float var = MathHelper.sin(MathHelper.sqrt_float(swingProgress) * (float) Math.PI);
                            this.myau$transformFirstPersonItem(cancelEquip ? 0.0F : (equipProgress / 1.5f), 0.0f);
                            doBlockTransformations();
                            final float var16 = MathHelper.sin(swingProgress * swingProgress * (float) Math.PI);
                            GlStateManager.rotate(-var16 * 20.0f, 0.0f, 1.0f, 0.0f);
                            GlStateManager.rotate(-var * 20.0f, 0.0f, 0.0f, 1.0f);
                            GlStateManager.rotate(-var * 80.0f, 1.0f, 0.0f, 0.0f);
                            break;
                        }
                        case "Swank": {
                            GL11.glTranslated(animations.blockPosX.getValue().doubleValue(), animations.blockPosY.getValue().doubleValue(), animations.blockPosZ.getValue().doubleValue());
                            final float var9 = MathHelper.sin(MathHelper.sqrt_float(swingProgress) * (float) Math.PI);
                            this.myau$transformFirstPersonItem(cancelEquip ? 0.0F : (equipProgress / 2.0f), swingProgress);
                            GL11.glRotatef(var9 * 30.0F / 2.0F, -var9, -0.0F, 9.0F);
                            GL11.glRotatef(var9 * 40.0F, 1.0F, -var9 / 2.0F, -0.0F);
                            doBlockTransformations();
                            break;
                        }
                        case "Swonk": {
                            GL11.glTranslated(animations.blockPosX.getValue().doubleValue(), animations.blockPosY.getValue().doubleValue() + 0.03, animations.blockPosZ.getValue().doubleValue());
                            final float var9 = MathHelper.sin(MathHelper.sqrt_float(swingProgress) * (float) Math.PI);
                            this.myau$transformFirstPersonItem(cancelEquip ? 0.0F : (equipProgress / 1.8f), 0.0f);
                            GL11.glRotated(-var9 * -30.0F / 2.0F, var9 / 2.0F, 1.0F, 4.0F);
                            GL11.glRotated(-var9 * 7.5F, 1.0F, var9 / 3.0F, -0.0F);
                            doBlockTransformations();
                            break;
                        }
                        case "MoonPush": {
                            GL11.glTranslated(animations.blockPosX.getValue().doubleValue(), animations.blockPosY.getValue().doubleValue(), animations.blockPosZ.getValue().doubleValue());
                            this.myau$transformFirstPersonItem(cancelEquip ? 0.0F : (equipProgress / 1.5f), 0.0f);
                            doBlockTransformations();
                            final float sin = MathHelper.sin(MathHelper.sqrt_float(swingProgress) * (float) Math.PI);
                            GlStateManager.translate(-0.2F, 0.45F, 0.25F);
                            GlStateManager.rotate(-sin * 20.0F, -5.0F, -5.0F, 9.0F);
                            break;
                        }
                        case "Stella": {
                            GL11.glTranslated(animations.blockPosX.getValue().doubleValue(), animations.blockPosY.getValue().doubleValue(), animations.blockPosZ.getValue().doubleValue());
                            this.myau$transformFirstPersonItem(cancelEquip ? -0.1F : (equipProgress - 0.1F / 1.2f), swingProgress);
                            GlStateManager.translate(-0.5F, 0.3F, -0.2F);
                            GlStateManager.rotate(32, 0, 1, 0);
                            GlStateManager.rotate(-70, 1, 0, 0);
                            GlStateManager.rotate(40, 0, 1, 0);
                            break;
                        }
                        case "Sigma3": {
                            GL11.glTranslated(animations.blockPosX.getValue().doubleValue() + 0.02, animations.blockPosY.getValue().doubleValue() + 0.02, animations.blockPosZ.getValue().doubleValue());
                            this.myau$transformFirstPersonItem(cancelEquip ? 0.0F : (equipProgress / 2.0f), swingProgress);
                            GL11.glTranslated(0.4D, -0.06D, -0.46D);
                            final float swang = MathHelper.sin(MathHelper.sqrt_float(swingProgress) * (float) Math.PI);
                            GlStateManager.rotate(swang * 25.0F / 2.0F, -swang, -0.0F, 9.0F);
                            GlStateManager.rotate(swang * 15.0F, 1.0F, -swang / 2.0F, -0.0F);
                            doBlockTransformations();
                            break;
                        }
                        case "Push": {
                            GL11.glTranslated(animations.blockPosX.getValue().doubleValue(), animations.blockPosY.getValue().doubleValue(), animations.blockPosZ.getValue().doubleValue());
                            final float var9 = MathHelper.sin(MathHelper.sqrt_float(this.mc.thePlayer.getSwingProgress(partialTicks)) * (float) Math.PI);
                            this.myau$transformFirstPersonItem(cancelEquip ? 0.0F : (equipProgress / 2.5f), 0.0f);
                            GlStateManager.rotate(-var9 * 40.0F / 2.0F, var9 / 2.0F, 1.0F, 4.0F);
                            GlStateManager.rotate(-var9 * 30.0F, 1.0F, var9 / 3.0F, -0.0F);
                            doBlockTransformations();
                            break;
                        }
                        case "Yamato": {
                            GL11.glTranslated(animations.blockPosX.getValue().doubleValue(), animations.blockPosY.getValue().doubleValue(), animations.blockPosZ.getValue().doubleValue());
                            final float var9 = MathHelper.sin(MathHelper.sqrt_float(swingProgress) * (float) Math.PI);
                            this.myau$transformFirstPersonItem(cancelEquip ? 0.0F : (equipProgress / 2.5f), 0.0f);
                            doBlockTransformations();
                            GL11.glRotatef(-var9 * 200F / 2.0F, -9.0F, 5.0F, 9.0F);
                            break;
                        }
                        case "Aqua": {
                            GL11.glTranslated(animations.blockPosX.getValue().doubleValue(), animations.blockPosY.getValue().doubleValue(), animations.blockPosZ.getValue().doubleValue());
                            final float var9 = MathHelper.sin(MathHelper.sqrt_float(swingProgress) * (float) Math.PI);
                            this.myau$transformFirstPersonItem(cancelEquip ? 0.0F : (equipProgress / 6.0f), 0.0f);
                            GlStateManager.rotate(-var9 * 17.0F / 2.0F, var9 / 2.0F, 1.0F, 4.0F);
                            GlStateManager.rotate(-var9 * 6.0F, 1.0F, var9 / 3.0F, -0.0F);
                            doBlockTransformations();
                            break;
                        }
                        case "Swang": {
                            GL11.glTranslated(animations.blockPosX.getValue().doubleValue(), animations.blockPosY.getValue().doubleValue() + 0.03, animations.blockPosZ.getValue().doubleValue());
                            final float var9 = MathHelper.sin(MathHelper.sqrt_float(swingProgress) * (float) Math.PI);
                            this.myau$transformFirstPersonItem(cancelEquip ? 0.0F : (equipProgress / 2.5f), 0.0f);
                            GlStateManager.rotate(-var9 * 74.0F / 2.0F, var9 / 2.0F, 1.0F, 4.0F);
                            GlStateManager.rotate(-var9 * 52.0F, 1.0F, var9 / 3.0F, -0.0F);
                            doBlockTransformations();
                            break;
                        }
                        case "Moon": {
                            GL11.glTranslated(animations.blockPosX.getValue().doubleValue() - 0.08, animations.blockPosY.getValue().doubleValue() + 0.12, animations.blockPosZ.getValue().doubleValue());
                            final float var9 = MathHelper.sin(MathHelper.sqrt_float(swingProgress) * (float) Math.PI);
                            this.myau$transformFirstPersonItem(cancelEquip ? 0.0F : (equipProgress / 1.4f), 0.0f);
                            GlStateManager.rotate(-var9 * 65.0F / 2.0F, var9 / 2.0F, 1.0F, 4.0F);
                            GlStateManager.rotate(-var9 * 60.0F, 1.0F, var9 / 3.0F, -0.0F);
                            doBlockTransformations();
                            break;
                        }
                        case "1_8": {
                            GL11.glTranslated(animations.blockPosX.getValue().doubleValue(), animations.blockPosY.getValue().doubleValue(), animations.blockPosZ.getValue().doubleValue());
                            this.myau$transformFirstPersonItem(cancelEquip ? 0.0F : equipProgress, 0.0f);
                            doBlockTransformations();
                            break;
                        }
                        case "Swing": {
                            GL11.glTranslated(animations.blockPosX.getValue().doubleValue(), animations.blockPosY.getValue().doubleValue(), animations.blockPosZ.getValue().doubleValue());
                            this.myau$transformFirstPersonItem(cancelEquip ? 0.0F : equipProgress, swingProgress);
                            this.doBlockTransformations();
                            break;
                        }
                        case "SlideSwing": {
                            GL11.glTranslated(animations.blockPosX.getValue().doubleValue(), animations.blockPosY.getValue().doubleValue(), animations.blockPosZ.getValue().doubleValue());
                            this.myau$slideSwing(cancelEquip ? 0.0F : equipProgress, swingProgress);
                            this.doBlockTransformations();
                            break;
                        }
                        case "SmallPush": {
                            GL11.glTranslated(animations.blockPosX.getValue().doubleValue(), animations.blockPosY.getValue().doubleValue(), animations.blockPosZ.getValue().doubleValue());
                            this.myau$smallPush(cancelEquip ? 0.0F : (equipProgress / 1.8f), swingProgress);
                            this.doBlockTransformations();
                            break;
                        }
                        case "Avatar": {
                            GL11.glTranslated(animations.blockPosX.getValue().doubleValue(), animations.blockPosY.getValue().doubleValue(), animations.blockPosZ.getValue().doubleValue());
                            this.myau$avatar(cancelEquip ? 0.0F : (equipProgress / 2.5f), swingProgress);
                            this.doBlockTransformations();
                            break;
                        }
                        case "Float": {
                            GL11.glTranslated(animations.blockPosX.getValue().doubleValue(), animations.blockPosY.getValue().doubleValue(), animations.blockPosZ.getValue().doubleValue());
                            this.myau$transformFirstPersonItem(cancelEquip ? 0.0F : (equipProgress / 2.0f), 0.0f);
                            GlStateManager.rotate(-MathHelper.sin(swingProgress * swingProgress * (float) Math.PI) * 40.0F / 2.0F, MathHelper.sin(swingProgress * swingProgress * (float) Math.PI) / 2.0F, -0.0F, 9.0F);
                            GlStateManager.rotate(-MathHelper.sin(swingProgress * swingProgress * (float) Math.PI) * 30.0F, 1.0F, MathHelper.sin(swingProgress * swingProgress * (float) Math.PI) / 2.0F, -0.0F);
                            this.doBlockTransformations();
                            break;
                        }
                        case "Invent": {
                            GL11.glTranslated(animations.blockPosX.getValue().doubleValue(), animations.blockPosY.getValue().doubleValue(), animations.blockPosZ.getValue().doubleValue());
                            final float table = MathHelper.sin(MathHelper.sqrt_float(swingProgress) * (float) Math.PI);
                            GlStateManager.rotate(-table * 30.0F, -8.0F, -0.2F, 9.0F);
                            this.myau$transformFirstPersonItem(cancelEquip ? 0.0F : (equipProgress / 1.8f), 0.0f);
                            this.doBlockTransformations();
                            break;
                        }
                        case "Fadeaway": {
                            GL11.glTranslated(animations.blockPosX.getValue().doubleValue(), animations.blockPosY.getValue().doubleValue(), animations.blockPosZ.getValue().doubleValue());
                            final float var = MathHelper.sin(MathHelper.sqrt_float(swingProgress) * (float) Math.PI);
                            this.myau$transformFirstPersonItem(cancelEquip ? 0.0F : (equipProgress / 1.4f), -0.3f);
                            doBlockTransformations();
                            final float var16 = MathHelper.sin(swingProgress * swingProgress * (float) Math.PI);
                            GlStateManager.rotate(-var16 * 45f, 0.0f, 0.0f, 1.0f);
                            GlStateManager.rotate(-var * 0f, 0.0f, 0.0f, 1.0f);
                            GlStateManager.rotate(-var * 0f, 1.5f, 0.0f, 0.0f);
                            break;
                        }
                        case "Edit": {
                            GL11.glTranslated(animations.blockPosX.getValue().doubleValue() - 0.04, animations.blockPosY.getValue().doubleValue() + 0.06, animations.blockPosZ.getValue().doubleValue());
                            this.myau$transformFirstPersonItem(cancelEquip ? 0.0F : (equipProgress / 1.4f), swingProgress);
                            final float swang = MathHelper.sin(MathHelper.sqrt_float(swingProgress) * (float) Math.PI);
                            GlStateManager.rotate(swang * 16.0F / 2.0F, -swang, -0.0F, 2.0F);
                            GlStateManager.rotate(swang * 22.0F, 1.0F, -swang / 3.0F, -0.0F);
                            doBlockTransformations();
                            break;
                        }
                        case "Test": {
                            GL11.glTranslated(animations.blockPosX.getValue().doubleValue() - 0.13, animations.blockPosY.getValue().doubleValue() + 0.17, animations.blockPosZ.getValue().doubleValue());
                            this.myau$transformFirstPersonItem(cancelEquip ? 0.0F : (equipProgress / 1.3f), swingProgress);
                            final float swang = MathHelper.sin(MathHelper.sqrt_float(swingProgress) * (float) Math.PI);
                            GlStateManager.rotate(swang * 23.0F / 3.0F, -swang, -0.1F, 3.0F);
                            GlStateManager.rotate(swang * 15.0F, 1.0F, -swang / 2.0F, -0.1F);
                            doBlockTransformations();
                            break;
                        }
                        default:
                            this.myau$transformFirstPersonItem(equip, 0.0F);
                            this.doBlockTransformations();
                            break;
                    }

                    GlStateManager.scale(animations.itemSize.getValue() + 1.0F, animations.itemSize.getValue() + 1.0F, animations.itemSize.getValue() + 1.0F);
                } else if (action == EnumAction.BOW) {
                    this.myau$transformFirstPersonItem(equip, 0.0F);
                    this.doBowTransformations(partialTicks, player);
                    GlStateManager.scale(animations.itemSize.getValue() + 1.0F, animations.itemSize.getValue() + 1.0F, animations.itemSize.getValue() + 1.0F);
                } else {
                    this.myau$transformFirstPersonItem(equip, swingProgress);
                    GlStateManager.scale(animations.itemSize.getValue() + 1.0F, animations.itemSize.getValue() + 1.0F, animations.itemSize.getValue() + 1.0F);
                }

                this.renderItem(player, this.itemToRender, ItemCameraTransforms.TransformType.FIRST_PERSON);
            }
        } else if (!player.isInvisible()) {
            this.renderPlayerArm(player, equipProgress, swingProgress);
        }

        GlStateManager.popMatrix();
        GlStateManager.disableRescaleNormal();
        RenderHelper.disableStandardItemLighting();
        GL11.glTranslated(-animations.itemPosX.getValue().doubleValue(), -animations.itemPosY.getValue().doubleValue(), -animations.itemPosZ.getValue().doubleValue());

        ci.cancel();
    }
}
