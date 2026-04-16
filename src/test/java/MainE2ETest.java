import net.sf.expectit.Expect;
import net.sf.expectit.ExpectBuilder;
import org.junit.jupiter.api.*;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.TimeUnit;

import static net.sf.expectit.matcher.Matchers.contains;

class MainE2ETest {

    private static final String PROJECT_ROOT = System.getProperty("user.dir");
    private static final String JAR_PATH     = PROJECT_ROOT + "/build/libs/untitled-1.0-SNAPSHOT.jar";

    private Process process;
    private Expect  expect;

    @BeforeAll
    static void buildJar() throws Exception {
        // IntelliJ / Gradle 어느 환경에서도 JAR을 먼저 빌드
        boolean isWindows = System.getProperty("os.name").toLowerCase().contains("win");
        String gradlew = PROJECT_ROOT + (isWindows ? "/gradlew.bat" : "/gradlew");
        ProcessBuilder pb = isWindows
                ? new ProcessBuilder("cmd", "/c", gradlew, "jar")
                : new ProcessBuilder(gradlew, "jar");

        int exit = pb.directory(new File(PROJECT_ROOT))
                     .inheritIO()
                     .start()
                     .waitFor();

        if (exit != 0) throw new RuntimeException("JAR 빌드 실패 (exit=" + exit + ")");
    }

    @BeforeEach
    void setUp() throws IOException {
        process = new ProcessBuilder(
                "java",
                "-Dfile.encoding=UTF-8",
                "-Dstdout.encoding=UTF-8",
                "-jar", JAR_PATH
        )
                .redirectErrorStream(true)
                .start();

        expect = new ExpectBuilder()
                .withOutput(process.getOutputStream())
                .withInputs(process.getInputStream())
                .withCharset(StandardCharsets.UTF_8)    // JAR UTF-8 출력과 인코딩 일치
                .withTimeout(5, TimeUnit.SECONDS)
                .withExceptionOnFailure()
                .build();
    }

    @AfterEach
    void tearDown() throws IOException {
        expect.close();
        process.destroyForcibly();
    }

    @Test
    @DisplayName("메뉴가 정상 출력된다")
    void menuIsDisplayed() throws IOException {
        expect.expect(contains("SAMPLE MANAGEMENT SYSTEM"));
        expect.expect(contains("1."));
        expect.expect(contains("4."));
    }

    @Test
    @DisplayName("1번 선택 시 메시지 출력 후 종료")
    void select1() throws IOException {
        expect.expect(contains("선택 >"));
        expect.sendLine("1");
        expect.expect(contains("1번을 선택하셨습니다."));
    }

    @Test
    @DisplayName("2번 선택 시 메시지 출력 후 종료")
    void select2() throws IOException {
        expect.expect(contains("선택 >"));
        expect.sendLine("2");
        expect.expect(contains("2번을 선택하셨습니다."));
    }

    @Test
    @DisplayName("3번 선택 시 메시지 출력 후 종료")
    void select3() throws IOException {
        expect.expect(contains("선택 >"));
        expect.sendLine("3");
        expect.expect(contains("3번을 선택하셨습니다."));
    }

    @Test
    @DisplayName("4번 선택 시 종료 메시지 출력")
    void select4() throws IOException {
        expect.expect(contains("선택 >"));
        expect.sendLine("4");
        expect.expect(contains("프로그램을 종료합니다."));
    }

    @Test
    @DisplayName("잘못된 입력 시 경고 후 재입력 요청")
    void invalidInput() throws IOException {
        expect.expect(contains("선택 >"));
        expect.sendLine("9");
        expect.expect(contains("1~4 사이의 숫자를 입력하세요."));
        expect.expect(contains("선택 >"));
        expect.sendLine("1");
        expect.expect(contains("1번을 선택하셨습니다."));
    }
}
