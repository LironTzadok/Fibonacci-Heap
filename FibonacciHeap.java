//username - liront
//id1      - 208005835
//name1    - Liron Tzadok
// id2      - 208634766
//name2    - Tal Ben Tov
import java.lang.Math;

/**
 * FibonacciHeap
 *
 * An implementation of a Fibonacci Heap over integers.
 */
public class FibonacciHeap
{
    private HeapNode min_node;
    private HeapNode first;
    private int size;
    private int count_roots;
    private int count_marked;
    private static int count_total_links = 0;
    private static int count_total_cuts = 0;

    public FibonacciHeap(){
        this.min_node = null;
        this.first = null;
        this.size = 0;
        this.count_roots = 0;
        this.count_marked=0;
    }

    /**
     * public HeapNode getMinNode()
     *
     * Returns the min root in the roots list of the heap
     */
    public HeapNode getMinNode() {
        return this.min_node;
    }

    /**
     * public HeapNode getFirst()
     *
     * Returns the first root in the roots list of the heap
     */
    public HeapNode getFirst() {
        return this.first;
    }

    /**
     * public int getRootsNum()
     *
     * returns the number of roots in the heap
     */
    public int getRootsNum() {
        return this.count_roots;
    }

    /**
     * public int getMarkedNum()
     *
     * returns the number of marked nodes in the heap
     */
    public int getMarkedNum(){
        return this.count_marked;
    }

    /**
     * public boolean isEmpty()
     *
     * Returns true if and only if the heap is empty.
     */
    public boolean isEmpty()
    {
        return this.size == 0;
    }

    /**
     * public HeapNode insert(int key)
     *
     * Creates a node (of type HeapNode) which contains the given key, and inserts it into the heap.
     * The added key is assumed not to already belong to the heap.
     *
     * Returns the newly created node.
     *
     * time complexity: O(1)
     */
    public HeapNode insert(int key)
    {
        HeapNode new_heap_node = new HeapNode(key);
        if (this.isEmpty()) {
            this.min_node = new_heap_node;
            new_heap_node.setNext(new_heap_node);
        }
        else {
            this.first.getPrev().setNext(new_heap_node);
            new_heap_node.setNext(this.first);
            this.findNewMin(new_heap_node);
        }
        this.first = new_heap_node;
        this.size += 1;
        this.count_roots += 1;
        return new_heap_node;
    }

    /**
     * public HeapNode insert(HeapNode node)
     *
     * an override method insert that inserts a node to the heap and updates
     his "child_pointer_for_kMin_method" field to node.child.
     *
     * this method is used for kMin method only!
     *
     * time complexity: O(1)
     */

    public HeapNode insert (HeapNode node) {
        this.insert(node.getKey());
        this.first.child_pointer_for_kMin_method = node.get_child_pointer_for_kMin_method();
        return this.first;
    }

    /**
     * public void deleteMin()
     *
     * Deletes the node containing the minimum key.
     *
     * time complexity: WC - O(n), amortized - O(log n)
     */
    public void deleteMin() {
        if (this.isEmpty()) {
            return;
        }
        HeapNode min = this.min_node;
        HeapNode child = min.getChild();
        HeapNode next = min.getNext();
        HeapNode prev = min.getPrev();

        // if min_node has children, they become roots
        if (min.getRank() != 0) {
            // if min_node is the only root in the heap, it's children become the only roots of the heap
            if (this.count_roots == 1) {
                this.first = child;
            }
            // min_node is not the only root in the heap, then it's children replace it in the list of roots of the heap
            else {
                child.getPrev().setNext(next);
                prev.setNext(child);
                if (min == this.first) {
                    this.first = child;
                }
            }
            // changing "mark" field of the deleted node's children to false and changing their "parent" field to null
            HeapNode child_for_loop = child;
            for (int i = 1; i <= min.getRank(); i++){
                setHeapNodeMarked(child_for_loop, false);
                child_for_loop.setParent(null);
                child_for_loop = child_for_loop.getNext();
            }
        }
        // min node rank is 0
        else {
            // the deleted root is the last one in the heap
            if (this.count_roots == 1) {
                this.first = null;
            }
            // connect deleted root siblings
            else {
                prev.setNext(next);
                if (min == this.first) {
                    this.first = next;
                }
            }
        }
        // set deleted node child to be null
        min.setChild(null);
        // detach deleted node siblings
        min.setNext(null);
        min.setPrev(null);
        // update counter fields
        this.count_roots += min.getRank() - 1;
        this.size -= 1;
        // set new min node and do successive linking
        this.min_node = SuccessiveLinking();
    }

