import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;

import javax.swing.Timer;
import java.io.IOException; 
import javax.imageio.ImageIO; 
import java.util.Map;
import java.util.Random;
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
    
    private List<JLabel> poopList = new ArrayList<>();
    
    private JButton btnFeed, btnPlay, btnSleep, btnClean; 
    private JTabbedPane actionTabs;

    private StatusGaugePanel hungerGauge;
    private StatusGaugePanel happinessGauge; 
    private StatusGaugePanel energyGauge; 
    private StatusGaugePanel cleanlinessGauge; 
   
    
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
    private Emoji emojiOverlay = new Emoji(Emoji.EmojiType.EMO1);

 
    private boolean isWaiting = false; // 캐릭터가 멈춰있는지 확인하는 변수
    
    private Emoji petEmoji; // 이모지 객체 변수 선언
    
 // DimagotchiGUI 클래스 상단 변수 선언
    private JLabel emojiBubbleLabel;
    
    
    private SoundPlayer soundPlayer; // 사운드 플레이어 객체 선언
    
    
    
    public DimagotchiGUI() {
        String name = JOptionPane.showInputDialog("다마고치 이름을 입력하세요:");
        if (name == null || name.trim().isEmpty()) name = "다마고치";
        pet = new Dimagotchi(name);

        
     //  Emoji 객체를 생성 (초기값은 행복한 표정 EMO1)
        this.petEmoji = new Emoji(Emoji.EmojiType.EMO1);
        
        setTitle("Dimagochi");
        setSize(800, 500);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        setResizable(false);
        setLocationRelativeTo(null);

        //사운드 추가 생성자 
        soundPlayer = new SoundPlayer();
        soundPlayer.playBGM("/res/dimagochi.wav");
        
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

 // DimagotchiGUI.java 내부
    private void openShopDialog() {
        ShopDialog shop = new ShopDialog(this, (selectedType) -> {
        	// 💡 ShopDialog 클래스명으로 getPrice를 호출합니다.
        	int price = ShopDialog.getPrice(selectedType);

            // 2. 코인 체크
            if (pet.getCoins() >= price) {
                pet.spendCoins(price); // 코인 차감
                pendingType = selectedType; // 가구 배치 대기 상태로 전환
                statusLabel.setText(price + "G를 사용하여 가구를 샀습니다! 배치할 곳을 클릭하세요.");
                updateUI(); // 코인 라벨 갱신
            } else {
                JOptionPane.showMessageDialog(this, "코인이 부족합니다!");
            }
        });
        shop.setVisible(true);
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
       
     // 2. 말풍선 전용 라벨 생성 (이게 '가구'처럼 Z-Order를 가집니다)
        emojiBubbleLabel = new JLabel();
        emojiBubbleLabel.setSize(100, 100);
        emojiBubbleLabel.setVisible(false); // 평소엔 숨김
        mainPanel.add(emojiBubbleLabel);
        
        backgroundLabel = new JLabel();
        backgroundLabel.setBounds(0, 0, 800, 500);
        mainPanel.add(backgroundLabel);
        
        imageLabel = new JLabel();
        imageLabel.setHorizontalAlignment(SwingConstants.CENTER);
        imageLabel.setVerticalAlignment(SwingConstants.CENTER);
        imageLabel.setSize(100, 100);
        mainPanel.add(imageLabel);
        
     // 3. 클릭 리스너 수정(이모지 말풍선)
        imageLabel.addMouseListener(new java.awt.event.MouseAdapter() {
            @Override
            public void mouseClicked(java.awt.event.MouseEvent e) {
                if (!isWaiting && Dimagotchi.isAliveStatic()) {
                    isWaiting = true;
                    movementTimer.stop(); // 멈춤

                 // ⭐ pet.getCleanliness()추가 .
                    emojiBubbleLabel.setIcon(petEmoji.getCombinedIcon(pet.getHappiness(), pet.getCleanliness()));

                    // 캐릭터(imageLabel)의 현재 위치를 기준으로 위쪽에 배치
                    emojiBubbleLabel.setLocation(imageLabel.getX() + 10, imageLabel.getY() - 80);
                    emojiBubbleLabel.setVisible(true); // 보이게 하기

                    // 1초 후 원상복구 타이머
                    Timer waitTimer = new Timer(1000, ae -> {
                        isWaiting = false;
                        emojiBubbleLabel.setVisible(false); // 다시 숨기기
                        movementTimer.start(); // 다시 이동
                        ((Timer)ae.getSource()).stop();
                    });
                    waitTimer.setRepeats(false);
                    waitTimer.start();
                }
            }
        });
        
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
        statsPanel.setLayout(new GridLayout(8, 1, 2, 2)); // 간격 살짝 줄임
        // 높이를 150 -> 220으로 늘려서 4개 바가 찌그러지지 않게 함
        statsPanel.setBounds(20, 20, 250, 220);
        statsPanel.setOpaque(false);
       
        hungerGauge = new StatusGaugePanel(); 
        hungerGauge.setPreferredSize(new Dimension(180, 25));
        
        happinessGauge = new StatusGaugePanel(); 
        happinessGauge.setCurrentColorIndex(1); 
        happinessGauge.setPreferredSize(new Dimension(180, 25));
        
        energyGauge = new StatusGaugePanel(); 
        energyGauge.setCurrentColorIndex(2); 
        energyGauge.setPreferredSize(new Dimension(180, 25));
        
        //청결도
        cleanlinessGauge = new StatusGaugePanel(); 
        cleanlinessGauge.setCurrentColorIndex(3); 
        cleanlinessGauge.setPreferredSize(new Dimension(180, 25));
        
        JPanel hungerLabel = new LabelWithBackgroundPanel("/res/button2.png", "포만감");
        hungerLabel.setPreferredSize(new Dimension(250, 25)); 
                
        JPanel happinessLabel = new LabelWithBackgroundPanel("/res/button2.png", "행복도");
        happinessLabel.setPreferredSize(new Dimension(250, 25));

        JPanel energyLabel = new LabelWithBackgroundPanel("/res/button2.png", "에너지");
        energyLabel.setPreferredSize(new Dimension(250, 25));
        
        JPanel cleanlinessLabel = new LabelWithBackgroundPanel("/res/button2.png", "청결도");
        cleanlinessLabel.setPreferredSize(new Dimension(250, 25));


        statsPanel.add(hungerLabel);
        statsPanel.add(hungerGauge); 
        
        statsPanel.add(happinessLabel);
        statsPanel.add(happinessGauge); 
        
        statsPanel.add(energyLabel);
        statsPanel.add(energyGauge);

        statsPanel.add(cleanlinessLabel);
        statsPanel.add(cleanlinessGauge);
        
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
        mainPanel.setComponentZOrder(emojiBubbleLabel, 1);
        mainPanel.setComponentZOrder(fortunePaperPanel, 0); 
        mainPanel.setComponentZOrder(fortuneCookiePanel, 2);
        mainPanel.setComponentZOrder(imageLabel, 3);
        mainPanel.setComponentZOrder(statsPanel, 4);
        mainPanel.setComponentZOrder(btnPanel, 5);
        mainPanel.setComponentZOrder(statusLabel, 6);
        mainPanel.setComponentZOrder(shopTabPanel, 7); 
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
                        
                     // ---------------------------------------------------------
                        JPopupMenu deleteMenu = new JPopupMenu();
                        JMenuItem deleteItem = new JMenuItem("가구 삭제");
                        deleteItem.setForeground(Color.RED);
                        deleteMenu.add(deleteItem);


                        deleteItem.addActionListener(ae -> {
                            int confirm = JOptionPane.showConfirmDialog(null, "이 가구를 삭제할까요?");
                            if (confirm == JOptionPane.YES_OPTION) {
                                mainPanel.remove(furnLabel);
                                mainPanel.repaint();
                            }
                        });
                        
                     // 우클릭 감지를 위한 리스너
                        furnLabel.addMouseListener(new java.awt.event.MouseAdapter() {
                            public void mousePressed(java.awt.event.MouseEvent me) {
                                if (me.isPopupTrigger()) deleteMenu.show(me.getComponent(), me.getX(), me.getY());
                            }
                            public void mouseReleased(java.awt.event.MouseEvent me) {
                                if (me.isPopupTrigger()) deleteMenu.show(me.getComponent(), me.getX(), me.getY());
                            }
                        });
                        
                        
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
        
        
     // 테스트를 위해 2초(2000ms)마다 깎이게 설정해 보세요.
     Timer autoDecreaseTimer = new Timer(2000, e -> {
         if (Dimagotchi.isAliveStatic()) {
             pet.updateStatusByTime(); // 👈 여기서 실제로 숫자가 깎임
             // updateUI()는 movementTimer가 이미 0.1초마다 실행중이므로 
             // 여기서 따로 안 불러줘도 게이지는 움직일 겁니다.
         }
     });
     autoDecreaseTimer.start();
        
        // --- 여기까지 ---

        add(mainPanel, BorderLayout.CENTER); // 
        
     // 벌레 스폰 타이머 (10초마다 확률적으로 생성)
        flySpawnTimer = new Timer(10000, e -> {
            if (Dimagotchi.isAliveStatic()) {
                // 30% 확률로 벌레 등장 (최대 5마리 제한)
                if (Math.random() < 0.3 && flyList.size() < 5) {
                    spawnVisualFly();
                }
            }
        });
        flySpawnTimer.start();

        movementTimer = new Timer(100, e -> {
            if (Dimagotchi.isAliveStatic()) {
                pet.getCharacter().updateMovement();
                updateFlies();
                updateBackground();
            }
            updateUI(); // 주기적 화면 갱신 (여기서 똥 시각효과 처리 호출됨)
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
    
 // 청결도에 따른 똥 아이콘 업데이트 로직
    private void updatePoopVisuals() {
        // 1. 현재 청결도 가져오기
        int cleanliness = pet.getCleanliness();
        
        // 2. 청결도에 따라 필요한 똥의 개수 계산
        // 100~80: 0개, 79~60: 1개, 59~40: 2개, 39~20: 3개, ... 최대 5개
        int expectedPoopCount = 0;
        
        if (cleanliness < 80) expectedPoopCount++;
        if (cleanliness < 60) expectedPoopCount++;
        if (cleanliness < 40) expectedPoopCount++;
        if (cleanliness < 20) expectedPoopCount++;
        if (cleanliness < 10) expectedPoopCount++;
        
        // 3. 현재 화면에 있는 똥의 개수와 비교하여 동기화
        int currentCount = poopList.size();
        
        // A. 똥이 부족하면 -> 추가 생성
        if (currentCount < expectedPoopCount) {
            // 부족한 만큼 반복해서 생성
            for (int i = 0; i < (expectedPoopCount - currentCount); i++) {
                addPoopIcon();
            }
        } 
        // B. 똥이 너무 많으면 -> 제거 (청소했을 때 동작)
        else if (currentCount > expectedPoopCount) {
            // 넘치는 만큼 제거 (리스트의 뒤에서부터 제거)
            while (poopList.size() > expectedPoopCount) {
                JLabel poopToRemove = poopList.remove(poopList.size() - 1);
                mainPanel.remove(poopToRemove);
            }
            mainPanel.repaint();
        }
    }
    
    //현재 완전히 그렇게 보임.
    // [추가된 보조 메서드] 똥 아이콘 하나를 화면에 랜덤 배치하여 추가
    private void addPoopIcon() {
        try {
            // 1. 이미지 로드
            java.net.URL poopImgUrl = getClass().getResource("/res/poooop.png");
            if (poopImgUrl == null) {
                // 이미지가 없으면 콘솔에 경고만 출력하고 중단
                System.err.println("경고: /res/poop.png 파일을 찾을 수 없습니다.");
                return;
            }
            BufferedImage poopImg = ImageIO.read(poopImgUrl);
            Image scaledPoop = poopImg.getScaledInstance(40, 40, Image.SCALE_SMOOTH);
            
            // 2. 라벨 생성
            JLabel poopLabel = new JLabel(new ImageIcon(scaledPoop));
            
            // 3. 위치 랜덤 설정 (바닥 부분에 배치되도록)
            Random rand = new Random();
            // X: 50 ~ 700 사이
            int x = 50 + rand.nextInt(650);
            // Y: 300 ~ 450 사이 (바닥)
            int y = 300 + rand.nextInt(150);
            
            poopLabel.setBounds(x, y, 40, 40);
            
            // 4. 패널 및 리스트에 추가
            mainPanel.add(poopLabel);
            poopList.add(poopLabel);
            
            // 5. Z-Order 설정 (캐릭터보다 앞에 오도록 3번 레이어 등 사용)
            // 0: 최상위 (운세종이), 1: 포춘쿠키, 2: 캐릭터 ...
            // 똥은 캐릭터 근처나 위에 있을 수 있으므로 2번 정도가 적당함.
            // setComponentZOrder 메서드는 인덱스가 낮을수록 화면 앞쪽입니다.
            mainPanel.setComponentZOrder(poopLabel, 2); 
            
            mainPanel.repaint();
            
        } catch (IOException e) {
            e.printStackTrace();
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
            case 1: 
                message = pet.feed(); 
                break;
            case 2: 
                message = pet.play(); 
                break;
            case 3: 
                message = pet.sleep(); 
                break;
            case 4: 
                // 청소하기 버튼 로직
                // 1. 데이터 상의 청결도 회복
                message = pet.clean();
                // 2. updateUI() -> updatePoopVisuals()가 호출되면서 
                //    높아진 청결도에 맞춰 똥 이미지가 자동으로 사라집니다.
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
        if (!Dimagotchi.isAliveStatic()) {
            JOptionPane.showMessageDialog(this, "다마고치가 아파서 TV를 볼 수 없습니다.");
            return;
        }
        // 미니게임 다이얼로그 열기
        new MiniGameDialog(this, pet).setVisible(true);
        updateUI(); // 게임 종료 후 먹이 개수 갱신을 위해 호출
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
        updatePoopVisuals();
        
        int currentHunger = pet.getHunger();
        int fullness = 100 - currentHunger;
        hungerGauge.setCurrentValue(fullness); 
        
        int currentHappiness = pet.getHappiness();
        
        // 만약 청결도가 낮다면 행복도를 깎아서 보여주는 시각적 효과 (선택사항)
        // 여기서는 데이터 자체를 passTime에서 깎으므로 그대로 표시합니다.
        happinessGauge.setCurrentValue(currentHappiness); 

        int currentEnergy = pet.getEnergy();
        energyGauge.setCurrentValue(currentEnergy);
        int currentCleanliness = pet.getCleanliness();
       cleanlinessGauge.setCurrentValue(currentCleanliness);
        
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
        
        if (btnFeed != null) {
            btnFeed.setText("밥주기 🍖 (" + pet.getFoodCount() + ")");
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
            checkGameOver();
        }
    }

    

    private void updateStatusBars() {
        // 선언하신 변수명(hungerGauge, happinessGauge, energyGauge)과 정확히 일치시켰습니다.
        if (hungerGauge != null) {
            hungerGauge.setCurrentValue(pet.getHunger());
        }
        
        if (happinessGauge != null) {
            happinessGauge.setCurrentValue(pet.getHappiness());
        }
        
        if (energyGauge != null) {
            energyGauge.setCurrentValue(pet.getEnergy());
        }
    }
    
    private void checkGameOver() {
        // 1. 정적 메서드를 통해 사망 여부 확인
        if (!Dimagotchi.isAliveStatic()) { 
            
            // 사망 원인을 가져와서 메시지 띄우기
            String reason = pet.getCauseOfDeath(); 
            
            int choice = JOptionPane.showConfirmDialog(this, 
                "다마고치가 무지개 다리를 건넜습니다...\n" + reason + "\n새로운 알을 입양하시겠습니까?", 
                "Game Over", JOptionPane.YES_NO_OPTION);

            if (choice == JOptionPane.YES_OPTION) {
                // 2. 리셋 수행
                processReset();
            } else {
                System.exit(0);
            }
        }
    }
    private void processReset() {
        // Dimagotchi 클래스의 reset() 호출 
        // (여기서 Dimagotchi.causeOfDeath = "" 가 반드시 실행되어야 함)
        pet.resetAll(); 
        for (JLabel poop : poopList) {
            mainPanel.remove(poop);
        }
        poopList.clear();
        
        // 멈췄던 타이머들을 다시 시작 
        if (movementTimer != null) {
            movementTimer.start(); 
        }
        if (flySpawnTimer != null) {
            flySpawnTimer.start();
        }
        // Character 객체의 위치와 타입을 EGG로 리셋
        pet.getCharacter().resetCharacter(); 
        
        // UI 요소 갱신
        statusLabel.setText("새로운 알이 도착했습니다!");
        updateStatusBars(); // 허기, 에너지 등의 바를 다시 그리는 메서드
        repaint();
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            new DimagotchiGUI();
        });
    } 
}