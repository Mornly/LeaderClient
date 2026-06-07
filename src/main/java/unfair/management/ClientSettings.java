package unfair.management;

import java.awt.*;

public class ClientSettings {
    public static final ClientSettings INSTANCE = new ClientSettings();
    
    public enum GUISize {
        SIZE_100(1.0f, "100%"),
        SIZE_75(0.87f, "75%");
        
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
            new Color(200, 220, 240), new Color(180, 200, 220),
            new Color(252, 254, 255), new Color(238, 245, 255), new Color(225, 238, 255),
            new Color(110, 125, 145), new Color(70, 130, 180),
            new Color(205, 212, 222), new Color(195, 202, 212),
            new Color(235, 240, 246), new Color(70, 130, 180), new Color(175, 182, 192), new Color(40, 50, 65),
            new Color(70, 130, 180), new Color(230, 70, 60),
            new Color(76, 175, 80), new Color(244, 67, 54),
            new Color(242, 246, 250), new Color(228, 236, 246),
            new Color(173, 216, 230), new Color(120, 135, 155), new Color(150, 155, 165),
            new Color(225, 238, 250), new Color(195, 210, 225),
            new Color(235, 238, 242), new Color(225, 232, 245),
            new Color(35, 45, 60), new Color(140, 145, 155), new Color(170, 175, 185),
            new Color(210, 212, 216), new Color(80, 90, 105), new Color(120, 130, 145),
            new Color(245, 247, 250), new Color(135, 206, 235),
            new Color(240, 243, 247), new Color(225, 230, 238),
            new Color(235, 242, 252), new Color(245, 247, 250), new Color(85, 95, 110),
            new Color(30, 35, 45), new Color(200, 205, 215),
            new Color(235, 242, 252), new Color(242, 235, 250),
            new Color(220, 235, 245), new Color(230, 220, 240),
            new Color(85, 95, 110), new Color(120, 80, 150),
            new Color(50, 100, 150), new Color(100, 60, 130),
            new Color(60, 65, 75)),
        
        SUNSET_ORANGE("Sunset Orange",
            new Color(255, 245, 235), new Color(255, 250, 245), new Color(255, 255, 255),
            new Color(60, 45, 35), new Color(255, 120, 50), new Color(255, 150, 80),
            new Color(255, 230, 210), new Color(255, 220, 200),
            new Color(255, 252, 248), new Color(255, 243, 232), new Color(255, 235, 218),
            new Color(140, 110, 85), new Color(255, 120, 50),
            new Color(225, 215, 205), new Color(215, 205, 195),
            new Color(252, 246, 240), new Color(255, 120, 50), new Color(165, 140, 120), new Color(55, 40, 30),
            new Color(255, 120, 50), new Color(230, 70, 60),
            new Color(76, 175, 80), new Color(244, 67, 54),
            new Color(252, 246, 240), new Color(245, 232, 220),
            new Color(255, 200, 160), new Color(160, 115, 85), new Color(165, 145, 135),
            new Color(245, 235, 220), new Color(220, 200, 180),
            new Color(245, 238, 228), new Color(255, 225, 195),
            new Color(60, 45, 35), new Color(155, 125, 105), new Color(185, 160, 140),
            new Color(220, 205, 190), new Color(90, 70, 55), new Color(140, 115, 95),
            new Color(255, 248, 240), new Color(255, 200, 150),
            new Color(255, 240, 230), new Color(255, 228, 215),
            new Color(255, 235, 218), new Color(255, 245, 235), new Color(100, 78, 60),
            new Color(45, 35, 28), new Color(210, 195, 180),
            new Color(255, 235, 218), new Color(255, 235, 218),
            new Color(255, 225, 195), new Color(245, 220, 200),
            new Color(100, 78, 60), new Color(160, 90, 60),
            new Color(60, 110, 90), new Color(120, 70, 80),
            new Color(100, 85, 75)),
        
