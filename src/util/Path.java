package util;

public class Path {
    public static String getUsersFilePath() {
        return "res/users.txt";
    }

    public static String getUserDirPath(String username) {
        return "res/" + username + "/";
    }

    public static String getVocaDirPath(String username) {
        return "res/" + username + "/vocas/";
    }

    public static String getVocaFilePath(String username, String filename) {
        return "res/" + username + "/vocas/" + filename;
    }

    public static String getNoteDirPath(String username) {
        return "res/" + username + "/notes";
    }

    public static String getNoteFilePath(String username, String filename) {
        return "res/" + username + "/notes/" + filename;
    }

    public static String getFavoriteDirPath(String username) {
        return "res/" + username + "/favorites/";
    }

    public static String getFavoriteFilePath(String username) {
        return "res/" + username + "/favorites/_favorites.txt";
    }

    public static String getPublicDirPath() {
        return "res/public/vocas";
    }

    public static String getPublicFilePath() {
        return "res/public/vocas/publics.txt";
    }
}
