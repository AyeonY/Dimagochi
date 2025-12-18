/**
 * Character.java
 * 캐릭터의 위치, 상태, 자동 움직임, 이미지 경로, 자세 정보를 관리합니다.
 */

import java.util.Random;
import java.awt.Image;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import javax.imageio.ImageIO;
import java.io.IOException;
import java.net.URL;

public class Character {
    
    // --- Enums ---
    public enum EvolutionType { EGG, DOG, CAT,SQUIRREL,RABBIT,BLOBFISH,EGG_M,EGG_M_B}
    
    public enum PetMood {
        EGG(0),
        DOG(1),
        CAT(2),
    	SQUIRREL(3),
    	RABBIT(4),
       	BLOBFISH(5),
       	EGG_M(6),
    	EGG_M_B(7);
    	
        private final int index;

        PetMood(int index) {
            this.index = index;
        }

        public int getIndex() {
            return index;
        }
    }
    // --- 핵심 상태 및 위치 필드 ---
    private EvolutionType type;
    private PetMood currentMood; 
    private int xPos;
    private int yPos;
    
	// 💡 [추가] 모션 상태를 토글하기 위한 변수
	private boolean isWalking = false;
	
    // --- 이미지 및 자세 관리 필드 ---
    private State[] states; 
    private BufferedImage eggImage;
    private BufferedImage dogAtlas; 
    private BufferedImage catAtlas; 
    private BufferedImage deadImage;
    private BufferedImage squirrelAtlas;
    private BufferedImage rabbitAtlas;
    private BufferedImage BlobfishImage;
    
    private BufferedImage eggMImage;
    private BufferedImage eggM_BImage;
    
    private Random random;

    // 알 상태의 자동 움직임을 위한 상태
    private int directionX; 
    
    // --- Constants ---
    // 💡 [유지] 사용자가 설정한 좌표값 그대로 유지
    private static final int MIN_X = 200; 
    private static final int MAX_X = 500; 
    
    private static final int MAX_Y = 380;
    private static final int EGG_MOVE_DISTANCE = 5;
    private static final int TARGET_FRAME_WIDTH = 100; 
    
    public Character(int initialX, int initialY) {
        this.random = new Random();
        
        initStates();
        loadImage();
        
        this.xPos = initialX; 
        this.yPos = initialY; 
        this.type = EvolutionType.EGG;
        this.currentMood = PetMood.EGG;
        
        if (random.nextBoolean()) {
            this.directionX = EGG_MOVE_DISTANCE;
        } else {
            this.directionX = -EGG_MOVE_DISTANCE;
        }
    }
    private void loadImage() {
        try {
            eggImage = loadResource("/res/egg.png");
            dogAtlas = loadResource("/res/dog.png");
            catAtlas = loadResource("/res/cat.png");
            squirrelAtlas = loadResource("/res/Squirrel.png");
            rabbitAtlas = loadResource("/res/Rabbit.png");
            deadImage = loadResource("/res/dead.png");
            BlobfishImage = loadResource("/res/Blobfish.png");
            
            eggMImage = loadResource("/res/egg-m.png");
            eggM_BImage = loadResource("/res/egg-m-wrong.png");
            
        } catch (IOException e) {
            System.err.println("캐릭터 이미지 리소스를 로드하는 데 실패했습니다.");
            e.printStackTrace();
        }
    }
    
    private BufferedImage loadResource(String path) throws IOException {
        URL imgURL = getClass().getResource(path);
        if (imgURL == null) throw new IOException("이미지 파일을 찾을 수 없습니다: " + path);
        return ImageIO.read(imgURL);
    }
    
