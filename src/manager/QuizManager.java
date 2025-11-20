package manager;

import data.PublicWord;
import data.User;
import data.Word;
import util.Path;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import java.util.Scanner;

public class QuizManager {

    static Scanner sc = new Scanner(System.in, StandardCharsets.UTF_8);
    static Random ran = new Random();

    ArrayList<String> noteWords = new ArrayList<>(); // 오답노트에 추가될 단어들
    final User user; //현재 사용자

    int score;

    public QuizManager(User user) {
        this.user = user;
    }


    public void personalWordQuiz(ArrayList<String> fileList) {
        //단어장 목록 출력 및 선택
        String chosen = pickFileFromList("개인 단어장 선택", fileList);
        if (chosen == null) return;
        //파일명을 전체 경로로 변환
        String fullPath = Path.getVocaFilePath(user.getName(), chosen);
        //파일에서 단어 읽기
        ArrayList<String> strings = loadWordsFromFile(fullPath);
        if (strings == null || strings.isEmpty()) {
            System.out.println("단어가 등록되어 있지 않습니다.");
            return;
        }
        ArrayList<Word> words = new ArrayList<>();
        for (String line : strings) {
            String[] parts = line.split("\t");
            words.add(new Word(parts[0].trim(), parts[1].trim()));
        }
        // 퀴즈 메뉴로 넘기기
        QuizMenu("개인 단어장 -" + fileNameOnly(fullPath) + "- ", words, false);
    }


    public void personalNoteQuiz(ArrayList<String> fileList) {
        // 오답노트 목록 출력 및 선택
        String chosen = pickFileFromList("오답노트 선택", fileList);
        if (chosen == null) return;
        // 파일명을 전체 경로로 변환
        String fullPath = Path.getNoteFilePath(user.getName(), chosen);
        // 파일에서 단어 읽기
        ArrayList<String> strings = loadWordsFromFile(fullPath);
        if (strings == null || strings.isEmpty()) {
            System.out.println("오답노트가 비어 있습니다.");
            return;
        }
        ArrayList<Word> words = new ArrayList<>();
        for (String line : strings) {
            String[] parts = line.split("\t");
            words.add(new Word(parts[0].trim(), parts[1].trim()));
        }
        // 퀴즈 메뉴로 넘기기
        QuizMenu("오답노트 -" + fileNameOnly(fullPath) + "- ", words, false);
    }

    public void personalFavoriteQuiz(String favoriteWordsFilename) {
        // 파일에서 단어 읽기
        ArrayList<String> strings = loadWordsFromFile(favoriteWordsFilename);
        if (strings == null || strings.isEmpty()) {
            System.out.println("즐겨찾기 단어가 없습니다.");
            return;
        }
        ArrayList<Word> words = new ArrayList<>();
        for (String line : strings) {
            String[] parts = line.split("\t");
            words.add(new Word(parts[0].trim(), parts[1].trim()));
        }

        // 퀴즈 메뉴로 넘기기
        QuizMenu("즐겨찾기 (" + fileNameOnly(favoriteWordsFilename) + ")", words, false);
    }

    public void publicWordQuiz() {
        // 파일명을 전체 경로로 변환
        String path = Path.getPublicFilePath();
        // 파일에서 단어 읽기
        ArrayList<String> strings = loadWordsFromFile(path);
        if (strings == null || strings.isEmpty()) {
            System.out.println("단어가 등록되어 있지 않습니다.");
            return;
        }
        ArrayList<Word> words = new ArrayList<>();
        for (String line : strings) {
            String[] parts = line.split("\t");
            if (parts.length == 4)
                words.add(new PublicWord(parts[0].trim(), parts[1].trim(), Integer.parseInt(parts[2].trim()), Integer.parseInt(parts[3].trim())));
            else words.add(new PublicWord(parts[0].trim(), parts[1].trim()));
        }

        // 퀴즈 메뉴로 넘기기
        QuizMenu("공용 단어장 -" + fileNameOnly(path) + "- ", words, true);
    }