    /**
     * private HeapNode SuccessiveLinking()
     *
     * goes throw all heap roots and links every two trees that have the same rank.
     * when a tree doesn't have a tree to be linked to, it stays in buckets[i] when i = rank.
     *
     * returns the new min_node of the heap after all the links.
     *
     * time complexity: WC O(n), amortized O(log n)
     */
    private HeapNode SuccessiveLinking() {
        int log_n = (int)(Math.ceil(Math.log(this.size + 1) / Math.log(2) + 1));
        int count_roots_copy = this.count_roots;
        HeapNode[] buckets = new HeapNode[log_n];
        HeapNode node = this.first;
        // if heap is empty
        if (node == null) {
            return null;
        }
        HeapNode tmp = node.getNext();
        int rank;
        HeapNode node_after_link;
        while (count_roots_copy > 0) {
            rank = node.getRank();
            // if node's rank bucket is empty
            if(buckets[rank] == null){
                buckets[rank] = node;
                node = tmp;
                tmp = tmp.getNext();
                count_roots_copy --;
            }
            else {
                node_after_link = link(node, buckets[rank]);
                buckets[rank] = null;
                node = node_after_link;
            }
        }
        HeapNode new_min = createHeapAndFindMin(buckets);
        return new_min;
    }

    /**
     * private HeapNode createHeapAndFindMin(HeapNode[] buckets)
     *
     * goes throw buckets array and creats a new heap.
     *
     * returns the node with minimal key in the array
     *
     * time complexity: O(log n)
     */
    private HeapNode createHeapAndFindMin(HeapNode[] buckets) {
        this.first = null;
        this.count_roots = 0;
        HeapNode min = null;
        HeapNode last_added_tree = null;
        for (int i = 0; i < buckets.length; i++) {
            if (buckets[i] != null) {
                this.count_roots += 1;
                // if "first" filed hasn't been initialized
                if (this.first == null) {
                    this.first = buckets[i];
                    last_added_tree = this.first;
                    min = this.first;
                }
                else {
                    last_added_tree.setNext(buckets[i]);
                    if(buckets[i].getKey() < min.getKey()) {
                        min = buckets[i];
                    }
                    last_added_tree = buckets[i];
                }
            }
        }
        if (last_added_tree != null) {
            last_added_tree.setNext(this.first);
        }
        return min;
    }

    /**
     * private HeapNode link(HeapNode node1, HeapNode node2)
     *
     * given to trees of the same rank k, this function links them to a one tree of rank k + 1.
     * the root of the new tree will be the node with the smaller key between both of the roots.
     *
     * returns the root of the new linked tree
     *
     * time complexity: O(1)
     */
    private HeapNode link(HeapNode node1, HeapNode node2){
        // make sure both trees are the same rank
        if(node1.getRank() != node2.getRank()) {
            return null;
        }
        HeapNode root;
        HeapNode left_child;
        if(node1.getKey() < node2.getKey()) {
            root = node1;
            left_child = node2;
        }
        else {
            root = node2;
            left_child = node1;
        }
        if (root.getRank() > 0) {
            root.getChild().getPrev().setNext(left_child);
            left_child.setNext(root.getChild());
        }
        else {
            left_child.setNext(left_child);
        }
        root.setChild(left_child);
        left_child.setParent(root);
        root.setRank(root.getRank() + 1);
        count_total_links++;
        return root;
    }

    /**
     * public HeapNode findMin()
     *
     * Returns the node of the heap whose key is minimal, or null if the heap is empty.
     *
     * time complexity: O(1)
     */
    public HeapNode findMin()
    {
        return this.min_node;
    }

    /**
     * public void meld (FibonacciHeap heap2)
     *
     * Melds heap2 with the current heap.
     *
     * Complexity: O(1)
     */
    public void meld(FibonacciHeap heap2)
    {
        //if one of the heaps are empty:
        if (heap2.isEmpty()){
            return;
        }
        if (this.isEmpty()){
            this.first = heap2.getFirst();
            this.min_node = heap2.getMinNode();
        }
        else {
            //melding the two heaps one after another:
            HeapNode last_node_in_this_heap = this.first.prev;
            HeapNode last_node_in_heap2 = heap2.getFirst().getPrev();
            last_node_in_this_heap.setNext(heap2.getFirst());
            last_node_in_heap2.setNext(this.first);
            //updating the new min_node:
            if (heap2.getMinNode().getKey() < this.min_node.getKey()){
                this.min_node = heap2.getMinNode();
            }
        }
        //updating fields size, count_roots and count_marked:
        this.size += heap2.size();
        this.count_roots += heap2.getRootsNum();
        this.count_marked += heap2.getMarkedNum();
    }

    /**
     * public int size()
     *
     * Returns the number of elements in the heap.
     *
     * Complexity: O(1)
     */
    public int size()
    {
        return this.size;
    }

