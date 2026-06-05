package unfair.ui.clickgui.panel.config;

import unfair.Unfair;
import unfair.management.ClientSettings;
import unfair.ui.clickgui.panel.PanelValueItem;
import unfair.util.shader.RoundedUtils;

import java.awt.*;

public class SettingsEntry extends PanelValueItem {
    private final String name;
    private final SettingsType type;
    private Object value;
    
    private float hoverAnim = 0f;
    private boolean hovered = false;
    private boolean sliderDragging = false;

    private float toggleAnim = 0f;
    private float sliderDisplayAlpha = 0f;
    
    public enum SettingsType {
        GUI_SIZE,
        THEME,
        BACKGROUND_ALPHA,
        BLUR_AREA
    }
    
    public SettingsEntry(String name, SettingsType type) {
        this.name = name;
        this.type = type;
        updateValue();
    }
    
    private void updateValue() {
        switch (type) {
            case GUI_SIZE:
                value = ClientSettings.INSTANCE.getGuiSize();
                break;
            case THEME:
                value = ClientSettings.INSTANCE.getTheme();
                break;
            case BACKGROUND_ALPHA:
                value = ClientSettings.INSTANCE.getBackgroundAlpha();
                break;
            case BLUR_AREA:
                value = ClientSettings.INSTANCE.isBlurArea();
                break;
        }
    }
    
    @Override
    public void update(float deltaTime) {
        float target = hovered ? 1f : 0f;
        hoverAnim = lerp(hoverAnim, target, 0.15f, deltaTime);

        if (type == SettingsType.BLUR_AREA) {
            boolean on = ClientSettings.INSTANCE.isBlurArea();
            float tTarget = on ? 1f : 0f;
            toggleAnim = lerp(toggleAnim, tTarget, 0.18f, deltaTime);
        }

        if (type == SettingsType.BACKGROUND_ALPHA) {
            float sTarget = sliderDragging ? 1f : 0f;
            sliderDisplayAlpha = lerp(sliderDisplayAlpha, sTarget, 0.15f, deltaTime);
        }
    }
    
    @Override
    public void render(int mouseX, int mouseY) {

        float entryHeight = getHeight();
        hovered = isHovering(mouseX, mouseY, x, y, width, entryHeight);
        
        Color bgColor = blendColor(ClientSettings.INSTANCE.getEntryBgColor(), ClientSettings.INSTANCE.getEntryHoverColor(), hoverAnim);
        RoundedUtils.drawRound(x, y, width, entryHeight, 5, blendAlpha(bgColor, alpha));
        
        Unfair.fontManager.getFont(14).drawString(name, x + 12, y + 8, 
                blendAlpha(ClientSettings.INSTANCE.getTitleMainColor(), alpha).getRGB(), false);
        
        if (type == SettingsType.BACKGROUND_ALPHA) {
            drawSlider(mouseX);
        } else if (type == SettingsType.BLUR_AREA) {
            drawToggle();
        } else {
            String valueText = getValueText();
            float textW = Unfair.fontManager.getFont(13).getStringWidth(valueText);
            Unfair.fontManager.getFont(13).drawString(valueText, x + width - textW - 12, y + 9, 
                    blendAlpha(ClientSettings.INSTANCE.getEntryValueColor(), alpha).getRGB(), false);
        }
    }

    private void drawSlider(int mouseX) {
        int alphaVal = ClientSettings.INSTANCE.getBackgroundAlpha();
        float sliderX = x + 12;
        float sliderY = y + 26;
        float sliderW = width - 24;
        float sliderH = 4;

        RoundedUtils.drawRound(sliderX, sliderY, sliderW, sliderH, 2,
                blendAlpha(ClientSettings.INSTANCE.getSliderTrackColor(), alpha));

        float fillW = (alphaVal / 255f) * sliderW;
        if (fillW > 0) {
            RoundedUtils.drawRound(sliderX, sliderY, fillW, sliderH, 2,
                    blendAlpha(ClientSettings.INSTANCE.getAccentColor(), alpha));
        }

        float dotSize = 10 + sliderDisplayAlpha * 4;
        float dotX = sliderX + fillW;
        Color dotColor = blendColor(ClientSettings.INSTANCE.getAccentColor(), ClientSettings.INSTANCE.getCardColor(), sliderDisplayAlpha);
        RoundedUtils.drawRound(dotX - dotSize / 2f, sliderY + (sliderH - dotSize) / 2f,
                dotSize, dotSize, dotSize / 2f, blendAlpha(dotColor, alpha));

        if (sliderDragging) {
            float relativeX = mouseX - sliderX;
            relativeX = Math.max(0, Math.min(sliderW, relativeX));
            int newAlpha = Math.round((relativeX / sliderW) * 255);
            ClientSettings.INSTANCE.setBackgroundAlpha(newAlpha);
            value = newAlpha;
        }
    }

