package shit.zen.hud;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffectUtil;
import shit.zen.event.impl.GlRenderEvent;
import shit.zen.event.impl.Render2DEvent;
import shit.zen.event.impl.TickEvent;
import shit.zen.render.DrawContext;
import shit.zen.render.FontPresets;
import shit.zen.render.FontRenderer;
import shit.zen.render.GlHelper;
import shit.zen.render.Paint;
import shit.zen.render.RoundedRectangle;
import shit.zen.utils.math.Easings;
import shit.zen.utils.render.ColorUtil;
import shit.zen.event.EventTarget;

public class PotionEffectsHud
extends HudElement {
    public static final class EffectEntry {
        public final PotionEffectsHud parent;
        public MobEffectInstance instance;
        public String effectName = "";
        public String durationText = "";
        public String amplifierText = "";
        public int originalDuration = 1;
        public final shit.zen.utils.animation.SmoothAnimationTimer fadeAnim = new shit.zen.utils.animation.SmoothAnimationTimer();
        public final shit.zen.utils.animation.SmoothAnimationTimer heightAnim = new shit.zen.utils.animation.SmoothAnimationTimer();
        public final shit.zen.utils.animation.SmoothAnimationTimer widthAnim = new shit.zen.utils.animation.SmoothAnimationTimer();
        public float alpha = 1.0f;
        public float targetY;
        public float currentY;
        public boolean removing = false;
        public boolean visible = true;

        public EffectEntry(PotionEffectsHud parent, MobEffectInstance instance) {
            this.parent = parent;
            this.instance = instance;
            this.originalDuration = Math.max(1, instance.getDuration());
            this.updateText();
        }

        public net.minecraft.world.effect.MobEffect getEffect() {
            return this.instance.getEffect();
        }

        public void updateEffect(MobEffectInstance instance) {
            this.instance = instance;
            this.updateText();
        }

        private void updateText() {
            this.effectName = this.instance.getEffect().getDisplayName().getString();
            this.durationText = net.minecraft.world.effect.MobEffectUtil.formatDuration(this.instance, 1.0f).getString();
            int amp = this.instance.getAmplifier();
            this.amplifierText = amp > 0 ? String.valueOf(amp + 1) : "";
        }

        public void show(float targetHeight) {
            this.visible = false;
            this.heightAnim.animate(targetHeight, 0.2, shit.zen.utils.math.Easings.EASE_OUT_POW3);
            this.widthAnim.animate(100.0, 0.2, shit.zen.utils.math.Easings.EASE_OUT_POW3);
        }

        public float getTotalWidth() {
            return this.widthAnim.getValueF();
        }

        public void startRemove() {
            this.removing = true;
        }

        public boolean isRemoveDone() {
            return this.removing && this.alpha <= 0.01f;
        }

        public void tick() {
            float target = this.removing ? 0.0f : 1.0f;
            this.alpha += (target - this.alpha) * 0.18f;
            this.fadeAnim.tick();
            this.heightAnim.tick();
            this.widthAnim.tick();
        }
    }

    private final List<PotionEffectsHud.EffectEntry> effectEntryList = new ArrayList<>();
    final FontRenderer effectNameFont = FontPresets.pingfang(16.0f);
    private final FontRenderer timerFont = FontPresets.axiformaBold(16.0f);
    final FontRenderer amplifierFont = FontPresets.axiformaBold(14.0f);
    private final Paint backgroundPaint = new Paint();
    private final Paint iconBgPaint = new Paint();
    private final Paint effectIconPaint = new Paint();
    private final Paint timerBarPaint = new Paint();

    public PotionEffectsHud() {
        super("Effects");
        this.setX(10.0f);
        this.setY(50.0f);
        this.setEnabled(true);
        this.backgroundPaint.setAntialias(true);
        this.iconBgPaint.setAntialias(true);
        this.effectIconPaint.setAntialias(true);
        this.timerBarPaint.setAntialias(true);
    }

    @Override
    public void onEnable() {
        if (this.effectEntryList != null) {
            this.effectEntryList.clear();
        }
    }

    @EventTarget
    public void onTick(TickEvent tickEvent) {
        if (mc.player == null) {
            if (this.effectEntryList != null && !this.effectEntryList.isEmpty()) {
                this.effectEntryList.forEach(PotionEffectsHud.EffectEntry::startRemove);
            }
            return;
        }
        Collection<MobEffectInstance> collection = mc.player.getActiveEffects();
        this.effectEntryList.stream().filter(e -> collection.stream().noneMatch(eff -> eff.getEffect() == e.getEffect())).forEach(PotionEffectsHud.EffectEntry::startRemove);
        for (MobEffectInstance mobEffectInstance : collection) {
            Optional<PotionEffectsHud.EffectEntry> optional = this.effectEntryList.stream().filter(e -> !e.removing && e.getEffect() == mobEffectInstance.getEffect()).findFirst();
            if (optional.isEmpty()) {
                PotionEffectsHud.EffectEntry potionEffectsHud$EffectEntry3 = new PotionEffectsHud.EffectEntry(this, mobEffectInstance);
                this.effectEntryList.add(potionEffectsHud$EffectEntry3);
                continue;
            }
            optional.get().updateEffect(mobEffectInstance);
        }
        this.effectEntryList.sort((potionEffectsHud$EffectEntry, potionEffectsHud$EffectEntry2) -> Float.compare(potionEffectsHud$EffectEntry2.getTotalWidth(), potionEffectsHud$EffectEntry.getTotalWidth()));
    }

    @Override
    public void onGlRender(GlRenderEvent glRenderEvent, float f, float f2) {
        if (!this.isEnabled()) {
            return;
        }
        this.renderEffects(glRenderEvent.drawContext(), f, f2);
    }

    private void renderEffects(DrawContext drawContext, float f, float f2) {
        if (drawContext == null) {
            return;
        }
        this.effectEntryList.removeIf(PotionEffectsHud.EffectEntry::isRemoveDone);
        if (this.effectEntryList.isEmpty()) {
            this.setWidth(0.0f);
            this.setHeight(0.0f);
            return;
        }
        float f3 = 2.0f;
        float f4 = 18.0f;
        float f5 = 4.5f;
        float f6 = 100.0f;
        float f7 = f2;
        float f8 = 0.0f;
        for (PotionEffectsHud.EffectEntry potionEffectsHud$EffectEntry : this.effectEntryList) {
            potionEffectsHud$EffectEntry.tick();
            float f9 = 20.0f;
            float f10 = 5.0f;
            float f11 = GlHelper.getStringWidth(potionEffectsHud$EffectEntry.effectName, this.effectNameFont);
            float f12 = GlHelper.getStringWidth(potionEffectsHud$EffectEntry.durationText, this.amplifierFont);
            float f13 = Math.max(f6, f9 + f11 + f12 + f10 * 3.0f);
            if (potionEffectsHud$EffectEntry.visible) {
                potionEffectsHud$EffectEntry.visible = false;
                potionEffectsHud$EffectEntry.fadeAnim.setCurrentValue(f7);
                potionEffectsHud$EffectEntry.show(f4);
            }
            potionEffectsHud$EffectEntry.fadeAnim.animate(f7, 0.15, Easings.EASE_OUT_SINE);
            float f14 = potionEffectsHud$EffectEntry.heightAnim.getValueF();
            float f15 = potionEffectsHud$EffectEntry.fadeAnim.getValueF();
            float f16 = potionEffectsHud$EffectEntry.widthAnim.getValueF();
            if (f13 > f8) {
                f8 = f13;
            }
            if (f14 <= 0.01f) {
                f7 += f16 + (f16 > 0.0f ? f3 : 0.0f);
                continue;
            }
            int n = this.getEffectColor(potionEffectsHud$EffectEntry.instance.getEffect());
            float f17 = f13 - f9;
            float f18 = f + f9;
            this.iconBgPaint.setColor(ColorUtil.fromARGB(30, 30, 35, (int)(80.0f * f14)));
            drawContext.drawRoundedRect(RoundedRectangle.ofXYWHRadii(f18, f15, f17, f16, new float[]{0.0f, 0.0f, 4.5f, 4.5f, 4.5f, 4.5f, 0.0f, 0.0f}), this.iconBgPaint);
            float f19 = potionEffectsHud$EffectEntry.instance.isInfiniteDuration() ? 1.0f : (float)potionEffectsHud$EffectEntry.instance.getDuration() / (float)potionEffectsHud$EffectEntry.originalDuration;
            this.effectIconPaint.setColor(ColorUtil.fromARGB(n >> 16 & 0xFF, n >> 8 & 0xFF, n & 0xFF, (int)(140.0f * f14)));
            if (f19 > 0.0f) {
                drawContext.drawRoundedRect(RoundedRectangle.ofXYWHRadii(f18, f15, f17 * f19, f16, new float[]{0.0f, 0.0f, f5 * f19, f5 * f19, f5 * f19, f5 * f19, 0.0f, 0.0f}), this.effectIconPaint);
            }
            int n2 = n >> 16 & 0xFF;
            int n3 = n >> 8 & 0xFF;
            int n4 = n & 0xFF;
            this.backgroundPaint.setColor(ColorUtil.fromARGB((int)((float)n2 * 0.7f + 76.5f), (int)((float)n3 * 0.7f + 76.5f), (int)((float)n4 * 0.7f + 76.5f), (int)(160.0f * f14)));
            drawContext.drawRoundedRect(RoundedRectangle.ofXYWHRadii(f, f15, f9, f16, new float[]{4.5f, 4.5f, 0.0f, 0.0f, 0.0f, 0.0f, 4.5f, 4.5f}), this.backgroundPaint);
            this.timerBarPaint.setColor(ColorUtil.fromARGB(255, 255, 255, (int)(185.0f * f14)));
            float f20 = f15 + (f16 - (float)GlHelper.getFontAscent(this.effectNameFont)) / 2.0f;
            float f21 = GlHelper.getStringWidth(potionEffectsHud$EffectEntry.amplifierText, this.timerFont);
            GlHelper.drawTextFormatted(potionEffectsHud$EffectEntry.amplifierText, f + (f9 - f21) / 2.0f, f20, this.timerFont, this.timerBarPaint, false);
            GlHelper.drawTextFormatted(potionEffectsHud$EffectEntry.effectName, f18 + f10, f20, this.effectNameFont, this.timerBarPaint, false);
            f12 = GlHelper.getStringWidth(potionEffectsHud$EffectEntry.durationText, this.amplifierFont);
            GlHelper.drawTextFormatted(potionEffectsHud$EffectEntry.durationText, f + f13 - f12 - f10, f20 + 1.0f, this.amplifierFont, this.timerBarPaint, false);
            f7 += f16 + (f16 > 0.0f ? f3 : 0.0f);
        }
        this.setWidth(f8);
        this.setHeight(Math.max(0.0f, f7 - f2 - f3));
    }

    private int getEffectColor(MobEffect mobEffect) {
        int n = mobEffect.getColor();
        return n != 0 ? n : 3376639;
    }

    String formatAmplifier(int n) {
        return switch (n) {
            case 1 -> "I";
            case 2 -> "II";
            case 3 -> "III";
            case 4 -> "IV";
            case 5 -> "V";
            default -> String.valueOf(n);
        };
    }

    String formatDuration(MobEffectInstance mobEffectInstance) {
        if (mobEffectInstance.isInfiniteDuration() || mobEffectInstance.getDuration() > 72000) {
            return "∞";
        }
        return MobEffectUtil.formatDuration(mobEffectInstance, 1.0f).getString();
    }

    @Override
    public void onRender2D(Render2DEvent render2DEvent, float f, float f2) {
    }

    @Override
    public void onSettings() {
    }
}