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


package org.apache.myfaces.shared.util;

import org.apache.myfaces.test.base.AbstractJsfTestCase;

import javax.faces.FacesException;

public class InitVector_CBCTestCase extends AbstractJsfTestCase {

    public InitVector_CBCTestCase(String name) {
        super(name);
    }
    
    // No longer necessary using junit 4 to run tests
    //public static Test suite() {
    //    return null; // keep this method or maven won't run it
    //}    

    public void setUp() throws Exception{
    
        super.setUp();
        
        servletContext.addInitParameter(StateUtils.INIT_SECRET, "shouldn't matter");
        servletContext.addInitParameter(StateUtils.INIT_ALGORITHM, "shouldn't matter either");
        servletContext.addInitParameter(StateUtils.INIT_ALGORITHM_PARAM, "CBC/PKCS5Padding");
        servletContext.addInitParameter(StateUtils.INIT_SECRET_KEY_CACHE, "false");
        servletContext.addInitParameter(StateUtils.INIT_MAC_SECRET, "shouldn't matter");
        // DO NOT UNCOMMENT THIS ! we are simulating a bad conf
        //servletContext.addInitParameter(org.apache.myfaces.shared.util.StateUtils.INIT_ALGORITHM_IV, BASE64_KEY_SIZE_16);        
        
    }

    public void testDecryption() {
        
        byte[] sensitiveBytes = "bound to fail".getBytes();
        
        try{
            
            StateUtils.decrypt(sensitiveBytes, externalContext);
            
            fail("MyFaces should throw a meaningful " +
                    "exception when users opt for CBC mode " +
                    "encryption w/out an initialization vector.");
            
        }catch(FacesException fe){
        }
        
    }
    
    public void testEncryption() {
        
        byte[] sensitiveBytes = "bound to fail".getBytes();
        
        try{
            
            StateUtils.encrypt(sensitiveBytes, externalContext);
            
            fail("MyFaces should throw a meaningful " +
                    "exception when users opt for CBC mode " +
                    "encryption w/out an initialization vector.");
            
        }catch(FacesException fe){
        }
        
    }
    
}
