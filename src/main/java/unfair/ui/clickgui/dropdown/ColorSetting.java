package unfair.ui.clickgui.dropdown;

import unfair.module.modules.render.HUD;
import unfair.property.properties.ColorProperty;
import unfair.Unfair;
import unfair.util.RenderUtil;
import unfair.management.ClientSettings;

import java.awt.*;

public class ColorSetting extends ValueItem {
    private final ColorProperty value;
    private boolean dragging = false;
    private boolean expanded = false;
    private int cachedHue = 0;

    public ColorSetting(ColorProperty v) { this.value = v; }

    private static HUD getHud() {
        return (HUD) Unfair.moduleManager.modules.get(HUD.class);
    }

    private static int opaque(int rgb) {
        return (rgb == 0) ? 0xFF000000 : (rgb | 0xFF000000);
    }

    @Override
    public void render(int mouseX, int mouseY) {
        Unfair.fontManager.getFont(17).drawString(value.getName(), x, y + 3, ClientSettings.INSTANCE.getSettingNameColor().getRGB(), false);

        int rgb = opaque(value.getValue());
        RenderUtil.drawRect(x + width - 15, y + 3, x + width - 3, y + 13, rgb);

        if (expanded) renderColorPicker(mouseX, mouseY);
    }

    private void renderColorPicker(int mouseX, int mouseY) {
        int rawRgb = value.getValue();
        int rgb = opaque(rawRgb);

        int[] hsb;
        if (rawRgb == 0) {
            hsb = new int[]{cachedHue, 0, 0};
        } else {
            hsb = rgbToHsb(new Color(rgb));
            if (hsb[2] > 0) cachedHue = hsb[0];
        }

        float pickerX = x + 8;
        float pickerY = y + 23;
        float pickerSize = Math.max(60, width - 16);
        float hueBarHeight = 6;

        RenderUtil.drawRect(pickerX, pickerY, pickerX + pickerSize, pickerY + pickerSize, ClientSettings.INSTANCE.getPickerBgColor().getRGB());

        float currentHue = cachedHue;
        for (int py = 0; py < (int)pickerSize; py += 3) {
            for (int px = 0; px < (int)pickerSize; px += 3) {
                float sat = px / pickerSize;
                float bright = 1 - (py / pickerSize);
                Color pc = hsbToRgb(new int[]{(int)currentHue, (int)(sat * 100), (int)(bright * 100)});
                RenderUtil.drawRect(pickerX + px, pickerY + py, pickerX + px + 3, pickerY + py + 3, pc.getRGB());
            }
        }

        float indicatorX = pickerX + (hsb[1] / 100f) * pickerSize;
        float indicatorY = pickerY + (1 - hsb[2] / 100f) * pickerSize;
        RenderUtil.drawRect(indicatorX - 2, indicatorY - 4, indicatorX + 2, indicatorY + 4, Color.WHITE.getRGB());
        RenderUtil.drawRect(indicatorX - 1, indicatorY - 3, indicatorX + 1, indicatorY + 3, rgb);

        float hueBarY = pickerY + pickerSize + 9;

        for (int i = 0; i < (int)pickerSize; i += 2) {
            float hue = i / pickerSize;
            Color hc = hsbToRgb(new int[]{(int)(hue * 360), 100, 100});
            RenderUtil.drawRect(pickerX + i, hueBarY, pickerX + i + 2, hueBarY + hueBarHeight, hc.getRGB());
        }

        float hueIndicatorX = pickerX + (cachedHue / 360f) * pickerSize;
        RenderUtil.drawRect(hueIndicatorX - 1, hueBarY - 1, hueIndicatorX + 2, hueBarY + hueBarHeight + 1, Color.WHITE.getRGB());
        RenderUtil.drawRect(hueIndicatorX, hueBarY, hueIndicatorX + 1, hueBarY + hueBarHeight, Color.BLACK.getRGB());

        if (dragging) updateFromPicker(mouseX, mouseY, pickerX, pickerY, pickerSize, hueBarY);
    }

    @Override
    public void mouseClicked(int mx, int my, int button) {
        if (button != 0) return;

        float previewX = x + width - 15;
        float previewY = y + 3;
        boolean inPreview = isHovering(mx, my, previewX, previewY, 12, 10);

        if (inPreview) {
            expanded = !expanded;
            if (expanded) initCachedHue();
            return;
        }

        if (!expanded) return;

        float pickerX = x + 8;
        float pickerY = y + 23;
        float pickerSize = width - 16;
        float hueBarY = pickerY + pickerSize + 9;
        float hueBarHeight = 6;

        boolean inPicker = isHovering(mx, my, pickerX, pickerY, pickerSize, pickerSize);
        boolean inHueBar = isHovering(mx, my, pickerX, hueBarY, pickerSize, hueBarHeight);
        dragging = inPicker || inHueBar;
    }

    @Override
    public void mouseReleased(int mx, int my, int button) { if (button == 0) dragging = false; }

    private void updateFromPicker(int mouseX, int mouseY, float pickerX, float pickerY, float pickerSize, float hueBarY) {
        if (isHovering(mouseX, mouseY, pickerX, pickerY, pickerSize, pickerSize)) {
            float sat = Math.max(0, Math.min(1, (mouseX - pickerX) / pickerSize));
            float bright = Math.max(0, Math.min(1, 1 - (mouseY - pickerY) / pickerSize));
            int[] hsb = new int[]{cachedHue, (int)(sat * 100), (int)(bright * 100)};
            value.setValue(hsbToRgb(hsb).getRGB() & 0xFFFFFF);
        }
        float hueBarHeight = 6;
        if (isHovering(mouseX, mouseY, pickerX, hueBarY, pickerSize, hueBarHeight)) {
            float hue = Math.max(0, Math.min(1, (mouseX - pickerX) / pickerSize));
            cachedHue = (int)(hue * 360);
            int[] hsb = rgbToHsb(new Color(opaque(value.getValue())));
            hsb[0] = cachedHue;
            value.setValue(hsbToRgb(hsb).getRGB() & 0xFFFFFF);
        }
    }

    private void initCachedHue() {
        int rawRgb = value.getValue();
        if (rawRgb != 0) {
            int[] hsb = rgbToHsb(new Color(opaque(rawRgb)));
            if (hsb[2] > 0) cachedHue = hsb[0];
        }
    }

    @Override
    public float getHeight() { return expanded ? 23 + (width - 16) + 9 + 6 : 21; }
    @Override
    public boolean visible() { return value.isVisible(); }

    private int[] rgbToHsb(Color c) {
        float[] hsb = Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), null);
        return new int[]{(int)(hsb[0] * 360), (int)(hsb[1] * 100), (int)(hsb[2] * 100)};
    }

    private Color hsbToRgb(int[] hsb) {
        int rgb = Color.HSBtoRGB(hsb[0] / 360f, hsb[1] / 100f, hsb[2] / 100f);
        return new Color(rgb);
    }
}