    private void initStates() {
        states = new State[8]; 
        
        // 0. 알 (EGG)
        State state = new State();
        states[PetMood.EGG.getIndex()] = state; 
        state.width = 32; state.height = 32; 
        state.start_x = 0; state.start_y = 0;
        state.frame_size = 1;
        
        // 1. 개 모션
        state = new State();
        states[PetMood.DOG.getIndex()] = state; 
        state.width = 32; state.height = 32; 
        state.start_x = 0; state.start_y = 0;
        state.frame_size = 2; 

        // 2. 고양이 모션
        state = new State();
        states[PetMood.CAT.getIndex()] = state;
        state.width = 32; state.height = 32; 
        state.start_x = 0; state.start_y = 0;
        state.frame_size = 2; 
       
         // 3.다람쥐 모션
        state = new State();
        states[PetMood.SQUIRREL.getIndex()] = state;
        state.width = 32; state.height = 32; 
        state.start_x = 64; state.start_y = 0;
        state.frame_size = 3; 
        
        // 4.토끼 모션
        state = new State();
        states[PetMood.RABBIT.getIndex()] = state;
        state.width = 32; state.height = 32; 
        state.start_x = 0; state.start_y = 0;
        state.frame_size = 3; 
        
        // 5.블롭피쉬 모션
        state = new State();
        states[PetMood.BLOBFISH.getIndex()] = state;
        state.width = 32; state.height = 32; 
        state.start_x = 0; state.start_y = 0;
        state.frame_size = 2; 
        
        // 6.알중간 모션
        state = new State();
        states[PetMood.EGG_M.getIndex()] = state;
        state.width = 32; state.height = 32; 
        state.start_x = 0; state.start_y = 0;
        state.frame_size = 2;
        
        // 7.알 블롭피쉬 중간 모션
        state = new State();
        states[PetMood.EGG_M_B.getIndex()] = state;
        state.width = 32; state.height = 32; 
        state.start_x = 0; state.start_y = 0;
        state.frame_size = 2;
    }
    
    /**
     * 자동으로 x축 위치를 업데이트합니다.
     */
    public void updateMovement() {
    	if (!Dimagotchi.isAliveStatic()) {
            return; 
        }
    	
        if (type == EvolutionType.EGG||
        		type ==EvolutionType.DOG||
        		type ==EvolutionType.CAT||
                type ==EvolutionType.SQUIRREL||
        		type ==EvolutionType.RABBIT||
        		type ==EvolutionType.BLOBFISH||
        		type ==EvolutionType.EGG_M||
        		type ==EvolutionType.EGG_M_B
        		) {
            xPos += directionX;

            // 💡 [유지] 사용자가 요청한 범위(MIN_X ~ MAX_X) 유지
            if (xPos >= MAX_X || xPos <= MIN_X) { 
                directionX *= -1;
                
                if (xPos > MAX_X) xPos = MAX_X;
                if (xPos < MIN_X) xPos = MIN_X;
            }
            
            if (type == EvolutionType.DOG) currentMood = PetMood.DOG;
            else if (type == EvolutionType.CAT) currentMood = PetMood.CAT;
            else if (type == EvolutionType.SQUIRREL) currentMood = PetMood.SQUIRREL;
            else if (type == EvolutionType.BLOBFISH) currentMood = PetMood.BLOBFISH;
            else if (type == EvolutionType.CAT) currentMood = PetMood.RABBIT;
            else if (type == EvolutionType.EGG_M) currentMood = PetMood.EGG_M;
            else if(type == EvolutionType.EGG_M_B) currentMood = PetMood.EGG_M_B;
        }
    }

    public void performActionMove(int dx, int dy) {
        if (type == EvolutionType.DOG) {
            this.currentMood = PetMood.DOG; 
            move(dx, dy); 
        } else if (type == EvolutionType.CAT) {
            this.currentMood = PetMood.CAT;
            move(dx, dy); 
        } else {
            move(dx/2, dy/2); 
        }
    }

    private void move(int dx, int dy) {
        this.xPos = this.xPos + dx;
        this.yPos = this.yPos + dy;

        this.xPos = Math.max(0, Math.min(MAX_X, this.xPos));
        this.yPos = Math.max(0, Math.min(MAX_Y, this.yPos));
    }
    
