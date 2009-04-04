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
package org.apache.myfaces.webapp;

import javax.el.ELContext;
import javax.el.ExpressionFactory;
import javax.el.MethodExpression;
import javax.el.ValueExpression;
import javax.faces.FacesException;
import javax.faces.context.ExternalContext;
import javax.servlet.ServletContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Initializes MyFaces in a JSP 2.0 (or less) environment.
 * 
 */
public class Jsp20FacesInitializer extends AbstractFacesInitializer
{
    /**
     * The logger instance for this class.
     */
    private static final Log log = LogFactory.getLog(Jsp20FacesInitializer.class);

    /**
     * This parameter specifies the ExpressionFactory implementation to use.
     */
    private static final String EXPRESSION_FACTORY = "org.apache.myfaces.EXPRESSION_FACTORY";

    /**
     * The ExpressionFactory implementation of the EL-RI.
     */
    private static final String EL_RI_EXPRESSION_FACTORY_IMPL = "com.sun.facelets.el.ExpressionFactoryImpl";

    /**
     * Jasper's ExpressionFactory implementation.
     */
    private static final String JASPER_EL_EXPRESSION_FACTORY_IMPL = "org.apache.el.ExpressionFactoryImpl";

    /**
     * All known ExpressionFactory implementations.
     */
    private static final String[] KNOWN_EXPRESSION_FACTORIES =
            new String[] { EL_RI_EXPRESSION_FACTORY_IMPL, JASPER_EL_EXPRESSION_FACTORY_IMPL };

    @Override
    protected void initContainerIntegration(ServletContext servletContext, ExternalContext externalContext)
    {
        if (log.isInfoEnabled())
        {
            log.info("This application isn't running in a JSP 2.1 container.");
        }

        // It's possible to run MyFaces in a JSP 2.0 Container, but the user has to provide
        // the ExpressionFactory implementation to use as there is no JspApplicationContext
        // we could ask for. Having said that, though, the user only has to provide it, if
        // there is no known ExpressionFactory available (i.e. if neither
        // "com.sun.facelets.el.ExpressionFactoryImpl" nor "org.apache.el.ExpressionFactoryImpl"
        // are available).
        ExpressionFactory expressionFactory = null;

        String expressionFactoryClassName = externalContext.getInitParameter(EXPRESSION_FACTORY);
        if (expressionFactoryClassName != null && expressionFactoryClassName.trim().length() > 0)
        {
            if (log.isDebugEnabled())
            {
                log.debug("Attempting to load the ExpressionFactory implementation " + "you've specified: '"
                        + expressionFactoryClassName + "'.");
            }

            expressionFactory = loadExpressionFactory(expressionFactoryClassName);
        }

        if (expressionFactory == null)
        {
            if (log.isInfoEnabled())
            {
                log.info("Either you haven't specified the ExpressionFactory implementation, or an "
                        + "error occured while instantiating the implementation you've specified. "
                        + "However, attempting to load a known implementation.");
            }

            expressionFactory = findExpressionFactory(KNOWN_EXPRESSION_FACTORIES);
            if (expressionFactory == null)
            { // if we still haven't got a valid implementation
                if (log.isErrorEnabled())
                {
                    log.error("No valid ExpressionFactory implementation is available "
                            + "but that's required as this application isn't running in a JSP 2.1 container.");
                }

                // use a dummy implementation that reports the error again
                expressionFactory = new ErrorExpressionFactory();
            }
        }

        if (log.isDebugEnabled())
        {
            log.debug("The following ExpressionFactory implementation will " + "be used: '" + expressionFactory + "'.");
        }

        buildConfiguration(servletContext, externalContext, expressionFactory);
    }

    /**
     * Loads and instantiates the given ExpressionFactory implementation.
     * 
     * @param expressionFactoryClassName
     *            the class name of the ExpressionFactory implementation
     * 
     * @return the newly created ExpressionFactory implementation, or <code>null</code>, if an error occurred
     */
    private static ExpressionFactory loadExpressionFactory(String expressionFactoryClassName)
    {
        try
        {
            Class<?> expressionFactoryClass = Class.forName(expressionFactoryClassName);
            return (ExpressionFactory)expressionFactoryClass.newInstance();
        }
        catch (Exception ex)
        {
            if (log.isDebugEnabled())
            {
                log.debug("An error occured while instantiating a new ExpressionFactory. "
                        + "Attempted to load class '" + expressionFactoryClassName + "'.", ex);
            }
        }

        return null;
    }

    /**
     * Attempts to find a valid ExpressionFactory implementation. Each of the given
     * "ExpressionFactory implementation candidates" will be tried to instantiate. If an attempt succeeded, the
     * ExpressionFactory implementation will be returned (i.e. the first valid ExpressionFactory implementation will be
     * returned) and if no attempt succeeds, <code>null</code> will be returned.
     * 
     * @param expressionFactoryClassNames
     *            "ExpresionFactory implementation candidates"
     * 
     * @return the newly created ExpressionFactory implementation, or <code>null</code>, if there is no valid
     *         implementation
     */
    private static ExpressionFactory findExpressionFactory(String[] expressionFactoryClassNames)
    {
        for (String expressionFactoryClassName : expressionFactoryClassNames)
        {
            ExpressionFactory expressionFactory = loadExpressionFactory(expressionFactoryClassName);
            if (expressionFactory != null)
            {
                return expressionFactory;
            }
        }

        return null;
    }

    /**
     * Dummy implementation informing the user that there is no valid ExpressionFactory implementation available. This
     * class makes it easier for the user to understand why the application crashes. Otherwise he would have to deal
     * with NullPointerExceptions.
     * 
     */
    private class ErrorExpressionFactory extends ExpressionFactory
    {

        @Override
        public Object coerceToType(Object obj, Class<?> targetType)
        {
            throw new FacesException("No valid ExpressionFactory implementation is available "
                    + "but that's required as this application isn't running in a JSP 2.1 container.");
        }

        @Override
        public MethodExpression createMethodExpression(ELContext context, String expression,
                                                       Class<?> expectedReturnType, Class<?>[] expectedParamTypes)
        {
            throw new FacesException("No valid ExpressionFactory implementation is available "
                    + "but that's required as this application isn't running in a JSP 2.1 container.");
        }

        @Override
        public ValueExpression createValueExpression(Object instance, Class<?> expectedType)
        {
            throw new FacesException("No valid ExpressionFactory implementation is available "
                    + "but that's required as this application isn't running in a JSP 2.1 container.");
        }

        @Override
        public ValueExpression createValueExpression(ELContext context, String expression, Class<?> expectedType)
        {
            throw new FacesException("No valid ExpressionFactory implementation is available "
                    + "but that's required as this application isn't running in a JSP 2.1 container.");
        }

    }

}
