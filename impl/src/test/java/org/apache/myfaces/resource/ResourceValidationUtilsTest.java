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
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class ResourceValidationUtilsTest extends AbstractFacesTestCase
{
    @Test
    public void testLocaleNames() throws Exception
    {
        Assertions.assertTrue(ResourceValidationUtils.isValidLocalePrefix("es_CO"));
        Assertions.assertTrue(ResourceValidationUtils.isValidLocalePrefix("de"));
        Assertions.assertTrue(ResourceValidationUtils.isValidLocalePrefix("de_AT"));
        Assertions.assertTrue(ResourceValidationUtils.isValidLocalePrefix("zh_CN_id"));
        Assertions.assertTrue(ResourceValidationUtils.isValidLocalePrefix("zh_CN_23"));
        
        Assertions.assertFalse(ResourceValidationUtils.isValidLocalePrefix("de-AT"));
        Assertions.assertFalse(ResourceValidationUtils.isValidLocalePrefix("."));
        Assertions.assertFalse(ResourceValidationUtils.isValidLocalePrefix(".."));
        Assertions.assertFalse(ResourceValidationUtils.isValidLocalePrefix("zh_"+'\t'+"CN"));
        Assertions.assertFalse(ResourceValidationUtils.isValidLocalePrefix("\\.."));
        Assertions.assertFalse(ResourceValidationUtils.isValidLocalePrefix("/.."));
        Assertions.assertFalse(ResourceValidationUtils.isValidLocalePrefix("../"));
        Assertions.assertFalse(ResourceValidationUtils.isValidLocalePrefix("..\\"));
        Assertions.assertFalse(ResourceValidationUtils.isValidLocalePrefix(".."));
    }
    
    @Test
    public void testLibraryNames() throws Exception
    {
        Assertions.assertTrue(ResourceValidationUtils.isValidLibraryName("mylib"));
        Assertions.assertTrue(ResourceValidationUtils.isValidLibraryName("org.apache.myfaces"));
        Assertions.assertTrue(ResourceValidationUtils.isValidLibraryName("some-js-lib"));
        Assertions.assertTrue(ResourceValidationUtils.isValidLibraryName("some_js_lib"));
        
        Assertions.assertTrue(ResourceValidationUtils.isValidLibraryName("components/panels", true));
        Assertions.assertFalse(ResourceValidationUtils.isValidLibraryName("components/panels", false));

        Assertions.assertFalse(ResourceValidationUtils.isValidLibraryName("/mylib"));
        Assertions.assertFalse(ResourceValidationUtils.isValidLibraryName("mylib"+'\t'+"22"));
        Assertions.assertFalse(ResourceValidationUtils.isValidLibraryName("\\mylib"));
        Assertions.assertFalse(ResourceValidationUtils.isValidLibraryName(".."));
        Assertions.assertFalse(ResourceValidationUtils.isValidLibraryName("..", true));
        Assertions.assertFalse(ResourceValidationUtils.isValidLibraryName("some:js"));
        Assertions.assertFalse(ResourceValidationUtils.isValidLibraryName("some?js"));
        Assertions.assertFalse(ResourceValidationUtils.isValidLibraryName("some&js"));
    }

    @Test
    public void testResourceNames() throws Exception
    {
        Assertions.assertTrue(ResourceValidationUtils.isValidResourceName("myres"));
        Assertions.assertTrue(ResourceValidationUtils.isValidResourceName("myres.css"));
        Assertions.assertTrue(ResourceValidationUtils.isValidResourceName("/myres"));
        Assertions.assertTrue(ResourceValidationUtils.isValidResourceName("/mydir/./myres.css"));
        Assertions.assertTrue(ResourceValidationUtils.isValidResourceName("org.apache.myfaces"));
        Assertions.assertTrue(ResourceValidationUtils.isValidResourceName("my_res_file.css"));
        Assertions.assertTrue(ResourceValidationUtils.isValidResourceName("my-res-file.css"));
        
        Assertions.assertFalse(ResourceValidationUtils.isValidResourceName("myres"+'\t'+"22"));
        Assertions.assertFalse(ResourceValidationUtils.isValidResourceName("\\myres"));
        Assertions.assertFalse(ResourceValidationUtils.isValidResourceName(".."));
        Assertions.assertFalse(ResourceValidationUtils.isValidResourceName("../"));
        Assertions.assertFalse(ResourceValidationUtils.isValidResourceName("/.."));
        Assertions.assertFalse(ResourceValidationUtils.isValidResourceName("\\.."));
        Assertions.assertFalse(ResourceValidationUtils.isValidResourceName("..\\"));
        Assertions.assertFalse(ResourceValidationUtils.isValidResourceName("myres.css/.."));
        Assertions.assertFalse(ResourceValidationUtils.isValidResourceName("myres.css\\.."));
        Assertions.assertFalse(ResourceValidationUtils.isValidResourceName("../myres.css"));
        Assertions.assertFalse(ResourceValidationUtils.isValidResourceName("..\\myres.css"));
        Assertions.assertFalse(ResourceValidationUtils.isValidResourceName("my/../res.css"));
        Assertions.assertFalse(ResourceValidationUtils.isValidResourceName("my\\../res.css"));
        Assertions.assertFalse(ResourceValidationUtils.isValidResourceName("my/..\\res.css"));
        Assertions.assertFalse(ResourceValidationUtils.isValidResourceName("/mydir/../myres.css"));
        Assertions.assertFalse(ResourceValidationUtils.isValidResourceName("my_res:file.css"));
        Assertions.assertFalse(ResourceValidationUtils.isValidResourceName("my_res?file.css"));
        Assertions.assertFalse(ResourceValidationUtils.isValidResourceName("my_res&file.css"));
    }
}
