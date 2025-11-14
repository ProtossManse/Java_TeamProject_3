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

/**
 * (PersonalVocaFileManager.java - 최종 수정본)
 * - '오답노트' 전용 메뉴 추가 (요청사항 1, 2)
 * - 오답노트도 '*' 토글 사용 (요청사항 3 - 동기화 위해 `*` 사용)
 * - editVoca, removeVoca 동기화 포함
 */
public class PersonalVocaFileManager extends VocaFileManager {

    private final boolean isFavoritesFile;
    private final boolean isPublicFile;
    private final boolean isNoteFile;

    public PersonalVocaFileManager(String fileName, String username) {
        super(fileName, username); // 부모에게 username 전달

        String standardizedPath = fileName.replace('\\', '/');

        this.isFavoritesFile = standardizedPath.endsWith("/favorites/_favorites.txt");
        this.isPublicFile = standardizedPath.startsWith("res/public/vocas");
        this.isNoteFile = standardizedPath.contains("/notes/");
    }

    /**
     * [수정]
     * 진입점 메뉴입니다.
     * 파일 타입에 따라 3가지 다른 메뉴를 호출합니다.
     */
    @Override
    public void menu() {
        if (this.isFavoritesFile) {
            specialFavoritesMenu(); // A. 즐겨찾기 (_favorites.txt)
        } else if (this.isNoteFile) {
            noteMenu(); // B. 오답노트 (note-....txt)
        } else {
            regularMenu(); // C. 개인/공용 단어장 (vocas/..., public/vocas/...)
        }
    }

    // ===================================================================
    // B. 오답노트 전용 메뉴 (요청사항 1, 2, 3)
    // ===================================================================

    /**
     * [신규 추가]
     * '오답노트' 파일을 위한 전용 메뉴입니다.
     * '즐겨찾기' 기능만 노출합니다.
     */
    private void noteMenu() {
        // 루프가 메뉴를 감싸서 즐겨찾기 토글 후 목록을 갱신합니다.
        while (true) {
            System.out.println("\n==== [오답노트 관리] ====");

            ArrayList<String> lines = loadFileLines();
            if (lines == null)
                return;
            if (lines.isEmpty()) {
                System.out.println("오답노트에 단어가 없습니다.");
                return;
            }

            System.out.println("--- 오답노트 목록 ---");
            for (int i = 0; i < lines.size(); i++)
                System.out.printf("%d) %s%n", i + 1, lines.get(i));
            System.out.println("--------------------");

            System.out.println("\n1. 단어 즐겨찾기 (토글)");
            System.out.println("2. 뒤로가기");
            System.out.print(">> ");

            int choice = readInt(1, 2);

            switch (choice) {
                case 1:
                    // 오답노트에 '*' 토글을 적용하고 동기화하는
                    // 'favoriteWithToggle()' 메서드를 호출합니다.
                    favoriteWithToggle();
                    // 토글 후 갱신된 목록을 봐야 하므로 break (while 재시작)
                    break;
                case 2:
                    System.out.println("메인 메뉴로 돌아갑니다.");
                    return; // return으로 메뉴 종료
            }
        }
    }

    // ===================================================================
    // C. 일반 (개인/공용) 단어장 메뉴
    // ===================================================================

