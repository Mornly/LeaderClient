package unfair.ui.clickgui.dropdown;

public abstract class ValueItem {
    public float x, y, width;
    public float masterAlpha = 1f;

    public abstract void render(int mouseX, int mouseY);
    public abstract void mouseClicked(int mx, int my, int button);
    public abstract void mouseReleased(int mx, int my, int button);
    public abstract float getHeight();
    public abstract boolean visible();

    protected boolean isHovering(int mx, int my, float x, float y, float w, float h) {
        return mx >= x && mx <= x + w && my >= y && my <= y + h;
    }
}
