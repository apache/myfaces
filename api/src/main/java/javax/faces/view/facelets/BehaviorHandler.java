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
package javax.faces.view.facelets;

import javax.faces.view.BehaviorHolderAttachedObjectHandler;

/**
 * @author Simon Lessard (latest modification by $Author: slessard $)
 * @version $Revision: 696523 $ $Date: 2009-03-15 17:28:58 -0400 (mer., 17 sept. 2008) $
 *
 * @since 2.0
 */
public class BehaviorHandler extends FaceletsAttachedObjectHandler implements BehaviorHolderAttachedObjectHandler
{
    /**
     * @param config
     */
    public BehaviorHandler(TagConfig config)
    {
        super(config);
        // TODO IMPLEMENT API
    }
    
    public String getBehaviorId()
    {
        // TODO IMPLEMENT API
        return null;
    }
    
    public TagAttribute getEvent()
    {
        // TODO IMPLEMENT API
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected TagHandlerDelegate getTagHandlerHelper()
    {
        // TODO IMPLEMENT API
        return null;
    }
}
