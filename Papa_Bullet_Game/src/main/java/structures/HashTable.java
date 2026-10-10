package structures;

/**
 * Tabla hash propia con encadenamiento: cada casilla del arreglo guarda una lista enlazada
 * de los pares (clave, valor) cuyas claves caen en esa casilla.
 * Cuando hay más de 0.75 elementos por casilla, el arreglo se duplica y todo se reubica
 * (así las listas siguen cortas y get/put son O(1) en promedio).
 * Acepta valores null (Sprites guarda null para "esa imagen no existe"); las claves no pueden ser null.
 */
public class HashTable<K, V> {

    private static final int INITIAL_CAPACITY = 16;
    private static final double MAX_LOAD = 0.75;

    /** Un par clave-valor y el siguiente de la misma casilla. */
    private static class Node<K, V> {
        final K key;
        V value;
        Node<K, V> next;

        Node(K key, V value, Node<K, V> next) {
            this.key = key;
            this.value = value;
            this.next = next;
        }
    }

    private Node<K, V>[] buckets;
    private int size;

    public HashTable() {
        buckets = newArray(INITIAL_CAPACITY);
    }

    /** Guarda el valor; si la clave ya estaba, lo reemplaza y devuelve el anterior. */
    public V put(K key, V value) {
        if (key == null) throw new IllegalArgumentException("La clave no puede ser null");
        Node<K, V> n = find(key);
        if (n != null) {
            V old = n.value;
            n.value = value;
            return old;
        }
        if (size + 1 > buckets.length * MAX_LOAD) resize();
        int i = indexOf(key, buckets.length);
        buckets[i] = new Node<>(key, value, buckets[i]);
        size++;
        return null;
    }

    public V get(K key) {
        Node<K, V> n = find(key);
        return n == null ? null : n.value;
    }

    public V getOrDefault(K key, V defaultValue) {
        Node<K, V> n = find(key);
        return n == null ? defaultValue : n.value;
    }

    /** Distingue "no está" de "está con valor null". */
    public boolean containsKey(K key) {
        return find(key) != null;
    }

    /** Quita la clave y devuelve su valor (null si no estaba). */
    public V remove(K key) {
        if (key == null) return null;
        int i = indexOf(key, buckets.length);
        Node<K, V> prev = null;
        for (Node<K, V> n = buckets[i]; n != null; prev = n, n = n.next) {
            if (n.key.equals(key)) {
                if (prev == null) buckets[i] = n.next;
                else prev.next = n.next;
                size--;
                return n.value;
            }
        }
        return null;
    }

    public void clear() {
        buckets = newArray(INITIAL_CAPACITY);
        size = 0;
    }

    public int size() { return size; }

    public boolean isEmpty() { return size == 0; }

    // ---------------- internos ----------------

    private Node<K, V> find(K key) {
        if (key == null) return null;
        for (Node<K, V> n = buckets[indexOf(key, buckets.length)]; n != null; n = n.next) {
            if (n.key.equals(key)) return n;
        }
        return null;
    }

    /** Casilla de la clave: el hashCode puede ser negativo, floorMod lo deja en [0, length). */
    private static int indexOf(Object key, int length) {
        return Math.floorMod(key.hashCode(), length);
    }

    private void resize() {
        Node<K, V>[] old = buckets;
        buckets = newArray(old.length * 2);
        for (Node<K, V> head : old) {
            for (Node<K, V> n = head; n != null; ) {
                Node<K, V> next = n.next;
                int i = indexOf(n.key, buckets.length);
                n.next = buckets[i];
                buckets[i] = n;
                n = next;
            }
        }
    }

    @SuppressWarnings("unchecked")
    private static <K, V> Node<K, V>[] newArray(int capacity) {
        return (Node<K, V>[]) new Node[capacity];
    }
}