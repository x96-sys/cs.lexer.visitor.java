package org.x96.sys.foundation.cs.lexer.visitor;

import org.x96.sys.foundation.cs.lexer.tokenizer.Tokenizer;

public class GhostVisitor extends Visitor {
    public GhostVisitor(Tokenizer t) {
        super(t);
    }

    @Override
    public boolean allowed() {
        return look() != 0x30;
    }
}
