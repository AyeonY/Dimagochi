import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.net.URL;
import javax.imageio.ImageIO;

public class furniture {
    // 가구 종류 정의
    public enum FurnitureType {
        FLOWER_POT(0), // 화분
    	FLOWER_POT2(8), // 화분
    	FLOWER_POT3(9), // 화분
    	FLOWER_POT4(10), // 화분
    	FLOWER_POT5(11), // FLOWER_POT4
        BOOKCASE1(1),       // 책장
        BOOKCASE2(2),       // 책장2
        SHELF(3),     // 벽걸이 선반
    	TABLE(4), //책상
    	SMALLSHELF(5), //작은 선반
    	THINSHELF(6), //좁은 선반
    	TABLE2(7); //화분 테이블
    

        private final int index;
        FurnitureType(int index) { this.index = index; }
        public int getIndex() { return index; }
    }

    // 각 가구의 시트 내 위치 정보를 담는 내부 클래스
    public static class State {
        public int start_x, start_y, width, height;
    }

    private BufferedImage spriteSheet;
    private State[] states;
    private FurnitureType currentType;
    private int xPos, yPos; // 화면에 배치될 좌표

    public furniture(FurnitureType type, int x, int y) {
        this.currentType = type;
        this.xPos = x;
        this.yPos = y;
        loadImage();
        initStates();
    }
    //이미지 를 2배 크게 만들어서 반환. 
    public Image getScaledImage(int scale) {
        BufferedImage cropped = getCroppedImage();
        if (cropped == null) return null;

        // 현재 너비와 높이의 'scale'배 만큼 크기를 키움 (2배면 scale=2)
        int newWidth = cropped.getWidth() * scale;
        int newHeight = cropped.getHeight() * scale;

        // SCALE_SMOOTH를 사용하면 이미지가 깨지는 것을 방지하고 부드럽게 커집니다.
        return cropped.getScaledInstance(newWidth, newHeight, Image.SCALE_SMOOTH);
    }
    
    private void loadImage() {
        try {
            URL imgURL = getClass().getResource("/res/furniture.png");
            if (imgURL == null) throw new IOException("이미지를 찾을 수 없습니다.");
            spriteSheet = ImageIO.read(imgURL);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void initStates() {
        states = new State[FurnitureType.values().length];

        // 0. 화분 (좌표값은 예시입니다. 실제 시트에 맞게 수정하세요)
        State pot = new State();
        pot.start_x = 270; pot.start_y = 3; pot.width = 18; pot.height = 31;
        states[FurnitureType.FLOWER_POT.getIndex()] = pot;

        // 1. 책장
        State Bookcase1 = new State();
        Bookcase1.start_x = 81; Bookcase1.start_y = 2; Bookcase1.width = 31; Bookcase1.height = 48;
        states[FurnitureType.BOOKCASE1.getIndex()] = Bookcase1;

        // 2. 책장2
        State Bookcase2 = new State();
        Bookcase2.start_x = 47; Bookcase2.start_y = 2; Bookcase2.width = 33; Bookcase2.height = 48;
        states[FurnitureType.BOOKCASE2.getIndex()] = Bookcase2;

        // 3.벽걸이 선반
        State Shelf = new State();
        Shelf.start_x = 480; Shelf.start_y = 2; Shelf.width = 32; Shelf.height = 43;
        states[FurnitureType.SHELF.getIndex()] = Shelf;

        // 4. 책상
           State Table = new State();
           Table.start_x = 134; Table.start_y = 2; Table.width = 35; Table.height = 45;
           states[FurnitureType.TABLE.getIndex()] = Table;
           
        // 5.작은 선반
           State smallShelf = new State();
           smallShelf.start_x = 109; smallShelf.start_y = 45; smallShelf.width = 36; smallShelf.height = 36;
           states[FurnitureType.SMALLSHELF.getIndex()] = smallShelf;
           
           // 6.좁은 선반
           State thinShelf = new State();
           thinShelf.start_x = 174; thinShelf.start_y = 3; thinShelf.width = 35; thinShelf.height = 50;
           states[FurnitureType.THINSHELF.getIndex()] = thinShelf;
           
           // 7.화분 테이블
           State Table2 = new State();
           Table2.start_x = 351; Table2.start_y = 16; Table2.width = 33; Table2.height = 36;
           states[FurnitureType.TABLE2.getIndex()] = Table2;
           
           // 8. 화분2 
           State pot2 = new State();
           pot2.start_x = 287; pot2.start_y = 3; pot2.width = 17; pot2.height = 33;
           states[FurnitureType.FLOWER_POT2.getIndex()] = pot2;
  
           // 8. 화분3 
           State pot3 = new State();
           pot3.start_x = 304; pot3.start_y = 3; pot3.width = 17; pot3.height = 33;
           states[FurnitureType.FLOWER_POT3.getIndex()] = pot3;

           
           // 8. 화분4 
           State pot4 = new State();
           pot4.start_x = 319; pot4.start_y = 3; pot4.width = 16; pot4.height = 32;
           states[FurnitureType.FLOWER_POT4.getIndex()] = pot4;
           
        // 8. 화분5
           State pot5 = new State();
           pot5.start_x = 335; pot5.start_y = 3; pot5.width = 16; pot5.height = 33;
           states[FurnitureType.FLOWER_POT5.getIndex()] = pot5;



        
        
    }

    // 현재 타입에 맞는 이미지를 시트에서 잘라내어 반환
    public BufferedImage getCroppedImage() {
        if (spriteSheet == null) return null;
     // index 범위를 벗어나는지 확인
        int index = currentType.getIndex();
        if (index < 0 || index >= states.length || states[index] == null) {
            System.err.println("경고: " + currentType + "에 대한 좌표 설정(State)이 없습니다.");
            return null; // 여기서 null을 반환하면 상점에서 이미지가 안 보이고 넘어갑니다.
        }
        State s = states[currentType.getIndex()];
        return spriteSheet.getSubimage(s.start_x, s.start_y, s.width, s.height);
    }

    // Getters
    public int getX() { return xPos; }
    public int getY() { return yPos; }
    public int getWidth() { return states[currentType.getIndex()].width; }
    public int getHeight() { return states[currentType.getIndex()].height; }
}