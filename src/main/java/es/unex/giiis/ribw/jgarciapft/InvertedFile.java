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
    private IDocumentCatalogue documentCatalogue;

    public InvertedFile(Map<String, Occurrences> invertedIndex, IDocumentCatalogue documentCatalogue) {
        this.invertedIndex = invertedIndex;
        this.documentCatalogue = documentCatalogue;
    }

    public InvertedFile(InvertedIndex source) {
        invertedIndex = source.getInvertedIndex();
        documentCatalogue = source.getDocumentCatalogue();
    }

    public Map<String, Occurrences> getInvertedIndex() {
        return invertedIndex;
    }

    public void setInvertedIndex(Map<String, Occurrences> invertedIndex) {
        this.invertedIndex = invertedIndex;
    }

    public IDocumentCatalogue getDocumentCatalogue() {
        return documentCatalogue;
    }

    public void setDocumentCatalogue(IDocumentCatalogue documentCatalogue) {
        this.documentCatalogue = documentCatalogue;
    }
}
