package es.unex.giiis.ribw.jgarciapft;

import java.io.Serializable;
import java.util.Map;
import java.util.TreeMap;

/**
 * A class for storing the total and partial frequencies for a token within the document's catalogue.
 * Each partial frequency is addressed by the numeric document identifier and not by its URL (dictionary data structure)
 *
 * @author Juan Pablo García Plaza Pérez (jgarciapft@alumnos.unex.es)
 */
public class Occurrences implements Serializable {

    private int globalFrequency; // The total amount of times a certain token appears inside all the documents
    private final Map<Integer, Integer> occurrences; // Holds the local count for each document

    public Occurrences() {
        globalFrequency = 0;
        occurrences = new TreeMap<>();
    }

    /**
     * Instantiate a occurrences objets with an initial occurrence. This first occurrence is immediately computed
     *
     * @param documentID The ID of the document where the fist occurrence occurred
     */
    public Occurrences(int documentID) {
        globalFrequency = 0;
        occurrences = new TreeMap<>();

        // Compute this first occurrence immediately
        computeOccurrenceInDocument(documentID);
    }

    /**
     * Register one occurrence within the document identified by documentID. This also increments the global frequency
     *
     * @param documentID The ID of the document where the occurrence occurred
     */
    public void computeOccurrenceInDocument(int documentID) {

        // If the document ID is already present increment its frequency by 1, otherwise create the entry with a frequency of 1

        if (occurrences.containsKey(documentID))
            occurrences.compute(documentID, (currentDocID, currentFrequency) -> currentFrequency + 1);
        else
            occurrences.put(documentID, 1);

        globalFrequency++; // Always increment the global frequency by 1

    }

    public int getGlobalFrequency() {
        return globalFrequency;
    }

    public Map<Integer, Integer> getOccurrences() {
        return occurrences;
    }
}
