package org.x96.sys.foundation.cs.lexer.visitor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.Test;
import org.x96.sys.foundation.buzz.cs.lexer.visitor.BuzzVisitorMismatch;
import org.x96.sys.foundation.cs.lexer.router.serial.Serial;
import org.x96.sys.foundation.cs.lexer.token.Kind;
import org.x96.sys.foundation.cs.lexer.token.Token;
import org.x96.sys.foundation.cs.lexer.tokenizer.Tokenizer;
import org.x96.sys.foundation.cs.lexer.visitor.entry.terminals.Terminal;
import org.x96.sys.foundation.cs.lexer.visitor.factory.ReflectiveVisitorFactory;
import org.x96.sys.foundation.io.ByteStream;

import java.util.LinkedList;
import java.util.List;

class VisitorTest {

    @Test
    void happyWrapBuzzVisitorMismatch() {
        Tokenizer t = new Tokenizer(ByteStream.wrapped(new byte[] {0x30}));
        t.advance();
        Visitor v = new GhostVisitor(t);
        var e = assertThrows(BuzzVisitorMismatch.class, v::safeVisit);

        String expected =
                String.format(
                        """
                        🦕 [0xFFF]
                        🐝 [BuzzVisitorMismatch]
                        🌵 > Atual visitante [GhostVisitor] encontrou token [0x30] inesperado;
                           > Tokenizer.pointer[1]
                           > Tokens Allowed [%s]
                          1 | \u00020\u0003
                        1:1 | ^
                          2 |\s
                        """,
                        discoveryAllowed(v.getClass()));
        String message = e.getMessage();

        Serial s = new Serial();
        s.oneOrMore(Terminal.class);
        for (Token token : s.stream(new Tokenizer(ByteStream.raw(expected.getBytes())))) {
            System.out.println(token);
        }

        System.out.println("=========================");
        s = new Serial();
        s.oneOrMore(Terminal.class);
        for (Token token :
                s.stream(
                        new Tokenizer(
                                ByteStream.raw(
                                        message.replaceAll("\u001B\\[[;\\d]*m", "").getBytes())))) {
            System.out.println(token);
        }
        assertEquals(expected, message.replaceAll("\u001B\\[[;\\d]*m", ""));
    }

    @Test
    void happyBuzzVisitorMismatch() {
        Tokenizer t = new Tokenizer(ByteStream.wrapped(new byte[] {0x30, 0x30}));
        t.advance();
        t.advance();

        Visitor v = new GhostVisitor(t);
        var e = assertThrows(BuzzVisitorMismatch.class, v::safeVisit);
        assertEquals(
                String.format(
                        """
                        🦕 [0xFFF]
                        🐝 [BuzzVisitorMismatch]
                        🌵 > Atual visitante [GhostVisitor] encontrou token [0x30] inesperado;
                           > Tokenizer.pointer[2]
                           > Tokens Allowed [%s]
                          1 | 00
                        1:2 |  ^
                          2 |\s
                        """,
                        discoveryAllowed(v.getClass())),
                e.getMessage()
                        .replace("\u0002", "") // remove byte 2
                        .replace("\u0003", "")
                        .replaceAll("\u001B\\[[;\\d]*m", ""));
    }

    private String discoveryAllowed(Class<? extends Visitor> v) {
        List<String> l = new LinkedList<>();
        for (int i = 0; i < 0x80; i++) {
            Tokenizer t = new Tokenizer(ByteStream.raw(new byte[] {(byte) i}));
            if (ReflectiveVisitorFactory.happens(v, t).allowed()) {
                l.add(String.format("0x%X", i));
            }
        }
        return String.join(", ", l);
    }

    @Test
    void happyBuzzVisitorMismatchMultiline() {
        byte[] payload =
                """
                ceci&sofi
                sofi0ceci
                ceci&sofi
                """
                        .getBytes();
        Tokenizer t = new Tokenizer(ByteStream.raw(payload));
        Visitor v = new GhostVisitor(t);
        while (v.allowed()) {
            v.visit();
        }
        var e = assertThrows(BuzzVisitorMismatch.class, v::safeVisit);
        assertEquals(
                String.format(
                        """
                        🦕 [0xFFF]
                        🐝 [BuzzVisitorMismatch]
                        🌵 > Atual visitante [GhostVisitor] encontrou token [0x30] inesperado;
                           > Tokenizer.pointer[14]
                           > Tokens Allowed [%s]
                          1 | ceci&sofi
                          2 | sofi0ceci
                        2:5 |     ^
                          3 | ceci&sofi
                        """,
                        discoveryAllowed(v.getClass())),
                e.getMessage().replaceAll("\u001B\\[[;\\d]*m", ""));
    }

    @Test
    void happyStx() {
        Visitor visitorStx =
                new Visitor(new Tokenizer(ByteStream.raw(new byte[] {0x2}))) {
                    @Override
                    public boolean allowed() {
                        return Kind.isStx(look());
                    }
                };
        assertTrue(visitorStx.allowed());
        Token[] tokens = visitorStx.safeVisit();
        assertEquals(1, tokens.length);
        assertEquals("Token { Kind[STX] Lexeme[0x2] Span[{0:0 0}:{1:1 1}] }", tokens[0].toString());
    }

