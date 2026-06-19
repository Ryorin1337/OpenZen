package shit.zen.patch;

import asm.patchify.annotation.At;
import asm.patchify.annotation.Inject;
import asm.patchify.annotation.Patch;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;

import net.minecraft.world.entity.LivingEntity;
import shit.zen.ZenClient;
import shit.zen.modules.impl.render.EntityEditor;
import shit.zen.utils.render.EntityUtil;

@Patch(EntityRenderDispatcher.class)
public class EntityRenderDispatcherPatch {

    private static final ThreadLocal<Boolean> IS_RENDERING_FAKE = ThreadLocal.withInitial(() -> false);

    @Inject(
            method = "render",
            desc = "(Lnet/minecraft/world/entity/Entity;DDDFFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At(At.Type.HEAD)
    )
    public static void onRender(
            EntityRenderDispatcher dispatcher,
            Entity entity,
            double x, double y, double z,
            float yaw, float partialTick,
            com.mojang.blaze3d.vertex.PoseStack poseStack,
            net.minecraft.client.renderer.MultiBufferSource buffer,
            int light,
            CallbackInfo ci
    ) {
        if (IS_RENDERING_FAKE.get()) {
            return;
        }

        Minecraft mc = Minecraft.getInstance();

        if (!ZenClient.isReady())
            return;

        if (EntityEditor.INSTANCE == null || !EntityEditor.INSTANCE.isEnabled())
            return;

        if (!(entity instanceof net.minecraft.world.entity.player.Player) && !EntityEditor.INSTANCE.getAllEntity()) {
            return;
        }

        if (entity.isInvisible())
            return;

        if (entity == mc.player && EntityEditor.INSTANCE.getIgnoreSelf())
            return;

        String target = EntityEditor.INSTANCE.getTargetEntity();

        Entity fake = EntityUtil.create(entity, target);
        if (fake == null)
            return;
        if (fake instanceof LivingEntity && entity instanceof LivingEntity) {
            LivingEntity fakeLiving = (LivingEntity) fake;
            LivingEntity original = (LivingEntity) entity;
            fakeLiving.setPos(original.getX(), original.getY(), original.getZ());
            fakeLiving.setYRot(original.getYRot());
            fakeLiving.setXRot(original.getXRot());
            fakeLiving.yRotO = original.yRotO;
            fakeLiving.xRotO = original.xRotO;
            fakeLiving.yBodyRot = original.yBodyRot;
            fakeLiving.yHeadRot = original.yHeadRot;
            fakeLiving.yBodyRotO = original.yBodyRotO;
            fakeLiving.yHeadRotO = original.yHeadRotO;
            fakeLiving.tickCount = original.tickCount;
            fakeLiving.walkAnimation.setSpeed(original.walkAnimation.speed());
            fakeLiving.swingTime = original.swingTime;
            fakeLiving.setYHeadRot(original.getYHeadRot());
        }
        ci.cancel();
        try {
            IS_RENDERING_FAKE.set(true);

            dispatcher.render(
                    fake,
                    x, y, z,
                    yaw,
                    partialTick,
                    poseStack,
                    buffer,
                    light
            );
        } finally {
            IS_RENDERING_FAKE.set(false);
        }
    }
}