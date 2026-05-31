package unfair.ui.clickgui.dropdown;

import unfair.Unfair;
import unfair.config.Config;
import unfair.util.RenderUtil;
import unfair.util.ChatUtil;
import unfair.property.properties.TextProperty;

import java.awt.*;

public class CreateDD {
    public float x, y, width;
    public static final float HEIGHT = 20;
    private boolean settingsOpen = false;
    private final ValueItem[] settings;
    private TextProperty nameProp;
    private final ConfigPanel parentPanel;

    public CreateDD(ConfigPanel panel) {
        this.parentPanel = panel;
        nameProp = new TextProperty("Name", "newconfig");
        TextSetting nameSetting = new TextSetting(nameProp);
        ButtonSetting createBtn = new ButtonSetting("Create", () -> {
            String name = nameProp.getValue().trim();
            if (name.isEmpty()) return;
            if (!name.endsWith(".json")) name += ".json";
            if (parentPanel.configExists(name)) {
                ChatUtil.sendFormatted("Config already exists: " + name);
                return;
            }
            new Config(name, true).save();
            parentPanel.refreshConfigs();
            ChatUtil.sendFormatted("Created config: " + name);
            nameProp.setValue("newconfig");
            settingsOpen = false;
        });
        settings = new ValueItem[]{nameSetting, createBtn};
    }

    public void render(int mouseX, int mouseY) {
        Unfair.fontManager.getFont(20).drawString("+ New Config", x + 7, y + 2,
                new Color(100, 180, 255).getRGB(), false);

        if (settingsOpen) {
            float totalSettingsHeight = 0;
            for (ValueItem s : settings) totalSettingsHeight += s.getHeight() + 3;
            if (totalSettingsHeight > 0) totalSettingsHeight -= 3;

            RenderUtil.drawRect(x, y + HEIGHT, x + width, y + HEIGHT + totalSettingsHeight,
                    new Color(0, 0, 0, 110).getRGB());

            float settingY = y + HEIGHT;
            for (ValueItem s : settings) {
                s.x = x; s.y = settingY; s.width = width; s.masterAlpha = 1f;
                s.render(mouseX, mouseY);
                settingY += s.getHeight() + 3;
            }
        }
    }

    public void mouseClicked(int mx, int my, int button) {
        if (isHovering(mx, my, x, y, width, HEIGHT)) {
            if (button == 1) settingsOpen = !settingsOpen;
            return;
        }
        if (settingsOpen) for (ValueItem s : settings) s.mouseClicked(mx, my, button);
    }

    public void mouseReleased(int mx, int my, int button) { for (ValueItem s : settings) s.mouseReleased(mx, my, button); }
    public void charTyped(char chr) { if (settingsOpen) for (ValueItem s : settings) if (s instanceof TextSetting) ((TextSetting)s).charTyped(chr); }
    public void keyPressed(int keyCode) { if (settingsOpen) for (ValueItem s : settings) if (s instanceof TextSetting) ((TextSetting)s).keyPressed(keyCode); }
    public float getTotalHeight() { return HEIGHT + (settingsOpen ? getSettingsHeight() : 0); }

    private float getSettingsHeight() {
        float h = 0;
        for (ValueItem s : settings) h += s.getHeight() + 3;
        return h > 3 ? h - 3 : 0;
    }

    protected boolean isHovering(int mx, int my, float x, float y, float w, float h) {
        return mx >= x && mx <= x + w && my >= y && my <= y + h;
    }
}
