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
package org.apache.myfaces.application.pss;

import org.apache.myfaces.application.MyfacesStateManager;
import org.apache.myfaces.application.TreeStructureManager;
import org.apache.myfaces.renderkit.MyfacesResponseStateManager;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.commons.collections.map.ReferenceMap;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.beanutils.BeanUtils;

import javax.faces.context.FacesContext;
import javax.faces.context.ExternalContext;
import javax.faces.application.StateManager;
import javax.faces.application.Application;
import javax.faces.application.ViewHandler;
import javax.faces.component.UIViewRoot;
import javax.faces.component.UIComponent;
import javax.faces.component.NamingContainer;
import javax.faces.component.UIOutput;
import javax.faces.render.RenderKit;
import javax.faces.render.ResponseStateManager;
import javax.faces.render.RenderKitFactory;
import javax.faces.FactoryFinder;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.util.*;
import java.util.zip.GZIPOutputStream;
import java.util.zip.GZIPInputStream;
import java.lang.reflect.InvocationTargetException;

import org.apache.myfaces.shared_impl.renderkit.ViewSequenceUtils;
import org.apache.myfaces.shared_impl.util.MyFacesObjectInputStream;
import org.apache.myfaces.context.servlet.ServletExternalContextImpl;
import org.apache.myfaces.context.portlet.PortletExternalContextImpl;

/**
 * @author Martin Haimberger
 */
public class PssJspStateManagerImpl extends MyfacesStateManager
{

    private static final Log log = LogFactory.getLog(PssJspStateManagerImpl.class);
    private static final String SERIALIZED_VIEW_SESSION_ATTR
            = PssJspStateManagerImpl.class.getName() + ".SERIALIZED_VIEW";
    private static final String SERIALIZED_VIEW_REQUEST_ATTR
            = PssJspStateManagerImpl.class.getName() + ".SERIALIZED_VIEW";
    private static final String RESTORED_SERIALIZED_VIEW_REQUEST_ATTR
    = PssJspStateManagerImpl.class.getName() + ".RESTORED_SERIALIZED_VIEW";

    private static final String PARTIAL_STATE_MANAGER_TREES = PssJspStateManagerImpl.class.getName() + ".PARTIAL_STATE_MANAGER_TREES";

    /**
     * Only applicable if state saving method is "server" (= default).
     * Defines the amount (default = 20) of the latest views are stored in session.
     */
    private static final String NUMBER_OF_VIEWS_IN_SESSION_PARAM = "org.apache.myfaces.NUMBER_OF_VIEWS_IN_SESSION";

    /**
     * Default value for <code>org.apache.myfaces.NUMBER_OF_VIEWS_IN_SESSION</code> context parameter.
     */
    private static final int DEFAULT_NUMBER_OF_VIEWS_IN_SESSION = 20;

    /**
     * Only applicable if state saving method is "server" (= default).
     * If <code>true</code> (default) the state will be serialized to a byte stream before it is written to the session.
     * If <code>false</code> the state will not be serialized to a byte stream.
     */
    private static final String SERIALIZE_STATE_IN_SESSION_PARAM = "org.apache.myfaces.SERIALIZE_STATE_IN_SESSION";

    /**
     * Only applicable if state saving method is "server" (= default) and if <code>org.apache.myfaces.SERIALIZE_STATE_IN_SESSION</code> is <code>true</code> (= default).
     * If <code>true</code> (default) the serialized state will be compressed before it is written to the session.
     * If <code>false</code> the state will not be compressed.
     */
    private static final String COMPRESS_SERVER_STATE_PARAM = "org.apache.myfaces.COMPRESS_STATE_IN_SESSION";

    /**
     * Default value for <code>org.apache.myfaces.COMPRESS_STATE_IN_SESSION</code> context parameter.
     */
    private static final boolean DEFAULT_COMPRESS_SERVER_STATE_PARAM = true;

    /**
     * Default value for <code>org.apache.myfaces.SERIALIZE_STATE_IN_SESSION</code> context parameter.
     */
    private static final boolean DEFAULT_SERIALIZE_STATE_IN_SESSION = true;

    private static final int UNCOMPRESSED_FLAG = 0;
    private static final int COMPRESSED_FLAG = 1;

    private RenderKitFactory _renderKitFactory = null;

    private static final String PARTIAL_STATE_SAVING_METHOD_PARAM_NAME = "javax.faces.PARTIAL_STATE_SAVING_METHOD";
    private static final String PARTIAL_STATE_SAVING_METHOD_ON = "true";
    private static final String PARTIAL_STATE_SAVING_METHOD_OFF = "false";

    private static final String PARTIAL_STATE_SAVING_DISPATCH_PARAM_NAME = "javax.faces.PARTIAL_STATE_SAVING_DISPATCH_EVERY_TIME";


    private Boolean _partialStateSaving = null;
    private Boolean _partialStateSavingDispatch = null;

    public PssJspStateManagerImpl()
    {
        if (log.isTraceEnabled()) log.trace("New JspStateManagerImpl instance created");
    }

    private boolean isPartialStateSavingOn(FacesContext context)
    {
        if(context == null) throw new NullPointerException("context");
        if (_partialStateSaving != null) return _partialStateSaving.booleanValue();
        String stateSavingMethod = context.getExternalContext().getInitParameter(PARTIAL_STATE_SAVING_METHOD_PARAM_NAME);
        if (stateSavingMethod == null)
        {
            _partialStateSaving = Boolean.FALSE; //Specs 10.1.3: default server saving
            context.getExternalContext().log("No context init parameter '"+PARTIAL_STATE_SAVING_METHOD_PARAM_NAME+"' found; no partial state saving method defined, assuming default partial state saving method off.");
        }
        else if (stateSavingMethod.equals(PARTIAL_STATE_SAVING_METHOD_ON))
        {
            _partialStateSaving = Boolean.TRUE;
        }
        else if (stateSavingMethod.equals(PARTIAL_STATE_SAVING_METHOD_OFF))
        {
            _partialStateSaving = Boolean.FALSE;
        }
        else
        {
            _partialStateSaving = Boolean.FALSE; //Specs 10.1.3: default server saving
            context.getExternalContext().log("Illegal partial state saving method '" + stateSavingMethod + "', default partial state saving will be used (partial state saving off).");
        }
        return _partialStateSaving.booleanValue();
    }

