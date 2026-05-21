package shit.zen.utils.game;

import java.util.Arrays;
import java.util.List;
import net.minecraft.client.Camera;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Matrix4fc;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector3f;
import org.joml.Vector4d;
import org.joml.Vector4f;
import shit.zen.ClientBase;

public class EntityUtil
extends ClientBase {
    public static boolean isVisible(Entity entity) {
        Frustum frustum = mc.levelRenderer.getFrustum();
        return frustum.isVisible(entity.getBoundingBox());
    }

    public static Vec3 getInterpolatedPos(Entity entity, float f) {
        double d = Mth.lerp(f, entity.xOld, entity.getX());
        double d2 = Mth.lerp(f, entity.yOld, entity.getY());
        double d3 = Mth.lerp(f, entity.zOld, entity.getZ());
        return new Vec3(d, d2, d3);
    }

    public static AABB getInterpolatedAABB(Entity entity, float f) {
        Vec3 vec3 = EntityUtil.getInterpolatedPos(entity, f);
        double d = (double)entity.getBbWidth() / 2.0;
        return new AABB(vec3.x - d, vec3.y, vec3.z - d, vec3.x + d, vec3.y + (double)entity.getBbHeight(), vec3.z + d);
    }

    public static Vector4d getScreenBounds(AABB aABB, Matrix4f matrix4f, Matrix4f matrix4f2) {
        Matrix4f matrix4f3 = new Matrix4f(matrix4f2).mul(matrix4f);
        int[] nArray = new int[]{0, 0, mc.getWindow().getWidth(), mc.getWindow().getHeight()};
        return EntityUtil.getScreenBoundsInternal(nArray, aABB, matrix4f3);
    }

    private static Vector4d getScreenBoundsInternal(int[] nArray, AABB aABB, Matrix4f matrix4f) {
        List<Vec3> list = Arrays.asList(new Vec3(aABB.minX, aABB.minY, aABB.minZ), new Vec3(aABB.minX, aABB.maxY, aABB.minZ), new Vec3(aABB.maxX, aABB.minY, aABB.minZ), new Vec3(aABB.maxX, aABB.maxY, aABB.minZ), new Vec3(aABB.minX, aABB.minY, aABB.maxZ), new Vec3(aABB.minX, aABB.maxY, aABB.maxZ), new Vec3(aABB.maxX, aABB.minY, aABB.maxZ), new Vec3(aABB.maxX, aABB.maxY, aABB.maxZ));
        Vector4f vector4f = new Vector4f();
        Vector4d vector4d = null;
        boolean bl = false;
        for (Vec3 vec3 : list) {
            Vector4f vector4f2 = new Vector4f((float)vec3.x(), (float)vec3.y(), (float)vec3.z(), 1.0f);
            matrix4f.transform(vector4f2);
            if (!(vector4f2.w > 0.0f)) continue;
            matrix4f.project((float)vec3.x(), (float)vec3.y(), (float)vec3.z(), nArray, vector4f);
            vector4f.y = (float)nArray[3] - vector4f.y;
            bl = true;
            if (vector4d == null) {
                vector4d = new Vector4d(vector4f.x, vector4f.y, vector4f.x, vector4f.y);
                continue;
            }
            vector4d.x = Math.min(vector4f.x, vector4d.x);
            vector4d.y = Math.min(vector4f.y, vector4d.y);
            vector4d.z = Math.max(vector4f.x, vector4d.z);
            vector4d.w = Math.max(vector4f.y, vector4d.w);
        }
        return bl ? vector4d : null;
    }

    public static Vector3f getCameraRelativePos(Entity entity, float f) {
        if (mc == null || mc.gameRenderer == null || mc.gameRenderer.getMainCamera() == null) {
            return null;
        }
        Vec3 vec3 = EntityUtil.getInterpolatedPos(entity, f);
        double d = vec3.x();
        double d2 = vec3.y();
        double d3 = vec3.z();
        Camera camera = mc.gameRenderer.getMainCamera();
        Vec3 vec32 = camera.getPosition();
        Quaternionf quaternionf = camera.rotation();
        Quaternionf quaternionf2 = new Quaternionf(quaternionf).conjugate();
        Vector3f vector3f = new Vector3f((float)(d - vec32.x), (float)(d2 - vec32.y), (float)(d3 - vec32.z));
        vector3f.rotate(quaternionf2);
        return vector3f;
    }
}