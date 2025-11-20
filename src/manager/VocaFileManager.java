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

// 단어장 파일 관리의 기본 기능을 제공하는 추상 클래스입니다. 
// PersonalVocaFileManager가 이 클래스를 상속받아 구체적인 기능을 확장합니다.

public abstract class VocaFileManager {
    File vocaFile;
    // 관리할 단어장 파일 객체

    Scanner scanner = new Scanner(System.in, StandardCharsets.UTF_8);
    // 사용자 입력을 받기 위한 스캐너 (UTF-8 설정)

    String username;
    // 현재 로그인한 사용자 이름 (자식 클래스에서 동기화 로직에 사용됨)

    // 생성자: 관리할 파일 경로와 사용자 이름을 받습니다.
    VocaFileManager(String fileName, String username) {
        vocaFile = new File(fileName);
        // 파일 객체 초기화

        this.username = username;
        // 사용자 이름 저장
    }

    // 자식 클래스에서 구현될 메뉴 진입점
    abstract public void menu();

    // 단어 추가 기능
    void addVoca() {
        System.out.println("==== 단어 추가 ====");
        System.out.print("영단어: ");
        String eng = scanner.nextLine().trim();
        // 영단어 입력 및 공백 제거

        // 유효성 검사 - 영어, 띄어쓰기, 하이픈만 허용. 이외의 특수기호는 즐겨찾기 토글 로직과 충돌 할 수 있기에 배제함
        if (!eng.matches("^[a-zA-Z][a-zA-Z -]*$")) {
            System.out.println("영단어에는 영어/띄어쓰기/하이픈만 사용할 수 있습니다.");
            return;
            // 유효하지 않은 문자 포함 시 중단
        }
        if (eng.isEmpty()) {
            System.out.println("영단어는 비어있을 수 없습니다.");
            return;
            // 빈 값 입력 시 중단
        }

        System.out.print("뜻(여러 뜻은 '/'로 구분): ");
        String kor = scanner.nextLine().trim();
        // 뜻 입력

        if (kor.isEmpty()) {
            System.out.println("뜻은 비어있을 수 없습니다.");
            return;
            // 빈 값 입력 시 중단
        }

        File parent = vocaFile.getParentFile();
        // 파일이 저장될 상위 디렉토리 확인

        if (parent != null && !parent.exists())
            parent.mkdirs();
        // 상위 디렉토리가 없으면 생성함

        ArrayList<String> lines = new ArrayList<>();
        // 파일 내용을 저장할 리스트

        // 기존 파일 내용을 읽어오기
        if (vocaFile.exists()) {
            try (Scanner fileScanner = new Scanner(vocaFile, StandardCharsets.UTF_8)) {
                while (fileScanner.hasNextLine()) {
                    lines.add(fileScanner.nextLine());
                }
            } catch (IOException e) {
                System.out.println("파일 읽기 중 오류 발생: " + e.getMessage());
                // 읽기 권한 문제 등 예외 처리
            }
        }

        boolean updated = false;
        // 기존 단어에 뜻을 추가했는지 여부 확인기

        // 중복 단어 검사 및 병합 로직
        for (int i = 0; i < lines.size(); i++) {
            String[] parts = lines.get(i).split("\t", 2);
            // 탭으로 단어와 뜻 분리

            // parts[0]에는 토글 문자(* 등)가 포함될 수 있으므로, 순수 영단어 비교를 위해 정제 필요할 수 있음
            // 하지만 addVoca는 기본적으로 '새 단어'를 다루므로 여기선 단순 비교 수행
            // 개인 단어장 관리 섹션에서 조금 더 자세하게 다룸
            if (parts[0].trim().equalsIgnoreCase(eng)) {
                // 이미 존재하는 영단어 발견 시,

                ArrayList<String> meanings = new ArrayList<>(Arrays.asList(parts[1].trim().split("/")));
                // 기존 뜻들을 리스트로 변환

                if (meanings.contains(kor)) {
                    System.out.println("이미 존재하는 단어입니다! 다시 확인해 주세요.");
                    return;
                    // 영단어와 뜻이 모두 같으면 추가 중단
                }

                meanings.add(kor);
                // 새로운 뜻 추가

                lines.set(i, eng + "\t" + String.join("/", meanings));
                // 해당 라인 업데이트

                updated = true;
                break;
                // for 루프 종료
            }
        }

        // 변경된 내용을 파일에 덮어쓰기
        try (PrintWriter pw = new PrintWriter(
                new OutputStreamWriter(new FileOutputStream(vocaFile, false), StandardCharsets.UTF_8))) {
            // 덮어쓰기 모드(false)로 파일 열기

            if (updated) {
                // 기존 단어에 뜻만 추가된 경우
                System.out.println("이미 존재하는 영단어입니다. 뜻을 추가합니다.");
                for (String line : lines)
                    pw.println(line);
                // 전체 라인 다시 쓰기
            } else {
                // 완전히 새로운 단어인 경우
                for (String line : lines)
                    pw.println(line);
                // 기존 내용 쓰고
                pw.printf("%s\t%s%n", eng, kor);
                // 새 단어 맨 뒤에 추가
                System.out.println("단어가 추가되었습니다.");
            }

        } catch (IOException e) {
            System.out.println("단어 추가 중 오류: " + e.getMessage());
            // 파일 쓰기 실패 시 에러 메시지
        }
    }

