package unfair.ui.clickgui.dropdown;

import unfair.Unfair;
import unfair.management.ClientSettings;
import unfair.module.modules.render.HUD;
import unfair.property.properties.ModeProperty;

import java.awt.*;

public class ModeSetting extends ValueItem {
    private final ModeProperty value;

    public ModeSetting(ModeProperty v) { this.value = v; }

    private static HUD getHud() {
        return (HUD) Unfair.moduleManager.modules.get(HUD.class);
    }

    @Override
    public void render(int mouseX, int mouseY) {
        Unfair.fontManager.getFont(15).drawString(value.getName(), x + 2, y + 1, ClientSettings.INSTANCE.getSettingNameColor().getRGB(), false);

        String modeText = value.getModeString();
        float textWidth = Unfair.fontManager.getFont(15).getStringWidth(modeText);
        HUD hud = getHud();
        Color accent = hud.getColor(System.currentTimeMillis());

        float labelX = x + width - textWidth - 2;

        Unfair.fontManager.getFont(15).drawString(modeText, labelX, y + 1, accent.getRGB(), false);
    }

    @Override
    public void mouseClicked(int mx, int my, int button) {
        if (button == 0 && isHovering(mx, my, x, y, width, getHeight())) value.nextMode();
    }

    @Override
    public void mouseReleased(int mx, int my, int button) {}
    @Override
    public float getHeight() { return 14; }
    @Override
    public boolean visible() { return value.isVisible(); }
}