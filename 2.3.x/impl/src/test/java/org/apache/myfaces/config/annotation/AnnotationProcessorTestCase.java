package org.apache.myfaces.config.annotation;

/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

import org.apache.myfaces.test.base.AbstractJsfTestCase;

import javax.naming.NamingException;
import java.lang.reflect.InvocationTargetException;


public class AnnotationProcessorTestCase extends AbstractJsfTestCase
{
    protected LifecycleProvider2 lifecycleProvider;
    protected AnnotatedManagedBean managedBean;


    public AnnotationProcessorTestCase(String string)
    {
        super(string);
    }

    public void setUp() throws Exception {
        super.setUp();
        lifecycleProvider = new AllAnnotationLifecycleProvider(null);
        managedBean = new AnnotatedManagedBean();
    }

    public void testPostConstruct() throws IllegalAccessException, InvocationTargetException, NamingException, InstantiationException, ClassNotFoundException
    {
        AnnotatedManagedBean managedBean = (AnnotatedManagedBean) lifecycleProvider.newInstance(AnnotatedManagedBean.class.getName());
        lifecycleProvider.postConstruct(managedBean);
        assertTrue(managedBean.isPostConstructCalled());
        assertFalse(managedBean.isPreDestroyCalled());
    }

    public void testPreDestroy() throws IllegalAccessException, InvocationTargetException, NamingException, InstantiationException, ClassNotFoundException
    {
        AnnotatedManagedBean managedBean = (AnnotatedManagedBean) lifecycleProvider.newInstance(AnnotatedManagedBean.class.getName());
        lifecycleProvider.postConstruct(managedBean);
        lifecycleProvider.destroyInstance(managedBean);
        assertTrue(managedBean.isPostConstructCalled());
        assertTrue(managedBean.isPreDestroyCalled());
    }

    public void testPostConstructException() throws IllegalAccessException, InvocationTargetException
    {
        try
        {
            lifecycleProvider.destroyInstance(new AnnotatedManagedBean(true));
        } catch (InvocationTargetException e) {
            return;  
        }
        assertTrue(false);

    }
}
