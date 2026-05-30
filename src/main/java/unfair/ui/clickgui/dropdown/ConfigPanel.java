package unfair.ui.clickgui.dropdown;

import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL11;
import unfair.Unfair;
import unfair.module.modules.render.HUD;
import unfair.util.RenderUtil;
import unfair.util.shader.BlurUtils;

import java.awt.*;
import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;

public class ConfigPanel {
    public float x, y;
    private CreateDD createDD;
    private final List<ConfigDD> configDDs = new ArrayList<>();
    private String currentConfig = "default.json";
    private static final FilenameFilter JSON_FILTER = (dir, name) -> name.endsWith(".json");
    private final boolean blurEnabled;

    public ConfigPanel(float x, float y, boolean blur) {
        this.x = x;
        this.y = y;
        this.blurEnabled = blur;
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
        for (File file : files) configDDs.add(new ConfigDD(file.getName(), this));
    }

    public void updateActiveConfig(String configName) {
        for (ConfigDD dd : configDDs) dd.setActive(dd.configName.equals(configName));
        currentConfig = configName;
    }

    public void removeConfig(ConfigDD toRemove) { configDDs.remove(toRemove); }
    public String getCurrentConfig() { return currentConfig; }
    public void setCurrentConfig(String name) { this.currentConfig = name; }
    public boolean isBlurEnabled() { return blurEnabled; }

    public void render(int mouseX, int mouseY, float scrollY) {
        float panelY = y + scrollY;
        float pw = 95;
        float ph = 18;

        long time = System.currentTimeMillis();
        HUD hud = (HUD) Unfair.moduleManager.modules.get(HUD.class);
        int c1 = hud.getColor(time).getRGB();
        int c2 = hud.getColor(time + 400).getRGB();
        RenderUtil.drawGradientRect((int)x, (int)panelY, x + pw, (int)(panelY + ph), c1, c2);

        Unfair.fontManager.getFont(20).drawString("Config", x + 7, panelY + 3, Color.WHITE.getRGB(), false);

        float contentHeight = calculateContentHeight();

        if (blurEnabled && contentHeight > 0) {
            BlurUtils.prepareBlur();
            RenderUtil.drawRect(x, panelY + ph, x + pw, panelY + ph + contentHeight, new Color(0, 0, 0, 90).getRGB());
            BlurUtils.blurEnd(2, 3f);
        } else {
            RenderUtil.drawRect(x, panelY + ph, x + pw, panelY + ph + contentHeight, new Color(0, 0, 0, 90).getRGB());
        }

        GlStateManager.enableBlend();
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        RenderUtil.scissor(x, panelY + ph, pw, contentHeight);

        float moduleY = ph + 3;
        createDD.x = x;
        createDD.y = panelY + moduleY;
        createDD.width = pw;
        createDD.render(mouseX, mouseY);
        moduleY += createDD.getTotalHeight() + 3;

        for (ConfigDD cfg : configDDs) {
            cfg.x = x;
            cfg.y = panelY + moduleY;
            cfg.width = pw;
            cfg.render(mouseX, mouseY);
            moduleY += cfg.getTotalHeight() + 3;
        }

        GL11.glDisable(GL11.GL_SCISSOR_TEST);
        GlStateManager.disableBlend();
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
        float h = createDD.getTotalHeight() + 3;
        for (ConfigDD cfg : configDDs) h += cfg.getTotalHeight() + 3;
        return Math.max(h - 3, 0);
    }
}
