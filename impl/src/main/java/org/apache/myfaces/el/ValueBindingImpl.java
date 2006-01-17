/*
 * Copyright 2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.myfaces.el;

import java.lang.reflect.Method;
import java.util.List;
import java.util.Map;

import javax.faces.application.Application;
import javax.faces.component.StateHolder;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.el.EvaluationException;
import javax.faces.el.PropertyNotFoundException;
import javax.faces.el.PropertyResolver;
import javax.faces.el.ReferenceSyntaxException;
import javax.faces.el.ValueBinding;
import javax.servlet.jsp.el.ELException;
import javax.servlet.jsp.el.FunctionMapper;
import javax.servlet.jsp.el.VariableResolver;

import org.apache.myfaces.config.RuntimeConfig;
import org.apache.myfaces.config.element.ManagedBean;
import org.apache.myfaces.util.BiLevelCacheMap;

import org.apache.commons.el.ArraySuffix;
import org.apache.commons.el.Coercions;
import org.apache.commons.el.ComplexValue;
import org.apache.commons.el.ConditionalExpression;
import org.apache.commons.el.Expression;
import org.apache.commons.el.ExpressionString;
import org.apache.commons.el.NamedValue;
import org.apache.commons.el.PropertySuffix;
import org.apache.commons.el.ValueSuffix;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * @author Manfred Geiler (latest modification by $Author$)
 * @author Anton Koinov
 * @version $Revision$ $Date$
 */
public class ValueBindingImpl extends ValueBinding implements StateHolder
{
    //~ Static fields/initializers --------------------------------------------

    static final Log log = LogFactory.getLog(ValueBindingImpl.class);

    /**
     * To implement function support, subclass and use a static
     * initialization block to assign your own function mapper
     */
    protected static FunctionMapper s_functionMapper = new FunctionMapper()
        {
            public Method resolveFunction(String prefix, String localName)
            {
                throw new ReferenceSyntaxException(
                    "Functions not supported in expressions. Function: "
                    + prefix + ":" + localName);
            }
        };

    private static final BiLevelCacheMap s_expressionCache =
        new BiLevelCacheMap(90)
        {
            protected Object newInstance(Object key)
            {
                return ELParserHelper.parseExpression((String) key);
            }
        };

    //~ Instance fields -------------------------------------------------------

    protected Application _application;
    protected String      _expressionString;
    protected Object      _expression;

    /**
     * RuntimeConfig is instantiated once per servlet and never changes--we can
     * safely cache it
     */
    private RuntimeConfig   _runtimeConfig;

    //~ Constructors ----------------------------------------------------------

    public ValueBindingImpl(Application application, String expression)
    {
        if (application == null)
        {
            throw new NullPointerException("application");
        }

        // Do not trim(), we support mixed string-bindings
        if ((expression == null) || (expression.length() == 0))
        {
            throw new ReferenceSyntaxException("Expression: empty or null");
        }
        _application = application;
        _expressionString  = expression;

        _expression = s_expressionCache.get(expression);
    }

    //~ Methods ---------------------------------------------------------------

    public String getExpressionString()
    {
        return _expressionString;
    }

    public boolean isReadOnly(FacesContext facesContext)
    {
        if (facesContext == null) {
            throw new NullPointerException("facesContext");
        }
        try
        {
            Object base_ = resolveToBaseAndProperty(facesContext);
            if (base_ instanceof String)
            {
                return VariableResolverImpl.s_standardImplicitObjects
                    .containsKey(base_);
            }

            Object[] baseAndProperty = (Object[]) base_;
            Object base      = baseAndProperty[0];
            Object property  = baseAndProperty[1];

            Integer index = ELParserHelper.toIndex(base, property);
            return (index == null)
                ? _application.getPropertyResolver().isReadOnly(base, property)
                : _application.getPropertyResolver()
                    .isReadOnly(base, index.intValue());
        }
        catch (NotVariableReferenceException e)
        {
            // if it is not a variable reference (e.g., a constant literal),
            // we cannot write to it but can read it
            return true;
        }
        catch (Exception e)
        {
            log.info("Exception while determining read-only state of value-binding : "+_expressionString);
            // Cannot determine read-only, return true
            // (todo: is this what the spec requires,
            // MYFACES-686 suggests using true due to problems with alias bean?)
            return true;
        }
    }

