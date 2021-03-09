package es.unex.giiis.ribw.jgarciapft.printers;

import es.unex.giiis.ribw.jgarciapft.InvertedIndex;
import es.unex.giiis.ribw.jgarciapft.Occurrences;

import java.io.File;
import java.util.Map;

/**
 * Prints an inverted index to the console (standard output)
 *
 * @author Juan Pablo García Plaza Pérez (jgarciapft@alumnos.unex.es)
 */
public class ConsolePrinter implements IInvertedIndexPrinter {

    /**
     * Print an inverted index to the console (standard output)
     *
     * @param invertedIndex The token frequency dictionary to be printed
     */
    @Override
    public void print(InvertedIndex invertedIndex) {

        System.out.println("\n--- INVERTED INDEX (Token, Global frequency, Partial frequencies) --------------------------------------\n");

        // For each token print its occurrences

        for (Map.Entry<String, Occurrences> tokenEntry : invertedIndex.getInvertedIndex().entrySet()) {

            Occurrences occurrences = tokenEntry.getValue();

            // Print the token and the global frequency
            System.out.printf("%s [%d TOTAL]\n", tokenEntry.getKey(), occurrences.getGlobalFrequency());

            // For each occurrence print an unranked list of documents where the current token appears plus partial frequencies

            for (Map.Entry<Integer, Integer> occurrence : occurrences.getOccurrences().entrySet()) {

                String documentURL = invertedIndex.getDocumentIdentifierMapper().getDocumentURLByID(occurrence.getKey());
                /* Windows platforms use backslash (\) to delimit resources inside paths, which is a metacharacter in RegEx language.
                If the current platform uses backslashes escape it as a RegEx pattern to detect a single backslash */
                String[] split = documentURL.split(File.separator.equals("\\") ? "\\\\" : File.separator);
                String documentName = split[split.length - 1]; // The document name is the last resource in the URL

                // Print the document name, partial frequency and document URL
                System.out.printf("  ├ %s => %d hit(s) [%s]\n", documentName, occurrence.getValue(), documentURL);

            }
        }
    }

}
