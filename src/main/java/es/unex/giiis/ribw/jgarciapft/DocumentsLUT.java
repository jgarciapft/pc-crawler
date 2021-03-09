package es.unex.giiis.ribw.jgarciapft;

import java.util.ArrayList;
import java.util.Objects;

/**
 * LUT table implementation of the document's catalogue backed by an ArrayList. It has O(1) complexity for both: adding
 * new documents and retrieving a document by ID. The index inside the array serves as the numeric document identifier
 *
 * @author Juan Pablo García Plaza Pérez (jgarciapft@alumnos.unex.es)
 */
public class DocumentsLUT implements IDocumentCatalogue {

    private final ArrayList<String> documentsLUT;

    public DocumentsLUT() {
        documentsLUT = new ArrayList<>();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int addDocument(String documentURL) {

        // Check that the document URL is not null and has some content. -1 is the error code

        if (documentURL == null || documentURL.length() == 0) return -1;

        int nextDocumentID = documentsLUT.size(); // The array length becomes the ID for the new catalogued document
        documentsLUT.add(documentURL);

        return nextDocumentID;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getDocumentURLByID(int documentID) {

        // Check that the ID is valid. An ID is valid if it's a valid index inside de array that backs this LUT

        if (documentsLUT.size() <= documentID) return null;

        return documentsLUT.get(documentID);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        DocumentsLUT that = (DocumentsLUT) o;
        return Objects.equals(documentsLUT, that.documentsLUT);
    }

    @Override
    public int hashCode() {
        return Objects.hash(documentsLUT);
    }

}
