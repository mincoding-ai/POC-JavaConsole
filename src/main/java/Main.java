import org.jline.keymap.BindingReader;
import org.jline.keymap.KeyMap;
import org.jline.reader.LineReader;
import org.jline.reader.LineReaderBuilder;
import org.jline.terminal.Attributes;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.utils.AttributedString;
import org.jline.utils.AttributedStyle;
import org.jline.utils.InfoCmp;

public class Main {

    private static final String[] MENU = {
            "1. 시료 등록",
            "2. 시료 조회",
            "3. 시료 삭제",
            "4. 종료"
    };

    private static final String[] FIELDS = {
            "시료명",
            "시료 ID",
            "채취일",
            "담당자"
    };

    private static final String KEY_UP    = "UP";
    private static final String KEY_DOWN  = "DOWN";
    private static final String KEY_ENTER = "ENTER";

    // 표 컬럼 너비
    private static final int COL1 = 12;
    private static final int COL2 = 22;

    public static void main(String[] args) throws Exception {

        Terminal terminal = TerminalBuilder.builder()
                .system(true)
                .jna(true)
                .jansi(true)
                .build();

        Attributes saved = terminal.enterRawMode();

        try {
            KeyMap<String> keyMap = buildKeyMap(terminal);
            BindingReader bindingReader = new BindingReader(terminal.reader());

            int selected = 0;

            while (true) {
                drawMenu(terminal, selected);

                String action = bindingReader.readBinding(keyMap);

                if (KEY_UP.equals(action)) {
                    selected = (selected - 1 + MENU.length) % MENU.length;
                } else if (KEY_DOWN.equals(action)) {
                    selected = (selected + 1) % MENU.length;
                } else if (KEY_ENTER.equals(action)) {
                    if (selected == 0) {
                        // Raw mode 잠시 해제 후 등록 화면 진입
                        terminal.setAttributes(saved);
                        registerSample(terminal);
                        saved = terminal.enterRawMode();
                        keyMap = buildKeyMap(terminal);
                        bindingReader = new BindingReader(terminal.reader());
                    } else if (selected == MENU.length - 1) {
                        break;
                    } else {
                        handleMenu(terminal, selected, bindingReader, keyMap);
                    }
                }
            }

            terminal.puts(InfoCmp.Capability.clear_screen);
            terminal.writer().println("프로그램을 종료합니다.");
            terminal.flush();

        } finally {
            terminal.setAttributes(saved);
        }
    }

    // ──────────────────────────────────────────────
    //  시료 등록 화면
    // ──────────────────────────────────────────────
    private static void registerSample(Terminal terminal) {
        String[] values = new String[FIELDS.length];
        LineReader lineReader = LineReaderBuilder.builder().terminal(terminal).build();

        for (int i = 0; i < FIELDS.length; i++) {
            drawRegisterScreen(terminal, values, i);
            String input = lineReader.readLine("  입력 > ");
            values[i] = input.isBlank() ? "(없음)" : input.trim();
        }

        // 완료 화면
        drawRegisterScreen(terminal, values, FIELDS.length);
        terminal.writer().println();
        printColored(terminal, "  ✔ 등록 완료! 메인 메뉴로 돌아가려면 Enter...", AttributedStyle.GREEN);
        terminal.writer().println();
        terminal.flush();
        lineReader.readLine("");
    }

    private static void drawRegisterScreen(Terminal terminal, String[] values, int currentStep) {
        terminal.puts(InfoCmp.Capability.clear_screen);

        terminal.writer().println("=====================================");
        terminal.writer().println("          시료 등록");
        terminal.writer().println("=====================================");
        terminal.writer().println();

        // 진행률 바
        drawProgressBar(terminal, currentStep, FIELDS.length);
        terminal.writer().println();

        // 구분선 + 표 헤더
        String border  = "  ┌" + "─".repeat(COL1) + "┬" + "─".repeat(COL2) + "┐";
        String header  = "  │" + center("필드", COL1) + "│" + center("값", COL2) + "│";
        String divider = "  ├" + "─".repeat(COL1) + "┼" + "─".repeat(COL2) + "┤";
        String footer  = "  └" + "─".repeat(COL1) + "┴" + "─".repeat(COL2) + "┘";

        terminal.writer().println(border);
        terminal.writer().println(header);
        terminal.writer().println(divider);

        for (int i = 0; i < FIELDS.length; i++) {
            String fieldCell = pad(FIELDS[i], COL1);
            String valueText = (values[i] != null) ? values[i] : (i == currentStep ? "▌" : "");
            String valueCell = pad(valueText, COL2);

            String row = "  │" + fieldCell + "│" + valueCell + "│";

            if (i == currentStep && currentStep < FIELDS.length) {
                printColored(terminal, row, AttributedStyle.CYAN);
            } else if (values[i] != null) {
                printColored(terminal, row, AttributedStyle.WHITE);
            } else {
                terminal.writer().println(row);
            }
        }

        terminal.writer().println(footer);
        terminal.writer().println();
        terminal.flush();
    }

