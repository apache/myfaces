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
package org.apache.myfaces.context.flash;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.event.PhaseId;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.apache.myfaces.test.base.junit.AbstractJsfTestCase;

import org.apache.myfaces.test.mock.MockExternalContext20;
import org.apache.myfaces.test.mock.MockExternalContext;
import org.apache.myfaces.test.mock.MockFacesContext20;
import org.apache.myfaces.test.mock.MockHttpServletRequest;
import org.apache.myfaces.test.mock.MockHttpServletResponse;
import org.junit.Assert;
import org.junit.Test;

/**
 * Tests for FlashImpl.
 */
public class FlashImplTest extends AbstractJsfTestCase
{
    
    private FlashImpl _flash;

    public FlashImplTest()
    {
    }

    @Override
    protected void setUpFacesContext() throws Exception
    {
        super.setUpFacesContext();

        // Unfortunately, setUpExternalContext() does not work, b/c MockFacesContext20 overwrites it!
        externalContext = new MockExternalContext(servletContext, request, response);
        facesContext.setExternalContext(externalContext);
    }

    @Override
    public void setUp() throws Exception
    {
        super.setUp();
        
        _flash = (FlashImpl) FlashImpl.getCurrentInstance(externalContext);
    }

    @Override
    public void tearDown() throws Exception
    {
        _flash = null;
        
        super.tearDown();
    }

    /**
     * Tests if FlashImpl uses the sessionMap as base for the SubKeyMap
     * and correctly stores the values in it.
     * @throws Exception
     */
    @Test
    public void testSessionMapWrapperSubKeyMap() throws Exception
    {
        // set phase to RESTORE_VIEW to create the flash tokens on doPrePhaseActions()
        facesContext.setCurrentPhaseId(PhaseId.RESTORE_VIEW);
        _flash.doPrePhaseActions(facesContext);
        
        // put the value in the scope an keep() it!
        _flash.putNow("testkey1", "testvalue1");
        _flash.keep("testkey1");
        
        // set phase to RENDER_RESPONSE --> now renderMap will be used
        facesContext.setCurrentPhaseId(PhaseId.RENDER_RESPONSE);
        
        // get the token for the render FlashMap (FlashImpl internals)
        final String renderToken = (String) externalContext
                .getRequestMap().get(FlashImpl.FLASH_RENDER_MAP_TOKEN);
        final String sessionMapKey = FlashImpl.FLASH_SESSION_MAP_SUBKEY_PREFIX + 
                FlashImpl.SEPARATOR_CHAR + renderToken + FlashImpl.SEPARATOR_CHAR + "testkey1";
        
        // Assertion
        Assert.assertEquals("The render FlashMap must use the session Map to store the values.",
                "testvalue1", session.getAttribute(sessionMapKey));     
    }
    