        FOREST_GREEN("Forest Green",
            new Color(235, 245, 235), new Color(245, 250, 245), new Color(255, 255, 255),
            new Color(35, 55, 35), new Color(80, 160, 80), new Color(100, 180, 100),
            new Color(210, 230, 210), new Color(200, 220, 200),
            new Color(248, 254, 248), new Color(235, 248, 235), new Color(220, 240, 220),
            new Color(100, 125, 100), new Color(80, 160, 80),
            new Color(212, 222, 212), new Color(202, 212, 202),
            new Color(242, 250, 242), new Color(80, 160, 80), new Color(155, 170, 150), new Color(35, 50, 35),
            new Color(80, 160, 80), new Color(230, 70, 60),
            new Color(76, 175, 80), new Color(244, 67, 54),
            new Color(242, 250, 242), new Color(228, 240, 228),
            new Color(140, 200, 160), new Color(100, 135, 105), new Color(130, 155, 140),
            new Color(225, 240, 225), new Color(200, 220, 200),
            new Color(230, 240, 230), new Color(215, 235, 215),
            new Color(35, 55, 35), new Color(120, 140, 125), new Color(160, 175, 165),
            new Color(205, 220, 205), new Color(65, 85, 65), new Color(100, 120, 105),
            new Color(248, 252, 248), new Color(140, 190, 140),
            new Color(235, 245, 235), new Color(220, 238, 220),
            new Color(220, 240, 220), new Color(240, 248, 240), new Color(75, 95, 75),
            new Color(30, 42, 30), new Color(185, 205, 185),
            new Color(220, 240, 220), new Color(235, 230, 240),
            new Color(210, 235, 215), new Color(225, 220, 235),
            new Color(75, 95, 75), new Color(100, 75, 120),
            new Color(45, 90, 60), new Color(90, 55, 85),
            new Color(75, 85, 80)),
        
        PURPLE_DREAM("Purple Dream",
            new Color(245, 240, 250), new Color(250, 245, 255), new Color(255, 255, 255),
            new Color(50, 40, 70), new Color(150, 100, 200), new Color(170, 130, 220),
            new Color(230, 220, 240), new Color(220, 210, 230),
            new Color(252, 248, 255), new Color(240, 232, 250), new Color(228, 216, 245),
            new Color(115, 100, 135), new Color(150, 100, 200),
            new Color(218, 212, 225), new Color(208, 202, 215),
            new Color(246, 240, 252), new Color(150, 100, 200), new Color(160, 145, 175), new Color(45, 38, 65),
            new Color(150, 100, 200), new Color(230, 70, 60),
            new Color(76, 175, 80), new Color(244, 67, 54),
            new Color(246, 240, 252), new Color(234, 224, 246),
            new Color(200, 170, 230), new Color(120, 105, 150), new Color(145, 135, 165),
            new Color(232, 222, 245), new Color(210, 200, 230),
            new Color(238, 232, 246), new Color(225, 215, 240),
            new Color(50, 40, 70), new Color(125, 118, 148), new Color(165, 158, 178),
            new Color(215, 208, 225), new Color(75, 65, 95), new Color(110, 100, 130),
            new Color(250, 244, 252), new Color(180, 150, 220),
            new Color(240, 232, 248), new Color(228, 218, 240),
            new Color(228, 216, 245), new Color(245, 238, 252), new Color(90, 80, 110),
            new Color(38, 32, 55), new Color(195, 185, 215),
            new Color(228, 216, 245), new Color(245, 235, 250),
            new Color(218, 208, 235), new Color(235, 225, 240),
            new Color(90, 80, 110), new Color(120, 80, 150),
            new Color(70, 55, 110), new Color(110, 70, 100),
            new Color(85, 80, 95)),
        
        ROSE_PINK("Rose Pink",
            new Color(255, 245, 250), new Color(255, 250, 252), new Color(255, 255, 255),
            new Color(60, 45, 55), new Color(255, 130, 150), new Color(255, 160, 180),
            new Color(255, 230, 240), new Color(255, 220, 235),
            new Color(255, 250, 252), new Color(255, 240, 246), new Color(255, 230, 240),
            new Color(135, 105, 120), new Color(255, 130, 150),
            new Color(228, 218, 225), new Color(218, 208, 215),
            new Color(252, 244, 250), new Color(255, 130, 150), new Color(170, 145, 158), new Color(55, 42, 52),
            new Color(255, 130, 150), new Color(230, 70, 60),
            new Color(76, 175, 80), new Color(244, 67, 54),
            new Color(252, 246, 250), new Color(248, 234, 242),
            new Color(255, 180, 200), new Color(150, 115, 130), new Color(165, 145, 155),
            new Color(245, 232, 242), new Color(225, 210, 225),
            new Color(248, 242, 247), new Color(245, 232, 242),
            new Color(60, 45, 55), new Color(140, 125, 135), new Color(170, 158, 166),
            new Color(225, 218, 222), new Color(95, 80, 88), new Color(125, 112, 122),
            new Color(254, 246, 250), new Color(255, 175, 195),
            new Color(250, 242, 248), new Color(245, 232, 242),
            new Color(250, 238, 248), new Color(252, 242, 248), new Color(105, 88, 98),
            new Color(48, 38, 46), new Color(205, 192, 202),
            new Color(250, 238, 248), new Color(248, 235, 245),
            new Color(242, 230, 242), new Color(248, 238, 245),
            new Color(95, 82, 92), new Color(140, 85, 105),
            new Color(80, 55, 95), new Color(115, 72, 88),
            new Color(90, 82, 88)),

