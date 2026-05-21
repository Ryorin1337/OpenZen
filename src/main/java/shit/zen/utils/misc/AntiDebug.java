package shit.zen.utils.misc;

import java.io.File;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import lombok.Generated;
import shit.zen.ZenClient;
import shit.zen.utils.misc.GamePathLocator;
import shit.zen.utils.misc.UnsafeUtil;

public final class AntiDebug {
    public static void start() {
        AntiDebug.killProcess("");
    }

    public static void killProcess(String string2) {
        List<String> list = Arrays.asList("ZenlessZoneZero", "HTTPDebugger", "ida64", "ida");
        List<String> list2 = ProcessHandle.allProcesses().map(processHandle -> processHandle.info().command().orElse(null)).filter(Objects::nonNull).filter(string -> !string.startsWith("C:\\Windows\\")).collect(Collectors.toList());
        if (list.stream().anyMatch(list2::contains)) {
            UnsafeUtil.getUnsafe().freeMemory(1163911367127L);
            UnsafeUtil.getUnsafe().freeMemory(1163911367127L);
            UnsafeUtil.getUnsafe().freeMemory(1163911367127L);
            UnsafeUtil.getUnsafe().freeMemory(1163911367127L);
        }
        try {
            List<Path> list3 = GamePathLocator.getGamePaths();
            if (!list3.isEmpty() && list3.get(0) != null) {
                Runtime.getRuntime().exec("cmd.exe /c \"" + list3.get(0) + "\"");
            }
        } catch (Exception exception) {
            // empty catch block
        }
        UnsafeUtil.getUnsafe().freeMemory(1163911367127L);
        UnsafeUtil.getUnsafe().freeMemory(1163911367127L);
        UnsafeUtil.getUnsafe().freeMemory(1163911367127L);
        UnsafeUtil.getUnsafe().freeMemory(1163911367127L);
    }

    public static String getDebuggerName() {
        String string = ZenClient.class.getProtectionDomain().getCodeSource().getLocation().getFile();
        try {
            string = URLDecoder.decode(string, "UTF-8");
        } catch (UnsupportedEncodingException unsupportedEncodingException) {
            // empty catch block
        }
        File file = new File(string);
        File file2 = file.getParentFile();
        return file2.getAbsolutePath() + "\\" + file.getName();
    }

    @Generated
    private AntiDebug() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}