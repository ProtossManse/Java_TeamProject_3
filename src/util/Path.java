package util;

public class Path {
    public static String getUsersFilePath() {
        return "res/users.txt";
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

    public static String getFavoriteFilePath(String username) {
        return "res/" + username + "/vocas/_favorite.txt";
    }

}
