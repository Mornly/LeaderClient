package unfair.ui.clickgui.panel.config;

import net.minecraft.client.renderer.GlStateManager;
import unfair.Unfair;
import unfair.config.Config;
import unfair.management.ClientSettings;
import unfair.ui.clickgui.panel.PanelValueItem;
import unfair.util.ChatUtil;
import unfair.util.RenderUtil;
import unfair.util.shader.RoundedUtils;

import java.awt.*;
import java.io.File;

import static org.lwjgl.input.Keyboard.*;

public class CreateConfigEntry extends PanelValueItem {
    private boolean focused = false;
    private String newName;
    private int cursorPos;

    private float focusAnim = 0f;
    private float widthAnim = 0f;
    private float textAnimProgress = 1f;
    private int lastTextLength = 0;

    private float successAnim = 0f;
    private float errorAnim = 0f;

    private boolean justCreated = false;
    

    private final float baseW_ratio = 0.85f;
    private final float expandedW_ratio = 1f;
    
    public CreateConfigEntry() {
        this.newName = "";
        this.cursorPos = 0;
        initVisibility(true);
    }

    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);
        setTargetVisibility(true);
        if (focused) {
            focusAnim = lerp(focusAnim, 1f, 0.15f, deltaTime);
            widthAnim = lerp(widthAnim, 1f, 0.15f, deltaTime);
        } else {
            focusAnim = lerp(focusAnim, 0f, 0.15f, deltaTime);
            widthAnim = lerp(widthAnim, 0f, 0.15f, deltaTime);
        }

        if (newName.length() != lastTextLength) {
            textAnimProgress = 0f;
            lastTextLength = newName.length();
        }
        textAnimProgress = lerp(textAnimProgress, 1f, 0.25f, deltaTime);

        successAnim = lerp(successAnim, 0f, 0.1f, deltaTime);
        errorAnim = lerp(errorAnim, 0f, 0.1f, deltaTime);
    }

    @Override
    public void render(int mouseX, int mouseY) {

        float h = getHeight();
        float btnW = 50;
        float inputH = 26;

        float baseInputW = (width - 70) * baseW_ratio;
        float expandedInputW = (width - 70) * expandedW_ratio;
        float currentInputW = baseInputW + (expandedInputW - baseInputW) * widthAnim;
        
        float inputX = x;
        float inputY = y;

        float bgAlpha = (0.3f + 0.7f * widthAnim) * alpha;
        Color bgColor = blendAlpha(ClientSettings.INSTANCE.getInputBgColor(), bgAlpha);

        if (focusAnim > 0.01f) {
            RoundedUtils.drawRoundedRectRise(inputX, inputY, currentInputW, inputH, 5,
                    bgColor.getRGB(), false, false, true, true);
        } else {
            RoundedUtils.drawRound(inputX, inputY, currentInputW, inputH, 5, bgColor);
        }

        if (focusAnim > 0.001f && alpha > 0.01f) {
            float underlineAlpha = focusAnim * 0.6f;
            Color borderColor = blendAlpha(ClientSettings.INSTANCE.getInputBorder_color(), underlineAlpha);

            float underlineStartX = inputX + currentInputW * (1f - widthAnim) / 2f;
            float underlineEndX = inputX + currentInputW - currentInputW * (1f - widthAnim) / 2f;
            
            if (widthAnim > 0.02f && underlineEndX > underlineStartX) {
                RenderUtil.drawRect(underlineStartX, inputY + inputH - 1, underlineEndX, inputY + inputH, 
                        borderColor.getRGB());
            }
        }

        if (newName.isEmpty() && !focused) {
            float placeholderAlpha = (0.5f + 0.5f * (1f - widthAnim)) * alpha;
            float placeholderOffset = (float) Math.sin(System.currentTimeMillis() / 1000.0 * Math.PI * 2) * 0.5f;
            Unfair.fontManager.getFont(16).drawString("Config name...", inputX + 10, inputY + 6 + placeholderOffset,
                    blendAlpha(ClientSettings.INSTANCE.getInputPlaceholderColor(), placeholderAlpha).getRGB(), false);
        } else {
            float textScale = 1f + (1f - textAnimProgress) * 0.05f;
            float textAlpha = (0.7f + 0.3f * textAnimProgress) * alpha;

            GlStateManager.pushMatrix();
            float textCenterX = inputX + 10 + Unfair.fontManager.getFont(16).getStringWidth(newName) / 2f;
            float textCenterY = inputY + 6 + 8;
            GlStateManager.translate(textCenterX, textCenterY, 0);
            GlStateManager.scale(textScale, textScale, 1f);
            GlStateManager.translate(-textCenterX, -textCenterY, 0);

            Unfair.fontManager.getFont(16).drawString(newName, inputX + 10, inputY + 6,
                    blendAlpha(ClientSettings.INSTANCE.getInputTextColor(), focusAnim * textAlpha).getRGB(), false);

            GlStateManager.popMatrix();

            if (focused) {
                String beforeCursor = newName.substring(0, cursorPos);
                float cursorX = inputX + 10 + Unfair.fontManager.getFont(16).getStringWidth(beforeCursor);

                long cursorTime = System.currentTimeMillis();
                boolean cursorVisible = (cursorTime % 1000 < 500) || 
                                       (cursorTime % 120 < 60 && textAnimProgress < 0.9f);

                if (cursorVisible || successAnim > 0.01f || errorAnim > 0.01f) {
                    float cursorAlpha = textAnimProgress < 0.9f ? 1f : 
                        (float)(Math.sin(cursorTime / 200.0 * Math.PI) * 0.3f + 0.7f);

                    if (successAnim > 0.01f) {
                        RenderUtil.drawRect(cursorX, inputY + 6, cursorX + 1, inputY + 22,
                                blendAlpha(ClientSettings.INSTANCE.getSuccessColor(), alpha * cursorAlpha).getRGB());
                    } else if (errorAnim > 0.01f) {
                        RenderUtil.drawRect(cursorX, inputY + 6, cursorX + 1, inputY + 22,
                                blendAlpha(ClientSettings.INSTANCE.getErrorColor(), alpha * cursorAlpha).getRGB());
                    } else {
                        RenderUtil.drawRect(cursorX, inputY + 6, cursorX + 1, inputY + 22,
                                blendAlpha(ClientSettings.INSTANCE.getInputBorder_color(), focusAnim * cursorAlpha).getRGB());

                        if (textAnimProgress < 0.95f) {
                            float glowW = 2f + (1f - textAnimProgress) * 3f;
                            float glowAlpha = (1f - textAnimProgress) * 0.3f * alpha;
                            RenderUtil.drawRect(cursorX - glowW/2, inputY + 6,
                                    cursorX + glowW/2, inputY + 22,
                                    blendAlpha(ClientSettings.INSTANCE.getInputBorder_color(), glowAlpha).getRGB());
                        }
                    }
                }
            }
        }

        float btnX = inputX + currentInputW + 8;
        boolean btnHovered = mouseX >= btnX && mouseX <= btnX + btnW &&
                           mouseY >= inputY && mouseY <= inputY + inputH;
        
        Color btnColor = ClientSettings.INSTANCE.getButtonPrimaryColor();
        if (btnHovered) btnColor = btnColor.brighter();
        if (successAnim > 0.3f) btnColor = blendColor(btnColor, ClientSettings.INSTANCE.getSuccessColor(), successAnim);
        
        RoundedUtils.drawRound(btnX, inputY, btnW, inputH, 5, blendAlpha(btnColor, alpha));
        
        String text = "Create";
        float textW = Unfair.fontManager.getFont(16).getStringWidth(text);
        Unfair.fontManager.getFont(16).drawString(text,
                btnX + (btnW - textW) / 2f, inputY + (inputH - 16) / 2f + 2.0f,
                blendAlpha(Color.WHITE, alpha).getRGB(), false);

        if (successAnim > 0.05f) {
            RoundedUtils.drawRound(x, y, width, h, 5, 
                blendAlpha(ClientSettings.INSTANCE.getSuccessColor(), alpha * successAnim * 0.15f));
        }
        if (errorAnim > 0.05f) {
            RoundedUtils.drawRound(x, y, width, h, 5, 
                blendAlpha(ClientSettings.INSTANCE.getErrorColor(), alpha * errorAnim * 0.15f));
        }
    }

    @Override
    public void mouseClicked(int mx, int my, int button) {
        if (button != 0) return;

        float baseInputW = (width - 70) * baseW_ratio;
        float expandedInputW = (width - 70) * expandedW_ratio;
        float currentInputW = baseInputW + (expandedInputW - baseInputW) * widthAnim;
        
        float btnX = x + currentInputW + 8;

        if (mx >= btnX && mx <= btnX + 50 && my >= y && my <= y + 26) {
            createConfig();
            return;
        }

        if (mx >= x && mx <= x + currentInputW && my >= y && my <= y + 26) {
            focused = true;
            cursorPos = newName.length();
            return;
        }

        focused = false;
    }

    @Override
    public void mouseReleased(int mx, int my, int button) {}
    
    @Override
    public void mouseDragged(int mx, int my, int button) {}

    @Override
    public float getHeight() { return 36; }

    @Override
    public boolean visible() { return true; }

    public boolean isFocused() { return focused; }
    
    public boolean justCreatedConfig() {
        if (justCreated) {
            justCreated = false;
            return true;
        }
        return false;
    }

    public void setFocused(boolean f) { 
        focused = f; 
        if (focused) {
            cursorPos = newName.length();
        }
    }

    public void handleKeyTyped(char typedChar, int keyCode) {
        if (!focused) return;

        if (keyCode == KEY_RETURN) {
            createConfig();
            return;
        }

        if (keyCode == KEY_ESCAPE) {
            focused = false;
            return;
        }

        if (keyCode == KEY_BACK && cursorPos > 0) {
            newName = newName.substring(0, cursorPos - 1) + newName.substring(cursorPos);
            cursorPos--;
            return;
        }

        if (Character.isLetterOrDigit(typedChar) || typedChar == '_' || typedChar == '-') {
            newName = newName.substring(0, cursorPos) + typedChar + newName.substring(cursorPos);
            cursorPos++;
        }
    }

    private void createConfig() {
        if (newName.isEmpty()) {
            errorAnim = 1f;
            ChatUtil.sendFormatted(Unfair.clientName + "&cPlease enter a config name");
            return;
        }

        try {
            File testFile = new File("./config/Unfair/" + newName + ".json");
            if (testFile.exists()) {
                errorAnim = 1f;
                ChatUtil.sendFormatted(Unfair.clientName + "&cConfig exists: &f" + newName);
                return;
            }

            Config config = new Config(newName, true);
            config.save();
            
            successAnim = 1f;
            justCreated = true;
            ChatUtil.sendFormatted(Unfair.clientName + "&aCreated: &f" + newName);

            newName = "";
            cursorPos = 0;
            lastTextLength = 0;
            textAnimProgress = 1f;
            focused = false;
        } catch (Exception e) {
            errorAnim = 1f;
            ChatUtil.sendFormatted(Unfair.clientName + "&cFailed to create: &f" + newName);
        }
    }

    private static Color blendColor(Color a, Color b, float t) {
        int r = (int)(a.getRed() + (b.getRed() - a.getRed()) * t);
        int g = (int)(a.getGreen() + (b.getGreen() - a.getGreen()) * t);
        int bl = (int)(a.getBlue() + (b.getBlue() - a.getBlue()) * t);
        return new Color(r, g, bl);
    }

    private static Color blendAlpha(Color c, float a) {
        return new Color(c.getRed(), c.getGreen(), c.getBlue(), (int)(a * 255));
    }
}
