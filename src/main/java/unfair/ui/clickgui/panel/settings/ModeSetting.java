package unfair.ui.clickgui.panel.settings;

import org.lwjgl.opengl.GL11;
import unfair.Unfair;
import unfair.management.ClientSettings;
import unfair.property.properties.ModeProperty;
import unfair.ui.clickgui.panel.PanelValueItem;
import unfair.util.RenderUtil;
import unfair.util.shader.RoundedUtils;

import java.awt.*;

public class ModeSetting extends PanelValueItem {
    private static final int MAX_VISIBLE_MODES = 4;
    private final ModeProperty value;
    private float expandAnim = 0;
    private boolean expanded = false;
    private float hoverAnim = 0;
    private float textAlpha = 0;
    private float modeScrollOffset = 0;
    private float targetScrollOffset = 0;
    private float tipAlpha = 0;
    public float panelScreenX = 0;
    public float panelScreenY = 0;
    public int col = 0;

    public ModeSetting(ModeProperty v) {
        this.value = v;
    }

    public boolean isExpanded() {
        return expanded || expandAnim > 0.01f;
    }

    public void setExpanded(boolean expanded) {
        this.expanded = expanded;
        if (!expanded) {
            targetScrollOffset = 0;
            modeScrollOffset = 0;
        }
    }

    public boolean isHoveringExpanded(int mx, int my) {
        if (!isExpanded()) return false;
        String[] modes = value.getModes();
        float btnW = 50;
        for (String m : modes) {
            float tw = Unfair.fontManager.getFont(12).getStringWidth(m);
            btnW = Math.max(btnW, tw + 20);
        }
        float btnH = 18;
        float itemW = 50;
        int visibleCount = Math.min(modes.length, MAX_VISIBLE_MODES);
        float currentExpandW = visibleCount * itemW * expandAnim;
        float totalW = btnW + currentExpandW;
        float panelX = x + width - totalW;
        float panelY = y;
        return mx >= panelX && mx <= panelX + totalW && my >= panelY && my <= panelY + btnH;
    }

    public void handleScroll(int delta) {
        String[] modes = value.getModes();
        if (modes.length <= MAX_VISIBLE_MODES) return;
        float itemW = 50;
        float step = itemW * 0.5f;
        float maxScrollOffset = (modes.length - MAX_VISIBLE_MODES) * itemW;
        if (delta > 0) {
            targetScrollOffset -= step;
        } else {
            targetScrollOffset += step;
        }
        if (targetScrollOffset < 0) targetScrollOffset = 0;
        if (targetScrollOffset > maxScrollOffset) targetScrollOffset = maxScrollOffset;
    }

    @Override
    public void update(float deltaTime) {
        super.update(deltaTime);
        setTargetVisibility(visible());
        float target = expanded ? 1f : 0f;
        expandAnim = lerp(expandAnim, target, 0.18f, deltaTime);

        if (expanded && expandAnim > 0.95f) {
            textAlpha = lerp(textAlpha, 1f, 0.2f, deltaTime);
        } else if (!expanded && expandAnim < 0.05f) {
            textAlpha = lerp(textAlpha, 0f, 0.2f, deltaTime);
        } else if (expanded) {
            textAlpha = lerp(textAlpha, 1f, 0.18f, deltaTime);
        } else {
            textAlpha = lerp(textAlpha, 0f, 0.3f, deltaTime);
            if (textAlpha < 0.01f) textAlpha = 0f;
        }

        if (!expanded && textAlpha > 0.01f) {
            expandAnim = 1f;
        }

        modeScrollOffset = lerp(modeScrollOffset, targetScrollOffset, 0.25f, deltaTime);
        if (Math.abs(modeScrollOffset - targetScrollOffset) < 0.01f) {
            modeScrollOffset = targetScrollOffset;
        }

        boolean scrollable = value.getModes().length > MAX_VISIBLE_MODES && expanded && expandAnim > 0.5f;
        float tipTarget = scrollable ? 1f : 0f;
        tipAlpha = lerp(tipAlpha, tipTarget, 0.15f, deltaTime);
    }

    private boolean isHoveringCurrent = false;

    public void setHovering(boolean hovering) {
        isHoveringCurrent = hovering;
    }

