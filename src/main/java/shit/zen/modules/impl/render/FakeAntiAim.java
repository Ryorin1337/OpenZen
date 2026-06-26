package shit.zen.modules.impl.render;

import shit.zen.modules.Category;
import shit.zen.modules.Module;
import shit.zen.settings.impl.BooleanSetting;
import shit.zen.settings.impl.ModeSetting;
import shit.zen.settings.impl.NumberSetting;
import shit.zen.utils.render.EntityUtil;

public class FakeAntiAim extends Module {

    public static FakeAntiAim  INSTANCE;

    public final NumberSetting spinSpeed = new NumberSetting("Spin Speed", 10, 0, 1000, 1);
    public FakeAntiAim() {
        super("FakeAntiAim ", Category.RENDER);
        INSTANCE = this;
    }

    @Override
    protected void onDisable() {
        super.onDisable();
    }
}