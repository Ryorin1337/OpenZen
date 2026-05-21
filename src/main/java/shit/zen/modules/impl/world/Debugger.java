package shit.zen.modules.impl.world;

import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.HashSet;

import shit.zen.event.impl.DisconnectEvent;
import shit.zen.modules.Category;
import shit.zen.modules.Module;
import shit.zen.utils.misc.ChatUtil;
import shit.zen.event.EventTarget;

public class Debugger
extends Module {
    public Debugger() {
        super("Debugger", Category.WORLD);
    }

    @EventTarget
    public void onDisconnect(DisconnectEvent disconnectEvent) {
        HashSet<String> hashSet = new HashSet<>();
        ThreadMXBean threadMXBean = ManagementFactory.getThreadMXBean();
        if (threadMXBean == null) {
            return;
        }
        ThreadInfo[] threads = threadMXBean.dumpAllThreads(false, false);
        int n = 0;
        for (ThreadInfo threadInfo : threads) {
            String string = threadInfo.getThreadName();
            StackTraceElement[] stackTraceElementArray = threadInfo.getStackTrace();
            if (string == null || stackTraceElementArray == null) continue;
            for (StackTraceElement stackTraceElement : stackTraceElementArray) {
                String string2 = stackTraceElement.getClassName();
                String string3 = stackTraceElement.getFileName();
                String string4 = stackTraceElement.getModuleName();
                if (string3 != null || string4 != null) continue;
                hashSet.add(string2);
                ++n;
            }
        }
        ChatUtil.print("N: " + n + ", Set: ");
        ChatUtil.print("==========================");
        for (String string : hashSet) {
            ChatUtil.print(string);
        }
        ChatUtil.print("==========================");
    }
}