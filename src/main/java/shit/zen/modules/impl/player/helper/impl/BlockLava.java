package shit.zen.modules.impl.player.helper.impl;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Optional;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import shit.zen.event.impl.MotionEvent;
import shit.zen.event.impl.RenderEvent;
import shit.zen.modules.impl.player.Helper;
import shit.zen.modules.impl.player.helper.HelperBase;
import shit.zen.utils.game.ItemUtil;
import shit.zen.utils.game.RotationUtil;
import shit.zen.utils.render.RenderUtil;
import shit.zen.utils.rotation.Rotation;

public class BlockLava
extends HelperBase {

    public enum State { NONE, LAVA, LAVA_SUPPORT }

    public record PlacementData(BlockPos blockPos, Direction direction, Vec3 hitVec) {
    }

    public Rotation targetRotation;
    private BlockPos targetPos;
    public BlockLava.State currentPlacement = BlockLava.State.NONE;
    private BlockLava.PlacementData currentState = null;
    private int blockSlot = -1;
    private int savedSlot = -1;
    private static final String NAME = "Block Lava";

    public BlockLava() {
        super(NAME);
    }

    @Override
    public void onEnable() {
        this.reset();
    }

    @Override
    public void onDisable() {
        this.reset();
    }

    private void reset() {
        this.targetPos = null;
        this.currentPlacement = BlockLava.State.NONE;
        this.targetRotation = null;
        if (this.savedSlot != -1 && mc.player != null) {
            mc.player.getInventory().selected = this.savedSlot;
        }
        this.savedSlot = -1;
        this.blockSlot = -1;
    }

    @Override
    public void onMotion(MotionEvent motionEvent) {
        if (mc.player == null || mc.level == null || mc.gameMode == null) {
            return;
        }
        if (mc.player.isOnFire()) {
            this.reset();
            return;
        }
        if (mc.player.isInLava()) {
            this.reset();
            return;
        }
        if (motionEvent.isPost()) {
            if (this.currentState != null) {
                if (Helper.isRotationNearTarget()) {
                    return;
                }
                this.placeBlock();
                this.currentState = null;
                if (this.currentPlacement != BlockLava.State.LAVA_SUPPORT) {
                    this.reset();
                }
                return;
            }
            switch (this.currentPlacement) {
                case LAVA: {
                    if (this.targetPos == null || !mc.level.getBlockState(this.targetPos).is(Blocks.LAVA)) {
                        this.reset();
                        return;
                    }
                    if (mc.level.getBlockState(this.targetPos.below()).isAir()) {
                        this.currentPlacement = BlockLava.State.LAVA_SUPPORT;
                        break;
                    }
                    if (this.tryFindPlacement()) break;
                    this.currentPlacement = BlockLava.State.LAVA_SUPPORT;
                    break;
                }
                case LAVA_SUPPORT: {
                    if (this.targetPos == null || !mc.level.getBlockState(this.targetPos).is(Blocks.LAVA)) {
                        this.reset();
                        return;
                    }
                    BlockPos blockPos = this.targetPos.below();
                    if (mc.level.getBlockState(blockPos).isSolid()) {
                        this.currentPlacement = BlockLava.State.LAVA;
                        this.tryFindPlacement();
                        return;
                    }
                    this.findSuitableFace(blockPos).ifPresentOrElse(blockLava$PlacementData -> {
                        this.targetRotation = RotationUtil.exactRotation(mc.player.getEyePosition(), blockLava$PlacementData.hitVec());
                        this.currentState = blockLava$PlacementData;
                    }, () -> this.reset());
                    break;
                }
                case NONE: {
                    this.findTargetPos();
                    if (this.targetPos == null) break;
                    this.currentPlacement = BlockLava.State.LAVA;
                }
            }
        }
    }

    private boolean tryFindPlacement() {
        Optional<BlockLava.PlacementData> optional = this.findSuitableFace(this.targetPos);
        if (optional.isPresent()) {
            BlockLava.PlacementData blockLava$PlacementData = optional.get();
            this.targetRotation = RotationUtil.exactRotation(mc.player.getEyePosition(), blockLava$PlacementData.hitVec());
            this.currentState = blockLava$PlacementData;
            return true;
        }
        return false;
    }

    private void placeBlock() {
        if (this.currentState == null) {
            return;
        }
        this.blockSlot = this.findBlockSlot();
        if (this.blockSlot == -1) {
            this.reset();
            return;
        }
        if (this.savedSlot == -1) {
            this.savedSlot = mc.player.getInventory().selected;
        }
        mc.player.getInventory().selected = this.blockSlot;
        BlockHitResult blockHitResult = new BlockHitResult(this.currentState.hitVec(), this.currentState.direction(), this.currentState.blockPos(), false);
        mc.gameMode.useItemOn(mc.player, InteractionHand.MAIN_HAND, blockHitResult);
        mc.player.swing(InteractionHand.MAIN_HAND);
    }

    @Override
    public void onRender(RenderEvent renderEvent) {
        if (this.currentPlacement == BlockLava.State.NONE || mc.gameRenderer == null || this.targetPos == null) {
            return;
        }
        Color color = new Color(255, 165, 0);
        PoseStack poseStack = renderEvent.poseStack();
        Vec3 vec3 = mc.gameRenderer.getMainCamera().getPosition();
        poseStack.pushPose();
        poseStack.translate(-vec3.x, -vec3.y, -vec3.z);
        AABB aABB = new AABB(this.targetPos);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.setShaderColor((float)color.getRed() / 255.0f, (float)color.getGreen() / 255.0f, (float)color.getBlue() / 255.0f, 0.25f);
        RenderUtil.drawSolidBox(aABB, poseStack);
        RenderSystem.setShaderColor((float)color.getRed() / 255.0f, (float)color.getGreen() / 255.0f, (float)color.getBlue() / 255.0f, 0.75f);
        RenderUtil.drawOutlineBox(aABB, poseStack);
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.disableBlend();
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(true);
        poseStack.popPose();
    }

    private void findTargetPos() {
        if (mc.player == null || mc.level == null) {
            this.targetPos = null;
            return;
        }
        BlockPos blockPos2 = mc.player.blockPosition();
        ArrayList<BlockPos> arrayList = new ArrayList<>();
        for (int i = -3; i <= 3; ++i) {
            for (int j = -2; j <= 2; ++j) {
                for (int k = -3; k <= 3; ++k) {
                    BlockPos blockPos3 = blockPos2.offset(i, j, k);
                    if (Helper.hasLavaPlacement(blockPos3) || !mc.level.getBlockState(blockPos3).is(Blocks.LAVA) || !mc.level.getFluidState(blockPos3).isSource() || !Helper.isPositionInFov(Vec3.atCenterOf(blockPos3))) continue;
                    arrayList.add(blockPos3);
                }
            }
        }
        Vec3 vec3 = mc.player.getEyePosition();
        this.targetPos = (BlockPos)arrayList.stream().min(Comparator.comparingDouble(blockPos -> Vec3.atCenterOf((Vec3i)blockPos).distanceToSqr(vec3))).orElse(null);
    }

    private int findBlockSlot() {
        int n = ItemUtil.getSlot(Items.COBBLESTONE);
        if (n != -1 && n < 9) {
            return n;
        }
        for (int i = 0; i < 9; ++i) {
            Block block;
            ItemStack itemStack = mc.player.getInventory().getItem(i);
            if (!(itemStack.getItem() instanceof BlockItem) || !(block = ((BlockItem)itemStack.getItem()).getBlock()).defaultBlockState().isSolid() || block.getSoundType(block.defaultBlockState()).equals(SoundEvents.WOOD_PLACE)) continue;
            return i;
        }
        return -1;
    }

    private Optional<BlockLava.PlacementData> findSuitableFace(BlockPos blockPos) {
        for (Direction direction : Direction.values()) {
            BlockPos blockPos3 = blockPos.relative(direction);
            if (mc.level.getBlockState(blockPos3).isSolid() && !mc.level.getBlockState(blockPos3).is(Blocks.LAVA)) {
                Vec3 hitVec = Vec3.atCenterOf(blockPos3).add(direction.getOpposite().getStepX() * 0.5, direction.getOpposite().getStepY() * 0.5, direction.getOpposite().getStepZ() * 0.5);
                if (mc.player.getEyePosition().distanceToSqr(hitVec) <= 25.0 && this.canSeeBlockFace(blockPos3, direction.getOpposite())) {
                    return Optional.of(new BlockLava.PlacementData(blockPos3, direction.getOpposite(), hitVec));
                }
            }
            if (direction != Direction.DOWN) continue;
            BlockPos blockPos2 = blockPos3;
            for (int i = 0; i < 3 && mc.level.getBlockState(blockPos2).is(Blocks.LAVA); ++i) {
                blockPos2 = blockPos2.below();
            }
            if (!mc.level.getBlockState(blockPos2).isSolid() || mc.level.getBlockState(blockPos2).is(Blocks.LAVA)) continue;
            Vec3 vec3 = Vec3.atCenterOf(blockPos2).add(new Vec3((double)Direction.UP.getStepX() * 0.5, (double)Direction.UP.getStepY() * 0.5, (double)Direction.UP.getStepZ() * 0.5));
            if (!(mc.player.getEyePosition().distanceToSqr(vec3) <= 25.0) || !this.canSeeBlockFace(blockPos2, Direction.UP)) continue;
            return Optional.of(new BlockLava.PlacementData(blockPos2, Direction.UP, vec3));
        }
        return Optional.empty();
    }

    private boolean hasLineOfSight(Vec3 vec3) {
        if (mc.player == null || mc.level == null) {
            return false;
        }
        Vec3 vec32 = mc.player.getEyePosition();
        return mc.level.clip(new ClipContext(vec32, vec3, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, mc.player)).getType() == HitResult.Type.MISS;
    }

    @Override
    public boolean isActive() {
        return this.targetRotation != null;
    }

    @Override
    public Rotation getTargetRotation() {
        return this.targetRotation;
    }

    private boolean canSeeBlockFace(BlockPos blockPos, Direction direction) {
        Vec3 vec3;
        if (mc.player == null || mc.level == null) {
            return false;
        }
        Vec3 vec32 = mc.player.getEyePosition();
        BlockHitResult blockHitResult = mc.level.clip(new ClipContext(vec32, vec3 = Vec3.atCenterOf(blockPos).add(new Vec3((double)direction.getStepX() * 0.49, (double)direction.getStepY() * 0.49, (double)direction.getStepZ() * 0.49)), ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, mc.player));
        if (blockHitResult.getType() != HitResult.Type.BLOCK) {
            return false;
        }
        BlockHitResult blockHitResult2 = blockHitResult;
        if (!blockHitResult2.getBlockPos().equals(blockPos)) {
            return false;
        }
        return blockHitResult2.getLocation().distanceToSqr(vec3) < 0.25;
    }
}