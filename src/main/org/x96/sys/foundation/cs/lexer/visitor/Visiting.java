package org.x96.sys.foundation.cs.lexer.visitor;

import org.x96.sys.foundation.cs.lexer.token.Token;

/**
 * Interface base para visitantes de tokens. Cada visitante é independente e pode processar
 * estruturas completas. Segue padrão FIFO (First In, First Out) na cadeia de visitantes.
 */
public interface Visiting {

    /**
     * Verifica se este visitante pode processar o tipo de token atual.
     *
     * @return true se pode processar, false caso contrário
     */
    boolean allowed();

    default boolean denied() {
        return !allowed();
    }

    /**
     * Processa o lexer a partir da posição atual. O visitante tem controle total sobre o avanço do
     * lexer.
     *
     * @return array de tokens processados
     */
    Token[] visit();

    /**
     * Nome descritivo do visitante para debug.
     *
     * @return nome do visitante
     */
    default String visitorName() {
        return this.getClass().getSimpleName();
    }
}
