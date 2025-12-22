import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.InputStream;
import javax.imageio.ImageIO;
import java.util.Random;

public class MiniGameDialog extends JDialog {
    private Dimagotchi pet;
    private JLabel infoLabel;
    private Random random = new Random();
    
    private Timer gameTimer;
    private Timer fireTimer;
    private Timer explodeTimer;
    private Timer targetMoveTimer; 
    private Timer countDownTimer; // 음식 게임용 타이머

    private int missileHits = 0;         
    private int remainingShots = 0;      
    private int feedingRound = 0;     // 음식 게임 라운드
    private int timeLeft = 50;        // 5.0초 (0.1초 단위)
    
    private boolean isFiring = false;    
    private boolean isExploding = false; 
    private Point targetPos = new Point(180, 50); 
    private Point shipPos = new Point(225, 420);  
    private int targetDirection = 1; 
    private int targetSpeed = 7;     

    private BufferedImage[] enemyImages = new BufferedImage[3]; 
    private BufferedImage[] foodImages = new BufferedImage[5]; // 음식 이미지들
    private String[] foodFiles = {"grape.png", "orange.png", "cherry.png", "apple.png", "lemon.png"};
    private int targetFoodIndex = 0; // 이번 라운드에 먹어야 할 음식

    private BufferedImage shipImage;  
    private JPanel gameCanvas; 
    private KeyListener gameKeyListener;

    private Point dragPoint = null;
    private int draggingFoodIndex = -1;

    public MiniGameDialog(JFrame owner, Dimagotchi pet) {
        super(owner, "미니게임!", true);
        this.pet = pet;
        setSize(450, 600);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());