    private boolean isPartialStateSavingDispatch(FacesContext context)
    {
        if(context == null) throw new NullPointerException("context");
        if (_partialStateSavingDispatch != null) return _partialStateSavingDispatch.booleanValue();
        String stateSavingDispatch = context.getExternalContext().getInitParameter(PARTIAL_STATE_SAVING_DISPATCH_PARAM_NAME);
        if (stateSavingDispatch == null)
        {
            _partialStateSavingDispatch = Boolean.TRUE; //Specs 10.1.3: default server saving
            context.getExternalContext().log("No context init parameter '"+PARTIAL_STATE_SAVING_DISPATCH_PARAM_NAME+"' found; no partial state saving dispatch usage behavior, assuming default partial state saving dispatch mode on.");
        }
        else if (stateSavingDispatch.equals(PARTIAL_STATE_SAVING_METHOD_ON))
        {
            _partialStateSavingDispatch = Boolean.TRUE;
        }
        else if (stateSavingDispatch.equals(PARTIAL_STATE_SAVING_METHOD_OFF))
        {
            _partialStateSavingDispatch = Boolean.FALSE;
        }
        else
        {
            _partialStateSavingDispatch = Boolean.TRUE; //Specs 10.1.3: default server saving
            context.getExternalContext().log("Illegal partial state saving behavior '" + stateSavingDispatch + "', default partial state saving dispatch behavior will be used (dispatch behavior on).");
        }
        return _partialStateSaving.booleanValue();
    }


    protected Object getComponentStateToSave(FacesContext facesContext)
    {
        if (log.isTraceEnabled()) log.trace("Entering getComponentStateToSave");

        UIViewRoot viewRoot = facesContext.getViewRoot();
        if (viewRoot.isTransient())
        {
            return null;
        }

        Object serializedComponentStates = viewRoot.processSaveState(facesContext);
        //Locale is a state attribute of UIViewRoot and need not be saved explicitly
        if (log.isTraceEnabled()) log.trace("Exiting getComponentStateToSave");
        return serializedComponentStates;
    }

    /**
     * Return an object which contains info about the UIComponent type
     * of each node in the view tree. This allows an identical UIComponent
     * tree to be recreated later, though all the components will have
     * just default values for their members.
     */
    protected Object getTreeStructureToSave(FacesContext facesContext)
    {
        if (log.isTraceEnabled()) log.trace("Entering getTreeStructureToSave");
        UIViewRoot viewRoot = facesContext.getViewRoot();
        if (viewRoot.isTransient())
        {
            return null;
        }
        TreeStructureManager tsm = new TreeStructureManager();
        Object retVal = tsm.buildTreeStructureToSave(viewRoot);
        if (log.isTraceEnabled()) log.trace("Exiting getTreeStructureToSave");
        return retVal;
    }

    /**
     * Return an object which contains info about the UIComponent type
     * of each node in the view tree. This allows an identical UIComponent
     * tree to be recreated later, though all the components will have
     * just default values for their members.
     */
    protected Object getTreeToSave(FacesContext facesContext)
    {
        if (log.isTraceEnabled()) log.trace("Entering getTreeStructureToSave");
        UIViewRoot viewRoot = facesContext.getViewRoot();
        if (viewRoot.isTransient())
        {
            return null;
        }
        PartialTreeStructureManager tsm = new PartialTreeStructureManager(facesContext);
        Object retVal = tsm.buildTreeStructureToSave(viewRoot,facesContext);
        if (log.isTraceEnabled()) log.trace("Exiting getTreeStructureToSave");
        return retVal;
    }


    /**
        * Return an object which contains info about the UIComponent type
        * of each node in the view tree. This allows an identical UIComponent
        * tree to be recreated later, though all the components will have
        * just default values for their members.
        */
       protected Object restoreViewRoot(FacesContext facesContext, Object storedView)
       {
           if (log.isTraceEnabled()) log.trace("Entering getTreeStructureToSave");
           PartialTreeStructureManager tsm = new PartialTreeStructureManager(facesContext);
           Object retVal = tsm.restoreTreeStructure(facesContext,storedView);
           if (log.isTraceEnabled()) log.trace("Exiting getTreeStructureToSave");
           return retVal;
       }


    private String getSequenceString(FacesContext facesContext, String renderKitId, String viewId) {
        RenderKit rk = getRenderKitFactory().getRenderKit(facesContext, renderKitId);
        ResponseStateManager responseStateManager = rk.getResponseStateManager();
        String sequenceStr = (String) responseStateManager.getTreeStructureToRestore(facesContext, viewId);
        return sequenceStr;
    }


    /**
     * Given a tree of UIComponent objects created the default constructor
     * for each node, retrieve saved state info (from either the client or
     * the server) and walk the tree restoring the members of each node
     * from the saved state information.
     */
    protected UIViewRoot restoreComponentState(FacesContext facesContext,
                                               String viewID,
                                               String renderKitId)
    {
        if (log.isTraceEnabled()) log.trace("Entering restoreComponentState");

        //===========================================
        // first, locate the saved state information
        //===========================================

        UIViewRoot uiViewRoot = null;

        Object serializedComponentStates = null;
        if (isSavingStateInClient(facesContext))
        {
            RenderKit renderKit = getRenderKitFactory().getRenderKit(facesContext, renderKitId);
            ResponseStateManager responseStateManager = renderKit.getResponseStateManager();
            // the State with the View ID
            serializedComponentStates = responseStateManager.getComponentStateToRestore(facesContext);
        }
        else
        {
            String sequenceStr = getSequenceString(facesContext, renderKitId, viewID);
            SerializedView serializedView = getSerializedViewFromServletSession(facesContext,
                                                                                viewID,
                                                                                sequenceStr);

            if (serializedView != null)
            {
                serializedComponentStates = serializedView.getStructure();
            }

        }

        // now ask the view root component to restore its state

        Object template = LoadTreeFromManager(facesContext,viewID);
        if (serializedComponentStates != null )
        {
            // we have got a view
            String serializedViewID = (String)((Object[])serializedComponentStates)[0];
            if (!serializedViewID.equals(viewID)) {
                // not the right view.
                serializedComponentStates = null;
            }
        }
        if (serializedComponentStates != null) {


            uiViewRoot = mergeComponentData(facesContext,viewID,(TreeStructComponent)((Object [])serializedComponentStates)[1]);
        }
        else {
            uiViewRoot = mergeComponentData(facesContext,viewID,(TreeStructComponent)null);
        }
        Object rebuildStateTree = null;
        if (serializedComponentStates != null)
        {

            rebuildStateTree = rebuildStateTree((TreeStructComponent)((Object [])serializedComponentStates)[1]);
        }
        else
        {
            rebuildStateTree = rebuildStateTree((TreeStructComponent)template);
        }

        uiViewRoot.processRestoreState(facesContext,rebuildStateTree);

        if (uiViewRoot.getRenderKitId() == null)
        {
            //Just to be sure...
            uiViewRoot.setRenderKitId(renderKitId);
        }


        if (log.isTraceEnabled()) log.trace("Exiting restoreComponentState");
        return uiViewRoot;
    }

