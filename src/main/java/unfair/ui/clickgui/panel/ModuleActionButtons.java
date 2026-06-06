package unfair.ui.clickgui.panel;

import org.lwjgl.input.Keyboard;
import unfair.Unfair;
import unfair.module.Module;
import unfair.management.ClientSettings;
import unfair.util.KeyBindUtil;
import unfair.util.shader.RoundedUtils;

import java.awt.*;

public class ModuleActionButtons extends PanelValueItem {
    private final Module module;
    private final float buttonWidth = 29;
    private final float buttonHeight = 15;
    private final float gap = 3;
    
    private boolean bindHovered = false;
    private boolean hideHovered = false;
    
    private float bindHoverAnim = 0f;
    private float hideHoverAnim = 0f;
    private float bindActiveAnim = 0f;
    private float hideActiveAnim = 0f;
    
    private boolean binding = false;

    public ModuleActionButtons(Module module) {
        this.module = module;
        this.width = (buttonWidth + gap) * 2 - gap;
        initVisibility(true);
    }

    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);
        setTargetVisibility(true);
        float targetBindAnim = bindHovered ? 1f : 0f;
        float targetHideAnim = hideHovered ? 1f : 0f;
        float targetBindActive = module.getKey() != 0 ? 1f : 0f;
        float targetHideActive = module.isHidden() ? 1f : 0f;

        bindHoverAnim = lerp(bindHoverAnim, targetBindAnim, 0.18f, deltaTime);
        hideHoverAnim = lerp(hideHoverAnim, targetHideAnim, 0.18f, deltaTime);
        bindActiveAnim = lerp(bindActiveAnim, targetBindActive, 0.18f, deltaTime);
        hideActiveAnim = lerp(hideActiveAnim, targetHideActive, 0.18f, deltaTime);
    }

    @Override
    public void render(int mouseX, int mouseY) {
        float visAlpha = getVisibilityAlpha();
        if (visAlpha < 0.01f) return;

        float bindX = x;
        float hideX = x + buttonWidth + gap;
        float btnY = y;

        bindHovered = isHovering(mouseX, mouseY, bindX, btnY, buttonWidth, buttonHeight);
        hideHovered = isHovering(mouseX, mouseY, hideX, btnY, buttonWidth, buttonHeight);

        Color bgNormal = ClientSettings.INSTANCE.getActionBtnNormalColor();
        Color bgHover = ClientSettings.INSTANCE.getActionBtnHoverColor();

        Color bindBgColor;
        if (binding) {
            bindBgColor = ClientSettings.INSTANCE.getBindActiveColor();
        } else if (bindActiveAnim > 0.01f) {
            bindBgColor = blendColor(bgNormal, ClientSettings.INSTANCE.getBindActiveBgColor(), bindActiveAnim);
        } else {
            bindBgColor = blendColor(bgNormal, bgHover, bindHoverAnim);
        }

        Color hideBgColor;
        if (hideActiveAnim > 0.01f) {
            hideBgColor = blendColor(bgNormal, ClientSettings.INSTANCE.getHideActiveBgColor(), hideActiveAnim);
        } else {
            hideBgColor = blendColor(bgNormal, ClientSettings.INSTANCE.getHideActiveColor(), hideHoverAnim);
        }

        RoundedUtils.drawRound(bindX, btnY, buttonWidth, buttonHeight, 3,
                blendAlpha(bindBgColor, alpha * visAlpha));
        RoundedUtils.drawRound(hideX, btnY, buttonWidth, buttonHeight, 3,
                blendAlpha(hideBgColor, alpha * visAlpha));
        
        String bindText;
        if (binding) {
            bindText = "...";
        } else if (module.getKey() != 0) {
            bindText = KeyBindUtil.getKeyName(module.getKey());
        } else {
            bindText = "Bind";
        }
        String hideText = module.isHidden() ? "Show" : "Hide";
        
        Color textNormal = ClientSettings.INSTANCE.getTextNormalColor();
        Color textBind = ClientSettings.INSTANCE.getAccentColor();
        Color textHide = ClientSettings.INSTANCE.getTextHideColor();
        
        Color bindTextColor;
        if (binding) {
            bindTextColor = textBind;
        } else if (bindActiveAnim > 0.01f) {
            bindTextColor = blendColor(textNormal, ClientSettings.INSTANCE.getBindActiveTextColor(), bindActiveAnim);
        } else {
            bindTextColor = blendColor(textNormal, textBind, bindHoverAnim);
        }
        
        Color hideTextColor;
        if (hideActiveAnim > 0.01f) {
            hideTextColor = blendColor(textNormal, ClientSettings.INSTANCE.getHideActiveTextColor(), hideActiveAnim);
        } else {
            hideTextColor = blendColor(textNormal, textHide, hideHoverAnim);
        }
        
        float bindTextW = Unfair.fontManager.getFont(11).getStringWidth(bindText);
        float hideTextW = Unfair.fontManager.getFont(11).getStringWidth(hideText);

        Unfair.fontManager.getFont(11).drawString(bindText,
                bindX + (buttonWidth - bindTextW) / 2f, btnY + (buttonHeight - 11) / 2f + 2.5f,
                blendAlpha(bindTextColor, alpha * visAlpha).getRGB(), false);
        Unfair.fontManager.getFont(11).drawString(hideText,
                hideX + (buttonWidth - hideTextW) / 2f, btnY + (buttonHeight - 11) / 2f + 2.5f,
                blendAlpha(hideTextColor, alpha * visAlpha).getRGB(), false);
    }

    @Override
    public void mouseClicked(int mx, int my, int button) {
        float bindX = x;
        float hideX = x + buttonWidth + gap;
        float btnY = y;
        
        if (isHovering(mx, my, bindX, btnY, buttonWidth, buttonHeight)) {
            if (binding) {
                binding = false;
                module.setKey(0);
            } else {
                binding = true;
            }
            return;
        }
        
        if (isHovering(mx, my, hideX, btnY, buttonWidth, buttonHeight)) {
            module.setHidden(!module.isHidden());
        }
    }

    @Override
    public void mouseReleased(int mx, int my, int button) {}

    @Override
    public void mouseDragged(int mx, int my, int button) {}

    @Override
    public float getHeight() {
        return buttonHeight;
    }

    @Override
    public boolean visible() {
        return true;
    }
    
    public boolean handleKeyInput(int keyCode) {
        if (binding && keyCode != 0) {
            if (keyCode == Keyboard.KEY_ESCAPE) {
                module.setKey(0);
                binding = false;
                return true;
            }
            module.setKey(keyCode);
            binding = false;
            return true;
        }
        return false;
    }
    
    public boolean isBinding() {
        return binding;
    }
    
    private static Color blendColor(Color a, Color b, float t) {
        int r = (int)(a.getRed() + (b.getRed() - a.getRed()) * t);
        int g = (int)(a.getGreen() + (b.getGreen() - a.getGreen()) * t);
        int bl = (int)(a.getBlue() + (b.getBlue() - a.getBlue()) * t);
        return new Color(r, g, bl);
    }
    
    private static Color blendAlpha(Color c, float alpha) {
        return new Color(c.getRed(), c.getGreen(), c.getBlue(), (int)(alpha * 255));
    }
}