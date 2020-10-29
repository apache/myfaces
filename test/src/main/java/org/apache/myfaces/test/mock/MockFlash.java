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

package org.apache.myfaces.test.mock;

import jakarta.faces.application.FacesMessage;
import jakarta.faces.context.ExternalContext;
import jakarta.faces.context.FacesContext;
import jakarta.faces.context.Flash;
import jakarta.faces.event.PhaseId;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.io.Serializable;
import java.math.BigInteger;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicLong;

/**
 * <p>Mock implementation of <code>Flash</code>.</p>
 * <p/>
 * $Id$
 *
 * @since 1.0.0
 */
public class MockFlash extends Flash
{

    /**
     * Key on app map to keep current instance
     */
    static protected final String FLASH_INSTANCE = MockFlash.class.getName()
            + ".INSTANCE";

    /**
     * Key used to check if there is the current request will be or was redirected
     */
    static protected final String FLASH_REDIRECT = MockFlash.class.getName()
            + ".REDIRECT";

    /**
     * Key used to check if this request should keep messages (like tomahawk sandbox RedirectTracker.
     * Used when post-redirect-get pattern is used)
     */
    static protected final String FLASH_KEEP_MESSAGES = MockFlash.class
            .getName()
            + ".KEEP_MESSAGES";

    static protected final String FLASH_KEEP_MESSAGES_LIST = "KEEPMESSAGESLIST";

    /**
     * Session map prefix to flash maps
     */
    static protected final String FLASH_SCOPE_CACHE = MockFlash.class.getName()
            + ".SCOPE";

    static protected final String FLASH_CURRENT_MAP_CACHE = MockFlash.class
            .getName()
            + ".CURRENTMAP.CACHE";

    static protected final String FLASH_CURRENT_MAP_KEY = MockFlash.class
            .getName()
            + ".CURRENTMAP.KEY";

    static protected final String FLASH_POSTBACK_MAP_CACHE = MockFlash.class
            .getName()
            + ".POSTBACKMAP.CACHE";

    static protected final String FLASH_POSTBACK_MAP_KEY = MockFlash.class
            .getName()
            + ".POSTBACKMAP.KEY";

    static private final char SEPARATOR_CHAR = '.';

    // the current token value
    private final AtomicLong _count;

    public MockFlash()
    {
        _count = new AtomicLong(_getSeed());
    }

    /**
     * @return a cryptographically secure random number to use as the _count seed
     */
    private static long _getSeed()
    {
        SecureRandom rng;
        try
        {
            // try SHA1 first
            rng = SecureRandom.getInstance("SHA1PRNG");
        }
        catch (NoSuchAlgorithmException e)
        {
            // SHA1 not present, so try the default (which could potentially not be
            // cryptographically secure)
            rng = new SecureRandom();
        }

        // use 48 bits for strength and fill them in
        byte[] randomBytes = new byte[6];
        rng.nextBytes(randomBytes);

        // convert to a long
        return new BigInteger(randomBytes).longValue();
    }

    /**
     * @return the next token to be assigned to this request
     */
    protected String _getNextToken()
    {
        // atomically increment the value
        long nextToken = _count.incrementAndGet();

        // convert using base 36 because it is a fast efficient subset of base-64
        return Long.toString(nextToken, 36);
    }

    /**
     * Return a Flash instance from the application map
     *
     * @param context
     * @return
     */
    public static Flash getCurrentInstance(ExternalContext context)
    {
        Map<String, Object> applicationMap = context.getApplicationMap();
        Flash flash = (Flash) applicationMap.get(FLASH_INSTANCE);

        synchronized (applicationMap)
        {
            if (flash == null)
            {
                flash = new MockFlash();
                context.getApplicationMap().put(FLASH_INSTANCE, flash);
            }
        }
        return flash;
    }

    /**
     * Return a wrapper from the session map used to implement flash maps
     * for more information see SubKeyMap doc
     */
    @SuppressWarnings("unchecked")
    private Map<String, Object> _getMapFromSession(FacesContext context,
            String token, boolean createIfNeeded)
    {
        ExternalContext external = context.getExternalContext();
        Object session = external.getSession(true);

        Map<String, Object> map = null;

        // Synchronize on the session object to ensure that
        // we don't ever create two different caches
        synchronized (session)
        {
            map = (Map<String, Object>) external.getSessionMap().get(token);
            if ((map == null) && createIfNeeded)
            {
                map = new MockSubKeyMap<Object>(context.getExternalContext()
                        .getSessionMap(), token);
            }
        }

        return map;
    }

