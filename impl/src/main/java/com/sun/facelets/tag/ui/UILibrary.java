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

package com.sun.facelets.tag.ui;

import com.sun.facelets.component.UIRepeat;
import com.sun.facelets.tag.AbstractTagLibrary;

/**
 * @author Jacob Hookom
 * @version $Id: UILibrary.java,v 1.8 2008/07/13 19:01:42 rlubke Exp $
 */
public final class UILibrary extends AbstractTagLibrary
{

    public final static String Namespace = "http://java.sun.com/jsf/facelets";

    public final static UILibrary Instance = new UILibrary();

    public UILibrary()
    {
        super(Namespace);

        this.addTagHandler("include", IncludeHandler.class);

        this.addTagHandler("composition", CompositionHandler.class);

        this.addComponent("component", ComponentRef.COMPONENT_TYPE, null, ComponentRefHandler.class);

        this.addComponent("fragment", ComponentRef.COMPONENT_TYPE, null, ComponentRefHandler.class);

        this.addTagHandler("define", DefineHandler.class);

        this.addTagHandler("insert", InsertHandler.class);

        this.addTagHandler("param", ParamHandler.class);

        this.addTagHandler("decorate", DecorateHandler.class);

        this.addComponent("repeat", UIRepeat.COMPONENT_TYPE, null, RepeatHandler.class);

        this.addComponent("debug", UIDebug.COMPONENT_TYPE, null);
    }
}
