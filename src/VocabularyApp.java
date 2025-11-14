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
        } else
            return null;

    }

    public ArrayList<String> getPersonalNotes() {
        File dir = new File(Path.getNoteDirPath(currentUser.getName()));

        String[] list = dir.list();
        if (list != null) {
            return new ArrayList<>(Arrays.asList(list));
        } else
            return null;

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

    // =========== 개인 단어 관리 ===========

    private void managePersonalVocas() {
        String choice;
        // 1. 'vocas' 폴더의 실제 파일 목록을 가져옵니다.
        ArrayList<String> wordBooksFileArray = getPersonalVocaFilesList();

        // 2. 만약 'vocas' 폴더가 비어있다면, 파일 생성을 유도합니다.
        // (즐겨찾기만 있어서는 이 메뉴의 의미가 없으므로 기존 로직 유지)
        if (wordBooksFileArray == null || wordBooksFileArray.isEmpty()) {
            System.out.println("'vocas' 폴더에 단어장이 없습니다. 새로 만듭니다.");
            createVocaFile();
            wordBooksFileArray = getPersonalVocaFilesList(); // 목록 다시 불러오기
            // 그래도 없으면(사용자가 생성을 취소했으면) 일단 진행
            if (wordBooksFileArray == null) {
                wordBooksFileArray = new ArrayList<>();
            }
        }

        while (true) {
            System.out.println("\n==== 개인 단어장 관리 ====");

            // 3. 화면에 보여줄 목록 (displayList)을 생성합니다.
            ArrayList<String> displayList = new ArrayList<>();
            // 3-1. "즐겨찾기"를 특별 항목으로 항상 추가합니다.
            displayList.add("즐겨찾기");
            // 3-2. 'vocas' 폴더의 파일들을 뒤에 추가합니다.
            displayList.addAll(wordBooksFileArray);

            System.out.print("단어장 목록: ");
            System.out.println(String.join(", ", displayList));

            System.out.print("관리할 단어장을 선택하세요 (q: 뒤로가기, n: 새 단어장 만들기): ");
            choice = scanner.next();
            scanner.nextLine();

            if (choice.equals("q") || choice.equals("Q")) {
                return;
            }
            if (choice.equals("n") || choice.equals("N")) {
                createVocaFile();
                // 새 파일 생성 후 목록을 갱신합니다.
                wordBooksFileArray = getPersonalVocaFilesList();
                continue; // 목록을 다시 보여주기 위해 루프 처음으로
            }

            // 4. 사용자의 선택을 처리합니다.
            String selectedFile = null;
            String selectedPath = null;

            // 4-1. "즐겨찾기"를 선택했는지 확인합니다.
            if (choice.equals("즐겨찾기")) {
                selectedFile = choice;
                selectedPath = Path.getFavoriteFilePath(currentUser.getName());

                // 4-2. 'vocas' 폴더의 다른 파일을 선택했는지 확인합니다.
            } else {
                for (String file : wordBooksFileArray) {
                    if (choice.equals(file)) {
                        selectedFile = file;
                        selectedPath = Path.getVocaFilePath(currentUser.getName(), selectedFile);
                        break;
                    }
                }
            }

            // 5. 선택된 파일에 대한 관리자를 실행합니다.
            if (selectedFile != null) {
                System.out.println("'" + selectedFile + "' 단어장을 엽니다.");
                VocaFileManager vocaFileManager = new PersonalVocaFileManager(selectedPath, currentUser.getName());
                vocaFileManager.menu();
                // 관리자 메뉴가 끝나면 이 화면으로 돌아오지 않고 메인 메뉴로 가도록 return
                return;
            } else {
                System.out.println("'" + choice + "'(은)는 목록에 없습니다. 다시 입력해주세요.");
            }
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

    /** 공용 단어장 파일 목록을 가져옵니다. */
    public ArrayList<String> getPublicVocaFilesList() {
        // Path 유틸에 따라 공용 폴더 경로는 username과 무관.
        File dir = new File(Path.getPublicDirPath(null));

        String[] list = dir.list((d, name) -> name.endsWith(".txt")); // .txt 파일만 필터링
        if (list != null && list.length > 0) {
            return new ArrayList<>(Arrays.asList(list));
        } else
            return null;
    }

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
            // Path 유틸을 사용해 전체 경로를 만듭니다.
            String publicFilePath = Path.getPublicFilePath(null, selectedFile);

            // (핵심) PersonalVocaFileManager를 공용 파일 경로로 실행합니다.
            // 이 인스턴스는 isPublicFile=true 플래그를 가지고 실행됩니다.
            VocaFileManager vocaFileManager = new PersonalVocaFileManager(publicFilePath, currentUser.getName());
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
            System.out.println("4. 공용 단어장 퀴즈 (추후구현)");
            System.out.println("5. 많이 틀리는 단어 퀴즈 (추후구현)");
            System.out.println("6. 돌아가기");
            System.out.print(">> ");

            choice = scanner.nextInt();
            scanner.nextLine();

            QuizManager quizManager = new QuizManager(currentUser);

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
