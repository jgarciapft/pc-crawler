package es.unex.giiis.ribw.jgarciapft;

import java.util.Map;

/**
 * Unmodifiable representation of a built inverted index for the purpose of exporting it out of the crawler and
 * enabling other operations, such as querying the index
 *
 * @author Juan Pablo García Plaza Pérez (jgarciapft@alumnos.unex.es)
 */
public class InvertedIndex {

    private final Map<String, Occurrences> invertedIndex;
    private final IDocumentCatalogue documentIdentifierMapper;

    public InvertedIndex(Map<String, Occurrences> invertedIndex, IDocumentCatalogue documentIdentifierMapper) {
        this.invertedIndex = invertedIndex;
        this.documentIdentifierMapper = documentIdentifierMapper;
    }

    public Map<String, Occurrences> getInvertedIndex() {
        return invertedIndex;
    }

    public IDocumentCatalogue getDocumentIdentifierMapper() {
        return documentIdentifierMapper;
    }

}
