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
package org.apache.myfaces.config;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.myfaces.config.FacesConfigurator.Version;
import org.apache.shale.test.base.AbstractJsfTestCase;

public class FacesConfiguratorTestCase extends AbstractJsfTestCase
{

    public FacesConfiguratorTestCase(String name)
    {
        super(name);
    }

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception
    {
        super.tearDown();
    }
    
    public static final String REGEX_LIBRARY = "((jar)?(besjar)?(wsjar)?(zip)?)?:(file:.*/(.+)-" +
    "(\\d+)(\\.(\\d+)(\\.(\\d+)(\\.(\\d+))?)?)?(-SNAPSHOT)?" +
    "\\.jar)!/META-INF/MANIFEST.MF";
    
        
    public void testVersionNumber() throws Exception
    {
        Pattern pattern = Pattern.compile(REGEX_LIBRARY);
        List<Version> l = new ArrayList<Version>();
        l.add(testJar(pattern, "jar:file:/C:/Program%20Files/Apache%20Software%20Foundation/Tomcat%206.0/webapps/ProjetBidonJSF/WEB-INF/lib/commons-collections-3.jar!/META-INF/MANIFEST.MF"));
        l.add(testJar(pattern, "jar:file:/C:/Program%20Files/Apache%20Software%20Foundation/Tomcat%206.0/webapps/ProjetBidonJSF/WEB-INF/lib/commons-codec-1.3.jar!/META-INF/MANIFEST.MF"));
        l.add(testJar(pattern, "jar:file:/C:/Program%20Files/Apache%20Software%20Foundation/Tomcat%206.0/webapps/ProjetBidonJSF/WEB-INF/lib/commons-beanutils-1.7.0.jar!/META-INF/MANIFEST.MF"));
        l.add(testJar(pattern, "jar:file:/C:/Program%20Files/Apache%20Software%20Foundation/Tomcat%206.0/webapps/ProjetBidonJSF/WEB-INF/lib/commons-beanutils-1.7.0.6.jar!/META-INF/MANIFEST.MF"));
        l.add(testJar(pattern, "jar:file:/C:/Program%20Files/Apache%20Software%20Foundation/Tomcat%206.0/webapps/ProjetBidonJSF/WEB-INF/lib/commons-beanutils-1.7.0-SNAPSHOT.jar!/META-INF/MANIFEST.MF"));
        l.add(testJar(pattern, "jar:file:/C:/Program%20Files/Apache%20Software%20Foundation/Tomcat%206.0/webapps/ProjetBidonJSF/WEB-INF/lib/tomahawk12-1.1.10-SNAPSHOT.jar!/META-INF/MANIFEST.MF"));
        l.add(testJar(pattern, "jar:file:/C:/Program%20Files/Apache%20Software%20Foundation/Tomcat%206.0/webapps/ProjetBidonJSF/WEB-INF/lib/tomahawk-sandbox12-1.1.10.jar!/META-INF/MANIFEST.MF"));
        
        assertEquals(new Version("3", null, null, null, null), l.get(0) );
        assertEquals(new Version("1", "3", null, null, null), l.get(1) );
        assertEquals(new Version("1", "7", "0", null, null), l.get(2) );
        assertEquals(new Version("1", "7", "0", "6", null), l.get(3) );
        assertEquals(new Version("1", "7", "0", null, "SNAPSHOT"), l.get(4) );
        assertEquals(new Version("1", "1", "10", null, "SNAPSHOT"), l.get(5) );
        assertEquals(new Version("1", "1", "10", null, null), l.get(6) );

        Collections.sort(l);
    }
    
    private static Version testJar(Pattern pattern, String libName)
    {
        Matcher matcher = pattern.matcher(libName);
        
        if (matcher.matches())
        {
            Version version = new Version(matcher.group(8), matcher.group(10), matcher.group(12), matcher.group(14), 
                    matcher.group(15)); 
            return version;
        }
        
        return null;
    }    
}
