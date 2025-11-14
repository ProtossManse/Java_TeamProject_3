package manager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Scanner;

import util.Path;

// PersonalVocaFileManager.java (최종 수정본)
public class PersonalVocaFileManager extends VocaFileManager {

    private final boolean isFavoritesFile;
    private final boolean isPublicFile;
    private final boolean isNoteFile;

    // (필수 수정 1) 생성자가 username을 받아 부모에게 전달
    public PersonalVocaFileManager(String fileName, String username) {
        super(fileName, username); // 부모(VocaFileManager) 생성자 호출

        String standardizedPath = fileName.replace('\\', '/');

        this.isFavoritesFile = standardizedPath.endsWith("/favorites/_favorites.txt");
        this.isPublicFile = standardizedPath.startsWith("res/public/vocas");
        this.isNoteFile = standardizedPath.contains("/notes/");
    }

    @Override
    public void menu() {
        if (this.isFavoritesFile) {
            specialFavoritesMenu();
        } else {
            regularMenu();
        }
    }

    // ===================================================================
    // B. 일반 (개인/공용/오답노트) 단어장 메뉴
    // ===================================================================

    private void regularMenu() {
        // (이 부분은 이전과 동일)
        int choice = 0;
        while (choice != 6) {
            String title = "[개인 단어장 관리]";
            if (isPublicFile)
                title = "[공용 단어장 관리]";
            if (isNoteFile)
                title = "[오답노트 관리]";

            System.out.println("\n===== " + title + " =====");
            System.out.println("1. 단어 추가");
            System.out.println("2. 단어 삭제");
            System.out.println("3. 단어 수정");
            System.out.println("4. 단어 검색");
            System.out.println("5. 단어 즐겨찾기 (토글)");
            System.out.println("6. 뒤로가기");
            System.out.print(">> ");
            try {
                choice = Integer.parseInt(super.scanner.nextLine().trim());
            } catch (NumberFormatException e) {
                choice = -1;
            }

            switch (choice) {
                case 1 -> super.addVoca();
                case 2 -> super.removeVoca();
                case 3 -> super.editVoca();
                case 4 -> super.searchVoca();
                case 5 -> {
                    if (this.isPublicFile || this.isNoteFile) {
                        favoriteNoToggle();
                    } else {
                        favoriteWithToggle();
                    }
                }
                case 6 -> System.out.println("메인 메뉴로 돌아갑니다.");
                default -> System.out.println("잘못된 입력입니다.");
            }
        }
    }

    private void favoriteWithToggle() {
        // (이 부분은 이전과 동일)
        ArrayList<String> lines = loadFileLines();
        if (lines == null || lines.isEmpty()) {
            System.out.println("단어가 등록되어 있지 않습니다.");
            return;
        }

        System.out.println("===== 단어 목록 =====");
        for (int i = 0; i < lines.size(); i++)
            System.out.printf("%d) %s%n", i + 1, lines.get(i));

        System.out.print("\n즐겨찾기 설정/해제할 단어 번호 입력 (0 : 취소) : ");
        int index = readInt(0, lines.size());
        if (index == 0)
            return;

        String selected = lines.get(index - 1);
        String eng, kor;

        if (selected.startsWith("*")) {
            String withoutStar = selected.substring(1);
            lines.set(index - 1, withoutStar);
            String[] parts = withoutStar.split("\t", 2);
            eng = parts[0].trim();
            removeFromFavoritesFile(eng); // 동기화
            System.out.println("즐겨찾기 해제 완료!");
        } else {
            lines.set(index - 1, "*" + selected);
            String[] parts = selected.split("\t", 2);
            eng = parts[0].trim();
            kor = parts.length > 1 ? parts[1].trim() : "";
            addToFavoritesFile(eng, kor); // 동기화
            System.out.println("즐겨찾기 추가 완료!");
        }
        saveFileLines(lines);
    }

    private void favoriteNoToggle() {
        // (이 부분은 이전과 동일)
        ArrayList<String> lines = loadFileLines();
        if (lines == null || lines.isEmpty()) {
            System.out.println("단어가 등록되어 있지 않습니다.");
            return;
        }

        System.out.println("===== 단어 목록 =====");
        for (int i = 0; i < lines.size(); i++)
            System.out.printf("%d) %s%n", i + 1, lines.get(i));

        System.out.print("\n즐겨찾기 설정/해제할 단어 번호 입력 (0 : 취소) : ");
        int index = readInt(0, lines.size());
        if (index == 0)
            return;

        String selected = lines.get(index - 1);
        String[] parts = selected.split("\t", 2);
        String eng = parts[0].trim();
        String kor = parts.length > 1 ? parts[1].trim() : "";

        if (isAlreadyInFavorites(eng)) {
            removeFromFavoritesFile(eng);
            syncRemoveStarFromPersonalVoca(eng); // 동기화
            System.out.println("즐겨찾기 해제 완료!");
        } else {
            addToFavoritesFile(eng, kor);
            System.out.println("즐겨찾기 추가 완료!");
        }
    }

