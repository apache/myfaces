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
package org.apache.myfaces.core.extensions.quarkus.deployment;

import java.util.UUID;

import jakarta.faces.annotation.ManagedProperty;

import org.jboss.jandex.AnnotationInstance;
import org.jboss.jandex.AnnotationValue;
import org.jboss.jandex.DotName;
import org.jboss.jandex.Type;

import io.quarkus.arc.deployment.BeanRegistrationPhaseBuildItem;
import io.quarkus.arc.processor.BuildExtension;
import io.quarkus.arc.processor.BuiltinScope;
import io.quarkus.arc.processor.InjectionPointInfo;
import io.quarkus.deployment.annotations.BuildProducer;
import org.apache.myfaces.core.extensions.quarkus.runtime.producer.ManagedPropertyBeanCreator;

public class ManagedPropertyBuildStep
{

    public static void build(BeanRegistrationPhaseBuildItem beanRegistrationPhase,
            BuildProducer<BeanRegistrationPhaseBuildItem.BeanConfiguratorBuildItem> beanConfigurators)
    {
        for (InjectionPointInfo injectionPoint : beanRegistrationPhase.getContext()
                .get(BuildExtension.Key.INJECTION_POINTS))
        {
            if (injectionPoint.hasDefaultedQualifier())
            {
                // Defaulted qualifier means no @ManagedProperty
                continue;
            }

            AnnotationInstance managedProperty = injectionPoint.getRequiredQualifier(
                    DotName.createSimple(ManagedProperty.class.getName()));
            if (managedProperty != null)
            {
                AnnotationValue value = managedProperty.value("value");
                if (value == null)
                {
                    continue;
                }

                Type requiredType = injectionPoint.getRequiredType();

                beanConfigurators.produce(new BeanRegistrationPhaseBuildItem.BeanConfiguratorBuildItem(
                        beanRegistrationPhase.getContext()
                                .configure(requiredType.name())
                                .qualifiers(managedProperty)
                                .scope(BuiltinScope.DEPENDENT.getInfo())
                                .types(requiredType)
                                .creator(ManagedPropertyBeanCreator.class)
                                .name(UUID.randomUUID().toString().replace("-", ""))
                                .defaultBean()
                                .param(ManagedPropertyBeanCreator.EXPRESSION, value.asString())));
            }
        }
    }
}
