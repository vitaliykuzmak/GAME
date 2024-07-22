//hi
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Random;
import javax.imageio.ImageIO;

public class MiniGame extends JPanel implements ActionListener {

    private static final int WIDTH = 1920;
    private static final int HEIGHT = 1080;
    private static final int PLAYER_WIDTH = 150;
    private static final int PLAYER_HEIGHT = 225;
    private static final int MONSTER_WIDTH = 150;
    private static final int MONSTER_HEIGHT = 225;
    private static final int COIN_SIZE = 60;
    private static final int GRAVITY = 1;
    private static final int JUMP_STRENGTH = 15;
    private static final int GROUND_HEIGHT = 50;
    private static final int MAX_COINS = 10;

    private Timer timer;
    private int playerX = 100, playerY = HEIGHT - PLAYER_HEIGHT - GROUND_HEIGHT;
    private int playerVelX = 0, playerVelY = 0;
    private boolean jumping = false;

    private ArrayList<Rectangle> monsters;
    private ArrayList<Rectangle> coins;
    private int collectedCoins = 0;
    private boolean gameWon = false;

    private JButton restartButton;

    private BufferedImage playerImage;
    private BufferedImage monsterImage;
    private BufferedImage houseImage;
    private BufferedImage coinImage;

    public MiniGame() {
        setPreferredSize(new Dimension(WIDTH, HEIGHT));
        setBackground(Color.CYAN);
        setFocusable(true);
        setLayout(null);

        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_LEFT:
                        playerVelX = -5;
                        break;
                    case KeyEvent.VK_RIGHT:
                        playerVelX = 5;
                        break;
                    case KeyEvent.VK_SPACE:
                        if (!jumping) {
                            playerVelY = -JUMP_STRENGTH;
                            jumping = true;
                        }
                        break;
                }
            }

            @Override
            public void keyReleased(KeyEvent e) {
                switch (e.getKeyCode()) {
                    case KeyEvent.VK_LEFT:
                    case KeyEvent.VK_RIGHT:
                        playerVelX = 0;
                        break;
                }
            }
        });

        timer = new Timer(16, this); // Approximately 60 FPS
        timer.start();

        monsters = new ArrayList<>();
        coins = new ArrayList<>();
        loadImages();
        spawnMonsters(5);
        spawnCoins(MAX_COINS);

        restartButton = new JButton("Restart Game");
        restartButton.setBounds(WIDTH / 2 - 100, HEIGHT / 2 + 60, 200, 50);
        restartButton.setFont(new Font("Arial", Font.PLAIN, 20));
        restartButton.setVisible(false);
        restartButton.addActionListener(e -> restartGame());
        add(restartButton);
    }

    private void loadImages() {
        try {
            // Завантаження зображень з ресурсів
            URL playerImageUrl = getClass().getResource("/player.png");
            if (playerImageUrl == null) {
                System.out.println("Не вдалося знайти ресурс: /player.png");
            } else {
                playerImage = ImageIO.read(playerImageUrl);
            }


            URL monsterImageUrl = getClass().getResource("/monster.png");
            monsterImage = ImageIO.read(monsterImageUrl);

            URL houseImageUrl = getClass().getResource("/house.png");
            houseImage = ImageIO.read(houseImageUrl);

            URL coinImageUrl = getClass().getResource("/coin.png");
            coinImage = ImageIO.read(coinImageUrl);

        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void spawnMonsters(int count) {
        Random rand = new Random();
        for (int i = 0; i < count; i++) {
            int y = HEIGHT - MONSTER_HEIGHT - GROUND_HEIGHT;
            monsters.add(new Rectangle(rand.nextInt(WIDTH - MONSTER_WIDTH), y, MONSTER_WIDTH, MONSTER_HEIGHT));
        }
    }

    private void spawnCoins(int count) {
        Random rand = new Random();
        for (int i = 0; i < count; i++) {
            int y = HEIGHT - COIN_SIZE - GROUND_HEIGHT;
            coins.add(new Rectangle(rand.nextInt(WIDTH - COIN_SIZE), y, COIN_SIZE, COIN_SIZE));
        }
    }

    private void restartGame() {
        playerX = 100;
        playerY = HEIGHT - PLAYER_HEIGHT - GROUND_HEIGHT;
        playerVelX = 0;
        playerVelY = 0;
        jumping = false;
        collectedCoins = 0;
        gameWon = false;
        monsters.clear();
        coins.clear();
        spawnMonsters(5);
        spawnCoins(MAX_COINS);
        restartButton.setVisible(false);
        repaint();
    }

    @Override
    public void actionPerformed(ActionEvent e) {
        if (gameWon) return;

        playerX += playerVelX;
        playerY += playerVelY;

        if (playerY >= HEIGHT - PLAYER_HEIGHT - GROUND_HEIGHT) {
            playerY = HEIGHT - PLAYER_HEIGHT - GROUND_HEIGHT;
            playerVelY = 0;
            jumping = false;
        } else {
            playerVelY += GRAVITY;
        }

        checkCollisions();
        repaint();
    }

    private void checkCollisions() {
        Rectangle playerRect = new Rectangle(playerX, playerY, PLAYER_WIDTH, PLAYER_HEIGHT);

        for (int i = coins.size() - 1; i >= 0; i--) {
            Rectangle coin = coins.get(i);
            if (playerRect.intersects(coin)) {
                coins.remove(i);
                collectedCoins++;
                if (collectedCoins >= MAX_COINS) {
                    gameWon = true;
                }
            }
        }

        for (int i = monsters.size() - 1; i >= 0; i--) {
            Rectangle monster = monsters.get(i);
            if (playerRect.intersects(monster)) {
                if (playerVelY > 0 && playerY + PLAYER_HEIGHT <= monster.y + 10) {
                    monsters.remove(i);
                    jumping = false;
                    playerVelY = 0;
                } else {
                    gameWon = false;
                }
            }
        }

        if (collectedCoins >= MAX_COINS) {
            gameWon = true;
        }
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        // Draw background elements
        g.setColor(Color.GREEN); // Grass color
        g.fillRect(0, HEIGHT - GROUND_HEIGHT, WIDTH, GROUND_HEIGHT);

        g.setColor(Color.YELLOW); // Sun color
        g.fillOval(WIDTH - 150, 50, 100, 100);

        g.setColor(Color.WHITE); // Cloud color
        g.fillOval(150, 100, 120, 60);
        g.fillOval(200, 80, 120, 60);

        // Draw house
        g.drawImage(houseImage, WIDTH / 2 - 75, HEIGHT - GROUND_HEIGHT - 200, 150, 150, this);

        if (gameWon) {
            g.setColor(Color.GREEN);
            g.setFont(new Font("Arial", Font.BOLD, 80));
            g.drawString("YOU WIN!", WIDTH / 2 - 250, HEIGHT / 2 - 100);
            restartButton.setVisible(true);
            return;
        }

        // Draw player
        g.drawImage(playerImage, playerX, playerY, PLAYER_WIDTH, PLAYER_HEIGHT, this);

        // Draw monsters
        for (Rectangle monster : monsters) {
            g.drawImage(monsterImage, monster.x, monster.y, MONSTER_WIDTH, MONSTER_HEIGHT, this);
        }

        // Draw coins
        for (Rectangle coin : coins) {
            g.drawImage(coinImage, coin.x, coin.y, COIN_SIZE, COIN_SIZE, this);
        }
    }

    public static void main(String[] args) {
        JFrame frame = new JFrame("Mini Game");
        MiniGame game = new MiniGame();
        frame.add(game);
        frame.pack();
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setVisible(true);
    }
}
