package unfair.ui.clickgui.dropdown;

import unfair.Unfair;
import unfair.management.ClientSettings;
import unfair.module.modules.render.HUD;
import unfair.property.properties.BooleanProperty;
import unfair.util.shader.RoundedUtils;

import java.awt.*;

public class BoolSetting extends ValueItem {
    private final BooleanProperty value;
    private static final float TOGGLE_WIDTH = 15;
    private static final float TOGGLE_HEIGHT = 7;

    public BoolSetting(BooleanProperty v) { this.value = v; }

    @Override
    public void render(int mouseX, int mouseY) {
        boolean on = value.getValue();
        Color textColor = on ? ClientSettings.INSTANCE.getTextEnabledColor() : ClientSettings.INSTANCE.getSettingNameColor();

        Unfair.fontManager.getFont(15).drawString(value.getName(), x + 2, y, textColor.getRGB(), false);

        float toggleX = x + width - TOGGLE_WIDTH - 2;
        float toggleY = y + (getHeight() - TOGGLE_HEIGHT) / 2f - 2.5f;//why???

        if (on) {
            long time = System.currentTimeMillis();
            HUD hud = (HUD) Unfair.moduleManager.modules.get(HUD.class);
            Color accent = hud.getColor(time);
            RoundedUtils.drawRound(toggleX, toggleY, TOGGLE_WIDTH, TOGGLE_HEIGHT, TOGGLE_HEIGHT / 2f,
                    new Color(accent.getRed(), accent.getGreen(), accent.getBlue(), 200));
        } else {
            RoundedUtils.drawRound(toggleX, toggleY, TOGGLE_WIDTH, TOGGLE_HEIGHT, TOGGLE_HEIGHT / 2f,
                    ClientSettings.INSTANCE.getToggleOffColor());
        }

        float knobX = on ? toggleX + TOGGLE_WIDTH - TOGGLE_HEIGHT + 1 : toggleX + 1;
        float knobY = toggleY + 1;
        RoundedUtils.drawRound(knobX, knobY, TOGGLE_HEIGHT - 2, TOGGLE_HEIGHT - 2, (TOGGLE_HEIGHT - 2) / 2f, new Color(200, 200, 200));
    }

    @Override
    public void mouseClicked(int mx, int my, int button) {
        if (button == 0 && isHovering(mx, my, x, y, width, getHeight())) {
            value.setValue(!value.getValue());
        }
    }

    @Override
    public void mouseReleased(int mx, int my, int button) {}
    @Override
    public float getHeight() { return 15; }
    @Override
    public boolean visible() { return value.isVisible(); }
}