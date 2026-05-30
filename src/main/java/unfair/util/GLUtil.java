package unfair.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.glu.GLU;

import java.nio.FloatBuffer;
import java.nio.IntBuffer;

public class GLUtil {
    private static final Minecraft mc = Minecraft.getMinecraft();

    public static float[] project2D(float x, float y, float z, int scaleFactor) {
        IntBuffer viewport = BufferUtils.createIntBuffer(16);
        FloatBuffer modelView = BufferUtils.createFloatBuffer(16);
        FloatBuffer projection = BufferUtils.createFloatBuffer(16);
        FloatBuffer result = BufferUtils.createFloatBuffer(4);

        GL11.glGetFloat(GL11.GL_MODELVIEW_MATRIX, modelView);
        GL11.glGetFloat(GL11.GL_PROJECTION_MATRIX, projection);
        GL11.glGetInteger(GL11.GL_VIEWPORT, viewport);

        if (GLU.gluProject(x, y, z, modelView, projection, viewport, result)) {
            ScaledResolution sr = new ScaledResolution(mc);
            return new float[]{
                    result.get(0) / scaleFactor,
                    (sr.getScaledHeight() - result.get(1) / scaleFactor),
                    result.get(2)
            };
        }
        return null;
    }
}