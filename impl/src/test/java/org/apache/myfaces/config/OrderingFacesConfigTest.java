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

import org.apache.myfaces.config.element.FacesConfig;
import org.apache.myfaces.config.element.OrderSlot;
import org.apache.myfaces.config.impl.element.AbsoluteOrderingImpl;
import org.apache.myfaces.config.impl.element.ConfigOthersSlotImpl;
import org.apache.myfaces.config.impl.element.FacesConfigNameSlotImpl;
import org.apache.myfaces.config.impl.element.OrderingImpl;
import org.apache.myfaces.test.base.junit.AbstractJsfTestCase;

import javax.faces.FacesException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.myfaces.config.impl.FacesConfigUnmarshallerImpl;
import org.junit.Assert;

public class OrderingFacesConfigTest extends AbstractJsfTestCase
{
    private static final Logger log = Logger.getLogger(OrderingFacesConfigTest.class.getName());
    
    private FacesConfigUnmarshallerImpl _impl;

    
    public void setUp() throws Exception
    {
        super.setUp();
        _impl = new FacesConfigUnmarshallerImpl(null);
    }
    
    public void printFacesConfigList(String label, List<FacesConfig> appConfigResources)
    {
        System.out.println("");
        System.out.print(""+label+": [");
        for (int i = 0; i < appConfigResources.size();i++)
        {
            if (appConfigResources.get(i).getName() == null)
            {
                System.out.print("No_id,");
            }
            else
            {
                System.out.print(appConfigResources.get(i).getName()+",");
            }
        }
        System.out.println("]");

    }
    
    /**
     * A before others
     * B after C
     * C after others
     * 
     * preferred result:
     * 
     * A C B No_id No_id 
     * 
     * @throws Exception
     */
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
    
