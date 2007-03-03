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

import org.apache.AnnotationProcessor;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.apache.myfaces.shared_impl.util.ClassUtils;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;


public class AnnotationProcessorFactory
{
    private static Log log = LogFactory.getLog(AnnotationProcessorFactory.class);
    private static AnnotationProcessor annotationProcessor;

    public static AnnotationProcessor getAnnotatonProcessor()
    {

        if (annotationProcessor == null)
        {
            try
            {
                ClassUtils.classForName("javax.annotation.PreDestroy");
            }
            catch (ClassNotFoundException e)
            {
                // no annotation available don't process annotations
                annotationProcessor = new NopAnnotationProcessor();
                return annotationProcessor;
            }
            Context context;
            try
            {
                context = new InitialContext();
                try
                {
                    ClassUtils.classForName("javax.ejb.EJB");
                    // Asume full JEE 5 container
                    annotationProcessor = new AllAnnotationProcessor(context);
                }
                catch (ClassNotFoundException e)
                {
                    // something else
                    annotationProcessor = new ResourceAnnotationProcessor(context);
                }
            }
            catch (NamingException e)
            {
                // no initial context available no injection
                annotationProcessor = new NoInjectionAnnotationProcessor();
                log.error("No InitialContext found. Using NoInjectionAnnotationProcessor.", e);

            }
        }
        return annotationProcessor;
    }
}
