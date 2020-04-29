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

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;
import static org.easymock.EasyMock.isA;

import jakarta.el.ExpressionFactory;
import jakarta.faces.webapp.FacesServlet;
import jakarta.servlet.ServletContext;
import jakarta.servlet.jsp.JspApplicationContext;
import jakarta.servlet.jsp.JspFactory;

import org.apache.myfaces.config.RuntimeConfig;
import org.apache.myfaces.el.DefaultPropertyResolver;
import org.apache.myfaces.el.VariableResolverImpl;
import org.apache.myfaces.el.unified.resolver.FacesCompositeELResolver;
import org.apache.myfaces.test.base.AbstractJsfTestCase;
import org.easymock.IAnswer;
import org.easymock.classextension.EasyMock;
import org.easymock.classextension.IMocksControl;

/**
 * @author Mathias Broekelmann (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
public class Jsp21FacesInitializerTest extends AbstractJsfTestCase
{

    public Jsp21FacesInitializerTest(String name)
    {
        super(name);
    }

    /**
     * Test method for {@link org.apache.myfaces.webapp.DefaultFacesInitializer#initFaces(javax.servlet.ServletContext)}.
     * @throws Exception 
     */
    public void testInitFaces() throws Exception
    {
        // TODO adapt this test case for MyFaces 2.0, because currently it checks nothing!
        
        Jsp21FacesInitializer initializer = new Jsp21FacesInitializer();
        IMocksControl control = EasyMock.createControl();
        
        JspFactory jspFactory = control.createMock(JspFactory.class);
        initializer.setJspFactory(jspFactory);
        
        RuntimeConfig runtimeConfig = control.createMock(RuntimeConfig.class);
        
        ServletContext context = control.createMock(ServletContext.class);
        ExpressionFactory expressionFactory = control.createMock(ExpressionFactory.class);
        runtimeConfig.setExpressionFactory(expressionFactory);
        runtimeConfig.setPropertyResolverChainHead(isA(DefaultPropertyResolver.class));
        runtimeConfig.setVariableResolverChainHead(isA(VariableResolverImpl.class));
        
        expect(context.getAttribute(eq(RuntimeConfig.class.getName()))).andReturn(runtimeConfig).anyTimes();
        
        expect(context.getInitParameter(eq(FacesServlet.CONFIG_FILES_ATTR))).andReturn(null);
        expect(context.getResourceAsStream(eq("/WEB-INF/faces-config.xml"))).andReturn(null);
        expect(context.getInitParameter(eq(FacesServlet.LIFECYCLE_ID_ATTR))).andReturn(null);

        // TODO: add myfaces specific tests
        expect(context.getResource(isA(String.class))).andReturn(null);
        expect(context.getResourceAsStream(isA(String.class))).andReturn(null);
        expect(context.getInitParameter(isA(String.class))).andReturn(null).anyTimes();
        expect(context.getAttribute(isA(String.class))).andReturn(null).anyTimes();
        context.setAttribute(isA(String.class), anyObject());
        expectLastCall().anyTimes();
        expect(context.getRealPath(isA(String.class))).andAnswer(new IAnswer<String>() {
            public String answer() throws Throwable
            {
                return (String) org.easymock.EasyMock.getCurrentArguments()[0];
            }
        });
        
        JspApplicationContext jspAppCtx = control.createMock(JspApplicationContext.class);
        expect(jspAppCtx.getExpressionFactory()).andReturn(expressionFactory);
        jspAppCtx.addELContextListener(isA(FacesELContextListener.class));
        expect(jspFactory.getJspApplicationContext(eq(context))).andReturn(jspAppCtx);
        jspAppCtx.addELResolver(isA(FacesCompositeELResolver.class));

        control.replay();
        initializer.initFaces(context);

        // In MYFACES-1222: The Jsp21FacesInitializer isn't practicable anymore.
        // The ServletContext-Mock won't return its WebXml instance, so Jsp21FacesInitializer will stop initializing.
        // This is why the next line is commented:
        
        //control.verify();
    }

}
