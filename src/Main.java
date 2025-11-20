public class Main {
    public static void main(String[] args) {
        // 프로그램 실행 중 발생할 수 있는 치명적인 오류를 잡기 위한 예외 처리
        try {
            // 런처 인스턴스 생성 및 시작
            VocabularyAppLauncher vocLauncher = new VocabularyAppLauncher();
            vocLauncher.start();
        } catch (Exception e) {
            // 예상치 못한 에러 발생 시 메시지 출력
            System.out.println("프로그램 실행 중 알 수 없는 오류가 발생했습니다.");
            System.out.println("오류 내용 : " + e.getMessage());
            System.out.println("오류 종류 : " + e.toString());
            // 오류 내용과 종류를 출력해서 디버깅 용이하게!
        }
    }
}
