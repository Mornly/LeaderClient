package unfair.ui.clickgui.dropdown;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import unfair.Unfair;
import unfair.config.Config;
import unfair.module.Category;
import unfair.module.modules.render.GuiModule;
import unfair.module.modules.render.HUD;
import unfair.util.RenderUtil;

import java.awt.*;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DropDownGui extends GuiScreen {
    final List<Panel> panels = new ArrayList<>();
    private ConfigPanel configPanel;
    public float posX = 13, posY = 18;
    public float scrollY = 0;
    private final long openTime;
    private final File configFile = new File("./config/Unfair/", "dropdowngui.txt");
    private boolean blurEnabled;

    public DropDownGui() {
        super();
        openTime = System.currentTimeMillis();
        GuiModule guiModule = (GuiModule) Unfair.moduleManager.modules.get(GuiModule.class);
        blurEnabled = guiModule != null && guiModule.blur.getValue();

        float offsetX = 0;
        for (Category cat : Category.values()) {
            if (cat == Category.CONFIG) continue;
            panels.add(new Panel(cat, posX + offsetX, posY, blurEnabled));
            offsetX += 100;
        }
        configPanel = new ConfigPanel(posX + offsetX, posY, blurEnabled);
        loadPositions();
    }

    @Override
    public void initGui() { super.initGui(); }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        RenderUtil.drawRect(0, 0, this.width, this.height, (int)(0.35F * 255) << 24);

        for (Panel panel : panels) panel.render(mouseX, mouseY, scrollY);
        if (configPanel != null) configPanel.render(mouseX, mouseY, scrollY);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        for (Panel panel : panels) panel.mouseClicked(mouseX, mouseY, mouseButton);
        if (configPanel != null) configPanel.mouseClicked(mouseX, mouseY, mouseButton);
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        for (Panel panel : panels) panel.mouseReleased(mouseX, mouseY, state);
        if (configPanel != null) configPanel.mouseReleased(mouseX, mouseY, state);
        super.mouseReleased(mouseX, mouseY, state);
    }

    @Override
    public void handleMouseInput() throws IOException {
        try { super.handleMouseInput(); } catch (IOException e) { e.printStackTrace(); }
        int wheelInput = Mouse.getDWheel();
        if (wheelInput != 0) {
            scrollY += wheelInput / 120.0F * 10;
            if (scrollY > 0) scrollY = 0;
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        long timeSinceOpen = System.currentTimeMillis() - openTime;
        if (timeSinceOpen < 1L) return;

        if (keyCode == Keyboard.KEY_ESCAPE) {
            this.mc.displayGuiScreen(null);
            return;
        }

        for (Panel panel : panels) panel.keyPressed(keyCode);
        for (Panel panel : panels) panel.charTyped(typedChar);
        if (configPanel != null) { configPanel.keyPressed(keyCode); configPanel.charTyped(typedChar); }

        super.keyTyped(typedChar, keyCode);
    }

    @Override
    public void onGuiClosed() {
        savePositions();
        autoSaveConfig();
        super.onGuiClosed();
    }

    private void autoSaveConfig() {
        try { new Config(Config.lastConfig != null ? Config.lastConfig : "default", true).save(); } catch (Exception ignored) {}
    }

    @Override
    public boolean doesGuiPauseGame() { return false; }

    private void savePositions() {
        JsonObject json = new JsonObject();
        json.addProperty("scrollY", scrollY);
        for (int i = 0; i < panels.size(); i++) {
            Panel p = panels.get(i);
            String key = "panel_" + i;
            JsonObject pos = new JsonObject();
            pos.addProperty("x", p.x); pos.addProperty("y", p.y);
            json.add(key, pos);
        }
        if (configPanel != null) {
            JsonObject cp = new JsonObject();
            cp.addProperty("x", configPanel.x); cp.addProperty("y", configPanel.y);
            json.add("configPanel", cp);
        }
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        try (FileWriter writer = new FileWriter(configFile)) { gson.toJson(json, writer); } catch (IOException e) { e.printStackTrace(); }
    }

    private void loadPositions() {
        if (!configFile.exists()) return;
        JsonParser parser = new JsonParser();
        try (FileReader reader = new FileReader(configFile)) {
            JsonObject json = parser.parse(reader).getAsJsonObject();
            if (json.has("scrollY")) scrollY = json.get("scrollY").getAsFloat();
            for (int i = 0; i < panels.size(); i++) {
                Panel p = panels.get(i);
                String key = "panel_" + i;
                if (json.has(key)) {
                    JsonObject pos = json.getAsJsonObject(key);
                    p.x = pos.get("x").getAsFloat(); p.y = pos.get("y").getAsFloat();
                }
            }
            if (configPanel != null && json.has("configPanel")) {
                JsonObject cp = json.getAsJsonObject("configPanel");
                configPanel.x = cp.get("x").getAsFloat(); configPanel.y = cp.get("y").getAsFloat();
            }
        } catch (IOException e) { e.printStackTrace(); }
    }
}
