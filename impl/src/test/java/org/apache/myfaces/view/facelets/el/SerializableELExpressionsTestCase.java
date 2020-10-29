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
package org.apache.myfaces.view.facelets.el;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import javax.el.ExpressionFactory;
import javax.el.ValueExpression;
import jakarta.faces.view.Location;

import org.apache.myfaces.view.facelets.FaceletTestCase;
import org.apache.myfaces.view.facelets.tag.TagAttributeImpl;
import org.junit.Assert;
import org.junit.Test;

public class SerializableELExpressionsTestCase extends FaceletTestCase
{
   
    @Override
    protected ExpressionFactory createExpressionFactory()
    {
        return new org.apache.el.ExpressionFactoryImpl();
    }
    
    @Test
    public void testSerializeLocationValueExpressionWrapper() throws Exception
    {
        ValueExpression ve = facesContext.getApplication().getExpressionFactory().createValueExpression(
                facesContext.getELContext(), "#{cc.attrs.value}", Object.class);
        
        TagAttributeImpl tai = new TagAttributeImpl(new Location("path",299, 12), 
                null, "value", "value", "#{cc.attrs.value}");
        TagValueExpression tve = new TagValueExpression(tai, ve);
        LocationValueExpression lve = new LocationValueExpression(
                new Location("path2",334, 22), tve);
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream(128);
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(lve);
        oos.flush();
        baos.flush();
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        LocationValueExpression lve2 = (LocationValueExpression) ois.readObject();
        Assert.assertEquals(lve.getExpressionString(), lve2.getExpressionString());
        Assert.assertEquals(lve.getLocation().getPath(), lve2.getLocation().getPath());
        Assert.assertEquals(lve.getLocation().getLine(), lve2.getLocation().getLine());
        Assert.assertEquals(lve.getLocation().getColumn(), lve2.getLocation().getColumn());
        oos.close();
        ois.close();
    }
    
    @Test
    public void testSerializeLocationValueExpressionUELWrapper() throws Exception
    {
        ValueExpression ve = facesContext.getApplication().getExpressionFactory().createValueExpression(
                facesContext.getELContext(), "#{cc.attrs.value}", Object.class);
        
        TagAttributeImpl tai = new TagAttributeImpl(new Location("path",299, 12), 
                null, "value", "value", "#{cc.attrs.value}");
        TagValueExpression tve = new TagValueExpression(tai, ve);
        LocationValueExpression lve = new LocationValueExpression(
                new Location("path2",334, 22), tve);
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream(128);
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(lve);
        oos.flush();
        baos.flush();
        ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
        ObjectInputStream ois = new ObjectInputStream(bais);
        LocationValueExpression lve2 = (LocationValueExpression) ois.readObject();
        Assert.assertEquals(lve.getExpressionString(), lve2.getExpressionString());
        Assert.assertEquals(lve.getLocation().getPath(), lve2.getLocation().getPath());
        Assert.assertEquals(lve.getLocation().getLine(), lve2.getLocation().getLine());
        Assert.assertEquals(lve.getLocation().getColumn(), lve2.getLocation().getColumn());
        oos.close();
        ois.close();
    }
    
}
