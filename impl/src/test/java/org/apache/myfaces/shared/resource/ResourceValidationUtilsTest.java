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
package org.apache.myfaces.shared.resource;

import org.apache.myfaces.test.base.junit4.AbstractJsfTestCase;
import org.junit.Assert;
import org.junit.Test;

public class ResourceValidationUtilsTest extends AbstractJsfTestCase
{
    @Test
    public void testLocaleNames() throws Exception
    {
        Assert.assertTrue(ResourceValidationUtils.isValidLocalePrefix("es_CO"));
        Assert.assertTrue(ResourceValidationUtils.isValidLocalePrefix("de"));
        Assert.assertTrue(ResourceValidationUtils.isValidLocalePrefix("de_AT"));
        Assert.assertTrue(ResourceValidationUtils.isValidLocalePrefix("zh_CN_id"));
        Assert.assertTrue(ResourceValidationUtils.isValidLocalePrefix("zh_CN_23"));
        
        Assert.assertFalse(ResourceValidationUtils.isValidLocalePrefix("de-AT"));
        Assert.assertFalse(ResourceValidationUtils.isValidLocalePrefix("."));
        Assert.assertFalse(ResourceValidationUtils.isValidLocalePrefix(".."));
        Assert.assertFalse(ResourceValidationUtils.isValidLocalePrefix("zh_"+'\t'+"CN"));
        Assert.assertFalse(ResourceValidationUtils.isValidLocalePrefix("\\.."));
        Assert.assertFalse(ResourceValidationUtils.isValidLocalePrefix("/.."));
        Assert.assertFalse(ResourceValidationUtils.isValidLocalePrefix("../"));
        Assert.assertFalse(ResourceValidationUtils.isValidLocalePrefix("..\\"));
        Assert.assertFalse(ResourceValidationUtils.isValidLocalePrefix(".."));
    }
    
    @Test
    public void testLibraryNames() throws Exception
    {
        Assert.assertTrue(ResourceValidationUtils.isValidLibraryName("mylib"));
        Assert.assertTrue(ResourceValidationUtils.isValidLibraryName("org.apache.myfaces"));
        Assert.assertTrue(ResourceValidationUtils.isValidLibraryName("some-js-lib"));
        Assert.assertTrue(ResourceValidationUtils.isValidLibraryName("some_js_lib"));
        
        Assert.assertTrue(ResourceValidationUtils.isValidLibraryName("components/panels", true));
        Assert.assertFalse(ResourceValidationUtils.isValidLibraryName("components/panels", false));

        Assert.assertFalse(ResourceValidationUtils.isValidLibraryName("/mylib"));
        Assert.assertFalse(ResourceValidationUtils.isValidLibraryName("mylib"+'\t'+"22"));
        Assert.assertFalse(ResourceValidationUtils.isValidLibraryName("\\mylib"));
        Assert.assertFalse(ResourceValidationUtils.isValidLibraryName(".."));
        Assert.assertFalse(ResourceValidationUtils.isValidLibraryName("..", true));
        Assert.assertFalse(ResourceValidationUtils.isValidLibraryName("some:js"));
        Assert.assertFalse(ResourceValidationUtils.isValidLibraryName("some?js"));
        Assert.assertFalse(ResourceValidationUtils.isValidLibraryName("some&js"));
    }

    @Test
    public void testResourceNames() throws Exception
    {
        Assert.assertTrue(ResourceValidationUtils.isValidResourceName("myres"));
        Assert.assertTrue(ResourceValidationUtils.isValidResourceName("myres.css"));
        Assert.assertTrue(ResourceValidationUtils.isValidResourceName("/myres"));
        Assert.assertTrue(ResourceValidationUtils.isValidResourceName("/mydir/./myres.css"));
        Assert.assertTrue(ResourceValidationUtils.isValidResourceName("org.apache.myfaces"));
        Assert.assertTrue(ResourceValidationUtils.isValidResourceName("my_res_file.css"));
        Assert.assertTrue(ResourceValidationUtils.isValidResourceName("my-res-file.css"));
        
        Assert.assertFalse(ResourceValidationUtils.isValidResourceName("myres"+'\t'+"22"));
        Assert.assertFalse(ResourceValidationUtils.isValidResourceName("\\myres"));
        Assert.assertFalse(ResourceValidationUtils.isValidResourceName(".."));
        Assert.assertFalse(ResourceValidationUtils.isValidResourceName("../"));
        Assert.assertFalse(ResourceValidationUtils.isValidResourceName("/.."));
        Assert.assertFalse(ResourceValidationUtils.isValidResourceName("\\.."));
        Assert.assertFalse(ResourceValidationUtils.isValidResourceName("..\\"));
        Assert.assertFalse(ResourceValidationUtils.isValidResourceName("myres.css/.."));
        Assert.assertFalse(ResourceValidationUtils.isValidResourceName("myres.css\\.."));
        Assert.assertFalse(ResourceValidationUtils.isValidResourceName("../myres.css"));
        Assert.assertFalse(ResourceValidationUtils.isValidResourceName("..\\myres.css"));
        Assert.assertFalse(ResourceValidationUtils.isValidResourceName("my/../res.css"));
        Assert.assertFalse(ResourceValidationUtils.isValidResourceName("my\\../res.css"));
        Assert.assertFalse(ResourceValidationUtils.isValidResourceName("my/..\\res.css"));
        Assert.assertFalse(ResourceValidationUtils.isValidResourceName("/mydir/../myres.css"));
        Assert.assertFalse(ResourceValidationUtils.isValidResourceName("my_res:file.css"));
        Assert.assertFalse(ResourceValidationUtils.isValidResourceName("my_res?file.css"));
        Assert.assertFalse(ResourceValidationUtils.isValidResourceName("my_res&file.css"));
    }
}