    @Test
    void happyFindByte() {
        Tokenizer tk = new Tokenizer(ByteStream.raw(new byte[] {0x1, 0x2}));
        tk.advance();
        Visitor visitorStx =
                new Visitor(tk) {
                    @Override
                    public boolean allowed() {
                        return Kind.isStx(look());
                    }
                };
        assertTrue(visitorStx.allowed());
        Token[] tokens = visitorStx.safeVisit();
        assertEquals(1, tokens.length);
        assertEquals("Token { Kind[STX] Lexeme[0x2] Span[{1:1 1}:{1:2 2}] }", tokens[0].toString());
    }

    @Test
    void happyWordOverKindMark() {
        Visitor visitorWord =
                new Visitor(new Tokenizer(ByteStream.raw("'ceci&sofi'".getBytes()))) {
                    @Override
                    public boolean allowed() {
                        return Kind.isApostrophe(look());
                    }

                    @Override
                    public Token[] visit() {
                        mark();
                        fill();
                        mark();
                        return stream();
                    }

                    @Override
                    public String visitorName() {
                        return "- Word";
                    }

                    private void fill() {
                        while (!allowed()) {
                            rec();
                        }
                    }

                    private void mark() {
                        if (allowed()) {
                            rec("Word");
                        } else {
                            String explain =
                                    String.format(
                                            "eh esperado `'`; encontrado: [%s]", (char) look());
                            throw new RuntimeException(explain);
                        }
                    }
                };
        assertEquals("- Word", visitorWord.visitorName());

        Token[] tokens = visitorWord.visit();

        assertEquals(11, tokens.length);

        assertEquals(Kind.APOSTROPHE, tokens[0].kind());
        assertEquals(Kind.LATIN_SMALL_LETTER_C, tokens[1].kind());
        assertEquals(Kind.LATIN_SMALL_LETTER_E, tokens[2].kind());
        assertEquals(Kind.LATIN_SMALL_LETTER_C, tokens[3].kind());
        assertEquals(Kind.LATIN_SMALL_LETTER_I, tokens[4].kind());
        assertEquals(Kind.AMPERSAND, tokens[5].kind());
        assertEquals(Kind.LATIN_SMALL_LETTER_S, tokens[6].kind());
        assertEquals(Kind.LATIN_SMALL_LETTER_O, tokens[7].kind());
        assertEquals(Kind.LATIN_SMALL_LETTER_F, tokens[8].kind());
        assertEquals(Kind.LATIN_SMALL_LETTER_I, tokens[9].kind());
        assertEquals(Kind.APOSTROPHE, tokens[10].kind());

        assertEquals(
                "Token { Kind[Word] Lexeme[0x27] Span[{0:0 0}:{1:1 1}] }", tokens[0].toString());
        assertEquals(
                "Token { Kind[LATIN_SMALL_LETTER_C] Lexeme[0x63] Span[{1:1 1}:{1:2 2}] }",
                tokens[1].toString());
        assertEquals(
                "Token { Kind[LATIN_SMALL_LETTER_E] Lexeme[0x65] Span[{1:2 2}:{1:3 3}] }",
                tokens[2].toString());
        assertEquals(
                "Token { Kind[LATIN_SMALL_LETTER_C] Lexeme[0x63] Span[{1:3 3}:{1:4 4}] }",
                tokens[3].toString());
        assertEquals(
                "Token { Kind[LATIN_SMALL_LETTER_I] Lexeme[0x69] Span[{1:4 4}:{1:5 5}] }",
                tokens[4].toString());
        assertEquals(
                "Token { Kind[AMPERSAND] Lexeme[0x26] Span[{1:5 5}:{1:6 6}] }",
                tokens[5].toString());
        assertEquals(
                "Token { Kind[LATIN_SMALL_LETTER_S] Lexeme[0x73] Span[{1:6 6}:{1:7 7}] }",
                tokens[6].toString());
        assertEquals(
                "Token { Kind[LATIN_SMALL_LETTER_O] Lexeme[0x6F] Span[{1:7 7}:{1:8 8}] }",
                tokens[7].toString());
        assertEquals(
                "Token { Kind[LATIN_SMALL_LETTER_F] Lexeme[0x66] Span[{1:8 8}:{1:9 9}] }",
                tokens[8].toString());
        assertEquals(
                "Token { Kind[LATIN_SMALL_LETTER_I] Lexeme[0x69] Span[{1:9 9}:{1:10 10}] }",
                tokens[9].toString());
        assertEquals(
                "Token { Kind[Word] Lexeme[0x27] Span[{1:10 10}:{1:11 11}] }",
                tokens[10].toString());
    }

