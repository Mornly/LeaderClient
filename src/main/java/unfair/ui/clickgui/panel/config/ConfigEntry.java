package unfair.ui.clickgui.panel.config;

import unfair.Unfair;
import unfair.config.Config;
import unfair.management.ClientSettings;
import unfair.ui.clickgui.panel.PanelValueItem;
import unfair.util.ChatUtil;
import unfair.util.shader.RoundedUtils;

import java.awt.*;
import java.io.File;

public class ConfigEntry extends PanelValueItem {
    private final String configName;
    private boolean isActive;
    private boolean isHovering;

    public ConfigEntry(String configName) {
        this.configName = configName.endsWith(".json") ? 
            configName.substring(0, configName.length() - 5) : configName;
        this.isActive = this.configName.equals(Config.lastConfig);
    }

    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);
    }

    @Override
    public void render(int mouseX, int mouseY) {
        isHovering = mouseX >= x && mouseX <= x + width && 
                     mouseY >= y && mouseY <= y + getHeight();

        float cardH = getHeight();
        
        Color cardNormal = ClientSettings.INSTANCE.getCardColor();
        Color cardActive = ClientSettings.INSTANCE.getEntryActiveColor();
        Color cardHover = ClientSettings.INSTANCE.getEntryHoverColor();
        
        Color bgColor;
        if (isActive) {
            bgColor = cardActive;
        } else if (isHovering) {
            bgColor = cardHover;
        } else {
            bgColor = cardNormal;
        }
        
        RoundedUtils.drawRound(x, y, width, cardH, 5, blendAlpha(bgColor, alpha));

        float iconX = x + 12;
        float iconY = y + (cardH - 16) / 2f;
        Unfair.fontManager.icon16.drawString("\uf0c7", iconX, iconY + 4.5f,
                blendAlpha(ClientSettings.INSTANCE.getEntryIconColor(), alpha).getRGB(), false);

        float textX = iconX + 22;
        float textY = y + (cardH - 18) / 2f;

        int textColor = isActive ? 
            blendAlpha(ClientSettings.INSTANCE.getEntryValueColor(), alpha).getRGB(): 
            blendAlpha(ClientSettings.INSTANCE.getConfigNameInactiveColor(), alpha).getRGB();
        
        Unfair.fontManager.getFont(18).drawString(configName, textX, textY + 1.5f, textColor, false);

        if (isActive) {
            String activeTag = "Active";
            float tagW = Unfair.fontManager.getFont(14).getStringWidth(activeTag);
            float tagFinalX = textX + Unfair.fontManager.getFont(18).getStringWidth(configName) + 8;

            RoundedUtils.drawRound(tagFinalX, textY, tagW + 12, 16, 3,
                    blendAlpha(ClientSettings.INSTANCE.getButtonPrimaryColor(), alpha * 0.9f));
            Unfair.fontManager.getFont(14).drawString(activeTag, tagFinalX + 6, textY + 4,
                    blendAlpha(ClientSettings.INSTANCE.getCardColor(), alpha).getRGB(), false);
        }

        float btnY = y + (cardH - 18) / 2f + 1;
        float btnStartX = x + width - 200;

        drawSmallButton(btnStartX, btnY, "Load", ClientSettings.INSTANCE.getButtonPrimaryColor(),
                       isBtnHovered(mouseX, mouseY, btnStartX, btnY, 48, 18));
        drawSmallButton(btnStartX + 52, btnY, "Save", ClientSettings.INSTANCE.getButtonPrimaryColor(),
                       isBtnHovered(mouseX, mouseY, btnStartX + 52, btnY, 48, 18));
        drawSmallButton(btnStartX + 104, btnY, "Delete", ClientSettings.INSTANCE.getButtonDangerColor(),
                       isBtnHovered(mouseX, mouseY, btnStartX + 104, btnY, 56, 18));
    }

    private void drawSmallButton(float bx, float by, String text, Color baseColor, boolean hovered) {
        float btnW = text.equals("Delete") ? 56 : 48;
        float btnH = 18;

        Color color = hovered ? baseColor.brighter() : baseColor;
        
        RoundedUtils.drawRound(bx, by, btnW, btnH, 4, blendAlpha(color, alpha));

        float textW = Unfair.fontManager.getFont(14).getStringWidth(text);
        Unfair.fontManager.getFont(14).drawString(text,
                bx + (btnW - textW) / 2f, by + (btnH - 14) / 2f + 2.0f,
                blendAlpha(ClientSettings.INSTANCE.getCardColor(), alpha).getRGB(), false);
    }

    private boolean isBtnHovered(int mx, int my, float bx, float by, float bw, float bh) {
        return isHovering && mx >= bx && mx <= bx + bw && my >= by && my <= by + bh;
    }

    @Override
    public void mouseClicked(int mx, int my, int button) {
        if (button != 0) return;
        
        boolean hovering = mx >= x && mx <= x + width && my >= y && my <= y + getHeight();
        if (!hovering) return;

        float cardH = getHeight();
        float btnY = y + (cardH - 18) / 2f + 1;
        float btnStartX = x + width - 200;

        if (isBtnHovered(mx, my, btnStartX, btnY, 48, 18)) {
            loadConfig();
        } else if (isBtnHovered(mx, my, btnStartX + 52, btnY, 48, 18)) {
            saveConfig();
        } else if (isBtnHovered(mx, my, btnStartX + 104, btnY, 56, 18)) {
            deleteConfig();
        }
    }

    @Override
    public void mouseReleased(int mx, int my, int button) {}
    
    @Override
    public void mouseDragged(int mx, int my, int button) {}

    @Override
    public float getHeight() { return 34; }

    @Override
    public boolean visible() { return true; }

    public boolean isHovering(int mx, int my) {
        return mx >= x && mx <= x + width && my >= y && my <= y + getHeight();
    }

    private void loadConfig() {
        try {
            Config config = new Config(configName, false);
            config.load();
            setActive(true);
            ChatUtil.sendFormatted(Unfair.clientName + "&aLoaded: &f" + configName);
        } catch (Exception e) {
            ChatUtil.sendFormatted(Unfair.clientName + "&cFailed to load: &f" + configName);
        }
    }

    private void saveConfig() {
        try {
            Config config = new Config(configName, false);
            config.save();
            setActive(true);
            ChatUtil.sendFormatted(Unfair.clientName + "&aSaved: &f" + configName);
        } catch (Exception e) {
            ChatUtil.sendFormatted(Unfair.clientName + "&cFailed to save: &f" + configName);
        }
    }

    private void deleteConfig() {
        try {
            File file = new File("./config/Unfair/" + configName + ".json");
            if (file.exists()) {
                file.delete();
                ChatUtil.sendFormatted(Unfair.clientName + "&cDeleted: &f" + configName);
                if (isActive) {
                    Config defaultCfg = new Config("default", false);
                    if (defaultCfg.file.exists()) defaultCfg.load();
                    setActive(false);
                }
            }
        } catch (Exception e) {
            ChatUtil.sendFormatted(Unfair.clientName + "&cFailed to delete: &f" + configName);
        }
    }

    public void setActive(boolean active) {
        this.isActive = active;
        if (active) Config.lastConfig = configName;
    }

    public String getConfigName() { return configName; }

    public void resetAnimations() {}

    private static Color blendAlpha(Color c, float a) {
        return new Color(c.getRed(), c.getGreen(), c.getBlue(), (int)(a * 255));
    }
}
