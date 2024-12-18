// Original code: https://www.geeksforgeeks.org/0-1-knapsack-problem-dp-10/

import java.time.Duration;
import java.time.Instant;
import java.util.Vector;

public class Knapsack_Recursive extends Knapsack
{

    // Returns the maximum value that can be put in a knapsack of  capacity W
    int KnapSack(int W, int n, Solution solution)
    {
        // Base Case
        if (n == 0 || W <= 0) {
            addSolution(solution);
            return 0;
        }

        // If weight of the nth item is more than Knapsack capacity W, then this item cannot be included
        // in the optimal solution
        if (Items.get(n - 1).getWeight() > W)
            return KnapSack(W, n - 1, solution);
        else
        {
            // Return the maximum of two cases: (1) nth item included or (2) not included
            Solution sol_included = new Solution(solution);
            sol_included.mochila.add(n - 1);
            sol_included.profit +=  Items.get(n - 1).getValue();
            sol_included.weight +=  Items.get(n - 1).getWeight();
            int profit_included =  Items.get(n - 1).getValue() + KnapSack(W - Items.get(n - 1).getWeight(), n-1,sol_included);
            int profit_not_included = KnapSack(W, n - 1, solution);

            return Math.max(profit_included, profit_not_included);
        }
    }

    public int CalculateKnapsack(int maxWeight, Vector<Item> items)
    {
        Instant start = Instant.now();

        // Sort Items by Value/Cost ratio.
        Items = items;
        Items.sort(itemComparator);
        MaxProfit = KnapSack(maxWeight, Items.size(), new Solution(0,0));

        Instant finish = Instant.now();
        long timeElapsed = Duration.between(start, finish).toMillis();  //in millis
        System.out.printf("[Knapsack_Recursive] Maximum Profit = %d. Total execution time: %.3f secs.\n", MaxProfit, timeElapsed/1000.0);
        System.out.printf("[Knapsack_Recursive] Solution: ");
        printSolution(BestSolution);

        return (MaxProfit);
    }
}
