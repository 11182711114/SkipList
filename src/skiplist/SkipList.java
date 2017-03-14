package skiplist;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.NoSuchElementException;
import java.util.Random;

public class SkipList<T extends Comparable<T>> implements List<T> {
	
	public static void main(String[] args) {
		SkipList<String> list = new SkipList<String>();
		
		list.add("2");
		list.add("3");
		list.add("1");
		
		for (String s : list)
			System.out.println(s);
	}

	private static class Node<T extends Comparable<T>> implements Comparable<Node<T>> {
		T ref;
		Node<T> under;
		Node<T> next;

		Node(Node<T> next, T ref, Node<T> under) {
			this.next = next;
			this.ref = ref;
			this.under = under;
		}
		
		public Node<T> clone() {
			return new Node(next, ref, under);
		}

		@Override
		public int compareTo(Node<T> other) {
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
			for (Node<T> un = under; under != null; under = under.under)
				s+= "\n" + un.ref;
			return s;
		}
	}
	
	private static final int ARRAY_RESIZE_MULTIPLICATOR = 2;
	
	private int size;
	private int skips = 0; // number of lists excluding full list
	private int probability = 1;  // 1/prob 
	
	private Node<T>[] heads;
	private Node<T>[] tails;

	public SkipList() {
		size = 0;
		heads = new Node[4];
		tails = new Node[4];
		tails[0] = new Node<>(null, null, null);
		heads[0] = new Node<>(tails[0], null, null);
	}
	
	@Override
	public Iterator<T> iterator() {
		return new SkipListIterator();
	}
	
	private void expandArrays() {
		Node<T>[] newHeads = Arrays.copyOf(heads, heads.length*ARRAY_RESIZE_MULTIPLICATOR);
		Node<T>[] newTails = Arrays.copyOf(tails, tails.length*ARRAY_RESIZE_MULTIPLICATOR);
		heads = newHeads;
		tails = newTails;
	}
	
	
	@Override
	public boolean add(T element) {
		Node<T> before = findItemBefore(element);
		Node<T> toAdd = new Node<T>(before.next, element, null);
		before.next = toAdd;
		
		promote(toAdd, 0);
		size++;
		return true;
	}
	
	public boolean insert(T element) {
		return false;
		
	}
	
	private Node<T> findItemBefore(T element) {
		if (size == 0)
			return heads[0];
		Node<T> toFindBefore = new Node<T>(null, element, null);
		Node<T> toReturn = null;
		
		int level = skips;
		for (Node<T> current = heads[skips]; current.next.next != tails[level]; current = current.next) {
			Node<T> next = current.next;
			if (next.compareTo(toFindBefore) > 0) {
				if (level == 0)
					return current;
				current = current.under;
				level--;
			} else {
				current = next;
			}
		}
			
		return toReturn;
	}
	
	private boolean promote(Node<T> toPromote, int level) {
		if (!shouldPromote(probability))
			return false;
		
		if (level == skips) {
			if (skips == heads.length)
				expandArrays();
			makeSkip();
		}
		Node<T> before = findBeforeInSpecificLevel(toPromote, level + 1);
		Node<T> promoteClone = toPromote.clone();
		promoteClone.next = before.next;
		before.next = promoteClone;
		promoteClone.under = toPromote;
		Node<T> test = promoteClone;
		promote(test, level + 1);
		
		return true;
	}
	
	private Node<T> findBeforeInSpecificLevel(Node<T> toFind, int level) {
//		if (!(skips <= level))
//			return null;
		Node<T> current = heads[level];
		for (Node<T> next = current.next; next != tails[level]; next = next.next) {
			if (next.compareTo(toFind) > 0)
				break;			
		}
			
		return current;
	}
	
	private void makeSkip() {
		Node<T> tail = new Node<T>(null, null, tails[skips]);
		Node<T> head = new Node<T>(tail, null, heads[skips]);
		
		skips++;
		heads[skips] = head;
		tails[skips] = tail;
	}

	private void linkAfter(Node<T> before, Node<T> toLink) {
		Node<T> after = before.next;
		toLink.next = after;
		before.next = toLink;
	}
	
	private boolean shouldPromote(int prob) {
		return new Random().nextInt(prob) == 0;
	}

	@Override
	public void add(int index, T element) {
		if (index < 0 || index > size) {
			throw new IndexOutOfBoundsException();
		} else if (index == size) {
			add(element);
		} else {
			
			Node<T> current = head;
			for (int i = -1; i < index; i++) {
				if (i == index - 1) {
					Node<T> before = current;
					Node<T> after = current.next;
					before.next = new Node<T>(after, element);
					break;
				}
				current = current.next;
			}
			
			size++;
		}
	}

