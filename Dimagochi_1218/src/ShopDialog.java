import javax.swing.*;
import java.awt.*;
import java.util.function.Consumer;

public class ShopDialog extends JDialog {
    private final Consumer<furniture.FurnitureType> onItemSelected;

    /**
     * @param owner 부모 프레임
     * @param callback 아이템 선택 시 실행할 동작 (함수형 인터페이스)
     */
    public ShopDialog(JFrame owner, Consumer<furniture.FurnitureType> callback) {
        super(owner, "다마고치 가구 상점", true);
        this.onItemSelected = callback;

        setSize(600, 450);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());

        initUI();
    }

    private void initUI() {
        // 상단 안내 메시지
        JLabel title = new JLabel("가구를 선택하세요", SwingConstants.CENTER);
        title.setFont(new Font("맑은 고딕", Font.BOLD, 18));
        title.setBorder(BorderFactory.createEmptyBorder(15, 0, 15, 0));
        add(title, BorderLayout.NORTH);

        // 아이템 리스트 패널 (그리드 레이아웃)
        JPanel shopPanel = new JPanel();
        shopPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 25, 25));
        shopPanel.setBackground(new Color(250, 250, 250));

        // 모든 가구 타입에 대해 카드 생성
        for (furniture.FurnitureType type : furniture.FurnitureType.values()) {
            shopPanel.add(createItemCard(type));
        }

        JScrollPane scrollPane = new JScrollPane(shopPanel);
        scrollPane.setBorder(null);
        add(scrollPane, BorderLayout.CENTER);
    }

    private JPanel createItemCard(furniture.FurnitureType type) {
        JPanel card = new JPanel();
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setBackground(Color.WHITE);
        card.setBorder(BorderFactory.createCompoundBorder(
            BorderFactory.createLineBorder(new Color(220, 220, 220), 1),
            BorderFactory.createEmptyBorder(10, 15, 10, 15)
        ));

        // 1. 가구 미리보기 이미지 (상점용 2배 배율)
        furniture tempFurn = new furniture(type, 0, 0);
        Image previewImg = tempFurn.getScaledImage(2);
        JLabel imgLabel = new JLabel(new ImageIcon(previewImg));
        imgLabel.setAlignmentX(Component.CENTER_ALIGNMENT);

        // 2. 가구 이름 (Enum 이름을 기반으로 한 한글 매핑 예시)
        String displayName = getDisplayName(type);
        JLabel nameLabel = new JLabel(displayName);
        nameLabel.setFont(new Font("맑은 고딕", Font.BOLD, 14));
        nameLabel.setAlignmentX(Component.CENTER_ALIGNMENT);
        nameLabel.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));

        // 3. 선택 버튼
        JButton buyBtn = new JButton("구매하기");
        buyBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        buyBtn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        buyBtn.addActionListener(e -> {
            onItemSelected.accept(type); // 메인 GUI에 선택된 타입 전달
            dispose(); // 상점 닫기
        });

        card.add(imgLabel);
        card.add(nameLabel);
        card.add(buyBtn);

        return card;
    }

    private String getDisplayName(furniture.FurnitureType type) {
        // Enum 이름을 한글로 변환 (필요에 따라 수정)
        switch (type) {
            case FLOWER_POT: return "넓은 나뭇잎 화분";
            case FLOWER_POT2: return "얇은 나뭇잎 화분";
            case FLOWER_POT3: return "우유병 화분";
            case FLOWER_POT4: return "처진 나뭇잎 화분";
            case FLOWER_POT5: return "꽃 화분";
         
            case BOOKCASE1: return "화분이 있는 책장";
            case BOOKCASE2: return "책이 있는 책장";
            
            case SHELF: return "벽걸이 선반";
            case TABLE: return "책상";
            case SMALLSHELF: return "작은 선반";
            case THINSHELF: return "좁은 선반";
            case TABLE2: return "화분 테이블";
          
            
 
            default: return type.name();
        }
    }
}