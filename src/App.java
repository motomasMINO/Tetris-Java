import javax.swing.*;

public class App {
    public static final int WIDTH = 460, HEIGHT = 639;

    private TetrisBoard board;
    private Title title;
    private JFrame window;

    public App() {
        window = new JFrame("TETRIS");
        window.setSize(WIDTH, HEIGHT);
        window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        window.setLocationRelativeTo(null);
        window.setResizable(false);

        board = new TetrisBoard(this);
        title = new Title(this); // ここで App を渡す

        window.add(title);
        window.setVisible(true);
    }

    public void startTetris() {
        window.remove(title);
        window.addMouseMotionListener(board);
        window.addMouseListener(board);
        window.add(board);
        board.startGame();
        window.revalidate();
        board.requestFocusInWindow(); // ゲーム画面にもフォーカス
    }

    public void returnToTitle() {
        window.remove(board);
        window.add(title);
        window.revalidate();
        window.repaint();
        title.requestFocusInWindow(); // タイトル画面に戻したときにもフォーカス
    }

    public static void main(String[] args) {
        new App();
    }
}