    public void publicFrequentlyMissedQuiz() {

        // 2. 전체 경로 변환
        String fullPath = Path.getPublicFilePath();


        // 3. 파일 읽기
        ArrayList<String> lines = loadWordsFromFile(fullPath);
        if (lines == null || lines.isEmpty()) {
            System.out.println("통계 정보가 있는 단어가 없습니다.");
            return;
        }

        // 4. 정답률 < 0.5 단어만 필터링
        ArrayList<Word> filtered = new ArrayList<>();

        for (String line : lines) {
            String[] parts = line.split("\t");
            if (parts.length < 2) continue; // eng, kor 미존재

            String eng = parts[0].trim();
            String kor = parts[1].trim();

            int total = 0;
            int correct = 0;


            if (parts.length >= 3) {
                try {
                    total = Integer.parseInt(parts[2].trim());
                } catch (NumberFormatException ignored) {
                    System.out.println("통계가 없습니다!");
                }
            }
            if (parts.length >= 4) {
                try {
                    correct = Integer.parseInt(parts[3].trim());
                } catch (NumberFormatException ignored) {
                    System.out.println("통계가 없습니다!");
                }
            }

            // total==0이면 출제한 적 없으니 제외
            if (total == 0) continue;

            PublicWord word = new PublicWord(eng, kor, total, correct);


            // 정답률 50% 미만인 단어만 포함
            if (word.getCorrectionRate() < 0.5) {
                filtered.add(word);
            }
        }

        if (filtered.isEmpty()) {
            System.out.println("정답률 50% 미만인 단어가 없습니다.");
            return;
        }

        QuizMenu("정답률 50% 미만 공용단어장 퀴즈 - " + fileNameOnly(fullPath), filtered, true);
    }


    private void createNote() { // 주어진 문제를 전부 풀고 난 뒤 오답노트 파일 만들기
        if (noteWords.isEmpty()) { // 노트에 추가될 단어가 없으면
            System.out.println("틀린 단어가 없습니다.");
            return;
        }

        //오답노트 파일 위치 가져오기
        File notes = new File(Path.getNoteDirPath(user.getName()));

        //파일 이름을 현재 시간으로 설정
        LocalDateTime now = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyyMMdd_HH_mm_ss");
        String formatted = now.format(formatter);

        File noteFile = new File(notes, "note-" + formatted + ".txt");

        //텍스트 파일 내에 오답노트 단어들 전부 추가
        try (PrintWriter pw = new PrintWriter(
                new OutputStreamWriter(new FileOutputStream(noteFile, false), StandardCharsets.UTF_8))) {
            for (String word : noteWords) {
                pw.println(word);
            }
            System.out.println("오답노트 생성 완료");
        } catch (IOException e) {
            System.out.println("파일 저장 중 오류 발생: " + e.getMessage());
        }

        noteWords.clear(); //다음 퀴즈 오답노트를 만들기 위해 오답노트에 추가할 단어 리스트는 비우기
    }

    private void addToNote(String aEng, String aKor) { // 주관/객관식 문제에서 오답이 나올때마다 오답노트에 추가
        String entry = aEng + "\t" + aKor; // 각 단어는 영어 \t 한국어 형식으로 리스트에 추가
        if (!noteWords.contains(entry)) {
            noteWords.add(entry); //리스트에 중복되는 단어가 없을 때 리스트에 단어 추가
        }
    }

    private void QuizMenu(String title, ArrayList<Word> words, boolean isPublic) {
        if (words == null || words.isEmpty()) {
            System.out.println("단어가 등록되어 있지 않습니다.");
            return;
        }

        while (true) {
            System.out.println("\n===== [" + title + " 퀴즈 유형 선택] =====");
            System.out.println("1) 주관식 퀴즈");
            System.out.println("2) 객관식 퀴즈");
            System.out.println("0) 뒤로가기");
            System.out.print("번호 선택: ");

            int mode = readInt();
            if (mode == 0)
                break;

            switch (mode) {
                case 1 -> shortAnswerQuestion(words, isPublic);
                case 2 -> multipleChoiceQuestion(words, isPublic);
                default -> System.out.println("잘못된 선택입니다.");
            }
        }
    }

    private ArrayList<String> loadWordsFromFile(String pathStr) {
        try {
            java.nio.file.Path p = java.nio.file.Paths.get(pathStr);
            ArrayList<String> out = new ArrayList<>();

            for (String line : java.nio.file.Files.readAllLines(p, StandardCharsets.UTF_8)) {
                String t = line.trim();
                if (t.isEmpty() || t.startsWith("#"))
                    continue;
                int tab = t.indexOf('\t');
                if (tab <= 0 || tab == t.length() - 1)
                    continue;
                out.add(t);
            }
            return out;

        } catch (IOException e) {
            System.out.println("파일을 읽는 중 오류가 발생했습니다: " + e.getMessage());
            return null;
        }
    }

    private String fileNameOnly(String path) {
        try {
            java.nio.file.Path p = java.nio.file.Paths.get(path);
            java.nio.file.Path fn = p.getFileName();
            return (fn == null) ? path : fn.toString();
        } catch (Exception e) {
            return path;
        }
    }

    private String pickFileFromList(String title, ArrayList<String> files) {
        if (files == null || files.isEmpty()) {
            System.out.println("선택할 파일이 없습니다.");
            return null;
        }
        System.out.println("\n===== [" + title + "] =====");
        for (int i = 0; i < files.size(); i++) {
            System.out.println((i + 1) + ") " + files.get(i));
        }
        System.out.println("0) 뒤로가기");
        System.out.print("번호 선택: ");

        int sel = readInt();

        if (sel == 0)
            return null;
        if (sel < 1 || sel > files.size()) {
            System.out.println("잘못된 선택입니다.");
            return null;
        }
        return files.get(sel - 1);
    }

