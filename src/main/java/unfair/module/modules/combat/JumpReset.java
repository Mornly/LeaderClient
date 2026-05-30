package unfair.module.modules.combat;

import net.minecraft.potion.Potion;
import unfair.Unfair;
import unfair.event.EventTarget;
import unfair.events.KnockbackEvent;
import unfair.events.LivingUpdateEvent;
import unfair.mixin.IAccessorEntity;
import unfair.module.Module;
import unfair.property.properties.BooleanProperty;
import unfair.property.properties.PercentProperty;
import unfair.util.ChatUtil;
import unfair.util.MoveUtil;
import unfair.util.RandomUtil;

import static unfair.config.Config.mc;

public class JumpReset extends Module {
    private boolean jumpFlag = false;
    public final PercentProperty chance = new PercentProperty("Chance", 100, 20, 100, null);
    public final BooleanProperty dbg = new BooleanProperty("debug", true);

    public JumpReset() {
        super("JumpReset", false);
    }

    @Override
    public String[] getSuffix() {
        return new String[]{chance.getValue() + "%"};
    }

    private boolean isInLiquidOrWeb() {
        return mc.thePlayer.isInWater() || mc.thePlayer.isInLava() || ((IAccessorEntity) mc.thePlayer).getIsInWeb();
    }

    @EventTarget
    public void onKnockback(KnockbackEvent event) {
        if (this.isEnabled()) {
            if (mc.thePlayer.hurtTime >= 7) {
                int random = RandomUtil.nextInt(1, 101);
                int threshold = chance.getValue();
                if (random <= threshold) {
                    this.jumpFlag = true;
                    if (dbg.getValue()) {
                        ChatUtil.sendFormatted(Unfair.clientName + "§7[§bJumpReset§7] §a触发 (" + random + " ≤ " + threshold + ")");
                    }
                } else {
                    if (dbg.getValue()) {
                        ChatUtil.sendFormatted(Unfair.clientName + "§7[§bJumpReset§7] §c未触发 (" + random + " > " + threshold + ")");
                    }
                }
            }
        }
    }

    @EventTarget
    public void onLivingUpdate(LivingUpdateEvent event) {
        if (this.isEnabled() && this.jumpFlag) {
            this.jumpFlag = false;
            if (mc.thePlayer.onGround && MoveUtil.isForwardPressed() && !mc.thePlayer.isPotionActive(Potion.jump) && !this.isInLiquidOrWeb()) {
                mc.thePlayer.movementInput.jump = true;
                if (this.dbg.getValue()) ChatUtil.sendFormatted(Unfair.clientName + "jump");
            }
        }
    }
}