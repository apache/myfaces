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

package com.sun.facelets.tag.jstl.core;

import com.sun.facelets.tag.AbstractTagLibrary;

/**
 * @author Jacob Hookom
 * @version $Id: JstlCoreLibrary.java,v 1.5 2008/07/13 19:01:43 rlubke Exp $
 */
public final class JstlCoreLibrary extends AbstractTagLibrary
{

    public final static String Namespace = "http://java.sun.com/jstl/core";

    public final static JstlCoreLibrary Instance = new JstlCoreLibrary();

    public JstlCoreLibrary()
    {
        super(Namespace);

        this.addTagHandler("if", IfHandler.class);

        this.addTagHandler("forEach", ForEachHandler.class);

        this.addTagHandler("catch", CatchHandler.class);

        this.addTagHandler("choose", ChooseHandler.class);

        this.addTagHandler("when", ChooseWhenHandler.class);

        this.addTagHandler("otherwise", ChooseOtherwiseHandler.class);

        this.addTagHandler("set", SetHandler.class);
    }

}
