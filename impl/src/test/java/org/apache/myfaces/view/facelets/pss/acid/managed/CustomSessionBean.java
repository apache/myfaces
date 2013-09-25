/*
 * Copyright 2013 The Apache Software Foundation.
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
package org.apache.myfaces.view.facelets.pss.acid.managed;

import java.io.Serializable;
import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.SessionScoped;

/**
 *
 * @author lu4242
 */
@ManagedBean(name="customSessionBean")
@SessionScoped
public class CustomSessionBean implements Serializable
{
    
    private boolean showSection1;

    @PostConstruct
    public void init()
    {
        showSection1 = false;
    }
    
    /**
     * @return the showSection1
     */
    public boolean isShowSection1()
    {
        return showSection1;
    }

    /**
     * @param showSection1 the showSection1 to set
     */
    public void setShowSection1(boolean showSection1)
    {
        this.showSection1 = showSection1;
    }
    
}
