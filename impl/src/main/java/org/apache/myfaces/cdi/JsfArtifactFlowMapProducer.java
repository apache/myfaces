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
package org.apache.myfaces.cdi;

import java.lang.reflect.Type;
import java.util.Map;
import javax.enterprise.inject.spi.BeanManager;
import javax.enterprise.util.AnnotationLiteral;
import jakarta.faces.annotation.FlowMap;
import jakarta.faces.context.FacesContext;
import jakarta.faces.flow.FlowScoped;
import org.apache.myfaces.cdi.util.AbstractDynamicProducer;
import org.apache.myfaces.cdi.util.ParameterizedTypeImpl;

public class JsfArtifactFlowMapProducer extends AbstractDynamicProducer
{
    class FlowMapAnnotationLiteral extends AnnotationLiteral<FlowMap> implements FlowMap
    {
        private static final long serialVersionUID = -8623640277155878657L;
    }
    
    public JsfArtifactFlowMapProducer(BeanManager beanManager)
    {
        super(beanManager);
        
        super.name("flowScope")
                .scope(FlowScoped.class)
                .qualifiers(new FlowMapAnnotationLiteral())
                .types(new ParameterizedTypeImpl(Map.class, new Type[] { Object.class, Object.class }),
                        Map.class,
                        Object.class)
                .beanClass(Map.class)
                .create(e -> FacesContext.getCurrentInstance().getApplication().getFlowHandler().getCurrentFlowScope());
    }
}
