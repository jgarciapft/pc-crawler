package es.unex.giiis.ribw.jgarciapft.marshallers;

import es.unex.giiis.ribw.jgarciapft.IDocumentCatalogue;
import es.unex.giiis.ribw.jgarciapft.Occurrences;

import java.io.File;
import java.util.Map;

/**
 * Specifies a strategy to marshall (a.k.a serialize) an inverted index, also called creating an inverted file
 *
 * @author Juan Pablo García Plaza Pérez (jgarciapft@alumnos.unex.es)
 */
public interface IInvertedIndexMarshaller {

    /**
     * Serialize a inverted index to an output file (inverted file)
     *
     * @param invertedIndex            Inverted index to be serialized
     * @param documentIdentifierMapper Document identifier mapper to be serialized
     * @param outFile                  Output inverted file
     */
    void marshall(Map<String, Occurrences> invertedIndex, IDocumentCatalogue documentIdentifierMapper, File outFile);

}
