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
    // 사용자 입력을 받기 위한 스캐너 객체 (한글 깨짐 방지를 위해 UTF-8 설정)

    ArrayList<User> users = new ArrayList<>();
    // 파일에서 읽어온 사용자 정보들을 메모리에 저장해 둘 리스트

    public void start() {
        initializeSystemResources();
        // 프로그램 시작 시 공용 단어장 폴더와 파일이 있는지 확인하고, 없으면 생성하는 메서드

        int choice = 0;
        while (choice != 2) {
            System.out.println("========== 단어장 앱 ==========");
            System.out.println("1. 로그인하기");
            System.out.println("2. 종료하기");
            System.out.print(">> ");

            try {
                String input = scanner.nextLine().trim();
                // 입력 버퍼 오류를 방지하기 위해 nextLine으로 한 줄을 읽고 공백 제거

                choice = Integer.parseInt(input);
                // 읽어온 문자열을 숫자로 변환 시도
            } catch (NumberFormatException e) {
                System.out.println("숫자만 입력해주세요.");
                choice = -1;
                // 숫자가 아닌 값을 입력했을 경우 에러 메시지를 띄우고 다시 입력받음
            }

            switch (choice) {
                case 1 -> {
                    login();
                    // 로그인 화면으로 이동
                    return;
                    // 로그인이 성공하고 보카앱이 종료되면 런처도 함께 종료
                }
                case 2 -> System.out.println("단어장 런처를 종료합니다.");
                default -> System.out.println("다시 입력해주세요.");
            }
        }
    }

    private void initializeSystemResources() {
        try {
            File publicVocaDir = new File(Path.getPublicDirPath());
            // 공용 단어장 디렉토리 경로 객체 생성

            if (!publicVocaDir.exists()) {
                if (publicVocaDir.mkdirs()) {
                    System.out.println("기본 공용 단어장 폴더(res/public/vocas)를 생성했습니다.");
                    // 폴더가 없으면 새로 생성함
                }
            }

            File defaultPublicFile = new File(Path.getPublicFilePath());
            // 공용 단어장 기본 파일(publics.txt) 경로 객체 생성

            if (!defaultPublicFile.exists()) {
                if (defaultPublicFile.createNewFile()) {
                    System.out.println("공용 단어 파일(publics.txt)을 생성했습니다.");
                    // 파일이 없으면 새로 생성함
                }
            }
        } catch (IOException e) {
            System.out.println("시스템 리소스 초기화 중 오류 발생: " + e.getMessage());
            // 파일 시스템 권한 문제 등으로 생성 실패 시 에러 출력
        }
    }

    private void login() {
        readUsers();
        // users.txt 파일에서 사용자 목록을 읽어와 users 리스트에 저장

        if (users.isEmpty()) {
            createUser();
            // 등록된 사용자가 한 명도 없다면 즉시 회원가입 절차 진행
        }

        String choice;
        while (true) {
            System.out.println("==== 계정 목록 ====");
            for (User user : users) {
                System.out.println(user.getName());
                // 현재 등록된 모든 username을 출력
            }
            System.out.print("로그인 할 계정 이름을 입력하세요(q 입력시 종료, n 입력시 새 프로필 추가): ");
            choice = scanner.nextLine().trim();
            // 사용자 선택 입력 받기

            if (choice.equalsIgnoreCase("q")) {
                System.out.println("단어장 런처를 종료합니다.");
                return;
                // q 입력 시 프로그램 종료
            }

            if (choice.equalsIgnoreCase("n")) {
                createUser();
                // n 입력 시 신규 사용자 생성 화면으로 이동
                continue;
            }

            User foundUser = null;
            for (User user : users) {
                if (choice.equals(user.getName())) {
                    foundUser = user;
                    break;
                    // 입력한 이름과 일치하는 사용자를 리스트에서 찾음
                }
            }

            if (foundUser == null) {
                System.out.println("이름을 다시 입력해주세요.");
                continue;
                // 일치하는 사용자가 없으면 루프 처음으로 돌아가 다시 입력받음
            }

            System.out.print("비밀번호를 입력해주세요: ");
            String password = scanner.nextLine().trim();
            // 비밀번호 입력 받기

            if (!password.equals(foundUser.getPassword())) {
                System.out.println("비밀번호가 일치하지 않습니다.");
                continue;
                // 비밀번호 틀릴 경우 문구 출력
            }

            System.out.println("로그인 성공!");

            updateUserStreakAndSave(foundUser);
            // 로그인 성공 처리: 연속 접속일(Streak)을 갱신하고 파일에 저장

            VocabularyApp app = new VocabularyApp(foundUser);
            // 보카앱 객체 생성 (현재 로그인한 사용자 정보 전달)

            app.menu();
            // 보카앱 메뉴 실행
            return;
        }
    }

    private void readUsers() {
        users.clear();
        // 리스트를 비워 중복 로드 방지

        File usersFile = new File(Path.getUsersFilePath());
        if (!usersFile.exists()) {
            return;
            // 사용자 정보 파일이 없으면 읽지 않고 종료
        }

        try (Scanner file = new Scanner(usersFile, StandardCharsets.UTF_8.name())) {
            // 파일을 UTF-8로 읽는 스캐너 생성

            while (file.hasNextLine()) {
                String line = file.nextLine();
                String[] data = line.split("\t");
                // 탭(Tab) 으로 구분된 데이터를 분리

                if (data.length < 4)
                    continue;
                // 데이터가 불완전하면(이름, 비번, 스트릭, 날짜 중 하나라도 없으면) 건너뜀

                try {
                    users.add(new User(data[0].trim(), data[1].trim(), Integer.parseInt(data[2].trim()),
                            LocalDate.parse(data[3].trim())));
                    // 파싱한 정보로 User 객체 생성하여 리스트에 추가
                } catch (Exception e) {
                    System.out.println("손상된 사용자 데이터가 있어 건너뜁니다: " + line);
                    // 날짜 형식 등이 잘못된 경우 에러 로그 출력 후 계속 진행
                }
            }
        } catch (FileNotFoundException ignored) {
            // 위에서 exists() 체크를 했으므로 발생 확률 거의 없늠
        }
    }

    private void createUser() {
        System.out.println("==== 새 프로필 추가 ====");
        System.out.print("이름 입력: ");
        String name = scanner.nextLine().trim();

        for (User user : users) {
            if (name.equals(user.getName())) {
                System.out.println("이미 존재하는 사용자입니다!");
                return;
                // 중복된 ID 방지
            }
        }

        System.out.print("비밀번호 입력: ");
        String password = scanner.nextLine().trim();

        File userDir = new File(Path.getUserDirPath(name));
        LocalDate date = LocalDate.now();

        try {
            userDir.mkdirs();
            // 1. 사용자용 메인 폴더 생성

            new File(Path.getVocaDirPath(name)).mkdirs();
            // 2. 단어장 폴더 생성

            new File(Path.getNoteDirPath(name)).mkdirs();
            // 3. 오답노트 폴더 생성

            new File(Path.getFavoriteDirPath(name)).mkdirs();
            // 4. 즐겨찾기 폴더 생성

            File favFile = new File(Path.getFavoriteFilePath(name));
            favFile.createNewFile();
            // 5. 빈 즐겨찾기 파일(_favorites.txt) 미리 생성

            // 사용자 정보 파일(users.txt)에 새 사용자 추가 (이어쓰기 모드)
            try (PrintWriter usersDataPrintWriter = new PrintWriter(
                    new OutputStreamWriter(new FileOutputStream(Path.getUsersFilePath(), true),
                            StandardCharsets.UTF_8))) {
                // 이어쓰기(append) 모드로 스트림 열기 (기존 사용자 정보 보존 위해서!)
            }

        } catch (IOException e) {
            System.out.println("사용자 폴더/파일 생성 중 오류 발생: " + e.getMessage());
            // 파일 생성 실패 시 에러 출력
        }

        User newUser = new User(name, password, 1, date);
        users.add(newUser);
        // 메모리 리스트에 새 사용자 추가

        saveAllUsersToFile();
        // 변경된 사용자 목록 전체를 파일에 안전하게 저장 (동기화)
    }

    private void saveAllUsersToFile() {
        try (PrintWriter pw = new PrintWriter(
                new OutputStreamWriter(new FileOutputStream(Path.getUsersFilePath(), false), StandardCharsets.UTF_8))) {
            // 덮어쓰기 모드로 파일 열기

            for (User u : users) {
                pw.printf("%s\t%s\t%d\t%s\r\n", u.getName(), u.getPassword(), u.getStreak(), u.getLastDate());
                // 리스트에 있는 모든 사용자 정보를 파일에 기록
            }
        } catch (IOException e) {
            System.out.println("사용자 정보 저장 실패: " + e.getMessage());
        }
    }

    private void updateUserStreakAndSave(User foundUser) {
        try (PrintWriter userDataFile = new PrintWriter(
                new OutputStreamWriter(new FileOutputStream(Path.getUsersFilePath(), false),
                        StandardCharsets.UTF_8))) {
            // 파일 전체 갱신을 위해 덮어쓰기 모드로 열기

            for (User user : users) {
                if (user.equals(foundUser)) {
                    int streak = countStreak(foundUser);
                    // 접속일 기준 스트릭 재계산

                    userDataFile.printf("%s\t%s\t%d\t%s\r\n", foundUser.getName(), foundUser.getPassword(), streak,
                            LocalDate.now());
                    // 파일에는 오늘 날짜와 갱신된 스트릭 저장

                    foundUser.setStreak(streak);
                    // 메모리 객체에도 반영
                } else {
                    userDataFile.printf("%s\t%s\t%d\t%s\r\n", user.getName(), user.getPassword(), user.getStreak(),
                            user.getLastDate());
                    // 다른 사용자는 기존 정보 그대로 저장
                }
            }
        } catch (IOException e) {
            System.out.println("사용자 정보 업데이트 중 오류: " + e.getMessage());
        }
    }

    private int countStreak(User user) {
        int currentStreak = user.getStreak();
        if (user.getLastDate().equals(LocalDate.now())) {
            return currentStreak;
            // 오늘 이미 접속했다면 스트릭 유지
        } else if (user.getLastDate().plusDays(1).equals(LocalDate.now())) {
            return currentStreak + 1;
            // 마지막 접속일이 어제라면 스트릭 1 증가 (연속 접속 성공!)
        } else {
            return 1;
            // 연속 접속이 끊겼으므로 1일부터 다시 시작
        }
    }
}