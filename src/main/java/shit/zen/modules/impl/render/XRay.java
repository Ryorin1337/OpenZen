package shit.zen.modules.impl.render;

import shit.zen.modules.Category;
import shit.zen.modules.Module;

public class XRay extends Module {
    public static XRay INSTANCE;

    public XRay() {
        super("XRay", Category.RENDER);
        INSTANCE = this;
    }
}
