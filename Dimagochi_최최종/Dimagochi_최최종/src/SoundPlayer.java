import javax.sound.sampled.*;
import java.io.BufferedInputStream;
import java.io.InputStream;

public class SoundPlayer {
    private Clip bgmClip;

    // BGM 재생 메서드
    public void playBGM(String filePath) {
        try {
            // 리소스 폴더에서 음악 파일 로드 (wav 형식 추천)
            InputStream audioSrc = getClass().getResourceAsStream(filePath);
            if (audioSrc == null) {
                System.err.println("음악 파일을 찾을 수 없습니다: " + filePath);
                return;
            }
            
            // 버퍼를 이용해 스트림 읽기
            InputStream bufferedIn = new BufferedInputStream(audioSrc);
            AudioInputStream audioStream = AudioSystem.getAudioInputStream(bufferedIn);
            
            bgmClip = AudioSystem.getClip();
            bgmClip.open(audioStream);
            
            // 무한 반복 설정
            bgmClip.loop(Clip.LOOP_CONTINUOUSLY);
            bgmClip.start();
            
            // 볼륨 조절 (선택 사항: -10.0f 정도로 낮추면 적당함)
            FloatControl gainControl = (FloatControl) bgmClip.getControl(FloatControl.Type.MASTER_GAIN);
            gainControl.setValue(-15.0f); 
            
        } catch (Exception e) {
            System.err.println("BGM 재생 중 오류 발생: " + e.getMessage());
        }
    }

    // 음악 정지 메서드 (사망 시 호출 가능)
    public void stopBGM() {
        if (bgmClip != null && bgmClip.isRunning()) {
            bgmClip.stop();
            bgmClip.close();
        }
    }
}