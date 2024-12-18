import com.opencsv.CSVParser;
import com.opencsv.CSVReader;
import com.opencsv.CSVParserBuilder;
import com.opencsv.CSVReaderBuilder;
import com.opencsv.exceptions.CsvValidationException;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import java.util.Vector;

public class KnapsackSec
{
    private static final char Default_CSVSeparator = ',';
    private static int NItems = 3;
    private static int MaxWeight = 50;
    private static Vector<Item> Items = new  Vector<Item>();


    public static void main(String[] args)
    {
        String itemsFile = "";
        if (args.length>2)
            System.err.println("Error in Parameters. Usage: Knapsack_Sec [<Items_File>] [Maximum_Weight]");
        else if (args.length==0)
        {   // Default Items.
            Items.add(new Item(1,60,10));
            Items.add(new Item(2,100,20));
            Items.add(new Item(3,120,30));
        }
        else if (args.length==1)
            ReadItems(args[0]);
        else if (args.length==2) {
            ReadItems(args[0]);
            MaxWeight = Integer.parseInt(args[1]);
        }

        // Recursive Method
        if (Items.size()<=250)
        {
            Knapsack_Recursive knapsack_rec = new Knapsack_Recursive();
            int sol_Recursive = knapsack_rec.CalculateKnapsack(MaxWeight, Items);
            //System.out.printf("[Knapsack_Recursive] Maximum Profit: %d\n", sol_Recursive);
        }

        // Branch & Bound Method
        Knapsack_BranchBound knapsack_bb = new Knapsack_BranchBound();
        int sol_BranchBound = knapsack_bb.CalculateKnapsack(MaxWeight, Items);
        //System.out.printf("[Knapsack_BranchBound] Maximum Profit: %d\n", sol_BranchBound);
    }

    // Method to read csv items files, composed by N tuples of (values, weight)
    public static void ReadItems(String itemsPath)
    {
        int value, weigth;

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
            while ((line = csvReader.readNext()) != null)
            {
                if (line.length==2) {
                    value = Integer.parseInt(line[0].replaceAll("\\s", ""));
                    weigth = Integer.parseInt(line[1].replaceAll("\\s", ""));
                    NItems++;
                    Items.add(new Item(NItems, value, weigth));
                }
                else
                    System.err.printf("[KnapsackSec::ReadItems] Error reading items fields (%s).\n", line);
            }

        } catch (FileNotFoundException e) {
            System.err.printf("[KnapsackSec::ReadItems] File %s not found.\n",itemsPath);
            e.printStackTrace();
        } catch (IOException e) {
            System.err.printf("[KnapsackSec::ReadItems] Error file reading %s.\n",itemsPath);
            e.printStackTrace();
        } catch (CsvValidationException e) {
            System.err.printf("[KnapsackSec::ReadItems] Error csv format in %s.\n",itemsPath);
            e.printStackTrace();
        }
    }
}