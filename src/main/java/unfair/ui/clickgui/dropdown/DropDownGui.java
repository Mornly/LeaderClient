package unfair.ui.clickgui.dropdown;

import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;
import unfair.module.Category;
import unfair.util.RenderUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DropDownGui extends GuiScreen {
    final List<Panel> panels = new ArrayList<>();
    private ConfigPanel configPanel;
    public float posX = 13, posY = 18;
    public float scrollY = 0;
    private final long openTime;

    public DropDownGui() {
        super();
        openTime = System.currentTimeMillis();

        float offsetX = 0;
        for (Category cat : Category.values()) {
            if (cat == Category.CONFIG) continue;
            panels.add(new Panel(cat, posX + offsetX, posY, false));
            offsetX += 120;
        }
        configPanel = new ConfigPanel(posX + offsetX, posY, false);
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
            if (ClientSetting.anyListening) {
                for (Panel panel : panels) panel.keyPressed(keyCode);
                if (configPanel != null) configPanel.keyPressed(keyCode);
                return;
            }
            this.mc.displayGuiScreen(null);
            return;
        }

        for (Panel panel : panels) panel.keyPressed(keyCode);
        for (Panel panel : panels) panel.charTyped(typedChar);
        if (configPanel != null) { configPanel.keyPressed(keyCode); configPanel.charTyped(typedChar); }

        super.keyTyped(typedChar, keyCode);
    }

    @Override
    public boolean doesGuiPauseGame() { return false; }
}
