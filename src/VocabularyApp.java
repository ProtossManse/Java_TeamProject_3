import data.User;
import manager.PersonalVocaFileManager;
import manager.QuizManager;
import manager.VocaFileManager;
import util.Path;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

public class VocabularyApp {
    Scanner scanner = new Scanner(System.in, StandardCharsets.UTF_8);
    final User currentUser;

    VocabularyApp(User user) {
        this.currentUser = user;
    }

    public ArrayList<String> getPersonalVocaFilesList() {
        File dir = new File(Path.getVocaDirPath(currentUser.getName()));

        String[] list = dir.list();
        if (list != null) {
            return new ArrayList<>(Arrays.asList(list));
        } else return null;

    }

    public ArrayList<String> getPersonalNotes() {
        File dir = new File(Path.getNoteDirPath(currentUser.getName()));

        String[] list = dir.list();
        if (list != null) {
            return new ArrayList<>(Arrays.asList(list));
        } else return null;

    }

    public void menu() {
        int choice = 0;
        while (choice != 4) {
            System.out.println("\n\n\n==== 단어장 메뉴 화면 ====");
            System.out.println("이름: " + currentUser.getName());
            System.out.println(currentUser.getStreak() + "일 연속 공부 중!");
            System.out.println("1. 개인 단어장 관리");
            System.out.println("2. 공용 단어장 관리");
            System.out.println("3. 퀴즈 풀기");
            System.out.println("4. 종료하기");
            System.out.print(">> ");
            choice = scanner.nextInt();
            scanner.nextLine();
            switch (choice) {
                case 1 -> managePersonalVocas();
                case 2 -> managePublicVocas();
                case 3 -> quiz();
                case 4 -> System.out.println("단어장 앱을 종료합니다.");

            }
        }
    }


    // =========== 개인 단어 관리 ===========

    private void managePersonalVocas() {
        String choice;
        ArrayList<String> wordBooksFileArray = getPersonalVocaFilesList();
        if (wordBooksFileArray == null) {
            createVocaFile();
            wordBooksFileArray = getPersonalVocaFilesList();
        }
        while (true) {
            System.out.println("==== 개인 단어장 관리 ====");
            System.out.print("단어장 목록: ");
            System.out.println(String.join(", ", wordBooksFileArray));

            System.out.print("관리할 단어장을 선택하세요 (q 입력시 뒤로가기, n 입력시 새 단어장 만들기): ");
            choice = scanner.next();
            scanner.nextLine();
            String selectedFile = null;


            if (choice.equals("q") || choice.equals("Q")) {
                return;
            }
            if (choice.equals("n") || choice.equals("N")) {
                createVocaFile();
                continue;
            }


            for (String file : wordBooksFileArray) {
                if (choice.equals(file)) {
                    selectedFile = file;
                }
            }
            if (selectedFile == null) {
                System.out.println("다시 입력해주세요.");
                continue;
            }

            VocaFileManager vocaFileManager = new PersonalVocaFileManager(Path.getVocaFilePath(currentUser.getName(), selectedFile));
            vocaFileManager.menu();
            return;

        }

    }


    private void createVocaFile() {
        System.out.println("==== 새 단어장 만들기 ====");
        System.out.print("만들 단어장 이름을 입력하세요 (확장자 포함 가능, 예: myvoca.txt): ");
        String filename = scanner.next().trim();
        scanner.nextLine();
        if (filename.isEmpty()) {
            System.out.println("파일 이름을 입력해야 합니다.");
            return;
        }

        File vocaDir = new File(Path.getVocaDirPath(currentUser.getName()));
        if (!vocaDir.exists()) {
            if (!vocaDir.mkdirs()) {
                System.out.println("단어장 디렉토리를 생성할 수 없습니다.");
                return;
            }
        }

        File newFile = new File(Path.getVocaFilePath(currentUser.getName(), filename));
        if (newFile.exists()) {
            System.out.println("이미 같은 이름의 단어장이 존재합니다.");
            return;
        }

        try {
            boolean created = newFile.createNewFile();
            if (!created) {
                System.out.println("단어장 생성에 실패했습니다.");
                return;
            }
            System.out.println("단어장 '" + filename + "'이 생성되었습니다.");
        } catch (IOException e) {
            System.out.println("단어장 생성 중 오류: " + e.getMessage());
        }
    }

    // =========== 공용 단어 관리 ===========


    private void managePublicVocas() {
        System.out.println("Not Implemented");
        // TODO: 추후 구현
    }


    // =========== 퀴즈 ===========

    private void quiz() {
        int choice = 0;
        while (choice != 6) {
            System.out.println("==== 퀴즈 ====");
            System.out.println("1. 개인 단어장 퀴즈");
            System.out.println("2. 개인 오답노트 퀴즈");
            System.out.println("3. 즐겨찾기 단어 퀴즈");
            System.out.println("4. 공용 단어장 퀴즈 (추후구현)");
            System.out.println("5. 많이 틀리는 단어 퀴즈 (추후구현)");
            System.out.println("6. 돌아가기");
            System.out.print(">> ");

            choice = scanner.nextInt();
            scanner.nextLine();

            QuizManager quizManager = new QuizManager();


            switch (choice) {
                case 1 -> quizManager.personalWordQuiz(getPersonalVocaFilesList());
                case 2 -> quizManager.personalNoteQuiz(getPersonalNotes());
                case 3 -> quizManager.personalFavoriteQuiz(Path.getFavoriteFilePath(currentUser.getName()));
                case 4 -> quizManager.publicWordQuiz();
                case 5 -> quizManager.publicFrequentlyMissedQuiz();
                case 6 -> System.out.println("메인메뉴로 돌아갑니다.");
            }
        }
    }


}
