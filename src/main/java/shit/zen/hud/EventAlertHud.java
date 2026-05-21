package shit.zen.hud;

import com.mojang.blaze3d.vertex.PoseStack;
import java.awt.Color;
import java.util.Comparator;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.StreamSupport;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LightningBolt;
import net.minecraft.world.entity.projectile.ThrownEnderpearl;
import net.minecraft.world.phys.Vec3;
import shit.zen.ClientBase;
import shit.zen.modules.impl.render.Projectiles;
import shit.zen.render.DrawContext;
import shit.zen.render.FontPresets;
import shit.zen.render.FontRenderer;
import shit.zen.render.Fonts;
import shit.zen.render.GradientFactory;
import shit.zen.render.Paint;
import shit.zen.render.Path;
import shit.zen.render.PathMeasure;

public class EventAlertHud
extends ClientBase
implements IHudElement {
    public record AlertEntry(Vec3 position, double distance, Optional<Float> timeRemaining, String title, String icon) {

        public Vec3 pos() {
            return position;
        }

            public String getFormattedDescription() {
                if (timeRemaining.isPresent()) {
                    return String.format("%.1fs · %.1fm", timeRemaining.get(), distance);
                }
                return String.format("%.1fm", distance);
            }
        }

    private static final FontRenderer titleFont;
    private static final FontRenderer subtitleFont;
    private static final FontRenderer iconFont;
    private static final FontRenderer timeFont;
    private static final Path iconPath;
    private static final float iconPathLength;
    private final Map<Vec3, Long> activeAlerts = new ConcurrentHashMap<>();
    private Vec3 lastAlertPos = null;
    private long lastAlertTime = 0L;

    private Optional<EventAlertHud.AlertEntry> findProjectileAlert() {
        if (mc == null || mc.player == null || Projectiles.projectileMap.isEmpty()) {
            return Optional.empty();
        }
        Optional<Projectiles.ProjectileEntry> optional = Projectiles.projectileMap.entrySet().stream().filter(entry -> {
            if (mc.level == null) {
                return false;
            }
            Entity entity = mc.level.getEntity(entry.getKey());
            if (entity == null || !entity.isAlive() || entity.onGround()) {
                return false;
            }
            if (!(entity instanceof ThrownEnderpearl)) {
                return false;
            }
            Entity entity2 = ((ThrownEnderpearl)entity).getOwner();
            return entity2 != null && !entity2.equals(mc.player);
        }).map(java.util.Map.Entry::getValue).min(Comparator.comparingDouble(p -> p.getVelocity().distanceToSqr(mc.player.position())));
        return optional.map(e -> new EventAlertHud.AlertEntry(e.getVelocity(), e.getZ(), Optional.of((float)e.getX()), "Find an ender pearl!", ""));
    }

    private Optional<EventAlertHud.AlertEntry> findEntityAlert() {
        if (mc == null || mc.player == null || mc.level == null) {
            return Optional.empty();
        }
        long l = System.currentTimeMillis();
        StreamSupport.stream(mc.level.entitiesForRendering().spliterator(), false).filter(entity -> entity instanceof LightningBolt && entity.isAlive()).forEach(entity -> this.activeAlerts.put(entity.position(), l));
        this.activeAlerts.entrySet().removeIf(entry -> l - entry.getValue() > 5000L);
        if (this.activeAlerts.isEmpty()) {
            return Optional.empty();
        }
        return this.activeAlerts.keySet().stream().filter(v -> mc.player.position().distanceToSqr(v) < 65536.0).min(Comparator.comparingDouble(v -> mc.player.position().distanceToSqr(v))).map(v -> new EventAlertHud.AlertEntry(v, mc.player.position().distanceTo(v), Optional.empty(), "Found a lightning strike!", ""));
    }

    private Optional<EventAlertHud.AlertEntry> findBestAlert() {
        return java.util.stream.Stream.of(this.findProjectileAlert(), this.findEntityAlert()).filter(Optional::isPresent).map(Optional::get).min(Comparator.comparingDouble(a -> a.distance));
    }

    @Override
    public boolean isVisible() {
        return this.findBestAlert().isPresent();
    }

    @Override
    public IHudElement.Size getHudAlignment() {
        return new IHudElement.Size(200.0f, 40.0f);
    }

    @Override
    public IHudElement.Alignment getHudSize() {
        return IHudElement.Alignment.CENTER;
    }

    @Override
    public boolean hasBackground() {
        return true;
    }

    @Override
    public void renderGui(GuiGraphics guiGraphics, PoseStack poseStack, float f, float f2, float f3, float f4, float f5) {
    }

    @Override
    public void render(DrawContext drawContext, float f, float f2, float f3, float f4, float f5) {
        if (mc == null || mc.player == null || f5 <= 0.01f) {
            return;
        }
        this.findBestAlert().ifPresent(eventAlertHud$AlertEntry -> {
            float f6 = 12.0f;
            float f7 = f + f6;
            float f8 = f2 + f4 / 2.0f;
            boolean bl = "".equals(eventAlertHud$AlertEntry.icon());
            if (bl) {
                if (this.lastAlertPos == null || !this.lastAlertPos.equals(eventAlertHud$AlertEntry.pos())) {
                    this.lastAlertPos = eventAlertHud$AlertEntry.pos();
                    this.lastAlertTime = System.currentTimeMillis();
                }
            } else {
                this.lastAlertPos = null;
            }
            try (Paint paint = new Paint()){
                Paint.LinearGradient paint$LinearGradient;
                float f9;
                paint.setColor(this.colorWithAlpha(Color.WHITE.getRGB(), f5));
                float f10 = titleFont.getWidth(eventAlertHud$AlertEntry.icon());
                float f11 = f8 - (titleFont.getMetrics().ascent() + titleFont.getMetrics().descent()) / 2.0f - titleFont.getMetrics().descent();
                if (bl && iconPath != null) {
                    long l = System.currentTimeMillis() - this.lastAlertTime;
                    float f12 = Mth.clamp((float)l / 1000.0f, 0.0f, 1.0f);
                    paint.setStrokeCap(Paint.StrokeCap.STROKE);
                    paint.setStrokeWidth(1.5f);
                    paint.setStrokeJoin(Paint.StrokeJoin.ROUND);
                    if (f12 < 1.0f) {
                        f9 = iconPathLength;
                        float f13 = f12 * f9;
                        paint$LinearGradient = GradientFactory.buildLinearGradient(new float[]{f13, f9}, 0.0f);
                        try {
                            paint.setLinGradient(paint$LinearGradient);
                        } finally {
                            if (paint$LinearGradient != null) {
                                paint$LinearGradient.close();
                            }
                        }
                    } else {
                        paint.setLinGradient(null);
                    }
                    drawContext.save();
                    drawContext.translate(f7, f11);
                    drawContext.drawPath(iconPath, paint);
                    drawContext.restore();
                    paint.setStrokeCap(Paint.StrokeCap.FILL);
                    paint.setLinGradient(null);
                } else {
                    drawContext.drawString(eventAlertHud$AlertEntry.icon(), f7, f11, titleFont, paint);
                }
                float f14 = f7 + f10 + f6;
                paint.setColor(this.colorWithAlpha(Color.WHITE.getRGB(), f5));
                float f15 = f8 - iconFont.getMetrics().getLineHeight() / 2.0f + 2.0f;
                drawContext.drawString(eventAlertHud$AlertEntry.title(), f14, f15, iconFont, paint);
                String string = eventAlertHud$AlertEntry.getFormattedDescription();
                paint.setColor(this.colorWithAlpha(new Color(170, 170, 170).getRGB(), f5));
                f9 = f8 + timeFont.getMetrics().getLineHeight() / 2.0f + 6.0f;
                drawContext.drawString(string, f14, f9, timeFont, paint);
                paint.setColor(this.colorWithAlpha(Color.WHITE.getRGB(), f5));
                Vec3 vec3 = mc.player.getEyePosition();
                Vec3 alertPos = eventAlertHud$AlertEntry.pos();
                double d = alertPos.x - vec3.x;
                double d2 = alertPos.z - vec3.z;
                if (Math.sqrt(d * d + d2 * d2) < 1.0) {
                    String string2 = alertPos.y > vec3.y ? "" : "";
                    float f16 = subtitleFont.getWidth(string2);
                    float f17 = f + f3 - f6 - f16;
                    float f18 = f8 - (subtitleFont.getMetrics().ascent() + subtitleFont.getMetrics().descent()) / 2.0f - subtitleFont.getMetrics().descent();
                    drawContext.drawString(string2, f17, f18, subtitleFont, paint);
                } else {
                    float f19 = subtitleFont.getWidth("");
                    float f20 = f + f3 - f6 - f19;
                    float f21 = f8;
                    drawContext.save();
                    drawContext.translate(f20 + f19 / 2.0f, f21);
                    float f22 = (float)(Mth.atan2(d2, d) * 57.29577951308232) - 90.0f;
                    float f23 = Mth.wrapDegrees(mc.player.getYRot());
                    float f24 = Mth.wrapDegrees(f22 - f23);
                    drawContext.rotate(f24);
                    float f25 = subtitleFont.getMetrics().ascent();
                    float f26 = subtitleFont.getMetrics().descent();
                    float f27 = -(f25 + f26) / 2.0f;
                    drawContext.drawString("", -f19 / 2.0f, f27, subtitleFont, paint);
                    drawContext.restore();
                }
            }
        });
    }

    static {
        titleFont = Fonts.getRenderer("MaterialIcons-Regular.ttf", 48.0f);
        subtitleFont = Fonts.getRenderer("MaterialIcons-Regular.ttf", 44.0f);
        iconFont = FontPresets.poppinsBold(8.0f);
        timeFont = FontPresets.poppinsMedium(6.0f);
        Path path = null;
        float length = 0.0f;
        try {
            short[] glyphCodes = titleFont.getGlyphCodes("");
            if (glyphCodes != null && glyphCodes.length > 0) {
                path = titleFont.getGlyphPath(glyphCodes[0]);
                if (path != null) {
                    try (PathMeasure pathMeasure = new PathMeasure(path)) {
                        length = pathMeasure.getLength();
                    }
                }
            }
        } catch (Exception exception) {
            path = null;
        }
        iconPath = path;
        iconPathLength = length;
    }
}