package il.org.spartan.refactoring.wring;

import static il.org.spartan.refactoring.utils.Funcs.*;
import static il.org.spartan.refactoring.wring.TernaryPushdown.*;

import org.eclipse.jdt.core.dom.*;

import il.org.spartan.refactoring.utils.*;

/** A {@link Wring} to convert
 *
 * <pre>
 * if (x)
 *   f(a);
 * else
 *   f(b);
 * </pre>
 *
 * into
 *
 * <pre>
 * f(x ? a : b);
 * </pre>
 *
 * @author Yossi Gil
 * @since 2015-07-29 */
public final class IfExpressionStatementElseSimilarExpressionStatement extends Wring.ReplaceCurrentNode<IfStatement> implements Kind.Canonicalization {
  @Override String description(@SuppressWarnings("unused") final IfStatement __) {
    return "Consolidate two branches of an 'if' into a single ";
  }
  @Override Statement replacement(final IfStatement s) {
    final Expression then = extract.expression(extract.expressionStatement(then(s)));
    if (then == null)
      return null;
    final Expression elze = extract.expression(extract.expressionStatement(elze(s)));
    if (elze == null)
      return null;
    final Expression e = pushdown(subject.pair(then, elze).toCondition(s.getExpression()));
    return e == null ? null : subject.operand(e).toStatement();
  }
}