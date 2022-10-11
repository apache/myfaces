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

package extras.apache.org.jsintegration.other;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Named;


/**
 * @author Werner Punz (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
@Named
@RequestScoped
public class MultiFormBean
{
    String inputText1 = "";
    String inputText2 = "";

    public String getInputText1()
    {
        return inputText1;
    }

    public void setInputText1(String inputText1)
    {
        this.inputText1 = inputText1;
    }

    public String getInputText2()
    {
        return inputText2;
    }

    public void setInputText2(String inputText2)
    {
        this.inputText2 = inputText2;
    }

    public String doSubmit1() {
        return null;
    }

    public String doSubmit2() {
        return null;
    }

}
