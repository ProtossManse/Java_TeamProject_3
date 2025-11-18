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

/**
 * (VocabularyApp.java - 수정본)
 * VocaFileManager 생성자 호출 부분을 수정합니다.
 */
public class VocabularyApp {
    Scanner scanner = new Scanner(System.in, StandardCharsets.UTF_8);
    final User currentUser;

    VocabularyApp(User user) {
        this.currentUser = user;
    }

    public ArrayList<String> getPersonalVocaFilesList() {
        File dir = new File(Path.getVocaDirPath(currentUser.getName()));

        String[] list = dir.list((d, name) -> name.endsWith(".txt")); // .txt만
        if (list != null && list.length > 0) {
            return new ArrayList<>(Arrays.asList(list));
        } else
            return new ArrayList<>(); // null 대신 빈 리스트 반환
    }

    public ArrayList<String> getPersonalNotes() {
        File dir = new File(Path.getNoteDirPath(currentUser.getName()));

        String[] list = dir.list((d, name) -> name.endsWith(".txt")); // .txt만
        if (list != null) {
            return new ArrayList<>(Arrays.asList(list));
        } else
            return null;
    }

    /** 공용 단어장 파일 목록을 가져옵니다. */
    public ArrayList<String> getPublicVocaFilesList() {
        File dir = new File(Path.getPublicDirPath());
        String[] list = dir.list((d, name) -> name.endsWith(".txt"));
        if (list != null && list.length > 0) {
            return new ArrayList<>(Arrays.asList(list));
        } else
            return null;
    }

    public void menu() {
        int choice = 0;
        // [수정] 6번 종료가 아닌 5번 종료
        while (choice != 5) {
            System.out.println("\n\n\n==== 단어장 메뉴 화면 ====");
            System.out.println("이름: " + currentUser.getName());
            System.out.println(currentUser.getStreak() + "일 연속 공부 중!");
            System.out.println("1. 개인 단어장 관리");
            System.out.println("2. 공용 단어장 관리");
            System.out.println("3. 퀴즈 풀기");

            // ▼▼▼ [수정된 부분] ▼▼▼
            System.out.println("4. 오답노트 관리"); // 신규 추가
            System.out.println("5. 종료하기"); // 기존 4번 -> 5번
            // ▲▲▲ [수정된 부분] ▲▲▲

            System.out.print(">> ");

            try {
                // [수정] nextInt() 대신 nextLine()을 사용하여 입력 오류 방지
                choice = Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                choice = -1; // 잘못된 입력 처리
            }

            switch (choice) {
                case 1 -> managePersonalVocas();
                case 2 -> managePublicVocas();
                case 3 -> quiz();

                // ▼▼▼ [수정된 부분] ▼▼▼
                case 4 -> manageNotes(); // 4번: 오답노트 관리
                case 5 -> System.out.println("단어장 앱을 종료합니다."); // 5번: 종료
                // ▲▲▲ [수정된 부분] ▲▲▲

                default -> System.out.println("잘못된 입력입니다.");
            }
        }
    }

    // =========== 개인 단어 관리 ===========

    private void managePersonalVocas() {
        String choice;
        ArrayList<String> wordBooksFileArray = getPersonalVocaFilesList();

        // (수정) 비어있어도 바로 생성하지 않고, 'n'을 누를 때 생성하도록 유도
        if (wordBooksFileArray.isEmpty()) {
            System.out.println("'vocas' 폴더에 단어장이 없습니다. 'n'을 눌러 새로 만드세요.");
        }

        while (true) {
            System.out.println("\n==== 개인 단어장 관리 ====");

            ArrayList<String> displayList = new ArrayList<>();
            displayList.add("즐겨찾기"); // 특별 항목
            displayList.addAll(wordBooksFileArray); // 'vocas' 폴더 파일들

            System.out.print("단어장 목록: ");
            System.out.println(String.join(", ", displayList));

            System.out.print("관리할 단어장을 선택하세요 (q: 뒤로가기, n: 새 단어장 만들기): ");
            choice = scanner.nextLine().trim(); // next() 대신 nextLine() 사용

            if (choice.equalsIgnoreCase("q")) {
                return;
            }
            if (choice.equalsIgnoreCase("n")) {
                createVocaFile();
                wordBooksFileArray = getPersonalVocaFilesList(); // 목록 갱신
                continue;
            }

            String selectedFile = null;
            String selectedPath = null;

            if (choice.equals("즐겨찾기")) {
                selectedFile = choice;
                selectedPath = Path.getFavoriteFilePath(currentUser.getName());
            } else {
                for (String file : wordBooksFileArray) {
                    if (choice.equals(file)) {
                        selectedFile = file;
                        selectedPath = Path.getVocaFilePath(currentUser.getName(), selectedFile);
                        break;
                    }
                }
            }

            if (selectedFile != null) {
                System.out.println("'" + selectedFile + "' 단어장을 엽니다.");

                // [필수 수정] 생성자에 currentUser.getName()을 전달
                VocaFileManager vocaFileManager = new PersonalVocaFileManager(
                        selectedPath, currentUser.getName());

                vocaFileManager.menu();
                // (수정) 개인 단어장 관리 메뉴로 돌아오도록 return 제거
            } else {
                System.out.println("'" + choice + "'(은)는 목록에 없습니다. 다시 입력해주세요.");
            }
        }
    }