    /**
     * Tests the functionality of keep() in a normal postback scenario.
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testKeepValueNormalPostback() throws Exception
    {
        // simulate Faces lifecycle:
        // note that doPrePhaseActions() only performs tasks on RESTORE_VIEW
        // and doPostPhaseActions() only on the last phase.
        
        // initial request ----------------------------------------------------
        
        // this request is a normal GET request, and thus not a postback
        ((MockFacesContext20) facesContext).setPostback(false);
        
        // simulate Faces lifecycle
        facesContext.setCurrentPhaseId(PhaseId.RESTORE_VIEW);
        _flash.doPrePhaseActions(facesContext);
        
        // simulate Faces lifecycle
        facesContext.setCurrentPhaseId(PhaseId.RENDER_RESPONSE);
        _flash.doPostPhaseActions(facesContext);
        
        // first postback -----------------------------------------------------
        
        // simulate a new request
        _simulateNewRequest();
        
        // this request should be a postback
        ((MockFacesContext20) facesContext).setPostback(true);
        
        // simulate Faces lifecycle
        facesContext.setCurrentPhaseId(PhaseId.RESTORE_VIEW);
        _flash.doPrePhaseActions(facesContext);
        
        // simulate Faces lifecycle
        facesContext.setCurrentPhaseId(PhaseId.INVOKE_APPLICATION);
        
        // put the value in the scope an keep() it!
        _flash.putNow("flashkey", "flashvalue");
        _flash.keep("flashkey");
        
        // simulate Faces lifecycle
        facesContext.setCurrentPhaseId(PhaseId.RENDER_RESPONSE);
        _flash.doPostPhaseActions(facesContext);
        
        // second postback ----------------------------------------------------
        
        // simulate a new request
        _simulateNewRequest();
        
        // this request should be a postback
        ((MockFacesContext20) facesContext).setPostback(true);
        
        // simulate Faces lifecycle
        facesContext.setCurrentPhaseId(PhaseId.RESTORE_VIEW);
        _flash.doPrePhaseActions(facesContext);
        
        // simulate Faces lifecycle
        facesContext.setCurrentPhaseId(PhaseId.INVOKE_APPLICATION);
        
        // _flash.get() will ask the execute FlashMap for the value
        // and this must be the render FlashMap of the previous request,
        // thus it must contain the value from the previous request.
        Assert.assertEquals("flashvalue", _flash.get("flashkey"));
        
        // simulate Faces lifecycle
        facesContext.setCurrentPhaseId(PhaseId.RENDER_RESPONSE);
        _flash.doPostPhaseActions(facesContext);
        
        // _flash.get() also references to the execute FlashMap, but
        // this one has to be cleared by now, thus it must be null.
        Assert.assertNull("Execute FlashMap must have been cleared", _flash.get("flashkey"));
        
        // get the execute Map of the second postback (FlashImpl internals)
        Map<String, Object> executeMap = (Map<String, Object>) externalContext
                .getRequestMap().get(FlashImpl.FLASH_EXECUTE_MAP);
        
        // must be empty
        Assert.assertTrue("The execute Map of the second postback must have been cleared",
                executeMap.isEmpty());
    }
    
    /**
     * Tests the functionality of keep() in a POST-REDIRECT-GET scenario.
     * @throws Exception
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testKeepValuePostRedirectGet() throws Exception
    {
        // simulate Faces lifecycle:
        // note that doPrePhaseActions() only performs tasks on RESTORE_VIEW
        // and doPostPhaseActions() only on the last phase.
        
        // initial request ----------------------------------------------------
        
        // this request is a normal GET request, and thus not a postback
        ((MockFacesContext20) facesContext).setPostback(false);
        
        // simulate Faces lifecycle
        facesContext.setCurrentPhaseId(PhaseId.RESTORE_VIEW);
        _flash.doPrePhaseActions(facesContext);
        
        // simulate Faces lifecycle
        facesContext.setCurrentPhaseId(PhaseId.RENDER_RESPONSE);
        _flash.doPostPhaseActions(facesContext);
        
        // first postback (POST of POST-REDIRECT-GET) -------------------------
        
        // simulate a new request
        _simulateNewRequest();
        
        // this request should be a postback
        ((MockFacesContext20) facesContext).setPostback(true);
        
        // simulate Faces lifecycle
        facesContext.setCurrentPhaseId(PhaseId.RESTORE_VIEW);
        _flash.doPrePhaseActions(facesContext);
        
        // simulate Faces lifecycle
        facesContext.setCurrentPhaseId(PhaseId.INVOKE_APPLICATION);
        
        // put the value in the scope an keep() it!
        _flash.put("flashkey", "flashvalue");
        _flash.keep("flashkey");
        
        // set redirect to true, this happens by the NavigationHandler in phase 5
        _flash.setRedirect(true);
        
        Assert.assertTrue("setRedirect(true) was just called, thus isRedirect() must be true",
                _flash.isRedirect());
        
        // note that setRedirect(true) was called, thus the cleanup happens
        // in phase 5, because doPostPhaseActions() won't be called on phase 6.
        _flash.doPostPhaseActions(facesContext);
        
        // GET request of POST-REDIRECT-GET -----------------------------------
        
        // simulate a new request
        _simulateNewRequest();
        
        // this request is not a postback
        ((MockFacesContext20) facesContext).setPostback(false);
        
        // simulate Faces lifecycle
        // Note that doPrePhaseActions() is called on RESTORE_VIEW even
        // though this request is not a postback.
        facesContext.setCurrentPhaseId(PhaseId.RESTORE_VIEW);
        _flash.doPrePhaseActions(facesContext);
        
        // check isRedirect();
        Assert.assertTrue("setRedirect(true) was called on the previous request, "
                + " and we are in the execute portion of the lifecycle, "
                + " thus isRedirect() must be true.",
                _flash.isRedirect());
        
        // simulate Faces lifecycle - Faces will immediately jump to phase 6
        facesContext.setCurrentPhaseId(PhaseId.RENDER_RESPONSE);
        
        // check isRedirect();
        Assert.assertFalse("setRedirect(true) was called on the previous request, "
                + " but we are already in the render portion of the lifecycle, "
                + " thus isRedirect() must be false.",
                _flash.isRedirect());
        
        // _flash.get() will ask the execute FlashMap and this one
        // must contain the key used in keep()
        Assert.assertEquals("flashvalue", _flash.get("flashkey"));
        
        _flash.doPostPhaseActions(facesContext);
        
        // second postback (after POST-REDIRECT-GET) --------------------------
        
        // simulate a new request
        _simulateNewRequest();
        
        // this request should be a postback
        ((MockFacesContext20) facesContext).setPostback(true);
        
        // simulate Faces lifecycle
        facesContext.setCurrentPhaseId(PhaseId.RESTORE_VIEW);
        _flash.doPrePhaseActions(facesContext);
        
        // simulate Faces lifecycle
        facesContext.setCurrentPhaseId(PhaseId.INVOKE_APPLICATION);
        
        // check isRedirect();
        Assert.assertFalse("setRedirect(true) was called on the pre-previous request, "
                + " thus isRedirect() must be false again.",
                _flash.isRedirect());
        
        // _flash.get() will ask the execute FlashMap for the value
        // and this must be the render FlashMap of the previous (GET) request,
        // thus it must not contain the value from the previous request,
        // because the value was on the previous request's execute FlashMap
        // and not on the previous request's render FlashMap.
        Assert.assertNull(_flash.get("flashkey"));
        
        // simulate Faces lifecycle
        facesContext.setCurrentPhaseId(PhaseId.RENDER_RESPONSE);
        _flash.doPostPhaseActions(facesContext);
        
        // get the execute Map of the second postback (FlashImpl internals)
        Map<String, Object> executeMap = (Map<String, Object>) externalContext
                .getRequestMap().get(FlashImpl.FLASH_EXECUTE_MAP);
        
        // must be empty
        Assert.assertTrue("The execute Map of the second postback must have been cleared",
                executeMap.isEmpty());
    }
    
    /**
     * Tests the functionality of keepMessages in a normal postback scenario.
     * @throws Exception
     */
    @Test
    public void testKeepMessagesNormalPostback() throws Exception
    {
        // simulate Faces lifecycle:
        // note that doPrePhaseActions() only performs tasks on RESTORE_VIEW
        // and doPostPhaseActions() only on the last phase.
        
        // initial request ----------------------------------------------------
        
        // this request is a normal GET request, and thus not a postback
        ((MockFacesContext20) facesContext).setPostback(false);
        
        // simulate Faces lifecycle
        facesContext.setCurrentPhaseId(PhaseId.RESTORE_VIEW);
        _flash.doPrePhaseActions(facesContext);
        
        // simulate Faces lifecycle
        facesContext.setCurrentPhaseId(PhaseId.RENDER_RESPONSE);
        _flash.doPostPhaseActions(facesContext);
        
        // first postback -----------------------------------------------------
        
        // simulate a new request
        _simulateNewRequest();
        
        // this request should be a postback
        ((MockFacesContext20) facesContext).setPostback(true);
        
        // simulate Faces lifecycle
        facesContext.setCurrentPhaseId(PhaseId.RESTORE_VIEW);
        _flash.doPrePhaseActions(facesContext);
        
        // simulate Faces lifecycle
        facesContext.setCurrentPhaseId(PhaseId.INVOKE_APPLICATION);
        
        // add FacesMessages to the facesContext
        FacesMessage messageClientId = new FacesMessage("message for clientId");
        facesContext.addMessage("clientId", messageClientId);
        FacesMessage messageNoClientId = new FacesMessage("message without clientId");
        facesContext.addMessage(null, messageNoClientId);
        
        // now the FacesContext must contain 2 messages
        Assert.assertEquals(2, facesContext.getMessageList().size());
        
        // keep messages
        _flash.setKeepMessages(true);
        
        Assert.assertTrue("setKeepMessages(true) was just called, thus isKeepMessages() "
                + "must be true.", _flash.isKeepMessages());
        
        // simulate Faces lifecycle
        facesContext.setCurrentPhaseId(PhaseId.RENDER_RESPONSE);
        _flash.doPostPhaseActions(facesContext);
        
        // second postback ----------------------------------------------------
        
        // simulate a new request
        _simulateNewRequest();
        
        // this request should be a postback
        ((MockFacesContext20) facesContext).setPostback(true);
        
        // now the FacesContext must contain 0 messages (new request, new FacesContext)
        Assert.assertEquals(0, facesContext.getMessageList().size());
        
        // simulate Faces lifecycle
        facesContext.setCurrentPhaseId(PhaseId.RESTORE_VIEW);
        _flash.doPrePhaseActions(facesContext);
        
        // now the messages must be here again
        Assert.assertEquals(2, facesContext.getMessageList().size());
        Assert.assertEquals(Arrays.asList(messageClientId), facesContext.getMessageList("clientId"));
        Assert.assertEquals(Arrays.asList(messageNoClientId), facesContext.getMessageList(null));
        
        Assert.assertFalse("setKeepMessages(true) was not called on this request, thus "
                + "isKeepMessages() must be false.", _flash.isKeepMessages());
        
        // simulate Faces lifecycle
        facesContext.setCurrentPhaseId(PhaseId.RENDER_RESPONSE);
        _flash.doPostPhaseActions(facesContext);
        
        // third postback ----------------------------------------------------
        
        // simulate a new request
        _simulateNewRequest();
        
        // this request should be a postback
        ((MockFacesContext20) facesContext).setPostback(true);
        
        // now the FacesContext must contain 0 messages (new request, new FacesContext)
        Assert.assertEquals(0, facesContext.getMessageList().size());
        
        // simulate Faces lifecycle
        facesContext.setCurrentPhaseId(PhaseId.RESTORE_VIEW);
        _flash.doPrePhaseActions(facesContext);
        
        // the messages must still be gone here, because setKeepMessages(true)
        // was not called on the previous request
        Assert.assertEquals(0, facesContext.getMessageList().size());
        
        // simulate Faces lifecycle
        facesContext.setCurrentPhaseId(PhaseId.RENDER_RESPONSE);
        _flash.doPostPhaseActions(facesContext); 
    }
    
