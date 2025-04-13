package FlappyBird;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Random;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;

public class FlappyBird implements ActionListener {
    JFrame frame;
    JPanel panel;
    JButton replayButton, pauseButton;
    JButton easyButton, mediumButton, hardButton;
    Timer timer, transitionTimer;
    ArrayList<Rectangle> pipes;
    ArrayList<Rectangle> powerUps;
    int birdX = 100, birdY = 250, birdWidth = 60, birdHeight = 60;
    int pipeWidth = 60, pipeHeight = 150, gap = 120;
    int score = 0, level = 1; // Thêm hệ thống cấp độ
    int pipeSpeed = 5;
    String difficulty = "medium";
    boolean gameOver = false, gameStarted = false, countingDown = false, paused = false, inTutorial = true;
    int countdown = 3, transitionAlpha = 0; // Hiệu ứng chuyển cảnh
    boolean powerUpActive = false;
    int powerUpTimer = 0;
    int backgroundX = 0;
    BufferedImage ronaldoImage, fireImage, backgroundImage, powerUpImage;
    int velocityY = 0, gravity = 1;

    public FlappyBird() {
        frame = new JFrame("Flappy Bird");
        frame.setSize(600, 500);
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(false);

        try {
            ronaldoImage = ImageIO.read(getClass().getResource("/resources/ronaldo.png"));
        } catch (IOException e) {
            System.err.println("Error loading ronaldo.png: " + e.getMessage());
            ronaldoImage = null;
        }

        try {
            fireImage = ImageIO.read(getClass().getResource("/resources/fire.png"));
        } catch (IOException e) {
            System.err.println("Error loading fire.png: " + e.getMessage());
            fireImage = null;
        }

        try {
            backgroundImage = ImageIO.read(getClass().getResource("/resources/background.png"));
        } catch (IOException e) {
            System.err.println("Error loading background.png: " + e.getMessage());
            backgroundImage = null;
        }

        try {
            powerUpImage = ImageIO.read(getClass().getResource("/resources/powerup.png"));
        } catch (IOException e) {
            System.err.println("Error loading powerup.png: " + e.getMessage());
            powerUpImage = null;
        }

        panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                draw(g);
            }
        };
        panel.setBackground(Color.cyan);
        panel.setLayout(null);
        panel.setFocusable(true);
        panel.requestFocusInWindow();

        panel.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_SPACE) {
                    if (inTutorial) {
                        inTutorial = false;
                    } else if (!gameOver && gameStarted && !countingDown && !paused) {
                        velocityY = -10;
                    }
                } else if (e.getKeyCode() == KeyEvent.VK_P) {
                    togglePause();
                }
            }
        });

        frame.add(panel);

        pipes = new ArrayList<>();
        powerUps = new ArrayList<>();
        addPipe();

        replayButton = new JButton("Replay");
        replayButton.setBounds(250, 250, 100, 40);
        replayButton.addActionListener(e -> {
            restartGame();
            panel.requestFocusInWindow();
        });
        replayButton.setVisible(false);
        panel.add(replayButton);

        pauseButton = new JButton("Pause");
        pauseButton.setBounds(500, 10, 80, 30);
        pauseButton.addActionListener(e -> {
            togglePause();
            panel.requestFocusInWindow();
        });
        pauseButton.setVisible(false);
        panel.add(pauseButton);

        easyButton = new JButton("Easy");
        easyButton.setBounds(200, 200, 100, 40);
        easyButton.addActionListener(e -> {
            startGameWithDifficulty("easy");
            panel.requestFocusInWindow();
        });
        easyButton.setVisible(true);
        panel.add(easyButton);

        mediumButton = new JButton("Medium");
        mediumButton.setBounds(200, 250, 100, 40);
        mediumButton.addActionListener(e -> {
            startGameWithDifficulty("medium");
            panel.requestFocusInWindow();
        });
        mediumButton.setVisible(true);
        panel.add(mediumButton);

        hardButton = new JButton("Hard");
        hardButton.setBounds(200, 300, 100, 40);
        hardButton.addActionListener(e -> {
            startGameWithDifficulty("hard");
            panel.requestFocusInWindow();
        });
        hardButton.setVisible(true);
        panel.add(hardButton);

        timer = new Timer(20, this);
        transitionTimer = new Timer(20, e -> {
            if (gameOver) {
                transitionAlpha += 5;
                if (transitionAlpha >= 255) transitionAlpha = 255;
            } else {
                transitionAlpha -= 5;
                if (transitionAlpha <= 0) transitionAlpha = 0;
            }
            panel.repaint();
        });
        transitionTimer.start();
        frame.setVisible(true);
    }

    public void setDifficulty(String level) {
        difficulty = level;
        switch (level) {
            case "easy":
                pipeSpeed = 3;
                gap = 160;
                break;
            case "medium":
                pipeSpeed = 5;
                gap = 120;
                break;
            case "hard":
                pipeSpeed = 7;
                gap = 90;
                break;
        }
    }

    public void startGameWithDifficulty(String level) {
        setDifficulty(level);
        countingDown = true;
        easyButton.setVisible(false);
        mediumButton.setVisible(false);
        hardButton.setVisible(false);
        new Timer(1000, new ActionListener() {
            public void actionPerformed(ActionEvent evt) {
                countdown--;
                if (countdown == 0) {
                    ((Timer) evt.getSource()).stop();
                    gameStarted = true;
                    timer.start();
                    pauseButton.setVisible(true);
                    countdown = 3;
                    countingDown = false;
                    panel.requestFocusInWindow();
                }
                panel.repaint();
            }
        }).start();
    }

    public void togglePause() {
        if (!gameOver && gameStarted && !countingDown) {
            paused = !paused;
            if (paused) {
                timer.stop();
                pauseButton.setText("Resume");
            } else {
                timer.start();
                pauseButton.setText("Pause");
            }
            panel.repaint();
        }
    }

    public void addPipe() {
        int height = new Random().nextInt(200) + 50;
        pipes.add(new Rectangle(600, 0, pipeWidth, height));
        pipes.add(new Rectangle(600, height + gap, pipeWidth, 500 - height - gap));
    }

    public void addPowerUp() {
        if (Math.random() < 0.01) {
            int y = new Random().nextInt(440) + 30;
            powerUps.add(new Rectangle(600, y, 30, 30));
        }
    }

    public void draw(Graphics g) {
        if (backgroundImage != null) {
            g.drawImage(backgroundImage, backgroundX, 0, 600, 500, null);
            g.drawImage(backgroundImage, backgroundX + 600, 0, 600, 500, null);
        } else {
            g.setColor(Color.cyan);
            g.fillRect(0, 0, 600, 500);
        }

        for (Rectangle pipe : pipes) {
            if (fireImage != null) {
                g.drawImage(fireImage, pipe.x, pipe.y, pipe.width, pipe.height, null);
            } else {
                g.setColor(Color.orange);
                g.fillRect(pipe.x, pipe.y, pipe.width, pipe.height);
            }
        }

        for (Rectangle powerUp : powerUps) {
            if (powerUpImage != null) {
                g.drawImage(powerUpImage, powerUp.x, powerUp.y, 30, 30, null);
            } else {
                g.setColor(Color.YELLOW);
                g.fillRect(powerUp.x, powerUp.y, 30, 30);
            }
        }

        if (ronaldoImage != null) {
            g.drawImage(ronaldoImage, birdX, birdY, birdWidth, birdHeight, null);
        } else {
            g.setColor(Color.YELLOW);
            g.fillOval(birdX, birdY, birdWidth, birdHeight);
        }

        g.setColor(Color.white);
        g.setFont(new Font("Arial", Font.BOLD, 24));
        g.drawString("Score: " + score, 20, 30);
        g.drawString("Level: " + level, 20, 60);

        if (inTutorial) {
            // Hiển thị hướng dẫn ở góc phải dưới
            g.setFont(new Font("Arial", Font.BOLD, 20));
            g.setColor(Color.black);
            g.drawString("Flappy Bird", 400, 350); // Dịch xuống góc phải dưới
            g.setFont(new Font("Arial", Font.PLAIN, 16));
            g.drawString("Press SPACE to jump", 400, 380);
            g.drawString("Avoid the pipes!", 400, 400);
            g.drawString("Collect power-ups", 400, 420);
            g.drawString("Choose difficulty", 400, 440);
        } else if (!gameStarted && !countingDown) {
            g.setFont(new Font("Arial", Font.BOLD, 30));
            g.setColor(Color.black);
            g.drawString("Choose Difficulty", 180, 150);
        }

        if (countingDown) {
            g.setFont(new Font("Arial", Font.BOLD, 60));
            g.setColor(Color.BLACK);
            g.drawString(String.valueOf(countdown), 280, 250);
        }

        if (paused) {
            g.setFont(new Font("Arial", Font.BOLD, 40));
            g.setColor(Color.blue);
            g.drawString("Paused", 230, 200);
            g.setFont(new Font("Arial", Font.PLAIN, 20));
            g.drawString("Press P to resume", 220, 250);
        }

        if (gameOver) {
            g.setFont(new Font("Arial", Font.BOLD, 40));
            g.setColor(Color.red);
            g.drawString("Game Over!", 200, 200);
            g.setFont(new Font("Arial", Font.PLAIN, 20));
            g.drawString("Final Score: " + score, 230, 240);
            replayButton.setVisible(true);
            pauseButton.setVisible(false);
        }

        g.setColor(new Color(0, 0, 0, transitionAlpha));
        g.fillRect(0, 0, 600, 500);
    }
    @Override
    public void actionPerformed(ActionEvent e) {
        if (gameStarted && !gameOver && !paused) {
            birdY += velocityY;
            velocityY += gravity;

            backgroundX -= 1;
            if (backgroundX <= -600) backgroundX = 0;

            for (Rectangle pipe : pipes) {
                pipe.x -= pipeSpeed;
            }

            for (Rectangle powerUp : powerUps) {
                powerUp.x -= pipeSpeed;
            }

            if (pipes.size() > 0 && pipes.get(0).x + pipeWidth < 0) {
                pipes.remove(0);
                pipes.remove(0);
                addPipe();
                score++;
                if (score % 10 == 0) { // Tăng cấp độ mỗi 10 điểm
                    level++;
                    pipeSpeed++;
                    gap -= 5;
                    if (gap < 80) gap = 80;
                    if (pipeSpeed > 12) pipeSpeed = 12;
                }
            }

            powerUps.removeIf(powerUp -> powerUp.x + 30 < 0);

            addPowerUp();

            Rectangle birdRect = new Rectangle(birdX, birdY, birdWidth, birdHeight);
            for (int i = powerUps.size() - 1; i >= 0; i--) {
                if (birdRect.intersects(powerUps.get(i))) {
                    powerUps.remove(i);
                    powerUpActive = true;
                    powerUpTimer = 250;
                    pipeSpeed += 3;
                }
            }

            if (powerUpActive) {
                powerUpTimer--;
                if (powerUpTimer <= 0) {
                    powerUpActive = false;
                    pipeSpeed -= 3;
                }
            }

            checkCollision();
            panel.repaint();
        }
    }

    public void checkCollision() {
        if (birdY + birdHeight >= 500) {
            gameOver = true;
            timer.stop();
        }

        for (Rectangle pipe : pipes) {
            if (pipe.intersects(new Rectangle(birdX, birdY, birdWidth, birdHeight))) {
                gameOver = true;
                timer.stop();
                break;
            }
        }
    }

    public void restartGame() {
        birdY = 250;
        velocityY = 0;
        pipes.clear();
        powerUps.clear();
        addPipe();
        score = 0;
        level = 1;
        setDifficulty(difficulty);
        gameOver = false;
        gameStarted = false;
        countingDown = false;
        countdown = 3;
        powerUpActive = false;
        powerUpTimer = 0;
        backgroundX = 0;
        inTutorial = true;
        paused = false;

        replayButton.setVisible(false);
        easyButton.setVisible(true);
        mediumButton.setVisible(true);
        hardButton.setVisible(true);
        pauseButton.setVisible(false);
        pauseButton.setText("Pause");

        panel.repaint();
    }

    public static void main(String[] args) {
        new FlappyBird();
    }
}