    /**
     * Return the flash map created on this traversal. This one will be sent
     * on next request, so it will be retrieved as postback map of the next
     * request.
     * <p/>
     * Note it is supposed that FLASH_CURRENT_MAP_KEY is initialized before
     * restore view phase (see doPrePhaseActions() for details).
     *
     * @param context
     * @return
     */
    @SuppressWarnings("unchecked")
    protected Map<String, Object> getCurrentRequestMap(FacesContext context)
    {
        Map<String, Object> requestMap = context.getExternalContext()
                .getRequestMap();
        Map<String, Object> map = (Map<String, Object>) requestMap
                .get(FLASH_CURRENT_MAP_CACHE);
        if (map == null)
        {
            String token = (String) requestMap.get(FLASH_CURRENT_MAP_KEY);
            String fullToken = FLASH_SCOPE_CACHE + SEPARATOR_CHAR + token;
            map = _getMapFromSession(context, fullToken, true);
            requestMap.put(FLASH_CURRENT_MAP_CACHE, map);
        }
        return map;
    }

    @SuppressWarnings("unchecked")
    protected Map<String, Object> getPostbackRequestMap(FacesContext context)
    {
        Map<String, Object> requestMap = context.getExternalContext()
                .getRequestMap();
        Map<String, Object> map = (Map<String, Object>) requestMap
                .get(FLASH_POSTBACK_MAP_CACHE);
        if (map == null)
        {
            String token = (String) requestMap.get(FLASH_POSTBACK_MAP_KEY);
            if (token == null && isRedirect())
            {
                // In post-redirect-get, request values are reset, so we need
                // to get the postback key again.
                token = _getPostbackMapKey(context.getExternalContext());
            }
            String fullToken = FLASH_SCOPE_CACHE + SEPARATOR_CHAR + token;
            map = _getMapFromSession(context, fullToken, true);
            requestMap.put(FLASH_POSTBACK_MAP_CACHE, map);
        }
        return map;
    }

    /**
     * Get the proper map according to the current phase:
     * <p/>
     * Normal case:
     * <p/>
     * - First request, restore view phase (create a new one): current map n
     * - First request, execute phase: Skipped
     * - First request, render  phase: current map n
     * <p/>
     * Current map n saved and put as postback map n
     * <p/>
     * - Second request, execute phase: postback map n
     * - Second request, render  phase: current map n+1
     * <p/>
     * Post Redirect Get case: Redirect is triggered by a call to setRedirect(true) from NavigationHandler
     * or earlier using c:set tag.
     * <p/>
     * - First request, restore view phase (create a new one): current map n
     * - First request, execute phase: Skipped
     * - First request, render  phase: current map n
     * <p/>
     * Current map n saved and put as postback map n
     * <p/>
     * POST
     * <p/>
     * - Second request, execute phase: postback map n
     * <p/>
     * REDIRECT
     * <p/>
     * - NavigationHandler do the redirect, requestMap data lost, called Flash.setRedirect(true)
     * <p/>
     * Current map n saved and put as postback map n
     * <p/>
     * GET
     * <p/>
     * - Third  request, restore view phase (create a new one): current map n+1
     * (isRedirect() should return true as javadoc says)
     * - Third  request, execute phase: skipped
     * - Third  request, render  phase: current map n+1
     * <p/>
     * In this way proper behavior is preserved even in the case of redirect, since the GET part is handled as
     * the "render" part of the current traversal, keeping the semantic of flash object.
     *
     * @return
     */
    private Map<String, Object> getCurrentPhaseMap()
    {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        if (PhaseId.RENDER_RESPONSE.equals(facesContext.getCurrentPhaseId())
                || !facesContext.isPostback() || isRedirect())
        {
            return getCurrentRequestMap(facesContext);
        }
        else
        {
            return getPostbackRequestMap(facesContext);
        }
    }

    private void _removeAllChildren(FacesContext facesContext)
    {
        Map<String, Object> map = getPostbackRequestMap(facesContext);

        // Clear everything - note that because of naming conventions,
        // this will in fact automatically recurse through all children
        // grandchildren etc. - which is kind of a design flaw of SubKeyMap,
        // but one we're relying on
        map.clear();
    }

