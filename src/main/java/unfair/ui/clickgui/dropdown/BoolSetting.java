package unfair.ui.clickgui.dropdown;

import unfair.property.properties.BooleanProperty;
import unfair.Unfair;
import unfair.util.RenderUtil;

import java.awt.*;

public class BoolSetting extends ValueItem {
    private final BooleanProperty value;

    public BoolSetting(BooleanProperty v) { this.value = v; }

    @Override
    public void render(int mouseX, int mouseY) {
        boolean on = value.getValue();
        Color textColor = on ? Color.WHITE : new Color(160, 160, 160);
        Unfair.fontManager.getFont(17).drawString(value.getName(), x, y - 1, textColor.getRGB(), false);
    }

    @Override
    public void mouseClicked(int mx, int my, int button) {
        if (button == 0 && isHovering(mx, my, x, y, width, 10)) {
            value.setValue(!value.getValue());
        }
    }

    @Override
    public void mouseReleased(int mx, int my, int button) {}
    @Override
    public float getHeight() { return 12; }
    @Override
    public boolean visible() { return value.isVisible(); }
}
