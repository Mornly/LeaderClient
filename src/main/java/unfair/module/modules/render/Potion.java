package unfair.module.modules.render;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import unfair.Unfair;
import unfair.event.EventTarget;
import unfair.events.Render2DEvent;
import unfair.module.Module;
import unfair.property.properties.IntProperty;
import unfair.property.properties.ModeProperty;
import unfair.property.properties.PercentProperty;
import unfair.util.RenderUtil;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import static unfair.config.Config.mc;
import static unfair.util.RenderUtil.*;

public class Potion extends Module {
    public Potion() {
        super("Potion", false, false);
    }

    private final ModeProperty mode = new ModeProperty("Mode", 0, new String[]{"Circle", "GlowCircle", "Bar"});
    public final PercentProperty background = new PercentProperty("background", 15, () -> mode.getValue() != 1);
    private final ModeProperty fontColorMode = new ModeProperty("FontColorMode", 0, new String[]{"HUD", "White", "Potion"}, () -> mode.getValue() == 1);
    private final ModeProperty circleColorMode = new ModeProperty("CircleColorMode", 0, new String[]{"HUD", "White", "Potion"}, () -> mode.getValue() == 1);
    private final IntProperty postx = new IntProperty("PostX", 240, -480, 640);
    private final IntProperty posty = new IntProperty("PostY", 60, -280, 350);
    private final Map<Integer, Integer> potionMaxDurations = new HashMap<>();

    private static class AnimData {
        float progress;
        boolean fadingOut;
    }
    private final Map<Integer, AnimData> animationMap = new ConcurrentHashMap<>();
    private List<PotionEffect> currentEffects = new ArrayList<>();
    private long lastFrameTime = System.currentTimeMillis();

    private static float animateSmooth(float target, float current, float speed, float deltaTime) {
        float diff = target - current;
        float change = diff * Math.min(1.0f, speed * deltaTime);
        if (Math.abs(change) < 0.001f && Math.abs(diff) < 0.001f) return target;
        return current + change;
    }

    private String getPotionName(PotionEffect effect) {
        net.minecraft.potion.Potion potion = net.minecraft.potion.Potion.potionTypes[effect.getPotionID()];
        String name = I18n.format(potion.getName());
        name = name + " " + intToRomanByGreedy(effect.getAmplifier() + 1);
        return name;
    }

