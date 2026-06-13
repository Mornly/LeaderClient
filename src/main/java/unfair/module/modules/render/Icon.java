package unfair.module.modules.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import unfair.event.EventTarget;
import unfair.events.Render2DEvent;
import unfair.module.Module;
import unfair.property.properties.IntProperty;
import unfair.property.properties.ModeProperty;
import unfair.util.RenderUtil;

public class Icon extends Module {
    private static final Minecraft mc = Minecraft.getMinecraft();

    public final ModeProperty styles = new ModeProperty("Styles", 0, new String[]{"Pig", "Shuai ge", "Liquid bounce", "Rise"});
    public final IntProperty width = new IntProperty("Width", 32, 8, 256);
    public final IntProperty height = new IntProperty("Height", 32, 8, 256);
    public final ModeProperty posX = new ModeProperty("Position X", 0, new String[]{"Left", "Right"});
    public final ModeProperty posY = new ModeProperty("Position Y", 0, new String[]{"Top", "Bottom"});
    public final IntProperty offsetX = new IntProperty("Offset X", 2, 0, 255);
    public final IntProperty offsetY = new IntProperty("Offset Y", 2, 0, 255);

    private boolean dragging = false;
    private boolean prevMouseDown = false;
    private float dragOffsetX = 0.0F;
    private float dragOffsetY = 0.0F;

    public Icon() {
        super("Icon", false);
    }

    private ResourceLocation getCurrentIcon() {
        String style = styles.getModeString();
        String fileName;
        switch (style.toLowerCase()) {
            case "pig":
                fileName = "pig.png";
                break;
            case "shuai ge":
                fileName = "handsome.png";
                break;
            case "liquid bounce":
                fileName = "liquidbounce.png";
                break;
            case "rise":
                fileName = "rise.png";
                break;
            default:
                fileName = "pig.png";
        }
        return new ResourceLocation("minecraft", "unfair/texture/icon/" + fileName);
    }

    private float[] getIconSize() {
        return new float[]{width.getValue(), height.getValue()};
    }

    private float[] applyDragging(float iconWidth, float iconHeight) {
        if (!(mc.currentScreen instanceof GuiChat)) {
            dragging = false;
            prevMouseDown = false;
            ScaledResolution sr = new ScaledResolution(mc);
            float sw = sr.getScaledWidth();
            float sh = sr.getScaledHeight();
            float x = offsetX.getValue();
            float y = offsetY.getValue();
            if (posX.getValue() == 1) {
                x = sw - iconWidth - x;
            }
            if (posY.getValue() == 1) {
                y = sh - iconHeight - y;
            }
            return new float[]{x, y};
        }

        ScaledResolution sr = new ScaledResolution(mc);
        float sw = sr.getScaledWidth();
        float sh = sr.getScaledHeight();

        float localX = offsetX.getValue();
        float localY = offsetY.getValue();
        if (posX.getValue() == 1) {
            localX = sw - iconWidth - localX;
        }
        if (posY.getValue() == 1) {
            localY = sh - iconHeight - localY;
        }

        float screenX = localX;
        float screenY = localY;
        float screenW = iconWidth;
        float screenH = iconHeight;

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
            float targetScreenX = mouseX - dragOffsetX;
            float targetScreenY = mouseY - dragOffsetY;
            int newOffsetX, newOffsetY;
            if (posX.getValue() == 0) {
                newOffsetX = Math.round(targetScreenX);
            } else {
                newOffsetX = Math.round(sw - iconWidth - targetScreenX);
            }
            if (posY.getValue() == 0) {
                newOffsetY = Math.round(targetScreenY);
            } else {
                newOffsetY = Math.round(sh - iconHeight - targetScreenY);
            }
            newOffsetX = Math.max(0, Math.min(255, newOffsetX));
            newOffsetY = Math.max(0, Math.min(255, newOffsetY));
            offsetX.setValue(newOffsetX);
            offsetY.setValue(newOffsetY);
        }
        prevMouseDown = mouseDown;

        float newLocalX = offsetX.getValue();
        float newLocalY = offsetY.getValue();
        if (posX.getValue() == 1) {
            newLocalX = sw - iconWidth - newLocalX;
        }
        if (posY.getValue() == 1) {
            newLocalY = sh - iconHeight - newLocalY;
        }
        return new float[]{newLocalX, newLocalY};
    }

    @EventTarget
    public void onRender2D(Render2DEvent event) {
        if (!isEnabled()) return;

        float[] size = getIconSize();
        float iconWidth = size[0];
        float iconHeight = size[1];
        float[] pos = applyDragging(iconWidth, iconHeight);
        float x = pos[0];
        float y = pos[1];

        ResourceLocation icon = getCurrentIcon();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        RenderUtil.drawImage(icon, x, y, iconWidth, iconHeight, 0xFFFFFFFF);
        GlStateManager.disableBlend();
    }
}