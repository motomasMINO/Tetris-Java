import javax.swing.*;
import javax.sound.sampled.Clip;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;

public class Title extends JPanel implements KeyListener {
    private App app;
    private BufferedImage titlescreen;
    private BufferedImage background;
    private Clip titleDemo;

    public Title(App app) {
        titlescreen = Loader.loadImage("/Resources/title.png");
        background = Loader.loadImage("/Resources/background.jpg");
        titleDemo = Loader.loadSound("/Resources/Happy-Happy.wav");

        this.app = app;

        setFocusable(true);
        addKeyListener(this);
        requestFocusInWindow();
        titleDemo.loop(Clip.LOOP_CONTINUOUSLY);
    }

    @Override
    public void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(background, 0, 0, getWidth(), getHeight(), null);
        g.drawImage(titlescreen, App.WIDTH/2 - titlescreen.getWidth()/2, 30 - titlescreen.getHeight()/2 + 150, null);
        g.setColor(Color.YELLOW);
        g.setFont(new Font("Arial", Font.PLAIN, 18));
        g.drawString("* PUSH SPACE KEY *", 125, App.HEIGHT/2 + 100);
    }

    @Override
    public void keyPressed(KeyEvent e) {
        if (e.getKeyCode() == KeyEvent.VK_SPACE) {
            app.startTetris();
            titleDemo.stop();
        }
    }

    @Override public void keyReleased(KeyEvent e) {}
    @Override public void keyTyped(KeyEvent e) {}
}
