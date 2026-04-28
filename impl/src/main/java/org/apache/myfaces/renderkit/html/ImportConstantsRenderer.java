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
package org.apache.myfaces.renderkit.html;

import jakarta.faces.component.UIImportConstants;
import jakarta.faces.event.AbortProcessingException;
import jakarta.faces.event.ComponentSystemEvent;
import jakarta.faces.event.ComponentSystemEventListener;
import jakarta.faces.event.ListenerFor;
import jakarta.faces.event.PostAddToViewEvent;
import jakarta.faces.render.Renderer;
import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFRenderer;
import org.apache.myfaces.util.ConstantsCollector;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * <b>ImportConstantsRenderer</b> is a class that runs the <code>f:importConstants</code>
 * which is declared outside the metadata facet.
 *
 * @see UIImportConstants
 * @since 5.0
 */
@JSFRenderer(renderKitId = "HTML_BASIC",
        family = "jakarta.faces.ImportConstants",
        type = "jakarta.faces.ImportConstants")
@ListenerFor(systemEventClass = PostAddToViewEvent.class)
public class ImportConstantsRenderer extends Renderer<UIImportConstants> implements ComponentSystemEventListener
{
    public static final String RENDERER_TYPE = "jakarta.faces.ImportConstants";

    /**
     * After adding component to view, run
     * {@link ViewMetadataImpl#importConstants(jakarta.faces.context.FacesContext, UIImportConstants)} immediately.
     * NOTE: when declared inside f:metadata, the component isn't added to the component tree,
     * so this renderer wouldn't run in first place, so no precheck needed.
     */
    @Override
    public void processEvent(ComponentSystemEvent event) throws AbortProcessingException
    {
        UIImportConstants uiImportConstants = (UIImportConstants) event.getComponent();
        Map<String, Object> applicationMap = event.getFacesContext().getExternalContext().getApplicationMap();

        String type = uiImportConstants.getType();

        if (!applicationMap.containsKey(type))
        {
            applicationMap.putIfAbsent(
                    constructVar(uiImportConstants),
                    ConstantsCollector.collectConstants(event.getFacesContext(), type));
        }
    }

    private static String constructVar(UIImportConstants uiImportConstants)
    {
        String var = uiImportConstants.getVar();
        String type = uiImportConstants.getType();
        if (var == null)
        {
            int innerClass = type.lastIndexOf('$');
            int outerClass = type.lastIndexOf('.');
            var = type.substring(Math.max(innerClass, outerClass) + 1);
        }
        return var;
    }

    public static Map<String, String> toVarTypeMap(Collection<UIImportConstants> uiImportConstants)
    {
        HashMap<String, String> importConstantsMap = new HashMap<>(uiImportConstants.size());
        for (UIImportConstants c : uiImportConstants)
        {
            String type = c.getType();
            importConstantsMap.put(constructVar(c), type);
        }
        return importConstantsMap;
    }
}