package org.spartan.refactoring.wring;

import static org.spartan.refactoring.utils.Funcs.*;

import org.eclipse.jdt.core.dom.*;

/**
 * /** A {@link Wring} to convert <code>if (x)
 *   return b;
 * else {
 * }</code> into <code>if (x)
 *   return b;</code>
 *
 * @author Yossi Gil
 * @since 2015-08-01
 */
public final class IfEmptyElse extends Wring.Replacing<IfStatement> {
  @Override Statement replacement(final IfStatement s) {
    final IfStatement $ = duplicate(s);
    $.setElseStatement(null);
    return $;
  }
  @Override boolean scopeIncludes(final IfStatement s) {
    return s != null && Wrings.degenerateElse(s);
  }
  @Override String description(@SuppressWarnings("unused") final IfStatement _) {
    return "Remove vacuous 'else' branch";
  }
}