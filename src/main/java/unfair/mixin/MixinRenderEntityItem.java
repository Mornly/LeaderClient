package unfair.mixin;


import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.client.renderer.entity.RenderEntityItem;
import net.minecraft.client.renderer.entity.RenderItem;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.resources.model.IBakedModel;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import org.lwjgl.opengl.GL11;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import unfair.Unfair;
import unfair.module.modules.render.ItemPhysics;

import java.util.Random;

@Mixin(RenderEntityItem.class)
public abstract class MixinRenderEntityItem extends Render<EntityItem> {

    // 映射原版的私有字段
    @Shadow private Random field_177079_e;
    @Shadow @Final
    private RenderItem itemRenderer;


    protected MixinRenderEntityItem(RenderManager renderManager) {
        super(renderManager);
    }

    /**
     * 接管 doRender 方法
     * 使用 @Inject 在方法头部拦截，如果开启了物理掉落，则执行自定义逻辑并取消原版后续逻辑
     */
    @Inject(method = "doRender", at = @At("HEAD"), cancellable = true)
    public void onDoRender(EntityItem entity, double x, double y, double z, float entityYaw, float partialTicks, CallbackInfo ci) {
        if (Unfair.moduleManager.modules.get(ItemPhysics.class).isEnabled()) {
            this.doPhysicsRender(entity, x, y, z, entityYaw, partialTicks);
            ci.cancel();
        }
    }

    // 将你代码中的 physicsRender 逻辑迁移到这里
    private void doPhysicsRender(EntityItem entity, double x, double y, double z, float entityYaw, float partialTicks) {
        Minecraft mc = Minecraft.getMinecraft();


        ItemStack itemstack = entity.getEntityItem();
        if (itemstack == null || itemstack.getItem() == null) return;
        int seed = Item.getIdFromItem(itemstack.getItem()) + itemstack.getMetadata();
        this.field_177079_e.setSeed(seed);

        this.bindEntityTexture(entity);
        this.renderManager.renderEngine.getTexture(this.getEntityTexture(entity)).setBlurMipmap(false, false);

        GlStateManager.enableRescaleNormal();
        GlStateManager.alphaFunc(516, 0.1F);
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.pushMatrix();

        IBakedModel ibakedmodel = this.itemRenderer.getItemModelMesher().getItemModel(itemstack);
        int j = this.func_177078_a(itemstack); // 调用原版的堆叠计算方法

        GlStateManager.translate((float) x, (float) y, (float) z);

        if (ibakedmodel.isGui3d()) {
            GlStateManager.scale(0.5F, 0.5F, 0.5F);
        }

        GlStateManager.rotate(90.0F, 1.0F, 0.0F, 0.0F);
        GlStateManager.rotate(entity.rotationYaw, 0.0F, 0.0F, 1.0F);

        if (!entity.onGround) {
            entity.rotationPitch +=  (((IAccessorMinecraft)mc).getTimer().renderPartialTicks) * ItemPhysics.rollSpeed.getValue(); // 这里需要注意，直接改实体的 rotationPitch 可能会同步到服务端或影响其他逻辑
        } else {
            entity.rotationPitch = 0;
        }
        GlStateManager.rotate(entity.rotationPitch, 1, 0, 0);

        for (int k = 0; k < j; ++k) {
            GlStateManager.pushMatrix();
            if (ibakedmodel.isGui3d()) {
                if (k > 0) {
                    float f7 = (this.field_177079_e.nextFloat() * 2.0F - 1.0F) * 0.15F;
                    float f9 = (this.field_177079_e.nextFloat() * 2.0F - 1.0F) * 0.15F;
                    float f6 = (this.field_177079_e.nextFloat() * 2.0F - 1.0F) * 0.15F;
                    GlStateManager.translate(f7, f9, f6);
                }
                this.itemRenderer.renderItem(itemstack, ibakedmodel);
            } else {
                if (k > 0) {
                    GlStateManager.translate(0, 0, 0.05375F * k);
                }
                this.itemRenderer.renderItem(itemstack, ibakedmodel);
            }
            GlStateManager.popMatrix();
        }

        GlStateManager.popMatrix();
        GlStateManager.disableRescaleNormal();
        GlStateManager.disableBlend();
        this.bindEntityTexture(entity);
        this.renderManager.renderEngine.getTexture(this.getEntityTexture(entity)).restoreLastBlurMipmap();
    }
    @Shadow
    protected abstract int func_177078_a(ItemStack stack);
}