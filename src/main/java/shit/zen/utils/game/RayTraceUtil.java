package shit.zen.utils.game;

import java.util.List;
import java.util.Optional;
import lombok.Generated;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import shit.zen.ClientBase;
import shit.zen.utils.game.RotationUtil;
import shit.zen.utils.rotation.Rotation;

public final class RayTraceUtil
extends ClientBase {
    private static final String UTILITY_MSG = "This is a utility class and cannot be instantiated";

    public static boolean canRayTrace(Rotation rotation, Direction direction, BlockPos blockPos, boolean bl) {
        Vec3 vec3;
        Vec3 vec32;
        if (mc.player == null || mc.level == null) {
            return false;
        }
        float f = rotation.getYaw();
        float f2 = rotation.getPitch();
        Vec3 vec33 = mc.player.getEyePosition(1.0f);
        BlockHitResult blockHitResult = mc.level.clip(new ClipContext(vec33, vec32 = vec33.add((vec3 = Vec3.directionFromRotation(f2, f)).scale(5.0)), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, mc.player));
        if (blockHitResult.getType() != HitResult.Type.BLOCK) {
            return false;
        }
        boolean bl2 = blockHitResult.getBlockPos().equals(blockPos);
        boolean bl3 = !bl || blockHitResult.getDirection() == direction;
        return bl2 && bl3;
    }

    public static HitResult rayTrace(float f, Rotation rotation) {
        HitResult hitResult = null;
        Entity entity = mc.getCameraEntity();
        if (entity != null && mc.level != null) {
            double d = mc.gameMode.getPickRange();
            hitResult = RayTraceUtil.rayTrace(d, f, true, rotation.getYaw(), rotation.getPitch());
        }
        return hitResult;
    }

    public static HitResult rayTrace(double d, float f, boolean bl, Rotation rotation) {
        HitResult hitResult = null;
        Entity entity = mc.getCameraEntity();
        if (entity != null && mc.level != null) {
            hitResult = RayTraceUtil.rayTrace(d, f, bl, rotation.getYaw(), rotation.getPitch());
        }
        return hitResult;
    }

    public static Vec3 getViewVector(float f, float f2) {
        float f3 = f * ((float)Math.PI / 180);
        float f4 = -f2 * ((float)Math.PI / 180);
        float f5 = Mth.cos(f4);
        float f6 = Mth.sin(f4);
        float f7 = Mth.cos(f3);
        float f8 = Mth.sin(f3);
        return new Vec3(f6 * f7, -f8, f5 * f7);
    }

    public static HitResult rayTrace(double d, float f, boolean bl, float f2, float f3) {
        Vec3 vec3 = new Vec3(mc.player.getX(), mc.player.getY() + 1.62, mc.player.getZ());
        Vec3 vec32 = RayTraceUtil.getViewVector(f3, f2);
        Vec3 vec33 = vec3.add(vec32.x * d, vec32.y * d, vec32.z * d);
        return mc.player.level().clip(new ClipContext(vec3, vec33, ClipContext.Block.OUTLINE, bl ? ClipContext.Fluid.ANY : ClipContext.Fluid.NONE, mc.player));
    }

    public static HitResult clipWithEntity(Vec3 vec3, Vec3 vec32, boolean bl, boolean bl2, boolean bl3, Entity entity) {
        ClipContext.Block block = bl2 ? ClipContext.Block.COLLIDER : (bl3 ? ClipContext.Block.VISUAL : ClipContext.Block.OUTLINE);
        ClipContext.Fluid fluid = bl ? ClipContext.Fluid.ANY : ClipContext.Fluid.NONE;
        ClipContext clipContext = new ClipContext(vec3, vec32, block, fluid, entity);
        return mc.level.clip(clipContext);
    }

    public static EntityHitResult getEntityHit(AABB aABB, Vec3 vec32, Vec3 vec33) {
        Optional<Vec3> optional = aABB.clip(vec32, vec33);
        return optional.map(vec3 -> new EntityHitResult(null, vec3)).orElse(null);
    }

    public static HitResult rayTraceForEntity(Rotation rotation, double d, float f, Entity entity, Entity entity2, boolean bl) {
        if (entity == null || mc.level == null) {
            return null;
        }
        float f2 = mc.getFrameTime();
        Vec3 vec3 = entity.getEyePosition(f2);
        Vec3 vec32 = RotationUtil.directionFromRotation(rotation);
        Vec3 vec33 = vec3.add(vec32.x * d, vec32.y * d, vec32.z * d);
        BlockHitResult blockHitResult = null;
        double d2 = d;
        if (!bl) {
            blockHitResult = mc.level.clip(new ClipContext(vec3, vec33, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity));
            d2 = blockHitResult.getType() == HitResult.Type.BLOCK ? vec3.distanceTo(blockHitResult.getLocation()) : d;
        }
        double d3 = Math.min(d, d2) + (double)f;
        AABB aABB = new AABB(vec3.x - d3, vec3.y - d3, vec3.z - d3, vec3.x + d3, vec3.y + d3, vec3.z + d3);
        List<Entity> list = mc.level.getEntitiesOfClass(Entity.class, aABB, entity3 -> entity3 != entity && (entity2 == null || entity3 == entity2) && EntitySelector.NO_SPECTATORS.test(entity3) && entity3.isPickable());
        Entity entity4 = null;
        Vec3 vec34 = null;
        double d4 = Math.min(d, d2);
        d4 *= d4;
        for (Entity entity5 : list) {
            Vec3 vec35;
            BlockHitResult blockHitResult2;
            Vec3 vec36;
            double d5;
            AABB aABB2 = entity5.getBoundingBox().inflate(f);
            Optional<Vec3> optional = aABB2.clip(vec3, vec33);
            if (!optional.isPresent() || !((d5 = vec3.distanceToSqr(vec36 = optional.get())) < d4)) continue;
            boolean bl2 = bl || (blockHitResult2 = mc.level.clip(new ClipContext(vec3, vec35 = aABB2.getCenter(), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity))).getType() != HitResult.Type.BLOCK || !(vec3.distanceToSqr(blockHitResult2.getLocation()) <= d5);
            if (!bl2) continue;
            d4 = d5;
            entity4 = entity5;
            vec34 = vec36;
        }
        if (entity4 != null) {
            return new EntityHitResult(entity4, vec34);
        }
        if (!bl && blockHitResult != null) {
            return blockHitResult;
        }
        return mc.level.clip(new ClipContext(vec3, vec33, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, entity));
    }

    @Generated
    private RayTraceUtil() {
        throw new UnsupportedOperationException(UTILITY_MSG);
    }
}