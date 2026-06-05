package unfair.ui.clickgui.panel;

public abstract class PanelValueItem {
    public float x, y, width;
    public float alpha = 1f;
    protected float visibilityAnim = 1f;
    private float targetVisibility = 1f;
    private boolean initialized = false;

    public void update(float deltaTime) {
        if (!initialized) return;
        visibilityAnim = lerp(visibilityAnim, targetVisibility, 0.12f, deltaTime);
    }

    public void initVisibility(boolean visible) {
        if (!initialized) {
            initialized = true;
            targetVisibility = visible ? 1f : 0f;
            visibilityAnim = targetVisibility;
        }
    }

    public void setTargetVisibility(boolean visible) {
        targetVisibility = visible ? 1f : 0f;
    }

    public float getVisibilityAlpha() {
        return visibilityAnim;
    }

    public boolean isVisible() {
        return visibilityAnim > 0.01f;
    }

    public abstract void render(int mouseX, int mouseY);
    public abstract void mouseClicked(int mx, int my, int button);
    public abstract void mouseReleased(int mx, int my, int button);
    public abstract void mouseDragged(int mx, int my, int button);
    public abstract float getHeight();
    public abstract boolean visible();

    protected boolean isHovering(int mx, int my, float x, float y, float w, float h) {
        return mx >= x && mx <= x + w && my >= y && my <= y + h;
    }

    protected static float lerp(float current, float target, float speed, float dt) {
        float diff = target - current;
        if (Math.abs(diff) < 0.001f) return target;

        float adjustedSpeed = speed * 1.5f;
        float progress = 1f - (float)Math.pow(1f - adjustedSpeed, dt * 60f);

        return current + diff * progress;
    }
}
