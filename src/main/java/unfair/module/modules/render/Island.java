package unfair.module.modules.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.util.EnumChatFormatting;
import org.lwjgl.opengl.GL11;
import unfair.Unfair;
import unfair.event.EventTarget;
import unfair.events.Render2DEvent;
import unfair.module.Module;
import unfair.module.modules.player.Scaffold;
import unfair.property.properties.FloatProperty;
import unfair.property.properties.ModeProperty;
import unfair.util.NotificationTask;
import unfair.util.RenderUtil;
import unfair.util.TimerUtil;
import unfair.util.animations.advanced.ContinualAnimation;

import java.awt.*;
import java.util.concurrent.CopyOnWriteArrayList;

import static unfair.config.Config.mc;

public class Island extends Module {
    public Island(){
        super("Island",false,false);
    }
    public final ModeProperty blockCounterMode = new ModeProperty("BlockCounterStyle",0,new String[]{"Bar","Circle"});
    public final ModeProperty blockCounterPosition = new ModeProperty("BlockCounterPosition",0,new String[]{"Middle","Top"});
    public final FloatProperty backgroundRadius = new FloatProperty("BackgroundRadius", 4f, 0f, 20f);
    public float x, y, width, height;
    private ScaledResolution sr;
    public ContinualAnimation animatedX = new ContinualAnimation();
    public ContinualAnimation animatedY = new ContinualAnimation();
    public ContinualAnimation animatedWidth = new ContinualAnimation();
    public ContinualAnimation animatedHeight = new ContinualAnimation();

    public void runToXy(float realX, float realY) {
        animatedX.animate(getRenderX(realX), 40);
        animatedY.animate(getRenderY(realY), 40);
        animatedWidth.animate(width, 40);
        animatedHeight.animate(height, 40);
    }
    public void drawBackgroundAuto(int identifier) {
        float left = animatedX.getOutput();
        float top = animatedY.getOutput();
        float right = left + animatedWidth.getOutput();
        float bottom = top + animatedHeight.getOutput() + (identifier == 1 ? 10 : 0);

        float scissorH = animatedHeight.getOutput() + (identifier == 1 ? 10 : 0);
        RenderUtil.scissor(left - 1, top - 1, animatedWidth.getOutput() + 2, scissorH + 2);

        HUD hud = (HUD) Unfair.moduleManager.getModule(HUD.class);
        RenderUtil.drawRect(left + 5, top + 1, left + animatedWidth.getOutput() - 5, top + 2,
                hud.getColor(System.currentTimeMillis()).getRGB());
        RenderUtil.drawRoundedRectangle(left, top, right, bottom,
                backgroundRadius.getValue(), new Color(0, 0, 0, 70).getRGB());
    }

    private int fps = 0;
    private TimerUtil timer = new TimerUtil();
    public String title, description;

    @Override
    public void onEnabled() {
        this.sr = new ScaledResolution(mc);
        if (mc.theWorld == null) {
            x = sr.getScaledWidth() / 2f;
            y = 40;
            width = 0;
            height = 0;
            this.title = "";
        }
    }

    @EventTarget
    public void onRender2D(Render2DEvent event) {
        if (this.isEnabled()) {
            render(new ScaledResolution(mc), false);
        }
    }

