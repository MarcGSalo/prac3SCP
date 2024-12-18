import java.time.Duration;
import java.time.Instant;
import java.util.Vector;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.RecursiveTask;

public class Knapsack_Recursive_Concurrent extends Knapsack {

    private static final int THRESHOLD = 5; // Threshold level for parallel execution

    // Recursive task class for concurrent knapsack solution
    class KnapsackTask extends RecursiveTask<Integer> {
        private final int W;
        private final int n;
        private final Solution solution;

        KnapsackTask(int W, int n, Solution solution) {
            this.W = W;
            this.n = n;
            this.solution = solution;
        }

        @Override
        protected Integer compute() {
            // Base case
            if (n == 0 || W <= 0) {
                addSolution(solution);
                return 0;
            }

            // If weight of the nth item is more than the capacity, skip it
            if (Items.get(n - 1).getWeight() > W) {
                return KnapSack(W, n - 1, solution); // Sequential call for base case
            } else {
                // Split tasks if level is at or beyond the threshold
                if (n < THRESHOLD) {
                    // Sequential calls below the threshold
                    Solution solIncluded = new Solution(solution);
                    solIncluded.mochila.add(n - 1);
                    solIncluded.profit += Items.get(n - 1).getValue();
                    solIncluded.weight += Items.get(n - 1).getWeight();
                    int profitIncluded = Items.get(n - 1).getValue() + KnapSack(W - Items.get(n - 1).getWeight(), n - 1, solIncluded);
                    int profitNotIncluded = KnapSack(W, n - 1, solution);

                    return Math.max(profitIncluded, profitNotIncluded);
                } else {
                    // Concurrent calls above the threshold
                    Solution solIncluded = new Solution(solution);
                    solIncluded.mochila.add(n - 1);
                    solIncluded.profit += Items.get(n - 1).getValue();
                    solIncluded.weight += Items.get(n - 1).getWeight();

                    KnapsackTask includeTask = new KnapsackTask(W - Items.get(n - 1).getWeight(), n - 1, solIncluded);
                    KnapsackTask excludeTask = new KnapsackTask(W, n - 1, solution);

                    // Fork both tasks and then join them
                    includeTask.fork();
                    int profitNotIncluded = excludeTask.compute();
                    int profitIncluded = Items.get(n - 1).getValue() + includeTask.join();

                    return Math.max(profitIncluded, profitNotIncluded);
                }
            }
        }
    }

    // Start the concurrent knapsack computation
    public int CalculateKnapsack(int maxWeight, Vector<Item> items) {
        Instant start = Instant.now();

        // Sort items by Value/Weight ratio
        Items = items;
        Items.sort(itemComparator);

        ForkJoinPool forkJoinPool = new ForkJoinPool();
        KnapsackTask task = new KnapsackTask(maxWeight, Items.size(), new Solution(0, 0));
        MaxProfit = forkJoinPool.invoke(task);

        Instant finish = Instant.now();
        long timeElapsed = Duration.between(start, finish).toMillis();
        System.out.printf("[Knapsack_Recursive_Concurrent] Maximum Profit = %d. Total execution time: %.3f secs.\n", MaxProfit, timeElapsed / 1000.0);
        System.out.printf("[Knapsack_Recursive_Concurrent] Solution: ");
        printSolution(BestSolution);

        return MaxProfit;
    }

    // Sequential KnapSack function used by the task
    private int KnapSack(int W, int n, Solution solution) {
        if (n == 0 || W <= 0) {
            addSolution(solution);
            return 0;
        }

        if (Items.get(n - 1).getWeight() > W) {
            return KnapSack(W, n - 1, solution);
        } else {
            Solution solIncluded = new Solution(solution);
            solIncluded.mochila.add(n - 1);
            solIncluded.profit += Items.get(n - 1).getValue();
            solIncluded.weight += Items.get(n - 1).getWeight();
            int profitIncluded = Items.get(n - 1).getValue() + KnapSack(W - Items.get(n - 1).getWeight(), n - 1, solIncluded);
            int profitNotIncluded = KnapSack(W, n - 1, solution);

            return Math.max(profitIncluded, profitNotIncluded);
        }
    }
}
