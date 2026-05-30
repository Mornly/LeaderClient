package unfair.module.modules.combat;

import net.minecraft.potion.Potion;
import unfair.Unfair;
import unfair.event.EventTarget;
import unfair.events.KnockbackEvent;
import unfair.events.LivingUpdateEvent;
import unfair.mixin.IAccessorEntity;
import unfair.module.Module;
import unfair.property.properties.BooleanProperty;
<<<<<<< HEAD
import unfair.property.properties.PercentProperty;
import unfair.util.ChatUtil;
import unfair.util.MoveUtil;
import unfair.util.RandomUtil;
=======
import unfair.util.ChatUtil;
import unfair.util.MoveUtil;
>>>>>>> 839a5315ef498d98d4be72e8b3f4e7cc0c660d5c

import static unfair.config.Config.mc;

public class JumpReset extends Module {
    private boolean jumpFlag = false;
<<<<<<< HEAD
    public final PercentProperty chance = new PercentProperty("Chance", 100, 20, 100, null);
=======

>>>>>>> 839a5315ef498d98d4be72e8b3f4e7cc0c660d5c
    public final BooleanProperty dbg = new BooleanProperty("debug", true);

    public JumpReset() {
        super("JumpReset", false);
    }

<<<<<<< HEAD
    @Override
    public String[] getSuffix() {
        return new String[]{chance.getValue() + "%"};
    }

=======
>>>>>>> 839a5315ef498d98d4be72e8b3f4e7cc0c660d5c
    private boolean isInLiquidOrWeb() {
        return mc.thePlayer.isInWater() || mc.thePlayer.isInLava() || ((IAccessorEntity) mc.thePlayer).getIsInWeb();
    }

    @EventTarget
    public void onKnockback(KnockbackEvent event) {
        if (this.isEnabled()) {
            if (mc.thePlayer.hurtTime >= 7) {
<<<<<<< HEAD
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
=======
                this.jumpFlag = true;
>>>>>>> 839a5315ef498d98d4be72e8b3f4e7cc0c660d5c
            }
        }
    }

    @EventTarget
    public void onLivingUpdate(LivingUpdateEvent event) {
        if (this.isEnabled() && this.jumpFlag) {
            this.jumpFlag = false;
            if (mc.thePlayer.onGround && MoveUtil.isForwardPressed() && !mc.thePlayer.isPotionActive(Potion.jump) && !this.isInLiquidOrWeb()) {
                mc.thePlayer.movementInput.jump = true;
<<<<<<< HEAD
                if (this.dbg.getValue()) ChatUtil.sendFormatted(Unfair.clientName + "jump");
            }
        }
    }
}
=======
                if(this.dbg.getValue()) ChatUtil.sendFormatted(Unfair.clientName + "jump");
            }
        }

    }
}
>>>>>>> 839a5315ef498d98d4be72e8b3f4e7cc0c660d5c
