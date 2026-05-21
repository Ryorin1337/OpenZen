package shit.zen.modules.impl.render;

import shit.zen.modules.Category;
import shit.zen.modules.Module;
import shit.zen.settings.impl.NumberSetting;

public class AspectRatio
extends Module {
    public static AspectRatio INSTANCE;
    public final NumberSetting ratioSetting = new NumberSetting("Ratio", Float.valueOf(1.78f), Float.valueOf(0.1f), Float.valueOf(5.0f), Float.valueOf(0.1f));

    public AspectRatio() {
        super("AspectRatio", Category.RENDER);
        INSTANCE = this;
    }
}