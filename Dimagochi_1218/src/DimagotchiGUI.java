import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

import javax.swing.Timer;
import java.io.IOException; 
import javax.imageio.ImageIO; 
import java.util.Map;
import java.util.HashMap;
import java.util.List;          
import java.util.ArrayList;    
import java.awt.event.MouseAdapter; 
import java.awt.event.MouseEvent;  

public class DimagotchiGUI extends JFrame {

	private static final long serialVersionUID = 1L;
    private JPanel mainPanel; 
    private final Map<String, ItemInfo> itemMap = new HashMap<>();
	private Dimagotchi pet;
    
    private JLabel imageLabel;
    private JLabel statusLabel;
    
    private JButton btnFeed, btnPlay, btnSleep, btnClean; 
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
    
    private JPanel coinPanel; //  코인 패널
    private JLabel coinLabel; //  코인 라벨
    
    private JPanel crystalBallPanel; // 수정구 패널 (운세 시스템)
    private JPanel tvPanel; //  TV 패널 (미니게임)
    
    private List<Fly> flyList = new ArrayList<>();
    private Timer flySpawnTimer;

    //가구배치
    private furniture.FurnitureType pendingType = null; // 구매 후 배치를 기다리는 타입

    private double tvAnimAngle = 0; // TV 애니메이션용 각도

