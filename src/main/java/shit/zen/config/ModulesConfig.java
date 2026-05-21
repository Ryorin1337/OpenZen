package shit.zen.config;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import shit.zen.ZenClient;
import shit.zen.config.Config;
import shit.zen.exception.ModuleNotFoundException;
import shit.zen.manager.ModuleManager;
import shit.zen.modules.Module;

public class ModulesConfig
extends Config {
    private static final Logger LOGGER = LogManager.getLogger(ModulesConfig.class);

    public ModulesConfig() {
        super("modules.cfg");
    }

    @Override
    public void read(BufferedReader bufferedReader) throws IOException {
        String string;
        ModuleManager moduleManager = ZenClient.getInstance().getModuleManager();
        while ((string = bufferedReader.readLine()) != null) {
            String[] stringArray = string.split(":", 3);
            if (stringArray.length != 3) {
                LOGGER.error("Failed to read line {}!", string);
                continue;
            }
            String string2 = stringArray[0];
            int n = Integer.parseInt(stringArray[1]);
            boolean bl = Boolean.parseBoolean(stringArray[2]);
            try {
                Module module = moduleManager.getModule(string2);
                module.setKey(n);
                module.setEnabled(bl);
            } catch (ModuleNotFoundException moduleNotFoundException) {
                LOGGER.error("Failed to find module {}!", string2);
            }
        }
    }

    @Override
    public void save(BufferedWriter bufferedWriter) throws IOException {
        ModuleManager moduleManager = ZenClient.getInstance().getModuleManager();
        ArrayList<Module> arrayList = new ArrayList<>(moduleManager.getModules());
        for (Module module : arrayList) {
            bufferedWriter.write(String.format((String)"%s:%d:%s\n", (Object[])new Object[]{module.getName(), module.getKey(), module.isEnabled()}));
        }
    }
}