    private static void drawProgressBar(Terminal terminal, int step, int total) {
        int barWidth = 30;
        int filled   = (total == 0) ? barWidth : (int) Math.round((double) step / total * barWidth);
        int percent  = (total == 0) ? 100 : (int) Math.round((double) step / total * 100);

        String bar = "█".repeat(filled) + "░".repeat(barWidth - filled);

        String line = "  진행률  [" + bar + "] " + percent + "%";

        int color = percent < 50 ? AttributedStyle.YELLOW
                  : percent < 100 ? AttributedStyle.CYAN
                  : AttributedStyle.GREEN;
        printColored(terminal, line, color);
        terminal.writer().println();
    }

    // ──────────────────────────────────────────────
    //  유틸
    // ──────────────────────────────────────────────
    private static void printColored(Terminal terminal, String text, int color) {
        AttributedString str = new AttributedString(
                text,
                AttributedStyle.DEFAULT.foreground(color)
        );
        terminal.writer().println(str.toAnsi());
    }

    /** 문자열을 width 에 맞춰 오른쪽 패딩 (한글 2바이트 고려) */
    private static String pad(String s, int width) {
        int visual = visualLength(s);
        int spaces = Math.max(0, width - visual);
        return " " + s + " ".repeat(spaces - 1);
    }

    /** 문자열을 width 안에서 가운데 정렬 */
    private static String center(String s, int width) {
        int visual  = visualLength(s);
        int total   = Math.max(0, width - visual);
        int left    = total / 2;
        int right   = total - left;
        return " ".repeat(left) + s + " ".repeat(right);
    }

    /** 한글은 2칸, 나머지는 1칸으로 계산 */
    private static int visualLength(String s) {
        int len = 0;
        for (char c : s.toCharArray()) {
            len += (c >= 0xAC00 && c <= 0xD7A3) || (c >= 0x1100 && c <= 0x11FF)
                    || (c >= 0x3130 && c <= 0x318F) ? 2 : 1;
        }
        return len;
    }

    // ──────────────────────────────────────────────
    //  공통 메뉴 렌더링
    // ──────────────────────────────────────────────
    private static KeyMap<String> buildKeyMap(Terminal terminal) {
        KeyMap<String> keyMap = new KeyMap<>();

        keyMap.bind(KEY_UP,    "\033[A", "\033OA");
        keyMap.bind(KEY_DOWN,  "\033[B", "\033OB");
        keyMap.bind(KEY_ENTER, "\r", "\n");

        String up   = terminal.getStringCapability(InfoCmp.Capability.key_up);
        String down = terminal.getStringCapability(InfoCmp.Capability.key_down);
        if (up   != null && !up.isEmpty())   keyMap.bind(KEY_UP,   up);
        if (down != null && !down.isEmpty()) keyMap.bind(KEY_DOWN, down);

        return keyMap;
    }

    private static void drawMenu(Terminal terminal, int selected) {
        terminal.puts(InfoCmp.Capability.clear_screen);

        terminal.writer().println("=====================================");
        terminal.writer().println("     SAMPLE MANAGEMENT SYSTEM");
        terminal.writer().println("=====================================\n");

        for (int i = 0; i < MENU.length; i++) {
            if (i == selected) {
                printColored(terminal, "> " + MENU[i], AttributedStyle.YELLOW);
            } else {
                terminal.writer().println("  " + MENU[i]);
            }
        }

        terminal.writer().println("\n(↑↓: 이동, Enter: 선택)");
        terminal.flush();
    }

    private static void handleMenu(Terminal terminal, int selected,
                                   BindingReader bindingReader, KeyMap<String> keyMap) {
        terminal.puts(InfoCmp.Capability.clear_screen);
        terminal.writer().println("\n선택됨: " + MENU[selected]);
        terminal.writer().println("\n계속하려면 Enter...");
        terminal.flush();

        String action;
        do {
            action = bindingReader.readBinding(keyMap);
        } while (!KEY_ENTER.equals(action));
    }
}