    private int readInt() {
        while (true) {
            String s = sc.nextLine().trim();
            try {
                return Integer.parseInt(s);
            } catch (NumberFormatException e) {
                System.out.print("숫자를 입력하세요: ");
            }
        }
    }


    private String getKorStr(ArrayList<String> aKorList) {
        String korStr = ""; //출력할 정답
        for (String kor : aKorList) {
            korStr += kor + "/"; //한국어/한국어2/ ... / 형식
        }
        return korStr.substring(0, korStr.length() - 1); //맨 마지막의 /를 지우기
    }

    private void shortAnswerQuestion(ArrayList<Word> words, boolean isPublic) {

        if (words == null) { //받은 리스트에 단어가 하나도 없으면
            System.out.println("단어가 등록되어 있지 않습니다.");
            return;
        }

        // 출제할 문제 수 입력받기
        System.out.print("출제할 문제 수를 입력하세요: ");
        int quizNum = 0;
        while (true) {
            quizNum = readInt();
            if (quizNum < 1) {
                System.out.print("1문제 이상 출제해야 합니다. 다시 입력하세요: ");
            } else {
                break;
            }
        }
        if (quizNum > words.size()) {
            quizNum = words.size(); // 최대 단어 개수까지만
        }

        ArrayList<Word> mutableWords = new ArrayList<>(words);

        int i = 0;
        while (!mutableWords.isEmpty() && i < quizNum) {

            int randomIndex = ran.nextInt(mutableWords.size());
            Word word = mutableWords.get(randomIndex);
            mutableWords.remove(randomIndex);
            // aEng (영어), aKor (한국어) 나누기
            String aEng = word.getEnglish();
            String aKor = word.getKorean();

            if (aKor.contains("/")) {
                String[] aKorArr = aKor.split("/");
                for (String kor : aKorArr) {
                    word.koreanList.add(kor.trim()); // 슬래시를 기준으로 각 단어들을 리스트에 추가
                }
            } else {
                word.koreanList.add(aKor); // 슬래시가 없다면 한국어 뜻이 하나만 있으므로 aKor만 추가
            }

            if (isPublic && word instanceof PublicWord) {
                ((PublicWord) word).questions++;
            }

            int randquiz = ran.nextInt(2) + 1; //한국어 -> 영어 | 영어 -> 한국어 문제 형식 정하기
            if (randquiz == 1) { //영어 -> 한국어

                //문제 출력 (형식: [문제 번호/총 문제수] '영어'의 뜻은?)
                System.out.println("\n[" + (i + 1) + "/" + quizNum + "] " + aEng + "의 뜻은?");
                String answer = sc.nextLine().trim(); //대답에 모든 공백 지우기

                String[] userAnswers = answer.split("/"); // 슬래시를 기준으로 답 나누기
                boolean isCorrect = true; //정답인지 아닌지 판별

                for (String userAnswer : userAnswers) { //나눠진 답들 하나씩 볼 때
                    if (!word.koreanList.contains(userAnswer.trim())) {  // 하나라도 정답에 없으면
                        isCorrect = false; //오답 처리
                        break;
                    }
                }

                if (isCorrect) {
                    System.out.println("정답!");
                    if (isPublic && word instanceof PublicWord) {
                        ((PublicWord) word).correct++;
                    }
                    this.score++; //점수 증가
                } else {
                    System.out.println("오답!");
                    addToNote(aEng, aKor); //오답노트에 추가될 단어를 리스트에 추가

                    System.out.println("정답은 " + aEng + " = " + getKorStr(word.koreanList)); //(형식: 정답은 '영어' = '한국어/한국어2/...)
                }

            } else { //한국어 -> 영어

                //문제 출력 (형식: [문제 번호/총 문제수] '한국어'를(을) 영어로 하면?)
                String questionStr = getKorStr(word.koreanList);
                System.out.println("\n[" + (i + 1) + "/" + words.size() + "] " + "'" + questionStr + "'" + "를(을) 영어로 하면?");
                String answer = sc.nextLine().trim();

                if (answer.toLowerCase().equals(aEng)) { //입력한 영어와 같다면
                    System.out.println("정답!");
                    if (isPublic && word instanceof PublicWord) {
                        ((PublicWord) word).correct++;
                    }
                    this.score++; //점수 증가
                } else {
                    System.out.println("오답!");
                    addToNote(aEng, aKor); //오답노트에 추가될 단어를 리스트에 추가
                    System.out.println("정답은 " + aEng + " = " + questionStr); //(형식: 정답은 '영어' = '한국어/한국어2/...)
                }
            }
            i++;
        }
        // (형식: 총 {문제 수}문제 중 {맞힌 개수}개 정답 (정답률 {소수점 첫째자리까지의 정답률})
        System.out.printf("\n총 %d문제 중 %d개 정답 (정답률 %.1f%%)\n",
                words.size(), this.score, 100.0 * this.score / quizNum);
        if (isPublic) {
            updateStatistics(words);
        }
        createNote(); //퀴즈가 끝난 뒤 오답노트 생성
    }


