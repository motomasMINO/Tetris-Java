import java.awt.image.BufferedImage;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import javax.imageio.ImageIO;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.Clip;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.UnsupportedAudioFileException;

public class Loader {
    public static BufferedImage loadImage(String resourcePath) {
        try (InputStream is = Loader.class.getResourceAsStream(resourcePath)) {
            if(is == null) {
                throw new IOException("リソースが見つかりません: " + resourcePath);
            }
            return ImageIO.read(is);
        } catch(IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static Clip loadSound(String path) {
    try {
        Clip clip = AudioSystem.getClip();
        clip.open(AudioSystem.getAudioInputStream(Loader.class.getResource(path)));
        return clip;
    } catch(LineUnavailableException | IOException | UnsupportedAudioFileException e) {
            e.printStackTrace();
    }
    return null;
  }
}