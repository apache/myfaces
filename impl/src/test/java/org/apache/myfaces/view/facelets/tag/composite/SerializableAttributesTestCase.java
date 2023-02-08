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
package org.apache.myfaces.view.facelets.tag.composite;

import java.beans.BeanDescriptor;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import jakarta.faces.component.UINamingContainer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class SerializableAttributesTestCase
{
    @Test
    public void testSerializeCompositeResourceWrapper() throws Exception
    {
        CompositeResouceWrapper subject = new CompositeResouceWrapper();
        subject.setResourceName("testRes");
        subject.setLibraryName("testLib");
        subject.setContentType(null);
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream(128);
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(subject);
        oos.flush();
        baos.flush();
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        CompositeResouceWrapper blorg = (CompositeResouceWrapper) ois.readObject();
        Assertions.assertEquals(blorg.getResourceName(), subject.getResourceName());
        Assertions.assertEquals(blorg.getLibraryName(), subject.getLibraryName());
        Assertions.assertEquals(blorg.getContentType(), subject.getContentType());
        oos.close();
        ois.close();
    }
    
    @Test
    public void testSerializeCompositeComponentBeanInfo() throws Exception
    {
        BeanDescriptor descriptor = new BeanDescriptor(UINamingContainer.class);
        CompositeComponentBeanInfo subject = new CompositeComponentBeanInfo(descriptor);
        CompositeComponentPropertyDescriptor pd = new CompositeComponentPropertyDescriptor("attrName");
        pd.setValue("type","someClass");
        subject.getPropertyDescriptorsList().add(pd);       
        ByteArrayOutputStream baos = new ByteArrayOutputStream(128);
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(subject);
        oos.flush();
        baos.flush();
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        CompositeComponentBeanInfo blorg = (CompositeComponentBeanInfo) ois.readObject();
        
        Assertions.assertEquals(UINamingContainer.class, blorg.getBeanDescriptor().getBeanClass());
        Assertions.assertEquals(pd.getName(), blorg.getPropertyDescriptorsList().get(0).getName());
        oos.close();
        ois.close();
    }
}
