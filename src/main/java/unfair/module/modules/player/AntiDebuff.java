package unfair.module.modules.player;

import unfair.module.Module;
import unfair.property.properties.BooleanProperty;

public class AntiDebuff extends Module {
    public final BooleanProperty blindness = new BooleanProperty("Blindness", true);
    public final BooleanProperty nausea = new BooleanProperty("Nausea", true);

    public AntiDebuff() {
        super("AntiDebuff", false);
    }
}
