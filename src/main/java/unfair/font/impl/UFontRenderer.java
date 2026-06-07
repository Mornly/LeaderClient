package unfair.font.impl;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;
import unfair.Unfair;

import java.awt.*;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class UFontRenderer extends FontRenderer {
    private static final int[] COLOR_CODE = new int[32];
    private static final Map<String, Font> FONT_CACHE = new HashMap<>();

    static {
        for (int i = 0; i <= 31; i++) {
            int j = (i >> 3 & 1) * 85;
            int k = (i >> 2 & 1) * 170 + j;
            int l = (i >> 1 & 1) * 170 + j;
            int i1 = (i & 1) * 170 + j;
            if (i == 6) {
                k += 85;
            }
            if (i >= 16) {
                k /= 4;
                l /= 4;
                i1 /= 4;
            }
            COLOR_CODE[i] = (k & 255) << 16 | (l & 255) << 8 | (i1 & 255);
        }
    }

    private StringCache stringCache;
    private final int size;

    public UFontRenderer(String name, int size) {
        super(
                Minecraft.getMinecraft().gameSettings,
                new ResourceLocation("textures/font/ascii.png"),
                Minecraft.getMinecraft().getTextureManager(),
                false
        );
        this.size = size;
        Font font = loadFont(name, size);
        stringCache = new StringCache(COLOR_CODE);
        stringCache.setDefaultFont(font, size, true);
    }

    private static Font loadFont(String name, int size) {
        Font base = FONT_CACHE.get(name);
        if (base != null) {
            return base.deriveFont(Font.PLAIN, size);
        }
        try {
            InputStream is = UFontRenderer.class.getResourceAsStream("/assets/unfair/fonts/" + name + ".ttf");
            if (is != null) {
                base = Font.createFont(Font.TRUETYPE_FONT, is);
                is.close();
                FONT_CACHE.put(name, base);
                return base.deriveFont(Font.PLAIN, size);
            }
        } catch (Exception ignored) {
        }
        return new Font("Arial", Font.PLAIN, size);
    }

    @Override
    public int drawStringWithShadow(String text, float x, float y, int color) {
        drawString(text, x, y, color, true);
        return getStringWidth(text);
    }

    public String trimStringToWidth(String text, float width) {
        return trimString(text, width, false);
    }

    public String trimString(String text, float width, boolean reverse) {
        StringBuilder stringbuilder = new StringBuilder();
        for (char c : text.toCharArray()) {
            if (getStringWidth(stringbuilder.toString()) < width)
                stringbuilder.append(c);
            else
                break;
        }
        return stringbuilder.toString();
    }

    public int drawString(String text, float x, int y, int color) {
        return this.drawString(text, x, (float) y, color, false);
    }

    public int drawString(String text, float x, float y, int color) {
        this.drawString(text, x, y, color, false);
        return getStringWidth(text);
    }

    public int drawStringCapableWithEmoji(String text, float x, float y, int color) {
        char[] chars = text.toCharArray();
        int lastCut = 0;
        float xOffset = x;
        for (int i = 0; i < chars.length; i++) {
            if (isEmojiCharacter(text.codePointAt(i))) {
                xOffset += this.drawString(text.substring(0, i), xOffset, y, color, false);
                this.drawString(String.valueOf(chars[i]), xOffset, y, color, false);
                xOffset += this.getStringWidth(String.valueOf(chars[i]));
                lastCut = i + 1;
            }
        }
        this.drawString(text.substring(lastCut), xOffset, y, color, false);
        return getStringWidth(text);
    }

    public static boolean isEmojiCharacter(int codePoint) {
        return (codePoint == 0x0) ||
                (codePoint == 0x9) ||
                (codePoint == 0xA) ||
                (codePoint == 0xD) ||
                (codePoint >= 0x20 && codePoint <= 0xD7FF) || ((codePoint >= 0xE000) && (codePoint <= 0xFFFD));
    }

    @Override
    public int drawString(String text, float x, float y, int color, boolean dropShadow) {
        float densityScale = getDensityScale();
        if (densityScale > 1.0f) {
            return drawHighDensityString(text, x, y, color, dropShadow, densityScale);
        }
        return drawStringInternal(text, x, y, color, dropShadow);
    }

    private int drawHighDensityString(String text, float x, float y, int color, boolean dropShadow, float densityScale) {
        UFontRenderer renderer = getDensityRenderer(densityScale);
        if (renderer == this) {
            return drawStringInternal(text, x, y, color, dropShadow);
        }

        float actualDensityScale = renderer.size / (float) size;
        float inverseScale = 1.0f / actualDensityScale;
        GL11.glPushMatrix();
        GL11.glScalef(inverseScale, inverseScale, 1.0f);
        try {
            int result = renderer.drawStringInternal(text, x * actualDensityScale, y * actualDensityScale, color, dropShadow, actualDensityScale * 0.5f);
            return Math.round(result * inverseScale);
        } finally {
            GL11.glPopMatrix();
        }
    }

    private int drawStringInternal(String text, float x, float y, int color, boolean dropShadow) {
        return drawStringInternal(text, x, y, color, dropShadow, 0.5f);
    }

    private int drawStringInternal(String text, float x, float y, int color, boolean dropShadow, float shadowOffset) {
        if (dropShadow) {
            int alpha = (color >> 24) & 0xFF;
            if (alpha > 50) {
                int shadowColor = (20 << 16) | (20 << 8) | 20 | (alpha << 24);
                stringCache.renderString(text, x + shadowOffset, y + shadowOffset, shadowColor, true);
            }
        }
        return stringCache.renderString(text, x, y, color, false);
    }

    @Override
    public int getStringWidth(String text) {
        float densityScale = getDensityScale();
        if (densityScale > 1.0f) {
            UFontRenderer renderer = getDensityRenderer(densityScale);
            if (renderer != this) {
                float actualDensityScale = renderer.size / (float) size;
                return Math.round(renderer.stringCache.getStringWidth(text) / actualDensityScale);
            }
        }
        return stringCache.getStringWidth(text);
    }

    public void drawCenteredString(String text, float x, float y, int color) {
        drawString(text, x - stringCache.getStringWidth(text) / 2f, y, color, false);
    }

    public int getHeight() {
        float densityScale = getDensityScale();
        if (densityScale > 1.0f) {
            UFontRenderer renderer = getDensityRenderer(densityScale);
            if (renderer != this) {
                float actualDensityScale = renderer.size / (float) size;
                return Math.round(renderer.stringCache.height / 2f / actualDensityScale);
            }
        }
        return stringCache.height / 2;
    }

    private float getDensityScale() {
        try {
            int guiScale = Minecraft.getMinecraft().gameSettings.guiScale;
            if (guiScale <= 0) guiScale = 1;
            return Math.max(1.0f, guiScale);
        } catch (Exception e) {
            return 1.0f;
        }
    }

    private UFontRenderer getDensityRenderer(float densityScale) {
        int scaledSize = Math.max(size, Math.round(size * densityScale));
        if (scaledSize == size) {
            return this;
        }
        return Unfair.fontManager.getFont(scaledSize);
    }

    public float drawStringCapableWithEmojiWithShadow(String text, float x, float y, int color) {
        String[] sbs = new String[]{"\uD83C\uDF89", "\uD83C\uDF81", "\uD83D\uDC79", "\uD83C\uDFC0", "⚽", "\uD83C\uDF6D", "\uD83C\uDF20", "\uD83D\uDC7E", "\uD83D\uDC0D"
                , "\uD83D\uDD2E", "\uD83D\uDC7D", "\uD83D\uDCA3", "\uD83C\uDF6B", "\uD83C\uDF82"};
        for (String sb : sbs) {
            text = text.replaceAll(sb, "");
        }
        return drawStringWithShadow(text, x, y, color);
    }
}