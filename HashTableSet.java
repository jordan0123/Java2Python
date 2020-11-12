import java.util.Stack;
import java.util.HashMap;
import java.util.Iterator;

public class HashTableSet<E> {
    private HashMap<E, Integer> hashTable;
    private Stack<E> keyStack;

    public HashTableSet() {
        hashTable = new HashMap<E, Integer>();
        keyStack = new Stack<E>();
    }

    public E peek() { return keyStack.peek(); }

    public boolean add(E key) {
        boolean changed = false;

        if (hashTable.containsKey(key)) hashTable.put(key, hashTable.get(key) + 1);
        else {
            hashTable.put(key, 1);
            changed = true;
        }

        return changed;
    }

    public boolean addStack(E key) {
        keyStack.push(key);
        return add(key);
    }

    public boolean remove(E key) {
        boolean changed = false;

        if (hashTable.containsKey(key)) {
            hashTable.put(key, hashTable.get(key) - 1);
            if (hashTable.get(key) == 0) {
                hashTable.remove(key);
                changed = true;
            }
        }

        return changed;
    }

    public boolean removeStack(E key) {
        // NOTE: bound to cause stack ordering issues in the future
        keyStack.pop();
        return remove(key);
    }

    // remove key from table regardless of its score
    public boolean clear(E key) {
        return (hashTable.remove(key) > 0);
    }

    // remove all keys regardless of their score
    public boolean clear() {
        hashTable.clear();
        return true;
    }

    public boolean contains(E key) {
        return hashTable.containsKey(key);
    }

    public int size() {
        return hashTable.size();
    }

    public Iterator<E> iterator() {
        return hashTable.keySet().iterator();
    }
}