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
package org.apache.myfaces.core.extensions.quarkus.runtime.graal;

import org.apache.myfaces.config.impl.element.facelets.FaceletSourceTagImpl;

import com.oracle.svm.core.annotate.Alias;
import com.oracle.svm.core.annotate.Substitute;
import com.oracle.svm.core.annotate.TargetClass;

@TargetClass(FaceletSourceTagImpl.class)
public final class SubstituteFaceletSourceTag
{

    @Alias private String source;

    @Substitute
    public String getSource()
    {
        // we never want jar:file:/project/quarkus-runner.jar!/META-INF/resources/tags/custom.xhtml
        // it must always be resource:/META-INF/resources/tags/custom.xhtml in native mode
        if (!source.contains("!/"))
        {
            return source;
        }
        String resource = source.split("!")[1];
        if (!resource.startsWith("/META-INF"))
        {
            resource = "/META-INF" + resource;
        }
        resource = "resource:" + resource;
        return resource;
    }

}
