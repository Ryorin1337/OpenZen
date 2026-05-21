package shit.zen.modules.impl.player;

import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import shit.zen.event.impl.TickEvent;
import shit.zen.modules.Category;
import shit.zen.modules.Module;
import shit.zen.settings.impl.BooleanSetting;
import shit.zen.settings.impl.NumberSetting;
import shit.zen.utils.game.ItemUtil;
import shit.zen.utils.game.RotationUtil;
import shit.zen.utils.rotation.Rotation;
import shit.zen.event.EventTarget;

public class AutoMLG
extends Module {
    public static AutoMLG INSTANCE;
    private final NumberSetting triggerDistanceSetting = new NumberSetting("Fall distance", Float.valueOf(3.0f), Float.valueOf(1.0f), Float.valueOf(10.0f), Float.valueOf(0.1f));
    private final NumberSetting predictTicksSetting = new NumberSetting("Predict Ticks", Float.valueOf(2.0f), Float.valueOf(1.0f), Float.valueOf(5.0f), Float.valueOf(1.0f));
    private final BooleanSetting solidCheckSetting = new BooleanSetting("Solid check", true);
    private final BooleanSetting recoverySetting = new BooleanSetting("Recorvey", true);
    public Rotation targetRotation = null;
    private float accumulatedFall;
    private double lastY;
    private Integer slotToRestore;
    private boolean waterPlaced;
    private boolean recoveryActive;
    private int recoveryDelay;
    private int recoveryCountdown;
    private Integer waterBucketSlot;
    private BlockPos placedWaterPos;
    private boolean readyToPlace;
    private int postPlaceCooldown;
    private int postActionCooldown;
    private int extraCooldown;

    public AutoMLG() {
        super("AutoMLG", Category.PLAYER);
        INSTANCE = this;
    }

    @Override
    protected void onEnable() {
        this.slotToRestore = null;
        this.waterPlaced = false;
        this.recoveryActive = false;
        this.recoveryDelay = 0;
        this.recoveryCountdown = 0;
        this.waterBucketSlot = null;
        this.placedWaterPos = null;
        this.readyToPlace = false;
        this.postPlaceCooldown = 0;
        this.postActionCooldown = 0;
        this.extraCooldown = 0;
        this.accumulatedFall = 0.0f;
        this.lastY = mc.player != null ? mc.player.getY() : 0.0;
        super.onEnable();
    }

    @Override
    protected void onDisable() {
        this.slotToRestore = null;
        this.waterPlaced = false;
        this.recoveryActive = false;
        this.recoveryDelay = 0;
        this.recoveryCountdown = 0;
        this.waterBucketSlot = null;
        this.placedWaterPos = null;
        this.readyToPlace = false;
        this.postPlaceCooldown = 0;
        this.postActionCooldown = 0;
        this.extraCooldown = 0;
        this.accumulatedFall = 0.0f;
        super.onDisable();
    }

    public boolean isInCooldown() {
        return this.postPlaceCooldown > 0;
    }

    @EventTarget
    public void onTick(TickEvent tickEvent) {
        Rotation rotation;
        BlockHitResult blockHitResult;
        BlockPos blockPos;
        int n;
        double d;
        if (mc.player == null || mc.level == null) {
            return;
        }
        if (mc.player.isFallFlying()) {
            return;
        }
        if (mc.player.onGround() || mc.player.getAbilities().flying || mc.player.isInWaterRainOrBubble() || mc.player.isInLava()) {
            this.accumulatedFall = 0.0f;
        } else {
            d = mc.player.getY() - this.lastY;
            if (d < 0.0) {
                this.accumulatedFall -= (float)d;
            }
        }
        this.lastY = mc.player.getY();
        if (this.postPlaceCooldown > 0) {
            --this.postPlaceCooldown;
        }
        if (this.postActionCooldown > 0) {
            --this.postActionCooldown;
        }
        if (this.extraCooldown > 0) {
            --this.extraCooldown;
        }
        if (this.slotToRestore != null) {
            mc.player.getInventory().selected = this.slotToRestore;
            this.slotToRestore = null;
        }
        if (mc.player.onGround() || this.accumulatedFall <= 0.0f) {
            this.waterPlaced = false;
            this.readyToPlace = false;
        }
        if (this.recoveryActive) {
            if (this.recoveryDelay > 0) {
                --this.recoveryDelay;
                return;
            }
            if (this.recoveryCountdown-- <= 0) {
                this.recoveryActive = false;
                return;
            }
            if (this.waterBucketSlot == null) {
                this.waterBucketSlot = ItemUtil.findItemInRange(0, 9, Items.BUCKET);
                if (this.waterBucketSlot == null) {
                    this.recoveryActive = false;
                    return;
                }
            }
            if (mc.player.getInventory().items.get(this.waterBucketSlot.intValue()).getItem() == Items.WATER_BUCKET) {
                this.recoveryActive = false;
                this.waterBucketSlot = null;
                this.placedWaterPos = null;
                this.postPlaceCooldown = Math.max(this.postPlaceCooldown, 1);
                return;
            }
            if (this.placedWaterPos == null || !this.isWaterSource(this.placedWaterPos)) {
                this.recoveryActive = false;
                this.waterBucketSlot = null;
                this.placedWaterPos = null;
                return;
            }
            Rotation rotation2 = RotationUtil.rotationToBlock(this.placedWaterPos, 0.0f);
            BlockHitResult blockHitResult2 = this.raycastFluid(rotation2, 4.5);
            if (blockHitResult2.getType() == HitResult.Type.MISS || !blockHitResult2.getBlockPos().equals(this.placedWaterPos)) {
                this.recoveryActive = false;
                this.waterBucketSlot = null;
                this.placedWaterPos = null;
                return;
            }
            this.setTargetRotation(rotation2);
            this.selectSlot(this.waterBucketSlot);
            this.useItem(rotation2);
            return;
        }
        if (!this.waterPlaced && !this.recoveryActive && this.placedWaterPos == null && this.postPlaceCooldown == 0 && this.postActionCooldown == 0 && this.accumulatedFall <= 0.5f && ItemUtil.findItemInRange(0, 9, Items.WATER_BUCKET) < 0 && (n = ItemUtil.findItemInRange(0, 9, Items.BUCKET)) >= 0 && (blockPos = this.findBucketPos()) != null && (blockHitResult = this.raycastFluid(rotation = RotationUtil.rotationToBlock(blockPos, 0.0f), 4.5)).getType() != HitResult.Type.MISS && blockHitResult.getBlockPos().equals(blockPos)) {
            this.setTargetRotation(rotation);
            this.selectSlot(n);
            this.useItem(rotation);
            this.postActionCooldown = 8;
            this.postPlaceCooldown = Math.max(this.postPlaceCooldown, 1);
            return;
        }
        if (this.waterPlaced && !this.readyToPlace && mc.player.getDeltaMovement().y < 0.0 && (d = this.distanceToGround(2.5)) > 0.0 && d <= 1.05) {
            this.readyToPlace = true;
        }
        if (this.waterPlaced) {
            return;
        }
        if (this.accumulatedFall < this.triggerDistanceSetting.getValue().floatValue()) {
            return;
        }
        n = ItemUtil.findItemInRange(0, 9, Items.WATER_BUCKET);
        if (n < 0) {
            return;
        }
        int n2 = this.ticksUntilGround();
        if (n2 <= this.predictTicksSetting.getValue().intValue()) {
            if (this.solidCheckSetting.getValue() && !this.hasSolidBelow(BlockPos.containing(mc.player.getX(), mc.player.getY(), mc.player.getZ()))) {
                return;
            }
            rotation = new Rotation(mc.player.getYRot(), 90.0f);
            blockHitResult = this.raycastSolid(rotation, 5.0);
            if (blockHitResult.getType() == HitResult.Type.MISS) {
                return;
            }
            this.placeWaterBucket(n, true);
        }
    }

    private int ticksUntilGround() {
        if (mc.player.getDeltaMovement().y >= 0.0) {
            return 999;
        }
        double d = this.distanceToGround(30.0);
        if (d == Double.POSITIVE_INFINITY) {
            return 999;
        }
        double d2 = 0.0;
        double d3 = mc.player.getDeltaMovement().y;
        for (int i = 1; i <= 20; ++i) {
            d2 += d3;
            d3 = (d3 - 0.08) * 0.98;
            if (!(Math.abs(d2) >= d)) continue;
            return i;
        }
        return 999;
    }

    private void useItem(Rotation rotation) {
        if (mc.gameMode != null && mc.player != null) {
            float f = mc.player.getXRot();
            float f2 = mc.player.getYRot();
            if (rotation != null) {
                mc.player.setXRot(rotation.getPitch());
                mc.player.setYRot(rotation.getYaw());
            }
            mc.gameMode.useItem(mc.player, InteractionHand.MAIN_HAND);
            mc.player.swing(InteractionHand.MAIN_HAND);
            if (rotation != null) {
                mc.player.setXRot(f);
                mc.player.setYRot(f2);
            }
        }
    }

    private BlockPos findBucketPos() {
        BlockPos blockPos = BlockPos.containing(mc.player.getX(), mc.player.getY(), mc.player.getZ());
        BlockPos blockPos2 = null;
        double d = Double.POSITIVE_INFINITY;
        int n = 4;
        for (int i = -1; i <= 1; ++i) {
            for (int j = -n; j <= n; ++j) {
                for (int k = -n; k <= n; ++k) {
                    Rotation rotation;
                    BlockHitResult blockHitResult;
                    double d2;
                    BlockPos blockPos3 = blockPos.offset(j, i, k);
                    if (!this.isWaterSource(blockPos3) || (d2 = mc.player.position().distanceToSqr((double)blockPos3.getX() + 0.5, (double)blockPos3.getY() + 0.5, (double)blockPos3.getZ() + 0.5)) >= d || (blockHitResult = this.raycastFluid(rotation = RotationUtil.rotationToBlock(blockPos3, 0.0f), 4.5)).getType() == HitResult.Type.MISS || !blockHitResult.getBlockPos().equals(blockPos3)) continue;
                    blockPos2 = blockPos3;
                    d = d2;
                }
            }
        }
        return blockPos2;
    }

    private void setTargetRotation(Rotation rotation) {
        this.targetRotation = rotation;
    }

    private void selectSlot(int n) {
        this.slotToRestore = mc.player.getInventory().selected;
        mc.player.getInventory().selected = n;
    }

    private void placeWaterBucket(int n, boolean bl) {
        Rotation rotation = new Rotation(mc.player.getYRot(), 90.0f);
        this.setTargetRotation(rotation);
        this.selectSlot(n);
        this.useItem(rotation);
        if (bl) {
            this.waterPlaced = true;
        }
        this.recoveryActive = this.recoverySetting.getValue();
        this.recoveryDelay = 3;
        this.recoveryCountdown = this.recoveryActive ? 2 : 0;
        this.waterBucketSlot = null;
        this.placedWaterPos = this.getPlacementBlockPos(rotation);
    }

    private BlockPos getPlacementBlockPos(Rotation rotation) {
        BlockHitResult blockHitResult = this.raycastSolid(rotation, 4.5);
        if (blockHitResult.getType() == HitResult.Type.MISS) {
            return null;
        }
        return blockHitResult.getBlockPos().relative(blockHitResult.getDirection());
    }

    private BlockHitResult raycastSolid(Rotation rotation, double d) {
        Vec3 vec3 = mc.player.getEyePosition(1.0f);
        Vec3 vec32 = Vec3.directionFromRotation(rotation.getPitch(), rotation.getYaw());
        Vec3 vec33 = vec3.add(vec32.scale(d));
        return mc.level.clip(new ClipContext(vec3, vec33, ClipContext.Block.OUTLINE, ClipContext.Fluid.NONE, mc.player));
    }

    private BlockHitResult raycastFluid(Rotation rotation, double d) {
        Vec3 vec3 = mc.player.getEyePosition(1.0f);
        Vec3 vec32 = Vec3.directionFromRotation(rotation.getPitch(), rotation.getYaw());
        Vec3 vec33 = vec3.add(vec32.scale(d));
        return mc.level.clip(new ClipContext(vec3, vec33, ClipContext.Block.OUTLINE, ClipContext.Fluid.SOURCE_ONLY, mc.player));
    }

    private boolean isWaterSource(BlockPos blockPos) {
        FluidState fluidState = mc.level.getFluidState(blockPos);
        return fluidState.getType() == Fluids.WATER && fluidState.isSource();
    }

    private boolean hasSolidBelow(BlockPos blockPos) {
        return this.isSolidNonMenu(blockPos.below()) || this.isSolidNonMenu(blockPos.below(2));
    }

    private boolean isSolidNonMenu(BlockPos blockPos) {
        BlockState blockState = mc.level.getBlockState(blockPos);
        boolean bl = !blockState.getCollisionShape(mc.level, blockPos).isEmpty();
        boolean bl2 = blockState.getMenuProvider(mc.level, blockPos) == null;
        return bl && bl2;
    }

    private double distanceToGround(double d) {
        Vec3 vec3;
        Vec3 vec32 = new Vec3(mc.player.getX(), mc.player.getBoundingBox().minY, mc.player.getZ());
        BlockHitResult blockHitResult = mc.level.clip(new ClipContext(vec32, vec3 = vec32.add(0.0, -d, 0.0), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, mc.player));
        if (blockHitResult.getType() == HitResult.Type.MISS) {
            return Double.POSITIVE_INFINITY;
        }
        return vec32.y - blockHitResult.getLocation().y;
    }
}