/*
 * Copyright 2007 The Apache Software Foundation.
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
package org.apache.myfaces.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import javax.faces.FacesException;

import org.apache.myfaces.config.impl.digester.DigesterFacesConfigUnmarshallerImpl;
import org.apache.myfaces.config.impl.digester.elements.ConfigOthersSlot;
import org.apache.myfaces.config.impl.digester.elements.FacesConfig;
import org.apache.myfaces.config.impl.digester.elements.FacesConfigNameSlot;
import org.apache.myfaces.config.impl.digester.elements.OrderSlot;
import org.apache.myfaces.config.impl.digester.elements.Ordering;
import org.apache.myfaces.test.base.AbstractJsfTestCase;

public class OrderingFacesConfigTest extends AbstractJsfTestCase
{

    private DigesterFacesConfigUnmarshallerImpl _impl;
    
    public OrderingFacesConfigTest(String name)
    {
        super(name);
    }
    
    protected void setUp() throws Exception
    {
        super.setUp();
        _impl = new DigesterFacesConfigUnmarshallerImpl(null);
    }
    
    public void testSimpleOrdering() throws Exception
    {
        FacesConfig cfg = _impl.getFacesConfig(getClass().getResourceAsStream(
        "empty-config.xml"), "empty-config.xml");
        FacesConfig cfgA = _impl.getFacesConfig(getClass().getResourceAsStream(
            "a-config.xml"), "a-config.xml");
        FacesConfig cfgB = _impl.getFacesConfig(getClass().getResourceAsStream(
            "b-config.xml"), "b-config.xml");
        FacesConfig cfgC = _impl.getFacesConfig(getClass().getResourceAsStream(
            "c-config.xml"), "c-config.xml");
        
        
        List<FacesConfig> appConfigResources = new ArrayList<FacesConfig>();
        appConfigResources.add(cfgA);
        appConfigResources.add(cfgB);
        appConfigResources.add(cfgC);
        appConfigResources.add(cfg);
        appConfigResources.add(cfg);
        
        //Brute force       
        for (int i = 0; i < 30; i++)
        {
            Collections.shuffle(appConfigResources);
            applyAlgorithm(appConfigResources);
        }
    }
    
    public void testMiddleOrdering() throws Exception
    {
        FacesConfig cfg = _impl.getFacesConfig(getClass().getResourceAsStream(
        "empty-config.xml"), "empty-config.xml");        
        FacesConfig cfgA = new FacesConfig();
        FacesConfig cfgB = new FacesConfig();
        FacesConfig cfgC = new FacesConfig();
        FacesConfig cfgD = new FacesConfig();
        FacesConfig cfgE = new FacesConfig();
        
        cfgA.setName("A");
        cfgB.setName("B");
        cfgC.setName("C");
        cfgD.setName("D");
        cfgE.setName("E");
        
        cfgC.setOrdering(new Ordering());
        FacesConfigNameSlot temp = new FacesConfigNameSlot();
        temp.setName("D");
        cfgC.getOrdering().getBeforeList().add(temp);
        temp = new FacesConfigNameSlot();
        temp.setName("E");
        cfgC.getOrdering().getBeforeList().add(temp);        
        temp = new FacesConfigNameSlot();
        temp.setName("A");
        cfgC.getOrdering().getAfterList().add(temp);
        temp = new FacesConfigNameSlot();
        temp.setName("B");
        cfgC.getOrdering().getAfterList().add(temp);
        
        cfgA.setOrdering(new Ordering());
        temp = new FacesConfigNameSlot();
        temp.setName("B");
        cfgA.getOrdering().getBeforeList().add(temp);
        temp = new FacesConfigNameSlot();
        temp.setName("E");
        cfgA.getOrdering().getBeforeList().add(temp);
        
        cfgE.setOrdering(new Ordering());
        temp = new FacesConfigNameSlot();
        temp.setName("D");
        cfgE.getOrdering().getAfterList().add(temp);
        
        cfgD.setOrdering(new Ordering());
        cfgD.getOrdering().getBeforeList().add(new ConfigOthersSlot());
        
        List<FacesConfig> appConfigResources = new ArrayList<FacesConfig>();
        appConfigResources.add(cfgA);
        appConfigResources.add(cfgB);
        appConfigResources.add(cfgC);
        appConfigResources.add(cfgD);
        appConfigResources.add(cfgE);
        appConfigResources.add(cfg);
        
        //Brute force       
        for (int i = 0; i < 30; i++)
        {
            Collections.shuffle(appConfigResources);
            applyAlgorithm(appConfigResources);
        }
    }
    
    public void testMaxConditionsOrdering() throws Exception
    {
        FacesConfig cfg = _impl.getFacesConfig(getClass().getResourceAsStream(
        "empty-config.xml"), "empty-config.xml");        
        FacesConfig cfgA = new FacesConfig();
        FacesConfig cfgB = new FacesConfig();
        FacesConfig cfgC = new FacesConfig();

        cfgA.setName("A");
        cfgB.setName("B");
        cfgC.setName("C");
        
        cfgA.setOrdering(new Ordering());
        FacesConfigNameSlot temp = new FacesConfigNameSlot();
        temp.setName("B");
        cfgA.getOrdering().getBeforeList().add(temp);
        temp = new FacesConfigNameSlot();
        temp.setName("C");
        cfgA.getOrdering().getBeforeList().add(temp);
        
        cfgB.setOrdering(new Ordering());
        temp = new FacesConfigNameSlot();
        temp.setName("A");
        cfgB.getOrdering().getAfterList().add(temp);
        temp = new FacesConfigNameSlot();
        temp.setName("C");
        cfgB.getOrdering().getBeforeList().add(temp);
        
        cfgC.setOrdering(new Ordering());
        temp = new FacesConfigNameSlot();
        temp.setName("A");
        cfgC.getOrdering().getAfterList().add(temp);
        temp = new FacesConfigNameSlot();
        temp.setName("B");
        cfgC.getOrdering().getAfterList().add(temp);
        //cfgC.getOrdering().getBeforeList().add(new ConfigOthersSlot());
        
        List<FacesConfig> appConfigResources = new ArrayList<FacesConfig>();
        appConfigResources.add(cfgC);
        appConfigResources.add(cfgA);
        appConfigResources.add(cfgB);
        
        //Brute force       
        for (int i = 0; i < 30; i++)
        {
            Collections.shuffle(appConfigResources);
            applyAlgorithm(appConfigResources);
        }
    }
    
    public void testEx1()
    {      
        FacesConfig cfgA = new FacesConfig();
        FacesConfig cfgB = new FacesConfig();
        FacesConfig cfgC = new FacesConfig();
        FacesConfig cfgD = new FacesConfig();
        FacesConfig cfgE = new FacesConfig();
        FacesConfig cfgF = new FacesConfig();

        cfgA.setName("A");
        cfgB.setName("B");
        cfgC.setName("C");
        cfgD.setName("D");
        cfgE.setName("E");
        cfgF.setName("F");
        
        cfgA.setOrdering(new Ordering());
        cfgA.getOrdering().getAfterList().add(new ConfigOthersSlot());
        FacesConfigNameSlot temp = new FacesConfigNameSlot();
        temp.setName("C");
        cfgA.getOrdering().getAfterList().add(temp);
        
        cfgB.setOrdering(new Ordering());
        cfgB.getOrdering().getBeforeList().add(new ConfigOthersSlot());

        cfgC.setOrdering(new Ordering());
        cfgC.getOrdering().getAfterList().add(new ConfigOthersSlot());

        cfgF.setOrdering(new Ordering());
        cfgF.getOrdering().getBeforeList().add(new ConfigOthersSlot());
        temp = new FacesConfigNameSlot();
        temp.setName("B");
        cfgF.getOrdering().getBeforeList().add(temp);
        
        
        List<FacesConfig> appConfigResources = new ArrayList<FacesConfig>();
        appConfigResources.add(cfgA);
        appConfigResources.add(cfgB);
        appConfigResources.add(cfgC);
        appConfigResources.add(cfgD);
        appConfigResources.add(cfgE);
        appConfigResources.add(cfgF);
        
        //Brute force       
        for (int i = 0; i < 30; i++)
        {
            Collections.shuffle(appConfigResources);
            applyAlgorithm(appConfigResources);
        }
    }
    
    public void testEx2()
    {
        FacesConfig cfg = new FacesConfig();
        FacesConfig cfgB = new FacesConfig();
        FacesConfig cfgC = new FacesConfig();
        FacesConfig cfgD = new FacesConfig();
        FacesConfig cfgE = new FacesConfig();
        FacesConfig cfgF = new FacesConfig();

        cfgB.setName("B");
        cfgC.setName("C");
        cfgD.setName("D");
        cfgE.setName("E");
        cfgF.setName("F");
        
        cfg.setOrdering(new Ordering());
        cfg.getOrdering().getAfterList().add(new ConfigOthersSlot());
        FacesConfigNameSlot temp = new FacesConfigNameSlot();
        temp.setName("C");
        cfg.getOrdering().getBeforeList().add(temp);

        cfgB.setOrdering(new Ordering());
        cfgB.getOrdering().getBeforeList().add(new ConfigOthersSlot());
        
        cfgD.setOrdering(new Ordering());
        cfgD.getOrdering().getAfterList().add(new ConfigOthersSlot());

        cfgE.setOrdering(new Ordering());
        cfgE.getOrdering().getBeforeList().add(new ConfigOthersSlot());

        List<FacesConfig> appConfigResources = new ArrayList<FacesConfig>();
        appConfigResources.add(cfg);
        appConfigResources.add(cfgB);
        appConfigResources.add(cfgC);
        appConfigResources.add(cfgD);
        appConfigResources.add(cfgE);
        appConfigResources.add(cfgF);

        //Brute force       
        for (int i = 0; i < 30; i++)
        {
            Collections.shuffle(appConfigResources);
            applyAlgorithm(appConfigResources);
        }
    }
    
    public void testEx3()
    {
        FacesConfig cfgA = new FacesConfig();
        FacesConfig cfgB = new FacesConfig();
        FacesConfig cfgC = new FacesConfig();
        FacesConfig cfgD = new FacesConfig();

        cfgA.setName("A");
        cfgB.setName("B");
        cfgC.setName("C");
        cfgD.setName("D");

        cfgA.setOrdering(new Ordering());
        FacesConfigNameSlot temp = new FacesConfigNameSlot();
        temp.setName("B");
        cfgA.getOrdering().getAfterList().add(temp);

        cfgC.setOrdering(new Ordering());
        cfgC.getOrdering().getBeforeList().add(new ConfigOthersSlot());
        
        List<FacesConfig> appConfigResources = new ArrayList<FacesConfig>();
        appConfigResources.add(cfgA);
        appConfigResources.add(cfgB);
        appConfigResources.add(cfgC);
        appConfigResources.add(cfgD);

        //Brute force       
        for (int i = 0; i < 30; i++)
        {
            Collections.shuffle(appConfigResources);
            applyAlgorithm(appConfigResources);
        }
    }
        
    public void applyAlgorithm(List<FacesConfig> appConfigResources) throws FacesException
    {
        FacesConfigurator configurator = new FacesConfigurator(externalContext);
        
        /*
        System.out.println("");
        System.out.print("Start List: [");
        for (int i = 0; i < appConfigResources.size();i++)
        {
            if (appConfigResources.get(i).getName() == null)
            {
                System.out.print("No id,");
            }
            else
            {
                System.out.print(appConfigResources.get(i).getName()+",");
            }
        }
        System.out.println("]");
        */
        
        List<FacesConfig> postOrderedList = configurator.getPostOrderedList(appConfigResources);
        
        /*
        System.out.print("Pre-Ordered-List: [");
        for (int i = 0; i < postOrderedList.size();i++)
        {
            if (postOrderedList.get(i).getName() == null)
            {
                System.out.print("No id,");
            }
            else
            {
                System.out.print(postOrderedList.get(i).getName()+",");
            }
        }
        System.out.println("]");
        */
        
        List<FacesConfig> sortedList = configurator.sortRelativeOrderingList(postOrderedList);
        
        /*
        System.out.print("Sorted-List: [");
        for (int i = 0; i < sortedList.size();i++)
        {
            if (sortedList.get(i).getName() == null)
            {
                System.out.print("No id,");
            }
            else
            {
                System.out.print(sortedList.get(i).getName()+",");
            }
        }
        System.out.println("]");
        */
    }
    
    /**
     * Sort a list of pre ordered elements. It scans one by one the elements
     * and apply the conditions mentioned by Ordering object if it is available.
     * 
     * The preOrderedList ensures that application config resources referenced by
     * other resources are processed first, making more easier the sort procedure. 
     * 
     * @param preOrderedList
     * @return
     */
    /*
    private List<FacesConfig> sortRelativeOrderingList(List<FacesConfig> preOrderedList) throws FacesException
    {
        List<FacesConfig> sortedList = new ArrayList<FacesConfig>();
        
        for (int i=0; i < preOrderedList.size(); i++)
        {
            FacesConfig resource = preOrderedList.get(i);
            if (resource.getOrdering() != null)
            {
                if (resource.getOrdering().getBeforeList().isEmpty() &&
                    resource.getOrdering().getAfterList().isEmpty())
                {
                    //No order rules, just put it as is
                    sortedList.add(resource);
                }
                else if (resource.getOrdering().getBeforeList().isEmpty())
                {
                    //Only after rules
                    applyAfterRule(sortedList, resource);
                }
                else if (resource.getOrdering().getAfterList().isEmpty())
                {
                    //Only before rules
                    
                    //Resolve if there is a later reference to this node before
                    //apply it
                    boolean referenceNode = false;

                    for (int j = i+1; j < preOrderedList.size(); j++)
                    {
                        FacesConfig pointingResource = preOrderedList.get(j);
                        for (OrderSlot slot : pointingResource.getOrdering().getBeforeList())
                        {
                            if (slot instanceof FacesConfigNameSlot &&
                                    resource.getName().equals(((FacesConfigNameSlot)slot).getName()) )
                            {
                                referenceNode = true;
                            }
                            if (slot instanceof ConfigOthersSlot)
                            {
                                //No matter if there is a reference, because this rule
                                //is not strict and before other ordering is unpredictable.
                                //
                                referenceNode = false;
                                break;
                            }
                        }
                        if (referenceNode)
                        {
                            break;
                        }
                        for (OrderSlot slot : pointingResource.getOrdering().getAfterList())
                        {
                            if (slot instanceof FacesConfigNameSlot &&
                                resource.getName().equals(((FacesConfigNameSlot)slot).getName()) )
                            {
                                referenceNode = true;
                                break;
                            }
                        }
                    }
                    
                    applyBeforeRule(sortedList, resource, referenceNode);
                }
                else
                {
                    //Both before and after rules
                    //In this case we should compare before and after rules
                    //and the one with names takes precedence over the other one.
                    //It both have names references, before rules takes
                    //precedence over after
                    //after some action is applied a check of the condition is made.
                    int beforeWeight = 0;
                    int afterWeight = 0;
                    for (OrderSlot slot : resource.getOrdering()
                            .getBeforeList())
                    {
                        if (slot instanceof FacesConfigNameSlot)
                        {
                            beforeWeight++;
                        }
                    }
                    for (OrderSlot slot : resource.getOrdering()
                            .getAfterList())
                    {
                        if (slot instanceof FacesConfigNameSlot)
                        {
                            afterWeight++;
                        }
                    }
                    
                    if (beforeWeight >= afterWeight)
                    {
                        applyBeforeRule(sortedList, resource,false);
                    }
                    else
                    {
                        applyAfterRule(sortedList, resource);
                    }
                    
                    
                }
            }
            else
            {
                //No order rules, just put it as is
                sortedList.add(resource);
            }
        }
        
        //Check
        for (int i = 0; i < sortedList.size(); i++)
        {
            FacesConfig resource = sortedList.get(i);
            
            if (resource.getOrdering() != null)
            {
                for (OrderSlot slot : resource.getOrdering().getBeforeList())
                {
                    if (slot instanceof FacesConfigNameSlot)
                    {
                        String name = ((FacesConfigNameSlot) slot).getName();
                        if (name != null && !"".equals(name))
                        {
                            boolean founded = false;                                
                            for (int j = i-1; j >= 0; j--)
                            {
                                if (name.equals(sortedList.get(j).getName()))
                                {
                                    founded=true;
                                    break;
                                }
                            }
                            if (founded)
                            {
                                //LOG WARN MESSAGE ABOUT IT
                                throw new FacesException();
                            }
                        }
                    }
                }
                for (OrderSlot slot : resource.getOrdering().getAfterList())
                {
                    if (slot instanceof FacesConfigNameSlot)
                    {
                        String name = ((FacesConfigNameSlot) slot).getName();
                        if (name != null && !"".equals(name))
                        {
                            boolean founded = false;                                
                            for (int j = i+1; j < sortedList.size(); j++)
                            {
                                if (name.equals(sortedList.get(j).getName()))
                                {
                                    founded=true;
                                    break;
                                }
                            }
                            if (founded)
                            {
                                //LOG WARN MESSAGE ABOUT IT
                                throw new FacesException();
                            }
                        }
                    }
                }
            }
        }
        
        return sortedList;
    }
    
    private void applyBeforeRule(List<FacesConfig> sortedList, FacesConfig resource, boolean referenced) throws FacesException
    {
        //Only before rules
        boolean configOthers = false;
        List<String> names = new ArrayList<String>();
        
        for (OrderSlot slot : resource.getOrdering().getBeforeList())
        {
            if (slot instanceof ConfigOthersSlot)
            {
                configOthers = true;
                break;
            }
            else
            {
                FacesConfigNameSlot nameSlot = (FacesConfigNameSlot) slot;
                names.add(nameSlot.getName());
            }
        }
        
        if (configOthers)
        {
            //<before>....<others/></before> case
            //other reference where already considered when
            //pre ordered list was calculated, so just add to the end.
            
            //There is one very special case, and it is when there
            //is another resource with a reference on it. In this case,
            //it is better do not apply this rule and add it to the end
            //to give the chance to the other one to be applied.
            if (resource.getOrdering().getBeforeList().size() > 1)
            {
                //If there is a reference apply it
                sortedList.add(0,resource);
            }
            else if (!referenced)
            {
                //If it is not referenced apply it
                sortedList.add(0,resource);
            }
            else
            {
                //if it is referenced bypass the rule and add it to the end
                sortedList.add(resource);
            }
        }
        else
        {
            //Scan the nearest reference and add it after
            boolean founded = false;
            for (int i = 0; i < sortedList.size() ; i++)
            {
                if (names.contains(sortedList.get(i).getName()))
                {
                    sortedList.add(i,resource);
                    founded = true;
                    break;
                }
            }
            if (!founded)
            {
                //just add it to the end
                sortedList.add(resource);
            }
        }        
    }
    
    private void applyAfterRule(List<FacesConfig> sortedList, FacesConfig resource) throws FacesException
    {
        boolean configOthers = false;
        List<String> names = new ArrayList<String>();
        
        for (OrderSlot slot : resource.getOrdering().getAfterList())
        {
            if (slot instanceof ConfigOthersSlot)
            {
                configOthers = true;
                break;
            }
            else
            {
                FacesConfigNameSlot nameSlot = (FacesConfigNameSlot) slot;
                names.add(nameSlot.getName());
            }
        }
        
        if (configOthers)
        {
            //<after>....<others/></after> case
            //other reference where already considered when
            //pre ordered list was calculated, so just add to the end.
            sortedList.add(resource);
        }
        else
        {
            //Scan the nearest reference and add it after
            boolean founded = false;
            for (int i = sortedList.size()-1 ; i >=0 ; i--)
            {
                if (names.contains(sortedList.get(i).getName()))
                {
                    if (i+1 < sortedList.size())
                    {
                        sortedList.add(i+1,resource);
                    }
                    else
                    {
                        sortedList.add(resource);
                    }
                    founded = true;
                    break;
                }
            }
            if (!founded)
            {
                //just add it to the end
                sortedList.add(resource);
            }
        }        
    }*/
    
    
    /**
     * Pre Sort the appConfigResources, detecting cyclic references, so when sort process
     * start, it is just necessary to traverse the preOrderedList once. To do that, we just
     * scan "before" and "after" lists for references, and then those references are traversed
     * again, so the first elements of the pre ordered list does not have references and
     * the next elements has references to the already added ones.
     * 
     * The elements on the preOrderedList looks like this:
     * 
     * [ no ordering elements , referenced elements ... more referenced elements, 
     *  before others / after others non referenced elements]
     * 
     * @param appConfigResources
     * @return
     */
    /*
    private List<FacesConfig> getPostOrderedList(final List<FacesConfig> appConfigResources) throws FacesException
    {
        
        List<FacesConfig> appFilteredConfigResources = new ArrayList<FacesConfig>(); 
        
        //0. Clean up: remove all not found resource references from the ordering 
        //descriptions.
        List<String> availableReferences = new ArrayList<String>();
        for (FacesConfig resource : appFilteredConfigResources)
        {
            String name = resource.getName();
            if (name != null && "".equals(name))
            {
                availableReferences.add(name);
            }
        }
        
        for (FacesConfig resource : appFilteredConfigResources)
        {
            for (Iterator<OrderSlot> it =  resource.getOrdering().getBeforeList().iterator();it.hasNext();)
            {
                OrderSlot slot = it.next();
                if (slot instanceof FacesConfigNameSlot)
                {
                    String name = ((FacesConfigNameSlot) slot).getName();
                    if (!availableReferences.contains(name))
                    {
                        it.remove();
                    }
                }
            }
            for (Iterator<OrderSlot> it =  resource.getOrdering().getAfterList().iterator();it.hasNext();)
            {
                OrderSlot slot = it.next();
                if (slot instanceof FacesConfigNameSlot)
                {
                    String name = ((FacesConfigNameSlot) slot).getName();
                    if (!availableReferences.contains(name))
                    {
                        it.remove();
                    }
                }
            }
        }
        
        //1. Pre filtering: Sort nodes according to its weight. The weight is the number of named
        //nodes containing in both before and after lists. The sort is done from the more complex
        //to the most simple
        if (appConfigResources instanceof ArrayList)
        {
            appFilteredConfigResources = (List<FacesConfig>)
                ((ArrayList<FacesConfig>)appConfigResources).clone();
        }
        else
        {
            appFilteredConfigResources = new ArrayList<FacesConfig>();
            appFilteredConfigResources.addAll(appConfigResources);
        }
        Collections.sort(appFilteredConfigResources,
                new Comparator<FacesConfig>()
                {
                    public int compare(FacesConfig o1, FacesConfig o2)
                    {
                        int o1Weight = 0;
                        int o2Weight = 0;
                        if (o1.getOrdering() != null)
                        {
                            for (OrderSlot slot : o1.getOrdering()
                                    .getBeforeList())
                            {
                                if (slot instanceof FacesConfigNameSlot)
                                {
                                    o1Weight++;
                                }
                            }
                            for (OrderSlot slot : o1.getOrdering()
                                    .getAfterList())
                            {
                                if (slot instanceof FacesConfigNameSlot)
                                {
                                    o1Weight++;
                                }
                            }
                        }
                        if (o2.getOrdering() != null)
                        {
                            for (OrderSlot slot : o2.getOrdering()
                                    .getBeforeList())
                            {
                                if (slot instanceof FacesConfigNameSlot)
                                {
                                    o2Weight++;
                                }
                            }
                            for (OrderSlot slot : o2.getOrdering()
                                    .getAfterList())
                            {
                                if (slot instanceof FacesConfigNameSlot)
                                {
                                    o2Weight++;
                                }
                            }
                        }
                        return o2Weight - o1Weight;
                    }
                });
        
        List<FacesConfig> postOrderedList = new LinkedList<FacesConfig>();
        List<FacesConfig> othersList = new ArrayList<FacesConfig>();
        
        List<String> nameBeforeStack = new ArrayList<String>();
        List<String> nameAfterStack = new ArrayList<String>();
        
        boolean[] visitedSlots = new boolean[appFilteredConfigResources.size()];
        
        //2. Scan and resolve conflicts
        for (int i = 0; i < appFilteredConfigResources.size(); i++)
        {
            if (!visitedSlots[i])
            {
                resolveConflicts(appFilteredConfigResources, i, visitedSlots, 
                        nameBeforeStack, nameAfterStack, postOrderedList, othersList, false);
            }
        }
        
        //Add othersList to postOrderedList so <before><others/></before> and <after><others/></after>
        //ordering conditions are handled at last if there are not referenced by anyone
        postOrderedList.addAll(othersList);
        
        return postOrderedList;
    }
        
    private void resolveConflicts(final List<FacesConfig> appConfigResources, int index, boolean[] visitedSlots,
            List<String> nameBeforeStack, List<String> nameAfterStack, List<FacesConfig> postOrderedList,
            List<FacesConfig> othersList, boolean indexReferenced) throws FacesException
    {
        FacesConfig facesConfig = appConfigResources.get(index);
        
        if (nameBeforeStack.contains(facesConfig.getName()))
        {
            //Already referenced, just return. Later if there exists a
            //circular reference, it will be detected and solved.
            return;
            //log.fatal("Circular references detected when ordering " +
            //        "application faces config resources:"+nameBeforeStack.toString() +
            //        " already visited and trying to resolve "+facesConfig.getName());
            //throw new FacesException("Circular references detected when ordering " +
            //        "application faces config resources:"+nameBeforeStack.toString() +
            //        " already visited and trying to resolve "+facesConfig.getName());
        }
        
        if (nameAfterStack.contains(facesConfig.getName()))
        {
            //Already referenced, just return. Later if there exists a
            //circular reference, it will be detected and solved.
            return;
            //log.fatal("Circular references detected when ordering " +
            //        "application faces config resources:"+nameAfterStack.toString() +
            //        " already visited and trying to resolve "+facesConfig.getName());
            //throw new FacesException("Circular references detected when ordering " +
            //        "application faces config resources:"+nameAfterStack.toString() +
            //        " already visited and trying to resolve "+facesConfig.getName());
        }
        
        if (facesConfig.getOrdering() != null)
        {
            boolean pointingResource = false;
            
            //Deal with before restrictions first
            for (OrderSlot slot : facesConfig.getOrdering().getBeforeList())
            {
                if (slot instanceof FacesConfigNameSlot)
                {
                    FacesConfigNameSlot nameSlot = (FacesConfigNameSlot) slot;
                    //The resource pointed is not added yet?
                    boolean alreadyAdded = false;
                    for (FacesConfig res : postOrderedList)
                    {
                        if (nameSlot.getName().equals(res.getName()))
                        {
                            alreadyAdded = true;
                            break;
                        }
                    }
                    if (!alreadyAdded)
                    {
                        int indexSlot = -1;
                        //Find it
                        for (int i = 0; i < appConfigResources.size(); i++)
                        {
                            FacesConfig resource = appConfigResources.get(i);
                            if (resource.getName() != null && nameSlot.getName().equals(resource.getName()))
                            {
                                indexSlot = i;
                                break;
                            }
                        }
                        
                        //Resource founded on appConfigResources
                        if (indexSlot != -1)
                        {
                            pointingResource = true;
                            //Add to nameStac
                            nameBeforeStack.add(facesConfig.getName());
                            
                            resolveConflicts(appConfigResources, indexSlot, visitedSlots, 
                                    nameBeforeStack, nameAfterStack, postOrderedList,
                                    othersList,true);
                            
                            nameBeforeStack.remove(facesConfig.getName());
                        }
                    }
                    else
                    {
                        pointingResource = true;
                    }
                }
            }
            
            for (OrderSlot slot : facesConfig.getOrdering().getAfterList())
            {
                if (slot instanceof FacesConfigNameSlot)
                {
                    FacesConfigNameSlot nameSlot = (FacesConfigNameSlot) slot;
                    //The resource pointed is not added yet?
                    boolean alreadyAdded = false;
                    for (FacesConfig res : postOrderedList)
                    {
                        if (nameSlot.getName().equals(res.getName()))
                        {
                            alreadyAdded = true;
                            break;
                        }
                    }
                    if (!alreadyAdded)
                    {
                        int indexSlot = -1;
                        //Find it
                        for (int i = 0; i < appConfigResources.size(); i++)
                        {
                            FacesConfig resource = appConfigResources.get(i);
                            if (resource.getName() != null && nameSlot.getName().equals(resource.getName()))
                            {
                                indexSlot = i;
                                break;
                            }
                        }
                        
                        //Resource founded on appConfigResources
                        if (indexSlot != -1)
                        {
                            pointingResource = true;
                            //Add to nameStac
                            nameAfterStack.add(facesConfig.getName());
                            
                            resolveConflicts(appConfigResources, indexSlot, visitedSlots, 
                                    nameBeforeStack, nameAfterStack, postOrderedList,
                                    othersList,true);
                            
                            nameAfterStack.remove(facesConfig.getName());
                        }
                    }
                    else
                    {
                        pointingResource = true;
                    }
                }
            }
            
            if (facesConfig.getOrdering().getBeforeList().isEmpty() &&
                facesConfig.getOrdering().getAfterList().isEmpty())
            {
                //Fits in the category "others", put at beginning
                postOrderedList.add(0,appConfigResources.get(index));
            }
            else if (pointingResource || indexReferenced)
            {
                //If the node points to other or is referenced from other,
                //add to the postOrderedList at the end
                postOrderedList.add(appConfigResources.get(index));                    
            }
            else
            {
                //Add to othersList
                othersList.add(appConfigResources.get(index));
            }
        }
        else
        {
            //Add at start of the list, since does not have any ordering
            //instructions and on the next step makes than "before others" and "after others"
            //works correctly
            postOrderedList.add(0,appConfigResources.get(index));
        }
        //Set the node as visited
        visitedSlots[index] = true;
    }*/
    
}