        MIDNIGHT_BLACK("Midnight Black",
            // bg, sidebar, card
            new Color(15, 18, 30), new Color(20, 24, 38), new Color(28, 32, 46),
            // text primary
            new Color(200, 205, 220),
            // accent, accentHover
            new Color(80, 170, 255), new Color(110, 190, 255),
            // border, borderLight
            new Color(45, 50, 68), new Color(40, 44, 60),
            // entryBg, entryHover, entryActive
            new Color(32, 36, 52), new Color(38, 42, 60), new Color(42, 48, 68),
            // entryIcon, entryValue
            new Color(130, 140, 165), new Color(100, 180, 255),
            // sliderTrack, toggleOff
            new Color(50, 55, 75), new Color(45, 50, 65),
            // inputBg, inputBorder, inputPlaceholder, inputText
            new Color(22, 26, 42), new Color(55, 60, 80), new Color(100, 105, 125), new Color(180, 185, 200),
            // buttonPrimary, buttonDanger
            new Color(70, 140, 240), new Color(230, 70, 60),
            // success, error
            new Color(76, 175, 80), new Color(244, 67, 54),
            // actionBtnNormal, actionBtnHover
            new Color(38, 42, 58), new Color(48, 52, 70),
            // logoText, categoryText, sectionHeader
            new Color(100, 190, 255), new Color(150, 155, 175), new Color(130, 135, 155),
            // cardEnabled, cardHiddenEnabled
            new Color(35, 42, 62), new Color(30, 36, 55),
            // searchBg, selCatBg
            new Color(30, 34, 50), new Color(40, 45, 65),
            // titleMain, titleSub, searchPlaceholder
            new Color(200, 210, 230), new Color(140, 145, 165), new Color(90, 95, 115),
            // hpBarTrack, settingName, valueText
            new Color(45, 48, 62), new Color(175, 180, 200), new Color(200, 205, 225),
            // pickerBg, circleFill
            new Color(22, 26, 42), new Color(100, 180, 240),
            // modeBgNormal, modeBgHover, modeItemSelected, modeItemHover
            new Color(32, 36, 52), new Color(42, 46, 62), new Color(45, 50, 72), new Color(50, 55, 75),
            // modeTextNormal, tipBg, tipText
            new Color(170, 175, 195), new Color(35, 38, 50), new Color(190, 195, 210),
            // bindActive, hideActive, bindActiveBg, hideActiveBg
            new Color(42, 48, 72), new Color(50, 42, 68), new Color(32, 38, 60), new Color(38, 34, 55),
            // textNormal, textHide
            new Color(195, 200, 215), new Color(140, 100, 170),
            // bindActiveText, hideActiveText
            new Color(110, 175, 250), new Color(165, 125, 205),
            // configNameInactive
            new Color(130, 135, 150));

        private final String displayName;
        private final Color[] colors;
        
        Theme(String displayName, Color... colors) {
            this.displayName = displayName;
            this.colors = colors;
        }
        
