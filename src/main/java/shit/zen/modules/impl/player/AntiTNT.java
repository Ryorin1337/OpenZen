package shit.zen.modules.impl.player;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import shit.zen.event.impl.RenderEvent;
import shit.zen.event.impl.TickEvent;
import shit.zen.modules.Category;
import shit.zen.modules.Module;
import shit.zen.utils.animation.TickTimer;
import shit.zen.utils.game.BlockUtil;
import shit.zen.utils.game.RotationUtil;
import shit.zen.utils.math.MathUtil;
import shit.zen.utils.render.RenderUtil;
import shit.zen.utils.rotation.Rotation;
import shit.zen.event.EventTarget;

public class AntiTNT
extends Module {
    public static AntiTNT INSTANCE;
    private final TickTimer placementTimer = new TickTimer();
    private final List<BlockPos> blockPositionQueue = new ArrayList<>();
    private PrimedTnt targetTnt = null;
    private int savedHotbarSlot = -1;
    private BlockPos lastPlacedPos = null;
    public static Rotation targetRotation;

    public AntiTNT() {
        super("AntiTNT", Category.PLAYER);
        INSTANCE = this;
    }

    @Override
    public void onEnable() {
        this.blockPositionQueue.clear();
        this.targetTnt = null;
        this.savedHotbarSlot = -1;
        this.lastPlacedPos = null;
        this.placementTimer.reset();
        targetRotation = null;
    }

    @Override
    public void onDisable() {
        this.blockPositionQueue.clear();
        this.targetTnt = null;
        this.restoreSlot();
        targetRotation = null;
    }

    @EventTarget
    public void onTick(TickEvent tickEvent) {
        if (mc.player == null || mc.level == null || mc.gameMode == null) {
            return;
        }
        if (this.isMoving()) {
            if (!this.blockPositionQueue.isEmpty()) {
                this.blockPositionQueue.clear();
                this.restoreSlot();
            }
            targetRotation = null;
            this.targetTnt = null;
            return;
        }
        if (this.blockPositionQueue.isEmpty()) {
            this.targetTnt = this.findNearestTNT();
        }
        if (!this.blockPositionQueue.isEmpty()) {
            this.placeNextBlock();
            return;
        }
        if (this.targetTnt != null) {
            this.collectBlockPositions();
        } else {
            targetRotation = null;
        }
    }

    @EventTarget
    public void onRender(RenderEvent renderEvent) {
        PoseStack poseStack = renderEvent.poseStack();
        Vec3 vec3 = mc.gameRenderer.getMainCamera().getPosition();
        poseStack.pushPose();
        poseStack.translate(-vec3.x, -vec3.y, -vec3.z);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.depthMask(false);
        RenderSystem.setShader(GameRenderer::getPositionShader);
        if (this.lastPlacedPos != null) {
            RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 0.8f);
            RenderUtil.drawOutlineBox(new AABB(this.lastPlacedPos), poseStack);
        }
        if (this.targetTnt != null && this.targetTnt.isAlive()) {
            AABB aABB = this.targetTnt.getBoundingBox();
            RenderSystem.setShaderColor(1.0f, 0.0f, 0.0f, 0.25f);
            RenderUtil.drawSolidBox(aABB, poseStack);
            RenderSystem.setShaderColor(1.0f, 0.0f, 0.0f, 0.8f);
            RenderUtil.drawOutlineBox(aABB, poseStack);
            float f = (float)this.targetTnt.getFuse() / 20.0f;
            if (f > 0.0f) {
                String string = String.format("%.1fs", new Object[]{Float.valueOf(f)});
                BlockPos blockPos = this.targetTnt.blockPosition();
                poseStack.pushPose();
                poseStack.translate((double)blockPos.getX() + 0.5 - vec3.x, (double)blockPos.getY() + 1.1 - vec3.y, (double)blockPos.getZ() + 0.5 - vec3.z);
                poseStack.mulPose(mc.gameRenderer.getMainCamera().rotation());
                poseStack.scale(-0.025f, -0.025f, 0.025f);
                float f2 = mc.font.width(string);
                mc.font.drawInBatch(string, -f2 / 2.0f, 0.0f, -1, false, poseStack.last().pose(), mc.renderBuffers().bufferSource(), Font.DisplayMode.NORMAL, 0, 0xF000F0);
                poseStack.popPose();
            }
        }
        RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        RenderSystem.disableBlend();
        RenderSystem.enableDepthTest();
        RenderSystem.depthMask(true);
        poseStack.popPose();
    }

    private boolean isMoving() {
        if (mc.options == null) {
            return false;
        }
        return mc.options.keyUp.isDown() || mc.options.keyDown.isDown() || mc.options.keyLeft.isDown() || mc.options.keyRight.isDown() || mc.player.isSprinting();
    }

    private PrimedTnt findNearestTNT() {
        return mc.level.getEntitiesOfClass(PrimedTnt.class, mc.player.getBoundingBox().inflate(20.0)).stream().filter(primedTnt -> primedTnt.getFuse() > 0).filter(primedTnt -> this.isMovingTowardsPlayer(primedTnt) || this.hasLineOfSight(primedTnt)).min(Comparator.comparingDouble(primedTnt -> primedTnt.distanceToSqr(mc.player))).orElse(null);
    }

    private boolean hasLineOfSight(PrimedTnt primedTnt) {
        Vec3 vec3;
        if (mc.player == null || mc.level == null) {
            return false;
        }
        float f = 8.0f;
        if (primedTnt.distanceToSqr(mc.player) > 64.0) {
            return false;
        }
        Vec3 vec32 = primedTnt.position();
        BlockHitResult blockHitResult = mc.level.clip(new ClipContext(vec32, vec3 = mc.player.getEyePosition(), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, mc.player));
        return blockHitResult.getType() == HitResult.Type.MISS;
    }

    private boolean isMovingTowardsPlayer(PrimedTnt primedTnt) {
        Vec3 vec3 = mc.player.position().subtract(primedTnt.position()).normalize();
        return primedTnt.getDeltaMovement().dot(vec3) > 0.05;
    }

    private void collectBlockPositions() {
        BlockPos blockPos;
        if (!this.blockPositionQueue.isEmpty()) {
            return;
        }
        if (mc.screen != null) {
            mc.player.closeContainer();
            mc.setScreen(null);
        }
        BlockPos blockPos2 = mc.player.blockPosition();
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            blockPos = blockPos2.relative(direction);
            if (!this.canPlaceAt(blockPos)) continue;
            this.blockPositionQueue.add(blockPos);
        }
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            blockPos = blockPos2.above().relative(direction);
            if (!this.canPlaceAt(blockPos)) continue;
            this.blockPositionQueue.add(blockPos);
        }
        BlockPos abovePos = blockPos2.above(2);
        if (this.canPlaceAt(abovePos)) {
            this.blockPositionQueue.add(abovePos);
        }
        this.placementTimer.reset();
    }

    private void placeNextBlock() {
        if (!this.placementTimer.hasPassed(1)) {
            return;
        }
        if (this.blockPositionQueue.isEmpty()) {
            return;
        }
        BlockPos blockPos = this.blockPositionQueue.get(0);
        BlockHitResult blockHitResult = this.getPlacementHitResult(blockPos);
        if (blockHitResult == null) {
            this.blockPositionQueue.remove(0);
            return;
        }
        int n = this.findBlockSlot();
        if (n == -1) {
            this.blockPositionQueue.clear();
            this.restoreSlot();
            return;
        }
        if (this.savedHotbarSlot == -1) {
            this.savedHotbarSlot = mc.player.getInventory().selected;
        }
        mc.player.getInventory().selected = n;
        targetRotation = RotationUtil.rotationToBlock(blockHitResult.getBlockPos(), 0.0f);
        InteractionResult interactionResult = mc.gameMode.useItemOn(mc.player, InteractionHand.MAIN_HAND, blockHitResult);
        if (interactionResult.consumesAction()) {
            mc.player.swing(InteractionHand.MAIN_HAND);
        }
        this.lastPlacedPos = blockPos;
        this.blockPositionQueue.remove(0);
        this.placementTimer.reset();
        if (this.blockPositionQueue.isEmpty()) {
            this.restoreSlot();
            targetRotation = null;
        }
    }

    private void restoreSlot() {
        if (this.savedHotbarSlot != -1) {
            mc.player.getInventory().selected = this.savedHotbarSlot;
            this.savedHotbarSlot = -1;
        }
    }

    private boolean canPlaceAt(BlockPos blockPos) {
        return mc.level.getBlockState(blockPos).canBeReplaced() && !mc.player.getBoundingBox().intersects(new AABB(blockPos));
    }

    private int findBlockSlot() {
        for (int i = 0; i < 9; ++i) {
            ItemStack itemStack = mc.player.getInventory().getItem(i);
            if (itemStack.isEmpty() || !(itemStack.getItem() instanceof BlockItem) || !BlockUtil.isPlaceable(itemStack)) continue;
            return i;
        }
        return -1;
    }

    private boolean isSolidBlock(BlockPos blockPos) {
        return mc.level.getBlockState(blockPos).isSolidRender(mc.level, blockPos);
    }

    private BlockHitResult getPlacementHitResult(BlockPos blockPos) {
        BlockPos blockPos2 = blockPos.below();
        if (this.isSolidBlock(blockPos2)) {
            return new BlockHitResult(this.getHitVec(blockPos2, Direction.UP), Direction.UP, blockPos2, false);
        }
        for (Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos blockPos3 = blockPos.relative(direction);
            if (!this.isSolidBlock(blockPos3)) continue;
            Direction direction2 = direction.getOpposite();
            return new BlockHitResult(this.getHitVec(blockPos3, direction2), direction2, blockPos3, false);
        }
        return null;
    }

    private Vec3 getHitVec(BlockPos blockPos, Direction direction) {
        double d = (double)blockPos.getX() + 0.5;
        double d2 = (double)blockPos.getY() + 0.5;
        double d3 = (double)blockPos.getZ() + 0.5;
        if (direction == Direction.UP || direction == Direction.DOWN) {
            d += MathUtil.randomDouble(-0.3, 0.3);
            d3 += MathUtil.randomDouble(-0.3, 0.3);
        } else {
            d2 += MathUtil.randomDouble(-0.25, 0.25);
        }
        if (direction == Direction.WEST || direction == Direction.EAST) {
            d3 += MathUtil.randomDouble(-0.3, 0.3);
        }
        if (direction == Direction.SOUTH || direction == Direction.NORTH) {
            d += MathUtil.randomDouble(-0.3, 0.3);
        }
        double d4 = Math.max(blockPos.getX(), Math.min(blockPos.getX() + 1, d));
        double d5 = Math.max(blockPos.getY(), Math.min(blockPos.getY() + 1, d2));
        double d6 = Math.max(blockPos.getZ(), Math.min(blockPos.getZ() + 1, d3));
        return new Vec3(d4, d5, d6);
    }

    static {
        targetRotation = null;
    }
}