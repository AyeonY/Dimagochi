import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        
        System.out.println("=== Dimagochi ===");
        System.out.print("다마고치의 이름을 지어주세요: ");
        String name = scanner.nextLine();
        
        
        Dimagotchi myPet = new Dimagotchi(name);

        while (!Dimagotchi.isAliveStatic()) {
            myPet.printStatus();
            System.out.println("1. 밥주기  2. 놀아주기  3. 재우기  4. 청소하기  5. 종료");
            System.out.print("선택 >> ");
            
            String input = scanner.nextLine();

            switch (input) {
                case "1":
                    myPet.feed();
                    break;
                case "2":
                    myPet.play();
                    break;
                case "3":
                    myPet.sleep();
                    break;
                case "4": 
                    myPet.clean();
                    break;
                case "5":
                    System.out.println("게임을 종료합니다.");
                    return;
                default:
                    System.out.println("잘못된 입력입니다.");
            }
        }
        
        System.out.println("\n=== 게임 오버 ===");
        if (!Dimagotchi.isAliveStatic()) {
            System.out.println("❌ 사망 원인: " + myPet.getCauseOfDeath()); //왜 죽었는지 
        }
        scanner.close();
    }
}