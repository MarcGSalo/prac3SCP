import java.util.*;

public abstract class Knapsack
{
    int MaxProfit = 0;
    Knapsack_BranchBound.Solution BestSolution;
    Vector<Item> Items;
    Set<Knapsack_BranchBound.Solution> Solutions = new HashSet<>();


    class Solution {
        int profit;
        int weight;
        Set<Integer> mochila;

        Solution(int profit, int weight) {
            this.profit = profit;
            this.weight = weight;
            this.mochila = new TreeSet();
        }
        Solution(int profit, int weight, Set<Integer> mochila) {
            this.profit = profit;
            this.weight = weight;
            this.mochila = new TreeSet(mochila);
        }

        public Solution(Solution sol) {
            this.profit = sol.profit;
            this.weight = sol.weight;
            this.mochila = new TreeSet(sol.mochila);
        }
    }

    Comparator<Item> itemComparator = (a, b) -> {
        double ratio1 = (double) a.getValue()  / a.getWeight();
        double ratio2 = (double) b.getValue()  / b.getWeight();
        // Sorting in decreasing order of value per unit weight
        return Double.compare(ratio2, ratio1);
    };

    void addSolution(Solution solution)
    {
        if (solution.mochila.size()>0 && solution.profit>=MaxProfit && Solutions.add((Knapsack_BranchBound.Solution) solution))
            printBestSolution(solution);
    }

    void printBestSolution(Solution solution)
    {
        if (solution.profit>MaxProfit) {
            System.out.print("  *");
            MaxProfit = solution.profit;
            BestSolution = solution;
        }
        else {
            return;
            //System.out.print(" ");
        }
        printSolution(solution);
    }
    void printSolution(Solution solution)
    {
        int i;

        System.out.printf("%d (%d) => ",solution.profit, solution.weight);
        for (Integer item : solution.mochila)
        {
            System.out.print(Items.get(item).getId() + "(" +
                    + Items.get(item).getValue() + ":" +
                    + Items.get(item).getWeight()
                    + "), ");
        }
        System.out.println();
    }

    abstract int CalculateKnapsack(int maxWeight, Vector<Item> items);

}
