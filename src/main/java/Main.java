import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStyle;
import org.jline.utils.InfoCmp;

public class Main {

    private static final String[] MENU = {
            "1. 🧪 시료 등록",
            "2. 🔍 시료 조회",
            "3. 🗑️  시료 삭제",
            "4. 🚪 종료"
    };

    public static void main(String[] args) throws Exception {

        Terminal terminal = TerminalBuilder.builder()
                .system(true)
                .dumb(true)   // TTY 없는 환경(파이프, CI)에서도 예외 없이 동작
                .build();

        LineReader reader = LineReaderBuilder.builder()
                .terminal(terminal)
                .build();

        while (true) {
            drawMenu(terminal);

            String input = reader.readLine("선택 > ").trim();

            if (input.equals("4")) {
                terminal.puts(InfoCmp.Capability.clear_screen);
                terminal.flush();
                println(terminal, "🚪 프로그램을 종료합니다.", AttributedStyle.DEFAULT);
                break;
            } else if (input.equals("1") || input.equals("2") || input.equals("3")) {
                terminal.puts(InfoCmp.Capability.clear_screen);
                terminal.flush();
                println(terminal, "✅ " + input + "번을 선택하셨습니다.", AttributedStyle.DEFAULT.foreground(AttributedStyle.GREEN));
                break;
            } else {
                println(terminal, "⚠️  1~4 사이의 숫자를 입력하세요.", AttributedStyle.DEFAULT.foreground(AttributedStyle.RED));
            }
        }

        terminal.flush();
    }

    private static void drawMenu(Terminal terminal) {
        terminal.puts(InfoCmp.Capability.clear_screen);
        terminal.flush();
        println(terminal, "=====================================", AttributedStyle.DEFAULT);
        println(terminal, "  🔬  SAMPLE MANAGEMENT SYSTEM  🔬",  AttributedStyle.DEFAULT.foreground(AttributedStyle.YELLOW));
        println(terminal, "=====================================", AttributedStyle.DEFAULT);
        for (String item : MENU) {
            println(terminal, "  " + item, AttributedStyle.DEFAULT.foreground(AttributedStyle.CYAN));
        }
        println(terminal, "=====================================", AttributedStyle.DEFAULT);
        terminal.flush();
    }

    private static void println(Terminal terminal, String text, AttributedStyle style) {
        terminal.writer().println(
                new AttributedString(text, style).toAnsi(terminal)
        );
    }
}
