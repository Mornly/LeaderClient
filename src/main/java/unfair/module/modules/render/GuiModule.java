package unfair.module.modules.render;

import net.minecraft.client.Minecraft;
import org.lwjgl.input.Keyboard;
import unfair.module.Module;
import unfair.property.properties.ModeProperty;
import unfair.ui.clickgui.dropdown.DropDownGui;
import unfair.ui.clickgui.panel.PanelGui;

public class GuiModule extends Module {
    private static final Minecraft mc = Minecraft.getMinecraft();
    public final ModeProperty mode = new ModeProperty("Mode", 1, new String[]{"DropDown", "Panel"});
    private DropDownGui dropDownGui;

    public GuiModule() {
        super("ClickGui", false);
        setKey(Keyboard.KEY_RSHIFT);
    }

    @Override
    public void onEnabled() {
        setEnabled(false);
        if (mode.getValue() == 0) {
            if (dropDownGui == null) dropDownGui = new DropDownGui();
            mc.displayGuiScreen(dropDownGui);
        }

        if (mode.getValue() == 1) {
            mc.displayGuiScreen(new PanelGui());
        }
    }
}
