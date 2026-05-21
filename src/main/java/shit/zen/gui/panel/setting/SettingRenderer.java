package shit.zen.gui.panel.setting;

import net.minecraft.client.gui.GuiGraphics;
import shit.zen.settings.Setting;

public interface SettingRenderer {
    int render(GuiGraphics var1, Setting<?> var2, int var3, int var4, int var5, int var6, int var7, float var8, float var9);

    boolean onClick(Setting<?> var1, int var2, int var3, int var4, int var5, int var6, int var7, float var8);

    boolean supports(Setting<?> var1);

    default int getHeight(Setting<?> setting, float f) {
        return Math.round(20.0f * f);
    }

    default void onMouseMove(double d, double d2) {
    }

    void onMouseRelease(double var1, double var3, int var5);
}