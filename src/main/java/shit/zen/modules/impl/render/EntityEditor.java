package shit.zen.modules.impl.render;

import shit.zen.modules.Category;
import shit.zen.modules.Module;
import shit.zen.settings.impl.BooleanSetting;
import shit.zen.settings.impl.ModeSetting;

public class EntityEditor extends Module {

    public static EntityEditor INSTANCE;

    private final ModeSetting targetSetting = new ModeSetting("Target", "Pig", "Cow", "Zombie", "Creeper").withDefault("Pig");
    private final BooleanSetting ignoreSelf = new BooleanSetting("Ignore Self",true);
    private final BooleanSetting allEntity = new BooleanSetting("AllEntity",false);


    public EntityEditor() {
        super("EntityEditor", Category.RENDER);
        INSTANCE = this;
    }

    public String getTargetEntity() {
       return targetSetting.getValue();
    }
    public boolean getIgnoreSelf(){
        return ignoreSelf.getValue();
    }
    public boolean getAllEntity(){
        return allEntity.getValue();
    }
    @Override
    protected void onEnable() {
        super.onEnable();
    }

}