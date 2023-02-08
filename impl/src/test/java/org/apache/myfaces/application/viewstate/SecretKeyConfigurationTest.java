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

import jakarta.faces.FacesException;
import org.apache.myfaces.test.base.junit.AbstractJsfTestCase;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class SecretKeyConfigurationTest extends AbstractJsfTestCase
{
    @Override
    @BeforeEach
    public void setUp() throws Exception
    {
        super.setUp();
        servletContext.addInitParameter(StateUtils.INIT_SECRET, "shouldn't matter");
        servletContext.addInitParameter(StateUtils.INIT_MAC_SECRET, AbstractStateUtilsTest.BASE64_KEY_SIZE_8);
        
    }

    @Test
    public void testMissingSecretKeyEncrypt(){
        
        try{
            StateUtils.encrypt("serialized objects".getBytes(), externalContext);
            Assertions.fail("An exception should be thrown if there" +
                    " is no SecretKey in application scope and cacheing is enabled ");
        }catch(Exception e){
        }
        
    }
    
    @Test
    public void testNonSecretKeyEncrypt(){
        
        servletContext.setAttribute(StateUtils.INIT_SECRET_KEY_CACHE, new Integer(8));
        
        try{
            
            StateUtils.encrypt("serialized objects".getBytes(), externalContext);
            Assertions.fail("An exception should be thrown if there" +
                    " is no SecretKey in application scope and cacheing is enabled ");
        }catch(Exception cce){
        }
        
    }
    
    @Test
    public void testMissingSecretKeyDecrypt(){
        
        Assertions.assertThrows(FacesException.class, () -> {
            StateUtils.decrypt("serialized objects".getBytes(), externalContext);
        });
    }
    
    @Test
    public void testNonSecretKeyDecrypt(){
        
        servletContext.setAttribute(StateUtils.INIT_SECRET_KEY_CACHE, new Integer(8));
        
        try{
            
            StateUtils.decrypt("serialized objects".getBytes(), externalContext);
            Assertions.fail("An exception should be thrown if there" +
                    " is no SecretKey in application scope and cacheing is enabled ");
        }catch(Exception cce){
        }
        
    }

}
