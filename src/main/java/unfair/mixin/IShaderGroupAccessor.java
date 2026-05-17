package unfair.mixin;

import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.shader.Shader;
import net.minecraft.client.shader.ShaderGroup;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.List;
@Mixin(ShaderGroup.class)
public interface IShaderGroupAccessor {
    // 映射到原有的私有字段 listShaderLayers
    @Accessor("listShaders")
    List<Shader> getListShaders();

    @Accessor("listShaders")
    void setShaderList(List<Shader> list);
}