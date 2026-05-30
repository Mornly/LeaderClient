package unfair.module.modules.render;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.entity.RenderManager;
import org.lwjgl.opengl.GL11;
import unfair.Unfair;
import unfair.enums.BlinkModules;
import unfair.enums.ChatColors;
import unfair.event.EventTarget;
import unfair.event.types.EventType;
import unfair.events.Render2DEvent;
import unfair.events.TickEvent;
import unfair.font.impl.UFontRenderer;
import unfair.mixin.IAccessorGuiChat;
import unfair.module.Module;
import unfair.property.properties.*;
import unfair.util.ColorUtil;
import unfair.util.RenderUtil;
import unfair.util.Timer;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class HUD extends Module {
    private static final Minecraft mc = Minecraft.getMinecraft();
    private static final float ANIMATION_DURATION = 200.0F;

    private static final float SUFFIX_GAP = 3.0F;
    private static final float BAR_WIDTH = 1.0F;
    private static final float TEXT_Y_OFFSET = -0.5F;
    public final ModeProperty colorMode = new ModeProperty(
            "color", 3, new String[]{"RAINBOW", "CHROMA", "ASTOLFO", "CUSTOM1", "CUSTOM12", "CUSTOM123"}
    );
<<<<<<< HEAD
    public static final ModeProperty font = new ModeProperty("Font", 0, new String[]{"Unfair", "MineCraft"});
=======
    public static final ModeProperty font = new ModeProperty("Font",0,new String[]{"Unfair","MineCraft"});
>>>>>>> 839a5315ef498d98d4be72e8b3f4e7cc0c660d5c
    public final FloatProperty colorSpeed = new FloatProperty("color-speed", 1.0F, 0.5F, 1.5F);
    public final PercentProperty colorSaturation = new PercentProperty("color-saturation", 50);
    public final PercentProperty colorBrightness = new PercentProperty("color-brightness", 100);
    public final ColorProperty custom1 = new ColorProperty("custom-color-1", Color.WHITE.getRGB(), () -> this.colorMode.getValue() == 3 || this.colorMode.getValue() == 4 || this.colorMode.getValue() == 5);
    public final ColorProperty custom2 = new ColorProperty("custom-color-2", Color.WHITE.getRGB(), () -> this.colorMode.getValue() == 4 || this.colorMode.getValue() == 5);
    public final ColorProperty custom3 = new ColorProperty("custom-color-3", Color.WHITE.getRGB(), () -> this.colorMode.getValue() == 5);
    public final ModeProperty posX = new ModeProperty("position-x", 0, new String[]{"LEFT", "RIGHT"});
    public final ModeProperty posY = new ModeProperty("position-y", 0, new String[]{"TOP", "BOTTOM"});
    public final IntProperty offsetX = new IntProperty("offset-x", 2, 0, 255);
<<<<<<< HEAD
=======

>>>>>>> 839a5315ef498d98d4be72e8b3f4e7cc0c660d5c
    public final IntProperty offsetY = new IntProperty("offset-y", 2, 0, 255);
    public final IntProperty bgWidth = new IntProperty("bg-width", 1, 0, 10);
    public final IntProperty bgHeight = new IntProperty("bg-height", 2, 0, 20);
    public final FloatProperty scale = new FloatProperty("scale", 1.0F, 0.5F, 1.5F);
<<<<<<< HEAD
    public final ColorProperty backgroundColor = new ColorProperty("background-color", new Color(0, 0, 0, 255).getRGB());
    public final PercentProperty background = new PercentProperty("background-alpha", 50);
=======
    public final PercentProperty background = new PercentProperty("background", 50);
>>>>>>> 839a5315ef498d98d4be72e8b3f4e7cc0c660d5c

    public final BooleanProperty showBar = new BooleanProperty("bar", true);
    public final IntProperty barHeight = new IntProperty("BarHeightLess", 0, 0, 10);

    public final BooleanProperty shadow = new BooleanProperty("shadow", true);
    public final BooleanProperty suffixes = new BooleanProperty("suffixes", true);
    public final BooleanProperty lowerCase = new BooleanProperty("lower-case", false);
    public final BooleanProperty chatOutline = new BooleanProperty("chat-outline", true);
    public final BooleanProperty blinkTimer = new BooleanProperty("blink-timer", true);
    public final BooleanProperty toggleSound = new BooleanProperty("toggle-sounds", true);
    public final BooleanProperty toggleAlerts = new BooleanProperty("toggle-alerts", false);

<<<<<<< HEAD
    private final Map<Module, Float> animationMap = new HashMap<>();
    private List<Module> activeModules = new ArrayList<>();

    private static float animateSmooth(float target, float current, float speed, float deltaTime) {
        float diff = target - current;
        float change = diff * Math.min(1.0f, speed * deltaTime);
        if (Math.abs(change) < 0.01f && Math.abs(diff) < 0.01f) return target;
        return current + change;
    }

=======
    private final Set<Module> fadingOutModules = new HashSet<>();
    private final Map<Module, Timer> animationMap = new HashMap<>();
    private List<Module> activeModules = new ArrayList<>();

>>>>>>> 839a5315ef498d98d4be72e8b3f4e7cc0c660d5c
    public HUD() {
        super("HUD", true, true);
    }

<<<<<<< HEAD
    private UFontRenderer getCurrentFontRenderer() {
        int size = (int) (18 * this.scale.getValue());
        if (font.getValue() == 0) {
            return Unfair.fontManager.getFont(size);
        }
        return null;
    }

=======
>>>>>>> 839a5315ef498d98d4be72e8b3f4e7cc0c660d5c
    private String getModuleName(Module module) {
        String moduleName = module.getName();
        if (this.lowerCase.getValue()) {
            moduleName = moduleName.toLowerCase(Locale.ROOT);
        }
        return moduleName;
    }
    private String[] getModuleSuffix(Module module) {
        String[] moduleSuffix = module.getSuffix();
        if (this.lowerCase.getValue()) {
            for (int i = 0; i < moduleSuffix.length; i++) {
                moduleSuffix[i] = moduleSuffix[i].toLowerCase();
            }
        }
        return moduleSuffix;
    }

    private int getModuleWidth(Module module) {
        return this.calculateStringWidth(
                this.getModuleName(module), this.getModuleSuffix(module)
        );
    }
<<<<<<< HEAD

    private int calculateStringWidth(String string, String[] arr) {
        if (font.getValue() == 1) {
            int width = mc.fontRendererObj.getStringWidth(string);
            if (this.suffixes.getValue()) {
                for (String str : arr) {
                    width += 3 + mc.fontRendererObj.getStringWidth(str);
                }
            }
            return width;
        } else {
            UFontRenderer fr = getCurrentFontRenderer();
            if (fr == null) return 0;
            int width = fr.getStringWidth(string);
            if (this.suffixes.getValue()) {
                for (String str : arr) {
                    width += 3 + fr.getStringWidth(str);
                }
            }
            return width;
        }
    }

    private float getExactWidth(String string, String[] arr, UFontRenderer fr) {
        float width = fr.getStringWidth(string);
        if (this.suffixes.getValue() && arr != null) {
            for (String str : arr) {
                width += SUFFIX_GAP + fr.getStringWidth(str);
            }
=======
    private UFontRenderer getFontRenderer() {
        return Unfair.fontManager.getFont((int) (18 * this.scale.getValue()));
    }
    private int calculateStringWidth(String string, String[] arr) {
        UFontRenderer fr = getFontRenderer();
        int width = mc.fontRendererObj.getStringWidth(string);
        if (this.suffixes.getValue()) {
            for (String str : arr) {
                if (font.getValue() == 1) {
                    width += 3 + mc.fontRendererObj.getStringWidth(str);
                }
                if (font.getValue() == 0){
                    width += 3 + fr.getStringWidth(str);
                }
            }
>>>>>>> 839a5315ef498d98d4be72e8b3f4e7cc0c660d5c
        }
        return width;
    }

    private float getColorCycle(long long3, long long4) {
        long speed = (long) (3000.0 / Math.pow(Math.min(Math.max(0.5F, this.colorSpeed.getValue()), 1.5F), 3.0));
        return 1.0F - (float) (Math.abs(long3 - long4 * 300L) % speed) / (float) speed;
    }

    public Color getColor(long time) {
        return this.getColor(time, 0L);
    }

    public Color getColor(long time, long offset) {
        Color color = Color.white;
        switch (this.colorMode.getValue()) {
            case 0:
                color = ColorUtil.fromHSB(this.getColorCycle(time, offset), 1.0F, 1.0F);
                break;
            case 1:
                color = ColorUtil.fromHSB(this.getColorCycle(time / 3L, 0L), 1.0F, 1.0F);
                break;
            case 2:
                float cycle = this.getColorCycle(time, offset);
                if (cycle % 1.0F < 0.5F) {
                    cycle = 1.0F - cycle % 1.0F;
                }
                color = ColorUtil.fromHSB(cycle, 1.0F, 1.0F);
                break;
            case 3:
                color = new Color(this.custom1.getValue());
                break;
            case 4:
                double cycle1 = this.getColorCycle(time, offset);
                color = ColorUtil.interpolate(
                        (float) (2.0 * Math.abs(cycle1 - Math.floor(cycle1 + 0.5))),
                        new Color(this.custom1.getValue()),
                        new Color(this.custom2.getValue())
                );
                break;
            case 5:
                double cycle2 = this.getColorCycle(time, offset);
                float floor = (float) (2.0 * Math.abs(cycle2 - Math.floor(cycle2 + 0.5)));
                if (floor <= 0.5F) {
                    color = ColorUtil.interpolate(floor * 2.0F, new Color(this.custom1.getValue()), new Color(this.custom2.getValue()));
                } else {
                    color = ColorUtil.interpolate((floor - 0.5F) * 2.0F, new Color(this.custom2.getValue()), new Color(this.custom3.getValue()));
                }
        }
        float[] hsb = Color.RGBtoHSB(color.getRed(), color.getGreen(), color.getBlue(), null);
        return Color.getHSBColor(
                hsb[0],
                hsb[1] * (this.colorSaturation.getValue().floatValue() / 100.0F),
                hsb[2] * (this.colorBrightness.getValue().floatValue() / 100.0F)
        );
    }
<<<<<<< HEAD
=======
    private float getExactWidth(String string, String[] arr, UFontRenderer fr) {
        float width = fr.getStringWidth(string);
        if (this.suffixes.getValue() && arr != null) {
            for (String str : arr) {
                width += SUFFIX_GAP + fr.getStringWidth(str);
            }
        }
        return width;
    }
>>>>>>> 839a5315ef498d98d4be72e8b3f4e7cc0c660d5c

    @EventTarget
    public void onTick(TickEvent event) {
        if (this.isEnabled() && event.getType() == EventType.POST) {
            List<Module> newActiveModules = Unfair.moduleManager.modules.values().stream()
<<<<<<< HEAD
                    .filter(module -> !module.isHidden() && (module.isEnabled() || this.animationMap.getOrDefault(module, 0.0F) > 0.01F))
                    .collect(Collectors.toList());

            newActiveModules.sort(Comparator.comparingInt(this::getModuleWidth).reversed());
=======
                    .filter(module -> module.isEnabled() && !module.isHidden())
                    .sorted(Comparator.comparingInt(this::getModuleWidth).reversed())
                    .collect(Collectors.<Module>toList());

            for (Module module : newActiveModules) {
                if (!this.activeModules.contains(module) && !this.animationMap.containsKey(module)) {
                    Timer timer = new Timer(ANIMATION_DURATION);
                    timer.start();
                    this.animationMap.put(module, timer);
                    this.fadingOutModules.remove(module);
                } else if (this.fadingOutModules.remove(module)) {

                    Timer timer = new Timer(ANIMATION_DURATION);
                    timer.start();
                    this.animationMap.put(module, timer);
                }
            }

            for (Module module : this.activeModules) {
                if (!newActiveModules.contains(module)) {

                    Timer existing = this.animationMap.get(module);
                    if (existing == null || existing.cached == 1.0F || this.fadingOutModules.contains(module)) {
                        Timer timer = new Timer(ANIMATION_DURATION);
                        timer.start();
                        this.animationMap.put(module, timer);
                    }
                    this.fadingOutModules.add(module);
                }
            }

            Iterator<Map.Entry<Module, Timer>> it = this.animationMap.entrySet().iterator();
            while (it.hasNext()) {
                Map.Entry<Module, Timer> entry = it.next();
                Module m = entry.getKey();
                Timer t = entry.getValue();
                if (m == null || t == null) {
                    it.remove();
                    continue;
                }
                if (!m.isEnabled()) {
                    long elapsed = System.currentTimeMillis() - t.last;
                    if (t.cached == 0.0F || elapsed > ANIMATION_DURATION + 100) {
                        it.remove();
                        this.fadingOutModules.remove(m);
                    }
                }

            }
>>>>>>> 839a5315ef498d98d4be72e8b3f4e7cc0c660d5c

            this.activeModules = newActiveModules;
        }
    }

    @EventTarget
    public void onRender2D(Render2DEvent event) {
        if (this.chatOutline.getValue() && mc.currentScreen instanceof GuiChat) {
            String text = ((IAccessorGuiChat) mc.currentScreen).getInputField().getText().trim();
            if (Unfair.commandManager != null && Unfair.commandManager.isTypingCommand(text)) {
                RenderUtil.enableRenderState();
                RenderUtil.drawOutlineRect(
                        2.0F,
                        (float) (mc.currentScreen.height - 14),
                        (float) (mc.currentScreen.width - 2),
                        (float) (mc.currentScreen.height - 2),
                        1.5F,
                        0,
                        this.getColor(System.currentTimeMillis()).getRGB()
                );
                RenderUtil.disableRenderState();
            }
        }
        if (this.isEnabled() && !mc.gameSettings.showDebugInfo) {
            if (font.getValue() == 1) {
<<<<<<< HEAD
                float textHeight = (float) mc.fontRendererObj.FONT_HEIGHT - 1.0F;
                float padX = (float) this.bgWidth.getValue();
                float padY = (float) this.bgHeight.getValue();
                float rowHeight = textHeight + padY * 2;
                float barSize = this.showBar.getValue() ? BAR_WIDTH : 0.0F;

=======
                float height = (float) mc.fontRendererObj.FONT_HEIGHT - 1.0F;
>>>>>>> 839a5315ef498d98d4be72e8b3f4e7cc0c660d5c
                float x = (float) this.offsetX.getValue()
                        + (1.0F + (this.showBar.getValue() ? (this.shadow.getValue() ? 2.0F : 1.0F) : 0.0F)) * this.scale.getValue();
                float y = (float) this.offsetY.getValue() + 1.0F * this.scale.getValue();
                if (this.posX.getValue() == 1) {
                    x = (float) new ScaledResolution(mc).getScaledWidth() - x;
                }
                if (this.posY.getValue() == 1) {
<<<<<<< HEAD
                    y = (float) new ScaledResolution(mc).getScaledHeight() - y - rowHeight * this.scale.getValue();
=======
                    y = (float) new ScaledResolution(mc).getScaledHeight() - y - height * this.scale.getValue();
>>>>>>> 839a5315ef498d98d4be72e8b3f4e7cc0c660d5c
                }
                GlStateManager.pushMatrix();
                GlStateManager.scale(this.scale.getValue(), this.scale.getValue(), 0.0F);
                long l = System.currentTimeMillis();
                long offset = 0L;
<<<<<<< HEAD
                float deltaTime = 1.0F / Math.max(Minecraft.getDebugFPS(), 5);
                float currentY = y;
                float yDir = (this.posY.getValue() == 0) ? 1.0F : -1.0F;

                List<Module> renderList = this.activeModules;
=======

                List<Module> renderList = new ArrayList<>(this.activeModules);
                for (Module fading : this.fadingOutModules) {
                    if (!fading.isHidden() && !renderList.contains(fading)) {
                        renderList.add(fading);
                    }
                }

                renderList.sort(Comparator.comparingInt(this::getModuleWidth).reversed());
>>>>>>> 839a5315ef498d98d4be72e8b3f4e7cc0c660d5c

                for (Module module : renderList) {
                    String moduleName = this.getModuleName(module);
                    String[] moduleSuffix = this.getModuleSuffix(module);
<<<<<<< HEAD
                    int fullWidthPx = this.calculateStringWidth(moduleName, moduleSuffix);
                    float fullWidth = fullWidthPx;
                    float targetSlide = module.isEnabled() ? fullWidth : 0.0F;
                    float currentSlide = this.animationMap.getOrDefault(module, 0.0F);
                    currentSlide = animateSmooth(targetSlide, currentSlide, 10.0F, deltaTime);
                    currentSlide = Math.max(0.0F, Math.min(fullWidth, currentSlide));
                    this.animationMap.put(module, currentSlide);
                    float heightFactor = (fullWidth > 0.0F) ? (currentSlide / fullWidth) : 0.0F;
                    float effectiveHeight = rowHeight * heightFactor;
                    if (currentSlide <= 0.1F) {
                        offset++;
                        continue;
                    }
                    float totalWidth = fullWidthPx - (this.shadow.getValue() ? 0 : 1);
                    int color = this.getColor(l, offset).getRGB();
=======
                    float totalWidth = (float) (this.calculateStringWidth(moduleName, moduleSuffix) - (this.shadow.getValue() ? 0 : 1));
                    int color = this.getColor(l, offset).getRGB();

                    float animProgress = 1.0F;
                    boolean isFadingOut = !module.isEnabled();
                    Timer animTimer = this.animationMap.get(module);
                    if (animTimer != null && animTimer.last > 0 && animTimer.cached != 1.0F) {
                        try {
                            if (isFadingOut) {

                                animProgress = Math.max(0.0F, 1.0F - animTimer.getValueFloat(0.0F, 1.0F, 2));
                            } else {

                                animProgress = animTimer.getValueFloat(0.0F, 1.0F, 2);
                            }
                        } catch (Exception ignored) {
                            animProgress = isFadingOut ? 0.0F : 1.0F;
                        }
                    }

                    if (isFadingOut && animProgress <= 0.01F) {
                        continue;
                    }
>>>>>>> 839a5315ef498d98d4be72e8b3f4e7cc0c660d5c

                    boolean alignLeft = this.posX.getValue() == 0;
                    boolean alignTop = this.posY.getValue() == 0;

<<<<<<< HEAD
                    float shiftAmount = (this.posX.getValue() == 0) ? -(fullWidth - currentSlide) : (fullWidth - currentSlide);
                    float newX = x + shiftAmount;

                    RenderUtil.enableRenderState();
                    if (this.background.getValue() > 0 && heightFactor > 0.02F) {
                        Color bgCol = new Color(this.backgroundColor.getValue());
                        int alpha = (int) (heightFactor * this.background.getValue().floatValue() / 100.0F * 255.0F);
                        alpha = Math.min(255, Math.max(0, alpha));
                        int finalColor = (bgCol.getRGB() & 0x00FFFFFF) | (alpha << 24);
                        float textLeft = newX / this.scale.getValue() - (alignLeft ? 0.0F : totalWidth);
                        float textRight = textLeft + totalWidth;
                        float left = textLeft - padX;
                        float right = textRight + padX;
                        float top = currentY / this.scale.getValue();
                        float bottom = currentY / this.scale.getValue() + effectiveHeight;
                        RenderUtil.drawRect(left, top, right, bottom, finalColor);
                    }
                    if (this.showBar.getValue() && heightFactor > 0.02F) {
                        int barAlpha = (int) (heightFactor * 255.0F);
                        barAlpha = Math.min(barAlpha, 255);
                        int barColor = (color & 0x00FFFFFF) | (barAlpha << 24);
                        float barLeft = newX / this.scale.getValue() + (alignLeft ? -3.0F : 1.0F);
                        float barRight = newX / this.scale.getValue() + (alignLeft ? -2.0F : 2.0F);
                        float barTop = currentY / this.scale.getValue() + this.barHeight.getValue();
                        float barBottom = currentY / this.scale.getValue() + effectiveHeight - this.barHeight.getValue();
                        RenderUtil.drawRect(barLeft, barTop, barRight, barBottom, barColor);
                    }
                    RenderUtil.disableRenderState();
                    GlStateManager.disableDepth();
                    if (heightFactor > 0.05F) {
                        GlStateManager.enableBlend();
                        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
                        float textX = newX / this.scale.getValue() + (alignLeft ? padX : -(totalWidth + padX));
                        float textYBase = currentY / this.scale.getValue() + padY;
                        if (this.shadow.getValue()) {
                            mc.fontRendererObj.drawStringWithShadow(moduleName, textX, textYBase, color);
                        } else {
                            mc.fontRendererObj.drawString(moduleName, textX, textYBase + (alignTop ? 0.0F : 1.0F), color, false);
                        }
                        if (this.suffixes.getValue() && moduleSuffix.length > 0 && heightFactor > 0.5F) {
                            float width = (float) mc.fontRendererObj.getStringWidth(moduleName) + 3.0F;
                            int suffixAlpha = (int) (((heightFactor - 0.5F) / 0.5F) * 255.0F);
                            suffixAlpha = Math.min(suffixAlpha, 255);
                            int suffixColor = ChatColors.GRAY.toAwtColor() & 0x00FFFFFF | (suffixAlpha << 24);
                            for (String string : moduleSuffix) {
                                float suffixX = textX + width;
                                if (this.shadow.getValue()) {
                                    mc.fontRendererObj.drawStringWithShadow(string, suffixX, textYBase, suffixColor);
                                } else {
                                    mc.fontRendererObj.drawString(string, suffixX, textYBase + (alignTop ? 0.0F : 1.0F), suffixColor, false);
=======
                    float xSlideDir = alignLeft ? -1.0F : 1.0F;
                    float xSlideAmount = (1.0F - animProgress) * totalWidth * xSlideDir;

                    float ySlideDir = alignTop ? 1.0F : -1.0F;
                    float ySlideAmount = (1.0F - animProgress) * (height + 2.0F) * ySlideDir;

                    float currentX = x + xSlideAmount;
                    float currentY = y + ySlideAmount;

                    int animatedColor = color;
                    if (animProgress < 1.0F) {
                        int alpha = Math.max(0, Math.min(255, (int) (animProgress * 255.0F)));
                        animatedColor = (color & 0x00FFFFFF) | (alpha << 24);
                    } else {
                        animatedColor = color;
                    }

                    RenderUtil.enableRenderState();
                    if (this.background.getValue() > 0 && animProgress > 0.02F) {
                        int bgAlpha = (int) (animProgress * this.background.getValue().floatValue() / 100.0F * 255.0F);
                        bgAlpha = Math.min(bgAlpha, 255);
                        RenderUtil.drawRect(currentX / this.scale.getValue() - 1.0F - (alignLeft ? 0.0F : totalWidth),
                                currentY / this.scale.getValue() - (alignTop ? (offset == 0L ? 1.0F : 0.0F) : (this.shadow.getValue() ? 1.0F : 0.0F)),
                                currentX / this.scale.getValue() + 1.0F + (alignLeft ? totalWidth : 0.0F),
                                currentY / this.scale.getValue() + height + (alignTop ? (this.shadow.getValue() ? 1.0F : 0.0F) : (offset == 0L ? 1.0F : 0.0F)),
                                new Color(0.0F, 0.0F, 0.0F, bgAlpha / 255.0F).getRGB());
                    }
                    if (this.showBar.getValue() && animProgress > 0.02F) {
                        int barAlpha = (int) (animProgress * 255.0F);
                        barAlpha = Math.min(barAlpha, 255);
                        int barColor = (color & 0x00FFFFFF) | (barAlpha << 24);
                        RenderUtil.drawRect(
                                currentX / this.scale.getValue() + (alignLeft ? -3.0F : 1.0F),
                                currentY / this.scale.getValue() - (alignTop ? (offset == 0L ? 1.0F : 0.0F) : 1.0F) + barHeight.getValue(),
                                currentX / this.scale.getValue() + (alignLeft ? -2.0F : 2.0F),
                                currentY / this.scale.getValue() + height + (alignTop ? 1.0F : (offset == 0L ? 1.0F : 0.0F)) - barHeight.getValue(),
                                barColor
                        );
                    }
                    RenderUtil.disableRenderState();
                    GlStateManager.disableDepth();
                    if (animProgress > 0.05F) {
                        GlStateManager.enableBlend();
                        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
                        if (this.shadow.getValue()) {
                            mc.fontRendererObj
                                    .drawStringWithShadow(moduleName, currentX / this.scale.getValue() - (alignLeft ? 0.0F : totalWidth), currentY / this.scale.getValue(), animatedColor);
                        } else {
                            mc.fontRendererObj
                                    .drawString(
                                            moduleName,
                                            currentX / this.scale.getValue() - (alignLeft ? 0.0F : totalWidth),
                                            currentY / this.scale.getValue() + (alignTop ? 0.0F : 1.0F),
                                            animatedColor,
                                            false
                                    );
                        }
                        if (this.suffixes.getValue() && moduleSuffix.length > 0 && animProgress > 0.5F) {
                            float width = (float) mc.fontRendererObj.getStringWidth(moduleName) + 3.0F;
                            int suffixAlpha = (int) (((animProgress - 0.5F) / 0.5F) * 255.0F);
                            suffixAlpha = Math.min(suffixAlpha, 255);
                            int suffixColor = ChatColors.GRAY.toAwtColor() & 0x00FFFFFF | (suffixAlpha << 24);
                            for (String string : moduleSuffix) {
                                if (this.shadow.getValue()) {
                                    mc.fontRendererObj
                                            .drawStringWithShadow(
                                                    string,
                                                    currentX / this.scale.getValue() - (alignLeft ? 0.0F : totalWidth) + width,
                                                    currentY / this.scale.getValue(),
                                                    suffixColor
                                            );
                                } else {
                                    mc.fontRendererObj
                                            .drawString(
                                                    string,
                                                    currentX / this.scale.getValue() - (alignLeft ? 0.0F : totalWidth) + width,
                                                    currentY / this.scale.getValue() + (alignTop ? 0.0F : 1.0F),
                                                    suffixColor,
                                                    false
                                            );
>>>>>>> 839a5315ef498d98d4be72e8b3f4e7cc0c660d5c
                                }
                                width += (float) mc.fontRendererObj.getStringWidth(string) + (this.shadow.getValue() ? 3.0F : 2.0F);
                            }
                        }
                        GlStateManager.disableBlend();
                    }
<<<<<<< HEAD
                    currentY += effectiveHeight * this.scale.getValue() * yDir;
=======
                    y += (height + (this.shadow.getValue() ? 1.0F : 0.0F)) * this.scale.getValue() * (alignTop ? 1.0F : -1.0F);
>>>>>>> 839a5315ef498d98d4be72e8b3f4e7cc0c660d5c
                    offset++;
                }
                if (this.blinkTimer.getValue()) {
                    BlinkModules blinkingModule = Unfair.blinkManager.getBlinkingModule();
                    if (blinkingModule != BlinkModules.NONE && blinkingModule != BlinkModules.AUTO_BLOCK) {
                        long movementPacketSize = Unfair.blinkManager.countMovement();
                        if (movementPacketSize > 0L) {
                            GlStateManager.enableBlend();
                            GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
<<<<<<< HEAD
                            String text = String.valueOf(movementPacketSize);
                            float centerX = (float) new ScaledResolution(mc).getScaledWidth() / 2.0F / this.scale.getValue()
                                    - (float) mc.fontRendererObj.getStringWidth(text) / 2.0F;
                            float centerY = (float) new ScaledResolution(mc).getScaledHeight() / 5.0F * 3.0F / this.scale.getValue();
                            int blinkColor = this.getColor(l, offset).getRGB() & 0x00FFFFFF | 0xBF000000;
                            mc.fontRendererObj.drawString(text, centerX, centerY, blinkColor, this.shadow.getValue());
=======
                            mc.fontRendererObj
                                    .drawString(
                                            String.valueOf(movementPacketSize),
                                            (float) new ScaledResolution(mc).getScaledWidth() / 2.0F / this.scale.getValue()
                                                    - (float) mc.fontRendererObj.getStringWidth(String.valueOf(movementPacketSize)) / 2.0F,
                                            (float) new ScaledResolution(mc).getScaledHeight() / 5.0F * 3.0F / this.scale.getValue(),
                                            this.getColor(l, offset).getRGB() & 16777215 | -1090519040,
                                            this.shadow.getValue()
                                    );
>>>>>>> 839a5315ef498d98d4be72e8b3f4e7cc0c660d5c
                            GlStateManager.disableBlend();
                        }
                    }
                }
                GlStateManager.enableDepth();
                GlStateManager.popMatrix();
            }
<<<<<<< HEAD
            else {
                UFontRenderer fr = getCurrentFontRenderer();
                if (fr == null) return;
=======
        }
        if (font.getValue() == 0) {
            if (this.isEnabled() && !mc.gameSettings.showDebugInfo) {
                UFontRenderer fr = getFontRenderer();
>>>>>>> 839a5315ef498d98d4be72e8b3f4e7cc0c660d5c

                float fontHeight = (float) fr.getHeight();
                float padX = (float) this.bgWidth.getValue();
                float padY = (float) this.bgHeight.getValue();
<<<<<<< HEAD
                float rowHeight = fontHeight + padY;
=======

                float rowHeight = fontHeight + padY;

>>>>>>> 839a5315ef498d98d4be72e8b3f4e7cc0c660d5c
                float scaleVal = this.scale.getValue();
                ScaledResolution sr = new ScaledResolution(mc);
                float scaledWidth = sr.getScaledWidth() / scaleVal;
                float scaledHeight = sr.getScaledHeight() / scaleVal;

                boolean alignLeft = this.posX.getValue() == 0;
                boolean alignTop = this.posY.getValue() == 0;

                float startX = alignLeft ? this.offsetX.getValue() : scaledWidth - this.offsetX.getValue();
                float startY = alignTop ? this.offsetY.getValue() : scaledHeight - this.offsetY.getValue();

                GlStateManager.pushMatrix();
                GlStateManager.scale(scaleVal, scaleVal, 1.0F);

                long l = System.currentTimeMillis();
                long offset = 0L;
<<<<<<< HEAD
                float deltaTime = 1.0F / Math.max(Minecraft.getDebugFPS(), 5);
                float currentY = startY;
                float yDir = (this.posY.getValue() == 0) ? 1.0F : -1.0F;

                List<Module> renderList = this.activeModules;
=======

                List<Module> renderList = new ArrayList<>(this.activeModules);
                for (Module fading : this.fadingOutModules) {
                    if (!fading.isHidden() && !renderList.contains(fading)) {
                        renderList.add(fading);
                    }
                }

                renderList.sort((m1, m2) -> Float.compare(
                        getExactWidth(getModuleName(m2), getModuleSuffix(m2), fr),
                        getExactWidth(getModuleName(m1), getModuleSuffix(m1), fr)
                ));

                float currentY = startY;
>>>>>>> 839a5315ef498d98d4be72e8b3f4e7cc0c660d5c

                for (Module module : renderList) {
                    String moduleName = this.getModuleName(module);
                    String[] moduleSuffix = this.getModuleSuffix(module);
                    float textWidth = getExactWidth(moduleName, moduleSuffix, fr);
<<<<<<< HEAD
                    float fullWidth = textWidth;
                    float targetSlide = module.isEnabled() ? fullWidth : 0.0F;
                    float currentSlide = this.animationMap.getOrDefault(module, 0.0F);
                    currentSlide = animateSmooth(targetSlide, currentSlide, 15.0F, deltaTime);
                    currentSlide = Math.max(0.0F, Math.min(fullWidth, currentSlide));
                    this.animationMap.put(module, currentSlide);
                    float heightFactor = (fullWidth > 0.0F) ? (currentSlide / fullWidth) : 0.0F;
                    float effectiveHeight = rowHeight * heightFactor;
                    if (currentSlide <= 0.1F) {
                        offset++;
                        continue;
                    }
                    float barSize = this.showBar.getValue() ? BAR_WIDTH : 0.0F;
                    float totalModuleWidth = barSize + padX + textWidth + padX;
                    float xSlideAmount = (1.0F - heightFactor) * (totalModuleWidth + 5.0F);
                    float currentX = startX + (alignLeft ? -xSlideAmount : xSlideAmount);
                    float moduleLeft, moduleRight, textRenderX;
                    if (alignLeft) {
                        moduleLeft = currentX;
                        moduleRight = currentX + totalModuleWidth;
=======

                    float animProgress = 1.0F;
                    boolean isFadingOut = !module.isEnabled();
                    Timer animTimer = this.animationMap.get(module);
                    if (animTimer != null && animTimer.last > 0 && animTimer.cached != 1.0F) {
                        try {
                            animProgress = isFadingOut ? Math.max(0.0F, 1.0F - animTimer.getValueFloat(0.0F, 1.0F, 2)) : animTimer.getValueFloat(0.0F, 1.0F, 2);
                        } catch (Exception ignored) {
                            animProgress = isFadingOut ? 0.0F : 1.0F;
                        }
                    }

                    if (isFadingOut && animProgress <= 0.01F) continue;

                    float barSize = this.showBar.getValue() ? BAR_WIDTH : 0.0F;

                    float totalModuleWidth = barSize + padX + textWidth + padX;

                    float xSlideAmount = (1.0F - animProgress) * (totalModuleWidth + 5.0F);
                    float currentX = startX + (alignLeft ? -xSlideAmount : xSlideAmount);

                    float moduleLeft, moduleRight, textRenderX;

                    if (alignLeft) {
                        moduleLeft = currentX;
                        moduleRight = currentX + totalModuleWidth;

>>>>>>> 839a5315ef498d98d4be72e8b3f4e7cc0c660d5c
                        textRenderX = moduleLeft + barSize + padX;
                    } else {
                        moduleRight = currentX;
                        moduleLeft = currentX - totalModuleWidth;
<<<<<<< HEAD
                        textRenderX = moduleLeft + padX;
                    }
                    float drawY = alignTop ? currentY : currentY - rowHeight;
                    int color = this.getColor(l, offset).getRGB();
                    int animatedColor = color;
                    if (heightFactor < 1.0F) {
                        int alpha = Math.max(0, Math.min(255, (int) (heightFactor * 255.0F)));
=======

                        textRenderX = moduleLeft + padX;
                    }

                    float itemAdvance = rowHeight * animProgress;
                    float drawY = alignTop ? currentY : currentY - rowHeight;

                    int color = this.getColor(l, offset).getRGB();
                    int animatedColor = color;
                    if (animProgress < 1.0F) {
                        int alpha = Math.max(0, Math.min(255, (int) (animProgress * 255.0F)));
>>>>>>> 839a5315ef498d98d4be72e8b3f4e7cc0c660d5c
                        animatedColor = (color & 0x00FFFFFF) | (alpha << 24);
                    }

                    RenderUtil.enableRenderState();

<<<<<<< HEAD
                    if (this.background.getValue() > 0 && heightFactor > 0.02F) {
                        Color bgCol = new Color(this.backgroundColor.getValue());
                        int alpha = (int) (heightFactor * this.background.getValue().floatValue() / 100.0F * 255.0F);
                        alpha = Math.min(255, Math.max(0, alpha));
                        int bgColor = (bgCol.getRGB() & 0x00FFFFFF) | (alpha << 24);
                        RenderUtil.drawRect(moduleLeft, drawY, moduleRight, drawY + rowHeight, bgColor);
                    }

                    if (this.showBar.getValue() && heightFactor > 0.02F) {
                        int barAlpha = Math.min(255, (int) (heightFactor * 255.0F));
                        int barColor = (color & 0x00FFFFFF) | (barAlpha << 24);
                        if (alignLeft) {
                            RenderUtil.drawRect(moduleLeft, drawY + this.barHeight.getValue(), moduleLeft + BAR_WIDTH, drawY + rowHeight - this.barHeight.getValue(), barColor);
                        } else {
                            RenderUtil.drawRect(moduleRight - BAR_WIDTH, drawY + this.barHeight.getValue(), moduleRight, drawY + rowHeight - this.barHeight.getValue(), barColor);
=======
                    if (this.background.getValue() > 0 && animProgress > 0.02F) {
                        int bgAlpha = (int) (animProgress * this.background.getValue().floatValue() / 100.0F * 255.0F);
                        int bgColor = new Color(0, 0, 0, Math.min(bgAlpha, 255)).getRGB();
                        RenderUtil.drawRect(moduleLeft,drawY,moduleRight,drawY + rowHeight,bgColor);
                    }

                    if (this.showBar.getValue() && animProgress > 0.02F) {
                        int barAlpha = Math.min(255, (int) (animProgress * 255.0F));
                        int barColor = (color & 0x00FFFFFF) | (barAlpha << 24);
                        if (alignLeft) {
                            RenderUtil.drawRect(moduleLeft, drawY + barHeight.getValue(), moduleLeft + BAR_WIDTH , drawY + rowHeight - barHeight.getValue(), barColor);
                        } else {
                            RenderUtil.drawRect(moduleRight - BAR_WIDTH, drawY + barHeight.getValue(), moduleRight, drawY + rowHeight - barHeight.getValue(), barColor);
>>>>>>> 839a5315ef498d98d4be72e8b3f4e7cc0c660d5c
                        }
                    }
                    RenderUtil.disableRenderState();

                    GlStateManager.disableDepth();
<<<<<<< HEAD
                    if (heightFactor > 0.05F) {
                        GlStateManager.enableBlend();
                        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
=======
                    if (animProgress > 0.05F) {
                        GlStateManager.enableBlend();
                        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

>>>>>>> 839a5315ef498d98d4be72e8b3f4e7cc0c660d5c
                        float textY = drawY + (padY / 2.0F) + TEXT_Y_OFFSET;
                        float currentTextX = textRenderX;

                        fr.drawString(moduleName, currentTextX, textY, animatedColor, this.shadow.getValue());
                        currentTextX += fr.getStringWidth(moduleName);

<<<<<<< HEAD
                        if (this.suffixes.getValue() && moduleSuffix.length > 0 && heightFactor > 0.5F) {
                            int suffixAlpha = Math.min(255, (int) (((heightFactor - 0.5F) / 0.5F) * 255.0F));
                            int suffixColor = (ChatColors.GRAY.toAwtColor() & 0x00FFFFFF) | (suffixAlpha << 24);
=======
                        if (this.suffixes.getValue() && moduleSuffix.length > 0 && animProgress > 0.5F) {
                            int suffixAlpha = Math.min(255, (int) (((animProgress - 0.5F) / 0.5F) * 255.0F));
                            int suffixColor = (ChatColors.GRAY.toAwtColor() & 0x00FFFFFF) | (suffixAlpha << 24);

>>>>>>> 839a5315ef498d98d4be72e8b3f4e7cc0c660d5c
                            for (String string : moduleSuffix) {
                                currentTextX += SUFFIX_GAP;
                                fr.drawString(string, currentTextX, textY, suffixColor, this.shadow.getValue());
                                currentTextX += fr.getStringWidth(string);
                            }
                        }
                        GlStateManager.disableBlend();
                    }
<<<<<<< HEAD
                    currentY += alignTop ? effectiveHeight : -effectiveHeight;
=======

                    currentY += alignTop ? itemAdvance : -itemAdvance;
>>>>>>> 839a5315ef498d98d4be72e8b3f4e7cc0c660d5c
                    offset++;
                }

                if (this.blinkTimer.getValue()) {
                    BlinkModules blinkingModule = Unfair.blinkManager.getBlinkingModule();
                    if (blinkingModule != BlinkModules.NONE && blinkingModule != BlinkModules.AUTO_BLOCK) {
                        long movementPacketSize = Unfair.blinkManager.countMovement();
                        if (movementPacketSize > 0L) {
                            GlStateManager.enableBlend();
                            GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
<<<<<<< HEAD
                            String bText = String.valueOf(movementPacketSize);
                            int blinkColor = this.getColor(l, offset).getRGB() & 0x00FFFFFF | 0xBF000000;
                            fr.drawString(bText, scaledWidth / 2.0F - (float) fr.getStringWidth(bText) / 2.0F, scaledHeight * 0.6F, blinkColor, this.shadow.getValue());
=======

                            String bText = String.valueOf(movementPacketSize);
                            fr.drawString(
                                    bText,
                                    scaledWidth / 2.0F - (float) fr.getStringWidth(bText) / 2.0F,
                                    scaledHeight * 0.6F,
                                    this.getColor(l, offset).getRGB() & 16777215 | -1090519040,
                                    this.shadow.getValue()
                            );
>>>>>>> 839a5315ef498d98d4be72e8b3f4e7cc0c660d5c
                            GlStateManager.disableBlend();
                        }
                    }
                }
                GlStateManager.enableDepth();
                GlStateManager.popMatrix();
            }
        }
    }
}