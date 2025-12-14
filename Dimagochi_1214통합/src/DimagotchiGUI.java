import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

import javax.swing.Timer;
import java.io.IOException; 
import javax.imageio.ImageIO; 
import java.util.Map;
import java.util.HashMap;

public class DimagotchiGUI extends JFrame {

	private static final long serialVersionUID = 1L;
    private JPanel mainPanel; 
    private final Map<String, ItemInfo> itemMap = new HashMap<>();
	private Dimagotchi pet;
    
    private JLabel imageLabel;
    private JLabel statusLabel;
    
    private JButton btnFeed, btnPlay, btnSleep, btnClean; // 💡 [추가] btnClean
    private JTabbedPane actionTabs;

    private StatusGaugePanel hungerGauge;
    private StatusGaugePanel happinessGauge; 
    private StatusGaugePanel energyGauge; 
    
    private Timer movementTimer; 
    
    private JLabel backgroundLabel; 
    
    private String currentBackgroundPath = ""; 
    
    private JPanel fortuneCookiePanel; // 포춘쿠키 패널
    private JPanel fortunePaperPanel; // 운세 종이 패널
    private boolean fortuneCookieActive = false; // 포춘쿠키 활성화 여부
    private boolean fortunePaperActive = false; // 운세 종이 활성화 여부

    public DimagotchiGUI() {
        String name = JOptionPane.showInputDialog("다마고치 이름을 입력하세요:");
        if (name == null || name.trim().isEmpty()) name = "다마고치";
        pet = new Dimagotchi(name);

        setTitle("Dimagochi");
        setSize(800, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setResizable(false);
        setLocationRelativeTo(null);

        initUI();

        setVisible(true);
        updateUI();
        initializeItemData();
    }
    
    private static class ItemInfo {
        String imagePath;
        int x;
        int y;

        public ItemInfo(String imagePath, int x, int y) {
            this.imagePath = imagePath;
            this.x = x;
            this.y = y;
        }
    }
    private void initializeItemData() {
        itemMap.put("bed", new ItemInfo("/res/bed.png", 50, 250)); 
    }

    private void openShopDialog() {
        JDialog shopDialog = new JDialog(this, "다마고치 상점", true);
        shopDialog.setSize(400, 300);
        shopDialog.setLocationRelativeTo(this);

        JPanel shopPanel = new JPanel();
        shopPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 20, 20));

        JButton itemBed = new JButton("푹신한 침대 (100G)");
        itemBed.addActionListener(e -> {
            purchaseItem("bed", 100);
            shopDialog.dispose(); 
        });
        shopPanel.add(itemBed);

