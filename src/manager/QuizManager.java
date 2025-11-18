package manager;

import data.User;
import util.Path;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

public class QuizManager {

    static Scanner sc = new Scanner(System.in, StandardCharsets.UTF_8);
    static Random ran = new Random();

    ArrayList<String> noteWords = new ArrayList<>(); // 오답노트에 추가될 단어들
    final User user; //현재 사용자
    private String currentSourceFile = null;

    public QuizManager(User user) {
        this.user = user;
    }

    public void personalWordQuiz(ArrayList<String> list) {
        // TODO: 개인 단어장 퀴즈 구현
        // 단어장 목록 출력 및 선택, 문제풀기
        String chosen = pickFileFromList("개인 단어장 선택", list);
        if (chosen == null)
            return;
        ArrayList<String> words = loadWordsFromFile(chosen);

        if (words == null || words.isEmpty()) {
            System.out.println("단어가 등록되어 있지 않습니다.");
            return;
        }
        currentSourceFile = chosen;
        QuizMenu("개인 단어장 -" + fileNameOnly(chosen) + "-", words);
    }

    public void personalNoteQuiz(ArrayList<String> list) {
        // TODO: 개인 오답노트 퀴즈 구현
        // 오답노트 목록 출력 및 선택, 문제풀기
        String chosen = pickFileFromList("오답노트 선택", list);
        if (chosen == null)
            return;
        ArrayList<String> words = loadWordsFromFile(chosen);

        if (words == null || words.isEmpty()) {
            System.out.println("오답노트가 비어 있습니다.");
            return;
        }
        currentSourceFile = chosen;
        QuizMenu("오답노트 -" + fileNameOnly(chosen) + "-", words);
    }

    public void personalFavoriteQuiz(String favoriteWordsFilename) {
        // TODO: 즐겨찾기 퀴즈 구현
        ArrayList<String> words = loadWordsFromFile(favoriteWordsFilename);

        if (words == null || words.isEmpty()) {
            System.out.println("즐겨찾기 단어가 없습니다.");
            return;
        }
        QuizMenu("즐겨찾기 (" + fileNameOnly(favoriteWordsFilename) + ")", words);

    }

    public void publicWordQuiz(String filename) {
        ArrayList<String> words = loadWordsFromFile(filename);

        if (words == null || words.isEmpty()) {
            System.out.println("공용 단어가 없습니다.");
            return;
        }
        QuizMenu("공용 단어장 (" + fileNameOnly(filename) + ")", words);

    }