    private Object[] rebuildStateTree(TreeStructComponent componentState) {

        Map facetMap = null;
        if (componentState.getFacets() != null)
        {
            for (int iter = 0;iter < componentState.getFacets().length;iter++)
            {
                if (facetMap == null) facetMap = new HashMap();
                TreeStructComponent componentchild = (TreeStructComponent)((Object[])(componentState.getFacets()[iter]))[1];
                Object componentKey = ((Object[])(componentState.getFacets()[iter]))[0];
                facetMap.put(componentKey,rebuildStateTree(componentchild));
            }

        }

        List childrenList = null;
        if (componentState.getChildren() != null)
        {

            TreeStructComponent[]  children = componentState.getChildren();
            for (int it = 0; it < children.length;it++ )
            {

            TreeStructComponent child = children[it];
              if (childrenList == null) {
                childrenList = new ArrayList(children.length);
              }
              Object childState = rebuildStateTree(child);
              if (childState != null) {
                childrenList.add(childState);
              }
            }
        }
        return new Object[] {componentState.get_componentState(),
                             facetMap,
                             childrenList};
    }


    private void mergeComponent(TreeStructComponent currentComponent, TreeStructComponent templateComponent)
    {

        TreeStructComponent[] currentchildren;
        Object[] facets;

        TreeStructComponent[] templatechildren = null;

        currentchildren = currentComponent.getChildren();
        facets = currentComponent.getFacets();

        if (templateComponent != null)
        {
            templatechildren = templateComponent.getChildren();
            if (currentComponent.getStatus() == TreeStructComponent.STATE_IS_TEMPLATE_STATE)
            {
                // restore State from template
                currentComponent.set_componentState(templateComponent.get_componentState());
                currentComponent.setStatus(TreeStructComponent.STATE_IS_RESTORED);
            }
            if ((currentComponent.getStatus() == TreeStructComponent.STATE_IS_NEW_COMPONENT) ||
                    (currentComponent.getStatus() == TreeStructComponent.STATE_IS_NEW_STATE))
            {
                currentComponent.setStatus(TreeStructComponent.STATE_IS_RESTORED);
            }
        }
        else
        {   // new Component ... State is restored
            currentComponent.setStatus(TreeStructComponent.STATE_IS_RESTORED);

        }

        if ((currentchildren != null) )
        {
            // create HashMap to find the children easily
            HashMap templatechildrenMap = new HashMap();
            if (templatechildren != null)
            {
                for(int componentIndex = 0;componentIndex < templatechildren.length;componentIndex++ )
                {
                    templatechildrenMap.put(templatechildren[componentIndex].getComponentId(),templatechildren[componentIndex]);
                }
            }
            // loop throw the childs
            for(int componentIndex = 0;componentIndex < currentchildren.length;componentIndex++ )
            {
                String id = currentchildren[componentIndex].getComponentId();
                TreeStructComponent foundtemplateComponent =(TreeStructComponent) templatechildrenMap.get(id);
                mergeComponent(currentchildren[componentIndex] ,foundtemplateComponent);
            }

        }

        if (facets != null)
        {
            // create HashMap to find the children easily
            HashMap templatefacetMap = new HashMap();
            if ((templateComponent != null)  && (templateComponent.getFacets() != null))
            {
                Object [] templatefacets = templateComponent.getFacets();
                for(int componentIndex = 0;componentIndex < templatefacets.length;componentIndex++ )
                {
                    templatefacetMap.put(((Object[])(templatefacets[componentIndex]))[0],(TreeStructComponent)((Object[])(templatefacets[componentIndex]))[1]);
                }
            }

            Map facetMap = new HashMap();
            for (int iter = 0;iter < facets.length;iter++)
            {
                TreeStructComponent facet = (TreeStructComponent)(((Object[])facets[iter])[1]);
                Object key = (((Object[])facets[iter])[0]);
                facetMap.put(key,facet);
            }

            Iterator iter = facetMap.entrySet().iterator();

            while(iter.hasNext())
            {
                Map.Entry entry = (Map.Entry)iter.next();

                TreeStructComponent foundtemplateComponent =(TreeStructComponent) templatefacetMap.get(entry.getKey());
                mergeComponent((TreeStructComponent)entry.getValue() ,foundtemplateComponent);
            }
        }

    }



    private UIViewRoot mergeComponentData(FacesContext facesContext,String viewRoot, TreeStructComponent state) {

        TreeStructComponent template = (TreeStructComponent)LoadTreeFromManager(facesContext,viewRoot);

        UIViewRoot tempRoot = null;
        if (state == null)
        {
            // nothing loaded ... use the templeate
            tempRoot =  (UIViewRoot)restoreViewRoot(facesContext,template);

        }
        else
        {
            mergeComponent(state,template);
            tempRoot =  (UIViewRoot)restoreViewRoot(facesContext,state);
        }
        return tempRoot;
    }

