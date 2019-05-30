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

package org.apache.myfaces.cdi.validator;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.Typed;
import javax.enterprise.inject.spi.BeanManager;
import javax.faces.FacesException;
import javax.faces.validator.Validator;
import org.apache.myfaces.cdi.util.AbstractDynamicProducer;
import org.apache.myfaces.util.ClassUtils;

@ApplicationScoped
@Typed
public class FacesValidatorProducer extends AbstractDynamicProducer<Validator>
{
    public FacesValidatorProducer(BeanManager beanManager, FacesValidatorInfo typeInfo)
    {
        super(beanManager);
        
        String validatorId = typeInfo.getValidatorId() == null ? "" : typeInfo.getValidatorId();
        String id = "" + typeInfo.getType() + '_' + validatorId;

        FacesValidatorAnnotationLiteral literal = new FacesValidatorAnnotationLiteral(
                        typeInfo.getValidatorId() == null ? "" : typeInfo.getValidatorId(), false, true);

        super.id(id)
                .scope(Dependent.class)
                .qualifiers(literal)
                .types(typeInfo.getType(), Object.class)
                .beanClass(ClassUtils.simpleClassForName(typeInfo.getType().getTypeName()))
                .create(e -> createValidator(e));
    }

    protected Validator createValidator(CreationalContext<Validator> cc)
    {
        Class<? extends Validator> converterClass = (Class<? extends Validator>) getBeanClass();        
        Validator converter = null;
        try
        {
            converter = converterClass.newInstance();
        }
        catch (Exception ex)
        {
            Logger.getLogger(FacesValidatorProducer.class.getName()).log(
                    Level.SEVERE, "Could not instantiate converter " + converterClass.getName(), ex);
            throw new FacesException("Could not instantiate converter: " + converterClass.getName(), ex);
            
        }
        return converter;
    }

}