    public Class getType(FacesContext facesContext)
    {
        if (facesContext == null) {
            throw new NullPointerException("facesContext");
        }
        try
        {
            Object base_ = resolveToBaseAndProperty(facesContext);
            if (base_ instanceof String)
            {
                String name = (String) base_;

                // Check if it is a ManagedBean
                // WARNING: must do this check first to avoid instantiating
                //          the MB in resolveVariable()
                ManagedBean mbConfig =
                    getRuntimeConfig(facesContext).getManagedBean(name);
                if (mbConfig != null)
                {
                    // Note: if MB Class is not set, will return
                    //       <code>null</code>, which is a valid return value
                    return mbConfig.getManagedBeanClass();
                }

                Object val = _application.getVariableResolver()
                    .resolveVariable(facesContext, name);

                // Note: if there is no ManagedBean or variable with this name
                //       in any scope,then we will create a new one and thus
                //       any Object is allowed.
                return (val != null) ? val.getClass() : Object.class;
            }
            else
            {
                Object[] baseAndProperty = (Object[]) base_;
                Object base      = baseAndProperty[0];
                Object property  = baseAndProperty[1];

                Integer index = ELParserHelper.toIndex(base, property);
                return (index == null)
                    ? _application.getPropertyResolver().getType(base, property)
                    : _application.getPropertyResolver()
                        .getType(base, index.intValue());
            }
        }
        catch (NotVariableReferenceException e)
        {
            // It is not a value reference, then it probably is an expression
            // that evaluates to a literal. Get the value and then it's class
            // Note: we could hadle this case in a more performance efficient manner--
            //       but this case is so rare, that for months no-one detected
            //       the error before this code was added.
            try
            {
                return getValue(facesContext).getClass();
            }
            catch (Exception e1)
            {
                // Cannot determine type, return null per JSF spec
                return null;
            }
        }
        catch (PropertyNotFoundException e) {
            throw e;
        }
        catch (Exception e)
        {
            if(log.isDebugEnabled())
                log.debug("Exception while retrieving type for ValueBinding.",e);

            // Cannot determine type, return null per JSF spec
            return null;
        }
    }

    public void setValue(FacesContext facesContext, Object newValue)
            throws EvaluationException, PropertyNotFoundException
    {
        if (facesContext == null) {
            throw new NullPointerException("facesContext");
        }
        try
        {
            Object base_ = resolveToBaseAndProperty(facesContext);
            if (base_ instanceof String)
            {
                String name = (String) base_;
                if (VariableResolverImpl.s_standardImplicitObjects
                    .containsKey(name))
                {
                    String errorMessage =
                        "Cannot set value of implicit object '"
                        + name + "' for expression '" + _expressionString + "'";
                    throw new ReferenceSyntaxException(errorMessage);
                }

                // Note: will be coerced later
                setValueInScope(facesContext, name, newValue);
            }
            else
            {
                Object[] baseAndProperty = (Object[]) base_;
                Object base      = baseAndProperty[0];
                Object property  = baseAndProperty[1];
                PropertyResolver propertyResolver =
                    _application.getPropertyResolver();

                Integer index = ELParserHelper.toIndex(base, property);
                if (index == null)
                {
                    propertyResolver.setValue(
                        base, property, newValue);
                }
                else
                {
                    int indexVal = index.intValue();
                    propertyResolver.setValue(
                        base, indexVal, newValue);
                }
            }
        }
        catch (IndexOutOfBoundsException e)
        {
            // ArrayIndexOutOfBoundsException also here
            throw new PropertyNotFoundException(
                "Expression: '" + _expressionString + "'", e);
        }
        catch (PropertyNotFoundException e)
        {
          throw e;
        }
        catch (Exception e)
        {
            String msg;
            if (newValue == null)
            {
                msg = "Cannot set value for expression '"
                    + _expressionString + "' to null.";
            }
            else
            {
                msg = "Cannot set value for expression '"
                    + _expressionString + "' to a new value of type "
                    + newValue.getClass().getName();
            }
            throw new EvaluationException(msg, e);
        }
    }

