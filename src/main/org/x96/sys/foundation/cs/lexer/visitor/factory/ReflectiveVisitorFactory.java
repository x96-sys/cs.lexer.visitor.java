package org.x96.sys.foundation.cs.lexer.visitor.factory;

import org.x96.sys.foundation.buzz.Buzz;
import org.x96.sys.foundation.cs.lexer.tokenizer.Tokenizer;
import org.x96.sys.foundation.cs.lexer.visitor.Visitor;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public final class ReflectiveVisitorFactory {
    private static Constructor<? extends Visitor> build(Class<? extends Visitor> cls) {
        try {
            return cls.getConstructor(Tokenizer.class);
        } catch (NoSuchMethodException e) {
            throw new Buzz("Não foi possível construir visitante");
        }
    }

    public static Visitor happens(Class<? extends Visitor> cls, Tokenizer tokenizer) {
        try {
            return build(cls).newInstance(tokenizer);
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            throw new Buzz("Não foi possível instanciar visitante");
        }
    }
}
