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

import org.apache.shale.test.base.AbstractJsfTestCase;

import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FacesConfiguratorTestCase extends AbstractJsfTestCase
{

    public FacesConfiguratorTestCase(String name)
    {
        super(name);
    }

    public void testVersionNumber() throws Exception
    {

        // tests single digits
        // tests more digits
        // tests alpha
        // tests SNAPSHOT
        // tests digits in artifact names

        Map<String, List<FacesConfigurator.JarInfo>> libs = new HashMap<String, List<FacesConfigurator.JarInfo>>(30);
        FacesConfigurator.addJarInfo(libs, new URL("jar:file:/C:/.../WEB-INF/lib/myfaces-api-1.2.11-SNAPSHOT.jar!/META-INF/MANIFEST.MF"));
        FacesConfigurator.addJarInfo(libs, new URL("jar:file:/C:/.../WEB-INF/lib/myfaces-api-2.jar!/META-INF/MANIFEST.MF"));
        FacesConfigurator.addJarInfo(libs, new URL("jar:file:/C:/.../WEB-INF/lib/tomahawk12-1.1.10-SNAPSHOT.jar!/META-INF/MANIFEST.MF"));
        FacesConfigurator.addJarInfo(libs, new URL("jar:file:/G:/.../WEB-INF/lib/tomahawk-facelets-taglib-1.0.jar!/META-INF/MANIFEST.MF"));
        FacesConfigurator.addJarInfo(libs, new URL("jar:file:/C:/.../WEB-INF/lib/tomahawk-sandbox12-1.1.10.jar!/META-INF/MANIFEST.MF"));
        FacesConfigurator.addJarInfo(libs, new URL("jar:file:/home/.../tobago-core/1.5.0-alpha-3-SNAPSHOT/tobago-core-1.5.0-alpha-3-SNAPSHOT.jar!/META-INF/MANIFEST.MF"));
        FacesConfigurator.addJarInfo(libs, new URL("jar:file:/home/.../tobago-core/1.0.35/tobago-core-1.0.35.jar!/META-INF/MANIFEST.MF"));

        final List<FacesConfigurator.JarInfo> mf = libs.get(FacesConfigurator.MYFACES_API_PACKAGE_NAME);
        assertEquals(2, mf.size());
        assertEquals("1.2.11-SNAPSHOT", mf.get(0).getVersion());
        assertEquals("2", mf.get(1).getVersion());

        final List<FacesConfigurator.JarInfo> tk12 = libs.get(FacesConfigurator.MYFACES_TOMAHAWK12_PACKAGE_NAME);
        assertEquals(1, tk12.size());
        assertEquals("1.1.10-SNAPSHOT", tk12.get(0).getVersion());

        final List<FacesConfigurator.JarInfo> tksb = libs.get(FacesConfigurator.MYFACES_TOMAHAWK_SANDBOX12_PACKAGE_NAME);
        assertEquals(1, tksb.size());
        assertEquals("1.1.10", tksb.get(0).getVersion());

        final List<FacesConfigurator.JarInfo> tobago = libs.get(FacesConfigurator.MYFACES_TOBAGO_PACKAGE_NAME);
        assertEquals(2, tobago.size());
        assertEquals("1.5.0-alpha-3-SNAPSHOT", tobago.get(0).getVersion());
        assertEquals("1.0.35", tobago.get(1).getVersion());
    }
}
