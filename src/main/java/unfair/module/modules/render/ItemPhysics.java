package unfair.module.modules.render;


import unfair.module.Module;
import unfair.property.properties.IntProperty;

public class ItemPhysics extends Module {
    public ItemPhysics(){super("ItemPhysics",false,false);}
    public static IntProperty rollSpeed = new IntProperty("RollSpeed",10,1,20);
}
