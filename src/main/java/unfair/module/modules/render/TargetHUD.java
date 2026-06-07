package unfair.module.modules.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C02PacketUseEntity;
import net.minecraft.network.play.client.C02PacketUseEntity.Action;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;
import unfair.Unfair;
import unfair.enums.ChatColors;
import unfair.event.EventTarget;
import unfair.event.types.EventType;
import unfair.events.PacketEvent;
import unfair.events.Render2DEvent;
import unfair.font.impl.UFontRenderer;
import unfair.module.Module;
import unfair.module.modules.combat.KillAura;
import unfair.property.properties.*;
import unfair.util.ColorUtil;
import unfair.util.RenderUtil;
import unfair.util.TeamUtil;
import unfair.util.TimerUtil;
import unfair.util.shader.BlurUtils;
import unfair.util.shader.RoundedUtils;

import java.awt.*;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class TargetHUD extends Module {
    private static final Minecraft mc = Minecraft.getMinecraft();
    private static final DecimalFormat healthFormat = new DecimalFormat("0.0", new DecimalFormatSymbols(Locale.US));
    private static final DecimalFormat diffFormat = new DecimalFormat("+0.0;-0.0", new DecimalFormatSymbols(Locale.US));

    public final ModeProperty style = new ModeProperty("Style", 3, new String[]{"Myau", "Ravenbs Modern", "Ravenbs Legacy", "Leader", "Rounded", "Astolfo", "Exhibition", "Adjust"});
    public final ModeProperty font = new ModeProperty("Font", 0, new String[]{"Unfair", "Minecraft"});
    public final ModeProperty color = new ModeProperty("Color", 0, new String[]{"Default", "Hud"});
    public final ModeProperty posX = new ModeProperty("Position X", 1, new String[]{"Left", "Middle", "Right"});
    public final ModeProperty posY = new ModeProperty("Position Y", 1, new String[]{"Top", "Middle", "Bottom"});
    public final FloatProperty scale = new FloatProperty("Scale", 1.0F, 0.5F, 1.5F);
    public final IntProperty offX = new IntProperty("Offset X", 0, -255, 255);
    public final IntProperty offY = new IntProperty("Offset Y", 40, -255, 255);
    public final PercentProperty background = new PercentProperty("Background", 25);
    public final IntProperty mixFactor = new IntProperty("Mix Factor", 20, 1, 200, () -> this.style.getValue() == 4);
    public final BooleanProperty head = new BooleanProperty("Head", true, () -> this.style.getValue() == 0);
    public final BooleanProperty indicator = new BooleanProperty("Indicator", true, () -> this.style.getValue() == 0);
    public final BooleanProperty outline = new BooleanProperty("Outline", false, () -> this.style.getValue() == 0 || this.style.getValue() == 1);
    public final BooleanProperty animations = new BooleanProperty("Animations", true, () -> this.style.getValue() == 0);
    public final BooleanProperty shadow = new BooleanProperty("Shadow", true, () -> this.style.getValue() == 0);
    public final BooleanProperty kaOnly = new BooleanProperty("Ka Only", true);
    public final BooleanProperty chatPreview = new BooleanProperty("Chat Preview", false);
    public final BooleanProperty armor = new BooleanProperty("Armor", true, () -> this.style.getValue() == 7);

    private final TimerUtil lastAttackTimer = new TimerUtil();
    private final TimerUtil animTimer = new TimerUtil();
    private EntityLivingBase lastTarget = null;
    private EntityLivingBase target = null;
    private ResourceLocation headTexture = null;
    private float oldHealth = 0.0F;
    private float newHealth = 0.0F;
    private float maxHealth = 0.0F;
    private float lastHealthBar = 0.0F;
    private TimerUtil fadeTimer = null;
    private boolean fadingIn = false;
    private EntityLivingBase fadingEntity = null;

    private float animHealth = 0.0F;
    private float ghostHealth = 0.0F;
    private long lastRenderMs = 0L;
    private boolean dragging = false;
    private boolean prevMouseDown = false;
    private float dragOffsetX = 0.0F;
    private float dragOffsetY = 0.0F;

    public TargetHUD() {
        super("TargetHUD", false, true);
    }

    private UFontRenderer getCustomFontRenderer() {
        if (font.getValue() == 0) {
            return Unfair.fontManager.getFont(18);
        }
        return null;
    }

    private void drawText(String text, float x, float y, int color, boolean shadow) {
        if (font.getValue() == 1) {
            if (shadow) {
                mc.fontRendererObj.drawStringWithShadow(text, x, y, color);
            } else {
                mc.fontRendererObj.drawString(text, (int)x, (int)y, color);
            }
        } else {
            UFontRenderer fr = getCustomFontRenderer();
            if (fr != null) {
                if (shadow) {
                    fr.drawStringWithShadow(text, x, y - 2, color);
                } else {
                    fr.drawString(text, x, y - 2, color);
                }
            }
        }
    }

    private EntityLivingBase resolveTarget() {
        KillAura killAura = (KillAura) Unfair.moduleManager.modules.get(KillAura.class);
        if (killAura.isEnabled() && killAura.isAttackAllowed() && TeamUtil.isEntityLoaded(killAura.getTarget())) {
            return killAura.getTarget();
        } else if (!(Boolean) this.kaOnly.getValue()
                && !this.lastAttackTimer.hasTimeElapsed(1500L)
                && TeamUtil.isEntityLoaded(this.lastTarget)) {
            return this.lastTarget;
        } else {
            return this.chatPreview.getValue() && mc.currentScreen instanceof GuiChat ? mc.thePlayer : null;
        }
    }

    private ResourceLocation getSkin(EntityLivingBase entityLivingBase) {
        if (entityLivingBase instanceof EntityPlayer) {
            NetworkPlayerInfo playerInfo = mc.getNetHandler().getPlayerInfo(entityLivingBase.getName());
            if (playerInfo != null) {
                return playerInfo.getLocationSkin();
            }
        }
        return null;
    }

    private Color getTargetColor(EntityLivingBase entityLivingBase) {
        if (entityLivingBase == null){
            return new Color(-1);
        }
        if (entityLivingBase instanceof EntityPlayer) {
            if (TeamUtil.isFriend((EntityPlayer) entityLivingBase)) {
                return Unfair.friendManager.getColor();
            }
            if (TeamUtil.isTarget((EntityPlayer) entityLivingBase)) {
                return Unfair.targetManager.getColor();
            }
        }
        switch (this.color.getValue()) {
            case 0:
                if (!(entityLivingBase instanceof EntityPlayer)) {
                    return new Color(-1);
                }
                return TeamUtil.getTeamColor((EntityPlayer) entityLivingBase, 1.0F);
            case 1:
                int rgb = ((HUD) Unfair.moduleManager.modules.get(HUD.class)).getColor(System.currentTimeMillis()).getRGB();
                return new Color(rgb);
            default:
                return new Color(-1);
        }
    }

    private Color getAstolfoColor(int offset) {
        if (this.color.getValue() == 1) {
            HUD hud = (HUD) Unfair.moduleManager.modules.get(HUD.class);
            if (hud != null) {
                return hud.getColor(System.currentTimeMillis(), offset);
            }
        }
        return getTargetColor(target);
    }

    private void drawEntityOnScreen(int x, int y, EntityLivingBase entity) {
        GlStateManager.enableColorMaterial();
        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, 50.0F);
        float size = 16 / Math.max(entity.height / 1.8F, 1);
        GlStateManager.scale(-size, size, size);
        GlStateManager.rotate(180.0F, 0.0F, 0.0F, 1.0F);
        RenderHelper.enableStandardItemLighting();
        RenderManager rm = mc.getRenderManager();
        rm.setRenderShadow(false);
        rm.renderEntityWithPosYaw(entity, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F);
        rm.setRenderShadow(true);
        RenderHelper.disableStandardItemLighting();
        GlStateManager.popMatrix();
    }

    private void drawHorizontalGradientRect(float x, float y, float width, float height, int startColor, int endColor) {
        float startA = (float) (startColor >> 24 & 255) / 255.0F;
        float startR = (float) (startColor >> 16 & 255) / 255.0F;
        float startG = (float) (startColor >> 8 & 255) / 255.0F;
        float startB = (float) (startColor & 255) / 255.0F;
        float endA = (float) (endColor >> 24 & 255) / 255.0F;
        float endR = (float) (endColor >> 16 & 255) / 255.0F;
        float endG = (float) (endColor >> 8 & 255) / 255.0F;
        float endB = (float) (endColor & 255) / 255.0F;

        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableAlpha();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.shadeModel(7425);

        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer worldrenderer = tessellator.getWorldRenderer();
        worldrenderer.begin(7, DefaultVertexFormats.POSITION_COLOR);
        worldrenderer.pos(x, y, 0.0D).color(startR, startG, startB, startA).endVertex();
        worldrenderer.pos(x, y + height, 0.0D).color(startR, startG, startB, startA).endVertex();
        worldrenderer.pos(x + width, y + height, 0.0D).color(endR, endG, endB, endA).endVertex();
        worldrenderer.pos(x + width, y, 0.0D).color(endR, endG, endB, endA).endVertex();
        tessellator.draw();
        GlStateManager.shadeModel(7424);
        GlStateManager.disableBlend();
        GlStateManager.enableAlpha();
        GlStateManager.enableTexture2D();
    }

    private void drawExhibitionBorderedRect(float x1, float y1, float x2, float y2, float border, int fill, int out) {
        RenderUtil.drawRect(x1, y1, x2, y2, out);
        RenderUtil.drawRect(x1 + border, y1 + border, x2 - border, y2 - border, fill);
    }

    private int getExhibitionColor(int b) {
        return new Color(b, b, b, 255).getRGB();
    }

    private int getExhibitionColor(int b, int a) {
        return new Color(b, b, b, a).getRGB();
    }

    private Color blendColors(float[] fr, Color[] c, float p) {
        if (p >= 1) return c[c.length - 1];
        if (p <= 0) return c[0];
        int i = 0;
        while (fr[i + 1] <= p) i++;
        float f = (p - fr[i]) / (fr[i + 1] - fr[i]);
        return new Color((int) (c[i].getRed() + (c[i + 1].getRed() - c[i].getRed()) * f),
                (int) (c[i].getGreen() + (c[i + 1].getGreen() - c[i].getGreen()) * f),
                (int) (c[i].getBlue() + (c[i + 1].getBlue() - c[i].getBlue()) * f));
    }

    private float getTextWidth(String text) {
        if (font.getValue() == 1) {
            return mc.fontRendererObj.getStringWidth(text);
        } else {
            UFontRenderer fr = getCustomFontRenderer();
            return fr != null ? fr.getStringWidth(text) : 0;
        }
    }

    private float[] getPosition(float width, float height) {
        ScaledResolution sr = new ScaledResolution(mc);
        float x = offX.getValue().floatValue();
        float y = offY.getValue().floatValue();
        switch (posX.getValue()) {
            case 1:
                x += sr.getScaledWidth() / 2f - width / 2f;
                break;
            case 2:
                x = sr.getScaledWidth() - width - x;
                break;
        }
        switch (posY.getValue()) {
            case 1:
                y += sr.getScaledHeight() / 2f - height / 2f;
                break;
            case 2:
                y = sr.getScaledHeight() - height - y;
                break;
        }
        return new float[]{x, y};
    }

    private List<ItemStack> collectItems(EntityLivingBase entity) {
        List<ItemStack> items = new ArrayList<>();
        if (!this.armor.getValue()) {
            return items;
        }
        ItemStack held = entity.getHeldItem();
        if (held != null) {
            items.add(held);
        }
        for (int slot = 4; slot >= 1; slot--) {
            ItemStack piece = entity.getEquipmentInSlot(slot);
            if (piece != null) {
                items.add(piece);
            }
        }
        return items;
    }

    private void drawItem(ItemStack stack, float x, float y, float size) {
        float factor = size / 16.0F;
        GlStateManager.pushMatrix();
        GlStateManager.depthMask(true);
        GlStateManager.clear(GL11.GL_DEPTH_BUFFER_BIT);
        GlStateManager.enableDepth();
        RenderHelper.enableGUIStandardItemLighting();
        GlStateManager.translate(x, y, 0.0F);
        GlStateManager.scale(factor, factor, factor);
        mc.getRenderItem().zLevel = 0.0F;
        mc.getRenderItem().renderItemIntoGUI(stack, 0, 0);
        RenderHelper.disableStandardItemLighting();
        GlStateManager.disableDepth();
        GlStateManager.popMatrix();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
    }

    private void renderAdjustStyle() {
        EntityLivingBase entity = this.target;
        if (entity == null) return;

        float heal = entity.getHealth() + entity.getAbsorptionAmount();
        float maxHeal = Math.max(entity.getMaxHealth(), heal);
        float ratio = maxHeal <= 0.0F ? 0.0F : Math.min(Math.max(heal / maxHeal, 0.0F), 1.0F);

        long now = System.currentTimeMillis();
        if (this.target != this.lastTarget) {
            this.animHealth = ratio;
            this.ghostHealth = ratio;
            this.lastRenderMs = now;
        }
        float delta = Math.min((now - this.lastRenderMs) / 1000.0F, 0.1F);
        this.lastRenderMs = now;
        if (this.animations.getValue()) {
            this.animHealth += (ratio - this.animHealth) * Math.min(1.0F, delta * 9.0F);
            if (this.ghostHealth < this.animHealth) {
                this.ghostHealth = this.animHealth;
            } else {
                this.ghostHealth = Math.max(this.animHealth, this.ghostHealth - delta * 0.55F);
            }
        } else {
            this.animHealth = ratio;
            this.ghostHealth = ratio;
        }

        String name = ChatColors.formatColor(String.format("&r%s&r", TeamUtil.stripName(entity)));
        List<ItemStack> items = this.collectItems(entity);
        int itemCount = items.size();

        float pad = 3.0F;
        boolean showHead = this.head.getValue() && this.headTexture != null;
        float headGap = 4.0F;
        float nameHeight = getTextWidth("A") - 2;
        float innerGap = 2.0F;
        float itemSize = 11.0F;
        float itemGap = 1.0F;
        float gapBeforeBar = 2.0F;
        float barHeight = 3.0F;
        float barBottomPad = 3.0F;
        float barSidePad = 3.0F;

        float contentHeight = nameHeight + innerGap + itemSize;
        float height = pad + contentHeight + gapBeforeBar + barHeight + barBottomPad;
        float barY1 = height - barBottomPad - barHeight;
        float barY2 = height - barBottomPad;

        float headX = pad;
        float headY = pad;
        float headSize = barY1 - pad - headY;
        float leftWidth = showHead ? headX + headSize + headGap : pad;

        float itemsWidth = itemCount > 0 ? itemCount * (itemSize + itemGap) - itemGap : 0.0F;
        float nameWidth = getTextWidth(name);
        float rightWidth = Math.max(nameWidth, itemsWidth);
        float width = Math.max(120.0F, leftWidth + rightWidth + pad);

        float rightX = leftWidth;
        float nameY = pad;
        float itemsY = pad + nameHeight + innerGap;
        float barX1 = barSidePad;
        float barX2 = width - barSidePad;

        ScaledResolution sr = new ScaledResolution(mc);
        float sw = sr.getScaledWidth();
        float sh = sr.getScaledHeight();
        float scaleV = this.scale.getValue();

        float localX = this.offX.getValue().floatValue() / scaleV;
        switch (this.posX.getValue()) {
            case 1:
                localX += sw / scaleV / 2.0F - width / 2.0F;
                break;
            case 2:
                localX = -this.offX.getValue().floatValue() / scaleV + sw / scaleV - width;
                break;
        }
        float localY = this.offY.getValue().floatValue() / scaleV;
        switch (this.posY.getValue()) {
            case 1:
                localY += sh / scaleV / 2.0F - height / 2.0F;
                break;
            case 2:
                localY = -this.offY.getValue().floatValue() / scaleV + sh / scaleV - height;
                break;
        }

        float screenX = localX * scaleV;
        float screenY = localY * scaleV;
        float screenW = width * scaleV;
        float screenH = height * scaleV;

        boolean chatOpen = mc.currentScreen instanceof GuiChat;
        if (chatOpen) {
            float mouseX = Mouse.getX() * sw / mc.displayWidth;
            float mouseY = sh - Mouse.getY() * sh / mc.displayHeight - 1.0F;
            boolean hovered = mouseX >= screenX && mouseX <= screenX + screenW && mouseY >= screenY && mouseY <= screenY + screenH;

            boolean mouseDown = Mouse.isButtonDown(0);
            if (mouseDown && !this.prevMouseDown && hovered) {
                this.dragging = true;
                this.dragOffsetX = mouseX - screenX;
                this.dragOffsetY = mouseY - screenY;
            }
            if (!mouseDown) {
                this.dragging = false;
            }
            if (this.dragging) {
                float targetX = mouseX - this.dragOffsetX;
                float targetY = mouseY - this.dragOffsetY;
                this.offX.setValue(this.clampOffset(this.invertX(targetX, this.posX.getValue(), sw, screenW)));
                this.offY.setValue(this.clampOffset(this.invertY(targetY, this.posY.getValue(), sh, screenH)));
            }
            this.prevMouseDown = mouseDown;
        } else {
            this.dragging = false;
            this.prevMouseDown = false;
        }

        Color targetColor = this.getTargetColor(entity);
        Color barColor = this.color.getValue() == 0 ? ColorUtil.getHealthBlend(this.animHealth) : targetColor;
        int backgroundColor = new Color(0.0F, 0.0F, 0.0F, this.background.getValue() / 100.0F).getRGB();
        int trackColor = ColorUtil.darker(barColor, 0.22F).getRGB();
        float barInner = barX2 - barX1;

        GlStateManager.pushMatrix();
        GlStateManager.scale(scaleV, scaleV, scaleV);
        GlStateManager.translate(localX, localY, 0.0F);

        RenderUtil.enableRenderState();
        RenderUtil.drawRect(0.0F, 0.0F, width, height, backgroundColor);
        RenderUtil.drawRect(barX1, barY1, barX2, barY2, trackColor);
        if (this.animHealth > 0.0F) {
            RenderUtil.drawRect(barX1, barY1, barX1 + barInner * this.animHealth, barY2, barColor.getRGB() | 0xFF000000);
        }
        if (this.ghostHealth > this.animHealth + 0.001F) {
            int ghostAlpha = (int) (175.0F * Math.min(1.0F, (this.ghostHealth - this.animHealth) / 0.3F));
            int ghostColor = new Color(255, 255, 255, ghostAlpha).getRGB();
            RenderUtil.drawRect(barX1 + barInner * this.animHealth, barY1, barX1 + barInner * this.ghostHealth, barY2, ghostColor);
        }
        RenderUtil.disableRenderState();

        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.disableDepth();
        if (showHead) {
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
            mc.getTextureManager().bindTexture(this.headTexture);
            Gui.drawScaledCustomSizeModalRect((int) headX, (int) headY, 8.0F, 8.0F, 8, 8, (int) headSize, (int) headSize, 64.0F, 64.0F);
            Gui.drawScaledCustomSizeModalRect((int) headX, (int) headY, 40.0F, 8.0F, 8, 8, (int) headSize, (int) headSize, 64.0F, 64.0F);
            GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        }

        if (itemCount > 0) {
            float drawX = rightX;
            for (ItemStack stack : items) {
                this.drawItem(stack, drawX, itemsY, itemSize);
                drawX += itemSize + itemGap;
            }
        }

        GlStateManager.disableDepth();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        drawText(name, rightX, nameY, -1, this.shadow.getValue());

        if (chatOpen && (this.dragging || (Mouse.getX() * sw / mc.displayWidth >= screenX && Mouse.getX() * sw / mc.displayWidth <= screenX + screenW &&
                sh - Mouse.getY() * sh / mc.displayHeight - 1.0F >= screenY && sh - Mouse.getY() * sh / mc.displayHeight - 1.0F <= screenY + screenH))) {
            RenderUtil.enableRenderState();
            int boxColor = new Color(255, 255, 255, 235).getRGB();
            RenderUtil.drawRect(0.0F, 0.0F, width, 1.0F, boxColor);
            RenderUtil.drawRect(0.0F, height - 1.0F, width, height, boxColor);
            RenderUtil.drawRect(0.0F, 0.0F, 1.0F, height, boxColor);
            RenderUtil.drawRect(width - 1.0F, 0.0F, width, height, boxColor);
            RenderUtil.disableRenderState();
        }

        GlStateManager.enableDepth();
        GlStateManager.disableBlend();
        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
        GlStateManager.popMatrix();
    }

    private int invertX(float screenTarget, int mode, float sw, float scaledWidth) {
        switch (mode) {
            case 1:
                return Math.round(screenTarget - sw / 2.0F + scaledWidth / 2.0F);
            case 2:
                return Math.round(sw - scaledWidth - screenTarget);
            default:
                return Math.round(screenTarget);
        }
    }

    private int invertY(float screenTarget, int mode, float sh, float scaledHeight) {
        switch (mode) {
            case 1:
                return Math.round(screenTarget - sh / 2.0F + scaledHeight / 2.0F);
            case 2:
                return Math.round(sh - scaledHeight - screenTarget);
            default:
                return Math.round(screenTarget);
        }
    }

    private int clampOffset(int value) {
        return Math.max(-255, Math.min(255, value));
    }

    @EventTarget
    public void onRender(Render2DEvent event) {
        if (this.isEnabled() && mc.thePlayer != null) {
            EntityLivingBase entityLivingBase = this.target;
            this.target = this.resolveTarget();

            if (this.target != null) {
                if (entityLivingBase == null && fadeTimer == null) {
                    fadeTimer = new TimerUtil();
                    fadeTimer.reset();
                    fadingIn = true;
                } else if (fadingIn && fadeTimer != null && fadeTimer.getElapsedTime() >= 400) {
                    fadeTimer = null;
                    fadingIn = false;
                }
            } else {
                if (entityLivingBase != null && fadeTimer == null) {
                    fadeTimer = new TimerUtil();
                    fadeTimer.reset();
                    fadingIn = false;
                    fadingEntity = entityLivingBase;
                }
            }

            if (entityLivingBase != null || fadeTimer != null) {
                EntityLivingBase entity = this.target != null ? this.target : fadingEntity;
                float health = (mc.thePlayer.getHealth() + mc.thePlayer.getAbsorptionAmount()) / 2.0F;
                float abs = entity.getAbsorptionAmount() / 2.0F;
                float heal = entity.getHealth() / 2.0F + abs;

                if (entity != this.target) {
                    this.headTexture = null;
                    this.animTimer.setTime();
                    this.oldHealth = heal;
                    this.newHealth = heal;
                }
                if (!this.animations.getValue() || this.animTimer.hasTimeElapsed(150L)) {
                    this.oldHealth = this.newHealth;
                    this.newHealth = heal;
                    this.maxHealth = entity.getMaxHealth() / 2.0F;
                    if (this.oldHealth != this.newHealth) {
                        this.animTimer.reset();
                    }
                }
                ResourceLocation resourceLocation = this.getSkin(entity);
                if (resourceLocation != null) {
                    this.headTexture = resourceLocation;
                }

                int styleMode = this.style.getValue();
                if (target != null) {
                    if (styleMode == 0) {
                        drawUnfairStyle(health, abs, heal);
                    } else if (styleMode <= 2) {
                        drawRavenBSStyle(styleMode - 1, entity, health, abs, heal);
                    } else if (styleMode == 3) {
                        drawLeaderStyle(health, abs, heal);
                    } else if (styleMode == 4) {
                        drawSimplifiedRoundedTargetHUD(health, abs, heal);
                    } else if (styleMode == 5) {
                        renderAstolfo(heal);
                    } else if (styleMode == 6) {
                        renderExhibition(heal);
                    } else if (styleMode == 7) {
                        renderAdjustStyle();
                    }
                }
            }
        }
    }

    private void renderAstolfo(float heal) {
        String name = TeamUtil.stripName(target);
        float nameWidth = getTextWidth(name);
        float width = Math.max(130, nameWidth + 60);
        float height = 56;
        float[] pos = getPosition(width, height);
        float x = pos[0];
        float y = pos[1];
        int bgAlpha = (int) (this.background.getValue() / 100.0F * 255);
        int bgColor = new Color(0, 0, 0, bgAlpha).getRGB();

        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, 0);
        RenderUtil.drawRect(0, 0, width, height, bgColor);
        drawEntityOnScreen(25, 45, target);
        drawText(name, 50, 6, -1, this.shadow.getValue());

        GlStateManager.pushMatrix();
        GlStateManager.scale(1.5f, 1.5f, 1.0f);
        String healthText = String.format("%.1f", heal) + " ❤";
        int color = getAstolfoColor(0).getRGB();
        drawText(healthText, 50 / 1.5f, 22 / 1.5f, color, this.shadow.getValue());
        GlStateManager.popMatrix();

        float healthPct = MathHelper.clamp_float(heal / target.getMaxHealth(), 0, 1);
        float barWidth = width - 54;
        RenderUtil.drawRect(48, 42, 48 + barWidth, 49, ColorUtil.darker(getAstolfoColor(0), 0.3f).getRGB());
        if (healthPct > 0) {
            float fillWidth = barWidth * healthPct;
            drawHorizontalGradientRect(48, 42, fillWidth, 7, getAstolfoColor(0).getRGB(), getAstolfoColor((int) (fillWidth * 2)).getRGB());
        }
        GlStateManager.popMatrix();
    }

    private void renderExhibition(float heal) {
        String name = TeamUtil.stripName(target);
        float nameWidth = getTextWidth(name);
        float width = Math.max(120, nameWidth + 50);
        float height = 40;
        float[] pos = getPosition(width, height);
        float x = pos[0];
        float y = pos[1];
        int bgAlpha = (int) (this.background.getValue() / 100.0F * 255);
        int bgColor = new Color(0, 0, 0, bgAlpha).getRGB();

        GlStateManager.pushMatrix();
        GlStateManager.translate(x, y, 0);
        drawExhibitionBorderedRect(-2.5f, -2.5f, width + 2.5f, height + 2.5f, 0.5f, getExhibitionColor(60), getExhibitionColor(10));
        drawExhibitionBorderedRect(-1.5f, -1.5f, width + 1.5f, height + 1.5f, 1.5f, getExhibitionColor(60), getExhibitionColor(40));
        drawExhibitionBorderedRect(0, 0, width, height, 0.5f, getExhibitionColor(22), getExhibitionColor(60));
        drawExhibitionBorderedRect(2, 2, 38, 38, 0.5f, getExhibitionColor(0, 0), getExhibitionColor(10));
        drawExhibitionBorderedRect(2.5f, 2.5f, 37.5f, 37.5f, 0.5f, getExhibitionColor(17), getExhibitionColor(48));
        drawEntityOnScreen(20, 36, target);

        drawText(name, 46, 4, -1, this.shadow.getValue());

        float pct = MathHelper.clamp_float(heal / target.getMaxHealth(), 0, 1);
        Color hpColor = blendColors(new float[]{0f, 0.5f, 1f}, new Color[]{Color.RED, Color.YELLOW, Color.GREEN}, pct);
        RenderUtil.drawRect(42, 12, width - 8, 16, getExhibitionColor(0, 0));
        RenderUtil.drawRect(42.5f, 12.5f, 42.5f + (width - 51f) * pct, 15.5f, hpColor.getRGB());

        String infoText = "HP: " + (int) heal + " | Dist: " + (int) mc.thePlayer.getDistanceToEntity(target);
        drawText(infoText, 46, 19, -1, this.shadow.getValue());
        GlStateManager.popMatrix();
    }

    private void drawSimplifiedRoundedTargetHUD(float health, float abs, float heal) {
        float elapsedTime = (float) Math.min(Math.max(this.animTimer.getElapsedTime(), 0L), 150L);
        float lerpedHealthRatio = Math.min(Math.max(
                RenderUtil.lerpFloat(this.newHealth, this.oldHealth, elapsedTime / 150.0F) / this.maxHealth,
                0.0F), 1.0F);

        ScaledResolution sr = new ScaledResolution(mc);
        String targetNameText = ChatColors.formatColor(String.format("&r%s&r", TeamUtil.stripName(this.target)));
        int targetNameWidth = RenderUtil.getWidth(targetNameText);

        final float HEAD_SIZE = 32.0F;
        final float PADDING = 6.0F;
        final float HEAD_OFFSET = PADDING + HEAD_SIZE + 6.0F;
        final float BAR_HEIGHT = 6.0F;
        final float NAME_Y = PADDING + 2.0F;
        final float BAR_Y = PADDING + HEAD_SIZE + 4.0F;
        final float BG_HEIGHT = BAR_Y + BAR_HEIGHT + PADDING;
        final float CORNER_RADIUS = 8.0F;

        float minWidth = 220.0F;
        float contentWidth = Math.max(minWidth, HEAD_OFFSET + targetNameWidth + PADDING);

        float posX = this.offX.getValue().floatValue() / this.scale.getValue();
        switch (this.posX.getValue()) {
            case 1: posX += sr.getScaledWidth() / this.scale.getValue() / 2.0F - contentWidth / 2.0F; break;
            case 2: posX = -posX + sr.getScaledWidth() / this.scale.getValue() - contentWidth; break;
        }
        float posY = this.offY.getValue().floatValue() / this.scale.getValue();
        switch (this.posY.getValue()) {
            case 1: posY += sr.getScaledHeight() / this.scale.getValue() / 2.0F - BG_HEIGHT / 2.0F; break;
            case 2: posY = -posY + sr.getScaledHeight() / this.scale.getValue() - BG_HEIGHT; break;
        }
        Color targetColor = this.getTargetColor(this.target);
        int backgroundColor = new Color(0F, 0F, 0F, this.background.getValue() / 100F).getRGB();

        GlStateManager.pushMatrix();
        GlStateManager.scale(this.scale.getValue(), this.scale.getValue(), 0F);
        GlStateManager.translate(posX, posY, -450F);
        RenderUtil.enableRenderState();
        RenderUtil.drawRoundedRectangle(0F, 0F, contentWidth, BG_HEIGHT, CORNER_RADIUS, backgroundColor);
        float barLeft = HEAD_OFFSET;
        float barRight = contentWidth - PADDING;
        float barBgTop = BAR_Y;
        float barBgBottom = BAR_Y + BAR_HEIGHT;
        RenderUtil.drawRect(barLeft, barBgTop - 8, barRight, barBgBottom - 8,
                ColorUtil.darker(this.color.getValue() == 0 ? ColorUtil.getHealthBlend(lerpedHealthRatio) : targetColor, 0.2F).getRGB());

        float fillWidth = lerpedHealthRatio * (barRight - barLeft);
        if (fillWidth > 0) {
            int segments = Math.max(1, (int)(fillWidth / 4F));
            float segWidth = fillWidth / segments;
            for (int i = 0; i < segments; i++) {
                float segStart = barLeft + i * segWidth;
                float segEnd = segStart + segWidth;
                float ratio = (i + 0.5F) / segments * lerpedHealthRatio;
                Color segColor = this.color.getValue() == 0
                        ? ColorUtil.getHealthBlend(ratio)
                        : ((HUD) Unfair.moduleManager.modules.get(HUD.class)).getColor(System.currentTimeMillis() + (long) i * mixFactor.getValue());
                RenderUtil.drawRect(segStart, barBgTop - 8, segEnd, barBgBottom - 8, segColor.getRGB());
            }
        }
        String hpText = String.format("%.0f%%", lerpedHealthRatio * 100);
        int hpTextWidth = RenderUtil.getWidth(hpText);
        int hpTextX = (int)(barRight - hpTextWidth - 78);
        int hpTextY = (int)(barBgTop + (BAR_HEIGHT - 10) / 2 - 8);
        drawText(hpText, hpTextX, hpTextY, -1, this.shadow.getValue());

        RenderUtil.disableRenderState();
        GlStateManager.disableDepth();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        drawText(targetNameText, HEAD_OFFSET, NAME_Y + 2, -1, this.shadow.getValue());

        if (this.head.getValue() && this.headTexture != null) {
            GlStateManager.color(1F, 1F, 1F);
            mc.getTextureManager().bindTexture(this.headTexture);
            Gui.drawScaledCustomSizeModalRect(6, 10, 8F, 8F, 8, 8, (int)HEAD_SIZE, (int)HEAD_SIZE, 64F, 64F);
            GlStateManager.color(1F, 1F, 1F);
        }

        GlStateManager.disableBlend();
        GlStateManager.enableDepth();
        GlStateManager.popMatrix();
    }

    private void drawLeaderStyle(float health, float abs, float heal) {
        float elapsedTime = (float) Math.min(Math.max(this.animTimer.getElapsedTime(), 0L), 150L);
        float lerpedHealthRatio = Math.min(Math.max(RenderUtil.lerpFloat(this.newHealth, this.oldHealth, elapsedTime / 150.0F) / this.maxHealth, 0.0F), 1.0F);

        Color targetColor = this.getTargetColor(this.target);
        Color healthBarColor = this.color.getValue() == 0 ? ColorUtil.getHealthBlend(lerpedHealthRatio) : targetColor;

        ScaledResolution scaledResolution = new ScaledResolution(mc);
        String targetNameText = ChatColors.formatColor(String.format("&r%s&r", TeamUtil.stripName(this.target)));
        int targetNameWidth = RenderUtil.getWidth(targetNameText);

        String healthText = ChatColors.formatColor(
                String.format("&r&f%s%s❤&r", healthFormat.format(heal), abs > 0.0F ? "&6" : "&c")
        );
        int healthTextWidth = RenderUtil.getWidth(healthText);
        float barContentWidth = Math.max((float) targetNameWidth, (float) healthTextWidth) + 10.0F;

        float headIconOffset = this.headTexture != null ? 25.0F : 0.0F;
        float barTotalWidth = headIconOffset + barContentWidth + 6.0F;
        float barHeight = 5.0F;

        float posX = this.offX.getValue().floatValue() / this.scale.getValue();
        switch (this.posX.getValue()) {
            case 1:
                posX += (float) scaledResolution.getScaledWidth() / this.scale.getValue() / 2.0F - barTotalWidth / 2.0F;
                break;
            case 2:
                posX *= -1.0F;
                posX += (float) scaledResolution.getScaledWidth() / this.scale.getValue() - barTotalWidth;
        }

        float posY = this.offY.getValue().floatValue() / this.scale.getValue();
        switch (this.posY.getValue()) {
            case 1:
                posY += (float) scaledResolution.getScaledHeight() / this.scale.getValue() / 2.0F - 15.0F;
                break;
            case 2:
                posY *= -1.0F;
                posY += (float) scaledResolution.getScaledHeight() / this.scale.getValue() - 30.0F;
        }

        GlStateManager.pushMatrix();
        GlStateManager.scale(this.scale.getValue(), this.scale.getValue(), 0.0F);
        GlStateManager.translate(posX, posY, -450.0F);

        RenderUtil.enableRenderState();

        int backgroundColor = new Color(0.0F, 0.0F, 0.0F, (float) this.background.getValue() / 100.0F).getRGB();
        int outlineColor = new Color(0, 0, 0, 0).getRGB();

        RenderUtil.drawOutlineRect(0.0F, 0.0F, barTotalWidth, 22.0F, 1.0F, backgroundColor, outlineColor);

        float barStartX = headIconOffset + 2.0F;
        float barStartY = 16.0F;
        float barEndX = barTotalWidth - 2.0F;
        float barEndY = barStartY + barHeight;

        RenderUtil.drawRect(barStartX, barStartY, barEndX, barEndY, ColorUtil.darker(healthBarColor, 0.3F).getRGB());

        drawGlowingHealthBar(barStartX, barStartY, barEndX, barEndY, lerpedHealthRatio, healthBarColor);

        RenderUtil.disableRenderState();
        GlStateManager.disableDepth();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        drawText(targetNameText, headIconOffset + 2.0F, 3, -1, this.shadow.getValue());

        if (this.headTexture != null) {
            GlStateManager.color(1.0F, 1.0F, 1.0F);
            mc.getTextureManager().bindTexture(this.headTexture);
            Gui.drawScaledCustomSizeModalRect(2, 2, 8.0F, 8.0F, 8, 8, 23, 23, 64.0F, 64.0F);
            Gui.drawScaledCustomSizeModalRect(2, 2, 40.0F, 8.0F, 8, 8, 23, 23, 64.0F, 64.0F);
            GlStateManager.color(1.0F, 1.0F, 1.0F);
        }

        GlStateManager.disableBlend();
        GlStateManager.enableDepth();
        GlStateManager.popMatrix();
    }

    private void drawGlowingHealthBar(float startX, float startY, float endX, float endY, float healthRatio, Color healthBarColor) {
        float healthBarWidth = (endX - startX) * healthRatio;
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE);
        drawGlowLayer_(startX, startY, startX + healthBarWidth, endY, healthBarColor, 0.15F, 3.0F);
        drawGlowLayer_(startX, startY, startX + healthBarWidth, endY, healthBarColor, 0.25F, 1.5F);
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        RenderUtil.drawRect(startX, startY, startX + healthBarWidth, endY, healthBarColor.getRGB());
        drawHighlight_(startX, startY, startX + healthBarWidth, endY, healthBarColor);
        GlStateManager.disableBlend();
    }

    private void drawGlowLayer_(float startX, float startY, float endX, float endY, Color color, float alpha, float expandSize) {
        Color glowColor = new Color(color.getRed() / 255.0F, color.getGreen() / 255.0F, color.getBlue() / 255.0F, alpha);
        RenderUtil.drawRect(
                startX - expandSize,
                startY - expandSize,
                endX + expandSize,
                endY + expandSize,
                glowColor.getRGB()
        );
    }

    private void drawHighlight_(float startX, float startY, float endX, float endY, Color color) {
        float highlightHeight = (endY - startY) / 3.0F;
        Color highlightColor = new Color(
                Math.min(color.getRed() + 80, 255) / 255.0F,
                Math.min(color.getGreen() + 80, 255) / 255.0F,
                Math.min(color.getBlue() + 80, 255) / 255.0F,
                0.4F
        );
        RenderUtil.drawRect(startX, startY, endX, startY + highlightHeight, highlightColor.getRGB());
    }

    private void drawUnfairStyle(float health, float abs, float heal) {
        float elapsedTime = (float) Math.min(Math.max(this.animTimer.getElapsedTime(), 0L), 150L);
        float lerpedHealthRatio = Math.min(Math.max(RenderUtil.lerpFloat(this.newHealth, this.oldHealth, elapsedTime / 150.0F) / this.maxHealth, 0.0F), 1.0F);
        Color targetColor = this.getTargetColor(this.target);
        Color healthBarColor = this.color.getValue() == 0 ? ColorUtil.getHealthBlend(lerpedHealthRatio) : targetColor;
        float healthDeltaRatio = Math.min(Math.max((health - heal + 1.0F) / 2.0F, 0.0F), 1.0F);
        Color healthDeltaColor = ColorUtil.getHealthBlend(healthDeltaRatio);
        ScaledResolution scaledResolution = new ScaledResolution(mc);
        String targetNameText = ChatColors.formatColor(String.format("&r%s&r", TeamUtil.stripName(this.target)));
        int targetNameWidth = RenderUtil.getWidth(targetNameText);
        String healthText = ChatColors.formatColor(
                String.format("&r&f%s%s❤&r", healthFormat.format(heal), abs > 0.0F ? "&6" : "&c")
        );
        int healthTextWidth = RenderUtil.getWidth(healthText);
        String statusText = ChatColors.formatColor(String.format("&r&l%s&r", heal == health ? "D" : (heal < health ? "W" : "L")));
        int statusTextWidth = RenderUtil.getWidth(statusText);
        String healthDiffText = ChatColors.formatColor(
                String.format("&r%s&r", heal == health ? "0.0" : diffFormat.format(health - heal))
        );
        int healthDiffWidth = RenderUtil.getWidth(healthDiffText);
        float barContentWidth = Math.max(
                (float) targetNameWidth + (this.indicator.getValue() ? 2.0F + (float) statusTextWidth + 2.0F : 0.0F),
                (float) healthTextWidth + (this.indicator.getValue() ? 2.0F + (float) healthDiffWidth + 2.0F : 0.0F)
        );
        float headIconOffset = this.head.getValue() && this.headTexture != null ? 25.0F : 0.0F;
        float barTotalWidth = Math.max(headIconOffset + 70.0F, headIconOffset + 2.0F + barContentWidth + 2.0F);
        float posX = this.offX.getValue().floatValue() / this.scale.getValue();
        switch (this.posX.getValue()) {
            case 1:
                posX += (float) scaledResolution.getScaledWidth() / this.scale.getValue() / 2.0F - barTotalWidth / 2.0F;
                break;
            case 2:
                posX *= -1.0F;
                posX += (float) scaledResolution.getScaledWidth() / this.scale.getValue() - barTotalWidth;
        }
        float posY = this.offY.getValue().floatValue() / this.scale.getValue();
        switch (this.posY.getValue()) {
            case 1:
                posY += (float) scaledResolution.getScaledHeight() / this.scale.getValue() / 2.0F - 13.5F;
                break;
            case 2:
                posY *= -1.0F;
                posY += (float) scaledResolution.getScaledHeight() / this.scale.getValue() - 27.0F;
        }
        GlStateManager.pushMatrix();
        GlStateManager.scale(this.scale.getValue(), this.scale.getValue(), 0.0F);
        GlStateManager.translate(posX, posY, -450.0F);
        RenderUtil.enableRenderState();
        int backgroundColor = new Color(0.0F, 0.0F, 0.0F, (float) this.background.getValue() / 100.0F).getRGB();
        int outlineColor = this.outline.getValue() ? targetColor.getRGB() : new Color(0, 0, 0, 0).getRGB();
        RenderUtil.drawOutlineRect(0.0F, 0.0F, barTotalWidth, 27.0F, 1.5F, backgroundColor, outlineColor);
        RenderUtil.drawRect(headIconOffset + 2.0F, 22.0F, barTotalWidth - 2.0F, 25.0F, ColorUtil.darker(healthBarColor, 0.2F).getRGB());
        RenderUtil.drawRect(headIconOffset + 2.0F, 22.0F, headIconOffset + 2.0F + lerpedHealthRatio * (barTotalWidth - 2.0F - headIconOffset - 2.0F), 25.0F, healthBarColor.getRGB());
        RenderUtil.disableRenderState();
        GlStateManager.disableDepth();
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        drawText(targetNameText, headIconOffset + 2.0F, 2, -1, this.shadow.getValue());
        drawText(healthText, headIconOffset + 2.0F, 12, -1, this.shadow.getValue());
        if (this.indicator.getValue()) {
            drawText(statusText, barTotalWidth - 2.0F - statusTextWidth, 2, healthDeltaColor.getRGB(), this.shadow.getValue());
            drawText(healthDiffText, barTotalWidth - 2.0F - healthDiffWidth, 12, ColorUtil.darker(healthDeltaColor, 0.8F).getRGB(), this.shadow.getValue());
        }
        if (this.head.getValue() && this.headTexture != null) {
            GlStateManager.color(1.0F, 1.0F, 1.0F);
            mc.getTextureManager().bindTexture(this.headTexture);
            Gui.drawScaledCustomSizeModalRect(2, 2, 8.0F, 8.0F, 8, 8, 23, 23, 64.0F, 64.0F);
            Gui.drawScaledCustomSizeModalRect(2, 2, 40.0F, 8.0F, 8, 8, 23, 23, 64.0F, 64.0F);
            GlStateManager.color(1.0F, 1.0F, 1.0F);
        }
        GlStateManager.disableBlend();
        GlStateManager.enableDepth();
        GlStateManager.popMatrix();
    }

    private void drawRavenBSStyle(int mode, EntityLivingBase entity, float health, float abs, float heal) {
        String playerInfo = entity.getDisplayName().getFormattedText();
        double healthRatio = entity.getHealth() / entity.getMaxHealth();
        if (entity.isDead) {
            healthRatio = 0;
        }
        String healthStr = String.format("%.1f", heal);
        playerInfo += " §c" + healthStr;

        if (this.indicator.getValue()) {
            playerInfo += " " + ((healthRatio <= health / mc.thePlayer.getMaxHealth()) ? "§aW" : "§cL");
        }

        int alpha = 255;
        if (fadeTimer != null) {
            long elapsed = fadeTimer.getElapsedTime();
            if (elapsed < 400) {
                if (fadingIn) {
                    alpha = (int) ((elapsed / 400.0f) * 255);
                } else {
                    alpha = (int) (255 - (elapsed / 400.0f) * 255);
                }
            } else {
                alpha = fadingIn ? 255 : 0;
                if (!fadingIn) {
                    this.target = null;
                    fadeTimer = null;
                    fadingEntity = null;
                    return;
                }
            }
        }

        final ScaledResolution scaledResolution = new ScaledResolution(mc);
        final int padding = 8;
        final int targetStrWithPadding = RenderUtil.getWidth(playerInfo) + padding;
        final int x = (scaledResolution.getScaledWidth() / 2 - targetStrWithPadding / 2) + offX.getValue();
        final int y = (scaledResolution.getScaledHeight() / 2 + 15) + offY.getValue();
        final int n6 = x - padding;
        final int n7 = y - padding;
        final int n8 = x + targetStrWithPadding;
        final int n9 = y + (RenderUtil.getHeight() + 5) - 6 + padding;

        final int maxAlphaOutline = Math.min(alpha, 110);
        final int maxAlphaBackground = Math.min(alpha, 210);

        HUD hud = (HUD) Unfair.moduleManager.modules.get(HUD.class);
        int gradientLeft = hud.getColor(System.currentTimeMillis()).getRGB();
        int gradientRight = hud.getColor(System.currentTimeMillis() + 500).getRGB();
        int[] gradientColors = new int[]{gradientLeft, gradientRight};

        switch (mode) {
            case 0:
                float bloomRadius = (fadeTimer == null) ? 2f : (2f * alpha / 255f);
                float blurRadius = (fadeTimer == null) ? 3 : (3f * alpha / 255f);
                BlurUtils.prepareBloom();
                RoundedUtils.drawRound((float) n6, (float) n7, (float) (n8 - n6), (float) (n9 + 13 - n7), 8.0f, true, new Color(0, 0, 0, maxAlphaBackground));
                BlurUtils.bloomEnd(3, bloomRadius);
                BlurUtils.prepareBlur();
                RoundedUtils.drawRound((float) n6, (float) n7, (float) (n8 - n6), (float) (n9 + 13 - n7), 8.0f, true, new Color(RenderUtil.mergeAlpha(Color.black.getRGB(), maxAlphaOutline)));
                BlurUtils.blurEnd(2, blurRadius);
                break;
            case 1:
                RenderUtil.drawRoundedGradientOutlinedRectangle((float) n6, (float) n7, (float) n8, (float) (n9 + 13), 10.0f,
                        RenderUtil.mergeAlpha(Color.black.getRGB(), maxAlphaOutline),
                        RenderUtil.mergeAlpha(gradientColors[0], alpha),
                        RenderUtil.mergeAlpha(gradientColors[1], alpha));
                break;
        }

        final int n13 = n6 + 6;
        final int n14 = n8 - 6;
        final int n15 = n9;

        RenderUtil.drawRoundedRectangle((float) n13, (float) n15, (float) n14, (float) (n15 + 5), 4.0f,
                RenderUtil.mergeAlpha(Color.black.getRGB(), maxAlphaOutline));

        int mergedGradientLeft = RenderUtil.mergeAlpha(gradientColors[0], maxAlphaBackground);
        int mergedGradientRight = RenderUtil.mergeAlpha(gradientColors[1], maxAlphaBackground);

        float healthBar = (float) (n14 + (n13 - n14) * (1 - healthRatio));

        if (lastHealthBar != healthBar && lastHealthBar - n13 >= 3) {
            float diff = lastHealthBar - healthBar;
            if (diff > 0) {
                lastHealthBar = lastHealthBar - diff * 0.1f;
            } else {
                lastHealthBar = lastHealthBar + (-diff) * 0.1f;
            }
        } else {
            lastHealthBar = healthBar;
        }

        if (lastHealthBar > n14) {
            lastHealthBar = n14;
        }

        switch (mode) {
            case 0:
                RenderUtil.drawRoundedRectangle((float) n13, (float) n15, lastHealthBar, (float) (n15 + 5), 4.0f,
                        RenderUtil.darkenColor(mergedGradientRight, 25));
                RenderUtil.drawRoundedGradientRect((float) n13, (float) n15, healthBar, (float) (n15 + 5), 4.0f,
                        mergedGradientLeft, mergedGradientLeft, mergedGradientRight, mergedGradientRight);
                break;
            case 1:
                RenderUtil.drawRoundedGradientRect((float) n13, (float) n15, lastHealthBar, (float) (n15 + 5), 4.0f,
                        mergedGradientLeft, mergedGradientLeft, mergedGradientRight, mergedGradientRight);
                break;
        }

        GL11.glPushMatrix();
        GL11.glEnable(GL11.GL_BLEND);
        drawText(playerInfo, x, y, (new Color(220, 220, 220, 255).getRGB() & 0xFFFFFF) | Math.min(alpha + 15, 255) << 24, true);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glPopMatrix();
    }

    @EventTarget
    public void onPacket(PacketEvent event) {
        if (event.getType() == EventType.SEND && event.getPacket() instanceof C02PacketUseEntity) {
            C02PacketUseEntity packet = (C02PacketUseEntity) event.getPacket();
            if (packet.getAction() != Action.ATTACK) {
                return;
            }
            Entity entity = packet.getEntityFromWorld(mc.theWorld);
            if (entity instanceof EntityLivingBase) {
                if (entity instanceof EntityArmorStand) {
                    return;
                }
                this.lastAttackTimer.reset();
                this.lastTarget = (EntityLivingBase) entity;
            }
        }
    }
}