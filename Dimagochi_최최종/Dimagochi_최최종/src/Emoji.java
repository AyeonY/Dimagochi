import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import javax.imageio.ImageIO;
import javax.swing.ImageIcon;

public class Emoji {
    public enum EmojiType {
        EMO1(0), EMO2(1), EMO3(2), EMO4(3), EMO5(4), EMO6(5), EMO7(6);
        private final int index;
        EmojiType(int index) { this.index = index; }
        public int getIndex() { return index; }
    }

    public static class State {
        public int start_x, start_y, width, height;
    }

    private BufferedImage spriteSheet;   // 이모지 시트
    private BufferedImage bubbleImage;   // 말풍선 배경
    private State[] states;
    private EmojiType currentType;

    public Emoji(EmojiType type) {
        this.currentType = type;
        loadImages();
        initStates();
    }

    private void loadImages() {
        try {
            // 1. 이모지 시트 로드
            URL emojiURL = getClass().getResource("/res/emoji.png");
            if (emojiURL != null) spriteSheet = ImageIO.read(emojiURL);

            // 2. 말풍선 이미지 로드 
            URL bubbleURL = getClass().getResource("/res/emojiback.png");
            if (bubbleURL != null) bubbleImage = ImageIO.read(bubbleURL);
            
        } catch (IOException e) {
            System.err.println("이미지 로드 실패: " + e.getMessage());
        }
    }

    private void initStates() {
        states = new State[EmojiType.values().length];
        // 사용자가 제공한 좌표값들
        addState(0, 11, 35, 248, 257); //애정도 만땅
        addState(1, 284, 44, 248, 257);//싱긋 
        addState(2, 600, 44, 247, 229);//활짝
        addState(3, 865, 323, 250, 266);//머쓱
        addState(4, 586, 337, 241, 253);//쓰읍
        addState(5, 331, 361, 236, 236);//울망
        addState(6, 20, 358, 225, 252);//화남
    }

    private void addState(int index, int x, int y, int w, int h) {
        State s = new State();
        s.start_x = x; s.start_y = y; s.width = w; s.height = h;
        states[index] = s;
    }
    //이모지 수정
    public ImageIcon getCombinedIcon(int happiness, int cleanliness) {
        // 1. 최악의 상태 우선 체크 (매우 지저분하거나 매우 불행할 때)
        if (cleanliness < 30) {
            currentType = EmojiType.EMO7; // 화남
        } 
        else if (happiness < 20) {
            currentType = EmojiType.EMO6; // 울망울망 (매우 슬픔)
        }
        // 2. 긍정적인 상태 체크 (높은 수치부터)
        else if (happiness >= 85 && cleanliness >= 85) {
            currentType = EmojiType.EMO1; // 애정도 만땅 (완벽한 상태)
        } 
        else if (happiness >= 70) {
            currentType = EmojiType.EMO3; // 활짝 (매우 기쁨)
        } 
        else if (happiness >= 50) {
            currentType = EmojiType.EMO2; // 싱긋 (좋은 상태)
        } 
        else if (happiness >= 30) {
            currentType = EmojiType.EMO5; // 쓰읍/무표정 (지루하거나 배고픔)
        }
        // 3. 그 외 나머지 (중간 단계)
        else {
            currentType = EmojiType.EMO4; // 머쓱/곤란
        }
        // 2. 결정된 currentType으로 합성 이미지 생성 시작
        BufferedImage bubbleImg = bubbleImage; // 기존에 로드된 말풍선 이미지
        
        // 합성 도화지 만들기
        BufferedImage combined = new BufferedImage(100, 100, BufferedImage.TYPE_INT_ARGB);
        Graphics g = combined.getGraphics();

        // 말풍선 그리기
        g.drawImage(bubbleImg, 0, 0, 100, 100, null);

        // 결정된 감정의 이모지 조각 잘라내기
        State s = states[currentType.getIndex()];
        BufferedImage cropped = spriteSheet.getSubimage(s.start_x, s.start_y, s.width, s.height);
        
        // 이모지 크기 조절 및 중앙 배치 (아까 조정한 사이즈 적용)
        int emojiSize = 40; 
        int centeredX = (100 - emojiSize) / 2;
        int centeredY = (100 - emojiSize) / 2 - 5;
        
        g.drawImage(cropped, centeredX, centeredY, emojiSize, emojiSize, null);

        g.dispose();
        return new ImageIcon(combined);
    }
    
    public void draw(Graphics g, int petX, int petY) {
        if (bubbleImage == null || spriteSheet == null) return;

        // 1. 말풍선 설정
        int bubbleSize = 80;  // 말풍선 전체 크기
        int bx = petX + 30;  
        int by = petY - 75;

        // 2. 말풍선 그리기
        g.drawImage(bubbleImage, bx, by, bubbleSize, bubbleSize, null);

        // 3. 이모지 조각 잘라내기
        State s = states[currentType.getIndex()];
        BufferedImage emojiImg = spriteSheet.getSubimage(s.start_x, s.start_y, s.width, s.height);

        // 4. ⭐ 여기서 숫자를 직접 수정하세요! (이모지 출력 크기)
        // 80(말풍선)보다 작은 숫자를 넣어야 합니다. 
        // 30~40 정도가 적당하며, 숫자를 줄일수록 확실히 작아집니다.
        int emojiSize = 50; 

        // 5. 말풍선 내부의 정중앙 좌표 계산
        // (말풍선크기 - 이모지크기) / 2 를 더해주면 중앙에 옵니다.
        int xOffset = (bubbleSize - emojiSize) / 2;
        int yOffset = (bubbleSize - emojiSize) / 2 - 5; // 말풍선 꼬리 때문에 살짝 위로

        g.drawImage(emojiImg, bx + xOffset, by + yOffset, emojiSize, emojiSize, null);
    }

    public void setType(EmojiType type) {
        this.currentType = type;
    }
}