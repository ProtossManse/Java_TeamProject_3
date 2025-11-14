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

/**
 * (VocaFileManager.java - 수정본)
 * 모든 단어장 관리자의 부모 클래스입니다.
 */
public abstract class VocaFileManager {
    File vocaFile;
    Scanner scanner = new Scanner(System.in, StandardCharsets.UTF_8);
    String username; // [필수 수정] 현재 로그인한 사용자 이름을 저장

    /**
     * [필수 수정]
     * 생성자가 파일 경로와 함께 사용자 이름(username)을 받도록 변경합니다.
     */
    VocaFileManager(String fileName, String username) {
        vocaFile = new File(fileName);
        this.username = username; // 전달받은 username 저장
    }

    abstract public void menu();

    // --- (이하 단어 추가/수정/검색 기능은 기존과 동일) ---

    void addVoca() {
        System.out.println("==== 단어 추가 ====");
        System.out.print("영단어: ");
        String eng = scanner.nextLine().trim();

        if (!eng.matches("^[a-zA-Z][a-zA-Z -]*$")) {
            System.out.println("영단어에는 영어/띄어쓰기/하이픈만 사용할 수 있습니다.");
            return;
        }
        if (eng.isEmpty()) {
            System.out.println("영단어는 비어있을 수 없습니다.");
            return;
        }

        System.out.print("뜻(여러 뜻은 '/'로 구분): ");
        String kor = scanner.nextLine().trim();
        if (kor.isEmpty()) {
            System.out.println("뜻은 비어있을 수 없습니다.");
            return;
        }

        File parent = vocaFile.getParentFile();
        if (parent != null && !parent.exists())
            parent.mkdirs();

        ArrayList<String> lines = new ArrayList<>();

        try (Scanner fileScanner = new Scanner(vocaFile, StandardCharsets.UTF_8)) {
            while (fileScanner.hasNextLine()) {
                lines.add(fileScanner.nextLine());
            }
        } catch (Exception ignored) {
        }

        boolean updated = false;

        for (int i = 0; i < lines.size(); i++) {
            String[] parts = lines.get(i).split("\t", 2);
            if (parts[0].trim().equalsIgnoreCase(eng)) {
                ArrayList<String> meanings = new ArrayList<>(Arrays.asList(parts[1].trim().split("/")));
                if (meanings.contains(kor)) {
                    System.out.println("이미 존재하는 단어입니다! 다시 확인해 주세요.");
                    return;
                }
                meanings.add(kor);
                lines.set(i, eng + "\t" + String.join("/", meanings));
                updated = true;
                break;
            }
        }

        try (PrintWriter pw = new PrintWriter(
                new OutputStreamWriter(new FileOutputStream(vocaFile, false), StandardCharsets.UTF_8))) {

            if (updated) {
                System.out.println("이미 존재하는 영단어입니다. 뜻을 추가합니다.");
                for (String line : lines)
                    pw.println(line);
            } else {
                for (String line : lines)
                    pw.println(line);
                pw.printf("%s\t%s%n", eng, kor);
                System.out.println("단어가 추가되었습니다.");
            }

        } catch (IOException e) {
            System.out.println("단어 추가 중 오류: " + e.getMessage());
        }
    }

    /**
     * [참고]
     * 이 '단어 삭제' 기능은 PersonalVocaFileManager에 의해 오버라이드(재정의) 됩니다.
     * 이 메서드는 동기화 기능이 없는 원본입니다.
     */
    void removeVoca() {
        System.out.println("==== 단어 삭제 ====");
        ArrayList<String> lines = new ArrayList<>();
        if (!vocaFile.exists()) {
            System.out.println("단어장이 존재하지 않습니다.");
            return;
        }

        try (Scanner fileScanner = new Scanner(vocaFile, StandardCharsets.UTF_8.name())) {
            while (fileScanner.hasNextLine()) {
                lines.add(fileScanner.nextLine());
            }
        } catch (FileNotFoundException e) {
            System.out.println("파일을 읽을 수 없습니다.");
            return;
        }

        if (lines.isEmpty()) {
            System.out.println("단어가 등록되어 있지 않습니다.");
            return;
        }

        for (int i = 0; i < lines.size(); i++) {
            String[] parts = lines.get(i).split("\t", 2);
            String eng = parts.length > 0 ? parts[0].trim() : "";
            String kor = parts.length > 1 ? parts[1].trim() : "";
            System.out.printf("%d) %s = %s%n", i + 1, eng, kor);
        }

        System.out.print("삭제할 번호를 입력하세요 (q 입력시 취소): ");
        String input = scanner.nextLine().trim();
        if (input.equalsIgnoreCase("q")) {
            System.out.println("삭제 취소.");
            return;
        }

        int idx;
        try {
            idx = Integer.parseInt(input) - 1;
        } catch (NumberFormatException e) {
            System.out.println("숫자를 입력해야 합니다.");
            return;
        }

        if (idx < 0 || idx >= lines.size()) {
            System.out.println("유효하지 않은 번호입니다.");
            return;
        }

        lines.remove(idx);

        try (PrintWriter pw = new PrintWriter(
                new OutputStreamWriter(new FileOutputStream(vocaFile, false), StandardCharsets.UTF_8))) {
            for (String line : lines) {
                pw.println(line);
            }
            System.out.println("삭제가 완료되었습니다.");
        } catch (IOException e) {
            System.out.println("파일을 쓸 수 없습니다: " + e.getMessage());
        }
    }

