import javax.swing.*;
import javax.sound.sampled.Clip;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;

import java.util.Random;

public class TetrisBoard extends JPanel implements KeyListener, MouseListener, MouseMotionListener {
    private static final long serialVersionUID = 1L;

    private App app;

    // 画像
    private BufferedImage pause, refresh, background;

    // サウンド
    private Clip lineBreak, touch, gameOverSound, roundBGM;

    // ボードサイズ
    public static final int BOARD_WIDTH = 10, BOARD_HEIGHT = 20;

    // タイルサイズ
    public static final int tileSize = 30;
    
    // フィールド
    private Color[][] board = new Color[BOARD_HEIGHT][BOARD_WIDTH];
    
    // すべての可能な形状を持つ配列
    private TetrisShape[] shapes = new TetrisShape[7];

    // 現在の図形と次の図形
    private static TetrisShape currentShape, nextShape;

    // ゲームループ
    private Timer gameLoop;

    private static int FPS = 60;
    private static int delay = 1000 / FPS;

    // マウスイベント変数
    private int mouseX, mouseY;

    private boolean leftClick = false;
    
    private Rectangle stopBounds, refreshBounds;

    private boolean isPaused = false;
    private boolean gameOver = false;

    private Color[] colors = {Color.decode("#ed1c24"), // I 赤
                              Color.decode("#ff7f27"), // T オレンジ
                              Color.decode("#fff200"), // L 黄
                              Color.decode("#22b14c"), // J 緑
                              Color.decode("#00a2e8"), // S 水
                              Color.decode("#a349a4"), // Z 紫
                              Color.decode("#3f48cc")  // O 青
    };

    private Random random = new Random();
    
