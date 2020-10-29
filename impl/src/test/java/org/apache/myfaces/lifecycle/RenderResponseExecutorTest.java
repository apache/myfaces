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
package org.apache.myfaces.lifecycle;

import jakarta.faces.component.UIViewRoot;

import org.junit.Assert;

import org.apache.myfaces.test.base.junit.AbstractJsfTestCase;
import org.junit.Test;

public class RenderResponseExecutorTest extends AbstractJsfTestCase
{

    @Test
    public void testNavigationCondition() throws Exception
    {
        UIViewRoot a = new UIViewRoot();
        UIViewRoot b = new UIViewRoot();
        UIViewRoot c = new UIViewRoot();
        UIViewRoot d = new UIViewRoot();
        a.setViewId("/a.xhtml");
        b.setViewId("/a.xhtml");
        c.setViewId("/c.xhtml");

        //If the view was not changed continue (return false)
        Assert.assertFalse(checkCondition(a, a));
        
        //If the view is different instance but same viewId iterate again (return true) 
        Assert.assertTrue(checkCondition(a, b));
        
        //If the view is different instance and different id iterate again
        Assert.assertTrue(checkCondition(a, c));
        
        //If the view is different instance and id is null iterate again
        Assert.assertTrue(checkCondition(a, d));
        
        //If the view is different instance and id is not null iterate again
        Assert.assertTrue(checkCondition(d, a));

        //If the view was not change continue (return false)
        Assert.assertFalse(checkCondition(d, d));
    }
    
    protected boolean checkCondition(UIViewRoot previousRoot, UIViewRoot root)
    {
        String viewId = previousRoot.getViewId();
        String newViewId = (root == null) ? null : root.getViewId();
        
        boolean isNotSameRoot = !( (newViewId == null ? newViewId == viewId : newViewId.equals(viewId) ) && 
                previousRoot.equals(root) );
        
        if ((newViewId == null && viewId != null) 
                || (newViewId != null && (!newViewId.equals(viewId) || isNotSameRoot ) ))
        {
            return true;
        }
        else
        {
            return false;
        }
    }
}