    private String intToRomanByGreedy(int num) {
        int[] values = {1000, 900, 500, 400, 100, 90, 50, 40, 10, 9, 5, 4, 1};
        String[] symbols = {"M", "CM", "D", "CD", "C", "XC", "L", "XL", "X", "IX", "V", "IV", "I"};
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < values.length && num >= 0; i++) {
            while (values[i] <= num) {
                num -= values[i];
                sb.append(symbols[i]);
            }
        }
        return sb.toString();
    }

    private void updateMaxDurations() {
        List<Integer> toRemove = new ArrayList<>();
        for (Map.Entry<Integer, Integer> entry : potionMaxDurations.entrySet()) {
            if (mc.thePlayer.getActivePotionEffect(net.minecraft.potion.Potion.potionTypes[entry.getKey()]) == null) {
                toRemove.add(entry.getKey());
            }
        }
        for (int id : toRemove) potionMaxDurations.remove(id);
        for (PotionEffect effect : currentEffects) {
            int id = effect.getPotionID();
            if (!potionMaxDurations.containsKey(id) || potionMaxDurations.get(id) < effect.getDuration()) {
                potionMaxDurations.put(id, effect.getDuration());
            }
        }
    }

    private void updateAnimations(float deltaTime) {
        Set<Integer> activeIds = currentEffects.stream().map(PotionEffect::getPotionID).collect(Collectors.toSet());
        for (PotionEffect effect : currentEffects) {
            int id = effect.getPotionID();
            if (!animationMap.containsKey(id)) {
                AnimData data = new AnimData();
                data.progress = 0f;
                data.fadingOut = false;
                animationMap.put(id, data);
            } else {
                AnimData data = animationMap.get(id);
                if (data.fadingOut) data.fadingOut = false;
            }
        }
        for (Map.Entry<Integer, AnimData> entry : animationMap.entrySet()) {
            int id = entry.getKey();
            AnimData data = entry.getValue();
            if (!activeIds.contains(id) && !data.fadingOut) {
                data.fadingOut = true;
            }
            float target = data.fadingOut ? 0f : 1f;
            data.progress = animateSmooth(target, data.progress, 6f, deltaTime);
            if (data.progress <= 0.01f && data.fadingOut) {
                animationMap.remove(id);
            }
            if (data.progress >= 0.99f && !data.fadingOut) {
                data.progress = 1f;
            }
        }
    }

    private int getEffectFullWidth(PotionEffect effect) {
        String name = getPotionName(effect);
        String duration = net.minecraft.potion.Potion.getDurationString(effect);
        return Math.max(RenderUtil.getWidth(name), RenderUtil.getWidth(duration));
    }

    @EventTarget
    public void onRender2D(Render2DEvent event) {
        if (!isEnabled()) return;

        long now = System.currentTimeMillis();
        float deltaTime = Math.min(0.05f, (now - lastFrameTime) / 1000.0f);
        lastFrameTime = now;

        currentEffects = mc.thePlayer.getActivePotionEffects().stream()
                .sorted(Comparator.comparingInt(this::getEffectFullWidth).reversed())
                .collect(Collectors.toList());

        updateMaxDurations();
        updateAnimations(deltaTime);

        List<PotionEffect> renderList = new ArrayList<>(currentEffects);
        renderList.sort(Comparator.comparingInt(this::getEffectFullWidth).reversed());

        HUD hud = (HUD) Unfair.moduleManager.modules.get(HUD.class);
        int baseX = postx.getValue();
        int baseY = posty.getValue();

        if (mode.getValue() == 0) {
            renderCircleMode(renderList, hud, baseX, baseY);
        } else if (mode.getValue() == 1) {
            renderGlowCircleMode(renderList, hud, baseX, baseY);
        } else if (mode.getValue() == 2) {
            renderBarMode(renderList, hud, baseX, baseY);
        }
    }

    private void renderCircleMode(List<PotionEffect> renderList, HUD hud, int baseX, int baseY) {
        int offsetX = 21;
        int offsetY = 14;
        float rowHeight = 28.8f;
        int maxString = 0;
        float currentY = baseY;

        GlStateManager.color(1f, 1f, 1f, 1f);
        GlStateManager.disableLighting();

        drawFont("Potions", baseX + 6, baseY - 8, hud.getColor(System.currentTimeMillis()).getRGB(), true);
        drawRect(baseX, baseY - 8, baseX + 2, baseY, hud.getColor(System.currentTimeMillis()).getRGB());

        // 收集所有动画进度，用于背景透明度取最大值
        float maxProgress = 0f;
        for (PotionEffect effect : renderList) {
            AnimData anim = animationMap.get(effect.getPotionID());
            if (anim != null) maxProgress = Math.max(maxProgress, anim.progress);
        }

        for (PotionEffect effect : renderList) {
            int id = effect.getPotionID();
            AnimData anim = animationMap.get(id);
            if (anim == null) continue;
            float progress = anim.progress;
            if (progress <= 0.01f) continue;

            float effectiveHeight = rowHeight * progress;
            float drawY = currentY;

            String name = getPotionName(effect);
            String duration = net.minecraft.potion.Potion.getDurationString(effect);
            maxString = Math.max(maxString, Math.max(RenderUtil.getWidth(name), RenderUtil.getWidth(duration)));

            net.minecraft.potion.Potion potion = net.minecraft.potion.Potion.potionTypes[effect.getPotionID()];
            if (potion.hasStatusIcon()) {
                GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
                mc.getTextureManager().bindTexture(new ResourceLocation("textures/gui/container/inventory.png"));
                int iconIndex = potion.getStatusIconIndex();
                GlStateManager.enableBlend();
                Gui.drawModalRectWithCustomSizedTexture(baseX + offsetX - 17, (int) drawY + 16 - offsetY + getHeight(),
                        iconIndex % 8 * 18, 198 + iconIndex / 8 * 18, 18, 18, 256f, 256f);
                GlStateManager.disableBlend();
            }

            float ratio = (float) effect.getDuration() / (float) (potionMaxDurations.getOrDefault(id, 1));
            Color textColor = hud.getColor(System.currentTimeMillis());
            int alpha = (int) (progress * 255);
            Color fadedText = new Color(textColor.getRed(), textColor.getGreen(), textColor.getBlue(), alpha);

            int cx = baseX + offsetX - 20;
            int cy = (int) drawY + 16 - offsetY + getHeight() - 3;
            // 圆环背景透明度也随progress变化
            circle(cx, cy, 24, 360, false, new Color(0, 0, 0, (int)(70 * progress)));
            circle(cx, cy, 24, ratio * 360, false, fadedText);

            drawFont(name, baseX + offsetX + 7, (int) drawY + 16 - offsetY + getHeight(), fadedText.getRGB(), true);
            drawFont(duration, baseX + offsetX + 7, (int) drawY + 27 - offsetY + getHeight(), new Color(255, 255, 255, alpha).getRGB(), true);

            currentY += effectiveHeight;
        }

        // 背景矩形透明度随最大进度变化
        if (background.getValue() > 0 && !renderList.isEmpty() && maxProgress > 0.01f) {
            int bgAlpha = (int) (background.getValue().floatValue() / 100f * 255 * maxProgress);
            drawRoundedRectangle(baseX + offsetX - 22, baseY - 10, baseX + maxString + 8 + offsetX, (int) currentY + baseY, 4f, new Color(0, 0, 0, bgAlpha).getRGB());
        }
    }

    private void renderGlowCircleMode(List<PotionEffect> renderList, HUD hud, int baseX, int baseY) {
        int offsetX = 21;
        int offsetY = 14;
        float rowHeight = 28.8f;
        float currentY = baseY;

        float maxProgress = 0f;
        for (PotionEffect effect : renderList) {
            AnimData anim = animationMap.get(effect.getPotionID());
            if (anim != null) maxProgress = Math.max(maxProgress, anim.progress);
        }

        for (PotionEffect effect : renderList) {
            int id = effect.getPotionID();
            AnimData anim = animationMap.get(id);
            if (anim == null) continue;
            float progress = anim.progress;
            if (progress <= 0.01f) continue;

            float effectiveHeight = rowHeight * progress;
            float drawY = currentY;

            String name = getPotionName(effect);
            String duration = net.minecraft.potion.Potion.getDurationString(effect);

            net.minecraft.potion.Potion potion = net.minecraft.potion.Potion.potionTypes[effect.getPotionID()];
            int potionColor = potion.getLiquidColor();
            Color potCol = new Color(potionColor);
            if (circleColorMode.getValue() == 0) potCol = hud.getColor(System.currentTimeMillis());
            else if (circleColorMode.getValue() == 1) potCol = Color.WHITE;
            Color fontCol = new Color(potionColor);
            if (fontColorMode.getValue() == 0) fontCol = hud.getColor(System.currentTimeMillis());
            else if (fontColorMode.getValue() == 1) fontCol = Color.WHITE;

            int alpha = (int) (progress * 255);
            Color fadedCircle = new Color(potCol.getRed(), potCol.getGreen(), potCol.getBlue(), alpha);
            Color fadedFont = new Color(fontCol.getRed(), fontCol.getGreen(), fontCol.getBlue(), alpha);

            if (potion.hasStatusIcon()) {
                GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
                mc.getTextureManager().bindTexture(new ResourceLocation("textures/gui/container/inventory.png"));
                int iconIndex = potion.getStatusIconIndex();
                GlStateManager.enableBlend();
                Gui.drawModalRectWithCustomSizedTexture(baseX + offsetX - 17, (int) drawY + 16 - offsetY + getHeight(),
                        iconIndex % 8 * 18, 198 + iconIndex / 8 * 18, 18, 18, 256f, 256f);
                GlStateManager.disableBlend();
            }

            float ratio = (float) effect.getDuration() / (float) (potionMaxDurations.getOrDefault(id, 1));
            int cx = baseX + offsetX - 20;
            int cy = (int) drawY + 16 - offsetY + getHeight() - 3;

            // 所有圆环透明度均乘以progress
            circle(cx, cy, 27, ratio * 360, false, new Color(fadedCircle.getRed(), fadedCircle.getGreen(), fadedCircle.getBlue(), (int)(30 * progress)));
            circle(cx, cy, 25.5, ratio * 360, false, new Color(fadedCircle.getRed(), fadedCircle.getGreen(), fadedCircle.getBlue(), (int)(60 * progress)));
            circle(cx, cy, 24.5, ratio * 360, false, new Color(fadedCircle.getRed(), fadedCircle.getGreen(), fadedCircle.getBlue(), (int)(100 * progress)));
            circle(cx, cy, 24, ratio * 360, false, new Color(0, 0, 0, (int)(70 * progress)));
            circle(cx, cy, 24, ratio * 360, false, fadedCircle);

            drawFont(name, baseX + offsetX + 7, (int) drawY + 16 - offsetY + getHeight(), fadedFont.getRGB(), true);
            drawFont(duration, baseX + offsetX + 7, (int) drawY + 27 - offsetY + getHeight(), new Color(255, 255, 255, alpha).getRGB(), true);

            currentY += effectiveHeight;
        }

        // GlowCircle模式没有背景矩形，无需处理
    }

    private void renderBarMode(List<PotionEffect> renderList, HUD hud, int baseX, int baseY) {
        int offsetY = 28;
        int progressBarWidth = 100;
        int progressBarHeight = 4;
        float rowHeight = offsetY;
        float currentY = baseY;

        float maxProgress = 0f;
        for (PotionEffect effect : renderList) {
            AnimData anim = animationMap.get(effect.getPotionID());
            if (anim != null) maxProgress = Math.max(maxProgress, anim.progress);
        }

        for (PotionEffect effect : renderList) {
            int id = effect.getPotionID();
            AnimData anim = animationMap.get(id);
            if (anim == null) continue;
            float progress = anim.progress;
            if (progress <= 0.01f) continue;

            float effectiveHeight = rowHeight * progress;
            float drawY = currentY;

            String name = getPotionName(effect);
            String duration = net.minecraft.potion.Potion.getDurationString(effect);

            net.minecraft.potion.Potion potion = net.minecraft.potion.Potion.potionTypes[effect.getPotionID()];
            Color hudColor = hud.getColor(System.currentTimeMillis());
            int alpha = (int) (progress * 255);
            Color fadedHud = new Color(hudColor.getRed(), hudColor.getGreen(), hudColor.getBlue(), alpha);

            if (potion.hasStatusIcon()) {
                GlStateManager.color(1.0f, 1.0f, 1.0f, 1.0f);
                mc.getTextureManager().bindTexture(new ResourceLocation("textures/gui/container/inventory.png"));
                int iconIndex = potion.getStatusIconIndex();
                GlStateManager.enableBlend();
                Gui.drawModalRectWithCustomSizedTexture(baseX, (int) drawY + 4, iconIndex % 8 * 18, 198 + iconIndex / 8 * 18, 16, 16, 256f, 256f);
                GlStateManager.disableBlend();
            }

            drawFont(name, baseX + 20, (int) drawY, fadedHud.getRGB(), true);
            drawFont(duration, baseX + 20, (int) drawY + 10, new Color(255, 255, 255, alpha).getRGB(), true);

            float ratio = (float) effect.getDuration() / (float) (potionMaxDurations.getOrDefault(id, 1));
            int filledWidth = (int) (progressBarWidth * ratio);

            drawRect(baseX + 20, (int) drawY + 22, baseX + 20 + progressBarWidth, (int) drawY + 22 + progressBarHeight, new Color(0, 0, 0, (int)(120 * progress)).getRGB());
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(770, 1);
            drawRect(baseX + 20, (int) drawY + 22, baseX + 20 + filledWidth, (int) drawY + 22 + progressBarHeight, new Color(fadedHud.getRed(), fadedHud.getGreen(), fadedHud.getBlue(), (int)(200 * progress)).getRGB());
            drawRect(baseX + 20 - 1, (int) drawY + 22 - 1, baseX + 20 + filledWidth + 1, (int) drawY + 22 + progressBarHeight + 1, new Color(fadedHud.getRed(), fadedHud.getGreen(), fadedHud.getBlue(), (int)(120 * progress)).getRGB());
            drawRect(baseX + 20 - 2, (int) drawY + 22 - 2, baseX + 20 + filledWidth + 2, (int) drawY + 22 + progressBarHeight + 2, new Color(fadedHud.getRed(), fadedHud.getGreen(), fadedHud.getBlue(), (int)(60 * progress)).getRGB());
            drawRect(baseX + 20 - 3, (int) drawY + 22 - 3, baseX + 20 + filledWidth + 3, (int) drawY + 22 + progressBarHeight + 3, new Color(fadedHud.getRed(), fadedHud.getGreen(), fadedHud.getBlue(), (int)(30 * progress)).getRGB());
            GlStateManager.blendFunc(770, 771);
            GlStateManager.disableBlend();

            currentY += effectiveHeight;
        }

        // 背景透明度随最大进度变化
        if (background.getValue() > 0 && !renderList.isEmpty() && maxProgress > 0.01f) {
            int bgAlpha = (int) (background.getValue().floatValue() / 100f * 255 * maxProgress);
            drawRoundedRectangle(baseX, baseY, baseX + 22 + progressBarWidth, (int) currentY, 4f, new Color(0, 0, 0, bgAlpha).getRGB());
        }
    }
}