    public void publicFrequentlyMissedQuiz() {
        // TODO: 추후 구현
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

    private void QuizMenu(String title, ArrayList<String> list) {
        if (list == null || list.isEmpty()) {
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
                case 1 -> shortAnswerQuestion(list);
                case 2 -> multipleChoiceQuestion(list);
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

    private void shortAnswerQuestion(ArrayList<String> list) {
        if (list == null) { //받은 리스트에 단어가 하나도 없으면
            System.out.println("단어가 등록되어 있지 않습니다.");
            return;
        }

        //문제 수 입력
        System.out.print("문제 수를 입력해주세요 : ");
        int quizNum = sc.nextInt();
        sc.nextLine();

        if (quizNum < 1) { //문제 수 입력할 때 0이나 음수를 적으면
            System.out.println("1문제 이상 출제되어야 합니다.");
            return;
        } else if (quizNum > list.size()) { //입력한 문제 수가 가능한 문제 수보다 많으면
            quizNum = list.size(); //문제 수를 가능한 최대의 문제 수로 설정
        }

        boolean[] usedIndex = new boolean[list.size()]; //중복되는 문제를 방지하기 위함
        int score = 0; //사용자의 점수
        for (int i = 0; i < quizNum; i++) { //문제 수만큼 반복
            int answerIndex;
            do {
                answerIndex = ran.nextInt(list.size()); //리스트에 있는 랜덤의 단어 고르기
            } while (usedIndex[answerIndex]); //중복되지 않을 때까지
            usedIndex[answerIndex] = true; //성공적으로 고른 단어를 중복 표시

            // aEng (영어), aKor (한국어) 나누기
            String[] a = list.get(answerIndex).split("\t", 2);
            String aEng = a[0].trim();
            String aKor = a[1].trim();

            ArrayList<String> aKorList = new ArrayList<>(); //한국어 뜻이 2개 이상인 경우를 위함

            if (aKor.contains("/")) {
                String[] aKorArr = aKor.split("/");
                for (String kor : aKorArr) {
                    aKorList.add(kor.trim()); // 슬래시를 기준으로 각 단어들을 리스트에 추가
                }
            } else {
                aKorList.add(aKor); // 슬래시가 없다면 한국어 뜻이 하나만 있으므로 aKor만 추가
            }

            int randquiz = ran.nextInt(2) + 1; //한국어 -> 영어 | 영어 -> 한국어 문제 형식 정하기
            if (randquiz == 1) { //영어 -> 한국어
                //문제 출력 (형식: [문제 번호/총 문제수] '영어'의 뜻은?)
                System.out.println("\n[" + (i + 1) + "/" + quizNum + "] " + aEng + "의 뜻은?");
                String answer = sc.nextLine().trim();

                if (aKorList.contains(answer) || aKor.equals(answer)) { //한국어 뜻 중 하나만 적거나 전부 적으면
                    System.out.println("정답!");
                    score++; //점수 증가
                } else {
                    System.out.println("오답!");
                    addToNote(aEng, aKor); //오답노트에 추가될 단어를 리스트에 추가

                    String answerStr = ""; //출력할 정답
                    for (String kor : aKorList) {
                        answerStr += kor + "/ "; //한국어 / 한국어2 / ... / 형식
                    }
                    answerStr = answerStr.substring(0, answerStr.length() - 2); //맨 마지막의 /를 지우기
                    System.out.println("정답은 " + aEng + " = " + answerStr); //(형식: 정답은 '영어' = '한국어 / 한국어2 / ...)
                }

            } else { //한국어 -> 영어

                // answerStr과 같은 원리
                String questionStr = "";
                for (String kor : aKorList) {
                    questionStr += kor + "/ ";
                }
                questionStr = questionStr.substring(0, questionStr.length() - 2);

                //문제 출력 (형식: [문제 번호/총 문제수] '한국어'의 뜻은?)
                System.out.println("\n[" + (i + 1) + "/" + quizNum + "] " + questionStr + "의 뜻은?");
                String answer = sc.nextLine().trim();

                if (answer.equals(aEng)) { //입력한 영어와 같다면
                    System.out.println("정답!");
                    score++; //점수 증가
                } else {
                    System.out.println("오답!");
                    addToNote(aEng, aKor); //오답노트에 추가될 단어를 리스트에 추가
                    System.out.println("정답은 " + aEng + " = " + questionStr); //(형식: 정답은 '영어' = '한국어 / 한국어2 / ...)
                }
            }

        }
        // (형식: 총 {문제 수}문제 중 {맞힌 개수}개 정답 (정답률 {소수점 첫째자리까지의 정답률})
        System.out.printf("\n총 %d문제 중 %d개 정답 (정답률 %.1f%%)\n", quizNum, score, 100.0 * score / quizNum);
        createNote(); //퀴즈가 끝난 뒤 오답노트 생성
    }

    private void multipleChoiceQuestion(ArrayList<String> list, boolean isPublic) {
        // TODO: 객관식 각 문제 구현
        if (list == null) {
            System.out.println("단어가 등록되어 있지 않습니다.");
            return;
        } else if (list.size() < 4) {
            System.out.println("객관식 보기를 만들 단어(4개)가 부족합니다.");
            return;
        }

        System.out.print("문제 수를 입력해주세요 : ");
        int quizNum = sc.nextInt();
        sc.nextLine();

        if (quizNum < 1) {
            System.out.println("1문제 이상 출제되어야 합니다.");
            return;
        } else if (quizNum > list.size()) {
            quizNum = list.size();
        }

        boolean[] usedIndex = new boolean[list.size()];
        ArrayList<String> wrongs = new ArrayList<>();
        int score = 0;
        for (int i = 0; i < quizNum; i++) {
            int answerIndex;
            do {
                answerIndex = ran.nextInt(list.size());
            } while (usedIndex[answerIndex]);
            usedIndex[answerIndex] = true;

            String[] a = list.get(answerIndex).split("\t", 2);
            String aEng = a[0].trim();
            String aKor = a[1].trim();

            int[] choiceIndex = new int[4];
            choiceIndex[0] = answerIndex;
            int filled = 1;

            while (filled < 4) {
                int r = ran.nextInt(list.size());
                boolean usedWord = false;
                for (int j = 0; j < filled; j++) {
                    if (choiceIndex[j] == r) {
                        usedWord = true;
                        break;
                    }
                }
                if (!usedWord)
                    choiceIndex[filled++] = r;
            }

            for (int k = 0; k < 4; k++) {
                int s = ran.nextInt(4);
                int temp = choiceIndex[k];
                choiceIndex[k] = choiceIndex[s];
                choiceIndex[s] = temp;
            }

            System.out.println("\n[" + (i + 1) + "/" + quizNum + "] " + aEng + "의 뜻은?");
            for (int k = 0; k < 4; k++) {
                String[] s = list.get(choiceIndex[k]).split("\t", 2);
                String kor = s[1].trim();
                String showKor = kor.contains("/") ? kor.split("/")[0].trim() : kor;
                System.out.println((k + 1) + ") " + showKor);
            }

            int choice = -1;
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

            if (choiceIndex[choice - 1] == answerIndex) {
                System.out.println("정답!");
                score++;
            } else {
                int correctNum = -1;
                for (int k = 0; k < 4; k++) {
                    if (choiceIndex[k] == answerIndex) {
                        correctNum = k + 1;
                        break;
                    }
                }
                System.out.println("오답!");
                addToNote(aEng, aKor);
                System.out.println("정답은 [" + correctNum + "번] " + aEng + " = " + aKor);
                wrongs.add(aEng + "\t" + aKor);
            }
        }
        System.out.printf("\n총 %d문제 중 %d개 정답 (정답률 %.1f%%)\n", quizNum, score, 100.0 * score / quizNum);
        createNote();
    }
}
