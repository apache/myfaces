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

package javax.faces.component;

import javax.faces.el.ValueBinding;

import junit.framework.Test;

import org.apache.shale.test.base.AbstractJsfTestCase;
import org.apache.shale.test.mock.MockRenderKitFactory;
import org.apache.shale.test.mock.MockValueBinding;

public class UIComponentBaseTest extends AbstractJsfTestCase
{
	private UIComponentBase mock = null;
     
	public static void main(String[] args) {
		junit.textui.TestRunner.run(UIComponentBaseTest.class);
	}

	public UIComponentBaseTest(String name) {
		super(name);
	}

	public static Test suite() {
		return null; // keep this method or maven won't run it
	}

	public void setUp() {
		super.setUp();
        // TODO remove this line once shale-test goes alpha, see MYFACES-1155
		facesContext.getViewRoot().setRenderKitId(MockRenderKitFactory.HTML_BASIC_RENDER_KIT);
		mock = new UIComponentMock();
	}

	public void tearDown() {
		super.tearDown();
		mock = null;
	}

	/*
	 * Test method for 'javax.faces.component.UIComponentBase.isRendered()'
	 */
	public void testIsRendered() {
		// defaults to true
		assertTrue(mock.isRendered());
	}

	public void testIsRenderedValueSet() {
		mock.setRendered(true);
		assertTrue(mock.isRendered());
		mock.setRendered(false);
		assertFalse(mock.isRendered());
	}

	public void testIsRenderedBinding() {

		ValueBinding vb = new MockValueBinding(application,
				"#{requestScope.foo}");
		externalContext.getRequestMap().put("foo", new Boolean(false));

		mock.setValueBinding("rendered", vb);
		assertFalse(mock.isRendered());
	}

	/*
	 * Test method for 'javax.faces.component.UIComponentBase.getRendersChildren()'
	 */
	public void testGetRendersChildren() {

	}

	/*
	 * Test method for 'javax.faces.component.UIComponentBase.getChildCount()'
	 */
	public void testGetChildCount() {

	}

	/*
	 * Test method for 'javax.faces.component.UIComponentBase.UIComponentBase()'
	 */
	public void testUIComponentBase() {

	}

	/*
	 * Test method for 'javax.faces.component.UIComponentBase.getAttributes()'
	 */
	public void testGetAttributes() {

	}

	/*
	 * Test method for 'javax.faces.component.UIComponentBase.getValueBinding(String)'
	 */
	public void testGetValueBindingString() {

	}

	/*
	 * Test method for 'javax.faces.component.UIComponentBase.setValueBinding(String, ValueBinding)'
	 */
	public void testSetValueBindingStringValueBinding() {

	}

	/*
	 * Test method for 'javax.faces.component.UIComponentBase.getClientId(FacesContext)'
	 */
	public void testGetClientIdFacesContext() {

		UIInput input = createInputInTree();
        
		String str = input.getClientId(facesContext);

		assertEquals(str, "data:input");

		UIData uiData = (UIData) input.getParent().getParent();

		uiData.setRowIndex(1);

		str = input.getClientId(facesContext);
	}

	private UIInput createInputInTree() {
		UIViewRoot viewRoot = facesContext.getViewRoot();
		viewRoot.setId("root");

		UIData uiData = new UIData();
		uiData.setId("data");

		UIColumn column = new UIColumn();

		uiData.getChildren().add(column);

		UIInput input = new UIInput();
		input.setId("input");

		column.getChildren().add(input);

		viewRoot.getChildren().add(uiData);

		return input;
	}

	/*
	 * Test method for 'javax.faces.component.UIComponentBase.getId()'
	 */
	public void testGetId() {

	}

	/*
	 * Test method for 'javax.faces.component.UIComponentBase.setId(String)'
	 */
	public void testSetIdString() {

	}

	/*
	 * Test method for 'javax.faces.component.UIComponentBase.getParent()'
	 */
	public void testGetParent() {

	}

	/*
	 * Test method for 'javax.faces.component.UIComponentBase.setParent(UIComponent)'
	 */
	public void testSetParentUIComponent() {

	}

	/*
	 * Test method for 'javax.faces.component.UIComponentBase.getChildren()'
	 */
	public void testGetChildren() {

	}

	/*
	 * Test method for 'javax.faces.component.UIComponentBase.findComponent(String)'
	 */
	public void testFindComponentString() {

		UIInput input = createInputInTree();

		UIComponent comp = input.findComponent(":data:input");

		assertEquals(input, comp);

		comp = input.findComponent("input");

		assertEquals(input, comp);

	}

	/*
	 * Test method for 'javax.faces.component.UIComponentBase.getFacets()'
	 */
	public void testGetFacets() {

	}

	/*
	 * Test method for 'javax.faces.component.UIComponentBase.getFacet(String)'
	 */
	public void testGetFacetString() {

	}

	/*
	 * Test method for 'javax.faces.component.UIComponentBase.getFacetsAndChildren()'
	 */
	public void testGetFacetsAndChildren() {

	}

