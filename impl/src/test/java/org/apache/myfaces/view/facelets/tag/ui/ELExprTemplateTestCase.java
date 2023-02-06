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

package org.apache.myfaces.view.facelets.tag.ui;

import jakarta.faces.component.UIViewRoot;

import org.apache.myfaces.config.webparameters.MyfacesConfig;
import org.apache.myfaces.view.facelets.FaceletTestCase;
import org.junit.jupiter.api.Test;

public class ELExprTemplateTestCase extends FaceletTestCase {

    
    
    @Override
    protected void setUpServletObjects() throws Exception
    {
        super.setUpServletObjects();
        
        servletContext.addInitParameter(
                MyfacesConfig.CACHE_EL_EXPRESSIONS,
                "always");
    }

    /**
     * See MYFACES-3246 for details
     * 
     * @throws Exception
     */
    @Test
    public void testELExprTemplate() throws Exception
    {
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root, "elexpr_main.xhtml");
    }

}
