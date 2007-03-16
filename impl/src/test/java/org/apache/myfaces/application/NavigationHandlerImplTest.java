package org.apache.myfaces.application;

import junit.framework.Test;
import junit.framework.TestSuite;

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

	    // Return the tests included in this test case.
	    public static Test suite()
	    {
	        return (new TestSuite(NavigationHandlerImplTest.class));
	    }
}