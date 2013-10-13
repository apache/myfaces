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
package org.apache.myfaces.cdi.util;

import org.apache.myfaces.cdi.dependent.AbstractBeanStorage;
import org.apache.myfaces.cdi.dependent.DependentBeanEntry;
import org.apache.myfaces.cdi.dependent.DependentBeanStorage;
import org.apache.myfaces.cdi.dependent.ViewDependentBeanStorage;
import org.apache.myfaces.shared.config.MyfacesConfig;
import org.apache.myfaces.util.ExternalSpecifications;

import javax.enterprise.context.Dependent;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.spi.Bean;
import javax.enterprise.inject.spi.BeanManager;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.convert.Converter;
import javax.faces.validator.Validator;
import java.io.Serializable;
import java.util.Set;

public class ExternalArtifactResolver
{
    public static final String JAVAX_FACES_CONVERT_PACKAGE_NAME = "javax.faces.convert";
    public static final String JAVAX_FACES_VALIDATOR_PACKAGE_NAME = "javax.faces.validator";

    private static volatile Boolean converterInjectionEnabled;
    private static volatile Boolean validatorInjectionEnabled;

    public static boolean isConverterInjectionEnabled()
    {
        if (converterInjectionEnabled != null)
        {
            return converterInjectionEnabled;
        }

        initConverterInjectionEnabled();
        return converterInjectionEnabled;
    }

    private static synchronized void initConverterInjectionEnabled()
    {
        if (converterInjectionEnabled != null)
        {
            return;
        }

        converterInjectionEnabled = MyfacesConfig.getCurrentInstance(
                FacesContext.getCurrentInstance().getExternalContext()).isConverterInjectionEnabled();
    }

    public static Converter resolveManagedConverter(Class<? extends Converter> converterClass)
    {
        if (!isConverterInjectionEnabled())
        {
            return null;
        }

        ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
        if (!ExternalSpecifications.isCDIAvailable(externalContext) ||
                JAVAX_FACES_CONVERT_PACKAGE_NAME.equals(converterClass.getPackage().getName()))
        {
            return null;
        }

        BeanManager beanManager = CDIUtils.getBeanManager(externalContext);

        return getContextualReference(beanManager, converterClass);
    }

    public static boolean isValidatorInjectionEnabled()
    {
        if (validatorInjectionEnabled != null)
        {
            return validatorInjectionEnabled;
        }

        initValidatorInjectionEnabled();
        return validatorInjectionEnabled;
    }

    private static synchronized void initValidatorInjectionEnabled()
    {
        if (validatorInjectionEnabled != null)
        {
            return;
        }

        validatorInjectionEnabled = MyfacesConfig.getCurrentInstance(
                FacesContext.getCurrentInstance().getExternalContext()).isValidatorInjectionEnabled();
    }

    public static Validator resolveManagedValidator(Class<? extends Validator> validatorClass)
    {
        if (!isValidatorInjectionEnabled())
        {
            return null;
        }

        ExternalContext externalContext = FacesContext.getCurrentInstance().getExternalContext();
        if (!ExternalSpecifications.isCDIAvailable(externalContext) ||
                JAVAX_FACES_VALIDATOR_PACKAGE_NAME.equals(validatorClass.getPackage().getName()))
        {
            return null;
        }

        BeanManager beanManager = CDIUtils.getBeanManager(externalContext);

        return getContextualReference(beanManager, validatorClass);
    }

    private static <T> T getContextualReference(BeanManager beanManager, Class<T> type)
    {
        Set<Bean<?>> beans = beanManager.getBeans(type);

        if (beans == null || beans.isEmpty())
        {
            return null;
        }

        return getContextualReference(type, beanManager, beans);
    }

    private static <T> T getContextualReference(Class<T> type, BeanManager beanManager, Set<Bean<?>> beans)
    {
        Bean<?> bean = beanManager.resolve(beans);

        CreationalContext<?> creationalContext = beanManager.createCreationalContext(bean);

        @SuppressWarnings({ "unchecked", "UnnecessaryLocalVariable" })
        T result = (T) beanManager.getReference(bean, type, creationalContext);

        if (bean.getScope().equals(Dependent.class))
        {
            AbstractBeanStorage beanStorage;

            if (Serializable.class.isAssignableFrom(bean.getBeanClass()))
            {
                beanStorage = getContextualReference(beanManager, ViewDependentBeanStorage.class);
            }
            else
            {
                beanStorage = getContextualReference(beanManager, DependentBeanStorage.class);
            }

            //noinspection unchecked
            beanStorage.add(new DependentBeanEntry(result, bean, creationalContext));
        }

        return result;
    }
}
