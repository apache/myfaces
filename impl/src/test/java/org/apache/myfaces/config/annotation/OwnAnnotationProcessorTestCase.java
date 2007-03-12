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

import org.apache.shale.test.base.AbstractJsfTestCase;
import org.apache.myfaces.AnnotationProcessor;


public class OwnAnnotationProcessorTestCase extends AbstractJsfTestCase
{
	protected AnnotationProcessor processor;
	protected AnnotatedManagedBean managedBean;
    private static final String TEST_ANNOTATION_PROCESSOR = "org.apache.myfaces.config.annotation.TestAnnotationProcessor";


    public OwnAnnotationProcessorTestCase(String string)
    {
        super(string);
    }

    public void setUp() throws Exception {
        super.setUp();
        AnnotationProcessorFactory.getAnnotatonProcessorFactory().release();
        servletContext.addInitParameter(DefaultAnnotationProcessorFactory.ANNOTATION_PROCESSOR_PROVIDER, TEST_ANNOTATION_PROCESSOR);
        processor = AnnotationProcessorFactory.getAnnotatonProcessorFactory().getAnnotatonProcessor(externalContext);
        managedBean = new AnnotatedManagedBean();

    }

    public void testOwnProcessor()
    {
		assertEquals(TEST_ANNOTATION_PROCESSOR, processor.getClass().getName());
	}
}
