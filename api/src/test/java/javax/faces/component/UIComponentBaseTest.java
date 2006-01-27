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

import javax.faces.context.FacesContext;
import javax.faces.el.ValueBinding;
import javax.faces.FactoryFinder;
import org.apache.myfaces.mock.api.MockApplicationFactory;
import org.apache.myfaces.mock.api.MockRenderKitFactory;

import org.apache.myfaces.AbstractTestCase;
import org.apache.myfaces.mock.api.MockFacesContextHelper;
import org.apache.myfaces.mock.api.MockFacesContext;
import org.easymock.MockControl;
import org.easymock.classextension.MockClassControl;

public class UIComponentBaseTest extends AbstractTestCase {
  UIComponentBase mock = null;
  public static void main(String[] args) {
    junit.textui.TestRunner.run(UIComponentBaseTest.class);
  }

  public UIComponentBaseTest(String name) {
    super(name);
  }

  protected void setUp() throws Exception {
    super.setUp();
    
    mock = new UIComponentMock();
  }

  protected void tearDown() throws Exception {
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
    MockControl bindingControl = MockClassControl.createControl(ValueBinding.class);
    ValueBinding mockBinding = (ValueBinding)bindingControl.getMock();
    mockBinding.getValue(null);
    bindingControl.setReturnValue(Boolean.FALSE);
    bindingControl.replay();
    mock.setValueBinding("rendered", mockBinding);
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

      FacesContext context = new MockFacesContext();

      UIInput input = createInputInTree(context);

      String str = input.getClientId(context);

      assertEquals(str, "data:input");

      UIData uiData = (UIData) input.getParent().getParent();

      uiData.setRowIndex(1);

      str = input.getClientId(context);
  }

    private UIInput createInputInTree(FacesContext context)
    {
        UIViewRoot viewRoot = new UIViewRoot();
        viewRoot.setId("root");

        UIData uiData = new UIData();
        uiData.setId("data");

        UIColumn column = new UIColumn();

        uiData.getChildren().add(column);

        UIInput input = new UIInput();
        input.setId("input");

        column.getChildren().add(input);

        viewRoot.getChildren().add(uiData);

        FactoryFinder.setFactory(FactoryFinder.APPLICATION_FACTORY,
          MockApplicationFactory.class.getName());

        FactoryFinder.setFactory(FactoryFinder.RENDER_KIT_FACTORY,
          MockRenderKitFactory.class.getName());

        context.setViewRoot(viewRoot);

        MockFacesContextHelper.setCurrentInstance(context);
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
      FacesContext context = new MockFacesContext();

      UIInput input = createInputInTree(context);

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
    MockControl contextControl = MockClassControl.createControl(FacesContext.class);
    FacesContext facesContext = (FacesContext)contextControl.getMock();
    contextControl.replay();
    try {
      String id = "id";
      String rendererType = "Whumpy";
      mock.setId(id);
      mock.setRendered(true);
      mock.setRendererType(rendererType);
      Object value[] = (Object[])mock.saveState(facesContext);
      assertEquals(id, value[0]);
      assertEquals(Boolean.TRUE, value[1]);
      assertEquals(rendererType, value[2]);
      // the object 'mock' here, must be the same before and after saveState
      // calling getClientId in the saveState method would change the state
      // so it must be null if getClientId has not been called
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
    MockControl bindingControl = MockClassControl.createControl(ValueBinding.class);
    ValueBinding mockBinding = (ValueBinding)bindingControl.getMock();
    mockBinding.getValue(null);
    String whumpy = "Whumpy";
    bindingControl.setReturnValue(whumpy);
    bindingControl.replay();
    mock.setValueBinding("rendererType", mockBinding);
    assertEquals(mock.getRendererType(), whumpy);
  }

}
