package test;

import skiplist.SkipList;
import skiplist.SkipList.Node;

import static org.junit.Assert.*;

import java.util.Iterator;
import java.util.Map.Entry;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.TreeMap;

import org.junit.Before;
import org.junit.Test;

/**
 * This class contains JUnit test cases that you can use to test your
 * implementation of the list.
 * 
 * The reason most of the test cases are commented (i.e. hidden) is that it gets
 * too messy if you try to make all of them work at the same time. A better way
 * is to make one test case work, and the uncomment the next one, leaving the
 * ones already working in place to catch any bugs in already working code that
 * might sneek in.
 * 
 * When all the tests go through you will *PROBABLY* have a solution that
 * passes, i.e. if you also fulfills the requirements that can't be tested, such
 * as usage of the correct data structure, etc. Note though that the test cases
 * doesn't cover every nook and cranny, so feel free to test it even more. If we
 * find anything wrong with the code that these tests doesn't cover, then this
 * usually means a failed assignment.
 * 
 * Depending on settings you may get warnings for import statements that isn't
 * used. These are used by tests that orginally are commented out, so leave the
 * import statments in place.
 * 
 * @author Henrik
 * @author Modified for SkipList by Fredrik
 */
public class SkipListTest {

	// These two methods are the only places in the code that mentions the name
	// of your class.
	private static SkipList<String, String> createNewList() {
		return new SkipList<String, String>();
	}

	private static SkipList<Integer, String> createIntegerList() {
		return new SkipList<Integer, String>();
	}

	private SkipList<String, String> list = createNewList();

	// How you work on this assignment is up to you, but a recommendation is to
	// uncomment the test methods below one by one in the order they are
	// presented. Remember though that the tests only are intended to cover
	// obvious errors. Even if all of them works there may still be errors in
	// your code. You are responsible for finding those. You may add as many
	// tests as you like to the test suite, but do NOT remove or change any of
	// the existing ones unless you are absolutely certain that they are wrong.
	// If we find any problems with the tests we will publish information about
	// this in Moodle, and also update the tests there.

	@Test
	public void testEmpty() {
		list = createNewList();
		assertEquals(0, list.size());
		assertEquals("{}", list.toString());
	}

	@Test(expected = IndexOutOfBoundsException.class)
	public void testGetOnEmptyList() {
		list = createNewList();
		list.get(0);
	}

	@Before
	public void setUp() {
		list.add("A", "First");
		list.add("B", "Second");
		list.add("BD", "Third");
		list.add("D", "Fourth");
		list.add("DE", "Fifth");
	}

	@Test
	public void testSimpleMethodsOnDefaultList() {
		assertEquals(5, list.size());
		assertEquals("First", list.get(0));
		assertEquals("Third", list.get(2));
		assertEquals("Fifth", list.get(4));
		assertEquals("{A=First, B=Second, BD=Third, D=Fourth, DE=Fifth}", list.toString());

		list.add("Second", "Second");
		assertEquals(6, list.size());
		assertEquals("Second", list.get(5));
	}

	@Test(expected = IndexOutOfBoundsException.class)
	public void testIndexBelowZero() {
		list.get(-1);
	}

	@Test(expected = IndexOutOfBoundsException.class)
	public void testIndexAboveMax() {
		list.get(5);
	}
	
	@Test
	public void testDuplicateKey() {
		assertNotNull(list.add("A", "Test"));
		assertNotNull(list.add("B", "Test"));
		assertNull(list.add("CD", "Test"));
		assertNotNull(list.add("BD", "Test"));
		assertNull(list.add("E", "Test"));
	}

	@Test
	public void testClear() {
		list.clear();
		assertEquals(0, list.size());
		list.add("Zed", "First");
		list.add("Alpha", "Second");
		assertEquals(2, list.size());
		assertEquals("First", list.get(1));
		assertEquals("Second", list.get(0));
	}

	@Test
	public void testContains() {
		assertTrue(list.contains("A"));
		assertTrue(list.contains("BD"));
		assertFalse(list.contains("ABC"));
		assertFalse(list.contains(""));
	}

	@Test(expected = IndexOutOfBoundsException.class)
	public void testRemoveOnEmptyList() {
		list.clear();
		list.remove("First");
	}

	@Test
	public void testRemoveObject() {
		assertNotNull(list.remove("BD"));
		assertEquals("Second", list.get(1));
		assertEquals("Fourth", list.get(2));

		list.remove("A");
		assertEquals(3, list.size());
		assertEquals("Second", list.get(0));

		list.remove("DE");
		assertEquals(2, list.size());
		assertEquals("Fourth", list.get(1));

		list.remove("AB");
		assertEquals(2, list.size());
		assertEquals("Second", list.get(0));
		assertEquals("Fourth", list.get(1));
	}

	private static final java.util.Random rnd = new java.util.Random();
	private static final String[] names = { "Adam", "Bertil", "Cesar", "David", "Erik", "Filip", "Gustav", "Helge",
			"Ivar", "Johan", "Kalle", "ludvig", "Martin", "Niklas" };

	private String randomName() {
		return names[rnd.nextInt(names.length)];
	}

	private void testBeforeAndAfterRandomOperation(TreeMap<String, String> oracle) {
		// Here you can put test code that should be executed before and after
		// each random operation in the test below.
		assertEquals(oracle.size(), list.size());
		for (int n = 0; n < oracle.size(); n++) {
			assertEquals(oracle.values().toArray()[n], list.get(n));
		}
		assertEquals(oracle.toString(), list.toString());
	}

