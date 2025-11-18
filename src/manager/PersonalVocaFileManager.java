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
 * - '오답노트' 전용 메뉴 (최소 메뉴)
 * - '오답노트'는 Suffix '*' (apple*) 토글 사용
 * - '개인 단어장'은 Prefix '*' (*apple) 토글 사용
 * - 모든 동기화 로직(삭제, 수정)이 Prefix/Suffix를 구별하여 처리함
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
     * 진입점 메뉴. 파일 타입에 따라 3가지 다른 메뉴 호출
     */
    @Override
    public void menu() {
        if (this.isFavoritesFile) {
            specialFavoritesMenu(); // A. 즐겨찾기
        } else if (this.isNoteFile) {
            noteMenu(); // B. 오답노트 (요청하신 최소 메뉴)
        } else {
            regularMenu(); // C. 개인/공용 단어장
        }
    }

    // ===================================================================
    // B. 오답노트 전용 메뉴 (요청사항 1, 2, 3)
    // ===================================================================

    /**
     * [신규] '오답노트' 파일을 위한 전용 메뉴 (즐겨찾기 기능만 노출)
     */
    private void noteMenu() {
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
                    // 오답노트/개인단어장을 구분하여 토글하는 메서드 호출
                    favoriteWithToggle();
                    break; // 목록 갱신을 위해 while 재시작
                case 2:
                    System.out.println("메인 메뉴로 돌아갑니다.");
                    return; // 메뉴 종료
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
     * (수정) VocaFileManager의 editVoca를 오버라이드.
     * Prefix/Suffix 토글을 구별하여 동기화합니다.
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

        // 1. 기존 정보 파악 (Prefix/Suffix 구분)
        boolean wasFavorite;
        String oldEng, oldKor;

        if (this.isNoteFile) {
            // --- 오답노트 (Suffix) 로직 ---
            String[] parts = oldLine.split("\t", 2);
            String engPart = parts[0].trim();
            oldKor = parts.length > 1 ? parts[1].trim() : "";

            if (engPart.endsWith("*")) {
                wasFavorite = true;
                oldEng = engPart.substring(0, engPart.length() - 1);
            } else {
                wasFavorite = false;
                oldEng = engPart;
            }
        } else {
            // --- 개인 단어장 (Prefix) 로직 ---
            wasFavorite = oldLine.startsWith("*");
            String cleanOldLine = wasFavorite ? oldLine.substring(1) : oldLine;
            String[] parts = cleanOldLine.split("\t", 2);
            oldEng = parts[0].trim();
            oldKor = parts.length > 1 ? parts[1].trim() : "";
        }

        System.out.println("현재: " + oldEng + " = " + oldKor + (wasFavorite ? " (즐겨찾기)" : ""));

        // 2. 새 정보 입력 받기
        System.out.print("새 영단어 (엔터 입력 시 유지): ");
        String newEng = scanner.nextLine().trim();
        System.out.print("새 뜻 (엔터 입력 시 유지): ");
        String newKor = scanner.nextLine().trim();

        if (newEng.isEmpty())
            newEng = oldEng;
        if (newKor.isEmpty())
            newKor = oldKor;

        // 3. 중복 검사 (모든 토글을 제거하고 순수 영단어만 비교)
        for (int i = 0; i < lines.size(); i++) {
            if (i == idx)
                continue;
            String line = lines.get(i);

            String cleanEngPart;
            if (line.startsWith("*")) { // 개인 단어장
                cleanEngPart = line.substring(1).split("\t", 2)[0].trim();
            } else { // 오답노트 또는 토글 없음
                cleanEngPart = line.split("\t", 2)[0].trim();
                if (cleanEngPart.endsWith("*")) { // 오답노트
                    cleanEngPart = cleanEngPart.substring(0, cleanEngPart.length() - 1);
                }
            }

            if (cleanEngPart.equalsIgnoreCase(newEng)) {
                System.out.println("오류: '" + newEng + "'(은)는 이미 다른 항목에 존재합니다.");
                return;
            }
        }

        // 4. 새 라인 생성 (파일 타입에 맞게 토글 유지)
        String newLine;
        if (this.isNoteFile) {
            // 오답노트: "d*\t4"
            newLine = newEng + (wasFavorite ? "*" : "") + "\t" + newKor;
        } else {
            // 개인 단어장: "*d\t4"
            newLine = (wasFavorite ? "*" : "") + newEng + "\t" + newKor;
        }

        // 5. 파일 갱신
        lines.set(idx, newLine);
        saveFileLines(lines);
        System.out.println("수정이 완료되었습니다.");

        // 6. 즐겨찾기 동기화
        if (wasFavorite) {
            System.out.println("... 즐겨찾기 동기화 중 ...");
            removeFromFavoritesFile(oldEng); // 이전 항목 제거
            addToFavoritesFile(newEng, newKor); // 새 항목 추가
            syncRemoveStar(oldEng); // 다른 모든 파일에서 *이전* 토글 제거
            System.out.println("... 즐겨찾기 파일이 '" + newEng + "'로 업데이트되었습니다.");
        }
    }

    /**
     * (수정) VocaFileManager의 removeVoca를 오버라이드.
     * Prefix/Suffix 토글을 구별하여 동기화합니다.
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

        // --- 즐겨찾기 동기화 로직 (Prefix/Suffix 구분) ---
        String eng;
        boolean wasFavorite;

        if (this.isNoteFile) {
            // 오답노트 (Suffix) 로직
            String[] parts = lineToRemove.split("\t", 2);
            eng = parts[0].trim();
            if (eng.endsWith("*")) {
                wasFavorite = true;
                eng = eng.substring(0, eng.length() - 1); // Clean eng
            } else {
                wasFavorite = false;
            }
        } else {
            // 개인 단어장 (Prefix) 로직
            wasFavorite = lineToRemove.startsWith("*");
            if (wasFavorite) {
                lineToRemove = lineToRemove.substring(1); // Clean line
            }
            String[] parts = lineToRemove.split("\t", 2);
            eng = parts[0].trim();
        }

        if (wasFavorite) {
            System.out.println("... 즐겨찾기 동기화: '" + eng + "'를 즐겨찾기에서 제거합니다.");
            removeFromFavoritesFile(eng);
            syncRemoveStar(eng); // 다른 모든 파일의 토글도 제거
        }
    }

    /**
     * [개인 단어장, 오답노트]에서 사용 (Prefix/Suffix 구분)
     */
    private void favoriteWithToggle() {
        ArrayList<String> lines = loadFileLines();
        if (lines == null || lines.isEmpty()) {
            System.out.println("단어가 등록되어 있지 않습니다.");
            return;
        }

        // 오답노트 메뉴가 아니면 목록을 다시 출력
        if (!this.isNoteFile) {
            System.out.println("===== 단어 목록 =====");
            for (int i = 0; i < lines.size(); i++)
                System.out.printf("%d) %s%n", i + 1, lines.get(i));
        }

        System.out.print("\n즐겨찾기 설정/해제할 단어 번호 입력 (0 : 취소) : ");
        int index = readInt(0, lines.size());
        if (index == 0)
            return;

        int idx = index - 1;
        String selected = lines.get(idx);
        String eng, kor, cleanEng;
        boolean isCurrentlyFavorite;

        if (this.isNoteFile) {
            // --- 오답노트 (Suffix '*') 로직 ---
            String[] parts = selected.split("\t", 2);
            eng = parts[0].trim();
            kor = parts.length > 1 ? parts[1].trim() : "";

            if (eng.endsWith("*")) {
                isCurrentlyFavorite = true;
                cleanEng = eng.substring(0, eng.length() - 1);
            } else {
                isCurrentlyFavorite = false;
                cleanEng = eng;
            }

            if (isCurrentlyFavorite) {
                // 해제
                lines.set(idx, cleanEng + "\t" + kor);
                removeFromFavoritesFile(cleanEng);
                syncRemoveStar(cleanEng); // 모든 파일 동기화
                System.out.println("즐겨찾기 해제 완료!");
            } else {
                // 추가
                lines.set(idx, cleanEng + "*\t" + kor);
                addToFavoritesFile(cleanEng, kor);
                System.out.println("즐겨찾기 추가 완료!");
            }

        } else {
            // --- 개인 단어장 (Prefix '*') 로직 ---
            if (selected.startsWith("*")) {
                isCurrentlyFavorite = true;
                selected = selected.substring(1); // "apple\tsound"
            } else {
                isCurrentlyFavorite = false;
            }

            String[] parts = selected.split("\t", 2);
            cleanEng = parts[0].trim();
            kor = parts.length > 1 ? parts[1].trim() : "";

            if (isCurrentlyFavorite) {
                // 해제
                lines.set(idx, selected); // "apple\tsound"
                removeFromFavoritesFile(cleanEng);
                syncRemoveStar(cleanEng); // 모든 파일 동기화
                System.out.println("즐겨찾기 해제 완료!");
            } else {
                // 추가
                lines.set(idx, "*" + selected); // "*apple\tsound"
                addToFavoritesFile(cleanEng, kor);
                System.out.println("즐겨찾기 추가 완료!");
            }
        }

        saveFileLines(lines); // 현재 파일 저장
    }

    /**
     * [공용 단어장]에서 사용 (토글 없음)
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
            syncRemoveStar(eng); // 동기화
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
        while (true) {
            System.out.println("\n==== [즐겨찾기 단어장 관리] ====");

            ArrayList<String> lines = loadFileLines();
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
                    break;
                case 2:
                    System.out.println("메인 메뉴로 돌아갑니다.");
                    return;
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
        syncRemoveStar(eng); // 동기화
        System.out.println("제거 및 동기화 완료.");
    }

    /**
     * [핵심 동기화 - 수정]
     * `vocas/` (Prefix) 및 `notes/` (Suffix) 폴더를
     * 모두 스캔하여 `*` 토글을 제거합니다.
     */
    private void syncRemoveStar(String eng) {
        String username = super.username;
        if (username == null) {
            System.out.println("동기화 오류: 사용자 이름을 찾을 수 없습니다.");
            return;
        }

        File vocaDir = new File(Path.getVocaDirPath(username));
        File noteDir = new File(Path.getNoteDirPath(username));

        scanAndRemoveStar(vocaDir, eng, false); // false: Prefix 모드
        scanAndRemoveStar(noteDir, eng, true); // true: Suffix 모드
    }

    /**
     * [신규 헬퍼 - 수정]
     * syncRemoveStar의 헬퍼.
     * isNoteFile 플래그에 따라 Prefix/Suffix 모드로 토글을 찾아 제거합니다.
     */
    private void scanAndRemoveStar(File directory, String eng, boolean isNoteFile) {
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
                String[] parts = line.split("\t", 2);
                String fileEng = parts[0].trim();
                String fileKor = parts.length > 1 ? parts[1].trim() : "";

                if (isNoteFile) {
                    // --- 오답노트 (Suffix '*') 로직 ---
                    if (fileEng.endsWith("*")) {
                        String cleanEng = fileEng.substring(0, fileEng.length() - 1);
                        if (cleanEng.equalsIgnoreCase(eng)) {
                            lines.set(i, cleanEng + "\t" + fileKor); // Suffix '*' 제거
                            fileChanged = true;
                        }
                    }
                } else {
                    // --- 개인 단어장 (Prefix '*') 로직 ---
                    if (line.startsWith("*")) {
                        String cleanLine = line.substring(1);
                        String[] cleanParts = cleanLine.split("\t", 2);
                        String cleanEng = cleanParts[0].trim();

                        if (cleanEng.equalsIgnoreCase(eng)) {
                            lines.set(i, cleanLine); // Prefix '*' 제거
                            fileChanged = true;
                        }
                    }
                }
            }

            if (fileChanged) {
                System.out.println("... 동기화: " + file.getName() + "에서 토글 제거 완료.");
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