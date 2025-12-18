import javax.swing.*;
import java.awt.*;
import java.util.Random;

public class MiniGameDialog extends JDialog {
    private Dimagotchi pet;
    private JLabel infoLabel;
    private JPanel gamePanel;
    private int score = 0;
    private Random random = new Random();

    public MiniGameDialog(JFrame owner, Dimagotchi pet) {
        super(owner, "TV 미니게임 스테이션", true);
        this.pet = pet;
        setSize(400, 400);
        setLocationRelativeTo(owner);
        setLayout(new BorderLayout());

        infoLabel = new JLabel("게임을 선택하세요! 클리어 시 먹이 +2", SwingConstants.CENTER);
        infoLabel.setFont(new Font("맑은 고딕", Font.BOLD, 15));
        add(infoLabel, BorderLayout.NORTH);

        gamePanel = new JPanel(new CardLayout());
        
        // 메뉴 화면
        JPanel menuPanel = new JPanel(new GridLayout(2, 1, 10, 10));
        JButton btnGame1 = new JButton("1. 순발력 클릭 (5번 클릭)");
        JButton btnGame2 = new JButton("2. 빠른 암산 (3문제 풀기)");
        
        btnGame1.addActionListener(e -> startClickGame());
        btnGame2.addActionListener(e -> startMathGame());
        
        menuPanel.add(btnGame1);
        menuPanel.add(btnGame2);
        
        add(menuPanel, BorderLayout.CENTER);
    }

    // --- 게임 1: 순발력 클릭 ---
    private void startClickGame() {
        getContentPane().removeAll();
        setLayout(new BorderLayout());
        score = 0;
        
        JButton targetBtn = new JButton("클릭!");
        targetBtn.setBounds(150, 150, 80, 50);
        
        JPanel playArea = new JPanel(null);
        playArea.add(targetBtn);
        
        targetBtn.addActionListener(e -> {
            score++;
            if (score >= 5) {
                endGame("순발력 대장! 먹이 2개를 얻었습니다.");
            } else {
                targetBtn.setLocation(random.nextInt(280), random.nextInt(250));
            }
        });

        add(playArea, BorderLayout.CENTER);
        revalidate();
        repaint();
    }

    // --- 게임 2: 빠른 암산 ---
    private void startMathGame() {
        getContentPane().removeAll();
        setLayout(new FlowLayout());
        score = 0;

        showMathProblem();
    }

    private void showMathProblem() {
        int a = random.nextInt(10) + 1;
        int b = random.nextInt(10) + 1;
        int answer = a + b;

        JLabel question = new JLabel(a + " + " + b + " = ? ");
        JTextField input = new JTextField(5);
        JButton submit = new JButton("확인");

        submit.addActionListener(e -> {
            try {
                if (Integer.parseInt(input.getText()) == answer) {
                    score++;
                    if (score >= 3) {
                        endGame("수학 천재! 먹이 2개를 얻었습니다.");
                    } else {
                        getContentPane().removeAll();
                        showMathProblem();
                        revalidate();
                    }
                } else {
                    JOptionPane.showMessageDialog(this, "틀렸습니다! 다시 집중하세요.");
                }
            } catch (NumberFormatException ex) { }
        });

        add(question);
        add(input);
        add(submit);
        revalidate();
        repaint();
    }

    private void endGame(String msg) {
        JOptionPane.showMessageDialog(this, msg);
        pet.addFood(2); // 먹이 보상
        dispose();
    }
}