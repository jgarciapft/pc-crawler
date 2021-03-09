package es.unex.giiis.ribw.jgarciapft;

import es.unex.giiis.ribw.jgarciapft.printers.ConsolePrinter;
import es.unex.giiis.ribw.jgarciapft.printers.IInvertedIndexPrinter;
import es.unex.giiis.ribw.jgarciapft.ranking.DescendingFrequencyRanking;
import es.unex.giiis.ribw.jgarciapft.ranking.RankingCriterion;
import es.unex.giiis.ribw.jgarciapft.utils.NormalizationUtils;

import java.io.File;
import java.util.Map;
import java.util.Scanner;
import java.util.TreeSet;

/**
 * Interactive commandline interface (CLI) to implement user operations. It depends on the exported inverted index
 * representation exported by the Crawler class
 *
 * @author Juan Pablo García Plaza Pérez (jgarciapft@alumnos.unex.es)
 */
public class CrawlerCLI {

    private final InvertedIndex invertedIndex; // The inverted index used to carry on operations
    private RankingCriterion rankingCriterion; // Default ranking criterion to rank query results
    private IInvertedIndexPrinter indexPrinter; // Default printer strategy to print the inverted index for the user

    /**
     * Initializes the CLI with an inverted index and default ranking criterion and index printer
     *
     * @param invertedIndex An already built inverted index to be used to carry on operations
     */
    public CrawlerCLI(InvertedIndex invertedIndex) {
        this.invertedIndex = invertedIndex;

        rankingCriterion = new DescendingFrequencyRanking();
        indexPrinter = new ConsolePrinter();
    }

    /**
     * Starts an interactive session with the user. The user can exit the CLI selection the "exit" option
     */
    public void interactiveCLI() {

        Scanner scanner = new Scanner(System.in); // To read user input
        int selectedOption; // Currently user selected option

        // Maintain an interactive session with the user until he/she chooses to exit

        do {

            // Print options and prompt to enable option selection

            printOptions();
            prompt();

            selectedOption = scanner.nextInt();

            // Perform the user selected option

            switch (selectedOption) {

                case OptionCodes.RANKED_TERM_SEARCH: // Perform a ranked single term search

                    prompt();
                    System.out.print("[Term to search:] ");

                    rankedTermSearch(scanner.next());

                    break;

                case OptionCodes.PRINT_INVERTED_INDEX: // Print the entire inverted index

                    printInvertedIndex();
                    break;
            }

        } while (selectedOption != OptionCodes.EXIT);

        scanner.close();
    }

    /**
     * Perform a ranked single term search. The input term is normalized so that it can match with the inverted index,
     * which is also normalized. The output result contains the global and partial frequencies
     *
     * @param term The term to search, not necessarily normalized yet
     */
    private void rankedTermSearch(String term) {

        if (term == null) return;

        String normalizedInputTerm = NormalizationUtils.normalizeStringNFD(term); // Get normalized representation

        // If there's a match rank the occurrences with the ranking criterion, otherwise show no results were found

        if (!normalizedInputTerm.isEmpty() && invertedIndex.getInvertedIndex().containsKey(normalizedInputTerm)) {

            // Retrieve unordered occurrences
            Occurrences occurrences = invertedIndex.getInvertedIndex().get(normalizedInputTerm);

            // Rank the occurrences using the ranking criterion (a Comparator)

            TreeSet<Map.Entry<Integer, Integer>> supportTree = new TreeSet<>(rankingCriterion);
            supportTree.addAll(occurrences.getOccurrences().entrySet()); // Occurrences are ranked when added

            // Print the ranked results

            System.out.println("\n[Ranking criterion - " + rankingCriterion.getClass().getSimpleName() + "]");
            System.out.printf("\n%s [%d TOTAL]\n", normalizedInputTerm, occurrences.getGlobalFrequency());

            for (Map.Entry<Integer, Integer> rankedResult : supportTree) {

                String documentFullPath = invertedIndex.getDocumentIdentifierMapper().getDocumentURLByID(rankedResult.getKey());
                String[] split = documentFullPath.split(File.separator.equals("\\") ? "\\\\" : File.separator);
                String documentName = split[split.length - 1];

                System.out.printf("  ├ %s => %d hit(s) [%s]\n", documentName, rankedResult.getValue(), documentFullPath);
            }
        } else {
            System.out.println("\nNo results found");
        }

    }

    /**
     * Use the index printer to print the inverted index
     */
    private void printInvertedIndex() {
        indexPrinter.print(invertedIndex);
    }

    /**
     * Print the available options that the CLI can perform
     */
    private void printOptions() {
        System.out.println();
        System.out.println("--- PC-Crawler ------------------------------------------------");
        System.out.println();
        System.out.println("[" + OptionCodes.RANKED_TERM_SEARCH + "] Ranked term search");
        System.out.println("[" + OptionCodes.PRINT_INVERTED_INDEX + "] Print inverted index");
        System.out.println();
        System.out.println("[" + OptionCodes.EXIT + "] EXIT");
        System.out.println();
    }

    /**
     * Print a custom prompt to hint the user he/she can enter some input
     */
    private void prompt() {
        System.out.print("pc-crawler> ");
    }

    public RankingCriterion getRankingHeuristic() {
        return rankingCriterion;
    }

    public void setRankingHeuristic(RankingCriterion rankingCriterion) {
        this.rankingCriterion = rankingCriterion;
    }

    public IInvertedIndexPrinter getIndexPrinter() {
        return indexPrinter;
    }

    public void setIndexPrinter(IInvertedIndexPrinter indexPrinter) {
        this.indexPrinter = indexPrinter;
    }

    /**
     * Numeric codes associated with each option presented by the CLI
     */
    private static class OptionCodes {

        private static final int EXIT = 0;
        private static final int RANKED_TERM_SEARCH = 1;
        private static final int PRINT_INVERTED_INDEX = 2;

    }
}
