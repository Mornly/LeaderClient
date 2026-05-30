package unfair.ui.clickgui.dropdown;

import unfair.module.modules.render.HUD;
import unfair.property.Property;
import unfair.property.properties.*;
import unfair.Unfair;
import unfair.util.RenderUtil;

import java.awt.*;

public class SliderSetting extends ValueItem {
    private final Property<?> value;
    private boolean dragging = false;

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
        Unfair.fontManager.getFont(17).drawString(value.getName(), x, y + 1, new Color(160, 160, 160).getRGB(), false);

        String displayStr;
        if (value instanceof FloatProperty || value instanceof PercentProperty)
            displayStr = String.valueOf(Math.round(getVal() * 100.0) / 100.0);
        else
            displayStr = String.valueOf((int) Math.round(getVal()));

        float textWidth = Unfair.fontManager.getFont(17).getStringWidth(displayStr);
        Unfair.fontManager.getFont(17).drawString(displayStr, x + width - textWidth, y + 1, new Color(140, 140, 140).getRGB(), false);

        float sliderX = x;
        float sliderW = width;
        float sliderY = y + 18;
        float sliderH = 3;

        RenderUtil.drawRect(sliderX, sliderY, sliderX + sliderW, sliderY + sliderH, new Color(40, 40, 45).getRGB());

        double range = getMax() - getMin();
        float fillW = range > 0 ? (float)(sliderW * ((getVal() - getMin()) / range)) : 0;
        if (fillW > 0) {
            long time = System.currentTimeMillis();
            HUD hud = getHud();
            int c1 = hud.getColor(time).getRGB();
            int c2 = hud.getColor(time + 200).getRGB();
            RenderUtil.drawGradientRect((int)sliderX, (int)sliderY, sliderX + fillW, (int)(sliderY + sliderH), c1, c2);
        }

        if (dragging) {
            double pct = Math.max(0, Math.min(1, (mouseX - x) / width));
            double newVal = getMin() + pct * range;
            if (value instanceof FloatProperty) newVal = Math.round(newVal * 100.0) / 100.0;
            else newVal = Math.round(newVal);
            if (newVal < getMin()) newVal = getMin();
            if (newVal > getMax()) newVal = getMax();
            setVal(newVal);
        }
    }

    @Override
    public void mouseClicked(int mx, int my, int button) {
        float sliderY = y + 18;
        if (isHovering(mx, my, x, sliderY - 3, width, 9) && button == 0) dragging = true;
    }

    @Override
    public void mouseReleased(int mx, int my, int button) { if (button == 0) dragging = false; }
    @Override
    public float getHeight() { return 26; }
    @Override
    public boolean visible() { return value.isVisible(); }
}
