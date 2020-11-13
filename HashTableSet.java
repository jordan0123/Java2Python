import java.util.Stack;
import java.util.HashMap;
import java.util.Iterator;

import java.util.ArrayList;

public class HashTableSet<E> {
    private ArrayList<HashMap<E, Integer>> hashTable;
    private Stack<E> keyStack;

    private int currentScope = 0;

    public HashTableSet() {
        hashTable = new ArrayList<HashMap<E, Integer>>();
        hashTable.add(new HashMap<E, Integer>());
        keyStack = new Stack<E>();
    }

    void increaseScope() {
        hashTable.add(new HashMap<E, Integer>());
        currentScope += 1;
    }

    void decreaseScope() {
        if (currentScope > 0) {
            hashTable.remove(currentScope);
            currentScope -= 1;
        } else currentScope = 0;
    }

    public E peek() { return keyStack.peek(); }

    public int get(E key, int index) {
        if (hashTable.get(index).containsKey(key)) {
            return hashTable.get(index).get(key);
        } else return 0;
    }

    public int getGlobal(E key) {
        return get(key, 0);
    }

    public boolean add(E key, int index) {
        if (index < 0) index = currentScope + index;

        boolean changed = false;
        HashMap<E, Integer> scope = hashTable.get(index);

        if (scope.containsKey(key)) {
            scope.put(key, scope.get(key) + 1);
            hashTable.set(index, scope);
        } else {
            scope.put(key, 1);
            hashTable.set(index, scope);
            changed = true;
        }

        return changed;
    }

    public boolean add(E key) { return add(key, currentScope); }
    public boolean addGlobal(E key) { return add(key, 0); }

    public boolean addStack(E key) {
        keyStack.push(key);
        return add(key);
    }

    public boolean removeSelective(E key, int index) {
        boolean changed = false;
        HashMap<E, Integer> scope = hashTable.get(index);

        if (scope.containsKey(key)) {
            scope.put(key, scope.get(key) - 1);

            if (scope.get(key) == 0) {
                scope.remove(key);
                changed = true;
            }

            hashTable.set(index, scope);
        }

        return changed;
    }

    public boolean remove(E key, int index) {
        if (index < 0) { index = currentScope + index; }
        boolean changed = false;
        HashMap<E, Integer> scope = hashTable.get(index);

        if (scope.containsKey(key)) {
            removeSelective(key, index);
        } else if (index > 0) {
            return remove(key, index - 1);
        }

        return changed;
    }

    public boolean remove(E key) { return remove(key, currentScope); }

    public boolean removeStack(E key) {
        // NOTE: bound to cause stack ordering issues in the future
        keyStack.pop();
        return remove(key);
    }

    public boolean clear(E key, int index) {
        if (index < 0) { index = currentScope + index; }
        return hashTable.get(index).remove(key) != null;
    }

    // remove key from table regardless of its score
    public boolean clear(E key) {
        boolean changed = false;

        for (int i = 0; i < hashTable.size(); i++) {
            changed = this.clear(key, i) || changed;
        }

        return changed;
    }

    // remove all keys regardless of their score
    public boolean clear() {
        for (int i = 0; i < hashTable.size(); i++) {
            hashTable.get(i).clear();
        }

        return true;
    }

    public boolean clearCurrent(E key) { return clear(key, currentScope); }

    public boolean contains(E key, int index) {
        return hashTable.get(index).containsKey(key);
    }

    public boolean contains(E key) {
        boolean contains = false;

        for (int i = 0; !contains && i < hashTable.size(); i++) {
            contains = contains(key, i) || contains;
        }

        return contains;
    }

    public boolean containsCurrent(E key) { return contains(key, currentScope); }

    public int size() {
        return hashTable.size();
    }
}