    /**
     * Tests the functionality of keepMessages in a POST-REDIRECT-GET scenario.
     * In this test case the messages are only shipped from the POST to the GET and
     * then not from the GET to the next postback.
     * @throws Exception
     */
    @Test
    public void testKeepMessagesPostRedirectGet() throws Exception
    {
        // simulate Faces lifecycle:
        // note that doPrePhaseActions() only performs tasks on RESTORE_VIEW
        // and doPostPhaseActions() only on the last phase.
        
        // initial request ----------------------------------------------------
        
        // this request is a normal GET request, and thus not a postback
        ((MockFacesContext20) facesContext).setPostback(false);
        
        // simulate Faces lifecycle
        facesContext.setCurrentPhaseId(PhaseId.RESTORE_VIEW);
        _flash.doPrePhaseActions(facesContext);
        
        // simulate Faces lifecycle
        facesContext.setCurrentPhaseId(PhaseId.RENDER_RESPONSE);
        _flash.doPostPhaseActions(facesContext);
        
        // first postback (POST of POST-REDIRECT-GET) -------------------------
        
        // simulate a new request
        _simulateNewRequest();
        
        // this request should be a postback
        ((MockFacesContext20) facesContext).setPostback(true);
        
        // simulate Faces lifecycle
        facesContext.setCurrentPhaseId(PhaseId.RESTORE_VIEW);
        _flash.doPrePhaseActions(facesContext);
        
        // simulate Faces lifecycle
        facesContext.setCurrentPhaseId(PhaseId.INVOKE_APPLICATION);
        
        // add FacesMessages to the facesContext
        FacesMessage messageClientId = new FacesMessage("message for clientId");
        facesContext.addMessage("clientId", messageClientId);
        FacesMessage messageNoClientId = new FacesMessage("message without clientId");
        facesContext.addMessage(null, messageNoClientId);
        
        // now the FacesContext must contain 2 messages
        Assert.assertEquals(2, facesContext.getMessageList().size());
        
        // keep messages
        _flash.setKeepMessages(true);
        Assert.assertTrue("setKeepMessages(true) was just called, thus isKeepMessages() "
                + "must be true.", _flash.isKeepMessages());
        
        // set redirect to true, this happens by the NavigationHandler in phase 5
        _flash.setRedirect(true);
        Assert.assertTrue("setRedirect(true) was just called, thus isRedirect() must be true",
                _flash.isRedirect());
        // The redirect cause responseComplete() method to be called.
        facesContext.responseComplete();
        // note that setRedirect(true) was called, thus the cleanup happens
        // in phase 5, because doPostPhaseActions() won't be called on phase 6.
        _flash.doPostPhaseActions(facesContext);
        
        // GET request of POST-REDIRECT-GET -----------------------------------
        
        // simulate a new request
        _simulateNewRequest();
        
        // this request is not a postback
        ((MockFacesContext20) facesContext).setPostback(false);
        
        // now the FacesContext must contain 0 messages (new request, new FacesContext)
        Assert.assertEquals(0, facesContext.getMessageList().size());
        
        // simulate Faces lifecycle
        // Note that doPrePhaseActions() is called on RESTORE_VIEW even
        // though this request is not a postback.
        facesContext.setCurrentPhaseId(PhaseId.RESTORE_VIEW);
        _flash.doPrePhaseActions(facesContext);
        
        // simulate Faces lifecycle - Faces will immediately jump to phase 6
        facesContext.setCurrentPhaseId(PhaseId.RENDER_RESPONSE);
        
        // now the messages must be here again
        Assert.assertEquals(2, facesContext.getMessageList().size());
        Assert.assertEquals(Arrays.asList(messageClientId), facesContext.getMessageList("clientId"));
        Assert.assertEquals(Arrays.asList(messageNoClientId), facesContext.getMessageList(null));
        
        // check isKeepMessages()
        Assert.assertFalse("setKeepMessages(true) was not called on this request, thus "
                + "isKeepMessages() must be false.", _flash.isKeepMessages());
        
        _flash.doPostPhaseActions(facesContext);
        
        // second postback (after POST-REDIRECT-GET) --------------------------
        
        // simulate a new request
        _simulateNewRequest();
        
        // this request should be a postback
        ((MockFacesContext20) facesContext).setPostback(true);
        
        // now the FacesContext must contain 0 messages (new request, new FacesContext)
        Assert.assertEquals(0, facesContext.getMessageList().size());
        
        // simulate Faces lifecycle
        facesContext.setCurrentPhaseId(PhaseId.RESTORE_VIEW);
        _flash.doPrePhaseActions(facesContext);
        
        // simulate Faces lifecycle
        facesContext.setCurrentPhaseId(PhaseId.INVOKE_APPLICATION);
        
        // now the FacesContext must contain 0 messages, because 
        // setKeepMessages(true) was not called on the GET-request
        Assert.assertEquals(0, facesContext.getMessageList().size());
        
        // simulate Faces lifecycle
        facesContext.setCurrentPhaseId(PhaseId.RENDER_RESPONSE);
        _flash.doPostPhaseActions(facesContext);
    }
    
