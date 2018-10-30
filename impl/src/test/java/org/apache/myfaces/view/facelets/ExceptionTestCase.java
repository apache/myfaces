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
package org.apache.myfaces.view.facelets;

import java.io.IOException;
import java.util.Iterator;

import javax.el.ExpressionFactory;
import javax.faces.FacesException;
import javax.faces.application.FacesMessage;
import javax.faces.component.ContextCallback;
import javax.faces.component.UICommand;
import javax.faces.component.UIComponent;
import javax.faces.component.UIInput;
import javax.faces.component.UIViewRoot;
import javax.faces.context.FacesContext;
import javax.faces.event.AbortProcessingException;
import javax.faces.event.ActionEvent;
import javax.faces.event.ExceptionQueuedEvent;
import javax.faces.event.ExceptionQueuedEventContext;
import javax.faces.event.PhaseId;
import javax.faces.event.ValueChangeEvent;
import javax.faces.validator.Validator;
import javax.faces.validator.ValidatorException;

import org.apache.myfaces.context.ExceptionHandlerImpl;
import org.easymock.classextension.EasyMock;
import org.junit.Assert;
import org.junit.Test;

public class ExceptionTestCase extends FaceletTestCase
{
    
    @Override
    protected ExpressionFactory createExpressionFactory()
    {
        return new org.apache.el.ExpressionFactoryImpl();
    }

    @Override
    protected void setUpFacesContext() throws Exception
    {
        super.setUpFacesContext();
        facesContext.setExceptionHandler(new ExceptionHandlerImpl());
    }
    
    @Test
    public void testActionException1() throws Exception
    {
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root,"testActionException1.xhtml");
        
        request.addParameter("mainForm:button1", "Submit");

        UICommand button = (UICommand) root.findComponent("mainForm:button1");
        Assert.assertNotNull(button);
        
        ExceptionBean bean = EasyMock.createMock(ExceptionBean.class);
        EasyMock.expect(bean.doSomeAction()).andThrow(new AbortProcessingException());
        // Setup is finished need to activate the mock
        EasyMock.replay(bean);
                
        request.setAttribute("bean", bean);
                
        button.processDecodes(facesContext);
        
        try
        {
            root.processApplication(facesContext);
        }
        catch(FacesException e)
        {
            Assert.fail("No exception should be thrown at this point.");
        }
        
