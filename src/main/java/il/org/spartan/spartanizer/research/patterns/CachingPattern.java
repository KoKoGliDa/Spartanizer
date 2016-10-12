package il.org.spartan.spartanizer.research.patterns;

import org.eclipse.jdt.core.dom.*;
import il.org.spartan.spartanizer.engine.*;
import il.org.spartan.spartanizer.leonidas.*;
import il.org.spartan.spartanizer.tipping.*;

/** @author Ori Marcovitch
 * @year 2016 */
public final class CachingPattern extends NanoPatternTipper<Block> {
  private static final UserDefinedTipper<Block> tipper = TipperFactory.tipper("if($X1 == null) $X1 = $X2; return $X1;",
      "return $X1 = $X1 == null ? $X2 : $X1;", "Caching pattern");

  @Override public String description(@SuppressWarnings("unused") final Block __) {
    return tipper.description();
  }

  @Override public boolean prerequisite(final Block x) {
    return tipper.canTip(x);
  }

  @Override public Tip tip(final Block x) throws TipperFailure {
    return tipper.tip(x);
  }
}