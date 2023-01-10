import java.lang.Math;
import java.util.ArrayList;
import java.util.Collections;

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
    private int roots_num;

    public FibonacciHeap(){
        this.min_node = null;
        this.first = null;
        this.size = 0;
        this.roots_num = 0;
    }

    public HeapNode getMinNode() {
        return this.min_node;
    }

    public void setMinNode(HeapNode min_node) {
        this.min_node = min_node;
    }

    public HeapNode getFirst() {
        return this.first;
    }

    public void setFirst(HeapNode first) {
        this.first = first;
    }

    public void setSize(int size) {
        this.size = size;
    }

   /**
    * public boolean isEmpty()
    *
    * Returns true if and only if the heap is empty.
    *   
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
            FindNewMin(new_heap_node);
        }
        this.first = new_heap_node;
        this.size += 1;
        this.roots_num += 1;
        return new_heap_node;
    }

    private void FindNewMin(HeapNode new_node) {
        if (new_node.getKey() < this.min_node.getKey()) {
            this.setMinNode(new_node);
        }
    }

   /**
    * public void deleteMin()
    *
    * Deletes the node containing the minimum key.
    *
    */
    public void deleteMin() {
        HeapNode min = this.min_node;
        HeapNode child = min.getChild();
        HeapNode next = min.getNext();
        HeapNode prev = min.getPrev();

        // if min_node has children, they become roots
        if (min.getRank() != 0) {
            // if min_node is the only root in the heap, it's children become the only roots of the heap
            if (this.roots_num == 1) {
                this.first = child;
            }
            // min_node is not the only root in the heap, then it's children replace it in the list of roots of the heap
            else {
                prev.setNext(child);
                child.getPrev().setNext(next);
            }
            // changing "mark" field of the deleted node's children to false and changing their "parent" field to null
            for (int i = 1; i <= min.getRank(); i++){
                child.setMarked(false);
                child.setParent(null);
                child = child.getNext();
            }
        }
        // min node rank is 0
        else {
            // the deleted root is the last one in the heap
            if (this.roots_num == 1) {
                this.first = null;
            }
            // connect deleted root siblings
            else {
                prev.setNext(next);
            }
        }
        // set deleted node child to be null
        min.setChild(null);
        // if first was the deleted node, replace it with it's perv
        if(this.first == min) {
            this.first = next;
        }
        // detach deleted node siblings
        min.setNext(null);
        min.setPrev(null);
        // update counter fileds
        this.roots_num -= 1;
        this.size -= 1;
        // set new min node and do successive linking
        this.min_node = SuccessiveLinking();
    }


    private HeapNode SuccessiveLinking() {

        int log_n = (int)(Math.ceil(Math.log(this.size + 1) / Math.log(2)));
        int roots_num_copy = this.roots_num;
        HeapNode[] buckets = new HeapNode[log_n];
        HeapNode node = this.first;
        int rank;
        HeapNode node_after_link;
        // if heap is empty
        if (node == null) {
            return null;
        }
        while (roots_num_copy > 0) {
            rank = node.getRank();
            // if node's rank bucket is empty
            if(buckets[rank] == null){
                buckets[rank] = node;
                node = node.getNext();
                roots_num_copy --;
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

    private HeapNode createHeapAndFindMin(HeapNode[] buckets) {
        this.first = null;
        this.roots_num = 0;
        HeapNode min = null;
        HeapNode last_added_tree = null;
        for (int i = 0; i < buckets.length; i++) {
            if (buckets[i] != null) {
                this.roots_num += 1;
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

    private HeapNode link(HeapNode node1, HeapNode node2){
        // make sure both trees are the same rank
        if(node1.getRank() != node2.getRank()) {
            return null;
        }
        HeapNode root;
        HeapNode left_child;
        HeapNode next = node1.getNext();
        //HeapNode prev = node1.getPrev();
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
            left_child.setPrev(left_child);
        }
        root.setChild(left_child);
        root.setNext(next);
        root.setRank(root.getRank() + 1);
        // !!!!!!!!!!! check if we need to change left_child mark filed !!!!!!!!
        return root;
    }

   /**
    * public HeapNode findMin()
    *
    * Returns the node of the heap whose key is minimal, or null if the heap is empty.
    *
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
    */
    public void meld (FibonacciHeap heap2)
    {
    	  return; // should be replaced by student code   		
    }

   /**
    * public int size()
    *
    * Returns the number of elements in the heap.
    *   
    */
    public int size()
    {
        return this.size;
    }
    	
    /**
    * public int[] countersRep()
    *
    * Return an array of counters. The i-th entry contains the number of trees of order i in the heap.
    * (Note: The size of of the array depends on the maximum order of a tree.)  
    * 
    */
    public int[] countersRep()
    {
    	int[] arr = new int[100];
        return arr; //	 to be replaced by student code
    }
	
   /**
    * public void delete(HeapNode x)
    *
    * Deletes the node x from the heap.
	* It is assumed that x indeed belongs to the heap.
    *
    */
    public void delete(HeapNode x) 
    {    
    	return; // should be replaced by student code
    }

   /**
    * public void decreaseKey(HeapNode x, int delta)
    *
    * Decreases the key of the node x by a non-negative value delta. The structure of the heap should be updated
    * to reflect this change (for example, the cascading cuts procedure should be applied if needed).
    */
    public void decreaseKey(HeapNode x, int delta)
    {    
    	return; // should be replaced by student code
    }

   /**
    * public int nonMarked() 
    *
    * This function returns the current number of non-marked items in the heap
    */
    public int nonMarked() 
    {    
        return -232; // should be replaced by student code
    }

   /**
    * public int potential() 
    *
    * This function returns the current potential of the heap, which is:
    * Potential = #trees + 2*#marked
    * 
    * In words: The potential equals to the number of trees in the heap
    * plus twice the number of marked nodes in the heap. 
    */
    public int potential() 
    {    
        return -234; // should be replaced by student code
    }

   /**
    * public static int totalLinks() 
    *
    * This static function returns the total number of link operations made during the
    * run-time of the program. A link operation is the operation which gets as input two
    * trees of the same rank, and generates a tree of rank bigger by one, by hanging the
    * tree which has larger value in its root under the other tree.
    */
    public static int totalLinks()
    {    
    	return -345; // should be replaced by student code
    }

   /**
    * public static int totalCuts() 
    *
    * This static function returns the total number of cut operations made during the
    * run-time of the program. A cut operation is the operation which disconnects a subtree
    * from its parent (during decreaseKey/delete methods). 
    */
    public static int totalCuts()
    {    
    	return -456; // should be replaced by student code
    }

     /**
    * public static int[] kMin(FibonacciHeap H, int k) 
    *
    * This static function returns the k smallest elements in a Fibonacci heap that contains a single tree.
    * The function should run in O(k*deg(H)). (deg(H) is the degree of the only tree in H.)
    *  
    * ###CRITICAL### : you are NOT allowed to change H. 
    */
    public static int[] kMin(FibonacciHeap H, int k)
    {    
        int[] arr = new int[100];
        return arr; // should be replaced by student code
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

    	public HeapNode(int key) {
    		this.key = key;
            this.rank = 0;
            this.marked = false;
            this.child = null;
            this.next = null;
            this.prev = null;
            this.parent = null;
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

       public boolean isMarked() {
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
    }



    public static void main(String[] args) {
        FibonacciHeap fibonacciHeap = new FibonacciHeap();

        ArrayList<Integer> numbers = new ArrayList<>();

        /*for (int i = 0; i < 5; i++) {
            numbers.add(i);
        }*/

        //Collections.shuffle(numbers);
        numbers.add(0);
        numbers.add(1);
        numbers.add(3);
        numbers.add(2);
        numbers.add(4);
        System.out.println(numbers);
        for (int i = 0; i < 5; i++) {
            fibonacciHeap.insert(numbers.get(i));
        }

        for (int i = 0; i < 5; i++) {
            if (fibonacciHeap.findMin().getKey() != i) {
                System.out.println(i);
                System.out.println(fibonacciHeap.findMin().getKey());
                System.out.println("wrong");
                return;
            }
            fibonacciHeap.deleteMin();
        }
    }

}
