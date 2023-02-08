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
package jakarta.faces.component;

import java.util.ArrayList;
import java.util.List;

import org.apache.myfaces.test.base.junit.AbstractJsfTestCase;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class UIComponentBaseGetChildrenTest extends AbstractJsfTestCase
{
    @Test
    public void testGetChildrenAddAll()
    {
        UIInput input0 = new UIInput();
        UIInput input1 = new UIInput();
        UIInput input2 = new UIInput();
        UIInput input3 = new UIInput();
        
        //Set Ids to identify them
        input0.setId("input0");
        input1.setId("input1");
        input2.setId("input2");
        input3.setId("input3");
        
        UIPanel panel = new UIPanel();
        
        List<UIComponent> children = panel.getChildren();
        children.add(0, input0);
        children.add(input1);
        
        List<UIComponent> list = new ArrayList<UIComponent>();
        list.add(input2);
        list.add(input3);

        children.addAll(list);
        // Add again, it should be removed from last positions and
        // inserted to the first ones
        children.addAll(0, list);
        
        Assertions.assertEquals(4, children.size());
        Assertions.assertEquals(input2.getId(), children.get(0).getId());
        Assertions.assertEquals(input3.getId(), children.get(1).getId());
    }
    
    @Test
    public void testSimpleAddRemove()
    {
        UIInput input = new UIInput();
        input.setId("input0");
        UIPanel panel = new UIPanel();
        panel.getChildren().add(input);
        Assertions.assertEquals(panel, input.getParent());
        panel.getChildren().remove(input);
        Assertions.assertNull(input.getParent());
    }
    
    /** Whenever a new child component is added, the parent property 
     * of the child must be set to this component instance. 
     * If the parent property of the child was already non-null, 
     * the child must first be removed from its previous parent 
     * (where it may have been either a child or a facet).
     */
    @Test
    public void testSetChild1()
    {
        UIInput input = new UIInput();
        input.setId("input0");

        UIInput input1 = new UIInput();
        input.setId("input1");
        
        UIPanel panel = new UIPanel();
        panel.getChildren().add(input);
        Assertions.assertEquals(panel, input.getParent());
        
        panel.getChildren().set(0, input1);
        
        Assertions.assertEquals(panel, input1.getParent());
        Assertions.assertNull(input.getParent());
    }
    
    @Test
    public void testSetChild2()
    {
        UIInput input = new UIInput();
        input.setId("input0");

        UIInput input1 = new UIInput();
        input.setId("input1");
        
        UIPanel panel = new UIPanel();
        panel.getChildren().add(input);
        Assertions.assertEquals(panel, input.getParent());
        
        UIViewRoot root = new UIViewRoot();
        root.getChildren().add(panel);
        root.getFacets().put("customFacet", input1);
        
        panel.getChildren().set(0, input1);
        
        Assertions.assertEquals(panel, input1.getParent());
        Assertions.assertNull(input.getParent());
        
        Assertions.assertTrue(root.getFacets().isEmpty());
    }
    
    
    
    /** Whenever an existing child component is removed, the parent 
     * property of the child must be set to null.
     */
    @Test
    public void testSetFacetClearChild()
    {
        UIInput input = new UIInput();
        input.setId("input0");

        UIInput input1 = new UIInput();
        input.setId("input1");
        
        UIPanel panel = new UIPanel();
        panel.getChildren().add(input);
        Assertions.assertEquals(panel, input.getParent());

        UIViewRoot root = new UIViewRoot();
        root.getChildren().add(panel);
        root.getFacets().put("customFacet", input1);

        root.getFacets().put("customFacet", input);
        Assertions.assertEquals(root, input.getParent());
        Assertions.assertNull(input1.getParent());
        
        Assertions.assertFalse(root.getFacets().isEmpty());
        Assertions.assertTrue(panel.getChildCount() == 0);
    }

    @Test
    public void testSetFacetClearFacet()
    {
        UIInput input = new UIInput();
        input.setId("input0");

        UIInput input1 = new UIInput();
        input.setId("input1");
        
        UIPanel panel = new UIPanel();
        panel.getFacets().put("header", input);
        Assertions.assertEquals(panel, input.getParent());

        UIViewRoot root = new UIViewRoot();
        root.getChildren().add(panel);
        root.getFacets().put("customFacet", input1);

        root.getFacets().put("customFacet", input);
        Assertions.assertEquals(root, input.getParent());
        Assertions.assertNull(input1.getParent());
        
        Assertions.assertFalse(root.getFacets().isEmpty());
        Assertions.assertTrue(panel.getChildCount() == 0);
    }
}
