package unfair.ui.clickgui.panel;

import org.lwjgl.input.Keyboard;
import unfair.Unfair;
import unfair.module.Module;
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
    
    private static final Color BG_NORMAL = new Color(240, 243, 247);
    private static final Color BG_HOVER = new Color(225, 230, 238);
    private static final Color BIND_ACTIVE = new Color(235, 242, 252);
    private static final Color HIDE_ACTIVE = new Color(242, 235, 250);
    private static final Color BIND_ACTIVE_BG = new Color(220, 235, 245);
    private static final Color HIDE_ACTIVE_BG = new Color(230, 220, 240);
    private static final Color TEXT_NORMAL = new Color(85, 95, 110);
    private static final Color TEXT_BIND = new Color(70, 130, 180);
    private static final Color TEXT_HIDE = new Color(120, 80, 150);
    private static final Color BIND_ACTIVE_TEXT = new Color(50, 100, 150);
    private static final Color HIDE_ACTIVE_TEXT = new Color(100, 60, 130);
    
    private boolean binding = false;

    public ModuleActionButtons(Module module) {
        this.module = module;
        this.width = (buttonWidth + gap) * 2 - gap;
    }

    @Override
    public void update(float deltaTime) {
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

        float bindX = x;
        float hideX = x + buttonWidth + gap;
        float btnY = y;
        
        bindHovered = isHovering(mouseX, mouseY, bindX, btnY, buttonWidth, buttonHeight);
        hideHovered = isHovering(mouseX, mouseY, hideX, btnY, buttonWidth, buttonHeight);
        
        Color bindBgColor;
        if (binding) {
            bindBgColor = BIND_ACTIVE;
        } else if (bindActiveAnim > 0.01f) {
            bindBgColor = blendColor(BG_NORMAL, BIND_ACTIVE_BG, bindActiveAnim);
        } else {
            bindBgColor = blendColor(BG_NORMAL, BG_HOVER, bindHoverAnim);
        }
        
        Color hideBgColor;
        if (hideActiveAnim > 0.01f) {
            hideBgColor = blendColor(BG_NORMAL, HIDE_ACTIVE_BG, hideActiveAnim);
        } else {
            hideBgColor = blendColor(BG_NORMAL, HIDE_ACTIVE, hideHoverAnim);
        }
        
        RoundedUtils.drawRound(bindX, btnY, buttonWidth, buttonHeight, 3, 
                blendAlpha(bindBgColor, alpha));
        RoundedUtils.drawRound(hideX, btnY, buttonWidth, buttonHeight, 3, 
                blendAlpha(hideBgColor, alpha));
        
        String bindText;
        if (binding) {
            bindText = "...";
        } else if (module.getKey() != 0) {
            bindText = KeyBindUtil.getKeyName(module.getKey());
        } else {
            bindText = "Bind";
        }
        String hideText = module.isHidden() ? "Show" : "Hide";
        
        Color bindTextColor;
        if (binding) {
            bindTextColor = TEXT_BIND;
        } else if (bindActiveAnim > 0.01f) {
            bindTextColor = blendColor(TEXT_NORMAL, BIND_ACTIVE_TEXT, bindActiveAnim);
        } else {
            bindTextColor = blendColor(TEXT_NORMAL, TEXT_BIND, bindHoverAnim);
        }
        
        Color hideTextColor;
        if (hideActiveAnim > 0.01f) {
            hideTextColor = blendColor(TEXT_NORMAL, HIDE_ACTIVE_TEXT, hideActiveAnim);
        } else {
            hideTextColor = blendColor(TEXT_NORMAL, TEXT_HIDE, hideHoverAnim);
        }
        
        float bindTextW = Unfair.fontManager.getFont(11).getStringWidth(bindText);
        float hideTextW = Unfair.fontManager.getFont(11).getStringWidth(hideText);
        
        Unfair.fontManager.getFont(11).drawString(bindText,
                bindX + (buttonWidth - bindTextW) / 2f, btnY + (buttonHeight - 11) / 2f + 2.5f,
                blendAlpha(bindTextColor, alpha).getRGB(), false);
        Unfair.fontManager.getFont(11).drawString(hideText,
                hideX + (buttonWidth - hideTextW) / 2f, btnY + (buttonHeight - 11) / 2f + 2.5f,
                blendAlpha(hideTextColor, alpha).getRGB(), false);
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
            return;
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