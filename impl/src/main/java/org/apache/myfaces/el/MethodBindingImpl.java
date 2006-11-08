/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.myfaces.el;

import org.apache.myfaces.el.ValueBindingImpl.NotVariableReferenceException;

import org.apache.commons.beanutils.MethodUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import javax.faces.application.Application;
import javax.faces.component.StateHolder;
import javax.faces.context.FacesContext;
import javax.faces.el.*;
import javax.faces.event.AbortProcessingException;
import javax.faces.validator.ValidatorException;
import javax.servlet.jsp.el.ELException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;


/**
 * @author Anton Koinov (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
public class MethodBindingImpl extends MethodBinding
    implements StateHolder
{
    static final Log log = LogFactory.getLog(MethodBindingImpl.class);

    //~ Instance fields -------------------------------------------------------

    ValueBindingImpl _valueBinding;
    Class[]          _argClasses;

    //~ Constructors ----------------------------------------------------------

    public MethodBindingImpl(Application application, String reference,
        Class[] argClasses)
    {
        // Note: using ValueBindingImpl, istead of creating a common subclass,
        //       to share single Expression cache
        // Note: we can trim() reference, since string-binding mixed
        //       expressions are not allowed for MethodBindings
        _valueBinding = new ValueBindingImpl(application, reference.trim());
        _argClasses = argClasses;
    }

    //~ Methods ---------------------------------------------------------------

    public String getExpressionString()
    {
        return _valueBinding._expressionString;
    }

    public Class getType(FacesContext facesContext)
    {
        if (facesContext == null) {
            throw new NullPointerException("facesContext");
        }
        try
        {
            Object[] baseAndProperty = resolveToBaseAndProperty(facesContext);
            Object base = baseAndProperty[0];
            Object property = baseAndProperty[1];

            Class returnType = base.getClass().getMethod(property.toString(), _argClasses).getReturnType();

            if (returnType.getName().equals("void")) {
                // the spec document says: "if type is void return null"
                // but the RI returns Void.class, so let's follow the RI
                return Void.class;
            }
            return returnType;
        }
        catch (ReferenceSyntaxException e)
        {
            throw e;
        }
        catch (IndexOutOfBoundsException e)
        {
            // ArrayIndexOutOfBoundsException also here
            throw new PropertyNotFoundException("Expression: "
                + getExpressionString(), e);
        }
        catch (Exception e)
        {
            throw new EvaluationException("Cannot get type for expression "
                + getExpressionString(), e);
        }
    }

    public Object invoke(FacesContext facesContext, Object[] args)
        throws EvaluationException, MethodNotFoundException
    {
        if (facesContext == null) {
            throw new NullPointerException("facesContext");
        }
        try
        {
            Object[] baseAndProperty = resolveToBaseAndProperty(facesContext);
            Object base = baseAndProperty[0];
            Object property = baseAndProperty[1];

            Method m = base.getClass().getMethod(property.toString(), _argClasses);

            // Check if the concrete class of this method is accessible and if not
            // search for a public interface that declares this method
            m = MethodUtils.getAccessibleMethod(m);
            if (m == null)
            {
                throw new MethodNotFoundException(
                    getExpressionString() + " (not accessible!)");
            }

            return m.invoke(base, args);
        }
        catch (ReferenceSyntaxException e)
        {
            throw e;
        }
        catch (IndexOutOfBoundsException e)
        {
            // ArrayIndexOutOfBoundsException also here
            throw new PropertyNotFoundException("Expression: "
                + getExpressionString(), e);
        }
        catch (InvocationTargetException e)
        {
            Throwable cause = e.getCause();
            if (cause != null)
            {
                if (cause instanceof ValidatorException ||
                    cause instanceof AbortProcessingException)
                {
                    throw new EvaluationException(cause);
                }
                else
                {
                    throw new EvaluationException("Exception while invoking expression "
                        + getExpressionString(), cause);
                }
            }
            else
            {
                throw new EvaluationException("Exception while invoking expression "
                    + getExpressionString(), e);
            }
        }
        catch (Exception e)
        {
            throw new EvaluationException("Exception while invoking expression "
                + getExpressionString(), e);
        }
    }

    protected Object[] resolveToBaseAndProperty(FacesContext facesContext)
        throws ELException
    {
        if (facesContext == null)
        {
            throw new NullPointerException("facesContext");
        }

        try
        {
            Object base = _valueBinding.resolveToBaseAndProperty(facesContext);

            if (!(base instanceof Object[]))
            {
                String errorMessage = "Expression not a valid method binding: "
                    + getExpressionString();
                throw new ReferenceSyntaxException(errorMessage);
            }

            return (Object[]) base;
        }
        catch (NotVariableReferenceException e)
        {
            throw new ReferenceSyntaxException("Expression: "
                + getExpressionString(), e);
        }
    }

    public String toString()
    {
        return _valueBinding.toString();
    }

    //~ StateHolder implementation --------------------------------------------

    private boolean _transient = false;

    /**
     * Empty constructor, so that new instances can be created when restoring
     * state.
     */
    public MethodBindingImpl()
    {
        _valueBinding = null;
        _argClasses = null;
    }

    public Object saveState(FacesContext facescontext)
    {
        return new Object[] { _valueBinding.saveState(facescontext),
            _argClasses};
    }

    public void restoreState(FacesContext facescontext, Object obj)
    {
        Object[] ar = (Object[]) obj;
        _valueBinding = new ValueBindingImpl();
        _valueBinding.restoreState(facescontext, ar[0]);
        _argClasses = (Class[]) ar[1];
    }

    public boolean isTransient()
    {
        return _transient;
    }

    public void setTransient(boolean flag)
    {
        _transient = flag;
    }

}