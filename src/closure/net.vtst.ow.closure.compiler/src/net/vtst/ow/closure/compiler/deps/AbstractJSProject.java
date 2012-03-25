package net.vtst.ow.closure.compiler.deps;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import com.google.javascript.jscomp.AbstractCompiler;
import com.google.javascript.jscomp.DiagnosticType;
import com.google.javascript.jscomp.JSError;
import com.google.javascript.jscomp.deps.SortedDependencies;
import com.google.javascript.jscomp.deps.SortedDependencies.CircularDependencyException;
import com.google.javascript.jscomp.deps.SortedDependencies.MissingProvideException;

/**
 * A super class for {@code JSLibrary} and {@code JSProject}.
 * @author vtst
 *
 */
public abstract class AbstractJSProject {
  
  SortedDependencies<? extends JSUnit> dependencies;
  
  /**
   * Set the list of units for the project, and re-build the dependency graph.
   * @param units  The new list of units.
   * @throws CircularDependencyException  If there is a circular dependency in the passed list.
   */
  public <T extends JSUnit> void setUnits(AbstractCompiler compiler, List<T> units) throws CircularDependencyException {
    dependencies = new SortedDependencies<T>(units);
    int index = 0;
    for (JSUnit unit: dependencies.getSortedList()) {
      unit.dependencyIndex = index;
      ++index;
    }
  }

  /**
   * @return  The list of unit, sorted according to their dependencies.
   */
  protected List<? extends JSUnit> getSortedUnits() {
    return dependencies.getSortedList();
  }

  /**
   * Get the unit providing a name in the current project.  Does not perform recursive calls
   * to the referenced projects.
   * @param name  The name to look for.
   * @return  The providing unit, or {@code null}.
   */
  protected JSUnit getUnitProviding(String name) {
    try {
      return dependencies.getInputProviding(name);
    } catch (MissingProvideException e) {
      return null;
    }
  }

  /**
   * @return  The list of referenced projects.
   */
  protected abstract List<AbstractJSProject> getReferencedProjects();

  private class DependencyBuilder {
    private ArrayList<ArrayList<JSUnit>> results;
    private LinkedList<String> namesToVisit;
    Set<String> foundNames;
    
    DependencyBuilder(AbstractJSProject project, JSUnit unit) {
      namesToVisit = Lists.newLinkedList(unit.getRequires());
      foundNames = Sets.newHashSet(Iterables.concat(unit.getProvides(), unit.getRequires()));
      List<AbstractJSProject> referencedProjects = getReferencedProjects();
      results = new ArrayList<ArrayList<JSUnit>>(referencedProjects.size() + 1);
      for (AbstractJSProject referencedProject: referencedProjects) {
        visit(referencedProject);
      }
      visit(project);
    }
    
    void visit(AbstractJSProject project) {
      LinkedList<String> remainingNames = new LinkedList<String>();
      ArrayList<JSUnit> unitsInThisProject = new ArrayList<JSUnit>();
      while (!namesToVisit.isEmpty()) {
        String name = namesToVisit.remove();
        JSUnit providingUnit = project.getUnitProviding(name);
        if (providingUnit == null) {
          remainingNames.add(name);
        } else {
          unitsInThisProject.add(providingUnit);
          for (String requiredName: providingUnit.getRequires()) {
            if (foundNames.add(requiredName)) namesToVisit.add(requiredName);
          }
        }
      }
      namesToVisit = remainingNames;
      Collections.sort(unitsInThisProject, new JSUnitComparator());
      results.add(unitsInThisProject);
    }
    
    ArrayList<JSUnit> get() {
      int size = 0;
      for (ArrayList<JSUnit> list: results) size += list.size();
      ArrayList<JSUnit> result = new ArrayList<JSUnit>(size);
      for (ArrayList<JSUnit> list:results) result.addAll(list);
      return result;
    }
    
  }
  
  /**
   * Returns the list of units which are required to build {@code unit}, ordered according to
   * their dependencies.  Units from referenced projects are included.
   * @param unit  The unit to look for, which must be in the list of units of the project.
   * @return  The units required to build {@code unit}.
   */
  public List<JSUnit> getSortedDependenciesOf(JSUnit unit) {
    DependencyBuilder builder = new DependencyBuilder(this, unit);
    return builder.get();
  }
  
  /**
   * Comparator for {@code JSUnit}, according to the dependency order in the project.
   */
  private static class JSUnitComparator implements Comparator<JSUnit> {
    @Override
    public int compare(JSUnit unit1, JSUnit unit2) {
      return (unit1.dependencyIndex - unit2.dependencyIndex);
    }
  }

  // **************************************************************************
  // Error reporting
  
  static final DiagnosticType CIRCULAR_DEPENDENCY_ERROR =
      DiagnosticType.error("JSC_CIRCULAR_DEP",
          "Circular dependency detected: {0}");

  protected void reportError(AbstractCompiler compiler, CircularDependencyException e) {
    compiler.report(JSError.make(CIRCULAR_DEPENDENCY_ERROR, e.getMessage()));   
  }
}