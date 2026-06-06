package unfair.ui.clickgui.panel.settings;

import unfair.property.Property;
import unfair.property.properties.*;
import unfair.Unfair;
import unfair.ui.clickgui.panel.PanelValueItem;
import unfair.management.ClientSettings;
import unfair.util.shader.RoundedUtils;

import java.awt.*;

public class SliderSetting extends PanelValueItem {
    private final Property<?> value;
    private boolean dragging = false;
    private float displayValue = 0;

    public SliderSetting(Property<?> v) {
        this.value = v;
        this.displayValue = (float) getVal();
    }

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

    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);
        setTargetVisibility(visible());
        displayValue = lerp(displayValue, (float) getVal(), 0.12f, deltaTime);
    }

    @Override
    public void render(int mouseX, int mouseY) {
        float visAlpha = getVisibilityAlpha();
        if (visAlpha < 0.01f) return;

        Unfair.fontManager.getFont(13).drawString(value.getName(), x, y + 1,
                blendAlpha(ClientSettings.INSTANCE.getSettingNameColor(), alpha * visAlpha).getRGB(), false);

        String displayStr;
        if (value instanceof FloatProperty || value instanceof PercentProperty)
            displayStr = String.valueOf(Math.round(displayValue * 100.0) / 100.0);
        else
            displayStr = String.valueOf(Math.round(displayValue));

        float textWidth = Unfair.fontManager.getFont(13).getStringWidth(displayStr);
        Unfair.fontManager.getFont(13).drawString(displayStr, x + width - textWidth, y + 1,
                blendAlpha(ClientSettings.INSTANCE.getValueTextColor(), alpha * visAlpha).getRGB(), false);

        float sliderX = x;
        float sliderW = width;
        float sliderY = y + 16;
        float sliderH = 3;

        RoundedUtils.drawRound(sliderX, sliderY, sliderW, sliderH, 2, blendAlpha(ClientSettings.INSTANCE.getSliderTrackColor(), alpha * visAlpha));

        double range = getMax() - getMin();
        float fillW = range > 0 ? (float)(sliderW * ((displayValue - getMin()) / range)) : 0;
        if (fillW > 0) {
            RoundedUtils.drawRound(sliderX, sliderY - 1, fillW, sliderH + 2, 2, blendAlpha(ClientSettings.INSTANCE.getAccentColor(), alpha * visAlpha));
        }

        RoundedUtils.drawRound(sliderX + fillW - 3, sliderY - 2, 6, sliderH + 4, 3, blendAlpha(ClientSettings.INSTANCE.getAccentColor(), alpha * visAlpha));

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
        float sliderY = y + 16;
        if (isHovering(mx, my, x, sliderY - 4, width, 12) && button == 0) dragging = true;
    }

    @Override
    public void mouseReleased(int mx, int my, int button) { if (button == 0) dragging = false; }
    @Override
    public void mouseDragged(int mx, int my, int button) {}
    @Override
    public float getHeight() { return 24; }
    @Override
    public boolean visible() { return value.isVisible(); }

    private static Color blendAlpha(Color c, float alpha) {
        return new Color(c.getRed(), c.getGreen(), c.getBlue(), (int)(alpha * 255));
    }
}