    /**
     *
     */
    @Override
    public void doPrePhaseActions(FacesContext facesContext)
    {
        Map<String, Object> requestMap = facesContext.getExternalContext()
                .getRequestMap();

        if (PhaseId.RESTORE_VIEW.equals(facesContext.getCurrentPhaseId()))
        {
            // Generate token and put on requestMap
            // It is necessary to set this token always, because on the next request
            // it should be possible to change postback map.
            String currentToken = _getNextToken();
            requestMap.put(FLASH_CURRENT_MAP_KEY, currentToken);

            if (facesContext.isPostback())
            {
                //Retore token
                String previousToken = _getPostbackMapKey(facesContext
                        .getExternalContext());

                if (previousToken != null)
                {
                    requestMap.put(FLASH_POSTBACK_MAP_KEY, previousToken);
                }
            }

            if (isKeepMessages())
            {
                restoreMessages(facesContext);
            }
        }

        //
        if (PhaseId.RENDER_RESPONSE.equals(facesContext.getCurrentPhaseId()))
        {
            // Put current map on next previous map
            // but only if this request is not a redirect
            if (!isRedirect())
            {
                _addPostbackMapKey(facesContext.getExternalContext());
            }
        }
    }

    @Override
    public void doPostPhaseActions(FacesContext facesContext)
    {
        if (PhaseId.RENDER_RESPONSE.equals(facesContext.getCurrentPhaseId()))
        {
            //Remove previous flash from session
            Map<String, Object> requestMap = facesContext.getExternalContext()
                    .getRequestMap();
            String token = (String) requestMap.get(FLASH_POSTBACK_MAP_KEY);

            if (token != null)
            {
                _removeAllChildren(facesContext);
            }

            if (isKeepMessages())
            {
                saveMessages(facesContext);
            }
        }
        else if (isRedirect()
                && (facesContext.getResponseComplete() || facesContext
                        .getRenderResponse()))
        {
            if (isKeepMessages())
            {
                saveMessages(facesContext);
            }
        }
    }

    private static class MessageEntry implements Serializable
    {
        private final Object clientId;
        private final Object message;

        public MessageEntry(Object clientId, Object message)
        {
            this.clientId = clientId;
            this.message = message;
        }
    }

    protected void saveMessages(FacesContext facesContext)
    {
        List<MessageEntry> messageList = null;

        Iterator<String> iterClientIds = facesContext
                .getClientIdsWithMessages();
        while (iterClientIds.hasNext())
        {
            String clientId = (String) iterClientIds.next();
            Iterator<FacesMessage> iterMessages = facesContext
                    .getMessages(clientId);

            while (iterMessages.hasNext())
            {
                FacesMessage message = iterMessages.next();

                if (messageList == null)
                {
                    messageList = new ArrayList<MessageEntry>();
                }
                messageList.add(new MessageEntry(clientId, message));
            }
        }

        if (messageList != null)
        {
            if (isRedirect())
            {
                getPostbackRequestMap(facesContext).put(
                        FLASH_KEEP_MESSAGES_LIST, messageList);
            }
            else
            {
                getCurrentRequestMap(facesContext).put(
                        FLASH_KEEP_MESSAGES_LIST, messageList);
            }
        }
    }

    protected void restoreMessages(FacesContext facesContext)
    {
        Map<String, Object> postbackMap = getPostbackRequestMap(facesContext);
        List<MessageEntry> messageList = (List<MessageEntry>) postbackMap
                .get(FLASH_KEEP_MESSAGES_LIST);

        if (messageList != null)
        {
            Iterator iterMessages = messageList.iterator();

            while (iterMessages.hasNext())
            {
                MessageEntry message = (MessageEntry) iterMessages.next();
                facesContext.addMessage((String) message.clientId,
                        (FacesMessage) message.message);
            }

            postbackMap.remove(FLASH_KEEP_MESSAGES_LIST);
        }
    }

    /**
     * Retrieve the postback map key
     */
    private String _getPostbackMapKey(ExternalContext externalContext)
    {
        String token = null;
        Object response = externalContext.getResponse();
        if (response instanceof HttpServletResponse)
        {
            //Use a cookie
            Cookie cookie = (Cookie) externalContext.getRequestCookieMap().get(
                    FLASH_POSTBACK_MAP_KEY);
            if (cookie != null)
            {
                token = cookie.getValue();
            }
        }
        else
        {
            //Use HttpSession or PortletSession object
            Map<String, Object> sessionMap = externalContext.getSessionMap();
            token = (String) sessionMap.get(FLASH_POSTBACK_MAP_KEY);
        }
        return token;
    }

    /**
     * Take the current map key and store it as a postback key for the next request.
     *
     * @param externalContext
     */
    private void _addPostbackMapKey(ExternalContext externalContext)
    {
        Object response = externalContext.getResponse();
        String token = (String) externalContext.getRequestMap().get(
                FLASH_CURRENT_MAP_KEY);

        //Use HttpSession or PortletSession object
        Map<String, Object> sessionMap = externalContext.getSessionMap();
        sessionMap.put(FLASH_POSTBACK_MAP_KEY, token);
    }

