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

import java.util.List;
import org.apache.myfaces.mc.test.core.annotation.DeclareFacesConfig;
import org.apache.myfaces.mc.test.core.annotation.TestContainer;
import org.apache.myfaces.mc.test.core.runner.MyFacesContainer;
import org.apache.myfaces.mc.test.core.runner.MyFacesTestRunner;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;

/**
 *
 * @author lu4242
 */
@RunWith(MyFacesTestRunner.class)
@DeclareFacesConfig({"/a-faces-config.xml","/b-faces-config.xml"})
public class PhaseListenerOrderingTest 
{
    @TestContainer
    MyFacesContainer container;
    
    @Test
    public void testPhaseOrdering()
    {
        container.startViewRequest("/dummy.xhtml");
        
        container.processLifecycleExecute();
        container.processLifecycleRender();
        
        List<String> msgList = DummyPhaseListenerA.getMsgList(container.getFacesContext());
        
        Assert.assertEquals("DummyPhaseListenerB beforePhase", msgList.get(0));
        Assert.assertEquals("DummyPhaseListenerA beforePhase", msgList.get(1));
        Assert.assertEquals("DummyPhaseListenerA afterPhase", msgList.get(2));
        Assert.assertEquals("DummyPhaseListenerB afterPhase", msgList.get(3));
        
        container.endRequest();
    }
}