    @Test
    void happyOverKind() {
        Visitor visitorStx =
                new Visitor(new Tokenizer(ByteStream.raw(new byte[] {0x2}))) {
                    @Override
                    public boolean allowed() {
                        return Kind.isStx(look());
                    }

                    @Override
                    public String overkind() {
                        return "UNKNOWN";
                    }
                };
        assertTrue(visitorStx.allowed());
        Token[] tokens = visitorStx.safeVisit();
        assertEquals(1, tokens.length);
        assertEquals(
                "Token { Kind[UNKNOWN] Lexeme[0x2] Span[{0:0 0}:{1:1 1}] }", tokens[0].toString());
    }

    @Test
    void happyNameless() {
        Visitor visitorStx =
                new Visitor(new Tokenizer(ByteStream.raw(new byte[] {0x2}))) {
                    @Override
                    public boolean allowed() {
                        return false;
                    }
                };
        assertEquals("", visitorStx.visitorName(), "default visitor has no name");
    }

    @Test
    void happyName() {
        Visitor visitorStx =
                new Visitor(new Tokenizer(ByteStream.raw(new byte[] {0x2}))) {
                    @Override
                    public boolean allowed() {
                        return false;
                    }

                    @Override
                    public String visitorName() {
                        return "default";
                    }
                };
        assertEquals("default", visitorStx.visitorName());
    }

    @Test
    void happyWord() {
        Tokenizer tokenizer = new Tokenizer(ByteStream.raw("'ceci&sofi'".getBytes()));
        Visitor visitorWord =
                new Visitor(tokenizer) {
                    @Override
                    public boolean allowed() {
                        return Kind.isApostrophe(look());
                    }

                    @Override
                    public Token[] visit() {
                        mark();
                        fill();
                        mark();
                        return stream();
                    }

                    @Override
                    public String visitorName() {
                        return "- Word";
                    }

                    private void fill() {
                        while (!allowed() && tokenizer.ready()) {
                            rec();
                        }
                    }

                    private void mark() {
                        if (allowed()) {
                            rec("Word");
                        } else {
                            String explain =
                                    String.format(
                                            "eh esperado `'`; encontrado: [%s]", (char) look());
                            throw new RuntimeException(explain);
                        }
                    }
                };
        assertEquals("- Word", visitorWord.visitorName());

        Token[] tokens = visitorWord.visit();

        assertEquals(11, tokens.length);

        assertEquals(Kind.APOSTROPHE, tokens[0].kind());
        assertEquals(Kind.LATIN_SMALL_LETTER_C, tokens[1].kind());
        assertEquals(Kind.LATIN_SMALL_LETTER_E, tokens[2].kind());
        assertEquals(Kind.LATIN_SMALL_LETTER_C, tokens[3].kind());
        assertEquals(Kind.LATIN_SMALL_LETTER_I, tokens[4].kind());
        assertEquals(Kind.AMPERSAND, tokens[5].kind());
        assertEquals(Kind.LATIN_SMALL_LETTER_S, tokens[6].kind());
        assertEquals(Kind.LATIN_SMALL_LETTER_O, tokens[7].kind());
        assertEquals(Kind.LATIN_SMALL_LETTER_F, tokens[8].kind());
        assertEquals(Kind.LATIN_SMALL_LETTER_I, tokens[9].kind());
        assertEquals(Kind.APOSTROPHE, tokens[10].kind());

        assertEquals(
                "Token { Kind[Word] Lexeme[0x27] Span[{0:0 0}:{1:1 1}] }", tokens[0].toString());
        assertEquals(
                "Token { Kind[LATIN_SMALL_LETTER_C] Lexeme[0x63] Span[{1:1 1}:{1:2 2}] }",
                tokens[1].toString());
        assertEquals(
                "Token { Kind[LATIN_SMALL_LETTER_E] Lexeme[0x65] Span[{1:2 2}:{1:3 3}] }",
                tokens[2].toString());
        assertEquals(
                "Token { Kind[LATIN_SMALL_LETTER_C] Lexeme[0x63] Span[{1:3 3}:{1:4 4}] }",
                tokens[3].toString());
        assertEquals(
                "Token { Kind[LATIN_SMALL_LETTER_I] Lexeme[0x69] Span[{1:4 4}:{1:5 5}] }",
                tokens[4].toString());
        assertEquals(
                "Token { Kind[AMPERSAND] Lexeme[0x26] Span[{1:5 5}:{1:6 6}] }",
                tokens[5].toString());
        assertEquals(
                "Token { Kind[LATIN_SMALL_LETTER_S] Lexeme[0x73] Span[{1:6 6}:{1:7 7}] }",
                tokens[6].toString());
        assertEquals(
                "Token { Kind[LATIN_SMALL_LETTER_O] Lexeme[0x6F] Span[{1:7 7}:{1:8 8}] }",
                tokens[7].toString());
        assertEquals(
                "Token { Kind[LATIN_SMALL_LETTER_F] Lexeme[0x66] Span[{1:8 8}:{1:9 9}] }",
                tokens[8].toString());
        assertEquals(
                "Token { Kind[LATIN_SMALL_LETTER_I] Lexeme[0x69] Span[{1:9 9}:{1:10 10}] }",
                tokens[9].toString());
        assertEquals(
                "Token { Kind[Word] Lexeme[0x27] Span[{1:10 10}:{1:11 11}] }",
                tokens[10].toString());
    }
}
