package unfair.module.modules.misc;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.ResourceLocation;
import unfair.event.EventTarget;
import unfair.events.AttackEvent;
import unfair.events.LoadWorldEvent;
import unfair.events.UpdateEvent;
import unfair.module.Module;
import unfair.property.properties.ModeProperty;
import unfair.util.SoundUtil;

public class KillSound extends Module {
    private static final String[] SOUNDS = {"Zako", "ZhangXueFeng", "FAHHHH"};

    public final ModeProperty audio = new ModeProperty("Audio", 0, SOUNDS);

    private EntityLivingBase target;
    private boolean played;

    public KillSound() {
        super("KillSound", false);
    }

    @Override
    public String[] getSuffix() {
        return new String[]{audio.getModeString()};
    }

    @EventTarget
    public void onAttack(AttackEvent event) {
        if (!isEnabled()) return;
        Entity entity = event.getTarget();
        if (entity instanceof EntityLivingBase) {
            target = (EntityLivingBase) entity;
            played = false;
        }
    }

    @EventTarget
    public void onUpdate(UpdateEvent event) {
        if (!isEnabled() || target == null || played) return;
        if (target.isDead || target.getHealth() <= 0.0f) {
            String soundName = SOUNDS[audio.getValue()];
            SoundUtil.playSound(new ResourceLocation("minecraft", "unfair/sounds/" + soundName).toString());
            played = true;
            target = null;
        }
    }

    @EventTarget
    public void onLoadWorld(LoadWorldEvent event) {
        reset();
    }

    @Override
    public void onDisabled() {
        reset();
    }

    private void reset() {
        target = null;
        played = false;
    }
}