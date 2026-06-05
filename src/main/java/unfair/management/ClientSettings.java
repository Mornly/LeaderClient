package unfair.management;

import unfair.util.shader.RoundedUtils;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ClientSettings {
    public static final ClientSettings INSTANCE = new ClientSettings();
    
    public enum GUISize {
        SIZE_100(1.0f, "100%"),
        SIZE_75(0.75f, "75%");
        
        private final float scale;
        private final String displayName;
        
        GUISize(float scale, String displayName) {
            this.scale = scale;
            this.displayName = displayName;
        }
        
        public float getScale() { return scale; }
        public String getDisplayName() { return displayName; }
    }
    
    public enum Theme {
        OCEAN_BLUE("Ocean Blue",
            new Color(230, 240, 250), new Color(245, 250, 255), new Color(255, 255, 255),
            new Color(35, 55, 85), new Color(70, 130, 180), new Color(100, 150, 200),
            new Color(200, 220, 240), new Color(180, 200, 220)),
        
        SUNSET_ORANGE("Sunset Orange",
            new Color(255, 245, 235), new Color(255, 250, 245), new Color(255, 255, 255),
            new Color(60, 45, 35), new Color(255, 120, 50), new Color(255, 150, 80),
            new Color(255, 230, 210), new Color(255, 220, 200)),
        
        FOREST_GREEN("Forest Green",
            new Color(235, 245, 235), new Color(245, 250, 245), new Color(255, 255, 255),
            new Color(35, 55, 35), new Color(80, 160, 80), new Color(100, 180, 100),
            new Color(210, 230, 210), new Color(200, 220, 200)),
        
        PURPLE_DREAM("Purple Dream",
            new Color(245, 240, 250), new Color(250, 245, 255), new Color(255, 255, 255),
            new Color(50, 40, 70), new Color(150, 100, 200), new Color(170, 130, 220),
            new Color(230, 220, 240), new Color(220, 210, 230)),
        
        ROSE_PINK("Rose Pink",
            new Color(255, 245, 250), new Color(255, 250, 252), new Color(255, 255, 255),
            new Color(60, 45, 55), new Color(255, 130, 150), new Color(255, 160, 180),
            new Color(255, 230, 240), new Color(255, 220, 235));
        
        private final String displayName;
        private final Color background;
        private final Color sidebar;
        private final Color card;
        private final Color text;
        private final Color accent;
        private final Color accentHover;
        private final Color border;
        private final Color borderLight;
        
        Theme(String displayName, Color background, Color sidebar, Color card, 
              Color text, Color accent, Color accentHover, Color border, Color borderLight) {
            this.displayName = displayName;
            this.background = background;
            this.sidebar = sidebar;
            this.card = card;
            this.text = text;
            this.accent = accent;
            this.accentHover = accentHover;
            this.border = border;
            this.borderLight = borderLight;
        }
        
        public String getDisplayName() { return displayName; }
        public Color getBackground() { return background; }
        public Color getSidebar() { return sidebar; }
        public Color getCard() { return card; }
        public Color getText() { return text; }
        public Color getAccent() { return accent; }
        public Color getAccentHover() { return accentHover; }
        public Color getBorder() { return border; }
        public Color getBorderLight() { return borderLight; }
    }
    
    private GUISize guiSize = GUISize.SIZE_100;
    private Theme currentTheme = Theme.OCEAN_BLUE;
    private Theme targetTheme = Theme.OCEAN_BLUE;

    private int backgroundAlpha = 255;
    private boolean blurArea = false;
    
    private float themeTransitionProgress = 1.0f;
    private float[] currentColors = new float[24];
    private float[] targetColors = new float[24];
    
    public ClientSettings() {
        updateColorArrays(currentTheme, currentColors);
        updateColorArrays(currentTheme, targetColors);
    }
    
    private void updateColorArrays(Theme theme, float[] colors) {
        Color bg = theme.getBackground();
        Color sb = theme.getSidebar();
        Color card = theme.getCard();
        Color text = theme.getText();
        Color accent = theme.getAccent();
        Color accentHover = theme.getAccentHover();
        Color border = theme.getBorder();
        Color borderLight = theme.getBorderLight();
        
        colors[0] = bg.getRed(); colors[1] = bg.getGreen(); colors[2] = bg.getBlue();
        colors[3] = sb.getRed(); colors[4] = sb.getGreen(); colors[5] = sb.getBlue();
        colors[6] = card.getRed(); colors[7] = card.getGreen(); colors[8] = card.getBlue();
        colors[9] = text.getRed(); colors[10] = text.getGreen(); colors[11] = text.getBlue();
        colors[12] = accent.getRed(); colors[13] = accent.getGreen(); colors[14] = accent.getBlue();
        colors[15] = accentHover.getRed(); colors[16] = accentHover.getGreen(); colors[17] = accentHover.getBlue();
        colors[18] = border.getRed(); colors[19] = border.getGreen(); colors[20] = border.getBlue();
        colors[21] = borderLight.getRed(); colors[22] = borderLight.getGreen(); colors[23] = borderLight.getBlue();
    }
    
    public void update(float deltaTime) {
        if (themeTransitionProgress < 1.0f) {
            themeTransitionProgress += deltaTime * 1.5f;
            if (themeTransitionProgress > 1.0f) {
                themeTransitionProgress = 1.0f;
                currentTheme = targetTheme;
                updateColorArrays(currentTheme, currentColors);
            } else {
                for (int i = 0; i < currentColors.length; i++) {
                    currentColors[i] = currentColors[i] + (targetColors[i] - currentColors[i]) * deltaTime * 1.5f;
                }
            }
        }
    }
    
    public GUISize getGuiSize() { return guiSize; }
    public void setGuiSize(GUISize size) { this.guiSize = size; }
    
    public Theme getTheme() { return currentTheme; }
    public void setTheme(Theme theme) {
        if (theme != currentTheme) {
            this.targetTheme = theme;
            updateColorArrays(theme, targetColors);
            themeTransitionProgress = 0.0f;
        }
    }
    
    public float getGUIScale() { return guiSize.getScale(); }
    
    public Color getBackgroundColor() {
        return new Color((int)currentColors[0], (int)currentColors[1], (int)currentColors[2]);
    }
    
    public Color getSidebarColor() {
        return new Color((int)currentColors[3], (int)currentColors[4], (int)currentColors[5]);
    }
    
    public Color getCardColor() {
        return new Color((int)currentColors[6], (int)currentColors[7], (int)currentColors[8]);
    }
    
    public Color getTextColor() {
        return new Color((int)currentColors[9], (int)currentColors[10], (int)currentColors[11]);
    }
    
    public Color getAccentColor() {
        return new Color((int)currentColors[12], (int)currentColors[13], (int)currentColors[14]);
    }
    
    public Color getAccentHoverColor() {
        return new Color((int)currentColors[15], (int)currentColors[16], (int)currentColors[17]);
    }
    
    public Color getBorderColor() {
        return new Color((int)currentColors[18], (int)currentColors[19], (int)currentColors[20]);
    }
    
    public Color getBorderLightColor() {
        return new Color((int)currentColors[21], (int)currentColors[22], (int)currentColors[23]);
    }

    public int getBackgroundAlpha() { return backgroundAlpha; }
    public void setBackgroundAlpha(int alpha) {
        this.backgroundAlpha = Math.max(0, Math.min(255, alpha));
    }

    public boolean isBlurArea() { return blurArea; }
    public void setBlurArea(boolean blur) { this.blurArea = blur; }
}