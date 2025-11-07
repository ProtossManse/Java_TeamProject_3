package manager;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Scanner;

public class PersonalVocaFileManager extends VocaFileManager {
    public PersonalVocaFileManager(String fileName) {
        super(fileName);
    }

    @Override
    public void menu() {
        int choice = 0;
        while (choice != 6) {
            System.out.println("1. 단어 추가");
            System.out.println("2. 단어 삭제");
            System.out.println("3. 단어 수정");
            System.out.println("4. 단어 검색( * 검색 시 즐겨찾기 단어 검색)");
            System.out.println("5. 단어 즐겨찾기");
            System.out.println("6. 뒤로가기");
            System.out.print(">> ");
            choice = super.scanner.nextInt();
            super.scanner.nextLine();
            switch (choice) {
                case 1 -> super.addVoca();
                case 2 -> super.removeVoca();
                case 3 -> super.editVoca();
                case 4 -> super.searchVoca();
                case 5 -> favoriteVoca();
                case 6 -> System.out.println("메인 메뉴로 돌아갑니다.");
            }
        }
    }

    private void favoriteVoca() {
        // 즐겨찾기 토글 (읽기/쓰기 모두 UTF-8)
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
            System.out.println("파일을 읽는 중 오류 발생");
            return;
        }

        if (lines.isEmpty()) {
            System.out.println("단어가 등록되어 있지 않습니다.");
            return;
        }

        System.out.println("===== 단어 목록 =====");
        for (int i = 0; i < lines.size(); i++) {
            System.out.printf("%d) %s%n", i + 1, lines.get(i));
        }

        System.out.print("\n즐겨찾기 설정/해제할 단어 번호 입력 (0 : 취소) : ");
        int index;
        try {
            index = Integer.parseInt(super.scanner.nextLine().trim());
        } catch (NumberFormatException e) {
            System.out.println("잘못된 입력입니다.");
            return;
        }

        if (index == 0) return;
        if (index < 1 || index > lines.size()) {
            System.out.println("잘못된 입력입니다.");
            return;
        }

        String selected = lines.get(index - 1);

        if (selected.startsWith("*")) {
            selected = selected.substring(1);
            System.out.println("즐겨찾기 해제 완료!");
        } else {
            selected = "*" + selected;
            System.out.println("즐겨찾기 추가 완료!");
        }

        lines.set(index - 1, selected);

        // 파일에 UTF-8로 덮어쓰기
        try (PrintWriter pw = new PrintWriter(
                new OutputStreamWriter(new FileOutputStream(vocaFile, false), StandardCharsets.UTF_8))) {
            for (String line : lines) {
                pw.println(line);
            }
        } catch (IOException e) {
            System.out.println("파일 저장 중 오류 발생: " + e.getMessage());
        }
    }


}
