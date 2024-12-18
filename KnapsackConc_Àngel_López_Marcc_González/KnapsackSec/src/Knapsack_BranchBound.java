// Original code: https://www.geeksforgeeks.org/0-1-knapsack-using-branch-and-bound/

import java.time.Duration;
import java.time.Instant;
import java.util.*;

public class Knapsack_BranchBound extends Knapsack
{
    PriorityQueue<Node> PendingTasksQueue = new PriorityQueue<>((a, b) -> Integer.compare(b.bound, a.bound));

    final boolean DPurgeWorstNodes = false;
    int PurgedTasks = 0;

    class Node extends Solution {
        int level;
        int bound=0;

        Node(int level, int profit, int weight) {
            super( profit, weight);
            this.level = level;
        }

        Node(int level, int profit, int weight, Set<Integer> mochila)
        {
            super( profit, weight, mochila);
            this.level = level;
        }
    }

    int bound(Node u, int W)
    {
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

    void KnapSack(int W)
    {
        Node u, v;

        // Create the fist node in the queue
        u = new Node(-1, 0, 0);
        int maxProfit = 0;
        while (u!=null)
        {
            if (u.level == -1)
                v = new Node(0, 0, 0);
            else if (u.level == Items.size() - 1)
            {   // The tree leaf has been reached, add solution
                addSolution(u);
                // Purge not optimal solutions nodes.
                if (DPurgeWorstNodes)
                    purgeWorstNodes(u);
                // Get next node to process.
                u = PendingTasksQueue.poll();
                continue;
            }
            else
                v = new Node(u.level + 1, u.profit, u.weight, u.mochila);

            // Evaluate next level node, where the level_th item is included in the solution
            // Add current level item to the current solution
            v.weight += Items.get(v.level).getWeight();
            v.profit += Items.get(v.level).getValue();
            v.mochila.add(v.level);

            // Udpdate the best/maximun profit
            if (v.weight <= W && v.profit > maxProfit)
                maxProfit = v.profit;

            // Calculate the maximum profit for this solution
            v.bound = bound(v, W);

            // If the solution can be improved in the following levels put in the processing queue (level item included, v)
            if (v.bound > maxProfit)
                PendingTasksQueue.offer(v);
            else
                // If not store as partial solution and not continue its processing.
                if (v.weight <= W)
                    addSolution(v);

            // Continue processing next level, where the level_th item is not include in the solution (u)
            u.level = u.level + 1;
        }
    }

    public void purgeWorstNodes(Node bestSolution)
    {
        int original_size = PendingTasksQueue.size();
        PendingTasksQueue.removeIf( node -> node.bound < MaxProfit);
        int purged_size = PendingTasksQueue.size();
        PurgedTasks += original_size-purged_size;
        // if ((original_size-purged_size)>0)
        //    System.out.println("\t\t Purged "+ (original_size-purged_size) + "("+PurgedTasks+") nodes." );
    }

    public int CalculateKnapsack(int maxWeight, Vector<Item> items)
    {
        Instant start = Instant.now();

        // Sort Items by Value/Cost ratio.
        Items = items;
        Items.sort(itemComparator);
        KnapSack(maxWeight);

        Instant finish = Instant.now();
        long timeElapsed = Duration.between(start, finish).toMillis();  //in millis
        System.out.printf("[Knapsack_BranchBound] Maximum Profit = %d. Total execution time: %.3f secs.\n", MaxProfit, timeElapsed/1000.0);
        System.out.printf("[Knapsack_BranchBound] Solution: ");
        printSolution(BestSolution);

        return (MaxProfit);
    }
}
