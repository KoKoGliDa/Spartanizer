package org.spartan.refactoring.wring;

import static org.spartan.refactoring.utils.Funcs.*;
import static org.spartan.refactoring.utils.Restructure.*;

import java.util.*;

import org.eclipse.jdt.core.dom.*;
import org.spartan.refactoring.utils.*;

/**
 * A {@link Wring} to convert <code>{;; g(); {}{;{;{;}};} }</code> into
 * <code>g();</code>
 *
 * @author Yossi Gil
 * @since 2015-07-29
 */
public class BlockSimplify extends Wring.Replacing<Block> {
  static Statement reorganizeNestedStatement(final Statement s) {
    final List<Statement> ss = Extract.statements(s);
    switch (ss.size()) {
      case 0:
        return s.getAST().newEmptyStatement();
      case 1:
        return duplicate(ss.get(0));
      default:
        return reorganizeStatement(s);
    }
  }
  private static boolean identical(final List<Statement> os1, final List<Statement> os2) {
    if (os1.size() != os2.size())
      return false;
    for (int i = 0; i < os1.size(); ++i)
      if (os1.get(i) != os2.get(i))
        return false;
    return true;
  }
  private static Block reorganizeStatement(final Statement s) {
    final List<Statement> ss = Extract.statements(s);
    final Block $ = s.getAST().newBlock();
    duplicateInto(ss, $.statements());
    return $;
  }
  @Override Statement replacement(final Block b) {
    final List<Statement> ss = Extract.statements(b);
    if (b == null || identical(ss, b.statements()))
      return null;
    if (!Is.statement(b.getParent()))
      return reorganizeStatement(b);
    switch (ss.size()) {
      case 0:
        return b.getAST().newEmptyStatement();
      case 1:
        return duplicate(Extract.singleStatement(b));
      default:
        return reorganizeNestedStatement(b);
    }
  }
  @Override String description(@SuppressWarnings("unused") final Block _) {
    return "Simplify block";
  }
}