    private void drawToggle() {
        float toggleW = 36;
        float toggleH = 16;
        float toggleX = x + width - toggleW - 12;
        float toggleY = y + (getHeight() - toggleH) / 2f;

        Color trackColor = blendColor(ClientSettings.INSTANCE.getToggleOffColor(), ClientSettings.INSTANCE.getAccentColor(), toggleAnim);
        RoundedUtils.drawRound(toggleX, toggleY, toggleW, toggleH, toggleH / 2f,
                blendAlpha(trackColor, alpha));

        float dotSize = toggleH - 4;
        float dotX = toggleX + 2 + toggleAnim * (toggleW - dotSize - 4);
        float dotY = toggleY + 2;
        RoundedUtils.drawRound(dotX, dotY, dotSize, dotSize, dotSize / 2f,
                blendAlpha(ClientSettings.INSTANCE.getCardColor(), alpha));
    }
    
    private String getValueText() {
        switch (type) {
            case GUI_SIZE:
                return ClientSettings.INSTANCE.getGuiSize().getDisplayName();
            case THEME:
                return ClientSettings.INSTANCE.getTheme().getDisplayName();
            default:
                return "";
        }
    }
    
    @Override
    public void mouseClicked(int mx, int my, int button) {
        if (button != 0) return;
        
        float entryHeight = getHeight();
        if (!isHovering(mx, my, x, y, width, entryHeight)) return;
        
        switch (type) {
            case GUI_SIZE:
                ClientSettings.GUISize[] sizes = ClientSettings.GUISize.values();
                int currentSizeIndex = ((ClientSettings.GUISize) value).ordinal();
                int nextSizeIndex = (currentSizeIndex + 1) % sizes.length;
                ClientSettings.INSTANCE.setGuiSize(sizes[nextSizeIndex]);
                value = sizes[nextSizeIndex];
                break;
                
            case THEME:
                ClientSettings.Theme[] themes = ClientSettings.Theme.values();
                int currentThemeIndex = ((ClientSettings.Theme) value).ordinal();
                int nextThemeIndex = (currentThemeIndex + 1) % themes.length;
                ClientSettings.INSTANCE.setTheme(themes[nextThemeIndex]);
                value = themes[nextThemeIndex];
                break;

            case BACKGROUND_ALPHA:
                sliderDragging = true;
                break;

            case BLUR_AREA:
                ClientSettings.INSTANCE.setBlurArea(!ClientSettings.INSTANCE.isBlurArea());
                value = ClientSettings.INSTANCE.isBlurArea();
                break;
        }
    }
    
    @Override
    public void mouseReleased(int mx, int my, int button) {
        if (button == 0) {
            sliderDragging = false;
        }
    }
    
    @Override
    public void mouseDragged(int mx, int my, int button) {}
    
    @Override
    public float getHeight() {
        return type == SettingsType.BACKGROUND_ALPHA ? 44 : 30;
    }
    
    @Override
    public boolean visible() {
        return true;
    }
    
    private static Color blendColor(Color a, Color b, float t) {
        int r = (int)(a.getRed() + (b.getRed() - a.getRed()) * t);
        int g = (int)(a.getGreen() + (b.getGreen() - a.getGreen()) * t);
        int bl = (int)(a.getBlue() + (b.getBlue() - a.getBlue()) * t);
        return new Color(r, g, bl);
    }
    
    private static Color blendAlpha(Color c, float alpha) {
        return new Color(c.getRed(), c.getGreen(), c.getBlue(), (int)(alpha * 255));
    }
}
