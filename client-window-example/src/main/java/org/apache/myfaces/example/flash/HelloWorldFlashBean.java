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
package org.apache.myfaces.example.flash;

import javax.faces.application.FacesMessage;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.RequestScoped;
import javax.faces.context.ExternalContext;
import javax.faces.context.FacesContext;
import javax.faces.context.Flash;
import javax.faces.event.PhaseEvent;

/**
 * A typical simple backing bean, that is backed to <code>helloworld.jsp</code>
 *
 */
@ManagedBean(name="helloWorldFlashBean")
@RequestScoped
public class HelloWorldFlashBean
{

    //Log log = LogFactory.getLog(HelloWorldFlashBean.class);

    private int before = 0;

    private int after = 0;

    public void beforePhase(PhaseEvent phaseEvent)
    {

        FacesContext facesContext = FacesContext.getCurrentInstance();
        before++;
        facesContext.addMessage(null, new FacesMessage("This is the message for phase before " +
                phaseEvent.getPhaseId().toString() + " : " + before + " " +
                facesContext.getExternalContext().getFlash().get("lastName")));
        //log.info("This is the message for phase before "+phaseEvent.getPhaseId().toString()+" : "+before);
    }

    public void afterPhase(PhaseEvent phaseEvent)
    {

        FacesContext facesContext = FacesContext.getCurrentInstance();
        after++;
        facesContext.addMessage(null, new FacesMessage("This is the message for phase after " +
                phaseEvent.getPhaseId().toString() + " : " + after + " " +
                facesContext.getExternalContext().getFlash().get("lastName")));
        //log.info("This is the message for phase after "+phaseEvent.getPhaseId().toString()+" : "+after);
    }

    //properties
    private String name;

    private String lastName;

    /**
     * default empty constructor
     */
    public HelloWorldFlashBean()
    {
    }

    //-------------------getter & setter
    public String getName()
    {
        return name;
    }
    public void setName(String name)
    {
        this.name = name;
    }

    public String getLastName()
    {
        return lastName;
    }

    public void setLastName(String lastName)
    {
        this.lastName = lastName;
    }

    /**
     * Method that is backed to a submit button of a form.
     */
    public String send()
    {
        //do real logic, return a string which will be used for the navigation system of JSF

        FacesContext facesContext = FacesContext.getCurrentInstance();
        ExternalContext externalContext = facesContext.getExternalContext();
        Flash flash = externalContext.getFlash();

        flash.put("name", getName());
        flash.setKeepMessages(true);
        facesContext.addMessage(null, new FacesMessage("var name put on flash scope"));

        return "success";
    }

    public String sendPutNow()
    {
        //do real logic, return a string which will be used for the navigation system of JSF

        FacesContext facesContext = FacesContext.getCurrentInstance();
        ExternalContext externalContext = facesContext.getExternalContext();
        Flash flash = externalContext.getFlash();

        flash.putNow("name", getName());
        flash.keep("name");
        flash.setKeepMessages(true);
        facesContext.addMessage(null, new FacesMessage("var name putNow on flash scope"));

        return "success";
    }

    public String keepRedirect()
    {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        ExternalContext externalContext = facesContext.getExternalContext();
        Flash flash = externalContext.getFlash();

        flash.putNow("name", getName());
        flash.keep("name");

        return "flashKeep2.xhtml?faces-redirect=true";
    }

    public String keepPostback()
    {
        FacesContext facesContext = FacesContext.getCurrentInstance();
        ExternalContext externalContext = facesContext.getExternalContext();
        Flash flash = externalContext.getFlash();

        flash.keep("name"); // does not work here anymore

        return "flashKeep3.xhtml";
    }


    public String back()
    {
        return "back";
    }
}