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
package org.apache.myfaces.spi.impl;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.myfaces.spi.FacesFlowProvider;
import org.apache.myfaces.spi.FacesFlowProviderFactory;
import javax.faces.context.ExternalContext;
import org.apache.myfaces.flow.cdi.DefaultCDIFacesFlowProvider;
import org.apache.myfaces.flow.impl.DefaultFacesFlowProvider;
import org.apache.myfaces.spi.ServiceProviderFinderFactory;
import org.apache.myfaces.util.ClassUtils;
import org.apache.myfaces.util.ExternalSpecifications;

/**
 * @author Leonardo Uribe
 */
public class DefaultFacesFlowProviderFactory extends FacesFlowProviderFactory
{
    
    public static final String FACES_FLOW_PROVIDER = FacesFlowProvider.class.getName();
    public static final String FACES_FLOW_PROVIDER_INSTANCE_KEY = FACES_FLOW_PROVIDER + ".INSTANCE";

    @Override
    public FacesFlowProvider getFacesFlowProvider(ExternalContext externalContext)
    {
        // check for cached instance
        FacesFlowProvider returnValue = (FacesFlowProvider)
                externalContext.getApplicationMap().get(FACES_FLOW_PROVIDER_INSTANCE_KEY);

        if (returnValue == null)
        {
            try
            {
                returnValue = resolveFacesFlowProviderFromService(externalContext);
            }
            catch (Exception e)
            {
                getLogger().log(Level.SEVERE, "", e);
            }

            if (returnValue == null)
            {
                if (ExternalSpecifications.isCDIAvailable(externalContext))
                {
                    returnValue = (FacesFlowProvider) ClassUtils.newInstance(
                            DefaultCDIFacesFlowProvider.class.getName());
                }
                else
                {
                    returnValue = (FacesFlowProvider) new DefaultFacesFlowProvider();
                }
            }

            externalContext.getApplicationMap().put(FACES_FLOW_PROVIDER_INSTANCE_KEY, returnValue);
        }

        return returnValue;
    }

    
    private FacesFlowProvider resolveFacesFlowProviderFromService(ExternalContext externalContext) throws Exception
    {
        List<String> classList = ServiceProviderFinderFactory.getServiceProviderFinder(externalContext).
                    getServiceProviderList(FACES_FLOW_PROVIDER);
        return ClassUtils.buildApplicationObject(FacesFlowProvider.class, classList, null);
    }
    
    private Logger getLogger()
    {
        return Logger.getLogger(DefaultFacesFlowProviderFactory.class.getName());
    }
}
