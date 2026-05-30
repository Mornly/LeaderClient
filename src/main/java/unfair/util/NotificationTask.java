package unfair.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;

public class NotificationTask {
    public String message;
    public long start;
    public long duration;
    public float animationX;
    public float targetX;

    public NotificationTask(String message, long duration) {
        this.message = message;
        this.duration = duration;
        this.start = System.currentTimeMillis();
        this.animationX = new ScaledResolution(Minecraft.getMinecraft()).getScaledWidth();
    }

    public boolean isFinished() {
        return System.currentTimeMillis() - start > duration;
    }

    public float getProgress() {
        return (float) (System.currentTimeMillis() - start) / (float) duration;
    }
}
