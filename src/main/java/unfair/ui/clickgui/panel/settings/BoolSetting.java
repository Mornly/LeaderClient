package unfair.ui.clickgui.panel.settings;

import unfair.Unfair;
import unfair.management.ClientSettings;
import unfair.property.properties.BooleanProperty;
import unfair.ui.clickgui.panel.PanelValueItem;
import unfair.util.shader.RoundedUtils;

import java.awt.*;

public class BoolSetting extends PanelValueItem {
    private final BooleanProperty value;
    private float animProgress;

    public BoolSetting(BooleanProperty v) {
        this.value = v;
        this.animProgress = v.getValue() ? 1f : 0f;
    }

    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);
        setTargetVisibility(visible());
        float target = value.getValue() ? 1f : 0f;
        animProgress = lerp(animProgress, target, 0.15f, deltaTime);
    }

    @Override
    public void render(int mouseX, int mouseY) {
        float visAlpha = getVisibilityAlpha();
        if (visAlpha < 0.01f) return;

        Unfair.fontManager.getFont(13).drawString(value.getName(), x, y + 2,
                blendAlpha(ClientSettings.INSTANCE.getSettingNameColor(), alpha * visAlpha).getRGB(), false);

        float toggleW = 24;
        float toggleH = 12;
        float dotSize = toggleH - 4;
        float padding = 2;
        float maxDotX = toggleW - dotSize - padding;

        float toggleX = x + width - toggleW;
        float toggleY = y + 1;

        if (alpha < 0.01f) return;

        if (animProgress < 0.5f) {
            RoundedUtils.drawRound(toggleX, toggleY, toggleW, toggleH, toggleH / 2f,
                    blendAlpha(ClientSettings.INSTANCE.getToggleOffColor(), alpha * visAlpha * (1 - animProgress * 2)));
        }
        if (animProgress > 0.0f) {
            RoundedUtils.drawRound(toggleX, toggleY, toggleW, toggleH, toggleH / 2f,
                    blendAlpha(ClientSettings.INSTANCE.getAccentColor(), alpha * visAlpha * Math.min(animProgress * 2, 1f)));
        }

        float dotX = toggleX + padding + animProgress * (maxDotX - padding);
        RoundedUtils.drawRound(dotX, toggleY + padding, dotSize, dotSize, dotSize / 2f,
                blendAlpha(ClientSettings.INSTANCE.getCardColor(), alpha * visAlpha));
    }

    @Override
    public void mouseClicked(int mx, int my, int button) {
        if (button == 0 && isHovering(mx, my, x, y, width, 14)) {
            value.setValue(!value.getValue());
        }
    }

    @Override
    public void mouseReleased(int mx, int my, int button) {}
    @Override
    public void mouseDragged(int mx, int my, int button) {}
    @Override
    public float getHeight() { return 16; }
    @Override
    public boolean visible() { return value.isVisible(); }

    private static Color blendAlpha(Color c, float alpha) {
        return new Color(c.getRed(), c.getGreen(), c.getBlue(), (int)(alpha * 255));
    }
}