    private double crystalballAnimAngle = 0; // crystalballAnimAngle 애니메이션용 각도

    
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
        // 1. ShopDialog 인스턴스 생성
        // 2. 람다식을 통해 선택된 타입(type)을 받아서 처리 로직만 작성
        new ShopDialog(this, (selectedType) -> {
            this.pendingType = selectedType;
            statusLabel.setText("🏠 [" + selectedType.name() + "]를 배치할 곳을 클릭하세요!");
        }).setVisible(true);
    }

    private void purchaseItem(String itemId, int price) {
        ItemInfo info = itemMap.get(itemId);
        if (info == null) return;
        
        // 코인 체크 및 차감
        if (!pet.spendCoins(price)) {
            JOptionPane.showMessageDialog(this, "코인이 부족합니다! 현재 코인: " + pet.getCoins() + "G", "구매 실패", JOptionPane.WARNING_MESSAGE);
            return;
        }

        addItemToBackground(info);
        updateUI(); // 코인 표시 업데이트
        
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
        
        // 포춘쿠키 패널 초기화 (보이지 않게 설정)
        fortuneCookiePanel = new JPanel();
        fortuneCookiePanel.setLayout(null);
        fortuneCookiePanel.setSize(80, 80);
        fortuneCookiePanel.setOpaque(false);
        fortuneCookiePanel.setVisible(false);
        mainPanel.add(fortuneCookiePanel);

        // 운세 종이 패널 초기화 (보이지 않게 설정)
        fortunePaperPanel = new JPanel();
        fortunePaperPanel.setLayout(null);
        fortunePaperPanel.setSize(400, 300);
        fortunePaperPanel.setOpaque(false);
        fortunePaperPanel.setVisible(false);
        mainPanel.add(fortunePaperPanel);
        
        JPanel statsPanel = new JPanel();
        statsPanel.setLayout(new GridLayout(6, 1, 5, 5));
        statsPanel.setBounds(20, 20, 250, 150); 
        statsPanel.setOpaque(false);
       
     
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
        
        // 코인 패널 초기화 (우측 상단)
        coinPanel = new JPanel();
        coinPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
        coinPanel.setBounds(580, 20, 200, 50);
        coinPanel.setOpaque(true);
        coinPanel.setBackground(new Color(255, 248, 220));
        coinPanel.setBorder(BorderFactory.createLineBorder(new Color(218, 165, 32), 3, true));
        
        try {
            java.net.URL coinImgURL = getClass().getResource("/res/Coin.png");
            if (coinImgURL != null) {
                BufferedImage coinImg = ImageIO.read(coinImgURL);
                Image scaledCoin = coinImg.getScaledInstance(35, 35, Image.SCALE_SMOOTH);
                JLabel coinIcon = new JLabel(new ImageIcon(scaledCoin));
                coinPanel.add(coinIcon);
            }
        } catch (IOException e) {
            System.err.println("코인 이미지 로드 실패: " + e.getMessage());
        }
        
        coinLabel = new JLabel("" + pet.getCoins());
        coinLabel.setFont(new Font("맑은 고딕", Font.BOLD, 24));
        coinLabel.setForeground(new Color(184, 134, 11));
        coinPanel.add(coinLabel);
        
        mainPanel.add(coinPanel);
        
        // 수정구 패널 초기화 (왼쪽 중간, 상태바 아래)
        crystalBallPanel = new JPanel();
        crystalBallPanel.setLayout(new BorderLayout());
        crystalBallPanel.setBounds(40, 200, 70, 70);
        crystalBallPanel.setOpaque(false);
        crystalBallPanel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        try {
            java.net.URL crystalImgURL = getClass().getResource("/res/Crystalball.png");
            if (crystalImgURL != null) {
                BufferedImage crystalImg = ImageIO.read(crystalImgURL);
                Image scaledCrystal = crystalImg.getScaledInstance(70, 70, Image.SCALE_SMOOTH);
                JLabel crystalIcon = new JLabel(new ImageIcon(scaledCrystal));
                crystalBallPanel.add(crystalIcon, BorderLayout.CENTER);
            }
        } catch (IOException e) {
            System.err.println("수정구 이미지 로드 실패: " + e.getMessage());
        }
        
        crystalBallPanel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                onCrystalBallClicked();
            }
        });
        
     // --- 크리스탈볼 둥둥 떠다니는 애니메이션 시작 ---
        // 기존 크리스탈볼의 시작 위치 기억 (620, 150)
        final int crystaloriginX = 40;
        final int crystaloriginY = 200;

        Timer crystalFloatTimer = new Timer(30, e -> {
            // 각도를 조금씩 증가시켜 사인파(Sine Wave)를 만듭니다.
            crystalballAnimAngle += 0.1; 
            
            // 위아래로 움직이는 폭을 10픽셀 정도로 설정
            int yOffset = (int) (Math.sin(crystalballAnimAngle) * 8);
            
            // 크리스탈볼 패널의 위치를 실시간으로 변경
            crystalBallPanel.setLocation(crystaloriginX, crystaloriginY + yOffset);
        });
        crystalFloatTimer.start();
        // --- 애니메이션 끝 ---
        
        mainPanel.add(crystalBallPanel);
        

        
        // TV 패널 초기화 (오른쪽 중간, 코인 아래)
        tvPanel = new JPanel();
        tvPanel.setLayout(new BorderLayout());
        tvPanel.setBounds(620, 150, 120, 100);
        tvPanel.setOpaque(false);
        tvPanel.setCursor(new Cursor(Cursor.HAND_CURSOR));
        
        try {
            java.net.URL tvImgURL = getClass().getResource("/res/TV.png");
            if (tvImgURL != null) {
                BufferedImage tvImg = ImageIO.read(tvImgURL);
                Image scaledTV = tvImg.getScaledInstance(120, 100, Image.SCALE_SMOOTH);
                JLabel tvIcon = new JLabel(new ImageIcon(scaledTV));
                tvPanel.add(tvIcon, BorderLayout.CENTER);
            }
        } catch (IOException e) {
            System.err.println("TV 이미지 로드 실패: " + e.getMessage());
        }
        
        tvPanel.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                onTVClicked();
            }
        });
        
        
        mainPanel.add(tvPanel);
        
        JPanel btnPanel = new JPanel();
        btnPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 10, 10)); // 간격 조정
        // 버튼 4개가 들어가도록 패널 너비 조정 (450 -> 560) 및 위치 조정 (173 -> 120)
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
        
     // --- TV 둥둥 떠다니는 애니메이션 시작 ---
        // 기존 TV의 시작 위치 기억 (620, 150)
        final int tvoriginX = 620;
        final int tvoriginY = 150;

        Timer tvFloatTimer = new Timer(45, e -> {
            // 각도를 조금씩 증가시켜 사인파(Sine Wave)를 만듭니다.
            tvAnimAngle += 0.07; 
            
            // 위아래로 움직이는 폭을 10픽셀 정도로 설정
            int yOffset = (int) (Math.sin(tvAnimAngle) * 5);
            
            // TV 패널의 위치를 실시간으로 변경
            tvPanel.setLocation(tvoriginX, tvoriginY + yOffset);
        });
        tvFloatTimer.start();
        
        
        add(mainPanel, BorderLayout.CENTER);
        
     // --- 여기서부터 가구 배치용 마우스 리스너 코드 시작 ---
        mainPanel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                // 상점에서 가구를 선택한 상태(pendingType이 null이 아님)일 때만 작동
                if (pendingType != null) {
                    // 1. 가구 객체 생성 및 이미지 잘라오기
                    furniture newFurn = new furniture(pendingType, e.getX(), e.getY());
                 // ⭐️ 2배 커진 이미지를 가져옵니다.
                    Image scaledImg = newFurn.getScaledImage(3);
               
                    if (scaledImg != null) {
                        JLabel furnLabel = new JLabel(new ImageIcon(scaledImg));
                        
                        // 2. 가구 위치 설정 (클릭한 지점이 가구의 중앙 하단이 되도록 배치)
                     // ⭐️ 중요: 레이블의 크기도 2배로 계산해야 합니다.
                        int fw = newFurn.getWidth() * 3;
                        int fh = newFurn.getHeight() * 3;
                        
                        furnLabel.setBounds(e.getX() - (fw / 2), e.getY() - (fh / 2), fw, fh);
                        
                        //가구 드래그 기능 추가 
                        final Point[] offset = new Point[1]; // 클릭 지점과 가구 왼쪽 상단 사이의 거리 저장
                        furnLabel.addMouseListener(new java.awt.event.MouseAdapter() {
                            @Override
                            public void mousePressed(java.awt.event.MouseEvent me) {
                                // 가구를 누르는 순간, 마우스와 가구 모서리 사이의 간격을 계산
                                offset[0] = me.getPoint();
                                // 드래그 중임을 알리기 위해 테두리를 임시로 표시할 수도 있습니다.
                                furnLabel.setBorder(BorderFactory.createLineBorder(Color.CYAN, 1));
                            }

                            @Override
                            public void mouseReleased(java.awt.event.MouseEvent me) {
                                // 마우스를 떼면 테두리 제거
                                furnLabel.setBorder(null);
                            }
                        });
                        
                        furnLabel.addMouseMotionListener(new java.awt.event.MouseMotionAdapter() {
                            @Override
                            public void mouseDragged(java.awt.event.MouseEvent me) {
                                if (offset[0] != null) {
                                    // 현재 마우스 위치에서 처음 클릭한 오프셋만큼 뺀 위치로 이동
                                    int newX = furnLabel.getX() + me.getX() - offset[0].x;
                                    int newY = furnLabel.getY() + me.getY() - offset[0].y;
                                    
                                    furnLabel.setLocation(newX, newY);
                                }
                            }
                        });
                        //가구 드래그 기능 추가 끝
                        

                        // 3. 메인 패널에 가구 추가
                        mainPanel.add(furnLabel);
                        
                        // 4. 레이어 순서(Z-Order) 조정 
                        // 현재 코드상 shopTabPanel이 6번이므로, 가구는 7번에 넣으면 배경(맨 뒤) 바로 위에 옵니다.
                        mainPanel.setComponentZOrder(furnLabel, 7);

                        // 5. 상태 초기화 및 화면 갱신
                        pendingType = null; 
                        statusLabel.setText("가구 배치 성공! 선택해서 옮길 수 있습니다!");
                        
                        mainPanel.revalidate();
                        mainPanel.repaint();
                    }
                }
            }
        });
        // --- 여기까지 ---

        add(mainPanel, BorderLayout.CENTER); // 
        
     // 💡 [추가] 벌레 스폰 타이머 (10초마다 확률적으로 생성)
        flySpawnTimer = new Timer(10000, e -> {
            if (Dimagotchi.isAliveStatic()) {
                // 30% 확률로 벌레 등장 (최대 5마리 제한)
                if (Math.random() < 0.3 && flyList.size() < 5) {
                    spawnVisualFly();
                }
            }
        });
        flySpawnTimer.start();

        movementTimer = new Timer(100, e -> { // 이동 속도 조정 (1000 -> 100)
            if (Dimagotchi.isAliveStatic()) {
                 // 기존 캐릭터 업데이트 (느리게 하기 위해 카운터 사용 가능하지만, 일단 둡니다)
                 // 캐릭터가 너무 빨리 움직이면 여기를 조정하세요.
                 pet.getCharacter().updateMovement();
                 
                 // 벌레 움직임 업데이트
                 updateFlies();
                 
                 updateBackground(); 
            }
            updateUI(); 
        });
        movementTimer.start(); 
    }
    
    // 벌레 생성 및 GUI 추가 메서드
    private void spawnVisualFly() {
        Fly fly = new Fly();
        
        // 벌레 클릭(잡기) 이벤트 리스너
        fly.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                // 잡기 로직
                String msg = pet.catchFly(); // 데이터 처리
                statusLabel.setText(msg);
                
                // 화면에서 제거
                mainPanel.remove(fly);
                flyList.remove(fly);
                mainPanel.repaint();
            }
        });
        
        flyList.add(fly);
        mainPanel.add(fly);
        pet.addFly(); // 데이터에 벌레 추가
        
        // Z-Order 조정 (캐릭터보다 위에 오도록)
        mainPanel.setComponentZOrder(fly, 0); 
        
        statusLabel.setText("윙윙~ 벌레가 나타났어요! 클릭해서 잡으세요!");
        mainPanel.repaint();
    }
    
    // 벌레 위치 업데이트 메서드
    private void updateFlies() {
        for (Fly fly : flyList) {
            fly.updatePosition();
        }
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
            case 4: 
                message = pet.clean(); 
                break; 
        }
     //  진화 메시지가 있으면 포춘쿠키 표시
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

    // 포춘쿠키 표시 메서드
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
                
                // 화면 중앙 하단 고정 위치에 배치 (조명 오른쪽)
                int cookieX = (mainPanel.getWidth() / 2) + 20; // 중앙에서 살짝 오른쪽
                int cookieY = mainPanel.getHeight() - 180; // 하단에서 180px 위
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

    //  포춘쿠키 클릭 시
    private void onFortuneCookieClicked() {
        if (!fortuneCookieActive) return;
        
        fortuneCookiePanel.setVisible(false);
        fortuneCookieActive = false;
        
        // 캐릭터 잠깐 숨기기 (종이와 겹치지 않게)
        imageLabel.setVisible(false);
        
        showFortunePaper();
    }

    // 운세 종이 표시 메서드
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

    // 운세 종이 닫기
    private void onFortunePaperClicked() {
        if (!fortunePaperActive) return;
        
        fortunePaperPanel.setVisible(false);
        fortunePaperActive = false;
        
        // 캐릭터 다시 보이기
        imageLabel.setVisible(true);
    }

    //  랜덤 운세 생성
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
    
    // 수정구 클릭 이벤트 - 운세 확인 다이얼로그
    private void onCrystalBallClicked() {
        int choice = JOptionPane.showOptionDialog(
            this,
            "운세를 확인할까요?",
            "수정구",
            JOptionPane.YES_NO_OPTION,
            JOptionPane.QUESTION_MESSAGE,
            null,
            new Object[]{"네", "아니요"},
            "네"
        );
        
        if (choice == JOptionPane.YES_OPTION) {
            String fortune = generateFortune();
            JOptionPane.showMessageDialog(
                this,
                fortune,
                "오늘의 운세",
                JOptionPane.INFORMATION_MESSAGE
            );
        }
    }
    
    // TV 클릭 이벤트 - 미니게임 (추후 구현 예정)
    private void onTVClicked() {
        JOptionPane.showMessageDialog(
            this,
            "미니게임 추가 예정",
            "TV",
            JOptionPane.INFORMATION_MESSAGE
        );
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
        
        // 만약 청결도가 낮다면 행복도를 깎아서 보여주는 시각적 효과 (선택사항)
        // 여기서는 데이터 자체를 passTime에서 깎으므로 그대로 표시합니다.
        happinessGauge.setCurrentValue(currentHappiness); 

        int currentEnergy = pet.getEnergy();
        energyGauge.setCurrentValue(currentEnergy);
        
        // 코인 라벨 업데이트
        if (coinLabel != null) {
            coinLabel.setText("" + pet.getCoins());
        }

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
            flySpawnTimer.stop();
            
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