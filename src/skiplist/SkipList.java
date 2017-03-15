package skiplist;

import java.util.Arrays;
import java.util.Iterator;
import java.util.NoSuchElementException;
import java.util.Random;

/** SkipList implementing Iterable, implemented as a single linked list.
 * 	Does not support duplicate keys.
 * @author Fredrik
 *
 * @param <K> key
 * @param <V> value
 */
public class SkipList<K extends Comparable<K>, V> implements Iterable<SkipList.Node<K, V>>{

	/** Node used in the {@link SkipList} for Key-Value mapping and links in LinkedList-like SkipList implementation.
	 * @author Fredrik
	 *
	 * @param <K> Key
	 * @param <V> Value
	 */
	public static class Node<K extends Comparable<K>, V> implements Comparable<Node<K,V>> {
		private K ref;
		private V value;
		private Node<K,V> under;
		private Node<K,V> next;

		private Node(Node<K,V> next, K ref, Node<K,V> under, V value) {
			this.next = next;
			this.ref = ref;
			this.value = value;
			this.under = under;
		}
		
		@Override
		public Node<K,V> clone() {
			return new Node<K,V>(next, ref, under, value);
		}
		
		@Override
		public int compareTo(Node<K,V> other) {
			if (ref == null)
				return Integer.MAX_VALUE;  // tail should always be bigger
			return ref.compareTo(other.ref);
		}
		
		@Override
		public String toString() {
			if (ref == null) {
				if (next != null)
					return "head";
				return "tail";
			}
			String s = ref.toString();
			for (Node<K,V> un = under; un != null; un = un.under)
				s+= "\n" + un.ref;
			return s;
		}

		/** returns the value of the Node
		 * @return the value of the Node
		 */
		public V getValue() {
			return value;
		}
		/** returns the key of the Node
		 * @return the key of the Node
		 */
		public K getKey() {
			return ref;
		}
	}
	
	/** Multiplicator used to expand the arrays */
	private static final int ARRAY_RESIZE_MULTIPLICATOR = 2;
	
	/** size of the list */
	private int size;
	/** number of lists excluding full list */
	private int skips = 0;
	/** probability of elevating an element to upper list. 1/probability */
	private int probability = 2;
	
	/** Array of all the heads of the lists */
	private Node<K,V>[] heads;
	/** Array of all the tails of the lists */
	private Node<K,V>[] tails;

