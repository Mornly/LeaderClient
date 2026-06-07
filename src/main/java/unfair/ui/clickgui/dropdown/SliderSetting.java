package unfair.ui.clickgui.dropdown;

import unfair.Unfair;
import unfair.management.ClientSettings;
import unfair.module.modules.render.HUD;
import unfair.property.Property;
import unfair.property.properties.FloatProperty;
import unfair.property.properties.IntProperty;
import unfair.property.properties.PercentProperty;
import unfair.util.shader.RoundedUtils;

import java.awt.*;

public class SliderSetting extends ValueItem {
    private final Property<?> value;
    private boolean dragging = false;
    private static final float SLIDER_HEIGHT = 3;
    private static final float KNOB_SIZE = 8;

    public SliderSetting(Property<?> v) { this.value = v; }

    private double getMin() {
        if (value instanceof FloatProperty) return ((FloatProperty) value).getMinimum();
        if (value instanceof IntProperty) return ((IntProperty) value).getMinimum();
        if (value instanceof PercentProperty) return ((PercentProperty) value).getMinimum();
        return 0;
    }

    private double getMax() {
        if (value instanceof FloatProperty) return ((FloatProperty) value).getMaximum();
        if (value instanceof IntProperty) return ((IntProperty) value).getMaximum();
        if (value instanceof PercentProperty) return ((PercentProperty) value).getMaximum();
        return 100;
    }

    private double getVal() {
        if (value instanceof FloatProperty) return ((FloatProperty) value).getValue();
        if (value instanceof IntProperty) return ((IntProperty) value).getValue().doubleValue();
        if (value instanceof PercentProperty) return ((PercentProperty) value).getValue();
        return 0;
    }

    private void setVal(double val) {
        if (value instanceof FloatProperty) value.setValue((float) val);
        else if (value instanceof IntProperty) value.setValue((int) Math.round(val));
        else if (value instanceof PercentProperty) value.setValue((int) Math.round(val));
    }

    private static HUD getHud() {
        return (HUD) Unfair.moduleManager.modules.get(HUD.class);
    }

    @Override
    public void render(int mouseX, int mouseY) {
        Unfair.fontManager.getFont(15).drawString(value.getName(), x + 2, y + 1, ClientSettings.INSTANCE.getSettingNameColor().getRGB(), false);

        String displayStr;
        if (value instanceof PercentProperty)
            displayStr = (int) Math.round(getVal()) + "%";
        else if (value instanceof FloatProperty)
            displayStr = String.valueOf(Math.round(getVal() * 100.0) / 100.0);
        else
            displayStr = String.valueOf((int) Math.round(getVal()));

        float textWidth = Unfair.fontManager.getFont(15).getStringWidth(displayStr);
        Unfair.fontManager.getFont(15).drawString(displayStr, x + width - textWidth - 2, y + 1, ClientSettings.INSTANCE.getValueTextColor().getRGB(), false);

        float sliderX = x + 2;
        float sliderW = width - 4;
        float sliderY = y + getHeight() - SLIDER_HEIGHT - 6;

        RoundedUtils.drawRound(sliderX, sliderY, sliderW, SLIDER_HEIGHT, SLIDER_HEIGHT / 2f,
                ClientSettings.INSTANCE.getSliderTrackColor());

        double range = getMax() - getMin();
        float fillW = range > 0 ? (float)(sliderW * ((getVal() - getMin()) / range)) : 0;
        if (fillW > 1) {
            long time = System.currentTimeMillis();
            HUD hud = getHud();
            Color c1 = hud.getColor(time);
            Color c2 = hud.getColor(time + 200);
            RoundedUtils.drawGradientHorizontal(sliderX, sliderY, fillW, SLIDER_HEIGHT, SLIDER_HEIGHT / 2f, c1, c2);
        }

        float knobX = sliderX + fillW - KNOB_SIZE / 2f;
        float knobY = sliderY + SLIDER_HEIGHT / 2f - KNOB_SIZE / 2f;
        RoundedUtils.drawRound(knobX, knobY, KNOB_SIZE, KNOB_SIZE, KNOB_SIZE / 2f, new Color(200, 200, 200));
        RoundedUtils.drawRound(knobX + 1.5f, knobY + 1.5f, KNOB_SIZE - 3, KNOB_SIZE - 3, (KNOB_SIZE - 3) / 2f,
                new Color(220, 220, 220));

        if (dragging) {
            double pct = Math.max(0, Math.min(1, (mouseX - sliderX) / sliderW));
            double newVal = getMin() + pct * range;
            if (value instanceof FloatProperty) newVal = Math.round(newVal * 100.0) / 100.0;
            else newVal = Math.round(newVal);
            newVal = Math.max(getMin(), Math.min(getMax(), newVal));
            setVal(newVal);
        }
    }

    @Override
    public void mouseClicked(int mx, int my, int button) {
        float sliderY = y + getHeight() - SLIDER_HEIGHT - 6;
        if (isHovering(mx, my, x + 2, sliderY - 4, width - 4, SLIDER_HEIGHT + 8) && button == 0) {
            dragging = true;
            double range = getMax() - getMin();
            double pct = Math.max(0, Math.min(1, (mx - (x + 2)) / (width - 4)));
            double newVal = getMin() + pct * range;
            if (value instanceof FloatProperty) newVal = Math.round(newVal * 100.0) / 100.0;
            else newVal = Math.round(newVal);
            setVal(Math.max(getMin(), Math.min(getMax(), newVal)));
        }
    }

    @Override
    public void mouseReleased(int mx, int my, int button) { if (button == 0) dragging = false; }
    @Override
    public float getHeight() { return 24; }
    @Override
    public boolean visible() { return value.isVisible(); }
}