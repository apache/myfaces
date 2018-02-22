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


public class OwnAnnotationProcessorTestCase extends AbstractJsfTestCase
{
    protected LifecycleProvider lifecycleProvider;
    protected AnnotatedManagedBean managedBean;
    private static final String TEST_LIFECYCLE_PROVIDER = "org.apache.myfaces.config.annotation.MockLifecycleProvider";


    public OwnAnnotationProcessorTestCase(String string)
    {
        super(string);
    }

    public void setUp() throws Exception {
        super.setUp();
        LifecycleProviderFactory.getLifecycleProviderFactory(externalContext).release();
        servletContext.addInitParameter(DefaultLifecycleProviderFactory.LIFECYCLE_PROVIDER, TEST_LIFECYCLE_PROVIDER);
        lifecycleProvider = LifecycleProviderFactory.getLifecycleProviderFactory().getLifecycleProvider(externalContext);
        managedBean = new AnnotatedManagedBean();

    }

    public void testOwnProcessor()
    {
        assertEquals(TEST_LIFECYCLE_PROVIDER, lifecycleProvider.getClass().getName());
    }
}
