package unfair.ui.clickgui.panel;

public abstract class PanelValueItem {
    public float x, y, width;
    public float alpha = 1f;

    public void update(float deltaTime) {}
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
