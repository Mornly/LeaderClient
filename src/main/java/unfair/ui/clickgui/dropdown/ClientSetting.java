package unfair.ui.clickgui.dropdown;

import net.minecraft.client.settings.KeyBinding;
import org.lwjgl.input.Keyboard;
import unfair.Unfair;
import unfair.management.ClientSettings;
import unfair.module.Module;
import unfair.module.modules.render.HUD;
import unfair.util.KeyBindUtil;
import unfair.util.shader.RoundedUtils;

import java.awt.*;

public class ClientSetting extends ValueItem {
    private final Module module;
    private boolean listening = false;
    public static boolean anyListening = false;

    public ClientSetting(Module module) { this.module = module; }

    private static HUD getHud() { return (HUD) Unfair.moduleManager.modules.get(HUD.class); }

    @Override
    public void render(int mouseX, int mouseY) {
        int fontSize = 15;

        String hiddenLabel = module.isHidden() ? "Hidden" : "Hide";
        int hiddenColor = module.isHidden() ? ClientSettings.INSTANCE.getTextEnabledColor().getRGB()
                : ClientSettings.INSTANCE.getTextDisabledColor().getRGB();

        float hideW = Unfair.fontManager.getFont(fontSize).getStringWidth(hiddenLabel);
        boolean hideHover = isHovering(mouseX, mouseY, x + 2, y, hideW + 4, getHeight());
        if (hideHover) {
            RoundedUtils.drawRound(x + 2, y + 1, hideW + 4, getHeight() - 2, 4,
                    new Color(ClientSettings.INSTANCE.getHideActiveBgColor().getRed(),
                            ClientSettings.INSTANCE.getHideActiveBgColor().getGreen(),
                            ClientSettings.INSTANCE.getHideActiveBgColor().getBlue(), 60));
        }
        Unfair.fontManager.getFont(fontSize).drawString(hiddenLabel, x + 4, y, hiddenColor, false);

        String keyName = listening ? "..." : KeyBindUtil.getKeyName(module.getKey());
        String display = "[" + keyName + "]";
        float bindW = Unfair.fontManager.getFont(fontSize).getStringWidth(display);
        float bindX = x + (width - bindW - 2);
        boolean bindHover = isHovering(mouseX, mouseY, bindX, y, bindW + 4, getHeight());

        if (listening) {
            HUD hud = getHud();
            Color c = hud.getColor(System.currentTimeMillis());
            RoundedUtils.drawRound(bindX - 2, y + 1, bindW + 6, getHeight() - 2, 4,
                    new Color(c.getRed(), c.getGreen(), c.getBlue(), 60));
        } else if (bindHover) {
            RoundedUtils.drawRound(bindX - 2, y + 1, bindW + 6, getHeight() - 2, 4,
                    new Color(ClientSettings.INSTANCE.getBindActiveBgColor().getRed(),
                            ClientSettings.INSTANCE.getBindActiveBgColor().getGreen(),
                            ClientSettings.INSTANCE.getBindActiveBgColor().getBlue(), 40));
        }
        int color = listening ? getHud().getColor(System.currentTimeMillis()).getRGB()
                : ClientSettings.INSTANCE.getTextDisabledColor().getRGB();
        Unfair.fontManager.getFont(fontSize).drawString(display, bindX, y, color, false);
    }

    @Override
    public void mouseClicked(int mx, int my, int button) {
        int fontSize = 17;
        String hiddenLabel = module.isHidden() ? "Hidden" : "Hide";
        float hideW = Unfair.fontManager.getFont(fontSize).getStringWidth(hiddenLabel);

        if (button == 0 && isHovering(mx, my, x + 2, y, hideW + 4, getHeight())) {
            module.setHidden(!module.isHidden());
            return;
        }
        String keyName = listening ? "..." : KeyBindUtil.getKeyName(module.getKey());
        String display = "[" + keyName + "]";
        float bindW = Unfair.fontManager.getFont(fontSize).getStringWidth(display);
        float bindX = x + (width - bindW - 2);

        if (button == 0 && isHovering(mx, my, bindX, y, bindW + 4, getHeight())) {
            if (!listening) {
                if (anyListening) return;
                listening = true;
                anyListening = true;
                KeyBinding.unPressAllKeys();
            }
        } else if (listening) {
            listening = false;
            anyListening = false;
        }
        if (listening && button != 0) {
            listening = false;
            anyListening = false;
        }
    }

    @Override
    public void mouseReleased(int mx, int my, int button) {}

    @Override
    public float getHeight() { return 14; }

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