    // ===================================================================
    // A. "즐겨찾기 단어장" 특별 메뉴
    // ===================================================================

    /**
     * '즐겨찾기'(_favorites.txt) 파일을 관리하기 위한 전용 메뉴입니다.
     * (문제 1 해결 지점)
     */
    private void specialFavoritesMenu() {

        // ▼▼▼ [수정] while 루프가 목록 로드를 포함하도록 구조 변경 ▼▼▼
        while (true) {
            System.out.println("\n==== [즐겨찾기 단어장 관리] ====");

            // 루프가 돌 때마다 목록을 새로고침
            ArrayList<String> lines = loadFileLines();
            if (lines == null)
                return; // 오류
            if (lines.isEmpty()) {
                System.out.println("즐겨찾기에 등록된 단어가 없습니다.");
                return; // 목록이 비었으므로 메뉴 종료
            }

            System.out.println("--- 즐겨찾기 목록 ---");
            for (int i = 0; i < lines.size(); i++)
                System.out.printf("%d) %s%n", i + 1, lines.get(i));
            System.out.println("--------------------");

            System.out.println("\n1. 즐겨찾기에서 단어 제거 (동기화)");
            System.out.println("2. 뒤로가기");
            System.out.print(">> ");

            int choice = readInt(1, 2);

            switch (choice) {
                case 1:
                    removeFromFavoritesMenu(lines); // 제거 실행
                    // [수정] 'return;' 대신 'break;'를 사용하여 루프를 계속 돔
                    break;
                case 2:
                    System.out.println("메인 메뉴로 돌아갑니다.");
                    return; // '뒤로가기'일 때만 메뉴 종료
            }
        }
        // ▲▲▲ [수정] ▲▲▲
    }

    private void removeFromFavoritesMenu(ArrayList<String> favLines) {
        // (이 부분은 이전과 동일)
        System.out.print("\n제거할 단어의 번호를 입력하세요 (0: 취소) : ");
        int index = readInt(0, favLines.size());
        if (index == 0)
            return;

        String selectedLine = favLines.get(index - 1);
        String[] parts = selectedLine.split("\t", 2);
        String eng = parts[0].trim();

        System.out.println("'" + eng + "' 단어를 즐겨찾기에서 제거합니다...");
        removeFromFavoritesFile(eng);
        syncRemoveStarFromPersonalVoca(eng); // 동기화
        System.out.println("제거 및 동기화 완료.");
    }

    private void syncRemoveStarFromPersonalVoca(String eng) {
        // (필수 수정 2) usernameFromVocaPath() 대신 super.username 사용
        String username = super.username;
        if (username == null) {
            System.out.println("동기화 오류: 사용자 이름을 찾을 수 없습니다.");
            return;
        }

        File vocaDir = new File(Path.getVocaDirPath(username));
        File[] personalVocaFiles = vocaDir.listFiles((dir, name) -> name.endsWith(".txt"));
        if (personalVocaFiles == null)
            return;

        for (File file : personalVocaFiles) {
            ArrayList<String> lines = new ArrayList<>();
            boolean fileChanged = false;

            try (Scanner sc = new Scanner(file, StandardCharsets.UTF_8.name())) {
                while (sc.hasNextLine())
                    lines.add(sc.nextLine());
            } catch (FileNotFoundException e) {
                continue;
            }

            for (int i = 0; i < lines.size(); i++) {
                String line = lines.get(i);
                if (line.startsWith("*")) {
                    String[] parts = line.substring(1).split("\t", 2);
                    String fileEng = parts[0].trim();

                    if (fileEng.equalsIgnoreCase(eng)) {
                        lines.set(i, line.substring(1)); // '*' 제거
                        fileChanged = true;
                    }
                }
            }

            if (fileChanged) {
                System.out.println("... 동기화: " + file.getName() + "에서 '*' 표시 제거 완료.");
                saveFileLines(lines, file);
            }
        }
    }

    // ===================================================================
    // 4. 헬퍼(Helper) 메서드 (파일 입출력 및 경로)
    // ===================================================================

    private int readInt(int min, int max) {
        // (이 부분은 이전과 동일)
        while (true) {
            try {
                int n = Integer.parseInt(super.scanner.nextLine().trim());
                if (n >= min && n <= max)
                    return n;
                System.out.printf("%d와 %d 사이의 숫자를 입력하세요: ", min, max);
            } catch (NumberFormatException e) {
                System.out.println("숫자를 입력하세요.");
            }
        }
    }