    /**
     * Tests the functionality of keepMessages in a POST-REDIRECT-GET scenario.
     * In this test case the messages are shipped from the POST to the GET and
     * then also from the GET to the next postback.
     * @throws Exception
     */
    @Test
    public void testKeepMessagesPostRedirectGetTwoTimes() throws Exception
    {
        // simulate Faces lifecycle:
        // note that doPrePhaseActions() only performs tasks on RESTORE_VIEW
        // and doPostPhaseActions() only on the last phase.
        
        // initial request ----------------------------------------------------
        
        // this request is a normal GET request, and thus not a postback
        ((MockFacesContext20) facesContext).setPostback(false);
        
        // simulate Faces lifecycle
        facesContext.setCurrentPhaseId(PhaseId.RESTORE_VIEW);
        _flash.doPrePhaseActions(facesContext);
        
        // simulate Faces lifecycle
        facesContext.setCurrentPhaseId(PhaseId.RENDER_RESPONSE);
        _flash.doPostPhaseActions(facesContext);
        
        // first postback (POST of POST-REDIRECT-GET) -------------------------
        
        // simulate a new request
        _simulateNewRequest();
        
        // this request should be a postback
        ((MockFacesContext20) facesContext).setPostback(true);
        
        // simulate Faces lifecycle
        facesContext.setCurrentPhaseId(PhaseId.RESTORE_VIEW);
        _flash.doPrePhaseActions(facesContext);
        
        // simulate Faces lifecycle
        facesContext.setCurrentPhaseId(PhaseId.INVOKE_APPLICATION);
        
        // add FacesMessages to the facesContext
        FacesMessage messageClientId = new FacesMessage("message for clientId");
        facesContext.addMessage("clientId", messageClientId);
        FacesMessage messageNoClientId = new FacesMessage("message without clientId");
        facesContext.addMessage(null, messageNoClientId);
        
        // now the FacesContext must contain 2 messages
        Assert.assertEquals(2, facesContext.getMessageList().size());
        
        // keep messages
        _flash.setKeepMessages(true);
        Assert.assertTrue("setKeepMessages(true) was just called, thus isKeepMessages() "
                + "must be true.", _flash.isKeepMessages());
        
        // set redirect to true, this happens by the NavigationHandler in phase 5
        _flash.setRedirect(true);
        Assert.assertTrue("setRedirect(true) was just called, thus isRedirect() must be true",
                _flash.isRedirect());
        // The redirect cause responseComplete() method to be called.
        facesContext.responseComplete();
        
        // note that setRedirect(true) was called, thus the cleanup happens
        // in phase 5, because doPostPhaseActions() won't be called on phase 6.
        _flash.doPostPhaseActions(facesContext);
        
        // GET request of POST-REDIRECT-GET -----------------------------------
        
        // simulate a new request
        _simulateNewRequest();
        
        // this request is not a postback
        ((MockFacesContext20) facesContext).setPostback(false);
        
        // now the FacesContext must contain 0 messages (new request, new FacesContext)
        Assert.assertEquals(0, facesContext.getMessageList().size());
        
        // simulate Faces lifecycle
        // Note that doPrePhaseActions() is called on RESTORE_VIEW even
        // though this request is not a postback.
        facesContext.setCurrentPhaseId(PhaseId.RESTORE_VIEW);
        _flash.doPrePhaseActions(facesContext);
        
        // simulate Faces lifecycle - Faces will immediately jump to phase 6
        facesContext.setCurrentPhaseId(PhaseId.RENDER_RESPONSE);
        
        // now the messages must be here again
        Assert.assertEquals(2, facesContext.getMessageList().size());
        Assert.assertEquals(Arrays.asList(messageClientId), facesContext.getMessageList("clientId"));
        Assert.assertEquals(Arrays.asList(messageNoClientId), facesContext.getMessageList(null));
        
        // check isKeepMessages()
        Assert.assertFalse("setKeepMessages(true) was not called on this request, thus "
                + "isKeepMessages() must be false.", _flash.isKeepMessages());
        
        // keep messages - again
        _flash.setKeepMessages(true);
        Assert.assertTrue("setKeepMessages(true) was just called, thus isKeepMessages() "
                + "must be true.", _flash.isKeepMessages());
        // The redirect cause responseComplete() method to be called.
        facesContext.responseComplete();
        
        _flash.doPostPhaseActions(facesContext);
        
        // second postback (after POST-REDIRECT-GET) --------------------------
        
        // simulate a new request
        _simulateNewRequest();
        
        // this request should be a postback
        ((MockFacesContext20) facesContext).setPostback(true);
        
        // now the FacesContext must contain 0 messages (new request, new FacesContext)
        Assert.assertEquals(0, facesContext.getMessageList().size());
        
        // simulate Faces lifecycle
        facesContext.setCurrentPhaseId(PhaseId.RESTORE_VIEW);
        _flash.doPrePhaseActions(facesContext);
        
        // simulate Faces lifecycle
        facesContext.setCurrentPhaseId(PhaseId.INVOKE_APPLICATION);
        
        // now the messages must be here again
        Assert.assertEquals(2, facesContext.getMessageList().size());
        Assert.assertEquals(Arrays.asList(messageClientId), facesContext.getMessageList("clientId"));
        Assert.assertEquals(Arrays.asList(messageNoClientId), facesContext.getMessageList(null));
        
        // simulate Faces lifecycle
        facesContext.setCurrentPhaseId(PhaseId.RENDER_RESPONSE);
        _flash.doPostPhaseActions(facesContext);
        
        // third postback -----------------------------------------------------
        
        // simulate a new request
        _simulateNewRequest();
        
        // this request should be a postback
        ((MockFacesContext20) facesContext).setPostback(true);
        
        // now the FacesContext must contain 0 messages (new request, new FacesContext)
        Assert.assertEquals(0, facesContext.getMessageList().size());
        
        // simulate Faces lifecycle
        facesContext.setCurrentPhaseId(PhaseId.RESTORE_VIEW);
        _flash.doPrePhaseActions(facesContext);
        
        // simulate Faces lifecycle
        facesContext.setCurrentPhaseId(PhaseId.INVOKE_APPLICATION);
        
        // now the FacesContext must contain 0 messages, because 
        // setKeepMessages(true) was not called on the previous postback
        Assert.assertEquals(0, facesContext.getMessageList().size());
        
        // simulate Faces lifecycle
        facesContext.setCurrentPhaseId(PhaseId.RENDER_RESPONSE);
        _flash.doPostPhaseActions(facesContext);
    }
    
