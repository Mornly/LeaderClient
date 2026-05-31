package unfair.ui.clickgui.dropdown;

import unfair.Unfair;
import unfair.module.Category;
import unfair.module.Module;
import unfair.module.modules.render.HUD;
import unfair.util.RenderUtil;
import unfair.util.shader.BlurUtils;

import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.List;

public class Panel {
    public Category category;
    public float x, y;
    public DD[] modules;
    private static final float PANEL_WIDTH = 95;
    private static final float HEADER_HEIGHT = 18;
    private final boolean blurEnabled;

    public Panel(Category c, float x, float y, boolean blur) {
        this.category = c;
        this.x = x;
        this.y = y;
        this.blurEnabled = blur;
        initModules();
    }

    private void initModules() {
        List<Module> mods = Unfair.moduleManager.getModulesByCategory(category);
        modules = new DD[mods.size()];
        for (int i = 0; i < mods.size(); i++) modules[i] = new DD(mods.get(i), blurEnabled);
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

        int color1 = hud.getColor(time).getRGB();
        int color2 = hud.getColor(time + 400).getRGB();
        RenderUtil.drawGradientRect((int)x, (int)panelY, x + PANEL_WIDTH, (int)(panelY + HEADER_HEIGHT), color1, color2);

        drawString(category.name(), x + 7, panelY + 3, Color.WHITE.getRGB(), 20, false);

        float contentHeight = calculateContentHeight();

        if (blurEnabled && contentHeight > 0) {
            BlurUtils.prepareBlur();
            RenderUtil.drawRect(x, panelY + HEADER_HEIGHT, x + PANEL_WIDTH, panelY + HEADER_HEIGHT + contentHeight, new Color(0, 0, 0, 90).getRGB());
            BlurUtils.blurEnd(2, 3f);
        } else {
            RenderUtil.drawRect(x, panelY + HEADER_HEIGHT, x + PANEL_WIDTH, panelY + HEADER_HEIGHT + contentHeight, new Color(0, 0, 0, 90).getRGB());
        }

        GlStateManager.enableBlend();
        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        RenderUtil.scissor(x, panelY + HEADER_HEIGHT, PANEL_WIDTH, contentHeight);

        float moduleY = HEADER_HEIGHT + 3;
        for (DD mod : modules) {
            mod.x = x;
            mod.y = panelY + moduleY;
            mod.width = PANEL_WIDTH;
            mod.render(mouseX, mouseY);
            moduleY += mod.getTotalHeight() + 2;
        }

        GL11.glDisable(GL11.GL_SCISSOR_TEST);
        GlStateManager.disableBlend();
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
        for (DD mod : modules) h += mod.getTotalHeight() + 2;
        return Math.max(h - 2, 0);
    }
}
