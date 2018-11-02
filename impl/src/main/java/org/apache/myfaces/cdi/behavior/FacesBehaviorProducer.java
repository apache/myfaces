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

package org.apache.myfaces.cdi.behavior;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.context.Dependent;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.Typed;
import javax.enterprise.inject.spi.BeanManager;
import javax.faces.FacesException;
import javax.faces.component.behavior.Behavior;
import org.apache.myfaces.cdi.util.AbstractDynamicProducer;
import org.apache.myfaces.shared.util.ClassUtils;

@Typed
public class FacesBehaviorProducer extends AbstractDynamicProducer<Behavior>
{
    public FacesBehaviorProducer(BeanManager beanManager, FacesBehaviorInfo typeInfo)
    {
        super(beanManager);
        
        FacesBehaviorAnnotationLiteral literal = new FacesBehaviorAnnotationLiteral(
                        typeInfo.getBehaviorId() == null ? "" : typeInfo.getBehaviorId(), true);
        
        String behaviorId = typeInfo.getBehaviorId() == null ? "" : typeInfo.getBehaviorId();
        String id = "" + typeInfo.getType() + '_' + behaviorId;

        super.id(id)
                .scope(Dependent.class)
                .qualifiers(literal)
                .types(typeInfo.getType(), Object.class)
                .beanClass(ClassUtils.simpleClassForName(typeInfo.getType().getTypeName()))
                .create(e -> createBehavior(e));
    }

    protected Behavior createBehavior(CreationalContext<Behavior> cc)
    {
        Class<? extends Behavior> behaviorClass = (Class<? extends Behavior>) getBeanClass();        
        Behavior converter = null;
        try
        {
            converter = behaviorClass.newInstance();
        }
        catch (Exception ex)
        {
            Logger.getLogger(FacesBehaviorProducer.class.getName()).log(
                    Level.SEVERE, "Could not instantiate behavior " + behaviorClass.getName(), ex);
            throw new FacesException("Could not instantiate behavior: " + behaviorClass.getName(), ex);
            
        }
        return converter;
    }

}
