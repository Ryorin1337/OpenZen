package shit.zen.modules;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import lombok.Getter;
import lombok.Generated;
import shit.zen.ClientBase;
import shit.zen.ZenClient;
import shit.zen.modules.Category;
import shit.zen.modules.KeyBind;
import shit.zen.settings.Setting;

public abstract class Module
extends ClientBase {
    @Getter
    private final String name;
    @Getter
    private final Category category;
    private int keyCode;
    private final KeyBind bind;
    @Getter
    private boolean enabled;
    @Getter
    private final List<Setting<?>> settings;
    private static final String REGISTER_FAIL_MSG = "Failed to register value for module ";

    protected Module(String string, Category category) {
        this.name = string;
        this.category = category;
        this.keyCode = 0;
        this.bind = new KeyBind(this.keyCode);
        this.settings = new ArrayList<>();
    }

    protected Module(String string, Category category, int n) {
        this.name = string;
        this.category = category;
        this.keyCode = n;
        this.bind = new KeyBind(this.keyCode);
        this.settings = new ArrayList<>();
    }

    public void setKey(int n) {
        this.keyCode = n;
        this.bind.setKey(n);
    }

    public KeyBind getBind() {
        return this.bind;
    }

    public void addSetting(Setting<?> setting) {
        this.settings.add(setting);
    }

    public void registerSettings() {
        for (Field field : this.getClass().getDeclaredFields()) {
            try {
                Object object;
                if (!field.isAccessible()) {
                    field.setAccessible(true);
                }
                if (!((object = field.get(this)) instanceof Setting)) continue;
                this.addSetting((Setting)object);
            } catch (IllegalAccessException illegalAccessException) {
                System.out.println(REGISTER_FAIL_MSG + this.getName() + "!");
            }
        }
    }

    public void setEnabled(boolean bl) {
        this.enabled = bl;
        if (bl) {
            ZenClient.getInstance().getEventBus().register(this);
            this.onEnable();
        } else {
            this.onDisable();
            ZenClient.getInstance().getEventBus().unregister(this);
        }
    }

    public void toggle() {
        this.setEnabled(!this.isEnabled());
    }

    protected void onEnable() {
    }

    protected void onDisable() {
    }

    @Generated
    public int getKey() {
        return this.keyCode;
    }

    }