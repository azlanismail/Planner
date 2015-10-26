/**
 * To simulate the idea of passing a set of pair values to my prism model / properties
 */

package synthesis;

import java.util.HashMap;
import java.util.Iterator;

public class HashMapTest {

	public static void main(String[] args) {
		// TODO Auto-generated method stub

		HashMap<String, Integer> cache = new HashMap<String, Integer>();
		cache.put("Twenty One",21);
		cache.put("Twenty Two",22);
		
		
		Iterator<String> keySetIterator = cache.keySet().iterator();

		while(keySetIterator.hasNext()){
		  String key = keySetIterator.next();
		  System.out.println("key: " + key + " value: " + cache.get(key));
		}
		
	}

}
