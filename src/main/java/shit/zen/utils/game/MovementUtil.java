package shit.zen.utils.game;

import lombok.Generated;
import net.minecraft.util.Mth;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import shit.zen.ClientBase;
import shit.zen.event.impl.StrafeEvent;
import shit.zen.modules.impl.movement.TargetStrafe;
import shit.zen.utils.game.RotationUtil;
import shit.zen.utils.rotation.Rotation;

public final class MovementUtil
extends ClientBase {
    private static final String UTILITY_MSG = "This is a utility class and cannot be instantiated";

    public static boolean isMoving() {
        return mc.options.keyUp.isDown() || mc.options.keyDown.isDown() || mc.options.keyLeft.isDown() || mc.options.keyRight.isDown();
    }

    public static void strafeWithYaw(double d, double d2) {
        if (!MovementUtil.isInputActive()) {
            return;
        }
        Vec3 vec3 = mc.player.getDeltaMovement();
        mc.player.setDeltaMovement(vec3.x + (double)(-Mth.sin((float)d)) * d2, vec3.y, vec3.z + (double)Mth.cos((float)d) * d2);
    }

    public static void strafeForward(double d) {
        if (!MovementUtil.isInputActive()) {
            return;
        }
        double d2 = MovementUtil.getMovementYaw();
        Vec3 vec3 = mc.player.getDeltaMovement();
        mc.player.setDeltaMovement(vec3.x + (double)(-Mth.sin((float)d2)) * d, vec3.y, vec3.z + (double)Mth.cos((float)d2) * d);
    }

    public static double getMovementYaw() {
        float f = mc.player.getYRot();
        if (mc.player.zza < 0.0f) {
            f += 180.0f;
        }
        float f2 = 1.0f;
        if (mc.player.zza < 0.0f) {
            f2 = -0.5f;
        } else if (mc.player.zza > 0.0f) {
            f2 = 0.5f;
        }
        if (mc.player.xxa > 0.0f) {
            f -= 90.0f * f2;
        } else if (mc.player.xxa < 0.0f) {
            f += 90.0f * f2;
        }
        return Math.toRadians(f);
    }

    public static double getBaseSpeed() {
        double d = 0.2875;
        if (mc.player.hasEffect(MobEffects.MOVEMENT_SPEED)) {
            int n = mc.player.getEffect(MobEffects.MOVEMENT_SPEED).getAmplifier();
            d *= 1.0 + 0.2 * (double)(n + 1);
        }
        return d;
    }

    public static double hypot(double d, double d2) {
        return Math.sqrt(d * d + d2 * d2);
    }

    public static double getSpeed() {
        return MovementUtil.hypot(mc.player.getDeltaMovement().x, mc.player.getDeltaMovement().z);
    }

    public static void setSpeed(double d) {
        float f = mc.player.input.forwardImpulse;
        float f2 = mc.player.input.leftImpulse;
        float f3 = mc.player.getYRot();
        if (f == 0.0f && f2 == 0.0f) {
            mc.player.setDeltaMovement(0.0, mc.player.getDeltaMovement().y, 0.0);
            return;
        }
        if (f != 0.0f && f2 != 0.0f) {
            f = (float)((double)f * Math.sin(0.7853981633974483));
            f2 = (float)((double)f2 * Math.cos(0.7853981633974483));
        }
        double d2 = (double)f * d * -Math.sin(Math.toRadians(f3)) + (double)f2 * d * Math.cos(Math.toRadians(f3));
        double d3 = (double)f * d * Math.cos(Math.toRadians(f3)) - (double)f2 * d * -Math.sin(Math.toRadians(f3));
        mc.player.setDeltaMovement(d2, mc.player.getDeltaMovement().y, d3);
    }

    public static boolean isAboveVoid(double d, double d2, double d3) {
        while (d2 > 0.0) {
            Vec3 vec3 = new Vec3(d, d2, d3);
            Vec3 vec32 = new Vec3(d, d2 - 1.0, d3);
            BlockHitResult blockHitResult = mc.level.clip(new ClipContext(vec3, vec32, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, mc.player));
            if (blockHitResult != null && blockHitResult.getType() != HitResult.Type.MISS) {
                return false;
            }
            d2 -= 1.0;
        }
        return true;
    }

    public static void stop() {
        mc.player.setDeltaMovement(0.0, mc.player.getDeltaMovement().y, 0.0);
    }

    public static double getSpeedHypot() {
        return Math.hypot(mc.player.getDeltaMovement().x, mc.player.getDeltaMovement().z);
    }

    public static void handleStrafe(StrafeEvent strafeEvent, float f) {
        float f2 = strafeEvent.getForward();
        float f3 = strafeEvent.getStrafe();
        float f4 = mc.player.getYRot();
        if (TargetStrafe.strafeTarget != null && TargetStrafe.INSTANCE.isEnabled() && (!TargetStrafe.isSmartStrafe() || mc.options.keyJump.isDown())) {
            float f5 = (float)(MovementUtil.getBaseSpeed() / ((double)TargetStrafe.getRange() * Math.PI * 2.0) * 360.0) * (float)TargetStrafe.strafeDirectionSign;
            Rotation rotation = RotationUtil.rotationToForBow(new Vec3(TargetStrafe.strafeTarget.getX(), TargetStrafe.strafeTarget.getY(), TargetStrafe.strafeTarget.getZ()), new Vec3(mc.player.getX(), mc.player.getY(), mc.player.getZ()));
            rotation.setYaw(rotation.getYaw() + f5);
            float f6 = rotation.getYaw() * ((float)Math.PI / 180);
            double d = TargetStrafe.strafeTarget.getX() - Math.sin(f6) * (double)TargetStrafe.getRange();
            double d2 = TargetStrafe.strafeTarget.getZ() + Math.cos(f6) * (double)TargetStrafe.getRange();
            f4 = (float)Math.toDegrees(RotationUtil.rotationToForBow(new Vec3(mc.player.getX(), mc.player.getY(), mc.player.getZ()), new Vec3(d, TargetStrafe.strafeTarget.getY(), d2)).getYaw() * ((float)Math.PI / 180));
        }
        double d = Mth.wrapDegrees(Math.toDegrees(MovementUtil.getDirectionYaw(f4, f2, f3)));
        if (f2 == 0.0f && f3 == 0.0f) {
            return;
        }
        int n = 0;
        int n2 = 0;
        float f7 = Float.MAX_VALUE;
        for (int i = -1; i <= 1; ++i) {
            for (int j = -1; j <= 1; ++j) {
                double d3;
                double d4;
                if (j == 0 && i == 0 || !((d4 = Math.abs(d - (d3 = Mth.wrapDegrees(Math.toDegrees(MovementUtil.getDirectionYaw(f, i, j)))))) < (double)f7)) continue;
                f7 = (float)d4;
                n = i;
                n2 = j;
            }
        }
        strafeEvent.setForward(n);
        strafeEvent.setStrafe(n2);
    }

    public static boolean isInputActive() {
        return mc.player != null && mc.level != null && ((double)mc.player.input.forwardImpulse != 0.0 || (double)mc.player.input.leftImpulse != 0.0);
    }

    public static double getDirectionYaw(float f, double d, double d2) {
        if (d < 0.0) {
            f += 180.0f;
        }
        float f2 = 1.0f;
        if (d < 0.0) {
            f2 = -0.5f;
        } else if (d > 0.0) {
            f2 = 0.5f;
        }
        if (d2 > 0.0) {
            f -= 90.0f * f2;
        }
        if (d2 < 0.0) {
            f += 90.0f * f2;
        }
        return Math.toRadians(f);
    }

    private static float getDirectionAngle(float f, float f2) {
        boolean bl;
        float f3 = mc.player.getYRot();
        boolean bl2 = f > 0.0f;
        boolean bl3 = f < 0.0f;
        boolean bl4 = f2 > 0.0f;
        boolean bl5 = f2 < 0.0f;
        boolean bl6 = bl4 || bl5;
        boolean bl7 = bl = bl2 || bl3;
        if (f != 0.0f || f2 != 0.0f) {
            if (bl3 && !bl6) {
                return f3 + 180.0f;
            }
            if (bl2 && bl5) {
                return f3 + 45.0f;
            }
            if (bl2 && bl4) {
                return f3 - 45.0f;
            }
            if (!bl && bl5) {
                return f3 + 90.0f;
            }
            if (!bl && bl4) {
                return f3 - 90.0f;
            }
            if (bl3 && bl5) {
                return f3 + 135.0f;
            }
            if (bl3) {
                return f3 - 135.0f;
            }
        }
        return f3;
    }

    public static double getEntitySpeed(Entity entity) {
        return Math.hypot(entity.getX() - entity.xo, entity.getZ() - entity.zo);
    }

    public static double getSpeedBps() {
        return MovementUtil.getEntitySpeed(mc.player) * 20.0 * 1.0;
    }

    @Generated
    private MovementUtil() {
        throw new UnsupportedOperationException(UTILITY_MSG);
    }
}