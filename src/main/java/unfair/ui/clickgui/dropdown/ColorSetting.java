package unfair.ui.clickgui.dropdown;

import unfair.Unfair;
import unfair.management.ClientSettings;
import unfair.module.modules.render.HUD;
import unfair.property.properties.ColorProperty;
import unfair.util.RenderUtil;
import unfair.util.shader.RoundedUtils;

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
        Unfair.fontManager.getFont(15).drawString(value.getName(), x + 2, y + 2, ClientSettings.INSTANCE.getSettingNameColor().getRGB(), false);

        int rgb = opaque(value.getValue());
        float previewX = x + width - 18;
        float previewY = y + 2;
        RoundedUtils.drawRound(previewX, previewY, 16, 12, 4, rgb);
        RoundedUtils.drawRoundOutline(previewX, previewY, 16, 12, 4, 0.5f,
                new Color(0, 0, 0, 0), new Color(200, 200, 200, 40));

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

        float pickerX = x + 4;
        float pickerY = y + 18;
        float pickerSize = Math.max(50, Math.min(width - 8, 60));
        float hueBarHeight = 5;
        float hueBarY = pickerY + pickerSize + 4;

        RoundedUtils.drawRound(pickerX, pickerY, pickerSize, pickerSize, 4,
                ClientSettings.INSTANCE.getPickerBgColor());

        float step = Math.max(4, pickerSize / 16f);
        for (int py = 0; py < (int) pickerSize; py += (int) step) {
            for (int px = 0; px < (int) pickerSize; px += (int) step) {
                float sat = px / pickerSize;
                float bright = 1f - (py / pickerSize);
                Color pc = hsbToRgb(new int[]{(int) cachedHue, (int) (sat * 100), (int) (bright * 100)});
                float pw = Math.min(step, pickerSize - px);
                float ph = Math.min(step, pickerSize - py);
                RenderUtil.drawRect(pickerX + px, pickerY + py, pickerX + px + pw, pickerY + py + ph, pc.getRGB());
            }
        }

        float indicatorX = pickerX + (hsb[1] / 100f) * pickerSize;
        float indicatorY = pickerY + (1f - hsb[2] / 100f) * pickerSize;
        RenderUtil.drawRect(indicatorX - 3, indicatorY - 1, indicatorX + 3, indicatorY + 1, new Color(200, 200, 200).getRGB());
        RenderUtil.drawRect(indicatorX - 1, indicatorY - 3, indicatorX + 1, indicatorY + 3, new Color(200, 200, 200).getRGB());

        for (int i = 0; i < (int) pickerSize; i += 4) {
            float hue = i / pickerSize;
            Color hc = hsbToRgb(new int[]{(int) (hue * 360), 100, 100});
            RenderUtil.drawRect(pickerX + i, hueBarY, pickerX + i + 4, hueBarY + hueBarHeight, hc.getRGB());
        }

        float hueIndicatorX = pickerX + (cachedHue / 360f) * pickerSize;
        RenderUtil.drawRect(hueIndicatorX - 1, hueBarY - 1, hueIndicatorX + 2, hueBarY + hueBarHeight + 1, new Color(200, 200, 200).getRGB());

        if (dragging) updateFromPicker(mouseX, mouseY, pickerX, pickerY, pickerSize, hueBarY, hueBarHeight);
    }

    @Override
    public void mouseClicked(int mx, int my, int button) {
        if (button != 0) return;

        float previewX = x + width - 18;
        float previewY = y + 2;
        boolean inPreview = isHovering(mx, my, previewX, previewY, 16, 12);

        if (inPreview) {
            expanded = !expanded;
            if (expanded) initCachedHue();
            return;
        }

        if (!expanded) return;

        float pickerX = x + 4;
        float pickerY = y + 20;
        float pickerSize = Math.max(50, Math.min(width - 8, 60));
        float hueBarHeight = 6;
        float hueBarY = pickerY + pickerSize + 6;

        boolean inPicker = isHovering(mx, my, pickerX, pickerY, pickerSize, pickerSize);
        boolean inHueBar = isHovering(mx, my, pickerX, hueBarY, pickerSize, hueBarHeight);
        if (inPicker || inHueBar) {
            dragging = true;
            updateFromPicker(mx, my, pickerX, pickerY, pickerSize, hueBarY, hueBarHeight);
        }
    }

    @Override
    public void mouseReleased(int mx, int my, int button) {
        if (button == 0) dragging = false;
    }

    private void updateFromPicker(int mouseX, int mouseY, float pickerX, float pickerY,
                                  float pickerSize, float hueBarY, float hueBarHeight) {
        if (isHovering(mouseX, mouseY, pickerX, pickerY, pickerSize, pickerSize)) {
            float sat = Math.max(0, Math.min(1, (mouseX - pickerX) / pickerSize));
            float bright = Math.max(0, Math.min(1, 1 - (mouseY - pickerY) / pickerSize));
            int[] hsb = new int[]{cachedHue, (int) (sat * 100), (int) (bright * 100)};
            value.setValue(hsbToRgb(hsb).getRGB() & 0xFFFFFF);
        }
        if (isHovering(mouseX, mouseY, pickerX, hueBarY, pickerSize, hueBarHeight)) {
            float hue = Math.max(0, Math.min(1, (mouseX - pickerX) / pickerSize));
            cachedHue = (int) (hue * 360);
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
    public float getHeight() {
        float base = 16;
        if (expanded) {
            float pickerSize = Math.max(50, Math.min(width - 8, 60));
            return base + pickerSize + 4 + 4 + 4;
        }
        return base;
    }

    @Override
    public boolean visible() { return value.isVisible(); }

    private int[] rgbToHsb(Color c) {
        float[] hsb = Color.RGBtoHSB(c.getRed(), c.getGreen(), c.getBlue(), null);
        return new int[]{(int) (hsb[0] * 360), (int) (hsb[1] * 100), (int) (hsb[2] * 100)};
    }

    private Color hsbToRgb(int[] hsb) {
        int rgb = Color.HSBtoRGB(hsb[0] / 360f, hsb[1] / 100f, hsb[2] / 100f);
        return new Color(rgb);
    }
}