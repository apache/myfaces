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

import org.apache.commons.discovery.tools.DiscoverSingleton;

import org.apache.myfaces.AnnotationProcessor;

import javax.faces.context.ExternalContext;
import java.util.Properties;


public abstract class AnnotationProcessorFactory
{
    protected static final String FACTORY_DEFAULT = DefaultAnnotationProcessorFactory.class.getName();
    private static Properties properties = new Properties();

    protected AnnotationProcessorFactory()
    {
    }

    public static void setAnnotationProcessorFactory(String className) {
        properties.setProperty(AnnotationProcessorFactory.class.getName(), className);
    }


    public static AnnotationProcessorFactory getAnnotatonProcessorFactory()
    {
        return (AnnotationProcessorFactory) DiscoverSingleton.find(AnnotationProcessorFactory.class, properties, FACTORY_DEFAULT);
    }

    public abstract AnnotationProcessor getAnnotatonProcessor(ExternalContext externalContext);

    public abstract void release();

}
