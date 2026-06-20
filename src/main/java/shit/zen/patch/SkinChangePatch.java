package shit.zen.patch;

import asm.patchify.annotation.At;
import asm.patchify.annotation.Inject;
import asm.patchify.annotation.Patch;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.resources.ResourceLocation;

import shit.zen.ZenClient;
import shit.zen.modules.impl.render.SkinChange;
@Patch(AbstractClientPlayer.class)
public class SkinChangePatch {

    @Inject(
            method = "getSkinTextureLocation",
            desc = "()Lnet/minecraft/resources/ResourceLocation;",
            at = @At(At.Type.TAIL)
    )
    public static void onGetSkin(AbstractClientPlayer player,
                                 CallbackInfo ci) {

        if (!ZenClient.isReady()) return;

        if (SkinChange.INSTANCE == null || !SkinChange.INSTANCE.isEnabled()) return;

        Object raw = ci.result;
        if (raw == null) return;

        ResourceLocation current = (ResourceLocation) raw;

        Minecraft mc = Minecraft.getInstance();

        // 自己
        if (player == mc.player) {
            if (SkinChange.INSTANCE.selfSkin.getValue() && SkinChange.INSTANCE.getSelfSkin() != null) {
                ci.result = SkinChange.INSTANCE.getSelfSkin();
                return;
            }
        }

        // 别人
        if (SkinChange.INSTANCE.otherSkin.getValue() && SkinChange.INSTANCE.getOtherSkin() != null) {
            ci.result = SkinChange.INSTANCE.getOtherSkin();
        }
    }

    @Inject(
            method = "isSlimModel",
            desc = "()Z",
            at = @At(At.Type.TAIL)
    )
    public static void onSlimModel(AbstractClientPlayer player,
                                   CallbackInfo ci) {

        if (!ZenClient.isReady()) return;

        if (SkinChange.INSTANCE == null || !SkinChange.INSTANCE.isEnabled()) return;

        boolean slim;

        if (player == Minecraft.getInstance().player) {
            slim = "Slim".equals(SkinChange.INSTANCE.getSelfModel());
        } else {
            slim = "Slim".equals(SkinChange.INSTANCE.getOtherModel());
        }

        ci.result = slim;
    }
}