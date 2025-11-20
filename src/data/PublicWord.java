package data;

public class PublicWord extends Word {
    public int questions = 0;
    public int correct = 0;

    public PublicWord(String english, String korean) {
        super(english, korean);
    }

    public PublicWord(String english, String korean, int questions, int correct) {
        super(english, korean);
        this.questions = questions;
        this.correct = correct;
    }

    public double getCorrectionRate() {
        return (double) correct / questions * 100;
    }

    @Override
    public String toString() {
        return super.toString() + '\t' + questions + '\t' + correct;
    }
}
