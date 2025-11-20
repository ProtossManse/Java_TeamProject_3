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
    // 사용자 입력을 받기 위한 스캐너 (한글 깨짐 방지 UTF-8)

    final User currentUser;
    // 현재 로그인한 사용자 정보 (final로 변경 불가)

    public VocabularyApp(User user) {
        this.currentUser = user;
        // 생성자 - 로그인 시 전달받은 사용자 정보를 저장
    }

    public ArrayList<String> getPersonalVocaFilesList() {
        File dir = new File(Path.getVocaDirPath(currentUser.getName()));
        // 사용자 개인 단어장 폴더 경로

        if (!dir.exists()) {
            return new ArrayList<>();
            // 폴더가 없으면 null 대신 빈 리스트 반환 (NullPointerException 방지 위함)
        }

        String[] list = dir.list((d, name) -> name.endsWith(".txt"));
        // .txt 파일만 필터링하여 배열로 가져옴

        if (list != null && list.length > 0) {
            return new ArrayList<>(Arrays.asList(list));
            // 파일이 있으면 리스트로 변환하여 반환
        } else {
            return new ArrayList<>();
            // 파일이 없으면 빈 리스트를 반환
        }
    }

    public ArrayList<String> getPersonalNotes() {
        File dir = new File(Path.getNoteDirPath(currentUser.getName()));
        // 사용자 오답노트 폴더 경로

        if (!dir.exists()) {
            return new ArrayList<>();
        }

        String[] list = dir.list((d, name) -> name.endsWith(".txt"));
        // .txt 파일만 필터링

        if (list != null && list.length > 0) {
            return new ArrayList<>(Arrays.asList(list));
        } else {
            return new ArrayList<>();
        } // 위 내용과 동일 작업
    }

    public ArrayList<String> getPublicVocaFilesList() {
        File dir = new File(Path.getPublicDirPath());
        // 공용 단어장 폴더 경로

        if (!dir.exists()) {
            return new ArrayList<>();
        }

        String[] list = dir.list((d, name) -> name.endsWith(".txt"));
        // .txt 파일만 필터링

        if (list != null && list.length > 0) {
            return new ArrayList<>(Arrays.asList(list));
        } else {
            return new ArrayList<>();
        } // 여기도 위랑 동일
    }

    public void menu() {
        int choice = 0;
        // 메뉴 선택 변수

        while (choice != 5) {
            System.out.println("\n\n\n==== 단어장 메뉴 화면 ====");
            System.out.println("이름: " + currentUser.getName());
            System.out.println(currentUser.getStreak() + "일 연속 공부 중!");
            System.out.println("1. 개인 단어장 관리");
            System.out.println("2. 공용 단어장 관리");
            System.out.println("3. 퀴즈 풀기");
            System.out.println("4. 오답노트 관리");
            System.out.println("5. 종료하기");
            System.out.print(">> ");

            try {
                String input = scanner.nextLine().trim();
                if (input.isEmpty())
                    continue;
                choice = Integer.parseInt(input);
                // 입력받은 문자열을 숫자로 변환 (입력 버퍼 오류 방지)
            } catch (NumberFormatException e) {
                choice = -1;
                // 숫자가 아닌 입력 시 -1로 처리하여 default 분기로 이동
            }

            switch (choice) {
                case 1 -> managePersonalVocas();
                case 2 -> managePublicVocas();
                case 3 -> quiz();
                case 4 -> manageNotes();
                case 5 -> System.out.println("단어장 앱을 종료합니다.");
                default -> System.out.println("잘못된 입력입니다.");
            }// switch 확장문
        }
    }

    // =========== 개인 단어 관리 ===========

    private void managePersonalVocas() {
        ArrayList<String> wordBooksFileArray = getPersonalVocaFilesList();
        // 개인 단어장 목록 로드

        if (wordBooksFileArray.isEmpty()) {
            System.out.println("'vocas' 폴더에 단어장이 없습니다. 'n'을 눌러 새로 만드세요.");
            // 단어장이 없을 때 안내 메시지
        }

        while (true) {
            System.out.println("\n==== 개인 단어장 관리 ====");

            ArrayList<String> displayList = new ArrayList<>();
            displayList.add("즐겨찾기");
            // 목록 맨 위에 '즐겨찾기' 고정 추가

            displayList.addAll(wordBooksFileArray);
            // 실제 파일 목록 추가

            System.out.print("단어장 목록: ");
            System.out.println(String.join(", ", displayList));
            // 목록 출력

            System.out.print("관리할 단어장을 선택하세요 (q: 뒤로가기, n: 새 단어장 만들기): ");
            String choice = scanner.nextLine().trim();

            if (choice.equalsIgnoreCase("q")) {
                return; // 뒤로가기
            }
            if (choice.equalsIgnoreCase("n")) {
                createVocaFile(); // 새 파일 생성
                wordBooksFileArray = getPersonalVocaFilesList(); // 목록 갱신
                continue; // while 루프 재시작
            }

            String selectedFile = null;
            // 선택된 파일명을 null로 명시적 초기화 (안 하면 노란불)

            String selectedPath = null;
            // 선택된 파일의 전체 경로

            if (choice.equals("즐겨찾기")) {
                selectedFile = choice;
                selectedPath = Path.getFavoriteFilePath(currentUser.getName());
                // 즐겨찾기 선택 시 경로 설정
            } else {
                for (String file : wordBooksFileArray) {
                    if (choice.equals(file)) {
                        selectedFile = file;
                        selectedPath = Path.getVocaFilePath(currentUser.getName(), selectedFile);
                        break;
                        // 입력한 이름이 목록에 있으면 파일명과 경로 설정
                    }
                }
            }

            if (selectedFile != null && selectedPath != null) {
                System.out.println("'" + selectedFile + "' 단어장을 엽니다.");

                // [중요] PersonalVocaFileManager 생성 시 currentUser.getName() 전달 (동기화 필수)
                VocaFileManager vocaFileManager = new PersonalVocaFileManager(
                        selectedPath, currentUser.getName());

                vocaFileManager.menu();
                // 관리 메뉴 실행
            } else {
                System.out.println("'" + choice + "'(은)는 목록에 없습니다. 다시 입력해주세요.");
                // 잘못된 입력 처리
            }
        }
    }

    private void createVocaFile() {
        System.out.println("==== 새 단어장 만들기 ====");
        System.out.print("만들 단어장 이름을 입력하세요 (예: myvoca.txt): ");
        String filename = scanner.nextLine().trim();

        if (filename.isEmpty()) {
            System.out.println("파일 이름을 입력해야 합니다.");
            return;
        }

        // 영문, 숫자, 한글, 하이픈(-), 언더바(_), 점(.) 만 허용
        // 특수문자(\, /, :, *, ?, ", <, >, | 등) 입력 시 차단
        // IOExeption을 방지하기 위해!
        if (!filename.matches("^[a-zA-Z0-9가-힣-_.]+$")) {
            System.out.println("파일 이름에는 특수문자를 사용할 수 없습니다.");
            return;
        }

        if (!filename.endsWith(".txt")) {
            filename += ".txt";
            // 확장자 .txt를 자동으로 추가
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
            if (newFile.createNewFile()) {
                System.out.println("단어장 '" + filename + "'이 생성되었습니다.");
            } else {
                System.out.println("단어장 생성에 실패했습니다.");
            }
        } catch (IOException e) {
            System.out.println("단어장 생성 중 오류: " + e.getMessage());
            // 파일 시스템 오류 처리
        }
    }

    // =========== 공용 단어 관리 ===========

    private void managePublicVocas() {
        ArrayList<String> publicVocaFiles = getPublicVocaFilesList();
        // 공용 단어장 목록 로드

        if (publicVocaFiles.isEmpty()) {
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
                String input = scanner.nextLine().trim();
                if (input.isEmpty())
                    continue;
                choice = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("숫자를 입력해주세요.");
                continue;
            }

            if (choice == 0)
                return; // 뒤로가기

            if (choice < 1 || choice > publicVocaFiles.size()) {
                System.out.println("잘못된 번호입니다.");
                continue;
            }

            String selectedFile = publicVocaFiles.get(choice - 1);
            // 선택된 파일명

            // Path 유틸 사용 (기본적으로 publics.txt 하나이겠지만 일단 목록에서 선택한 파일 사용)
            // Path.java의 getPublicDirPath() + "/" + selectedFile 조합 사용
            String publicFilePath = Path.getPublicDirPath() + "/" + selectedFile;

            // 공용 단어장도 사용자 이름(currentUser)을 전달하여 즐겨찾기 동기화 지원
            VocaFileManager vocaFileManager = new PersonalVocaFileManager(
                    publicFilePath, currentUser.getName());

            vocaFileManager.menu();
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
            System.out.println("5. 많이 틀리는 단어 퀴즈");
            System.out.println("6. 돌아가기");
            System.out.print(">> ");

            try {
                String input = scanner.nextLine().trim();
                if (input.isEmpty())
                    continue;
                choice = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                choice = -1;
            }

            QuizManager quizManager = new QuizManager(currentUser);
            // 퀴즈 매니저 생성

            switch (choice) {
                case 1 -> quizManager.personalWordQuiz(getPersonalVocaFilesList());
                case 2 -> quizManager.personalNoteQuiz(getPersonalNotes());
                case 3 -> quizManager.personalFavoriteQuiz(Path.getFavoriteFilePath(currentUser.getName()));
                case 4 -> quizManager.publicWordQuiz();
                case 5 -> quizManager.publicFrequentlyMissedQuiz();
                case 6 -> System.out.println("메인메뉴로 돌아갑니다.");
                default -> System.out.println("잘못된 입력입니다.");
            }
        }
    }

    // =========== 오답노트 관리 ===========

    private void manageNotes() {
        ArrayList<String> noteFiles = getPersonalNotes();
        // 오답노트 파일 목록 로드

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
                String input = scanner.nextLine().trim();
                if (input.isEmpty())
                    continue;
                choice = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.println("숫자를 입력해주세요.");
                continue;
            }

            if (choice == 0)
                return; // 뒤로가기

            if (choice < 1 || choice > noteFiles.size()) {
                System.out.println("잘못된 번호입니다.");
                continue;
            }

            String selectedFile = noteFiles.get(choice - 1);
            String noteFilePath = Path.getNoteFilePath(currentUser.getName(), selectedFile);

            System.out.println("'" + selectedFile + "' 오답노트를 엽니다.");

            // 오답노트 경로와 사용자 이름을 전달하여 PersonalVocaFileManager 생성
            VocaFileManager vocaFileManager = new PersonalVocaFileManager(
                    noteFilePath, currentUser.getName());

            vocaFileManager.menu();
        }
    }
}