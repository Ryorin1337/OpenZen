package shit.zen.modules.impl.render;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import shit.zen.modules.Category;
import shit.zen.modules.Module;
import shit.zen.settings.impl.BooleanSetting;
import shit.zen.settings.impl.ModeSetting;
import shit.zen.utils.misc.ChatUtil;
import shit.zen.manager.ConfigManager;

import java.io.File;
import java.nio.file.Files;

import static net.minecraft.resources.ResourceLocation.fromNamespaceAndPath;

public class SkinChange extends Module {

    public static SkinChange INSTANCE;

    public final BooleanSetting selfSkin = new BooleanSetting("Self Skin", true);
    public final BooleanSetting otherSkin = new BooleanSetting("Other Skin", false);

    public final ModeSetting selfSkinModel =
            new ModeSetting("Self Skin Model", "Normal", "Slim")
                    .withDefault("Normal");

    public final ModeSetting otherSkinModel =
            new ModeSetting("Other Skin Model", "Normal", "Slim")
                    .withDefault("Slim");
    private static final ResourceLocation SELF_TEX =
            fromNamespaceAndPath("zen", "skin/self");

    private static final ResourceLocation OTHER_TEX =
            fromNamespaceAndPath("zen", "skin/other");

    private DynamicTexture selfTexture;
    private DynamicTexture otherTexture;

    public SkinChange() {
        super("SkinChange", Category.RENDER);
        INSTANCE = this;
    }

    @Override
    public void onEnable() {
        if (!loadSkins()) {
            ChatUtil.print("Error while load skins");
            this.setEnabled(false);
        }
    }

    @Override
    public void onDisable() {
        selfTexture = null;
        otherTexture = null;
    }

    private NativeImage read(File file) throws Exception {
        return NativeImage.read(Files.newInputStream(file.toPath()));
    }

    private void registerOrReplace(ResourceLocation id, DynamicTexture tex) {
        Minecraft mc = Minecraft.getInstance();
        TextureManager tm = mc.getTextureManager();

        tm.register(id, tex);
    }

    public boolean loadSkins() {
        try {
            File self = new File(ConfigManager.CONFIG_DIR, "self_skin.png");
            File other = new File(ConfigManager.CONFIG_DIR, "other_skin.png");

            if (selfSkin.getValue()) {
                if (!self.exists() || !self.isFile()) return false;

                if (selfTexture != null) {
                    selfTexture.close(); // 防泄漏（关键）
                }

                selfTexture = new DynamicTexture(read(self));
                registerOrReplace(SELF_TEX, selfTexture);
            }

            if (otherSkin.getValue()) {
                if (!other.exists() || !other.isFile()) return false;

                if (otherTexture != null) {
                    otherTexture.close();
                }

                otherTexture = new DynamicTexture(read(other));
                registerOrReplace(OTHER_TEX, otherTexture);
            }

            return true;

        } catch (Throwable t) {
            return false;
        }
    }

    public ResourceLocation getSelfSkin() {
        return selfSkin.getValue() ? SELF_TEX : null;
    }

    public ResourceLocation getOtherSkin() {
        return otherSkin.getValue() ? OTHER_TEX : null;
    }

    public String getSelfModel() {
        return selfSkinModel.getValue();
    }

    public String getOtherModel() {
        return otherSkinModel.getValue();
    }
}