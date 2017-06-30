package net.vtst.ow.eclipse.less.less;

import net.vtst.ow.eclipse.less.scoping.MixinPath;

import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.nodemodel.ICompositeNode;
import org.eclipse.xtext.nodemodel.util.NodeModelUtils;

/**
 * Helper class for manipulating Mixins.
 */
public class MixinUtils {
  
  // **************************************************************************
  // Helper class for accessing Mixin objects
  
  public static boolean isDefinition(Mixin mixin) {
    return mixin.getBody() != null;
  }
  
  public static boolean isCall(Mixin mixin) {
    return mixin.getBody() == null;
  }

  public static MixinPath getPath(Mixin mixin) { 
    return new MixinPath(mixin.getSelectors().getSelector());
  }
  
  // **************************************************************************
  // Other utility functions
  
  /**
   * @param term
   * @return true if term is a single variable.
   */
  public static boolean isVariableRefOrNumericLiteral(Term term) {
    if (term instanceof ExtendedTerm) {
      ExtendedTerm extendedTerm = (ExtendedTerm) term;
      EList<EObject> subTerms = extendedTerm.getTerm();
      if (subTerms.size() != 1) return false;
      EObject subTerm = subTerms.get(0);
      if (subTerm instanceof AtVariableRef  || subTerm instanceof NumericLiteral) return true;
      else if (subTerm instanceof Term) return isVariableRefOrNumericLiteral((Term) subTerm);
      else return false;
    }
    return false;
  }
  
  /**
   * @param term
   * @return the single variable contained in term, or null.
   */
  public static AtVariableRef getVariableRef(Term term) {
    if (term instanceof ExtendedTerm) {
      ExtendedTerm extendedTerm = (ExtendedTerm) term;
      EList<EObject> subTerms = extendedTerm.getTerm();
      if (subTerms.size() != 1) return null;
      EObject subTerm = subTerms.get(0);
      if (subTerm instanceof AtVariableRef) return (AtVariableRef) subTerm;
      else if (subTerm instanceof Term) return getVariableRef((Term) subTerm);
      else return null;
    }
    return null;
  }
  
  public static AtVariableRefTarget getVariableBoundByMixinParameter(MixinParameter parameter) {
    if (parameter.isHasDefaultValue()) {
      return parameter.getIdent();
    } else if (parameter.getTerm().size() > 0) {
        return getVariableRef(parameter.getTerm().get(0));
    } else {
      return null;
    }
  }
 
  public static String getVariableName(MixinParameter parameter) {
    if (parameter.isHasDefaultValue()) {
      AtVariableDef variable = parameter.getIdent();
      if (variable != null) return variable.getIdent();
      else return null;
    } else if (parameter.getTerm().size() > 0) {
      AtVariableRef variable = MixinUtils.getVariableRef(parameter.getTerm().get(0));
      if (variable != null) return MixinUtils.getIdent(variable);
      else return null;
    } else {
      return null;
    }
  }

  public static EObject getFirstNonTermAncestor(EObject obj) {
    EObject result = obj.eContainer();
    while (result instanceof Term) result = result.eContainer();
    return result;
  }
  
  /**
   * @param obj
   * @return true if obj is a variable reference which is in fact the name of a mixin parameter in a
   *   mixin definition.
   */
  public static boolean isBoundByMixinDefinitionParameter(EObject obj) {
    EObject container = MixinUtils.getFirstNonTermAncestor(obj);
    if (container instanceof MixinParameter) {
      MixinParameter parameter = (MixinParameter) container;
      if (!parameter.isHasDefaultValue()) {
        EObject mixin = LessUtils.getNthAncestor(container, 2);
        if (mixin instanceof Mixin) {
          if (MixinUtils.isDefinition((Mixin) mixin)) return true;
        }
      }
    }
    return false;
  }
  
  public static boolean isBoundByMixinDefinitionSelector(EObject obj) {
    EObject container = obj.eContainer();
    if (container instanceof MixinSelectors) {
      EObject mixin = container.eContainer();
      if (mixin instanceof Mixin) {
        if (MixinUtils.isDefinition((Mixin) mixin)) return true;
      }
    }
    return false;
  }
  
  private static String getIdentText(EObject obj) {
    ICompositeNode node = NodeModelUtils.getNode(obj);
    if (node == null) {
    	return null;
    } else {
    	return NodeModelUtils.getTokenText(node);
    }
  }
  
  public static String getIdent(AtVariableRefTarget obj) {
    if (obj instanceof AtVariableDef) return ((AtVariableDef) obj).getIdent();
    if (obj instanceof AtVariableRef) return getIdentText(obj);
    throw new RuntimeException("Should not be called with a Mixin");
  }

  // Keep in sync with the above version
  public static String getIdent(AtVariableRef obj) {
    return getIdentText(obj);
  }
  
  public static String getIdent(HashOrClassRefTarget obj) {
    if (obj instanceof HashOrClass) return ((HashOrClass) obj).getIdent();
    if (obj instanceof HashOrClassRef || obj instanceof HashOrClassRefTarget)
      return getIdentText(obj);
    throw new RuntimeException("Unknown subclass of HashOrClassRefTarget: ");
  }

  // Keep in sync with the above version
  public static String getIdent(HashOrClassRef obj) {
    return getIdentText(obj);
  }

}
