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

// 개인, 공용, 오답노트, 즐겨찾기 파일을 통합 관리하는 클래스
// 현재 파일경로에 따라서 수행되는 로직이 달라지는 매우 중요한 추가기능 메소드를 담음

public class PersonalVocaFileManager extends VocaFileManager {

    private final boolean isFavoritesFile;
    // 현재 파일이 즐겨찾기 파일(_favorites.txt)인지 여부

    private final boolean isPublicFile;
    // 현재 파일이 공용 단어장인지 여부

    private final boolean isNoteFile;
    // 현재 파일이 오답노트인지 여부

    public PersonalVocaFileManager(String fileName, String username) {
        super(fileName, username);
        // 부모 클래스 생성자 호출하여 파일 객체와 사용자 이름 설정

        String standardizedPath = fileName.replace('\\', '/');
        // 경로 구분자를 통일하여 비교

        this.isFavoritesFile = standardizedPath.endsWith("/favorites/_favorites.txt");
        this.isPublicFile = standardizedPath.startsWith("res/public/vocas");
        this.isNoteFile = standardizedPath.contains("/notes/");
        // 파일 경로 패턴을 분석하여 현재 파일의 성격을 규정
    }

    // 파일 성격에 따라 적절한 전용 메뉴를 호출합니다.
    @Override
    public void menu() {
        if (this.isFavoritesFile) {
            specialFavoritesMenu();
            // 즐겨찾기 관리 모드 (목록 + 제거)
        } else if (this.isNoteFile) {
            noteMenu();
            // 오답노트 관리 모드 (목록 + 즐겨찾기 토글만)
        } else {
            regularMenu();
            // 일반 단어장 관리 모드 (추가/삭제/수정/검색/토글)
        }
    }

    // ===================================================================
    // [모드 1] 오답노트 전용 메뉴
    // ===================================================================

    private void noteMenu() {
        while (true) {
            System.out.println("\n==== [오답노트 관리] ====");

            ArrayList<String> lines = loadFileLines();
            // 파일 내용을 매번 새로 읽어옴 (변경 사항 반영)

            if (lines == null)
                return;
            // 읽기 오류 시 종료

            if (lines.isEmpty()) {
                System.out.println("오답노트에 단어가 없습니다.");
                return;
                // 내용이 없으면 종료
            }

            System.out.println("--- 오답노트 목록 ---");
            for (int i = 0; i < lines.size(); i++)
                System.out.printf("%d) %s%n", i + 1, lines.get(i));
            System.out.println("--------------------");

            System.out.println("\n1. 단어 즐겨찾기 (토글)");
            System.out.println("2. 뒤로가기");
            System.out.print(">> ");

            int choice = readInt(1, 2);
            // 1~2 사이의 정수 입력 받기

            switch (choice) {
                case 1:
                    favoriteWithToggle();
                    // 오답노트는 Suffix * 방식을 사용하므로 favoriteWithToggle 내부에서 처리됨
                    break;
                // 작업 후 목록 갱신을 위해 루프 재시작

                case 2:
                    System.out.println("메인 메뉴로 돌아갑니다.");
                    return;
                // 메뉴 종료
            }
        }
    }

    // ===================================================================
    // [모드 2] 일반 (개인/공용) 단어장 메뉴
    // ===================================================================

    private void regularMenu() {
        int choice = 0;
        while (choice != 6) {
            String title = isPublicFile ? "[공용 단어장 관리]" : "[개인 단어장 관리]";
            // 제목 동적 표시

            System.out.println("\n===== " + title + " =====");
            System.out.println("1. 단어 추가");
            System.out.println("2. 단어 삭제");
            System.out.println("3. 단어 수정");
            System.out.println("4. 단어 검색");
            System.out.println("5. 단어 즐겨찾기 (토글)");
            System.out.println("6. 뒤로가기");
            System.out.print(">> ");

            try {
                String input = super.scanner.nextLine().trim();
                if (input.isEmpty())
                    continue;
                choice = Integer.parseInt(input);
                // 메뉴 선택 입력
            } catch (NumberFormatException e) {
                choice = -1;
            }

            switch (choice) {
                case 1 -> super.addVoca();
                // 부모 클래스의 기본 추가 기능 사용

                case 2 -> this.removeVoca();
                // 동기화 기능이 포함된 삭제 메서드 호출

                case 3 -> this.editVoca();
                // 동기화 기능이 포함된 수정 메서드 호출

                case 4 -> super.searchVoca();
                // 부모 클래스의 기본 검색 기능 사용

                case 5 -> {
                    if (this.isPublicFile) {
                        favoriteNoToggle();
                        // 공용 단어장은 파일 수정 없이 즐겨찾기 파일만 갱신
                    } else {
                        favoriteWithToggle();
                        // 개인 단어장은 Prefix * 토글 적용하며 갱신
                    }
                }
                case 6 -> System.out.println("메인 메뉴로 돌아갑니다.");
                default -> System.out.println("잘못된 입력입니다.");
            }
        }
    }

