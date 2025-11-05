package manager;

import java.io.File;
import java.util.Scanner;

public abstract class VocaFileManager {
    File vocaFile;
    Scanner scanner = new Scanner(System.in);

    VocaFileManager(String fileName) {
        vocaFile = new File(fileName);
    }

    abstract public void menu();

    void addVoca() {
        // TODO: 단어 추가 로직 구현

    }
    void removeVoca() {
        // TODO: 단어 삭제 로직 구현
    }
    void editVoca() {
        // TODO: 단어 수정 로직 구현
    }
    void searchVoca() {
        // TODO: 단어 검색 로직 구현
    }

}
