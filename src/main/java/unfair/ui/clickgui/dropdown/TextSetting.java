package unfair.ui.clickgui.dropdown;

import unfair.Unfair;
import unfair.management.ClientSettings;
import unfair.module.modules.render.HUD;
import unfair.property.properties.TextProperty;
import unfair.util.shader.RoundedUtils;

import java.awt.*;

public class TextSetting extends ValueItem {
    private final TextProperty value;
    private boolean focused = false;

    public TextSetting(TextProperty v) { this.value = v; }

    private static HUD getHud() {
        return (HUD) Unfair.moduleManager.modules.get(HUD.class);
    }

    @Override
    public void render(int mouseX, int mouseY) {
        Unfair.fontManager.getFont(15).drawString(value.getName(), x + 2, y, ClientSettings.INSTANCE.getSettingNameColor().getRGB(), false);

        float inputY = y + 12;
        float inputH = 15;
        int radius = 4;

        int bgColor = focused ? ClientSettings.INSTANCE.getInputFocusedBgColor().getRGB()
                : ClientSettings.INSTANCE.getInputBgColor().getRGB();
        RoundedUtils.drawRound(x + 2, inputY, width - 4, inputH, radius, bgColor);

        if (focused) {
            HUD hud = getHud();
            Color hc = hud.getColor(System.currentTimeMillis());
            Color borderColor = new Color(hc.getRed(), hc.getGreen(), hc.getBlue(), 80);
            RoundedUtils.drawRoundOutline(x + 2, inputY, width - 4, inputH, radius, 1f, new Color(0, 0, 0, 0), borderColor);
        }

        String displayText = value.getValue();
        if (focused && System.currentTimeMillis() % 1000 < 500) displayText += "|";

        float textX = x + 6;
        float textY = inputY + (inputH - 10) / 2f;
        if (displayText.isEmpty() && !focused) {
            Unfair.fontManager.getFont(15).drawString("Enter text...", textX, textY,
                    ClientSettings.INSTANCE.getInputPlaceholderColor().getRGB(), false);
        } else {
            Unfair.fontManager.getFont(15).drawString(displayText, textX, textY,
                    ClientSettings.INSTANCE.getTextNormalColor().getRGB(), false);
        }
    }

    @Override
    public void mouseClicked(int mx, int my, int button) {
        if (button == 0) {
            float inputY = y + 14;
            float inputH = 18;
            focused = isHovering(mx, my, x + 2, inputY, width - 4, inputH);
        }
    }

    @Override
    public void mouseReleased(int mx, int my, int button) {}

    public void charTyped(char chr) {
        if (focused) {
            if (chr == '\u007F') value.setValue("");
            else if (chr >= 32 || chr == ' ') value.setValue(value.getValue() + chr);
        }
    }

    public void keyPressed(int keyCode) {
        if (focused && keyCode == 14) {
            String current = value.getValue();
            if (current.length() > 0) value.setValue(current.substring(0, current.length() - 1));
        }
    }

    @Override
    public float getHeight() { return 28; }
    @Override
    public boolean visible() { return value.isVisible(); }
}