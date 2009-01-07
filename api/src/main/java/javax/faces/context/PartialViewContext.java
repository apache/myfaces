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
package javax.faces.context;

import java.util.List;
import java.util.Map;

/**
 * @author Simon Lessard (latest modification by $Author: slessard $)
 * @version $Revision: 696523 $ $Date: 2009-01-07 18:36:53 -0400 (mer., 17 sept. 2008) $
 *
 * @since 2.0
 */
public abstract class PartialViewContext
{
    public static final String NO_PARTIAL_PHASE_CLIENT_IDS = "none";
    public static final String PARTIAL_EXECUTE_PARAM_NAME = "javax.faces.partial.execute";
    public static final String PARTIAL_RENDER_PARAM_NAME = "javax.faces.partial.render";
    
    public abstract void enableResponseWriting(boolean enable);
    
    public abstract Map<Object,Object> getAttributes();
    
    public abstract List<String> getExecutePhaseClientIds();
    
    public abstract ResponseWriter getPartialResponseWriter();
    
    public abstract List<String> getRenderPhaseClientIds();
    
    public abstract boolean isAjaxRequest();
    
    public abstract boolean isExecuteNone();
    
    public abstract boolean isRenderAll();
    
    public abstract boolean isRenderNone();
    
    public abstract void release();
    
    public abstract void setExecutePhaseClientIds(List<String> executePhaseClientIds);
    
    public abstract void setRenderAll(boolean renderAll);
    
    public abstract void setRenderPhaseClientIds(List<String> renderPhaseClientIds);
}
