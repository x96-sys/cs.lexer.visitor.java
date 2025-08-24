package org.x96.sys.foundation.cs.lexer.visitor;

import org.x96.sys.foundation.buzz.cs.lexer.visitor.BuzzVisitorMismatch;
import org.x96.sys.foundation.cs.ast.book.passage.pattern.modifier.Modifier;
import org.x96.sys.foundation.cs.ast.book.passage.pattern.modifier.Shell;
import org.x96.sys.foundation.cs.lexer.token.Kind;
import org.x96.sys.foundation.cs.lexer.token.Token;
import org.x96.sys.foundation.cs.lexer.tokenizer.Tokenizer;

import java.util.Arrays;
import java.util.LinkedList;

public abstract class Visitor implements Visiting {
    public final Tokenizer tokenizer;
    public final LinkedList<Token> tokens;
    public Modifier mod;

    public Visitor(Tokenizer tokenizer) {
        this.tokenizer = tokenizer;
        this.tokens = new LinkedList<>();
    }

    @Override
    public Token[] visit() {
        rec();
        return stream();
    }

    public Token[] safeVisit() {
        if (denied()) {
            throw new BuzzVisitorMismatch(this, tokenizer);
        } else {
            return visit();
        }
    }

    /**
     * @deprecated Use {@link #overKind()} instead.
     */
    @Deprecated
    public String overkind() {
        throw new UnsupportedOperationException("deve ser implementado pelo visitante");
    }

    public String overKind() {
        throw new UnsupportedOperationException("deve ser implementado pelo visitante");
    }

    public byte look() {
        return tokenizer.look();
    }

    public Kind kind() {
        return tokenizer.kind();
    }

    public void rec() {
        tokens.add(tokenizer.tokenize());
    }

    public void rec(String k) {
        tokens.add(tokenizer.tokenize(k));
    }

    public void push(Token[] tokenArray) {
        tokens.addAll(Arrays.asList(tokenArray));
    }

    public Token[] stream() {
        if (mod == null)
            return tokens.toArray(Token[]::new);

        return tokens.stream()
                .peek(
                        t -> {
                            if (mod instanceof Shell) {
                                t.overKind(overKind());
                            } else {
                                throw new RuntimeException("sem suporte ainda");
                            }
                        })
                .toArray(Token[]::new);
    }

    public void setMod(Modifier mod) {
        this.mod = mod;
    }
}
