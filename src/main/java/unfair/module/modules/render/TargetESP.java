package unfair.module.modules.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.EntityRenderer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.play.client.C02PacketUseEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;
import org.lwjgl.opengl.GL11;
import unfair.event.EventTarget;
import unfair.event.types.EventType;
import unfair.events.PacketEvent;
import unfair.events.UpdateEvent;
import unfair.events.Render2DEvent;
import unfair.events.Render3DEvent;
import unfair.events.Shader2DEvent;
import unfair.mixin.IAccessorEntityRenderer;
import unfair.mixin.IAccessorRenderManager;
import unfair.mixin.IAccessorMinecraft;
import unfair.module.Module;
import unfair.property.properties.BooleanProperty;
import unfair.property.properties.ModeProperty;
import unfair.property.properties.FloatProperty;
import unfair.property.properties.ColorProperty;
import unfair.util.*;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

public class TargetESP extends Module {
    private static final Minecraft mc = Minecraft.getMinecraft();

    public final ModeProperty mode = new ModeProperty("MarkMode", 1, new String[]{"Points", "Ghost", "Image", "Exhi", "Circle"});
    public final ModeProperty imageMode = new ModeProperty("ImageMode", 0, new String[]{"Rectangle", "QuadStapple", "TriangleStapple", "TriangleStipple", "Aim", "Custom"},
            () -> mode.getValue() == 2);
    public final BooleanProperty animation = new BooleanProperty("Animation", true, () -> mode.getValue() == 2 && imageMode.getValue() == 5);
    public final BooleanProperty selectImage = new BooleanProperty("SelectImage", false, () -> mode.getValue() == 2 && imageMode.getValue() == 5) {
        @Override
        public boolean setValue(Object value) {
            boolean result = super.setValue(value);
            if (result && (Boolean) value) {
                selectCustomImage();
                super.setValue(false);
            }
            return result;
        }
    };
    public final FloatProperty circleSpeed = new FloatProperty("CircleSpeed", 2.0F, 1.0F, 5.0F, () -> mode.getValue() == 4);
    public final BooleanProperty onlyPlayer = new BooleanProperty("OnlyPlayer", false);
    public final BooleanProperty showHurt = new BooleanProperty("ShowHurt", false, () -> mode.getValue() == 2);
    public final ColorProperty moduleColor = new ColorProperty("Color", 0xFFFFFF);

    private ResourceLocation customImage = null;
    private long lastHurtTime = 0;
    private static final long HURT_DURATION = 500;

    private EntityLivingBase target;
    private final TimerUtil displayTimer = new TimerUtil();
    private final TimerUtil animTimer = new TimerUtil();
    private long lastTime = System.currentTimeMillis();
    private boolean hasFullyFadedIn = false;
    private final Animation alphaAnim = new DecelerateAnimation(400, 1);

    private final ResourceLocation glowCircle = new ResourceLocation("minecraft", "unfair/texture/targetesp/glow_circle.png");
    private final ResourceLocation rectangle = new ResourceLocation("minecraft", "unfair/texture/targetesp/rectangle.png");
    private final ResourceLocation quadstapple = new ResourceLocation("minecraftr", "unfair/texture/targetesp/quadstapple.png");
    private final ResourceLocation trianglestapple = new ResourceLocation("minecraft", "unfair/texture/targetesp/trianglestapple.png");
    private final ResourceLocation trianglestipple = new ResourceLocation("minecraft", "unfair/texture/targetesp/trianglestipple.png");
    private final ResourceLocation aim = new ResourceLocation("minecraft", "unfair/texture/targetesp/shenmi.png");

    public double prevCircleStep;
    public double circleStep;

    public TargetESP() {
        super("TargetESP", false);
    }

