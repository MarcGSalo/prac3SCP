import java.time.Duration;
import java.time.Instant;
import java.util.Vector;
import java.util.concurrent.*;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Knapsack_Recursive_Conc_Syncronized extends Knapsack {

    private static final int THRESHOLD = 5;

    // Variables para estadísticas globales
    private static int globalRecursiveCalls = 0;
    private static int globalMaxProfit = 0;
    private static final Lock maxProfitLock = new ReentrantLock();

    private static int globalNodesProcessed = 0;
    private static int globalSolutionsFound = 0;

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
            incrementGlobalRecursiveCalls();

            if (n == 0 || W <= 0) {
                addSolution(solution);
                incrementGlobalSolutions();
                return 0;
            }

            incrementGlobalNodesProcessed();

            if (Items.get(n - 1).getWeight() > W) {
                return KnapSack(W, n - 1, solution);
            } else {
                if (n < THRESHOLD) {
                    // Cálculo secuencial
                    Solution solIncluded = new Solution(solution);
                    solIncluded.mochila.add(n - 1);
                    solIncluded.profit += Items.get(n - 1).getValue();
                    solIncluded.weight += Items.get(n - 1).getWeight();

                    int profitIncluded = Items.get(n - 1).getValue() + KnapSack(W - Items.get(n - 1).getWeight(), n - 1, solIncluded);
                    int profitNotIncluded = KnapSack(W, n - 1, solution);

                    updateGlobalMaxProfit(Math.max(profitIncluded, profitNotIncluded));
                    return Math.max(profitIncluded, profitNotIncluded);
                } else {
                    // División de tareas con sincronización manual
                    Solution solIncluded = new Solution(solution);
                    solIncluded.mochila.add(n - 1);
                    solIncluded.profit += Items.get(n - 1).getValue();
                    solIncluded.weight += Items.get(n - 1).getWeight();

                    CountDownLatch latch = new CountDownLatch(2);
                    final int[] profits = new int[2];

                    // Tarea para incluir el elemento
                    Thread includeThread = new Thread(() -> {
                        profits[0] = Items.get(n - 1).getValue() + KnapSack(W - Items.get(n - 1).getWeight(), n - 1, solIncluded);
                        latch.countDown();
                    });

                    // Tarea para excluir el elemento
                    Thread excludeThread = new Thread(() -> {
                        profits[1] = KnapSack(W, n - 1, solution);
                        latch.countDown();
                    });

                    // Iniciar ambas tareas
                    includeThread.start();
                    excludeThread.start();

                    // Esperar la finalización de ambas tareas
                    try {
                        latch.await();
                    } catch (InterruptedException e) {
                        Thread.currentThread().interrupt();
                        throw new RuntimeException("Main thread interrupted", e);
                    }

                    // Combinar los resultados
                    int profitIncluded = profits[0];
                    int profitNotIncluded = profits[1];

                    updateGlobalMaxProfit(Math.max(profitIncluded, profitNotIncluded));
                    return Math.max(profitIncluded, profitNotIncluded);
                }
            }
        }
    }

    public int CalculateKnapsack(int maxWeight, Vector<Item> items) {
        Instant start = Instant.now();

        Items = items;
        Items.sort(itemComparator);

        ForkJoinPool forkJoinPool = new ForkJoinPool();
        KnapsackTask task = new KnapsackTask(maxWeight, Items.size(), new Solution(0, 0));
        MaxProfit = forkJoinPool.invoke(task);

        Instant finish = Instant.now();
        long timeElapsed = Duration.between(start, finish).toMillis();

        printGlobalStats();

        System.out.printf("[Knapsack_Statistics] Maximum Profit = %d. Total execution time: %.3f secs.\n", MaxProfit, timeElapsed / 1000.0);
        System.out.printf("[Knapsack_Statistics] Solution: ");
        printSolution(BestSolution);

        return MaxProfit;
    }

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

    private synchronized void incrementGlobalRecursiveCalls() {
        globalRecursiveCalls++;
    }

    private synchronized void incrementGlobalNodesProcessed() {
        globalNodesProcessed++;
    }

    private synchronized void incrementGlobalSolutions() {
        globalSolutionsFound++;
    }

    private void updateGlobalMaxProfit(int profit) {
        maxProfitLock.lock();
        try {
            globalMaxProfit = Math.max(globalMaxProfit, profit);
        } finally {
            maxProfitLock.unlock();
        }
    }

    private void printGlobalStats() {
        System.out.printf("[Global Stats] Total nodes processed: %d, Total recursive calls: %d, Total solutions found: %d, Optimal solution: %d.\n",
                globalNodesProcessed, globalRecursiveCalls, globalSolutionsFound, globalMaxProfit);
    }
}