    /**
     * For check if there is a redirect we to take into accout this points:
     * <p/>
     * 1. isRedirect() could be accessed many times during the same
     * request.
     * 2. According to Post-Redirect-Get pattern, we cannot
     * ensure request scope values are preserved.
     */
    @Override
    public boolean isRedirect()
    {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        ExternalContext externalContext = facesContext.getExternalContext();
        Map<String, Object> requestMap = externalContext.getRequestMap();
        Boolean redirect = (Boolean) requestMap.get(FLASH_REDIRECT);
        if (redirect == null)
        {
            Object response = externalContext.getResponse();
            if (response instanceof HttpServletResponse)
            {
                // Request values are lost after a redirect. We can create a
                // temporal cookie to pass the params between redirect calls.
                // It is better than use HttpSession object, because this cookie
                // is never sent by the server.
                Cookie cookie = (Cookie) externalContext.getRequestCookieMap()
                        .get(FLASH_REDIRECT);
                if (cookie != null)
                {
                    redirect = Boolean.TRUE;
                    HttpServletResponse httpResponse = (HttpServletResponse) response;
                    // A redirect happened, so it is safe to remove the cookie, setting
                    // the maxAge to 0 seconds. The effect is we passed FLASH_REDIRECT param
                    // to this request object
                    cookie.setMaxAge(0);
                    cookie.setValue(null);
                    httpResponse.addCookie(cookie);
                    requestMap.put(FLASH_REDIRECT, redirect);
                }
                else
                {
                    redirect = Boolean.FALSE;
                }
            }
            else
            {
                // Note that on portlet world we can't create cookies,
                // so we are forced to use the session map. Anyway,
                // according to the Bridge implementation(for example see
                // org.apache.myfaces.portlet.faces.bridge.BridgeImpl)
                // session object is created at start faces request
                Map<String, Object> sessionMap = externalContext
                        .getSessionMap();
                redirect = (Boolean) sessionMap.get(FLASH_REDIRECT);
                if (redirect != null)
                {
                    sessionMap.remove(FLASH_REDIRECT);
                    requestMap.put(FLASH_REDIRECT, redirect);
                }
                else
                {
                    redirect = Boolean.FALSE;
                }
            }
        }
        return redirect;
    }

    @Override
    public void setRedirect(boolean redirect)
    {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        ExternalContext externalContext = facesContext.getExternalContext();
        Map<String, Object> requestMap = externalContext.getRequestMap();

        Boolean previousRedirect = (Boolean) requestMap.get(FLASH_REDIRECT);
        previousRedirect = (previousRedirect == null) ? Boolean.FALSE
                : previousRedirect;

        if (!previousRedirect.booleanValue() && redirect)
        {
            // This request contains a redirect. This condition is in general
            // triggered by a NavigationHandler. After a redirect all request scope
            // values get lost, so in order to preserve this value we need to
            // pass it between request. One strategy is use a cookie that is never sent
            // to the client. Other alternative is use the session map.
            externalContext.getSessionMap().put(FLASH_REDIRECT, redirect);
        }
        requestMap.put(FLASH_REDIRECT, redirect);
    }

    /**
     * In few words take a value from request scope map and put it on current request map,
     * so it is visible on the next request.
     */
    @Override
    public void keep(String key)
    {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        Map<String, Object> requestMap = facesContext.getExternalContext()
                .getRequestMap();
        Object value = requestMap.get(key);
        getCurrentRequestMap(facesContext).put(key, value);
    }

    /**
     * This is just an alias for request scope map.
     */
    @Override
    public void putNow(String key, Object value)
    {
        FacesContext.getCurrentInstance().getExternalContext().getRequestMap()
                .put(key, value);
    }

