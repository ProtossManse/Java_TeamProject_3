import data.User;
import util.Path;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Scanner;
import java.nio.charset.StandardCharsets;

public class VocabularyAppLauncher {
    Scanner scanner = new Scanner(System.in, StandardCharsets.UTF_8);
    ArrayList<User> users = new ArrayList<>();

    public void start() {
        initializeSystemResources();
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

    /**
     * 애플리케이션 시작 시 공용 리소스(폴더, 기본 파일)가 존재하는지 확인하고
     * 없는 경우 생성합니다.
     */
    private void initializeSystemResources() {
        try {
            // 1. 공용 단어장 폴더 생성 (res/public/vocas)
            File publicVocaDir = new File(Path.getPublicDirPath());
            if (!publicVocaDir.exists()) {
                publicVocaDir.mkdirs();
                System.out.println("기본 공용 단어장 폴더(res/public/vocas)를 생성했습니다.");
            }

            // 2. 공용 단어장 파일 생성 (예: 'publics.txt') //일단 기본 규칙대로 public+s 하긴 했는데 의미 상 public이
            // 맞아 보이긴 합니다.
            // (이 파일이 있어야 '공용 단어장 관리' 메뉴가 비어있지 않습니다.)
            File defaultPublicFile = new File(Path.getPublicFilePath());
            if (!defaultPublicFile.exists()) {
                defaultPublicFile.createNewFile();

                // (선택 사항) 기본 파일에 예시 내용을 추가합니다.
                // try (PrintWriter pw = new PrintWriter(
                // new OutputStreamWriter(new FileOutputStream(defaultPublicFile, false),
                // StandardCharsets.UTF_8))) {
                // pw.println("apple\t사과");
                // pw.println("banana\t바나나");
                // pw.println("computer\t컴퓨터");
                // }
                System.out.println("공용 단어 파일(publics.txt)을 생성했습니다.");
            }
        } catch (IOException e) {
            System.out.println("시스템 리소스 초기화 중 오류 발생: " + e.getMessage());
        }
    }

    private void login() {
        readUsers();
        if (users.isEmpty()) {
            createUser();
        }
        String choice;
        while (true) {
            System.out.println("==== 계정 목록 ====");
            for (User user : users) {
                System.out.println(user.getName());
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

            User foundUser = null;
            for (User user : users) {
                if (choice.equals(user.getName())) {
                    foundUser = user;
                    break;
                }
            }
            if (foundUser == null) {
                System.out.println("이름을 다시 입력해주세요.");
                continue;
            }

            System.out.print("비밀번호를 입력해주세요: ");
            String password = scanner.next();
            scanner.nextLine();

            if (!password.equals(foundUser.getPassword())) {
                System.out.println("비밀번호를 다시 입력해주세요.");
                continue;
            }
            System.out.println("로그인 성공!");
            try (PrintWriter userDataFile = new PrintWriter(
                    new OutputStreamWriter(new FileOutputStream(Path.getUsersFilePath(), false),
                            StandardCharsets.UTF_8))) {
                for (User user : users) {
                    if (user.equals(foundUser)) {
                        int streak = countStreak(foundUser);
                        userDataFile.printf("%s\t%s\t%d\t%s\r\n", foundUser.getName(), foundUser.getPassword(), streak,
                                LocalDate.now());
                        foundUser.setStreak(streak);
                    } else
                        userDataFile.printf("%s\t%s\t%d\t%s\r\n", user.getName(), user.getPassword(), user.getStreak(),
                                user.getLastDate());
                }

            } catch (FileNotFoundException ignored) {
                System.out.println("users 파일을 찾을 수 없습니다.");
            }

            VocabularyApp app = new VocabularyApp(foundUser);
            app.menu();
            return;
        }

    }

    private void readUsers() {
        users.clear();
        File usersFile = new File(Path.getUsersFilePath());
        if (!usersFile.exists()) {
            return;
        }
        try (Scanner file = new Scanner(usersFile, StandardCharsets.UTF_8.name())) {
            while (file.hasNextLine()) {
                String[] data = file.nextLine().split("\t");
                if (data.length < 4)
                    continue;
                users.add(new User(data[0].trim(), data[1].trim(), Integer.parseInt(data[2].trim()),
                        LocalDate.parse(data[3].trim())));
            }

        } catch (FileNotFoundException ignored) {
        }
    }

    private void createUser() {
        System.out.println("==== 새 프로필 추가 ====");
        System.out.print("이름 입력: ");
        String name = scanner.next();
        scanner.nextLine();
        for (User user : users) {
            if (name.equals(user.getName())) {
                System.out.println("이미 존재하는 사용자입니다!");
                return;
            }
        }
        System.out.print("비밀번호 입력: ");
        String password = scanner.next();
        scanner.nextLine();
        File userDir = new File(Path.getUserDirPath(name));
        LocalDate date = LocalDate.now();
        try {
            // 1. res/username/ 폴더 생성
            userDir.mkdirs();

            // 2. res/username/vocas/ 폴더 생성
            File vocasDir = new File(Path.getVocaDirPath(name));
            vocasDir.mkdirs();

            // 3. res/username/notes/ 폴더 생성
             File notesDir = new File(Path.getNoteDirPath(name));
             notesDir.mkdirs();

            // 4. res/username/favorites/ 폴더 생성
            File favDir = new File(Path.getFavoriteDirPath(name));
            favDir.mkdirs();

            // 5. res/username/favorites/_favorites.txt 빈 파일 생성
            File favFile = new File(Path.getFavoriteFilePath(name));
            favFile.createNewFile(); // IOException이 발생할 수 있으므로 try 블록 안이 맞습니다.
            try (PrintWriter usersDataPrintWriter = new PrintWriter(
                    new OutputStreamWriter(new FileOutputStream(Path.getUsersFilePath(), false),
                            StandardCharsets.UTF_8))) {
                usersDataPrintWriter.println(name + '\t' + password + '\t' + "1" + '\t' + date);
            }
        } catch (IOException e) {
            System.out.println("알 수 없는 에러 발생: " + e.getMessage());
        }

        users.add(new User(name, password, 1, date));
    }

    private int countStreak(User user) {
        int currentStreak = user.getStreak();
        if (user.getLastDate().equals(LocalDate.now())) {
            return currentStreak;
        } else if (user.getLastDate().plusDays(1).equals(LocalDate.now())) {
            return currentStreak + 1;
        } else {
            return 1;
        }

    }

}
