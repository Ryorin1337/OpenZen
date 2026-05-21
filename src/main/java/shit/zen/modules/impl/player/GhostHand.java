package shit.zen.modules.impl.player;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Optional;
import java.util.stream.Collectors;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.level.block.ChestBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import shit.zen.event.impl.PreMotionEvent;
import shit.zen.modules.Category;
import shit.zen.modules.Module;
import shit.zen.utils.game.BlockUtil;
import shit.zen.utils.game.ChunkUtil;
import shit.zen.event.EventTarget;

public class GhostHand
extends Module {
    public GhostHand() {
        super("GhostHand", Category.PLAYER);
    }

    @EventTarget
    public void onPreMotion(PreMotionEvent preMotionEvent) {
        if (mc.options.keyUse.isDown() && this.isChestOpen()) {
            preMotionEvent.setCancelled(true);
        }
    }

    public boolean isChestOpen() {
        if (mc.player == null || mc.level == null) {
            return false;
        }
        Vec3 vec3 = mc.player.getEyePosition(1.0f);
        Vec3 vec32 = mc.player.getViewVector(1.0f);
        Vec3 vec33 = vec3.add(vec32.scale(4.5));
        ChestBlockEntity chestBlockEntity = null;
        BlockHitResult blockHitResult = null;
        double d = Double.MAX_VALUE;
        ArrayList<BlockEntity> arrayList = ChunkUtil.getLoadedBlockEntities().collect(Collectors.toCollection(ArrayList::new));
        for (BlockEntity blockEntity : arrayList) {
            double d2;
            Optional<Vec3> optional;
            ChestBlockEntity chestBlockEntity2;
            AABB aABB;
            if (!(blockEntity instanceof ChestBlockEntity) || (aABB = this.getChestAABB(chestBlockEntity2 = (ChestBlockEntity)blockEntity)) == null || !(optional = aABB.clip(vec3, vec33)).isPresent() || !((d2 = optional.get().distanceTo(vec3)) < d)) continue;
            d = d2;
            chestBlockEntity = chestBlockEntity2;
            blockHitResult = new BlockHitResult(optional.get(), Direction.UP, chestBlockEntity2.getBlockPos(), false);
        }
        if (chestBlockEntity != null && blockHitResult != null) {
            mc.gameMode.useItemOn(mc.player, InteractionHand.MAIN_HAND, blockHitResult);
            mc.player.swing(InteractionHand.MAIN_HAND);
            return true;
        }
        return false;
    }

    private AABB getChestAABB(ChestBlockEntity chestBlockEntity) {
        BlockPos blockPos;
        BlockState blockState = chestBlockEntity.getBlockState();
        if (!blockState.hasProperty((Property)ChestBlock.TYPE)) {
            return null;
        }
        ChestType chestType = (ChestType)blockState.getValue((Property)ChestBlock.TYPE);
        if (chestType == ChestType.LEFT) {
            return null;
        }
        BlockPos blockPos2 = chestBlockEntity.getBlockPos();
        AABB aABB = BlockUtil.getBoundingBox(blockPos2);
        if (chestType != ChestType.SINGLE && BlockUtil.canBeClicked(blockPos = blockPos2.relative(ChestBlock.getConnectedDirection(blockState)))) {
            AABB aABB2 = BlockUtil.getBoundingBox(blockPos);
            aABB = aABB.minmax(aABB2);
        }
        return aABB;
    }

    static {
        new HashSet<>();
    }
}