    /**
     * See getTreeStructureToSave.
     */
    protected UIViewRoot restoreTreeStructure(FacesContext facesContext,
                                              String viewId,
                                              String renderKitId)
    {
        if (log.isTraceEnabled()) log.trace("Entering restoreTreeStructure");
        if (!isPartialStateSavingOn(facesContext)) {
            log.fatal("Partial state saving StateManager is installed, but partial state saving is not enabled. Please enable partial state saving or use another StateManager.");
        }

        UIViewRoot uiViewRoot;
        if (isSavingStateInClient(facesContext))
        {
            //reconstruct tree structure from request
            RenderKit rk = getRenderKitFactory().getRenderKit(facesContext, renderKitId);
            ResponseStateManager responseStateManager = rk.getResponseStateManager();
            Object treeStructure = responseStateManager.getTreeStructureToRestore(facesContext, viewId);
            if (treeStructure == null)
            {
                if (log.isDebugEnabled()) log.debug("Exiting restoreTreeStructure - No tree structure state found in client request");
                return null;
            }

            TreeStructureManager tsm = new TreeStructureManager();
            uiViewRoot = tsm.restoreTreeStructure((TreeStructureManager.TreeStructComponent)treeStructure);
            if (log.isTraceEnabled()) log.trace("Tree structure restored from client request");
        }
        else {
            String sequenceStr = getSequenceString(facesContext, renderKitId, viewId);
            //reconstruct tree structure from ServletSession
            SerializedView serializedView = getSerializedViewFromServletSession(facesContext,
                                                                                viewId,
                                                                                sequenceStr);
            if (serializedView == null)
            {
                if (log.isDebugEnabled()) log.debug("Exiting restoreTreeStructure - No serialized view found in server session!");
                return null;
            }

            Object treeStructure = serializedView.getStructure();
            if (treeStructure == null)
            {
                if (log.isDebugEnabled()) log.debug("Exiting restoreTreeStructure - No tree structure state found in server session, former UIViewRoot must have been transient");
                return null;
            }

            TreeStructureManager tsm = new TreeStructureManager();
            uiViewRoot = tsm.restoreTreeStructure((TreeStructureManager.TreeStructComponent)serializedView.getStructure());
            if (log.isTraceEnabled()) log.trace("Tree structure restored from server session");
        }

        if (log.isTraceEnabled()) log.trace("Exiting restoreTreeStructure");
        return uiViewRoot;
    }

    protected void restoreComponentState(FacesContext context, UIViewRoot viewRoot, String renderKitId) {
    }

    public UIViewRoot restoreView(FacesContext facescontext, String viewId, String renderKitId)
    {
        if (log.isTraceEnabled()) log.trace("Entering restoreView");
        UIViewRoot uiViewRoot = null;

        Object template = LoadTreeFromManager(facescontext,viewId);

        if (template != null)
        {
            uiViewRoot = restoreComponentState(facescontext,viewId,renderKitId);
            uiViewRoot.setViewId(viewId);
            String restoredViewId = uiViewRoot.getViewId();
            if (restoredViewId == null || !(restoredViewId.equals(viewId)))
            {
                if (log.isTraceEnabled()) log.trace("Exiting restoreView - restored view is null.");
                return null;
            }
            if (isPartialStateSavingDispatch(facescontext)) {
                dispatchJSP(facescontext,viewId);
            }
        }
        else
        {
            dispatchJSP(facescontext, viewId);

            SaveTreeInManager(facescontext);
            // reload the current view.
            uiViewRoot = restoreComponentState(facescontext,viewId,renderKitId);


        }

        if (log.isTraceEnabled()) log.trace("Exiting restoreView");

        return uiViewRoot;
    }

    /**
     * Dispatches the JSP with a wrapped UIViewRoot so no Component is rendered, but
     * the component tree is created and every tag will be executed too. This is helpfull
     * if you want to use the f:loadBundle instead of the new s:loadBundle from the Sandbox.
     * @param facescontext the faces context.
     * @param viewId the view id which should be dispatched.
     */

    private void dispatchJSP(FacesContext facescontext, String viewId) {
        UIViewRoot uiViewRoot;
        // the first time the jsp page is called
        // so create tree

        Application application = facescontext.getApplication();
        ViewHandler applicationViewHandler = application.getViewHandler();

        // create Component Tree
        ExternalContext externalContext = facescontext.getExternalContext();
        uiViewRoot = applicationViewHandler.createView(facescontext, viewId);
        uiViewRoot.setViewId(viewId);
        UIViewRootWrapper currentWrapper = new UIViewRootWrapper(uiViewRoot);

        facescontext.setViewRoot(currentWrapper);

        // save original Response
        Object orgResponse = facescontext.getExternalContext().getResponse();

        ViewHandlerResponseWrapperHelperImpl wrapped = new ViewHandlerResponseWrapperHelperImpl((HttpServletResponse)facescontext.getExternalContext().getResponse());

        // set the wrapped response into the externalContext
        if ((externalContext instanceof ServletExternalContextImpl)
            || (externalContext instanceof PortletExternalContextImpl)) {
            try {
                BeanUtils.setProperty(externalContext,"response",wrapped);
            } catch (IllegalAccessException e) {
                log.error(e.toString());
            } catch (InvocationTargetException e) {
                log.error(e.toString());
            }
        } else {
            log.error("External Response could not be set! Deaktivate Partial State Saving!");
        }

        try
        {
            externalContext.dispatch(viewId);
        }
        catch (IOException e)
        {
            log.error(e.toString());
        }
        // set the original response back into the externalContext
        if ((externalContext instanceof ServletExternalContextImpl)
            || (externalContext instanceof PortletExternalContextImpl)) {
            try {
                BeanUtils.setProperty(externalContext,"response",orgResponse);
            } catch (IllegalAccessException e) {
                log.error(e.toString());
            } catch (InvocationTargetException e) {
                log.error(e.toString());
            }

        } else {
            log.error("External Response could not be set! Deaktivate Partial State Saving!");
        }

        // After view Content
        // the first Component in the UIViewRoot is the before view content .... get it's length

        int beforeViewLength = 0;
        UIComponent beforeview = (UIComponent)facescontext.getViewRoot().getChildren().get(0);
        if (beforeview instanceof UIOutput) {
            // what we expect
            beforeViewLength = ((String)(((UIOutput)beforeview).getValue())).length();
        } else {
            log.error("No before view element found!");
        }

        String beforeViewContent = wrapped.toString().substring(beforeViewLength);
        facescontext.getViewRoot().getChildren().add(facescontext.getViewRoot().getChildCount(),createUIOutputComponentFromString(facescontext,beforeViewContent));
        wrapped.resetWriter();
    }