    @Override
    public void render(int mouseX, int mouseY) {
        float visAlpha = getVisibilityAlpha();
        if (visAlpha < 0.01f) return;

        Unfair.fontManager.getFont(13).drawString(value.getName(), x, y + 2,
                blendAlpha(ClientSettings.INSTANCE.getSettingNameColor(), alpha * visAlpha).getRGB(), false);

        String[] modes = value.getModes();
        if (modes.length == 0) return;

        String selectedText = value.getModeString();
        int font = 12;
        float textW = Unfair.fontManager.getFont(font).getStringWidth(selectedText);
        float btnW = Math.max(textW + 20, 50);
        float btnH = 18;
        float itemW = 50;
        int visibleCount = Math.min(modes.length, MAX_VISIBLE_MODES);
        float currentExpandW = visibleCount * itemW * expandAnim;

        float totalW = btnW + currentExpandW;
        float panelY = y;

        float panelX = x + width - totalW;
        float btnX = panelX + currentExpandW;

        boolean hovering = isHoveringCurrent;
        float hoverTarget = hovering ? 1f : 0f;
        hoverAnim = lerp(hoverAnim, hoverTarget, 0.15f, 0.016f);

        Color bgColor = blendColor(ClientSettings.INSTANCE.getModeBgNormalColor(), ClientSettings.INSTANCE.getModeBgHoverColor(), hoverAnim);
        RoundedUtils.drawRound(panelX, panelY, totalW, btnH, 4, blendAlpha(bgColor, alpha * visAlpha));

        if (currentExpandW > 1 && textAlpha > 0.01f) {
            float scrollOffset = modeScrollOffset;
            float clipRight = Math.min(btnX, x + width);

            float guiScale = ClientSettings.INSTANCE.getGUIScale();
            float mcClipX = panelScreenX + (panelX - panelScreenX) * guiScale;
            float mcClipY = panelScreenY + (panelY - panelScreenY) * guiScale;
            float mcClipW = (clipRight - panelX) * guiScale;
            float mcClipH = btnH * guiScale;

            GL11.glPushAttrib(GL11.GL_SCISSOR_BIT);
            if (mcClipW > 0 && mcClipH > 0) {
                GL11.glEnable(GL11.GL_SCISSOR_TEST);
                RenderUtil.scissor(mcClipX, mcClipY, mcClipW, mcClipH);
            } else {
                GL11.glDisable(GL11.GL_SCISSOR_TEST);
            }

            for (int i = 0; i < modes.length; i++) {
                float modeX = panelX + i * itemW - scrollOffset;

                if (modeX + itemW < panelX || modeX >= clipRight) continue;

                boolean selected = (i == value.getValue());
                boolean itemHover = mouseX >= modeX && mouseX <= modeX + itemW &&
                                   mouseY >= panelY && mouseY <= panelY + btnH;

                if (selected || itemHover) {
                    Color hlColor = selected ? ClientSettings.INSTANCE.getModeItemSelectedColor() : ClientSettings.INSTANCE.getModeItemHoverColor();
                    RoundedUtils.drawRound(modeX + 2, panelY + 2, itemW - 4, btnH - 4, 3,
                            blendAlpha(hlColor, alpha * textAlpha * visAlpha));
                }

                Color tc = selected ? ClientSettings.INSTANCE.getAccentColor() : ClientSettings.INSTANCE.getModeTextNormalColor();
                String modeText = modes[i];
                float modeTextW = Unfair.fontManager.getFont(11).getStringWidth(modeText);
                float textPosX = modeX + (itemW - modeTextW) / 2f;
                float textPosY = panelY + (btnH - 11) / 2f + 1;

                Unfair.fontManager.getFont(11).drawString(modeText, textPosX, textPosY,
                        blendAlpha(tc, alpha * textAlpha * visAlpha).getRGB(), false);
            }

            GL11.glPopAttrib();
        }

        if (tipAlpha > 0.01f) {
            String tipText = "Need more? Try to scroll.";
            float tipW = Unfair.fontManager.getFont(10).getStringWidth(tipText) + 12;
            float tipH = 16;
            float tipX = panelX + (totalW - tipW) / 2f;
            float tipY = panelY + btnH + 3;

            RoundedUtils.drawRound(tipX, tipY, tipW, tipH, 3,
                    blendAlpha(ClientSettings.INSTANCE.getTipBgColor(), alpha * tipAlpha * visAlpha * 0.85f));
            Unfair.fontManager.getFont(10).drawString(tipText,
                    tipX + (tipW - Unfair.fontManager.getFont(10).getStringWidth(tipText)) / 2f,
                    tipY + (tipH - 10) / 2f + 1.5f,
                    blendAlpha(ClientSettings.INSTANCE.getTipTextColor(), alpha * tipAlpha * visAlpha).getRGB(), false);
        }

        Unfair.fontManager.getFont(font).drawString(selectedText,
                btnX + (btnW - textW) / 2f, panelY + (btnH - font) / 2f + 1,
                blendAlpha(ClientSettings.INSTANCE.getAccentColor(), alpha * visAlpha).getRGB(), false);
    }

    @Override
    public void mouseClicked(int mx, int my, int button) {
        if (button != 0) return;
        String[] modes = value.getModes();
        float btnW = 50;
        for (String m : modes) {
            float tw = Unfair.fontManager.getFont(12).getStringWidth(m);
            btnW = Math.max(btnW, tw + 20);
        }
        float btnH = 18;
        float itemW = 50;
        int visibleCount = Math.min(modes.length, MAX_VISIBLE_MODES);
        float currentExpandW = visibleCount * itemW * expandAnim;

        float totalW = btnW + currentExpandW;
        float panelY = y;

        float panelX = x + width - totalW;
        float btnX = panelX + currentExpandW;

        if (mx >= panelX && mx <= panelX + totalW && my >= panelY && my <= panelY + btnH) {
            boolean clickedBtn = (mx >= btnX && mx < btnX + btnW);

            if (expanded && expandAnim > 0.5f && !clickedBtn && mx >= panelX && mx < btnX) {
                int clickedIndex = (int)((mx - panelX + modeScrollOffset) / itemW);
                if (clickedIndex >= 0 && clickedIndex < modes.length) {
                    value.setValue(clickedIndex);
                    expanded = false;
                    targetScrollOffset = 0;
                    modeScrollOffset = 0;
                    return;
                }
            }

            if (clickedBtn) {
                expanded = !expanded;
                if (!expanded) {
                    targetScrollOffset = 0;
                    modeScrollOffset = 0;
                }
            }
        }
    }

    @Override
    public void mouseReleased(int mx, int my, int button) {}
    @Override
    public void mouseDragged(int mx, int my, int button) {}
    @Override
    public float getHeight() { return 22; }
    @Override
    public boolean visible() { return value.isVisible(); }

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
