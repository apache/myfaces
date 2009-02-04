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

package com.sun.facelets.tag.jsf.html;

import com.sun.facelets.tag.AbstractTagLibrary;

/**
 * @author Jacob Hookom
 * @version $Id: AbstractHtmlLibrary.java,v 1.3 2008/07/13 19:01:50 rlubke Exp $
 */
public abstract class AbstractHtmlLibrary extends AbstractTagLibrary
{

    /**
     * @param namespace
     */
    public AbstractHtmlLibrary(String namespace)
    {
        super(namespace);
    }

    public void addHtmlComponent(String name, String componentType, String rendererType)
    {
        super.addComponent(name, componentType, rendererType, HtmlComponentHandler.class);
    }

}