    @Override
    public boolean isKeepMessages()
    {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        ExternalContext externalContext = facesContext.getExternalContext();
        Map<String, Object> requestMap = externalContext.getRequestMap();
        Boolean keepMessages = (Boolean) requestMap.get(FLASH_KEEP_MESSAGES);
        if (keepMessages == null)
        {
            Object response = externalContext.getResponse();
            if (response instanceof HttpServletResponse)
            {
                // Request values are lost after a redirect. We can create a
                // temporal cookie to pass the params between redirect calls.
                // It is better than use HttpSession object, because this cookie
                // is never sent by the server.
                Cookie cookie = (Cookie) externalContext.getRequestCookieMap()
                        .get(FLASH_KEEP_MESSAGES);
                if (cookie != null)
                {
                    keepMessages = Boolean.TRUE;
                    HttpServletResponse httpResponse = (HttpServletResponse) response;
                    // It is safe to remove the cookie, setting
                    // the maxAge to 0 seconds. The effect is we passed FLASH_KEEP_MESSAGES param
                    // to this request object
                    cookie.setMaxAge(0);
                    cookie.setValue(null);
                    httpResponse.addCookie(cookie);
                    requestMap.put(FLASH_KEEP_MESSAGES, keepMessages);
                }
                else
                {
                    keepMessages = Boolean.FALSE;
                }
            }
            else
            {
                // Note that on portlet world we can't create cookies,
                // so we are forced to use the session map. Anyway,
                // according to the Bridge implementation(for example see
                // org.apache.myfaces.portlet.faces.bridge.BridgeImpl)
                // session object is created at start faces request
                Map<String, Object> sessionMap = externalContext
                        .getSessionMap();
                keepMessages = (Boolean) sessionMap.get(FLASH_KEEP_MESSAGES);
                if (keepMessages != null)
                {
                    sessionMap.remove(FLASH_KEEP_MESSAGES);
                    requestMap.put(FLASH_KEEP_MESSAGES, keepMessages);
                }
                else
                {
                    keepMessages = Boolean.FALSE;
                }
            }
        }
        return keepMessages;
    }

    /**
     * If this property is true, the messages should be keep for the next request, no matter
     * if it is a normal postback case or a post-redirect-get case.
     */
    @Override
    public void setKeepMessages(boolean keepMessages)
    {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        ExternalContext externalContext = facesContext.getExternalContext();
        Map<String, Object> requestMap = externalContext.getRequestMap();

        Boolean previousKeepMessages = (Boolean) requestMap
                .get(FLASH_KEEP_MESSAGES);
        previousKeepMessages = (previousKeepMessages == null) ? Boolean.FALSE
                : previousKeepMessages;

        if (!previousKeepMessages.booleanValue() && keepMessages)
        {
            externalContext.getSessionMap().put(FLASH_KEEP_MESSAGES,
                    keepMessages);
        }
        requestMap.put(FLASH_KEEP_MESSAGES, keepMessages);
    }

    public void clear()
    {
        getCurrentPhaseMap().clear();
    }

    public boolean containsKey(Object key)
    {
        return getCurrentPhaseMap().containsKey(key);
    }

    public boolean containsValue(Object value)
    {
        return getCurrentPhaseMap().containsValue(value);
    }

    public Set<Entry<String, Object>> entrySet()
    {
        return getCurrentPhaseMap().entrySet();
    }

    public Object get(Object key)
    {
        if (key == null)
        {
            return null;
        }

        if ("keepMessages".equals(key))
        {
            return isKeepMessages();
        }
        else if ("redirect".equals(key))
        {
            return isRedirect();
        }

        FacesContext context = FacesContext.getCurrentInstance();
        Map<String, Object> postbackMap = getPostbackRequestMap(context);
        Object returnValue = null;

        if (postbackMap != null)
        {
            returnValue = postbackMap.get(key);
        }

        return returnValue;
    }

    public boolean isEmpty()
    {
        return getCurrentPhaseMap().isEmpty();
    }

    public Set<String> keySet()
    {
        return getCurrentPhaseMap().keySet();
    }

    public Object put(String key, Object value)
    {
        if (key == null)
        {
            return null;
        }

        if ("keepMessages".equals(key))
        {
            Boolean booleanValue = convertToBoolean(value);
            this.setKeepMessages(booleanValue);
            return booleanValue;
        }
        else if ("redirect".equals(key))
        {
            Boolean booleanValue = convertToBoolean(value);
            this.setRedirect(booleanValue);
            return booleanValue;
        }
        else
        {
            Object returnValue = getCurrentPhaseMap().put(key, value);
            return returnValue;
        }
    }

    private Boolean convertToBoolean(Object value)
    {
        Boolean booleanValue;
        if (value instanceof Boolean)
        {
            booleanValue = (Boolean) value;
        }
        else
        {
            booleanValue = Boolean.parseBoolean(value.toString());
        }
        return booleanValue;
    }

    public void putAll(Map<? extends String, ? extends Object> m)
    {
        getCurrentPhaseMap().putAll(m);
    }

    public Object remove(Object key)
    {
        return getCurrentPhaseMap().remove(key);
    }

    public int size()
    {
        return getCurrentPhaseMap().size();
    }

    public Collection<Object> values()
    {
        return getCurrentPhaseMap().values();
    }

}
