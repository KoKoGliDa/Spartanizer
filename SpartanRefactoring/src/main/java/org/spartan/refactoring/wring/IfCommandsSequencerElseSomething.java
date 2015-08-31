package org.spartan.refactoring.wring;

import static org.spartan.refactoring.utils.Funcs.*;
import static org.spartan.refactoring.utils.Restructure.*;

import java.util.*;

import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.rewrite.*;
import org.eclipse.text.edits.*;
import org.spartan.refactoring.utils.*;

/**
 * A {@link Wring} to convert <code>if (x) {
 *   ;
 *   f();
 *   return a;
 * } else {
 *   ;
 *   g();
 *   {
 *   }
 * }</code> into <code>if (x) {
 *   f();
 *   return a;
 * }
 * g();</code>
 *
 * @author Yossi Gil
 * @since 2015-07-29
 */
public final class IfCommandsSequencerElseSomething extends Wring<IfStatement> {
  static boolean endsWithSequencer(final Statement s) {
    return Is.sequencer(Extract.lastStatement(s));
  }
  @Override String description(@SuppressWarnings("unused") final IfStatement _) {
    return "Remove redundant else";
  }
  @Override boolean eligible(@SuppressWarnings("unused") final IfStatement _) {
    return true;
  }
  static void addAllReplacing(final List<Statement> to, final List<Statement> from, final Statement substitute, final Statement by1, final List<Statement> by2) {
    for (final Statement s : from)
      if (s != substitute)
        duplicateInto(s, to);
      else {
        duplicateInto(by1, to);
        duplicateInto(by2, to);
      }
  }
  @Override Rewrite make(final IfStatement s) {
    return new Rewrite(description(s), s) {
      @Override public void go(final ASTRewrite r, final TextEditGroup g) {
        assert scopeIncludes(s);
        final IfStatement shorterIf = endsWithSequencer(then(s)) ? duplicate(s) : Subject.pair(elze(s), then(s)).toNot(s.getExpression());
        final List<Statement> remainder = Extract.statements(elze(shorterIf));
        shorterIf.setElseStatement(null);
        final Block parent = asBlock(s.getParent());
        final Block newParent = s.getAST().newBlock();
        if (parent != null) {
          addAllReplacing(newParent.statements(), parent.statements(), s, shorterIf, remainder);
          r.replace(parent, newParent, g);
        } else {
          newParent.statements().add(shorterIf);
          duplicateInto(remainder, newParent.statements());
          r.replace(s, newParent, g);
        }
      }
    };
  }
  @Override boolean scopeIncludes(final IfStatement s) {
    return elze(s) != null && (endsWithSequencer(then(s)) || endsWithSequencer(elze(s)));
  }
}