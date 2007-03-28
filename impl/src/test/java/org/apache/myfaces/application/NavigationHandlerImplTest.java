package org.apache.myfaces.application;

import org.apache.shale.test.base.AbstractJsfTestCase;

public class NavigationHandlerImplTest extends AbstractJsfTestCase
{

	    public static void main(String[] args) {
	        junit.textui.TestRunner.run(NavigationHandlerImplTest.class);
	    }

	    public NavigationHandlerImplTest(String name) {
	        super(name);
	    }
	    
	    public void testNavigationRules() throws Exception
	    {
	    	NavigationHandlerImpl nh = new NavigationHandlerImpl();
	    	this.application.setNavigationHandler(nh);
	    }
}