    protected UIComponent createUIOutputComponentFromString(FacesContext context, String content) {
     UIOutput verbatim = null;

     verbatim = createUIOutputComponent(context);
     verbatim.setValue(content);

     return verbatim;
     }



     protected UIOutput createUIOutputComponent(FacesContext context) {
         //assert(null != (context));
         if (context == null) return null;
         UIOutput verbatim = null;
         Application application = context.getApplication();
         verbatim = (UIOutput) application.createComponent("javax.faces.HtmlOutputText");
         verbatim.setTransient(true);
         verbatim.getAttributes().put("escape", Boolean.FALSE);
         verbatim.setId(context.getViewRoot().createUniqueId());
         return verbatim;
     }



    public void SaveTreeInManager(FacesContext facesContext) throws IllegalStateException
    {
        if (log.isTraceEnabled()) log.trace("Entering saveSerializedView");

        checkForDuplicateIds(facesContext, facesContext.getViewRoot(), new HashSet());

        if (log.isTraceEnabled()) log.trace("Processing saveSerializedView - Checked for duplicate Ids");

        // save the Tree for Partial State Saving

        Object Tree = getTreeToSave(facesContext);


        Map appmap =  facesContext.getExternalContext().getApplicationMap();

        HashMap mymap = (HashMap)appmap.get(PARTIAL_STATE_MANAGER_TREES);

        if ( mymap== null)
        {
            // create on
            mymap = new HashMap();
        }

        mymap.put(facesContext.getViewRoot().getViewId(),Tree);
        appmap.put(PARTIAL_STATE_MANAGER_TREES,mymap);

    }

    public Object LoadUIViewRootFromManager(FacesContext facesContext, String viewID) throws IllegalStateException
    {
        Object tree = LoadTreeFromManager(facesContext,viewID);
        // get state
        if (tree != null)
        {
            return restoreViewRoot(facesContext,tree);
        }
        return null;
    }

     public Object LoadTreeFromManager(FacesContext facesContext, String viewID) throws IllegalStateException
    {
        Map appmap =  facesContext.getExternalContext().getApplicationMap();

        HashMap mymap = (HashMap)appmap.get(PARTIAL_STATE_MANAGER_TREES);

        if ( mymap != null)
        {
            Object test = mymap.get(viewID);
            return test;
        }
        return null;
    }

    public SerializedView saveSerializedView(FacesContext facesContext) throws IllegalStateException
    {
        if (log.isTraceEnabled()) log.trace("Entering saveSerializedView");

        checkForDuplicateIds(facesContext, facesContext.getViewRoot(), new HashSet());

        if (log.isTraceEnabled()) log.trace("Processing saveSerializedView - Checked for duplicate Ids");

        ExternalContext externalContext = facesContext.getExternalContext();

        TreeStructComponent templateRoot = (TreeStructComponent)LoadTreeFromManager(facesContext,facesContext.getViewRoot().getViewId());
        // now diff betwee the current and the template ViewRoot

        TreeStructComponent diff  = null;
        if (templateRoot != null)
        {
            diff = diffAgainsTemplate(facesContext, templateRoot);
        }

        // SerializedView already created before within this request?
        SerializedView serializedView = (SerializedView)externalContext.getRequestMap()
                                                            .get(SERIALIZED_VIEW_REQUEST_ATTR);
        if (serializedView == null)
        {
            if (log.isTraceEnabled()) log.trace("Processing saveSerializedView - create new serialized view");

            // first call to saveSerializedView --> create SerializedView
            // save the View ID also in the serialzed View
            serializedView = new StateManager.SerializedView(null, new Object[]{facesContext.getViewRoot().getViewId(),diff});
            externalContext.getRequestMap().put(SERIALIZED_VIEW_REQUEST_ATTR,
                                                serializedView);

            if (log.isTraceEnabled()) log.trace("Processing saveSerializedView - new serialized view created");
        }

        if (!isSavingStateInClient(facesContext))
        {
            if (log.isTraceEnabled()) log.trace("Processing saveSerializedView - server-side state saving - save state");
            //save state in server session
            saveSerializedViewInServletSession(facesContext, serializedView);

            if (log.isTraceEnabled()) log.trace("Exiting saveSerializedView - server-side state saving - saved state");
            Integer sequence = ViewSequenceUtils.getViewSequence(facesContext);
            return new SerializedView(sequence.toString(), null);        }

        if (log.isTraceEnabled()) log.trace("Exiting saveSerializedView - client-side state saving");

        return serializedView;
    }

    private boolean difState(Object[] currentState,Object[] templateState)
    {

        boolean isEqual = true;

        for(int index = 0;index < currentState.length;index++)
            {
                if ((templateState[index] == null) && (currentState[index] != null )) {
                    isEqual = false;
                }else if (currentState[index] instanceof Object[])
                {
                    if ( !difState((Object[])currentState[index],(Object[])templateState[index]))
                    {
                        isEqual = false;
                    }
                }
                else
                {
                    if ((currentState[index] != null)&&(!currentState[index].equals(templateState[index])))
                    {
                        isEqual= false;
                    }
                }
            }
        return isEqual;
    }

