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

package org.apache.myfaces.cdi.converter;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.context.Dependent;
import javax.enterprise.context.spi.CreationalContext;
import javax.enterprise.inject.Typed;
import javax.enterprise.inject.spi.BeanManager;
import javax.faces.FacesException;
import javax.faces.convert.Converter;
import org.apache.myfaces.cdi.util.AbstractDynamicProducer;
import org.apache.myfaces.util.lang.ClassUtils;

@ApplicationScoped
@Typed
public class FacesConverterProducer extends AbstractDynamicProducer<Converter>
{
    public FacesConverterProducer(BeanManager beanManager, FacesConverterInfo info)
    {
        super(beanManager);
        
        String forClass = info.getForClass() == null ? "" : 
                ((info.getForClass() == Object.class) ? "" : info.getForClass().getName());
        String converterId = info.getConverterId() == null ? "" : info.getConverterId();
        String id = "" + info.getType() + '_' + forClass + '_' + converterId;

        FacesConverterAnnotationLiteral literal = new FacesConverterAnnotationLiteral(
                        info.getForClass() == null ? Object.class : info.getForClass(), 
                        info.getConverterId() == null ? "" : info.getConverterId(), true);

        super.id(id)
                .scope(Dependent.class)
                .qualifiers(literal)
                .types(info.getType(), Object.class)
                .beanClass(ClassUtils.simpleClassForName(info.getType().getTypeName()))
                .create(e -> createConverter(e));
    }

    protected Converter createConverter(CreationalContext<Converter> cc)
    {
        Class<? extends Converter> converterClass = (Class<? extends Converter>) getBeanClass();        
        Converter converter = null;
        try
        {
            converter = converterClass.newInstance();
        }
        catch (Exception ex)
        {
            Logger.getLogger(FacesConverterProducer.class.getName()).log(
                    Level.SEVERE, "Could not instantiate converter " + converterClass.getName(), ex);
            throw new FacesException("Could not instantiate converter: " + converterClass.getName(), ex);
            
        }
        return converter;
    }
}
