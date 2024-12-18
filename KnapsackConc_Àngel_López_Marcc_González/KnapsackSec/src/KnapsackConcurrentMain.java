import com.opencsv.CSVParser;
import com.opencsv.CSVReader;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvValidationException;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.Vector;

public class KnapsackConcurrentMain {
    private static final char Default_CSVSeparator = ',';
    private static int NItems = 3;
    private static int MaxWeight = 50;
    private static Vector<Item> Items = new Vector<Item>();

    public static void main(String[] args) {
        String itemsFile = "";
        if (args.length > 2)
            System.err.println("Error in Parameters. Usage: KnapsackConcurrentMain [<Items_File>] [Maximum_Weight]");
        else if (args.length == 0) {
            // Default Items.
            Items.add(new Item(1, 60, 10));
            Items.add(new Item(2, 100, 20));
            Items.add(new Item(3, 120, 30));
        } else if (args.length == 1) {
            ReadItems(args[0]);
        } else if (args.length == 2) {
            ReadItems(args[0]);
            MaxWeight = Integer.parseInt(args[1]);
        }

        // Recursive Method
        if (Items.size() <= 250) {
            Knapsack_Recursive_Concurrent knapsack_Rec_concurrent = new Knapsack_Recursive_Concurrent();
            int sol_RecursiveConc = knapsack_Rec_concurrent.CalculateKnapsack(MaxWeight, Items);
            //System.out.printf("[Knapsack_Recursive] Maximum Profit: %d\n", sol_Recursive);
        }

        // Recursive Sincronized Method
        if (Items.size() <= 250) {
            Knapsack_Recursive_Conc_Syncronized knapsack_R_conc_syn = new Knapsack_Recursive_Conc_Syncronized();
            int sol_knapsack_R_conc_syn = knapsack_R_conc_syn.CalculateKnapsack(MaxWeight, Items);
            //System.out.printf("[Knapsack_Recursive] Maximum Profit: %d\n", sol_Recursive);
        }


        /*
        // Concurrent Branch & Bound Method
        Knapsack_BranchBound_Concurrent knapsack_BB_concurrent = new Knapsack_BranchBound_Concurrent();
        int sol_BranchBoundConcurrent = knapsack_BB_concurrent.CalculateKnapsack(MaxWeight, Items);
        //System.out.printf("[Knapsack_BranchBound_Concurrent] Maximum Profit: %d\n", sol_BranchBoundConcurrent);
         */
    }

    // Method to read csv items files, composed by N tuples of (value, weight)
    public static void ReadItems(String itemsPath) {
        int value, weight;

        try {
            // Create an object of file reader class with CSV file as a parameter.
            FileReader filereader = new FileReader(itemsPath);

            // create csvParser object with default custom separator
            CSVParser parser = new CSVParserBuilder()
                    .withSeparator(Default_CSVSeparator)
                    .withIgnoreQuotations(true)
                    .build();

            // create csvReader object with parameter filereader, skip header line and parser
            CSVReader csvReader = new CSVReaderBuilder(filereader)
                    .withSkipLines(1)
                    .withCSVParser(parser)
                    .build();

            // Read csv items
            NItems = 0;
            String[] line;
            while ((line = csvReader.readNext()) != null) {
                if (line.length == 2) {
                    value = Integer.parseInt(line[0].replaceAll("\\s", ""));
                    weight = Integer.parseInt(line[1].replaceAll("\\s", ""));
                    NItems++;
                    Items.add(new Item(NItems, value, weight));
                } else {
                    System.err.printf("[KnapsackConcurrentMain::ReadItems] Error reading items fields (%s).\n", line);
                }
            }

        } catch (FileNotFoundException e) {
            System.err.printf("[KnapsackConcurrentMain::ReadItems] File %s not found.\n", itemsPath);
            e.printStackTrace();
        } catch (IOException e) {
            System.err.printf("[KnapsackConcurrentMain::ReadItems] Error file reading %s.\n", itemsPath);
            e.printStackTrace();
        } catch (CsvValidationException e) {
            System.err.printf("[KnapsackConcurrentMain::ReadItems] Error csv format in %s.\n", itemsPath);
            e.printStackTrace();
        }
    }
}
