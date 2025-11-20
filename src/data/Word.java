package data;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class Word {
    String english;
    String korean;
    public ArrayList<String> koreanList = new ArrayList<>();

    public Word(String english, String korean) {
        this.english = english;
        this.korean = korean;
    }

    public String getEnglish() {
        return english;
    }

    public String getKorean() {
        return korean;
    }


    @Override
    public String toString() {
        return english + "\t" + korean;
    }

    @Override
    public boolean equals(Object o) {
        if (!(o instanceof Word word)) return false;
        return Objects.equals(english, word.english) && Objects.equals(korean, word.korean);
    }
}