    private TreeStructComponent diffComponent(TreeStructComponent currentComponent, TreeStructComponent templateComponent)
    {
        TreeStructComponent diffComponent = currentComponent.clone(templateComponent);

        boolean isEqual = false;
        Object[] currentComponentState;
        TreeStructComponent[] currentchildren;

        Object[] templateComponentState = null;
        TreeStructComponent[] templatechildren = null;

        currentComponentState = (Object[])currentComponent.get_componentState();
        currentchildren = currentComponent.getChildren();

        if (templateComponent != null)
        {
            templateComponentState = (Object[])templateComponent.get_componentState();
            templatechildren = templateComponent.getChildren();

            if (currentComponentState.length == templateComponentState.length)
            {
                isEqual = difState(currentComponentState,templateComponentState);
            }
        }
        else
        {
            isEqual = false;
        }

        if (!isEqual)
        {
            diffComponent.set_componentState(currentComponent.get_componentState());
            diffComponent.setStatus(TreeStructComponent.STATE_IS_NEW_STATE);
        }
        else
        {
            diffComponent.setStatus(TreeStructComponent.STATE_IS_TEMPLATE_STATE);
            diffComponent.set_componentState(null);
        }

        if ((currentchildren != null) )
        {
            // create HashMap to find the children easily
            HashMap templatechildrenMap = new HashMap();
            if (templatechildren != null)
            {
                for(int componentIndex = 0;componentIndex < templatechildren.length;componentIndex++ )
                {
                    templatechildrenMap.put(templatechildren[componentIndex].getComponentId(),templatechildren[componentIndex]);
                }
            }
            // loop throw the childs
            // get count of current child which are not transient
            ArrayList childs = new ArrayList();
            //TreeStructComponent[] childs= new TreeStructComponent[currentchildren.length];
            for(int componentIndex = 0;componentIndex < currentchildren.length;componentIndex++ )
            {
                // only process not transient components
                if (!currentchildren[componentIndex].isTransient()) {
                    String id = currentchildren[componentIndex].getComponentId();
                    TreeStructComponent foundtemplateComponent =(TreeStructComponent) templatechildrenMap.get(id);
                    childs.add(diffComponent(currentchildren[componentIndex] ,foundtemplateComponent));
                }
            }
            childs.toArray(new TreeStructComponent[childs.size()]);
            diffComponent.setChildren((TreeStructComponent[])childs.toArray(new TreeStructComponent[childs.size()]));
        }

        // process facets

        if (currentComponent.getFacets() != null)
        {

            ArrayList facets = new ArrayList();
            //Object[] facets= new Object[currentComponent.getFacets().length];

            // create HashMap to find the children easily
            HashMap templatefacetMap = new HashMap();
            if ((templateComponent != null)  && (templateComponent.getFacets() != null))
            {
                Object [] templatefacets = templateComponent.getFacets();
                for(int componentIndex = 0;componentIndex < templatefacets.length;componentIndex++ )
                {
                    templatefacetMap.put(((Object[])(templatefacets[componentIndex]))[0],(TreeStructComponent)((Object[])(templatefacets[componentIndex]))[1]);
                }
            }

            Map facetMap = new HashMap();
            if ((currentComponent != null)  && (currentComponent.getFacets() != null))
            {
                Object [] currentfacets = currentComponent.getFacets();
                for(int componentIndex = 0;componentIndex < currentfacets.length;componentIndex++ )
                {
                    facetMap.put(((Object[])(currentfacets[componentIndex]))[0],(TreeStructComponent)((Object[])(currentfacets[componentIndex]))[1]);
                }
            }

            Iterator iter = facetMap.entrySet().iterator();
            while(iter.hasNext())
            {
                Map.Entry entry = (Map.Entry)iter.next();
                // only process not transient components
                if (!((TreeStructComponent)entry.getValue()).isTransient() ) {
                    TreeStructComponent foundtemplateComponent =(TreeStructComponent) (templatefacetMap.get(entry.getKey()));
                    Object[] objectEntry = new Object[2];
                    objectEntry[0] = entry.getKey();
                    objectEntry[1] = diffComponent((TreeStructComponent)entry.getValue() ,foundtemplateComponent);
                    facets.add(objectEntry);
                }
            }
            diffComponent.setFacets(facets.toArray());
        }

        return diffComponent;
    }

    private TreeStructComponent diffAgainsTemplate(FacesContext facesContext, TreeStructComponent templateRoot)
    {

        TreeStructComponent currentTree = (TreeStructComponent)getTreeToSave(facesContext);
        TreeStructComponent dif = diffComponent(currentTree,templateRoot);
            return dif;

    }


    private static void checkForDuplicateIds(FacesContext context,
                                             UIComponent component,
                                             Set ids)
    {
        String id = component.getId();
        if (id != null && !ids.add(id))
        {
            throw new IllegalStateException("Client-id : "+id +
                                            " is duplicated in the faces tree. Component : "+component.getClientId(context)+", path: "+
                                            getPathToComponent(component));
        }
        Iterator it = component.getFacetsAndChildren();
        boolean namingContainer = component instanceof NamingContainer;
        while (it.hasNext())
        {
            UIComponent kid = (UIComponent) it.next();
            if (namingContainer)
            {
                checkForDuplicateIds(context, kid, new HashSet());
            }
            else
            {
                checkForDuplicateIds(context, kid, ids);
            }
        }
    }

    private static String getPathToComponent(UIComponent component)
    {
        StringBuffer buf = new StringBuffer();

        if(component == null)
        {
            buf.append("{Component-Path : ");
            buf.append("[null]}");
            return buf.toString();
        }

        getPathToComponent(component,buf);

        buf.insert(0,"{Component-Path : ");
        buf.append("}");

        return buf.toString();
    }

    private static void getPathToComponent(UIComponent component, StringBuffer buf)
    {
        if(component == null)
            return;

        StringBuffer intBuf = new StringBuffer();

        intBuf.append("[Class: ");
        intBuf.append(component.getClass().getName());
        if(component instanceof UIViewRoot)
        {
            intBuf.append(",ViewId: ");
            intBuf.append(((UIViewRoot) component).getViewId());
        }
        else
        {
            intBuf.append(",Id: ");
            intBuf.append(component.getId());
        }
        intBuf.append("]");

        buf.insert(0,intBuf.toString());

        if(component!=null)
        {
            getPathToComponent(component.getParent(),buf);
        }
    }


