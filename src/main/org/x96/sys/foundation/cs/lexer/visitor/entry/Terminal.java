package org.x96.sys.foundation.cs.lexer.visitor.entry;

import org.x96.sys.foundation.cs.lexer.tokenizer.Tokenizer;
import org.x96.sys.foundation.cs.lexer.visitor.Visitor;

public class Terminal extends Visitor {
    public Terminal(Tokenizer tokenizer) {
        super(tokenizer);
    }

    @Override
    public boolean allowed() {
        return true;
    }
}
