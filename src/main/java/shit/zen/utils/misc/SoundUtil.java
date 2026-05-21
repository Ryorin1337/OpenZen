package shit.zen.utils.misc;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.Control;
import javax.sound.sampled.FloatControl;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.swing.JOptionPane;
import shit.zen.manager.ConfigManager;
import shit.zen.utils.math.MathUtil;

public final class SoundUtil {
    public static void playSound(String string, String string2) {
        JOptionPane.showMessageDialog(null, string2, string, 0);
    }

    public static void playSound(String string, float f) {
        File file = new File(ConfigManager.CONFIG_DIR, string);
        if (!file.exists()) {
            System.out.println("Failed to find target file!");
            return;
        }
        new Thread(() -> {
            try {
                AudioInputStream audioInputStream = AudioSystem.getAudioInputStream(new BufferedInputStream(new FileInputStream(file)));
                Clip clip = AudioSystem.getClip();
                clip.open(audioInputStream);
                clip.start();
                FloatControl floatControl = (FloatControl)clip.getControl(FloatControl.Type.MASTER_GAIN);
                floatControl.setValue(f);
                clip.start();
            } catch (IOException | LineUnavailableException | UnsupportedAudioFileException throwable) {
                throwable.printStackTrace();
            }
        }, "Netty Client IO #" + MathUtil.randomInt(0, 100)).start();
    }
}