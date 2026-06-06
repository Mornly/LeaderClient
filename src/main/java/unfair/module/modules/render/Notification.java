package unfair.module.modules.render;

import net.minecraft.client.gui.ScaledResolution;
import unfair.event.EventTarget;
import unfair.events.Render2DEvent;
import unfair.module.Module;
import unfair.property.properties.IntProperty;
import unfair.property.properties.ModeProperty;
import unfair.util.NotificationTask;
import unfair.util.RenderUtil;

import java.awt.*;
import java.util.concurrent.CopyOnWriteArrayList;

import static unfair.config.Config.mc;

public class Notification extends Module {
    public Notification(){super("Notification",true,true);}
    public final ModeProperty mode = new ModeProperty("Mode",0,new String[]{"Normal","Clean"});
    public static final IntProperty showTime = new IntProperty("ShowTime",2000,0,10000);
    private final IntProperty space = new IntProperty("Space",20,0,10);
    public final IntProperty rectLeft = new IntProperty("RectLeft", 5, 0, 20);
    public final IntProperty rectTop = new IntProperty("RectTop", 50, 0, 100);
    public static final CopyOnWriteArrayList<NotificationTask> tasks = new CopyOnWriteArrayList<>();
    public static final CopyOnWriteArrayList<NotificationTask> disTasks = new CopyOnWriteArrayList<>();

    private float lerp(float current, float target,float speed) {
        return current + (target - current) * speed;
    }
    public static void post(String name) {
        tasks.add(new NotificationTask(name, showTime.getValue()));
    }
    public static void postDis(String name) {
        disTasks.add(new NotificationTask(name, showTime.getValue()));
    }
    @EventTarget
    public void onRender2D(Render2DEvent event) {
        if (!this.isEnabled()) return;
        ScaledResolution sr = new ScaledResolution(mc);
        float x = sr.getScaledWidth() - rectLeft.getValue();
        float y = sr.getScaledHeight() - rectTop.getValue();

        renderNotificationList(tasks, new Color(46, 204, 113), x, y);
        float tasksHeight = calculateTotalHeight(tasks);
        renderNotificationList(disTasks, new Color(231, 76, 60), x, y - tasksHeight);
    }

    private float calculateTotalHeight(CopyOnWriteArrayList<NotificationTask> list) {
        return list.size() * (24 + space.getValue());
    }

    private void renderNotificationList(CopyOnWriteArrayList<NotificationTask> list, Color accentColor, float x, float y) {
        if (mode.getValue() == 0) {
            for (NotificationTask task : list) {
                final float HEIGHT = 24;
                final float PADDING = 8;
                if (task.isFinished()) {
                    task.targetX = x + 5;
                    if (Math.abs(task.animationX - task.targetX) < 1) {
                        list.remove(task);
                        continue;
                    }
                } else {
                    float textWidth = RenderUtil.getWidth(task.message);
                    task.targetX = x - (textWidth + 35);
                }

                task.animationX = lerp(task.animationX, task.targetX, 0.15f);

                float renderX = task.animationX;
                float width = x - renderX;
                RenderUtil.drawRect(renderX, y, x, y + HEIGHT, new Color(20, 20, 20, 220).getRGB());
                RenderUtil.drawRect(x - 3, y, x, y + HEIGHT, accentColor.getRGB());
                float progress = task.getProgress();
                float progressWidth = width * (1.0f - progress);
                RenderUtil.drawRect(renderX, y + HEIGHT - 2, renderX + progressWidth, y + HEIGHT, accentColor.getRGB());
                RenderUtil.drawFont(task.message, (int) (renderX + PADDING + 3), (int) (y + 7), -1, true);
                y -= (HEIGHT + space.getValue());
            }
        }
        if (mode.getValue() == 1){
            for (NotificationTask task : list) {
                final float HEIGHT = 24;
                final float PADDING = 8;
                if (task.isFinished()) {
                    task.targetX = x + 5;
                    if (Math.abs(task.animationX - task.targetX) < 1) {
                        list.remove(task);
                        continue;
                    }
                } else {
                    float textWidth = RenderUtil.getWidth(task.message);
                    task.targetX = x - (textWidth + 20);
                }

                task.animationX = lerp(task.animationX, task.targetX, 0.15f);

                float renderX = task.animationX;
                float progress = task.getProgress();
                float width = x - renderX;
                float progressWidth = width * (1.0f - progress);
                RenderUtil.drawRoundedRectangle(renderX - 1, y - 1, x + 1, y + HEIGHT + 1,4.0f,new Color(0,0,0, 40).getRGB());
                RenderUtil.drawRoundedRectangle(renderX, y, x, y + HEIGHT,4.0f, new Color(252, 252, 252, 142).getRGB());
                RenderUtil.drawFont(task.message, (int) (renderX + PADDING), (int) (y + 7), accentColor.getRGB(),false);
                RenderUtil.drawRect(renderX, y + HEIGHT - 2, renderX + progressWidth, y + HEIGHT, accentColor.getRGB());
                y -= (HEIGHT + space.getValue());
            }
        }
    }
}
