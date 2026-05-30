package unfair.module.modules.render;

import unfair.Unfair;
import unfair.event.EventTarget;
import unfair.events.Render2DEvent;
import unfair.font.impl.UFontRenderer;
import unfair.module.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import unfair.property.properties.IntProperty;
import unfair.util.RenderUtil;

public class WaterMark extends Module {
    public WaterMark() {
        super("WaterMark", false, true);
    }

    public final IntProperty rectLeft = new IntProperty("RectLeft", 5, 0, 20);
    public final IntProperty rectTop = new IntProperty("RectTop", 5, 0, 20);
    @EventTarget
    public void onRender2D(Render2DEvent event) {
        if (!this.isEnabled()) return;

        Minecraft mc = Minecraft.getMinecraft();
        if (mc.theWorld == null || mc.thePlayer == null) return;

        String text = "Leader";

        int textWidth = RenderUtil.getWidth(text);
        int textHeight = RenderUtil.getHeight();

        int padding = 4;

        float rectRight = rectLeft.getValue() + textWidth + padding * 2;
        float rectBottom = rectTop.getValue() + textHeight + padding * 2;

        float radius = 6f;

        HUD hud = (HUD) Unfair.moduleManager.modules.get(HUD.class);

        int fillColor = 0x80000000;
        int hudColor = hud.getColor(System.currentTimeMillis()).getRGB();

        RenderUtil.drawRoundedGradientOutlinedRectangle(
                rectLeft.getValue(), rectTop.getValue(), rectRight, rectBottom,
                radius, fillColor, hudColor, hudColor
        );
        RenderUtil.drawFont(text, rectLeft.getValue() + padding, rectTop.getValue() + padding, hudColor,true);
    }
}