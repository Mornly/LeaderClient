package unfair.ui.clickgui.dropdown;

import net.minecraft.client.settings.KeyBinding;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import unfair.Unfair;
import unfair.module.Module;
import unfair.module.modules.render.HUD;
import unfair.util.KeyBindUtil;
import unfair.management.ClientSettings;

import java.awt.*;

public class ClientSetting extends ValueItem {
    private final Module module;
    private boolean listening = false;
    public static boolean anyListening = false;

    public ClientSetting(Module module) { this.module = module; }

    private static HUD getHud() { return (HUD) Unfair.moduleManager.modules.get(HUD.class); }

    @Override
    public void render(int mouseX, int mouseY) {
        int fontSize = 17;
        String hiddenLabel = module.isHidden() ? "Hidden" : "hide";
        int hiddenColor = module.isHidden() ? ClientSettings.INSTANCE.getTextEnabledColor().getRGB() : ClientSettings.INSTANCE.getTextDisabledColor().getRGB();
        Unfair.fontManager.getFont(fontSize).drawString(hiddenLabel, x, y - 1, hiddenColor, false);

        String keyName = listening ? "..." : KeyBindUtil.getKeyName(module.getKey());
        String display = "[" + keyName + "]";
        float textWidth = Unfair.fontManager.getFont(fontSize).getStringWidth(display);
        float textX = x + (width - textWidth);
        int color = listening ? getHud().getColor(System.currentTimeMillis()).getRGB() : ClientSettings.INSTANCE.getTextDisabledColor().getRGB();
        Unfair.fontManager.getFont(fontSize).drawString(display, textX, y - 1, color, false);
    }

    @Override
    public void mouseClicked(int mx, int my, int button) {
        int fontSize = 17;
        float hideW = Unfair.fontManager.getFont(fontSize).getStringWidth("Hidden") + 4;
        if (button == 0 && isHovering(mx, my, x, y, hideW, 12)) {
            module.setHidden(!module.isHidden());
            return;
        }
        String keyName = listening ? "..." : KeyBindUtil.getKeyName(module.getKey());
        String display = "[" + keyName + "]";
        float bindW = Unfair.fontManager.getFont(fontSize).getStringWidth(display);
        float bindX = x + (width - bindW);
        if (button == 0 && isHovering(mx, my, bindX, y, bindW, 12)) {
            listening = !listening;
            anyListening = listening;
            if (listening) {
                KeyBinding.unPressAllKeys();
            }
        } else if (listening) {
            listening = false;
            anyListening = false;
        }
    }

    @Override
    public void mouseReleased(int mx, int my, int button) {}

    @Override
    public float getHeight() { return 12; }

    @Override
    public boolean visible() { return true; }

    public void keyPressed(int keyCode) {
        if (!listening) return;
        if (keyCode == Keyboard.KEY_ESCAPE || keyCode == Keyboard.KEY_DELETE) {
            module.setKey(0);
        } else {
            module.setKey(keyCode);
        }
        listening = false;
        anyListening = false;
    }
}
