package shit.zen.hud;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import net.minecraft.util.Mth;
import shit.zen.ZenClient;
import shit.zen.event.impl.GlRenderEvent;
import shit.zen.event.impl.Render2DEvent;
import shit.zen.modules.Module;
import shit.zen.modules.impl.render.Interface;
import shit.zen.render.FontPresets;
import shit.zen.render.FontRenderer;
import shit.zen.render.GlHelper;
import shit.zen.utils.render.ColorUtil;
import shit.zen.event.EventTarget;

public class ModuleListHud
extends HudElement {
    public ModuleListHud() {
        super("ModuleList");
    }

    private List<Module> getVisibleModules() {
        return ZenClient.getInstance().getModuleManager().getModules().stream().filter(module -> !(module instanceof ModuleListHud) && !(module instanceof Interface)).filter(Module::isEnabled).filter(module -> !module.getName().isEmpty()).sorted((module, module2) -> Mth.ceil(GlHelper.getStringWidth(module2.getName(), FontPresets.pingfang(16.0f)) - GlHelper.getStringWidth(module.getName(), FontPresets.pingfang(16.0f)))).collect(Collectors.toList());
    }

    @Override
    public void onRender2D(Render2DEvent render2DEvent, float f, float f2) {
    }

    @EventTarget
    public void onGlRenderDirect(GlRenderEvent glRenderEvent) {
        if (!this.isEnabled()) {
            return;
        }
        if (!ZenClient.getInstance().getModuleManager().getModule(Interface.class).isEnabled()) {
            return;
        }
        FontRenderer fontRenderer = FontPresets.pingfang(16.0f);
        List<Module> list = this.getVisibleModules();
        GlHelper.drawTextShadowLegacy("Z", 4.0f, 4.0f, fontRenderer, ColorUtil.getRainbowColor(10, 1).getRGB());
        GlHelper.drawTextShadowLegacy("en (" + mc.getFps() + "FPS)", 4.0f + GlHelper.getStringWidth("Z", fontRenderer), 4.0f, fontRenderer, -1);
        if (!list.isEmpty()) {
            float f = 0.0f;
            for (Module module : list) {
                GlHelper.drawTextShadowLegacy(module.getName(), 4.0f, 16.0f + f, fontRenderer, ColorUtil.getRainbowColor(10, list.indexOf(module) * 8).getRGB());
                Objects.requireNonNull(mc.font);
                f += (float)(9 + 2);
            }
        }
    }

    @Override
    public void onGlRender(GlRenderEvent glRenderEvent, float f, float f2) {
    }

    @Override
    public void onSettings() {
    }
}