    // 단어 수정 기능 (동기화 포함)
    @Override
    void editVoca() {
        System.out.println("==== 단어 수정 ====");
        ArrayList<String> lines = loadFileLines();
        // 파일 읽기

        if (lines == null || lines.isEmpty()) {
            System.out.println("단어가 등록되어 있지 않습니다.");
            return;
        }

        for (int i = 0; i < lines.size(); i++)
            System.out.printf("%d) %s%n", i + 1, lines.get(i));
        // 목록 출력

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
            // 오답노트인 경우: Suffix '*' 확인
            String[] parts = oldLine.split("\t", 2);
            String engPart = parts[0].trim();
            oldKor = parts.length > 1 ? parts[1].trim() : "";

            if (engPart.endsWith("*")) {
                wasFavorite = true;
                oldEng = engPart.substring(0, engPart.length() - 1); // 뒤의 * 제거
            } else {
                wasFavorite = false;
                oldEng = engPart;
            }
        } else {
            // 개인 단어장인 경우 : Prefix '*' 확인
            wasFavorite = oldLine.startsWith("*");
            String cleanOldLine = wasFavorite ? oldLine.substring(1) : oldLine; // 앞의 * 제거
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
        // 입력이 없으면 기존 값 유지

        // 3. 중복 검사 (모든 토글을 제거하고 순수 영단어만 비교)
        for (int i = 0; i < lines.size(); i++) {
            if (i == idx)
                continue;
            // 자기 자신은 제외

            String line = lines.get(i);
            String cleanEngPart;

            if (line.startsWith("*")) {
                // 개인 단어장 스타일 (*A)
                cleanEngPart = line.substring(1).split("\t", 2)[0].trim();
            } else {
                // 오답노트 스타일 (A*) 또는 일반 (A)
                cleanEngPart = line.split("\t", 2)[0].trim();
                if (cleanEngPart.endsWith("*")) {
                    cleanEngPart = cleanEngPart.substring(0, cleanEngPart.length() - 1);
                }
            }

            if (cleanEngPart.equalsIgnoreCase(newEng)) {
                System.out.println("오류: '" + newEng + "'(은)는 이미 다른 항목에 존재합니다.");
                return;
                // 중복 방지
            }
        }

        // 4. 새 라인 생성 (기존 즐겨찾기 상태 유지)
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
        // 단어 이름이 바뀌었으므로 즐겨찾기 파일에서도 옛날 이름을 지우고 새 이름을 등록해야 함
        if (wasFavorite) {
            System.out.println("... 즐겨찾기 동기화 중 ...");
            removeFromFavoritesFile(oldEng);
            // 즐겨찾기 목록에서 구버전 단어 삭제

            addToFavoritesFile(newEng, newKor);
            // 즐겨찾기 목록에 신버전 단어 추가

            syncRemoveStar(oldEng);
            // 혹시 모를 다른 파일들의 구버전 단어 링크(토글) 제거
            // 이걸 안 하면 무더기 오류가 올라옴

            System.out.println("... 즐겨찾기 파일이 '" + newEng + "'로 업데이트되었습니다.");
        }
    }

    // 단어 삭제 기능 재정의 (동기화 포함)
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
        // 리스트에서 항목 제거

        saveFileLines(lines);
        System.out.println("삭제가 완료되었습니다.");

        // 즐겨찾기 동기화 로직 (Prefix/Suffix 구분)
        String eng;
        boolean wasFavorite;

        if (this.isNoteFile) {
            // 오답노트 (Suffix) 파싱
            String[] parts = lineToRemove.split("\t", 2);
            eng = parts[0].trim();
            if (eng.endsWith("*")) {
                wasFavorite = true;
                eng = eng.substring(0, eng.length() - 1); // * 제거한 순수 단어
            } else {
                wasFavorite = false;
            }
        } else {
            // 개인 단어장 (Prefix) 파싱
            wasFavorite = lineToRemove.startsWith("*");
            if (wasFavorite) {
                lineToRemove = lineToRemove.substring(1);
            }
            String[] parts = lineToRemove.split("\t", 2);
            eng = parts[0].trim();
        }

