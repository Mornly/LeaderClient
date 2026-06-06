package unfair.module.modules.render;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.client.shader.Shader;
import net.minecraft.client.shader.ShaderGroup;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import unfair.event.EventTarget;
import unfair.events.Render2DEvent;
import unfair.mixin.IShaderGroupAccessor;
import unfair.module.Module;
import unfair.property.properties.FloatProperty;

import java.util.List;

import static unfair.config.Config.mc;


public class MotionBlur extends Module {
    public MotionBlur(){super("MotionBlur",false,false);}
    public static final FloatProperty blurAmount = new FloatProperty("Blur Amount", 2f, 1f, 10f);
    private Framebuffer blurBufferMain = null;
    private Framebuffer blurBufferInto = null;
    @EventTarget
    public void onRender(Render2DEvent event) {
        if (mc.theWorld != null) {
            if (isEnabled()) {
                if ((mc.entityRenderer.getShaderGroup() == null))
                    mc.entityRenderer.loadShader(new ResourceLocation("minecraft", "shaders/post/motion_blur.json"));
                float uniform = 1F - Math.min(blurAmount.getValue() / 10F, 0.9f);
                ShaderGroup shaderGroup = mc.entityRenderer.getShaderGroup();
                if (shaderGroup != null) {
                    IShaderGroupAccessor accessor = (IShaderGroupAccessor) shaderGroup;
                    List<Shader> shaders = accessor.getListShaders();
                    shaders.get(0).getShaderManager().getShaderUniform("Phosphor").set(uniform, 0F, 0F);
                }
            } else {
                    if (mc.entityRenderer.isShaderActive())
                        mc.entityRenderer.stopUseShader();
                }
            }
    }

    private static Framebuffer checkFramebufferSizes(Framebuffer framebuffer, int width, int height) {
        if (framebuffer == null || framebuffer.framebufferWidth != width || framebuffer.framebufferHeight != height) {
            if (framebuffer == null) {
                framebuffer = new Framebuffer(width, height, true);
            } else {
                framebuffer.createBindFramebuffer(width, height);
            }
            framebuffer.setFramebufferFilter(9728);
        }
        return framebuffer;
    }
    public static void drawTexturedRectNoBlend(float x, float y, float width, float height, float uMin, float uMax, float vMin, float vMax, int filter) {
        GlStateManager.enableTexture2D();
        GL11.glTexParameteri(3553, 10241, filter);
        GL11.glTexParameteri(3553, 10240, filter);
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_TEX);
        worldrenderer.pos(x, y + height, 0.0).tex(uMin, vMax).endVertex();
        worldrenderer.pos(x + width, y + height, 0.0).tex(uMax, vMax).endVertex();
        worldrenderer.pos(x + width, y, 0.0).tex(uMax, vMin).endVertex();
        worldrenderer.pos(x, y, 0.0).tex(uMin, vMin).endVertex();
        tessellator.draw();
        GL11.glTexParameteri(3553, 10241, 9728);
        GL11.glTexParameteri(3553, 10240, 9728);
    }
    public void onBlurScreen() {
        if (OpenGlHelper.isFramebufferEnabled()) {
            int width = mc.getFramebuffer().framebufferWidth;
            int height = mc.getFramebuffer().framebufferHeight;
            GlStateManager.matrixMode(5889);
            GlStateManager.loadIdentity();
            GlStateManager.ortho(0.0, width, height, 0.0, 2000.0, 4000.0);
            GlStateManager.matrixMode(5888);
            GlStateManager.loadIdentity();
            GlStateManager.translate(0.0f, 0.0f, -2000.0f);
            this.blurBufferMain = checkFramebufferSizes(this.blurBufferMain, width, height);
            this.blurBufferInto = checkFramebufferSizes(this.blurBufferInto, width, height);
            this.blurBufferInto.framebufferClear();
            this.blurBufferInto.bindFramebuffer(true);
            OpenGlHelper.glBlendFunc(770, 771, 0, 1);
            GlStateManager.disableLighting();
            GlStateManager.disableFog();
            GlStateManager.disableBlend();
            mc.getFramebuffer().bindFramebufferTexture();
            GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
            drawTexturedRectNoBlend(0.0f, 0.0f, width, height, 0.0f, 1.0f, 0.0f, 1.0f, 9728);
            GlStateManager.enableBlend();
            this.blurBufferMain.bindFramebufferTexture();
            GlStateManager.color(1.0f, 1.0f, 1.0f, (float)(blurAmount.getValue() * 10) / 100.0f - 0.1f);
            drawTexturedRectNoBlend(0.0f, 0.0f, width, height, 0.0f, 1.0f, 1.0f, 0.0f, 9728);
            mc.getFramebuffer().bindFramebuffer(true);
            this.blurBufferInto.bindFramebufferTexture();
            GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
            GlStateManager.enableBlend();
            OpenGlHelper.glBlendFunc(770, 771, 1, 771);
            drawTexturedRectNoBlend(0.0f, 0.0f, width, height, 0.0f, 1.0f, 0.0f, 1.0f, 9728);
            Framebuffer tempBuff = this.blurBufferMain;
            this.blurBufferMain = this.blurBufferInto;
            this.blurBufferInto = tempBuff;
        }
}
}
