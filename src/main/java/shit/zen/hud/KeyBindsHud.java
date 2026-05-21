package shit.zen.hud;

import java.util.ArrayList;
import java.util.IdentityHashMap;
import java.util.List;
import java.util.Map;
import shit.zen.ZenClient;
import shit.zen.event.impl.GlRenderEvent;
import shit.zen.event.impl.Render2DEvent;
import shit.zen.event.impl.TickEvent;
import shit.zen.modules.Module;
import shit.zen.render.DrawContext;
import shit.zen.render.FontPresets;
import shit.zen.render.FontRenderer;
import shit.zen.render.GlHelper;
import shit.zen.render.Paint;
import shit.zen.render.Path;
import shit.zen.render.RoundedRectangle;
import shit.zen.utils.animation.SmoothAnimationTimer;
import shit.zen.utils.math.Easings;
import shit.zen.utils.misc.Triple;
import shit.zen.utils.misc.TripleProvider;
import shit.zen.utils.render.ColorUtil;
import shit.zen.event.EventTarget;

public class KeyBindsHud
extends HudElement {

    public static final class KeyBindEntry {
        public String name;
        public String key;
        public boolean enabled;
        public Module module;
        public float alpha;

        public KeyBindEntry(String name, String key, boolean enabled, Module module) {
            this.name = name;
            this.key = key;
            this.enabled = enabled;
            this.module = module;
            this.alpha = 1.0f;
        }
    }

    public static final class KeyBindRow {
        public final KeyBindsHud parent;
        public Module module;
        public KeyBindsHud.KeyBindEntry entry;
        public String name;
        public String key;
        public boolean enabled = true;
        public boolean visible = true;
        public boolean rightAligned = false;
        public boolean animatingIn = false;
        public float alpha = 1.0f;
        public float targetY;
        public float currentY;
        public float nameWidth;
        public float slideX;
        public float opacity;
        public float widthValue;
        public float rowHeightValue;
        public float alphaValue;
        public String displayName;
        public String keyName;

        public float getKeyWidth() {
            return this.widthValue;
        }

        public shit.zen.render.FontRenderer getFittingFont(shit.zen.render.FontRenderer base, float maxWidth) {
            return base;
        }
        public boolean removing = false;
        public final shit.zen.utils.animation.SmoothAnimationTimer fadeAnim = new shit.zen.utils.animation.SmoothAnimationTimer();
        public final shit.zen.utils.animation.SmoothAnimationTimer slideAnim = new shit.zen.utils.animation.SmoothAnimationTimer();
        public final shit.zen.utils.animation.SmoothAnimationTimer heightAnim = new shit.zen.utils.animation.SmoothAnimationTimer();
        public final shit.zen.utils.animation.SmoothAnimationTimer alphaAnim = new shit.zen.utils.animation.SmoothAnimationTimer();
        public final shit.zen.utils.animation.SmoothAnimationTimer widthAnim = new shit.zen.utils.animation.SmoothAnimationTimer();

        public void tick() {
            float target = this.removing ? 0.0f : 1.0f;
            this.alpha += (target - this.alpha) * 0.18f;
            this.fadeAnim.tick();
            this.slideAnim.tick();
            this.heightAnim.tick();
            this.alphaAnim.tick();
            this.widthAnim.tick();
        }

        public KeyBindRow(KeyBindsHud parent, Module module, KeyBindsHud.KeyBindEntry entry) {
            this.parent = parent;
            this.module = module;
            this.entry = entry;
            this.name = entry != null ? entry.name : module.getName();
            this.key = entry != null ? entry.key : module.getBind().getName();
        }

        public void startRemove() {
            this.removing = true;
        }

        public void cancelRemove() {
            this.removing = false;
        }

        public void Đ(KeyBindsHud.KeyBindEntry e) {
            this.entry = e;
            this.name = e != null ? e.name : this.module.getName();
            this.key = e != null ? e.key : this.module.getBind().getName();
        }

        public boolean isRemoveDone() {
            return this.removing && this.alpha <= 0.01f;
        }

        public float getNameWidth() {
            return this.nameWidth;
        }
    }

    private final List<KeyBindsHud.KeyBindRow> rowList = new ArrayList<>();
    private final FontRenderer nameFont = FontPresets.poppinsMedium(15.0f);
    final FontRenderer keyFont = FontPresets.poppinsRegular(15.0f);
    private final FontRenderer bindFont = FontPresets.materialIcons(18.0f);
    private final SmoothAnimationTimer scrollAnim = new SmoothAnimationTimer();
    private final SmoothAnimationTimer fadeAnim = new SmoothAnimationTimer();
    private final Paint backgroundPaint = new Paint();
    private final Paint enabledPaint = new Paint();
    private final Paint disabledPaint = new Paint();
    private float maxWidth = -1.0f;
    private float totalHeight;
    private float visibleHeight;
    private float scrollOffset;
    private float alpha;
    private float rowHeight;
    private final Path[] iconPaths = new Path[91];
    private final boolean[] iconLoaded = new boolean[91];
    private final Map<Module, KeyBindsHud.KeyBindEntry> rowMap = new IdentityHashMap<>();
    private final Map<Module, KeyBindsHud.KeyBindRow> removedRows = new IdentityHashMap<>();
    boolean isRightAligned = false;

    public KeyBindsHud() {
        super("KeyBinds");
        this.setWidth(150.0f);
        this.setHeight(100.0f);
        this.setEnabled(true);
        this.fadeAnim.setCurrentValue(1.0);
    }

    private void initSettings() {
        if (this.maxWidth >= 0.0f) {
            return;
        }
        this.maxWidth = GlHelper.getStringWidth("", this.bindFont);
        this.totalHeight = GlHelper.getStringWidth("Hotkeys", this.nameFont);
        this.visibleHeight = GlHelper.getStringWidth("", this.bindFont);
        this.scrollOffset = GlHelper.getFontAscent(this.nameFont);
        this.alpha = GlHelper.getFontAscent(this.keyFont);
        this.rowHeight = GlHelper.getFontAscent(this.bindFont);
    }

    @EventTarget
    public void onTick(TickEvent tickEvent) {
        Object object;
        if (mc.player == null) {
            this.rowList.clear();
            this.rowMap.clear();
            this.removedRows.clear();
            return;
        }
        this.rowMap.clear();
        this.removedRows.clear();
        List<Module> list = ZenClient.getInstance().getModuleManager().getModules();
        for (Module module : list) {
            KeyBindsHud.KeyBindEntry entry = null;
            if (module instanceof TripleProvider tp) {
                Triple<String, String, Boolean> triple = (Triple<String, String, Boolean>) tp.getTriple();
                if (triple != null) {
                    entry = new KeyBindsHud.KeyBindEntry(triple.first(), triple.second(), triple.isEnabled(), module);
                }
            } else if (module.isEnabled() && module.getKey() > 0
                    && !module.getName().equals("Interface")
                    && !module.getName().equals("ClickGui")) {
                entry = new KeyBindsHud.KeyBindEntry(module.getName(), null, true, module);
            }
            if (entry == null) continue;
            this.rowMap.put(module, entry);
        }
        for (KeyBindsHud.KeyBindRow row : this.rowList) {
            this.removedRows.put(row.module, row);
        }
        for (KeyBindsHud.KeyBindRow row : this.rowList) {
            if (this.rowMap.containsKey(row.module)) continue;
            row.startRemove();
        }
        boolean bl = false;
        for (java.util.Map.Entry<Module, KeyBindsHud.KeyBindEntry> e : this.rowMap.entrySet()) {
            KeyBindsHud.KeyBindRow existing = this.removedRows.get(e.getKey());
            if (existing != null) {
                if (existing.removing) existing.cancelRemove();
                existing.Đ(e.getValue());
                continue;
            }
            this.rowList.add(new KeyBindsHud.KeyBindRow(this, e.getKey(), e.getValue()));
            bl = true;
        }
        if (bl) {
            this.rowList.sort((a, b) -> Double.compare(b.getNameWidth(), a.getNameWidth()));
        }
    }

    @Override
    public void onRender2D(Render2DEvent render2DEvent, float f, float f2) {
    }

    @Override
    public void onGlRender(GlRenderEvent glRenderEvent, float f, float f2) {
        float f3;
        boolean bl;
        this.initSettings();
        this.rowList.removeIf(KeyBindsHud.KeyBindRow::isRemoveDone);
        this.rowList.forEach(KeyBindsHud.KeyBindRow::tick);
        this.scrollAnim.tick();
        this.fadeAnim.tick();
        if (this.rowList.isEmpty() && this.scrollAnim.isDone() && this.fadeAnim.isDone()) {
            return;
        }
        boolean bl2 = bl = f > (float)mc.getWindow().getGuiScaledWidth() / 2.0f;
        if (this.isRightAligned != bl) {
            this.isRightAligned = bl;
        }
        float f4 = this.scrollOffset + 10.0f;
        float f5 = this.maxWidth + 3.0f + this.totalHeight + 10.0f;
        float f6 = this.alpha + 10.0f;
        float f7 = f5;
        if (!this.rowList.isEmpty()) {
            f3 = 0.0f;
            for (KeyBindsHud.KeyBindRow keyBindsHud$KeyBindRow : this.rowList) {
                float f8 = keyBindsHud$KeyBindRow.enabled ? f6 + 3.0f : 0.0f;
                float f9 = f8 + (keyBindsHud$KeyBindRow.getNameWidth() + 10.0f);
                if (!(f9 > f3)) continue;
                f3 = f9;
            }
            f7 = Math.max(f5, f3);
        }
        if (this.scrollAnim.isDone() && this.rowList.isEmpty()) {
            this.scrollAnim.setCurrentValue(f7);
        }
        this.scrollAnim.animate(f7, 0.2, Easings.EASE_OUT_SINE);
        if (this.rowList.isEmpty()) {
            this.fadeAnim.animate(0.0, 0.2, Easings.EASE_IN_POW3);
        } else {
            this.fadeAnim.animate(1.0, 0.2, Easings.EASE_OUT_POW3);
        }
        f3 = this.scrollAnim.getValueF();
        float f10 = this.fadeAnim.getValueF();
        float f11 = f2 + f4 + 3.0f;
        for (KeyBindsHud.KeyBindRow keyBindsHud$KeyBindRow : this.rowList) {
            if (keyBindsHud$KeyBindRow.visible) {
                keyBindsHud$KeyBindRow.visible = false;
                keyBindsHud$KeyBindRow.fadeAnim.setCurrentValue(f11);
                keyBindsHud$KeyBindRow.slideAnim.setCurrentValue(keyBindsHud$KeyBindRow.rightAligned ? 20.0 : -20.0);
                keyBindsHud$KeyBindRow.heightAnim.setCurrentValue(0.0);
                keyBindsHud$KeyBindRow.alphaAnim.setCurrentValue(0.0);
                keyBindsHud$KeyBindRow.slideAnim.animate(0.0, 0.2, Easings.EASE_OUT_POW3);
                keyBindsHud$KeyBindRow.heightAnim.animate(1.0, 0.2, Easings.EASE_OUT_POW3);
                keyBindsHud$KeyBindRow.alphaAnim.animate(f6 + 3.0f, 0.2, Easings.EASE_OUT_POW3);
            } else if (keyBindsHud$KeyBindRow.animatingIn) {
                keyBindsHud$KeyBindRow.animatingIn = false;
                keyBindsHud$KeyBindRow.slideAnim.animate(0.0, 0.2, Easings.EASE_OUT_POW3);
                keyBindsHud$KeyBindRow.heightAnim.animate(1.0, 0.2, Easings.EASE_OUT_POW3);
                keyBindsHud$KeyBindRow.alphaAnim.animate(f6 + 3.0f, 0.2, Easings.EASE_OUT_POW3);
            }
            keyBindsHud$KeyBindRow.fadeAnim.animate(f11, 0.15, Easings.EASE_OUT_SINE);
            f11 += keyBindsHud$KeyBindRow.alphaAnim.getValueF();
        }
        Object object = glRenderEvent.drawContext();
        this.renderRows((DrawContext)object, f, f2, f3, this.isRightAligned, f10, f4, f6);
        this.setWidth(f3);
        this.setHeight(f11 - f2);
    }

    private void renderRows(DrawContext drawContext, float f, float f2, float f3, boolean bl, float f4, float f5, float f6) {
        float f7;
        float f8;
        float f9;
        float f10;
        if (f4 <= 0.01f) {
            return;
        }
        this.enabledPaint.setColor(ColorUtil.fromARGB(0, 0, 0, (int)(190.0f * f4)));
        GlHelper.drawRoundedRect(f, f2, f3, f5, 4.5f, this.enabledPaint);
        float f11 = f2 + 5.0f + (f5 - 10.0f - this.scrollOffset) / 2.0f + 1.0f;
        int n = ColorUtil.fromARGB(255, 255, 255, (int)(255.0f * f4));
        this.disabledPaint.setColor(n);
        if (bl) {
            float f12 = f + f3 - 5.0f - this.maxWidth;
            float f13 = f12 - 3.0f - this.totalHeight;
            GlHelper.drawTextWithShadow("Hotkeys", f13, f11, this.nameFont, this.disabledPaint);
            GlHelper.drawTextWithShadow("", f12, f11 + 1.0f, this.bindFont, this.disabledPaint);
        } else {
            GlHelper.drawTextWithShadow("", f + 5.0f, f11 + 1.0f, this.bindFont, this.disabledPaint);
            GlHelper.drawTextWithShadow("Hotkeys", f + 5.0f + this.maxWidth + 3.0f, f11, this.nameFont, this.disabledPaint);
        }
        if (this.rowList.isEmpty()) {
            return;
        }
        for (int i = 0; i < this.iconPaths.length; ++i) {
            if (!this.iconLoaded[i]) continue;
            this.iconPaths[i].reset();
            this.iconLoaded[i] = false;
        }
        for (KeyBindsHud.KeyBindRow keyBindsHud$KeyBindRow : this.rowList) {
            f10 = keyBindsHud$KeyBindRow.heightAnim.getValueF();
            if (f10 <= 0.0f) {
                keyBindsHud$KeyBindRow.slideX = 0.0f;
                continue;
            }
            f9 = f4 * f10;
            if (f9 <= 0.0f) {
                keyBindsHud$KeyBindRow.slideX = 0.0f;
                continue;
            }
            keyBindsHud$KeyBindRow.slideX = f9;
            float f14 = f + keyBindsHud$KeyBindRow.slideAnim.getValueF();
            float f15 = keyBindsHud$KeyBindRow.fadeAnim.getValueF();
            f8 = keyBindsHud$KeyBindRow.getNameWidth() + 10.0f;
            keyBindsHud$KeyBindRow.opacity = f15;
            keyBindsHud$KeyBindRow.widthValue = f8;
            int n2 = Math.max(0, Math.min(90, Math.round(90.0f * f9)));
            Path path = this.iconPaths[n2];
            if (path == null) {
                this.iconPaths[n2] = path = new Path();
            }
            this.iconLoaded[n2] = true;
            if (keyBindsHud$KeyBindRow.enabled) {
                if (bl) {
                    f7 = f14 + f3 - f6;
                    float f16 = f7 - 3.0f - f8;
                    keyBindsHud$KeyBindRow.rowHeightValue = f7;
                    keyBindsHud$KeyBindRow.alphaValue = f16;
                    path.addRoundedRect(RoundedRectangle.ofXYWHR(f16, f15, f8, f6, 4.5f));
                    path.addRoundedRect(RoundedRectangle.ofXYWHR(f7, f15, f6, f6, 4.5f));
                    continue;
                }
                keyBindsHud$KeyBindRow.rowHeightValue = f14;
                keyBindsHud$KeyBindRow.alphaValue = f14 + f6 + 3.0f;
                path.addRoundedRect(RoundedRectangle.ofXYWHR(f14, f15, f6, f6, 4.5f));
                path.addRoundedRect(RoundedRectangle.ofXYWHR(keyBindsHud$KeyBindRow.alphaValue, f15, f8, f6, 4.5f));
                continue;
            }
            keyBindsHud$KeyBindRow.alphaValue = f7 = bl ? f14 + f3 - f8 : f14;
            keyBindsHud$KeyBindRow.rowHeightValue = f7;
            path.addRoundedRect(RoundedRectangle.ofXYWHR(f7, f15, f8, f6, 4.5f));
        }
        for (int i = 0; i < this.iconPaths.length; ++i) {
            if (!this.iconLoaded[i]) continue;
            this.backgroundPaint.setColor(ColorUtil.fromARGB(0, 0, 0, i));
            drawContext.drawPath(this.iconPaths[i], this.backgroundPaint);
        }
        for (KeyBindsHud.KeyBindRow keyBindsHud$KeyBindRow : this.rowList) {
            if (keyBindsHud$KeyBindRow.slideX <= 0.0f) continue;
            f10 = keyBindsHud$KeyBindRow.opacity;
            f9 = keyBindsHud$KeyBindRow.widthValue;
            int n3 = (int)(255.0f * keyBindsHud$KeyBindRow.slideX);
            int n4 = ColorUtil.fromARGB(255, 255, 255, n3);
            f8 = f10 + 5.0f + (f6 - 10.0f - this.alpha) / 2.0f;
            float f17 = f10 + 5.0f + (f6 - 10.0f - this.rowHeight) / 2.0f + 2.5f;
            this.disabledPaint.setColor(n4);
            if (keyBindsHud$KeyBindRow.enabled) {
                float f18 = keyBindsHud$KeyBindRow.rowHeightValue;
                f7 = keyBindsHud$KeyBindRow.alphaValue;
                if (bl) {
                    float f19 = f7 + f9 - 5.0f - keyBindsHud$KeyBindRow.getNameWidth();
                    GlHelper.drawTextWithShadow(keyBindsHud$KeyBindRow.displayName, f19, f8, this.keyFont, this.disabledPaint);
                } else {
                    GlHelper.drawTextWithShadow(keyBindsHud$KeyBindRow.displayName, f7 + 5.0f, f8, this.keyFont, this.disabledPaint);
                }
                if (keyBindsHud$KeyBindRow.keyName == null) {
                    float f20 = f18 + (f6 - this.visibleHeight) / 2.0f;
                    GlHelper.drawTextWithShadow("", f20, f17, this.bindFont, this.disabledPaint);
                    continue;
                }
                FontRenderer fontRenderer = keyBindsHud$KeyBindRow.getFittingFont(this.keyFont, f6 - 5.0f);
                float f21 = keyBindsHud$KeyBindRow.getKeyWidth();
                float f22 = f18 + (f6 - f21) / 2.0f;
                float f23 = f10 + 5.0f + (f6 - 10.0f - (float)GlHelper.getFontAscent(fontRenderer)) / 2.0f;
                GlHelper.drawTextWithShadow(keyBindsHud$KeyBindRow.keyName, f22, f23, fontRenderer, this.disabledPaint);
                continue;
            }
            GlHelper.drawTextWithShadow(keyBindsHud$KeyBindRow.displayName, keyBindsHud$KeyBindRow.alphaValue + 5.0f, f8, this.keyFont, this.disabledPaint);
        }
    }

    @Override
    public void onSettings() {
    }
}