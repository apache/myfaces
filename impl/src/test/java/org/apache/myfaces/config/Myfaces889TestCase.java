package org.apache.myfaces.config;

/**
 * http://issues.apache.org/jira/browse/MYFACES-889?page=all
 * 
 * @author Dennis C. Byrne
 */

import java.util.List;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

public class Myfaces889TestCase extends AbstractManagedBeanBuilderTestCase {

	public Myfaces889TestCase(String name) {
		super(name);
	}

	private static Log log = LogFactory.getLog(Myfaces889TestCase.class);
	
	public void testWriteOnlyMap(){
		assertTrue(example != null);
		log.debug("managed bean successfully created");
		
		Map writeOnlyMap = example.getHiddenWriteOnlyMap();
		
		assertTrue(writeOnlyMap != null);
		log.debug("managed map is not null");
		
		scrutinizeMap(writeOnlyMap);
	}
	
	public void testManagedMap(){
		assertTrue(example != null);
		log.debug("managed bean successfully created");
		
		Map managedMap = example.getManagedMap();
		
		assertTrue(managedMap != null);
		log.debug("managed map is not null");
		
		scrutinizeMap(managedMap);
	}
	
	private void scrutinizeMap(Map map){
		assertTrue(map.size() == 3);
		log.debug("managed map has the correct size " + map.size());
		
		for(int i = 0; i < map.size(); i++){
			String entry = (String) map.get(i + "");
			String config = (String) MANAGED_MAP.get(i + "");
			log.debug("looking @ " + config + " and " + entry);
			assertTrue(config.equals(entry));
		}
		
	}
	
	public void testManagedList(){
		assertTrue(example != null);
		log.debug("managed bean successfully created");
		
		List managedList = example.getManagedList();
		
		scrutinizeList(managedList);
	}
	
	public void testWriteOnlyList(){
		assertTrue(example != null);
		log.debug("managed bean successfully created");
		
		List writeOnlyList = example.getHiddenWriteOnlyList();
		
		scrutinizeList(writeOnlyList);
	}
	
	private void scrutinizeList(List list){
		assertTrue(list != null);
		log.debug("managed list is not null " + list.size());
		assertTrue(list.size() == 3);
		log.debug("managed list has the correct size " + list.size());
		
		for(int i = 0 ; i < list.size(); i++){
			String entry = (String) list.get(i);
			String config = (String) MANAGED_LIST.get(i);
			log.debug("looking @ " + config + " and " + entry);
			assertTrue(config.equals(entry));
		}
	}
	
	public void testManagedProperty(){
		assertTrue(example != null);
		log.debug("managed bean successfully created");
		
		String managedPropertyValue = example.getManagedProperty();
		
		assertTrue(INJECTED_VALUE.equals(managedPropertyValue));
		log.debug("managed property String has the correct value ");
	}
	
}
