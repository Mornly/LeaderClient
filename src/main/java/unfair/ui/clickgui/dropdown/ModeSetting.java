package unfair.ui.clickgui.dropdown;

import unfair.Unfair;
import unfair.management.ClientSettings;
import unfair.module.modules.render.HUD;
import unfair.property.properties.ModeProperty;

public class ModeSetting extends ValueItem {
    private final ModeProperty value;

    public ModeSetting(ModeProperty v) { this.value = v; }

    private static HUD getHud() {
        return (HUD) Unfair.moduleManager.modules.get(HUD.class);
    }

    @Override
    public void render(int mouseX, int mouseY) {
        Unfair.fontManager.getFont(17).drawString(value.getName(), x, y + 1, ClientSettings.INSTANCE.getSettingNameColor().getRGB(), false);

        String modeText = value.getModeString();
        float textWidth = Unfair.fontManager.getFont(17).getStringWidth(modeText);
        HUD hud = getHud();
        int color = hud.getColor(System.currentTimeMillis()).getRGB();
        Unfair.fontManager.getFont(17).drawString(modeText, x + width - textWidth, y + 1, color, false);
    }

    @Override
    public void mouseClicked(int mx, int my, int button) {
        if (button == 0 && isHovering(mx, my, x, y, width, 10)) value.nextMode();
    }

    @Override
    public void mouseReleased(int mx, int my, int button) {}
    @Override
    public float getHeight() { return 12; }
    @Override
    public boolean visible() { return value.isVisible(); }
}
