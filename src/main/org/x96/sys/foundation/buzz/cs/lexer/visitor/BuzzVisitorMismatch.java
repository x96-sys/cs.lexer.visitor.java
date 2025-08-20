package org.x96.sys.foundation.buzz.cs.lexer.visitor;

import org.x96.sys.foundation.buzz.Buzz;
import org.x96.sys.foundation.cs.lexer.tokenizer.Tokenizer;

import org.x96.sys.foundation.cs.lexer.visitor.Visitor;

import java.util.LinkedList;
import java.util.List;
import org.x96.sys.foundation.cs.lexer.router.architecture.Analyzer;

public class BuzzVisitorMismatch extends Buzz {
  public static final int CODE = 0xFFF;

  public BuzzVisitorMismatch(Visitor v, Tokenizer t) {
    super(CODE, BuzzVisitorMismatch.class.getSimpleName(), explain(v, t));
  }

  private static String explain(Visitor v, Tokenizer t) {
    StringBuilder sb = new StringBuilder();
    sb.append(
        String.format(
            """
                Atual visitante [%s] encontrou token [0x%X] inesperado;
                   > Tokenizer.pointer[%s]
                   > Tokens Allowed [%s]
                %s\
                """,
            v.visitorName(),
            t.look(),
            t.pointer(),
            String.join(", ", discovery(v)),
            quadro(t)));
    return sb.toString();
  }

  public static String[] discovery(Visitor v) {
    List<String> l = new LinkedList<>();
    for (int i : Analyzer.discovery(v.getClass(), 0, 0x80)) {
      l.add(String.format("0x%X", i));
    }
    return l.toArray(String[]::new);
  }

  private static String quadro(Tokenizer t) {
    StringBuilder sb = new StringBuilder();
    int l = Math.max(1, t.position().line());
    int c = Math.max(1, t.position().column()); // posição lógica (1-based)

    String p = String.format("%s:%s", l, c);
    String i = " ".repeat(p.getBytes().length - String.valueOf(l).length());

    if (l > 1) {
      sb.append(String.format("%s%s | %s%n", i, l - 1, t.getLineByNumber(l - 1)));
    }

    String rawline = t.getLineByNumber(l);

    // Códigos ANSI
    final String RED = "\u001B[31m";
    final String RESET = "\u001B[0m";

    // Calcula índice real ignorando sentinelas (chars < 0x20 exceto \t e \n)
    int visualPos = 0;
    int insertIndex = -1;
    for (int idx = 0; idx < rawline.length(); idx++) {
      char ch = rawline.charAt(idx);
      if (ch >= 0x20 || ch == '\t') {
        visualPos++;
      }
      if (visualPos == c) {
        insertIndex = idx;
        break;
      }
    }

    if (insertIndex != -1 && insertIndex < rawline.length()) {
      rawline = rawline.substring(0, insertIndex)
          + RED
          + rawline.charAt(insertIndex)
          + RESET
          + rawline.substring(insertIndex + 1);
    }

    String currentLine = String.format("%s%s | %s%n", i, l, rawline);
    String nextLine = String.format("%s%s | %s%n", i, l + 1, t.getLineByNumber(l + 1));

    String explain = String.format("%s | %s^%n", p, " ".repeat(Math.max(0, c - 1)));

    sb.append(currentLine);
    sb.append(explain);
    sb.append(nextLine);

    return sb.toString();
  }
}