	/** Creates an empty skiplist */
	public SkipList() {
		clear();
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Iterable#iterator()
	 */
	public Iterator<Node<K, V>> iterator() {
		return new SkipListIterator();
	}
	
	/** Expands the arrays containing heads and tails to their current (size * {@link #ARRAY_RESIZE_MULTIPLICATOR multiplicator}) */
	private void expandArrays() {
		Node<K,V>[] newHeads = Arrays.copyOf(heads, heads.length*ARRAY_RESIZE_MULTIPLICATOR);
		Node<K,V>[] newTails = Arrays.copyOf(tails, tails.length*ARRAY_RESIZE_MULTIPLICATOR);
		heads = newHeads;
		tails = newTails;
	}
	
	/** removes highway lists when they are empty */
	private void cleanEmptyLists() {
		for (int i = skips; i > 0; i--) {
			if (heads[i].next == tails[i]) {
				heads[i] = null;
				tails[i] = null;
				skips--;
			}
		}
	}
	
	
	/** Adds a key, value pair to the skiplist
	 * @param key the key to identify the value
	 * @param value the value to store
	 * @return the node with the supplied key if it exists, null otherwise.
	 */
	public Node<K, V> add(K key, V value) {
		Node<K,V> toReturn = null;
		
		Node<K,V> before = findItemBefore(key);
		if (before.next.ref != null && before.next.ref.equals(key))
			toReturn = remove(key);
		
		Node<K,V> toAdd = new Node<K,V>(before.next, key, null, value);
		before.next = toAdd;
		
		promote(toAdd, 0);
		size++;
		return toReturn;
	}
	
	/** Finds the Node before the specified key
	 * @param key the key to find Node for
	 * @return the Node before the natural position of the key
	 */
	private Node<K,V> findItemBefore(K key) {
		if (size == 0)
			return heads[0];
		Node<K,V> toFindBefore = new Node<K,V>(null, key, null, null);
		Node<K,V> toReturn = null;
		
		int level = skips;
		boolean changedLevel = false;
		for (Node<K,V> current = heads[level]; current != tails[level];) {
			Node<K,V> next = current.next;
			if (next.compareTo(toFindBefore) >= 0) {
				if (level == 0)
					return current;
				current = current.under;
				level--;
				changedLevel = true;
			} 
			if(!changedLevel)
				current = current.next;
			else
				changedLevel = false;
		}
			
		return toReturn;
	}
	
	/** Determines whether the node should be elevated to a higher list and then does so.
	 * @param toPromote the node in question
	 * @param level the current level of the node
	 * @return true if the node was elevated, false if not
	 */
	private boolean promote(Node<K,V> toPromote, int level) {
		if (!shouldPromote(probability))
			return false;
		
		if (level == skips) {
			if (skips == heads.length-1)
				expandArrays();
			makeSkip();
		}
		Node<K,V> before = findBeforeInSpecificLevel(toPromote, level + 1);
		Node<K,V> promoteClone = toPromote.clone();
		promoteClone.next = before.next;
		before.next = promoteClone;
		promoteClone.under = toPromote; 
		promote(promoteClone, level + 1);
		
		return true;
	}
	
	/** 
	 * @param toFind
	 * @param level
	 * @return
	 */
	private Node<K,V> findBeforeInSpecificLevel(Node<K,V> toFind, int level) {
		Node<K,V> current = heads[level];
		for (Node<K,V> next = current.next; current != tails[level]; next = next.next) {
			if (next.compareTo(toFind) >= 0)
				break;	
			current = next;
		}
			
		return current;
	}
	
	private void makeSkip() {
		Node<K,V> tail = new Node<K,V>(null, null, tails[skips], null);
		Node<K,V> head = new Node<K,V>(tail, null, heads[skips], null);
		
		skips++;
		heads[skips] = head;
		tails[skips] = tail;
	}

	private boolean shouldPromote(int prob) {
		return new Random().nextInt(prob) == 0;
	}

	/** Removes the node with the specified value
	 * @param key of the node to be removed
	 * @return true if a node was removed, false otherwise
	 * @throws IndexOutOfBoundsException if the lists size is 0
	 * @throws ClassCastException if the key cannot be cast to K
	 */
	public Node<K,V> remove(Object key) throws ClassCastException{
		if (size == 0)
			throw new IndexOutOfBoundsException();
		Node<K,V> nodeToReturn = null;
		
		Node<K,V> tmp = new Node<K,V>(null , (K) key, null, null);
		for (int i = skips; i>=0; i--) {
			Node<K,V> before = findBeforeInSpecificLevel(tmp, i);
			if (before.next.ref != null && before.next.ref.equals(tmp.ref)) {
				if(i==0)
					nodeToReturn = before.next;
				before.next = before.next.next;
			}
		}
		if (nodeToReturn != null)
			size--;
		cleanEmptyLists();
		
		return nodeToReturn;
	}
	
	/** Returns the value at index
	 * @param index the position to return the value from
	 * @return the value of the node at index 
	 */
	public V get(int index) {
		if (index < 0 || index >= size) {
			throw new IndexOutOfBoundsException();
		}
		
		Node<K,V> current = heads[0];
		for (int i = 0; i <= index; i++) {
			current = current.next;
		}
		return current.value;
	}
	
	/** Returns the value of the specified key or null if no value that {@link Object#equals(Object) equals(key)}
	 * @param key the key to find value of
	 * @return the value of the key or null if no key is found
	 * @throws ClassCastException if the key cannot be cast to K
	 */
	public V get(Object key) throws ClassCastException {
		Node<K,V> node = findItemBefore((K) key).next;
		return node.ref.equals(key) ? node.value : null; 
	}

	/**	Checks if the SkipList contains the given key
	 * @param key the key to check if it exists
	 * @return true if the key exists, false if it doesnt
	 */
	public boolean contains(K key){
		K ref = findItemBefore(key).next.ref;
		return ref != null ? ref.equals(key) : false;
	}

	/** Clears the SkipList of all elements and sets size, skips to 0  */
	public void clear() {
		size = 0;
		skips = 0;
		heads = new Node[4];
		tails = new Node[4];
		tails[0] = new Node<>(null, null, null, null);
		heads[0] = new Node<>(tails[0], null, null, null);
	}

	/** Returns the number of key-value mappings in this SkipList
	 * @return Returns the number of key-value mappings in this SkipList
	 */
	public int size() {
		return size;
	}
	
	@Override
	public String toString() {
		String output = "{";
		
		Iterator<Node<K,V>> iter = iterator();
		if (iter.hasNext()) {
			Node<K,V> node = iter.next();
			output += node.ref + "=" + node.value;
		}
		while (iter.hasNext()) {
			Node<K,V> node = iter.next();
			output += ", "+ node.ref + "=" + node.value;
		}
		
		return output + "}";
	}

	/** Returns a string formatted so that the lists are presented in descending order, i.e. starting with the full list.
	 * @return A string formatted so that the lists are presented in descending order, i.e. starting with the full list.
	 */
	public String toStringWithExpress() {
		String output = "";
		
		for (int i = 0; i <= skips; i++) {
			output+="[";
			for (Node<K,V> n = heads[i].next; n != tails[i]; n = n.next) {
				output+= n.ref;
				if (n.next != tails[i])
					output+=", ";
			}
			output+="]\n";
		}
		
		return output;
	}

	/** Returns whether size == 0
	 * @return true if size == 0, false otherwise
	 */
	public boolean isEmpty() {
		return size == 0;
	}

	/** Iterator for {@link SkipList}. Supports some of the ListIterator methods.
	 * @author Fredrik
	 */
	public class SkipListIterator implements Iterator<Node<K,V>> {
		private Node<K,V> prev = null;
		private Node<K,V> lastReturned = heads[0];
		private int index = 0;
		
		private SkipListIterator() {}
		
		@Override
		public boolean hasNext() {
			return index != SkipList.this.size;
		}
	
		@Override
		public Node<K, V> next() {
			if (!hasNext()) {
				throw new NoSuchElementException();
			}
			
			prev = lastReturned;
			lastReturned = lastReturned.next;
			index++;
			return lastReturned;
		}
		
		public boolean hasPrevious() {
			return prev != null && prev != heads[0];
		}
	
		public int nextIndex() {
			return index;
		}
	
		public K previous() {
			if(!hasPrevious()) {
				throw new NoSuchElementException();
			}
			
			lastReturned = prev;
			prev = null;
			index--;
			return lastReturned.ref;
		}
	
		public int previousIndex() {
			return index - 1;
		}
	}
}
