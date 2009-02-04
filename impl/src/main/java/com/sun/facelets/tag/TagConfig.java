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

package com.sun.facelets.tag;

import javax.faces.webapp.pdl.facelets.FaceletHandler;

/**
 * Passed to the constructor of TagHandler, it defines the document definition of the handler we are instantiating
 * 
 * @see com.sun.facelets.tag.TagHandler
 * @author Jacob Hookom
 * @version $Id: TagConfig.java,v 1.3 2008/07/13 19:01:35 rlubke Exp $
 */
public interface TagConfig
{

    /**
     * A Tag representing this handler
     * 
     * @return a tag representing this handler
     */
    public Tag getTag();

    /**
     * The next FaceletHandler (child or children) to be applied
     * 
     * @return next FaceletHandler, never null
     */
    public FaceletHandler getNextHandler();

    /**
     * A document-unique id, follows the convention "_tagId##"
     * 
     * @return a document-unique id
     */
    public String getTagId();
}