    private void setValueInScope(
        FacesContext facesContext, String name, Object newValue)
    throws ELException
    {
        ExternalContext externalContext = facesContext.getExternalContext();

        // Request context
        Map scopeMap = externalContext.getRequestMap();
        Object obj = scopeMap.get(name);
        if (obj != null)
        {
            scopeMap.put(name, newValue);
            return;
        }

        // Session context
        scopeMap = externalContext.getSessionMap();
        obj = scopeMap.get(name);
        if (obj != null)
        {
            scopeMap.put(name, newValue);
            return;
        }

        // Application context
        scopeMap = externalContext.getApplicationMap();
        obj = scopeMap.get(name);
        if (obj != null)
        {
            scopeMap.put(name, newValue);
            return;
        }

        // Check for ManagedBean
        ManagedBean mbConfig =
            getRuntimeConfig(facesContext).getManagedBean(name);
        if (mbConfig != null)
        {
            String scopeName = mbConfig.getManagedBeanScope();

            // find the scope handler object
            // Note: this does not handle user-extended _scope values
            Scope scope =
                (Scope) VariableResolverImpl.s_standardScopes.get(scopeName);
            if (scope != null)
            {
                scope.put(externalContext, name, newValue);
                return;
            }

            log.error("Managed bean '" + name + "' has illegal scope: "
                + scopeName);

            externalContext.getRequestMap().put(name, newValue);
            return;
        }

        // unknown target class, put newValue into request scope without coercion
        externalContext.getRequestMap().put(name, newValue);
    }

    public Object getValue(FacesContext facesContext)
    throws EvaluationException, PropertyNotFoundException
    {
        if (facesContext == null) {
            throw new NullPointerException("facesContext");
        }
        try
        {
            return _expression instanceof Expression
                ? ((Expression) _expression).evaluate(
                    new ELVariableResolver(facesContext),
                    s_functionMapper, ELParserHelper.LOGGER)
                : ((ExpressionString) _expression).evaluate(
                    new ELVariableResolver(facesContext),
                    s_functionMapper, ELParserHelper.LOGGER);
        }
        catch (PropertyNotFoundException e) {
            throw e;
        }
        catch (IndexOutOfBoundsException e)
        {
            // ArrayIndexOutOfBoundsException also here
            throw new PropertyNotFoundException(
                "Expression: '" + _expressionString + "'", e);
        }
        catch (Exception e)
        {
            throw new EvaluationException(
                    "Cannot get value for expression '" + _expressionString
                    + "'", e);
        }
    }

