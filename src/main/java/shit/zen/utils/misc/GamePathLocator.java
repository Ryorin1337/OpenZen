package shit.zen.utils.misc;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;
import lombok.Generated;
import shit.zen.exception.SilentException;

public final class GamePathLocator {
    public static Path getGamePath() {
        if (GamePathLocator.isWindows()) {
            String string = System.getenv("USERPROFILE");
            String string2 = "AppData/LocalLow/miHoYo";
            return Paths.get(string, new String[0]).resolve(string2);
        }
        throw new SilentException();
    }

    public static List<Path> getGamePaths() throws Exception {
        Path path = GamePathLocator.getGamePath();
        ArrayList<Path> arrayList = new ArrayList<>();
        Path path2 = null;
        String string = "绝区零/Player.log";
        String string2 = "/ZenlessZoneZero_Data/";
        path2 = path.resolve("绝区零/Player.log");
        Optional<Path> optional = GamePathLocator.findExecutable(path2, "/ZenlessZoneZero_Data/");
        optional.ifPresent(arg_0 -> arrayList.add(arg_0));
        return arrayList;
    }

    private static Optional<Path> findExecutable(Path path, String string) {
        try (Stream<String> stream = Files.lines(path, StandardCharsets.UTF_8)) {
            return stream.filter(line -> line.contains(string)).map(line -> GamePathLocator.findGamePath(line, string)).filter(Optional::isPresent).map(Optional::get).findFirst();
        } catch (Exception exception) {
            exception.printStackTrace();
            return Optional.empty();
        }
    }

    private static Optional<Path> findGamePath(String string, String string2) {
        try {
            int n = string.indexOf(string2);
            if (n != -1) {
                String string3 = string.substring(string.indexOf("at path ") + 8).replace("/UnitySubsystems", "").replace("/ZenlessZoneZero_Data", "/ZenlessZoneZero.exe").trim();
                return Optional.of(Paths.get(string3, new String[0]));
            }
        } catch (InvalidPathException invalidPathException) {
            // empty catch block
        }
        return Optional.empty();
    }

    private static boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("win");
    }

    @Generated
    private GamePathLocator() {
        throw new UnsupportedOperationException("This is a utility class and cannot be instantiated");
    }
}