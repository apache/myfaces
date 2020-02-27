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

package org.apache.myfaces.test.mock;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import jakarta.faces.context.FacesContext;
import jakarta.faces.render.RenderKit;
import jakarta.faces.render.RenderKitFactory;

/**
 * <p>Mock implementation of <code>RenderKitFactory</code>.</p>
 *
 * $Id$
 * @since 1.0.0
 */

public class MockRenderKitFactory extends RenderKitFactory
{

    // ------------------------------------------------------------ Constructors

    /**
     * <p>Return a default instance.</p>
     */
    public MockRenderKitFactory()
    {

        renderKits = new HashMap();

    }

    // ----------------------------------------------------- Mock Object Methods

    // ------------------------------------------------------ Instance Variables

    /**
     * <p>The set of render kits that have been registered here.</p>
     */
    private Map renderKits = new HashMap();

    // ------------------------------------------------ RenderKitFactory Methods

    /** {@inheritDoc} */
    public void addRenderKit(String renderKitId, RenderKit renderKit)
    {

        if ((renderKitId == null) || (renderKit == null))
        {
            throw new NullPointerException();
        }
        if (renderKits.containsKey(renderKitId))
        {
            throw new IllegalArgumentException(renderKitId);
        }
        renderKits.put(renderKitId, renderKit);

    }

    /** {@inheritDoc} */
    public RenderKit getRenderKit(FacesContext context, String renderKitId)
    {

        if (renderKitId == null)
        {
            throw new NullPointerException();
        }
        RenderKit renderKit = (RenderKit) renderKits.get(renderKitId);
        if (renderKit == null)
        {
            // Issue 38294 -- We removed the automatic creation of the
            // default renderkit in the constructor, allowing it to be
            // added by AbstractJsfTestCase in the usual case.  To preserve
            // backwards compatibility, however, create one on the fly
            // if the user asks for the default HTML renderkit and it has
            // not been manually added yet
            if (RenderKitFactory.HTML_BASIC_RENDER_KIT.equals(renderKitId))
            {
                renderKit = new MockRenderKit();
                renderKits.put(RenderKitFactory.HTML_BASIC_RENDER_KIT,
                        renderKit);
                return renderKit;
            }
            throw new IllegalArgumentException(renderKitId);
        }
        return renderKit;

    }

    /** {@inheritDoc} */
    public Iterator getRenderKitIds()
    {

        return renderKits.keySet().iterator();

    }

}
