package shit.zen.patch;

import asm.patchify.annotation.At;
import asm.patchify.annotation.Inject;
import asm.patchify.annotation.Patch;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.LivingEntity;
import shit.zen.ZenClient;
import shit.zen.event.impl.RenderEntityEvent;

@Patch(LivingEntityRenderer.class)
public class LivingEntityRendererPatch {
    @Inject(
            method = "render",
            desc = "(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At(At.Type.HEAD)
    )
    public static void onRenderPre(
            LivingEntityRenderer<?, ?> renderer, LivingEntity entity, float yaw, float partialTick,
            PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, CallbackInfo callbackInfo) {
        if (!ZenClient.isReady()) return;
        RenderEntityEvent.Post pre = new RenderEntityEvent.Post(renderer, entity, poseStack, bufferSource, partialTick, packedLight);
        ZenClient.getInstance().getEventBus().call(pre);
        if (pre.isCancelled()) {
            callbackInfo.cancel();
        }
    }

    @Inject(
            method = "render",
            desc = "(Lnet/minecraft/world/entity/LivingEntity;FFLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;I)V",
            at = @At(At.Type.TAIL)
    )
    public static void onRenderPost(
            LivingEntityRenderer<?, ?> renderer, LivingEntity entity, float yaw, float partialTick,
            PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, CallbackInfo callbackInfo) {
        if (!ZenClient.isReady()) return;
        RenderEntityEvent.Pre post = new RenderEntityEvent.Pre(renderer, entity, poseStack, bufferSource, partialTick, packedLight);
        ZenClient.getInstance().getEventBus().call(post);
    }
}
