package unfair.module.modules.render;


import unfair.module.Module;
import unfair.property.properties.BooleanProperty;
import unfair.property.properties.FloatProperty;
import unfair.property.properties.ModeProperty;

public class Animations extends Module {

    public final ModeProperty mode = new ModeProperty(
            "mode",
            2,
            new String[]{
                    "Test",
                    "1_8",
                    "Hide",
                    "Swing",
                    "Old",
                    "Push",
                    "Dash",
                    "Slash",
                    "Slide",
                    "Scale",
                    "Swank",
                    "Swang",
                    "Swonk",
                    "Stella",
                    "Small",
                    "Edit",
                    "Rhys",
                    "Stab",
                    "Float",
                    "Remix",
                    "Avatar",
                    "Xiv",
                    "Winter",
                    "Yamato",
                    "SlideSwing",
                    "SmallPush",
                    "Reverse",
                    "Invent",
                    "Leaked",
                    "Aqua",
                    "Astro",
                    "Fadeaway",
                    "Astolfo",
                    "AstolfoSpin",
                    "Moon",
                    "MoonPush",
                    "Smooth",
                    "Jigsaw",
                    "Tap1",
                    "Tap2",
                    "Sigma3",
                    "Sigma4"
            }
    );
    public final BooleanProperty cancelEquip = new BooleanProperty("Cancel Equip", false);
    public final BooleanProperty cancelEquipBlockingOnly = new BooleanProperty("Cancel Equip Blocking Only", true, () -> this.cancelEquip.getValue());
    public final FloatProperty itemSize = new FloatProperty("Item Size", 0.0F, -0.5F, 0.5F);
    public final FloatProperty itemFov = new FloatProperty("Item Fov", 0.0F, -5.0F, 5.0F);
    public final FloatProperty itemPosX = new FloatProperty("Item Pos X", 0.0F, -1.0F, 1.0F);
    public final FloatProperty itemPosY = new FloatProperty("Item Pos Y", 0.0F, -1.0F, 1.0F);
    public final FloatProperty itemPosZ = new FloatProperty("Item Pos Z", 0.0F, -1.0F, 1.0F);
    public final FloatProperty blockPosX = new FloatProperty("Block Pos X", 0.0F, -1.0F, 1.0F);
    public final FloatProperty blockPosY = new FloatProperty("Block Pos Y", 0.0F, -1.0F, 1.0F);
    public final FloatProperty blockPosZ = new FloatProperty("Block Pos Z", 0.0F, -1.0F, 1.0F);

    public Animations() {
        super("Animations", false);
    }

    @Override
    public String[] getSuffix() {
        return new String[]{this.mode.getModeString()};
    }
}
