package unfair.font;

import unfair.font.impl.UFontRenderer;

import java.util.HashMap;
import java.util.Map;

public class FontManager {

    // 保留原有的固定尺寸字段（可选，但为了兼容性）
    public UFontRenderer s12;
    public UFontRenderer s14;
    public UFontRenderer s15;
    public UFontRenderer s16;
    public UFontRenderer s18;
    public UFontRenderer s20;
    public UFontRenderer s22;
    public UFontRenderer s24;
    public UFontRenderer s28;
    public UFontRenderer s36;
    public UFontRenderer s40;

    public UFontRenderer icon16;
    public UFontRenderer icon20;
    public UFontRenderer icon24;

    // 新增：字体名称 -> 大小 -> 渲染器
    private final Map<String, Map<Integer, UFontRenderer>> fontCache = new HashMap<>();

    public void load() {
        s12 = new UFontRenderer("NotoSansSC-Regular", 12);
        s14 = new UFontRenderer("NotoSansSC-Regular", 14);
        s15 = new UFontRenderer("NotoSansSC-Regular", 15);
        s16 = new UFontRenderer("NotoSansSC-Regular", 16);
        s18 = new UFontRenderer("NotoSansSC-Regular", 18);
        s20 = new UFontRenderer("NotoSansSC-Regular", 20);
        s22 = new UFontRenderer("NotoSansSC-Regular", 22);
        s24 = new UFontRenderer("NotoSansSC-Regular", 24);
        s28 = new UFontRenderer("NotoSansSC-Regular", 28);
        s36 = new UFontRenderer("NotoSansSC-Regular", 36);
        s40 = new UFontRenderer("NotoSansSC-Regular", 40);

        icon16 = new UFontRenderer("icon", 16);
        icon20 = new UFontRenderer("icon", 20);
        icon24 = new UFontRenderer("icon", 24);
    }

    public UFontRenderer getFont(int size) {
        return getFont("NotoSansSC-Regular", size);
    }

    public UFontRenderer getFont(String fontName, int size) {
        Map<Integer, UFontRenderer> sizeMap = fontCache.computeIfAbsent(fontName, k -> new HashMap<>());
        return sizeMap.computeIfAbsent(size, s -> new UFontRenderer(fontName, size));
    }
}