    void editVoca() {
        System.out.println("==== 단어 수정 ====");
        ArrayList<String> lines = new ArrayList<>();
        if (!vocaFile.exists()) {
            System.out.println("단어장이 존재하지 않습니다.");
            return;
        }

        try (Scanner fileScanner = new Scanner(vocaFile, StandardCharsets.UTF_8.name())) {
            while (fileScanner.hasNextLine()) {
                lines.add(fileScanner.nextLine());
            }
        } catch (FileNotFoundException e) {
            System.out.println("파일을 읽을 수 없습니다.");
            return;
        }

        if (lines.isEmpty()) {
            System.out.println("단어가 등록되어 있지 않습니다.");
            return;
        }

        for (int i = 0; i < lines.size(); i++) {
            String[] parts = lines.get(i).split("\t", 2);
            String eng = parts.length > 0 ? parts[0].trim() : "";
            String kor = parts.length > 1 ? parts[1].trim() : "";
            System.out.printf("%d) %s = %s%n", i + 1, eng, kor);
        }

        System.out.print("수정할 번호를 입력하세요 (q 입력시 취소): ");
        String input = scanner.nextLine().trim();
        if (input.equalsIgnoreCase("q")) {
            System.out.println("수정 취소.");
            return;
        }

        int idx;
        try {
            idx = Integer.parseInt(input) - 1;
        } catch (NumberFormatException e) {
            System.out.println("숫자를 입력해야 합니다.");
            return;
        }

        if (idx < 0 || idx >= lines.size()) {
            System.out.println("유효하지 않은 번호입니다.");
            return;
        }

        String[] parts = lines.get(idx).split("\t", 2);
        String curEng = parts.length > 0 ? parts[0].trim() : "";
        String curKor = parts.length > 1 ? parts[1].trim() : "";

        System.out.println("현재: " + curEng + " = " + curKor);
        System.out.print("새 영단어 (엔터 입력 시 유지): ");
        String newEng = scanner.nextLine().trim();
        System.out.print("새 뜻 (엔터 입력 시 유지, 여러 뜻은 '/'로 구분): ");
        String newKor = scanner.nextLine().trim();

        if (newEng.isEmpty())
            newEng = curEng;
        if (newKor.isEmpty())
            newKor = curKor;

        for (int i = 0; i < lines.size(); i++) {
            if (i == idx)
                continue;
            String[] p = lines.get(i).split("\t", 2);
            if (p[0].trim().equalsIgnoreCase(newEng)) {

                if (p[1].trim().equals(newKor)) {
                    System.out.println("이미 존재하는 단어입니다! 다시 확인해 주세요.");
                    return;
                }

                ArrayList<String> list = new ArrayList<>(Arrays.asList(p[1].trim().split("/")));
                if (list.contains(newKor)) {
                    System.out.println("이미 해당 뜻이 존재합니다! 다시 확인해 주세요.");
                    return;
                }

                list.add(newKor);
                lines.set(i, newEng + "\t" + String.join("/", list));
                System.out.println("이미 존재하는 영단어입니다. 뜻을 추가합니다.");
                return;
            }
        }

        lines.set(idx, newEng + "\t" + newKor);

        try (PrintWriter pw = new PrintWriter(
                new OutputStreamWriter(new FileOutputStream(vocaFile, false), StandardCharsets.UTF_8))) {
            for (String line : lines)
                pw.println(line);
            System.out.println("수정이 완료되었습니다.");
        } catch (IOException e) {
            System.out.println("파일을 쓸 수 없습니다: " + e.getMessage());
        }
    }

    void searchVoca() {
        System.out.println("==== 단어 검색 ====");
        if (!vocaFile.exists()) {
            System.out.println("단어장이 존재하지 않습니다.");
            return;
        }

        System.out.print("검색어 입력: ");
        String q = scanner.nextLine().trim().toLowerCase();
        if (q.isEmpty()) {
            System.out.println("검색어를 입력하세요.");
            return;
        }

        boolean found = false;
        try (Scanner fileScanner = new Scanner(vocaFile, StandardCharsets.UTF_8.name())) {
            int idx = 0;
            while (fileScanner.hasNextLine()) {
                String line = fileScanner.nextLine();
                idx++;
                String[] parts = line.split("\t", 2);
                String eng = parts.length > 0 ? parts[0].trim() : "";
                String kor = parts.length > 1 ? parts[1].trim() : "";
                if (eng.toLowerCase().contains(q) || kor.toLowerCase().contains(q)) {
                    System.out.printf("%d) %s = %s%n", idx, eng, kor);
                    found = true;
                }
            }
        } catch (FileNotFoundException e) {
            System.out.println("파일을 읽을 수 없습니다.");
            return;
        }

        if (!found) {
            System.out.println("검색 결과가 없습니다.");
        }
    }

}