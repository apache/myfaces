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
package org.apache.myfaces.spi.impl;

import java.lang.reflect.InvocationTargetException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.faces.context.ExternalContext;
import javax.naming.NamingException;
import javax.servlet.ServletContext;

import org.apache.myfaces.spi.InjectionProvider;
import org.apache.myfaces.spi.InjectionProviderException;

public class TomcatAnnotationInjectionProvider extends InjectionProvider
{
    private static Logger log = Logger.getLogger(TomcatAnnotationInjectionProvider.class.getName());

    private ExternalContext externalContext;
    private org.apache.AnnotationProcessor annotationProcessor;

    public TomcatAnnotationInjectionProvider(ExternalContext externalContext)
    {
        this.externalContext = externalContext;
    }

    @Override
    public Object inject(Object instance) throws InjectionProviderException
    {
        try
        {
            annotationProcessor.processAnnotations(instance);
        }
        catch (IllegalAccessException ex)
        {
            throw new InjectionProviderException(ex);
        }
        catch (InvocationTargetException ex)
        {
            throw new InjectionProviderException(ex);
        }
        catch (NamingException ex)
        {
            throw new InjectionProviderException(ex);
        }
        return null;
    }

    @Override
    public void preDestroy(Object instance, Object creationMetaData) throws InjectionProviderException
    {
        if (log.isLoggable(Level.FINEST))
        {
            log.info("Destroy instance of " + instance.getClass().getName());
        }
        try
        {
            annotationProcessor.preDestroy(instance);
        }
        catch (IllegalAccessException ex)
        {
            throw new InjectionProviderException(ex);
        }
        catch (InvocationTargetException ex)
        {
            throw new InjectionProviderException(ex);
        }
    }

    @Override
    public boolean isAvailable()
    {
        try
        {
            annotationProcessor =  (org.apache.AnnotationProcessor) ((ServletContext)
                     externalContext.getContext()).getAttribute(org.apache.AnnotationProcessor.class.getName());
            return annotationProcessor != null;
        }
        catch (Throwable e)
        {
            // ignore
        }
        return false;
    }

    @Override
    public void postConstruct(Object instance, Object creationMetaData) throws InjectionProviderException
    {
        try
        {
            annotationProcessor.postConstruct(instance);
        }
        catch (IllegalAccessException ex)
        {
            throw new InjectionProviderException(ex);
        }
        catch (InvocationTargetException ex)
        {
            throw new InjectionProviderException(ex);
        }
    }
}