    /**
     * Test if setRedirect(true) works via _flash.put("redirect", true)
     * and if isRedirect() is equal to _flash.get("redirect").
     */
    @Test
    public void testSetRedirect()
    {
        Assert.assertFalse(_flash.isRedirect());
        Assert.assertFalse((Boolean) _flash.get("redirect"));
        
        _flash.put("redirect", true);
        
        Assert.assertTrue(_flash.isRedirect());
        Assert.assertTrue((Boolean) _flash.get("redirect"));
    }
    
    /**
     * Test if setKeepMessages(true) works via _flash.put("keepMessages", true)
     * and if isKeepMessages() is equal to _flash.get("keepMessages").
     */
    @Test
    public void testSetKeepMessages()
    {
        Assert.assertFalse(_flash.isKeepMessages());
        Assert.assertFalse((Boolean) _flash.get("keepMessages"));
        
        _flash.put("keepMessages", true);
        
        Assert.assertTrue(_flash.isKeepMessages());
        Assert.assertTrue((Boolean) _flash.get("keepMessages"));
    }
    
    /**
     * Tests the functionality of putNow().
     */
    @SuppressWarnings("unchecked")
    @Test
    public void testPutNow()
    {
        Map<String, Object> requestMap = externalContext.getRequestMap();
        
        // requestMap must NOT contain the key
        Assert.assertNull(requestMap.get("flashkey"));
        
        _flash.putNow("flashkey", "flashvalue");
        
        // requestMap must contain the key
        Assert.assertEquals("flashvalue", requestMap.get("flashkey"));
    }
    
