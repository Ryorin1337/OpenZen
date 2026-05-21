package shit.zen.gui.panel.setting;

import java.util.ArrayList;
import java.util.List;
import net.minecraft.client.gui.GuiGraphics;
import shit.zen.gui.panel.setting.BooleanSettingRenderer;
import shit.zen.gui.panel.setting.ModeSettingRenderer;
import shit.zen.gui.panel.setting.MultiSelectSettingRenderer;
import shit.zen.gui.panel.setting.NumberSettingRenderer;
import shit.zen.gui.panel.setting.SettingRenderer;
import shit.zen.settings.Setting;
import shit.zen.settings.impl.ModeSetting;

public class SettingRendererRegistry {
    private static final SettingRendererRegistry INSTANCE;
    private final List<SettingRenderer> renderers = new ArrayList<>();
    private static final String versionString;

    private SettingRendererRegistry() {
        this.register(new ModeSettingRenderer());
        this.register(new BooleanSettingRenderer());
        this.register(new NumberSettingRenderer());
        this.register(new MultiSelectSettingRenderer());
    }

    public static SettingRendererRegistry getInstance() {
        return INSTANCE;
    }

    public void register(SettingRenderer settingRenderer) {
        this.renderers.add(settingRenderer);
    }

    public SettingRenderer findRenderer(Setting<?> setting) {
        for (SettingRenderer settingRenderer : this.renderers) {
            if (!settingRenderer.supports(setting)) continue;
            return settingRenderer;
        }
        return null;
    }

    public int render(GuiGraphics guiGraphics, Setting<?> setting, int n, int n2, int n3, int n4, int n5, float f, float f2) {
        SettingRenderer settingRenderer = this.findRenderer(setting);
        if (!setting.getName().equals(versionString) || setting instanceof ModeSetting) {
            // empty if block
        }
        if (settingRenderer != null) {
            return settingRenderer.render(guiGraphics, setting, n, n2, n3, n4, n5, f, f2);
        }
        return 0;
    }

    public boolean onClick(Setting<?> setting, int n, int n2, int n3, int n4, int n5, int n6, float f) {
        SettingRenderer settingRenderer = this.findRenderer(setting);
        if (settingRenderer != null) {
            return settingRenderer.onClick(setting, n, n2, n3, n4, n5, n6, f);
        }
        return false;
    }

    public int getHeight(Setting<?> setting, float f) {
        SettingRenderer settingRenderer = this.findRenderer(setting);
        if (settingRenderer != null) {
            return settingRenderer.getHeight(setting, f);
        }
        return 0;
    }

    public int getHeightForScroll(Setting<?> setting, float f) {
        SettingRenderer settingRenderer = this.findRenderer(setting);
        if (settingRenderer != null) {
            return settingRenderer.getHeight(setting, f);
        }
        return 0;
    }

    static {
        versionString = "Mode";
        INSTANCE = new SettingRendererRegistry();
    }
}