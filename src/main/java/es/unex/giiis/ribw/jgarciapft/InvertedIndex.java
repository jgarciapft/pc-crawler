package es.unex.giiis.ribw.jgarciapft;

import java.io.Serializable;
import java.util.Map;

/**
 * Unmodifiable representation of a built inverted index for the purpose of exporting it out of the crawler and
 * enabling other operations, such as querying the index
 *
 * @author Juan Pablo García Plaza Pérez (jgarciapft@alumnos.unex.es)
 */
public class InvertedIndex implements Serializable {

    private final Map<String, Occurrences> invertedIndex;
    private final IDocumentCatalogue documentCatalogue;

    public InvertedIndex(Map<String, Occurrences> invertedIndex, IDocumentCatalogue documentCatalogue) {
        this.invertedIndex = invertedIndex;
        this.documentCatalogue = documentCatalogue;
    }

    public Map<String, Occurrences> getInvertedIndex() {
        return invertedIndex;
    }

    public IDocumentCatalogue getDocumentCatalogue() {
        return documentCatalogue;
    }

}
