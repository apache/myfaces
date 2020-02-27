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
package jakarta.faces.flow;

import jakarta.faces.FacesWrapper;
import jakarta.faces.context.FacesContext;

/**
 * @since 2.2
 */
public abstract class FlowHandlerFactoryWrapper extends FlowHandlerFactory
    implements FacesWrapper<FlowHandlerFactory>
{
    private FlowHandlerFactory delegate;

    @Deprecated
    public FlowHandlerFactoryWrapper()
    {
    }

    public FlowHandlerFactoryWrapper(FlowHandlerFactory delegate)
    {
        this.delegate = delegate;
    }
    
    public FlowHandler createFlowHandler(FacesContext context)
    {
        return getWrapped().createFlowHandler(context);
    }
    
    public FlowHandlerFactory getWrapped()
    {
        return delegate;
    }
}
