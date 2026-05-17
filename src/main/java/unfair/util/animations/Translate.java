package unfair.util.animations;


import net.minecraft.client.Minecraft;

import java.math.BigDecimal;
import java.math.MathContext;


public final class Translate {
    private double x, y;
    private long lastMS = System.currentTimeMillis();

    public void setX(float x) {
        this.x = x;
    }

    public void setY(float y) {
        this.y = y;
    }

    public Translate(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public void translate(float targetX, float targetY) {
        this.x = (float)Translate.anim(this.x, targetX, 1.0);
        this.y = (float)Translate.anim(this.y, targetY, 1.0);
    }

    public void animate(double newX, double newY) {
        this.x = transition(this.x, newX, 1.0);
        this.y = transition(this.y, newY, 1.0);
    }

    public void interpolate(double targetX, double targetY, double smoothing) {
        this.x = AnimationUtil.animate(targetX, this.x, smoothing);
        this.y = AnimationUtil.animate(targetY, this.y, smoothing);
    }

    public void interpolate(float targetX, float targetY, float smoothing) {
        long currentMS = System.currentTimeMillis();
        long delta = currentMS - this.lastMS;
        this.lastMS = currentMS;
        int deltaX = (int)(Math.abs((double)targetX - this.x) * (double)smoothing);
        int deltaY = (int)(Math.abs((double)targetY - this.y) * (double)smoothing);
        this.x = AnimationUtil.calculateCompensation(targetX, (float)this.x, delta, deltaX);
        this.y = AnimationUtil.calculateCompensation(targetY, (float)this.y, delta, deltaY);
    }

    public static double transition(double now, double desired, double speed) {
        double dif = Math.abs(now - desired);
        int fps = Minecraft.getDebugFPS();
        if (dif > 0.0) {
            double animationSpeed = roundToDecimalPlace(Math.min(10.0, Math.max(0.0625, 144.0 / (double)fps * (dif / 10.0) * speed)), 0.0625);
            if (dif < animationSpeed) {
                animationSpeed = dif;
            }
            if (now < desired) {
                return now + animationSpeed;
            }
            if (now > desired) {
                return now - animationSpeed;
            }
        }
        return now;
    }
    public static double roundToDecimalPlace(double value, double inc) {
        double halfOfInc = inc / 2.0;
        double floored = StrictMath.floor(value / inc) * inc;
        if (value >= floored + halfOfInc) {
            return new BigDecimal(StrictMath.ceil(value / inc) * inc, MathContext.DECIMAL64).stripTrailingZeros().doubleValue();
        }
        return new BigDecimal(floored, MathContext.DECIMAL64).stripTrailingZeros().doubleValue();
    }

    public static double anim(double now, double desired, double speed) {
        double dif = Math.abs(now - desired);
        int fps = Minecraft.getDebugFPS();
        if (dif > 0.0) {
            double animationSpeed = roundToDecimalPlace(Math.min(10.0, Math.max(0.0625, 144.0 / (double)fps * dif / 10.0 * speed)), 0.0625);
            if (dif < animationSpeed) {
                animationSpeed = dif;
            }
            if (now < desired) {
                return now + animationSpeed;
            }
            if (now > desired) {
                return now - animationSpeed;
            }
        }
        return now;
    }
}
