package org.x96.sys.foundation.buzz;

public class Buzz extends RuntimeException {

    public static final String ANSI_RESET = "\u001B[0m";
    public static final String ANSI_RED = "\u001B[31m";
    public static final String ANSI_GREEN = "\u001B[32m";

    // 🐞
    private static final String[] BUGS = {
        "🐞", "🕷️", "🪲", "🐜", "🦟", "🐝", "🦋", "🦖", "🦕", "🌵"
    };

    public Buzz(String message, Buzz cause) {
        super(message, cause);
    }

    /*─────────────────────────────────────────────────────────────*/
    /* Construtores                                                */
    /*─────────────────────────────────────────────────────────────*/

    /** Construtor principal com cause opcional. */
    protected Buzz(int code, String bee, String msg, Throwable cause) {
        super(format(code, bee, msg), cause);
    }

    /** Construtor sem cause. */
    public Buzz(int code, String bee, String msg) {
        this(code, bee, msg, null);
    }

    /** Construtor apenas com mensagem (código 0). */
    public Buzz(String msg) {
        this(0, "?", msg, null);
    }

    public static String format(int code, String bee, String msg) {

        return String.format(
                "%s [0x%X]%n%s [%s]%n%s > %s", BUGS[8], code, BUGS[5], bee, BUGS[9], msg);
    }

    public static String hex(int n) {
        return String.format("0x%X", n);
    }
}
