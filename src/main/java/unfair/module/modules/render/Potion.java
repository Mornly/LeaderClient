package unfair.module.modules.render;

import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.resources.I18n;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import unfair.Unfair;
import unfair.event.EventTarget;
import unfair.events.Render2DEvent;
import unfair.module.Module;
import unfair.property.properties.FloatProperty;
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
    private final IntProperty glowLayer = new IntProperty("GlowLayers",10,1,100,() -> mode.getValue() == 1);
    private final FloatProperty glowRadius = new FloatProperty("GlowRadius",1.5F,0.1F,5F,() -> mode.getValue() == 1);
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

    // 拖拽状态
    private boolean dragging = false;
    private boolean prevMouseDown = false;
    private float dragOffsetX = 0.0F;
    private float dragOffsetY = 0.0F;

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

    // 计算当前模式下药水区域的总宽度和高度（不缩放）
    private float[] calculateAreaSize(List<PotionEffect> renderList, int modeVal) {
        float totalWidth = 0;
        float totalHeight = 0;
        if (renderList.isEmpty()) return new float[]{0, 0};

        if (modeVal == 0) { // Circle
            int offsetX = 21;
            int maxString = 0;
            for (PotionEffect effect : renderList) {
                AnimData anim = animationMap.get(effect.getPotionID());
                if (anim == null || anim.progress <= 0.01f) continue;
                String name = getPotionName(effect);
                String duration = net.minecraft.potion.Potion.getDurationString(effect);
                int w = Math.max(RenderUtil.getWidth(name), RenderUtil.getWidth(duration));
                if (w > maxString) maxString = w;
            }
            totalWidth = offsetX + maxString + 8 + offsetX - 22 + 10; // 估算
            float rowHeight = 28.8f;
            for (PotionEffect effect : renderList) {
                AnimData anim = animationMap.get(effect.getPotionID());
                if (anim != null) totalHeight += rowHeight * anim.progress;
            }
        } else if (modeVal == 1) { // GlowCircle
            int offsetX = 21;
            int maxString = 0;
            for (PotionEffect effect : renderList) {
                AnimData anim = animationMap.get(effect.getPotionID());
                if (anim == null || anim.progress <= 0.01f) continue;
                String name = getPotionName(effect);
                String duration = net.minecraft.potion.Potion.getDurationString(effect);
                int w = Math.max(RenderUtil.getWidth(name), RenderUtil.getWidth(duration));
                if (w > maxString) maxString = w;
            }
            totalWidth = offsetX + maxString + 7 + 10;
            float rowHeight = 28.8f;
            for (PotionEffect effect : renderList) {
                AnimData anim = animationMap.get(effect.getPotionID());
                if (anim != null) totalHeight += rowHeight * anim.progress;
            }
        } else if (modeVal == 2) { // Bar
            int progressBarWidth = 100;
            int nameWidth = 0;
            for (PotionEffect effect : renderList) {
                AnimData anim = animationMap.get(effect.getPotionID());
                if (anim == null || anim.progress <= 0.01f) continue;
                String name = getPotionName(effect);
                int w = RenderUtil.getWidth(name);
                if (w > nameWidth) nameWidth = w;
            }
            totalWidth = 20 + nameWidth + progressBarWidth + 10;
            float rowHeight = 28f;
            for (PotionEffect effect : renderList) {
                AnimData anim = animationMap.get(effect.getPotionID());
                if (anim != null) totalHeight += rowHeight * anim.progress;
            }
        }
        return new float[]{totalWidth, totalHeight};
    }

    // 拖拽处理函数
    private void applyDragging(float areaWidth, float areaHeight, int[] pos) {
        if (!(mc.currentScreen instanceof GuiChat)) {
            dragging = false;
            prevMouseDown = false;
            return;
        }
        ScaledResolution sr = new ScaledResolution(mc);
        float sw = sr.getScaledWidth();
        float sh = sr.getScaledHeight();
        float screenX = pos[0];
        float screenY = pos[1];
        float screenW = areaWidth;
        float screenH = areaHeight;

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
            int newX = Math.round(targetScreenX);
            int newY = Math.round(targetScreenY);
            newX = Math.max(-480, Math.min(640, newX));
            newY = Math.max(-280, Math.min(350, newY));
            postx.setValue(newX);
            posty.setValue(newY);
            pos[0] = newX;
            pos[1] = newY;
        }
        prevMouseDown = mouseDown;
    }

    @EventTarget
    public void onRender2D(Render2DEvent event) {
        if (!isEnabled() || mc.thePlayer.getActivePotionEffects().isEmpty()) return;

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

        // 计算当前模式下的渲染区域尺寸
        int modeVal = mode.getValue();
        float[] areaSize = calculateAreaSize(renderList, modeVal);
        float areaWidth = areaSize[0];
        float areaHeight = areaSize[1];

        // 拖拽处理，可能更新 baseX, baseY
        int[] pos = {baseX, baseY};
        if (areaWidth > 0 && areaHeight > 0) {
            applyDragging(areaWidth, areaHeight, pos);
            baseX = pos[0];
            baseY = pos[1];
        }

        if (modeVal == 0) {
            renderCircleMode(renderList, hud, baseX, baseY);
        } else if (modeVal == 1) {
            renderGlowCircleMode(renderList, hud, baseX, baseY);
        } else if (modeVal == 2) {
            renderBarMode(renderList, hud, baseX, baseY);
        }
    }

    // 以下三个渲染方法保持原样，只是参数 baseX, baseY 使用传入的值
    // 注意：这三个方法内部使用了 postx/posty 吗？没有，它们直接用传入的 baseX, baseY。
    // 所以拖拽后新的 baseX, baseY 会直接影响渲染位置。

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
            circle(cx, cy, 24, 360, false, new Color(0, 0, 0, (int)(70 * progress)));
            circle(cx, cy, 24, ratio * 360, false, fadedText);

            drawFont(name, baseX + offsetX + 7, (int) drawY + 16 - offsetY + getHeight(), fadedText.getRGB(), true);
            drawFont(duration, baseX + offsetX + 7, (int) drawY + 27 - offsetY + getHeight(), new Color(255, 255, 255, alpha).getRGB(), true);

            currentY += effectiveHeight;
        }
        if (background.getValue() > 0 && !renderList.isEmpty() && maxProgress > 0.01f) {
            int bgAlpha = (int) (background.getValue().floatValue() / 100f * 255 * maxProgress);
            drawRoundedRectangle(baseX + offsetX - 22, baseY - 10, baseX + maxString + 8 + offsetX, (int) currentY + baseY - 50, 4f, new Color(0, 0, 0, bgAlpha).getRGB());
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
                Gui.drawModalRectWithCustomSizedTexture(baseX + offsetX - 17,
                        (int) drawY + 16 - offsetY + getHeight(),
                        iconIndex % 8 * 18, 198 + iconIndex / 8 * 18, 18, 18, 256f, 256f);
                GlStateManager.disableBlend();
            }

            float ratio = (float) effect.getDuration() / (float) (potionMaxDurations.getOrDefault(id, 1));
            double iconCenterX = baseX + offsetX - 17 + 9;
            double iconCenterY = (int) drawY + 16 - offsetY + getHeight() + 9;
            double mainRealRadius = 12.5;
            double glowMaxRealRadius = 12.5 + glowRadius.getValue();
            GlStateManager.enableBlend();
            GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);

            int glowLayers = glowLayer.getValue();
            for (int i = 0; i < glowLayers; i++) {
                float t = (float) i / (glowLayers - 1);
                double realR = mainRealRadius + (glowMaxRealRadius - mainRealRadius) * (1.0 - t);
                int layerAlpha = (int) ((15 + 20 * t) * progress);
                if (layerAlpha > 255) layerAlpha = 255;
                Color glowColor = new Color(
                        fadedCircle.getRed(),
                        fadedCircle.getGreen(),
                        fadedCircle.getBlue(),
                        layerAlpha
                );
                double drawX = iconCenterX - realR;
                double drawY1 = iconCenterY - realR;
                double drawR = realR * 2.0;
                circle(drawX, drawY1, drawR, ratio * 360, false, glowColor);
            }

            GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            double mainDrawX = iconCenterX - mainRealRadius;
            double mainDrawY = iconCenterY - mainRealRadius;
            double mainDrawR = mainRealRadius * 2.0;
            circle(mainDrawX, mainDrawY, mainDrawR, ratio * 360, false, fadedCircle);

            GlStateManager.disableBlend();

            drawFont(name, baseX + offsetX + 7, (int) drawY + 16 - offsetY + getHeight(), fadedFont.getRGB(), true);
            drawFont(duration, baseX + offsetX + 7, (int) drawY + 27 - offsetY + getHeight(),
                    new Color(255, 255, 255, alpha).getRGB(), true);

            currentY += effectiveHeight;
        }
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

        if (background.getValue() > 0 && !renderList.isEmpty() && maxProgress > 0.01f) {
            int bgAlpha = (int) (background.getValue().floatValue() / 100f * 255 * maxProgress);
            drawRoundedRectangle(baseX, baseY, baseX + 22 + progressBarWidth, (int) currentY, 4f, new Color(0, 0, 0, bgAlpha).getRGB());
        }
    }
}