    private void multipleChoiceQuestion(ArrayList<Word> words, boolean isPublic) {
        //단어가 없거나 보기 4개를 만들 수 없는 경우 반환
        if (words == null) {
            System.out.println("단어가 등록되어 있지 않습니다.");
            return;
        } else if (words.size() < 4) {
            System.out.println("객관식 보기를 만들 단어(4개)가 부족합니다.");
            return;
        }

        // 출제할 문제 수 입력받기
        System.out.print("출제할 문제 수를 입력하세요: ");
        int quizNum;
        while (true) {
            quizNum = readInt();
            if (quizNum < 1) {
                System.out.print("1문제 이상 출제해야 합니다. 다시 입력하세요: ");
            } else {
                break;
            }
        }
        if (quizNum > words.size()) {
            quizNum = words.size(); // 최대 단어 개수까지만
        }

        ArrayList<Word> mutableWords = new ArrayList<>(words);

        int i = 0;
        while (!mutableWords.isEmpty() && i < quizNum) {

            int randomIndex = ran.nextInt(mutableWords.size());
            Word word = mutableWords.get(randomIndex);
            mutableWords.remove(randomIndex);
            String aEng = word.getEnglish();
            String aKor = word.getKorean();

            if (isPublic && word instanceof PublicWord) {
                ((PublicWord) word).questions++;
            }

            // 무작위 temp 배열 생성 (정답 외 보기 용)
            ArrayList<Word> temp = new ArrayList<>(words);
            temp.remove(word);
            Collections.shuffle(temp);

            // 보기 구성
            ArrayList<Word> choices = new ArrayList<>();
            choices.add(word);
            choices.addAll(temp.subList(0, 3));

            //보기 섞기
            Collections.shuffle(choices);


            //문제 출력
            System.out.println("\n[" + (i + 1) + "/" + quizNum + "] " + word.getEnglish() + "의 뜻은?");

            for (int j = 0; j < choices.size(); j++) {
                String kor = choices.get(j).getKorean();
                String showKor = kor.contains("/") ? kor.split("/")[0].trim() : kor; // 뜻 여러 개 중 첫 번째 표시
                System.out.printf("%d) %s ", j + 1, showKor);
            }

            //사용자 입력 (1~4)
            int choice;
            while (true) {
                System.out.print("답(1~4): ");
                String line = sc.nextLine().trim();
                try {
                    choice = Integer.parseInt(line);
                    if (1 <= choice && choice <= 4)
                        break;
                    System.out.println("1~4 사이의 숫자를 입력하세요.");
                } catch (NumberFormatException e) {
                    System.out.println("숫자를 입력하세요.");
                }
            }
            //정답 확인
            if (choices.get(choice - 1).equals(word)) {
                System.out.println("정답!");
                if (isPublic && word instanceof PublicWord) {
                    ((PublicWord) word).correct++;
                }
                this.score++;
            } else {
                //정답 보기 번호 찾기
                int correctNum = -1;
                for (int k = 0; k < choices.size(); k++) {
                    if (choices.get(k).equals(word)) {
                        correctNum = choices.indexOf(word) + 1;
                        break;
                    }
                }
                System.out.println("오답!");
                addToNote(aEng, aKor);
                System.out.println("정답은 [" + correctNum + "번] " + aEng + " = " + aKor);
            }
            i++;
        }
        //결과 출력 + 오답노트 생성
        System.out.printf("\n총 %d문제 중 %d개 정답 (정답률 %.1f%%)\n",
                words.size(), this.score, 100.0 * this.score / quizNum);
        if (isPublic) {
            updateStatistics(words);
        }
        createNote();
    }

    private void updateStatistics(ArrayList<Word> list) {
        try (PrintWriter pw = new PrintWriter(
                new OutputStreamWriter(new FileOutputStream(Path.getPublicFilePath(), false), StandardCharsets.UTF_8))) {
            for (Word word : list) {
                if (word instanceof PublicWord)
                    pw.println(word);

            }
            System.out.println("단어 통계를 파일에 저장했습니다.");
        } catch (IOException e) {
            System.out.println("단어 통계 저장 중 오류 발생: " + e.getMessage());
        }
    }
}