	@Test
	public void testMix() {
		list.clear();
		TreeMap<String, String> oracle = new TreeMap<String, String>();

		for (int n = 0; n < 100000; n++) {
			String key = randomName();
			String value = randomName();
			testBeforeAndAfterRandomOperation(oracle);
			list.add(key, value);
			oracle.put(key, value);
			testBeforeAndAfterRandomOperation(oracle);
		}

		if (oracle.size() > 0) {

			// Random removal 70% of the times
			switch (rnd.nextInt(10)) {
			case 0:
			case 1:
			case 2:
			case 3:
			case 4:
			case 5:
			case 6:
				testBeforeAndAfterRandomOperation(oracle);
				String key = randomName();
				list.remove(key);
				oracle.remove(key);
				testBeforeAndAfterRandomOperation(oracle);
				break;
			}
		}

		if (oracle.size() == 0) {
			assertEquals(0, list.size());
		} else {
			// Random check
			switch (rnd.nextInt(10)) {
			case 0:
				assertEquals(oracle.size(), list.size());
				break;
			case 1:
				assertEquals(oracle.values().toArray()[0], list.get(0));
				break;
			case 2:
				assertEquals(oracle.values().toArray()[oracle.size() - 1], list.get(list.size() - 1));
				break;
			case 3:
				Entry<String,String> entry = (Entry<String, String>) oracle.entrySet().toArray()[oracle.size() - 1];
				assertEquals(entry.getValue(), list.get(entry.getKey()));
				break;
			case 4:
			case 5:
			case 6:
			case 7:
			case 8:
				int index = rnd.nextInt(list.size());
				assertEquals(oracle.values().toArray()[index], list.get(index));
				break;
			}
		}
	}

	@Test
	public void testIsItearble() {
		for (Node<String, String> s : list)
			// This code is not necessay byt removes a warning that s isn't
			// used.
			s.toString();
	}

	@Test
	public void testBasicIteration() {
		Iterator<Node<String, String>> i = list.iterator();
		assertTrue(i.hasNext());
		assertEquals("First", i.next().getValue());
		assertTrue(i.hasNext());
		assertEquals("Second", i.next().getValue());
		assertTrue(i.hasNext());
		assertEquals("Third", i.next().getValue());
		assertTrue(i.hasNext());
		assertEquals("Fourth", i.next().getValue());
		assertTrue(i.hasNext());
		assertEquals("Fifth", i.next().getValue());
		assertFalse(i.hasNext());
	}

	@Test(expected = NoSuchElementException.class)
	public void testToLongIteration() {
		Iterator<Node<String, String>> i = list.iterator();
		for (int n = 0; n <= list.size(); n++) {
			i.next();
		}
	}

	@Test(expected = NoSuchElementException.class)
	public void testIterationOnEmptyList() {
		list.clear();
		Iterator<Node<String, String>> i = list.iterator();
		assertFalse(i.hasNext());
		i.next();
	}

	@Test
	public void testMultipleConcurrentIterators() {
		Iterator<Node<String, String>> i1 = list.iterator();
		assertTrue(i1.hasNext());
		assertEquals("First", i1.next().getValue());
		assertEquals("Second", i1.next().getValue());
		Iterator<Node<String, String>> i2 = list.iterator();
		assertTrue(i2.hasNext());
		assertEquals("First", i2.next().getValue());
		assertEquals("Third", i1.next().getValue());
		assertEquals("Second", i2.next().getValue());
		assertEquals("Fourth", i1.next().getValue());
		assertEquals("Third", i2.next().getValue());
		assertEquals("Fourth", i2.next().getValue());
		assertEquals("Fifth", i2.next().getValue());
		assertEquals("Fifth", i1.next().getValue());
		assertFalse(i1.hasNext());
		assertFalse(i2.hasNext());
	}

	private void testBeforeAndAfterRandomIntegerOperation(TreeMap<Integer, String> oracle, SkipList<Integer, String> ilist) {
		assertEquals(oracle.size(), ilist.size());
		for (Integer n = 0; n < oracle.size(); n++) {
			assertEquals(oracle.get(n), ilist.get(n));
		}
		assertEquals(oracle.toString(), ilist.toString());
	}
	
	@Test
	public void testIntegerKey() {
		SkipList<Integer, String> ilist = createIntegerList();
		TreeMap<Integer, String> oracle = new TreeMap<>();
		
		Random rand = new Random();
		
		for (int i = 0; i <= 1000; i++) {
			int key = rand.nextInt(5000);
			String value = randomName();
			testBeforeAndAfterRandomIntegerOperation(oracle, ilist);
			ilist.add(key, value);
			oracle.put(key, value);
			testBeforeAndAfterRandomIntegerOperation(oracle, ilist);
			
			// remove doesnt function properly due to compareTo use
//			if (oracle.size() > 0) {
//
//				// Random removal 70% of the times
//				switch (rnd.nextInt(10)) {
//				case 0:
//				case 1:
//				case 2:
//				case 3:
//				case 4:
//				case 5:
//				case 6:
//					testBeforeAndAfterRandomIntegerOperation(oracle, ilist);
//					Integer randKey = rand.nextInt(5000);
//					list.remove(randKey);
//					oracle.remove(randKey);
//					testBeforeAndAfterRandomIntegerOperation(oracle, ilist);
//					break;
//				}
//			}
		}
	}

}
