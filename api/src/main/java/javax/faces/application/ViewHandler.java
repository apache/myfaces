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
package javax.faces.application;

import javax.faces.FacesException;
import javax.faces.context.ExternalContext;

import java.io.UnsupportedEncodingException;
import java.util.Locale;

/**
 * A ViewHandler manages the component-tree-creation and component-tree-rendering parts
 * of a request lifecycle (ie "create view", "restore view" and "render response").
 * <p>
 * A ViewHandler is responsible for generating the component tree when a new view is
 * requested; see method "createView".
 * <p>
 * When the user performs a "postback", ie activates a UICommand component within a view,
 * then the ViewHandler is responsible for recreating a view tree identical to the one
 * used previously to render that view; see method "restoreView".
 * <p>
 * And the ViewHandler is also responsible for rendering the final output to be sent to the
 * user by invoking the rendering methods on components; see method "renderView".
 * <p>
 * This class also isolates callers from the underlying request/response system. In particular,
 * this class does not explicitly depend upon the javax.servlet apis. This allows JSF to be
 * used on servers that do not implement the servlet API (for example, plain CGI). 
 * <p>
 * Examples:
 * <ul>
 * <li>A JSP ViewHandler exists for using "jsp" pages as the presentation technology.
 * This class then works together with a taghandler class and a jsp servlet class to
 * implement the methods on this abstract class definition.
 * <li>A Facelets ViewHandler instead uses an xml file to define the components and
 * non-component data that make up a specific view. 
 * </ul>
 * Of course there is no reason why the "template" needs to be a textual file. A view could
 * be generated based on data in a database, or many other mechanisms.
 * <p>
 * This class is expected to be invoked via the concrete implementation of 
 * {@link javax.faces.lifecycle.Lifecycle}.
 * <p>
 * For the official specification for this class, see 
 * <a href="http://java.sun.com/javaee/javaserverfaces/1.2/docs/api/index.html">JSF Specification</a>.
 *
 * @author Manfred Geiler (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
public abstract class ViewHandler
{
    public static final String CHARACTER_ENCODING_KEY = "javax.faces.request.charset";
    public static final String DEFAULT_SUFFIX_PARAM_NAME = "javax.faces.DEFAULT_SUFFIX";
    public static final String DEFAULT_SUFFIX = ".jsp";

    /**
     * @since JSF 1.2
     */
    public String calculateCharacterEncoding(javax.faces.context.FacesContext context)
    {
        String _encoding = null;
        ExternalContext externalContext = context.getExternalContext();
        String _contentType = (String) externalContext.getRequestHeaderMap().get("Content-Type");
        int _indexOf = _contentType == null ? -1 :_contentType.indexOf("charset");
        if(_indexOf != -1)
        {
            String _tempEnc =_contentType.substring(_indexOf); //charset=UTF-8 
            _encoding = _tempEnc.substring(_tempEnc.indexOf("=")+1); //UTF-8
        }
        else 
        {
            boolean _sessionAvailable = externalContext.getSession(false) != null;
            if(_sessionAvailable)
            {
                Object _sessionParam = externalContext.getSessionMap().get(CHARACTER_ENCODING_KEY); 
                if (_sessionParam != null)
                {
                    _encoding = _sessionParam.toString();
                }
            }
        }
        
        return _encoding;
    }
    
    /**
     * Return the Locale object that should be used when rendering this view to the
     * current user.
     * <p>
     * Some request protocols allow an application user to specify what locale they prefer
     * the response to be in. For example, HTTP requests can specify the "accept-language"
     * header.
     * <p>
     * Method {@link javax.faces.application.Application#getSupportedLocales()} defines
     * what locales this JSF application is capable of supporting.
     * <p>
     * This method should match such sources of data up and return the Locale object that
     * is the best choice for rendering the current application to the current user.
     */
    public abstract Locale calculateLocale(javax.faces.context.FacesContext context);

    /**
     * Return the id of an available render-kit that should be used to map the JSF
     * components into user presentation.
     * <p>
     * The render-kit selected (eg html, xhtml, pdf, xul, ...) may depend upon the user,
     * properties associated with the request, etc.
     */
    public abstract String calculateRenderKitId(javax.faces.context.FacesContext context);

    /**
     * Build a root node for a component tree.
     * <p>
     * When a request is received, this method is called if restoreView returns null,
     * ie this is not a "postback". In this case, a root node is created and then renderView
     * is invoked. It is the responsibility of the renderView method to build the full
     * component tree (ie populate the UIViewRoot with descendant nodes).
     * <p>
     * This method is also invoked when navigation occurs from one view to another, where
     * the viewId passed is the id of the new view to be displayed. Again it is the responsibility
     * of renderView to then populate the viewroot with descendants.
     */
    public abstract javax.faces.component.UIViewRoot createView(javax.faces.context.FacesContext context,
                                                                String viewId);

    /**
     * Return a URL that a remote system can invoke in order to access the specified view.
     */
    public abstract String getActionURL(javax.faces.context.FacesContext context,
                                        String viewId);

    /**
     * Return a URL that a remote system can invoke in order to access the specified resource..
     */
    public abstract String getResourceURL(javax.faces.context.FacesContext context,
                                          String path);
    
    /**
     * Method must be called by the JSF impl at the beginning of Phase <i>Restore View</i> of the JSF
     * lifecycle.
     * 
     * @since JSF 1.2
     */
    public void initView(javax.faces.context.FacesContext context) throws FacesException
    {
        String _encoding = this.calculateCharacterEncoding(context);
        if(_encoding != null)
        {
            try
            {
                context.getExternalContext().setRequestCharacterEncoding(_encoding);
            }
            catch(UnsupportedEncodingException uee)
            {
                throw new FacesException(uee);
            }
        }
    }
    
    /**
     * Combine the output of all the components in the viewToRender with data from the
     * original view template (if any) and write the result to context.externalContext.response.
     * <p>
     * Component output is generated by invoking the encodeBegin, encodeChildren (optional)
     * and encodeEnd methods on relevant components in the specified view. How this is
     * interleaved with the non-component content of the view template (if any) is left
     * to the ViewHandler implementation.
     * <p>
     * The actual type of the Response object depends upon the concrete implementation of
     * this class. It will almost certainly be an OutputStream of some sort, but may be
     * specific to the particular request/response  system in use.
     * <p>
     * If the view cannot be rendered (eg due to an error in a component) then a FacesException
     * is thrown.
     * <p>
     * Note that if a "postback" has occurred but no navigation to a different view, then
     * the viewToRender will be fully populated with components already. However on direct
     * access to a new view, or when navigation has occurred, the viewToRender will just
     * contain an empty UIViewRoot object that must be populated with components from
     * the "view template". 
     */
    public abstract void renderView(javax.faces.context.FacesContext context,
                                    javax.faces.component.UIViewRoot viewToRender)
            throws java.io.IOException,
            FacesException;

    /**
     * Handle a "postback" request by recreating the component tree that was most recently
     * presented to the user for the specified view.
     * <p>
     * When the user performs a "postback" of a view that has previously been created, ie
     * is updating the state of an existing view tree, then the view handler must recreate
     * a view tree identical to the one used previously to render that view to the user,
     * so that the data received from the user can be compared to the old state and the
     * correct "changes" detected (well, actually only those components that respond to
     * input are actually needed before the render phase). 
     * <p>
     * The components in this tree will then be given the opportunity to examine new input
     * data provided by the user, and react in the appropriate manner for that component,
     * as specified for the JSF lifecycle.
     * <p>
     * Much of the work required by this method <i>must</i> be delegated to an instance
     * of StateManager.
     * <p>
     * If there is no record of the current user having been sent the specified view
     * (ie no saved state information available), then NULL should be returned.
     * <p>
     * Note that input data provided by the user may also be used to determine exactly
     * how to restore this view. In the case of "client side state", information
     * about the components to be restored will be available here. Even for
     * "server side state", user input may include an indicator that is used to
     * select among a number of different saved states available for this viewId and
     * this user.
     * <p>
     * Note that data received from users is inherently untrustworthy; care should be
     * taken to validate this information appropriately.
     * <p>
     * See writeState for more details. 
     */
    public abstract javax.faces.component.UIViewRoot restoreView(javax.faces.context.FacesContext context,
                                                                 String viewId);

    /**
     * Write sufficient information to context.externalContext.response in order to
     * be able to restore this view if the user performs a "postback" using that
     * rendered response.
     * <p>
     * For "client side state saving", sufficient information about the view
     * state should be written to allow a "restore view" operation to succeed 
     * later. This does not necessarily mean storing <i>all</i> data about the
     * current view; only data that cannot be recreated from the "template" for
     * this view needs to be saved.
     * <p>
     * For "server side state saving", this method may write out nothing. Alternately
     * it may write out a "state identifier" to identify which of multiple saved
     * user states for a particular view should be selected (or just verify that the
     * saved state does indeed correspond to the expected one).
     */
    public abstract void writeState(javax.faces.context.FacesContext context)
            throws java.io.IOException;
}
