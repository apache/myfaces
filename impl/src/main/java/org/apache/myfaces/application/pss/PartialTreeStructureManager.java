/*
 * Copyright 2004 The Apache Software Foundation.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.apache.myfaces.application.pss;

import org.apache.myfaces.shared_impl.util.ClassUtils;

import javax.faces.component.UIViewRoot;
import javax.faces.component.UIComponent;
import javax.faces.context.FacesContext;
import java.util.*;

/**
 * @author Martin Haimberger
 */
public class PartialTreeStructureManager
{
    public static final String PARTIAL_STATE_CLASS_IDS = PartialTreeStructureManager.class.getName() + ".PARTIAL_STATE_MANAGER_COMPONENT_IDS";

    public PartialTreeStructureManager(FacesContext facesContext)
    {
        _facesContext = facesContext;
    }

    public Object buildTreeStructureToSave(UIViewRoot viewRoot, FacesContext facesContext)
    {
        Object savedStateTree = viewRoot.processSaveState(facesContext);
        if (viewRoot instanceof UIViewRootWrapper) {
            // the first call ... all components have to be saved in the template
           return internalBuildInitalTreeStructureToSave(viewRoot,facesContext, savedStateTree,0);
        }
        else return internalBuildTreeStructureToSave(viewRoot,facesContext, savedStateTree,0);
    }

    private TreeStructComponent internalBuildInitalTreeStructureToSave(UIComponent component,FacesContext facesContext, Object state, int childIndex)
    {

        Object myState = ((Object[])state)[0];
        Map facetStateMap = (Map)((Object[])state)[1];
        List childrenStateList = (List)((Object[])state)[2];

        TreeStructComponent structComp = new TreeStructComponent(convertStringToComponentClassId(facesContext,component.getClass().getName()),
                                                                      component.getId(),myState,component.isTransient());

        //children
        if (component.getChildCount() > 0)
        {
            List childList = component.getChildren();
            List structChildList = new ArrayList();
            for (int i = 0, len = childList.size(); i < len; i++)
            {
                UIComponent child = (UIComponent)childList.get(i);
               if (!child.isTransient())
               {

                    TreeStructComponent structChild = internalBuildInitalTreeStructureToSave(child,facesContext,childrenStateList != null ? childrenStateList.get(childIndex++):null,0);
                    structChildList.add(structChild);
                }
                else
               {

                   child.setTransient(false);
                   TreeStructComponent structChild = internalBuildInitalTreeStructureToSave(child,facesContext,child.processSaveState(facesContext),0);
                   structChildList.add(structChild);
                   child.setTransient(true);

               }
            }
            TreeStructComponent[] childArray = (TreeStructComponent[])structChildList.toArray(new TreeStructComponent[structChildList.size()]);
            structComp.setChildren(childArray);
        }

        //facets
        Map facetMap = component.getFacets();
        if (!facetMap.isEmpty())
        {
            List structFacetList = new ArrayList();
            for (Iterator it = facetMap.entrySet().iterator(); it.hasNext(); )
            {
                Map.Entry entry = (Map.Entry)it.next();
                UIComponent child = (UIComponent)entry.getValue();
                String facetName = (String)entry.getKey();
                if (!child.isTransient())
                {

                    TreeStructComponent structChild = internalBuildInitalTreeStructureToSave(child,facesContext,facetStateMap.get(facetName),0);
                    structFacetList.add(new Object[] {facetName, structChild});
                }
                else
               {
                   // this is a transient Component ... save it anyway
                   child.setTransient(false);
                   TreeStructComponent structChild = internalBuildInitalTreeStructureToSave(child,facesContext,child.processSaveState(facesContext),0);
                   structFacetList.add(new Object[] {facetName, structChild});
                   child.setTransient(true);
               }
            }
            Object[] facetArray = structFacetList.toArray(new Object[structFacetList.size()]);
            structComp.setFacets(facetArray);
        }

        return structComp;
    }

