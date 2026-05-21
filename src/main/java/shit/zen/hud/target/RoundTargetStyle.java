package shit.zen.hud.target;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import java.awt.Color;

import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import shit.zen.event.impl.Render2DEvent;
import shit.zen.modules.impl.render.NameProtect;
import shit.zen.render.FontPresets;
import shit.zen.render.FontRenderer;
import shit.zen.render.GlHelper;
import shit.zen.render.Paint;
import shit.zen.render.Renderer;
import shit.zen.utils.animation.SmoothAnimationTimer;
import shit.zen.utils.math.Easings;
import shit.zen.utils.render.RenderUtil;

public class RoundTargetStyle
extends TargetStyle {
    private static final Color COLOR_PANEL_BG;
    private static final Color COLOR_HEALTH_BG;
    private static final Color COLOR_HEALTH_BAR;
    private static final Color COLOR_HEALTH_BAR2;
    private static final Color COLOR_HEALTH_LAG;
    private final FontRenderer nameFont;
    private final FontRenderer subFont;
    private final SmoothAnimationTimer scaleAnim = new SmoothAnimationTimer();
    private LivingEntity lastTarget;
    private int lastHurtTime;
    private final ItemStack[] equipmentSlots = new ItemStack[4];
    private final Paint panelPaint = new Paint();
    private final Paint healthBgPaint = new Paint();
    private final Paint healthLagPaint = new Paint();
    private final SmoothAnimationTimer fadeAnim;
    private final SmoothAnimationTimer slideAnim;
    private final SmoothAnimationTimer contentAnim;
    private boolean visible = false;
    private LivingEntity currentTarget;
    private long lastActiveTime = 0L;
    private static final String currentTargetName;

    public RoundTargetStyle() {
        super(currentTargetName);
        this.nameFont = FontPresets.pingfang(14.0f);
        this.subFont = FontPresets.astaSans(13.0f);
        this.scaleAnim.setCurrentValue(1.0);
        this.fadeAnim = new SmoothAnimationTimer();
        this.fadeAnim.setCurrentValue(0.0);
        this.slideAnim = new SmoothAnimationTimer();
        this.slideAnim.setCurrentValue(5.0);
        this.contentAnim = new SmoothAnimationTimer();
        this.contentAnim.setCurrentValue(0.0);
    }

    @Override
    public void render(Render2DEvent render2DEvent, LivingEntity livingEntity, SmoothAnimationTimer smoothAnimationTimer, SmoothAnimationTimer smoothAnimationTimer2, float f, float f2, float f3) {
        float f4;
        boolean bl;
        for (int i = 0; i < this.equipmentSlots.length; ++i) {
            this.equipmentSlots[i] = ItemStack.EMPTY;
        }
        float f5 = 120.0f;
        float f6 = 38.0f;
        float f7 = 3.0f;
        float f8 = 5.0f;
        boolean bl2 = livingEntity != null;
        long l = System.currentTimeMillis();
        boolean bl3 = false;
        if (bl2) {
            this.lastActiveTime = l;
            if (this.currentTarget != livingEntity) {
                this.currentTarget = livingEntity;
                this.lastTarget = livingEntity;
                bl3 = true;
            }
        }
        boolean bl4 = bl = bl2 || l - this.lastActiveTime < 300L;
        if (bl != this.visible) {
            this.visible = bl;
            if (this.visible) {
                this.fadeAnim.animate(1.0, 0.35, Easings.EASE_OUT_POW3);
                this.slideAnim.setCurrentValue(5.0);
                this.slideAnim.setStartTime(0L);
                this.contentAnim.setCurrentValue(0.0);
                this.contentAnim.setStartTime(0L);
                this.scaleAnim.setCurrentValue(1.0);
                this.scaleAnim.animate(1.0, 0.0);
            } else {
                this.fadeAnim.animate(0.0, 0.15, Easings.EASE_IN_POW3);
                this.slideAnim.animate(5.0, 0.15, Easings.EASE_IN_POW3);
                this.contentAnim.animate(0.0, 0.15, Easings.EASE_IN_POW3);
            }
        } else if (bl3 && this.visible) {
            this.fadeAnim.animate(1.0, 0.35, Easings.EASE_OUT_POW3);
            this.slideAnim.setCurrentValue(5.0);
            this.slideAnim.setStartTime(0L);
            this.contentAnim.setCurrentValue(0.0);
            this.contentAnim.setStartTime(0L);
            this.scaleAnim.setCurrentValue(1.0);
            this.scaleAnim.animate(1.0, 0.0);
        }
        this.fadeAnim.tick();
        if (this.fadeAnim.isAnimating() && this.visible) {
            if (this.fadeAnim.getProgress() >= 0.08 && this.slideAnim.getStartTime() == 0L) {
                this.slideAnim.animate(0.0, 0.3, Easings.EASE_OUT_POW3);
            }
            if (this.fadeAnim.getProgress() >= 0.15 && this.contentAnim.getStartTime() == 0L) {
                this.contentAnim.animate(1.0, 0.4, Easings.EASE_OUT_POW3);
            }
        }
        if (this.slideAnim.getStartTime() != 0L) {
            this.slideAnim.tick();
        }
        if (this.contentAnim.getStartTime() != 0L) {
            this.contentAnim.tick();
        }
        if ((f4 = this.fadeAnim.getValueF()) <= 0.01f) {
            return;
        }
        LivingEntity livingEntity2 = this.currentTarget;
        if (livingEntity2 == null) {
            return;
        }
        float f9 = 30.0f;
        float f10 = 4.0f;
        float f11 = f2 + 4.0f + 30.0f + 4.0f;
        float f12 = 120.0f - (f11 - f2) - 3.0f;
        float f13 = f3 + 3.0f + 2.0f;
        float f14 = GlHelper.getFontAscent(this.nameFont);
        float f15 = f13 + f14 + 4.0f;
        PoseStack poseStack = render2DEvent.guiGraphics().pose();
        poseStack.pushPose();
        RenderUtil.drawBlurredRect(poseStack, f2, f3, 120.0f, 38.0f, 5.0f, 15.0f, 0.95f * f4, 0);
        poseStack.popPose();
        Renderer.renderConsumer((drawContext -> {
            this.panelPaint.setColor(new Color(0, 0, 0, (int)((float)COLOR_PANEL_BG.getAlpha() * f4)).getRGB());
            GlHelper.drawRoundedRect(f2, f3, 120.0f, 38.0f, 5.0f, this.panelPaint);
            if (bl2 && livingEntity.hurtTime > this.lastHurtTime) {
                this.scaleAnim.setCurrentValue(0.7f);
                this.scaleAnim.animate(1.0, 1.5, Easings.EASE_OUT_ELASTIC);
            }
            if (bl2) {
                this.lastHurtTime = livingEntity.hurtTime;
            }
            this.scaleAnim.tick();
            float scaleValue = this.scaleAnim.getValueF();
            float minScale = (float)Math.max(0.7, f4);
            float combinedScale = scaleValue * minScale;
            float headSize = 30.0f * combinedScale;
            float headX = f2 + 4.0f + (30.0f - headSize) / 2.0f;
            float headY = f3 + (38.0f - headSize) / 2.0f;
            if (livingEntity2 instanceof AbstractClientPlayer abstractClientPlayer) {
                GlHelper.drawPlayerHeadRounded(abstractClientPlayer, headX, headY, headSize, headSize, f4, 5.0f * combinedScale);
            }
            float slideOff = this.slideAnim.getValueF();
            String string = livingEntity2 == mc.player ? NameProtect.getProtectedName() : livingEntity2.getName().getString();
            GlHelper.drawTextShadowLegacy(string, f11, f13 + 1.0f + slideOff, this.nameFont, new Color(1.0f, 1.0f, 1.0f, f4).getRGB());
            float healthY = f15 + 16.0f;
            float healthH = 4.0f;
            float healthW = f12 - 2.0f;
            this.healthBgPaint.setColor(new Color(0, 0, 0, (int)((float)COLOR_HEALTH_BG.getAlpha() * f4)).getRGB());
            GlHelper.drawRoundedRect(f11, healthY, healthW, healthH, 3.0f, this.healthBgPaint);
            this.healthLagPaint.setColor(new Color(99, 99, 99, (int)((float)COLOR_HEALTH_LAG.getAlpha() * f4)).getRGB());
            float lagWidth = smoothAnimationTimer2.getValueF() * healthW;
            GlHelper.drawRoundedRect(f11, healthY, lagWidth, healthH, 3.0f, this.healthLagPaint);
            float contentVal = this.contentAnim.getValueF();
            float barWidth = smoothAnimationTimer.getValueF() * healthW * contentVal;
            Color color = new Color(COLOR_HEALTH_BAR.getRed(), COLOR_HEALTH_BAR.getGreen(), COLOR_HEALTH_BAR.getBlue(), (int)(255.0f * f4));
            Color color2 = new Color(COLOR_HEALTH_BAR2.getRed(), COLOR_HEALTH_BAR2.getGreen(), COLOR_HEALTH_BAR2.getBlue(), (int)(255.0f * f4));
            GlHelper.drawGradientRoundedRect(f11, healthY, barWidth, healthH, 3.0f, color, color2);
        }));
        if (livingEntity2 != null) {
            this.equipmentSlots[0] = livingEntity2.getItemBySlot(EquipmentSlot.HEAD);
            this.equipmentSlots[1] = livingEntity2.getItemBySlot(EquipmentSlot.CHEST);
            this.equipmentSlots[2] = livingEntity2.getItemBySlot(EquipmentSlot.LEGS);
            this.equipmentSlots[3] = livingEntity2.getItemBySlot(EquipmentSlot.FEET);
        }
        float f16 = f11;
        float f17 = 0.8f;
        float f18 = 16.0f * f17;
        float f19 = 2.0f;
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        for (ItemStack itemStack : this.equipmentSlots) {
            if (itemStack != null && !itemStack.isEmpty()) {
                PoseStack poseStack2 = render2DEvent.guiGraphics().pose();
                poseStack2.pushPose();
                poseStack2.translate(f16, f15, 0.0f);
                poseStack2.scale(f17, f17, 1.0f);
                RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, f4);
                render2DEvent.guiGraphics().renderItem(itemStack, 0, 0);
                poseStack2.popPose();
            }
            f16 += f18 + f19;
        }
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.disableBlend();
    }

    static {
        currentTargetName = "Round";
        COLOR_PANEL_BG = new Color(0, 0, 0, 80);
        COLOR_HEALTH_BG = new Color(0, 0, 0, 100);
        COLOR_HEALTH_BAR = new Color(0, 150, 255);
        COLOR_HEALTH_BAR2 = new Color(0, 100, 255);
        COLOR_HEALTH_LAG = new Color(99, 99, 99, 120);
    }
}