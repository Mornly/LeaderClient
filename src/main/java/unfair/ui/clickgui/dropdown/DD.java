package unfair.ui.clickgui.dropdown;

import unfair.Unfair;
import unfair.management.ClientSettings;
import unfair.module.Module;
import unfair.module.modules.render.HUD;
import unfair.property.Property;
import unfair.property.properties.*;
import unfair.util.RenderUtil;
import unfair.util.shader.RoundedUtils;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class DD {
    public Module module;
    public float x, y, width;
    public static final float HEIGHT = 17;

    public boolean settingsOpen = false;

    public ValueItem[] settings;

    public DD(Module m) {
        this.module = m;
        initSettings();
    }

    private void initSettings() {
        List<ValueItem> list = new ArrayList<>();
        list.add(new ClientSetting(module));
        List<Property<?>> props = Unfair.propertyManager.properties.get(module.getClass());
        if (props != null) {
            for (Property<?> v : props) {
                if (v instanceof BooleanProperty) list.add(new BoolSetting((BooleanProperty) v));
                else if (v instanceof FloatProperty || v instanceof IntProperty || v instanceof PercentProperty) list.add(new SliderSetting(v));
                else if (v instanceof ModeProperty) list.add(new ModeSetting((ModeProperty) v));
                else if (v instanceof ColorProperty) list.add(new ColorSetting((ColorProperty) v));
                else if (v instanceof TextProperty) list.add(new TextSetting((TextProperty) v));
            }
        }
        settings = list.toArray(new ValueItem[0]);
    }

    private static HUD getHud() {
        return (HUD) Unfair.moduleManager.modules.get(HUD.class);
    }

    private static void drawString(String text, float x, float y, int color, int size, boolean shadow) {
        Unfair.fontManager.getFont(size).drawString(text, x, y, color, shadow);
    }

    private static float stringWidth(String text, int size) {
        return Unfair.fontManager.getFont(size).getStringWidth(text);
    }

    public void render(int mouseX, int mouseY) {
        String name = module.getName();
        int fontSize = 17;
        float textWidth = stringWidth(name, fontSize);
        float textX = x + (width - textWidth) / 2;
        boolean enabled = module.isEnabled();

        long time = System.currentTimeMillis();
        HUD hud = getHud();

        if (enabled) {
            Color accent = hud.getColor(time);
            int textColor = accent.getRGB();
            drawString(name, textX, y + 3, textColor, fontSize, false);
        } else {
            drawString(name, textX, y + 3, ClientSettings.INSTANCE.getTextDisabledColor().getRGB(), fontSize, false);
        }

        if (hasVisibleSettings()) {
            String arrow = settingsOpen ? "▼" : "▶";
            float arrowW = stringWidth(arrow, 12);
            int arrowColor = settingsOpen ? hud.getColor(time).getRGB() : ClientSettings.INSTANCE.getTextDisabledColor().getRGB();
            Unfair.fontManager.getFont(12).drawString(arrow, x + width - arrowW - 3, y + 3, arrowColor, false);
        }

        if (settingsOpen && hasVisibleSettings()) {
            float totalSettingsHeight = 0;
            for (ValueItem setting : settings) {
                if (setting.visible()) totalSettingsHeight += setting.getHeight() + 2;
            }
            if (totalSettingsHeight > 0) totalSettingsHeight -= 2;

            int bgColor = ClientSettings.INSTANCE.getDropdownBgColor().getRGB();
            RoundedUtils.drawRound(x + 2, y + HEIGHT - 1, width - 4, totalSettingsHeight, 4, bgColor);

            float settingY = y + HEIGHT + 1;
            for (ValueItem setting : settings) {
                if (!setting.visible()) continue;
                setting.x = x + 2;
                setting.y = settingY;
                setting.width = width - 4;
                setting.masterAlpha = 1f;
                setting.render(mouseX, mouseY);
                settingY += setting.getHeight() + 2;
            }
        }
    }

    public void mouseClicked(int mx, int my, int button) {
        if (isHovering(mx, my, x, y, width, HEIGHT)) {
            if (button == 0) module.toggle();
            else if (button == 1 && hasVisibleSettings()) settingsOpen = !settingsOpen;
            return;
        }
        if (settingsOpen) {
            for (ValueItem s : settings) s.mouseClicked(mx, my, button);
        }
    }

    public void mouseReleased(int mx, int my, int button) {
        for (ValueItem s : settings) s.mouseReleased(mx, my, button);
    }

    public void charTyped(char chr) {
        for (ValueItem s : settings) {
            if (s instanceof TextSetting) ((TextSetting) s).charTyped(chr);
        }
    }

    public void keyPressed(int keyCode) {
        for (ValueItem s : settings) {
            if (s instanceof TextSetting) ((TextSetting) s).keyPressed(keyCode);
            else if (s instanceof ClientSetting) ((ClientSetting) s).keyPressed(keyCode);
        }
    }

    public float getTotalHeight() { return HEIGHT + (settingsOpen ? getSettingsHeight() : 0); }

    private float getSettingsHeight() {
        float h = 0;
        for (ValueItem s : settings) {
            if (!s.visible()) continue;
            h += s.getHeight() + 2;
        }
        return h > 2 ? h - 2 : 0;
    }

    private boolean hasVisibleSettings() {
        for (ValueItem s : settings) if (s.visible()) return true;
        return false;
    }

    private boolean isHovering(int mx, int my, float x, float y, float w, float h) {
        return mx >= x && mx <= x + w && my >= y && my <= y + h;
    }
}