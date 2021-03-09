package es.unex.giiis.ribw.jgarciapft;

import java.io.Serializable;
import java.util.Map;

/**
 * Serializable representation of an inverted index, also called an inverted file
 *
 * @author Juan Pablo García Plaza Pérez (jgarciapft@alumnos.unex.es)
 */
public class InvertedFile implements Serializable {

    private Map<String, Occurrences> invertedIndex;
    private IDocumentCatalogue documentIdentifierMapper;

    public InvertedFile(Map<String, Occurrences> invertedIndex, IDocumentCatalogue documentIdentifierMapper) {
        this.invertedIndex = invertedIndex;
        this.documentIdentifierMapper = documentIdentifierMapper;
    }

    public Map<String, Occurrences> getInvertedIndex() {
        return invertedIndex;
    }

    public void setInvertedIndex(Map<String, Occurrences> invertedIndex) {
        this.invertedIndex = invertedIndex;
    }

    public IDocumentCatalogue getDocumentIdentifierMapper() {
        return documentIdentifierMapper;
    }

    public void setDocumentIdentifierMapper(IDocumentCatalogue documentIdentifierMapper) {
        this.documentIdentifierMapper = documentIdentifierMapper;
    }
}