    // 단어 삭제 기능
    void removeVoca() {
        System.out.println("==== 단어 삭제 ====");
        ArrayList<String> lines = new ArrayList<>();

        if (!vocaFile.exists()) {
            System.out.println("단어장이 존재하지 않습니다.");
            return;
            // 파일이 없으면 삭제할 것도 없음
        }

        try (Scanner fileScanner = new Scanner(vocaFile, StandardCharsets.UTF_8.name())) {
            while (fileScanner.hasNextLine()) {
                lines.add(fileScanner.nextLine());
            }
            // 파일 전체 읽기
        } catch (FileNotFoundException e) {
            System.out.println("파일을 읽을 수 없습니다.");
            return;
            // 파일 접근 불가 시 종료
        }

        if (lines.isEmpty()) {
            System.out.println("단어가 등록되어 있지 않습니다.");
            return;
            // 내용이 텅텅 비어있음
        }

        // 삭제할 목록 출력
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
            // 사용자 취소
        }

        int idx;
        try {
            idx = Integer.parseInt(input) - 1;
            // 사용자 입력(1부터 시작)을 인덱스(0부터 시작)로 변환
        } catch (NumberFormatException e) {
            System.out.println("숫자를 입력해야 합니다.");
            return;
            // 숫자가 아닌 입력 처리
        }

        if (idx < 0 || idx >= lines.size()) {
            System.out.println("유효하지 않은 번호입니다.");
            return;
            // 범위 벗어난 번호 처리
        }

        lines.remove(idx);
        // 리스트에서 해당 항목 제거