        loadImages();
        showMenu();
    }

    private void loadImages() {
        shipImage = loadImageFile("/ufo.png");
        enemyImages[0] = loadImageFile("/alien.png");
        enemyImages[1] = loadImageFile("/alien2.png");
        enemyImages[2] = loadImageFile("/alien3.png");
        
        for (int i = 0; i < foodFiles.length; i++) {
            foodImages[i] = loadImageFile("/" + foodFiles[i]);
        }
    }

    private BufferedImage loadImageFile(String path) {
        try (InputStream is = getClass().getResourceAsStream(path)) {
            if (is != null) return ImageIO.read(is);
            try (InputStream is2 = getClass().getResourceAsStream("/res" + path)) {
                if (is2 != null) return ImageIO.read(is2);
            }
        } catch (IOException e) {
            System.err.println(path + " 로딩 실패: " + e.getMessage());
        }
        return null;
    }

    private void showMenu() {
        stopAllTimers();
        if (gameKeyListener != null) {
            removeKeyListener(gameKeyListener);
            gameKeyListener = null;
        }
        
        getContentPane().removeAll();
        infoLabel = new JLabel("원하는 훈련을 선택하세요!", SwingConstants.CENTER);
        infoLabel.setFont(new Font("맑은 고딕", Font.BOLD, 18));
        add(infoLabel, BorderLayout.NORTH);

        JPanel menuPanel = new JPanel(new GridLayout(3, 1, 10, 10));
        menuPanel.setBorder(BorderFactory.createEmptyBorder(20, 50, 20, 50));

        JButton btnMissile = new JButton("🚀 외계인 격추");
        JButton btnFeeding = new JButton("🍎 배고픈 다마고치");
        JButton btnCups = new JButton("🎲 컵 속의 간식 찾기");

        btnMissile.addActionListener(e -> startMissileGame());
        btnFeeding.addActionListener(e -> startFeedingGame());
        btnCups.addActionListener(e -> startCupGame());

        menuPanel.add(btnMissile);
        menuPanel.add(btnFeeding);
        menuPanel.add(btnCups);

        add(menuPanel, BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    private void startFeedingGame() {
        getContentPane().removeAll();
        feedingRound = 1;
        startNewFeedingRound();
    }

    private void startNewFeedingRound() {
        timeLeft = 50;
        targetFoodIndex = random.nextInt(5);
        draggingFoodIndex = -1;
        dragPoint = null;

        // [중요] gameCanvas를 새로 생성하고 paintComponent를 오버라이드함
        gameCanvas = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(new Color(240, 255, 240));
                g2.fillRect(0, 0, getWidth(), getHeight());

                // 1. 펫 이미지 (기존 GUI와 동일하게 Character에서 직접 가져옴)
                Image petImg = (pet.getCharacter() != null) ? pet.getCharacter().getCurrentImage() : null;
                int petX = getWidth() / 2 - 60;
                int petY = getHeight() / 2 - 80;
                
                if (petImg != null) {
                    g2.drawImage(petImg, petX, petY, 120, 120, this);
                }

                // 2. 말풍선 (크기 조정: 50x50)
                int bSize = 50; 
                int bX = petX + 90; 
                int bY = petY - 30;

                g2.setColor(Color.WHITE);
                g2.fillRoundRect(bX, bY, bSize, bSize, 15, 15);
                g2.setColor(Color.BLACK);
                g2.setStroke(new BasicStroke(2f));
                g2.drawRoundRect(bX, bY, bSize, bSize, 15, 15);

                // 말풍선 내부 과일 (중앙 배치)
                if (foodImages[targetFoodIndex] != null) {
                    int fSize = 30;
                    int fX = bX + (bSize - fSize) / 2;
                    int fY = bY + (bSize - fSize) / 2;
                    g2.drawImage(foodImages[targetFoodIndex], fX, fY, fSize, fSize, this);
                }

                // 3. 하단 음식 리스트
                for (int i = 0; i < 5; i++) {
                    if (foodImages[i] != null && i != draggingFoodIndex) {
                        g2.drawImage(foodImages[i], 50 + (i * 70), 450, 50, 50, this);
                    }
                }
                if (draggingFoodIndex != -1 && dragPoint != null) {
                    g2.drawImage(foodImages[draggingFoodIndex], dragPoint.x - 25, dragPoint.y - 25, 60, 60, this);
                }

                // 상단 UI
                g2.setFont(new Font("맑은 고딕", Font.BOLD, 18));
                g2.setColor(Color.BLACK);
                g2.drawString("Round: " + feedingRound + " / 3", 20, 40);
                
                g2.setColor(Color.GRAY);
                g2.fillRect(160, 25, 240, 15);
                g2.setColor(timeLeft > 15 ? new Color(50, 200, 50) : Color.RED);
                g2.fillRect(160, 25, (int)(240 * (timeLeft / 50.0)), 15);
            }
        };

        MouseAdapter ma = new MouseAdapter() {
            @Override
            public void mousePressed(MouseEvent e) {
                for (int i = 0; i < 5; i++) {
                    Rectangle r = new Rectangle(50 + (i * 70), 450, 50, 50);
                    if (r.contains(e.getPoint())) {
                        draggingFoodIndex = i;
                        dragPoint = e.getPoint();
                        break;
                    }
                }
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                if (draggingFoodIndex != -1) {
                    dragPoint = e.getPoint();
                    gameCanvas.repaint();
                }
            }

            @Override
            public void mouseReleased(MouseEvent e) {
                if (draggingFoodIndex != -1) {
                    Rectangle petArea = new Rectangle(gameCanvas.getWidth()/2 - 60, gameCanvas.getHeight()/2 - 80, 120, 120);
                    if (petArea.contains(e.getPoint())) {
                        if (draggingFoodIndex == targetFoodIndex) {
                            handleSuccess();
                        } else {
                            draggingFoodIndex = -1;
                            dragPoint = null;
                            gameCanvas.repaint();
                        }
                    } else {
                        draggingFoodIndex = -1;
                        dragPoint = null;
                        gameCanvas.repaint();
                    }
                }
            }
        };

        gameCanvas.addMouseListener(ma);
        gameCanvas.addMouseMotionListener(ma);

        if (countDownTimer != null) countDownTimer.stop();
        countDownTimer = new Timer(100, e -> {
            timeLeft--;
            if (timeLeft <= 0) {
                countDownTimer.stop();
                JOptionPane.showMessageDialog(this, "시간이 다 됐어요! 실패...");
                showMenu();
            }
            gameCanvas.repaint();
        });
        countDownTimer.start();

        getContentPane().removeAll();
        add(gameCanvas, BorderLayout.CENTER);
        revalidate();
        repaint();
    }
