package manager;

import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Random;
import java.util.Scanner;

public class QuizManager {
    public void personalWordQuiz(ArrayList<String> list) {
        // TODO: 개인 단어장 퀴즈 구현
        // 단어장 목록 출력 및 선택, 문제풀기

    }

    public void personalNoteQuiz(ArrayList<String> list) {
        // TODO: 개인 오답노트 퀴즈 구현
        // 오답노트 목록 출력 및 선택, 문제풀기
    }

    public void personalFavoriteQuiz(String favoriteWordsFilename) {
        // TODO: 즐겨찾기 퀴즈 구현
    }

    public void publicWordQuiz() {
        // TODO: 추후 구현
    }

    public void publicFrequentlyMissedQuiz() {
        // TODO: 추후 구현
    }

    private void createNote() {
        // TODO: 오답노트 생성 구현
    }

    private void shortAnswerQuestion() {
        if (list == null) {
            System.out.println("단어가 등록되어 있지 않습니다.");
            return;
        }
        Scanner sc = new Scanner(System.in, StandardCharsets.UTF_8);
        Random ran = new Random();

        System.out.print("문제 수를 입력해주세요 : ");
        int quizNum = sc.nextInt();
        sc.nextLine();

        if(quizNum < 1) {
            System.out.println("1문제 이상 출제되어야 합니다.");
            return;
        } else if (quizNum > list.size()) {
            quizNum = list.size();
        }

        boolean[] usedIndex = new boolean[list.size()];
        int score = 0;
        for(int i = 0; i < quizNum; i++){
            int answerIndex;
            do {
                answerIndex = ran.nextInt(list.size());
            } while (usedIndex[answerIndex]);
            usedIndex[answerIndex] = true;

            String[] a = list.get(answerIndex).split("\t", 2);
            String aEng = a[0].trim();
            String aKor = a[1].trim();

            ArrayList<String> aKorList = new ArrayList<>();

            if (aKor.contains("/")) {
                String[] aKorArr = aKor.split("/");
                for (String kor : aKorArr) {
                    aKorList.add(kor.trim());
                }
            } else {
                aKorList.add(aKor);
            }

            int randquiz = ran.nextInt(2) + 1;
            if(randquiz==1){
                System.out.println("\n[" + (i + 1) + "/" + quizNum + "] " + aEng + "의 뜻은?");
                String answer = sc.nextLine().trim();

                if (aKorList.contains(answer) || aKor.equals(answer)) {
                    System.out.println("정답!");
                    score++;
                } else {
                    System.out.println("오답!");

                    String answerStr = "";
                    for (String kor : aKorList) {
                        answerStr += kor + "/ ";
                    }
                    answerStr = answerStr.substring(0,answerStr.length()-2);
                    System.out.println("정답은 " + aEng + " = " + answerStr);
                }

            } else{

                String questionStr = "";
                for (String kor : aKorList) {
                    questionStr += kor + "/ ";
                }
                questionStr = questionStr.substring(0,questionStr.length()-2);

                System.out.println("\n[" + (i + 1) + "/" + quizNum + "] " + questionStr + "의 뜻은?");
                String answer = sc.nextLine().trim();

                if (answer.equals(aEng)) {
                    System.out.println("정답!");
                    score++;
                } else {
                    System.out.println("오답!");
                    System.out.println("정답은 " + aEng + " = " + questionStr);
                }
            }

        }
        System.out.printf("\n총 %d문제 중 %d개 정답 (정답률 %.1f%%)\n", quizNum, score, 100.0 * score / quizNum);
    }

    private void multipleChoiceQuestion(ArrayList<String> list) {
        // TODO: 객관식 각 문제 구현
        if (list == null){
            System.out.println("단어가 등록되어 있지 않습니다.");
            return;
        } else if (list.size() < 4) {
            System.out.println("객관식 보기를 만들 단어(4개)가 부족합니다.");
            return;
        }
        Scanner sc= new Scanner(System.in, StandardCharsets.UTF_8);
        Random ran = new Random();

        System.out.print("문제 수를 입력해주세요 : ");
        int quizNum = sc.nextInt();
        sc.nextLine();

        if(quizNum < 1) {
            System.out.println("1문제 이상 출제되어야 합니다.");
            return;
        } else if (quizNum > list.size()) {
            quizNum = list.size();
        }

        boolean[] usedIndex = new boolean[list.size()];
        ArrayList<String> wrongs = new ArrayList<>();
        int score = 0;
        for(int i = 0; i < quizNum; i++){
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
            while(true){
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
                System.out.println("정답은 [" + correctNum + "번] " + aEng + " = " + aKor);
                wrongs.add(aEng + "\t" + aKor);
            }
        }
        System.out.printf("\n총 %d문제 중 %d개 정답 (정답률 %.1f%%)\n", quizNum, score, 100.0 * score / quizNum);
    }
}
