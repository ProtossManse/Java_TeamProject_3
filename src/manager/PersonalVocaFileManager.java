package manager;

public class PersonalVocaFileManager extends VocaFileManager{
    public PersonalVocaFileManager(String fileName) {
        super(fileName);
    }

    @Override
    public void menu() {
        int choice = 0;
        while (choice != 6) {
            System.out.println("1. 단어 추가");
            System.out.println("2. 단어 삭제");
            System.out.println("3. 단어 수정");
            System.out.println("4. 단어 검색");
            System.out.println("5. 단어 즐겨찾기");
            System.out.println("6. 뒤로가기");
            System.out.print(">> ");
            choice = super.scanner.nextInt();
            super.scanner.nextLine();
            switch (choice) {
                case 1 -> super.addVoca();
                case 2 -> super.removeVoca();
                case 3 -> super.editVoca();
                case 4 -> super.searchVoca();
                case 5 -> favoriteVoca();
                case 6 -> System.out.println("메인 메뉴로 돌아갑니다.");
            }
        }
    }

    private void favoriteVoca() {
        // TODO: 즐겨찾기 기능 구현
        // 즐겨찾기는 res/{username}/vocas/favorite.txt로 만드는 게 좋을 것 같습니다.
    }

}
