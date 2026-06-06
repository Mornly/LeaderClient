package unfair.ui.clickgui.dropdown;

import unfair.Unfair;
import unfair.management.ClientSettings;
import unfair.module.modules.render.HUD;
import unfair.property.properties.TextProperty;
import unfair.util.RenderUtil;

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
        Unfair.fontManager.getFont(17).drawString(value.getName(), x, y - 3, ClientSettings.INSTANCE.getSettingNameColor().getRGB(), false);

        float inputY = y + 12;
        float inputH = 14;

        int bgColor = focused ? ClientSettings.INSTANCE.getInputFocusedBgColor().getRGB() : ClientSettings.INSTANCE.getInputNormalBgColor().getRGB();
        RenderUtil.drawRect(x, inputY, x + width, inputY + inputH, bgColor);

        if (focused) {
            HUD hud = getHud();
            int hc = hud.getColor(System.currentTimeMillis()).getRGB();
            int alpha = (hc >> 24 & 255);
            int blended = (alpha < 40 ? (hc & 0x00FFFFFF) | 0x28000000 : (hc & 0x00FFFFFF) | (40 << 24));
            RenderUtil.drawRect(x, inputY, x + width, inputY + inputH, blended);
        }

        String displayText = value.getValue();
        if (focused && System.currentTimeMillis() % 1000 < 500) displayText += "|";

        Unfair.fontManager.getFont(15).drawString(displayText, x + 3, inputY, Color.WHITE.getRGB(), false);
    }

    @Override
    public void mouseClicked(int mx, int my, int button) {
        if (button == 0) focused = isHovering(mx, my, x, y, width, 16);
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
    public float getHeight() { return 26; }
    @Override
    public boolean visible() { return value.isVisible(); }
}