    /**
     * public int[] countersRep()
     *
     * Return an array of counters. The i-th entry contains the number of trees of order i in the heap.
     * (Note: The size of the array depends on the maximum order of a tree.)
     *
     * Complexity: O(n)
     */
    public int[] countersRep()
    {
        int count_roots_copy = this.count_roots;
        HeapNode node = this.first;
        int max_rank=0;
        // if heap is empty:
        if (node == null) {
            return new int[0];
        }
        // iterating through the heap's roots, searching for the max rank:
        while (count_roots_copy > 0) {
            if (node.getRank() > max_rank){
                max_rank = node.getRank();
            }
            node = node.getNext();
            count_roots_copy--;
        }
        int[] countersRep = new int[max_rank + 1];
        // iterating through the heap's roots, and updating "countersRep" according to the root's ranks:
        count_roots_copy = this.count_roots;
        node = this.first;
        while (count_roots_copy > 0) {
            countersRep[node.getRank()]++;
            node = node.getNext();
            count_roots_copy--;
        }
        return countersRep;
    }

    /**
     * public void delete(HeapNode x)
     *
     * Deletes the node x from the heap.
     * It is assumed that x indeed belongs to the heap.
     *
     * time complexity: WC - O(n), amortized - O(log n)
     */
    public void delete(HeapNode x)
    {
        // turning x to the node with minimal key:
        decreaseKey(x, Integer.MIN_VALUE);
        // delete x:
        this.deleteMin();
    }

    /**
     * public void decreaseKey(HeapNode x, int delta)
     *
     * Decreases the key of the node x by a non-negative value delta. The structure of the heap should be updated
     * to reflect this change (for example, the cascading cuts procedure should be applied if needed).
     *
     * time complexity: WC O(n), amortized O(1)
     */
    public void decreaseKey(HeapNode x, int delta)
    {
        if (delta == Integer.MIN_VALUE) {
            x.setKey(delta);
        }
        else {
            x.setKey(x.getKey() -delta);
        }
        HeapNode parent = x.getParent();
        // x is one of the roots of the heap
        if (parent == null) {
            this.findNewMin(x);
            return;
        }
        if (x.getKey() >= parent.getKey()) {
            return;
        }
        this.cascadingCut(x, parent);
    }

    /**
     * private void findNewMin(HeapNode node)
     *
     * compares current heap's min_node key with given node's key. if the second is smaller, the min_node
     * becomes to the given node.
     *
     * Complexity: O(1)
     */
    private void findNewMin(HeapNode node) {
        if(node.getKey() < this.min_node.getKey()) {
            this.min_node = node;
        }
    }

    /**
     * private void cascadingCut(HeapNode x, HeapNode parent)
     *
     * cuts the first node from the second node. if the second node have lost 2 children after this cut,
     * it continues to cut links between the parent and its grandfather, until the grandfather is null or until parent
     * did not lose 2 children.
     *
     * time complexity: WC O(n), amortized O(1)
     */
    private void cascadingCut(HeapNode x, HeapNode parent) {
        this.cut(x, parent);
        HeapNode grandfather = parent.getParent();
        if(grandfather != null){
            if (!parent.getMarked()) {
                setHeapNodeMarked(parent, true);
            }
            else {
                cascadingCut(parent, grandfather);
            }
        }
    }

    /**
     * private void setHeapNodeMarked(HeapNode node, boolean value)
     *
     * changes the given node "mark" filed according to the given value. if mark filed was changed, it updates the
     * "count_mark" filed.
     *
     * Complexity: O(1)
     */
    private void setHeapNodeMarked(HeapNode node, boolean value) {
        boolean isNodeMarked = node.getMarked();
        if (isNodeMarked != value)
        {
            count_marked += isNodeMarked ? -1 : 1;
        }
        node.setMarked(value);
    }

    /**
     * private void cut(HeapNode x, HeapNode y)
     *
     * cuts a node from its parent and adds the node to the root's list of the heap.
     *
     * Complexity: O(1)
     */
    private void cut(HeapNode x, HeapNode y) {
        x.setParent(null);
        this.setHeapNodeMarked(x, false);
        y.setRank(y.getRank() - 1);
        if(x.getNext() == x) {
            y.setChild(null);
        }
        else {
            HeapNode next = x.getNext();
            HeapNode prev = x.getPrev();
            if(y.getChild() == x) {
                y.setChild(next);
            }
            prev.setNext(next);
        }
        // add x to roots list + define x as the new heap min_node if needed
        this.first.getPrev().setNext(x);
        x.setNext(this.first);
        this.first = x;
        this.findNewMin(x);
        this.count_roots ++;
        count_total_cuts ++;
    }

    /**
     * public int nonMarked()
     *
     * This function returns the current number of non-marked items in the heap
     *
     * Complexity: O(1)
     */
    public int nonMarked()
    {
        return this.size - this.count_marked;
    }