    protected Object resolveToBaseAndProperty(FacesContext facesContext)
        throws ELException, NotVariableReferenceException
    {
        if (facesContext == null)
        {
            throw new NullPointerException("facesContext");
        }

        VariableResolver variableResolver =
            new ELVariableResolver(facesContext);
        Object expression = _expression;

        while (expression instanceof ConditionalExpression)
        {
            ConditionalExpression conditionalExpression =
                ((ConditionalExpression) expression);
            // first, evaluate the condition (and coerce the result to a
            // boolean value)
            boolean condition =
              Coercions.coerceToBoolean(
                  conditionalExpression.getCondition().evaluate(
                      variableResolver, s_functionMapper,
                      ELParserHelper.LOGGER),
                      ELParserHelper.LOGGER)
                  .booleanValue();

            // then, use this boolean to branch appropriately
            expression = condition ? conditionalExpression.getTrueBranch()
                : conditionalExpression.getFalseBranch();
        }

        if (expression instanceof NamedValue)
        {
            return ((NamedValue) expression).getName();
        }

        if (!(expression instanceof ComplexValue)) {
            // all other cases are not variable references
            throw new NotVariableReferenceException(
                "Parsed Expression of unsupported type for this operation. Expression class: "
                    + _expression.getClass().getName() + ". Expression: '"
                    + _expressionString + "'");
        }

        ComplexValue complexValue = (ComplexValue) expression;

        // resolve the prefix
        Object base = complexValue.getPrefix()
            .evaluate(variableResolver, s_functionMapper,
                ELParserHelper.LOGGER);
        if (base == null)
        {
            throw new PropertyNotFoundException("Base is null: "
                + complexValue.getPrefix().getExpressionString());
        }

        // Resolve and apply the suffixes
        List suffixes = complexValue.getSuffixes();
        int max = suffixes.size() - 1;
        for (int i = 0; i < max; i++)
        {
            ValueSuffix suffix = (ValueSuffix) suffixes.get(i);
            base = suffix.evaluate(base, variableResolver, s_functionMapper,
                ELParserHelper.LOGGER);
            if (base == null)
            {
                throw new PropertyNotFoundException("Base is null: "
                    + suffix.getExpressionString());
            }
        }

        // Resolve the last suffix
        ArraySuffix arraySuffix = (ArraySuffix) suffixes.get(max);
        Expression arraySuffixIndex = arraySuffix.getIndex();

        Object index;
        if (arraySuffixIndex != null)
        {
            index = arraySuffixIndex.evaluate(
                    variableResolver, s_functionMapper,
                    ELParserHelper.LOGGER);
            if (index == null)
            {
                throw new PropertyNotFoundException("Index is null: "
                    + arraySuffixIndex.getExpressionString());
            }
        }
        else
        {
            index = ((PropertySuffix) arraySuffix).getName();
        }

        return new Object[] {base, index};
    }

    private Object coerce(Object value, Class clazz) throws ELException
    {
        return (value == null) ? null
            : (clazz == null) ? value :
                Coercions.coerce(value, clazz, ELParserHelper.LOGGER);
    }

    protected RuntimeConfig getRuntimeConfig(FacesContext facesContext)
    {
        if (_runtimeConfig == null)
        {
            _runtimeConfig = RuntimeConfig.getCurrentInstance(facesContext.getExternalContext());
        }
        return _runtimeConfig;
    }

    public String toString()
    {
        return _expressionString;
    }

    //~ State Holder ------------------------------------------------------

    private boolean _transient = false;

    /**
     * Empty constructor, so that new instances can be created when restoring
     * state.
     */
    public ValueBindingImpl()
    {
        _application = null;
        _expressionString = null;
        _expression = null;
    }

    public Object saveState(FacesContext facesContext)
    {
        return _expressionString;
    }

    public void restoreState(FacesContext facesContext, Object obj)
    {
        _application = facesContext.getApplication();
        _expressionString  = (String) obj;
        _expression = s_expressionCache.get(_expressionString);
    }

    public boolean isTransient()
    {
        return _transient;
    }

    public void setTransient(boolean flag)
    {
        _transient = flag;
    }

    //~ Internal classes ------------------------------------------------------

    public static class ELVariableResolver implements VariableResolver {
        private final FacesContext _facesContext;

        public ELVariableResolver(FacesContext facesContext)
        {
            _facesContext = facesContext;
        }

        public Object resolveVariable(String pName)
            throws ELException
        {
            return _facesContext.getApplication().getVariableResolver()
                .resolveVariable(_facesContext, pName);
        }
    }

    public static final class NotVariableReferenceException
        extends ReferenceSyntaxException
    {
        private static final long serialVersionUID = 818254526596948605L;

        public NotVariableReferenceException(String message)
        {
            super(message);
        }
    }
}
