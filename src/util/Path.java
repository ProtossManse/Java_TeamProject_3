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

    public static String getFavoriteFilePath(String username) {
        return "res/" + username + "/vocas/_favorite.txt";
    }

    public static String getPublicVocaDirPath() {
        return "res/public/vocas/";   // 실제 폴더 구조에 맞게 수정
    }

    public static String getPublicVocaFilePath(String filename) {
        return "res/public/vocas/" + filename;
    }
}
