package org.x96.sys.foundation.cs.lexer.visitor;

import static org.junit.jupiter.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.x96.sys.foundation.cs.lexer.token.Token;

import java.lang.reflect.Method;

class VisitingTest {

    @Test
    void testInterfaceMethodsExist() throws Exception {
        Class<?> clazz =
                Class.forName(
                        "org.x96.sys.foundation.cs.lexer.visitor.Visiting");

        Method allowed = clazz.getMethod("allowed");
        assertEquals(boolean.class, allowed.getReturnType());

        Method visit = clazz.getMethod("visit");
        assertEquals(Token[].class, visit.getReturnType());

        Method name = clazz.getMethod("visitorName");
        assertEquals(String.class, name.getReturnType());
    }
}
