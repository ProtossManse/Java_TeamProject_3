import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Scanner;

public class VocabularyAppLauncher {
    Scanner scanner = new Scanner(System.in);
    HashMap<String, String> userData = new HashMap<>();
    static final String USERS_DATA_FILE_NAME = "res/users.txt";

    public void start() {
        int choice = 0;
        while (choice != 2) {

            System.out.println("========== 단어장 앱 ==========");

            System.out.println("1. 로그인하기");
            System.out.println("2. 종료하기");
            System.out.print(">> ");

            choice = scanner.nextInt();
            scanner.nextLine();
            switch (choice) {
                case 1 -> {
                    login();
                    return;
                }
                case 2 -> System.out.println("단어장 런처를 종료합니다.");
                default -> System.out.println("다시 입력해주세요.");
            }
        }
    }

    private void login() {
        readUsers();
        if (userData.isEmpty()) {
            createUser();
        }
        String choice = "";
        while (true) {
            System.out.println("==== 계정 목록 ====");
            for (String name : userData.keySet()) {
                System.out.println(name);
            }
            System.out.print("로그인 할 계정 이름을 입력하세요(q 입력시 종료, n 입력시 새 프로필 추가): ");
            choice = scanner.next();
            scanner.nextLine();

            if (choice.equals("q") || choice.equals("Q")) {
                System.out.println("단어장 런처를 종료합니다.");
                return;
            }

            if (choice.equals("n") || choice.equals("N")) {
                createUser();
                continue;
            }

            if (!userData.containsKey(choice)) {
                System.out.println("이름을 다시 입력해주세요.");
                continue;
            }

            System.out.print("비밀번호를 입력해주세요: ");
            String password = scanner.next();
            scanner.nextLine();

            if (!password.equals(userData.get(choice))) {
                System.out.println("비밀번호를 다시 입력해주세요.");
                continue;
            }
            System.out.println("로그인 성공!");
            VocabularyMenu app = new VocabularyMenu(choice);
            app.menu();
            return;
        }

    }

    private void readUsers() {
        try (Scanner file = new Scanner(new File(USERS_DATA_FILE_NAME))) {
            while (file.hasNextLine()) {
                String[] data = file.nextLine().split("\t");
                userData.put(data[0].trim(), data[1].trim());
            }

        } catch (FileNotFoundException ignored) {

        }
    }

    private void createUser() {
        System.out.println("==== 새 프로필 추가 ====");
        System.out.print("이름 입력: ");
        String name = scanner.next();
        scanner.nextLine();
        if (userData.containsKey(name)) {
            System.out.println("이미 존재하는 이름입니다!");
            return;
        }
        System.out.print("비밀번호 입력: ");
        String password = scanner.next();
        scanner.nextLine();
        File usersDataFile = new File(USERS_DATA_FILE_NAME);
        try {
            usersDataFile.createNewFile();
            try (PrintWriter usersDataPrintWriter = new PrintWriter(USERS_DATA_FILE_NAME)) {
                usersDataPrintWriter.println(name + '\t' + password);
            }

        } catch (IOException e) {
            System.out.println("알 수 없는 에러 발생");
        }


        userData.put(name, password);


    }

}
