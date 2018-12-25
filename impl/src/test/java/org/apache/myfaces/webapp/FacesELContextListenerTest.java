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
package org.apache.myfaces.webapp;

import static org.easymock.EasyMock.*;

import javax.el.ELContext;
import javax.el.ELContextEvent;
import javax.el.ELContextListener;
import javax.faces.application.Application;
import javax.faces.context.FacesContext;
import org.apache.myfaces.test.mock.MockFacesContext;

import org.apache.myfaces.test.mock.MockFacesContext12;
import org.easymock.IMocksControl;
import org.easymock.classextension.EasyMock;
import org.junit.Test;

/**
 * @author Mathias Broekelmann (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
public class FacesELContextListenerTest
{

    /**
     * Test method for {@link org.apache.myfaces.webapp.FacesELContextListener#contextCreated(javax.el.ELContextEvent)}.
     */
    @Test
    public void testContextCreated()
    {
        FacesELContextListener listener = new FacesELContextListener();
        IMocksControl mockControl = EasyMock.createControl();
        ELContext elctx = mockControl.createMock(ELContext.class);
        MockFacesContext facesctx = new MockFacesContext();
        Application app = mockControl.createMock(Application.class);
        facesctx.setApplication(app);
        ELContextEvent event = mockControl.createMock(ELContextEvent.class);
        expect(event.getELContext()).andReturn(elctx);
        elctx.putContext(eq(FacesContext.class), same(facesctx));
        ELContextListener elctxListener = mockControl.createMock(ELContextListener.class);
        expect(app.getELContextListeners()).andReturn(new ELContextListener[] { elctxListener });
        elctxListener.contextCreated(same(event));
        mockControl.replay();
        listener.contextCreated(event);
        mockControl.verify();
    }

}
