package org.spartan.refactoring.spartanizations;

import static org.spartan.refactoring.spartanizations.DialogBoxes.*;

import java.util.*;
import java.util.List;

import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.jdt.core.*;
import org.eclipse.jdt.core.dom.*;
import org.eclipse.jdt.core.dom.rewrite.*;
import org.eclipse.jface.text.*;
import org.eclipse.ltk.core.refactoring.*;
import org.eclipse.ltk.ui.refactoring.*;
import org.eclipse.swt.widgets.*;
import org.eclipse.ui.*;
import org.spartan.refactoring.handlers.*;
import org.spartan.refactoring.utils.*;

/**
 * the base class for all Spartanization Refactoring classes, contains common
 * functionality
 *
 * @author Artium Nihamkin (original)
 * @author Boris van Sosin <boris.van.sosin [at] gmail.com>} (v2)
 * @author Yossi Gil <code><yossi.gil [at] gmail.com></code>: major refactoring
 *         2013/07/10
 * @since 2013/01/01
 */
public abstract class Spartanization extends Refactoring {
  /**
   * @param u A compilation unit for reference - you give me an arbitrary
   *          compilation unit from the project and I'll find the root of the
   *          project and do my magic.
   * @param pm A standard ProgressMonitor - if you don't care about operation
   *          times put a "new NullProgressMonitor()"
   * @return List of all compilation units in the current project
   * @throws JavaModelException don't forget to catch
   */
  public static final List<ICompilationUnit> getAllProjectCompilationUnits(final ICompilationUnit u, final IProgressMonitor pm) throws JavaModelException {
    pm.beginTask("Gathering project information...", 1);
    final List<ICompilationUnit> $ = new ArrayList<>();
    if (u == null) {
      announce("Cannot find current compilation unit " + u);
      return $;
    }
    final IJavaProject javaProject = u.getJavaProject();
    if (javaProject == null) {
      announce("Cannot find project of " + u);
      return $;
    }
    final IPackageFragmentRoot[] packageFragmentRoots = javaProject.getPackageFragmentRoots();
    if (packageFragmentRoots == null) {
      announce("Cannot find roots of " + javaProject);
      return $;
    }
    for (final IPackageFragmentRoot r : packageFragmentRoots)
      if (r.getKind() == IPackageFragmentRoot.K_SOURCE)
        for (final IJavaElement e : r.getChildren())
          if (e.getElementType() == IJavaElement.PACKAGE_FRAGMENT)
            $.addAll(Arrays.asList(((IPackageFragment) e).getCompilationUnits()));
    pm.done();
    return $;
  }
  protected static boolean isNodeOutsideMarker(final ASTNode n, final IMarker m) {
    try {
      return n.getStartPosition() < ((Integer) m.getAttribute(IMarker.CHAR_START)).intValue()
          || n.getLength() + n.getStartPosition() > ((Integer) m.getAttribute(IMarker.CHAR_END)).intValue();
    } catch (final CoreException e) {
      return true;
    }
  }
  private ITextSelection selection = null;
  private ICompilationUnit compilationUnit = null;
  private IMarker marker = null;
  final Collection<TextFileChange> changes = new ArrayList<>();
  private final String name;
  /***
   * Instantiates this class, with message identical to name
   *
   * @param name a short name of this instance
   */
  protected Spartanization(final String name) {
    this.name = name;
  }
  @Override public RefactoringStatus checkFinalConditions(final IProgressMonitor pm) throws CoreException, OperationCanceledException {
    changes.clear();
    if (marker == null)
      runAsManualCall(pm);
    else {
      innerRunAsMarkerFix(pm, marker, true);
      marker = null; // consume marker
    }
    pm.done();
    return new RefactoringStatus();
  }
  @Override public RefactoringStatus checkInitialConditions(@SuppressWarnings("unused") final IProgressMonitor pm) {
    final RefactoringStatus $ = new RefactoringStatus();
    if (compilationUnit == null && marker == null)
      $.merge(RefactoringStatus.createFatalErrorStatus("Nothing to refactor."));
    return $;
  }
  /**
   * Count the number of suggestions offered by this instance.
   * <p>
   * This is an slow operation. Do not call light-headedly.
   *
   * @return the total number of suggestions offered by this instance
   */
  public int countSuggestions() {
    setMarker(null);
    try {
      checkFinalConditions(new NullProgressMonitor());
    } catch (final OperationCanceledException e) {
      e.printStackTrace();
    } catch (final CoreException e) {
      e.printStackTrace();
    }
    return changes.size();
  }
  @Override public final Change createChange(@SuppressWarnings("unused") final IProgressMonitor pm) throws OperationCanceledException {
    return new CompositeChange(getName(), changes.toArray(new Change[changes.size()]));
  }
  /**
   * creates an ASTRewrite which contains the changes
   *
   * @param u the Compilation Unit (outermost ASTNode in the Java Grammar)
   * @param pm a progress monitor in which the progress of the refactoring is
   *          displayed
   * @return an ASTRewrite which contains the changes
   */
  public final ASTRewrite createRewrite(final CompilationUnit u, final SubProgressMonitor pm) {
    return createRewrite(pm, u, (IMarker) null);
  }
  /**
   * Checks a Compilation Unit (outermost ASTNode in the Java Grammar) for
   * spartanization suggestions
   *
   * @param u what to check
   * @return a collection of {@link Rewrite} objects each containing a
   *         spartanization opportunity
   */
  public final List<Rewrite> findOpportunities(final CompilationUnit u) {
    final List<Rewrite> $ = new ArrayList<>();
    u.accept(collect($));
    return $;
  }
  /**
   * @return the compilationUnit
   */
  public ICompilationUnit getCompilationUnit() {
    return compilationUnit;
  }
  /**
   * @return a quick fix for this instance
   */
  public IMarkerResolution getFix() {
    return getFix(getName());
  }
  /**
   * @param s Spartanization's name
   * @return a quickfix which automatically performs the spartanization
   */
  public IMarkerResolution getFix(final String s) {
    /**
     * a quickfix which automatically performs the spartanization
     *
     * @author Boris van Sosin <code><boris.van.sosin [at] gmail.com></code>
     * @since 2013/07/01
     */
    return new IMarkerResolution() {
      @Override public String getLabel() {
        return "Do it! " + s;
      }
      @Override public void run(final IMarker m) {
        try {
          runAsMarkerFix(new NullProgressMonitor(), m);
        } catch (final CoreException e) {
          throw new RuntimeException(e);
        }
      }
    };
  }
  /**
   * @return a quick fix with a preview for this instance.
   */
  public IMarkerResolution getFixWithPreview() {
    return getFixWithPreview(getName());
  }
  /**
   * @param s Text for the preview dialog
   * @return a quickfix which opens a refactoring wizard with the spartanization
   */
  public IMarkerResolution getFixWithPreview(final String s) {
    return new IMarkerResolution() {
      /**
       * a quickfix which opens a refactoring wizard with the spartanization
       *
       * @author Boris van Sosin <code><boris.van.sosin [at] gmail.com></code>
       *         (v2)
       */
      @Override public String getLabel() {
        return Spartanization.this + "Show me a preview of '" + s + "' first";
      }
      @Override public void run(final IMarker m) {
        setMarker(m);
        try {
          new RefactoringWizardOpenOperation(new Wizard(Spartanization.this)).run(Display.getCurrent().getActiveShell(), "Spartan refactoring: " + Spartanization.this);
        } catch (final InterruptedException e) {
          e.printStackTrace();
        }
      }
    };
  }
  @Override public final String getName() {
    return name;
  }
  /**
   * @return the selection
   */
  public ITextSelection getSelection() {
    return selection;
  }
  /**
   * .
   *
   * @return True if there are Spartanizations which can be performed on the
   *         compilation unit.
   */
  public boolean haveSuggestions() {
    return countSuggestions() > 0;
  }
  /**
   * @param m marker which represents the range to apply the Spartanization
   *          within
   * @param n the node which needs to be within the range of
   *          <code><b>m</b></code>
   * @return True if the node is within range
   */
  public final boolean inRange(final IMarker m, final ASTNode n) {
    return m != null ? !isNodeOutsideMarker(n, m) : !isTextSelected() || !isNodeOutsideSelection(n);
  }
  /**
   * Performs the current Spartanization on the provided compilation unit
   *
   * @param cu the compilation to Spartanize
   * @param pm progress monitor for long operations (could be
   *          {@link NullProgressMonitor} for light operations)
   * @throws CoreException exception from the <code>pm</code>
   */
  public void performRule(final ICompilationUnit cu, final IProgressMonitor pm) throws CoreException {
    pm.beginTask("Creating change for a single compilation unit...", 2);
    final TextFileChange textChange = new TextFileChange(cu.getElementName(), (IFile) cu.getResource());
    textChange.setTextType("java");
    final SubProgressMonitor spm = new SubProgressMonitor(pm, 1, SubProgressMonitor.SUPPRESS_SUBTASK_LABEL);
    textChange.setEdit(createRewrite((CompilationUnit) Make.COMPILIATION_UNIT.parser(cu).createAST(spm), spm).rewriteAST());
    if (textChange.getEdit().getLength() != 0)
      textChange.perform(pm);
    pm.done();
  }
  /**
   * @param pm a progress monitor in which to display the progress of the
   *          refactoring
   * @param m the marker for which the refactoring needs to run
   * @return a RefactoringStatus
   * @throws CoreException the JDT core throws it
   */
  public RefactoringStatus runAsMarkerFix(final IProgressMonitor pm, final IMarker m) throws CoreException {
    return innerRunAsMarkerFix(pm, m, false);
  }
  /**
   * @param compilationUnit the compilationUnit to set
   */
  public void setCompilationUnit(final ICompilationUnit compilationUnit) {
    this.compilationUnit = compilationUnit;
  }
  /**
   * @param marker the marker to set for the refactoring
   */
  public final void setMarker(final IMarker marker) {
    this.marker = marker;
  }
  /**
   * @param selection the selection to set
   */
  public void setSelection(final ITextSelection selection) {
    this.selection = selection;
  }
  @Override public String toString() {
    return name;
  }
  protected abstract ASTVisitor collect(final List<Rewrite> $);
  protected abstract void fillRewrite(ASTRewrite r, CompilationUnit cu, IMarker m);
  /**
   * Determines if the node is outside of the selected text.
   *
   * @return true if the node is not inside selection. If there is no selection
   *         at all will return false.
   */
  protected boolean isNodeOutsideSelection(final ASTNode n) {
    return !isSelected(n.getStartPosition());
  }
  /**
   * @param u
   * @throws CoreException
   */
  protected void scanCompilationUnit(final ICompilationUnit u, final IProgressMonitor m) throws CoreException {
    m.beginTask("Creating change for a single compilation unit...", 2);
    final TextFileChange textChange = new TextFileChange(u.getElementName(), (IFile) u.getResource());
    textChange.setTextType("java");
    final SubProgressMonitor subProgressMonitor = new SubProgressMonitor(m, 1, SubProgressMonitor.SUPPRESS_SUBTASK_LABEL);
    textChange.setEdit(createRewrite((CompilationUnit) Make.COMPILIATION_UNIT.parser(u).createAST(subProgressMonitor), subProgressMonitor).rewriteAST());
    if (textChange.getEdit().getLength() != 0)
      changes.add(textChange);
    m.done();
  }
  // TODO: Do not add new public methods without JavaDoc!
  protected void scanCompilationUnitForMarkerFix(final IMarker m, final IProgressMonitor pm, final boolean preview) throws CoreException {
    pm.beginTask("Creating change(s) for a single compilation unit...", 2);
    final ICompilationUnit u = As.iCompilationUnit(m);
    final TextFileChange textChange = new TextFileChange(u.getElementName(), (IFile) u.getResource());
    textChange.setTextType("java");
    textChange.setEdit(createRewrite(new SubProgressMonitor(pm, 1, SubProgressMonitor.SUPPRESS_SUBTASK_LABEL), m).rewriteAST());
    if (textChange.getEdit().getLength() != 0)
      if (!preview)
        textChange.perform(pm);
      else
        changes.add(textChange);
    pm.done();
  }
  /**
   * Creates a change from each compilation unit and stores it in the changes
   * array
   *
   * @throws IllegalArgumentException
   * @throws CoreException
   */
  protected void scanCompilationUnits(final List<ICompilationUnit> cus, final IProgressMonitor pm) throws IllegalArgumentException, CoreException {
    pm.beginTask("Iterating over gathered compilation units...", cus.size());
    for (final ICompilationUnit cu : cus)
      scanCompilationUnit(cu, new SubProgressMonitor(pm, 1, SubProgressMonitor.SUPPRESS_SUBTASK_LABEL));
    pm.done();
  }
  private ASTRewrite createRewrite(final SubProgressMonitor pm, final CompilationUnit cu, final IMarker m) {
    if (pm != null)
      pm.beginTask("Creating rewrite operation...", 1);
    final ASTRewrite $ = ASTRewrite.create(cu.getAST());
    fillRewrite($, cu, m);
    if (pm != null)
      pm.done();
    return $;
  }
  /**
   * creates an ASTRewrite, under the context of a text marker, which contains
   * the changes
   *
   * @param pm a progress monitor in which to display the progress of the
   *          refactoring
   * @param m the marker
   * @return an ASTRewrite which contains the changes
   */
  private final ASTRewrite createRewrite(final SubProgressMonitor pm, final IMarker m) {
    return createRewrite(pm, (CompilationUnit) As.COMPILIATION_UNIT.ast(m, pm), m);
  }
  private List<ICompilationUnit> getUnits(final IProgressMonitor pm) throws JavaModelException {
    if (!isTextSelected())
      return getAllProjectCompilationUnits(compilationUnit == null ? BaseHandler.getCompilationUnit() : compilationUnit,
          new SubProgressMonitor(pm, 1, SubProgressMonitor.SUPPRESS_SUBTASK_LABEL));
    final List<ICompilationUnit> $ = new ArrayList<>();
    $.add(compilationUnit);
    return $;
  }
  private RefactoringStatus innerRunAsMarkerFix(final IProgressMonitor pm, final IMarker m, final boolean preview) throws CoreException {
    marker = m;
    pm.beginTask("Running refactoring...", 2);
    scanCompilationUnitForMarkerFix(m, pm, preview);
    marker = null;
    pm.done();
    return new RefactoringStatus();
  }
  private boolean isSelected(final int offset) {
    return isTextSelected() && offset >= selection.getOffset() && offset < selection.getLength() + selection.getOffset();
  }
  private final boolean isTextSelected() {
    return selection != null && !selection.isEmpty() && selection.getLength() != 0;
  }
  private void runAsManualCall(final IProgressMonitor pm) throws JavaModelException, CoreException {
    pm.beginTask("Checking preconditions...", 2);
    scanCompilationUnits(getUnits(pm), new SubProgressMonitor(pm, 1, SubProgressMonitor.SUPPRESS_SUBTASK_LABEL));
  }
}