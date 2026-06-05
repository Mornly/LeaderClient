package unfair.ui.clickgui.panel;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.EntityLivingBase;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import unfair.Unfair;
import unfair.event.EventManager;
import unfair.event.EventTarget;
import unfair.events.Render2DEvent;
import unfair.management.ClientSettings;
import unfair.module.Category;
import unfair.module.Module;
import unfair.property.Property;
import unfair.property.properties.*;
import unfair.ui.clickgui.panel.config.ConfigEntry;
import unfair.ui.clickgui.panel.config.CreateConfigEntry;
import unfair.ui.clickgui.panel.config.SettingsEntry;
import unfair.ui.clickgui.panel.settings.BoolSetting;
import unfair.ui.clickgui.panel.settings.ColorSetting;
import unfair.ui.clickgui.panel.settings.ModeSetting;
import unfair.ui.clickgui.panel.settings.SliderSetting;
import unfair.util.RenderUtil;
import unfair.util.shader.BlurUtils;
import unfair.util.shader.RoundedUtils;

import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class PanelGui extends GuiScreen {

    private static final float PANEL_WIDTH = 720;
    private static final float PANEL_HEIGHT = 460;
    private static final float SIDEBAR_WIDTH = 150;
    private static final float RADIUS = 10;
    private static final float CAT_ITEM_H = 20;
    private static final float MODULE_CAT_H = 17;
    private static final float CONFIG_DOWN_OFFSET = 20;
    private static final float CLIENT_TITLE_SPACING = 8;
    private static final float CONFIG_SEL_BG_EXTRA = -2.5f;
    private static final float NONCONFIG_SEL_BG_UP = 1;
    private static final float COL_GAP = 8;

    private Color BG_COLOR = new Color(250, 250, 250);
    private Color LOGO_BOX_COLOR = new Color(70, 130, 180);
    private Color LOGO_TEXT_COLOR = new Color(173, 216, 230);
    private Color CATEGORY_TEXT_COLOR = new Color(120, 135, 155);
    private Color CATEGORY_ACTIVE_COLOR = new Color(35, 45, 60);
    private Color SECTION_HEADER_COLOR = new Color(150, 155, 165);
    private Color CARD_NORMAL = new Color(255, 255, 255);
    private Color CARD_ENABLED = new Color(242, 248, 255);
    private Color CARD_HIDDEN_ENABLED = new Color(245, 240, 250);
    private Color SEARCH_BG = new Color(235, 238, 242);
    private Color SEARCH_FOCUSED_BORDER = new Color(70, 130, 180);
    private Color PLAYER_BG = new Color(240, 243, 248);

    private float displayHealth;
    private boolean healthInitialized;

    private int selectedCategoryIndex = 0;
    private final List<Category> categoryList = new ArrayList<>();
    private final List<ModuleEntry> moduleEntries = new ArrayList<>();
    private final List<ModuleEntry> filteredEntries = new ArrayList<>();
    private final List<ConfigEntry> configEntries = new ArrayList<>();
    private final List<ConfigEntry> filteredConfigs = new ArrayList<>();
    private final List<SettingsEntry> settingsEntries = new ArrayList<>();
    private CreateConfigEntry createConfigEntry;
    private boolean configListDirty = true;

    private final List<ModuleEntry> allModuleEntries = new ArrayList<>();
    private int preSearchCategoryIndex = 0;
    private float categoryTitleAlpha = 1f;

    private float scrollY = 0;
    private float targetScrollY = 0;
    private float maxScrollY = 0;

    private long lastRenderTime = 0;
    private float catSelectAnimY = 0;
    private float catSelectTargetY = 0;
    private float contentAlpha = 1f;
    private boolean categoryChanging = false;
    private boolean categoryFadingIn = false;
    private float categoryChangeTimer = 0f;
    private int prevSelectedCategoryIndex;

    private String searchText = "";
    private boolean searchFocused = false;
    private int searchCursorPos = 0;
    private int searchSelectionStart = -1;
    private float searchAlpha = 0f;
    private float searchWidthAnim = 0f;
    private boolean searchActive = false;
    private float filterAlpha = 1f;

    private boolean backspaceHeld = false;
    private float backspaceRepeatTimer = 0;
    private static final float BACKSPACE_INITIAL_DELAY = 0.4f;
    private static final float BACKSPACE_REPEAT_RATE = 0.05f;

    private float textAnimProgress = 1f;
    private int lastTextLength = 0;

    private float guiAlpha = 0f;
    private boolean closing = false;
    private float renderDt = 0.016f;
    private static final float GUI_ANIM_SPEED = 0.12f;

    public PanelGui() {
        EventManager.register(this);
        Collections.addAll(categoryList, Category.values());
    }

    @Override
    public void initGui() {
        rebuildAllModules();
        rebuildModules();
        refreshConfigs();
        refreshSettings();
        createConfigEntry = new CreateConfigEntry();
        lastRenderTime = System.currentTimeMillis();
        guiAlpha = 0f;
        closing = false;
    }

    @EventTarget
    public void onRender2D(Render2DEvent event) {
        long now = System.currentTimeMillis();
        float dt = Math.min((now - lastRenderTime) / 1000f, 0.1f);
        lastRenderTime = now;
        renderDt = dt;

        updateAnimations(dt);

        float scale = ClientSettings.INSTANCE.getGUIScale();
        float scaledWidth = PANEL_WIDTH * scale;
        float scaledHeight = PANEL_HEIGHT * scale;
        
        float x = (this.width - scaledWidth) / 2f;
        float y = (this.height - scaledHeight) / 2f;

        int bgAlpha = ClientSettings.INSTANCE.getBackgroundAlpha();
        boolean useBlur = ClientSettings.INSTANCE.isBlurArea();

        GlStateManager.enableBlend();

        if (useBlur) {
            BlurUtils.prepareBlur();
            RoundedUtils.drawRound(x, y, scaledWidth, scaledHeight, RADIUS * scale,
                    new Color(BG_COLOR.getRed(), BG_COLOR.getGreen(), BG_COLOR.getBlue(),
                            (int)(guiAlpha * 255)).getRGB());
            BlurUtils.blurEnd(2, 8f);
        }

        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, 0);
        GlStateManager.scale(scale, scale, 1f);
        GlStateManager.translate(-x, -y, 0);
        
        GlStateManager.color(1F, 1F, 1F, guiAlpha);

        Color bgWithAlpha = blendAlpha(BG_COLOR, guiAlpha * bgAlpha / 255f);
        RoundedUtils.drawRound(x, y, PANEL_WIDTH, PANEL_HEIGHT, RADIUS, bgWithAlpha);

        drawLogo(x, y);
        drawCategories(x, y);
        drawPlayerInfo(x, y);

        float contentX = x + SIDEBAR_WIDTH + 8;
        float contentTop = y + 10;

        drawSearchBar(contentX, contentTop);
        drawCategoryTitle(contentX, contentTop);
        drawModuleArea(x, y, contentX, contentTop);

        GL11.glDisable(GL11.GL_SCISSOR_TEST);
        
        GlStateManager.popMatrix();

        GlStateManager.disableBlend();
    }

    private void updateAnimations(float dt) {
        ClientSettings.INSTANCE.update(dt);
        updateThemeColors();
        
        if (closing) {
            guiAlpha = PanelValueItem.lerp(guiAlpha, 0f, GUI_ANIM_SPEED, dt);
            if (guiAlpha < 0.01f) {
                mc.displayGuiScreen(null);
                return;
            }
        } else {
            guiAlpha = PanelValueItem.lerp(guiAlpha, 1f, GUI_ANIM_SPEED, dt);
        }

        scrollY = PanelValueItem.lerp(scrollY, targetScrollY, 0.14f, dt);
        if (Math.abs(scrollY - targetScrollY) < 0.5f) scrollY = targetScrollY;

        catSelectAnimY = PanelValueItem.lerp(catSelectAnimY, catSelectTargetY, 0.18f, dt);

        if (categoryChanging) {
            categoryChangeTimer += dt;
            contentAlpha = PanelValueItem.lerp(contentAlpha, 0f, 0.14f, dt);
            if (contentAlpha < 0.03f && categoryChangeTimer > 0.15f) {
                rebuildModules();
                if (categoryList.get(selectedCategoryIndex) == Category.CONFIG || categoryList.get(selectedCategoryIndex) == Category.SETTINGS) {
                    resetConfigAnimations();
                }
                categoryChanging = false;
                categoryFadingIn = true;
                categoryChangeTimer = 0f;
                contentAlpha = 0f;
            }
        } else if (categoryFadingIn) {
            contentAlpha = PanelValueItem.lerp(contentAlpha, 1f, 0.11f, dt);
            if (contentAlpha > 0.98f) {
                contentAlpha = 1f;
                categoryFadingIn = false;
            }
        } else {
            contentAlpha = PanelValueItem.lerp(contentAlpha, 1f, 0.11f, dt);
        }

        contentAlpha *= guiAlpha;

        if (searchFocused || searchActive) {
            searchAlpha = PanelValueItem.lerp(searchAlpha, 1f, 0.15f, dt);
            searchWidthAnim = PanelValueItem.lerp(searchWidthAnim, 1f, 0.15f, dt);
            if (!searchText.isEmpty() && categoryList.get(selectedCategoryIndex) != Category.CONFIG && categoryList.get(selectedCategoryIndex) != Category.SETTINGS) {
                categoryTitleAlpha = PanelValueItem.lerp(categoryTitleAlpha, 0f, 0.15f, dt);
            } else {
                categoryTitleAlpha = PanelValueItem.lerp(categoryTitleAlpha, 1f, 0.15f, dt);
            }
        } else {
            searchAlpha = PanelValueItem.lerp(searchAlpha, 0f, 0.15f, dt);
            searchWidthAnim = PanelValueItem.lerp(searchWidthAnim, 0f, 0.15f, dt);
            categoryTitleAlpha = PanelValueItem.lerp(categoryTitleAlpha, 1f, 0.15f, dt);
        }

        searchAlpha *= guiAlpha;

        filterAlpha = PanelValueItem.lerp(filterAlpha, (searchText.isEmpty() ? 1f : 0.3f), 0.12f, dt);

        checkBackspaceReleased();

        if (backspaceHeld && searchFocused) {
            backspaceRepeatTimer += dt;
            if (backspaceRepeatTimer >= BACKSPACE_INITIAL_DELAY) {
                float repeatTime = backspaceRepeatTimer - BACKSPACE_INITIAL_DELAY;
                int deleteCount = (int)(repeatTime / BACKSPACE_REPEAT_RATE);
                if (deleteCount > 0) {
                    for (int i = 0; i < deleteCount && !searchText.isEmpty(); i++) {
                        if (searchSelectionStart >= 0) {
                            int selMin = Math.min(searchSelectionStart, searchCursorPos);
                            int selMax = Math.max(searchSelectionStart, searchCursorPos);
                            searchText = searchText.substring(0, selMin) + searchText.substring(selMax);
                            searchCursorPos = selMin;
                            searchSelectionStart = -1;
                            applyFilter();
                        } else if (searchCursorPos > 0) {
                            searchText = searchText.substring(0, searchCursorPos - 1) + searchText.substring(searchCursorPos);
                            searchCursorPos--;
                            applyFilter();
                        }
                    }
                    backspaceRepeatTimer = BACKSPACE_INITIAL_DELAY + (repeatTime % BACKSPACE_REPEAT_RATE);
                }
            }
        }

        if (searchText.length() != lastTextLength) {
            textAnimProgress = 0f;
            lastTextLength = searchText.length();
        }
        textAnimProgress = PanelValueItem.lerp(textAnimProgress, 1f, 0.25f, dt);

        for (ModuleEntry entry : moduleEntries) {
            entry.toggleAnim = PanelValueItem.lerp(entry.toggleAnim, entry.mod.isEnabled() ? 1f : 0f, 0.15f, dt);
            entry.hideAnim = PanelValueItem.lerp(entry.hideAnim, entry.mod.isHidden() ? 1f : 0f, 0.15f, dt);
            entry.actionButtons.update(dt);
            for (PanelValueItem s : entry.settings) s.update(dt);
        }

        for (ModuleEntry entry : allModuleEntries) {
            if (!moduleEntries.contains(entry)) {
                entry.toggleAnim = PanelValueItem.lerp(entry.toggleAnim, entry.mod.isEnabled() ? 1f : 0f, 0.15f, dt);
                entry.hideAnim = PanelValueItem.lerp(entry.hideAnim, entry.mod.isHidden() ? 1f : 0f, 0.15f, dt);
                entry.actionButtons.update(dt);
                for (PanelValueItem s : entry.settings) s.update(dt);
            }
        }
    }

    private void drawLogo(float x, float y) {
        float logoSize = 32;
        float logoX = x + 16;
        float logoY = y + 14;
        RoundedUtils.drawRound(logoX, logoY, logoSize, logoSize, 7, blendAlpha(LOGO_BOX_COLOR, guiAlpha));

        String logoText = "L";
        int logoFontSize = 22;
        float lw = Unfair.fontManager.getFont(logoFontSize).getStringWidth(logoText);
        Unfair.fontManager.getFont(logoFontSize).drawString(logoText,
                logoX + (logoSize - lw) / 2f, logoY + (logoSize - logoFontSize) / 2f + 7,
                blendAlpha(LOGO_TEXT_COLOR, guiAlpha).getRGB(), false);

        float nameX = logoX + logoSize + 10;
        Unfair.fontManager.getFont(20).drawString("Leader", nameX, logoY + 4, blendAlpha(new Color(35, 45, 60), guiAlpha).getRGB(), false);
        Unfair.fontManager.getFont(12).drawString("for Minecraft", nameX, logoY + 24, blendAlpha(new Color(140, 145, 155), guiAlpha).getRGB(), false);
    }

    private void drawCategories(float x, float y) {
        float headerY = y + 70;

        Unfair.fontManager.getFont(13).drawString("Modules", x + 16, headerY,
                blendAlpha(SECTION_HEADER_COLOR, guiAlpha).getRGB(), false);

        float catY = headerY + 8;

        for (Category category : categoryList) {
            if (category == Category.CONFIG || category == Category.SETTINGS) {
                break;
            }
        }

        catSelectTargetY = catY;
        for (int i = 0; i < selectedCategoryIndex; i++) {
            boolean isConfig = categoryList.get(i) == Category.CONFIG;
            if (isConfig) {
                catSelectTargetY += CONFIG_DOWN_OFFSET + CLIENT_TITLE_SPACING;
            }
            catSelectTargetY += (isConfig || categoryList.get(i) == Category.SETTINGS) ? CAT_ITEM_H : MODULE_CAT_H;
        }
        if (categoryList.get(selectedCategoryIndex) == Category.CONFIG) {
            catSelectTargetY += CONFIG_DOWN_OFFSET + CLIENT_TITLE_SPACING;
        }
        if (!Float.isFinite(catSelectAnimY)) catSelectAnimY = catSelectTargetY;

        float selBgW = SIDEBAR_WIDTH - 20;
        float selBgX = x + 10;
        boolean selIsConfig = categoryList.get(selectedCategoryIndex) == Category.CONFIG;
        float selItemH = selIsConfig ? CAT_ITEM_H : MODULE_CAT_H;
        float selBgH = (CAT_ITEM_H - 2) / 2f;
        float selBgActualY;
        if (selIsConfig) {
            selBgActualY = catSelectAnimY + (selItemH - selBgH) / 2f + CONFIG_SEL_BG_EXTRA;
        } else {
            selBgActualY = catSelectAnimY + (selItemH - selBgH) / 2f - NONCONFIG_SEL_BG_UP;
        }
        RoundedUtils.drawRound(selBgX, selBgActualY, selBgW, selBgH, 5, blendAlpha(new Color(225, 232, 245), guiAlpha));

        for (int i = 0; i < categoryList.size(); i++) {
            boolean isConfig = categoryList.get(i) == Category.CONFIG;
            boolean isClientCat = categoryList.get(i) == Category.CONFIG || categoryList.get(i) == Category.SETTINGS;

            if (isConfig) {
                catY += CONFIG_DOWN_OFFSET;
                Unfair.fontManager.getFont(13).drawString("Client", x + 16, catY,
                        blendAlpha(SECTION_HEADER_COLOR, guiAlpha).getRGB(), false);
                catY += CLIENT_TITLE_SPACING;
            }

            String catName = categoryList.get(i).getDisplayName();
            int textColor = (i == selectedCategoryIndex) ? blendAlpha(CATEGORY_ACTIVE_COLOR, guiAlpha).getRGB() : blendAlpha(CATEGORY_TEXT_COLOR, guiAlpha).getRGB();

            float textOffsetY = !isClientCat ? 3.5f : 4f;
            Unfair.fontManager.getFont(15).drawString(catName, x + 16, catY + textOffsetY, textColor, false);
            catY += isClientCat ? CAT_ITEM_H : MODULE_CAT_H;
        }
    }

    private void drawSearchBar(float contentX, float contentTop) {
        float baseW = 160;
        float expandedW = 220;
        float currentW = baseW + (expandedW - baseW) * searchWidthAnim;
        float searchH = 28;

        float bgAlpha = (0.3f + 0.7f * searchWidthAnim) * guiAlpha;
        Color bgColor = blendAlpha(SEARCH_BG, bgAlpha);

        if (searchFocused) {
            RoundedUtils.drawRoundedRectRise(contentX, contentTop, currentW, searchH, 5,
                    bgColor.getRGB(), false, false, true, true);
        } else {
            RoundedUtils.drawRound(contentX, contentTop, currentW, searchH, 5, bgColor);
        }

        if (searchAlpha > 0.05f && guiAlpha > 0.05f) {
            Color borderColor = blendAlpha(SEARCH_FOCUSED_BORDER, searchAlpha * 0.6f);
            RenderUtil.drawRect(contentX, contentTop + searchH - 1, contentX + currentW, contentTop + searchH, borderColor.getRGB());
        }

        if (searchText.isEmpty() && !searchFocused) {
            float placeholderAlpha = (0.5f + 0.5f * (1f - searchWidthAnim)) * guiAlpha;
            float placeholderOffset = (float) Math.sin(System.currentTimeMillis() / 1000.0 * Math.PI * 2) * 0.5f;
            Unfair.fontManager.getFont(13).drawString("Search...", contentX + 10, contentTop + 8 + placeholderOffset,
                    blendAlpha(new Color(170, 175, 185), placeholderAlpha).getRGB(), false);
        } else {
            if (searchSelectionStart >= 0 && searchSelectionStart != searchCursorPos) {
                int selMin = Math.min(searchSelectionStart, searchCursorPos);
                int selMax = Math.max(searchSelectionStart, searchCursorPos);
                String beforeSel = searchText.substring(0, selMin);
                float selX = contentX + 10 + Unfair.fontManager.getFont(13).getStringWidth(beforeSel);
                float selW = Unfair.fontManager.getFont(13).getStringWidth(searchText.substring(selMin, selMax));
                float selectionPulse = (float) Math.sin(System.currentTimeMillis() / 300.0 * Math.PI * 2) * 0.1f + 0.3f;
                RenderUtil.drawRect(selX, contentTop + 7, selX + selW, contentTop + 21,
                        blendAlpha(new Color(70, 130, 180), selectionPulse * guiAlpha).getRGB());
            }

            float textScale = 1f + (1f - textAnimProgress) * 0.05f;
            float textAlpha = (0.7f + 0.3f * textAnimProgress) * guiAlpha;

            GlStateManager.pushMatrix();
            float textCenterX = contentX + 10 + Unfair.fontManager.getFont(13).getStringWidth(searchText) / 2f;
            float textCenterY = contentTop + 8 + 6;
            GlStateManager.translate(textCenterX, textCenterY, 0);
            GlStateManager.scale(textScale, textScale, 1f);
            GlStateManager.translate(-textCenterX, -textCenterY, 0);

            Unfair.fontManager.getFont(13).drawString(searchText, contentX + 10, contentTop + 8,
                    blendAlpha(new Color(40, 50, 65), searchAlpha * textAlpha).getRGB(), false);

            GlStateManager.popMatrix();

            if (searchFocused) {
                String beforeCursor = searchText.substring(0, searchCursorPos);
                float cursorX = contentX + 10 + Unfair.fontManager.getFont(13).getStringWidth(beforeCursor);

                long cursorTime = System.currentTimeMillis();
                boolean cursorVisible = (cursorTime % 1000 < 500) || (cursorTime % 120 < 60 && textAnimProgress < 0.9f);

                if (cursorVisible) {
                    float cursorAlpha = textAnimProgress < 0.9f ? 1f : (float)(Math.sin(cursorTime / 200.0 * Math.PI) * 0.3f + 0.7f);
                    RenderUtil.drawRect(cursorX, contentTop + 8, cursorX + 1, contentTop + 21,
                            blendAlpha(new Color(70, 130, 180), searchAlpha * cursorAlpha).getRGB());

                    if (textAnimProgress < 0.95f) {
                        float glowW = 2f + (1f - textAnimProgress) * 3f;
                        float glowAlpha = (1f - textAnimProgress) * 0.3f * guiAlpha;
                        RenderUtil.drawRect(cursorX - glowW/2, contentTop + 8,
                                cursorX + glowW/2, contentTop + 21,
                                blendAlpha(new Color(70, 130, 180), glowAlpha).getRGB());
                    }
                }
            }
        }
    }

    private void drawCategoryTitle(float contentX, float contentTop) {
        int renderCatIdx = categoryChanging ? prevSelectedCategoryIndex : selectedCategoryIndex;
        Category selectedCat = categoryList.get(renderCatIdx);
        if (selectedCat == Category.CONFIG || selectedCat == Category.SETTINGS) {
            String categoryName = selectedCat.getDisplayName();
            Unfair.fontManager.getFont(18).drawString(categoryName, contentX, contentTop + 38,
                    blendAlpha(new Color(35, 45, 60), contentAlpha * categoryTitleAlpha).getRGB(), false);
        } else {
            if (searchActive && !searchText.isEmpty()) {
                String searchResultText = "Search Results";
                Unfair.fontManager.getFont(18).drawString(searchResultText, contentX, contentTop + 38,
                        blendAlpha(new Color(35, 45, 60), contentAlpha).getRGB(), false);
            } else {
                String categoryName = selectedCat.getDisplayName();
                Unfair.fontManager.getFont(18).drawString(categoryName, contentX, contentTop + 38,
                        blendAlpha(new Color(35, 45, 60), contentAlpha * categoryTitleAlpha).getRGB(), false);
            }
        }
    }

    private void drawModuleArea(float panelX, float panelY, float contentX, float contentTop) {
        float searchH = 28;
        float categoryTitleH = 24;
        float moduleAreaTop = contentTop + searchH + categoryTitleH + 10;
        float moduleAreaBottom = panelY + PANEL_HEIGHT - 12;
        float moduleAreaHeight = moduleAreaBottom - moduleAreaTop;
        float contentRight = panelX + PANEL_WIDTH - 8;
        float contentWidth = contentRight - contentX;

        int renderCategoryIndex = categoryChanging ? prevSelectedCategoryIndex : selectedCategoryIndex;

        if (categoryList.get(renderCategoryIndex) == Category.CONFIG) {
            drawConfigArea(contentX, moduleAreaTop, contentWidth, moduleAreaHeight);
            return;
        }
        
        if (categoryList.get(renderCategoryIndex) == Category.SETTINGS) {
            drawSettingsArea(contentX, moduleAreaTop, contentWidth, moduleAreaHeight);
            return;
        }

        float colWidth = (contentWidth - COL_GAP) / 2f;
        float col2X = contentX + colWidth + COL_GAP;

        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        applyScissor(contentX, moduleAreaTop, contentWidth, moduleAreaHeight);

        List<ModuleEntry> visible = searchActive && !searchText.isEmpty() ? filteredEntries : moduleEntries;

        float leftY = moduleAreaTop - scrollY;
        float rightY = moduleAreaTop - scrollY;

        for (ModuleEntry entry : visible) {
            float ex = entry.col == 0 ? contentX : col2X;
            float ey = entry.col == 0 ? leftY : rightY;
            entry.x = ex;
            entry.width = colWidth;

            float cardH = entry.getTotalHeight();

            float entryAlpha = contentAlpha;
            Color baseEnabledColor = blendColor(CARD_ENABLED, CARD_HIDDEN_ENABLED, entry.hideAnim);
            Color cardColor = blendColor(
                    blendAlpha(CARD_NORMAL, entryAlpha),
                    blendAlpha(baseEnabledColor, entryAlpha),
                    entry.toggleAnim
            );
            RoundedUtils.drawRound(ex, ey, colWidth, cardH, 5, cardColor);

            GlStateManager.color(1F, 1F, 1F, entryAlpha);

            Module mod = entry.mod;
            int nameColor = blendAlpha(mod.isEnabled() ? CATEGORY_ACTIVE_COLOR : CATEGORY_TEXT_COLOR, entryAlpha).getRGB();
            Unfair.fontManager.getFont(14).drawString(mod.getName(), ex + 12, ey + 6, nameColor, false);

            float actionBtnWidth = (29 + 3) * 2 - 3;
            float actionBtnX = ex + colWidth - actionBtnWidth - 40;
            float actionBtnY = ey + 2 + 1.5f;
            
            int rawMX = (int)(Mouse.getX() * this.width / (double)this.mc.displayWidth);
            int rawMY = (int)(this.height - Mouse.getY() * this.height / (double)this.mc.displayHeight - 1);
            int[] transformed = transformMouseCoords(rawMX, rawMY);
            int mx = transformed[0];
            int mY = transformed[1];
            
            entry.actionButtons.x = actionBtnX;
            entry.actionButtons.y = actionBtnY;
            entry.actionButtons.width = actionBtnWidth;
            entry.actionButtons.alpha = entryAlpha;
            entry.actionButtons.update(renderDt);
            entry.actionButtons.render(mx, mY);

            float toggleW = 26;
            float toggleH = 12;
            float toggleX = ex + colWidth - toggleW - 12;
            float toggleY = ey + 5;

            Color trackColor = blendColor(new Color(200, 204, 210), new Color(70, 130, 180), entry.toggleAnim);
            RoundedUtils.drawRound(toggleX, toggleY, toggleW, toggleH, toggleH / 2f, blendAlpha(trackColor, entryAlpha));

            float dotSize = toggleH - 4;
            float dotX = toggleX + 2 + entry.toggleAnim * (toggleW - dotSize - 4);
            float dotY = toggleY + 2;
            Color dotColor = blendAlpha(Color.WHITE, entryAlpha);
            RoundedUtils.drawRound(dotX, dotY, dotSize, dotSize, dotSize / 2f, dotColor);

            float settingY = ey + 24;
            
            for (PanelValueItem setting : entry.settings) {
                if (!setting.visible()) continue;
                setting.x = ex + 12;
                setting.y = settingY;
                setting.width = colWidth - 24;
                setting.alpha = entryAlpha;

                if (setting instanceof ModeSetting) {
                    ModeSetting modeSetting = (ModeSetting) setting;
                    modeSetting.panelScreenX = panelX;
                    modeSetting.panelScreenY = panelY;
                    float setH = setting.getHeight();
                    boolean modeHovering = mx >= ex + 12 && mx <= ex + colWidth - 12 &&
                                           mY >= settingY && mY <= settingY + setH;
                    modeSetting.setHovering(modeHovering);
                    setting.render(mx, mY);
                } else if (setting instanceof ColorSetting) {
                    setting.render(mx, mY);
                } else {
                    setting.render(mx, mY);
                }
                settingY += setting.getHeight() + 2;
            }

            GlStateManager.color(1F, 1F, 1F, 1F);

            if (entry.col == 0) leftY += cardH + 6;
            else rightY += cardH + 6;
        }

        maxScrollY = calculateMaxScroll(moduleAreaHeight, visible);
        if (targetScrollY < 0) targetScrollY = 0;
        if (targetScrollY > maxScrollY) targetScrollY = maxScrollY;
    }

    private void drawConfigArea(float contentX, float areaTop, float contentWidth, float areaHeight) {
        if (createConfigEntry.justCreatedConfig()) {
            configListDirty = true;
        }
        
        if (configListDirty) {
            refreshConfigs();
        }

        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        applyScissor(contentX, areaTop, contentWidth, areaHeight);

        int mx = (int)(Mouse.getX() * this.width / (double)this.mc.displayWidth);
        int mY = (int)(this.height - Mouse.getY() * this.height / (double)this.mc.displayHeight - 1);
        
        int[] transformed = transformMouseCoords(mx, mY);
        int logicMX = transformed[0];
        int logicMY = transformed[1];

        List<ConfigEntry> visible = searchActive && !searchText.isEmpty() ? filteredConfigs : configEntries;

        float currentY = areaTop - scrollY;
        float cardW = (contentWidth - COL_GAP);
        float cardX = contentX + COL_GAP / 2f;

        createConfigEntry.x = cardX;
        createConfigEntry.y = currentY;
        createConfigEntry.width = cardW;
        createConfigEntry.alpha = contentAlpha;
        createConfigEntry.update(renderDt);
        createConfigEntry.render(logicMX, logicMY);
        currentY += createConfigEntry.getHeight() + 8;

        for (ConfigEntry entry : visible) {
            entry.x = cardX;
            entry.y = currentY;
            entry.width = cardW;
            entry.alpha = contentAlpha;
            entry.update(renderDt);
            entry.render(logicMX, logicMY);

            currentY += entry.getHeight() + 6;
        }

        GL11.glDisable(GL11.GL_SCISSOR_TEST);

        float totalHeight = createConfigEntry.getHeight() + 8;
        for (ConfigEntry entry : visible) {
            totalHeight += entry.getHeight() + 6;
        }
        
        maxScrollY = Math.max(totalHeight - areaHeight, 0);
        if (targetScrollY < 0) targetScrollY = 0;
        if (targetScrollY > maxScrollY) targetScrollY = maxScrollY;
    }
    
    private void drawSettingsArea(float contentX, float areaTop, float contentWidth, float areaHeight) {
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        applyScissor(contentX, areaTop, contentWidth, areaHeight);

        int mx = (int)(Mouse.getX() * this.width / (double)this.mc.displayWidth);
        int mY = (int)(this.height - Mouse.getY() * this.height / (double)this.mc.displayHeight - 1);
        
        int[] transformed = transformMouseCoords(mx, mY);
        int logicMX = transformed[0];
        int logicMY = transformed[1];

        float colWidth = (contentWidth - COL_GAP) / 2f;
        float col1X = contentX;
        float col2X = contentX + colWidth + COL_GAP;

        float leftY = areaTop - scrollY;
        float rightY = areaTop - scrollY;

        for (int i = 0; i < settingsEntries.size(); i++) {
            SettingsEntry entry = settingsEntries.get(i);
            float cardX = (i % 2 == 0) ? col1X : col2X;
            float cardY = (i % 2 == 0) ? leftY : rightY;

            entry.x = cardX;
            entry.y = cardY;
            entry.width = colWidth;
            entry.alpha = contentAlpha;
            entry.update(renderDt);
            entry.render(logicMX, logicMY);

            if (i % 2 == 0) {
                leftY += entry.getHeight() + 6;
            } else {
                rightY += entry.getHeight() + 6;
            }
        }

        GL11.glDisable(GL11.GL_SCISSOR_TEST);

        float leftTotalHeight = 0;
        float rightTotalHeight = 0;
        for (int i = 0; i < settingsEntries.size(); i++) {
            if (i % 2 == 0) {
                leftTotalHeight += settingsEntries.get(i).getHeight() + 6;
            } else {
                rightTotalHeight += settingsEntries.get(i).getHeight() + 6;
            }
        }
        
        float totalHeight = Math.max(leftTotalHeight, rightTotalHeight);
        maxScrollY = Math.max(totalHeight - areaHeight, 0);
        if (targetScrollY < 0) targetScrollY = 0;
        if (targetScrollY > maxScrollY) targetScrollY = maxScrollY;
    }
    
    private void drawPlayerInfo(float panelX, float panelY) {
        float moduleAreaBottom = panelY + PANEL_HEIGHT - 8;
        float infoX = panelX + 10;
        float infoY = moduleAreaBottom - 44;
        float infoW = SIDEBAR_WIDTH - 20;
        float infoH = 40;

        RoundedUtils.drawRound(infoX, infoY, infoW, infoH, 5, blendAlpha(PLAYER_BG, guiAlpha));

        EntityLivingBase player = mc.thePlayer;
        if (player == null) return;

        String playerName = player.getName();
        float nameX = infoX + 10;
        float textY = infoY + 8;
        Unfair.fontManager.getFont(13).drawString(playerName, nameX, textY, blendAlpha(CATEGORY_ACTIVE_COLOR, guiAlpha).getRGB(), false);

        float health = player.getHealth();
        float maxHealth = player.getMaxHealth();

        if (!healthInitialized) {
            displayHealth = health;
            healthInitialized = true;
        }
        displayHealth = PanelValueItem.lerp(displayHealth, health, 0.12f, renderDt);

        float hpBarY = textY + 18;
        float hpBarW = infoW - 20;
        float hpBarH = 3;
        RenderUtil.drawRect(nameX, hpBarY, nameX + hpBarW, hpBarY + hpBarH, blendAlpha(new Color(210, 212, 216), guiAlpha).getRGB());
        if (maxHealth > 0 && displayHealth > 0) {
            float fillW = hpBarW * Math.min(displayHealth / maxHealth, 1f);
            if (fillW > 0) {
                RoundedUtils.drawRound(nameX, hpBarY, fillW, hpBarH, 1.5f, blendAlpha(LOGO_BOX_COLOR, guiAlpha));
            }
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if (closing) return;

        int[] transformed = transformMouseCoords(mouseX, mouseY);
        mouseX = transformed[0];
        mouseY = transformed[1];

        float scale = ClientSettings.INSTANCE.getGUIScale();
        float scaledWidth = PANEL_WIDTH * scale;
        float scaledHeight = PANEL_HEIGHT * scale;
        float x = (this.width - scaledWidth) / 2f;
        float y = (this.height - scaledHeight) / 2f;

        float contentX = x + SIDEBAR_WIDTH + 8;
        float contentTop = y + 10;
        float searchW = 160 + 60 * searchWidthAnim;
        float searchH = 28;

        if (mouseX >= contentX && mouseX <= contentX + searchW &&
                mouseY >= contentTop && mouseY <= contentTop + searchH) {
            if (!searchActive) {
                preSearchCategoryIndex = selectedCategoryIndex;
            }
            searchFocused = true;
            searchActive = true;
            return;
        }

        float headerY = y + 70 + 8;

        for (Category category : categoryList) {
            if (category == Category.CONFIG || category == Category.SETTINGS) {
                break;
            }
        }

        float catClickY = headerY;
        for (int i = 0; i < categoryList.size(); i++) {
            boolean isConfig = categoryList.get(i) == Category.CONFIG;
            boolean isClientCat = categoryList.get(i) == Category.CONFIG || categoryList.get(i) == Category.SETTINGS;
            if (isConfig) {
                catClickY += CONFIG_DOWN_OFFSET + CLIENT_TITLE_SPACING;
            }
            float itemH = isClientCat ? CAT_ITEM_H : MODULE_CAT_H;

            if (mouseX >= x + 10 && mouseX <= x + SIDEBAR_WIDTH - 10 &&
                    mouseY >= catClickY && mouseY <= catClickY + itemH) {
                if (searchActive || searchFocused) {
                    searchFocused = false;
                    searchActive = false;
                    searchText = "";
                    searchCursorPos = 0;
                    searchSelectionStart = -1;
                }
                if (i != selectedCategoryIndex) {
                    closeAllColorPickers();
                    prevSelectedCategoryIndex = selectedCategoryIndex;
                    selectedCategoryIndex = i;
                    categoryChanging = true;
                    categoryFadingIn = false;
                    categoryChangeTimer = 0f;
                }
                return;
            }

            catClickY += itemH;
        }

        float moduleAreaTop = contentTop + 62;
        float contentRight = x + PANEL_WIDTH - 8;
        float contentWidth = contentRight - contentX;

        if (categoryList.get(selectedCategoryIndex) == Category.CONFIG) {
            handleConfigClicks(mouseX, mouseY, mouseButton);
        } else if (categoryList.get(selectedCategoryIndex) == Category.SETTINGS) {
            handleSettingsClicks(mouseX, mouseY, mouseButton);
        } else {
            float colWidth = (contentWidth - COL_GAP) / 2f;
            float col2X = contentX + colWidth + COL_GAP;

            List<ModuleEntry> visible = searchActive && !searchText.isEmpty() ? filteredEntries : moduleEntries;
            float leftClickY = moduleAreaTop - scrollY;
            float rightClickY = moduleAreaTop - scrollY;
            for (ModuleEntry entry : visible) {
                float ex = entry.col == 0 ? contentX : col2X;
                float clickY = entry.col == 0 ? leftClickY : rightClickY;
                float cardH = entry.getTotalHeight();
                if (mouseX >= ex && mouseX <= ex + colWidth && mouseY >= clickY && mouseY <= clickY + cardH) {
                    if (mouseButton == 0) {
                        float actionBtnWidth = (29 + 3) * 2 - 3;
                        float actionBtnX = ex + colWidth - actionBtnWidth - 40;
                        float actionBtnY = clickY + 2 + 1.5f;
                        
                        if (mouseX >= actionBtnX && mouseX <= actionBtnX + actionBtnWidth &&
                            mouseY >= actionBtnY && mouseY <= actionBtnY + 15) {
                            entry.actionButtons.mouseClicked(mouseX, mouseY, mouseButton);
                            return;
                        }
                        
                        boolean hitSetting = false;
                        float settingY = clickY + 24;
                        for (PanelValueItem setting : entry.settings) {
                            if (!setting.visible()) continue;
                            if (mouseX >= ex + 12 && mouseX <= ex + colWidth - 12 &&
                                    mouseY >= settingY && mouseY <= settingY + setting.getHeight()) {
                                setting.mouseClicked(mouseX, mouseY, mouseButton);
                                hitSetting = true;
                                break;
                            }
                            settingY += setting.getHeight() + 2;
                        }
                        if (!hitSetting) {
                            entry.mod.toggle();
                        }
                    }
                    return;
                }
                if (entry.col == 0) leftClickY += cardH + 6;
                else rightClickY += cardH + 6;
            }
        }

        if (searchFocused || searchActive) {
            searchFocused = false;
            searchActive = false;
            searchText = "";
            searchCursorPos = 0;
            searchSelectionStart = -1;
            if (categoryList.get(selectedCategoryIndex) != Category.CONFIG && categoryList.get(selectedCategoryIndex) != Category.SETTINGS) {
                selectedCategoryIndex = preSearchCategoryIndex;
                rebuildModules();
            }
            applyFilter();
        }

        closeAllColorPickers();

        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    private void handleConfigClicks(int mouseX, int mouseY, int mouseButton) {
        List<ConfigEntry> visible = searchActive && !searchText.isEmpty() ? filteredConfigs : configEntries;

        createConfigEntry.mouseClicked(mouseX, mouseY, mouseButton);

        for (ConfigEntry entry : visible) {
            if (entry.isHovering(mouseX, mouseY)) {
                entry.mouseClicked(mouseX, mouseY, mouseButton);
                configListDirty = true;
                return;
            }
        }
    }
    
    private void handleSettingsClicks(int mouseX, int mouseY, int mouseButton) {
        for (SettingsEntry entry : settingsEntries) {
            if (entry.isHovering(mouseX, mouseY, entry.x, entry.y, entry.width, entry.getHeight())) {
                entry.mouseClicked(mouseX, mouseY, mouseButton);
                return;
            }
        }
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        int[] transformed = transformMouseCoords(mouseX, mouseY);
        mouseX = transformed[0];
        mouseY = transformed[1];
        
        List<ModuleEntry> visible = searchActive && !searchText.isEmpty() ? filteredEntries : moduleEntries;
        for (ModuleEntry entry : visible) {
            for (PanelValueItem s : entry.settings) s.mouseDragged(mouseX, mouseY, clickedMouseButton);
        }
        
        if (categoryList.get(selectedCategoryIndex) == Category.CONFIG || categoryList.get(selectedCategoryIndex) == Category.SETTINGS) {
            for (SettingsEntry entry : settingsEntries) {
                entry.mouseDragged(mouseX, mouseY, clickedMouseButton);
            }
        }
        
        super.mouseClickMove(mouseX, mouseY, clickedMouseButton, timeSinceLastClick);
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        int[] transformed = transformMouseCoords(mouseX, mouseY);
        mouseX = transformed[0];
        mouseY = transformed[1];
        
        List<ModuleEntry> visible = searchActive && !searchText.isEmpty() ? filteredEntries : moduleEntries;
        for (ModuleEntry entry : visible) {
            for (PanelValueItem s : entry.settings) s.mouseReleased(mouseX, mouseY, state);
        }
        
        if (categoryList.get(selectedCategoryIndex) == Category.CONFIG || categoryList.get(selectedCategoryIndex) == Category.SETTINGS) {
            for (SettingsEntry entry : settingsEntries) {
                entry.mouseReleased(mouseX, mouseY, state);
            }
        }
        
        super.mouseReleased(mouseX, mouseY, state);
    }

    @Override
    public void handleMouseInput() throws IOException {
        int delta = Mouse.getEventDWheel();
        if (delta != 0) {
            int mx = (int)(Mouse.getX() * this.width / (double)this.mc.displayWidth);
            int mY = (int)(this.height - Mouse.getY() * this.height / (double)this.mc.displayHeight - 1);
            
            int[] transformed = transformMouseCoords(mx, mY);
            mx = transformed[0];
            mY = transformed[1];
            
            for (ModuleEntry entry : moduleEntries) {
                for (PanelValueItem setting : entry.settings) {
                    if (setting instanceof ModeSetting) {
                        ModeSetting modeSetting = (ModeSetting) setting;
                        if (modeSetting.isExpanded()) {
                            if (modeSetting.isHoveringExpanded(mx, mY)) {
                                modeSetting.handleScroll(delta);
                                return;
                            } else {
                                modeSetting.setExpanded(false);
                            }
                        }
                    }
                }
            }
            targetScrollY -= delta * 0.3f;
        }
        super.handleMouseInput();
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (closing && keyCode != Keyboard.KEY_ESCAPE) return;

        if (categoryList.get(selectedCategoryIndex) == Category.CONFIG) {
            boolean configHandled = handleConfigKeyInput(typedChar, keyCode);
            if (configHandled) return;
        }

        List<ModuleEntry> allEntries = searchActive && !searchText.isEmpty() ? filteredEntries : moduleEntries;
        for (ModuleEntry entry : allEntries) {
            if (entry.actionButtons.isBinding() && entry.actionButtons.handleKeyInput(keyCode)) {
                return;
            }
        }

        if (searchFocused) {
            if (keyCode == Keyboard.KEY_ESCAPE) {
                deactivateSearch();
                return;
            }
            if (keyCode == Keyboard.KEY_RETURN) {
                searchFocused = false;
                return;
            }
            if (isCtrlKeyDown() && keyCode == Keyboard.KEY_A) {
                searchSelectionStart = 0;
                searchCursorPos = searchText.length();
                return;
            }
            if (isCtrlKeyDown() && keyCode == Keyboard.KEY_C && searchSelectionStart >= 0) {
                int selMin = Math.min(searchSelectionStart, searchCursorPos);
                int selMax = Math.max(searchSelectionStart, searchCursorPos);
                writeClipboard(searchText.substring(selMin, selMax));
                return;
            }
            if (isCtrlKeyDown() && keyCode == Keyboard.KEY_V) {
                try {
                    String clipboard = readClipboard();
                    if (clipboard != null) insertText(clipboard);
                } catch (Exception ignored) {}
                return;
            }
            if (keyCode == Keyboard.KEY_LEFT) {
                if (isCtrlKeyDown()) searchCursorPos = moveWordLeft(searchText, searchCursorPos);
                else searchCursorPos = Math.max(0, searchCursorPos - 1);
                if (!isShiftKeyDown()) searchSelectionStart = -1;
                else if (searchSelectionStart < 0) searchSelectionStart = searchCursorPos;
                return;
            }
            if (keyCode == Keyboard.KEY_RIGHT) {
                if (isCtrlKeyDown()) searchCursorPos = moveWordRight(searchText, searchCursorPos);
                else searchCursorPos = Math.min(searchText.length(), searchCursorPos + 1);
                if (!isShiftKeyDown()) searchSelectionStart = -1;
                else if (searchSelectionStart < 0) searchSelectionStart = searchCursorPos;
                return;
            }
            if (keyCode == Keyboard.KEY_BACK) {
                if (searchSelectionStart >= 0) {
                    int selMin = Math.min(searchSelectionStart, searchCursorPos);
                    int selMax = Math.max(searchSelectionStart, searchCursorPos);
                    searchText = searchText.substring(0, selMin) + searchText.substring(selMax);
                    searchCursorPos = selMin;
                    searchSelectionStart = -1;
                } else if (searchCursorPos > 0) {
                    searchText = searchText.substring(0, searchCursorPos - 1) + searchText.substring(searchCursorPos);
                    searchCursorPos--;
                }
                backspaceHeld = true;
                backspaceRepeatTimer = 0;
                applyFilter();
                return;
            }
            if (keyCode == Keyboard.KEY_DELETE && searchSelectionStart >= 0) {
                int selMin = Math.min(searchSelectionStart, searchCursorPos);
                int selMax = Math.max(searchSelectionStart, searchCursorPos);
                searchText = searchText.substring(0, selMin) + searchText.substring(selMax);
                searchCursorPos = selMin;
                searchSelectionStart = -1;
                applyFilter();
                return;
            }
            if (Character.isISOControl(typedChar)) {
                super.keyTyped(typedChar, keyCode);
                return;
            }
            if (searchSelectionStart >= 0) {
                int selMin = Math.min(searchSelectionStart, searchCursorPos);
                int selMax = Math.max(searchSelectionStart, searchCursorPos);
                searchText = searchText.substring(0, selMin) + typedChar + searchText.substring(selMax);
                searchCursorPos = selMin + 1;
                searchSelectionStart = -1;
            } else {
                searchText = searchText.substring(0, searchCursorPos) + typedChar + searchText.substring(searchCursorPos);
                searchCursorPos++;
            }
            applyFilter();
            return;
        }

        if (isCtrlKeyDown() && keyCode == Keyboard.KEY_F) {
            if (!searchActive) {
                preSearchCategoryIndex = selectedCategoryIndex;
            }
            searchFocused = true;
            searchActive = true;
            return;
        }

        if (keyCode == Keyboard.KEY_ESCAPE) {
            if (!closing) {
                closing = true;
            }
            return;
        }

        super.keyTyped(typedChar, keyCode);
    }

    private void checkBackspaceReleased() {
        if (backspaceHeld && !Keyboard.isKeyDown(Keyboard.KEY_BACK)) {
            backspaceHeld = false;
            backspaceRepeatTimer = 0;
        }
    }

    private void deactivateSearch() {
        searchFocused = false;
        searchActive = false;
        searchText = "";
        searchCursorPos = 0;
        searchSelectionStart = -1;
        filteredEntries.clear();
        if (categoryList.get(selectedCategoryIndex) != Category.CONFIG && categoryList.get(selectedCategoryIndex) != Category.SETTINGS) {
            selectedCategoryIndex = preSearchCategoryIndex;
            rebuildModules();
        }
    }

    private void insertText(String text) {
        if (searchSelectionStart >= 0) {
            int selMin = Math.min(searchSelectionStart, searchCursorPos);
            int selMax = Math.max(searchSelectionStart, searchCursorPos);
            searchText = searchText.substring(0, selMin) + text + searchText.substring(selMax);
            searchCursorPos = selMin + text.length();
            searchSelectionStart = -1;
        } else {
            searchText = searchText.substring(0, searchCursorPos) + text + searchText.substring(searchCursorPos);
            searchCursorPos += text.length();
        }
        applyFilter();
    }

    private boolean handleConfigKeyInput(char typedChar, int keyCode) {
        if (createConfigEntry.isFocused()) {
            createConfigEntry.handleKeyTyped(typedChar, keyCode);
            if (!createConfigEntry.isFocused()) {
                configListDirty = true;
            }
            return true;
        }

        return false;
    }

    private void applyFilter() {
        filteredEntries.clear();
        if (searchText.isEmpty()) return;
        
        String lower = searchText.toLowerCase();

        Category selectedCat = categoryList.get(selectedCategoryIndex);
        if (selectedCat == Category.CONFIG || selectedCat == Category.SETTINGS) {
            for (ModuleEntry entry : moduleEntries) {
                if (entry.mod.getName().toLowerCase().contains(lower)) {
                    filteredEntries.add(entry);
                }
            }
        } else {
            for (ModuleEntry entry : allModuleEntries) {
                if (entry.mod.getName().toLowerCase().contains(lower)) {
                    filteredEntries.add(entry);
                }
            }
        }

        filteredConfigs.clear();
        for (ConfigEntry entry : configEntries) {
            if (entry.getConfigName().toLowerCase().contains(lower)) {
                filteredConfigs.add(entry);
            }
        }
        
        targetScrollY = 0;
    }

    private int moveWordLeft(String text, int pos) {
        if (pos <= 0) return 0;
        do pos--;
        while (pos > 0 && !Character.isWhitespace(text.charAt(pos - 1)));
        return pos;
    }

    private int moveWordRight(String text, int pos) {
        if (pos >= text.length()) return text.length();
        while (pos < text.length() && !Character.isWhitespace(text.charAt(pos))) pos++;
        while (pos < text.length() && Character.isWhitespace(text.charAt(pos))) pos++;
        return pos;
    }

    private String readClipboard() {
        try {
            return (String) Toolkit.getDefaultToolkit().getSystemClipboard().getData(DataFlavor.stringFlavor);
        } catch (Exception e) { return ""; }
    }

    private void writeClipboard(String text) {
        try {
            Toolkit.getDefaultToolkit().getSystemClipboard().setContents(new StringSelection(text), null);
        } catch (Exception ignored) {}
    }

    private float calculateMaxScroll(float areaHeight, List<ModuleEntry> entries) {
        float leftTotal = 0;
        float rightTotal = 0;
        for (ModuleEntry e : entries) {
            if (e.col == 0) leftTotal += e.getTotalHeight() + 6;
            else rightTotal += e.getTotalHeight() + 6;
        }
        float maxCol = Math.max(leftTotal, rightTotal);
        float result = maxCol - areaHeight;
        return result > 0 ? result : 0;
    }

    private void rebuildAllModules() {
        allModuleEntries.clear();
        List<Module> allMods = new ArrayList<>();
        for (Module mod : Unfair.moduleManager.modules.values()) {
            if (mod.getCategory() != Category.CONFIG && mod.getCategory() != Category.SETTINGS) {
                allMods.add(mod);
            }
        }
        for (int i = 0; i < allMods.size(); i++) {
            allModuleEntries.add(new ModuleEntry(allMods.get(i), i % 2));
        }
    }

    private void rebuildModules() {
        moduleEntries.clear();
        filteredEntries.clear();

        Category selectedCat = categoryList.get(selectedCategoryIndex);

        if (selectedCat == Category.CONFIG || selectedCat == Category.SETTINGS) {
            refreshConfigs();
            targetScrollY = 0;
            scrollY = 0;
            return;
        }

        List<Module> mods = new ArrayList<>();
        for (Module mod : Unfair.moduleManager.modules.values()) {
            if (mod.getCategory() == selectedCat) mods.add(mod);
        }
        for (int i = 0; i < mods.size(); i++) {
            moduleEntries.add(new ModuleEntry(mods.get(i), i % 2));
        }
        targetScrollY = 0;
        scrollY = 0;
    }

    private void resetConfigAnimations() {
        createConfigEntry.setFocused(false);

        for (ConfigEntry entry : configEntries) {
            entry.resetAnimations();
        }
    }

    private int[] transformMouseCoords(int mouseX, int mouseY) {
        float scale = ClientSettings.INSTANCE.getGUIScale();
        float scaledWidth = PANEL_WIDTH * scale;
        float scaledHeight = PANEL_HEIGHT * scale;
        
        float screenPanelX = (this.width - scaledWidth) / 2f;
        float screenPanelY = (this.height - scaledHeight) / 2f;
        
        int transformedX = (int)((mouseX - screenPanelX) / scale + screenPanelX);
        int transformedY = (int)((mouseY - screenPanelY) / scale + screenPanelY);
        
        return new int[]{transformedX, transformedY};
    }
    
    private void applyScissor(float logicX, float logicY, float logicWidth, float logicHeight) {
        float guiScale = ClientSettings.INSTANCE.getGUIScale();
        float panelLogicW = PANEL_WIDTH * guiScale;
        float panelLogicH = PANEL_HEIGHT * guiScale;
        float panelX = (this.width - panelLogicW) / 2f;
        float panelY = (this.height - panelLogicH) / 2f;

        float mcX = panelX + (logicX - panelX) * guiScale;
        float mcY = panelY + (logicY - panelY) * guiScale;
        float mcW = logicWidth * guiScale;
        float mcH = logicHeight * guiScale;

        if (mcW < 0 || mcH < 0) {
            return;
        }

        RenderUtil.scissor(mcX, mcY, mcW, mcH);
    }
    
    private void updateThemeColors() {
        Color tgtBG = ClientSettings.INSTANCE.getBackgroundColor();
        BG_COLOR = tgtBG;

        Color tgtAccent = ClientSettings.INSTANCE.getAccentColor();
        LOGO_BOX_COLOR = tgtAccent;
        SEARCH_FOCUSED_BORDER = tgtAccent;

        Color tgtText = ClientSettings.INSTANCE.getTextColor();
        CATEGORY_ACTIVE_COLOR = tgtText;

        Color tgtCard = ClientSettings.INSTANCE.getCardColor();
        CARD_NORMAL = tgtCard;

        Color tgtSidebar = ClientSettings.INSTANCE.getSidebarColor();
        PLAYER_BG = tgtSidebar;

        LOGO_TEXT_COLOR = new Color(
            Math.min(255, tgtAccent.getRed() + 50),
            Math.min(255, tgtAccent.getGreen() + 50),
            Math.min(255, tgtAccent.getBlue() + 50)
        );

        CATEGORY_TEXT_COLOR = new Color(
            Math.min(255, tgtText.getRed() + 85),
            Math.min(255, tgtText.getGreen() + 90),
            Math.min(255, tgtText.getBlue() + 95)
        );

        SECTION_HEADER_COLOR = new Color(
            Math.min(255, tgtText.getRed() + 115),
            Math.min(255, tgtText.getGreen() + 110),
            Math.min(255, tgtText.getBlue() + 105)
        );

        CARD_ENABLED = new Color(
            Math.max(0, tgtCard.getRed() - 13),
            Math.min(255, tgtCard.getGreen() + 8),
            Math.min(255, tgtCard.getBlue() + 5)
        );

        CARD_HIDDEN_ENABLED = new Color(
            Math.max(0, tgtCard.getRed() - 10),
            Math.max(0, tgtCard.getGreen() - 15),
            Math.min(255, tgtCard.getBlue() + 10)
        );

        SEARCH_BG = new Color(
            Math.max(0, tgtBG.getRed() - 15),
            Math.max(0, tgtBG.getGreen() - 12),
            Math.max(0, tgtBG.getBlue() - 8)
        );

        ConfigEntry.cardNormal = CARD_NORMAL;
        ConfigEntry.cardActive = new Color(
            Math.max(0, tgtCard.getRed() - 20),
            Math.max(0, tgtCard.getGreen() - 5),
            Math.min(255, tgtCard.getBlue() + 15)
        );
        ConfigEntry.cardHover = new Color(
            Math.max(0, tgtCard.getRed() - 10),
            Math.min(255, tgtCard.getGreen() + 3),
            Math.min(255, tgtCard.getBlue() + 10)
        );
    }
    
    private void closeAllColorPickers() {
        for (ModuleEntry entry : moduleEntries) {
            for (PanelValueItem setting : entry.settings) {
                if (setting instanceof ColorSetting) {
                    ((ColorSetting) setting).closePicker();
                }
            }
        }
    }

    private void refreshConfigs() {
        configEntries.clear();
        filteredConfigs.clear();

        File dir = new File("./config/Unfair/");
        if (!dir.exists() || !dir.isDirectory()) {
            dir.mkdirs();
            return;
        }

        File[] files = dir.listFiles((d, name) -> name.endsWith(".json"));
        if (files == null) return;

        java.util.Set<String> seen = new java.util.HashSet<>();
        for (File file : files) {
            try {
                String canonical = file.getCanonicalPath().toLowerCase();
                if (!canonical.endsWith(".json") || seen.contains(canonical)) continue;
                seen.add(canonical);
                configEntries.add(new ConfigEntry(file.getName()));
            } catch (Exception ignored) {}
        }

        configListDirty = false;
        if (searchActive && !searchText.isEmpty()) {
            applyFilter();
        }
    }
    
    private void refreshSettings() {
        settingsEntries.clear();
        settingsEntries.add(new SettingsEntry("GUI Size", SettingsEntry.SettingsType.GUI_SIZE));
        settingsEntries.add(new SettingsEntry("Theme", SettingsEntry.SettingsType.THEME));
        settingsEntries.add(new SettingsEntry("Background Alpha", SettingsEntry.SettingsType.BACKGROUND_ALPHA));
        settingsEntries.add(new SettingsEntry("Blur Area", SettingsEntry.SettingsType.BLUR_AREA));
    }

    private static class ModuleEntry {
        Module mod;
        List<PanelValueItem> settings;
        ModuleActionButtons actionButtons;
        float x;
        float width;
        int col;
        float toggleAnim;
        float hideAnim;

        ModuleEntry(Module m, int c) {
            this.mod = m;
            this.col = c;
            this.settings = new ArrayList<>();
            this.actionButtons = new ModuleActionButtons(m);
            this.toggleAnim = m.isEnabled() ? 1f : 0f;
            this.hideAnim = m.isHidden() ? 1f : 0f;
            List<Property<?>> props = Unfair.propertyManager.properties.get(m.getClass());
            if (props != null) {
                for (Property<?> p : props) {
                    if (!p.isVisible()) continue;
                    if (p instanceof BooleanProperty) settings.add(new BoolSetting((BooleanProperty) p));
                    else if (p instanceof FloatProperty || p instanceof IntProperty || p instanceof PercentProperty)
                        settings.add(new SliderSetting(p));
                    else if (p instanceof ModeProperty) {
                        ModeSetting modeSetting = new ModeSetting((ModeProperty) p);
                        modeSetting.col = c;
                        settings.add(modeSetting);
                    }
                    else if (p instanceof ColorProperty)
                        settings.add(new ColorSetting((ColorProperty) p));
                }
            }
        }

        float getTotalHeight() {
            float h = 22;
            for (PanelValueItem s : settings) {
                if (s.visible()) h += s.getHeight() + 2;
            }
            return Math.max(h, 40);
        }
    }

    private static Color blendColor(Color a, Color b, float t) {
        int r = (int)(a.getRed() + (b.getRed() - a.getRed()) * t);
        int g = (int)(a.getGreen() + (b.getGreen() - a.getGreen()) * t);
        int bl = (int)(a.getBlue() + (b.getBlue() - a.getBlue()) * t);
        int al = (int)(a.getAlpha() + (b.getAlpha() - a.getAlpha()) * t);
        return new Color(r, g, bl, al);
    }

    private static Color blendAlpha(Color c, float alpha) {
        return new Color(c.getRed(), c.getGreen(), c.getBlue(), (int)(alpha * 255));
    }

    @Override
    public void onGuiClosed() {
        EventManager.unregister(this);
    }

    @Override
    public boolean doesGuiPauseGame() { return false; }
}