	/*
	 * Test method for 'javax.faces.component.UIComponentBase.broadcast(FacesEvent)'
	 */
	public void testBroadcastFacesEvent() {

	}

	/*
	 * Test method for 'javax.faces.component.UIComponentBase.decode(FacesContext)'
	 */
	public void testDecodeFacesContext() {

	}

	/*
	 * Test method for 'javax.faces.component.UIComponentBase.encodeBegin(FacesContext)'
	 */
	public void testEncodeBeginFacesContext() {

	}

	/*
	 * Test method for 'javax.faces.component.UIComponentBase.encodeChildren(FacesContext)'
	 */
	public void testEncodeChildrenFacesContext() {

	}

	/*
	 * Test method for 'javax.faces.component.UIComponentBase.encodeEnd(FacesContext)'
	 */
	public void testEncodeEndFacesContext() {

	}

	/*
	 * Test method for 'javax.faces.component.UIComponentBase.addFacesListener(FacesListener)'
	 */
	public void testAddFacesListenerFacesListener() {

	}

	/*
	 * Test method for 'javax.faces.component.UIComponentBase.getFacesListeners(Class)'
	 */
	public void testGetFacesListenersClass() {

	}

	/*
	 * Test method for 'javax.faces.component.UIComponentBase.removeFacesListener(FacesListener)'
	 */
	public void testRemoveFacesListenerFacesListener() {

	}

	/*
	 * Test method for 'javax.faces.component.UIComponentBase.queueEvent(FacesEvent)'
	 */
	public void testQueueEventFacesEvent() {

	}

	/*
	 * Test method for 'javax.faces.component.UIComponentBase.processDecodes(FacesContext)'
	 */
	public void testProcessDecodesFacesContext() {

	}

	/*
	 * Test method for 'javax.faces.component.UIComponentBase.processValidators(FacesContext)'
	 */
	public void testProcessValidatorsFacesContext() {

	}

	/*
	 * Test method for 'javax.faces.component.UIComponentBase.processUpdates(FacesContext)'
	 */
	public void testProcessUpdatesFacesContext() {

	}

	/*
	 * Test method for 'javax.faces.component.UIComponentBase.processSaveState(FacesContext)'
	 */
	public void testProcessSaveStateFacesContext() {

	}

	/*
	 * Test method for 'javax.faces.component.UIComponentBase.processRestoreState(FacesContext, Object)'
	 */
	public void testProcessRestoreStateFacesContextObject() {

	}

	/*
	 * Test method for 'javax.faces.component.UIComponentBase.getFacesContext()'
	 */
	public void testGetFacesContext() {

	}

	/*
	 * Test method for 'javax.faces.component.UIComponentBase.getRenderer(FacesContext)'
	 */
	public void testGetRendererFacesContext() {

	}

	/*
	 * Test method for 'javax.faces.component.UIComponentBase.getPathToComponent(UIComponent)'
	 */
	public void testGetPathToComponent() {

	}

	/*
	 * Test method for 'javax.faces.component.UIComponentBase.isTransient()'
	 */
	public void testIsTransient() {

	}

	/*
	 * Test method for 'javax.faces.component.UIComponentBase.setTransient(boolean)'
	 */
	public void testSetTransient() {

	}

	/*
	 * Test method for 'javax.faces.component.UIComponentBase.saveAttachedState(FacesContext, Object)'
	 */
	public void testSaveAttachedState() {

	}

	/*
	 * Test method for 'javax.faces.component.UIComponentBase.restoreAttachedState(FacesContext, Object)'
	 */
	public void testRestoreAttachedState() {

	}

	public void testSaveState() throws Exception {

		try {
			String id = "id";
			String rendererType = "Whumpy";
			mock.setId(id);
			mock.setRendered(true);
			mock.setRendererType(rendererType);
			Object value[] = (Object[]) mock.saveState(facesContext);
			assertEquals(id, value[0]);
			assertEquals(Boolean.TRUE, value[1]);
			assertEquals(rendererType, value[2]);

			assertNull(value[3]);
			assertNull(value[4]);
			assertNull(value[5]);
			assertNull(value[6]);
		} catch (NullPointerException e) {
			fail("Should not throw an exception");
		}
	}

	/*
	 * Test method for 'javax.faces.component.UIComponentBase.restoreState(FacesContext, Object)'
	 */
	public void testRestoreState() {

	}

	/*
	 * Test method for 'javax.faces.component.UIComponentBase.setRendererType(String)'
	 */
	public void testSetRendererType() {
		assertNull(mock.getRendererType());
	}

	public void testSetRendererTypeStringValue() {
		String rendererType = "BlueBlorf";
		mock.setRendererType(rendererType);
		assertEquals(mock.getRendererType(), rendererType);
	}

	public void testSetRendererTypeStringBinding() {

		String whumpy = "Whumpy";

		ValueBinding vb = new MockValueBinding(application,
				"#{requestScope.foo}");
		externalContext.getRequestMap().put("foo", whumpy);

		mock.setValueBinding("rendererType", vb);
		assertEquals(mock.getRendererType(), whumpy);

	}

}
