package shit.zen.utils.render;

import com.mojang.blaze3d.systems.RenderSystem;
import java.lang.reflect.Method;
import java.nio.FloatBuffer;
import java.nio.IntBuffer;
import lombok.Generated;
import net.minecraft.client.Camera;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.joml.Quaternionfc;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.joml.Vector4f;
import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import shit.zen.ClientBase;
import shit.zen.utils.math.Vector2f;
import shit.zen.utils.misc.ReflectionUtil;

public final class ProjectionUtil
extends ClientBase {
    private static final Matrix4f modelViewMatrix = new Matrix4f();
    private static final Matrix4f projectionMatrix = new Matrix4f();
    private static final Vector4f tempVec4a = new Vector4f();
    private static final Vector4f tempVec4b = new Vector4f();
    private static final FloatBuffer floatBuffer = BufferUtils.createFloatBuffer(16);
    private static final IntBuffer viewport = BufferUtils.createIntBuffer(16);
    private static final Vector3f tempVec3 = new Vector3f();
    private static final Quaternionf tempQuat = new Quaternionf();

    public static void updateMatrices() {
        floatBuffer.clear();
        RenderSystem.getProjectionMatrix().get(floatBuffer);
        projectionMatrix.set(floatBuffer);
        floatBuffer.clear();
        RenderSystem.getModelViewMatrix().get(floatBuffer);
        modelViewMatrix.set(floatBuffer);
        viewport.clear();
        GL11.glGetIntegerv(2978, viewport);
        tempVec4a.set((float)viewport.get(0), (float)viewport.get(1), (float)viewport.get(2), (float)viewport.get(3));
    }

    public static Vector2f project(double d, double d2, double d3, float f) {
        Entity entity;
        Vec3 vec3 = mc.getEntityRenderDispatcher().camera.getPosition();
        Quaternionf quaternionf = new Quaternionf(mc.getEntityRenderDispatcher().cameraOrientation());
        quaternionf.conjugate();
        Vector3f vector3f = new Vector3f((float)(vec3.x - d), (float)(vec3.y - d2), (float)(vec3.z - d3));
        vector3f.rotate(quaternionf);
        if (mc.options.bobView().get() && (entity = mc.getCameraEntity()) instanceof Player) {
            Player player = (Player)entity;
            ProjectionUtil.applyBobbing(player, vector3f, f);
        }
        double d4 = 1.2f;
        try {
            Method method = mc.gameRenderer.getClass().getDeclaredMethod(ReflectionUtil.getMappedMethodName(mc.gameRenderer.getClass(), "getFov", "(Lnet/minecraft/client/Camera;FZ)D"), Camera.class, Float.TYPE, Boolean.TYPE);
            method.setAccessible(true);
            d4 = (Double)method.invoke(mc.gameRenderer, new Object[]{mc.getEntityRenderDispatcher().camera, Float.valueOf(f), true});
        } catch (Exception exception) {
            exception.printStackTrace();
        }
        return ProjectionUtil.projectInternal(vector3f, d4);
    }

    private static void applyBobbing(Player player, Vector3f vector3f, float f) {
        float f2 = player.walkDist;
        float f3 = f2 - player.walkDistO;
        float f4 = -(f2 + f3 * f);
        float f5 = Mth.lerp(f, player.oBob, player.bob);
        Quaternionf quaternionf = new Quaternionf().rotationX(Math.abs(Mth.cos(f4 * (float)Math.PI - 0.2f) * f5) * 5.0f * ((float)Math.PI / 180));
        quaternionf.conjugate();
        vector3f.rotate(quaternionf);
        Quaternionf quaternionf2 = new Quaternionf().rotationZ(Mth.sin(f4 * (float)Math.PI) * f5 * 3.0f * ((float)Math.PI / 180));
        quaternionf2.conjugate();
        vector3f.rotate(quaternionf2);
        Vector3f vector3f2 = new Vector3f(Mth.sin(f4 * (float)Math.PI) * f5 * 0.5f, -Math.abs(Mth.cos(f4 * (float)Math.PI) * f5), 0.0f);
        vector3f2.y = -vector3f2.y;
        vector3f.add(vector3f2);
    }

    private static Vector2f projectInternal(Vector3f vector3f, double d) {
        float f = (float)mc.getWindow().getGuiScaledHeight() / 2.0f;
        float f2 = f / (vector3f.z() * (float)Math.tan(Math.toRadians(d / 2.0)));
        if (vector3f.z() < 0.0f) {
            return new Vector2f(-vector3f.x() * f2 + (float)mc.getWindow().getGuiScaledWidth() / 2.0f, (float)mc.getWindow().getGuiScaledHeight() / 2.0f - vector3f.y() * f2);
        }
        return new Vector2f(Float.MAX_VALUE, Float.MAX_VALUE);
    }

    public static Vector2f project(double d, double d2, double d3) {
        Camera camera = mc.gameRenderer.getMainCamera();
        Vec3 vec3 = camera.getPosition();
        tempVec3.set((float)(d - vec3.x), (float)(d2 - vec3.y), (float)(d3 - vec3.z));
        tempQuat.set(camera.rotation()).conjugate();
        tempVec3.rotate(tempQuat);
        ProjectionUtil.tempVec3.x = -ProjectionUtil.tempVec3.x;
        tempVec4b.set(ProjectionUtil.tempVec3.x, ProjectionUtil.tempVec3.y, -ProjectionUtil.tempVec3.z, 1.0f);
        projectionMatrix.transform(tempVec4b);
        if (ProjectionUtil.tempVec4b.w <= 0.0f) {
            return null;
        }
        float f = ProjectionUtil.tempVec4b.x / ProjectionUtil.tempVec4b.w;
        float f2 = ProjectionUtil.tempVec4b.y / ProjectionUtil.tempVec4b.w;
        if (Float.isNaN(f) || Float.isNaN(f2) || f < -1.2f || f > 1.2f || f2 < -1.2f || f2 > 1.2f) {
            return null;
        }
        float f3 = tempVec4a.x() + (1.0f + f) * tempVec4a.z() / 2.0f;
        float f4 = tempVec4a.y() + (1.0f - f2) * tempVec4a.w() / 2.0f;
        double d4 = mc.getWindow().getGuiScale();
        if (d4 == 0.0) {
            d4 = 1.0;
        }
        f3 = (float)((double)f3 / d4);
        f4 = (float)((double)f4 / d4);
        return new Vector2f(f3, f4);
    }

    @Generated
    private ProjectionUtil() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}