    public void render(ScaledResolution sr, boolean shader) {
        this.sr = sr;

        if (mc.theWorld == null) {
            x = sr.getScaledWidth() / 2f;
            y = 40;
            width = 0;
            height = 0;
            this.title = "";
        }
        HUD hud = (HUD) Unfair.moduleManager.getModule(HUD.class);

        if (Unfair.moduleManager.modules.get(Scaffold.class).isEnabled()) {
            title = "Block Counter";
            int size = Scaffold.count;
            description = "Stack Size: " + (size > 64 ? EnumChatFormatting.GREEN : size > 32 ? EnumChatFormatting.YELLOW : EnumChatFormatting.RED) + size;

            float textWidth = Math.max(RenderUtil.getWidth(title), RenderUtil.getWidth(description));
            width = textWidth + 10 + 12 + 10;
            height = 30f;
            x = sr.getScaledWidth() / 2f;
            if (blockCounterPosition.getValue() == 0) {
                y = sr.getScaledHeight() / 2f;
            }
            else {
                y = 40;
            }
            runToXy(x, y);
            if (this.blockCounterMode.getValue() == 0) {
                GL11.glEnable(GL11.GL_SCISSOR_TEST);
                float progress = Math.min(64, size) / 64f;
                int barColor = hud.getColor(System.currentTimeMillis()).getRGB();
                drawBackgroundAuto(1);
                float barLeft = animatedX.getOutput() + animatedWidth.getOutput() - 15;
                float barTop = animatedY.getOutput() + 6;
                float barRight = barLeft + 6;
                float barBottom = animatedY.getOutput() + animatedHeight.getOutput() + 10 - 6;

                RenderUtil.drawRect(barLeft + 1, barTop + 1, barRight + 1, barBottom + 1, new Color(0, 0, 0, 100).getRGB());
                float fillHeight = (barBottom - barTop) * progress;
                float fillTop = barBottom - fillHeight;
                RenderUtil.drawRect(barLeft + 2, fillTop + 1, barRight, barBottom + 1, barColor);


                if (!shader) {
                    RenderUtil.drawFont(title, (int) (animatedX.getOutput() + 8), (int) (animatedY.getOutput() + 10), -1, true);
                    RenderUtil.drawFont(description, (int) (animatedX.getOutput() + 8), (int) (animatedY.getOutput() + 22), -1, true);
                }

                GL11.glDisable(GL11.GL_SCISSOR_TEST);
            }
            if (this.blockCounterMode.getValue() == 1) {
                width = textWidth + 10 + 20 + 10;
                height = 30f;
                GL11.glEnable(GL11.GL_SCISSOR_TEST);
                float progress = Math.min(64, size) / 64f;
                Color potCol = hud.getColor(System.currentTimeMillis());
                drawBackgroundAuto(1);
                float circleCenterX = animatedX.getOutput() + animatedWidth.getOutput() - 22; // 微调，使圆居中
                float circleCenterY = animatedY.getOutput() + animatedHeight.getOutput() / 2f;
                RenderUtil.circle(circleCenterX, circleCenterY, 16, 360, false, new Color(0, 0, 0, 70));
                RenderUtil.circle(circleCenterX, circleCenterY, 16, progress * 360, false,
                        new Color(potCol.getRed(), potCol.getGreen(), potCol.getBlue(), 30));
                RenderUtil.circle(circleCenterX, circleCenterY, 15.5, progress * 360, false,
                        new Color(potCol.getRed(), potCol.getGreen(), potCol.getBlue(), 60));
                RenderUtil.circle(circleCenterX, circleCenterY, 15, progress * 360, false,
                        new Color(potCol.getRed(), potCol.getGreen(), potCol.getBlue(), 100));
                RenderUtil.circle(circleCenterX, circleCenterY, 14.5, progress * 360, false,
                        new Color(0, 0, 0, 70));
                RenderUtil.circle(circleCenterX, circleCenterY, 14.5, progress * 360, false, potCol);
                if (!shader) {
                    RenderUtil.drawFont(title, (int)(animatedX.getOutput() + 8), (int)(animatedY.getOutput() + 10), -1, true);
                    RenderUtil.drawFont(description, (int)(animatedX.getOutput() + 8), (int)(animatedY.getOutput() + 22), -1, true);
                }

                GL11.glDisable(GL11.GL_SCISSOR_TEST);
            }
        }else {
            CopyOnWriteArrayList<NotificationTask> notifications = Notification.tasks;
            if (!notifications.isEmpty()) {
                notifications.removeIf(it -> Notification.tasks.isEmpty());

                NotificationTask notification = notifications.get(0);
                if (!Notification.tasks.isEmpty()) {
                    title = "Disabled " + notification.message;
                    description = "Enabled " + notification.message;

                    width = (float) RenderUtil.getWidth(title) + 10;
                    height = 30;
                    x = sr.getScaledWidth() / 2f;
                    y = 40;

                    runToXy(x, y);

                    GL11.glEnable(GL11.GL_SCISSOR_TEST);

                    drawBackgroundAuto(1);
                    RenderUtil.drawRect(animatedX.getOutput() + 6, animatedY.getOutput() + ((y - animatedY.getOutput()) * 2), animatedX.getOutput() + 6 + (width - 12) * Math.min(1, notification.getProgress()), animatedY.getOutput() + ((y - animatedY.getOutput()) * 2) + 2f, hud.getColor(System.currentTimeMillis()).getRGB());

                    if (!shader) {
                        RenderUtil.drawFont(description, (int) (animatedX.getOutput() + 6), (int) (animatedY.getOutput() + 12), -1, true);
                    }

                    GL11.glDisable(GL11.GL_SCISSOR_TEST);
                }
            } else {
                CopyOnWriteArrayList<NotificationTask> notifications1 = Notification.disTasks;
                if (!notifications1.isEmpty()) {
                    notifications1.removeIf(it -> Notification.disTasks.isEmpty());

                    NotificationTask notification = notifications1.get(0);
                    if (!Notification.disTasks.isEmpty()) {
                        title = "Disabled " + notification.message;
                        description = "Disabled " + notification.message;
                        width = (float) RenderUtil.getWidth(title) + 10;
                        height = 30;
                        x = sr.getScaledWidth() / 2f;
                        y = 40;

                        runToXy(x, y);

                        GL11.glEnable(GL11.GL_SCISSOR_TEST);

                        drawBackgroundAuto(1);
                        RenderUtil.drawRect(animatedX.getOutput() + 6, animatedY.getOutput() + ((y - animatedY.getOutput()) * 2) + 1, animatedX.getOutput() + 6 + (width - 12) * Math.min(1, notification.getProgress()), animatedY.getOutput() + ((y - animatedY.getOutput()) * 2) + 3f, hud.getColor(System.currentTimeMillis()).getRGB());

                        if (!shader) {
                            RenderUtil.drawFont(description, (int) (animatedX.getOutput() + 6), (int) (animatedY.getOutput() + 12), -1, true);
                        }

                        GL11.glDisable(GL11.GL_SCISSOR_TEST);
                    }
                } else {
                    if (timer.hasTimeElapsed(120 - (Math.abs(fps - Minecraft.getDebugFPS())) * 2L)) {
                        timer.reset();
                        getSmoothFps();
                    }
                    title = "Leader" + EnumChatFormatting.WHITE + " | " + mc.thePlayer.getName() + " | " + fps + " FPS";
                    width = (float) (RenderUtil.getWidth(title) + 10);
                    height = 15;
                    x = sr.getScaledWidth() / 2f;
                    y = 40;

                    runToXy(x, y);

                    GL11.glPushMatrix();
                    GL11.glEnable(GL11.GL_SCISSOR_TEST);
                    drawBackgroundAuto(0);

                    if (!shader) {
                        RenderUtil.drawFont(title, (int) (animatedX.getOutput() + 5), (int) (animatedY.getOutput() + 5), hud.getColor(System.currentTimeMillis()).getRGB(), true);
                    }
                    GL11.glDisable(GL11.GL_SCISSOR_TEST);
                    GL11.glPopMatrix();
                }
            }
        }
    }

    private void getSmoothFps() {
        int currentFps = Minecraft.getDebugFPS();
        if (fps < currentFps) {
            fps = Math.min(fps + 1, currentFps);
        } else if (fps > currentFps) {
            fps = Math.max(fps - 1, currentFps);
        }
    }

    public float getRenderX(float x) {
        return x - width / 2;
    }

    public float getRenderY(float y) {
        return y - height / 2;
    }

}
