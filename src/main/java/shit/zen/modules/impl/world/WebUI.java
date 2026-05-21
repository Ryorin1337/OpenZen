package shit.zen.modules.impl.world;

import shit.zen.modules.Category;
import shit.zen.modules.Module;
import shit.zen.utils.misc.ChatUtil;

public class WebUI extends Module {
    public WebUI() {
        super("WebUI", Category.WORLD);
        this.setEnabled(false);
    }

    @Override
    public void onEnable() {
        ChatUtil.print("WebUI is unavailable in this build.");
        this.setEnabled(false);
    }

    @Override
    public void onDisable() {
    }
}
