package unfair.module.modules.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.ResourceLocation;
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

    public Icon() {
        super("Icon", false);
    }

    private ResourceLocation getCurrentIcon() {
        String style = styles.getModeString();
        String fileName;
        switch (style) {
            case "pig":
                fileName = "pig.png";
                break;
            case "shuaige":
                fileName = "handsome.png";
                break;
            case "liquidbounce":
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

    @EventTarget
    public void onRender2D(Render2DEvent event) {
        if (!isEnabled()) return;

        ScaledResolution sr = new ScaledResolution(mc);
        float scaledWidth = sr.getScaledWidth();
        float scaledHeight = sr.getScaledHeight();

        float x;
        float y;

        if (posX.getValue() == 0) {
            x = offsetX.getValue();
        } else {
            x = scaledWidth - offsetX.getValue() - width.getValue();
        }

        if (posY.getValue() == 0) {
            y = offsetY.getValue();
        } else {
            y = scaledHeight - offsetY.getValue() - height.getValue();
        }

        ResourceLocation icon = getCurrentIcon();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        RenderUtil.drawImage(icon, x, y, width.getValue(), height.getValue(), 0xFFFFFFFF);
        GlStateManager.disableBlend();
    }
}