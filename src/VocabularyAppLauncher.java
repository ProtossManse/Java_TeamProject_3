import data.User;
import util.Path;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Scanner;

public class VocabularyAppLauncher {
    Scanner scanner = new Scanner(System.in);
    ArrayList<User> users = new ArrayList<>();

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
            try (
                    PrintWriter userDataFile = new PrintWriter(Path.getUsersFilePath())
            ) {
                for (User user : users) {
                    if (user.equals(foundUser)) {
                        int streak = countStreak(foundUser);
                        userDataFile.printf("%s\t%s\t%d\t%s\r\n", foundUser.getName(), foundUser.getPassword(), streak, LocalDate.now());
                        foundUser.setStreak(streak);
                    }
                    else
                        userDataFile.printf("%s\t%s\t%d\t%s\r\n", user.getName(), user.getPassword(), user.getStreak(), user.getLastDate());
                }

            } catch (FileNotFoundException ignored) {

            }

            VocabularyApp app = new VocabularyApp(foundUser);
            app.menu();
            return;
        }

    }

    private void readUsers() {
        try (Scanner file = new Scanner(new File(Path.getUsersFilePath()))) {
            while (file.hasNextLine()) {
                String[] data = file.nextLine().split("\t");
                users.add(new User(data[0].trim(), data[1].trim(), Integer.parseInt(data[2].trim()), LocalDate.parse(data[3].trim())));
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
        try (PrintWriter usersDataPrintWriter = new PrintWriter(Path.getUsersFilePath())) {
            userDir.mkdir();
            usersDataPrintWriter.println(name + '\t' + password + '\t' + "1" + '\t' + date);
        } catch (FileNotFoundException e) {
            System.out.println("알 수 없는 에러 발생");
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
            return 0;
        }

    }


}