//음식과 코인 수정
    private void handleSuccess() {
        countDownTimer.stop();
        if (feedingRound >= 3) {
        	int rewardCoins = 10; 
            pet.addCoins(rewardCoins); // 쉬우니까 10개 추가 
            JOptionPane.showMessageDialog(this, "정말 맛있어해요! 훈련 성공!\n보상: " + rewardCoins + "coin 획득 , 먹이추가!");
            
            pet.addFood(2);
            dispose();
        } else {
            feedingRound++;
            startNewFeedingRound();
        }
    }

    private void startMissileGame() {
        getContentPane().removeAll();
        missileHits = 0;
        remainingShots = 7; 
        targetSpeed = 7; 
        isFiring = false;
        isExploding = false;
        shipPos.setLocation(200, 420); 
        targetPos.setLocation(180, 50);  

        gameCanvas = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                Graphics2D g2 = (Graphics2D) g;
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

                g2.setColor(new Color(5, 5, 25)); 
                g2.fillRect(0, 0, getWidth(), getHeight());

                if (shipImage != null) {
                    g2.drawImage(shipImage, shipPos.x - 30, shipPos.y - 30, 60, 60, this);
                } else {
                    g2.drawString("🛸", shipPos.x - 20, shipPos.y);
                }

                if (isFiring) {
                    g2.setColor(new Color(50, 255, 50));
                    g2.setStroke(new BasicStroke(5f));
                    g2.drawLine(shipPos.x, shipPos.y - 30, shipPos.x, 0);
                }

                if (isExploding) {
                    g2.setFont(new Font("Serif", Font.PLAIN, 40));
                    g2.drawString("💥", targetPos.x, targetPos.y + 45);
                } else {
                    BufferedImage currentEnemy = (missileHits < 3) ? enemyImages[missileHits] : null;
                    if (currentEnemy != null) {
                        g2.drawImage(currentEnemy, targetPos.x, targetPos.y, 60, 60, this);
                    }
                }

                g2.setColor(Color.WHITE);
                g2.setFont(new Font("맑은 고딕", Font.BOLD, 15));
                g2.drawString("격추 수: " + missileHits + " / 3", 20, 30);
                
                if (remainingShots <= 2) g2.setColor(Color.RED);
                g2.drawString("남은 탄환: " + remainingShots, 20, 55);
                
                g2.setColor(Color.WHITE);
                g2.setFont(new Font("맑은 고딕", Font.PLAIN, 12));
                g2.drawString("A, D: 이동 | Space: 발사 (탄환 소모)", 20, 80);
            }
        };

        targetMoveTimer = new Timer(30, e -> {
            if (!isExploding) {
                targetPos.x += (targetDirection * targetSpeed);
                if (targetPos.x <= 0 || targetPos.x >= gameCanvas.getWidth() - 60) {
                    targetDirection *= -1;
                }
            }
            gameCanvas.repaint();
        });
        targetMoveTimer.start();
        
        gameKeyListener = new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                int code = e.getKeyCode();
                if (code == KeyEvent.VK_A) shipPos.x = Math.max(30, shipPos.x - 25);
                else if (code == KeyEvent.VK_D) shipPos.x = Math.min(gameCanvas.getWidth() - 30, shipPos.x + 25);
                
                if (code == KeyEvent.VK_SPACE) {
                    if (!isFiring && !isExploding && remainingShots > 0) {
                        remainingShots--; 
                        triggerSequence();
                    }
                }
                gameCanvas.repaint();
            }
        };
        
        this.addKeyListener(gameKeyListener);
        add(gameCanvas, BorderLayout.CENTER);
        revalidate();
        repaint();
        this.setFocusable(true);
        this.requestFocusInWindow();
    }

    private void triggerSequence() {
        isFiring = true;
        gameCanvas.repaint();
        
        boolean isHit = Math.abs(shipPos.x - (targetPos.x + 30)) < 40;

        fireTimer = new Timer(150, e -> {
            isFiring = false;
            if (isHit) {
                isExploding = true;
                gameCanvas.repaint();
                
                explodeTimer = new Timer(400, e2 -> {
                    isExploding = false;
                    missileHits++;
                    if (missileHits >= 3) {
                        endGame("모든 외계인을 해치웠습니다! 임무 성공!", true);
                    } else {
                        targetSpeed += 4;
                        targetPos.x = random.nextInt(300);
                        checkGameOver(); 
                        gameCanvas.repaint();
                    }
                });
                explodeTimer.setRepeats(false);
                explodeTimer.start();
            } else {
                gameCanvas.repaint();
                checkGameOver(); 
            }
        });
        fireTimer.setRepeats(false);
        fireTimer.start();
    }

    private void checkGameOver() {
        if (remainingShots <= 0 && !isExploding && missileHits < 3) {
            Timer failTimer = new Timer(500, e -> {
                JOptionPane.showMessageDialog(this, "탄환이 바닥났습니다! 임무 실패...");
                showMenu();
            });
            failTimer.setRepeats(false);
            failTimer.start();
        }
    }

    private void endGame(String msg, boolean isVictory) {
        stopAllTimers();
        JOptionPane.showMessageDialog(this, msg);
        if (isVictory) {
            // [수정] 코인 100G 지급
            int rewardCoins = 30;
            pet.addCoins(rewardCoins);
            pet.addFood(2);
            System.out.println("외계인 격추 보상: " + rewardCoins + "coin 획득, 먹이추가!");
        }
        dispose();
    }

    private void startCupGame() {
        getContentPane().removeAll();
        JPanel cupPanel = new JPanel(new GridLayout(1, 3, 20, 20));
        JLabel msg = new JLabel("간식이 든 컵을 찾으세요!", SwingConstants.CENTER);
        
        int luckyIndex = random.nextInt(3);
        
        for (int i = 0; i < 3; i++) {
            int current = i;
            JButton cup = new JButton("❓");
            cup.setFont(new Font("맑은 고딕", Font.BOLD, 40));
            
            cup.addActionListener(e -> {
            	// startCupGame 메서드 내부 정답 처리 부분
            	if (current == luckyIndex) {
            	    cup.setText("🍗");
            	    Timer delay = new Timer(500, ev -> {
            	        //맞출 확률이 낮으니까 50개 
            	        int rewardCoins = 50;
            	        pet.addCoins(rewardCoins);
            	        pet.addFood(2);
            	        JOptionPane.showMessageDialog(this, "간식을 찾았습니다!\n보상: " + rewardCoins + "coin 획득, 먹이 획득!");
            	        
            	        dispose(); 
            	    });
            	    delay.setRepeats(false);
            	    delay.start();
            	} else {
                    // ❌ 틀린 경우 (이 부분을 수정했습니다)
                    cup.setText("❌");
                    // "틀렸습니다" 알림창을 띄웁니다.
                    JOptionPane.showMessageDialog(this, "틀렸습니다! ");
                    // 알림창의 '확인'을 누른 후에 메뉴로 돌아갑니다.
                    showMenu(); 
                }
            });
            cupPanel.add(cup);
        }
        
        add(msg, BorderLayout.NORTH); 
        add(cupPanel, BorderLayout.CENTER);
        revalidate(); 
        repaint();
    }
    
    private void stopAllTimers() {
        if (gameTimer != null) gameTimer.stop();
        if (fireTimer != null) fireTimer.stop();
        if (explodeTimer != null) explodeTimer.stop();
        if (targetMoveTimer != null) targetMoveTimer.stop();
        if (countDownTimer != null) countDownTimer.stop();
    }
}