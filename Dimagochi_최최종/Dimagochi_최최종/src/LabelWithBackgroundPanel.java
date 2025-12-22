import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.imageio.ImageIO;

public class LabelWithBackgroundPanel extends JPanel {
    private BufferedImage backgroundImage;
    private final JLabel textLabel;

    /**
     * 배경 이미지 파일명과 표시할 텍스트를 받아 초기화합니다.
     * @param imageFileName 리소스 폴더 내의 배경 이미지 파일명 (예: "/res/button2.png")
     * @param labelText 패널 위에 표시할 텍스트 (예: "포만감")
     */
    public LabelWithBackgroundPanel(String imageFileName, String labelText) {
        // 이미지를 로드합니다.
        try {
            // ⭐ 이미지 경로를 프로젝트 구조에 맞게 수정하세요! (예: /res/button2.png) ⭐
            backgroundImage = ImageIO.read(getClass().getResource(imageFileName));
        } catch (IOException e) {
            System.err.println("배경 이미지 로드 실패: " + imageFileName);
            e.printStackTrace();
        }

        // 1. 레이아웃: 이미지 위에 텍스트를 중앙에 겹치게 배치하기 위해 BorderLayout을 사용합니다.
        setLayout(new BorderLayout());
        setOpaque(false); // JPanel 기본 배경을 투명하게 설정 (이미지 배경이 보이도록)

        // 2. 텍스트 레이블 생성 및 설정
        textLabel = new JLabel(labelText, SwingConstants.LEFT);
        textLabel.setFont(new Font("맑은 고딕", Font.BOLD, 12));
        textLabel.setForeground(Color.BLACK); // 텍스트 색상
        
     // ⭐ 수정 부분: EmptyBorder를 사용하여 왼쪽 여백(Padding)을 추가합니다. ⭐
        // 아래 20은 픽셀 값이며, 이 값을 조정하여 글자를 오른쪽으로 밀 수 있습니다.
        int leftPadding = 3; // 👈 텍스트를 오른쪽으로 20픽셀 미는 효과
        
        textLabel.setBorder(BorderFactory.createEmptyBorder(
            0,                 // 위 여백
            leftPadding,       // 왼쪽 여백 (⭐ X축 조정 핵심 ⭐)
            0,                 // 아래 여백
            0                  // 오른쪽 여백
        ));
        
        // JLabel을 패널의 중앙에 추가하여 이미지 위에 겹치게 합니다.
        // FlowLayout을 사용하거나 Insets을 조정하여 적절한 위치에 배치할 수 있습니다.
        // 여기서는 간단히 BorderLayout.CENTER에 추가하고 텍스트 정렬을 Left로 맞춥니다.
        // 필요하다면 JPanel(FlowLayout)로 한번 더 감싸서 여백을 줄 수 있습니다.
        add(textLabel, BorderLayout.WEST);
    }

@Override
protected void paintComponent(Graphics g) {
    super.paintComponent(g);
    
    if (backgroundImage != null) {
        Graphics2D g2d = (Graphics2D) g;
        
        // --- ❌ 기존: 패널 크기에 맞춰 강제로 늘리는 코드 (문제 발생 원인) ---
        // int panelWidth = getWidth();
        // int panelHeight = getHeight();
        // g2d.drawImage(backgroundImage, 
        //               0, 0, panelWidth, panelHeight, 
        //               this);
        // ---------------------------------------------------------------
        
        
        // --- ✅ 수정: 원본 크기 또는 특정 고정 크기로 그리는 코드 ---
        
        // 1. 이미지의 원본 너비와 높이를 가져옵니다.
        int imgWidth = backgroundImage.getWidth();
        int imgHeight = backgroundImage.getHeight();
        
        // 2. 패널의 높이를 기준으로 이미지를 스케일링합니다 (Aspect Ratio 유지)
        //    (패널 높이에 맞춰서 이미지를 그리고 싶지 않다면, imgHeight와 imgWidth를 그대로 사용합니다.)
        /*
        // ⭐ 옵션 A: 원본 크기 그대로 그림 (가장 간단) ⭐
        int drawWidth = imgWidth;
        int drawHeight = imgHeight;
        int drawX = 0; // 왼쪽 정렬
        */
        // ⭐ 옵션 B: 패널의 높이에 맞춰서 비율 유지하며 그림 ⭐
        
        int panelHeight = getHeight();
        int drawHeight = panelHeight;
        // 비율 유지하며 너비 계산
        int drawWidth = (int) ((double) imgWidth * panelHeight / imgHeight);
        int drawX = 0; // 왼쪽 정렬
        
        
        // ⭐ 옵션 C: 특정 고정 크기로 그림 (예: 50x25 픽셀 고정) ⭐
        /*
        int drawWidth = 50; 
        int drawHeight = 25; 
        int drawX = 0; // 왼쪽 정렬
        */

        // 여기서는 Option A (원본 크기 그대로 그림)를 사용하겠습니다.
        
        // 이미지를 그립니다.
        g2d.drawImage(backgroundImage, 
                      drawX, 0, drawWidth, drawHeight, 
                      this);
    }
}
}