    private TreeStructComponent internalBuildTreeStructureToSave(UIComponent component,FacesContext facesContext, Object state, int childIndex)
    {

        Object myState = ((Object[])state)[0];
        Map facetStateMap = (Map)((Object[])state)[1];
        List childrenStateList = (List)((Object[])state)[2];

        TreeStructComponent structComp = new TreeStructComponent(convertStringToComponentClassId(facesContext,component.getClass().getName()),
                                                                      component.getId(),myState,component.isTransient());

        //children
        if (component.getChildCount() > 0)
        {
            List childList = component.getChildren();
            List structChildList = new ArrayList();
            for (int i = 0, len = childList.size(); i < len; i++)
            {
                UIComponent child = (UIComponent)childList.get(i);
               if (!child.isTransient())
               {

                    TreeStructComponent structChild = internalBuildTreeStructureToSave(child,facesContext,childrenStateList != null ? childrenStateList.get(childIndex++):null,0);
                    structChildList.add(structChild);
                }
            }
            TreeStructComponent[] childArray = (TreeStructComponent[])structChildList.toArray(new TreeStructComponent[structChildList.size()]);
            structComp.setChildren(childArray);
        }

        //facets
        Map facetMap = component.getFacets();
        if (!facetMap.isEmpty())
        {
            List structFacetList = new ArrayList();
            for (Iterator it = facetMap.entrySet().iterator(); it.hasNext(); )
            {
                Map.Entry entry = (Map.Entry)it.next();
                UIComponent child = (UIComponent)entry.getValue();
                String facetName = (String)entry.getKey();
                if (!child.isTransient())
                {

                    TreeStructComponent structChild = internalBuildTreeStructureToSave(child,facesContext,facetStateMap.get(facetName),0);
                    structFacetList.add(new Object[] {facetName, structChild});
                }

            }
            Object[] facetArray = structFacetList.toArray(new Object[structFacetList.size()]);
            structComp.setFacets(facetArray);
        }

        return structComp;
    }



    public UIViewRoot restoreTreeStructure(FacesContext facesContext,Object treeStructRoot)
    {
        if (treeStructRoot instanceof TreeStructComponent)
        {
            return (UIViewRoot)internalRestoreTreeStructure((TreeStructComponent)treeStructRoot,facesContext);
        }
        else
        {
            throw new IllegalArgumentException("TreeStructure of type " + treeStructRoot.getClass().getName() + " is not supported.");
        }
    }

    private UIComponent internalRestoreTreeStructure(TreeStructComponent treeStructComp,FacesContext facesContext)
    {
        String compClass = convertComponentClassIdToString(facesContext,treeStructComp.getComponentClass());
        String compId = treeStructComp.getComponentId();
        UIComponent component = (UIComponent) ClassUtils.newInstance(compClass);
        component.setId(compId);
        component.setTransient(treeStructComp.isTransient());

        //children
        TreeStructComponent[] childArray = treeStructComp.getChildren();
        if (childArray != null)
        {
            List childList = component.getChildren();
            for (int i = 0, len = childArray.length; i < len; i++)
            {
                UIComponent child = internalRestoreTreeStructure(childArray[i],facesContext);
                childList.add(child);
            }
        }

        //facets
        Object[] facetArray = treeStructComp.getFacets();
        if (facetArray != null)
        {
            Map facetMap = component.getFacets();
            for (int i = 0, len = facetArray.length; i < len; i++)
            {
                TreeStructComponent structChild = (TreeStructComponent)((Object[])facetArray[i])[1];
                String facetName = (String)((Object[])facetArray[i])[0];
                UIComponent child = internalRestoreTreeStructure(structChild,facesContext);
                facetMap.put(facetName, child);
            }
        }


        return component;
    }



    private String convertComponentClassIdToString(FacesContext facesContext,Integer classId){
        Object[] idmaps = (Object[])facesContext.getExternalContext().getApplicationMap().get(PARTIAL_STATE_CLASS_IDS);

        if ( idmaps== null)
        {
            // create on
            idmaps = new Object[2];
            // contains the Classid as Map
            idmaps[0] = new HashMap();
            idmaps[1] = new HashMap();
            facesContext.getExternalContext().getApplicationMap().put(PARTIAL_STATE_CLASS_IDS,idmaps);
        }
        return (String)((HashMap)idmaps[0]).get(classId);
    }

    private Integer convertStringToComponentClassId(FacesContext facesContext,String stringToConvert){

        // if it was the first time and the wrapper was use ... use the original UIViewRoot

        if (stringToConvert.equalsIgnoreCase("org.apache.myfaces.application.pss.UIViewRootWrapper")) {
            stringToConvert = "javax.faces.component.UIViewRoot";
        }

        Object[] idmaps = (Object[])facesContext.getExternalContext().getApplicationMap().get(PARTIAL_STATE_CLASS_IDS);

        if ( idmaps== null)
        {
            // create on
            idmaps = new Object[2];
            // contains the Classid as Map
            idmaps[0] = new HashMap();
            idmaps[1] = new HashMap();
        }
        Integer idInMap = (Integer)((HashMap)idmaps[1]).get(stringToConvert);
        HashMap idToStringMap=((HashMap)idmaps[0]);
        HashMap stringToIdMap=((HashMap)idmaps[1]);

        if (idInMap == null )
        {
            // this type is not jet registerd ... register now
            Integer id = new Integer(stringToIdMap.size());

            stringToIdMap.put(stringToConvert,id);
            idToStringMap.put(id,stringToConvert);
        }
        facesContext.getExternalContext().getApplicationMap().put(PARTIAL_STATE_CLASS_IDS,idmaps);
        return (Integer)stringToIdMap.get(stringToConvert);
    }


}
