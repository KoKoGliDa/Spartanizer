package il.org.spartan.refactoring.engine;

import static il.org.spartan.azzert.*;

import java.io.*;
import java.util.*;
import java.util.Map.*;

import org.eclipse.jdt.core.dom.*;

import il.org.spartan.*;
import il.org.spartan.refactoring.ast.*;
import il.org.spartan.refactoring.java.*;
import il.org.spartan.refactoring.java.Environment.*;
import il.org.spartan.refactoring.utils.*;

public class EnvironmentTestEngine {
  enum AnnotationType {
    FLAT, NESTED, BEGINEND
  }

  static Set<Entry<String, Environment.Information>> generateSet() {
    return Collections.unmodifiableSet(new HashSet<>());
  }

  /** @param from - file path
   * @return CompilationUnit of the code written in the file specified. */
  static ASTNode getCompilationUnit(final String from) {
    final String ROOT = "./src/test/resources/";
    final File f = new File(ROOT + from);
    azzert.notNull(ROOT);
    azzert.notNull(from);
    azzert.notNull(f);
    azzert.aye(f.exists());
    final ASTNode $ = makeAST.COMPILATION_UNIT.from(f);
    azzert.notNull($);
    azzert.that($, instanceOf(CompilationUnit.class));
    return $;
  }

  private final ASTNode n;
  Set<Entry<String, Environment.Information>> testNestedENV;
  Set<Entry<String, Environment.Information>> testFlatENV;
  Set<Entry<String, Environment.Information>> testBeginEnd;

  public EnvironmentTestEngine(final ASTNode $) {
    n = $;
    testNestedENV = generateSet();
    testFlatENV = generateSet();
    testBeginEnd = generateSet();
  }

  public EnvironmentTestEngine(final String ¢) {
    n = getCompilationUnit(¢);
    testNestedENV = generateSet();
    testFlatENV = generateSet();
    testBeginEnd = generateSet();
  }

  private void addValueToBeginEnd(final List<MemberValuePair> ps) {
  }

  // TODO: Information should be instantiated with PrudentType
  private void addValueToFlat(final List<MemberValuePair> ps) {
    testFlatENV.add(new MapEntry<>(wizard.asString(ps.get(0).getValue()), new Information()));
  }

  private void addValueToNested(final List<MemberValuePair> ps) {
  }

  void addValueToSetsDispatch(final List<MemberValuePair> ps, final AnnotationType t) {
    switch (t) {
      case FLAT:
        addValueToFlat(ps);
        break;
      case NESTED:
        addValueToNested(ps);
        break;
      case BEGINEND:
        addValueToBeginEnd(ps);
        break;
      default:
        break;
    }
  }

  /** Compares output Set (testFlatENV) with provided set, that will be the
   * result of the flat version of defines.
   * @param $ */
  public void compareFlatInOrder(final Set<Entry<String, Information>> $) {
    // Go over both sets in serial manner, and make sure every two members are
    // equal.
    // Also, check size, to avoid the case Set A is contained in B.
    // azzert.fail Otherwise.
  }

  /** Compares flat output Set (flat) with provided Set, that will be the result
   * of the flat version of defines.
   * @param $ */
  public void compareFlatOutOfOrder(final Set<Entry<String, Information>> $) {
    // Check that each member of $ is contained in FlatENV, and that the size is
    // equal.
    // azzert.fail Otherwise.
  }

  /** Compares output Set (testNestedENV) with provided Set, that will be the
   * result of Nested version of Defines.
   * @param $ */
  public void compareNested(final Set<Entry<String, Information>> $) {
    // Go over both sets in serial manner, and make sure every two members are
    // equal.
    // Also, check size, to avoid the case Set A is contained in B.
    // azzert.fail Otherwise.
  }

  /** Compares output Set (testBeginEnd) with provided Set, that will be the
   * result of Nested version of uses.
   * @param $ */
  public void compareUses(final Set<Entry<String, Information>> $) {
    // Go over both sets in serial manner, and make sure every two members are
    // equal.
    // Also, check size, to avoid the case Set A is contained in B.
    // azzert.fail Otherwise.
  }

  /* define: outer annotation = OutOfOrderNestedENV, InOrderFlatENV, Begin, End.
   * define: inner annotation = Id. ASTVisitor that goes over the ASTNodes in
   * which annotations can be defined, and checks if the annotations are of the
   * kind that interests us. An array of inner annotations is defined inside of
   * each outer annotation of interest. I think it will be less error prone and
   * more scalable to implement another, internal, ASTVisitor that will go over
   * each inner annotation node, and send everything to an outside function to
   * add to the Sets as required. That means that each inner annotation will be
   * visited twice from the same outer annotation, but that should not cause
   * worry, since the outside visitor will do nothing.
   *
   * TODO: internal node parsing. Think about Nested parsing. */
  public void runTest() {
    n.accept(new ASTVisitor() {
      /** Iterate over annotations of the current declaration and dispatch them
       * to handlers. otherwise */
      void checkAnnotations(final List<Annotation> as) {
        for (final Annotation ¢ : as)
          dispatch(¢);
      }

      void beginEndHandler(final Annotation a) {
      }

      /** Call the corresponding handler according to the annotation sort. TODO:
       * only flat is implemented (partially)
       * @param a JD */
      void dispatch(final Annotation a) {
        if (a.getTypeName() + "" == "nestedENV")
          nestedHandler(a);
        else if (a.getTypeName() + "" == "flatENV")
          flatHandler(a);
        else if (a.getTypeName() + "" == "BegingEndENV")
          beginEndHandler(a);
      }

      /** TODO: May be redundant wrapper.
       * @param ¢ JD */
      void flatHandler(final Annotation ¢) {
        flatHandler(az.singleMemberAnnotation(¢));
      }

      boolean isNameId(final Name n1) {
        return "@Id".equals("" + n1);
      }

      /** Parse the outer annotation to get the inner ones. Add to the flat Set.
       * Compare uses() and declares() output to the flat Set.
       * @param $ JD */
      void flatHandler(final SingleMemberAnnotation $) {
        if ($ == null)
          return;
        $.accept(new ASTVisitor() {
          @Override public boolean visit(final NormalAnnotation ¢) {
            if (isNameId(¢.getTypeName()))
              addValueToSetsDispatch(values(¢), AnnotationType.FLAT);
            return true;
          }

          @SuppressWarnings("unchecked") List<MemberValuePair> values(final NormalAnnotation ¢) {
            return ¢.values();
          }
        });
      }

      void nestedHandler(final Annotation a) {
      }

      /** TODO: only MothodDeclaration is implemented. Should implement all
       * nodes which can have annotations. */
      @Override public boolean visit(final MethodDeclaration d) {
        checkAnnotations(extract.annotations(d));
        /** Set<Entry<String, Information>> useSet = Environment.uses(d);
         * Set<Entry<String, Information>> declareSet = Environment.declares(d);
         * TODO: Run the code above and compare to the corresponding testSets */
        testNestedENV.clear();
        testFlatENV.clear();
        testBeginEnd.clear();
        return true;
      }
    });
  }
}