    /**
     * Tests keep()
     * @throws Exception
     */
    @Test
    public void testKeep() throws Exception
    {
        // simulate Faces lifecycle:
        // note that doPrePhaseActions() only performs tasks on RESTORE_VIEW
        // and doPostPhaseActions() only on the last phase.
        
        // initial request ----------------------------------------------------
        
        // this request is a normal GET request, and thus not a postback
        ((MockFacesContext20) facesContext).setPostback(false);
        
        // simulate Faces lifecycle
        facesContext.setCurrentPhaseId(PhaseId.RESTORE_VIEW);
        _flash.doPrePhaseActions(facesContext);
        
        // simulate Faces lifecycle
        facesContext.setCurrentPhaseId(PhaseId.RENDER_RESPONSE);
        _flash.doPostPhaseActions(facesContext);
        
        // first postback -----------------------------------------------------
        
        // simulate a new request
        _simulateNewRequest();
        
        // this request should be a postback
        ((MockFacesContext20) facesContext).setPostback(true);
        
        // simulate Faces lifecycle
        facesContext.setCurrentPhaseId(PhaseId.RESTORE_VIEW);
        _flash.doPrePhaseActions(facesContext);
        
        // simulate Faces lifecycle
        facesContext.setCurrentPhaseId(PhaseId.INVOKE_APPLICATION);
        
        // put a value into the request FlashMap
        _flash.putNow("flashkey", "flashvalue");
        
        // and keep() it
        _flash.keep("flashkey");
        
        // simulate Faces lifecycle
        facesContext.setCurrentPhaseId(PhaseId.RENDER_RESPONSE);
        
        // cleanup flash
        _flash.doPostPhaseActions(facesContext);
        
        // second postback ----------------------------------------------------
        
        // simulate a new request
        _simulateNewRequest();
        
        // this request should be a postback
        ((MockFacesContext20) facesContext).setPostback(true);
        
        // simulate Faces lifecycle
        facesContext.setCurrentPhaseId(PhaseId.RESTORE_VIEW);
        _flash.doPrePhaseActions(facesContext);
        
        // simulate Faces lifecycle
        facesContext.setCurrentPhaseId(PhaseId.INVOKE_APPLICATION);
     
        // the value must be in the executeMap
        Assert.assertEquals("flashvalue", _flash.get("flashkey"));
        
        // simulate Faces lifecycle
        facesContext.setCurrentPhaseId(PhaseId.RENDER_RESPONSE);
        
        // cleanup flash
        _flash.doPostPhaseActions(facesContext);
    }
    
