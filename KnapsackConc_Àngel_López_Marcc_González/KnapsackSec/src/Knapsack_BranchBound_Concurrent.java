import java.time.Duration;
import java.time.Instant;
import java.util.*;
import java.util.concurrent.*;

public class Knapsack_BranchBound_Concurrent extends Knapsack {
    final boolean DPurgeWorstNodes = false;
    int PurgedTasks = 0;
    private static final int THRESHOLD = 7; // Nivel a partir del cual se ejecutarán subárboles concurrentes

    class Node extends Solution {
        int level;
        int bound = 0;

        Node(int level, int profit, int weight) {
            super(profit, weight);
            this.level = level;
        }

        Node(int level, int profit, int weight, Set<Integer> mochila) {
            super(profit, weight, mochila);
            this.level = level;
        }
    }

    // Bound calculation
    int bound(Node u, int W) {
        if (u.weight >= W)
            return 0;

        int profitBound = u.profit;
        int j = u.level + 1;
        float totalWeight = u.weight;

        while (j < Items.size() && totalWeight + Items.get(j).getWeight() <= W) {
            totalWeight += Items.get(j).getWeight();
            profitBound += Items.get(j).getValue();
            j++;
        }

        if (j < Items.size())
            profitBound += (int) ((W - totalWeight) * Items.get(j).getValue() / Items.get(j).getWeight());

        return profitBound;
    }

    // Concurrent task that processes subtrees
    class KnapsackTask extends RecursiveTask<Integer> {
        private final Node u;
        private final int W;

        KnapsackTask(Node u, int W) {
            this.u = u;
            this.W = W;
        }

        @Override
        protected Integer compute() {
            if (u.level == Items.size() - 1) {
                // Reached a leaf node
                addSolution(u);
                return u.profit;
            }

            List<KnapsackTask> tasks = new ArrayList<>();
            int maxProfit = 0;
            PriorityQueue<Node> localQueue = new PriorityQueue<>((a, b) -> Integer.compare(b.bound, a.bound));

            localQueue.offer(u);

            while (!localQueue.isEmpty()) {
                Node currentNode = localQueue.poll();

                if (currentNode.level == Items.size() - 1) {
                    addSolution(currentNode);
                    continue;
                }

                // Generate next node where item is included
                Node includeNode = new Node(currentNode.level + 1, currentNode.profit, currentNode.weight, currentNode.mochila);
                includeNode.weight += Items.get(includeNode.level).getWeight();
                includeNode.profit += Items.get(includeNode.level).getValue();
                includeNode.mochila.add(includeNode.level);

                // If valid, update maxProfit
                if (includeNode.weight <= W && includeNode.profit > maxProfit) {
                    maxProfit = includeNode.profit;
                }

                includeNode.bound = bound(includeNode, W);

                if (includeNode.bound > maxProfit) {
                    if (includeNode.level < THRESHOLD) {
                        // Continue exploring this branch sequentially
                        localQueue.offer(includeNode);
                    } else {
                        // Fork new task to handle this subtree
                        KnapsackTask subTask = new KnapsackTask(includeNode, W);
                        subTask.fork();
                        tasks.add(subTask);
                    }
                } else {
                    if (includeNode.weight <= W) {
                        addSolution(includeNode);
                    }
                }

                // Generate next node where item is NOT included
                Node excludeNode = new Node(currentNode.level + 1, currentNode.profit, currentNode.weight, currentNode.mochila);
                excludeNode.bound = bound(excludeNode, W);

                if (excludeNode.bound > maxProfit) {
                    if (excludeNode.level < THRESHOLD) {
                        // Continue exploring this branch sequentially
                        localQueue.offer(excludeNode);
                    } else {
                        KnapsackTask subTask = new KnapsackTask(excludeNode, W);
                        subTask.fork();
                        tasks.add(subTask);
                    }
                }
            }

            // Join results from sub-tasks
            for (KnapsackTask task : tasks) {
                maxProfit = Math.max(maxProfit, task.join());
            }

            return maxProfit;
        }
    }

    public void KnapSack(int W) {
        ForkJoinPool forkJoinPool = new ForkJoinPool();
        Node root = new Node(-1, 0, 0);
        KnapsackTask task = new KnapsackTask(root, W);
        int maxProfit = forkJoinPool.invoke(task);

        System.out.printf("[Knapsack_BranchBound_Concurrent] Maximum Profit = %d.\n", maxProfit);
    }

    public int CalculateKnapsack(int maxWeight, Vector<Item> items) {
        Instant start = Instant.now();

        // Sort Items by Value/Weight ratio
        Items = items;
        Items.sort(itemComparator);

        KnapSack(maxWeight);

        Instant finish = Instant.now();
        long timeElapsed = Duration.between(start, finish).toMillis();
        System.out.printf("[Knapsack_BranchBound_Concurrent] Total execution time: %.3f secs.\n", timeElapsed / 1000.0);

        return MaxProfit;
    }
}