    private ArrayList<String> loadFileLines() {
        // (이 부분은 이전과 동일)
        ArrayList<String> lines = new ArrayList<>();
        if (!vocaFile.exists()) {
            System.out.println("단어장 파일(" + vocaFile.getName() + ")이 존재하지 않습니다.");
            return lines;
        }
        try (Scanner fileScanner = new Scanner(vocaFile, StandardCharsets.UTF_8.name())) {
            while (fileScanner.hasNextLine())
                lines.add(fileScanner.nextLine());
        } catch (FileNotFoundException e) {
            System.out.println("파일을 읽는 중 오류 발생: " + e.getMessage());
            return null;
        }
        return lines;
    }

    private void saveFileLines(ArrayList<String> lines) {
        // (이 부분은 이전과 동일)
        saveFileLines(lines, this.vocaFile);
    }

    private void saveFileLines(ArrayList<String> lines, File file) {
        // (이 부분은 이전과 동일)
        try (PrintWriter pw = new PrintWriter(
                new OutputStreamWriter(new FileOutputStream(file, false), StandardCharsets.UTF_8))) {
            for (String line : lines)
                pw.println(line);
        } catch (IOException e) {
            System.out.println(file.getName() + " 파일 저장 중 오류 발생: " + e.getMessage());
        }
    }

    // (필수 수정 3) 더 이상 경로 추측이 필요 없으므로 이 메서드 삭제
    // private String usernameFromVocaPath() { ... }

    private boolean isAlreadyInFavorites(String eng) {
        // (필수 수정 4) super.username 사용
        String username = super.username;
        if (username == null)
            return false;

        File favFile = new File(Path.getFavoriteFilePath(username));
        if (!favFile.exists())
            return false;

        try (Scanner sc = new Scanner(favFile, StandardCharsets.UTF_8.name())) {
            while (sc.hasNextLine()) {
                String[] p = sc.nextLine().split("\t", 2);
                String e = p[0].trim();
                if (e.equalsIgnoreCase(eng))
                    return true;
            }
        } catch (FileNotFoundException ignored) {
        }
        return false;
    }

    private boolean addToFavoritesFile(String eng, String kor) {
        // (필수 수정 5) super.username 사용
        String username = super.username;
        if (username == null) {
            System.out.println("사용자 정보를 찾을 수 없어 favorites에 추가할 수 없습니다.");
            return false;
        }
        String favFilePath = Path.getFavoriteFilePath(username);
        File favFile = new File(favFilePath);

        // (이하 로직은 이전과 동일)
        ArrayList<String> favLines = new ArrayList<>();
        try (Scanner sc = new Scanner(favFile, StandardCharsets.UTF_8.name())) {
            while (sc.hasNextLine())
                favLines.add(sc.nextLine());
        } catch (Exception ignored) {
        }

        for (int i = 0; i < favLines.size(); i++) {
            String[] p = favLines.get(i).split("\t", 2);
            String e = p[0].trim();
            String k = p.length > 1 ? p[1].trim() : "";

            if (e.equalsIgnoreCase(eng)) {
                ArrayList<String> meanings = new ArrayList<>(Arrays.asList(k.split("/")));
                for (int j = 0; j < meanings.size(); j++)
                    meanings.set(j, meanings.get(j).trim());
                if (meanings.contains(kor))
                    return false;

                meanings.add(kor);
                favLines.set(i, e + "\t" + String.join("/", meanings));
                saveFileLines(favLines, favFile);
                return true;
            }
        }
        try (PrintWriter pw = new PrintWriter(
                new OutputStreamWriter(new FileOutputStream(favFile, true), StandardCharsets.UTF_8))) {
            pw.printf("%s\t%s%n", eng, kor);
        } catch (IOException ex) {
            System.out.println("즐겨찾기 파일 저장 오류: " + ex.getMessage());
        }
        return true;
    }

    private void removeFromFavoritesFile(String eng) {
        // (필수 수정 6) super.username 사용
        String username = super.username;
        if (username == null)
            return;

        String favFilePath = Path.getFavoriteFilePath(username);
        File favFile = new File(favFilePath);
        if (!favFile.exists())
            return;

        // (이하 로직은 이전과 동일)
        ArrayList<String> favLines = new ArrayList<>();
        try (Scanner sc = new Scanner(favFile, StandardCharsets.UTF_8.name())) {
            while (sc.hasNextLine())
                favLines.add(sc.nextLine());
        } catch (Exception ignored) {
            return;
        }

        boolean changed = false;
        for (int i = favLines.size() - 1; i >= 0; i--) {
            String[] p = favLines.get(i).split("\t", 2);
            String e = p[0].trim();
            if (e.equalsIgnoreCase(eng)) {
                favLines.remove(i);
                changed = true;
            }
        }
        if (changed)
            saveFileLines(favLines, favFile);
    }
}