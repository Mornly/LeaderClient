package unfair.ui.clickgui.panel.settings;

import unfair.Unfair;
import unfair.management.ClientSettings;
import unfair.property.properties.ColorProperty;
import unfair.ui.clickgui.panel.PanelValueItem;
import unfair.util.shader.RoundedUtils;

import java.awt.*;

public class ColorSetting extends PanelValueItem {
    private final ColorProperty value;
    private boolean expanded = false;
    private float expandAnim = 0f;
    private float contentAnim = 0f;

    private float hue;
    private float saturation;
    private float brightness;

    private boolean draggingHue = false;
    private boolean draggingSV = false;

    public int col = 0;

    public ColorSetting(ColorProperty v) {
        this.value = v;
        int color = v.getValue();
        float[] hsb = Color.RGBtoHSB((color >> 16) & 0xFF, (color >> 8) & 0xFF, color & 0xFF, null);
        this.hue = hsb[0];
        this.saturation = hsb[1];
        this.brightness = hsb[2];
    }

    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);
        setTargetVisibility(visible());
        if (expanded) {
            expandAnim = lerp(expandAnim, 1f, 0.18f, deltaTime);
            if (expandAnim > 0.95f) {
                contentAnim = lerp(contentAnim, 1f, 0.15f, deltaTime);
            }
        } else {
            contentAnim = lerp(contentAnim, 0f, 0.15f, deltaTime);
            if (contentAnim < 0.05f) {
                expandAnim = lerp(expandAnim, 0f, 0.18f, deltaTime);
            }
        }
    }

    @Override
    public void render(int mouseX, int mouseY) {
        float visAlpha = getVisibilityAlpha();
        if (visAlpha < 0.01f) return;

        Unfair.fontManager.getFont(13).drawString(value.getName(), x, y + 2,
                blendAlpha(ClientSettings.INSTANCE.getSettingNameColor(), alpha * visAlpha).getRGB(), false);

        int currentColor = value.getValue() & 0xFFFFFF;
        float previewSize = 14;
        float previewX = x + width - previewSize;
        float previewY = y + 1;

        RoundedUtils.drawRound(previewX, previewY, previewSize, previewSize, 3,
                blendAlpha(new Color(currentColor), alpha * visAlpha));

        if (expandAnim > 0.001f) {
            float pickerWidth = width - 4;
            float pickerX = x + 2;
            float svSize = pickerWidth - 16;
            float svH = svSize * 0.7f;
            float hueBarH = 10;
            float pickerContentH = 6 + svH + 8 + hueBarH + 6;
            float pickerHeight = pickerContentH * expandAnim;
            float pickerY = y + 20;

            if (alpha * visAlpha > 0.01f) {
                RoundedUtils.drawRound(pickerX, pickerY, pickerWidth, pickerHeight, 5,
                        new Color(ClientSettings.INSTANCE.getPickerBgColor().getRed(),
                                ClientSettings.INSTANCE.getPickerBgColor().getGreen(),
                                ClientSettings.INSTANCE.getPickerBgColor().getBlue(),
                                (int)(alpha * visAlpha * 255)));

                float contentAlpha = alpha * contentAnim * visAlpha;
                if (contentAlpha > 0.01f) {
                    float svX = pickerX + 8;
                    float svY = pickerY + 6;

                    Color baseColor = Color.getHSBColor(hue, 1f, 1f);
                    RoundedUtils.drawRound(svX, svY, svSize, svH, 2, new Color(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), (int)(contentAlpha * 255)));

                    Color white = new Color(255, 255, 255, (int)(contentAlpha * 255));
                    Color transparentWhite = new Color(255, 255, 255, 0);
                    RoundedUtils.drawGradientHorizontal(svX, svY, svSize, svH, 2, white, transparentWhite);

                    Color black = new Color(0, 0, 0, (int)(contentAlpha * 255));
                    Color transparentBlack = new Color(0, 0, 0, 0);
                    RoundedUtils.drawGradientVertical(svX, svY, svSize, svH, 2, transparentBlack, black);

                    float circleX = svX + saturation * svSize - 4;
                    float circleY = svY + (1f - brightness) * svH - 4;
                    float circleSize = 8;
                    int whiteOutline = blendAlpha(new Color(255, 255, 255), contentAlpha).getRGB();
                    int fillColor = blendAlpha(ClientSettings.INSTANCE.getCircleFillColor(), contentAlpha * 0.667f).getRGB();
                    RoundedUtils.drawRound(circleX, circleY, circleSize, circleSize, circleSize / 2f, whiteOutline);
                    RoundedUtils.drawRound(circleX + 1, circleY + 1, circleSize - 2, circleSize - 2, (circleSize - 2) / 2f, fillColor);

                    float hueBarY = svY + svH + 8;
                    for (int i = 0; i < (int)svSize; i++) {
                        float h = (float) i / svSize;
                        Color c = Color.getHSBColor(h, 1f, 1f);
                        RoundedUtils.drawRound(svX + i, hueBarY, 1, hueBarH, 0,
                                new Color(c.getRed(), c.getGreen(), c.getBlue(), (int)(contentAlpha * 255)).getRGB());
                    }

                    float hueX = svX + hue * svSize - 2;
                    RoundedUtils.drawRound(hueX, hueBarY - 1, 4, hueBarH + 2, 2, whiteOutline);
                    RoundedUtils.drawRound(hueX + 1, hueBarY, 2, hueBarH, 1, fillColor);
                }
            }
        }
    }

    @Override
    public void mouseClicked(int mx, int my, int button) {
        if (button != 0) return;

        float previewSize = 14;
        float previewX = x + width - previewSize;
        float previewY = y + 1;

        if (mx >= previewX && mx <= previewX + previewSize && my >= previewY && my <= previewY + previewSize) {
            expanded = !expanded;
            return;
        }

        if (!expanded) return;

        float pickerWidth = width - 4;
        float svSize = pickerWidth - 16;
        float svX = x + 2 + 8;
        float svY = y + 20 + 6;
        float svH = svSize * 0.7f;

        if (mx >= svX && mx <= svX + svSize && my >= svY && my <= svY + svH) {
            draggingSV = true;
            updateSV(mx, my);
            return;
        }

        float hueBarY = svY + svH + 8;
        float hueBarH = 10;
        if (mx >= svX && mx <= svX + svSize && my >= hueBarY && my <= hueBarY + hueBarH) {
            draggingHue = true;
            updateHue(mx);
        }
    }

    @Override
    public void mouseReleased(int mx, int my, int button) {
        draggingHue = false;
        draggingSV = false;
    }

    @Override
    public void mouseDragged(int mx, int my, int button) {
        if (button != 0) return;

        if (draggingSV) {
            updateSV(mx, my);
        }
        if (draggingHue) {
            updateHue(mx);
        }
    }

    private void updateSV(int mx, int my) {
        float pickerWidth = width - 4;
        float svSize = pickerWidth - 16;
        float svX = x + 2 + 8;
        float svY = y + 20 + 6;
        float svH = svSize * 0.7f;

        saturation = Math.max(0, Math.min(1, (mx - svX) / svSize));
        brightness = Math.max(0, Math.min(1, 1f - (my - svY) / svH));

        applyColor();
    }

    private void updateHue(int mx) {
        float pickerWidth = width - 4;
        float svSize = pickerWidth - 16;
        float svX = x + 2 + 8;

        hue = Math.max(0, Math.min(1, (mx - svX) / svSize));
        applyColor();
    }

    private void applyColor() {
        Color rgb = Color.getHSBColor(hue, saturation, brightness);
        int color = (rgb.getRed() << 16) | (rgb.getGreen() << 8) | rgb.getBlue();
        value.setValue(color);
    }

    public void closePicker() {
        expanded = false;
    }

    @Override
    public float getHeight() {
        if (!expanded && expandAnim < 0.01f) return 20;
        float pickerWidth = width - 4;
        float svSize = pickerWidth - 16;
        float svH = svSize * 0.7f;
        float pickerContentH = 6 + svH + 8 + 10 + 6;
        return 20 + pickerContentH * expandAnim;
    }

    @Override
    public boolean visible() { return value.isVisible(); }

    private static Color blendAlpha(Color c, float alpha) {
        return new Color(c.getRed(), c.getGreen(), c.getBlue(), (int)(alpha * 255));
    }
}