        if (wasFavorite) {
            System.out.println("... 즐겨찾기 동기화: '" + eng + "'를 즐겨찾기에서 제거합니다.");
            removeFromFavoritesFile(eng);
            // _favorites.txt에서 제거

            syncRemoveStar(eng);
            // 다른 파일들에 남아있을 수 있는 토글 제거
        }
    }

    // 토글이 있는 즐겨찾기 (개인 단어장, 오답노트)
    // 파일 종류에 따라 Prefix(*A) 또는 Suffix(A*) 적용
    private void favoriteWithToggle() {
        ArrayList<String> lines = loadFileLines();
        if (lines == null || lines.isEmpty()) {
            System.out.println("단어가 등록되어 있지 않습니다.");
            return;
        }

        // 오답노트 메뉴가 아니면 목록을 다시 출력 (오답노트 메뉴에서는 이미 출력됨)
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
            // 오답노트 (Suffix '*') 로직
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
                // 해제 : 토글 제거 -> favorites 파일 제거 -> 전체 동기화
                lines.set(idx, cleanEng + "\t" + kor);
                removeFromFavoritesFile(cleanEng);
                syncRemoveStar(cleanEng);
                System.out.println("즐겨찾기 해제 완료!");
            } else {
                // 추가 : 토글 추가 -> favorites 파일 추가
                lines.set(idx, cleanEng + "*\t" + kor);
                addToFavoritesFile(cleanEng, kor);
                System.out.println("즐겨찾기 추가 완료!");
            }

        } else {
            // 개인 단어장 (Prefix '*') 로직
            if (selected.startsWith("*")) {
                isCurrentlyFavorite = true;
                selected = selected.substring(1);
            } else {
                isCurrentlyFavorite = false;
            }

            String[] parts = selected.split("\t", 2);
            cleanEng = parts[0].trim();
            kor = parts.length > 1 ? parts[1].trim() : "";

            if (isCurrentlyFavorite) {
                // 해제
                lines.set(idx, selected);
                removeFromFavoritesFile(cleanEng);
                syncRemoveStar(cleanEng);
                System.out.println("즐겨찾기 해제 완료!");
            } else {
                // 추가
                lines.set(idx, "*" + selected);
                addToFavoritesFile(cleanEng, kor);
                System.out.println("즐겨찾기 추가 완료!");
            }
        }

        saveFileLines(lines);
        // 변경된 내용(토글 상태)을 파일에 저장!
        // 이게 안 되면 또 토글 오류가 또 발생
    }

    // 토글이 없는 즐겨찾기 (공용 단어장)
    // 원본 파일은 건드리지 않고 _favorites.txt 만 갱신
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

        // 이미 즐겨찾기 되어 있는지 확인 (토글이 없으므로 확인 필수)
        if (isAlreadyInFavorites(eng)) {
            removeFromFavoritesFile(eng);
            syncRemoveStar(eng); // 해제 시에는 개인/오답노트 동기화 수행
            System.out.println("즐겨찾기 해제 완료!");
        } else {
            addToFavoritesFile(eng, kor);
            System.out.println("즐겨찾기 추가 완료!");
        }
        // 공용 파일은 saveFileLines() 호출하지 않음 (원본 보존을 위해)
    }

    // ===================================================================
    // [모드 3] 즐겨찾기 파일 전용 메뉴
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
        removeFromFavoritesFile(eng); // 즐겨찾기 파일에서 제거
        syncRemoveStar(eng); // 다른 모든 파일에서 토글 제거
        System.out.println("제거 및 동기화 완료.");
    }

    // 동기화 핵심 메서드
    // 즐겨찾기 해제 시 '개인 단어장' 폴더와 '오답노트' 폴더를 모두 스캔하여
    // 해당 단어의 토글(*표시)을 제거
    private void syncRemoveStar(String eng) {
        String username = super.username;
        if (username == null) {
            System.out.println("동기화 오류: 사용자 이름을 찾을 수 없습니다.");
            return;
        }

        File vocaDir = new File(Path.getVocaDirPath(username));
        File noteDir = new File(Path.getNoteDirPath(username));

        scanAndRemoveStar(vocaDir, eng, false); // false: Prefix 모드 (개인단어장)
        scanAndRemoveStar(noteDir, eng, true); // true: Suffix 모드 (오답노트)
    }

    // syncRemoveStar의 헬퍼.
    // isNoteFile 플래그에 따라 Prefix/Suffix 모드로 토글을 찾아 제거합니다.
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
                    // 오답노트: 뒤에 *가 붙었는지 확인 (apple*)
                    if (fileEng.endsWith("*")) {
                        String cleanEng = fileEng.substring(0, fileEng.length() - 1);
                        if (cleanEng.equalsIgnoreCase(eng)) {
                            lines.set(i, cleanEng + "\t" + fileKor);
                            // Suffix '*' 제거하고 원상복구
                            fileChanged = true;
                        }
                    }
                } else {
                    // 개인 단어장: 앞에 *가 붙었는지 확인 (*apple)
                    if (line.startsWith("*")) {
                        String cleanLine = line.substring(1);
                        String[] cleanParts = cleanLine.split("\t", 2);
                        String cleanEng = cleanParts[0].trim();

                        if (cleanEng.equalsIgnoreCase(eng)) {
                            lines.set(i, cleanLine);
                            // Prefix '*' 제거하고 원상복구
                            fileChanged = true;
                        }
                    }
                }
            }

            if (fileChanged) {
                System.out.println("... 동기화: " + file.getName() + "에서 토글 제거 완료.");
                saveFileLines(lines, file);
                // 변경사항 저장
            }
        }
    }

    private int readInt(int min, int max) {
        while (true) {
            try {
                String input = super.scanner.nextLine().trim();
                if (input.isEmpty())
                    continue;
                int n = Integer.parseInt(input);
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
            // 즐겨찾기 파일은 없을 수도 있음 (이러면 조용히 리턴)

            System.out.println("단어장 파일(" + vocaFile.getName() + ")이 존재하지 않습니다.");
            return lines;
        }
        try (Scanner fileScanner = new Scanner(vocaFile, StandardCharsets.UTF_8.name())) {
            while (fileScanner.hasNextLine())
                lines.add(fileScanner.nextLine()); // 모든 라인 읽고
        } catch (FileNotFoundException e) {
            System.out.println("파일을 읽는 중 오류 발생: " + e.getMessage());
            return null; // 읽기 실패시 null 반환
        }
        return lines;
    }

    private void saveFileLines(ArrayList<String> lines) {
        saveFileLines(lines, this.vocaFile);
        // 현재 파일 덮어쓰기
    }

    private void saveFileLines(ArrayList<String> lines, File file) {
        try (PrintWriter pw = new PrintWriter(
                new OutputStreamWriter(new FileOutputStream(file, false), StandardCharsets.UTF_8))) {
            for (String line : lines)
                pw.println(line);
        } catch (IOException e) {
            System.out.println(file.getName() + " 파일 저장 중 오류 발생: " + e.getMessage());
            // 쓰기 실패 시 에러 메시지
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
                // 이미 파일에 존재하는지 확인
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
                favLines.add(sc.nextLine()); // 기존 파일 내용 로드
        } catch (Exception ignored) {
        }

        for (int i = 0; i < favLines.size(); i++) {
            String[] p = favLines.get(i).split("\t", 2);
            String e = p[0].trim();
            String k = p.length > 1 ? p[1].trim() : "";

            if (e.equalsIgnoreCase(eng)) {
                // 중복 단어 발견 시 뜻 병합
                ArrayList<String> meanings = new ArrayList<>(Arrays.asList(k.split("/")));
                for (int j = 0; j < meanings.size(); j++)
                    meanings.set(j, meanings.get(j).trim());
                if (meanings.contains(kor))
                    return false;
                // 뜻까지 똑같으면 중복이므로 추가 안 함

                meanings.add(kor);
                favLines.set(i, e + "\t" + String.join("/", meanings));
                saveFileLines(favLines, favFile);
                return true;
            }
        }

        // 없으면 새 줄 추가
        try (PrintWriter pw = new PrintWriter(
                new OutputStreamWriter(new FileOutputStream(favFile, true), StandardCharsets.UTF_8))) {
            pw.printf("%s\t%s%n", eng, kor);
            // append 모드로 추가
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
                favLines.add(sc.nextLine()); // 파일 로드
        } catch (Exception ignored) {
            return;
        }

        boolean changed = false;
        for (int i = favLines.size() - 1; i >= 0; i--) {
            String[] p = favLines.get(i).split("\t", 2);
            String e = p[0].trim();
            if (e.equalsIgnoreCase(eng)) {
                favLines.remove(i); // 일치하는 단어 삭제
                changed = true;
            }
        }
        if (changed)
            saveFileLines(favLines, favFile); // 변경사항이 있을 때만 저장
    }
}