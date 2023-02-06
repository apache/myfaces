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
package org.apache.myfaces.view.facelets.tag.composite.localized;

import org.apache.myfaces.test.core.AbstractMyFacesRequestTestCase;
import org.apache.myfaces.test.core.annotation.DeclareFacesConfig;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

/**
 * Check if a composite component can be localized following the rules
 * of Faces 2.0 Resource Handler API that are used for css, javascript
 * and other resources.
 * 
 * @author Leonardo Uribe
 *
 */
@DeclareFacesConfig("/faces-config.xml")
public class LocalizedCompositeComponentTestCase extends AbstractMyFacesRequestTestCase
{
    // This is a nice example that shows how AbstractMyFacesRequestTestCase 
    // could be be useful.
    // This test requires the default ResourceHandler algorithm and involves
    // create multiple views. So, for this test use FaceletTestCase just does
    // not fit well and it is more easy to use a test case than setup and
    // teardown all MyFaces container. 
    
    @Test
    public void testNoLocaleCompositeComponent() throws Exception
    {
        startViewRequest("/testNoLocalizedComposite.xhtml");
        processLifecycleExecuteAndRender();
        Assertions.assertTrue(getRenderedContent().contains(
                "English page fragment"));
        endRequest();
    }
    
    @Test
    public void testSpanishLocaleCompositeComponent() throws Exception
    {
        startViewRequest("/testSpanishLocalizedComposite.xhtml");
        processLifecycleExecuteAndRender();
        Assertions.assertTrue(getRenderedContent().contains(
                "Fragmento de pagina Espanol"));
        endRequest();
    }
    
    @Test
    public void testGermanLocaleCompositeComponent() throws Exception
    {
        startViewRequest("/testGermanLocalizedComposite.xhtml");
        processLifecycleExecuteAndRender();
        Assertions.assertTrue(getRenderedContent().contains(
                "Deutsches Seitenfragment"));
        endRequest();
    }
}