        int i = 0;
        for (Iterator<ExceptionQueuedEvent> it = facesContext.getExceptionHandler().getUnhandledExceptionQueuedEvents().iterator(); it.hasNext();)
        {
            ExceptionQueuedEvent eqe = it.next();
            Throwable e = eqe.getContext().getException();
            if (e instanceof AbortProcessingException && e.getCause() == null)
            {
                //Expected
                i++;
            }
            else
            {
                Assert.fail("Unexpected exception queued");
            }
        }
        Assert.assertEquals(1, i);
    }

    /**
     * A runtime exception thrown must be
     * 
     * @throws Exception
     */
    @Test
    public void testActionException1_1() throws Exception
    {
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root,"testActionException1.xhtml");
        
        request.addParameter("mainForm:button1", "Submit");

        UICommand button = (UICommand) root.findComponent("mainForm:button1");
        Assert.assertNotNull(button);
        
        ExceptionBean bean = EasyMock.createMock(ExceptionBean.class);
        EasyMock.expect(bean.doSomeAction()).andThrow(new RuntimeException());
        // Setup is finished need to activate the mock
        EasyMock.replay(bean);
                
        request.setAttribute("bean", bean);
                
        button.processDecodes(facesContext);
        
        try
        {
            root.processApplication(facesContext);
        }
        catch(FacesException e)
        {
            return;
        }
        Iterable<ExceptionQueuedEvent> unhandledExceptionQueuedEvents = facesContext.getExceptionHandler().getUnhandledExceptionQueuedEvents();
        ExceptionQueuedEvent exceptionQueuedEvent = unhandledExceptionQueuedEvents.iterator().next();
         
        Assert.assertNotNull(exceptionQueuedEvent.getContext().getException());
    }
    
    /**
     * A runtime exception thrown must be
     * 
     * @throws Exception
     */
    @Test
    public void testActionException1_2() throws Exception
    {
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root,"testActionException1.xhtml");
        
        request.addParameter("mainForm:button1", "Submit");

        UICommand button = (UICommand) root.findComponent("mainForm:button1");
        Assert.assertNotNull(button);
        
        ExceptionBean bean = EasyMock.createMock(ExceptionBean.class);
        EasyMock.expect(bean.doSomeAction()).andThrow(new IOException());
        // Setup is finished need to activate the mock
        EasyMock.replay(bean);
                
        request.setAttribute("bean", bean);
                
        button.processDecodes(facesContext);
        
        try
        {
            root.processApplication(facesContext);
        }
        catch(FacesException e)
        {
            return;
        }
        Iterable<ExceptionQueuedEvent> unhandledExceptionQueuedEvents = facesContext.getExceptionHandler().getUnhandledExceptionQueuedEvents();
        ExceptionQueuedEvent exceptionQueuedEvent = unhandledExceptionQueuedEvents.iterator().next();
         
        Assert.assertNotNull(exceptionQueuedEvent.getContext().getException());
    }

    @Test
    public void testActionListenerException1() throws Exception
    {
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root,"testActionListenerException1.xhtml");
        
        request.addParameter("mainForm:button1", "Submit");

        UICommand button = (UICommand) root.findComponent("mainForm:button1");
        Assert.assertNotNull(button);
        
        //ActionEvent event = new ActionEvent(button);
        ExceptionBean bean = EasyMock.createMock(ExceptionBean.class);
        bean.doSomeActionListener((ActionEvent)EasyMock.anyObject());
        EasyMock.expectLastCall().andThrow(new AbortProcessingException());
        // Setup is finished need to activate the mock
        EasyMock.replay(bean);
                
        request.setAttribute("bean", bean);
                
        button.processDecodes(facesContext);
        
        try
        {
            root.processApplication(facesContext);
        }
        catch(FacesException e)
        {
            Assert.fail("No exception should be thrown at this point.");
        }
        
        int i = 0;
        for (Iterator<ExceptionQueuedEvent> it = facesContext.getExceptionHandler().getUnhandledExceptionQueuedEvents().iterator(); it.hasNext();)
        {
            ExceptionQueuedEvent eqe = it.next();
            Throwable e = eqe.getContext().getException();
            if (e instanceof AbortProcessingException && e.getCause() == null)
            {
                //Expected
                i++;
            }
            else
            {
                Assert.fail("Unexpected exception queued");
            }
        }
        Assert.assertEquals(1, i);

    }

    /**
     * If a RuntimeException or other is thrown, AbortProcessingException is queued
     * 
     * @throws Exception
     */
    @Test
    public void testActionListenerException1_1() throws Exception
    {
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root,"testActionListenerException1.xhtml");
        
        request.addParameter("mainForm:button1", "Submit");

        UICommand button = (UICommand) root.findComponent("mainForm:button1");
        Assert.assertNotNull(button);
        
        //ActionEvent event = new ActionEvent(button);
        ExceptionBean bean = EasyMock.createMock(ExceptionBean.class);
        bean.doSomeActionListener((ActionEvent)EasyMock.anyObject());
        EasyMock.expectLastCall().andThrow(new RuntimeException());
        // Setup is finished need to activate the mock
        EasyMock.replay(bean);
                
        request.setAttribute("bean", bean);
                
        button.processDecodes(facesContext);
        
        try
        {
            root.processApplication(facesContext);
        }
        catch (Throwable e)
        {
            // JSF 2.0: publish the executor's exception (if any).
            
            publishException (e, PhaseId.INVOKE_APPLICATION, facesContext);
        }
        //catch(FacesException e)
        //{
        //    Assert.fail("No exception should be thrown at this point.");
        //}
        
        int i = 0;
        for (Iterator<ExceptionQueuedEvent> it = facesContext.getExceptionHandler().getUnhandledExceptionQueuedEvents().iterator(); it.hasNext();)
        {
            ExceptionQueuedEvent eqe = it.next();
            Throwable e = eqe.getContext().getException();
            if (e instanceof AbortProcessingException)
            {
                Assert.fail("Unexpected exception queued");
            }
            else
            {
                //Expected
                i++;
            }
        }
        Assert.assertEquals(1, i);

    }
    
    private void publishException (Throwable e, PhaseId phaseId, FacesContext facesContext)
    {
        ExceptionQueuedEventContext context = new ExceptionQueuedEventContext (facesContext, e, null, phaseId);
        
        facesContext.getApplication().publishEvent (facesContext, ExceptionQueuedEvent.class, context);
    }

    @Test
    public void testValidatorException1() throws Exception
    {
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root,"testValidatorException1.xhtml");

        root.invokeOnComponent(facesContext, "mainForm:input1", new ContextCallback()
        {
            public void invokeContextCallback(FacesContext context, UIComponent target)
            {
                Object submittedValue = "Hello!";
                ExceptionBean bean = EasyMock.createStrictMock(ExceptionBean.class);
                bean.validateMe(facesContext,(UIComponent)target, submittedValue);
                EasyMock.expectLastCall().andThrow(new ValidatorException(new FacesMessage(target.getClientId(facesContext),"not valid!")));
                // Setup is finished need to activate the mock
                EasyMock.replay(bean);
                
                request.setAttribute("bean", bean);
                
                UIInput input = (UIInput) target;
                input.setSubmittedValue(submittedValue);
                try
                {
                    input.processValidators(facesContext);
                    Assert.assertTrue(facesContext.isValidationFailed());
                }
                catch(FacesException e)
                {
                    Assert.fail("No exception expected");
                }
            }
        });
    }
    
    @Test
    public void testValidatorException2() throws Exception
    {
        application.addValidator("customValidatorId", CustomValidator.class.getName());
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root,"testValidatorException2.xhtml");

        root.invokeOnComponent(facesContext, "mainForm:input1", new ContextCallback()
        {
            public void invokeContextCallback(FacesContext context, UIComponent target)
            {
                Object submittedValue = "Hello!";
                
                UIInput input = (UIInput) target;
                input.setSubmittedValue(submittedValue);
                try
                {
                    input.processValidators(facesContext);
                    Assert.assertTrue(facesContext.isValidationFailed());
                }
                catch(FacesException e)
                {
                    Assert.fail("No exception expected");
                }
            }
        });
        
        Assert.assertEquals("not valid!", facesContext.getMessageList().get(0).getSummary());
    }
    
    @Test
    public void testValueChangeListenerException1() throws Exception
    {
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root,"testValueChangeListenerException1.xhtml");

        root.invokeOnComponent(facesContext, "mainForm:input1", new ContextCallback()
        {
            public void invokeContextCallback(FacesContext context, UIComponent target)
            {
                Object submittedValue = "Hello!";
                ExceptionBean bean = EasyMock.createStrictMock(ExceptionBean.class);
                bean.valueChangeListenerMe((ValueChangeEvent)EasyMock.anyObject());
                EasyMock.expectLastCall().andThrow(new AbortProcessingException());
                // Setup is finished need to activate the mock
                EasyMock.replay(bean);
                
                request.setAttribute("bean", bean);
                
                UIInput input = (UIInput) target;
                input.setSubmittedValue(submittedValue);
                try
                {
                    input.processValidators(facesContext);
                }
                catch(FacesException e)
                {
                    Assert.fail("No exception expected");
                }
            }
        });
        
        try
        {
            root.processUpdates(facesContext);
        }
        catch (Throwable e)
        {
            // JSF 2.0: publish the executor's exception (if any).
            publishException (e, PhaseId.UPDATE_MODEL_VALUES, facesContext);
        }
        
        int i = 0;
        for (Iterator<ExceptionQueuedEvent> it = facesContext.getExceptionHandler().getUnhandledExceptionQueuedEvents().iterator(); it.hasNext();)
        {
            ExceptionQueuedEvent eqe = it.next();
            Throwable e = eqe.getContext().getException();
            if (e instanceof AbortProcessingException && e.getCause() == null)
            {
                //Expected
                i++;
            }
            else
            {
                Assert.fail("Unexpected exception queued");
            }
        }
        Assert.assertEquals(1, i);
    }
    
    @Test
    public void testValueChangeListenerException1_1() throws Exception
    {
        UIViewRoot root = facesContext.getViewRoot();
        vdl.buildView(facesContext, root,"testValueChangeListenerException1.xhtml");

        root.invokeOnComponent(facesContext, "mainForm:input1", new ContextCallback()
        {
            public void invokeContextCallback(FacesContext context, UIComponent target)
            {
                Object submittedValue = "Hello!";
                ExceptionBean bean = EasyMock.createStrictMock(ExceptionBean.class);
                bean.valueChangeListenerMe((ValueChangeEvent)EasyMock.anyObject());
                EasyMock.expectLastCall().andThrow(new RuntimeException());
                // Setup is finished need to activate the mock
                EasyMock.replay(bean);
                
                UIInput input = (UIInput) target;
                input.setSubmittedValue(submittedValue);
                try
                {
                    input.processValidators(facesContext);
                }
                catch(FacesException e)
                {
                    Assert.fail("No exception expected");
                }
                
                request.setAttribute("bean", bean);                
            }
        });
     
        try
        {
            root.processUpdates(facesContext);
        }
        catch (Throwable e)
        {
            // JSF 2.0: publish the executor's exception (if any).
            publishException (e, PhaseId.UPDATE_MODEL_VALUES, facesContext);
        }
        
        int i = 0;
        for (Iterator<ExceptionQueuedEvent> it = facesContext.getExceptionHandler().getUnhandledExceptionQueuedEvents().iterator(); it.hasNext();)
        {
            ExceptionQueuedEvent eqe = it.next();
            Throwable e = eqe.getContext().getException();
            if (e instanceof AbortProcessingException)
            {
                Assert.fail("Unexpected exception queued");
            }
            else
            {
                //Expected
                i++;
            }
        }
        Assert.assertEquals(1, i);
    }
    
    public static class CustomValidator implements Validator
    {

        public void validate(FacesContext context, UIComponent component,
                Object value) throws ValidatorException
        {
            throw new ValidatorException(new FacesMessage("not valid!"));
        }
    }

    public static interface ExceptionBean
    {
        public void validateMe(FacesContext context, UIComponent target, Object value);
        
        public void doSomeActionListener(ActionEvent evt);
        
        public void valueChangeListenerMe(ValueChangeEvent evt);
        
        public Object doSomeAction() throws IOException;
    }

}
