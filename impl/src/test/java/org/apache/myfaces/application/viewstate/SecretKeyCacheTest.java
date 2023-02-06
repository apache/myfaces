/*
 * Copyright 2004-2006 The Apache Software Foundation.
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


package org.apache.myfaces.application.viewstate;

import org.apache.myfaces.application.viewstate.StateUtils;
import org.apache.myfaces.test.base.junit.AbstractJsfTestCase;

import javax.crypto.SecretKey;
import org.junit.Assert;
import org.junit.Test;

public class SecretKeyCacheTest extends AbstractJsfTestCase
{

    @Override
    public void setUp() throws Exception
    {
        super.setUp();
        
        servletContext.addInitParameter(StateUtils.INIT_SECRET, 
                AbstractStateUtilsTest.BASE64_KEY_SIZE_8);
        servletContext.addInitParameter(StateUtils.INIT_MAC_SECRET, AbstractStateUtilsTest.BASE64_KEY_SIZE_8);
    }

    @Test
    public void testDefaultAlgorithmUse(){
        
        StateUtils.initSecret(servletContext);
        
        SecretKey secretKey = (SecretKey) servletContext.getAttribute(StateUtils.INIT_SECRET_KEY_CACHE);
        
        Assert.assertTrue("Making sure MyFaces uses the " +
                "default algorithm when one is not specified",
                StateUtils.DEFAULT_ALGORITHM.equals(secretKey.getAlgorithm()));
        
    }
    
    @Test
    public void testInitFacesWithoutCache(){

        servletContext.addInitParameter(StateUtils.INIT_SECRET_KEY_CACHE, "false");
        
        StateUtils.initSecret(servletContext);

        Object object = servletContext.getAttribute(StateUtils.INIT_SECRET_KEY_CACHE);
        
        Assert.assertNull("Making sure StateUtils.initSecret does not create a SecretKey", object);
        
    }
    
    @Test
    public void testInitFacesWithCache(){
        
        StateUtils.initSecret(servletContext);
        
        Object object = servletContext.getAttribute(StateUtils.INIT_SECRET_KEY_CACHE);
        
        Assert.assertFalse("Making sure StateUtils.initSecret() puts an object in application scope", 
                object == null);
        
        Assert.assertTrue("Making sure StateUtils.initSecret() is creating a SecretKey", 
                object instanceof SecretKey);
        
    }
    
}
