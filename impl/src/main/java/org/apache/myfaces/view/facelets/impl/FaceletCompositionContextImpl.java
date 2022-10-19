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
package org.apache.myfaces.view.facelets.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import jakarta.faces.FactoryFinder;

import jakarta.faces.component.UIComponent;
import jakarta.faces.component.UIViewRoot;
import jakarta.faces.component.UniqueIdVendor;
import jakarta.faces.component.visit.VisitContextFactory;
import jakarta.faces.context.FacesContext;
import jakarta.faces.view.AttachedObjectHandler;
import jakarta.faces.view.EditableValueHolderAttachedObjectHandler;

import org.apache.myfaces.config.webparameters.MyfacesConfig;
import org.apache.myfaces.util.lang.Lazy;
import org.apache.myfaces.view.facelets.ELExpressionCacheMode;
import org.apache.myfaces.view.facelets.FaceletCompositionContext;
import org.apache.myfaces.view.facelets.FaceletFactory;
import org.apache.myfaces.view.facelets.FaceletViewDeclarationLanguage;
import org.apache.myfaces.view.facelets.tag.faces.ComponentSupport;

/**
 * @since 2.0.1
 * @author Leonardo Uribe (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
public class FaceletCompositionContextImpl extends FaceletCompositionContext
{

    public static final String JAKARTA_FACES_LOCATION_PREFIX = "jakarta_faces_location_";
    private static final String VISIT_CONTEXT_FACTORY = "oam.vf.VisitContextFactory";
    
    private FacesContext _facesContext;
    
    private FaceletFactory _factory;

    private LinkedList<UIComponent> _compositeComponentStack;
    
    private LinkedList<UniqueIdVendor> _uniqueIdVendorStack;
    
    private LinkedList<Map.Entry<String, EditableValueHolderAttachedObjectHandler>> _enclosingValidatorIdsStack;
    
    private Boolean _isRefreshingTransientBuild;
    
    private Boolean _isMarkInitialState;
    
    private Boolean _isBuildingViewMetadata;
    
    private Boolean _refreshTransientBuildOnPSS;
        
    private Boolean _usingPSSOnThisView;

    private List<Map<String, UIComponent>> _componentsMarkedForDeletion;
    
    private Map<String, UIComponent> _relocatableResourceForDeletion;
    
    private int _deletionLevel;
    
    private Map<UIComponent, List<AttachedObjectHandler>> _attachedObjectHandlers;
    
    private Map<UIComponent, Map<String, Object>> _methodExpressionsTargeted;
    
    private Map<UIComponent, Map<String, Boolean>> _compositeComponentAttributesMarked;

    private static final String VIEWROOT_FACELET_ID = "oam.VIEW_ROOT";
    
    private Lazy<SectionUniqueIdCounter> _sectionUniqueIdCounter;
    
    private Lazy<SectionUniqueIdCounter> _sectionUniqueComponentIdCounter;
    
    private List<String> _uniqueIdsList;
    private Iterator<String> _uniqueIdsIterator;
    private int _level;
    
    private int _isInMetadataSection;
    private Lazy<SectionUniqueIdCounter> _sectionUniqueMetadataIdCounter;
    private Lazy<SectionUniqueIdCounter> _sectionUniqueNormalIdCounter;
    private Lazy<SectionUniqueIdCounter> _sectionUniqueComponentMetadataIdCounter;
    private Lazy<SectionUniqueIdCounter> _sectionUniqueComponentNormalIdCounter;
    
    private List<SectionUniqueIdCounter> _sectionUniqueIdCounterStack;
    private List<SectionUniqueIdCounter> _sectionUniqueComponentIdCounterStack;
    
    private StringBuilder _sharedStringBuilder;
    
    private int _ccLevel;
    
    private boolean _dynamicComponentHandler;
    private boolean _oldRefreshingTransientBuild;
    private boolean _dynamicComponentTopLevel;
    private int _dynamicComponentSection = 0;
    
    private List<Integer> _dynamicOldDeletionLevel;
    
    private VisitContextFactory _visitContextFactory = null;
    private UIViewRoot _viewRoot = null;
    private MyfacesConfig myfacesConfig;
    
    public FaceletCompositionContextImpl(FaceletFactory factory, FacesContext facesContext)
    {
        super();
        _factory = factory;
        _facesContext = facesContext;
        _componentsMarkedForDeletion = new ArrayList<>();
        _relocatableResourceForDeletion = new HashMap<>();
        _deletionLevel = -1;
        _sectionUniqueIdCounter = new Lazy(() -> new SectionUniqueIdCounter());
        //Cached at facelet view
        myfacesConfig = MyfacesConfig.getCurrentInstance(facesContext);

        if (myfacesConfig.getComponentUniqueIdsCacheSize() > 0)
        {
            String[] componentIdsCache = (String[]) facesContext.getExternalContext().
                    getApplicationMap().get(FaceletViewDeclarationLanguage.CACHED_COMPONENT_IDS);
            if (componentIdsCache != null)
            {
                _sectionUniqueComponentIdCounter = new Lazy(() -> new SectionUniqueIdCounter("_", componentIdsCache));
            }
            else
            {
                _sectionUniqueComponentIdCounter = new Lazy(() -> new SectionUniqueIdCounter("_"));
            }
        }
        else
        {
            _sectionUniqueComponentIdCounter = new Lazy(() -> new SectionUniqueIdCounter("_"));
        }
        _sectionUniqueNormalIdCounter = _sectionUniqueIdCounter;
        _sectionUniqueComponentNormalIdCounter = _sectionUniqueComponentIdCounter;
        _uniqueIdsList = null;
        _uniqueIdsIterator = null;
        _level = 0;
        _isInMetadataSection = 0;
        _sharedStringBuilder = null;
        _ccLevel = 0;
        _viewRoot = null;
    }
    
    /**
     * This constructor is intended for places where the id generation strategy needs to be changed
     * adding a unique base id, like for example on a dynamic component creation.
     * 
     * @param factory
     * @param facesContext
     * @param base 
     */
    public FaceletCompositionContextImpl(FaceletFactory factory, FacesContext facesContext, String base)
    {
        this(factory, facesContext);
        _sectionUniqueIdCounter = new Lazy(() -> new SectionUniqueIdCounter(base+ '_'));
        _sectionUniqueComponentIdCounter = new Lazy(() -> new SectionUniqueIdCounter('_' + base + '_'));
        _sectionUniqueNormalIdCounter = _sectionUniqueIdCounter;
        _sectionUniqueComponentNormalIdCounter = _sectionUniqueComponentIdCounter;
        _dynamicComponentTopLevel = true;
        _dynamicComponentSection = 1;
    }
    
    
    @Override
    public void setUniqueIdsIterator(Iterator<String> uniqueIdsIterator)
    {
        _uniqueIdsList = null;
        _uniqueIdsIterator = uniqueIdsIterator;
    }
    
    @Override
    public void initUniqueIdRecording()
    {
        _uniqueIdsList = new LinkedList<>();
        _uniqueIdsIterator = null;
    }
    
    @Override
    public void addUniqueId(String uniqueId)
    {
        if (_uniqueIdsList != null && _level == 0 && !(_isInMetadataSection > 0) 
            && !(_dynamicComponentSection > 0))
        {
            _uniqueIdsList.add(uniqueId);
        }
    }
    
    @Override
    public String getUniqueIdFromIterator()
    {
        if (_uniqueIdsIterator != null && _uniqueIdsIterator.hasNext() && 
                _level == 0 && !(_isInMetadataSection > 0) && !(_dynamicComponentSection > 0))
        {
            return _uniqueIdsIterator.next();
        }
        return null;
    }
    
    @Override
    public List<String> getUniqueIdList()
    {
        return _uniqueIdsList;
    }

    @Override
    public FaceletFactory getFaceletFactory()
    {
        return _factory;
    }
    
    @Override
    public void release(FacesContext facesContext)
    {
        super.release(facesContext);
        _factory = null;
        _facesContext = null;
        _compositeComponentStack = null;
        _enclosingValidatorIdsStack = null;
        _uniqueIdVendorStack = null;
        _componentsMarkedForDeletion = null;
        _relocatableResourceForDeletion = null;
        _sectionUniqueIdCounter = null;
        _sectionUniqueNormalIdCounter = null;
        _sectionUniqueMetadataIdCounter = null;
        _sectionUniqueComponentIdCounter = null;
        _sectionUniqueComponentNormalIdCounter = null;
        _sectionUniqueComponentMetadataIdCounter = null;
        _sharedStringBuilder = null;
        _visitContextFactory = null;
        _dynamicOldDeletionLevel = null;
        _viewRoot = null;
    }
   
    @Override
    public UIComponent getCompositeComponentFromStack()
    {
        if (_compositeComponentStack != null && !_compositeComponentStack.isEmpty())
        {
            return _compositeComponentStack.peek();
        }
        return null;
    }

    @Override
    public void pushCompositeComponentToStack(UIComponent parent)
    {
        if (_compositeComponentStack == null)
        {
            _compositeComponentStack = new LinkedList<>();
        }
        _compositeComponentStack.addFirst(parent);
        _ccLevel++;
    }

    @Override
    public void popCompositeComponentToStack()
    {
        if (_compositeComponentStack != null && !_compositeComponentStack.isEmpty())
        {
            _compositeComponentStack.removeFirst();
        }
        _ccLevel--;
    }
    
    @Override
    public int getCompositeComponentLevel()
    {
        return _ccLevel;
    }

    @Override
    public UniqueIdVendor getUniqueIdVendorFromStack()
    {
        if (_uniqueIdVendorStack != null && !_uniqueIdVendorStack.isEmpty())
        {
            return _uniqueIdVendorStack.peek();
        }
        return null;
    }

    @Override
    public void popUniqueIdVendorToStack()
    {
        if (_uniqueIdVendorStack != null && !_uniqueIdVendorStack.isEmpty())
        {
            _uniqueIdVendorStack.removeFirst();
        }
    }

    @Override
    public void pushUniqueIdVendorToStack(UniqueIdVendor parent)
    {
        if (_uniqueIdVendorStack == null)
        {
            _uniqueIdVendorStack = new LinkedList<>();
        }
        _uniqueIdVendorStack.addFirst(parent);
    }
    
    /**
     * Removes top of stack.
     * @since 2.0
     */
    @Override
    public void popEnclosingValidatorIdToStack()
    {
        if (_enclosingValidatorIdsStack != null && !_enclosingValidatorIdsStack.isEmpty())
        {
            _enclosingValidatorIdsStack.removeFirst();
        }
    }
    
    @Override
    public void pushEnclosingValidatorIdToStack(String validatorId, 
            EditableValueHolderAttachedObjectHandler attachedObjectHandler)
    {
        if (_enclosingValidatorIdsStack == null)
        {
            _enclosingValidatorIdsStack = new LinkedList<>();
        }

        _enclosingValidatorIdsStack.addFirst(new SimpleEntry<>(validatorId, attachedObjectHandler));
    }

    public Iterator<Map.Entry<String, EditableValueHolderAttachedObjectHandler>> getEnclosingValidatorIdsAndHandlers()
    {
        if (_enclosingValidatorIdsStack != null && !_enclosingValidatorIdsStack.isEmpty())
        {
            return _enclosingValidatorIdsStack.iterator(); 
        }
        return null;
    }
    
    @Override
    public boolean containsEnclosingValidatorId(String id)
    {
        if (_enclosingValidatorIdsStack != null && !_enclosingValidatorIdsStack.isEmpty())
        {
            for (Map.Entry<String, EditableValueHolderAttachedObjectHandler> entry : _enclosingValidatorIdsStack)
            {
                if (entry.getKey().equals(id))
                {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public boolean isRefreshingTransientBuild()
    {
        if (_isRefreshingTransientBuild == null)
        {
            _isRefreshingTransientBuild = FaceletViewDeclarationLanguage.isRefreshingTransientBuild(_facesContext);
        }
        return _isRefreshingTransientBuild;
    }

    @Override
    public boolean isMarkInitialState()
    {
        if (_isMarkInitialState == null)
        {
            _isMarkInitialState = FaceletViewDeclarationLanguage.isMarkInitialState(_facesContext);
        }
        return _isMarkInitialState;
    }

    @Override
    public void setMarkInitialState(boolean value)
    {
        _isMarkInitialState = value;
    }

    @Override
    public boolean isRefreshTransientBuildOnPSS()
    {
        if (_refreshTransientBuildOnPSS == null)
        {
            _refreshTransientBuildOnPSS = FaceletViewDeclarationLanguage.
                isRefreshTransientBuildOnPSS(_facesContext);
        }
        return _refreshTransientBuildOnPSS;
    }
    
    @Override
    public boolean isRefreshTransientBuildOnPSSPreserveState()
    {
        return myfacesConfig.isRefreshTransientBuildOnPSSPreserveState();
    }
    
    @Override
    public boolean isBuildingViewMetadata()
    {
        if (_isBuildingViewMetadata == null)
        {
            _isBuildingViewMetadata = FaceletViewDeclarationLanguage.isBuildingViewMetadata(_facesContext);
        }
        return _isBuildingViewMetadata;
    }

    @Override
    public boolean isUsingPSSOnThisView()
    {
        if (_usingPSSOnThisView == null)
        {
            _usingPSSOnThisView = FaceletViewDeclarationLanguage.isUsingPSSOnThisView(_facesContext);
        }
        return _usingPSSOnThisView;
    }
    
    @Override
    public boolean isMarkInitialStateAndIsRefreshTransientBuildOnPSS()
    {
        return isMarkInitialState() && isRefreshTransientBuildOnPSS();
    }

    @Override
    public ELExpressionCacheMode getELExpressionCacheMode()
    {
        return myfacesConfig.getELExpressionCacheMode();
    }

    @Override
    public boolean isWrapTagExceptionsAsContextAware()
    {
        return myfacesConfig.isWrapTagExceptionsAsContextAware();
    }

    @Override
    public void addAttachedObjectHandler(UIComponent compositeComponentParent, AttachedObjectHandler handler)
    {
        List<AttachedObjectHandler> list = null;
        if (_attachedObjectHandlers == null)
        {
            _attachedObjectHandlers = new HashMap<>();
        }
        else
        {
            list = _attachedObjectHandlers.get(compositeComponentParent);
        }

        if (list == null)
        {
            list = new ArrayList<>();
            _attachedObjectHandlers.put(compositeComponentParent, list);
        }

        list.add(handler);
    }

    @Override
    public void removeAttachedObjectHandlers(UIComponent compositeComponentParent)
    {
        if (_attachedObjectHandlers == null)
        {
            return;
        }
        _attachedObjectHandlers.remove(compositeComponentParent);
    }

    @Override
    public List<AttachedObjectHandler> getAttachedObjectHandlers(UIComponent compositeComponentParent)
    {
        if (_attachedObjectHandlers == null)
        {
            return null;
        }
        return _attachedObjectHandlers.get(compositeComponentParent);
    }
    
    @Override
    public void addMethodExpressionTargeted(UIComponent targetedComponent, String attributeName, Object backingValue)
    {
        Map<String, Object> map = null;
        if (_methodExpressionsTargeted == null)
        {
            _methodExpressionsTargeted = new HashMap<>();
        }
        else
        {
            map = _methodExpressionsTargeted.get(targetedComponent);
        }

        if (map == null)
        {
            map = new HashMap<>(8);
            _methodExpressionsTargeted.put(targetedComponent, map);
        }

        map.put(attributeName, backingValue);
    }

    @Override
    public boolean isMethodExpressionAttributeApplied(UIComponent compositeComponentParent, String attributeName)
    {
        if (_compositeComponentAttributesMarked == null)
        {
            return false;
        }
        Map<String, Boolean> map = _compositeComponentAttributesMarked.get(compositeComponentParent);
        if (map == null)
        {
            return false;
        }
        Boolean v = map.get(attributeName);
        return v == null ? false : v;
    }
    
    @Override
    public void markMethodExpressionAttribute(UIComponent compositeComponentParent, String attributeName)
    {
        Map<String, Boolean> map = null;
        if (_compositeComponentAttributesMarked == null)
        {
            _compositeComponentAttributesMarked = new HashMap<>(); 
        }
        else
        {
            map = _compositeComponentAttributesMarked.get(compositeComponentParent);
        }
        
        if (map == null)
        {
            map = new HashMap<>(8);
            _compositeComponentAttributesMarked.put(compositeComponentParent, map);
        }
        map.put(attributeName, Boolean.TRUE);
        
    }
    
    @Override
    public void clearMethodExpressionAttribute(UIComponent compositeComponentParent, String attributeName)
    {
        if (_compositeComponentAttributesMarked == null)
        {
            return;
        }
        Map<String, Boolean> map = _compositeComponentAttributesMarked.get(compositeComponentParent);
        if (map == null)
        {
            //No map, so just return
            return;
        }
        map.put(attributeName, Boolean.FALSE);
    }
    
    
    @Override
    public Object removeMethodExpressionTargeted(UIComponent targetedComponent, String attributeName)
    {
        if (_methodExpressionsTargeted == null)
        {
            return null;
        }
        Map<String, Object> map = _methodExpressionsTargeted.get(targetedComponent);
        if (map != null)
        {
            return map.remove(attributeName);
        }
        return null;
    }

    /**
     * Add a level of components marked for deletion.
     */
    private void increaseComponentLevelMarkedForDeletion()
    {
        _deletionLevel++;
        if (_componentsMarkedForDeletion.size() <= _deletionLevel)
        {
            _componentsMarkedForDeletion.add(new HashMap<String, UIComponent>());
        }
    }

    /**
     * Remove the last component level from the components marked to be deleted. The components are removed
     * from this list because they are deleted from the tree. This is done in ComponentSupport.finalizeForDeletion.
     *
     * @return the array of components that are removed.
     */
    private void decreaseComponentLevelMarkedForDeletion()
    {
        //The common case is this co
        if (!_componentsMarkedForDeletion.get(_deletionLevel).isEmpty())
        {
            _componentsMarkedForDeletion.get(_deletionLevel).clear();
        }
        _deletionLevel--;
    }

    /** Mark a component to be deleted from the tree. The component to be deleted is addded on the
     * current level. This is done from ComponentSupport.markForDeletion
     *
     * @param id
     * @param component the component marked for deletion.
     */
    private void markComponentForDeletion(String id , UIComponent component)
    {
        _componentsMarkedForDeletion.get(_deletionLevel).put(id, component);
    }

    /**
     * Remove a component from the last level of components marked to be deleted.
     *
     * @param id
     */
    private UIComponent removeComponentForDeletion(String id)
    {
        UIComponent removedComponent = _componentsMarkedForDeletion.get(_deletionLevel).remove(id); 
        if (removedComponent != null && _deletionLevel > 0)
        {
            _componentsMarkedForDeletion.get(_deletionLevel-1).remove(id);
        }
        return removedComponent;
    }
    
    @Override
    public void markForDeletion(UIComponent component)
    {
        increaseComponentLevelMarkedForDeletion();
        
        String id = (String) component.getAttributes().get(ComponentSupport.MARK_CREATED);
        id = (id == null) ? VIEWROOT_FACELET_ID : id;
        markComponentForDeletion(id, component);
        
        
        if (component.getFacetCount() > 0)
        {
            for (UIComponent fc: component.getFacets().values())
            {
                id = (String) fc.getAttributes().get(ComponentSupport.MARK_CREATED);
                if (id != null)
                {
                    markComponentForDeletion(id, fc);
                }
                else if (Boolean.TRUE.equals(fc.getAttributes().get(ComponentSupport.FACET_CREATED_UIPANEL_MARKER)))
                {
                    //Mark its children, but do not mark itself.
                    int childCount = fc.getChildCount();
                    if (childCount > 0)
                    {
                        for (int i = 0; i < childCount; i++)
                        {
                            UIComponent child = fc.getChildren().get(i);
                            id = (String) child.getAttributes().get(ComponentSupport.MARK_CREATED);
                            if (id != null)
                            {
                                markComponentForDeletion(id, child);
                            }
                        }
                    }
                }
            }
        }
                
        int childCount = component.getChildCount();
        if (childCount > 0)
        {
            for (int i = 0; i < childCount; i++)
            {
                UIComponent child = component.getChildren().get(i);
                id = (String) child.getAttributes().get(ComponentSupport.MARK_CREATED);
                if (id != null)
                {
                    markComponentForDeletion(id, child);
                }
            }
        }
    }
    
    @Override
    public void removeComponentForDeletion(UIComponent component)
    {
        String id = (String) component.getAttributes().get(ComponentSupport.MARK_CREATED);
        if (id != null)
        {
            removeComponentForDeletion(id);
        }
        else if (id == null
                 && Boolean.TRUE.equals(component.getAttributes().get(ComponentSupport.FACET_CREATED_UIPANEL_MARKER)))
        {
            int childCount = component.getChildCount();
            if (childCount > 0)
            {
                for (int i = 0, size = childCount; i < size; i++)
                {
                    UIComponent child = component.getChildren().get(i);
                    id = (String) child.getAttributes().get(ComponentSupport.MARK_CREATED);
                    if (id != null)
                    {
                        removeComponentForDeletion(id);
                    }
                }
            }
        }
    }
    
    @Override
    public void finalizeForDeletion(UIComponent component)
    {
        String id = (String) component.getAttributes().get(ComponentSupport.MARK_CREATED);
        id = (id == null) ? VIEWROOT_FACELET_ID : id;
        // remove any existing marks of deletion
        removeComponentForDeletion(id);
        
        // finally remove any children marked as deleted
        int childCount = component.getChildCount();
        if (childCount > 0)
        {
            for (int i = 0; i < childCount; i ++)
            {
                UIComponent child = component.getChildren().get(i);
                id = (String) child.getAttributes().get(ComponentSupport.MARK_CREATED); 
                if (id != null && removeComponentForDeletion(id) != null)
                {
                    component.getChildren().remove(i);
                    i--;
                    childCount--;
                }
            }
        }

        // remove any facets marked as deleted
        
        if (component.getFacetCount() > 0)
        {
            Map<String, UIComponent> facets = component.getFacets();
            for (Iterator<UIComponent> itr = facets.values().iterator(); itr.hasNext();)
            {
                UIComponent fc = itr.next();
                id = (String) fc.getAttributes().get(ComponentSupport.MARK_CREATED);
                if (id != null && removeComponentForDeletion(id) != null)
                {
                    itr.remove();
                }
                else if (id == null
                         && Boolean.TRUE.equals(fc.getAttributes().get(ComponentSupport.FACET_CREATED_UIPANEL_MARKER)))
                {
                    if (fc.getChildCount() > 0)
                    {
                        for (int i = 0, size = fc.getChildCount(); i < size; i++)
                        {
                            UIComponent child = fc.getChildren().get(i);
                            id = (String) child.getAttributes().get(ComponentSupport.MARK_CREATED);
                            if (id != null && removeComponentForDeletion(id) != null)
                            {
                                fc.getChildren().remove(i);
                                i--;
                                size--;
                            }
                        }
                    }
                    if (fc.getChildCount() == 0)
                    {
                        itr.remove();
                    }
                }
            }
        }
        
        decreaseComponentLevelMarkedForDeletion();
    }
    
    @Override
    public void markRelocatableResourceForDeletion(UIComponent component)
    {
        // The idea is keep track of the component resources that can be relocated
        // to later check which resources were not refreshed and delete them.
        String id = (String) component.getAttributes().get(ComponentSupport.MARK_CREATED);
        if (id != null)
        {
            _relocatableResourceForDeletion.put(id, component);
        }
    }

    @Override
    public void finalizeRelocatableResourcesForDeletion(UIViewRoot root)
    {
        String id = null;
        //Check facets 
        if (root.getFacetCount() > 0)
        {
            Map<String, UIComponent> facets = root.getFacets();
            for (Iterator<UIComponent> itr = facets.values().iterator(); itr.hasNext();)
            {
                UIComponent fc = itr.next();
                // It is necessary to check only the facets that are used as holder for
                // component resources. To do that, the best way is check the ones that
                // has id starting with "jakarta_faces_location_"
                if (fc.getId() != null && fc.getId().startsWith(JAKARTA_FACES_LOCATION_PREFIX))
                {
                    // Check all children with MARK_CREATED and if one is found, check if it was
                    // refreshed by the algorithm.
                    int childCount = fc.getChildCount();
                    if (childCount > 0)
                    {
                        for (int i = 0; i < childCount; i ++)
                        {
                            UIComponent child = fc.getChildren().get(i);
                            id = (String) child.getAttributes().get(ComponentSupport.MARK_CREATED); 
                            if (id != null && finalizeRelocatableResourcesForDeletion(id) == null)
                            {
                                fc.getChildren().remove(i);
                                i--;
                                childCount--;
                            }
                        }
                    }
                }
            }
        }
    }
    
    private UIComponent finalizeRelocatableResourcesForDeletion(String id)
    {
        return _relocatableResourceForDeletion.remove(id); 
    }
    
    @Override
    public String startComponentUniqueIdSection()
    {
        _level++;
        _sectionUniqueComponentIdCounter.get().startUniqueIdSection();
        return _sectionUniqueIdCounter.get().startUniqueIdSection();
    }
    
    @Override
    public String startComponentUniqueIdSection(String base)
    {
        _level++;
        _sectionUniqueComponentIdCounter.get().startUniqueIdSection(base);
        return _sectionUniqueIdCounter.get().startUniqueIdSection(base);
    }

    @Override
    public void incrementUniqueId()
    {
        _sectionUniqueIdCounter.get().incrementUniqueId();
    }
    
    @Override
    public String generateUniqueId()
    {
        return _sectionUniqueIdCounter.get().generateUniqueId();
    }
    
    @Override
    public void generateUniqueId(StringBuilder builderToAdd)
    {
        _sectionUniqueIdCounter.get().generateUniqueId(builderToAdd);
    }

    @Override
    public String generateUniqueComponentId()
    {
        return _sectionUniqueComponentIdCounter.get().generateUniqueId();
    }
    
    @Override
    public void incrementUniqueComponentId()
    {
        _sectionUniqueComponentIdCounter.get().incrementUniqueId();
    }
    
    @Override
    public void endComponentUniqueIdSection()
    {
        _level--;
        _sectionUniqueIdCounter.get().endUniqueIdSection();
        _sectionUniqueComponentIdCounter.get().endUniqueIdSection();
    }
    
    @Override
    public void endComponentUniqueIdSection(String base)
    {
        _level--;
        _sectionUniqueIdCounter.get().endUniqueIdSection(base);
        _sectionUniqueComponentIdCounter.get().endUniqueIdSection(base);
    }
    
    @Override
    public void startMetadataSection()
    {
        if (_isInMetadataSection == 0)
        {
            if (_sectionUniqueMetadataIdCounter == null)
            {
                _sectionUniqueMetadataIdCounter = new Lazy(() -> new SectionUniqueIdCounter("__md_"));
            }
            if (_sectionUniqueComponentMetadataIdCounter == null)
            {
                _sectionUniqueComponentMetadataIdCounter = new Lazy(() -> new SectionUniqueIdCounter("__md_"));
            }
            //Replace the counter with metadata counter
            _sectionUniqueIdCounter = _sectionUniqueMetadataIdCounter;
            _sectionUniqueComponentIdCounter = _sectionUniqueComponentMetadataIdCounter;
        }
        _isInMetadataSection++;
    }
    
    @Override
    public void endMetadataSection()
    {
        _isInMetadataSection--;
        if (_isInMetadataSection == 0)
        {
            //Use normal id counter again
            _sectionUniqueIdCounter = _sectionUniqueNormalIdCounter;
            _sectionUniqueComponentIdCounter = _sectionUniqueComponentNormalIdCounter;
        }
    }
    
    @Override
    public boolean isInMetadataSection()
    {
       return _isInMetadataSection > 0;
    }
    
    @Override
    public boolean isRefreshingSection()
    {
       return isRefreshingTransientBuild() ||  (!isBuildingViewMetadata() && isInMetadataSection());
    }
    
    @Override
    public StringBuilder getSharedStringBuilder()
    {
        if (_sharedStringBuilder == null)
        {
            _sharedStringBuilder = new StringBuilder();
        }
        else
        {
            _sharedStringBuilder.setLength(0);
        }
        return _sharedStringBuilder;
    }
    
    @Override
    public boolean isDynamicCompositeComponentHandler()
    {
        return this._dynamicComponentHandler;
    }
    
    @Override
    public void setDynamicCompositeComponentHandler(boolean value)
    {
        this._dynamicComponentHandler = value;
    }

    @Override
    public void pushDynamicComponentSection(String base)
    {
        if (_sectionUniqueIdCounterStack == null)
        {
            _sectionUniqueIdCounterStack = new ArrayList<SectionUniqueIdCounter>();
        }
        if (_sectionUniqueComponentIdCounterStack == null)
        {
            _sectionUniqueComponentIdCounterStack = new ArrayList<SectionUniqueIdCounter>();
        }
        // Activate refresh transient build over dynamic component section.
        if (_sectionUniqueComponentIdCounterStack.isEmpty())
        {
            _oldRefreshingTransientBuild = _isRefreshingTransientBuild;
        }
        _isRefreshingTransientBuild = true;
        
        _sectionUniqueIdCounterStack.add(_sectionUniqueIdCounter.get());
        _sectionUniqueComponentIdCounterStack.add(_sectionUniqueComponentIdCounter.get());
        _sectionUniqueIdCounter = new Lazy(() -> new SectionUniqueIdCounter(base + '_'));
        _sectionUniqueComponentIdCounter = new Lazy(() -> new SectionUniqueIdCounter('_' + base + '_'));
        _sectionUniqueNormalIdCounter = _sectionUniqueIdCounter;
        _sectionUniqueComponentNormalIdCounter = _sectionUniqueComponentIdCounter;
        _dynamicComponentTopLevel = true;
        _dynamicComponentSection++;
        if (_dynamicOldDeletionLevel == null)
        {
            _dynamicOldDeletionLevel = new ArrayList<Integer>(4);
        }
        _dynamicOldDeletionLevel.add(_deletionLevel);
        // Increase one level in the mark/delete algorithm to avoid any interference in the previous code.
        increaseComponentLevelMarkedForDeletion();
        
    }

    @Override
    public void popDynamicComponentSection()
    {
        decreaseComponentLevelMarkedForDeletion();
        int oldDeletionLevel = _dynamicOldDeletionLevel.remove(_dynamicOldDeletionLevel.size()-1);
        if (_deletionLevel != oldDeletionLevel)
        {
            // This happens because in a dynamic component section, the dynamic top component level does not take
            // part in the algorithm. The easiest solution so far is just decrease one level to let it as it was
            // before enter the algorithm.
            decreaseComponentLevelMarkedForDeletion();
        }
        
        _sectionUniqueIdCounter.reset(
                _sectionUniqueIdCounterStack.remove(_sectionUniqueIdCounterStack.size() - 1));
        _sectionUniqueComponentIdCounter.reset(
                _sectionUniqueComponentIdCounterStack.remove(_sectionUniqueComponentIdCounterStack.size() - 1));
        
        //Restore refresh section
        if (_sectionUniqueComponentIdCounterStack.isEmpty())
        {
            _isRefreshingTransientBuild = _oldRefreshingTransientBuild;
        }
        
        _sectionUniqueNormalIdCounter = _sectionUniqueIdCounter;
        _sectionUniqueComponentNormalIdCounter = _sectionUniqueComponentIdCounter;
        _dynamicComponentTopLevel = false;
        _dynamicComponentSection--;
    }
    
    @Override
    public boolean isDynamicComponentTopLevel()
    {
        return _dynamicComponentTopLevel;
    }
    
    @Override
    public void setDynamicComponentTopLevel(boolean value)
    {
        _dynamicComponentTopLevel = value;
    }
    
    @Override
    public boolean isDynamicComponentSection()
    {
        return _dynamicComponentSection > 0;
    }

    @Override
    public void setViewRoot(UIViewRoot root)
    {
        this._viewRoot = root;
    }

    @Override
    public UIViewRoot getViewRoot(FacesContext facesContext)
    {
        if (_viewRoot == null)
        {
            return facesContext.getViewRoot();
        }
        return _viewRoot;
    }
    
    @Override
    public VisitContextFactory getVisitContextFactory()
    {
        if (_visitContextFactory == null)
        {
            // Store it in application map improve performance because it avoids FactoryFinde.getFactory(...) call
            // which has synchronized blocks.
            _visitContextFactory = (VisitContextFactory) _facesContext.getExternalContext().getApplicationMap()
                    .computeIfAbsent(
                            VISIT_CONTEXT_FACTORY,
                            k -> FactoryFinder.getFactory(FactoryFinder.VISIT_CONTEXT_FACTORY));
        }
        return _visitContextFactory;
    }
    
    private static class SimpleEntry<K, V> implements Map.Entry<K, V>
    {
        private final K _key;
        private final V _value;

        public SimpleEntry(K key, V value)
        {
            _key = key;
            _value = value;
        }
        
        @Override
        public K getKey()
        {
            return _key;
        }

        @Override
        public V getValue()
        {
            return _value;
        }

        @Override
        public int hashCode()
        {
            final int prime = 31;
            int result = 1;
            result = prime * result + ((_key == null) ? 0 : _key.hashCode());
            result = prime * result + ((_value == null) ? 0 : _value.hashCode());
            return result;
        }

        @SuppressWarnings("unchecked")
        @Override
        public boolean equals(Object obj)
        {
            if (this == obj)
            {
                return true;
            }
            if (obj == null)
            {
                return false;
            }
            if (getClass() != obj.getClass())
            {
                return false;
            }
            SimpleEntry other = (SimpleEntry) obj;
            if (_key == null)
            {
                if (other._key != null)
                {
                    return false;
                }
            }
            else if (!_key.equals(other._key))
            {
                return false;
            }
            
            if (_value == null)
            {
                if (other._value != null)
                {
                    return false;
                }
            }
            else if (!_value.equals(other._value))
            {
                return false;
            }
            return true;
        }

        @Override
        public V setValue(V value)
        {
            throw new UnsupportedOperationException();
        }
    }
}