	@Override
	public T remove(int index) {
		if (!(index < 0 || index >= size)){
			Node<T> current = head;
			for (int i = -1; i < index; i++) {
				if (i == index - 1) {
					T toReturn = current.next.ref;
					removeNextNode(current);
					return toReturn;
				}
				current = current.next;		
			}
		}
		throw new IndexOutOfBoundsException();
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean remove(Object element) throws ClassCastException{
		Node<T> nodeBefore = findNodeBeforeElement((T) element);
		if (nodeBefore != null) {
			removeNextNode(nodeBefore);
			return true;
		}
		return false;
	}

	private void removeNextNode(Node<T> beforeRefNode) {
		Node<T> after = beforeRefNode.next.next;
		beforeRefNode.next = after;
		size--;
	}
	
	private Node<T> findNodeBeforeElement(T element) {
		Node<T> current = head;
		for (int i = -1; i < size - 1; i++) {
			if (current.next.ref.equals(element))
				return current;
			current = current.next;
		}
		
		return null;
	}
	
	@Override
	public T get(int index) {
		if (index < 0 || index >= size) {
			throw new IndexOutOfBoundsException();
		}
		
		Node<T> current = head.next;
		for (int i = 0; i <= index; i++) {
			if (i == index) {
				break;
			}
			current = current.next;
		}
		return current.ref;
	}

	@SuppressWarnings("unchecked")
	@Override
	public boolean contains(Object element) throws ClassCastException{
		return indexOf((T) element) != -1;
	}

	@SuppressWarnings("unchecked")
	@Override
	public int indexOf(Object element) throws ClassCastException{
		if(size == 0)
			return -1;
		
		Node<T> current = head.next;
		for (int i = 0; i < size; i++) {
			if (current.ref.equals((T)element)) {
				return i;
			}
			current = current.next;
		}
		
		return -1;
	}

	@Override
	public void clear() {
		head.next = tail;
		size = 0;
	}

	@Override
	public int size() {
		return size;
	}

	@Override
	public String toString() {
		String output = "[";
		
		Iterator<T> iter = iterator();
		if (iter.hasNext()) {
			output += iter.next().toString();
		}
		while (iter.hasNext()) {
			output += ", " + iter.next().toString();
		}
		
		return output + "]";
	}

	private class SkipListIterator implements ListIterator<T> {
		Node<T> prev = null;
		Node<T> lastReturned = heads[0];
		boolean removeActive = false;
		int index = 0;
		public SkipListIterator() {}
		
		public SkipListIterator(int i) {
			if (i >= 0 && i <= SkipList.this.size)
			while (index < i)
				next();
		}
		
		@Override
		public boolean hasNext() {
			return index != SkipList.this.size;
		}
	
		@Override
		public T next() {
			if (!hasNext()) {
				throw new NoSuchElementException();
			}
			
			prev = lastReturned;
			lastReturned = lastReturned.next;
			removeActive = true;
			index++;
			return lastReturned.ref;
		}
	
		@Override
		public void remove() {
			if (!removeActive) {
				throw new IllegalStateException();
			}
			
			SkipList.this.removeNextNode(prev);
			index--;
			removeActive = false;
		}
	
		@Override
		public void add(T element) {
			Node<T> after = lastReturned.next;
			lastReturned.next = new Node<T>(after,element);
			SkipList.this.size++;
			index++;
		}
		
		@Override
		public boolean hasPrevious() {
			return prev != null && prev != head;
		}
	
		@Override
		public int nextIndex() {
			return index;
		}
	
		@Override
		public T previous() {
			if(!hasPrevious()) {
				throw new NoSuchElementException();
			}
			
			lastReturned = prev;
			prev = null;
			index--;
			return lastReturned.ref;
		}
	
		@Override
		public int previousIndex() {
			return index - 1;
		}
	
		@Override
		public void set(T element) {
			if(!removeActive) {
				throw new NoSuchElementException();
			}
			
			lastReturned.ref = element;
		}
	}

	@Override
	public boolean containsAll(Collection<?> c) throws ClassCastException{
		for (Object item : c) {
			if (!contains(item))
				return false;
		}
		return true;
	}

	@Override
	public boolean isEmpty() {
		return size == 0;
	}

	@Override
	public int lastIndexOf(Object o) {
		return 0;
	}

	@Override
	public ListIterator<T> listIterator() {
		return (ListIterator<T>) iterator();
	}

	@Override
	public ListIterator<T> listIterator(int index) {
		return new SkipListIterator(index);
	}

	@Override
	public List<T> subList(int fromIndex, int toIndex) {
		return null;
	}

	@Override
	public Object[] toArray() {
		Object[] objs = new Object[size];
		for (ListIterator<T> it = listIterator(); it.hasNext();)
			objs[it.nextIndex()] = it.next();
		return objs;
	}

	@Override
	public <T> T[] toArray(T[] a) {
		return null;
	}

	@Override
	public boolean removeAll(Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean retainAll(Collection<?> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public T set(int index, T element) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean addAll(Collection<? extends T> c) {
		throw new UnsupportedOperationException();
	}

	@Override
	public boolean addAll(int index, Collection<? extends T> c) {
		throw new UnsupportedOperationException();
	}

}