    /**
     * Like testKeep(), but without calling keep() to keep the value.
     * @throws Exception
     */
    @Test
    public void testNotKeep() throws Exception
    {
        // simulate Faces lifecycle:
        // note that doPrePhaseActions() only performs tasks on RESTORE_VIEW
        // and doPostPhaseActions() only on RENDER_RESPONSE.
        
        // initial request ----------------------------------------------------
        
        // this request is a normal GET request, and thus not a postback
        ((MockFacesContext20) facesContext).setPostback(false);
        
        // simulate Faces lifecycle
        facesContext.setCurrentPhaseId(PhaseId.RESTORE_VIEW);
        _flash.doPrePhaseActions(facesContext);
        
        // simulate Faces lifecycle
        facesContext.setCurrentPhaseId(PhaseId.RENDER_RESPONSE);
        _flash.doPostPhaseActions(facesContext);
        
        // first postback -----------------------------------------------------
        
        // simulate a new request
        _simulateNewRequest();
        
        // this request should be a postback
        ((MockFacesContext20) facesContext).setPostback(true);
        
        // simulate Faces lifecycle
        facesContext.setCurrentPhaseId(PhaseId.RESTORE_VIEW);
        _flash.doPrePhaseActions(facesContext);
        
        // simulate Faces lifecycle
        facesContext.setCurrentPhaseId(PhaseId.INVOKE_APPLICATION);
        
        // put a value into the request FlashMap
        _flash.putNow("flashkey", "flashvalue");
        
        // and do NOT keep it.
        
        // simulate Faces lifecycle
        facesContext.setCurrentPhaseId(PhaseId.RENDER_RESPONSE);
        
        _flash.doPostPhaseActions(facesContext);
        
        // second postback ----------------------------------------------------
        
        // simulate a new request
        _simulateNewRequest();
        
        // this request should be a postback
        ((MockFacesContext20) facesContext).setPostback(true);
        
        // simulate Faces lifecycle
        facesContext.setCurrentPhaseId(PhaseId.RESTORE_VIEW);
        _flash.doPrePhaseActions(facesContext);
        
        // simulate Faces lifecycle
        facesContext.setCurrentPhaseId(PhaseId.INVOKE_APPLICATION);
     
        // render FlashMap must be empty
        Assert.assertNull(_flash.get("flashkey"));
        
        // simulate Faces lifecycle
        facesContext.setCurrentPhaseId(PhaseId.RENDER_RESPONSE);
        
        // cleanup flash
        _flash.doPostPhaseActions(facesContext);
    }
    
    /**
     * Tests if the reading functions use _getFlashMapForReading()
     * and if the writing functions use _getFlashMapForWriting().
     */
    @Test
    public void testMapMethodsUseDifferentMaps() throws Exception
    {
        // simulate Faces lifecycle:
        // note that doPrePhaseActions() only performs tasks on RESTORE_VIEW
        // and doPostPhaseActions() only on RENDER_RESPONSE.
        
        // initial request ----------------------------------------------------
        
        // this request is a normal GET request, and thus not a postback
        ((MockFacesContext20) facesContext).setPostback(false);
        
        // simulate Faces lifecycle
        facesContext.setCurrentPhaseId(PhaseId.RESTORE_VIEW);
        _flash.doPrePhaseActions(facesContext);
        
        // simulate Faces lifecycle
        facesContext.setCurrentPhaseId(PhaseId.RENDER_RESPONSE);
        _flash.doPostPhaseActions(facesContext);
        
        // first postback -----------------------------------------------------
        
        // simulate a new request
        _simulateNewRequest();
        
        // this request should be a postback
        ((MockFacesContext20) facesContext).setPostback(true);
        
        // simulate Faces lifecycle
        facesContext.setCurrentPhaseId(PhaseId.RESTORE_VIEW);
        _flash.doPrePhaseActions(facesContext);
        
        // simulate Faces lifecycle
        facesContext.setCurrentPhaseId(PhaseId.RENDER_RESPONSE);
        
        // in this configuration put() and get() are executed on different maps
        
        // there must not be a value with the key "flashkey"
        Assert.assertNull(_flash.get("flashkey"));
        
        // put() always references the active FlashMap,
        // which is the render FlashMap in this case (phase is render response)
        _flash.put("flashkey", "flashvalue");
        
        // there must still not be a value with the key "flashkey"
        // NOTE that get still references the execute FlashMap
        Assert.assertNull(_flash.get("flashkey"));
        
        _flash.doPostPhaseActions(facesContext);
    }
    
