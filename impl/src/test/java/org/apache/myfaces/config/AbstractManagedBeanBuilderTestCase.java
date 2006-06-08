package org.apache.myfaces.config;

/**
 * Creates an environment to easily test the creation and initialization of
 * managed beans.
 * 
 * @author Dennis C. Byrne
 */

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.faces.application.Application;

import junit.framework.Test;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.myfaces.application.ApplicationImpl;
import org.apache.myfaces.config.impl.digester.elements.ListEntries;
import org.apache.myfaces.config.impl.digester.elements.ManagedBean;
import org.apache.myfaces.config.impl.digester.elements.ManagedProperty;
import org.apache.myfaces.config.impl.digester.elements.MapEntries;
import org.apache.myfaces.config.impl.digester.elements.ListEntries.Entry;
import org.apache.myfaces.el.PropertyResolverImpl;
import org.apache.shale.test.base.AbstractJsfTestCase;
import org.apache.shale.test.mock.MockFacesContext;

public abstract class AbstractManagedBeanBuilderTestCase extends AbstractJsfTestCase {

	public AbstractManagedBeanBuilderTestCase(String name) {
		super(name);
	}

	private static Log log = LogFactory.getLog(AbstractManagedBeanBuilderTestCase.class);
	protected MangedBeanExample example;
	
	// managed property values
	protected static final List MANAGED_LIST = new ArrayList();
	protected static final Map MANAGED_MAP = new HashMap();
	protected static final String INJECTED_VALUE = "tatiana";
	
    public static Test suite() {
        return null; // keep this method or maven won't run it
    }
	
	/**
	 * Skips digester and manually builds and configures a managed bean.
	 */
	
	public void setUp(){
		super.setUp();
		ManagedBeanBuilder managedBeanBuilder = new ManagedBeanBuilder();
		ManagedBean managedBean = new ManagedBean();
		
		managedBean.setBeanClass(MangedBeanExample.class.getName());
		managedBean.setName("managed");
		managedBean.setScope("request");
		
		// test methods of children will want to make sure these values come 
		// out on the other end of this.
		MANAGED_LIST.add("0");
		MANAGED_LIST.add("1");
		MANAGED_LIST.add("2");
		MANAGED_MAP.put("0", "0");
		MANAGED_MAP.put("1", "1");
		MANAGED_MAP.put("2", "2");
		
		ManagedProperty managedProperty = new ManagedProperty();
		managedProperty.setPropertyName("managedProperty");
		managedProperty.setValue(INJECTED_VALUE);
		
		ManagedProperty managedList = new ManagedProperty();
		managedList.setPropertyName("managedList");
		ListEntries listEntries = makeListEntries();
		managedList.setListEntries(listEntries);
		
		ManagedProperty writeOnlyList = new ManagedProperty();
		writeOnlyList.setPropertyName("writeOnlyList");
		ListEntries writeOnlyListEntries = makeListEntries();
		writeOnlyList.setListEntries(writeOnlyListEntries);
		
		ManagedProperty managedMap = new ManagedProperty();
		managedMap.setPropertyName("managedMap");
		MapEntries mapEntries = makeMapEntries();
		managedMap.setMapEntries(mapEntries);
		
		ManagedProperty writeOnlyMap = new ManagedProperty();
		writeOnlyMap.setPropertyName("writeOnlyMap");
		MapEntries writeOnlyMapEntries = makeMapEntries();
		writeOnlyMap.setMapEntries(writeOnlyMapEntries);		
		
		managedBean.addProperty(managedProperty);
		managedBean.addProperty(managedList);
		managedBean.addProperty(writeOnlyList);
		managedBean.addProperty(managedMap);
		managedBean.addProperty(writeOnlyMap);

		// provide the minimal environment 
		Application application = null;
		application = new ApplicationImpl();
		application.setPropertyResolver(new PropertyResolverImpl());
		MockFacesContext facesContext = new MockFacesContext();
		facesContext.setApplication(application);
		
		// simulate a managed bean creation
		example = (MangedBeanExample) managedBeanBuilder
			.buildManagedBean(facesContext, managedBean);
	}
	
	public void tearDown() {
		example = null;
		MANAGED_LIST.clear();
		MANAGED_MAP.clear();
	}
	
	private ListEntries makeListEntries(){
		ListEntries listEntries = new ListEntries();
		
		for(int i = 0; i < MANAGED_LIST.size(); i++){
			Entry entry = new Entry();
			entry.setValue((String) MANAGED_LIST.get(i));
			listEntries.addEntry(entry);
		}
		return listEntries;
	}
	
	private MapEntries makeMapEntries(){
		MapEntries mapEntries = new MapEntries();
		
		for(int i = 0 ; i < MANAGED_MAP.size(); i++){
			MapEntries.Entry mapEntry = new MapEntries.Entry();
			mapEntry.setKey((String) MANAGED_MAP.get(i + ""));
			mapEntry.setValue((String) MANAGED_MAP.get(i + ""));
			mapEntries.addEntry(mapEntry);
		}
		return mapEntries;
	}
	
}