    public void writeState(FacesContext facesContext,
                           SerializedView serializedView) throws IOException {
        if (log.isTraceEnabled()) log.trace("Entering writeState");

        if (log.isTraceEnabled())
            log.trace("Processing writeState - either client-side (full state) or server-side (partial information; e.g. sequence)");
        if (serializedView != null) {
            UIViewRoot uiViewRoot = facesContext.getViewRoot();
            //save state in response (client-side: full state; server-side: sequence)
            RenderKit renderKit = getRenderKitFactory().getRenderKit(facesContext, uiViewRoot.getRenderKitId());
            renderKit.getResponseStateManager().writeState(facesContext, serializedView);

            if (log.isTraceEnabled()) log.trace("Exiting writeState");
        }
    }

    /**
     * MyFaces extension
     * @param facesContext
     * @param serializedView
     * @throws IOException
     */
    public void writeStateAsUrlParams(FacesContext facesContext,
                                      SerializedView serializedView) throws IOException
    {
        if (log.isTraceEnabled()) log.trace("Entering writeStateAsUrlParams");

        if (isSavingStateInClient(facesContext))
        {
            if (log.isTraceEnabled()) log.trace("Processing writeStateAsUrlParams - client-side state saving writing state");

            UIViewRoot uiViewRoot = facesContext.getViewRoot();
            //save state in response (client)
            RenderKit renderKit = getRenderKitFactory().getRenderKit(facesContext, uiViewRoot.getRenderKitId());
            ResponseStateManager responseStateManager = renderKit.getResponseStateManager();
            if (responseStateManager instanceof MyfacesResponseStateManager)
            {
                ((MyfacesResponseStateManager)responseStateManager).writeStateAsUrlParams(facesContext,
                                                                                          serializedView);
            }
            else
            {
                log.error("ResponseStateManager of render kit " + uiViewRoot.getRenderKitId() + " is no MyfacesResponseStateManager and does not support saving state in url parameters.");
            }
        }

        if (log.isTraceEnabled()) log.trace("Exiting writeStateAsUrlParams");
    }

    //helpers

    protected RenderKitFactory getRenderKitFactory()
    {
        if (_renderKitFactory == null)
        {
            _renderKitFactory = (RenderKitFactory)FactoryFinder.getFactory(FactoryFinder.RENDER_KIT_FACTORY);
        }
        return _renderKitFactory;
    }


    protected void saveSerializedViewInServletSession(FacesContext context,
                                                      SerializedView serializedView) {
        Map sessionMap = context.getExternalContext().getSessionMap();
        SerializedViewCollection viewCollection = (SerializedViewCollection) sessionMap
            .get(SERIALIZED_VIEW_SESSION_ATTR);
        if (viewCollection == null) {
            viewCollection = new SerializedViewCollection();
            sessionMap.put(SERIALIZED_VIEW_SESSION_ATTR, viewCollection);
        }
        viewCollection.add(context, serializeView(context, serializedView));
        // replace the value to notify the container about the change
        sessionMap.put(SERIALIZED_VIEW_SESSION_ATTR, viewCollection);
    }

    protected SerializedView getSerializedViewFromServletSession(FacesContext context, String viewId, String sequenceStr)
    {
        ExternalContext externalContext = context.getExternalContext();
        Map requestMap = externalContext.getRequestMap();
        SerializedView serializedView = null;
        if (requestMap.containsKey(RESTORED_SERIALIZED_VIEW_REQUEST_ATTR))
        {
            serializedView = (SerializedView) requestMap.get(RESTORED_SERIALIZED_VIEW_REQUEST_ATTR);
        }
        else
        {
            SerializedViewCollection viewCollection = (SerializedViewCollection) externalContext
                .getSessionMap().get(SERIALIZED_VIEW_SESSION_ATTR);
            if (viewCollection != null) {
                Integer sequence = null;
                if (sequenceStr == null) {
                    // use latest sequence
                    sequence = ViewSequenceUtils.getCurrentSequence(context);
                }
                else {
                    sequence = new Integer(sequenceStr);
                }
                if (sequence != null) {
                    Object state = viewCollection.get(sequence, viewId);
                    if (state != null) {
                        serializedView = deserializeView(state);
                    }
                }
            }
            requestMap.put(RESTORED_SERIALIZED_VIEW_REQUEST_ATTR, serializedView);
            ViewSequenceUtils.nextViewSequence(context);
        }
        return serializedView;
    }

    protected Object serializeView(FacesContext context, SerializedView serializedView)
    {
        if (log.isTraceEnabled()) log.trace("Entering serializeView");

        if(isSerializeStateInSession(context))
        {
            if (log.isTraceEnabled()) log.trace("Processing serializeView - serialize state in session");

            ByteArrayOutputStream baos = new ByteArrayOutputStream(1024);
            try
            {
                OutputStream os = baos;
                if(isCompressStateInSession(context))
                {
                    if (log.isTraceEnabled()) log.trace("Processing serializeView - serialize compressed");

                    os.write(COMPRESSED_FLAG);
                    os = new GZIPOutputStream(os, 1024);
                }
                else
                {
                    if (log.isTraceEnabled()) log.trace("Processing serializeView - serialize uncompressed");

                    os.write(UNCOMPRESSED_FLAG);
                }
                ObjectOutputStream out = new ObjectOutputStream(os);
                out.writeObject(serializedView.getStructure());
                out.writeObject(serializedView.getState());
                out.close();
                baos.close();

                if (log.isTraceEnabled()) log.trace("Exiting serializeView - serialized. Bytes : "+baos.size());
                return baos.toByteArray();
            }
            catch (IOException e)
            {
                log.error("Exiting serializeView - Could not serialize state: " + e.getMessage(), e);
                return null;
            }
        }
        else
        {
            if (log.isTraceEnabled()) log.trace("Exiting serializeView - do not serialize state in session.");
            return new Object[] {serializedView.getStructure(), serializedView.getState()};
        }
    }

    /**
     * Reads the value of the <code>org.apache.myfaces.SERIALIZE_STATE_IN_SESSION</code> context parameter.
     * @see SERIALIZE_STATE_IN_SESSION_PARAM
     * @param context <code>FacesContext</code> for the request we are processing.
     * @return boolean true, if the server state should be serialized in the session
     */
    protected boolean isSerializeStateInSession(FacesContext context)
    {
        String value = context.getExternalContext().getInitParameter(
                SERIALIZE_STATE_IN_SESSION_PARAM);
        boolean serialize = DEFAULT_SERIALIZE_STATE_IN_SESSION;
        if (value != null)
        {
           serialize = new Boolean(value).booleanValue();
        }
        return serialize;
    }

