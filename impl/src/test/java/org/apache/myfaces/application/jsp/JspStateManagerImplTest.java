/*
 * Copyright 2004-2006 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.myfaces.application.jsp;

//import org.apache.myfaces.shared_impl.config.MyfacesConfig;

import java.io.BufferedWriter;
import java.io.CharArrayWriter;

import javax.faces.application.StateManager;
import javax.faces.component.UIOutput;
import javax.faces.component.UIViewRoot;
import javax.faces.render.RenderKitFactory;

import org.apache.myfaces.test.base.AbstractJsfTestCase;
import org.apache.myfaces.test.mock.MockResponseWriter;

public class JspStateManagerImplTest extends AbstractJsfTestCase {

    public static void main(String[] args) {
        junit.textui.TestRunner.run(JspStateManagerImplTest.class);
    }

    public JspStateManagerImplTest(String name) {
        super(name);
    }

    /**
     * New test to address an issue uncovered through TCK testing.
     */
    public void testWriteAndRestoreState() throws Exception
    {
        // additional setup not provided automatically by the myfaces mock stuff
        facesContext.setResponseWriter(new MockResponseWriter(new BufferedWriter(new CharArrayWriter()), null, null));

        UIViewRoot viewRoot = facesContext.getViewRoot();
        viewRoot.setViewId("/root");
        StateManager stateManager = new JspStateManagerImpl();

        UIOutput output = new UIOutput();
        output.setValue("foo");
        output.setId("foo");

        stateManager.writeState(facesContext, stateManager.saveSerializedView(facesContext));

        UIViewRoot restoredViewRoot = stateManager.restoreView(facesContext, "/root", RenderKitFactory.HTML_BASIC_RENDER_KIT);
        assertNotNull("restored view root should not be null", restoredViewRoot);
    }

    public void testSaveInSessionWithoutSerialize() throws Exception
    {
        // create a fake viewRoot
        UIViewRoot root = new UIViewRoot();
        root.getAttributes().put("key", "value");

        // simulate server-side-state-saving without serialization
        Object state = root.saveState(facesContext);

        // restore view
        UIViewRoot root1 = new UIViewRoot();
        root1.restoreState(facesContext, state);

        // restore view .. next request
        UIViewRoot root2 = new UIViewRoot();
        root2.restoreState(facesContext, state);

        // chaange attribute in root1
        root1.getAttributes().put("key", "borken");

        // other values should not have been changed
        assertEquals("value", root2.getAttributes().get("key"));
        assertEquals("value", root.getAttributes().get("key"));
    }
}
