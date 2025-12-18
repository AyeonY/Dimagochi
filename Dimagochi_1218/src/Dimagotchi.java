public class Dimagotchi {
    private String name;
    
    private int hunger;
    private int happiness;
    private int energy;
    public static String causeOfDeath;//왜 죽었는지 저장. 
    private int count;// 진화를 위한 행동 카운텨.
    private boolean isEvolved;  // 진화 여부 필드 추가
    private String evolutionMessage; // 진화 메시지 필드 추가
    
    private int feedCount;
    private int playCount;
    private int sleepCount;
    private int cleanCount; 
    
    private int evolLevel=0; 
    
    private int coins; 
    
    private int hygiene; // 청결도 (0~100)
    private int flyCount; // 현재 있는 벌레 수
    
    private Character character; // Character 객체 추가
    
    public Dimagotchi(String name) {
        this.name = name;
        this.hunger = 50;
        this.happiness = 50;
        this.energy = 100;
        this.causeOfDeath ="";
        this.count = 0;
        this.isEvolved = false; 
        this.evolutionMessage = ""; 
        
        this.feedCount = 0;
        this.playCount = 0;
        this.sleepCount = 0;
        this.cleanCount = 0; 
        
        this.coins = 0; //  초기 코인 0개로 시작
        
     // Character 객체 초기화 (DimagotchiGUI의 화면 크기 800x500 고려)
        this.character = new Character(350, 300);
        
    }

    private int foodCount = 3; // 초기 먹이 개수 3개
    
    public int getFoodCount() { return foodCount; }
    public void addFood(int amount) { this.foodCount += amount; }
    
    public String feed() {
     if (!causeOfDeath.isEmpty()) return "다마고치가 밥을 먹지 않습니다....";
     
     // 먹이가 있는지 확인
     if (foodCount <= 0) {
         return "먹이가 부족합니다! TV 미니게임을 플레이하세요.";
     }

     this.foodCount--; // 먹이 소모
     this.hunger = Math.max(0, hunger - 20);
     this.energy = Math.min(100, energy + 5); 
     this.feedCount++;
     passTime();
     
     return "냠냠! 밥을 먹었습니다. (남은 먹이: " + foodCount + "개)";
 }

    public String play() {
        if (!causeOfDeath.isEmpty()) return "반응이 없습니다...";
        if (energy < 20) {
            System.out.println("너무 피곤해서 놀 수 없어요.");
            return "너무 피곤해서 놀 수 없어요.";
        }
        System.out.println("energy:"+energy);
        this.happiness = Math.min(100, happiness + 15);
        this.energy = Math.max(0, energy - 20);
        System.out.println("energy:"+energy);
        
        System.out.println(hunger);
        this.hunger = Math.min(100, hunger + 10); 
        this.playCount++;
        passTime();
        
        System.out.println("신나게 놀았습니다!");
        return "신나게 놀았습니다!";
    }

    public String sleep() {
        if (!causeOfDeath.isEmpty()) return "영원히 잠들었습니다...";
        this.energy =  Math.min(100, energy +20);
        passTime();
        
        this.sleepCount++;
        System.out.println("쿨쿨... 잠을 자고 에너지를 채웠습니다.");
        return "쿨쿨... 잠을 자고 에너지를 채웠습니다.";
    }
    
    // 💡 [추가] 청소하기 메서드
    public String clean() {
        if (!causeOfDeath.isEmpty()) return "반응이 없습니다...";
        
        // 청소 효과: 행복도 증가, 에너지 소모
        this.happiness = Math.min(100, happiness + 10);
        this.energy = Math.max(0, energy - 10);
        
        this.hygiene = 100;
        
        this.cleanCount++;
        passTime();
        
        System.out.println("쓱싹쓱싹! 깨끗하게 청소했습니다.");
        return "쓱싹쓱싹! 깨끗하게 청소했습니다.";
    }

    private void passTime() {
    	//어떤 행동을 하면서 자동적으로 줄어드는 기본 패널티 
    	this.count++;
        this.hunger -= 5;
        this.happiness -= 5;
        
        // 벌레가 있으면 청결도가 떨어짐
        if (flyCount > 0) {
            this.hygiene = Math.max(0, this.hygiene - (flyCount * 2));
        }
        
        // 청결도가 낮으면 행복도가 추가로 떨어짐 (악취 패널티)
        if (this.hygiene < 50) {
            this.happiness -= 2;
        }
        
        checkStatus();
        checkEvolution(); // 진화 여부 확인
    }

    private void checkStatus() {
    	if(causeOfDeath.isEmpty()) {
    		//hunger가 100이넘고, happiness가 0으로 되면 
        	// 아사 조건 변경
        	if(hunger>=100) { 
        		Dimagotchi.causeOfDeath ="［사망원인］:아사";
        	}
        	else if(happiness<=0){
        		Dimagotchi.causeOfDeath ="［사망원인］:고독사";
        	}
        	// 탈진사 조건 추가
        	else if(energy<=0){ 
        		Dimagotchi.causeOfDeath ="［사망원인］:탈진사";
        	}
    	}
    }
 // Character.java가 사용할 정적 메소드 (Dimagotchi.isAliveStatic() 호출)
    public static boolean isAliveStatic() {
        // causeOfDeath가 비어있으면(isEmpty) 아직 살아있는 상태(true)
        // 비어있지 않고 값이 채워져 있으면 사망 상태(false)
        return causeOfDeath.isEmpty();
    }
    
    private void checkEvolution() {
        // evolLevel이 2 미만이고, 행동 횟수(count)가 8번 이상일 때 진화 시도
        if (evolLevel < 2 && count >= 8 && Dimagotchi.isAliveStatic()) { 
            evolLevel++; // 진화 레벨 상승 (1: 중간, 2: 최종)
            
            // Character 객체의 evolve 메서드 호출
            character.evolve(this, happiness, evolLevel); 
            
            // 다음 진화 단계를 위해 카운터들 초기화
            count = 0; 
            // 1단계에서 2단계로 갈 때의 성향을 새로 측정하기 위해 행동 카운트 초기화
            this.feedCount = 0;
            this.playCount = 0;
            this.sleepCount = 0;
            this.cleanCount = 0;

            evolutionMessage = "✨ " + name + "이(가) " + evolLevel + "단계로 성장했습니다! ✨";
        }
    }
    
    public void printStatus() {
        //String moodFace = happiness > 70 ? "(^‿^)" : (happiness > 30 ? "(•_•)" : "(T_T)");
        
    	String moodFace ="";
        System.out.println("\n-------------------------");
        System.out.println(" 이름: " + name + " " + moodFace);
        System.out.println(" 배고픔: " + drawBar(hunger, true));
        System.out.println(" 행복도: " + drawBar(happiness, false));
        System.out.println(" 에너지: " + drawBar(energy, false));
        System.out.println(" 청결도: " + drawBar(100 - hygiene, true));
        System.out.println("-------------------------");
    }

    //시각적으로 텍스트 막대로 변화해 콘솔에 추가하기 위해서
    private String drawBar(int value, boolean isBadStat) {
        StringBuilder bar = new StringBuilder("[");
        int count = value / 10; 
        for (int i = 0; i < 10; i++) {
            if (i < count) bar.append(isBadStat ? "■" : "■"); 
            else bar.append(" ");
        }
        bar.append("] " + value);
        return bar.toString();
    }

    
    public Character getCharacter() { return character; }
    public int getXPos() { return character.getXPos(); } // Character로 위임
    public int getYPos() { return character.getYPos(); } // Character로 위임
    
 // Getter 추가
    public int getFeedCount() { return feedCount; }
    public int getPlayCount() { return playCount; }
    public int getSleepCount() { return sleepCount; }
    public int getCleanCount() { return cleanCount; } //  Getter
    
    public int getHunger() { return hunger; }
    public int getHappiness() { return happiness; }
    public int getEnergy() { return energy; }
    //public boolean isAlive() { return isAlive; }
    public String getName() { return name; }
    
    public String getCauseOfDeath() { return causeOfDeath;}// 왜 죽었는지 원인 출력. 
    
    public String getEvolutionMessage() { return evolutionMessage; } // 진화 메시지 Getter
    public void resetEvolutionMessage() { this.evolutionMessage = ""; } // 메시지 초기화
    
    // 코인 관련 메서드
    public int getCoins() { return coins; }
    
    public boolean spendCoins(int amount) {
        if (coins >= amount) {
            coins -= amount;
            return true;
        }
        return false;
    }
    
    public void addCoins(int amount) {
        coins += amount;
}
    
    // 벌레 및 청결도 관련 메서드
    public int getHygiene() { return hygiene; }
    
    public void addFly() {
        this.flyCount++;
        // 벌레가 늘어나면 청결도가 즉시 조금 깎임
        this.hygiene = Math.max(0, hygiene - 5);
    }
    
    public String catchFly() {
        if (flyCount > 0) {
            flyCount--;
            // 벌레를 잡으면 청결도 약간 회복 및 코인 획득(보너스)
            hygiene = Math.min(100, hygiene + 5);
            addCoins(5); 
            return "벌레를 잡았다! (5G 획득)";
        }
        return "";
    }
    
    public int getFlyCount() { return flyCount; }

}