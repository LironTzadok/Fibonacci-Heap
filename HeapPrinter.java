import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Stack;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class HeapPrinter {
    static final String NULL = "(null)";
    final PrintStream stream;

    public HeapPrinter(PrintStream stream) {
        this.stream = stream;
    }

    void printIndentPrefix(ArrayList<Boolean> hasNexts) {
        int size = hasNexts.size();
        for (int i = 0; i < size - 1; ++i) {
            this.stream.format("%c   ", hasNexts.get(i).booleanValue() ? '│' : ' ');
        }
    }

    void printIndent(FibonacciHeap.HeapNode heapNode, ArrayList<Boolean> hasNexts) {
        int size = hasNexts.size();
        printIndentPrefix(hasNexts);

        this.stream.format("%c── %s\n",
                hasNexts.get(size - 1).booleanValue() ? '├' : '╰',
                heapNode == null ? NULL : String.valueOf(heapNode.getKey()));
    }

    static String repeatString(String s, int count) {
        StringBuilder r = new StringBuilder();
        for (int i = 0; i < count; i++) {
            r.append(s);
        }
        return r.toString();
    }

    void printIndentVerbose(FibonacciHeap.HeapNode heapNode, ArrayList<Boolean> hasNexts) {
        int size = hasNexts.size();
        if (heapNode == null) {
            printIndentPrefix(hasNexts);
            this.stream.format("%c── %s\n", hasNexts.get(size - 1).booleanValue() ? '├' : '╰', NULL);
            return;
        }

        Function<Supplier<FibonacciHeap.HeapNode>, String> keyify = f -> {
            FibonacciHeap.HeapNode node = f.get();
            return node == null ? NULL : String.valueOf(node.getKey());
        };
        String title = String.format(" Key: %d ", heapNode.getKey());
        List<String> content = Arrays.asList(
                String.format(" Rank: %d ", heapNode.getRank()),
                String.format(" Marked: %b ", heapNode.getMarked()),
                String.format(" Parent: %s ", keyify.apply(heapNode::getParent)),
                String.format(" Next: %s ", keyify.apply(heapNode::getNext)),
                String.format(" Prev: %s ", keyify.apply(heapNode::getPrev)),
                String.format(" Child: %s", keyify.apply(heapNode::getChild)));

        /* Print details in box */
        int length = Math.max(
                title.length(),
                content.stream().map(String::length).max(Integer::compareTo).get());
        String line = repeatString("─", length);
        String padded = String.format("%%-%ds", length);
        boolean hasNext = hasNexts.get(size - 1);

        // print header row
        printIndentPrefix(hasNexts);
        this.stream.format("%c── ╭%s╮\n", hasNext ? '├' : '╰', line);

        // print title row
        printIndentPrefix(hasNexts);
        this.stream.format("%c   │" + padded + "│\n", hasNext ? '│' : ' ', title);

        // print separator
        printIndentPrefix(hasNexts);
        this.stream.format("%c   ├%s┤\n", hasNext ? '│' : ' ', line);

        // print content
        for (String data : content) {
            printIndentPrefix(hasNexts);
            this.stream.format("%c   │" + padded + "│\n", hasNext ? '│' : ' ', data);
        }

        // print footer
        printIndentPrefix(hasNexts);
        this.stream.format("%c   ╰%s╯\n", hasNext ? '│' : ' ', line);
    }

    void printHeapNode(FibonacciHeap.HeapNode heapNode, boolean verbose) {
        BiConsumer<FibonacciHeap.HeapNode, ArrayList<Boolean>> function =
            verbose ?  this::printIndentVerbose : this::printIndent;

        Stack<Pair<FibonacciHeap.HeapNode, Integer>> stack = new Stack<>();
        Set<FibonacciHeap.HeapNode> visited = new HashSet<>();
        visited.add(null);

        ArrayList<Boolean> nexts = new ArrayList<>();

        nexts.add(false);
        int depth = 1;
        while (!visited.contains(heapNode) || !stack.empty()) {
            if (visited.contains(heapNode)) {
                Pair<FibonacciHeap.HeapNode, Integer> pair = stack.pop();
                heapNode = pair.first;
                depth = pair.second;
                while (nexts.size() > depth) {
                    nexts.remove(nexts.size() - 1);
                }
                continue;
            }

            visited.add(heapNode);
            nexts.set(nexts.size() - 1, !visited.contains(heapNode.getNext()));
            stack.push(new Pair<>(heapNode.getNext(), depth));

            function.accept(heapNode, nexts);

            heapNode = heapNode.getChild();
            if (heapNode != null) {
                nexts.add(false);
            }
            depth++;
        }
    }

    public void print(FibonacciHeap heap, boolean verbose) {
        if (heap == null) {
            this.stream.print(NULL + "\n");
            return;
        } else if (heap.isEmpty()) {
            this.stream.print("(empty)\n");
            return;
        }

        this.stream.print("╮\n");
        ArrayList<Boolean> list = new ArrayList<>();
        list.add(false);
        printHeapNode(heap.getFirst(), verbose);
    }
    class Pair<T, S> {
        public final T first;
        public final S second;

        public Pair(T first, S second) {
            this.first = first;
            this.second = second;
        }
    }
}