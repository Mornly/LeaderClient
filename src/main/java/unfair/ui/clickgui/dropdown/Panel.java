package unfair.ui.clickgui.dropdown;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL11;
import unfair.Unfair;
import unfair.management.ClientSettings;
import unfair.module.Category;
import unfair.module.Module;
import unfair.module.modules.render.HUD;
import unfair.util.RenderUtil;

import java.awt.*;
import java.util.List;

public class Panel {
    public Category category;
    public float x, y;
    public DD[] modules;
    private static final float PANEL_WIDTH = 100;
    private static final float HEADER_HEIGHT = 18;

    public Panel(Category c, float x, float y) {
        this.category = c;
        this.x = x;
        this.y = y;
        initModules();
    }

    private void initModules() {
        List<Module> mods = Unfair.moduleManager.getModulesByCategory(category);
        modules = new DD[mods.size()];
        for (int i = 0; i < mods.size(); i++) modules[i] = new DD(mods.get(i));
    }

    private static HUD getHud() {
        return (HUD) Unfair.moduleManager.modules.get(HUD.class);
    }

    private static void drawString(String text, float x, float y, int color, int size, boolean shadow) {
        Unfair.fontManager.getFont(size).drawString(text, x, y, color, shadow);
    }

    public void render(int mouseX, int mouseY, float scrollY) {
        long time = System.currentTimeMillis();
        HUD hud = getHud();

        float panelY = y + scrollY;
        float contentHeight = calculateContentHeight();

        Color color1 = hud.getColor(time);
        Color color2 = hud.getColor(time + 400);
        RenderUtil.drawGradientRect((int) x, (int) panelY, x + PANEL_WIDTH, (int) (panelY + HEADER_HEIGHT),
                color1.getRGB(), color2.getRGB());

        drawString(category.name(), x + 8, panelY + 4, new Color(200, 200, 220).getRGB(), 18, false);

        if (contentHeight > 0) {
            int bgColor = ClientSettings.INSTANCE.getPanelContentBgColor().getRGB();
            RenderUtil.drawRect(x, panelY + HEADER_HEIGHT, x + PANEL_WIDTH, panelY + HEADER_HEIGHT + contentHeight + 4F, bgColor);
        }

        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        Minecraft mc = Minecraft.getMinecraft();
        float scaleFactor = mc.displayWidth / (float) mc.currentScreen.width;
        int scissorX = (int) (x * scaleFactor);
        int scissorY = (int) ((mc.currentScreen.height - (panelY + HEADER_HEIGHT + contentHeight)) * scaleFactor);
        int scissorW = (int) (PANEL_WIDTH * scaleFactor);
        int scissorH = (int) (contentHeight * scaleFactor);
        GL11.glScissor(scissorX, scissorY, scissorW, scissorH);

        float moduleY = HEADER_HEIGHT + 1;
        for (DD mod : modules) {
            mod.x = x + 2;
            mod.y = panelY + moduleY;
            mod.width = PANEL_WIDTH - 4;
            mod.render(mouseX, mouseY);
            moduleY += mod.getTotalHeight();
        }

        GL11.glDisable(GL11.GL_SCISSOR_TEST);
    }

    public void mouseClicked(int mx, int my, int button) {
        for (DD mod : modules) mod.mouseClicked(mx, my, button);
    }

    public void mouseReleased(int mx, int my, int button) {
        for (DD mod : modules) mod.mouseReleased(mx, my, button);
    }

    public void charTyped(char chr) { for (DD mod : modules) mod.charTyped(chr); }
    public void keyPressed(int keyCode) { for (DD mod : modules) mod.keyPressed(keyCode); }

    public float calculateContentHeight() {
        float h = 0;
        for (DD mod : modules) h += mod.getTotalHeight();
        return Math.max(h - 1, 0);
    }
}