package es.unex.giiis.ribw.jgarciapft.marshallers;

import es.unex.giiis.ribw.jgarciapft.InvertedIndex;

import java.io.File;

/**
 * Specifies a strategy to marshall (a.k.a serialize) an inverted index, also called creating an inverted file
 *
 * @author Juan Pablo García Plaza Pérez (jgarciapft@alumnos.unex.es)
 */
public interface IInvertedIndexMarshaller {

    /**
     * Serialize a inverted index to an output file (inverted file)
     *
     * @param invertedIndex The inverted index to be serialized
     * @param outFile       Output inverted file
     */
    void marshall(InvertedIndex invertedIndex, File outFile);

}