    private void regularMenu() {
        int choice = 0;
        while (choice != 6) {
            String title = isPublicFile ? "[공용 단어장 관리]" : "[개인 단어장 관리]";

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
                case 2 -> this.removeVoca(); // 동기화 O
                case 3 -> this.editVoca(); // 동기화 O
                case 4 -> super.searchVoca();
                case 5 -> {
                    // [수정] 오답노트(isNoteFile)가 noteMenu()로 분리되었음
                    if (this.isPublicFile) {
                        favoriteNoToggle(); // *토글 없음 (공용)
                    } else {
                        favoriteWithToggle(); // *토글 있음 (개인 단어장)
                    }
                }
                case 6 -> System.out.println("메인 메뉴로 돌아갑니다.");
                default -> System.out.println("잘못된 입력입니다.");
            }
        }
    }

    /**
     * (수정)
     * VocaFileManager의 editVoca를 오버라이드(재정의)합니다.
     * 단어 수정 시 즐겨찾기 동기화 기능을 추가합니다.
     */
    @Override
    void editVoca() {
        System.out.println("==== 단어 수정 ====");
        ArrayList<String> lines = loadFileLines();
        if (lines == null || lines.isEmpty()) {
            System.out.println("단어가 등록되어 있지 않습니다.");
            return;
        }

        for (int i = 0; i < lines.size(); i++)
            System.out.printf("%d) %s%n", i + 1, lines.get(i));

        System.out.print("수정할 번호를 입력하세요 (0: 취소): ");
        int index = readInt(0, lines.size());
        if (index == 0) {
            System.out.println("수정 취소.");
            return;
        }

        int idx = index - 1;
        String oldLine = lines.get(idx);

        // 기존 정보 파악
        boolean wasFavorite = oldLine.startsWith("*");
        String cleanOldLine = wasFavorite ? oldLine.substring(1) : oldLine;
        String[] parts = cleanOldLine.split("\t", 2);
        String oldEng = parts[0].trim();
        String oldKor = parts.length > 1 ? parts[1].trim() : "";

        System.out.println("현재: " + oldEng + " = " + oldKor + (wasFavorite ? " (즐겨찾기)" : ""));

        System.out.print("새 영단어 (엔터 입력 시 유지): ");
        String newEng = scanner.nextLine().trim();
        System.out.print("새 뜻 (엔터 입력 시 유지): ");
        String newKor = scanner.nextLine().trim();

        if (newEng.isEmpty())
            newEng = oldEng;
        if (newKor.isEmpty())
            newKor = oldKor;

        // 중복 검사
        for (int i = 0; i < lines.size(); i++) {
            if (i == idx)
                continue;
            String line = lines.get(i);
            String cleanLine = line.startsWith("*") ? line.substring(1) : line;
            String[] p = cleanLine.split("\t", 2);
            String e = p[0].trim();
            if (e.equalsIgnoreCase(newEng)) {
                System.out.println("오류: '" + newEng + "'(은)는 이미 다른 항목에 존재합니다.");
                return;
            }
        }

        // 새 라인 생성 (즐겨찾기 상태 유지)
        String newLine = newEng + "\t" + newKor;
        if (wasFavorite) {
            newLine = "*" + newLine;
        }

        lines.set(idx, newLine);
        saveFileLines(lines);
        System.out.println("수정이 완료되었습니다.");

        // 즐겨찾기 동기화
        if (wasFavorite) {
            System.out.println("... 즐겨찾기 동기화 중 ...");
            removeFromFavoritesFile(oldEng); // 이전 항목 제거
            addToFavoritesFile(newEng, newKor); // 새 항목 추가
            System.out.println("... 즐겨찾기 파일이 '" + newEng + "'로 업데이트되었습니다.");
        }
    }

    /**
     * VocaFileManager의 removeVoca를 오버라이드(재정의)합니다.
     * 단어 삭제 시 즐겨찾기 동기화 기능을 추가합니다.
     */
    @Override
    void removeVoca() {
        System.out.println("==== 단어 삭제 ====");
        ArrayList<String> lines = loadFileLines();
        if (lines == null || lines.isEmpty()) {
            System.out.println("단어가 등록되어 있지 않습니다.");
            return;
        }

        for (int i = 0; i < lines.size(); i++)
            System.out.printf("%d) %s%n", i + 1, lines.get(i));

        System.out.print("삭제할 번호를 입력하세요 (0: 취소): ");
        int index = readInt(0, lines.size());
        if (index == 0) {
            System.out.println("삭제 취소.");
            return;
        }

        String lineToRemove = lines.get(index - 1);
        lines.remove(index - 1);

        saveFileLines(lines);
        System.out.println("삭제가 완료되었습니다.");

        // 즐겨찾기 동기화
        if (lineToRemove.startsWith("*")) {
            String[] parts = lineToRemove.substring(1).split("\t", 2);
            String eng = parts[0].trim();
            System.out.println("... 즐겨찾기 동기화: '" + eng + "'를 즐겨찾기에서 제거합니다.");
            removeFromFavoritesFile(eng);
        }
    }

    /**
     * [개인 단어장, 오답노트]에서 사용
     * `*` 토글 및 `_favorites.txt` 동기화
     */
    private void favoriteWithToggle() {
        ArrayList<String> lines = loadFileLines();
        if (lines == null || lines.isEmpty()) {
            System.out.println("단어가 등록되어 있지 않습니다.");
            return;
        }

        // 오답노트 메뉴가 아니면 목록을 다시 출력
        // (오답노트 메뉴는 이미 목록을 출력했음)
        if (!this.isNoteFile) {
            System.out.println("===== 단어 목록 =====");
            for (int i = 0; i < lines.size(); i++)
                System.out.printf("%d) %s%n", i + 1, lines.get(i));
        }

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
        saveFileLines(lines); // *토글이 적용된 파일 저장
    }

    /**
     * [공용 단어장]에서 사용
     * 토글 없고 `_favorites.txt`만 동기화
     */
    private void favoriteNoToggle() {
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
        // 공용 파일은 saveFileLines() 호출 안 함
    }

    // ===================================================================
    // A. "즐겨찾기 단어장" 특별 메뉴
    // ===================================================================

    private void specialFavoritesMenu() {

        while (true) { // 메뉴 갱신
            System.out.println("\n==== [즐겨찾기 단어장 관리] ====");

            ArrayList<String> lines = loadFileLines(); // 목록 새로고침
            if (lines == null)
                return;
            if (lines.isEmpty()) {
                System.out.println("즐겨찾기에 등록된 단어가 없습니다.");
                return;
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
                    removeFromFavoritesMenu(lines);
                    break; // break로 루프 계속
                case 2:
                    System.out.println("메인 메뉴로 돌아갑니다.");
                    return; // return으로 메뉴 종료
            }
        }
    }

    private void removeFromFavoritesMenu(ArrayList<String> favLines) {
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

    /**
     * [핵심 동기화]
     * `_favorites.txt`에서 삭제 시, `vocas/` 및 `notes/` 폴더를
     * 모두 스캔하여 `*` 토글을 제거합니다.
     */
    private void syncRemoveStarFromPersonalVoca(String eng) {
        String username = super.username; // 주입된 username 사용
        if (username == null) {
            System.out.println("동기화 오류: 사용자 이름을 찾을 수 없습니다.");
            return;
        }

        // [수정] 스캔할 폴더 2개 지정
        File vocaDir = new File(Path.getVocaDirPath(username));
        File noteDir = new File(Path.getNoteDirPath(username));

        // 두 폴더를 모두 스캔
        scanAndRemoveStar(vocaDir, eng);
        scanAndRemoveStar(noteDir, eng);
    }

    /**
     * syncRemoveStar...의 헬퍼 메서드.
     * 지정된 디렉토리의 .txt 파일에서 '*'를 제거합니다.
     */
    private void scanAndRemoveStar(File directory, String eng) {
        if (!directory.exists())
            return;

        File[] files = directory.listFiles((dir, name) -> name.endsWith(".txt"));
        if (files == null)
            return;

        for (File file : files) {
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
        ArrayList<String> lines = new ArrayList<>();
        if (!vocaFile.exists()) {
            if (this.isFavoritesFile)
                return lines;
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
        saveFileLines(lines, this.vocaFile);
    }

    private void saveFileLines(ArrayList<String> lines, File file) {
        try (PrintWriter pw = new PrintWriter(
                new OutputStreamWriter(new FileOutputStream(file, false), StandardCharsets.UTF_8))) {
            for (String line : lines)
                pw.println(line);
        } catch (IOException e) {
            System.out.println(file.getName() + " 파일 저장 중 오류 발생: " + e.getMessage());
        }
    }

    private boolean isAlreadyInFavorites(String eng) {
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
        String username = super.username;
        if (username == null) {
            System.out.println("사용자 정보를 찾을 수 없어 favorites에 추가할 수 없습니다.");
            return false;
        }
        String favFilePath = Path.getFavoriteFilePath(username);
        File favFile = new File(favFilePath);

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
        String username = super.username;
        if (username == null)
            return;

        String favFilePath = Path.getFavoriteFilePath(username);
        File favFile = new File(favFilePath);
        if (!favFile.exists())
            return;

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