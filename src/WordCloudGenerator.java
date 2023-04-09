import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

/**
 * Generates a word cloud of the top n most occurring words given a text file
 * and integer value for n
 *
 * @author Zia Mirza
 *
 */
public final class WordCloudGenerator {

    /**
     * Private constructor so this utility class cannot be instantiated.
     */
    private WordCloudGenerator() {
    }

    /**
     * Compare integers in decreasing order (high to low).
     */
    private static class CompareInteger
            implements Comparator<Entry<String, Integer>> {
        @Override
        public int compare(Entry<String, Integer> v1,
                Entry<String, Integer> v2) {
            return v2.getValue().compareTo(v1.getValue());
        }
    }

    /**
     * Compares strings in alphabetical order.
     */
    private static class CompareString
            implements Comparator<Entry<String, Integer>> {
        @Override
        public int compare(Entry<String, Integer> k1,
                Entry<String, Integer> k2) {
            return k1.getKey().compareTo(k2.getKey());
        }
    }

    /**
     * Creates a set of all word separators.
     *
     * @return a set of separators
     */
    private static Set<Character> createSeparatorList() {
        Character[] separatorList = new Character[] { '\t', '\n', '\r', ',',
                '-', '.', '!', '?', '[', ']', ';', ':', '/', '(', ')', ' ' };
        Set<Character> separatorSet = new HashSet<>();

        for (int i = 0; i < separatorList.length; i++) {
            separatorSet.add(separatorList[i]);
        }
        return separatorSet;

    }

    /**
     * Finds the next valid word or string of separators.
     *
     * @param line
     *            string to find the next word or separator from
     * @param separators
     *            the set of word separators
     * @param start
     *            the starting index
     * @return the next word or string of separators
     * @requires line is not null
     */
    private static String nextWordOrSeparator(String line,
            Set<Character> separators, int start) {
        assert line != null : "Violation of: line is not null";

        int index = start;

        //If the first character is not a separator, find the next word
        if (!(separators.contains(line.charAt(start)))) {
            while ((index < line.length())
                    && !(separators.contains(line.charAt(index)))) {
                index++;
            }
            //otherwise find the string of separators
        } else {
            while ((index < line.length())
                    && (separators.contains(line.charAt(index)))) {
                index++;
            }
        }

        return line.substring(start, index);

    }

    /**
     * Generates a map of words and their number of occurrences.
     *
     * @param input
     *            the input to read lines from
     * @param separators
     *            the set of word separators
     * @param count
     *            the map of word counts
     */
    private static void generateMap(BufferedReader input,
            Set<Character> separators, Map<String, Integer> count) {

        String line = null;
        try {
            line = input.readLine();
        } catch (IOException e1) {
            System.err.println("Error reading file");
        }

        while (line != null) {
            line = line.toLowerCase();

            int start = 0;
            String word = "";
            while (start < line.length()) {
                word = nextWordOrSeparator(line, separators, start);
                start += word.length();
                //Checks to make sure separators are not being added to the map
                if (!separators.contains(word.charAt(0))) {
                    //If the word already exists in the map, increment its count by 1
                    if (count.containsKey(word)) {
                        int increment = count.remove(word) + 1;
                        count.put(word, increment);
                    } else {
                        count.put(word, 1);
                    }
                }
            }
            try {
                line = input.readLine();
            } catch (IOException e) {
                System.err.println("Error reading file");
            }
        }

    }

    /**
     * Sorts a map by its values in decreasing order of count.
     *
     *
     * @param count
     *            the map of word counts
     * @param c1
     *            the comparator used to sort list
     * @param m1
     *            the map of sorted word counts
     * @param n
     *            the number of words in the tag cloud
     *
     * @return an array containing the maximum and minimum key values
     * @ensures range[0] <= range[1]
     */
    private static Integer[] sort(Map<String, Integer> count,
            Comparator<Map.Entry<String, Integer>> c1, Map<String, Integer> m1,
            int n) {

        List<Entry<String, Integer>> numericalSort = new ArrayList<>(
                count.entrySet());
        numericalSort.sort(c1);

        if (n < numericalSort.size()) {
            numericalSort = numericalSort.subList(0, n);
        }

        //Maximum and minimum word counts used in order to find font scaling
        int max = 1;
        int min = 1;
        if (numericalSort.size() > 0) {
            max = numericalSort.get(0).getValue();
            min = numericalSort.get(numericalSort.size() - 1).getValue();
        }

        for (Entry<String, Integer> entry : numericalSort) {
            m1.put(entry.getKey(), entry.getValue());
        }

        //Return the minimum and maximum values.
        Integer[] range = { min, max };
        return range;

    }