        // 변경된 리스트를 파일에 덮어쓰기
        try (PrintWriter pw = new PrintWriter(
                new OutputStreamWriter(new FileOutputStream(vocaFile, false), StandardCharsets.UTF_8))) {
            for (String line : lines) {
                pw.println(line);
            }
            System.out.println("삭제가 완료되었습니다.");
        } catch (IOException e) {
            System.out.println("파일을 쓸 수 없습니다: " + e.getMessage());
            // 저장 실패 시 에러 메시지
        }
    }

    // 단어 수정 기능
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

        // 수정할 목록 출력
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

        // 선택된 단어의 현재 정보 가져오기
        String[] parts = lines.get(idx).split("\t", 2);
        String curEng = parts.length > 0 ? parts[0].trim() : "";
        String curKor = parts.length > 1 ? parts[1].trim() : "";

        System.out.println("현재: " + curEng + " = " + curKor);

        // 새 정보 입력 받기 (빈 값 + 엔터 입력 시 유지)
        System.out.print("새 영단어 (엔터 입력 시 유지): ");
        String newEng = scanner.nextLine().trim();
        System.out.print("새 뜻 (엔터 입력 시 유지, 여러 뜻은 '/'로 구분): ");
        String newKor = scanner.nextLine().trim();

        if (newEng.isEmpty())
            newEng = curEng;
        // 입력 없으면 기존 값 유지
        if (newKor.isEmpty())
            newKor = curKor;
        // 입력 없으면 기존 값 유지

        // 중복 검사 (수정하려는 단어가 이미 다른 곳에 존재하는지)
        for (int i = 0; i < lines.size(); i++) {
            if (i == idx)
                continue;
            // 자기 자신은 제외하고 비교

            String[] p = lines.get(i).split("\t", 2);
            if (p[0].trim().equalsIgnoreCase(newEng)) {
                // 같은 철자의 영단어가 다른 라인에 존재함

                if (p[1].trim().equals(newKor)) {
                    System.out.println("이미 존재하는 단어입니다! 다시 확인해 주세요.");
                    return;
                    // 뜻까지 완전히 같으면 중복이므로 거부
                }

                // 영단어는 같지만 뜻이 다른 경우 -> 병합 유도 또는 경고 출력하기
                // 여기서는 병합하여 저장하는 로직 수행
                ArrayList<String> list = new ArrayList<>(Arrays.asList(p[1].trim().split("/")));
                if (list.contains(newKor)) {
                    System.out.println("이미 해당 뜻이 존재합니다! 다시 확인해 주세요.");
                    return;
                }

                list.add(newKor);
                lines.set(i, newEng + "\t" + String.join("/", list));
                // 기존에 있던 다른 라인에 뜻을 추가하고 병합 후 종료 처리
                System.out.println("이미 존재하는 영단어입니다. 해당 항목에 뜻을 추가했습니다.");

                // 기존 idx 라인의 저장이나 꼬임 문제를 피하기 위해 여기서는 파일 저장을 수행하고 종료.

                try (PrintWriter pw = new PrintWriter(
                        new OutputStreamWriter(new FileOutputStream(vocaFile, false), StandardCharsets.UTF_8))) {
                    for (String line : lines) {
                        pw.println(line);
                    }
                } catch (IOException e) {
                    System.out.println("파일 저장 실패: " + e.getMessage());
                }
                return;
            }
        }

        // 중복이 없으면 해당 라인을 새 내용으로 교체
        lines.set(idx, newEng + "\t" + newKor);

        // 파일 저장
        try (PrintWriter pw = new PrintWriter(
                new OutputStreamWriter(new FileOutputStream(vocaFile, false), StandardCharsets.UTF_8))) {
            for (String line : lines)
                pw.println(line);
            System.out.println("수정이 완료되었습니다.");
        } catch (IOException e) {
            System.out.println("파일을 쓸 수 없습니다: " + e.getMessage());
        }
    }

    // 단어 검색 기능
    void searchVoca() {
        System.out.println("==== 단어 검색 ====");
        if (!vocaFile.exists()) {
            System.out.println("단어장이 존재하지 않습니다.");
            return;
        }

        System.out.print("검색어 입력: ");
        String q = scanner.nextLine().trim().toLowerCase();
        // 검색어를 소문자로 변환하여 대소문자 구분 없이 검색

        if (q.isEmpty()) {
            System.out.println("검색어를 입력하세요.");
            return;
        }

        boolean found = false;
        // 검색 결과 존재 여부 플래그

        try (Scanner fileScanner = new Scanner(vocaFile, StandardCharsets.UTF_8.name())) {
            int idx = 0;
            while (fileScanner.hasNextLine()) {
                String line = fileScanner.nextLine();
                idx++;

                String[] parts = line.split("\t", 2);
                String eng = parts.length > 0 ? parts[0].trim() : "";
                String kor = parts.length > 1 ? parts[1].trim() : "";

                // 영단어 또는 뜻에 검색어가 포함되어 있는지 확인
                if (eng.toLowerCase().contains(q) ||
                        kor.toLowerCase().contains(q)) {
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