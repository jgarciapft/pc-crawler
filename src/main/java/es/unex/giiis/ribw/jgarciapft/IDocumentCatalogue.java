package es.unex.giiis.ribw.jgarciapft;

import java.io.Serializable;

/**
 * Implementation agnostic definition of the operations that should support a document's catalogue. The purpose of the
 * catalogue is to establish a bijection between document URLs and numeric document identifiers, but only URLs can be
 * queried from identifiers
 *
 * @author Juan Pablo García Plaza Pérez (jgarciapft@alumnos.unex.es)
 */
public interface IDocumentCatalogue extends Serializable {

    /**
     * Add a new document identified by its URL to the catalogue
     *
     * @param documentURL The URL of the new document
     * @return The associated numeric identifier for the new document, or -1 in the event of an error
     */
    int addDocument(String documentURL);

    /**
     * Query a document URL associated with a document ID
     *
     * @param documentID Numeric document ID
     * @return The document URL associated with this document ID, or null if no such mapping exists
     */
    String getDocumentURLByID(int documentID);

}
