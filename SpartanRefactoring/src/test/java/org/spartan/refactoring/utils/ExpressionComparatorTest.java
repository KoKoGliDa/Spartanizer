package org.spartan.refactoring.utils;

import static org.spartan.hamcrest.CoreMatchers.*;
import static org.spartan.hamcrest.MatcherAssert.*;
import static org.spartan.hamcrest.OrderingComparison.*;
import static org.spartan.refactoring.utils.Into.*;

import org.eclipse.jdt.core.dom.*;
import org.junit.*;
import org.junit.runners.*;

/**
 * Test class for {@link ExpressionComparator}
 *
 * @author Yossi Gil
 * @since 2015-07-17
 */
@FixMethodOrder(MethodSorters.NAME_ASCENDING) //
@SuppressWarnings({ "javadoc", "static-method" }) //
public class ExpressionComparatorTest {
  @Test public void alphabeticalCompare() {
    final Expression e1 = e("1+2");
    final Expression e2 = e("6+7");
    assertThat(ExpressionComparator.alphabeticalCompare(e1, e2), lessThan(0));
  }
  @Test public void characterCompare() {
    final Expression e1 = e("1+2");
    final Expression e2 = e("6+7");
    assertThat(ExpressionComparator.characterCompare(e1, e2), is(0));
  }
  @Test public void literalAndProductAddition() {
    final Expression e1 = e("1");
    final Expression e2 = e("2*3");
    assertThat(ExpressionComparator.ADDITION.compare(e1, e2), greaterThan(0));
  }
  @Test public void literalAndClassConstant() {
    assertThat(ExpressionComparator.ADDITION.compare(e("1"), e("BOB")), greaterThan(0));
  }
  @Test public void twoClassConstants() {
    assertThat(ExpressionComparator.ADDITION.compare(e("SPONGE"), e("BOB")), greaterThan(0));
  }
  @Test public void literalAndProductMULITIPLICATION() {
    final Expression e1 = e("1");
    final Expression e2 = e("2*3");
    assertThat(ExpressionComparator.MULTIPLICATION.compare(e1, e2), lessThan(0));
  }
  @Test public void literalCompare() {
    final Expression e1 = e("1+2");
    final Expression e2 = e("6+7");
    assertThat(ExpressionComparator.literalCompare(e1, e2), is(0));
  }
  @Test public void longLiteralShortLiteralAddition() {
    final Expression e1 = e("1");
    final Expression e2 = e("12");
    assertThat(ExpressionComparator.ADDITION.compare(e1, e2), lessThan(0));
  }
  @Test public void longLiteralShortLiteralMultiplication() {
    final Expression e1 = e("1");
    final Expression e2 = e("12");
    assertThat(ExpressionComparator.MULTIPLICATION.compare(e1, e2), lessThan(0));
  }
  @Test public void nodesCompare() {
    final Expression e1 = e("1+2");
    final Expression e2 = e("6+7");
    assertThat(ExpressionComparator.nodesCompare(e1, e2), is(0));
  }
  @Test public void twoExpression() {
    final Expression e1 = e("1+2");
    final Expression e2 = e("6+7");
    assertThat(ExpressionComparator.ADDITION.compare(e1, e2), lessThan(0));
  }
  @Test public void twoFunctionAddition() {
    assertThat(ExpressionComparator.ADDITION.compare(e("f(a,b,c)"), e("f(a,b,c)")), is(0));
  }
  @Test public void twoFunctionMultiplication() {
    assertThat(ExpressionComparator.MULTIPLICATION.compare(e("f(a,b,c)"), e("f(a,b,c)")), is(0));
  }
}