        shopDialog.add(shopPanel);
        shopDialog.setVisible(true);
    }

    private void purchaseItem(String itemId, int price) {
        ItemInfo info = itemMap.get(itemId);
        if (info == null) return;

        addItemToBackground(info);
        
        JOptionPane.showMessageDialog(this, info.imagePath + "를 구매했습니다! 배경에 배치됩니다.");
    }

    private void addItemToBackground(ItemInfo info) {
        try {
            ImageIcon icon = new ImageIcon(getClass().getResource(info.imagePath));
            if (icon.getImageLoadStatus() != MediaTracker.COMPLETE) {
                 throw new IOException("Failed to load image: " + info.imagePath);
            }
            
            JLabel itemLabel = new JLabel(icon);
            itemLabel.setBounds(info.x, info.y, icon.getIconWidth(), icon.getIconHeight());

            mainPanel.add(itemLabel);
            mainPanel.setComponentZOrder(itemLabel, 5);
            mainPanel.setComponentZOrder(backgroundLabel, mainPanel.getComponentCount() - 1);

            mainPanel.revalidate();
            mainPanel.repaint();

        } catch (Exception e) {
            System.err.println("아이템 이미지 로드 실패: " + info.imagePath);
            e.printStackTrace();
        }
    }
    private void initUI() {
        mainPanel = new JPanel();
        mainPanel.setLayout(null); 
        mainPanel.setPreferredSize(new Dimension(800, 500));
        
        backgroundLabel = new JLabel();
        backgroundLabel.setBounds(0, 0, 800, 500);
        mainPanel.add(backgroundLabel);
        
        imageLabel = new JLabel();
        imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        imageLabel.setVerticalAlignment(SwingConstants.CENTER);
        imageLabel.setSize(100, 100);
        mainPanel.add(imageLabel);
        
        // [추가] 포춘쿠키 패널 초기화 (보이지 않게 설정)
        fortuneCookiePanel = new JPanel();
        fortuneCookiePanel.setLayout(null);
        fortuneCookiePanel.setSize(80, 80);
        fortuneCookiePanel.setOpaque(false);
        fortuneCookiePanel.setVisible(false);
        mainPanel.add(fortuneCookiePanel);

        // [추가] 운세 종이 패널 초기화 (보이지 않게 설정)
        fortunePaperPanel = new JPanel();
        fortunePaperPanel.setLayout(null);
        fortunePaperPanel.setSize(400, 300);
        fortunePaperPanel.setOpaque(false);
        fortunePaperPanel.setVisible(false);
        mainPanel.add(fortunePaperPanel);
        
        JPanel statsPanel = new JPanel();
        statsPanel.setLayout(new GridLayout(6, 1, 5, 5));
        statsPanel.setBounds(20, 20, 250, 150); 
        statsPanel.setOpaque(true);
        statsPanel.setBackground(Color.WHITE); 
        statsPanel.setBorder(BorderFactory.createLineBorder(new Color(200, 200, 200), 2, true));
     
        hungerGauge = new StatusGaugePanel(); 
        hungerGauge.setPreferredSize(new Dimension(180, 25));
        
        happinessGauge = new StatusGaugePanel(); 
        happinessGauge.setCurrentColorIndex(1); 
        happinessGauge.setPreferredSize(new Dimension(180, 25));
        
        energyGauge = new StatusGaugePanel(); 
        energyGauge.setCurrentColorIndex(2); 
        energyGauge.setPreferredSize(new Dimension(180, 25));
        
        JPanel hungerLabel = new LabelWithBackgroundPanel("/res/button2.png", "포만감");
        hungerLabel.setPreferredSize(new Dimension(250, 25)); 
                
        JPanel happinessLabel = new LabelWithBackgroundPanel("/res/button2.png", "행복도");
        happinessLabel.setPreferredSize(new Dimension(250, 25));

        JPanel energyLabel = new LabelWithBackgroundPanel("/res/button2.png", "에너지");
        energyLabel.setPreferredSize(new Dimension(250, 25));

        statsPanel.add(hungerLabel);
        statsPanel.add(hungerGauge); 
        
        statsPanel.add(happinessLabel);
        statsPanel.add(happinessGauge); 
        
        statsPanel.add(energyLabel);
        statsPanel.add(energyGauge);   
        
        mainPanel.add(statsPanel);
        
        JPanel btnPanel = new JPanel();
        btnPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 10)); // 간격 조정
        // 💡 [수정] 버튼 4개가 들어가도록 패널 너비 조정 (450 -> 560) 및 위치 조정 (173 -> 120)
        btnPanel.setBounds(120, 400, 560, 60); 
        btnPanel.setOpaque(false);

        btnFeed = new JButton("밥주기 🍖");
        btnPlay = new JButton("산책하기 🎾");
        btnSleep = new JButton("잠자기 💤");
        btnClean = new JButton("청소하기 🧹");
        
        styleButton(btnFeed);
        styleButton(btnPlay);
        styleButton(btnSleep);
        styleButton(btnClean);

        btnFeed.addActionListener(e -> performAction(1));
        btnPlay.addActionListener(e -> performAction(2));
        btnSleep.addActionListener(e -> performAction(3));
        btnClean.addActionListener(e -> performAction(4));

        btnPanel.add(btnFeed);
        btnPanel.add(btnPlay);
        btnPanel.add(btnSleep);
        btnPanel.add(btnClean); 
        
        mainPanel.add(btnPanel);
        
        // 상점 버튼 패널
        JPanel shopTabPanel = new JPanel();
        shopTabPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5)); 
        shopTabPanel.setOpaque(false); 
        
        shopTabPanel.setBounds(650, 415, 120, 40); 
        
        JButton btnShop = new JButton("상점 🛒");
        styleButton(btnShop); 
        btnShop.setPreferredSize(new Dimension(100, 25)); 

        btnShop.addActionListener(e -> openShopDialog()); 
        shopTabPanel.add(btnShop);

        mainPanel.add(shopTabPanel);

        // 상태 메시지 라벨 (투명화 & 위치 상단 이동)
        statusLabel = new JLabel("다마고치가 태어났습니다!", SwingConstants.CENTER);
        statusLabel.setBounds(200, 20, 400, 40); 
        statusLabel.setFont(new Font("맑은 고딕", Font.BOLD, 16)); 
        statusLabel.setForeground(Color.BLACK); 
        
        statusLabel.setOpaque(false); 
        statusLabel.setBorder(null); 
        
        mainPanel.add(statusLabel);

        mainPanel.setComponentZOrder(fortunePaperPanel, 0); 
        mainPanel.setComponentZOrder(fortuneCookiePanel, 1);
        mainPanel.setComponentZOrder(imageLabel, 2);
        mainPanel.setComponentZOrder(statsPanel, 3);
        mainPanel.setComponentZOrder(btnPanel, 4);
        mainPanel.setComponentZOrder(statusLabel, 5);
        mainPanel.setComponentZOrder(shopTabPanel, 6); 
        mainPanel.setComponentZOrder(backgroundLabel, mainPanel.getComponentCount() - 1);
        
        add(mainPanel, BorderLayout.CENTER);

        movementTimer = new Timer(1000, e -> {
            if (Dimagotchi.isAliveStatic()) {
                 pet.getCharacter().updateMovement();
                 updateBackground(); 
            }
            updateUI(); 
        });
        movementTimer.start(); 
    }
    
    private void styleButton(JButton button) {
        button.setFont(new Font("맑은 고딕", Font.BOLD, 14));
        button.setPreferredSize(new Dimension(120, 40));
        button.setFocusPainted(false);
        button.setBackground(Color.WHITE); 
        button.setOpaque(true);
        button.setBorder(BorderFactory.createLineBorder(new Color(150, 150, 150), 2, true));
        button.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    private JProgressBar createBar(Color color) {
        JProgressBar bar = new JProgressBar(0, 100);
        bar.setValue(50);
        bar.setStringPainted(true);
        bar.setForeground(color);
        return bar;
    }

    private void performAction(int actionType) {
        String message = "";
        switch (actionType) {
            case 1: message = pet.feed(); break;
            case 2: message = pet.play(); break;
            case 3: message = pet.sleep(); break;
            case 4: message = pet.clean(); break; 
        }
     // 💡 [수정] 진화 메시지가 있으면 포춘쿠키 표시
        String evolutionMsg = pet.getEvolutionMessage();
        if (!evolutionMsg.isEmpty()) {
            message = "<html><center>" + message + "<br><span style='color:magenta;font-weight:bold;'>" + evolutionMsg + "</span></center></html>";
            pet.resetEvolutionMessage(); 
            
            // 진화 시 포춘쿠키 등장!
            showFortuneCookie();
        }
        
        statusLabel.setText(message);
        updateUI();
    }

    // 💡 [추가] 포춘쿠키 표시 메서드
    private void showFortuneCookie() {
        try {
            java.net.URL imgURL = getClass().getResource("/res/fortunecookie.png"); // 경로 주의 (/res)
            if (imgURL != null) {
                BufferedImage img = ImageIO.read(imgURL);
                Image scaledImg = img.getScaledInstance(80, 80, Image.SCALE_SMOOTH);
                
                // 기존 패널 제거하고 새로 생성 (이벤트 리스너 중복 방지 및 이미지 갱신)
                mainPanel.remove(fortuneCookiePanel);
                
                fortuneCookiePanel = new JPanel() {
                    @Override
                    protected void paintComponent(Graphics g) {
                        super.paintComponent(g);
                        g.drawImage(scaledImg, 0, 0, this);
                    }
                };
                fortuneCookiePanel.setLayout(null);
                fortuneCookiePanel.setSize(80, 80);
                fortuneCookiePanel.setOpaque(false);
                fortuneCookiePanel.setCursor(new Cursor(Cursor.HAND_CURSOR));
                fortuneCookiePanel.addMouseListener(new java.awt.event.MouseAdapter() {
                    public void mouseClicked(java.awt.event.MouseEvent evt) {
                        onFortuneCookieClicked();
                    }
                });
                
                // 캐릭터 오른쪽 옆에 배치
                int cookieX = pet.getXPos() + 120;
                int cookieY = pet.getYPos() + 10;
                fortuneCookiePanel.setLocation(cookieX, cookieY);
                
                mainPanel.add(fortuneCookiePanel);
                mainPanel.setComponentZOrder(fortuneCookiePanel, 0); // 최상단 배치
                
                fortuneCookiePanel.setVisible(true);
                fortuneCookieActive = true;
                
                mainPanel.revalidate();
                mainPanel.repaint();
            }
        } catch (IOException e) {
            System.err.println("포춘쿠키 이미지 로드 실패: " + e.getMessage());
        }
    }

    // 💡 [추가] 포춘쿠키 클릭 시
    private void onFortuneCookieClicked() {
        if (!fortuneCookieActive) return;
        
        fortuneCookiePanel.setVisible(false);
        fortuneCookieActive = false;
        
        // 캐릭터 잠깐 숨기기 (종이와 겹치지 않게)
        imageLabel.setVisible(false);
        
        showFortunePaper();
    }

    // 💡 [추가] 운세 종이 표시 메서드
    private void showFortunePaper() {
        try {
            java.net.URL imgURL = getClass().getResource("/res/fortune.png"); // 경로 주의
            if (imgURL != null) {
                BufferedImage img = ImageIO.read(imgURL);
                Image scaledImg = img.getScaledInstance(400, 300, Image.SCALE_SMOOTH);
                String fortune = generateFortune();
                
                mainPanel.remove(fortunePaperPanel); // 리셋

                fortunePaperPanel = new JPanel() {
                    @Override
                    protected void paintComponent(Graphics g) {
                        super.paintComponent(g);
                        Graphics2D g2d = (Graphics2D) g;
                        // 텍스트 품질 향상
                        g2d.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                        g2d.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
                        
                        g2d.drawImage(scaledImg, 0, 0, this);
                        
                        // 텍스트 회전 (종이 기울기에 맞춰)
                        double angle = Math.toRadians(-8); 
                        int centerX = 200;
                        int centerY = 150;
                        
                        java.awt.geom.AffineTransform oldTransform = g2d.getTransform();
                        g2d.rotate(angle, centerX, centerY);
                        
                        g2d.setColor(new Color(44, 24, 16)); 
                        g2d.setFont(new Font("맑은 고딕", Font.BOLD, 18));
                        
                        FontMetrics fm = g2d.getFontMetrics();
                        int textWidth = fm.stringWidth(fortune);
                        int x = centerX - (textWidth / 2);
                        int y = centerY;
                        
                        g2d.drawString(fortune, x, y);
                        g2d.setTransform(oldTransform);
                    }
                };
                fortunePaperPanel.setLayout(null);
                fortunePaperPanel.setSize(400, 300);
                fortunePaperPanel.setOpaque(false);
                fortunePaperPanel.setCursor(new Cursor(Cursor.HAND_CURSOR));
                fortunePaperPanel.addMouseListener(new java.awt.event.MouseAdapter() {
                    public void mouseClicked(java.awt.event.MouseEvent evt) {
                        onFortunePaperClicked();
                    }
                });
                
                // 화면 중앙 배치
                fortunePaperPanel.setLocation(200, 100);
                mainPanel.add(fortunePaperPanel);
                mainPanel.setComponentZOrder(fortunePaperPanel, 0); // 최상단
                
                fortunePaperPanel.setVisible(true);
                fortunePaperActive = true;
                mainPanel.revalidate();
                mainPanel.repaint();
            }
        } catch (IOException e) {
            System.err.println("운세 종이 이미지 로드 실패: " + e.getMessage());
        }
    }

    // 💡 [추가] 운세 종이 닫기
    private void onFortunePaperClicked() {
        if (!fortunePaperActive) return;
        
        fortunePaperPanel.setVisible(false);
        fortunePaperActive = false;
        
        // 캐릭터 다시 보이기
        imageLabel.setVisible(true);
    }

    // 💡 [추가] 랜덤 운세 생성
    private String generateFortune() {
        String[] fortunes = {
            "오늘은 행운이 가득한 날입니다!",
            "새로운 친구를 만날 수 있어요.",
            "맛있는 간식이 당신을 기다립니다.",
            "오늘은 산책하기 좋은 날씨예요.",
            "당신의 미소가 모두를 행복하게 해요.",
            "곧 좋은 소식이 있을 거예요!",
            "오늘은 충분히 쉬어가세요.",
            "당신은 특별한 존재입니다!",
            "사랑과 관심이 당신을 감싸줄 거예요.",
            "건강한 하루 되세요!",
            "즐거운 시간이 당신을 기다려요.",
            "행복은 작은 것에서 시작됩니다."
        };
        java.util.Random random = new java.util.Random();
        return fortunes[random.nextInt(fortunes.length)];
    }
    
    private ImageIcon loadScaledBackground(String path) {
        try {
            java.net.URL imgURL = getClass().getResource(path);
            if (imgURL == null) {
                System.err.println("배경 이미지 파일을 찾을 수 없습니다: " + path);
                return null;
            }
            Image originalImage = ImageIO.read(imgURL);
            Image scaledImage = originalImage.getScaledInstance(
                800, 500, Image.SCALE_SMOOTH
            );
            return new ImageIcon(scaledImage);
        } catch (IOException e) {
            System.err.println("배경 이미지를 로드하는 데 실패했습니다.");
            e.printStackTrace();
            return null;
        }
    }
    
    private void updateBackground() {
        String newPath = TimeManager.getBackgroundImagePath();
        if (!newPath.equals(currentBackgroundPath)) {
            ImageIcon icon = loadScaledBackground(newPath);
            if (icon != null) {
                backgroundLabel.setIcon(icon);
                currentBackgroundPath = newPath;
            }
        }
    }

    private void updateUI() {
        updateBackground(); 
        
        int currentHunger = pet.getHunger();
        int fullness = 100 - currentHunger;
        hungerGauge.setCurrentValue(fullness); 
        
        int currentHappiness = pet.getHappiness();
        happinessGauge.setCurrentValue(currentHappiness); 

        int currentEnergy = pet.getEnergy();
        energyGauge.setCurrentValue(currentEnergy);

        Image currentImage = pet.getCharacter().getCurrentImage();
        
        if (currentImage != null) {
            ImageIcon icon = new ImageIcon(currentImage);
            imageLabel.setIcon(icon);
            imageLabel.setText(""); 
        } else {
            imageLabel.setIcon(null);
            imageLabel.setText("☠️"); 
        }
        
        int x = pet.getXPos();
        int y = pet.getYPos();
        imageLabel.setLocation(x, y);
        
        if (!Dimagotchi.isAliveStatic()) {
            movementTimer.stop();
            if (!statusLabel.getText().contains("밥을 먹지 않습니다")
                    && !statusLabel.getText().contains("반응이 없습니다")
                    && !statusLabel.getText().contains("영원히 잠들었습니다")) {

                statusLabel.setText("<html>다마고치가 무지개 다리를 건넜습니다.<br>"
                                    + pet.getCauseOfDeath() + "</html>");
            }
        }
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new DimagotchiGUI();
        });
    } 
}