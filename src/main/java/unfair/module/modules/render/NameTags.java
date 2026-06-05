package unfair.module.modules.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.boss.EntityDragon;
import net.minecraft.entity.boss.EntityWither;
import net.minecraft.entity.monster.*;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityBat;
import net.minecraft.entity.passive.EntitySquid;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.scoreboard.Score;
import net.minecraft.scoreboard.ScoreObjective;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.EnumChatFormatting;
import org.apache.commons.lang3.StringUtils;
import org.lwjgl.opengl.GL11;
import unfair.Unfair;
import unfair.enums.BlinkModules;
import unfair.enums.ChatColors;
import unfair.event.EventTarget;
import unfair.event.types.EventType;
import unfair.events.Render2DEvent;
import unfair.events.Render3DEvent;
import unfair.events.TickEvent;
import unfair.font.impl.UFontRenderer;
import unfair.mixin.IAccessorGuiChat;
import unfair.mixin.IAccessorRenderManager;
import unfair.module.Module;
import unfair.property.properties.*;
import unfair.util.ColorUtil;
import unfair.util.RenderUtil;
import unfair.util.TeamUtil;

import java.awt.*;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class NameTags extends Module {
    private static final Minecraft mc = Minecraft.getMinecraft();
    private static final DecimalFormat healthFormatter = new DecimalFormat("0.0", new DecimalFormatSymbols(Locale.US));
    public final FloatProperty scale = new FloatProperty("scale", 1.0F, 0.5F, 2.0F);
    public final BooleanProperty autoScale = new BooleanProperty("auto-scale", true);
    public final ModeProperty font = new ModeProperty("Font", 0, new String[]{"Unfair", "Minecraft"});
    public final PercentProperty backgroundOpacity = new PercentProperty("background", 25);
    public final BooleanProperty shadow = new BooleanProperty("shadow", true);
    public final ModeProperty distanceMode = new ModeProperty("distance", 0, new String[]{"NONE", "DEFAULT", "VAPE"});
    public final ModeProperty healthMode = new ModeProperty("health", 2, new String[]{"NONE", "HP", "HEARTS", "TAB"});
    public final BooleanProperty armor = new BooleanProperty("armor", true);
    public final BooleanProperty effects = new BooleanProperty("effects", true);
    public final BooleanProperty players = new BooleanProperty("players", true);
    public final BooleanProperty friends = new BooleanProperty("friends", true);
    public final BooleanProperty enemies = new BooleanProperty("enemies", true);
    public final BooleanProperty bossees = new BooleanProperty("bosses", false);
    public final BooleanProperty mobs = new BooleanProperty("mobs", false);
    public final BooleanProperty creepers = new BooleanProperty("creepers", false);
    public final BooleanProperty endermans = new BooleanProperty("endermen", false);
    public final BooleanProperty blazes = new BooleanProperty("blazes", false);
    public final BooleanProperty animals = new BooleanProperty("animals", false);
    public final BooleanProperty self = new BooleanProperty("self", false);
    public final BooleanProperty bots = new BooleanProperty("bots", false);

    public NameTags() {
        super("NameTags", false, true);
    }

    private UFontRenderer getFontRenderer() {
        if (font.getValue() == 0) {
            return Unfair.fontManager.getFont(18);
        }
        return null;
    }

    private int getStringWidth(String text) {
        UFontRenderer fr = getFontRenderer();
        if (fr != null) {
            return fr.getStringWidth(text);
        }
        return mc.fontRendererObj.getStringWidth(text);
    }

    private void drawString(String text, float x, float y, int color, boolean shadow) {
        UFontRenderer fr = getFontRenderer();
        if (fr != null) {
            if (shadow) {
                fr.drawStringWithShadow(text, x, y, color);
            } else {
                fr.drawString(text, x, y, color);
            }
        } else {
            if (shadow) {
                mc.fontRendererObj.drawStringWithShadow(text, x, y, color);
            } else {
                mc.fontRendererObj.drawString(text, (int) x, (int) y, color);
            }
        }
    }

    private int getFontHeight() {
        UFontRenderer fr = getFontRenderer();
        if (fr != null) {
            return fr.getHeight();
        }
        return mc.fontRendererObj.FONT_HEIGHT;
    }

    public boolean shouldRenderTags(EntityLivingBase entityLivingBase) {
        if (entityLivingBase.deathTime > 0) {
            return false;
        } else if (mc.getRenderViewEntity().getDistanceToEntity(entityLivingBase) > 512.0F) {
            return false;
        } else if (entityLivingBase instanceof EntityPlayer) {
            if (entityLivingBase != mc.thePlayer && entityLivingBase != mc.getRenderViewEntity()) {
                if (TeamUtil.isBot((EntityPlayer) entityLivingBase)) {
                    return this.bots.getValue();
                } else if (TeamUtil.isFriend((EntityPlayer) entityLivingBase)) {
                    return this.friends.getValue();
                } else {
                    return TeamUtil.isTarget((EntityPlayer) entityLivingBase) ? this.enemies.getValue() : this.players.getValue();
                }
            } else {
                return this.self.getValue() && mc.gameSettings.thirdPersonView != 0;
            }
        } else if (entityLivingBase instanceof EntityDragon || entityLivingBase instanceof EntityWither) {
            return !entityLivingBase.isInvisible() && this.bossees.getValue();
        } else if (!(entityLivingBase instanceof EntityMob) && !(entityLivingBase instanceof EntitySlime)) {
            return (entityLivingBase instanceof EntityAnimal
                    || entityLivingBase instanceof EntityBat
                    || entityLivingBase instanceof EntitySquid
                    || entityLivingBase instanceof EntityVillager) && this.animals.getValue();
        } else if (entityLivingBase instanceof EntityCreeper) {
            return this.creepers.getValue();
        } else if (entityLivingBase instanceof EntityEnderman) {
            return this.endermans.getValue();
        } else {
            return entityLivingBase instanceof EntityBlaze ? this.blazes.getValue() : this.mobs.getValue();
        }
    }

    @EventTarget
    public void onRender(Render3DEvent event) {
        if (!this.isEnabled()) return;
        int fontHeight = getFontHeight();
        for (Entity entity : TeamUtil.getLoadedEntitiesSorted()) {
            if (!(entity instanceof EntityLivingBase)) continue;
            EntityLivingBase living = (EntityLivingBase) entity;
            if (!this.shouldRenderTags(living)) continue;
            if (!entity.ignoreFrustumCheck && !RenderUtil.isInViewFrustum(entity.getEntityBoundingBox(), 10.0)) continue;

            String teamName = TeamUtil.stripName(entity);
            if (StringUtils.isBlank(EnumChatFormatting.getTextWithoutFormattingCodes(teamName))) continue;

            double x = RenderUtil.lerpDouble(entity.posX, entity.lastTickPosX, event.getPartialTicks()) - ((IAccessorRenderManager) mc.getRenderManager()).getRenderPosX();
            double y = RenderUtil.lerpDouble(entity.posY, entity.lastTickPosY, event.getPartialTicks()) - ((IAccessorRenderManager) mc.getRenderManager()).getRenderPosY() + entity.getEyeHeight();
            double z = RenderUtil.lerpDouble(entity.posZ, entity.lastTickPosZ, event.getPartialTicks()) - ((IAccessorRenderManager) mc.getRenderManager()).getRenderPosZ();
            double distance = mc.getRenderViewEntity().getDistanceToEntity(entity);

            GlStateManager.pushMatrix();
            GlStateManager.translate(x, y + (entity.isSneaking() ? 0.225 : 0.4), z);
            GlStateManager.rotate(mc.getRenderManager().playerViewY * -1.0F, 0.0F, 1.0F, 0.0F);
            float view = mc.gameSettings.thirdPersonView == 2 ? -1.0F : 1.0F;
            GlStateManager.rotate(mc.getRenderManager().playerViewX, view, 0.0F, 0.0F);
            double scale = Math.pow(Math.min(Math.max(this.autoScale.getValue() ? distance : 0.0, 6.0), 128.0), 0.75) * 0.0075;
            GlStateManager.scale(-scale * this.scale.getValue(), -scale * this.scale.getValue(), 1.0);

            String distanceText = "";
            switch (this.distanceMode.getValue()) {
                case 1:
                    distanceText = String.format("&7%dm&r ", (int) distance);
                    break;
                case 2:
                    distanceText = String.format("&a[&f%d&a]&r ", (int) distance);
            }
            float health = living.getHealth();
            float absorption = living.getAbsorptionAmount();
            float max = living.getMaxHealth();
            float percent = Math.min(Math.max((health + absorption) / max, 0.0F), 1.0F);
            String healText = "";
            switch (this.healthMode.getValue()) {
                case 1:
                    healText = String.format(" %d%s", (int) health, absorption > 0.0F ? String.format(" &6%d&r", (int) absorption) : "&r");
                    break;
                case 2:
                    healText = String.format(" %s%s", healthFormatter.format(health / 2.0), absorption > 0.0F ? String.format(" &6%s&r", healthFormatter.format(absorption / 2.0)) : "&r");
                    break;
                case 3:
                    if (entity instanceof EntityPlayer) {
                        Scoreboard scoreboard = mc.theWorld.getScoreboard();
                        if (scoreboard != null) {
                            ScoreObjective objective = scoreboard.getObjectiveInDisplaySlot(2);
                            if (objective != null) {
                                Score score = scoreboard.getValueFromObjective(entity.getName(), objective);
                                if (score != null) {
                                    healText = String.format(" &e%d&r", score.getScorePoints());
                                }
                            }
                        }
                    }
            }
            String color = ChatColors.formatColor(String.format("%s&f%s&r%s", distanceText, teamName, healText));
            int width = getStringWidth(color);

            // 背景绘制（普通矩形，无圆角）
            if (this.backgroundOpacity.getValue() > 0) {
                Color textColor = !entity.isSneaking() && !entity.isInvisible()
                        ? new Color(0.0F, 0.0F, 0.0F, (float) this.backgroundOpacity.getValue() / 100.0F)
                        : new Color(0.33F, 0.0F, 0.33F, (float) this.backgroundOpacity.getValue() / 100.0F);
                RenderUtil.enableRenderState();
                float left = (float) (-width) / 2.0F - 1.0F;
                float right = (float) width / 2.0F + (this.shadow.getValue() ? 1.0F : 0.0F);
                float top = (float) (-fontHeight) - 1.0F;
                float bottom = this.shadow.getValue() ? 0.0F : -1.0F;
                RenderUtil.drawRect(left, top, right, bottom, textColor.getRGB());
                RenderUtil.disableRenderState();
            }

            GlStateManager.disableDepth();
            drawString(color, (float) (-width) / 2.0F, (float) (-fontHeight), ColorUtil.getHealthBlend(percent).getRGB(), this.shadow.getValue());
            GlStateManager.enableDepth();

            if (entity instanceof EntityPlayer) {
                EntityPlayer player = (EntityPlayer) entity;
                int height = fontHeight + 2;
                if (this.armor.getValue()) {
                    ArrayList<ItemStack> renderingItems = new ArrayList<>();
                    for (int i = 4; i >= 0; i--) {
                        ItemStack itemStack;
                        if (i == 0) {
                            itemStack = player.getHeldItem();
                        } else {
                            itemStack = player.inventory.armorInventory[i - 1];
                        }
                        if (itemStack != null) renderingItems.add(itemStack);
                    }
                    if (!renderingItems.isEmpty()) {
                        int offset = renderingItems.size() * -8;
                        for (int i = 0; i < renderingItems.size(); i++) {
                            RenderUtil.renderItemInGUI(renderingItems.get(i), offset + i * 16, -height - 16);
                        }
                        height += 16;
                    }
                }
                if (this.effects.getValue()) {
                    List<PotionEffect> effects = player.getActivePotionEffects().stream()
                            .filter(p -> Potion.potionTypes[p.getPotionID()].hasStatusIcon())
                            .collect(Collectors.toList());
                    if (!effects.isEmpty()) {
                        GlStateManager.pushMatrix();
                        GlStateManager.scale(0.5F, 0.5F, 1.0F);
                        int offset = effects.size() * -9;
                        for (int i = 0; i < effects.size(); i++) {
                            RenderUtil.renderPotionEffect(effects.get(i), offset + i * 18, -(height * 2) - 18);
                        }
                        GlStateManager.popMatrix();
                    }
                }
                if (TeamUtil.isFriend(player)) {
                    RenderUtil.enableRenderState();
                    float x1 = (float) (-width) / 2.0F - 1.0F;
                    float y1 = (float) (-fontHeight) - 1.0F;
                    float x2 = (float) width / 2.0F + 1.0F;
                    float y2 = this.shadow.getValue() ? 0.0F : -1.0F;
                    int friendColor = Unfair.friendManager.getColor().getRGB();
                    RenderUtil.drawOutlineRect(x1, y1, x2, y2, 1.5F, 0, friendColor);
                    RenderUtil.disableRenderState();
                } else if (TeamUtil.isTarget(player)) {
                    RenderUtil.enableRenderState();
                    float x1 = (float) (-width) / 2.0F - 1.0F;
                    float y1 = (float) (-fontHeight) - 1.0F;
                    float x2 = (float) width / 2.0F + 1.0F;
                    float y2 = this.shadow.getValue() ? 0.0F : -1.0F;
                    int targetColor = Unfair.targetManager.getColor().getRGB();
                    RenderUtil.drawOutlineRect(x1, y1, x2, y2, 1.5F, 0, targetColor);
                    RenderUtil.disableRenderState();
                }
            }
            GlStateManager.popMatrix();
        }
    }
}