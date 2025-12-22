import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.IOException;

public class StatusGaugePanel extends JPanel {
    
    private BufferedImage spriteSheet; 
    private BufferedImage bgImage; // 배경 이미지 변수 추가
    
   // private BufferedImage[] gaugeSegments = new BufferedImage[3];
    private BufferedImage[][] gaugeSegments = new BufferedImage[4][3];
    
    private int currentValue = 0;
    private final int maxValue = 100;
    
 // ⭐ 현재 사용할 색상을 지정하는 변수 추가 ⭐
    private int currentColorIndex = 0; // 0: Red (기본값)
    
    private static final String SPRITE_FILENAME = "/res/button.png"; 

    private static final int GAUGE_HEIGHT = 50; // 바의 높이 (변동 없음)
    
      // (2) 배경 이미지 (황금색 프레임 포함 전체)의 크기/위치 정보
    public static final int FULL_BG_WIDTH = 1138; 
    public static final int FULL_BG_HEIGHT = 100; 
    public static final int FULL_BG_X = 6; 
    private static final int FULL_BG_Y = 50; 
    
    private static final int RED_Y = 206;         // 픽셀 
   
    private static final int GREEN_Y = 465;
    
    private static final int BLUE_Y = 342;
    
    private static final int PURPLE_Y = 580;

    private static final int START_X = 30;
    private static final int START_WIDTH = 70;
    private static final int END_WIDTH = 10;
    private static final int RED_BAR_WIDTH = 1080; 
    private static final int GAUGE_OFFSET_X = 30;   // ⭐ 원하는 시작 위치의 원본 픽셀 값으로 설정하세요.
    // --------------------------------------------------------------------

    public StatusGaugePanel() {
        loadAndSegmentImages(); 
        setOpaque(false); 
    }
    
    private void loadAndSegmentImages() {
        try {
            spriteSheet = ImageIO.read(getClass().getResource(SPRITE_FILENAME));

            if (spriteSheet == null) {
                throw new IOException("스프라이트 시트 파일을 찾을 수 없습니다: " + SPRITE_FILENAME);
            }
            
            // 1. 배경 이미지 추출 및 저장
            // BG_Y와 GAUGE_HEIGHT를 사용합니다.
            bgImage = spriteSheet.getSubimage(FULL_BG_X, FULL_BG_Y, FULL_BG_WIDTH, FULL_BG_HEIGHT); // 👈 이 코드가 정확한지 확인

            // 2. 게이지 세그먼트 추출
            int MIDDLE_X = START_X + START_WIDTH;
            int MIDDLE_WIDTH = RED_BAR_WIDTH - START_WIDTH - END_WIDTH;
            
         // **⭐ 기존 Red (인덱스 0) 추출 로직 ⭐**
            // (gaugeSegments[0]에 저장됨)
            gaugeSegments[0][0] = spriteSheet.getSubimage(START_X, RED_Y, START_WIDTH, GAUGE_HEIGHT); // R-Start
            gaugeSegments[0][1] = spriteSheet.getSubimage(MIDDLE_X, RED_Y, MIDDLE_WIDTH, GAUGE_HEIGHT); // R-Middle
            gaugeSegments[0][2] = spriteSheet.getSubimage(MIDDLE_X + MIDDLE_WIDTH, RED_Y, END_WIDTH, GAUGE_HEIGHT); // R-End

            // **⭐ 파란색 (인덱스 1) 추출 로직 추가 ⭐**
            // (BLUE_Y 사용, gaugeSegments[1]에 저장됨)
            gaugeSegments[1][0] = spriteSheet.getSubimage(START_X, BLUE_Y, START_WIDTH, GAUGE_HEIGHT); // B-Start
            gaugeSegments[1][1] = spriteSheet.getSubimage(MIDDLE_X, BLUE_Y, MIDDLE_WIDTH, GAUGE_HEIGHT); // B-Middle
            gaugeSegments[1][2] = spriteSheet.getSubimage(MIDDLE_X + MIDDLE_WIDTH, BLUE_Y, END_WIDTH, GAUGE_HEIGHT); // B-End

            // **⭐ 초록색 (인덱스 2) 추출 로직 추가 ⭐**
            // (GREEN_Y 사용, gaugeSegments[2]에 저장됨)
            gaugeSegments[2][0] = spriteSheet.getSubimage(START_X, GREEN_Y, START_WIDTH, GAUGE_HEIGHT); // G-Start
            gaugeSegments[2][1] = spriteSheet.getSubimage(MIDDLE_X, GREEN_Y, MIDDLE_WIDTH, GAUGE_HEIGHT); // G-Middle
            gaugeSegments[2][2] = spriteSheet.getSubimage(MIDDLE_X + MIDDLE_WIDTH, GREEN_Y, END_WIDTH, GAUGE_HEIGHT); // G-End
            
            
            // **⭐ 보라색(인덱스 3) 추출 로직 추가 ⭐**
            // (GREEN_Y 사용, gaugeSegments[2]에 저장됨)
            gaugeSegments[3][0] = spriteSheet.getSubimage(START_X, PURPLE_Y, START_WIDTH, GAUGE_HEIGHT); // G-Start
            gaugeSegments[3][1] = spriteSheet.getSubimage(MIDDLE_X, PURPLE_Y, MIDDLE_WIDTH, GAUGE_HEIGHT); // G-Middle
            gaugeSegments[3][2] = spriteSheet.getSubimage(MIDDLE_X + MIDDLE_WIDTH, PURPLE_Y, END_WIDTH, GAUGE_HEIGHT); // G-End
            

        } catch (IOException e) {
            System.err.println("이미지 파일 로드 실패: " + e.getMessage());
            e.printStackTrace();
        } catch (java.awt.image.RasterFormatException e) {
            System.err.println("이미지 좌표 오류! 상수(GAUGE_HEIGHT, RED_Y 등)를 확인하세요.");
            e.printStackTrace();
        }
    }
    
