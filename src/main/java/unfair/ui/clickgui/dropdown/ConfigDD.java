package unfair.ui.clickgui.dropdown;

import unfair.Unfair;
import unfair.config.Config;
import unfair.management.ClientSettings;
import unfair.property.properties.TextProperty;
import unfair.util.ChatUtil;
import unfair.util.RenderUtil;
import unfair.util.shader.BlurUtils;
import unfair.util.shader.RoundedUtils;

import java.awt.*;
import java.io.File;

public class ConfigDD {
    public String configName;
    public float x, y, width;
    public static final float HEIGHT = 17;
    public boolean active = false;
    public boolean settingsOpen = false;

    private ValueItem[] settings;
    private String renameText = "";
    private ConfigPanel parentPanel;

    public ConfigDD(String name, ConfigPanel panel) {
        this.configName = name;
        this.parentPanel = panel;
        this.renameText = name.replace(".json", "");
        initSettings();
        if (name.equals(parentPanel.getCurrentConfig())) active = true;
    }

    private void initSettings() {
        ButtonSetting loadBtn = new ButtonSetting("Load", () -> {
            new Config(configName, false).load();
            parentPanel.setCurrentConfig(configName);
            parentPanel.updateActiveConfig(configName);
        });

        ButtonSetting saveBtn = new ButtonSetting("Save", () -> {
            new Config(configName, true).save();
            parentPanel.setCurrentConfig(configName);
            parentPanel.updateActiveConfig(configName);
        });

        TextProperty renameProp = new TextProperty("Rename", renameText);
        TextSetting renameSetting = new TextSetting(renameProp);

        ButtonSetting confirmRenameBtn = new ButtonSetting("Confirm Rename", () -> {
            String newName = renameProp.getValue().trim();
            if (newName.isEmpty()) return;
            if (!newName.endsWith(".json")) newName += ".json";
            if (newName.equalsIgnoreCase(configName)) return;
            if (parentPanel.configExists(newName)) {
                ChatUtil.sendFormatted("Config already exists: " + newName);
                return;
            }
            File oldFile = new File("./config/Unfair/", configName);
            File newFile = new File("./config/Unfair/", newName);
            if (oldFile.exists() && !newFile.exists() && oldFile.renameTo(newFile)) {
                configName = newName;
                renameText = newName.replace(".json", "");
                ChatUtil.sendFormatted("Renamed config: " + newName);
                parentPanel.refreshConfigs();
            }
        });

        if ("default.json".equals(configName)) {
            settings = new ValueItem[]{loadBtn, saveBtn, renameSetting, confirmRenameBtn};
        } else {
            ButtonSetting deleteBtn = new ButtonSetting("Delete", () -> {
                if ("default.json".equals(configName)) return;
                File file = new File("./config/Unfair/", configName);
                if (file.delete()) {
                    parentPanel.removeConfig(this);
                    ChatUtil.sendFormatted("Deleted config: " + configName);
                }
            });
            settings = new ValueItem[]{loadBtn, saveBtn, deleteBtn, renameSetting, confirmRenameBtn};
        }
    }

    public void render(int mouseX, int mouseY) {
        String displayName = configName.replace(".json", "");
        int fontSize = 17;
        float textWidth = Unfair.fontManager.getFont(fontSize).getStringWidth(displayName);
        float textX = x + (width - textWidth) / 2;

        boolean hover = isHovering(mouseX, mouseY, x, y, width, HEIGHT);

        Unfair.fontManager.getFont(fontSize).drawString(displayName, textX, y + 2,
                active ? ClientSettings.INSTANCE.getTextEnabledColor().getRGB() : ClientSettings.INSTANCE.getTextDisabledColor().getRGB(), false);

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
                setting.x = x + 2; setting.y = settingY; setting.width = width - 4; setting.masterAlpha = 1f;
                setting.render(mouseX, mouseY);
                settingY += setting.getHeight() + 2;
            }
        }
    }

    public void mouseClicked(int mx, int my, int button) {
        if (isHovering(mx, my, x, y, width, HEIGHT)) {
            if (button == 0) {
                new Config(configName, false).load();
                parentPanel.setCurrentConfig(configName);
                parentPanel.updateActiveConfig(configName);
            } else if (button == 1 && hasVisibleSettings()) settingsOpen = !settingsOpen;
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
        for (ValueItem s : settings) { if (!s.visible()) continue; h += s.getHeight() + 2; }
        return h > 2 ? h - 2 : 0;
    }

    private boolean hasVisibleSettings() { for (ValueItem s : settings) if (s.visible()) return true; return false; }
    public void setActive(boolean state) { this.active = state; }

    protected boolean isHovering(int mx, int my, float x, float y, float w, float h) {
        return mx >= x && mx <= x + w && my >= y && my <= y + h;
    }
}