    /**
     * public int potential()
     *
     * This function returns the current potential of the heap, which is:
     * Potential = #trees + 2*#marked
     *
     * In words: The potential equals to the number of trees in the heap
     * plus twice the number of marked nodes in the heap.
     *
     * Complexity: O(1)
     */
    public int potential()
    {
        return this.count_roots + 2 * this.count_marked;
    }

    /**
     * public static int totalLinks()
     *
     * This static function returns the total number of link operations made during the
     * run-time of the program. A link operation is the operation which gets as input two
     * trees of the same rank, and generates a tree of rank bigger by one, by hanging the
     * tree which has larger value in its root under the other tree.
     *
     * Complexity: O(1)
     */
    public static int totalLinks()
    {
        return count_total_links;
    }

    /**
     * public static int totalCuts()
     *
     * This static function returns the total number of cut operations made during the
     * run-time of the program. A cut operation is the operation which disconnects a subtree
     * from its parent (during decreaseKey/delete methods).
     *
     * Complexity: O(1)
     */
    public static int totalCuts()
    {
        return count_total_cuts;
    }

    /**
     * public static int[] kMin(FibonacciHeap H, int k)
     *
     * This static function returns the k smallest elements in a Fibonacci heap that contains a single tree.
     * The function should run in O(k*deg(H)). (deg(H) is the degree of the only tree in H.)
     * ###CRITICAL### : you are NOT allowed to change H.
     *
     * Complexity: O(k*deg(H))
     */
    public static int[] kMin(FibonacciHeap H, int k)
    {
        FibonacciHeap B = new FibonacciHeap();
        int[] arr = new int[k];
        int index_of_arr = 0;
        if (H.isEmpty()) {
            return arr;
        }
        HeapNode first_node_in_B = new HeapNode(H.getMinNode().getKey());
        // updating field "child_pointer_for_kMin_method" that will hold to the child's pointer without
        // affecting the structure of the heap, meaning without "child" field compromised:
        first_node_in_B.set_child_pointer_for_kMin_method(H.getMinNode().getChild());
        B.insert(first_node_in_B); // using an override method "insert"
        for (int i = 0; i < k; i ++){
            // finding minimum in B and inserting it to the sorted arr:
            HeapNode min_pointer_in_B = B.findMin();
            arr[index_of_arr] = min_pointer_in_B.getKey();
            index_of_arr ++;
            // inserting all of min's children to heap B:
            HeapNode child = min_pointer_in_B.get_child_pointer_for_kMin_method();
            if (child == null) {
                B.deleteMin();
                continue;
            }
            for (int j = 0 ; j < child.getParent().getRank() ; j++) {
                HeapNode child_for_heap_B = new HeapNode(child.getKey());
                child_for_heap_B.set_child_pointer_for_kMin_method(child.getChild());
                B.insert(child_for_heap_B);
                child = child.getNext();
            }
            // deleting minimum from B and making B a binomial tree again:
            B.deleteMin();
        }
        return arr;
    }

    /**
     * public class HeapNode
     *
     * If you wish to implement classes other than FibonacciHeap
     * (for example HeapNode), do it in this file, not in another file.
     *
     */
    public static class HeapNode {

        public int key;
        private int rank;
        private boolean marked;
        private HeapNode child;
        private HeapNode next;
        private HeapNode prev;
        private HeapNode parent;
        private HeapNode child_pointer_for_kMin_method; // used for kMin method only

        public HeapNode(int key) {
            this.key = key;
            this.rank = 0;
            this.marked = false;
            this.child = null;
            this.next = null;
            this.prev = null;
            this.parent = null;
            this.child_pointer_for_kMin_method = null;
        }

        public int getKey() {
            return this.key;
        }

        public void setKey(int key) {
            this.key=key;
        }

        public int getRank() {
            return this.rank;
        }

        public void setRank(int rank) {
            this.rank = rank;
        }

        public boolean getMarked() {
            return this.marked;
        }

        public void setMarked(boolean marked) {
            this.marked = marked;
        }

        public HeapNode getChild() {
            return this.child;
        }

        public void setChild(HeapNode child) {
            this.child = child;
        }

        public HeapNode getNext() {
            return this.next;
        }

        public void setNext(HeapNode next) {
            this.next = next;
            if(next != null){
                next.setPrev(this);
            }
        }

        public HeapNode getPrev() {
            return this.prev;
        }

        public void setPrev(HeapNode prev) {
            this.prev = prev;
        }

        public HeapNode getParent() {
            return this.parent;
        }

        public void setParent(HeapNode parent) {
            this.parent = parent;
        }

        public HeapNode get_child_pointer_for_kMin_method() { return this.child_pointer_for_kMin_method; }

        public void set_child_pointer_for_kMin_method (HeapNode child) { this.child_pointer_for_kMin_method = child; }
    }

}