    public void setCurrentValue(int value) {
        if (value < 0) value = 0;
        if (value > maxValue) value = maxValue;
        this.currentValue = value;
        repaint();
    }
    
    
    public int getCurrentValue() {
        return currentValue;
    }

    public void setCurrentColorIndex(int colorIndex) {
    	if (colorIndex >= 0 && colorIndex < gaugeSegments.length) {
    		this.currentColorIndex = colorIndex;
    		repaint(); // 화면을 다시 그려 변경된 색상을 반영
    	}
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        
        // 현재 인덱스의 게이지 세그먼트가 로드되었는지 확인
        if (bgImage == null || gaugeSegments[currentColorIndex][0] == null) {
            return;
        }
        
        // 현재 색상 인덱스에 맞는 게이지 세그먼트 할당
        BufferedImage filledStart = gaugeSegments[currentColorIndex][0];
        BufferedImage filledMiddle = gaugeSegments[currentColorIndex][1];
        BufferedImage filledEnd = gaugeSegments[currentColorIndex][2];

        Graphics2D g2d = (Graphics2D) g.create(); 
        int panelWidth = getWidth();
        int panelHeight = getHeight();
        int originalWidth = FULL_BG_WIDTH;

        // 1. 배경 그리기 (패널 크기에 맞춰 늘립니다)
        g2d.drawImage(bgImage, 0, 0, panelWidth, panelHeight, this); 

        // --- 2. 게이지 스케일링 및 위치 계산 (Ratio 기반) ---
        
        // X축 스케일링 비율을 기준으로 합니다.
        double ratioX = (double) panelWidth / originalWidth; 
        double ratio = ratioX;
        
        // Y축 스케일링 비율은 Y축 크기를 따라 높이 스케일링에 사용됩니다.
        double ratioY = (double) panelHeight / FULL_BG_HEIGHT;

        // 2-1. Y축 정렬 (세로 중앙 정렬)
        // GAUGE_HEIGHT (50)에 Y축 비율(ratioY)을 곱해 높이를 스케일링
        int scaledGaugeHeight = (int)(GAUGE_HEIGHT * ratioY); 
        int scaledOffsetY = (panelHeight - scaledGaugeHeight) / 2; // 패널 중앙에 배치

        /// 2-2. X축 정렬
     // GAUGE_OFFSET_X (14)에 X축 비율(ratio)을 곱해 오프셋 스케일링
        int scaledOffsetX = (int)(GAUGE_OFFSET_X * ratio); // ⭐ GAUGE_OFFSET_X 상수를 사용하도록 변경
        int gaugeMaxWidth = (int)(RED_BAR_WIDTH * ratio); // 스케일링된 최대 너비
        
        if (currentValue == 0) { g2d.dispose(); return; }

        // --- 3. 게이지 채우기 로직 ---
        double percentage = (double) currentValue / maxValue;
        int filledWidth = (int) (gaugeMaxWidth * percentage);

        // 세그먼트 너비는 X축 비율(ratio)로 스케일링
        int startWidth = (int)(filledStart.getWidth() * ratio);
        int endWidth = (int)(filledEnd.getWidth() * ratio);
        
        int middleWidth = filledWidth - startWidth - endWidth;
        
        // 최소 길이 처리
        if (filledWidth < startWidth + endWidth) {
            middleWidth = 0; 
            startWidth = Math.min(filledWidth, startWidth); 
            endWidth = 0; 
        }
        
        // A. 시작 부분 그리기
        g2d.drawImage(filledStart, 
                      scaledOffsetX, scaledOffsetY, 
                      scaledOffsetX + startWidth, scaledOffsetY + scaledGaugeHeight, 
                      0, 0, filledStart.getWidth(), filledStart.getHeight(), this);

        // B. 중간 부분 늘려 그리기
        if (middleWidth > 0) {
            g2d.drawImage(filledMiddle,
                          scaledOffsetX + startWidth, scaledOffsetY, 
                          scaledOffsetX + startWidth + middleWidth, scaledOffsetY + scaledGaugeHeight, 
                          0, 0, filledMiddle.getWidth(), filledMiddle.getHeight(),
                          this);
        }

        // C. 끝 부분 그리기
        if (endWidth > 0) {
            g2d.drawImage(filledEnd, 
                          scaledOffsetX + startWidth + middleWidth, scaledOffsetY, 
                          scaledOffsetX + startWidth + middleWidth + endWidth, scaledOffsetY + scaledGaugeHeight,
                          0, 0, filledEnd.getWidth(), filledEnd.getHeight(), 
                          this);
        }
        g2d.dispose(); 
    }
    
    
}