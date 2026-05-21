package shit.zen.utils.game;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import shit.zen.ClientBase;
import shit.zen.utils.game.RayTraceUtil;

public class MotionSimulator {
    public double x;
    public double y;
    public double z;
    private double motionX;
    private double motionY;
    private double motionZ;
    private final float yaw;
    private final float strafeSpeed;
    private final float forwardSpeed;
    private float jumpPower;

    public MotionSimulator(double d, double d2, double d3, double d4, double d5, double d6, float f, float f2, float f3) {
        this.x = d;
        this.y = d2;
        this.z = d3;
        this.motionX = d4;
        this.motionY = d5;
        this.motionZ = d6;
        this.yaw = f;
        this.strafeSpeed = f2;
        this.forwardSpeed = f3;
    }

    public MotionSimulator(Player player) {
        this(player.getX(), player.getY(), player.getZ(), player.getDeltaMovement().x, player.getDeltaMovement().y, player.getDeltaMovement().z, player.getYRot(), player.xxa, player.zza);
        float f;
        float f2 = player.level().getBlockState(player.blockPosition()).getBlock().getJumpFactor();
        float f3 = player.level().getBlockState(player.getOnPos()).getBlock().getJumpFactor();
        this.jumpPower = f = 0.42f * ((double)f2 == 1.0 ? f3 : f2) + player.getJumpBoostPower();
    }

    private void tick() {
        float f = this.strafeSpeed;
        float f2 = this.forwardSpeed;
        float f3 = f * f + f2 * f2;
        if (f3 >= 1.0E-4f) {
            if ((f3 = Mth.sqrt(f3)) < 1.0f) {
                f3 = 1.0f;
            }
            float f4 = this.jumpPower;
            if (ClientBase.mc.player.isSprinting()) {
                f4 *= 1.3f;
            }
            f3 = f4 / f3;
            float f5 = Mth.sin(this.yaw * (float)Math.PI / 180.0f);
            float f6 = Mth.cos(this.yaw * (float)Math.PI / 180.0f);
            this.motionX += (f *= f3) * f6 - (f2 *= f3) * f5;
            this.motionZ += f2 * f6 + f * f5;
        }
        this.motionY -= 0.08;
        this.motionY *= 0.98f;
        this.x += this.motionX;
        this.y += this.motionY;
        this.z += this.motionZ;
    }

    private void tickWithFriction() {
        float f = this.strafeSpeed * 0.98f;
        float f2 = this.forwardSpeed * 0.98f;
        float f3 = f * f + f2 * f2;
        if (f3 >= 1.0E-4f) {
            if ((f3 = Mth.sqrt(f3)) < 1.0f) {
                f3 = 1.0f;
            }
            float f4 = this.jumpPower;
            if (ClientBase.mc.player.isSprinting()) {
                f4 *= 1.3f;
            }
            f3 = f4 / f3;
            float f5 = Mth.sin(this.yaw * (float)Math.PI / 180.0f);
            float f6 = Mth.cos(this.yaw * (float)Math.PI / 180.0f);
            this.motionX += (f *= f3) * f6 - (f2 *= f3) * f5;
            this.motionZ += f2 * f6 + f * f5;
        }
        this.motionY -= 0.08;
        this.motionY *= 0.98f;
        this.x += this.motionX;
        this.y += this.motionY;
        this.z += this.motionZ;
        this.motionX *= 0.91;
        this.motionZ *= 0.91;
    }

    public BlockPos findLandingBlock(int n) {
        for (int i = 0; i < n; ++i) {
            Vec3 vec3 = new Vec3(this.x, this.y, this.z);
            this.tickWithFriction();
            Vec3 vec32 = new Vec3(this.x, this.y, this.z);
            float f = ClientBase.mc.player.getBbWidth() / 2.0f;
            BlockPos blockPos = this.rayTraceBlock(vec3, vec32);
            if (blockPos != null) {
                return blockPos;
            }
            blockPos = this.rayTraceBlock(vec3.add(f, 0.0, f), vec32);
            if (blockPos != null) {
                return blockPos;
            }
            blockPos = this.rayTraceBlock(vec3.add(-f, 0.0, f), vec32);
            if (blockPos != null) {
                return blockPos;
            }
            blockPos = this.rayTraceBlock(vec3.add(f, 0.0, -f), vec32);
            if (blockPos != null) {
                return blockPos;
            }
            blockPos = this.rayTraceBlock(vec3.add(-f, 0.0, -f), vec32);
            if (blockPos != null) {
                return blockPos;
            }
            blockPos = this.rayTraceBlock(vec3.add(f, 0.0, f / 2.0f), vec32);
            if (blockPos != null) {
                return blockPos;
            }
            blockPos = this.rayTraceBlock(vec3.add(-f, 0.0, f / 2.0f), vec32);
            if (blockPos != null) {
                return blockPos;
            }
            blockPos = this.rayTraceBlock(vec3.add(f / 2.0f, 0.0, f), vec32);
            if (blockPos != null) {
                return blockPos;
            }
            blockPos = this.rayTraceBlock(vec3.add(f / 2.0f, 0.0, -f), vec32);
            if (blockPos == null) continue;
            return blockPos;
        }
        return null;
    }

    private BlockPos rayTraceBlock(Vec3 vec3, Vec3 vec32) {
        BlockHitResult blockHitResult;
        HitResult hitResult = RayTraceUtil.clipWithEntity(vec3, vec32, false, false, false, ClientBase.mc.player);
        if (hitResult != null && hitResult.getType() == HitResult.Type.BLOCK && hitResult instanceof BlockHitResult && (blockHitResult = (BlockHitResult)hitResult).getDirection() == Direction.UP) {
            return blockHitResult.getBlockPos();
        }
        return null;
    }

    public void simulate(int n) {
        for (int i = 0; i < n; ++i) {
            this.tick();
        }
    }

    public void simulateWithFriction(int n) {
        for (int i = 0; i < n; ++i) {
            this.tickWithFriction();
        }
    }
}