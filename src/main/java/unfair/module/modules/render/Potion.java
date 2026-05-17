package unfair.module.modules.render;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import unfair.Unfair;
import unfair.event.EventTarget;
import unfair.event.types.EventType;
import unfair.events.Render2DEvent;
import unfair.events.UpdateEvent;
import unfair.module.Module;
import unfair.property.properties.IntProperty;
import unfair.property.properties.ModeProperty;
import unfair.property.properties.PercentProperty;
import unfair.util.ColorUtil;
import unfair.util.RenderUtil;
import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

import static unfair.config.Config.mc;
import static unfair.util.RenderUtil.*;

public class Potion extends Module {
    public Potion(){super("Potion",false,false);}

    private final ModeProperty mode = new ModeProperty("Mode",0,new String[]{"Circle","GlowCircle","Bar"});
    public final PercentProperty background = new PercentProperty("background", 15,() -> mode.getValue() != 1);
    private final ModeProperty fontColorMode = new ModeProperty("FontColorMode",0,new String[]{"HUD","White","Potion"},() -> mode.getValue() == 1);
    private final ModeProperty circleColorMode = new ModeProperty("CircleColorMode",0,new String[]{"HUD","White","Potion"},() -> mode.getValue() == 1);
    private final IntProperty postx = new IntProperty("PostX", 240, -480, 640);
    private final IntProperty  posty = new IntProperty("PostY", 60, -280, 350);
    private int maxString = 0;
    private final Map<Integer, Integer> potionMaxDurations = new HashMap<>();

    List<PotionEffect> effects = new ArrayList<>();
    private String get(PotionEffect potioneffect) {
        net.minecraft.potion.Potion potion = net.minecraft.potion.Potion.potionTypes[potioneffect.getPotionID()];
        String s1 = I18n.format(potion.getName());
        s1 = s1 + " " + this.intToRomanByGreedy(potioneffect.getAmplifier() + 1);
        return s1;
    }


