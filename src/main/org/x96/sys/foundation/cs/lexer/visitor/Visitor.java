package org.x96.sys.foundation.cs.lexer.visitor;

import org.x96.sys.foundation.buzz.cs.lexer.visitor.BuzzVisitorMismatch;
import org.x96.sys.foundation.cs.lexer.token.Kind;
import org.x96.sys.foundation.cs.lexer.token.Token;
import org.x96.sys.foundation.cs.lexer.tokenizer.Tokenizer;

import java.util.Arrays;
import java.util.LinkedList;

public abstract class Visitor implements Visiting {
    public final Tokenizer tokenizer;
    public final LinkedList<Token> tokens;

    public Visitor(Tokenizer tokenizer) {
        this.tokenizer = tokenizer;
        this.tokens = new LinkedList<>();
    }

    @Override
    public Token[] visit() {
        rec(overkind());
        return stream();
    }

    public Token[] safeVisit() {
        if (!allowed()) {
            throw new BuzzVisitorMismatch(this, tokenizer);
        } else {
            return visit();
        }
    }

    public String overkind() {
        return kind().toString();
    }

    public byte look() {
        return tokenizer.look();
    }

    public Kind kind() {
        return tokenizer.kind();
    }

    public void rec() {
        rec(overkind());
    }

    public void rec(String overKind) {
        tokens.add(tokenizer.tokenize(overKind));
    }

    public void push(Token[] tokenArray) {
        tokens.addAll(Arrays.asList(tokenArray));
    }

    public Token[] stream() {
        return tokens.toArray(Token[]::new);
    }
}
