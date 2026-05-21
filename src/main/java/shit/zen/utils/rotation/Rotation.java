package shit.zen.utils.rotation;

import java.util.concurrent.ThreadLocalRandom;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.RandomUtils;
import shit.zen.ClientBase;
import shit.zen.utils.math.Vector2f;

@EqualsAndHashCode
@ToString
public class Rotation {
    @Getter @Setter
    public float yaw;
    @Getter @Setter
    public float pitch;
    @Getter @Setter
    public double distanceSq;
    @Getter @Setter
    public Runnable task;
    @Getter @Setter
    public Runnable postTask;
    static final boolean Đ = true;

    public Rotation() {
        this.yaw = 0.0f;
        this.pitch = 0.0f;
    }

    public Rotation(float f, float f2) {
        this.yaw = f;
        this.pitch = f2;
    }

    public Rotation(Vector2f vector2f) {
        this.yaw = vector2f.getX();
        this.pitch = vector2f.getY();
    }

    public Rotation(Vec3 vec3, Vec3 vec32) {
        Vec3 vec33 = vec32.subtract(vec3);
        this.yaw = Mth.wrapDegrees((float)Math.toDegrees(Math.atan2(vec33.z, vec33.x)) - 90.0f);
        this.pitch = Mth.wrapDegrees((float)(-Math.toDegrees(Math.atan2(vec33.y, Math.sqrt(vec33.x * vec33.x + vec33.z * vec33.z)))));
    }

    public Rotation clone() {
        return new Rotation(this.getYaw(), this.getPitch());
    }

    public Vector2f toVector2f() {
        return new Vector2f(this.yaw, this.pitch);
    }

    public Rotation subtract(Rotation rotation) {
        return new Rotation(this.yaw - rotation.yaw, this.pitch - rotation.pitch);
    }

    public Rotation negate() {
        return new Rotation(-this.yaw, -this.pitch);
    }

    public Rotation withTask(Runnable runnable) {
        this.task = runnable;
        return this;
    }

    public Rotation withPostTask(Runnable runnable) {
        this.postTask = runnable;
        return this;
    }

    public void apply() {
        ClientBase.mc.player.setYRot(this.yaw);
        ClientBase.mc.player.setXRot(this.pitch);
    }

    public void applyToPlayer(Player player) {
        if (Float.isNaN(this.yaw) || Float.isNaN(this.pitch)) {
            return;
        }
        this.snapToSensitivity(Float.valueOf(ClientBase.mc.options.sensitivity().get().floatValue()));
        player.setYRot(this.yaw);
        player.setXRot(this.pitch);
    }

    public Rotation snapToSensitivity(Float f) {
        float f2 = f.floatValue() * 0.6f + 0.2f;
        float f3 = f2 * f2 * f2 * 1.2f;
        this.yaw -= this.yaw % f3;
        this.pitch -= this.pitch % f3;
        return this;
    }

    public static float moveTowards(float f, float f2, float f3) {
        float f4 = Mth.wrapDegrees(f2 - f);
        if (f4 > f3) {
            f4 = f3;
        }
        if (f4 < -f3) {
            f4 = -f3;
        }
        return f + f4;
    }

    public double distanceTo(Rotation rotation) {
        float f = Mth.wrapDegrees(this.yaw);
        float f2 = Mth.wrapDegrees(rotation.yaw);
        float f3 = Mth.wrapDegrees(f - f2);
        float f4 = Mth.wrapDegrees(this.pitch);
        float f5 = Mth.wrapDegrees(rotation.pitch);
        float f6 = Mth.wrapDegrees(f4 - f5);
        return Math.sqrt(f3 * f3 + f6 * f6);
    }

