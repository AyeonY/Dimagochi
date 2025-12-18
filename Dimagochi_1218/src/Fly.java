import javax.swing.*;
import java.awt.*;
import java.util.Random;
import java.net.URL;

public class Fly extends JLabel {
    private int x, y;
    private int dx, dy; // 이동 속도 및 방향
    private final int MAX_X = 750; // 화면 범위 제한
    private final int MAX_Y = 400;
    private Random random = new Random();

    public Fly() {
        
        // 이미지 로드
        try {
            URL imgURL = getClass().getResource("/res/fly.png");
            if (imgURL != null) {
                ImageIcon icon = new ImageIcon(imgURL);
                Image img = icon.getImage().getScaledInstance(30, 40, Image.SCALE_SMOOTH);
                setIcon(new ImageIcon(img));
            }
        } catch (Exception e) {
            setText("Bug");
        }

        // 초기 위치 및 속도 랜덤 설정
        this.x = random.nextInt(MAX_X);
        this.y = random.nextInt(MAX_Y);
        // 속도는 -3 ~ 3 사이 (0 제외)
        this.dx = random.nextInt(7) - 3;
        this.dy = random.nextInt(7) - 3;
        if (dx == 0) dx = 1;
        if (dy == 0) dy = 1;

        setSize(30, 30); // 컴포넌트 크기 설정
        setLocation(x, y);
        setCursor(new Cursor(Cursor.HAND_CURSOR)); // 클릭 가능하다는 표시
    }

    // 벌레 이동 로직
    public void updatePosition() {
        x += dx;
        y += dy;

        // 벽에 부딪히면 튕기기
        if (x <= 0 || x >= MAX_X) dx *= -1;
        if (y <= 0 || y >= MAX_Y) dy *= -1;

        // 가끔 방향 바꾸기 (불규칙한 움직임)
        if (random.nextInt(100) < 5) {
            dx = random.nextInt(7) - 3;
            dy = random.nextInt(7) - 3;
        }

        setLocation(x, y);
    }
}