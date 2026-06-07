package unfair.ui.clickgui.dropdown;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL11;
import unfair.Unfair;
import unfair.management.ClientSettings;
import unfair.module.modules.render.HUD;
import unfair.util.RenderUtil;

import java.awt.*;
import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ConfigPanel {
    public float x, y;
    private CreateDD createDD;
    private final List<ConfigDD> configDDs = new ArrayList<>();
    private String currentConfig = "default.json";
    private static final FilenameFilter JSON_FILTER = (dir, name) -> name.endsWith(".json");
    private static final float PANEL_WIDTH = 100;
    private static final float HEADER_HEIGHT = 18;

    public ConfigPanel(float x, float y) {
        this.x = x;
        this.y = y;
        initConfigs();
    }

    private void initConfigs() {
        createDD = new CreateDD(this);
        refreshConfigs();
    }

    public void refreshConfigs() {
        configDDs.clear();
        File dir = new File("./config/Unfair/");
        if (!dir.exists() || !dir.isDirectory()) return;
        File[] files = dir.listFiles(JSON_FILTER);
        if (files == null) return;
        Set<String> seen = new HashSet<>();
        for (File file : files) {
            try {
                String canonical = file.getCanonicalPath().toLowerCase();
                if (!canonical.endsWith(".json") || seen.contains(canonical)) continue;
                seen.add(canonical);
                configDDs.add(new ConfigDD(file.getName(), this));
            } catch (Exception ignored) {}
        }
    }

    public boolean configExists(String name) {
        if (!name.endsWith(".json")) name += ".json";
        for (ConfigDD dd : configDDs) {
            if (dd.configName.equals(name)) return true;
        }
        return false;
    }

    public void updateActiveConfig(String configName) {
        for (ConfigDD dd : configDDs) dd.setActive(dd.configName.equals(configName));
        currentConfig = configName;
    }

    public void removeConfig(ConfigDD toRemove) { configDDs.remove(toRemove); }
    public String getCurrentConfig() { return currentConfig; }
    public void setCurrentConfig(String name) { this.currentConfig = name; }

    public void render(int mouseX, int mouseY, float scrollY) {
        float panelY = y + scrollY;

        long time = System.currentTimeMillis();
        HUD hud = (HUD) Unfair.moduleManager.modules.get(HUD.class);
        Color c1 = hud.getColor(time);
        Color c2 = hud.getColor(time + 400);

        // Header: solid gradient rect
        RenderUtil.drawGradientRect((int) x, (int) panelY, x + PANEL_WIDTH, (int) (panelY + HEADER_HEIGHT),
                c1.getRGB(), c2.getRGB());

        Unfair.fontManager.getFont(18).drawString("Config", x + 8, panelY + 4, new Color(200, 200, 220).getRGB(), false);

        float contentHeight = calculateContentHeight();

        // Content background: solid rect
        if (contentHeight > 0) {
            int bgColor = ClientSettings.INSTANCE.getPanelContentBgColor().getRGB();
            RenderUtil.drawRect(x, panelY + HEADER_HEIGHT, x + PANEL_WIDTH, panelY + HEADER_HEIGHT + contentHeight, bgColor);
        }

        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        Minecraft mc = Minecraft.getMinecraft();
        float scaleFactor = mc.displayWidth / (float)mc.currentScreen.width;
        int scissorX = (int)(x * scaleFactor);
        int scissorY = (int)((mc.currentScreen.height - (panelY + HEADER_HEIGHT + contentHeight)) * scaleFactor);
        int scissorW = (int)(PANEL_WIDTH * scaleFactor);
        int scissorH = (int)(contentHeight * scaleFactor);
        GL11.glScissor(scissorX, scissorY, scissorW, scissorH);

        float moduleY = HEADER_HEIGHT + 2;
        createDD.x = x + 2;
        createDD.y = panelY + moduleY;
        createDD.width = PANEL_WIDTH - 4;
        createDD.render(mouseX, mouseY);
        moduleY += createDD.getTotalHeight() + 2;

        for (ConfigDD cfg : configDDs) {
            cfg.x = x + 2;
            cfg.y = panelY + moduleY;
            cfg.width = PANEL_WIDTH - 4;
            cfg.render(mouseX, mouseY);
            moduleY += cfg.getTotalHeight() + 2;
        }

        GL11.glDisable(GL11.GL_SCISSOR_TEST);
    }

    public void mouseClicked(int mx, int my, int button) {
        createDD.mouseClicked(mx, my, button);
        for (ConfigDD cfg : new ArrayList<>(configDDs)) cfg.mouseClicked(mx, my, button);
    }

    public void mouseReleased(int mx, int my, int button) {
        createDD.mouseReleased(mx, my, button);
        for (ConfigDD cfg : new ArrayList<>(configDDs)) cfg.mouseReleased(mx, my, button);
    }

    public void charTyped(char chr) {
        createDD.charTyped(chr);
        for (ConfigDD cfg : new ArrayList<>(configDDs)) cfg.charTyped(chr);
    }

    public void keyPressed(int keyCode) {
        createDD.keyPressed(keyCode);
        for (ConfigDD cfg : new ArrayList<>(configDDs)) cfg.keyPressed(keyCode);
    }

    public float calculateContentHeight() {
        float h = createDD.getTotalHeight() + 2;
        for (ConfigDD cfg : configDDs) h += cfg.getTotalHeight() + 2;
        return Math.max(h - 2, 0);
    }
}