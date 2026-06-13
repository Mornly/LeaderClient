package unfair.module.modules.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.input.Mouse;
import unfair.Unfair;
import unfair.event.EventTarget;
import unfair.events.Render2DEvent;
import unfair.module.Module;
import unfair.property.properties.IntProperty;
import unfair.util.RenderUtil;

public class WaterMark extends Module {
    public WaterMark() {
        super("WaterMark", false, true);
    }

    public final IntProperty rectLeft = new IntProperty("Rect Left", 5, 0, 20);
    public final IntProperty rectTop = new IntProperty("Rect Top", 5, 0, 20);

    private boolean dragging = false;
    private boolean prevMouseDown = false;
    private float dragOffsetX = 0.0F;
    private float dragOffsetY = 0.0F;

    @EventTarget
    public void onRender2D(Render2DEvent event) {
        if (!this.isEnabled()) return;

        Minecraft mc = Minecraft.getMinecraft();
        if (mc.theWorld == null || mc.thePlayer == null) return;

        String text = "Leader";
        int textWidth = RenderUtil.getWidth(text);
        int textHeight = RenderUtil.getHeight();
        int padding = 4;

        float width = textWidth + padding * 2;
        float height = textHeight + padding * 2;

        // 拖拽处理
        if (mc.currentScreen instanceof GuiChat) {
            ScaledResolution sr = new ScaledResolution(mc);
            float sw = sr.getScaledWidth();
            float sh = sr.getScaledHeight();

            float screenX = rectLeft.getValue();
            float screenY = rectTop.getValue();
            float screenW = width;
            float screenH = height;

            float mouseX = Mouse.getX() * sw / mc.displayWidth;
            float mouseY = sh - Mouse.getY() * sh / mc.displayHeight - 1.0F;
            boolean hovered = mouseX >= screenX && mouseX <= screenX + screenW && mouseY >= screenY && mouseY <= screenY + screenH;
            boolean mouseDown = Mouse.isButtonDown(0);

            if (mouseDown && !prevMouseDown && hovered) {
                dragging = true;
                dragOffsetX = mouseX - screenX;
                dragOffsetY = mouseY - screenY;
            }
            if (!mouseDown) {
                dragging = false;
            }
            if (dragging) {
                float targetX = mouseX - dragOffsetX;
                float targetY = mouseY - dragOffsetY;
                int newLeft = Math.max(0, Math.min(20, Math.round(targetX)));
                int newTop = Math.max(0, Math.min(20, Math.round(targetY)));
                rectLeft.setValue(newLeft);
                rectTop.setValue(newTop);
            }
            prevMouseDown = mouseDown;
        } else {
            dragging = false;
            prevMouseDown = false;
        }

        float rectRight = rectLeft.getValue() + width;
        float rectBottom = rectTop.getValue() + height;
        float radius = 6f;

        HUD hud = (HUD) Unfair.moduleManager.modules.get(HUD.class);
        int fillColor = 0x80000000;
        int hudColor = hud.getColor(System.currentTimeMillis()).getRGB();

        RenderUtil.drawRoundedGradientOutlinedRectangle(
                rectLeft.getValue(), rectTop.getValue(), rectRight, rectBottom,
                radius, fillColor, hudColor, hudColor
        );
        RenderUtil.drawFont(text, rectLeft.getValue() + padding, rectTop.getValue() + padding, hudColor, true);
    }
}