package shit.zen.utils.game;

import com.mojang.blaze3d.platform.InputConstants;
import java.lang.reflect.Method;
import javax.annotation.Nullable;
import lombok.Generated;
import net.minecraft.client.KeyMapping;
import net.minecraft.core.BlockPos;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import shit.zen.ClientBase;
import shit.zen.utils.game.RotationUtil;
import shit.zen.utils.misc.ChatUtil;
import shit.zen.utils.misc.ReflectionUtil;
import shit.zen.utils.rotation.Rotation;

public final class PlayerUtil
extends ClientBase {
    public static void updateWalkAnim() {
        if (mc.player == null) {
            return;
        }
        mc.player.walkAnimation.setSpeed(mc.player.walkAnimation.speed());
        double d = mc.player.getX() - mc.player.xo;
        double d2 = mc.player.getZ() - mc.player.zo;
        float f = Mth.sqrt((float)(d * d + d2 * d2)) * 4.0f;
        f = Mth.clamp(f, 0.0f, 1.0f);
        mc.player.walkAnimation.update(f, 0.4f);
    }

    public static void sendCarriedItem() {
        try {
            Method method = mc.gameMode.getClass().getDeclaredMethod(ReflectionUtil.getMappedMethodName(mc.gameMode.getClass(), "ensureHasSentCarriedItem", "()V"));
            method.setAccessible(true);
            method.invoke(mc.gameMode);
        } catch (Exception exception) {
            exception.printStackTrace();
            ChatUtil.print("Failed to set item!");
        }
    }

    public static boolean isSafe(double d) {
        int n = 0;
        while ((double)n < d) {
            AABB aABB = mc.player.getBoundingBox().move(0.0, -n, 0.0);
            if (!PlayerUtil.isNoClip(mc.player, aABB)) {
                return true;
            }
            n += 2;
        }
        return false;
    }

    public static boolean isNoClip(@Nullable Entity entity, AABB aABB) {
        for (VoxelShape voxelShape : mc.level.getBlockCollisions(entity, aABB)) {
            if (voxelShape.isEmpty()) continue;
            return false;
        }
        return true;
    }

    public static void click(int n, boolean bl) {
        InputConstants.Key key = n == 0 ? mc.options.keyAttack.getKey() : mc.options.keyUse.getKey();
        KeyMapping.set(key, bl);
        if (bl) {
            KeyMapping.click(key);
        }
    }

    public static int getArmorPoints(LivingEntity livingEntity) {
        int n = 0;
        for (ItemStack itemStack : livingEntity.getArmorSlots()) {
            Item item = itemStack.getItem();
            if (!(item instanceof ArmorItem armorItem)) continue;
            n += armorItem.getDefense();
        }
        return n;
    }

    public static Block getBlock(double d, double d2, double d3) {
        return PlayerUtil.getBlock(BlockPos.containing(d, d2, d3));
    }

    public static Block getBlock(BlockPos blockPos) {
        if (mc.level == null || mc.player == null) {
            return null;
        }
        return mc.level.getBlockState(blockPos).getBlock();
    }

    public static Block getBlockBelow(Player player) {
        return PlayerUtil.getBlock(BlockPos.containing(player.getX(), player.getY() - 1.0, player.getZ()));
    }

    public static HitResult rayTrace(double d, float f, float f2) {
        if (mc.player == null || mc.level == null) {
            return null;
        }
        Vec3 vec3 = mc.player.getEyePosition(1.0f);
        Vec3 vec32 = RotationUtil.directionFromRotation(new Rotation(f, f2));
        Vec3 vec33 = vec3.add(vec32.x * d, vec32.y * d, vec32.z * d);
        return mc.level.clip(new ClipContext(vec3, vec33, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, mc.player));
    }

    public static boolean isSafeToScaffold() {
        if (mc.player == null || mc.level == null) {
            return true;
        }
        Vec3 vec3 = mc.player.position();
        BlockPos blockPos = new BlockPos((int)Math.floor(vec3.x + Math.sin(Math.toRadians(mc.player.getYRot())) * 0.8), (int)Math.floor(vec3.y), (int)Math.floor(vec3.z + -Math.cos(Math.toRadians(mc.player.getYRot())) * 0.8));
        if (mc.level.getBlockState(blockPos).is(Blocks.LAVA)) {
            return false;
        }
        BlockPos blockPos2 = blockPos.below();
        BlockState blockState = mc.level.getBlockState(blockPos2);
        if (blockState.is(Blocks.LAVA)) {
            return false;
        }
        if (blockState.isAir()) {
            if (blockPos2.getY() < mc.level.getMinBuildHeight()) {
                return false;
            }
            if (mc.level.getBlockState(blockPos2.below()).isAir()) {
                return blockPos2.below().getY() >= mc.level.getMinBuildHeight() && !mc.level.getBlockState(blockPos2.below().below()).isAir();
            }
        }
        return true;
    }

    @Generated
    private PlayerUtil() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}