    private void selectCustomImage() {
        new Thread(() -> {
            FileDialog fileDialog = new FileDialog((java.awt.Frame) null, "Select Custom Image", FileDialog.LOAD);
            fileDialog.setFile("*.png");
            fileDialog.setFilenameFilter((dir, name) -> name.toLowerCase().endsWith(".png"));
            fileDialog.setVisible(true);

            String file = fileDialog.getFile();
            if (file != null) {
                String directory = fileDialog.getDirectory();
                File imageFile = new File(directory, file);
                try {
                    BufferedImage image = ImageIO.read(imageFile);
                    if (image != null) {
                        ResourceLocation newImage = new ResourceLocation("unfair", "custom_target_" + System.currentTimeMillis());
                        mc.addScheduledTask(() -> {
                            mc.getTextureManager().loadTexture(newImage, new net.minecraft.client.renderer.texture.DynamicTexture(image));
                            customImage = newImage;
                        });
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }, "Image Selector Thread").start();
    }

    private Color getModuleColor() {
        return new Color(moduleColor.getValue());
    }

    @Override
    public void onEnabled() {
        target = null;
        alphaAnim.reset();
        displayTimer.reset();
        animTimer.reset();
        lastTime = System.currentTimeMillis();
        hasFullyFadedIn = false;
        prevCircleStep = 0;
        circleStep = 0;
    }

    @Override
    public void onDisabled() {
        target = null;
        alphaAnim.reset();
    }

    @EventTarget
    public void onPacket(PacketEvent event) {
        if (!isEnabled()) return;
        if (event.getType() == EventType.SEND && event.getPacket() instanceof C02PacketUseEntity) {
            C02PacketUseEntity packet = (C02PacketUseEntity) event.getPacket();
            if (packet.getAction() == C02PacketUseEntity.Action.ATTACK) {
                Entity entity = packet.getEntityFromWorld(mc.theWorld);
                if (entity == target) {
                    lastHurtTime = System.currentTimeMillis();
                }
            }
            if (packet.getAction() != C02PacketUseEntity.Action.ATTACK) return;

            Entity entity = packet.getEntityFromWorld(mc.theWorld);
            if (entity instanceof EntityLivingBase &&
                    (!onlyPlayer.getValue() || entity instanceof EntityPlayer)) {
                EntityLivingBase newTarget = (EntityLivingBase) entity;
                if (target != newTarget) {
                    target = newTarget;
                    lastTime = System.currentTimeMillis();
                    alphaAnim.reset();
                    alphaAnim.setDirection(Animation.Direction.FORWARDS);
                    animTimer.reset();
                    hasFullyFadedIn = false;
                }
                displayTimer.reset();
            }
        }
    }

    @EventTarget
    public void onUpdate(UpdateEvent event) {
        if (!isEnabled()) return;
        if (target != null && displayTimer.hasTimeElapsed(1000)) {
            hasFullyFadedIn = false;
            target = null;
        }
    }

    private float getHurtAlpha() {
        if (!showHurt.getValue()) return 0.0f;

        long timeSinceHurt = System.currentTimeMillis() - lastHurtTime;
        if (timeSinceHurt > HURT_DURATION) return 0.0f;

        float progress = (float) timeSinceHurt / HURT_DURATION;
        if (progress < 0.5f) {
            return progress * 2.0f;
        } else {
            return 2.0f - (progress * 2.0f);
        }
    }

    private float getAlpha() {
        if (target == null) return 0.0f;

        long animElapsed = animTimer.getElapsedTime();
        long displayElapsed = displayTimer.getElapsedTime();

        if (!hasFullyFadedIn) {
            if (animElapsed < 200) {
                return animElapsed / 200.0f;
            } else {
                hasFullyFadedIn = true;
                return 1.0f;
            }
        } else {
            if (displayElapsed > 800) {
                return Math.max(0.0f, (1000 - displayElapsed) / 200.0f);
            } else {
                return 1.0f;
            }
        }
    }

    @EventTarget
    public void onRender3D(Render3DEvent event) {
        if (!isEnabled()) return;
        if (target == null) return;
        int modeVal = mode.getValue();

        if (modeVal == 0) points(event);
        if (modeVal == 3) {
            float alpha = getAlpha();
            int baseAlpha = (int) (75 * alpha);
            int color = target.hurtTime > 3 ? new Color(200, 255, 100, baseAlpha).getRGB() :
                    target.hurtTime < 3 ? new Color(235, 40, 40, baseAlpha).getRGB() :
                            new Color(255, 255, 255, baseAlpha).getRGB();
            GlStateManager.pushMatrix();
            GL11.glShadeModel(GL11.GL_SMOOTH);
            GL11.glHint(GL11.GL_PERSPECTIVE_CORRECTION_HINT, GL11.GL_NICEST);
            ((IAccessorEntityRenderer) mc.entityRenderer).callSetupCameraTransform(event.getPartialTicks(), 2);
            double x = target.prevPosX + (target.posX - target.prevPosX) * event.getPartialTicks() - ((IAccessorRenderManager) mc.getRenderManager()).getRenderPosX();
            double y = target.prevPosY + (target.posY - target.prevPosY) * event.getPartialTicks() - ((IAccessorRenderManager) mc.getRenderManager()).getRenderPosY();
            double z = target.prevPosZ + (target.posZ - target.prevPosZ) * event.getPartialTicks() - ((IAccessorRenderManager) mc.getRenderManager()).getRenderPosZ();
            double motionX = 0.0, motionY = 0.0, motionZ = 0.0;
            GlStateManager.translate(x + (motionX + (mc.thePlayer.motionX + 0.005)),
                    y + (motionY + (mc.thePlayer.motionY - 0.002)),
                    z + (motionZ + (mc.thePlayer.motionZ + 0.005)));
            AxisAlignedBB bb = target.getEntityBoundingBox();
            RenderUtil.drawAxisAlignedBB(new AxisAlignedBB(bb.minX - 0.1 - target.posX,
                    bb.minY - 0.1 - target.posY,
                    bb.minZ - 0.1 - target.posZ,
                    bb.maxX + 0.1 - target.posX,
                    bb.maxY + 0.2 - target.posY,
                    bb.maxZ + 0.1 - target.posZ), true, color);
            GlStateManager.popMatrix();
        }
        if (modeVal == 1) {
            GlStateManager.pushMatrix();
            GlStateManager.disableLighting();
            GlStateManager.depthMask(false);
            GlStateManager.enableBlend();
            GlStateManager.shadeModel(GL11.GL_SMOOTH);
            GlStateManager.disableCull();
            GlStateManager.disableAlpha();
            GlStateManager.tryBlendFuncSeparate(770, 1, 0, 1);

            double radius = 0.67;
            float speed = 45;
            float size = 0.4f;
            double distance = 19;
            int length = 20;

            Vec3 interpolated = MathUtil.interpolate(new Vec3(target.lastTickPosX, target.lastTickPosY, target.lastTickPosZ),
                    target.getPositionVector(), event.getPartialTicks());
            interpolated = new Vec3(interpolated.xCoord, interpolated.yCoord + 0.75f, interpolated.zCoord);

            RenderUtil.setupOrientationMatrix(interpolated.xCoord, interpolated.yCoord + 0.5f, interpolated.zCoord);

            float[] idk = new float[]{mc.getRenderManager().playerViewY, mc.getRenderManager().playerViewX};

            GL11.glRotated(-idk[0], 0.0, 1.0, 0.0);
            GL11.glRotated(idk[1], 1.0, 0.0, 0.0);

            Color baseColor = getModuleColor();
            float alpha = getAlpha();
            int color = ColorUtil.applyOpacity(baseColor, alpha).getRGB();

            for (int i = 0; i < length; i++) {
                double angle = 0.15f * (System.currentTimeMillis() - lastTime - (i * distance)) / speed;
                double s = Math.sin(angle) * radius;
                double c = Math.cos(angle) * radius;
                GlStateManager.translate(s, c, -c);
                GlStateManager.translate(-size / 2f, -size / 2f, 0);
                GlStateManager.translate(size / 2f, size / 2f, 0);
                RenderUtil.drawImage(glowCircle, 0f, 0f, -size, -size, color);
                GlStateManager.translate(-size / 2f, -size / 2f, 0);
                GlStateManager.translate(size / 2f, size / 2f, 0);
                GlStateManager.translate(-s, -c, c);
            }
            for (int i = 0; i < length; i++) {
                double angle = 0.15f * (System.currentTimeMillis() - lastTime - (i * distance)) / speed;
                double s = Math.sin(angle) * radius;
                double c = Math.cos(angle) * radius;
                GlStateManager.translate(-s, s, -c);
                GlStateManager.translate(-size / 2f, -size / 2f, 0);
                GlStateManager.translate(size / 2f, size / 2f, 0);
                RenderUtil.drawImage(glowCircle, 0f, 0f, -size, -size, color);
                GlStateManager.translate(-size / 2f, -size / 2f, 0);
                GlStateManager.translate(size / 2f, size / 2f, 0);
                GlStateManager.translate(s, -s, c);
            }
            for (int i = 0; i < length; i++) {
                double angle = 0.15f * (System.currentTimeMillis() - lastTime - (i * distance)) / speed;
                double s = Math.sin(angle) * radius;
                double c = Math.cos(angle) * radius;
                GlStateManager.translate(-s, -s, c);
                GlStateManager.translate(-size / 2f, -size / 2f, 0);
                GlStateManager.translate(size / 2f, size / 2f, 0);
                RenderUtil.drawImage(glowCircle, 0f, 0f, -size, -size, color);
                GlStateManager.translate(-size / 2f, -size / 2f, 0);
                GlStateManager.translate(size / 2f, size / 2f, 0);
                GlStateManager.translate(s, s, -c);
            }

            GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
            GlStateManager.disableBlend();
            GlStateManager.enableCull();
            GlStateManager.enableAlpha();
            GlStateManager.depthMask(true);
            GlStateManager.popMatrix();
        }
        if (modeVal == 4) {
            prevCircleStep = circleStep;
            circleStep += circleSpeed.getValue() * RenderUtil.deltaTime() * 0.05;
            float eyeHeight = target.getEyeHeight();
            if (target.isSneaking()) eyeHeight -= 0.2F;

            double cs = prevCircleStep + (circleStep - prevCircleStep) * event.getPartialTicks();
            double prevSinAnim = Math.abs(1.0D + Math.sin(cs - 0.5D)) / 2.0D;
            double sinAnim = Math.abs(1.0D + Math.sin(cs)) / 2.0D;

            double x = target.lastTickPosX + (target.posX - target.lastTickPosX) * event.getPartialTicks() - ((IAccessorRenderManager) mc.getRenderManager()).getRenderPosX();
            double y = target.lastTickPosY + (target.posY - target.lastTickPosY) * event.getPartialTicks() - ((IAccessorRenderManager) mc.getRenderManager()).getRenderPosY() + prevSinAnim * eyeHeight;
            double z = target.lastTickPosZ + (target.posZ - target.lastTickPosZ) * event.getPartialTicks() - ((IAccessorRenderManager) mc.getRenderManager()).getRenderPosZ();
            double nextY = target.lastTickPosY + (target.posY - target.lastTickPosY) * event.getPartialTicks() - ((IAccessorRenderManager) mc.getRenderManager()).getRenderPosY() + sinAnim * eyeHeight;

            GL11.glPushMatrix();
            GL11.glDisable(GL11.GL_CULL_FACE);
            GL11.glDisable(GL11.GL_TEXTURE_2D);
            GL11.glEnable(GL11.GL_BLEND);
            GL11.glDisable(GL11.GL_DEPTH_TEST);
            GL11.glDisable(GL11.GL_ALPHA_TEST);
            GL11.glShadeModel(GL11.GL_SMOOTH);
            GL11.glBegin(GL11.GL_TRIANGLE_STRIP);

            Color col = getModuleColor();
            float alpha = getAlpha();
            for (int i = 0; i <= 360; ++i) {
                double rad = Math.toRadians(i);
                double cos = Math.cos(rad);
                double sin = Math.sin(rad);
                GL11.glColor4f(col.getRed() / 255f, col.getGreen() / 255f, col.getBlue() / 255f, 0.6f * alpha);
                GL11.glVertex3d(x + cos * target.width * 0.8D, nextY, z + sin * target.width * 0.8D);
                GL11.glColor4f(col.getRed() / 255f, col.getGreen() / 255f, col.getBlue() / 255f, 0.01f * alpha);
                GL11.glVertex3d(x + cos * target.width * 0.8D, y, z + sin * target.width * 0.8D);
            }
            GL11.glEnd();
            GL11.glEnable(GL11.GL_LINE_SMOOTH);
            GL11.glBegin(GL11.GL_LINE_LOOP);
            for (int i = 0; i <= 360; ++i) {
                double rad = Math.toRadians(i);
                GL11.glColor4f(col.getRed() / 255f, col.getGreen() / 255f, col.getBlue() / 255f, 0.8f * alpha);
                GL11.glVertex3d(x + Math.cos(rad) * target.width * 0.8D, nextY, z + Math.sin(rad) * target.width * 0.8D);
            }
            GL11.glEnd();
            GL11.glDisable(GL11.GL_LINE_SMOOTH);
            GL11.glEnable(GL11.GL_TEXTURE_2D);
            GL11.glEnable(GL11.GL_ALPHA_TEST);
            GL11.glEnable(GL11.GL_DEPTH_TEST);
            GL11.glShadeModel(GL11.GL_FLAT);
            GL11.glDisable(GL11.GL_BLEND);
            GL11.glEnable(GL11.GL_CULL_FACE);
            GL11.glPopMatrix();
            GlStateManager.resetColor();
        }
    }

    @EventTarget
    public void onRender2D(Render2DEvent event) {
        if (!isEnabled()) return;
        int index = 3;
        if (mode.getValue() == 2 && target != null) {
            float dst = mc.thePlayer.getDistanceToEntity(target);
            float[] pos = targetESPSPos(target, event);
            if (pos != null) {
                float sizeBase = 96.0f;
                float scale = MathHelper.clamp_float(sizeBase / (0.5f + dst * 0.1f), 30.0f, 180.0f);
                drawTargetESP2D(pos[0], pos[1], scale, index);
            }
        }
    }

    @EventTarget
    public void onShader2D(Shader2DEvent event) {
        if (!isEnabled()) return;
        if (event.getShaderType() != Shader2DEvent.ShaderType.GLOW) return;
        int index = 3;
        if (mode.getValue() == 2 && imageMode.getValue() == 0 && target != null) {
            float dst = mc.thePlayer.getDistanceToEntity(target);
            float[] pos = targetESPSPos(target, null);
            if (pos != null) {
                float sizeBase = 128.0f;
                float scale = MathHelper.clamp_float(sizeBase / (0.5f + dst * 0.1f), 40.0f, 240.0f);
                drawTargetESP2D(pos[0], pos[1], scale, index);
            }
        }
    }

    private void points(Render3DEvent event) {
        if (target == null) return;

        double markerX = MathUtil.interpolate(target.lastTickPosX, target.posX, event.getPartialTicks());
        double markerY = MathUtil.interpolate(target.lastTickPosY, target.posY, event.getPartialTicks()) + target.height / 1.6f;
        double markerZ = MathUtil.interpolate(target.lastTickPosZ, target.posZ, event.getPartialTicks());

        float time = (float) ((System.currentTimeMillis() - lastTime) / 1500F) + (float)(Math.sin(((System.currentTimeMillis() - lastTime) / 1500F)) / 10f);
        float alpha = 0.5f;
        float pl = 0;
        boolean fa = false;

        Color baseColor = getModuleColor();
        float moduleAlpha = getAlpha();

        for (int iteration = 0; iteration < 3; iteration++) {
            for (float i = time * 360; i < time * 360 + 90; i += 2) {
                float max = time * 360 + 90;
                float dc = MathUtil.normalize(i, time * 360 - 45, max);
                float rf = 0.6f;
                double radians = Math.toRadians(i);
                double plY = pl + Math.sin(radians * 1.2f) * 0.1f;
                int color = ColorUtil.applyOpacity(baseColor, moduleAlpha).getRGB();

                GlStateManager.pushMatrix();
                RenderUtil.setupOrientationMatrix(markerX, markerY, markerZ);

                float[] idk = new float[]{mc.getRenderManager().playerViewY, mc.getRenderManager().playerViewX};
                GL11.glRotated(-idk[0], 0.0, 1.0, 0.0);
                GL11.glRotated(idk[1], 1.0, 0.0, 0.0);
                GlStateManager.depthMask(false);
                float q = (!fa ? 0.25f : 0.15f) * (Math.max(fa ? 0.25f : 0.15f, fa ? dc : (1f + (0.4f - dc)) / 2f) + 0.45f);
                float size = q * (2f + ((0.5f - alpha) * 2));
                RenderUtil.drawImage(glowCircle, (float)(Math.cos(radians) * rf - size / 2f), (float)(plY - 0.7), size, size, color);
                GL11.glEnable(GL11.GL_DEPTH_TEST);
                GlStateManager.depthMask(true);
                GlStateManager.popMatrix();
            }
            time *= -1.025f;
            fa = !fa;
            pl += 0.45f;
        }
    }

    private void drawTargetESP2D(float x, float y, float scale, int index) {
        long millis = (System.currentTimeMillis() - lastTime) + index * 400L;
        boolean useAnimation = (imageMode.getValue() == 5) ? animation.getValue() : true;
        double angle = useAnimation ? MathHelper.clamp_double((Math.sin(millis / 150.0) + 1.0) / 2.0 * 30.0, 0.0, 30.0) : 15.0;
        double scaled = useAnimation ? MathHelper.clamp_double((Math.sin(millis / 500.0) + 1.0) / 2.0, 0.8, 1.0) : 0.9;
        double rotate = useAnimation ? MathHelper.clamp_double((Math.sin(millis / 1000.0) + 1.0) / 2.0 * 360.0, 0.0, 360.0) : 0.0;
        rotate = (imageMode.getValue() == 1 ? 45 : 0) - (angle - 15.0) + rotate;

        Color baseColor = getModuleColor();
        float hurtAlpha = getHurtAlpha();

        Color hurtColor = new Color(255, 0, 0, 185);
        Color baseWithAlpha = ColorUtil.applyOpacity(baseColor, 1.0f);
        Color hurtWithAlpha = ColorUtil.applyOpacity(hurtColor, hurtAlpha);

        int r = (int)(baseWithAlpha.getRed() * (1 - hurtAlpha) + hurtWithAlpha.getRed() * hurtAlpha);
        int g = (int)(baseWithAlpha.getGreen() * (1 - hurtAlpha) + hurtWithAlpha.getGreen() * hurtAlpha);
        int b = (int)(baseWithAlpha.getBlue() * (1 - hurtAlpha) + hurtWithAlpha.getBlue() * hurtAlpha);
        int a = (int)(baseWithAlpha.getAlpha() * (1 - hurtAlpha) + hurtWithAlpha.getAlpha() * hurtAlpha);

        int color = new Color(r, g, b, a).getRGB();
        float size = scale * (float)scaled;

        float renderX = x - size / 2.0f;
        float renderY = y - size / 2.0f;
        float x2 = renderX + size;
        float y2 = renderY + size;

        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, 0);
        GlStateManager.rotate((float) rotate, 0, 0, 1);
        GlStateManager.translate(-x, -y, 0);
        GL11.glDisable(GL11.GL_ALPHA_TEST);
        GlStateManager.depthMask(false);
        GlStateManager.enableBlend();
        GlStateManager.shadeModel(GL11.GL_SMOOTH);
        GlStateManager.tryBlendFuncSeparate(770, 1, 1, 0);

        float alpha = getAlpha();
        GL11.glColor4f(baseColor.getRed() / 255f, baseColor.getGreen() / 255f, baseColor.getBlue() / 255f, alpha);

        switch (imageMode.getValue()) {
            case 0:
                RenderUtil.drawImage(rectangle, renderX, renderY, x2, y2, color, color, color, color);
                break;
            case 1:
                RenderUtil.drawImage(quadstapple, renderX, renderY, x2, y2, color, color, color, color);
                break;
            case 2:
                RenderUtil.drawImage(trianglestapple, renderX, renderY, x2, y2, color, color, color, color);
                break;
            case 3:
                RenderUtil.drawImage(trianglestipple, renderX, renderY, x2, y2, color, color, color, color);
                break;
            case 4:
                RenderUtil.drawImage(aim, renderX, renderY, x2, y2, color, color, color, color);
                break;
            case 5:
                if (customImage != null) {
                    RenderUtil.drawImage(customImage, renderX, renderY, x2, y2, color, color, color, color);
                } else {
                    RenderUtil.drawImage(rectangle, renderX, renderY, x2, y2, color, color, color, color);
                }
                break;
        }

        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.resetColor();
        GlStateManager.shadeModel(GL11.GL_FLAT);
        GlStateManager.depthMask(true);
        GL11.glEnable(GL11.GL_ALPHA_TEST);
        GlStateManager.popMatrix();
    }

    private float[] targetESPSPos(EntityLivingBase entity, Render2DEvent event) {
        EntityRenderer entityRenderer = mc.entityRenderer;
        float partialTicks = (event != null) ? event.getPartialTicks() : ((IAccessorMinecraft) mc).getTimer().renderPartialTicks;
        double x = MathUtil.interpolate(entity.prevPosX, entity.posX, partialTicks);
        double y = MathUtil.interpolate(entity.prevPosY, entity.posY, partialTicks) + entity.height * 0.4f;
        double z = MathUtil.interpolate(entity.prevPosZ, entity.posZ, partialTicks);
        double width = entity.width / 2.0f;
        double height = entity.height / 4.0f;
        AxisAlignedBB bb = new AxisAlignedBB(x - width, y - height, z - width, x + width, y + height, z + width);
        final double[][] vectors = {
                {bb.minX, bb.minY, bb.minZ}, {bb.minX, bb.maxY, bb.minZ}, {bb.minX, bb.maxY, bb.maxZ}, {bb.minX, bb.minY, bb.maxZ},
                {bb.maxX, bb.minY, bb.minZ}, {bb.maxX, bb.maxY, bb.minZ}, {bb.maxX, bb.maxY, bb.maxZ}, {bb.maxX, bb.minY, bb.maxZ}
        };
        ((IAccessorEntityRenderer) entityRenderer).callSetupCameraTransform(partialTicks, 0);
        final float[] position = new float[]{Float.MAX_VALUE, Float.MAX_VALUE, -1.0F, -1.0F};
        for (final double[] vec : vectors) {
            float[] proj = GLUtil.project2D((float)(vec[0] - ((IAccessorRenderManager) mc.getRenderManager()).getRenderPosX()),
                    (float)(vec[1] - ((IAccessorRenderManager) mc.getRenderManager()).getRenderPosY()),
                    (float)(vec[2] - ((IAccessorRenderManager) mc.getRenderManager()).getRenderPosZ()),
                    new ScaledResolution(mc).getScaleFactor());
            if (proj != null && proj[2] >= 0.0F && proj[2] < 1.0F) {
                position[0] = Math.min(proj[0], position[0]);
                position[1] = Math.min(proj[1], position[1]);
                position[2] = Math.max(proj[0], position[2]);
                position[3] = Math.max(proj[1], position[3]);
            }
        }
        entityRenderer.setupOverlayRendering();
        float centerX = (position[0] + position[2]) / 2.0f;
        float centerY = (position[1] + position[3]) / 2.0f;
        return new float[]{centerX, centerY};
    }
}