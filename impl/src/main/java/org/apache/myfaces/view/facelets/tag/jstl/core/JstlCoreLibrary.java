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
package org.apache.myfaces.view.facelets.tag.jstl.core;

import org.apache.myfaces.view.facelets.tag.AbstractTagLibrary;

/**
 * @author Jacob Hookom
 * @version $Id$
 */
public final class JstlCoreLibrary extends AbstractTagLibrary
{
    public final static String NAMESPACE = "jakarta.tags.core";
    public final static String JCP_NAMESPACE = "http://xmlns.jcp.org/jsp/jstl/core";
    public final static String SUN_NAMESPACE = "http://java.sun.com/jsp/jstl/core";

    // This namespace was used in the early stages of Faces 2.0 development, but later
    // it was fixed to use the same syntax for jsp. But some applications still
    // uses the old syntax and it is too hard to fix them, so in Faces 2.2 it was 
    // decided to add this namespace as an alternative synonym.
    public final static String ALTERNATIVE_NAMESPACE = "http://java.sun.com/jstl/core";

    public final static JstlCoreLibrary INSTANCE = new JstlCoreLibrary();

    public JstlCoreLibrary()
    {
        super(NAMESPACE, JCP_NAMESPACE, SUN_NAMESPACE);

        this.addTagHandler("if", IfHandler.class);

        this.addTagHandler("forEach", ForEachHandler.class);

        this.addTagHandler("catch", CatchHandler.class);

        this.addTagHandler("choose", ChooseHandler.class);

        this.addTagHandler("when", ChooseWhenHandler.class);

        this.addTagHandler("otherwise", ChooseOtherwiseHandler.class);

        this.addTagHandler("set", SetHandler.class);
    }
    
    public JstlCoreLibrary(String namespace)
    {
        super(namespace);

        this.addTagHandler("if", IfHandler.class);

        this.addTagHandler("forEach", ForEachHandler.class);

        this.addTagHandler("catch", CatchHandler.class);

        this.addTagHandler("choose", ChooseHandler.class);

        this.addTagHandler("when", ChooseWhenHandler.class);

        this.addTagHandler("otherwise", ChooseOtherwiseHandler.class);

        this.addTagHandler("set", SetHandler.class);
    }
}
