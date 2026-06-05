package unfair.ui.clickgui.dropdown;

import unfair.module.modules.render.HUD;
import unfair.Unfair;
import unfair.util.RenderUtil;
import unfair.management.ClientSettings;

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
        boolean hover = isHovering(mouseX, mouseY, x, y, width, 12);
        int bgColor;
        if (hover) {
            HUD hud = getHud();
            Color c = hud.getColor(System.currentTimeMillis());
            bgColor = new Color(c.getRed(), c.getGreen(), c.getBlue(), 50).getRGB();
        } else {
            bgColor = ClientSettings.INSTANCE.getButtonNormalColor().getRGB();
        }
        RenderUtil.drawRect(x, y, x + width, y + 12, bgColor);

        float textWidth = Unfair.fontManager.getFont(15).getStringWidth(label);
        float textX = x + (width - textWidth) / 2;
        int textColor = hover ? getHud().getColor(System.currentTimeMillis()).getRGB() : ClientSettings.INSTANCE.getTextDisabledColor().getRGB();
        Unfair.fontManager.getFont(15).drawString(label, textX, y + 1, textColor, false);
    }

    @Override
    public void mouseClicked(int mx, int my, int button) {
        if (button == 0 && isHovering(mx, my, x, y, width, 12)) {
            if (action != null) action.run();
        }
    }

    @Override
    public void mouseReleased(int mx, int my, int button) {}
    @Override
    public float getHeight() { return 15; }
    @Override
    public boolean visible() { return true; }
}
