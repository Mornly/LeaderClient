package unfair.module.modules.render;

import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.effect.EntityLightningBolt;
import net.minecraft.init.Blocks;
import net.minecraft.util.EnumParticleTypes;
import unfair.event.EventTarget;
import unfair.events.AttackEvent;
import unfair.events.LoadWorldEvent;
import unfair.events.UpdateEvent;
import unfair.module.Module;
import unfair.property.properties.BooleanProperty;
import unfair.util.SoundUtil;

import static unfair.config.Config.mc;

public class KillEffect extends Module{
    public KillEffect(){super("KillEffect",false,false);}
    public static int killedTimes = 0;

    private final BooleanProperty lightning = new BooleanProperty("Lightning", true);

    private final BooleanProperty explosion = new BooleanProperty("Explosion", true);
    private final BooleanProperty bloodValue = new BooleanProperty("Blood", true);
    private EntityLivingBase target;
    @EventTarget
    public void onUpdate(UpdateEvent event) {
        if (this.isEnabled()){
        if (this.target != null && (!mc.theWorld.loadedEntityList.contains(this.target) || target.getHealth() <= 0)) {
            if (this.isEnabled()) {
                if (this.lightning.getValue()) {
                    final EntityLightningBolt entityLightningBolt = new EntityLightningBolt(mc.theWorld, target.posX, target.posY, target.posZ);
                    mc.theWorld.addEntityToWorld((int) (-Math.random() * 100000), entityLightningBolt);
                    SoundUtil.playSound("ambient.weather.thunder");
                }

                if (this.explosion.getValue()) {
                    for (int i = 0; i <= 8; i++) {
                        mc.effectRenderer.emitParticleAtEntity(target, EnumParticleTypes.FLAME);
                    }

                    SoundUtil.playSound("item.fireCharge.use");
                }
                if (this.bloodValue.getValue()) {
                    for (int i = 0; i < 10; ++i) {
                        mc.effectRenderer.spawnEffectParticle(EnumParticleTypes.BLOCK_CRACK.getParticleID(), target.posX, target.posY + (double) (target.height / 2.0f), target.posZ, target.motionX + (double) KillEffect.nextFloat(-0.5f, 0.5f), target.motionY + (double) KillEffect.nextFloat(-0.5f, 0.5f), target.motionZ + (double) KillEffect.nextFloat(-0.5f, 0.5f), Block.getStateId(Blocks.redstone_block.getDefaultState()));
                    }
                }
            }
                this.target = null;
            killedTimes++;
        }
        }
    }
    @EventTarget
    public void onWorld(LoadWorldEvent event){
        if (this.isEnabled()){
            this.target = null;
        }
    }
    public static float nextFloat(float startInclusive, float endInclusive) {
        if (startInclusive == endInclusive || endInclusive - startInclusive <= 0.0f) {
            return startInclusive;
        }
        return (float) ((double) startInclusive + (double) (endInclusive - startInclusive) * Math.random());
    }
    public double easeInOutCirc(double x) {
        return x < 0.5 ? (1 - Math.sqrt(1 - Math.pow(2 * x, 2))) / 2 : (Math.sqrt(1 - Math.pow(-2 * x + 2, 2)) + 1) / 2;
    }

    @EventTarget
    public void onAttack(AttackEvent event) {
        if (this.isEnabled()) {
            final Entity entity = event.getTarget();
            if (entity instanceof EntityLivingBase) {
                target = (EntityLivingBase) entity;
            }
        }
    }
}
