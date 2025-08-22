package org.x96.sys.foundation.cs.lexer.visitor;

import org.x96.sys.foundation.buzz.cs.lexer.visitor.BuzzVisitorMismatch;
import org.x96.sys.foundation.cs.lexer.token.Token;
import org.x96.sys.foundation.cs.lexer.tokenizer.Tokenizer;

// alpha_low = [0x61-0x7A];
public class AlphaLow extends Visitor {

    public AlphaLow(Tokenizer tokenizer) {
        super(tokenizer);
    }

    @Override
    public Token[] visit() {
        if (look() < 0x61 || look() > 0x7A)
            throw new BuzzVisitorMismatch(this, this.tokenizer);
        rec(overKind());
        return stream();
    }

    @Override
    public boolean allowed() {
        return look() >= 0x61 && look() <= 0x7A;
    }

    @Override
    public String overKind() {
        return "alpha_low";
    }
}