    private String intToRomanByGreedy(int num) {
        int[] values = new int[]{1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1};
        String[] symbols = new String[]{"M", "CM", "D", "CD", "C", "XC", "L", "XL", "X", "IX", "V", "IV", "I"};
        StringBuilder stringBuilder = new StringBuilder();
        for (int i2 = 0; i2 < values.length && num >= 0; ++i2) {
            while (values[i2] <= num) {
                num -= values[i2];
                stringBuilder.append(symbols[i2]);
            }
        }
        return stringBuilder.toString();
    }
    @EventTarget
    public void onRender(Render2DEvent event){
        if (this.isEnabled()){
            if (mode.getValue() == 0) {
                this.effects = mc.thePlayer.getActivePotionEffects().stream().sorted(Comparator.comparingInt(it -> RenderUtil.getWidth(this.get(it)))).collect(Collectors.toList());
                int x = this.postx.getValue();
                int y2 = this.posty.getValue();
                int offsetX = 21;
                int offsetY = 14;
                HUD hud = (HUD) Unfair.moduleManager.modules.get(HUD.class);
                int i2 = 16;
                ArrayList<Integer> needRemove = new ArrayList<>();
                for (Map.Entry<Integer, Integer> entry : this.potionMaxDurations.entrySet()) {
                    if (mc.thePlayer.getActivePotionEffect(net.minecraft.potion.Potion.potionTypes[entry.getKey()]) != null)
                        continue;
                    needRemove.add(entry.getKey());
                }
                for (int id : needRemove) {
                    this.potionMaxDurations.remove(id);
                }
                for (PotionEffect effect : this.effects) {
                    if (this.potionMaxDurations.containsKey(effect.getPotionID()) && this.potionMaxDurations.get(effect.getPotionID()) >= effect.getDuration())
                        continue;
                    this.potionMaxDurations.put(effect.getPotionID(), effect.getDuration());
                }
                if (this.effects.isEmpty()) {
                    this.maxString = 0;
                }
                if (!this.effects.isEmpty()) {
                    GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
                    GlStateManager.disableLighting();
                    int l2 = 24;
                    RenderUtil.drawFont("Potions", x + 6, y2 - 8, hud.getColor(System.currentTimeMillis()).getRGB(),true);
                    RenderUtil.drawRect(x, y2 - 8, x + 2, y2, hud.getColor(System.currentTimeMillis()).getRGB());
                    for (PotionEffect potioneffect : this.effects) {
                        net.minecraft.potion.Potion potion = net.minecraft.potion.Potion.potionTypes[potioneffect.getPotionID()];
                        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
                        if (potion.hasStatusIcon()) {
                            mc.getTextureManager().bindTexture(new ResourceLocation("textures/gui/container/inventory.png"));
                            int i1 = potion.getStatusIconIndex();
                            GlStateManager.enableBlend();
                            Gui.drawModalRectWithCustomSizedTexture(x + offsetX - 17, y2 + i2 - offsetY + RenderUtil.getHeight(), i1 % 8 * 18, 198 + i1 / 8 * 18, 18, 18, 256.0f, 256.0f);
                        }
                        float potionDurationRatio = (float) potioneffect.getDuration() / (potionMaxDurations.get(potioneffect.getPotionID()) != null ? potionMaxDurations.get(potioneffect.getPotionID()) : 1);
                        String s2 = net.minecraft.potion.Potion.getDurationString(potioneffect);
                        String s1 = this.get(potioneffect);
                        RenderUtil.drawFont(s1, x + offsetX + 7, y2 + i2 - offsetY + RenderUtil.getHeight(),hud.getColor(System.currentTimeMillis()).getRGB(),true);
                        RenderUtil.drawFont(s2, x + offsetX + 7, y2 + i2 + 11 - offsetY + RenderUtil.getHeight(),-1,true);
                        RenderUtil.circle(x + offsetX - 20, y2 + i2 - offsetY + RenderUtil.getHeight() - 3, 24, 360, false, new Color(0, 0, 0, 70));
                        RenderUtil.circle(x + offsetX - 20, y2 + i2 - offsetY + RenderUtil.getHeight() - 3, 24, potionDurationRatio * 360, false, Color.white);
                        i2 = (int) ((double) i2 + (double) l2 * 1.2);
                        if (this.maxString >= RenderUtil.getWidth(s1)) continue;
                        this.maxString = RenderUtil.getWidth(s1);
                    }
                    int backgroundColor = new Color(0.0F, 0.0F, 0.0F, (float) this.background.getValue() / 100.0F).getRGB();
                    RenderUtil.drawRoundedRectangle(x + offsetX - 22,y2 - 10,x + maxString + 8 + offsetX,i2 + y2,4f,backgroundColor);
                }
            }
            if (mode.getValue() == 1) {
                this.effects = mc.thePlayer.getActivePotionEffects().stream().sorted(Comparator.comparingInt(it -> RenderUtil.getWidth(this.get(it)))).collect(Collectors.toList());
                int x = this.postx.getValue();
                int y2 = this.posty.getValue();
                int offsetX = 21;
                int offsetY = 14;
                HUD hud = (HUD) Unfair.moduleManager.modules.get(HUD.class);
                int i2 = 16;
                ArrayList<Integer> needRemove = new ArrayList<>();
                for (Map.Entry<Integer, Integer> entry : this.potionMaxDurations.entrySet()) {
                    if (mc.thePlayer.getActivePotionEffect(net.minecraft.potion.Potion.potionTypes[entry.getKey()]) != null)
                        continue;
                    needRemove.add(entry.getKey());
                }
                for (int id : needRemove) {
                    this.potionMaxDurations.remove(id);
                }
                for (PotionEffect effect : this.effects) {
                    if (this.potionMaxDurations.containsKey(effect.getPotionID()) && this.potionMaxDurations.get(effect.getPotionID()) >= effect.getDuration())
                        continue;
                    this.potionMaxDurations.put(effect.getPotionID(), effect.getDuration());
                }
                if (this.effects.isEmpty()) {
                    this.maxString = 0;
                }
                for (PotionEffect potioneffect : this.effects) {
                    net.minecraft.potion.Potion potion = net.minecraft.potion.Potion.potionTypes[potioneffect.getPotionID()];
                    GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
                    int potionColor = potion.getLiquidColor();
                    Color potCol = new Color(potionColor);
                    if (circleColorMode.getValue() == 0){
                        potCol = hud.getColor(System.currentTimeMillis());
                    }
                    if (circleColorMode.getValue() == 1){
                        potCol = Color.WHITE;
                    }
                    Color fontCol = new Color(potionColor);
                    if (fontColorMode.getValue() == 0){
                        fontCol = hud.getColor(System.currentTimeMillis());
                    }
                    if (fontColorMode.getValue() == 1){
                        fontCol = Color.WHITE;
                    }
                    if (potion.hasStatusIcon()) {
                        mc.getTextureManager().bindTexture(new ResourceLocation("textures/gui/container/inventory.png"));
                        int i1 = potion.getStatusIconIndex();
                        GlStateManager.enableBlend();
                        Gui.drawModalRectWithCustomSizedTexture(
                                x + offsetX - 17, y2 + i2 - offsetY + RenderUtil.getHeight(),
                                i1 % 8 * 18, 198 + i1 / 8 * 18, 18, 18, 256.0f, 256.0f
                        );
                    }
                    int l2 = 24;
                    float potionDurationRatio = (float) potioneffect.getDuration() /
                            (potionMaxDurations.get(potioneffect.getPotionID()) != null
                                    ? potionMaxDurations.get(potioneffect.getPotionID()) : 1);
                    String s2 = net.minecraft.potion.Potion.getDurationString(potioneffect);
                    String s1 = this.get(potioneffect);

                    RenderUtil.drawFont(s1, x + offsetX + 7, y2 + i2 - offsetY + RenderUtil.getHeight(),
                            fontCol.getRGB(), true);
                    RenderUtil.drawFont(s2, x + offsetX + 7, y2 + i2 + 11 - offsetY + RenderUtil.getHeight(),
                            -1, true);

                    float cx = x + offsetX - 20;
                    float cy = y2 + i2 - offsetY + RenderUtil.getHeight() - 3;


                    RenderUtil.circle(cx, cy, 27,   potionDurationRatio * 360, false, new Color(potCol.getRed(), potCol.getGreen(), potCol.getBlue(), 30));
                    RenderUtil.circle(cx, cy, 25.5, potionDurationRatio * 360, false, new Color(potCol.getRed(), potCol.getGreen(), potCol.getBlue(), 60));
                    RenderUtil.circle(cx, cy, 24.5, potionDurationRatio * 360, false, new Color(potCol.getRed(), potCol.getGreen(), potCol.getBlue(), 100));
                    RenderUtil.circle(cx, cy, 24, potionDurationRatio * 360, false, new Color(0, 0, 0, 70));
                    RenderUtil.circle(cx, cy, 24, potionDurationRatio * 360, false, potCol);
                    i2 = (int) ((double) i2 + (double) l2 * 1.2);
                    if (this.maxString >= RenderUtil.getWidth(s1)) continue;
                    this.maxString = RenderUtil.getWidth(s1);
                }
            }
            if (mode.getValue() == 2){
                this.effects = mc.thePlayer.getActivePotionEffects().stream()
                        .sorted(Comparator.comparingInt(it -> RenderUtil.getWidth(this.get(it))))
                        .collect(Collectors.toList());

                int x = this.postx.getValue();
                int y = this.posty.getValue();
                int offsetY = 28;
                HUD hud = (HUD) Unfair.moduleManager.modules.get(HUD.class);
                int progressBarWidth = 100;
                int progressBarHeight = 4;

                ArrayList<Integer> needRemove = new ArrayList<>();
                for (Map.Entry<Integer, Integer> entry : this.potionMaxDurations.entrySet()) {
                    if (mc.thePlayer.getActivePotionEffect(net.minecraft.potion.Potion.potionTypes[entry.getKey()]) != null) continue;
                    needRemove.add(entry.getKey());
                }
                for (int id : needRemove) {
                    this.potionMaxDurations.remove(id);
                }

                for (PotionEffect effect : this.effects) {
                    if (this.potionMaxDurations.containsKey(effect.getPotionID()) &&
                            this.potionMaxDurations.get(effect.getPotionID()) >= effect.getDuration()) continue;
                    this.potionMaxDurations.put(effect.getPotionID(), effect.getDuration());
                }
                if (!this.effects.isEmpty()) {
                    GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
                    GlStateManager.disableLighting();

                    int currentY = y;
                    for (PotionEffect potionEffect : this.effects) {
                        net.minecraft.potion.Potion potion = net.minecraft.potion.Potion.potionTypes[potionEffect.getPotionID()];
                        Color hudColor = hud.getColor(System.currentTimeMillis());
                        GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
                        if (potion.hasStatusIcon()) {
                            mc.getTextureManager().bindTexture(new ResourceLocation("textures/gui/container/inventory.png"));
                            int iconIndex = potion.getStatusIconIndex();
                            GlStateManager.enableBlend();
                            Gui.drawModalRectWithCustomSizedTexture(x, currentY + 4, iconIndex % 8 * 18, 198 + iconIndex / 8 * 18, 16, 16, 256.0f, 256.0f);
                        }
                        String potionName = this.get(potionEffect);
                        RenderUtil.drawFont(potionName, x + 20, currentY, hudColor.getRGB(),true);
                        String duration = net.minecraft.potion.Potion.getDurationString(potionEffect);
                        RenderUtil.drawFont(duration, x + 20, currentY + 10,new Color(255,255,255).getRGB(),true);
                        float durationRatio = (float) potionEffect.getDuration() /
                                (this.potionMaxDurations.get(potionEffect.getPotionID()) != null ?
                                        this.potionMaxDurations.get(potionEffect.getPotionID()) : 1);

                        int filledWidth = (int) (progressBarWidth * durationRatio);
                        RenderUtil.drawRect(x + 20, currentY + 22, x + 20 + progressBarWidth,
                                currentY + 22 + progressBarHeight, new Color(0, 0, 0, 120).getRGB());
                        GlStateManager.enableBlend();
                        GlStateManager.blendFunc(770, 1);
                        RenderUtil.drawRect(x + 20, currentY + 22, x + 20 + filledWidth,
                                currentY + 22 + progressBarHeight,
                                new Color(hudColor.getRed(), hudColor.getGreen(), hudColor.getBlue(), 200).getRGB());
                        RenderUtil.drawRect(x + 20 - 1, currentY + 22 - 1, x + 20 + filledWidth + 1,
                                currentY + 22 + progressBarHeight + 1,
                                new Color(hudColor.getRed(), hudColor.getGreen(), hudColor.getBlue(), 120).getRGB());
                        RenderUtil.drawRect(x + 20 - 2, currentY + 22 - 2, x + 20 + filledWidth + 2,
                                currentY + 22 + progressBarHeight + 2,
                                new Color(hudColor.getRed(), hudColor.getGreen(), hudColor.getBlue(), 60).getRGB());
                        RenderUtil.drawRect(x + 20 - 3, currentY + 22 - 3, x + 20 + filledWidth + 3,
                                currentY + 22 + progressBarHeight + 3,
                                new Color(hudColor.getRed(), hudColor.getGreen(), hudColor.getBlue(), 30).getRGB());
                        GlStateManager.blendFunc(770, 771);
                        GlStateManager.disableBlend();
                        currentY += offsetY;
                    }
                    int backgroundColor = new Color(0.0F, 0.0F, 0.0F, (float) this.background.getValue() / 100.0F).getRGB();
                    RenderUtil.drawRoundedRectangle(x,y,x + 22 + progressBarWidth,currentY,4f,backgroundColor);
                }
            }
        }
    }
}
