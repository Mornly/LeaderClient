package unfair.module.modules.render;

import net.minecraft.client.Minecraft;
import org.lwjgl.input.Keyboard;
import unfair.module.Module;
import unfair.property.properties.BooleanProperty;
import unfair.ui.clickgui.dropdown.DropDownGui;

public class GuiModule extends Module {
    private static final Minecraft mc = Minecraft.getMinecraft();
    public final BooleanProperty blur = new BooleanProperty("blur", false);
    private DropDownGui dropDownGui;

    public GuiModule() {
        super("ClickGui", false);
        setKey(Keyboard.KEY_RSHIFT);
    }

    @Override
    public void onEnabled() {
        setEnabled(false);
        if (dropDownGui == null) {
            dropDownGui = new DropDownGui();
        }
        mc.displayGuiScreen(dropDownGui);
    }
}
