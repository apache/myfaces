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

package org.apache.myfaces.view.facelets.tag.composite;

import jakarta.faces.view.ViewScoped;
import jakarta.inject.Named;

import java.io.Serializable;

@Named
@ViewScoped
public class OverwriteRequiredCompositeBean implements Serializable
{
    private String testString1;
    private String testString2;
    private String testString3;
    private String testString4;

    public String getTestString1() {
        return testString1;
    }

    public void setTestString1(String testString1) {
        this.testString1 = testString1;
    }

    public String getTestString2() {
        return testString2;
    }

    public void setTestString2(String testString2) {
        this.testString2 = testString2;
    }

    public String getTestString3() {
        return testString3;
    }

    public void setTestString3(String testString3) {
        this.testString3 = testString3;
    }

    public String getTestString4() {
        return testString4;
    }

    public void setTestString4(String testString4) {
        this.testString4 = testString4;
    }

    public boolean isStringRequired() {
        return true;
    }
}
