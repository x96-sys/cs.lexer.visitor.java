package org.x96.sys.foundation.cs.lexer.visitor;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.x96.sys.foundation.buzz.cs.lexer.visitor.BuzzVisitorMismatch;
import org.x96.sys.foundation.cs.ast.book.passage.pattern.modifier.Shell;
import org.x96.sys.foundation.cs.lexer.token.Kind;
import org.x96.sys.foundation.cs.lexer.token.Token;
import org.x96.sys.foundation.cs.lexer.tokenizer.Tokenizer;
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

                    @Override
                    public Token[] visit() {
                        rec();
                        return stream();
                    }
                };
        assertTrue(visitorStx.allowed());
        Token[] tokens = visitorStx.safeVisit();
        assertEquals(1, tokens.length);
        assertEquals("Token { Kind[STX] Lexeme[0x2] Span[{0:0 0}:{1:1 1}] }", tokens[0].toString());
    }

    @Test
    void happySingleVisit() {
        Tokenizer tk = new Tokenizer(ByteStream.raw(new byte[] {0x1, 0x2}));
        tk.advance();
        Visitor visitorStx =
                new Visitor(tk) {
                    @Override
                    public boolean allowed() {
                        return Kind.isStx(look());
                    }

                    @Override
                    public Token[] visit() {
                        rec();
                        return stream();
                    }
                };
        assertTrue(visitorStx.allowed());
        Token[] tokens = visitorStx.safeVisit();
        assertEquals(1, tokens.length);
        assertEquals("Token { Kind[STX] Lexeme[0x2] Span[{1:1 1}:{1:2 2}] }", tokens[0].toString());
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
                        while (denied() && tokenizer.ready()) {
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
    }

    @Test
    void happyAlphaLow() {
        Tokenizer tokenizer = new Tokenizer(ByteStream.raw("cs".getBytes()));
        AlphaLow alphaLow = new AlphaLow(tokenizer);

        Token[] tokens = alphaLow.visit();

        assertEquals(1, tokens.length);
        // [0x63] [LATIN_SMALL_LETTER_C] [c]
        assertEquals(
                "Token { Kind[alpha_low] Lexeme[0x63] Span[{0:0 0}:{1:1 1}] }",
                tokens[0].toString());
        assertEquals("LATIN_SMALL_LETTER_C", tokens[0].kind().toString());
        assertEquals("alpha_low", tokens[0].overKind);
        assertEquals(0, tokens[0].span().start().line());
        assertEquals(0, tokens[0].span().start().column());
        assertEquals(0, tokens[0].span().start().offset());
        assertEquals(1, tokens[0].span().end().line());
        assertEquals(1, tokens[0].span().end().column());
        assertEquals(0, tokens[0].span().start().offset());
    }

    @Test
    void happyWordWithAlphaLow() {
        Tokenizer tokenizer = new Tokenizer(ByteStream.raw("'cecisofi'".getBytes()));
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
                        if (denied() && tokenizer.ready()) {
                            push(new AlphaLow(tokenizer).safeVisit());
                            fill();
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
        assertEquals(10, tokens.length);

        assertEquals(10, tokens.length);
        // [0x27] [APOSTROPHE] [']
        assertEquals(
                "Token { Kind[Word] Lexeme[0x27] Span[{0:0 0}:{1:1 1}] }", tokens[0].toString());
        assertEquals("APOSTROPHE", tokens[0].kind().toString());
        assertEquals("Word", tokens[0].overKind);
        assertEquals(0, tokens[0].span().start().line());
        assertEquals(0, tokens[0].span().start().column());
        assertEquals(0, tokens[0].span().start().offset());
        assertEquals(1, tokens[0].span().end().line());
        assertEquals(1, tokens[0].span().end().column());
        assertEquals(0, tokens[0].span().start().offset());

        // [0x63] [LATIN_SMALL_LETTER_C] [c]
        assertEquals(
                "Token { Kind[alpha_low] Lexeme[0x63] Span[{1:1 1}:{1:2 2}] }",
                tokens[1].toString());
        assertEquals("LATIN_SMALL_LETTER_C", tokens[1].kind().toString());
        assertEquals("alpha_low", tokens[1].overKind);
        assertEquals(1, tokens[1].span().start().line());
        assertEquals(1, tokens[1].span().start().column());
        assertEquals(1, tokens[1].span().start().offset());
        assertEquals(1, tokens[1].span().end().line());
        assertEquals(2, tokens[1].span().end().column());
        assertEquals(1, tokens[1].span().start().offset());

        // [0x65] [LATIN_SMALL_LETTER_E] [e]
        assertEquals(
                "Token { Kind[alpha_low] Lexeme[0x65] Span[{1:2 2}:{1:3 3}] }",
                tokens[2].toString());
        assertEquals("LATIN_SMALL_LETTER_E", tokens[2].kind().toString());
        assertEquals("alpha_low", tokens[2].overKind);
        assertEquals(1, tokens[2].span().start().line());
        assertEquals(2, tokens[2].span().start().column());
        assertEquals(2, tokens[2].span().start().offset());
        assertEquals(1, tokens[2].span().end().line());
        assertEquals(3, tokens[2].span().end().column());
        assertEquals(2, tokens[2].span().start().offset());

        // [0x63] [LATIN_SMALL_LETTER_C] [c]
        assertEquals(
                "Token { Kind[alpha_low] Lexeme[0x63] Span[{1:3 3}:{1:4 4}] }",
                tokens[3].toString());
        assertEquals("LATIN_SMALL_LETTER_C", tokens[3].kind().toString());
        assertEquals("alpha_low", tokens[3].overKind);
        assertEquals(1, tokens[3].span().start().line());
        assertEquals(3, tokens[3].span().start().column());
        assertEquals(3, tokens[3].span().start().offset());
        assertEquals(1, tokens[3].span().end().line());
        assertEquals(4, tokens[3].span().end().column());
        assertEquals(3, tokens[3].span().start().offset());

        // [0x69] [LATIN_SMALL_LETTER_I] [i]
        assertEquals(
                "Token { Kind[alpha_low] Lexeme[0x69] Span[{1:4 4}:{1:5 5}] }",
                tokens[4].toString());
        assertEquals("LATIN_SMALL_LETTER_I", tokens[4].kind().toString());
        assertEquals("alpha_low", tokens[4].overKind);
        assertEquals(1, tokens[4].span().start().line());
        assertEquals(4, tokens[4].span().start().column());
        assertEquals(4, tokens[4].span().start().offset());
        assertEquals(1, tokens[4].span().end().line());
        assertEquals(5, tokens[4].span().end().column());
        assertEquals(4, tokens[4].span().start().offset());

        // [0x73] [LATIN_SMALL_LETTER_S] [s]
        assertEquals(
                "Token { Kind[alpha_low] Lexeme[0x73] Span[{1:5 5}:{1:6 6}] }",
                tokens[5].toString());
        assertEquals("LATIN_SMALL_LETTER_S", tokens[5].kind().toString());
        assertEquals("alpha_low", tokens[5].overKind);
        assertEquals(1, tokens[5].span().start().line());
        assertEquals(5, tokens[5].span().start().column());
        assertEquals(5, tokens[5].span().start().offset());
        assertEquals(1, tokens[5].span().end().line());
        assertEquals(6, tokens[5].span().end().column());
        assertEquals(5, tokens[5].span().start().offset());

        // [0x6F] [LATIN_SMALL_LETTER_O] [o]
        assertEquals(
                "Token { Kind[alpha_low] Lexeme[0x6F] Span[{1:6 6}:{1:7 7}] }",
                tokens[6].toString());
        assertEquals("LATIN_SMALL_LETTER_O", tokens[6].kind().toString());
        assertEquals("alpha_low", tokens[6].overKind);
        assertEquals(1, tokens[6].span().start().line());
        assertEquals(6, tokens[6].span().start().column());
        assertEquals(6, tokens[6].span().start().offset());
        assertEquals(1, tokens[6].span().end().line());
        assertEquals(7, tokens[6].span().end().column());
        assertEquals(6, tokens[6].span().start().offset());

        // [0x66] [LATIN_SMALL_LETTER_F] [f]
        assertEquals(
                "Token { Kind[alpha_low] Lexeme[0x66] Span[{1:7 7}:{1:8 8}] }",
                tokens[7].toString());
        assertEquals("LATIN_SMALL_LETTER_F", tokens[7].kind().toString());
        assertEquals("alpha_low", tokens[7].overKind);
        assertEquals(1, tokens[7].span().start().line());
        assertEquals(7, tokens[7].span().start().column());
        assertEquals(7, tokens[7].span().start().offset());
        assertEquals(1, tokens[7].span().end().line());
        assertEquals(8, tokens[7].span().end().column());
        assertEquals(7, tokens[7].span().start().offset());

        // [0x69] [LATIN_SMALL_LETTER_I] [i]
        assertEquals(
                "Token { Kind[alpha_low] Lexeme[0x69] Span[{1:8 8}:{1:9 9}] }",
                tokens[8].toString());
        assertEquals("LATIN_SMALL_LETTER_I", tokens[8].kind().toString());
        assertEquals("alpha_low", tokens[8].overKind);
        assertEquals(1, tokens[8].span().start().line());
        assertEquals(8, tokens[8].span().start().column());
        assertEquals(8, tokens[8].span().start().offset());
        assertEquals(1, tokens[8].span().end().line());
        assertEquals(9, tokens[8].span().end().column());
        assertEquals(8, tokens[8].span().start().offset());

        // [0x27] [APOSTROPHE] [']
        assertEquals(
                "Token { Kind[Word] Lexeme[0x27] Span[{1:9 9}:{1:10 10}] }", tokens[9].toString());
        assertEquals("APOSTROPHE", tokens[9].kind().toString());
        assertEquals("Word", tokens[9].overKind);
        assertEquals(1, tokens[9].span().start().line());
        assertEquals(9, tokens[9].span().start().column());
        assertEquals(9, tokens[9].span().start().offset());
        assertEquals(1, tokens[9].span().end().line());
        assertEquals(10, tokens[9].span().end().column());
        assertEquals(9, tokens[9].span().start().offset());
    }

    @Test
    void happyWordAtomicWithAlphaLow() {
        Tokenizer tokenizer = new Tokenizer(ByteStream.raw("'cecisofi'".getBytes()));
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
                        if (denied() && tokenizer.ready()) {
                            push(new AlphaLow(tokenizer).safeVisit());
                            fill();
                        }
                    }

                    private void mark() {
                        if (allowed()) {
                            rec("gonabeover");
                        } else {
                            String explain =
                                    String.format(
                                            "eh esperado `'`; encontrado: [%s]", (char) look());
                            throw new RuntimeException(explain);
                        }
                    }

                    @Override
                    public String overkind() {
                        return "wrd";
                    }
                };
        visitorWord.setMod(new Shell("@".getBytes()[0]));
        assertEquals("- Word", visitorWord.visitorName());

        Token[] tokens = visitorWord.visit();

        assertEquals(10, tokens.length);
        // [0x27] [APOSTROPHE] [']
        assertEquals(
                "Token { Kind[wrd] Lexeme[0x27] Span[{0:0 0}:{1:1 1}] }", tokens[0].toString());
        assertEquals("APOSTROPHE", tokens[0].kind().toString());
        assertEquals("wrd", tokens[0].overKind);
        assertEquals(0, tokens[0].span().start().line());
        assertEquals(0, tokens[0].span().start().column());
        assertEquals(0, tokens[0].span().start().offset());
        assertEquals(1, tokens[0].span().end().line());
        assertEquals(1, tokens[0].span().end().column());
        assertEquals(0, tokens[0].span().start().offset());

        // [0x63] [LATIN_SMALL_LETTER_C] [c]
        assertEquals(
                "Token { Kind[wrd] Lexeme[0x63] Span[{1:1 1}:{1:2 2}] }", tokens[1].toString());
        assertEquals("LATIN_SMALL_LETTER_C", tokens[1].kind().toString());
        assertEquals("wrd", tokens[1].overKind);
        assertEquals(1, tokens[1].span().start().line());
        assertEquals(1, tokens[1].span().start().column());
        assertEquals(1, tokens[1].span().start().offset());
        assertEquals(1, tokens[1].span().end().line());
        assertEquals(2, tokens[1].span().end().column());
        assertEquals(1, tokens[1].span().start().offset());

        // [0x65] [LATIN_SMALL_LETTER_E] [e]
        assertEquals(
                "Token { Kind[wrd] Lexeme[0x65] Span[{1:2 2}:{1:3 3}] }", tokens[2].toString());
        assertEquals("LATIN_SMALL_LETTER_E", tokens[2].kind().toString());
        assertEquals("wrd", tokens[2].overKind);
        assertEquals(1, tokens[2].span().start().line());
        assertEquals(2, tokens[2].span().start().column());
        assertEquals(2, tokens[2].span().start().offset());
        assertEquals(1, tokens[2].span().end().line());
        assertEquals(3, tokens[2].span().end().column());
        assertEquals(2, tokens[2].span().start().offset());

        // [0x63] [LATIN_SMALL_LETTER_C] [c]
        assertEquals(
                "Token { Kind[wrd] Lexeme[0x63] Span[{1:3 3}:{1:4 4}] }", tokens[3].toString());
        assertEquals("LATIN_SMALL_LETTER_C", tokens[3].kind().toString());
        assertEquals("wrd", tokens[3].overKind);
        assertEquals(1, tokens[3].span().start().line());
        assertEquals(3, tokens[3].span().start().column());
        assertEquals(3, tokens[3].span().start().offset());
        assertEquals(1, tokens[3].span().end().line());
        assertEquals(4, tokens[3].span().end().column());
        assertEquals(3, tokens[3].span().start().offset());

        // [0x69] [LATIN_SMALL_LETTER_I] [i]
        assertEquals(
                "Token { Kind[wrd] Lexeme[0x69] Span[{1:4 4}:{1:5 5}] }", tokens[4].toString());
        assertEquals("LATIN_SMALL_LETTER_I", tokens[4].kind().toString());
        assertEquals("wrd", tokens[4].overKind);
        assertEquals(1, tokens[4].span().start().line());
        assertEquals(4, tokens[4].span().start().column());
        assertEquals(4, tokens[4].span().start().offset());
        assertEquals(1, tokens[4].span().end().line());
        assertEquals(5, tokens[4].span().end().column());
        assertEquals(4, tokens[4].span().start().offset());

        // [0x73] [LATIN_SMALL_LETTER_S] [s]
        assertEquals(
                "Token { Kind[wrd] Lexeme[0x73] Span[{1:5 5}:{1:6 6}] }", tokens[5].toString());
        assertEquals("LATIN_SMALL_LETTER_S", tokens[5].kind().toString());
        assertEquals("wrd", tokens[5].overKind);
        assertEquals(1, tokens[5].span().start().line());
        assertEquals(5, tokens[5].span().start().column());
        assertEquals(5, tokens[5].span().start().offset());
        assertEquals(1, tokens[5].span().end().line());
        assertEquals(6, tokens[5].span().end().column());
        assertEquals(5, tokens[5].span().start().offset());

        // [0x6F] [LATIN_SMALL_LETTER_O] [o]
        assertEquals(
                "Token { Kind[wrd] Lexeme[0x6F] Span[{1:6 6}:{1:7 7}] }", tokens[6].toString());
        assertEquals("LATIN_SMALL_LETTER_O", tokens[6].kind().toString());
        assertEquals("wrd", tokens[6].overKind);
        assertEquals(1, tokens[6].span().start().line());
        assertEquals(6, tokens[6].span().start().column());
        assertEquals(6, tokens[6].span().start().offset());
        assertEquals(1, tokens[6].span().end().line());
        assertEquals(7, tokens[6].span().end().column());
        assertEquals(6, tokens[6].span().start().offset());

        // [0x66] [LATIN_SMALL_LETTER_F] [f]
        assertEquals(
                "Token { Kind[wrd] Lexeme[0x66] Span[{1:7 7}:{1:8 8}] }", tokens[7].toString());
        assertEquals("LATIN_SMALL_LETTER_F", tokens[7].kind().toString());
        assertEquals("wrd", tokens[7].overKind);
        assertEquals(1, tokens[7].span().start().line());
        assertEquals(7, tokens[7].span().start().column());
        assertEquals(7, tokens[7].span().start().offset());
        assertEquals(1, tokens[7].span().end().line());
        assertEquals(8, tokens[7].span().end().column());
        assertEquals(7, tokens[7].span().start().offset());

        // [0x69] [LATIN_SMALL_LETTER_I] [i]
        assertEquals(
                "Token { Kind[wrd] Lexeme[0x69] Span[{1:8 8}:{1:9 9}] }", tokens[8].toString());
        assertEquals("LATIN_SMALL_LETTER_I", tokens[8].kind().toString());
        assertEquals("wrd", tokens[8].overKind);
        assertEquals(1, tokens[8].span().start().line());
        assertEquals(8, tokens[8].span().start().column());
        assertEquals(8, tokens[8].span().start().offset());
        assertEquals(1, tokens[8].span().end().line());
        assertEquals(9, tokens[8].span().end().column());
        assertEquals(8, tokens[8].span().start().offset());

        // [0x27] [APOSTROPHE] [']
        assertEquals(
                "Token { Kind[wrd] Lexeme[0x27] Span[{1:9 9}:{1:10 10}] }", tokens[9].toString());
        assertEquals("APOSTROPHE", tokens[9].kind().toString());
        assertEquals("wrd", tokens[9].overKind);
        assertEquals(1, tokens[9].span().start().line());
        assertEquals(9, tokens[9].span().start().column());
        assertEquals(9, tokens[9].span().start().offset());
        assertEquals(1, tokens[9].span().end().line());
        assertEquals(10, tokens[9].span().end().column());
        assertEquals(9, tokens[9].span().start().offset());
    }
}
