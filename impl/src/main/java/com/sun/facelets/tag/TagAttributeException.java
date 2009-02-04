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

import javax.faces.webapp.pdl.facelets.FaceletException;

/**
 * An Exception caused by a TagAttribute
 * 
 * @author Jacob Hookom
 * @version $Id: TagAttributeException.java,v 1.4 2008/07/13 19:01:36 rlubke Exp $
 */
public final class TagAttributeException extends FaceletException
{

    /**
     * 
     */
    private static final long serialVersionUID = 1L;

    public TagAttributeException(TagAttribute attr)
    {
        super(attr.toString());
    }

    public TagAttributeException(TagAttribute attr, String message)
    {
        super(attr + " " + message);
    }

    public TagAttributeException(TagAttribute attr, Throwable cause)
    {
        super(attr + " " + cause.getMessage(), cause);
    }

    public TagAttributeException(TagAttribute attr, String message, Throwable cause)
    {
        super(attr + " " + message, cause);
    }

    /**
     * 
     */
    public TagAttributeException(Tag tag, TagAttribute attr)
    {
        super(print(tag, attr));
    }

    private final static String print(Tag tag, TagAttribute attr)
    {
        return tag.getLocation() + " <" + tag.getQName() + " " + attr.getQName() + "=\"" + attr.getValue() + "\">";
    }

    /**
     * @param message
     */
    public TagAttributeException(Tag tag, TagAttribute attr, String message)
    {
        super(print(tag, attr) + " " + message);
    }

    /**
     * @param cause
     */
    public TagAttributeException(Tag tag, TagAttribute attr, Throwable cause)
    {
        super(print(tag, attr) + " " + cause.getMessage(), cause);
    }

    /**
     * @param message
     * @param cause
     */
    public TagAttributeException(Tag tag, TagAttribute attr, String message, Throwable cause)
    {
        super(print(tag, attr) + " " + message, cause);
    }

}