        public String getDisplayName() { return displayName; }
        public Color getBackground() { return colors[0]; }
        public Color getSidebar() { return colors[1]; }
        public Color getCard() { return colors[2]; }
        public Color getText() { return colors[3]; }
        public Color getAccent() { return colors[4]; }
        public Color getAccentHover() { return colors[5]; }
        public Color getBorder() { return colors[6]; }
        public Color getBorderLight() { return colors[7]; }
        public Color getEntryBg() { return colors[8]; }
        public Color getEntryHover() { return colors[9]; }
        public Color getEntryActive() { return colors[10]; }
        public Color getEntryIcon() { return colors[11]; }
        public Color getEntryValue() { return colors[12]; }
        public Color getSliderTrack() { return colors[13]; }
        public Color getToggleOff() { return colors[14]; }
        public Color getInputBg() { return colors[15]; }
        public Color getInputBorder() { return colors[16]; }
        public Color getInputPlaceholder() { return colors[17]; }
        public Color getInputText() { return colors[18]; }
        public Color getButtonPrimary() { return colors[19]; }
        public Color getButtonDanger() { return colors[20]; }
        public Color getSuccessColor() { return colors[21]; }
        public Color getErrorColor() { return colors[22]; }
        public Color getActionBtnNormal() { return colors[23]; }
        public Color getActionBtnHover() { return colors[24]; }
        public Color getLogoText() { return colors[25]; }
        public Color getCategoryText() { return colors[26]; }
        public Color getSectionHeader() { return colors[27]; }
        public Color getCardEnabled() { return colors[28]; }
        public Color getCardHiddenEnabled() { return colors[29]; }
        public Color getSearchBg() { return colors[30]; }
        public Color getSelCatBg() { return colors[31]; }
        public Color getTitleMain() { return colors[32]; }
        public Color getTitleSub() { return colors[33]; }
        public Color getSearchPlaceholder() { return colors[34]; }
        public Color getHpBarTrack() { return colors[35]; }
        public Color getSettingName() { return colors[36]; }
        public Color getValueText() { return colors[37]; }
        public Color getPickerBg() { return colors[38]; }
        public Color getCircleFill() { return colors[39]; }
        public Color getModeBgNormal() { return colors[40]; }
        public Color getModeBgHover() { return colors[41]; }
        public Color getModeItemSelected() { return colors[42]; }
        public Color getModeItemHover() { return colors[43]; }
        public Color getModeTextNormal() { return colors[44]; }
        public Color getTipBg() { return colors[45]; }
        public Color getTipText() { return colors[46]; }
        public Color getBindActive() { return colors[47]; }
        public Color getHideActive() { return colors[48]; }
        public Color getBindActiveBg() { return colors[49]; }
        public Color getHideActiveBg() { return colors[50]; }
        public Color getTextNormal() { return colors[51]; }
        public Color getTextHide() { return colors[52]; }
        public Color getBindActiveText() { return colors[53]; }
        public Color getHideActiveText() { return colors[54]; }
        public Color getConfigNameInactive() { return colors[55]; }
    }
    
    private GUISize guiSize = GUISize.SIZE_100;
    private Theme currentTheme = Theme.MIDNIGHT_BLACK;
    private Theme targetTheme = Theme.MIDNIGHT_BLACK;

    private int backgroundAlpha = 255;
    private boolean blurArea = false;
    
    private float themeTransitionProgress = 1.0f;
    private static final int COLOR_COUNT = 56;
    private float[] currentColors = new float[COLOR_COUNT * 3];
    private float[] targetColors = new float[COLOR_COUNT * 3];
    
    public ClientSettings() {
        updateColorArrays(currentTheme, currentColors);
        updateColorArrays(currentTheme, targetColors);
    }
    
    private void updateColorArrays(Theme theme, float[] arr) {
        Color[] c = theme.colors;
        int i = 0;
        for (Color color : c) {
            arr[i++] = color.getRed();
            arr[i++] = color.getGreen();
            arr[i++] = color.getBlue();
        }
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

    private Color c(int idx) {
        int r = idx * 3, g = r + 1, b = r + 2;
        return new Color((int)currentColors[r], (int)currentColors[g], (int)currentColors[b]);
    }

    public Color getBackgroundColor() { return c(0); }
    public Color getSidebarColor() { return c(1); }
    public Color getCardColor() { return c(2); }
    public Color getTextColor() { return c(3); }
    public Color getAccentColor() { return c(4); }
    public Color getAccentHoverColor() { return c(5); }
    public Color getBorderColor() { return c(6); }
    public Color getBorderLightColor() { return c(7); }
    public Color getEntryBgColor() { return c(8); }
    public Color getEntryHoverColor() { return c(9); }
    public Color getEntryActiveColor() { return c(10); }
    public Color getEntryIconColor() { return c(11); }
    public Color getEntryValueColor() { return c(12); }
    public Color getSliderTrackColor() { return c(13); }
    public Color getToggleOffColor() { return c(14); }
    public Color getInputBgColor() { return c(15); }
    public Color getInputBorder_color() { return c(16); }
    public Color getInputPlaceholderColor() { return c(17); }
    public Color getInputTextColor() { return c(18); }
    public Color getButtonPrimaryColor() { return c(19); }
    public Color getButtonDangerColor() { return c(20); }
    public Color getSuccessColor() { return c(21); }
    public Color getErrorColor() { return c(22); }
    public Color getActionBtnNormalColor() { return c(23); }
    public Color getActionBtnHoverColor() { return c(24); }
    public Color getLogoTextColor() { return c(25); }
    public Color getCategoryTextColor() { return c(26); }
    public Color getSectionHeaderColor() { return c(27); }
    public Color getCardEnabledColor() { return c(28); }
    public Color getCardHiddenEnabledColor() { return c(29); }
    public Color getSearchBgColor() { return c(30); }
    public Color getSelCatBgColor() { return c(31); }
    public Color getTitleMainColor() { return c(32); }
    public Color getTitleSubColor() { return c(33); }
    public Color getSearchPlaceholderColor() { return c(34); }
    public Color getHpBarTrackColor() { return c(35); }
    public Color getSettingNameColor() { return c(36); }
    public Color getValueTextColor() { return c(37); }
    public Color getPickerBgColor() { return c(38); }
    public Color getCircleFillColor() { return c(39); }
    public Color getModeBgNormalColor() { return c(40); }
    public Color getModeBgHoverColor() { return c(41); }
    public Color getModeItemSelectedColor() { return c(42); }
    public Color getModeItemHoverColor() { return c(43); }
    public Color getModeTextNormalColor() { return c(44); }
    public Color getTipBgColor() { return c(45); }
    public Color getTipTextColor() { return c(46); }
    public Color getBindActiveColor() { return c(47); }
    public Color getHideActiveColor() { return c(48); }
    public Color getBindActiveBgColor() { return c(49); }
    public Color getHideActiveBgColor() { return c(50); }
    public Color getTextNormalColor() { return c(51); }
    public Color getTextHideColor() { return c(52); }
    public Color getBindActiveTextColor() { return c(53); }
    public Color getHideActiveTextColor() { return c(54); }
    public Color getConfigNameInactiveColor() { return c(55); }

    // Dropdown & Additional UI Colors
    public Color getTextEnabledColor() { return c(3); }  // TextPrimary (light on dark themes)
    public Color getTextDisabledColor() { return c(51); } // TextNormalColor
    public Color getDropdownBgColor() { 
        return new Color(c(0).getRed(), c(0).getGreen(), c(0).getBlue(), 110); 
    }
    public Color getButtonNormalColor() { return c(23); } // ActionBtnNormalColor
    public Color getInputFocusedBgColor() { return c(15); } // InputBgColor
    public Color getInputNormalBgColor() { return c(8); }  // EntryBgColor
    public Color getPanelContentBgColor() {
        return new Color(0, 0, 0, 120);
    }

    public int getBackgroundAlpha() { return backgroundAlpha; }
    public void setBackgroundAlpha(int alpha) {
        this.backgroundAlpha = Math.max(0, Math.min(255, alpha));
    }

    /**
     * 分层级的Alpha计算系统
     * 基于backgroundAlpha为不同UI元素计算不同的透明度
     * 层级越高，元素越不透明（保证可读性和视觉层次）
     *
     * @param baseAlpha 基础动画alpha (0-1)
     * @param level 层级 (0-4)
     *   0 = 背景 (最透明)
     *   1 = 侧边栏/面板容器
     *   2 = 卡片/模块条目
     *   3 = 设置项/按钮
     *   4 = 文字/图标 (最不透明)
     */
    public float getLayeredAlpha(float baseAlpha, int level) {
        float baseBgAlpha = backgroundAlpha / 255f;
        float[] layerFactors = {0.0f, 0.25f, 0.5f, 0.7f, 0.85f};
        float factor = level >= 0 && level < layerFactors.length ? layerFactors[level] : layerFactors[2];
        return baseAlpha * (baseBgAlpha + (1f - baseBgAlpha) * factor);
    }

    public boolean isBlurArea() { return blurArea; }
    public void setBlurArea(boolean blur) { this.blurArea = blur; }
}