    /**
     * Creates the header for the html file.
     *
     * @param out
     *            the file that the tag cloud is being generated in
     * @param fileName
     *            the name of the file being used to find word count
     * @param n
     *            the number of words in the tag cloud
     */
    private static void header(PrintWriter out, String fileName, int n) {
        out.println("<!DOCTYPE html>");
        out.println("<html>");
        out.println("<head>");
        out.println("<title>Top " + n + " words in " + fileName + "</title>");
        out.println("<link rel=\"stylesheet\" href=\"wordcloud.css\">");
        out.println("</head>");
        out.println("<body>");
        out.println("<h2>Top " + n + " words in " + fileName + "</h2>");
        out.println("<hr>");
        out.println("<div class=\"cdiv\">");
        out.println("<p class=\"cbox\">");

    }

    /**
     * Generates the tag cloud onto the html file.
     *
     * @param out
     *            the file that the tag cloud is being generated in
     * @param min
     *            the smallest found word count
     * @param max
     *            the largest found word count
     * @param n
     *            the number of words in the tag cloud
     * @param m2
     *            the map sorted alphabetically
     */
    private static void tagCloud(PrintWriter out, int min, int max, int n,
            Map<String, Integer> m2) {
        final int minFont = 11;
        final int maxFont = 48;

        Set<Entry<String, Integer>> s = m2.entrySet();
        Iterator<Entry<String, Integer>> it = s.iterator();

        while (it.hasNext()) {
            Entry<String, Integer> word = it.next();
            it.remove();

            //Calculates the font size using min-max normalization
            int font = word.getValue();

            //Finds the range of word counts
            int range = max - min;
            //Sets the range to 1 if max and min are equal to prevent
            //dividing by 0
            if (range == 0) {
                range = 1;
            }

            font = ((font - min) * (maxFont - minFont)) / (range);
            font += minFont;

            //Outputs the scaled words based on their count
            out.println("<span class=\"f" + font + "\" title=\"count: "
                    + word.getValue() + "\">" + word.getKey() + "</span>");

        }
        //Closing html tags.
        out.println("</p>");
        out.println("</div>");
        out.println("</body>");
        out.println("</html>");

    }

    /**
     * Main method.
     *
     * @param args
     *            the command line arguments
     */
    public static void main(String[] args) {

        BufferedReader in = new BufferedReader(
                new InputStreamReader(System.in));

        System.out.println("Enter name for input file:");
        //Example: data/empty.txt
        String fileName;
        try {
            fileName = in.readLine();
        } catch (IOException e) {
            System.err.println("Error reading input");
            return;
        }

        BufferedReader input;
        try {
            input = new BufferedReader(new FileReader(fileName));
        } catch (IOException e) {
            System.err.println("Error opening file");
            return;
        }

        System.out.println("Enter name of output file:");
        //Example: data/empty.html
        String folder;
        try {
            folder = in.readLine();
        } catch (IOException e) {
            System.err.println("Error reading input");
            return;
        }

        PrintWriter out;
        try {
            out = new PrintWriter(new BufferedWriter(new FileWriter(folder)));
        } catch (IOException e) {
            System.err.println("Error outputting to file location");
            return;
        }

        System.out.println("Enter number of desired words in the word cloud:");
        int n = -1;

        //Runs until the user inputs a valid integer value for the word count
        while (n < 0) {
            String userCount = "";
            try {
                userCount = in.readLine();
            } catch (IOException e) {
                System.err.println("Error reading input");
            }
            try {
                n = Integer.parseInt(userCount);
                if (n < 0) {
                    System.out.println("Word count cannot be negative");
                    System.out.println("Please enter a positive integer:");
                }
            } catch (Exception e) {
                System.err.println("Error reading desired word count");
                return;
            }
        }

        //Creates a map to store words and the number of their occurrences.
        Map<String, Integer> count = new HashMap<>();

        Set<Character> separators = createSeparatorList();
        generateMap(input, separators, count);

        Comparator<Entry<String, Integer>> c1 = new CompareInteger();
        Comparator<Entry<String, Integer>> c2 = new CompareString();

        //Creates a map to store words sorted by their number of occurences
        Map<String, Integer> mDecreasing = new HashMap<>();

        Integer[] range = sort(count, c1, mDecreasing, n);

        //Put values into a tree map to sort map alphabetically
        Map<String, Integer> mAlphabetical = new TreeMap<>(mDecreasing);

        //Generate the tag cloud html file.
        header(out, fileName, n);
        tagCloud(out, range[0], range[1], n, mAlphabetical);

        //Closing
        try {
            input.close();
        } catch (IOException e) {
            System.err.println("Error closing input stream");
        }
        try {
            in.close();
        } catch (IOException e) {
            System.err.println("Error closing file");
        }

        out.close();

    }

}