    /**
     * 다마고치를 행동 빈도에 따라 진화시킵니다.
     */
    public void evolve(Dimagotchi pet, int happiness, int evolLevel) { 
    	int feed = pet.getFeedCount();
        int play = pet.getPlayCount();
        int sleep = pet.getSleepCount();
        int clean = pet.getCleanCount(); // 💡 [추가] 청소 횟수 가져오기
        
        System.out.println("feed:"+feed);
        System.out.println("play:"+play);
        System.out.println("sleep:"+sleep);
        System.out.println("clean:"+clean);
        System.out.println("evolLevel :"+evolLevel);
        
        if(evolLevel==1) {
        	if(happiness<=10) {
        		this.type = EvolutionType.EGG_M_B;
                this.currentMood = PetMood.EGG_M_B;
        	}else {
        		this.type = EvolutionType.EGG_M;
                this.currentMood = PetMood.EGG_M;
        	}
        } else {
            // 💡 [수정] 가장 높은 카운터를 찾아서 진화 (청소 포함)
            
            // 1. 청소가 가장 많으면 -> 토끼
            if (clean >= feed && clean >= play && clean >= sleep) {
                this.type = EvolutionType.RABBIT;
                this.currentMood = PetMood.RABBIT;
            }
            // 2. 놀기가 가장 많으면 -> 다람쥐
            else if (play >= feed && play >= sleep && play >= clean) {
                this.type = EvolutionType.SQUIRREL;
                this.currentMood = PetMood.SQUIRREL;
            }
            // 3. 잠이 가장 많으면 -> 고양이
            else if (sleep >= feed && sleep >= play && sleep >= clean) {
                this.type = EvolutionType.CAT;
                this.currentMood = PetMood.CAT;
            } 
            // 4. 그 외(밥이 많거나 기본) -> 강아지
            else {
                this.type = EvolutionType.DOG; 
                this.currentMood = PetMood.DOG;
            }
        }
    }

    public Image getCurrentImage() {
        State currentState = states[currentMood.getIndex()];
        BufferedImage sourceAtlas = null;
        
        boolean isAlive = Dimagotchi.isAliveStatic(); 
        
        if (!isAlive) { 
             sourceAtlas = deadImage;
        } else if (type == EvolutionType.EGG) {
            sourceAtlas = eggImage;
        } else if (type == EvolutionType.DOG) {
            sourceAtlas = dogAtlas;
        } else if (type == EvolutionType.CAT) {
        	 sourceAtlas = catAtlas;
        } else if (type == EvolutionType.SQUIRREL) {
            sourceAtlas = squirrelAtlas;
        } else if (type == EvolutionType.RABBIT) {
            sourceAtlas = rabbitAtlas;
        } else if (type == EvolutionType.EGG_M) {
            sourceAtlas = eggMImage;
        } else if (type == EvolutionType.EGG_M_B) {
            sourceAtlas = eggM_BImage;
        }
        
        if (sourceAtlas == null) return null;

        if (type == EvolutionType.EGG || !isAlive) {
            return sourceAtlas.getScaledInstance(
                TARGET_FRAME_WIDTH, TARGET_FRAME_WIDTH, Image.SCALE_SMOOTH
            );
        }

        int frameIndex = 0;
        
        if ((type == EvolutionType.DOG && currentMood == PetMood.DOG) || 
            (type == EvolutionType.CAT && currentMood == PetMood.CAT) ||
            (type == EvolutionType.EGG_M && currentMood == PetMood.EGG_M) ||
            (type == EvolutionType.EGG_M_B && currentMood == PetMood.EGG_M_B)) {
            
            currentState.index = (int)(System.currentTimeMillis() / 1000) % currentState.frame_size;
            frameIndex = currentState.index;
        } 
        
        int cropX = currentState.start_x + (frameIndex * currentState.width); 
        
        BufferedImage croppedImage = sourceAtlas.getSubimage(
            cropX, 
            currentState.start_y,
            currentState.width,
            currentState.height 
        );

        BufferedImage finalImage = croppedImage;
        
        if (directionX > 0) {
            AffineTransform tx = AffineTransform.getScaleInstance(-1, 1);
            tx.translate(-croppedImage.getWidth(), 0);
            AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_NEAREST_NEIGHBOR);
            finalImage = op.filter(croppedImage, null);
        }

        return finalImage.getScaledInstance(
            TARGET_FRAME_WIDTH, 
            TARGET_FRAME_WIDTH, 
            Image.SCALE_SMOOTH
        );
     }
 
    public int getXPos() { return xPos; }
    public int getYPos() { return yPos; }
    public EvolutionType getType() { return type; } 
}