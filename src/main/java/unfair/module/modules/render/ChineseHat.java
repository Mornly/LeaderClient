package unfair.module.modules.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.Render;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.Vec3;
import unfair.event.EventTarget;
import unfair.events.Render3DEvent;
import unfair.module.Module;
import unfair.property.properties.*;
import unfair.util.RenderUtil;
import unfair.util.TeamUtil;

import java.awt.*;
import java.lang.reflect.Field;
import java.util.List;
import java.util.stream.Collectors;

public class ChineseHat extends Module {
    private static final Minecraft mc = Minecraft.getMinecraft();

    private final ModeProperty colorMode = new ModeProperty("Color Mode", 0, new String[]{"Custom", "Distance Color", "Rainbow"});
    private final ColorProperty customColor = new ColorProperty("Color", new Color(0, 160, 255, 150).getRGB(), () -> colorMode.getValue() == 0);
    private final PercentProperty opacity = new PercentProperty("Opacity", 100, 0, 100, null);
    private final FloatProperty playerHeight = new FloatProperty("Player Height", 0.5f, 0.25f, 2.0f);
    private final FloatProperty coneWidth = new FloatProperty("Cone Width", 0.5f, 0.0f, 2.0f);
    private final FloatProperty coneHeight = new FloatProperty("Cone Height", 0.5f, 0.1f, 2.0f);
    private final BooleanProperty renderSelf = new BooleanProperty("Render Self", false);
    private final IntProperty maxRenderDistance = new IntProperty("Max Render Distance", 100, 1, 200);
    private final BooleanProperty onLook = new BooleanProperty("On Look", false);
    private final FloatProperty maxAngleDifference = new FloatProperty("Max Angle Difference", 90.0f, 5.0f, 90.0f, () -> onLook.getValue());
    private final BooleanProperty bots = new BooleanProperty("Bots", true);
    private final BooleanProperty teams = new BooleanProperty("Teams", false);
    private final BooleanProperty mobs = new BooleanProperty("Mobs", false);
    private final BooleanProperty thruBlocks = new BooleanProperty("Thru Blocks", true);

    public ChineseHat() {
        super("ChineseHat", false);
    }

    private float getEntityShadowSize(EntityLivingBase entity) {
        try {
            Render<?> render = mc.getRenderManager().getEntityRenderObject(entity);
            Field shadowSizeField = Render.class.getDeclaredField("shadowSize");
            shadowSizeField.setAccessible(true);
            return shadowSizeField.getFloat(render);
        } catch (Exception e) {
            return 0.5f;
        }
    }

    private boolean isEntityHeightVisible(EntityLivingBase entity) {
        Vec3 eyePos = mc.thePlayer.getPositionEyes(1.0f);
        Vec3 top = new Vec3(entity.posX, entity.posY + entity.height, entity.posZ);
        Vec3 bottom = new Vec3(entity.posX, entity.posY, entity.posZ);
        return mc.theWorld.rayTraceBlocks(eyePos, top) == null || mc.theWorld.rayTraceBlocks(eyePos, bottom) == null;
    }

    private boolean isValidTarget(EntityLivingBase entity) {
        if (entity == mc.thePlayer) return renderSelf.getValue();
        if (entity.deathTime > 0) return false;

        if (entity instanceof EntityMob || entity instanceof EntitySlime) {
            if (!mobs.getValue()) return false;
        }

        float distSq = (float) mc.thePlayer.getDistanceSqToEntity(entity);
        float maxDistSq = maxRenderDistance.getValue() * maxRenderDistance.getValue();
        if (distSq > maxDistSq) return false;
        if (onLook.getValue() && !isLookingOnEntity(entity, maxAngleDifference.getValue())) return false;
        if (thruBlocks.getValue() && !isEntityHeightVisible(entity)) return false;
        if (!bots.getValue() && entity instanceof EntityPlayer && TeamUtil.isBot((EntityPlayer) entity)) {
            return false;
        }
        if (teams.getValue() && entity instanceof EntityPlayer && TeamUtil.isSameTeam((EntityPlayer) entity)) {
            return false;
        }
        return true;
    }

    private boolean isLookingOnEntity(Entity entity, float maxAngle) {
        if (entity == mc.thePlayer) return true;
        Vec3 lookVec = mc.thePlayer.getLookVec();
        Vec3 toEntity = new Vec3(
                entity.posX - mc.thePlayer.posX,
                entity.posY + entity.getEyeHeight() - (mc.thePlayer.posY + mc.thePlayer.getEyeHeight()),
                entity.posZ - mc.thePlayer.posZ
        ).normalize();
        double angle = Math.toDegrees(Math.acos(lookVec.dotProduct(toEntity)));
        return angle <= maxAngle;
    }

    private Color getRainbowColor(long offset) {
        float hue = (System.currentTimeMillis() + offset) % 10000 / 10000.0f;
        return new Color(Color.HSBtoRGB(hue, 1.0f, 1.0f));
    }

    private Color getColorForEntity(EntityLivingBase entity, long index) {
        if (colorMode.getValue() == 0) {
            Color base = new Color(customColor.getValue());
            int finalAlpha = (int) (base.getAlpha() * (opacity.getValue() / 100.0f));
            return new Color(base.getRed(), base.getGreen(), base.getBlue(), Math.min(255, Math.max(0, finalAlpha)));
        } else if (colorMode.getValue() == 1) {
            float dist = mc.thePlayer.getDistanceToEntity(entity);
            int intensity = (int) Math.min(255, dist);
            int alpha = (int) (new Color(customColor.getValue()).getAlpha() * (opacity.getValue() / 100.0f));
            return new Color(255 - intensity, intensity, 0, alpha);
        } else {
            int alpha = (int) (255 * (opacity.getValue() / 100.0f));
            Color rainbow = getRainbowColor(index * 200);
            return new Color(rainbow.getRed(), rainbow.getGreen(), rainbow.getBlue(), alpha);
        }
    }

    @EventTarget
    public void onRender3D(Render3DEvent event) {
        if (!isEnabled()) return;

        List<EntityLivingBase> entities = mc.theWorld.loadedEntityList.stream()
                .filter(e -> e instanceof EntityLivingBase)
                .map(e -> (EntityLivingBase) e)
                .filter(this::isValidTarget)
                .collect(Collectors.toList());

        long[] indexHolder = {0};
        RenderUtil.drawConesForEntities(() -> {
            for (EntityLivingBase entity : entities) {
                boolean isSelf = entity == mc.thePlayer;
                if (isSelf && (!renderSelf.getValue() || mc.gameSettings.thirdPersonView == 0)) {
                    indexHolder[0]++;
                    continue;
                }

                double x = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * event.getPartialTicks() - mc.getRenderManager().viewerPosX;
                double y = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * event.getPartialTicks() + playerHeight.getValue() - mc.getRenderManager().viewerPosY;
                double z = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * event.getPartialTicks() - mc.getRenderManager().viewerPosZ;

                float shadowSize = getEntityShadowSize(entity);
                float width = shadowSize + coneWidth.getValue();
                float height = coneHeight.getValue();

                GlStateManager.pushMatrix();
                GlStateManager.translate(x, y, z);

                Color color = getColorForEntity(entity, indexHolder[0]);
                RenderUtil.color(color);
                RenderUtil.drawCone(width, height);

                GlStateManager.popMatrix();
                indexHolder[0]++;
            }
        });
    }

    @Override
    public String[] getSuffix() {
        return new String[]{colorMode.getModeString()};
    }
}