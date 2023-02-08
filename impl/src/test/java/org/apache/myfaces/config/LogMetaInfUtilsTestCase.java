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

import org.apache.myfaces.test.base.junit.AbstractJsfTestCase;

import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class LogMetaInfUtilsTestCase extends AbstractJsfTestCase
{
    @Test
    public void testVersionNumber() throws Exception
    {

        // tests single digits
        // tests more digits
        // tests alpha
        // tests SNAPSHOT
        // tests digits in artifact names

        Map<String, List<LogMetaInfUtils.JarInfo>> libs = new HashMap<String, List<LogMetaInfUtils.JarInfo>>(30);
        LogMetaInfUtils.addJarInfo(libs, new URL("jar:file:/C:/.../WEB-INF/lib/myfaces-api-1.2.11-SNAPSHOT.jar!/META-INF/MANIFEST.MF"));
        LogMetaInfUtils.addJarInfo(libs, new URL("jar:file:/C:/.../WEB-INF/lib/myfaces-api-2.jar!/META-INF/MANIFEST.MF"));
        LogMetaInfUtils.addJarInfo(libs, new URL("jar:file:/C:/.../WEB-INF/lib/tomahawk12-1.1.10-SNAPSHOT.jar!/META-INF/MANIFEST.MF"));
        LogMetaInfUtils.addJarInfo(libs, new URL("jar:file:/G:/.../WEB-INF/lib/tomahawk-facelets-taglib-1.0.jar!/META-INF/MANIFEST.MF"));
        LogMetaInfUtils.addJarInfo(libs, new URL("jar:file:/C:/.../WEB-INF/lib/tomahawk-sandbox12-1.1.10.jar!/META-INF/MANIFEST.MF"));
        LogMetaInfUtils.addJarInfo(libs, new URL("jar:file:/home/.../tobago-core/1.5.0-alpha-3-SNAPSHOT/tobago-core-1.5.0-alpha-3-SNAPSHOT.jar!/META-INF/MANIFEST.MF"));
        LogMetaInfUtils.addJarInfo(libs, new URL("jar:file:/home/.../tobago-core/1.0.35/tobago-core-1.0.35.jar!/META-INF/MANIFEST.MF"));
        LogMetaInfUtils.addJarInfo(libs, new URL("jar:file:/home/.../other/1.0/other-1.0.jar!/META-INF/MANIFEST.MF"));
        LogMetaInfUtils.addJarInfo(libs, new URL("file:/opt/project/tobago/tobago-example/tobago-example-demo/target/tobago-example-demo/WEB-INF/lib/slf4j-log4j12-1.6.1.jar"));

        final List<LogMetaInfUtils.JarInfo> mf = libs.get("myfaces-api");
        Assertions.assertEquals(2, mf.size());
        Assertions.assertEquals("1.2.11-SNAPSHOT", mf.get(0).getVersion());
        Assertions.assertEquals("2", mf.get(1).getVersion());

        final List<LogMetaInfUtils.JarInfo> tk12 = libs.get("tomahawk12");
        Assertions.assertEquals(1, tk12.size());
        Assertions.assertEquals("1.1.10-SNAPSHOT", tk12.get(0).getVersion());

        final List<LogMetaInfUtils.JarInfo> tksb = libs.get("tomahawk-sandbox12");
        Assertions.assertEquals(1, tksb.size());
        Assertions.assertEquals("1.1.10", tksb.get(0).getVersion());

        final List<LogMetaInfUtils.JarInfo> tobago = libs.get("tobago-core");
        Assertions.assertEquals(2, tobago.size());
        Assertions.assertEquals("1.5.0-alpha-3-SNAPSHOT", tobago.get(0).getVersion());
        Assertions.assertEquals("1.0.35", tobago.get(1).getVersion());

        final List<LogMetaInfUtils.JarInfo> other = libs.get("other");
        Assertions.assertNull(other);

        final List<LogMetaInfUtils.JarInfo> slf = libs.get("slf4j-log4j12");
        Assertions.assertNull(slf);
    }
}