    public float smoothYaw(float f, float f2, float f3) {
        float f4 = Rotation.moveTowards(f2, f3, f + RandomUtils.nextFloat(0.0f, 15.0f));
        double d = Mth.wrapDegrees(f3 - f2);
        if ((double)(-f) > d || d > (double)f) {
            if (!Đ && ClientBase.mc.player == null) {
                throw new AssertionError();
            }
            f4 += (float)((double)RandomUtils.nextFloat(1.0f, 2.0f) * Math.sin((double)ClientBase.mc.player.getXRot() * Math.PI));
        }
        if (f4 == f2) {
            return f2;
        }
        float f5 = ClientBase.mc.options.sensitivity().get().floatValue();
        if ((double)f5 == 0.5) {
            f5 = 0.47887325f;
        }
        float f6 = f5 * 0.6f + 0.2f;
        float f7 = f6 * f6 * f6 * 8.0f;
        int n = (int)((6.667 * (double)f4 - 6.666666666666667 * (double)f2) / (double)f7);
        float f8 = (float)n * f7;
        f4 = (float)((double)f2 + (double)f8 * 0.15);
        return f4;
    }

    public float smoothYawArray(float f, float[] fArray, float f2) {
        float f3 = Rotation.moveTowards(fArray[0], f2, f + RandomUtils.nextFloat(0.0f, 15.0f));
        if (f3 != f2) {
            f3 += (float)((double)RandomUtils.nextFloat(1.0f, 2.0f) * Math.sin((double)fArray[1] * Math.PI));
        }
        if (f3 == fArray[0]) {
            return fArray[0];
        }
        float f4 = ClientBase.mc.options.sensitivity().get().floatValue();
        f3 += (float)(ThreadLocalRandom.current().nextGaussian() * 0.2);
        if ((double)f4 == 0.5) {
            f4 = 0.47887325f;
        }
        float f5 = f4 * 0.6f + 0.2f;
        float f6 = f5 * f5 * f5 * 8.0f;
        int n = (int)((6.667 * (double)f3 - 6.6666667 * (double)fArray[0]) / (double)f6);
        float f7 = (float)n * f6;
        f3 = (float)((double)fArray[0] + (double)f7 * 0.15);
        return f3;
    }

    public float smoothPitch(float f, float f2, float f3) {
        float f4;
        float f5 = Rotation.moveTowards(f2, f3, f + RandomUtils.nextFloat(0.0f, 15.0f));
        if (f5 != f3) {
            f5 += (float)((double)RandomUtils.nextFloat(1.0f, 2.0f) * Math.sin((double)ClientBase.mc.player.getYRot() * Math.PI));
        }
        if ((double)(f4 = ClientBase.mc.options.sensitivity().get().floatValue()) == 0.5) {
            f4 = 0.47887325f;
        }
        float f6 = f4 * 0.6f + 0.2f;
        float f7 = f6 * f6 * f6 * 8.0f;
        int n = (int)((6.667 * (double)f5 - 6.666667 * (double)f2) / (double)f7) * -1;
        float f8 = (float)n * f7;
        float f9 = (float)((double)f2 - (double)f8 * 0.15);
        f5 = Mth.clamp(f9, -90.0f, 90.0f);
        return f5;
    }

    public float smoothPitchArray(float f, float[] fArray, float f2) {
        float f3;
        float f4 = Rotation.moveTowards(fArray[1], f2, f + RandomUtils.nextFloat(0.0f, 15.0f));
        if (f4 != f2) {
            f4 += (float)((double)RandomUtils.nextFloat(1.0f, 2.0f) * Math.sin((double)fArray[0] * Math.PI));
        }
        if ((double)(f3 = ClientBase.mc.options.sensitivity().get().floatValue()) == 0.5) {
            f3 = 0.47887325f;
        }
        float f5 = f3 * 0.6f + 0.2f;
        float f6 = f5 * f5 * f5 * 8.0f;
        int n = (int)((6.667 * (double)f4 - 6.666667 * (double)fArray[1]) / (double)f6) * -1;
        float f7 = (float)n * f6;
        float f8 = (float)((double)fArray[1] - (double)f7 * 0.15);
        f4 = Mth.clamp(f8, -90.0f, 90.0f);
        return f4;
    }

    public void setYawPitch(float f, float f2) {
        this.yaw = f;
        this.pitch = f2;
    }

}