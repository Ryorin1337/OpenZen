package shit.zen.utils.game;

import java.util.Iterator;
import java.util.List;
import java.util.Random;
import lombok.Generated;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import org.antlr.v4.runtime.misc.OrderedHashSet;
import shit.zen.ClientBase;
import shit.zen.utils.math.MathUtil;
import shit.zen.utils.rotation.Rotation;
import shit.zen.utils.rotation.RotationHandler;

public final class RotationUtil
extends ClientBase {
    public record BestHitInfo(Vec3 hitPoint, Vec3 closestPoint, double distance, Rotation rotation) {
    }

    public static Rotation normalizeRotation(Rotation rotation) {
        return new Rotation(Mth.wrapDegrees(rotation.getYaw()), Mth.wrapDegrees(rotation.getPitch()));
    }

    public static Rotation smoothRotation(Rotation rotation, Rotation rotation2, double d) {
        float f = rotation2.getYaw();
        float f2 = rotation2.getPitch();
        float f3 = rotation.getYaw();
        float f4 = rotation.getPitch();
        if (d != 0.0) {
            float f5 = (float)d;
            double d2 = Mth.wrapDegrees(rotation2.getYaw() - rotation.getYaw());
            double d3 = f2 - f4;
            double d4 = Math.sqrt(d2 * d2 + d3 * d3);
            double d5 = Math.abs(d2 / d4);
            double d6 = Math.abs(d3 / d4);
            double d7 = (double)f5 * d5;
            double d8 = (double)f5 * d6;
            float f6 = (float)Math.max(Math.min(d2, d7), -d7);
            float f7 = (float)Math.max(Math.min(d3, d8), -d8);
            f = f3 + f6;
            f2 = f4 + f7;
        }
        boolean bl = Math.random() > 0.8;
        for (int i = 1; i <= (int)(2.0 + Math.random() * 2.0); ++i) {
            Rotation rotation3;
            Rotation rotation4;
            if (bl) {
                f += (float)((Math.random() - 0.5) / 1.0E8);
                f2 -= (float)(Math.random() / 2.0E8);
            }
            if ((rotation4 = (rotation3 = new Rotation(f, f2)).snapToSensitivity(Float.valueOf(mc.options.sensitivity().get().floatValue()))) == null) continue;
            f = rotation4.getYaw();
            f2 = Mth.clamp(rotation4.getPitch(), -90.0f, 90.0f);
        }
        return new Rotation(f, f2);
    }

    public static float clampAngle(float f, float f2) {
        if (Math.abs(f) < f2) {
            return f;
        }
        if (f > 0.0f) {
            return f2;
        }
        if (f < 0.0f) {
            return -f2;
        }
        return 0.0f;
    }

    public static Rotation rotationTo(Vec3 vec3, Vec3 vec32) {
        double d = vec32.x - vec3.x;
        double d2 = vec32.y - vec3.y;
        double d3 = vec32.z - vec3.z;
        double d4 = Math.sqrt(d * d + d3 * d3);
        float f = RotationUtil.toDegrees(Math.atan2(d3, d)) - 90.0f;
        float f2 = RotationUtil.toDegrees(-Math.atan2(d2, d4));
        return new Rotation(f, f2);
    }

    public static float toDegrees(double d) {
        return (float)(d * 180.0 / Math.PI);
    }

    public static Rotation rotationToForBow(Vec3 vec3, Vec3 vec32) {
        double d = vec32.x - vec3.x;
        double d2 = vec32.y - vec3.y;
        double d3 = vec32.z - vec3.z;
        return RotationUtil.rotationFromDeltas(d, d2, d3);
    }

    public static boolean isLookingAt(float f, LivingEntity livingEntity) {
        if (mc.player == null) {
            return false;
        }
        Vec3 vec3 = mc.player.getEyePosition(1.0f);
        Vec3 vec32 = new Vec3(livingEntity.getX(), livingEntity.getY() + (double)livingEntity.getBbHeight() * 0.5, livingEntity.getZ());
        Vec3 vec33 = mc.player.getLookAngle();
        Vec3 vec34 = vec32.subtract(vec3);
        if (vec34.lengthSqr() < 1.0E-7) {
            return true;
        }
        Vec3 vec35 = vec34.normalize();
        double d = vec33.dot(vec35);
        double d2 = Math.toDegrees(Math.acos(d));
        return f >= 180.0f || d2 <= (double)f;
    }

    public static Rotation rotationFromEyes(Vec3 vec3) {
        if (mc.player == null) {
            return null;
        }
        return RotationUtil.bowRotation(mc.player.position().add(0.0, mc.player.getEyeHeight(), 0.0), vec3);
    }

    public static Rotation bowRotation(Vec3 vec3, Vec3 vec32) {
        Vec3 vec33 = vec32.add(0.0, -0.7, 0.0).subtract(vec3);
        double d = Math.hypot(vec33.x, vec33.z);
        double d2 = 0.03;
        double d3 = 1.5;
        float f = (float)(Mth.atan2(vec33.z, vec33.x) * 57.29577951308232) - 90.0f;
        double d4 = d / 1.5;
        double d5 = 0.015 * d4 * d4;
        double d6 = vec33.y + d5;
        double d7 = Math.atan2(d6, d);
        float f2 = (float)(-(d7 * 57.29577951308232));
        return new Rotation(f, f2);
    }

    public static Rotation rotationToBlock(BlockPos blockPos, float f) {
        Vec3 vec3 = new Vec3(mc.player.getX() + mc.player.getDeltaMovement().x * (double)f, mc.player.getY() + (double)mc.player.getEyeHeight() + mc.player.getDeltaMovement().y() * (double)f, mc.player.getZ() + mc.player.getDeltaMovement().z() * (double)f);
        double d = (double)blockPos.getX() - vec3.x + 0.5;
        double d2 = (double)blockPos.getY() - vec3.y + 0.5;
        double d3 = (double)blockPos.getZ() - vec3.z + 0.5;
        return RotationUtil.rotationFromDeltas(RotationUtil.addNoise(d), RotationUtil.addNoise(d2), RotationUtil.addNoise(d3));
    }

    public static Rotation rotationFromDeltas(double d, double d2, double d3) {
        double d4 = Math.sqrt(d * d + d3 * d3);
        float f = (float)Math.toDegrees(Math.atan2(d3, d)) - 90.0f;
        float f2 = (float)(-Math.toDegrees(Math.atan2(d2, d4)));
        return new Rotation(Mth.wrapDegrees(f), Mth.wrapDegrees(f2));
    }

    private static double addNoise(double d) {
        return d + MathUtil.randomDouble(0.05, 0.08) * (MathUtil.randomDouble(0.0, 1.0) * 2.0 - 1.0);
    }

    public static double getHitDistance(Entity entity, Vec3 vec3, Rotation rotation) {
        AABB aABB = RotationUtil.getEntityBB(entity);
        HitResult hitResult = RotationUtil.raycastForBB(aABB, rotation, vec3, 6.0);
        if (hitResult != null) {
            Vec3 vec32 = hitResult.getLocation();
            return vec32.distanceTo(vec3);
        }
        return 1000.0;
    }

    public static List<Float> getEyeHeights() {
        return List.of(Float.valueOf(mc.player.getEyeHeight()));
    }

    public static double getMinHitDistance(Entity entity, Rotation rotation) {
        double d = Double.MAX_VALUE;
        Iterator<Float> iterator = RotationUtil.getEyeHeights().iterator();
        while (iterator.hasNext()) {
            double d2 = iterator.next();
            Vec3 vec3 = new Vec3(mc.player.getX(), mc.player.getY(), mc.player.getZ());
            Vec3 vec32 = vec3.add(0.0, d2, 0.0);
            d = Math.min(d, RotationUtil.getHitDistance(entity, vec32, rotation));
        }
        return d;
    }

    public static HitResult raycastForBB(AABB aABB, Rotation rotation, Vec3 vec3, double d) {
        Vec3 vec32 = RotationUtil.getDirection(rotation.getYaw(), rotation.getPitch());
        Vec3 vec33 = vec3.add(vec32.x * d, vec32.y * d, vec32.z * d);
        return ProjectileUtil.getEntityHitResult(mc.player, vec3, vec33, aABB, entity -> !entity.isSpectator() && entity.isPickable(), d * d);
    }

    public static float moveTowards(float f, float f2, float f3) {
        return RotationUtil.rotateTowards(f2, f3, f);
    }

    public static float rotateTowards(float f, float f2, float f3) {
        float f4 = Mth.wrapDegrees(f2 - f);
        if (f4 > f3) {
            f4 = f3;
        }
        if (f4 < -f3) {
            f4 = -f3;
        }
        return f + f4;
    }

    public static float angleDiff(float f, float f2) {
        float f3 = Math.abs(f - f2) % 360.0f;
        if (f3 > 180.0f) {
            f3 = 0.0f;
        }
        return f3;
    }

    public static float ballisticPitch(float f, float f2, float f3, float f4) {
        float f5 = f3 * f3 * f3 * f3 - f4 * (f4 * (f * f) + 2.0f * f2 * (f3 * f3));
        return (float)Math.toDegrees(Math.atan(((double)(f3 * f3) - Math.sqrt(f5)) / (double)(f4 * f)));
    }

    public static float[] getBallisticAngles(Vec3 vec3) {
        if (mc.player == null || mc.level == null) {
            return null;
        }
        Vec3 vec32 = mc.player.getEyePosition();
        double d = 1.5;
        double d2 = 0.03;
        double d3 = 0.99;
        double d4 = vec3.x - vec32.x;
        double d5 = vec3.y - vec32.y;
        double d6 = vec3.z - vec32.z;
        float f = (float)(Math.toDegrees(Math.atan2(d6, d4)) - 90.0);
        double d7 = Math.sqrt(d4 * d4 + d6 * d6);
        if (d7 == 0.0) {
            return new float[]{f, d5 > 0.0 ? -90.0f : 90.0f};
        }
        block0: for (float f2 = 90.0f; f2 >= -90.0f; f2 -= 0.5f) {
            double d8 = Math.toRadians(f2);
            double d9 = -Math.sin(Math.toRadians(f)) * Math.cos(d8);
            double d10 = -Math.sin(d8);
            double d11 = Math.cos(Math.toRadians(f)) * Math.cos(d8);
            double d12 = Math.sqrt(d9 * d9 + d10 * d10 + d11 * d11);
            d9 = d9 / d12 * d;
            d10 = d10 / d12 * d;
            d11 = d11 / d12 * d;
            Vec3 vec33 = new Vec3(vec32.x, vec32.y, vec32.z);
            Vec3 vec34 = new Vec3(d9, d10, d11);
            for (int i = 0; i < 300; ++i) {
                vec33 = vec33.add(vec34);
                vec34 = new Vec3(vec34.x * d3, vec34.y * d3 - d2, vec34.z * d3);
                if (vec33.y < (double)(mc.level.getMinBuildHeight() - 10)) continue block0;
                double d13 = Math.sqrt(Math.pow(vec33.x - vec32.x, 2.0) + Math.pow(vec33.z - vec32.z, 2.0));
                if (!(d13 >= d7)) continue;
                if (Math.abs(vec33.y - vec3.y) < 1.0) {
                    return new float[]{Mth.wrapDegrees(f), Mth.wrapDegrees(f2)};
                }
                if (vec34.y < 0.0 && vec33.y < vec3.y) continue block0;
            }
        }
        return null;
    }

    public static RotationUtil.BestHitInfo getBestHit(Entity entity) {
        double d;
        double d2;
        Vec3 vec3 = new Vec3(mc.player.getX(), mc.player.getY(), mc.player.getZ());
        Vec3 vec32 = vec3.add(0.0, mc.player.getEyeHeight(), 0.0);
        AABB aABB = RotationUtil.getEntityBB(entity);
        double d3 = aABB.minX;
        double d4 = aABB.minY;
        double d5 = aABB.minZ;
        double d6 = aABB.maxX;
        double d7 = aABB.maxY;
        double d8 = aABB.maxZ;
        double d9 = 0.1;
        OrderedHashSet<Vec3> orderedHashSet = new OrderedHashSet<>();
        orderedHashSet.add(new Vec3(d3 + d6 / 2.0, d4 + d7 / 2.0, d5 + d8 / 2.0));
        orderedHashSet.add(RotationUtil.closestPoint(vec32, aABB));
        for (d2 = d3; d2 <= d6; d2 += d9) {
            for (d = d4; d <= d7; d += d9) {
                orderedHashSet.add(new Vec3(d2, d, d5));
                orderedHashSet.add(new Vec3(d2, d, d8));
            }
        }
        for (d2 = d3; d2 <= d6; d2 += d9) {
            for (d = d5; d <= d8; d += d9) {
                orderedHashSet.add(new Vec3(d2, d4, d));
                orderedHashSet.add(new Vec3(d2, d7, d));
            }
        }
        for (d2 = d4; d2 <= d7; d2 += d9) {
            for (d = d5; d <= d8; d += d9) {
                orderedHashSet.add(new Vec3(d3, d2, d));
                orderedHashSet.add(new Vec3(d6, d2, d));
            }
        }
        for (Vec3 vec33 : orderedHashSet) {
            HitResult hitResult;
            Rotation rotation = RotationUtil.exactRotation(vec32, vec33);
            if (rotation == null) {
                logger.error("NULL????");
            }
            if ((hitResult = RotationUtil.performRaycast(rotation)) == null) {
                logger.error("NULL2????");
            }
            if (!RotationUtil.isHitValid(vec32, hitResult, entity)) continue;
            try {
                Vec3 vec34 = hitResult.getLocation();
                return new RotationUtil.BestHitInfo(vec32, vec34, vec34.distanceTo(vec32), RotationUtil.getSensitivitySnappedRotation(rotation.getYaw(), rotation.getPitch(), RotationHandler.prevRotation.yaw, RotationHandler.prevRotation.pitch));
            } catch (Exception exception) {
                logger.error("er here");
                logger.error(exception);
                exception.printStackTrace();
                return null;
            }
        }
        return new RotationUtil.BestHitInfo(vec32, vec32, 1000.0, null);
    }

    public static Rotation getEntityRotation(Entity entity, float f, float f2, float f3) {
        if (entity == null) {
            return null;
        }
        LocalPlayer localPlayer = mc.player;
        if (localPlayer == null) {
            return null;
        }
        Random random = new Random();
        double d = (random.nextDouble() - 0.5) * (double)entity.getBbWidth() * 0.5 * (double)f;
        double d2 = (random.nextDouble() - 0.5) * (double)entity.getBbWidth() * 0.5 * (double)f;
        double d3 = entity.getY();
        d3 = f3 <= 0.1f ? (d3 += entity.getBbHeight() * 0.1f) : (f3 >= 0.9f ? (d3 += entity.getEyeHeight() * (0.8f + random.nextFloat() * 0.2f)) : (d3 += entity.getBbHeight() * Mth.clamp(f3, 0.1f, 0.9f)));
        double d4 = (random.nextDouble() - 0.5) * (double)entity.getBbHeight() * 0.3 * (double)f2;
        double d5 = entity.getX() + d;
        double d6 = d3 + d4;
        double d7 = entity.getZ() + d2;
        double d8 = d5 - localPlayer.getX();
        double d9 = d6 - (localPlayer.getY() + (double)localPlayer.getEyeHeight());
        double d10 = d7 - localPlayer.getZ();
        double d11 = Mth.sqrt((float)(d8 * d8 + d10 * d10));
        float f4 = (float)(Math.atan2(d10, d8) * 180.0 / Math.PI) - 90.0f;
        float f5 = (float)(-(Math.atan2(d9, d11) * 180.0 / Math.PI));
        float f6 = localPlayer.getYRot() + Mth.wrapDegrees(f4 - localPlayer.getYRot());
        float f7 = localPlayer.getXRot() + Mth.wrapDegrees(f5 - localPlayer.getXRot());
        f7 = Mth.clamp(f7, -90.0f, 90.0f);
        return RotationUtil.getSensitivitySnappedRotation(f6, f7, RotationHandler.prevRotation.yaw, RotationHandler.prevRotation.pitch);
    }

    public static Rotation getSensitivitySnappedRotation(float f, float f2, float f3, float f4) {
        float f5 = (float)(mc.options.sensitivity().get() * (double)0.6f + (double)0.2f);
        float f6 = f5 * f5 * f5 * 1.2f;
        float f7 = f - f3;
        float f8 = f2 - f4;
        float f9 = f7 - f7 % f6;
        float f10 = f8 - f8 % f6;
        float f11 = f3 + f9;
        float f12 = f4 + f10;
        return new Rotation(f11, f12);
    }

    private static AABB getEntityBB(Entity entity) {
        return entity.getBoundingBox();
    }

    private static boolean isHitValid(Vec3 vec3, HitResult hitResult, Entity entity) {
        if (hitResult.getType() == HitResult.Type.ENTITY && ((EntityHitResult)hitResult).getEntity() == entity) {
            Vec3 vec32 = hitResult.getLocation();
            return RotationUtil.isInsideAABB(RotationUtil.getEntityBB(entity), vec3) || vec32.distanceTo(vec3) <= 3.0;
        }
        return false;
    }

    private static HitResult performRaycast(Rotation rotation) {
        AABB aABB;
        double d = mc.gameMode.getPickRange();
        HitResult hitResult = RayTraceUtil.rayTrace(d, 1.0f, false, rotation);
        Vec3 vec3 = mc.player.getEyePosition(1.0f);
        boolean bl = false;
        double d2 = d;
        if (mc.gameMode.hasFarPickRange()) {
            d = d2 = 6.0;
        } else if (d > 3.0) {
            bl = true;
        }
        d2 *= d2;
        if (hitResult != null) {
            d2 = hitResult.getLocation().distanceToSqr(vec3);
        }
        Vec3 vec32 = RotationUtil.getDirection(rotation.getYaw(), rotation.getPitch());
        Vec3 vec33 = vec3.add(vec32.x * d, vec32.y * d, vec32.z * d);
        EntityHitResult entityHitResult = ProjectileUtil.getEntityHitResult(mc.player, vec3, vec33, aABB = mc.player.getBoundingBox().expandTowards(vec32.scale(d)).inflate(1.0, 1.0, 1.0), entity -> !entity.isSpectator() && entity.isPickable(), d2);
        if (entityHitResult != null) {
            Vec3 vec34 = entityHitResult.getLocation();
            double d3 = vec3.distanceToSqr(vec34);
            if (bl && d3 > 9.0) {
                hitResult = BlockHitResult.miss(vec34, Direction.getNearest(vec32.x, vec32.y, vec32.z), BlockPos.containing(vec34));
            } else if (d3 < d2 || hitResult == null) {
                hitResult = entityHitResult;
            }
        }
        return hitResult;
    }

    public static Vec3 getDirection(float f, float f2) {
        float f3 = Mth.cos(-f * ((float)Math.PI / 180) - (float)Math.PI);
        float f4 = Mth.sin(-f * ((float)Math.PI / 180) - (float)Math.PI);
        float f5 = -Mth.cos(-f2 * ((float)Math.PI / 180));
        float f6 = Mth.sin(-f2 * ((float)Math.PI / 180));
        return new Vec3(f4 * f5, f6, f3 * f5);
    }

    public static boolean isInsideAABB(AABB aABB, Vec3 vec3) {
        return vec3.x > aABB.minX && vec3.x < aABB.maxX && vec3.y > aABB.minY && vec3.y < aABB.maxY && vec3.z > aABB.minZ && vec3.z < aABB.maxZ;
    }

    public static Rotation exactRotation(Vec3 vec3, Vec3 vec32) {
        double d = vec32.x - vec3.x;
        double d2 = vec32.y - vec3.y;
        double d3 = vec32.z - vec3.z;
        double d4 = Math.sqrt(d * d + d3 * d3);
        float f = (float)Math.toDegrees(Math.atan2(d3, d)) - 90.0f;
        float f2 = (float)(-Math.toDegrees(Math.atan2(d2, d4)));
        return new Rotation(Mth.wrapDegrees(f), Mth.wrapDegrees(f2));
    }

    public static Vec3 closestPoint(Vec3 vec3, AABB aABB) {
        double d = Math.max(aABB.minX, Math.min(vec3.x, aABB.maxX));
        double d2 = Math.max(aABB.minY, Math.min(vec3.y, aABB.maxY));
        double d3 = Math.max(aABB.minZ, Math.min(vec3.z, aABB.maxZ));
        return new Vec3(d, d2, d3);
    }

    public static Rotation rotationFromVec(Vec3 vec3) {
        return RotationUtil.rotationFromCoords(vec3.x, vec3.y, vec3.z);
    }

    public static Rotation rotationFromCoords(double d, double d2, double d3) {
        if (mc.player == null) {
            return new Rotation(0.0f, 0.0f);
        }
        return RotationUtil.rotationFromPoints(d, d2, d3, mc.player.getX(), mc.player.getY() + (double)mc.player.getEyeHeight(mc.player.getPose()), mc.player.getZ());
    }

    private static double normalizeAngle(double d) {
        return ((d + 180.0) % 360.0 + 360.0) % 360.0 - 180.0;
    }

    public static double angleDiffDouble(float f, float f2) {
        double d = f - f2;
        return RotationUtil.normalizeAngle(d);
    }

    public static Rotation rotationFromPoints(double d, double d2, double d3, double d4, double d5, double d6) {
        double d7 = RotationUtil.addNoise(d - d4);
        double d8 = RotationUtil.addNoise(d2 - d5);
        double d9 = RotationUtil.addNoise(d3 - d6);
        double d10 = Mth.sqrt((float)(d7 * d7 + d9 * d9));
        float f = (float)(Math.atan2(d9, d7) * 180.0 / Math.PI) - 90.0f;
        float f2 = (float)(-(Math.atan2(d8, d10) * 180.0 / Math.PI));
        return new Rotation(f, f2);
    }

    public static Rotation entityRotation(Entity entity) {
        if (entity == null) {
            return null;
        }
        double d = entity.getX() - mc.player.getX();
        double d2 = entity.getZ() - mc.player.getZ();
        double d3 = entity.getY() + (double)entity.getEyeHeight() - (mc.player.getY() + (double)mc.player.getEyeHeight());
        return RotationUtil.createRotation(d, d3, d2);
    }

    public static Rotation createRotation(double d, double d2, double d3) {
        double d4 = Math.sqrt(d * d + d3 * d3);
        float f = (float)Math.toDegrees(Math.atan2(d3, d)) - 90.0f;
        float f2 = (float)(-Math.toDegrees(Math.atan2(d2, d4)));
        return new Rotation(Mth.wrapDegrees(f), Mth.wrapDegrees(f2));
    }

    public static boolean isEntityInFov(Entity entity, float f) {
        Rotation rotation = RotationUtil.entityRotation(entity);
        float f2 = Math.abs(mc.player.getYRot() % 360.0f - rotation.getYaw());
        float f3 = Math.abs(Math.min(f2, 360.0f - f2));
        return f3 <= f;
    }

    public static Vec3 directionFromRotation(Rotation rotation) {
        float f = (float)Math.cos(-rotation.getYaw() * ((float)Math.PI / 180) - (float)Math.PI);
        float f2 = (float)Math.sin(-rotation.getYaw() * ((float)Math.PI / 180) - (float)Math.PI);
        float f3 = (float)(-Math.cos(-rotation.getPitch() * ((float)Math.PI / 180)));
        float f4 = (float)Math.sin(-rotation.getPitch() * ((float)Math.PI / 180));
        return new Vec3(f2 * f3, f4, f * f3);
    }

    @Generated
    private RotationUtil() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}