    /**
     * Tests the implementation of the methods from the java.util.Map interface.
     */
    @Test
    public void testMapMethods()
    {
        // ensure that _getActiveFlashMap() returns the execute FlashMap
        facesContext.setCurrentPhaseId(PhaseId.RESTORE_VIEW);
        
        // run assertions for an empty FlashMap
        _noElementAssertions();
        
        // use put() to put a value into the map
        _flash.put("flashkey", "flashvalue");
        
        // run assertions for the FlashMap with one element
        _oneElementAssertions();
        
        // remove the key using remove();
        _flash.remove("flashkey");
        
        _noElementAssertions();

        // use putAll() to put a value into the map
        Map<String, Object> map = new HashMap<String, Object>();
        map.put("flashkey", "flashvalue");
        _flash.putAll(map);
        
        _oneElementAssertions();
        
        // use clear() to remove the value from the map
        _flash.clear();
        
        _noElementAssertions();
        
        // put the value into the map again
        _flash.put("flashkey", "flashvalue");
        
        _oneElementAssertions();
        
        // use the keySet to clear the map
        _flash.keySet().clear();
        
        _noElementAssertions();
    }
    
    /**
     * Utility method used by testMapMethods()
     */
    private void _noElementAssertions()
    {
        Assert.assertTrue(_flash.isEmpty());
        Assert.assertEquals(0, _flash.size());
        Assert.assertFalse(_flash.containsKey("flashkey"));
        Assert.assertFalse(_flash.containsValue("flashvalue"));
        Assert.assertEquals(Collections.emptySet(), _flash.keySet());
        Assert.assertNull(_flash.get("flashkey"));
        Assert.assertTrue(_flash.values().isEmpty());
    }
    
    /**
     * Utility method used by testMapMethods()
     */
    private void _oneElementAssertions()
    {
        Assert.assertFalse(_flash.isEmpty());
        Assert.assertEquals(1, _flash.size());
        Assert.assertTrue(_flash.containsKey("flashkey"));
        Assert.assertTrue(_flash.containsValue("flashvalue"));
        Assert.assertEquals(new HashSet<String>(Arrays.asList("flashkey")), _flash.keySet());
        Assert.assertEquals("flashvalue", _flash.get("flashkey"));
        Assert.assertTrue(_flash.values().contains("flashvalue"));
    }
    
    /**
     * Create new request, response, ExternalContext and FacesContext
     * to simulate a new request. Also resend any Cookies added to the
     * current request by the Flash implementation.
     * 
     * @throws Exception
     */
    private void _simulateNewRequest() throws Exception
    {
        // we will now have a cookie with the token for the new request
        Cookie renderTokenCookie = response.getCookie(FlashImpl.FLASH_RENDER_MAP_TOKEN);
        
        // the Cookie must exist
        Assert.assertNotNull(renderTokenCookie);
        
        // check for the redirect-cookie
        Cookie redirectCookie = response.getCookie(FlashImpl.FLASH_REDIRECT);
        
        // create new request, response, ExternalContext and FacesContext
        // to simulate a new request
        request = new MockHttpServletRequest(session);
        request.setServletContext(servletContext);
        response = new MockHttpServletResponse();
        setUpExternalContext();
        setUpFacesContext();
        
        facesContext.setApplication(application);
        
        // add the cookie to the new request
        request.addCookie(renderTokenCookie);
        
        // add the redirect-cookie to the new request, if exists
        if (redirectCookie != null)
        {
            // maxage == 0 means remove the cookie
            if (redirectCookie.getMaxAge() != 0)
            {
                request.addCookie(redirectCookie);
            }
        }
    }

    /**
     * Adds isSecure() implementation to MockExternalContext20.
     *
     * TODO remove this one as soon as MyFaces-Test provides MockExternalContext21.
     */
    private static class MockExternalContext21 extends MockExternalContext20
    {

        private boolean secure = false;

        private MockExternalContext21(ServletContext context, HttpServletRequest request, HttpServletResponse response)
        {
            super(context, request, response);
        }

        @Override
        public boolean isSecure()
        {
            return secure;
        }

        public void setSecure(boolean secure)
        {
            this.secure = secure;
        }

        @Override
        public void release() {
            
        }
    }

}
