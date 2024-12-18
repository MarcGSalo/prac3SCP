import java.time.Duration;
import java.time.Instant;
import java.util.Vector;
import java.util.concurrent.*;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class Knapsack_Recursive_Conc_Syncronized extends Knapsack {

    private static final int THRESHOLD = 5;
    private static final int M = 10; // Nodos para imprimir estadísticas parciales

    // Variables para estadísticas globales
    private static int globalRecursiveCalls = 0; // Usando synchronized
    private static int globalMaxProfit = 0; // Usando Lock
    private static final Lock maxProfitLock = new ReentrantLock();

    private static int globalNodesProcessed = 0; // Usando semáforo
    private static final Semaphore nodesSemaphore = new Semaphore(1);

    private static int globalSolutionsFound = 0; // Usando variable de condición
    private static final Lock solutionsLock = new ReentrantLock();
    private static final Condition solutionCondition = solutionsLock.newCondition();

    // Clase de tarea con estadísticas parciales
    class KnapsackTaskWithStats extends RecursiveTask<Integer> {
        private final int W;
        private final int n;
        private final Solution solution;
        private final CountDownLatch latch;
        private final Phaser phaser;

        // Estadísticas locales
        private int localNodesProcessed = 0;
        private int localRecursiveCalls = 0;
        private int localSolutionsFound = 0;

        KnapsackTaskWithStats(int W, int n, Solution solution, CountDownLatch latch,Phaser phaser) {
            this.W = W;
            this.n = n;
            this.solution = solution;
            this.latch = latch;
            this.phaser = phaser;
            phaser.register();
        }

        @Override
        protected Integer compute() {
            try {
                incrementGlobalRecursiveCalls();
                localRecursiveCalls++;

                if (n == 0 || W <= 0) {
                    addSolution(solution);
                    incrementGlobalSolutions();
                    localSolutionsFound++;
                    if (latch != null) latch.countDown();
                    phaser.arriveAndDeregister();//-------------------------------------------------
                    return 0;
                }

                incrementGlobalNodesProcessed();
                localNodesProcessed++;
                if (globalNodesProcessed % M == 0) {
                    phaser.arriveAndAwaitAdvance();//esperem a tots els fils
                    printPartialStats();
                }

                if (Items.get(n - 1).getWeight() > W) {
                    int result = new KnapsackTaskWithStats(W, n - 1, solution, null,phaser).compute();
                    if (latch != null) latch.countDown();
                    phaser.arriveAndDeregister();//---------------------------------------------------
                    return result;
                } else {
                    if (n < THRESHOLD) {
                        Solution solIncluded = new Solution(solution);
                        solIncluded.mochila.add(n - 1);
                        solIncluded.profit += Items.get(n - 1).getValue();
                        solIncluded.weight += Items.get(n - 1).getWeight();

                        int profitIncluded = Items.get(n - 1).getValue() +
                                new KnapsackTaskWithStats(W - Items.get(n - 1).getWeight(), n - 1, solIncluded, null,phaser).compute();
                        int profitNotIncluded = new KnapsackTaskWithStats(W, n - 1, solution, null,phaser).compute();

                        updateGlobalMaxProfit(Math.max(profitIncluded, profitNotIncluded));
                        if (latch != null) latch.countDown();
                        phaser.arriveAndDeregister();//-------------------------------------
                        return Math.max(profitIncluded, profitNotIncluded);
                    } else {
                        CountDownLatch childLatch = new CountDownLatch(2);

                        Solution solIncluded = new Solution(solution);
                        solIncluded.mochila.add(n - 1);
                        solIncluded.profit += Items.get(n - 1).getValue();
                        solIncluded.weight += Items.get(n - 1).getWeight();

                        KnapsackTaskWithStats includeTask = new KnapsackTaskWithStats(W - Items.get(n - 1).getWeight(), n - 1, solIncluded, childLatch,phaser);
                        KnapsackTaskWithStats excludeTask = new KnapsackTaskWithStats(W, n - 1, solution, childLatch,phaser);

                        includeTask.fork();
                        int profitNotIncluded = excludeTask.compute();
                        int profitIncluded = Items.get(n - 1).getValue() + includeTask.join();

                        childLatch.await();

                        updateGlobalMaxProfit(Math.max(profitIncluded, profitNotIncluded));
                        if (latch != null) latch.countDown();
                        phaser.arriveAndDeregister();//---------------------------------------------------------
                        return Math.max(profitIncluded, profitNotIncluded);
                    }
                }
            } catch (InterruptedException e) {
                phaser.arriveAndDeregister();//-------------------------------------------------
                Thread.currentThread().interrupt();
                throw new RuntimeException("Task interrupted", e);
            }
        }

        private void printPartialStats() {
            System.out.printf("[Partial Stats] Thread %s processed %d nodes, %d recursive calls, %d solutions.\n",
                    Thread.currentThread().getName(), localNodesProcessed, localRecursiveCalls, localSolutionsFound);
        }
    }

    public int CalculateKnapsack(int maxWeight, Vector<Item> items) {
        Instant start = Instant.now();

        Items = items;
        Items.sort(itemComparator);

        ForkJoinPool forkJoinPool = new ForkJoinPool();
        CountDownLatch latch = new CountDownLatch(1);
        Phaser phaser = new Phaser(1);//registrem el fil principal al phaser
        phaser.register();//registrem el fil principal.
        KnapsackTaskWithStats task = new KnapsackTaskWithStats(maxWeight, Items.size(), new Solution(0, 0), latch,phaser);
        MaxProfit = forkJoinPool.invoke(task);

        try {
            latch.await(); // Wait for root task to finish
            phaser.arriveAndDeregister();//desregistrem el fil principal
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            phaser.arriveAndDeregister();//desregistrem el fil principal
            throw new RuntimeException("Main thread interrupted", e);
        }

        Instant finish = Instant.now();
        long timeElapsed = Duration.between(start, finish).toMillis();

        printGlobalStats();

        System.out.printf("[Knapsack_Recursive_Conc_Syncronized] Maximum Profit = %d. Total execution time: %.3f secs.\n", MaxProfit, timeElapsed / 1000.0);
        System.out.printf("[Knapsack_Recursive_Conc_Syncronized] Solution: ");
        printSolution(BestSolution);

        return MaxProfit;
    }

    private synchronized void incrementGlobalRecursiveCalls() {
        globalRecursiveCalls++;
    }

    private void updateGlobalMaxProfit(int profit) {
        maxProfitLock.lock();
        try {
            globalMaxProfit = Math.max(globalMaxProfit, profit);
        } finally {
            maxProfitLock.unlock();
        }
    }

    private void incrementGlobalNodesProcessed() {
        try {
            nodesSemaphore.acquire();
            globalNodesProcessed++;
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        } finally {
            nodesSemaphore.release();
        }
    }

    private void incrementGlobalSolutions() {
        solutionsLock.lock();
        try {
            globalSolutionsFound++;
            solutionCondition.signalAll();
        } finally {
            solutionsLock.unlock();
        }
    }

    private void printGlobalStats() {
        System.out.printf("[Global Stats] Total nodes processed: %d, Total recursive calls: %d, Total solutions found: %d, Optimal solution: %d.\n",
                globalNodesProcessed, globalRecursiveCalls, globalSolutionsFound, globalMaxProfit);
    }
}