    /**
     * A before B
     * A before E
     * C after A
     * C after B
     * C before D
     * C before E
     * E after D
     * D before others
     * 
     * preferred results:
     * 
     * A B C D E No_id
     * 
     * @throws Exception
     */
    public void testMiddleOrdering() throws Exception
    {
        FacesConfig cfg = _impl.getFacesConfig(getClass().getResourceAsStream(
        "empty-config.xml"), "empty-config.xml");        
        org.apache.myfaces.config.impl.element.FacesConfigImpl cfgA = new org.apache.myfaces.config.impl.element.FacesConfigImpl();
        org.apache.myfaces.config.impl.element.FacesConfigImpl cfgB = new org.apache.myfaces.config.impl.element.FacesConfigImpl();
        org.apache.myfaces.config.impl.element.FacesConfigImpl cfgC = new org.apache.myfaces.config.impl.element.FacesConfigImpl();
        org.apache.myfaces.config.impl.element.FacesConfigImpl cfgD = new org.apache.myfaces.config.impl.element.FacesConfigImpl();
        org.apache.myfaces.config.impl.element.FacesConfigImpl cfgE = new org.apache.myfaces.config.impl.element.FacesConfigImpl();
        
        cfgA.setName("A");
        cfgB.setName("B");
        cfgC.setName("C");
        cfgD.setName("D");
        cfgE.setName("E");
        
        cfgC.setOrdering(new OrderingImpl());
        FacesConfigNameSlotImpl temp = new FacesConfigNameSlotImpl();
        temp.setName("D");
        ((OrderingImpl) cfgC.getOrdering()).addBeforeSlot(temp);
        temp = new FacesConfigNameSlotImpl();
        temp.setName("E");       
        ((OrderingImpl) cfgC.getOrdering()).addBeforeSlot(temp);
        temp = new FacesConfigNameSlotImpl();
        temp.setName("A");
        ((OrderingImpl) cfgC.getOrdering()).addAfterSlot(temp);
        temp = new FacesConfigNameSlotImpl();
        temp.setName("B");
        ((OrderingImpl) cfgC.getOrdering()).addAfterSlot(temp);
        
        cfgA.setOrdering(new OrderingImpl());
        temp = new FacesConfigNameSlotImpl();
        temp.setName("B");
        ((OrderingImpl) cfgA.getOrdering()).addBeforeSlot(temp);
        temp = new FacesConfigNameSlotImpl();
        temp.setName("E");
        ((OrderingImpl) cfgA.getOrdering()).addBeforeSlot(temp);
        
        cfgE.setOrdering(new OrderingImpl());
        temp = new FacesConfigNameSlotImpl();
        temp.setName("D");
        ((OrderingImpl) cfgE.getOrdering()).addAfterSlot(temp);
        
        cfgD.setOrdering(new OrderingImpl());
        ((OrderingImpl) cfgD.getOrdering()).addBeforeSlot(new ConfigOthersSlotImpl());
        
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
    
    /**
     * A before B
     * A before C
     * B after A
     * B before C
     * C after A
     * C after B
     * 
     * preferred result
     * 
     * A B C
     * 
     * @throws Exception
     */
    public void testMaxConditionsOrdering() throws Exception
    {
        org.apache.myfaces.config.impl.element.FacesConfigImpl cfg = _impl.getFacesConfig(getClass().getResourceAsStream(
        "empty-config.xml"), "empty-config.xml");        
        org.apache.myfaces.config.impl.element.FacesConfigImpl cfgA = new org.apache.myfaces.config.impl.element.FacesConfigImpl();
        org.apache.myfaces.config.impl.element.FacesConfigImpl cfgB = new org.apache.myfaces.config.impl.element.FacesConfigImpl();
        org.apache.myfaces.config.impl.element.FacesConfigImpl cfgC = new org.apache.myfaces.config.impl.element.FacesConfigImpl();

        cfgA.setName("A");
        cfgB.setName("B");
        cfgC.setName("C");
        
        cfgA.setOrdering(new OrderingImpl());
        FacesConfigNameSlotImpl temp = new FacesConfigNameSlotImpl();
        temp.setName("B");
        ((OrderingImpl) cfgA.getOrdering()).addBeforeSlot(temp);
        temp = new FacesConfigNameSlotImpl();
        temp.setName("C");
        ((OrderingImpl) cfgA.getOrdering()).addBeforeSlot(temp);
        
        cfgB.setOrdering(new OrderingImpl());
        temp = new FacesConfigNameSlotImpl();
        temp.setName("A");
        ((OrderingImpl) cfgB.getOrdering()).addAfterSlot(temp);
        temp = new FacesConfigNameSlotImpl();
        temp.setName("C");
        ((OrderingImpl) cfgB.getOrdering()).addBeforeSlot(temp);
        
        cfgC.setOrdering(new OrderingImpl());
        temp = new FacesConfigNameSlotImpl();
        temp.setName("A");
        ((OrderingImpl) cfgC.getOrdering()).addAfterSlot(temp);
        temp = new FacesConfigNameSlotImpl();
        temp.setName("B");
        ((OrderingImpl) cfgC.getOrdering()).addAfterSlot(temp);
        
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
        org.apache.myfaces.config.impl.element.FacesConfigImpl cfgA = new org.apache.myfaces.config.impl.element.FacesConfigImpl();
        org.apache.myfaces.config.impl.element.FacesConfigImpl cfgB = new org.apache.myfaces.config.impl.element.FacesConfigImpl();
        org.apache.myfaces.config.impl.element.FacesConfigImpl cfgC = new org.apache.myfaces.config.impl.element.FacesConfigImpl();
        org.apache.myfaces.config.impl.element.FacesConfigImpl cfgD = new org.apache.myfaces.config.impl.element.FacesConfigImpl();
        org.apache.myfaces.config.impl.element.FacesConfigImpl cfgE = new org.apache.myfaces.config.impl.element.FacesConfigImpl();
        org.apache.myfaces.config.impl.element.FacesConfigImpl cfgF = new org.apache.myfaces.config.impl.element.FacesConfigImpl();

        cfgA.setName("A");
        cfgB.setName("B");
        cfgC.setName("C");
        cfgD.setName("D");
        cfgE.setName("E");
        cfgF.setName("F");
        
        cfgA.setOrdering(new OrderingImpl());
        ((OrderingImpl) cfgA.getOrdering()).addAfterSlot(new ConfigOthersSlotImpl());
        FacesConfigNameSlotImpl temp = new FacesConfigNameSlotImpl();
        temp.setName("C");
        ((OrderingImpl) cfgA.getOrdering()).addAfterSlot(temp);
        
        cfgB.setOrdering(new OrderingImpl());
        ((OrderingImpl) cfgB.getOrdering()).addBeforeSlot(new ConfigOthersSlotImpl());

        cfgC.setOrdering(new OrderingImpl());
        ((OrderingImpl) cfgC.getOrdering()).addAfterSlot(new ConfigOthersSlotImpl());

        cfgF.setOrdering(new OrderingImpl());
        ((OrderingImpl) cfgF.getOrdering()).addBeforeSlot(new ConfigOthersSlotImpl());
        temp = new FacesConfigNameSlotImpl();
        temp.setName("B");
        ((OrderingImpl) cfgF.getOrdering()).addBeforeSlot(temp);
        
        
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
        org.apache.myfaces.config.impl.element.FacesConfigImpl cfg = new org.apache.myfaces.config.impl.element.FacesConfigImpl();
        org.apache.myfaces.config.impl.element.FacesConfigImpl cfgB = new org.apache.myfaces.config.impl.element.FacesConfigImpl();
        org.apache.myfaces.config.impl.element.FacesConfigImpl cfgC = new org.apache.myfaces.config.impl.element.FacesConfigImpl();
        org.apache.myfaces.config.impl.element.FacesConfigImpl cfgD = new org.apache.myfaces.config.impl.element.FacesConfigImpl();
        org.apache.myfaces.config.impl.element.FacesConfigImpl cfgE = new org.apache.myfaces.config.impl.element.FacesConfigImpl();
        org.apache.myfaces.config.impl.element.FacesConfigImpl cfgF = new org.apache.myfaces.config.impl.element.FacesConfigImpl();

        cfgB.setName("B");
        cfgC.setName("C");
        cfgD.setName("D");
        cfgE.setName("E");
        cfgF.setName("F");
        
        cfg.setOrdering(new OrderingImpl());
        ((OrderingImpl) cfg.getOrdering()).addAfterSlot(new ConfigOthersSlotImpl());
        FacesConfigNameSlotImpl temp = new FacesConfigNameSlotImpl();
        temp.setName("C");
        ((OrderingImpl) cfg.getOrdering()).addBeforeSlot(temp);

        cfgB.setOrdering(new OrderingImpl());
        ((OrderingImpl) cfgB.getOrdering()).addBeforeSlot(new ConfigOthersSlotImpl());
        
        cfgD.setOrdering(new OrderingImpl());
        ((OrderingImpl) cfgD.getOrdering()).addAfterSlot(new ConfigOthersSlotImpl());

        cfgE.setOrdering(new OrderingImpl());
        ((OrderingImpl) cfgE.getOrdering()).addBeforeSlot(new ConfigOthersSlotImpl());

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
        org.apache.myfaces.config.impl.element.FacesConfigImpl cfgA = new org.apache.myfaces.config.impl.element.FacesConfigImpl();
        org.apache.myfaces.config.impl.element.FacesConfigImpl cfgB = new org.apache.myfaces.config.impl.element.FacesConfigImpl();
        org.apache.myfaces.config.impl.element.FacesConfigImpl cfgC = new org.apache.myfaces.config.impl.element.FacesConfigImpl();
        org.apache.myfaces.config.impl.element.FacesConfigImpl cfgD = new org.apache.myfaces.config.impl.element.FacesConfigImpl();

        cfgA.setName("A");
        cfgB.setName("B");
        cfgC.setName("C");
        cfgD.setName("D");

        cfgA.setOrdering(new OrderingImpl());
        FacesConfigNameSlotImpl temp = new FacesConfigNameSlotImpl();
        temp.setName("B");
        ((OrderingImpl) cfgA.getOrdering()).addAfterSlot(temp);
        

        cfgC.setOrdering(new OrderingImpl());
        ((OrderingImpl) cfgC.getOrdering()).addAfterSlot(new ConfigOthersSlotImpl());
        
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
    
    public void testEx4() throws Exception
    {
        FacesConfig cfgA = _impl.getFacesConfig(getClass().getResourceAsStream(
            "transitive-a-config.xml"), "transitive-a-config.xml");
        FacesConfig cfgB = _impl.getFacesConfig(getClass().getResourceAsStream(
            "transitive-b-config.xml"), "transitive-b-config.xml");
        FacesConfig cfgC = _impl.getFacesConfig(getClass().getResourceAsStream(
            "transitive-c-config.xml"), "transitive-c-config.xml");
        
        List<FacesConfig> appConfigResources = new ArrayList<FacesConfig>();
        appConfigResources.add(cfgA);
        appConfigResources.add(cfgB);
        appConfigResources.add(cfgC);
        
        //Brute force       
        for (int i = 0; i < 30; i++)
        {
            Collections.shuffle(appConfigResources);
            applyAlgorithm(appConfigResources);
        }
    }

    public void applyAlgorithm(List<FacesConfig> appConfigResources) throws FacesException
    {
        DefaultFacesConfigurationMerger merger = new DefaultFacesConfigurationMerger();

        List<FacesConfig> postOrderedList = merger.getPostOrderedList(appConfigResources);
        
        List<FacesConfig> sortedList = merger.sortRelativeOrderingList(postOrderedList);
        
        if (sortedList == null)
        {
            //The previous algorithm can't sort correctly, try this one
            sortedList = merger.applySortingAlgorithm(appConfigResources);
        }
        
        //printFacesConfigList("Sorted List", sortedList);
    }

    public void testBeforeOthers1() throws Exception
    {
        FacesConfig cfgA = _impl.getFacesConfig(getClass().getResourceAsStream(
            "no-name-config.xml"), "no-name-config.xml");
        FacesConfig cfgB = _impl.getFacesConfig(getClass().getResourceAsStream(
            "no-name-config.xml"), "no-name-config.xml");
        FacesConfig cfgC = _impl.getFacesConfig(getClass().getResourceAsStream(
            "before-others-config.xml"), "before-others-config.xml");
        FacesConfig cfgD = _impl.getFacesConfig(getClass().getResourceAsStream(
            "no-name-config.xml"), "no-name-config.xml");        
        FacesConfig cfgE = _impl.getFacesConfig(getClass().getResourceAsStream(
            "no-name-config.xml"), "no-name-config.xml");        
        
        List<FacesConfig> appConfigResources = new ArrayList<FacesConfig>();
        appConfigResources.add(cfgA);
        appConfigResources.add(cfgB);
        appConfigResources.add(cfgC);
        appConfigResources.add(cfgD);
        appConfigResources.add(cfgE);
        
        //Brute force       
        for (int i = 0; i < 30; i++)
        {
            Collections.shuffle(appConfigResources);
            List<FacesConfig> sortedList = applyFullAlgorithm(appConfigResources);
            
            Assert.assertEquals(cfgC, sortedList.get(0));
        }
    }

    public void testAfterOthers1() throws Exception
    {
        FacesConfig cfgA = _impl.getFacesConfig(getClass().getResourceAsStream(
            "no-name-config.xml"), "no-name-config.xml");
        FacesConfig cfgB = _impl.getFacesConfig(getClass().getResourceAsStream(
            "no-name-config.xml"), "no-name-config.xml");
        FacesConfig cfgC = _impl.getFacesConfig(getClass().getResourceAsStream(
            "after-others-config.xml"), "after-others-config.xml");
        FacesConfig cfgD = _impl.getFacesConfig(getClass().getResourceAsStream(
            "no-name-config.xml"), "no-name-config.xml");        
        FacesConfig cfgE = _impl.getFacesConfig(getClass().getResourceAsStream(
            "no-name-config.xml"), "no-name-config.xml");        
        
        List<FacesConfig> appConfigResources = new ArrayList<FacesConfig>();
        appConfigResources.add(cfgA);
        appConfigResources.add(cfgB);
        appConfigResources.add(cfgC);
        appConfigResources.add(cfgD);
        appConfigResources.add(cfgE);
        
        //Brute force       
        for (int i = 0; i < 30; i++)
        {
            Collections.shuffle(appConfigResources);
            List<FacesConfig> sortedList = applyFullAlgorithm(appConfigResources);
            
            Assert.assertEquals(cfgC, sortedList.get(sortedList.size()-1));
        }
    }
    
    public void testBeforeOthers2() throws Exception
    {
        FacesConfig cfg1 = _impl.getFacesConfig(getClass().getResourceAsStream(
            "no-name-config.xml"), "no-name-config.xml");
        FacesConfig cfg2 = _impl.getFacesConfig(getClass().getResourceAsStream(
            "no-name-config.xml"), "no-name-config.xml");
        FacesConfig cfg3 = _impl.getFacesConfig(getClass().getResourceAsStream(
            "before-others-config.xml"), "before-others-config.xml");
        FacesConfig cfg4 = _impl.getFacesConfig(getClass().getResourceAsStream(
            "no-name-config.xml"), "no-name-config.xml");        
        FacesConfig cfg5 = _impl.getFacesConfig(getClass().getResourceAsStream(
            "no-name-config.xml"), "no-name-config.xml");     
        FacesConfig cfgA = _impl.getFacesConfig(getClass().getResourceAsStream(
            "transitive-a-config.xml"), "transitive-a-config.xml");
        FacesConfig cfgB = _impl.getFacesConfig(getClass().getResourceAsStream(
            "transitive-b-config.xml"), "transitive-b-config.xml");
        FacesConfig cfgC = _impl.getFacesConfig(getClass().getResourceAsStream(
            "transitive-c-config.xml"), "transitive-c-config.xml");
        FacesConfig cfg6 = _impl.getFacesConfig(getClass().getResourceAsStream(
            "after-others-config.xml"), "after-others-config.xml");
        
        
        List<FacesConfig> appConfigResources = new ArrayList<FacesConfig>();
        appConfigResources.add(cfgA);
        appConfigResources.add(cfgB);
        appConfigResources.add(cfgC);
        appConfigResources.add(cfg1);
        appConfigResources.add(cfg2);
        appConfigResources.add(cfg3);
        appConfigResources.add(cfg4);
        appConfigResources.add(cfg5);
        appConfigResources.add(cfg6);
        
        //Brute force       
        for (int i = 0; i < 30; i++)
        {
            Collections.shuffle(appConfigResources);
            List<FacesConfig> sortedList = applyFullAlgorithm(appConfigResources);
            
            Assert.assertEquals(cfg3, sortedList.get(0));
            
            Assert.assertEquals(cfg6, sortedList.get(sortedList.size()-1));
        }
    }
    
    public List<FacesConfig> applyFullAlgorithm(List<FacesConfig> appConfigResources) throws FacesException
    {    
        DefaultFacesConfigurationMerger merger = new DefaultFacesConfigurationMerger();
        
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
        
        List<FacesConfig> postOrderedList = merger.getPostOrderedList(appConfigResources);
        
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
        
        List<FacesConfig> sortedList = merger.sortRelativeOrderingList(postOrderedList);

        if (sortedList == null)
        {
            System.out.print("After Fix ");
            //The previous algorithm can't sort correctly, try this one
            sortedList = merger.applySortingAlgorithm(appConfigResources);
        }
        
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
        
        return sortedList;
    }
    /*
    public void applyAlgorithm(List<FacesConfig> appConfigResources) throws FacesException
    {
        printFacesConfigList("Start List", appConfigResources);
        
        //0. Convert the references into a graph
        List<Vertex> vertexList = new ArrayList<Vertex>();
        for (FacesConfig config : appConfigResources)
        {
            Vertex v = null;
            if (config.getName() != null)
            {
                v = new Vertex(config.getName(), config);
            }
            else
            {
                v = new Vertex(config);
            }
            vertexList.add(v);
        }
        
        //1. Resolve dependencies (before-after rules) and mark referenced vertex
        boolean[] referencedVertex = new boolean[vertexList.size()];

        for (int i = 0; i < vertexList.size(); i++)
        {
            Vertex v = vertexList.get(i);
            FacesConfig f = (FacesConfig) v.getNode();
            
            if (f.getOrdering() != null)
            {
                for (OrderSlot slot : f.getOrdering().getBeforeList())
                {
                    if (slot instanceof FacesConfigNameSlot)
                    {
                        String name = ((FacesConfigNameSlot) slot).getName();
                        int j = findVertex(vertexList, name);
                        Vertex v1 = vertexList.get(j);
                        if (v1 != null)
                        {
                            referencedVertex[i] = true;
                            referencedVertex[j] = true;
                            v1.addDependency(v);
                        }
                    }
                }
                for (OrderSlot slot : f.getOrdering().getAfterList())
                {
                    if (slot instanceof FacesConfigNameSlot)
                    {
                        String name = ((FacesConfigNameSlot) slot).getName();
                        int j = findVertex(vertexList, name);
                        Vertex v1 = vertexList.get(j);
                        if (v1 != null)
                        {
                            referencedVertex[i] = true;
                            referencedVertex[j] = true;
                            v.addDependency(v1);
                        }
                    }
                }
            }
        }

        //2. Classify into categories
        List<Vertex> beforeAfterOthersList = new ArrayList<Vertex>();
        List<Vertex> othersList = new ArrayList<Vertex>();
        List<Vertex> referencedList = new ArrayList<Vertex>();
        
        for (int i = 0; i < vertexList.size(); i++)
        {
            if (!referencedVertex[i])
            {
                Vertex v = vertexList.get(i);
                FacesConfig f = (FacesConfig) v.getNode();
                boolean added = false;
                if (f.getOrdering() != null)
                {
                    if (!f.getOrdering().getBeforeList().isEmpty())
                    {
                        added = true;
                        beforeAfterOthersList.add(v);                        
                    }
                    else if (!f.getOrdering().getAfterList().isEmpty())
                    {
                        added = true;
                        beforeAfterOthersList.add(v);
                    }
                }
                if (!added)
                {
                    othersList.add(v);
                }
            }
            else
            {
                referencedList.add(vertexList.get(i));
            }
        }
        
        //3. Sort all referenced nodes
        try
        {
            DirectedAcyclicGraphVerifier.topologicalSort(referencedList);
        }
        catch (CyclicDependencyException e)
        {
            e.printStackTrace();
        }

        //4. Add referenced nodes
        List<FacesConfig> sortedList = new ArrayList<FacesConfig>();
        for (Vertex v : referencedList)
        {
            sortedList.add((FacesConfig)v.getNode());
        }
        
        //5. add nodes without instructions at the end
        for (Vertex v : othersList)
        {
            sortedList.add((FacesConfig)v.getNode());
        }
        
        //6. add before/after nodes
        for (Vertex v : beforeAfterOthersList)
        {
            FacesConfig f = (FacesConfig) v.getNode();
            boolean added = false;
            if (f.getOrdering() != null)
            {
                if (!f.getOrdering().getBeforeList().isEmpty())
                {
                    added = true;
                    sortedList.add(0,f);                        
                }
            }
            if (!added)
            {
                sortedList.add(f);
            }            
        }
        
        printFacesConfigList("Sorted List", sortedList);
        
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
                                log.severe("Circular references detected when sorting " +
                                          "application config resources. Use absolute ordering instead.");
                                throw new FacesException("Circular references detected when sorting " +
                                        "application config resources. Use absolute ordering instead.");
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
                                log.severe("Circular references detected when sorting " +
                                    "application config resources. Use absolute ordering instead.");
                                throw new FacesException("Circular references detected when sorting " +
                                    "application config resources. Use absolute ordering instead.");
                            }
                        }
                    }
                }
            }
        }
    }
    
    public int findVertex(List<Vertex> vertexList, String name)
    {
        for (int i = 0; i < vertexList.size(); i++)
        {
            Vertex v = vertexList.get(i);
            if (name.equals(v.getName()))
            {
                return i;
            }
        }
        return -1;
    }*/

    public void applyAlgorithm2(List<FacesConfig> appConfigResources) throws FacesException
    {
        DefaultFacesConfigurationMerger merger = new DefaultFacesConfigurationMerger();
        
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
        
        List<FacesConfig> postOrderedList = merger.getPostOrderedList(appConfigResources);
        
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
        
        List<FacesConfig> sortedList = merger.sortRelativeOrderingList(postOrderedList);
        
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
    }
    
    public void testAbsoluteOrdering1() throws Exception
    {
        org.apache.myfaces.config.impl.element.FacesConfigImpl cfgAbs = new org.apache.myfaces.config.impl.element.FacesConfigImpl();
        org.apache.myfaces.config.impl.element.FacesConfigImpl cfgMK = new org.apache.myfaces.config.impl.element.FacesConfigImpl();
        org.apache.myfaces.config.impl.element.FacesConfigImpl cfgOWB = new org.apache.myfaces.config.impl.element.FacesConfigImpl();

        cfgMK.setName("cz_markoc_faces");
        
        AbsoluteOrderingImpl ao = new AbsoluteOrderingImpl();
        FacesConfigNameSlotImpl temp = new FacesConfigNameSlotImpl();
        temp.setName("cz_markoc_faces");
        ao.addOrderSlot(temp);
        ao.addOrderSlot(new ConfigOthersSlotImpl());
        
        cfgAbs.setAbsoluteOrdering(ao);

        List<FacesConfig> appConfigResources = new ArrayList<FacesConfig>();
        appConfigResources.add(cfgMK);
        appConfigResources.add(cfgOWB);
        
        //printFacesConfigList("Start List: [", appConfigResources);
        
        List<FacesConfig> sortedResources = orderAndFeedArtifactsAbsolute(appConfigResources, cfgAbs);
        
        //printFacesConfigList("Sorted-List: [", sortedResources);
        
        Assert.assertTrue(sortedResources.containsAll(appConfigResources));

        appConfigResources = new ArrayList<FacesConfig>();
        appConfigResources.add(cfgOWB);
        appConfigResources.add(cfgMK);
        
        //printFacesConfigList("Start List: [", appConfigResources);
        
        sortedResources = orderAndFeedArtifactsAbsolute(appConfigResources, cfgAbs);
        
        //printFacesConfigList("Sorted-List: [", sortedResources);
        
        Assert.assertTrue(sortedResources.containsAll(appConfigResources));

    }
    
    public List<FacesConfig> orderAndFeedArtifactsAbsolute(List<FacesConfig> appConfigResources, FacesConfig webAppConfig)
    {
        FacesConfigurator configurator = new FacesConfigurator(externalContext);

        if (webAppConfig != null && webAppConfig.getAbsoluteOrdering() != null)
        {
            if (webAppConfig.getOrdering() != null)
            {
                if (log.isLoggable(Level.WARNING))
                {
                    log.warning("<ordering> element found in application faces config. " +
                            "This description will be ignored and the actions described " +
                            "in <absolute-ordering> element will be taken into account instead.");
                }                
            }
            //Absolute ordering
            
            //1. Scan all appConfigResources and create a list
            //containing all resources not mentioned directly, preserving the
            //order founded
            List<FacesConfig> othersResources = new ArrayList<FacesConfig>();
            List<OrderSlot> slots = webAppConfig.getAbsoluteOrdering().getOrderList();
            for (FacesConfig resource : appConfigResources)
            {
                // First condition: if faces-config.xml does not have name it is 1) pre-JSF-2.0 or 2) has no <name> element,
                // -> in both cases cannot be ordered
                // Second condition : faces-config.xml has a name but <ordering> element does not have slot with that name
                //  -> resource can be ordered, but will fit into <others /> element
                if ((resource.getName() == null) || (resource.getName() != null && !containsResourceInSlot(slots, resource.getName())))
                {
                    othersResources.add(resource);
                }
            }

            List<FacesConfig> sortedResources = new ArrayList<FacesConfig>();
            //2. Scan slot by slot and merge information according
            for (OrderSlot slot : webAppConfig.getAbsoluteOrdering().getOrderList())
            {
                if (slot instanceof ConfigOthersSlotImpl)
                {
                    //Add all mentioned in othersResources
                    for (FacesConfig resource : othersResources)
                    {
                        sortedResources.add(resource);
                    }
                }
                else
                {
                    //Add it to the sorted list
                    FacesConfigNameSlotImpl nameSlot = (FacesConfigNameSlotImpl) slot;
                    sortedResources.add(getFacesConfig(appConfigResources, nameSlot.getName()));
                }
            }
            
            return sortedResources;
        }
        
        return null;
    }

    private FacesConfig getFacesConfig(List<FacesConfig> appConfigResources, String name)
    {
        for (FacesConfig cfg: appConfigResources)
        {
            if (cfg.getName() != null && name.equals(cfg.getName()))
            {
                return cfg;
            }
        }
        return null;
    }
    
    private boolean containsResourceInSlot(List<OrderSlot> slots, String name)
    {
        for (OrderSlot slot: slots)
        {
            if (slot instanceof FacesConfigNameSlotImpl)
            {
                FacesConfigNameSlotImpl nameSlot = (FacesConfigNameSlotImpl) slot;
                if (name.equals(nameSlot.getName()))
                {
                    return true;
                }
            }
        }
        return false;
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
