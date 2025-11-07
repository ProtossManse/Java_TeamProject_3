package data;

import java.time.LocalDate;

public class User {
    private final String name;
    private final String password;
    private int streak;
    private final LocalDate lastDate;

    public User(String name, String password, int streak, LocalDate lastDate) {
        this.name = name;
        this.password = password;
        this.lastDate = lastDate;
        this.streak = streak;
    }

    public String getPassword() {
        return password;
    }

    public String getName() {
        return name;
    }

    public int getStreak() {
        return streak;
    }

    public void setStreak(int streak) {
        this.streak = streak;
    }

    public LocalDate getLastDate() {
        return lastDate;
    }

    @Override
    public String toString() {
        return name + '\t' + password + '\t' + streak + '\t' + lastDate;
    }
}
