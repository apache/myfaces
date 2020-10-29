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
package org.apache.myfaces.view.facelets.pss.acid.managed;

import java.util.ArrayList;
import java.util.List;
import javax.enterprise.context.RequestScoped;

import jakarta.faces.event.ActionEvent;
import javax.inject.Named;

@Named("testManagedBean")
@RequestScoped
public class ProbeManagedBean {

	private static final List<String>	LIST;

	static {

		LIST = new ArrayList<String>();
		LIST.add( "Foo" );
		LIST.add( "Bar" );
		LIST.add( "Baz" );
	}

	public List<String> getList() {

		return LIST;
	}
	
	private String page = "page1.xhtml";
	
	public String getPage()
	{
	    return page;
	}
	
	public void setPage(String page)
	{
	    this.page = page;
	}
	
	public void page1(ActionEvent evt)
	{
	    setPage("page1.xhtml");
	}
	
    public void page2(ActionEvent evt)
    {
        setPage("page2.xhtml");
    }

	public void save() {

		// Do nothing. Just a way to POSTback
	}
}
