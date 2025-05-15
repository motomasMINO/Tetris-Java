import java.awt.*;

public class TetrisShape {
    private Color color;
    private int x, y;
    private long time, lastTime;
    private int normal = 600, fast = 50;
    private int delay;
    private int[][] coords;
    private int[][] reference;
    private int deltaX;
    private TetrisBoard board;
    private boolean collision = false, moveX = false;
    private int timePassedFromCollision = -1;

    public TetrisShape(int[][] coords, TetrisBoard board, Color color) {
        this.coords = coords;
        this.board = board;
        this.color = color;
        deltaX = 0;
        x = 4;
        y = 0;
        delay = normal;
        time = 0;
        lastTime = System.currentTimeMillis();
        reference = new int[coords.length][coords[0].length];

        for (int i = 0; i < coords.length; i++) {
            System.arraycopy(coords[i], 0, reference[i], 0, coords[i].length);
        }
    }

    long deltaTime;

    public void update() {
        moveX = true;
        deltaTime = System.currentTimeMillis() - lastTime;
        time += deltaTime;
        lastTime = System.currentTimeMillis();

        if (collision && timePassedFromCollision > 500) {
            for (int row = 0; row < coords.length; row++) {
                for (int col = 0; col < coords[0].length; col++) {
                    if (coords[row][col] != 0) {
                        int boardY = y + row;
                        int boardX = x + col;
                        if (boardY >= 0 && boardY < 20 && boardX >= 0 && boardX < 10) {
                            board.getBoard()[boardY][boardX] = color;
                        }
                    }
                }
            }
            board.setCurrentShape();
            board.checkLines();
            timePassedFromCollision = -1;
            return;
        }

        // 水平移動チェック
        boolean withinBounds = true;
        for (int row = 0; row < coords.length; row++) {
            for (int col = 0; col < coords[row].length; col++) {
                if (coords[row][col] != 0) {
                    int newX = x + deltaX + col;
                    int newY = y + row;
                    if (newX < 0 || newX >= 10 || newY < 0 || newY >= 20 || board.getBoard()[newY][newX] != null) {
                        withinBounds = false;
                        moveX = false;
                        break;
                    }
                }
            }
        }

        if (withinBounds && moveX) {
            x += deltaX;
        }

        // 下方向の移動チェック
        if (timePassedFromCollision == -1) {
            boolean canMoveDown = true;
            for (int row = 0; row < coords.length; row++) {
                for (int col = 0; col < coords[row].length; col++) {
                    if (coords[row][col] != 0) {
                        int newY = y + 1 + row;
                        int newX = x + col;

                        if (newY >= 20 || newX < 0 || newX >= 10 || board.getBoard()[newY][newX] != null) {
                            canMoveDown = false;
                            break;
                        }
                    }
                }
            }

            if (canMoveDown) {
                if (time > delay) {
                    y++;
                    time = 0;
                }
            } else {
                collision();
            }
        } else {
            timePassedFromCollision += deltaTime;
        }

        deltaX = 0;
    }

    private void collision() {
        collision = true;
        timePassedFromCollision = 0;
    }

    public void render(Graphics g) {
        g.setColor(color);
        for (int row = 0; row < coords.length; row++) {
            for (int col = 0; col < coords[0].length; col++) {
                if (coords[row][col] != 0) {
                    int drawX = col * 30 + x * 30;
                    int drawY = row * 30 + y * 30;
                    g.fill3DRect(drawX, drawY, TetrisBoard.tileSize, TetrisBoard.tileSize, true);
                }
            }
        }
    }

    public void rotateShape() {
        int[][] rotatedShape = transposeMatrix(coords);
        rotatedShape = reverseRows(rotatedShape);

        // 範囲チェック
        if ((x + rotatedShape[0].length > 10) || (y + rotatedShape.length > 20)) {
            return;
        }

        for (int row = 0; row < rotatedShape.length; row++) {
            for (int col = 0; col < rotatedShape[row].length; col++) {
                if (rotatedShape[row][col] != 0) {
                    int checkX = x + col;
                    int checkY = y + row;
                    if (checkX < 0 || checkX >= 10 || checkY < 0 || checkY >= 20 || board.getBoard()[checkY][checkX] != null) {
                        return;
                    }
                }
            }
        }
        coords = rotatedShape;
    }

    private int[][] transposeMatrix(int[][] matrix) {
        int[][] temp = new int[matrix[0].length][matrix.length];
        for (int i = 0; i < matrix.length; i++) {
            for (int j = 0; j < matrix[0].length; j++) {
                temp[j][i] = matrix[i][j];
            }
        }
        return temp;
    }

    private int[][] reverseRows(int[][] matrix) {
        int middle = matrix.length / 2;
        for (int i = 0; i < middle; i++) {
            int[] temp = matrix[i];
            matrix[i] = matrix[matrix.length - i - 1];
            matrix[matrix.length - i - 1] = temp;
        }
        return matrix;
    }

    public Color getColor() {
        return color;
    }

    public void setDeltaX(int deltaX) {
        this.deltaX = deltaX;
    }

    public void speedUp() {
        delay = fast;
    }

    public void speedDown() {
        delay = normal;
    }

    public int[][] getCoords() {
        return coords;
    }

    public int getX() {
        return x;
    }

    public int getY() {
        return y;
    }
}