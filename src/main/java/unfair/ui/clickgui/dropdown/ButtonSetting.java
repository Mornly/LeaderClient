package unfair.ui.clickgui.dropdown;

import unfair.Unfair;
import unfair.management.ClientSettings;
import unfair.module.modules.render.HUD;
import unfair.util.shader.RoundedUtils;

import java.awt.*;

public class ButtonSetting extends ValueItem {
    private final String label;
    private Runnable action;

    public ButtonSetting(String label, Runnable action) {
        this.label = label;
        this.action = action;
    }

    private static HUD getHud() {
        return (HUD) Unfair.moduleManager.modules.get(HUD.class);
    }

    @Override
    public void render(int mouseX, int mouseY) {
        boolean hover = isHovering(mouseX, mouseY, x, y, width, getHeight());
        int bgColor;
        if (hover) {
            HUD hud = getHud();
            Color c = hud.getColor(System.currentTimeMillis());
            bgColor = new Color(c.getRed(), c.getGreen(), c.getBlue(), 60).getRGB();
        } else {
            bgColor = ClientSettings.INSTANCE.getActionBtnNormalColor().getRGB();
        }
        RoundedUtils.drawRound(x + 2, y + 1, width - 4, getHeight() - 2, 4, bgColor);

        float textWidth = Unfair.fontManager.getFont(15).getStringWidth(label);
        float textX = x + (width - textWidth) / 2;
        int textColor = hover ? getHud().getColor(System.currentTimeMillis()).getRGB()
                : ClientSettings.INSTANCE.getValueTextColor().getRGB();
        Unfair.fontManager.getFont(15).drawString(label, textX, y + 2, textColor, false);
    }

    @Override
    public void mouseClicked(int mx, int my, int button) {
        if (button == 0 && isHovering(mx, my, x, y, width, getHeight())) {
            if (action != null) action.run();
        }
    }

    @Override
    public void mouseReleased(int mx, int my, int button) {}
    @Override
    public float getHeight() { return 18; }
    @Override
    public boolean visible() { return true; }
}