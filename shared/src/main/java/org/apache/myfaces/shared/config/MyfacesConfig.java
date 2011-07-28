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
package org.apache.myfaces.shared.config;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.faces.context.ExternalContext;
import javax.servlet.ServletContext;

import org.apache.myfaces.buildtools.maven2.plugin.builder.annotation.JSFWebConfigParam;
import org.apache.myfaces.shared.util.ClassUtils;

/**
 * Holds all configuration init parameters (from web.xml) that are independent
 * from the core implementation. The parameters in this class are available to
 * all shared, component and implementation classes.
 * See RuntimeConfig for configuration infos that come from the faces-config
 * files and are needed by the core implementation.
 *
 * MyfacesConfig is meant for components that implement some of the extended features
 * of MyFaces. Anyhow, using the MyFaces JSF implementation is no precondition for using
 * MyfacesConfig in custom components. Upon using another JSF implementation
 * (or omitting the extended init parameters) all config properties will simply have
 * their default values.
 *
 * @author Manfred Geiler (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
public class MyfacesConfig
{
    //private static final Log log = LogFactory.getLog(MyfacesConfig.class);
    private static final Logger log = Logger.getLogger(MyfacesConfig.class.getName());

    private static final String APPLICATION_MAP_PARAM_NAME = MyfacesConfig.class.getName();

    public static final String  INIT_PARAM_RESOURCE_VIRTUAL_PATH = "org.apache.myfaces.RESOURCE_VIRTUAL_PATH";
    public static final String  INIT_PARAM_RESOURCE_VIRTUAL_PATH_DEFAULT = "/faces/myFacesExtensionResource";

    /**
     * If true, rendered HTML code will be formatted, so that it is "human readable".
     * i.e. additional line separators and whitespace will be written, that do not
     * influence the HTML code. Default: "true"
     */
    @JSFWebConfigParam(defaultValue="true", expectedValues="true,false",since="1.1")
    private static final String  INIT_PARAM_PRETTY_HTML = "org.apache.myfaces.PRETTY_HTML";
    private static final boolean INIT_PARAM_PRETTY_HTML_DEFAULT = true;

    /**
     * This parameter tells MyFaces if javascript code should be allowed in the rendered HTML output.
     * If javascript is allowed, command_link anchors will have javascript code 
     * that submits the corresponding form.
     * If javascript is not allowed, the state saving info and nested parameters ill be 
     * added as url parameters.
     * Default: "true"
     */
    @JSFWebConfigParam(defaultValue="true", expectedValues="true,false",since="1.1")
    private static final String  INIT_PARAM_ALLOW_JAVASCRIPT = "org.apache.myfaces.ALLOW_JAVASCRIPT";
    private static final boolean INIT_PARAM_ALLOW_JAVASCRIPT_DEFAULT = true;

    /**
     * 
     */
    @JSFWebConfigParam(defaultValue="false", expectedValues="true,false",since="1.1")
    private static final String  INIT_PARAM_DETECT_JAVASCRIPT = "org.apache.myfaces.DETECT_JAVASCRIPT";
    private static final boolean INIT_PARAM_DETECT_JAVASCRIPT_DEFAULT = false;

    /**
     * If true, a javascript function will be rendered that is able to restore the 
     * former vertical scroll on every request. Convenient feature if you have pages
     * with long lists and you do not want the browser page to always jump to the top
     * if you trigger a link or button action that stays on the same page.
     * Default: "false"
     */
    @JSFWebConfigParam(defaultValue="false", expectedValues="true,false",since="1.1")
    private static final String  INIT_PARAM_AUTO_SCROLL = "org.apache.myfaces.AUTO_SCROLL";
    private static final boolean INIT_PARAM_AUTO_SCROLL_DEFAULT = false;

    /**
     * Tomahawk specific: A class implementing the
     * org.apache.myfaces.shared.renderkit.html.util.AddResource
     * interface. It is responsible to
     * place scripts and css on the right position in your HTML document.
     * Default: "org.apache.myfaces.shared.renderkit.html.util.DefaultAddResource"
     * Follow the description on the MyFaces-Wiki-Performance page to enable
     * StreamingAddResource instead of DefaultAddResource if you want to
     * gain performance.
     */
    @JSFWebConfigParam(defaultValue="org.apache.myfaces.renderkit.html.util.DefaultAddResource",since="1.1")
    private static final String INIT_PARAM_ADD_RESOURCE_CLASS = "org.apache.myfaces.ADD_RESOURCE_CLASS";
    private static final String INIT_PARAM_ADD_RESOURCE_CLASS_DEFAULT = "org.apache.myfaces.renderkit.html.util.DefaultAddResource";

    /**
     * Tomahawk specific: A very common problem in configuring MyFaces-web-applications
     * is that the Extensions-Filter is not configured at all
     * or improperly configured. This parameter will check for a properly
     * configured Extensions-Filter if it is needed by the web-app.
     * In most cases this check will work just fine, there might be cases
     * where an internal forward will bypass the Extensions-Filter and the check
     * will not work. If this is the case, you can disable the check by setting
     * this parameter to false.
     * 
     * In tomahawk for JSF 2.0 since version 1.1.11, this param is set by default to false, otherwise is true.
     */
    @JSFWebConfigParam(defaultValue="for JSF 2.0 since 1.1.11 false, otherwise true", expectedValues="true,false",since="1.1")
    private static final String  INIT_CHECK_EXTENSIONS_FILTER = "org.apache.myfaces.CHECK_EXTENSIONS_FILTER";
    private static final boolean INIT_CHECK_EXTENSIONS_FILTER_DEFAULT = false;

    /**
     * 
     */
    @JSFWebConfigParam(defaultValue="true", expectedValues="true,false",since="1.1")
    private static final String INIT_READONLY_AS_DISABLED_FOR_SELECT = "org.apache.myfaces.READONLY_AS_DISABLED_FOR_SELECTS";
    private static final boolean INIT_READONLY_AS_DISABLED_FOR_SELECT_DEFAULT = true;

    /**
     * 
     */
    @JSFWebConfigParam(defaultValue="2",since="1.1")
    public static final String INIT_PARAM_CONFIG_REFRESH_PERIOD = "org.apache.myfaces.CONFIG_REFRESH_PERIOD";
    public static final long INIT_PARAM_CONFIG_REFRESH_PERIOD_DEFAULT = 2;

    /**
     * 
     */
    @JSFWebConfigParam(defaultValue="false", expectedValues="true,false",since="1.1")
    private static final String  INIT_PARAM_VIEWSTATE_JAVASCRIPT = "org.apache.myfaces.VIEWSTATE_JAVASCRIPT";
    private static final boolean INIT_PARAM_VIEWSTATE_JAVASCRIPT_DEFAULT = false;

    /**
     * 
     */
    @JSFWebConfigParam(defaultValue="true", expectedValues="true,false",since="1.1")
    private static final String  INIT_PARAM_RENDER_VIEWSTATE_ID = "org.apache.myfaces.RENDER_VIEWSTATE_ID";
    private static final boolean INIT_PARAM_RENDER_VIEWSTATE_ID_DEFAULT = true;

    /**
     * 
     */
    @JSFWebConfigParam(defaultValue="true", expectedValues="true,false",since="1.1")
    private static final String  INIT_PARAM_STRICT_XHTML_LINKS = "org.apache.myfaces.STRICT_XHTML_LINKS";
    private static final boolean INIT_PARAM_STRICT_XHTML_LINKS_DEFAULT = true;
    
    /**
     * This param renders the clear javascript on button necessary only for
     * compatibility with hidden fields feature of myfaces. This is done 
     * because jsf ri does not render javascript on onclick method for button,
     * so myfaces should do this.
     */
    @JSFWebConfigParam(defaultValue="false", expectedValues="true,false",since="1.2.3")
    private static final String INIT_PARAM_RENDER_CLEAR_JAVASCRIPT_FOR_BUTTON = "org.apache.myfaces.RENDER_CLEAR_JAVASCRIPT_FOR_BUTTON";
    private static final boolean INIT_PARAM_RENDER_CLEAR_JAVASCRIPT_FOR_BUTTON_DEFAULT= false;

    /**
     * This param renders hidden fields at the end of h:form for link params when h:commandLink + f:param is used, instead
     * use javascript to create them. Set this param to true also enables org.apache.myfaces.RENDER_CLEAR_JAVASCRIPT_FOR_BUTTON 
     * automatically to ensure consistency. This feature is required to support Windows Mobile 6, because in this environment,
     * document.createElement() and form.appendChild() javascript methods are not supported.
     */
    @JSFWebConfigParam(defaultValue="false", expectedValues="true,false",since="1.2.9")
    private static final String INIT_PARAM_RENDER_HIDDEN_FIELDS_FOR_LINK_PARAMS = "org.apache.myfaces.RENDER_HIDDEN_FIELDS_FOR_LINK_PARAMS";
    private static final boolean INIT_PARAM_RENDER_HIDDEN_FIELDS_FOR_LINK_PARAMS_DEFAULT= false;
    
    /**
     * Add a code that save the form before submit using a
     * link (call to window.external.AutoCompleteSaveForm(form) ). It's a bug on IE.
     */
    @JSFWebConfigParam(defaultValue="false", expectedValues="true,false",since="1.1")
    private static final String INIT_PARAM_SAVE_FORM_SUBMIT_LINK_IE = "org.apache.myfaces.SAVE_FORM_SUBMIT_LINK_IE";
    private static final boolean INIT_PARAM_SAVE_FORM_SUBMIT_LINK_IE_DEFAULT = false;
    
    /**
     * 
     */
    @JSFWebConfigParam(since="1.2.7")
    private static final String INIT_PARAM_DELEGATE_FACES_SERVLET = "org.apache.myfaces.DELEGATE_FACES_SERVLET";

    /**
     * This param is only valid when partial state saving is on.
     * If this is set as true, the tag-handlers are reapplied like in facelets 1.1.x, allowing
     * c:if work correctly to "toggle" components based on a value changed on invoke application phase. 
     */
    @JSFWebConfigParam(since="2.0", defaultValue="auto", expectedValues="true,false,auto")
    public final static String INIT_PARAM_REFRESH_TRANSIENT_BUILD_ON_PSS = "org.apache.myfaces.REFRESH_TRANSIENT_BUILD_ON_PSS"; 
    public final static String INIT_PARAM_REFRESH_TRANSIENT_BUILD_ON_PSS_DEFAULT = "auto";

    /**
     * This param is only valid when partial state saving is on.
     * If this is set as true, parent components containing  c:if, c:forEach, 
     * c:choose and ui:insert with src=ELExpression are marked to be restored fully, so state
     * is preserved between request.
     */
    @JSFWebConfigParam(since="2.0", defaultValue="false")
    public final static String INIT_PARAM_REFRESH_TRANSIENT_BUILD_ON_PSS_PRESERVE_STATE = "org.apache.myfaces.REFRESH_TRANSIENT_BUILD_ON_PSS_PRESERVE_STATE";
    public final static boolean INIT_PARAM_REFRESH_TRANSIENT_BUILD_ON_PSS_PRESERVE_STATE_DEFAULT = false;
    
    /**
     * if set to <code>true</code>, tag library XML files and faces config XML files using schema 
     * will be validated during application start up
     */
    @JSFWebConfigParam(since="2.0", expectedValues="true,false")
    public final static String INIT_PARAM_VALIDATE_XML = "org.apache.myfaces.VALIDATE_XML";
    public final static boolean INIT_PARAM_VALIDATE_XML_DEFAULT = false;
    
    /**
     * Wrap content inside script with xml comment to prevent old browsers to display it. By default it is true. 
     */
    @JSFWebConfigParam(since="2.0.1", expectedValues="true,false", defaultValue="true")
    public final static String INIT_PARAM_WRAP_SCRIPT_CONTENT_WITH_XML_COMMENT_TAG = "org.apache.myfaces.WRAP_SCRIPT_CONTENT_WITH_XML_COMMENT_TAG";
    public final static boolean INIT_PARAM_WRAP_SCRIPT_CONTENT_WITH_XML_COMMENT_TAG_DEFAULT = true;
    
    /**
     * If set true, render the form submit script inline, as in myfaces core 1.2 and earlier versions 
     */
    @JSFWebConfigParam(since="2.0.2", expectedValues="true,false", defaultValue="false")
    public final static String INIT_PARAM_RENDER_FORM_SUBMIT_SCRIPT_INLINE = "org.apache.myfaces.RENDER_FORM_SUBMIT_SCRIPT_INLINE";
    public final static boolean INIT_PARAM_RENDER_FORM_SUBMIT_SCRIPT_INLINE_DEFAULT = false;
    
    /**
     * Enable/disable DebugPhaseListener feature, with provide useful information about ValueHolder variables (submittedValue, localValue, value).
     * Note evaluate those getters for each component could cause some unwanted side effects when using "access" type scopes like on MyFaces CODI.
     * This param only has effect when project stage is Development.     
     */
    @JSFWebConfigParam(since="2.0.8")
    public final static String INIT_PARAM_DEBUG_PHASE_LISTENER = "org.apache.myfaces.DEBUG_PHASE_LISTENER";
    public final static boolean INIT_PARAM_DEBUG_PHASE_LISTENER_DEFAULT = false;
    
    private boolean _prettyHtml;
    private boolean _detectJavascript;
    private boolean _allowJavascript;
    private boolean _autoScroll;
    private String _addResourceClass;
    private String _resourceVirtualPath;
    private boolean _checkExtensionsFilter;
    private boolean _readonlyAsDisabledForSelect;
    private long _configRefreshPeriod;
    private boolean _viewStateJavascript;
    private boolean _renderViewStateId;
    private boolean _strictXhtmlLinks;
    private boolean _renderClearJavascriptOnButton;
    private boolean renderHiddenFieldsForLinkParams;
    private boolean _saveFormSubmitLinkIE;
    private String _delegateFacesServlet;
    private boolean _refreshTransientBuildOnPSS;
    private boolean _refreshTransientBuildOnPSSAuto;
    private boolean refreshTransientBuildOnPSSPreserveState;
    private boolean _validateXML;
    private boolean _wrapScriptContentWithXmlCommentTag;
    private boolean _renderFormSubmitScriptInline;
    private boolean _debugPhaseListenerEnabled;

    private static final boolean TOMAHAWK_AVAILABLE;
    private static final boolean MYFACES_IMPL_AVAILABLE;
    private static final boolean RI_IMPL_AVAILABLE;

    static
    {
        boolean tomahawkAvailable;
        try
        {
            ClassUtils.classForName("org.apache.myfaces.webapp.filter.ExtensionsFilter");
            tomahawkAvailable = true;
        }
        catch (ClassNotFoundException e)
        {
            tomahawkAvailable = false;
        }
        TOMAHAWK_AVAILABLE = tomahawkAvailable;
    }

    static
    {
        boolean myfacesImplAvailable;
        try
        {
            ClassUtils.classForName("org.apache.myfaces.application.ApplicationImpl");
            myfacesImplAvailable = true;
        }
        catch (ClassNotFoundException e)
        {
            myfacesImplAvailable = false;
        }
        MYFACES_IMPL_AVAILABLE = myfacesImplAvailable;
    }

    static
    {
        boolean riImplAvailable;
        try
        {
            ClassUtils.classForName("com.sun.faces.application.ApplicationImpl");
            riImplAvailable = true;
        }
        catch (ClassNotFoundException e)
        {
            riImplAvailable = false;
        }
        RI_IMPL_AVAILABLE = riImplAvailable;
    }

    public static MyfacesConfig getCurrentInstance(ExternalContext extCtx)
    {
        MyfacesConfig myfacesConfig = (MyfacesConfig) extCtx
                .getApplicationMap().get(APPLICATION_MAP_PARAM_NAME);
        if (myfacesConfig == null)
        {

            myfacesConfig = createAndInitializeMyFacesConfig(extCtx);

            extCtx.getApplicationMap().put(APPLICATION_MAP_PARAM_NAME, myfacesConfig);

        }

        return myfacesConfig;
    }
    
    public MyfacesConfig()
    {
        setPrettyHtml(INIT_PARAM_PRETTY_HTML_DEFAULT);
        setAllowJavascript(INIT_PARAM_ALLOW_JAVASCRIPT_DEFAULT);
        setRenderClearJavascriptOnButton(INIT_PARAM_RENDER_CLEAR_JAVASCRIPT_FOR_BUTTON_DEFAULT);
        setRenderHiddenFieldsForLinkParams(INIT_PARAM_RENDER_HIDDEN_FIELDS_FOR_LINK_PARAMS_DEFAULT);
        setSaveFormSubmitLinkIE(INIT_PARAM_SAVE_FORM_SUBMIT_LINK_IE_DEFAULT);
        setReadonlyAsDisabledForSelect(INIT_READONLY_AS_DISABLED_FOR_SELECT_DEFAULT);
        setRenderViewStateId(INIT_PARAM_RENDER_VIEWSTATE_ID_DEFAULT);
        setStrictXhtmlLinks(INIT_PARAM_STRICT_XHTML_LINKS_DEFAULT);
        setConfigRefreshPeriod(INIT_PARAM_CONFIG_REFRESH_PERIOD_DEFAULT);        
        setViewStateJavascript(INIT_PARAM_VIEWSTATE_JAVASCRIPT_DEFAULT);        
        setRefreshTransientBuildOnPSS(true);
        setRefreshTransientBuildOnPSSAuto(true);
        setRefreshTransientBuildOnPSSPreserveState(INIT_PARAM_REFRESH_TRANSIENT_BUILD_ON_PSS_PRESERVE_STATE_DEFAULT);
        setValidateXML(INIT_PARAM_VALIDATE_XML_DEFAULT);
        setWrapScriptContentWithXmlCommentTag(INIT_PARAM_WRAP_SCRIPT_CONTENT_WITH_XML_COMMENT_TAG_DEFAULT);
        setDetectJavascript(INIT_PARAM_DETECT_JAVASCRIPT_DEFAULT);
        setAutoScroll(INIT_PARAM_AUTO_SCROLL_DEFAULT);
        setAddResourceClass(INIT_PARAM_ADD_RESOURCE_CLASS_DEFAULT);
        setResourceVirtualPath(INIT_PARAM_RESOURCE_VIRTUAL_PATH_DEFAULT);
        //The default is true but we'll let it false because it depends if 
        //tomahawk is on classpath and no test environment is set
        setCheckExtensionsFilter(false);
        setRenderFormSubmitScriptInline(INIT_PARAM_RENDER_FORM_SUBMIT_SCRIPT_INLINE_DEFAULT);
        setDebugPhaseListenerEnabled(INIT_PARAM_DEBUG_PHASE_LISTENER_DEFAULT);
    }

    private static MyfacesConfig createAndInitializeMyFacesConfig(ExternalContext extCtx) {
        
        MyfacesConfig myfacesConfig = new MyfacesConfig();

        myfacesConfig.setPrettyHtml(getBooleanInitParameter(extCtx, INIT_PARAM_PRETTY_HTML,
                                                            INIT_PARAM_PRETTY_HTML_DEFAULT));
        myfacesConfig.setAllowJavascript(getBooleanInitParameter(extCtx, INIT_PARAM_ALLOW_JAVASCRIPT,
                                                                 INIT_PARAM_ALLOW_JAVASCRIPT_DEFAULT));

        myfacesConfig.setRenderClearJavascriptOnButton(getBooleanInitParameter(extCtx, INIT_PARAM_RENDER_CLEAR_JAVASCRIPT_FOR_BUTTON,
                                                            INIT_PARAM_RENDER_CLEAR_JAVASCRIPT_FOR_BUTTON_DEFAULT));

        myfacesConfig.setRenderHiddenFieldsForLinkParams(getBooleanInitParameter(extCtx, INIT_PARAM_RENDER_HIDDEN_FIELDS_FOR_LINK_PARAMS,
                INIT_PARAM_RENDER_HIDDEN_FIELDS_FOR_LINK_PARAMS_DEFAULT));

        myfacesConfig.setSaveFormSubmitLinkIE(getBooleanInitParameter(extCtx, INIT_PARAM_SAVE_FORM_SUBMIT_LINK_IE,
                                                            INIT_PARAM_SAVE_FORM_SUBMIT_LINK_IE_DEFAULT));
        
        myfacesConfig.setReadonlyAsDisabledForSelect(getBooleanInitParameter(extCtx, INIT_READONLY_AS_DISABLED_FOR_SELECT,
                                                                 INIT_READONLY_AS_DISABLED_FOR_SELECT_DEFAULT));
        myfacesConfig.setRenderViewStateId(getBooleanInitParameter(extCtx, INIT_PARAM_RENDER_VIEWSTATE_ID,
                                                                   INIT_PARAM_RENDER_VIEWSTATE_ID_DEFAULT));
        myfacesConfig.setStrictXhtmlLinks(getBooleanInitParameter(extCtx, INIT_PARAM_STRICT_XHTML_LINKS,
                                                                  INIT_PARAM_STRICT_XHTML_LINKS_DEFAULT));
        myfacesConfig.setRenderFormSubmitScriptInline(getBooleanInitParameter(extCtx, INIT_PARAM_RENDER_FORM_SUBMIT_SCRIPT_INLINE,
                                                                  INIT_PARAM_RENDER_FORM_SUBMIT_SCRIPT_INLINE_DEFAULT));
        
        myfacesConfig.setConfigRefreshPeriod(getLongInitParameter(extCtx, INIT_PARAM_CONFIG_REFRESH_PERIOD,
                INIT_PARAM_CONFIG_REFRESH_PERIOD_DEFAULT));

        myfacesConfig.setViewStateJavascript(getBooleanInitParameter(extCtx, INIT_PARAM_VIEWSTATE_JAVASCRIPT,
                INIT_PARAM_VIEWSTATE_JAVASCRIPT_DEFAULT));

        myfacesConfig.setDelegateFacesServlet(extCtx.getInitParameter(INIT_PARAM_DELEGATE_FACES_SERVLET));
        
        String refreshTransientBuildOnPSS = getStringInitParameter(extCtx, INIT_PARAM_REFRESH_TRANSIENT_BUILD_ON_PSS, 
                INIT_PARAM_REFRESH_TRANSIENT_BUILD_ON_PSS_DEFAULT);
        
        if (refreshTransientBuildOnPSS == null)
        {
            myfacesConfig.setRefreshTransientBuildOnPSS(false);
            myfacesConfig.setRefreshTransientBuildOnPSSAuto(false);
        }
        else if ("auto".equalsIgnoreCase(refreshTransientBuildOnPSS))
        {
            myfacesConfig.setRefreshTransientBuildOnPSS(true);
            myfacesConfig.setRefreshTransientBuildOnPSSAuto(true);
        }
        else if (refreshTransientBuildOnPSS.equalsIgnoreCase("true") || 
                refreshTransientBuildOnPSS.equalsIgnoreCase("on") || 
                refreshTransientBuildOnPSS.equalsIgnoreCase("yes"))
        {
            myfacesConfig.setRefreshTransientBuildOnPSS(true);
            myfacesConfig.setRefreshTransientBuildOnPSSAuto(false);
        }
        else
        {
            myfacesConfig.setRefreshTransientBuildOnPSS(false);
            myfacesConfig.setRefreshTransientBuildOnPSSAuto(false);
        }
        
        myfacesConfig.setRefreshTransientBuildOnPSSPreserveState(getBooleanInitParameter(extCtx,
                INIT_PARAM_REFRESH_TRANSIENT_BUILD_ON_PSS_PRESERVE_STATE, 
                INIT_PARAM_REFRESH_TRANSIENT_BUILD_ON_PSS_PRESERVE_STATE_DEFAULT));
        
        myfacesConfig.setValidateXML(getBooleanInitParameter(extCtx, INIT_PARAM_VALIDATE_XML, INIT_PARAM_VALIDATE_XML_DEFAULT));
        
        myfacesConfig.setWrapScriptContentWithXmlCommentTag(getBooleanInitParameter(extCtx, 
                INIT_PARAM_WRAP_SCRIPT_CONTENT_WITH_XML_COMMENT_TAG, INIT_PARAM_WRAP_SCRIPT_CONTENT_WITH_XML_COMMENT_TAG_DEFAULT));
        
        myfacesConfig.setDebugPhaseListenerEnabled(getBooleanInitParameter(extCtx, INIT_PARAM_DEBUG_PHASE_LISTENER,
                INIT_PARAM_DEBUG_PHASE_LISTENER_DEFAULT));

        if (TOMAHAWK_AVAILABLE)
        {
            myfacesConfig.setDetectJavascript(getBooleanInitParameter(extCtx, INIT_PARAM_DETECT_JAVASCRIPT,
                    INIT_PARAM_DETECT_JAVASCRIPT_DEFAULT));
            myfacesConfig.setAutoScroll(getBooleanInitParameter(extCtx, INIT_PARAM_AUTO_SCROLL,
                    INIT_PARAM_AUTO_SCROLL_DEFAULT));
                        
            myfacesConfig.setAddResourceClass(getStringInitParameter(extCtx, INIT_PARAM_ADD_RESOURCE_CLASS,
                    INIT_PARAM_ADD_RESOURCE_CLASS_DEFAULT));
            myfacesConfig.setResourceVirtualPath(getStringInitParameter(extCtx, INIT_PARAM_RESOURCE_VIRTUAL_PATH,
                    INIT_PARAM_RESOURCE_VIRTUAL_PATH_DEFAULT));

            myfacesConfig.setCheckExtensionsFilter(getBooleanInitParameter(extCtx, INIT_CHECK_EXTENSIONS_FILTER,
                    INIT_CHECK_EXTENSIONS_FILTER_DEFAULT));
            
            if(RI_IMPL_AVAILABLE)
            {
                if(log.isLoggable(Level.INFO))
                {
                    log.info("Starting up Tomahawk on the RI-JSF-Implementation.");
                }
            }

            if(MYFACES_IMPL_AVAILABLE)
            {
                if(log.isLoggable(Level.INFO))
                {
                    log.info("Starting up Tomahawk on the MyFaces-JSF-Implementation");
                }
            }
        }
        else
        {
            if (log.isLoggable(Level.INFO))
            {
                log.info("Tomahawk jar not available. Autoscrolling, DetectJavascript, AddResourceClass and CheckExtensionsFilter are disabled now.");
            }
        }

        if(RI_IMPL_AVAILABLE && MYFACES_IMPL_AVAILABLE)
        {
            log.severe("Both MyFaces and the RI are on your classpath. Please make sure to use only one of the two JSF-implementations.");
        }
        return myfacesConfig;
    }

    private static boolean getBooleanInitParameter(ExternalContext externalContext,
                                                   String paramName,
                                                   boolean defaultValue)
    {
        String strValue = externalContext.getInitParameter(paramName);
        if (strValue == null)
        {
            if (log.isLoggable(Level.INFO)) log.info("No context init parameter '" + paramName + "' found, using default value " + defaultValue);
            return defaultValue;
        }
        else if (strValue.equalsIgnoreCase("true") || strValue.equalsIgnoreCase("on") || strValue.equalsIgnoreCase("yes"))
        {
            return true;
        }
        else if (strValue.equalsIgnoreCase("false") || strValue.equalsIgnoreCase("off") || strValue.equalsIgnoreCase("no"))
        {
            return false;
        }
        else
        {
            if (log.isLoggable(Level.WARNING)) log.warning("Wrong context init parameter '" + paramName + "' (='" + strValue + "'), using default value " + defaultValue);
            return defaultValue;
        }
    }

    private static String getStringInitParameter(ExternalContext externalContext,
                                                 String paramName,
                                                 String defaultValue)
    {
        String strValue = externalContext.getInitParameter(paramName);
        if (strValue == null)
        {
            if (log.isLoggable(Level.INFO)) log.info("No context init parameter '" + paramName + "' found, using default value " + defaultValue); //defaultValue==null should not be a problem here
            return defaultValue;
        }
        
        return strValue;
    }

    private static long getLongInitParameter(ExternalContext externalContext,
                                                  String paramName,
                                                  long defaultValue) {
       String strValue = externalContext.getInitParameter(paramName);
       if (strValue == null)
       {
           if (log.isLoggable(Level.INFO)) log.info("No context init parameter '" +paramName +"' found, using default value " +defaultValue);
           return defaultValue;
       }
       else
       {
           try {
               return Long.parseLong(strValue);
           } catch (NumberFormatException e) {
               if (log.isLoggable(Level.WARNING)) log.warning("Wrong context init parameter '" + paramName + "' (='" + strValue + "'), using default value " + defaultValue);
           }
           return defaultValue;
       }
    }
        
     private void setResourceVirtualPath( String resourceVirtualPath )
     {
         this._resourceVirtualPath = resourceVirtualPath;
    }

     public String getResourceVirtualPath()
     {
         return this._resourceVirtualPath;
     }

    public boolean isPrettyHtml()
    {
        return _prettyHtml;
    }

    private void setPrettyHtml(boolean prettyHtml)
    {
        _prettyHtml = prettyHtml;
    }

    public boolean isDetectJavascript()
    {
        return _detectJavascript;
    }

    private void setDetectJavascript(boolean detectJavascript)
    {
        _detectJavascript = detectJavascript;
    }

    private void setReadonlyAsDisabledForSelect(boolean readonlyAsDisabledForSelect)
    {
        _readonlyAsDisabledForSelect = readonlyAsDisabledForSelect;
    }

    public boolean isReadonlyAsDisabledForSelect()
    {
        return _readonlyAsDisabledForSelect;
    }


   public long getConfigRefreshPeriod()
   {
       return _configRefreshPeriod;
   }

   public void setConfigRefreshPeriod(long configRefreshPeriod)
   {
       _configRefreshPeriod = configRefreshPeriod;
   }

    /**
     * JSF API 1.2 defines a "javax.faces.ViewState" client parameter, that must be rendered as both the "name"
     * and the "id" attribute of the hidden input that is rendered for the purpose of state saving
     * (see <a href="http://java.sun.com/javaee/javaserverfaces/1.2/docs/api/javax/faces/render/ResponseStateManager.html#VIEW_STATE_PARAM">API</a>).
     * Actually this causes duplicate id attributes and thus invalid XHTML pages when multiple forms are rendered on
     * one page. With the {@link #INIT_PARAM_RENDER_VIEWSTATE_ID} context parameter you can tune this behaviour.
     * <br/>Set it to
     * <ul><li>true - to render JSF 1.2 compliant id attributes (that might cause invalid XHTML), or</li>
     * <li>false - to omit rendering of the id attribute (which is only needed for very special AJAX/Javascript components)</li></ul>
     * Default value is: true (for backwards compatibility and JSF 1.2 compliancy) 
     * @return true, if the client state hidden input "javax.faces.ViewState" id attribute should be rendered
     */
    public boolean isRenderViewStateId() {
        return _renderViewStateId;
    }

    public void setRenderViewStateId(boolean renderViewStateId) {
        _renderViewStateId = renderViewStateId;
    }

    /**
     * <p>W3C recommends to use the "&amp;amp;" entity instead of a plain "&amp;" character within HTML.
     * This also applies to attribute values and thus to the "href" attribute of &lt;a&gt; elements as well.
     * Even more, when XHTML is used as output the usage of plain "&amp;" characters is forbidden and would lead to
     * invalid XML code.
     * Therefore, since version 1.1.6 MyFaces renders the correct "&amp;amp;" entity for links.</p>
     * <p>The init parameter
     * {@link #INIT_PARAM_STRICT_XHTML_LINKS} makes it possible to restore the old behaviour and to make MyFaces
     * "bug compatible" to the Sun RI which renders plain "&amp;" chars in links as well.</p>
     * @see <a href="http://www.w3.org/TR/html401/charset.html#h-5.3.2">HTML 4.01 Specification</a>
     * @see <a href="http://issues.apache.org/jira/browse/MYFACES-1774">Jira: MYFACES-1774</a>
     * @return true if ampersand characters ("&amp;") should be correctly rendered as "&amp;amp;" entities within link urls (=default),
     *         false for old (XHTML incompatible) behaviour
     */
    public boolean isStrictXhtmlLinks() {
        return _strictXhtmlLinks;
    }

    public void setStrictXhtmlLinks(boolean strictXhtmlLinks) {
        _strictXhtmlLinks = strictXhtmlLinks;
    }

    public boolean isTomahawkAvailable()
    {
        return TOMAHAWK_AVAILABLE;
    }

    public boolean isMyfacesImplAvailable()
    {
        return MYFACES_IMPL_AVAILABLE;
    }

    public boolean isRiImplAvailable()
    {
        return RI_IMPL_AVAILABLE;
    }

    /**
     * Do not use this in renderers directly!
     * You should use {@link org.apache.myfaces.shared.renderkit.html.util.JavascriptUtils#isJavascriptAllowed}
     * to determine if javascript is allowed or not.
     */
    public boolean isAllowJavascript()
    {
        return _allowJavascript;
    }

    private void setAllowJavascript(boolean allowJavascript)
    {
        _allowJavascript = allowJavascript;
    }

    public boolean isAutoScroll()
    {
        return _autoScroll;
    }

    private void setAutoScroll(boolean autoScroll)
    {
        _autoScroll = autoScroll;
    }

    private void setAddResourceClass(String addResourceClass)
    {
        _addResourceClass = addResourceClass;
    }

    public String getAddResourceClass()
    {
        return _addResourceClass;
    }

    /**
     * ExtensionFilter needs access to AddResourceClass init param without having
     * an ExternalContext at hand.
     */
    public static String getAddResourceClassFromServletContext(ServletContext servletContext)
    {
        String addResourceClass = servletContext.getInitParameter(INIT_PARAM_ADD_RESOURCE_CLASS);

        return addResourceClass == null ? INIT_PARAM_ADD_RESOURCE_CLASS_DEFAULT : addResourceClass;
    }

    /**
     * Should the environment be checked so that the ExtensionsFilter will work properly. 
     */
    public boolean isCheckExtensionsFilter()
    {
        return _checkExtensionsFilter;
    }

    public void setCheckExtensionsFilter(boolean extensionsFilter)
    {
        _checkExtensionsFilter = extensionsFilter;
    }

    /**
     * 
     */
    public boolean isViewStateJavascript()
    {
        return _viewStateJavascript;
    }

    private void setViewStateJavascript(boolean viewStateJavascript)
    {
        _viewStateJavascript = viewStateJavascript;
    }

    public void setRenderClearJavascriptOnButton(
            boolean renderClearJavascriptOnButton)
    {
        _renderClearJavascriptOnButton = renderClearJavascriptOnButton;
    }

    /**
     * This param renders the clear javascript on button necessary only for
     * compatibility with hidden fields feature of myfaces. This is done 
     * because jsf ri does not render javascript on onclick method for button,
     * so myfaces should do this.
     * 
     * @return
     */
    public boolean isRenderClearJavascriptOnButton()
    {
        return _renderClearJavascriptOnButton;
    }

    public boolean isRenderHiddenFieldsForLinkParams()
    {
        return renderHiddenFieldsForLinkParams;
    }

    public void setRenderHiddenFieldsForLinkParams(
            boolean renderHiddenFieldsForLinkParams)
    {
        this.renderHiddenFieldsForLinkParams = renderHiddenFieldsForLinkParams;
    }

    public void setSaveFormSubmitLinkIE(boolean saveFormSubmitLinkIE)
    {
        _saveFormSubmitLinkIE = saveFormSubmitLinkIE;
    }

    /**
     * Add a code that save the form when submit a form using a
     * link. It's a bug on IE.
     * 
     * @return
     */
    public boolean isSaveFormSubmitLinkIE()
    {
        return _saveFormSubmitLinkIE;
    }
    
    public String getDelegateFacesServlet()
    {
        return _delegateFacesServlet;
    }
    
    public void setDelegateFacesServlet(String delegateFacesServlet)
    {
        _delegateFacesServlet = delegateFacesServlet;
    }

    public boolean isRefreshTransientBuildOnPSS()
    {
        return _refreshTransientBuildOnPSS;
    }

    public void setRefreshTransientBuildOnPSS(boolean refreshTransientBuildOnPSS)
    {
        this._refreshTransientBuildOnPSS = refreshTransientBuildOnPSS;
    }

    public boolean isRefreshTransientBuildOnPSSAuto()
    {
        return _refreshTransientBuildOnPSSAuto;
    }

    public void setRefreshTransientBuildOnPSSAuto(
            boolean refreshTransientBuildOnPSSAuto)
    {
        this._refreshTransientBuildOnPSSAuto = refreshTransientBuildOnPSSAuto;
    }

    public boolean isRefreshTransientBuildOnPSSPreserveState()
    {
        return refreshTransientBuildOnPSSPreserveState;
    }

    public void setRefreshTransientBuildOnPSSPreserveState(
            boolean refreshTransientBuildOnPSSPreserveState)
    {
        this.refreshTransientBuildOnPSSPreserveState = refreshTransientBuildOnPSSPreserveState;
    }
    
    public boolean isValidateXML()
    {
        return _validateXML;
    }

    public void setValidateXML(boolean validateXML)
    {
        _validateXML = validateXML;
    }

    public boolean isWrapScriptContentWithXmlCommentTag()
    {
        return _wrapScriptContentWithXmlCommentTag;
    }

    public void setWrapScriptContentWithXmlCommentTag(
            boolean wrapScriptContentWithXmlCommentTag)
    {
        this._wrapScriptContentWithXmlCommentTag = wrapScriptContentWithXmlCommentTag;
    }

    public boolean isRenderFormSubmitScriptInline()
    {
        return _renderFormSubmitScriptInline;
    }

    public void setRenderFormSubmitScriptInline(
            boolean renderFormSubmitScriptInline)
    {
        _renderFormSubmitScriptInline = renderFormSubmitScriptInline;
    }

    public boolean isDebugPhaseListenerEnabled()
    {
        return _debugPhaseListenerEnabled;
    }

    public void setDebugPhaseListenerEnabled(boolean debugPhaseListener)
    {
        this._debugPhaseListenerEnabled = debugPhaseListener;
    }
}
