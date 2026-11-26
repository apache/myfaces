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
package org.apache.myfaces.resource;

import org.apache.myfaces.test.base.junit.AbstractFacesTestCase;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ResourceValidationUtilsTest extends AbstractFacesTestCase
{
    @Test
    void localeNames() throws Exception
    {
        assertTrue(ResourceValidationUtils.isValidLocalePrefix("es_CO"));
        assertTrue(ResourceValidationUtils.isValidLocalePrefix("de"));
        assertTrue(ResourceValidationUtils.isValidLocalePrefix("de_AT"));
        assertTrue(ResourceValidationUtils.isValidLocalePrefix("zh_CN_id"));
        assertTrue(ResourceValidationUtils.isValidLocalePrefix("zh_CN_23"));
        
        assertFalse(ResourceValidationUtils.isValidLocalePrefix("de-AT"));
        assertFalse(ResourceValidationUtils.isValidLocalePrefix("."));
        assertFalse(ResourceValidationUtils.isValidLocalePrefix(".."));
        assertFalse(ResourceValidationUtils.isValidLocalePrefix("zh_"+'\t'+"CN"));
        assertFalse(ResourceValidationUtils.isValidLocalePrefix("\\.."));
        assertFalse(ResourceValidationUtils.isValidLocalePrefix("/.."));
        assertFalse(ResourceValidationUtils.isValidLocalePrefix("../"));
        assertFalse(ResourceValidationUtils.isValidLocalePrefix("..\\"));
        assertFalse(ResourceValidationUtils.isValidLocalePrefix(".."));
    }

    @Test
    void libraryNames() throws Exception
    {
        assertTrue(ResourceValidationUtils.isValidLibraryName("mylib"));
        assertTrue(ResourceValidationUtils.isValidLibraryName("org.apache.myfaces"));
        assertTrue(ResourceValidationUtils.isValidLibraryName("some-js-lib"));
        assertTrue(ResourceValidationUtils.isValidLibraryName("some_js_lib"));
        
        assertTrue(ResourceValidationUtils.isValidLibraryName("components/panels", true));
        assertFalse(ResourceValidationUtils.isValidLibraryName("components/panels", false));

        assertFalse(ResourceValidationUtils.isValidLibraryName("/mylib"));
        assertFalse(ResourceValidationUtils.isValidLibraryName("mylib"+'\t'+"22"));
        assertFalse(ResourceValidationUtils.isValidLibraryName("\\mylib"));
        assertFalse(ResourceValidationUtils.isValidLibraryName(".."));
        assertFalse(ResourceValidationUtils.isValidLibraryName("..", true));
        assertFalse(ResourceValidationUtils.isValidLibraryName("some:js"));
        assertFalse(ResourceValidationUtils.isValidLibraryName("some?js"));
        assertFalse(ResourceValidationUtils.isValidLibraryName("some&js"));
    }

    @Test
    void resourceNames() throws Exception
    {
        assertTrue(ResourceValidationUtils.isValidResourceName("myres"));
        assertTrue(ResourceValidationUtils.isValidResourceName("myres.css"));
        assertTrue(ResourceValidationUtils.isValidResourceName("/myres"));
        assertTrue(ResourceValidationUtils.isValidResourceName("/mydir/./myres.css"));
        assertTrue(ResourceValidationUtils.isValidResourceName("org.apache.myfaces"));
        assertTrue(ResourceValidationUtils.isValidResourceName("my_res_file.css"));
        assertTrue(ResourceValidationUtils.isValidResourceName("my-res-file.css"));
        
        assertFalse(ResourceValidationUtils.isValidResourceName("myres"+'\t'+"22"));
        assertFalse(ResourceValidationUtils.isValidResourceName("\\myres"));
        assertFalse(ResourceValidationUtils.isValidResourceName(".."));
        assertFalse(ResourceValidationUtils.isValidResourceName("../"));
        assertFalse(ResourceValidationUtils.isValidResourceName("/.."));
        assertFalse(ResourceValidationUtils.isValidResourceName("\\.."));
        assertFalse(ResourceValidationUtils.isValidResourceName("..\\"));
        assertFalse(ResourceValidationUtils.isValidResourceName("myres.css/.."));
        assertFalse(ResourceValidationUtils.isValidResourceName("myres.css\\.."));
        assertFalse(ResourceValidationUtils.isValidResourceName("../myres.css"));
        assertFalse(ResourceValidationUtils.isValidResourceName("..\\myres.css"));
        assertFalse(ResourceValidationUtils.isValidResourceName("my/../res.css"));
        assertFalse(ResourceValidationUtils.isValidResourceName("my\\../res.css"));
        assertFalse(ResourceValidationUtils.isValidResourceName("my/..\\res.css"));
        assertFalse(ResourceValidationUtils.isValidResourceName("/mydir/../myres.css"));
        assertFalse(ResourceValidationUtils.isValidResourceName("my_res:file.css"));
        assertFalse(ResourceValidationUtils.isValidResourceName("my_res?file.css"));
        assertFalse(ResourceValidationUtils.isValidResourceName("my_res&file.css"));
    }
}
