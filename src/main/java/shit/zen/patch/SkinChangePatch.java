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
    public static void onGetSkin(AbstractClientPlayer player, CallbackInfo ci) {
        if (!ZenClient.isReady()) return;
        if (SkinChange.INSTANCE == null || !SkinChange.INSTANCE.isEnabled()) return;
        if (ci.result == null) return;

        Minecraft mc = Minecraft.getInstance();
        if (player == mc.player) {
            ResourceLocation selfTex = SkinChange.INSTANCE.getSelfSkin();
            if (selfTex != null) {
                ci.result = selfTex;
            }
        } else {
            ResourceLocation otherTex = SkinChange.INSTANCE.getOtherSkin();
            if (otherTex != null) {
                ci.result = otherTex;
            }
        }
    }

    @Inject(
            method = "getModelName",
            desc = "()Ljava/lang/String;", // 返回值改成了 String
            at = @At(At.Type.TAIL)
    )
    public static void onGetModelName(AbstractClientPlayer player, CallbackInfo ci) {

        if (!ZenClient.isReady()) return;
        if (SkinChange.INSTANCE == null || !SkinChange.INSTANCE.isEnabled()) return;

        Minecraft mc = Minecraft.getInstance();

        if (player == mc.player) {
            if (SkinChange.INSTANCE.selfSkin.getValue()) {

                ci.result = "Slim".equals(SkinChange.INSTANCE.getSelfModel()) ? "slim" : "default";
            }
        } else {
            if (SkinChange.INSTANCE.otherSkin.getValue()) {
                ci.result = "Slim".equals(SkinChange.INSTANCE.getOtherModel()) ? "slim" : "default";
            }
        }
    }
}