    private void createVocaFile() {
        System.out.println("==== 새 단어장 만들기 ====");
        System.out.print("만들 단어장 이름을 입력하세요 (예: myvoca.txt): ");
        String filename = scanner.nextLine().trim(); // next() 대신 nextLine() 사용
        if (filename.isEmpty()) {
            System.out.println("파일 이름을 입력해야 합니다.");
            return;
        }

        // .txt 확장자 자동 추가
        if (!filename.endsWith(".txt")) {
            filename += ".txt";
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
            if (created) {
                System.out.println("단어장 '" + filename + "'이 생성되었습니다.");
            } else {
                System.out.println("단어장 생성에 실패했습니다.");
            }
        } catch (IOException e) {
            System.out.println("단어장 생성 중 오류: " + e.getMessage());
        }
    }

    // =========== 공용 단어 관리 ===========

    private void managePublicVocas() {
        ArrayList<String> publicVocaFiles = getPublicVocaFilesList();

        if (publicVocaFiles == null || publicVocaFiles.isEmpty()) {
            System.out.println("접근할 수 있는 공용 단어장이 없습니다.");
            return;
        }

        while (true) {
            System.out.println("\n==== 공용 단어장 목록 ====");
            for (int i = 0; i < publicVocaFiles.size(); i++) {
                System.out.printf("%d) %s%n", i + 1, publicVocaFiles.get(i));
            }
            System.out.println("0) 뒤로가기");
            System.out.print("관리할 단어장을 선택하세요: ");

            int choice;
            try {
                choice = Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("숫자를 입력해주세요.");
                continue;
            }

            if (choice == 0)
                return;
            if (choice < 1 || choice > publicVocaFiles.size()) {
                System.out.println("잘못된 번호입니다.");
                continue;
            }

            String selectedFile = publicVocaFiles.get(choice - 1);
            String publicFilePath = Path.getPublicFilePath();

            // [필수 수정] 생성자에 currentUser.getName()을 전달
            VocaFileManager vocaFileManager = new PersonalVocaFileManager(
                    publicFilePath, currentUser.getName());

            vocaFileManager.menu();
            // 메뉴가 끝나면 다시 이 목록으로 돌아옵니다.
        }
    }

    // =========== 퀴즈 ===========

    private void quiz() {
        int choice = 0;
        while (choice != 6) {
            System.out.println("==== 퀴즈 ====");
            System.out.println("1. 개인 단어장 퀴즈");
            System.out.println("2. 개인 오답노트 퀴즈");
            System.out.println("3. 즐겨찾기 단어 퀴즈");
            System.out.println("4. 공용 단어장 퀴즈");
            System.out.println("5. 많이 틀리는 단어 퀴즈 (추후구현)");
            System.out.println("6. 돌아가기");
            System.out.print(">> ");

            try {
                choice = Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                choice = -1;
            }

            QuizManager quizManager = new QuizManager(currentUser);

            switch (choice) {
                case 1 -> quizManager.personalWordQuiz(getPersonalVocaFilesList());
                case 2 -> quizManager.personalNoteQuiz(getPersonalNotes());
                case 3 -> quizManager.personalFavoriteQuiz(Path.getFavoriteFilePath(currentUser.getName()));
                case 4 -> quizManager.publicWordQuiz(getPublicVocaFilesList()); // (이 부분은 아직 미구현 상태)
                case 5 -> quizManager.publicFrequentlyMissedQuiz();
                case 6 -> System.out.println("메인메뉴로 돌아갑니다.");
                default -> System.out.println("잘못된 입력입니다.");
            }
        }
    }

    // =========== 오답노트 관리 ===========

    private void manageNotes() {
        // 'notes' 폴더의 실제 파일 목록을 가져옵니다.
        ArrayList<String> noteFiles = getPersonalNotes();

        if (noteFiles == null || noteFiles.isEmpty()) {
            System.out.println("관리할 오답노트가 없습니다. (퀴즈를 먼저 풀어주세요)");
            return;
        }

        while (true) {
            System.out.println("\n==== 오답노트 관리 ====");
            System.out.println("관리할 오답노트를 선택하세요.");

            for (int i = 0; i < noteFiles.size(); i++) {
                System.out.printf("%d) %s%n", i + 1, noteFiles.get(i));
            }
            System.out.println("0) 뒤로가기");
            System.out.print(">> ");

            int choice;
            try {
                choice = Integer.parseInt(scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("숫자를 입력해주세요.");
                continue;
            }

            if (choice == 0) {
                return; // 메인 메뉴로
            }
            if (choice < 1 || choice > noteFiles.size()) {
                System.out.println("잘못된 번호입니다.");
                continue;
            }

            String selectedFile = noteFiles.get(choice - 1);
            String noteFilePath = Path.getNoteFilePath(currentUser.getName(), selectedFile);

            // PersonalVocaFileManager를 '오답노트' 모드(isNoteFile=true)로 실행합니다.
            // username을 주입하여 동기화가 가능하도록 합니다.
            VocaFileManager vocaFileManager = new PersonalVocaFileManager(
                    noteFilePath, currentUser.getName());

            vocaFileManager.menu(); // 오답노트 전용 메뉴(noteMenu) 실행
        }
    }
}