    /**
     * Reads the value of the <code>org.apache.myfaces.COMPRESS_STATE_IN_SESSION</code> context parameter.
     * @see COMPRESS_SERVER_STATE_PARAM
     * @param context <code>FacesContext</code> for the request we are processing.
     * @return boolean true, if the server state steam should be compressed
     */
    protected boolean isCompressStateInSession(FacesContext context)
    {
        String value = context.getExternalContext().getInitParameter(
                COMPRESS_SERVER_STATE_PARAM);
        boolean compress = DEFAULT_COMPRESS_SERVER_STATE_PARAM;
        if (value != null)
        {
           compress = new Boolean(value).booleanValue();
        }
        return compress;
    }

    protected SerializedView deserializeView(Object state)
    {
        if (log.isTraceEnabled()) log.trace("Entering deserializeView");

        if(state instanceof byte[])
        {
            if (log.isTraceEnabled()) log.trace("Processing deserializeView - deserializing serialized state. Bytes : "+((byte[]) state).length);

            try
            {
                ByteArrayInputStream bais = new ByteArrayInputStream((byte[]) state);
                InputStream is = bais;
                if(is.read() == COMPRESSED_FLAG)
                {
                    is = new GZIPInputStream(is);
                }
                ObjectInputStream in = new MyFacesObjectInputStream(
                        is);
                Object a = in.readObject();
                Object b = in.readObject();
                return new SerializedView(a, b);
            }
            catch (IOException e)
            {
                log.error("Exiting deserializeView - Could not deserialize state: " + e.getMessage(), e);
                return null;
            }
            catch (ClassNotFoundException e)
            {
                log.error("Exiting deserializeView - Could not deserialize state: " + e.getMessage(), e);
                return null;
            }
        }
        else if (state instanceof Object[])
        {
            if (log.isTraceEnabled()) log.trace("Exiting deserializeView - state not serialized.");

            Object[] value = (Object[]) state;
            return new SerializedView(value[0], value[1]);
        }
        else if(state == null)
        {
            log.error("Exiting deserializeView - this method should not be called with a null-state.");
            return null;
        }
        else
        {
            log.error("Exiting deserializeView - this method should not be called with a state of type : "+state.getClass());
            return null;
        }
    }

    protected static class SerializedViewCollection implements Serializable
    {
        private static final long serialVersionUID = -3734849062185115847L;

        private final List _keys = new ArrayList(DEFAULT_NUMBER_OF_VIEWS_IN_SESSION);
        private final Map _serializedViews = new HashMap();

        // old views will be hold as soft references which will be removed by
        // the garbage collector if free memory is low
        private transient Map _oldSerializedViews = null;

        public synchronized void add(FacesContext context, Object state)
        {
            Object key = new SerializedViewKey(context);
            _serializedViews.put(key, state);

            while (_keys.remove(key));
            _keys.add(key);

            int views = getNumberOfViewsInSession(context);
            while (_keys.size() > views)
            {
                key = _keys.remove(0);
                Object oldView = _serializedViews.remove(key);
                if (oldView != null)
                {
                    getOldSerializedViewsMap().put(key, oldView);
                }
            }
        }

        /**
         * Reads the amount (default = 20) of views to be stored in session.
         * @see NUMBER_OF_VIEWS_IN_SESSION_PARAM
         * @param context FacesContext for the current request, we are processing
         * @return Number vf views stored in the session
         */
        protected int getNumberOfViewsInSession(FacesContext context)
        {
            String value = context.getExternalContext().getInitParameter(
                    NUMBER_OF_VIEWS_IN_SESSION_PARAM);
            int views = DEFAULT_NUMBER_OF_VIEWS_IN_SESSION;
            if (value != null)
            {
                try
                {
                    views = Integer.parseInt(value);
                    if (views <= 0)
                    {
                        log.error("Configured value for " + NUMBER_OF_VIEWS_IN_SESSION_PARAM
                                  + " is not valid, must be an value > 0, using default value ("
                                  + DEFAULT_NUMBER_OF_VIEWS_IN_SESSION);
                        views = DEFAULT_NUMBER_OF_VIEWS_IN_SESSION;
                    }
                }
                catch (Throwable e)
                {
                    log.error("Error determining the value for " + NUMBER_OF_VIEWS_IN_SESSION_PARAM
                              + ", expected an integer value > 0, using default value ("
                              + DEFAULT_NUMBER_OF_VIEWS_IN_SESSION + "): " + e.getMessage(), e);
                }
            }
            return views;
        }

        /**
         * @return old serialized views map
         */
        protected Map getOldSerializedViewsMap()
        {
            if (_oldSerializedViews == null)
            {
                _oldSerializedViews = new ReferenceMap();
            }
            return _oldSerializedViews;
        }

        public Object get(Integer sequence, String viewId)
        {
            Object key = new SerializedViewKey(viewId, sequence);
            Object value = _serializedViews.get(key);
            if (value == null)
            {
                value = getOldSerializedViewsMap().get(key);
            }
            return value;
        }
    }

 protected static class SerializedViewKey implements Serializable {
     private static final long serialVersionUID = -1170697124386063642L;

     private final String _viewId;
     private final Integer _sequenceId;

     public SerializedViewKey(String viewId, Integer sequence) {
         _sequenceId = sequence;
         _viewId = viewId;
     }

     public SerializedViewKey(FacesContext context) {
         _sequenceId = ViewSequenceUtils.getViewSequence(context);
         _viewId = context.getViewRoot().getViewId();
     }

     public boolean equals(Object obj) {
         if (obj == null) {
             return false;
         }
         if (obj == this) {
             return true;
         }
         if (obj instanceof SerializedViewKey) {
             SerializedViewKey other = (SerializedViewKey) obj;
             return new EqualsBuilder().append(other._viewId, _viewId).append(other._sequenceId,
                                                                              _sequenceId).isEquals();
         }
         return false;
     }

     public int hashCode() {
         return new HashCodeBuilder().append(_viewId).append(_sequenceId).toHashCode();
     }
 }
}
