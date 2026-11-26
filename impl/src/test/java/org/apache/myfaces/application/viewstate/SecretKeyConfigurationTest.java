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

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;

import jakarta.faces.FacesException;
import org.apache.myfaces.test.base.junit.AbstractFacesTestCase;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class SecretKeyConfigurationTest extends AbstractFacesTestCase
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
    void missingSecretKeyEncrypt(){
        
        try{
            StateUtils.encrypt("serialized objects".getBytes(), externalContext);
            fail("An exception should be thrown if there" +
                    " is no SecretKey in application scope and caching is enabled ");
        }catch(Exception e){
        }
        
    }

    @Test
    void nonSecretKeyEncrypt(){
        
        servletContext.setAttribute(StateUtils.INIT_SECRET_KEY_CACHE, new Integer(8));
        
        try{
            
            StateUtils.encrypt("serialized objects".getBytes(), externalContext);
            fail("An exception should be thrown if there" +
                    " is no SecretKey in application scope and caching is enabled ");
        }catch(Exception cce){
        }
        
    }

    @Test
    void missingSecretKeyDecrypt(){
        
        assertThrows(FacesException.class, () ->
            StateUtils.decrypt("serialized objects".getBytes(), externalContext));
    }

    @Test
    void nonSecretKeyDecrypt(){
        
        servletContext.setAttribute(StateUtils.INIT_SECRET_KEY_CACHE, new Integer(8));
        
        try{
            
            StateUtils.decrypt("serialized objects".getBytes(), externalContext);
            fail("An exception should be thrown if there" +
                    " is no SecretKey in application scope and caching is enabled ");
        }catch(Exception cce){
        }
        
    }

}
