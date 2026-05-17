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
import unfair.util.NotificationTask;
import unfair.util.RenderUtil;
import unfair.util.TimerUtils;
import unfair.util.animations.advanced.ContinualAnimation;

import java.awt.*;
import java.util.concurrent.CopyOnWriteArrayList;

import static unfair.config.Config.mc;

public class Island extends Module {
    public Island(){
        super("Island",false,false);
    }
    public final FloatProperty backgroundRadius = new FloatProperty("BackgroundRadius", 4f, 0f, 20f);
    public float x, y, width, height;
    private ScaledResolution sr;
    public ContinualAnimation animatedX = new ContinualAnimation();
    public ContinualAnimation animatedY = new ContinualAnimation();
    // 类成员声明
    public ContinualAnimation animatedWidth = new ContinualAnimation();
    public ContinualAnimation animatedHeight = new ContinualAnimation();

    // 修改 runToXy，同时平滑宽高
    public void runToXy(float realX, float realY) {
        animatedX.animate(getRenderX(realX), 40);
        animatedY.animate(getRenderY(realY), 40);
        animatedWidth.animate(width, 40);
        animatedHeight.animate(height, 40);
    }
    // 重写 drawBackgroundAuto，使用动画尺寸
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
    private TimerUtils timer = new TimerUtils();
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

            // 宽度计算：文字区域 + 右侧进度条区域（12px）
            float textWidth = Math.max(RenderUtil.getWidth(title), RenderUtil.getWidth(description));
            width = textWidth + 10 + 12 + 10; // 5padding + 12进度条 + 5padding
            height = 30f;

            // 定位到屏幕中央
            x = sr.getScaledWidth() / 2f;
            y = sr.getScaledHeight() / 2f;

            runToXy(x, y);

            GL11.glEnable(GL11.GL_SCISSOR_TEST);
            float progress = Math.min(64, size) / 64f; // 0~1，剩余越多 progress 越大
            float remaining = 1f - progress;
            int barColor = hud.getColor(System.currentTimeMillis()).getRGB();
            drawBackgroundAuto(1);

            // ========== 绘制竖条进度条 ==========
            float barLeft = animatedX.getOutput() + animatedWidth.getOutput() - 15; // 右侧留5px边距
            float barTop = animatedY.getOutput() + 6;                                // 上边距
            float barRight = barLeft + 6;                                            // 宽6px
            float barBottom = animatedY.getOutput() + animatedHeight.getOutput() + (1 == 1 ? 10 : 0) - 6; // 下边距

            // 1. 背景槽（深色半透明）
            RenderUtil.drawRect(barLeft, barTop, barRight, barBottom, new Color(0, 0, 0, 100).getRGB());

            // 2. 填充部分（从底部向上）
            float fillHeight = (barBottom - barTop) * progress; // 剩余越多，填充越高（因为 progress = 剩余/64）
            float fillTop = barBottom - fillHeight;
            RenderUtil.drawRect(barLeft + 1, fillTop, barRight - 1, barBottom - 1, barColor);
            // ===================================

            if (!shader) {
                RenderUtil.drawFont(title, (int) (animatedX.getOutput() + 5), (int) (animatedY.getOutput() + 8), -1, true);
                RenderUtil.drawFont(description, (int) (animatedX.getOutput() + 5), (int) (animatedY.getOutput() + 20), -1, true);
            }

            GL11.glDisable(GL11.GL_SCISSOR_TEST);
        }else {
            // ... 其余通知和默认显示保持不变 ...
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

    // ---------- 新增圆形进度绘制方法 ----------

    // ---------- 原有方法保持不变 ----------
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
