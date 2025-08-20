package org.x96.sys.foundation.cs.lexer.visitor.factory;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.x96.sys.foundation.buzz.Buzz;
import org.x96.sys.foundation.cs.lexer.tokenizer.Tokenizer;
import org.x96.sys.foundation.cs.lexer.visitor.Visitor;

class ReflectiveVisitorFactoryTest {

    @Test
    void happy2() throws NoSuchMethodException {
        class Dv1 extends Visitor {
            public Dv1(Tokenizer t) {
                super(t);
            }

            @Override
            public boolean allowed() {
                return false;
            }
        }
        var e = assertThrows(Buzz.class, () -> ReflectiveVisitorFactory.happens(Dv1.class, null));
        assertEquals(
                """
                🦕 [0x0]
                🐝 [?]
                🌵 > Não foi possível construir visitante\
                """,
                e.getMessage());
    }
}
