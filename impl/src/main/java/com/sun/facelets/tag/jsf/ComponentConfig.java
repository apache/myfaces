/**
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.sun.facelets.tag.jsf;

import com.sun.facelets.tag.TagConfig;

/**
 * Used in creating AbstractComponentHandler's and all implementations.
 * 
 * @see com.sun.facelets.tag.AbstractComponentHandler
 * @see com.sun.facelets.tag.jsf.ComponentHandler
 * @author Jacob Hookom
 * @version $Id: ComponentConfig.java,v 1.3 2008/07/13 19:01:46 rlubke Exp $
 */
public interface ComponentConfig extends TagConfig
{
    /**
     * ComponentType to pass to the Application. Cannot be null.
     * 
     * @return ComponentType to pass to the Application. Cannot be null.
     */
    public String getComponentType();

    /**
     * RendererType to set on created UIComponent instances.
     * 
     * @return RendererType to set on created UIComponent instances
     */
    public String getRendererType();
}
