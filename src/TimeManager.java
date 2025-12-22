import java.time.LocalDateTime;
import java.time.LocalTime;

/**
 * TimeManager.java
 * 현재 시스템 시간을 기반으로 낮/밤을 판단하고
 * 해당하는 배경 이미지 파일명을 결정하는 역할
 */
public class TimeManager {

    // --- 시간대 판단 기준 ---
    private static final LocalTime DAY_START = LocalTime.of(6, 0);    // 06:00
    private static final LocalTime NIGHT_START = LocalTime.of(18, 0);   // 18:00
    // 낮: 06:00 ~ 18:00 미만
    // 밤: 18:00 ~ 다음 날 06:00 미만

    /**
     * 현재 시스템의 시간에 따라 해당하는 배경 이미지 파일명을 반환
     * @return "/res/Day.png" 또는 "/res/Night.png"
     */
    public static String getBackgroundImagePath() {
        // 1. 현재 시간 정보 가져오기
        LocalDateTime now = LocalDateTime.now();
        LocalTime currentTime = now.toLocalTime();
        
        // 2. 시간대(낮/밤) 결정
        String timeOfDay = determineTimeOfDay(currentTime);
        
        // 3. 최종 파일 경로 구성
        return String.format("/res/%s.png", timeOfDay);
    }
    
    private static String determineTimeOfDay(LocalTime time) {
        // 06:00 이상 18:00 미만이면 낮, 그 외는 밤
        if ((time.equals(DAY_START) || time.isAfter(DAY_START)) && time.isBefore(NIGHT_START)) {
            return "Day";
        } else {
            return "Night";
        }
    }
    
    /**
     * 배경 변경 로직 테스트를 위한 Getter (디버깅용)
     */
    public static String getCurrentTimeOfDay() {
        return determineTimeOfDay(LocalDateTime.now().toLocalTime());
    }
}