package shit.zen.modules.impl.render;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import org.joml.Quaternionf;
import shit.zen.modules.Category;
import shit.zen.modules.Module;
import shit.zen.modules.impl.combat.KillAura;
import shit.zen.settings.impl.ModeSetting;
import shit.zen.settings.impl.NumberSetting;

public class OldHitting
extends Module {
    private final ModeSetting animationModeSetting = new ModeSetting("Animation", "Vanilla", "Leaked", "Slide").withDefault("Leaked");
    private final NumberSetting sizeSetting = new NumberSetting("Size", Double.valueOf(1.0), Double.valueOf(0.1), Double.valueOf(3.0), Double.valueOf(0.1));
    private final NumberSetting speedSetting = new NumberSetting("Speed", Double.valueOf(1.0), Double.valueOf(0.1), Double.valueOf(5.0), Double.valueOf(0.1));
    private final NumberSetting yOffsetSetting = new NumberSetting("Y-Offset", Double.valueOf(0.0), Double.valueOf(-1.0), Double.valueOf(1.0), Double.valueOf(0.1));
    public static OldHitting INSTANCE;

    public OldHitting() {
        super("OldHitting", Category.RENDER);
        INSTANCE = this;
    }

    public boolean isKillAuraAttacking() {
        return KillAura.INSTANCE != null && KillAura.INSTANCE.isEnabled() && KillAura.INSTANCE.noUseItem.getValue() != false && KillAura.aimingTarget != null;
    }

    public static void applyTranslate(double d, double d2, double d3, PoseStack poseStack) {
        poseStack.translate(d, d2, d3);
    }

    public static void applyRotate(float f, float f2, float f3, float f4, PoseStack poseStack) {
        poseStack.mulPose(new Quaternionf().rotationAxis(f * ((float)Math.PI / 180), f2, f3, f4));
    }

    public static void applyScale(float f, float f2, float f3, PoseStack poseStack) {
        poseStack.scale(f, f2, f3);
    }

    public void applyHitAnimation(PoseStack poseStack, float f, HumanoidArm humanoidArm, float f2) {
        float f3 = f * this.speedSetting.getValue().floatValue();
        float f4 = this.sizeSetting.getValue().floatValue();
        poseStack.translate(0.0f, this.yOffsetSetting.getValue().floatValue(), 0.0f);
        if (this.animationModeSetting.getValue().equalsIgnoreCase("vanilla")) {
            int n = humanoidArm == HumanoidArm.RIGHT ? 1 : -1;
            OldHitting.applyTranslate((float)n * 0.56f, -0.52f + f2 * -0.6f, -0.72, poseStack);
            OldHitting.applyTranslate((float)n * -0.1414214f, 0.08f, 0.1414213925600052, poseStack);
            OldHitting.applyRotate(-102.25f, 1.0f, 0.0f, 0.0f, poseStack);
            OldHitting.applyRotate((float)n * 13.365f, 0.0f, 1.0f, 0.0f, poseStack);
            OldHitting.applyRotate((float)n * 78.05f, 0.0f, 0.0f, 1.0f, poseStack);
            double d = Math.sin((double)(f3 * f3) * Math.PI);
            double d2 = Math.sin(Math.sqrt(f3) * Math.PI);
            OldHitting.applyRotate((float)(d * -20.0), 0.0f, 1.0f, 0.0f, poseStack);
            OldHitting.applyRotate((float)(d2 * -20.0), 0.0f, 0.0f, 1.0f, poseStack);
            OldHitting.applyRotate((float)(d2 * -80.0), 1.0f, 0.0f, 0.0f, poseStack);
            OldHitting.applyScale(f4, f4, f4, poseStack);
        }
        if (this.animationModeSetting.getValue().equalsIgnoreCase("leaked")) {
            this.setupLeakedAnim(poseStack, f2, f3, f4);
            this.setupLeakedArmPos(poseStack);
            float f5 = Mth.sin(Mth.sqrt(f3) * (float)Math.PI) / 8.0f;
            poseStack.translate(0.008, 0.24, 0.03);
            poseStack.translate(-0.16, -0.25, 0.0);
            poseStack.scale((float)(0.8 + (double)f5) * f4, (float)(0.8 + (double)f5) * f4, (float)(0.8 + (double)f5) * f4);
            OldHitting.applyRotate(-Mth.sin((float)((double)Mth.sqrt(f3) * Math.PI)) * 20.0f, 0.0f, 1.2f, -0.8f, poseStack);
            OldHitting.applyRotate(-Mth.sin((float)((double)Mth.sqrt(f3) * Math.PI)) * 30.0f, 1.0f, 0.0f, 0.0f, poseStack);
            poseStack.scale(2.4f * f4, 2.4f * f4, 2.4f * f4);
            OldHitting.applyRotate(-38.4f, 0.0f, 1.0f, 0.0f, poseStack);
            OldHitting.applyScale(f4, f4, f4, poseStack);
        }
        if (this.animationModeSetting.getValue().equalsIgnoreCase("slide")) {
            float f6 = Mth.sin(Mth.sqrt(f3) * (float)Math.PI);
            OldHitting.applyTranslate(0.648f, -0.55f, -0.7199999690055847, poseStack);
            OldHitting.applyTranslate(0.0, 0.0, 0.0, poseStack);
            OldHitting.applyRotate(77.0f, 0.0f, 1.0f, 0.0f, poseStack);
            OldHitting.applyRotate(-10.0f, 0.0f, 0.0f, 1.0f, poseStack);
            OldHitting.applyRotate(-80.0f, 1.0f, 0.0f, 0.0f, poseStack);
            OldHitting.applyRotate(-f6 * 20.0f, 1.0f, 0.0f, 0.0f, poseStack);
            OldHitting.applyScale(1.2f * f4, 1.2f * f4, 1.2f * f4, poseStack);
            OldHitting.applyScale(f4, f4, f4, poseStack);
        }
    }

    private void setupLeakedAnim(PoseStack poseStack, float f, float f2, float f3) {
        poseStack.translate(0.56f, -0.52f, -0.71999997f);
        OldHitting.applyRotate(45.0f, 0.0f, 1.0f, 0.0f, poseStack);
        float f4 = Mth.sin(f2 * f2 * (float)Math.PI);
        float f5 = Mth.sin(Mth.sqrt(f2) * (float)Math.PI);
        OldHitting.applyRotate(f4 * -20.0f, 0.0f, 1.0f, 0.0f, poseStack);
        OldHitting.applyRotate(f5 * -20.0f, 0.0f, 0.0f, 1.0f, poseStack);
        OldHitting.applyRotate(f5 * -80.0f, 1.0f, 0.0f, 0.0f, poseStack);
        poseStack.scale(0.4f * f3, 0.4f * f3, 0.4f * f3);
    }

    private void setupLeakedArmPos(PoseStack poseStack) {
        poseStack.translate(-0.5f, 0.2f, 0.0f);
        OldHitting.applyRotate(30.0f, 0.0f, 1.0f, 0.0f, poseStack);
        OldHitting.applyRotate(-80.0f, 1.0f, 0.0f, 0.0f, poseStack);
        OldHitting.applyRotate(60.0f, 0.0f, 1.0f, 0.0f, poseStack);
    }
}