    // ボタンの押下間隔
    private Timer buttonLapse = new Timer(300, new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
            buttonLapse.stop();
        }
    });

    // スコア
    private int score = 0;

    public TetrisBoard(App app) {
        this.app = app;
        setFocusable(true);
        addKeyListener(this);

        // 画像読み込み
        background = Loader.loadImage("/Resources/playBackground.jpg");
        pause = Loader.loadImage("/Resources/pause.png");
        refresh = Loader.loadImage("/Resources/refresh.png");

        // サウンド読み込み
        lineBreak = Loader.loadSound("/Resources/lineBreak.wav");
        touch = Loader.loadSound("/Resources/touch.wav");
        gameOverSound = Loader.loadSound("/Resources/Grand-Master-Jingle.wav");
        roundBGM = Loader.loadSound("/Resources/Hardening-drops.-normal-mix.wav");

        mouseX = 0;
        mouseY = 0;

        stopBounds = new Rectangle(350, 500, pause.getWidth(), pause.getHeight() + pause.getHeight() / 2);
        refreshBounds = new Rectangle(350, 500 - refresh.getHeight() - 20, refresh.getWidth(), 
                                      refresh.getHeight() + refresh.getHeight() / 2);

        // ゲームループ作成
        gameLoop = new Timer(delay, new GameLooper());
        
        // 図形の作成
        shapes[0] = new TetrisShape(new int[][] {
            {1, 1, 1, 1} // I
        }, this, colors[0]);

        shapes[1] = new TetrisShape(new int[][] {
            {1, 1, 0}, {0, 1, 1} // T
        }, this, colors[1]);

        shapes[2] = new TetrisShape(new int[][] {
            {1, 1, 1}, {1, 0, 0} // L
        }, this, colors[2]);

        shapes[3] = new TetrisShape(new int[][] {
            {1, 1, 1}, {0, 0, 1} // J
        }, this, colors[3]);

        shapes[4] = new TetrisShape(new int[][] {
            {0, 1, 1}, {1, 1, 0} // S
        }, this, colors[4]);

        shapes[5] = new TetrisShape(new int[][] {
            {1, 1, 1}, {0, 1, 0} // Z
        }, this, colors[5]);

        shapes[6] = new TetrisShape(new int[][] {
            {1, 1}, {1, 1} // O
        }, this, colors[6]);
    }

    private void update() {
        if(stopBounds.contains(mouseX, mouseY) && leftClick && !buttonLapse.isRunning() && !gameOver) {
            buttonLapse.start();
            isPaused = !isPaused;
        }

        if(refreshBounds.contains(mouseX, mouseY) && leftClick) {
            startGame();
        }

        if(isPaused || gameOver) {
            return;
        }
        currentShape.update();
    }
    
    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        g.drawImage(background, 0, 0, getWidth(), getHeight(), null);

        // ボードを描画
        for(int y = 0; y < board.length; y++) {
            for(int x = 0; x < board[y].length; x++) {
                if(board[y][x] != null) {
                g.setColor(board[y][x]);
                g.fill3DRect(x * tileSize, y * tileSize, tileSize, tileSize, true);
                }
            }
        }

        // 次の図形を描画
        g.setColor(nextShape.getColor());
        for(int row = 0; row < nextShape.getCoords().length; row++) {
           for(int col = 0; col < nextShape.getCoords()[0].length; col++) {
              if(nextShape.getCoords()[row][col] != 0) {
                 g.fill3DRect(col * 30 + 320, row * 30 + 50, TetrisBoard.tileSize, TetrisBoard.tileSize, true);
              }
           }
        }
        currentShape.render(g);

        if(stopBounds.contains(mouseX, mouseY)) {
            g.drawImage(pause.getScaledInstance(pause.getWidth() + 3, pause.getHeight() + 3, 
                        BufferedImage.SCALE_DEFAULT), stopBounds.x + 3, stopBounds.y + 3, null);
        } else {
            g.drawImage(pause, stopBounds.x, stopBounds.y, null);
        }

        if(refreshBounds.contains(mouseX, mouseY)) {
            g.drawImage(refresh.getScaledInstance(refresh.getWidth() + 3, refresh.getHeight() + 3, 
                        BufferedImage.SCALE_DEFAULT), refreshBounds.x + 3, refreshBounds.y + 3, null);
        } else {
            g.drawImage(refresh, refreshBounds.x, refreshBounds.y, null);
        }

        if(isPaused) {
            String isPausedString = "PAUSE";
            g.setColor(Color.WHITE);
            g.setFont(new Font("Arial", Font.PLAIN, 30));
            g.drawString(isPausedString, 100, App.HEIGHT / 2);
        }
        if(gameOver) {
            g.setColor(Color.RED);
            g.setFont(new Font("Arial", Font.PLAIN, 30));
            g.drawString("GAME OVER", 50, App.HEIGHT / 2);
        }

        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.PLAIN, 20));
        g.drawString("NEXT", App.WIDTH - 125, App.HEIGHT / 2 - 280);

        g.setColor(Color.WHITE);
        g.setFont(new Font("Arial", Font.PLAIN, 20));
        g.drawString("SCORE", App.WIDTH - 125, App.HEIGHT / 2);

        g.drawString(score + "", App.WIDTH - 125, App.HEIGHT / 2 + 30);
        g.setColor(Color.WHITE);

        for(int i = 0; i <= BOARD_HEIGHT; i++) {
            g.drawLine(0, i * tileSize, BOARD_WIDTH * tileSize, i * tileSize);
        }
        for(int j = 0; j <= BOARD_WIDTH; j++) {
            g.drawLine(j * tileSize, 0, j * tileSize, BOARD_HEIGHT * 30);
        }
    }

    public void setNextShape() {
        int index = random.nextInt(shapes.length);
        int[][] coords = shapes[index].getCoords();
        Color c = shapes[index].getColor();
        nextShape = new TetrisShape(coords, this, c);
    }

    public void setCurrentShape() {
        currentShape = nextShape;
        setNextShape();
        score += 2;
        touch.setFramePosition(0);
        touch.start();

        for(int row = 0; row < currentShape.getCoords().length; row++) {
            for(int col = 0; col < currentShape.getCoords()[0].length; col++) {
                if(currentShape.getCoords()[row][col] != 0) {
                    if(board[currentShape.getY() + row][currentShape.getX() + col] != null) {
                        gameOver = true;
                        roundBGM.stop();
                        gameOverSound.setFramePosition(0);
                        gameOverSound.start();
                        return;
                    }
                }
            }
        }
    }

    public Color[][] getBoard() {
        return board;
    }

    @Override
    public void keyTyped(KeyEvent e) {}

    @Override
    public void keyPressed(KeyEvent e) {
        if(gameOver) {
            if(e.getKeyCode() == KeyEvent.VK_SPACE) {
                app.returnToTitle();
            }
            return;
        }
        if(e.getKeyCode() == KeyEvent.VK_UP) {
            currentShape.rotateShape();
        }   
        if(e.getKeyCode() == KeyEvent.VK_DOWN) {
            currentShape.speedUp();
        }
        if(e.getKeyCode() == KeyEvent.VK_LEFT) {
            currentShape.setDeltaX(-1);
        }
        if(e.getKeyCode() == KeyEvent.VK_RIGHT) {
            currentShape.setDeltaX(1);
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        if(e.getKeyCode() == KeyEvent.VK_DOWN) {
           currentShape.speedDown();
        }
    }

    public void startGame() {
        stopGame();
        setNextShape();
        setCurrentShape();
        score = 0;
        gameOver = false;
        gameLoop.start();
        roundBGM.setFramePosition(0);
        roundBGM.loop(Clip.LOOP_CONTINUOUSLY);
    }

    public void stopGame() {
        score = 0;

        for(int row = 0; row < board.length; row++) {
            for(int col = 0; col < board[row].length; col++) {
                board[row][col] = null;
            }
        }
        gameLoop.stop();
    }

    class GameLooper implements ActionListener {
        @Override
        public void actionPerformed(ActionEvent e) {
            update();
            repaint();
        }
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        mouseX = e.getX();
        mouseY = e.getY();
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        mouseX = e.getX();
        mouseY = e.getY();
    }

    @Override
    public void mouseClicked(MouseEvent e) {}

    @Override
    public void mousePressed(MouseEvent e) {
        if(e.getButton() == MouseEvent.BUTTON1) {
            leftClick = true;
        }
    }

    @Override
    public void mouseReleased(MouseEvent e) {
        if(e.getButton() == MouseEvent.BUTTON1) {
            leftClick = false;
        }
    }

    @Override
    public void mouseEntered(MouseEvent e) {}

    @Override
    public void mouseExited(MouseEvent e) {}

    public void checkLines() {
        int linesCleared = 0;

        for (int row = 0; row < BOARD_HEIGHT; row++) {
            boolean lineFull = true;
            for (int col = 0; col < BOARD_WIDTH; col++) {
                if (board[row][col] == null) {
                    lineFull = false;
                    break;
                }
            }
    
            if (lineFull) {
                linesCleared++;
                lineBreak.setFramePosition(0);
                lineBreak.start();
    
                // 上の行を1つ下にシフト
                for (int r = row; r > 0; r--) {
                    for (int c = 0; c < BOARD_WIDTH; c++) {
                        board[r][c] = board[r - 1][c];
                    }
                }
    
                // 最上段はnullで初期化
                for (int c = 0; c < BOARD_WIDTH; c++) {
                    board[0][c] = null;
                }
            }
        }
    
        // スコア計算：ライン数に応じたスコア加算
        if (linesCleared >= 4) {
            score += 800;
        } else if (linesCleared == 3) {
            score += 500;
        } else if (linesCleared == 2) {
            score += 300;
        } else if (linesCleared == 1) {
            score += 100;
        }        
    }
}