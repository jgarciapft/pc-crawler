package es.unex.giiis.ribw.jgarciapft.marshallers;

import java.io.File;
import java.util.Map;

/**
 * Specifies a strategy to marshall (a.k.a serialize) a dictionary
 *
 * @param <K> Dictionary's keys type
 * @param <V> Dictionary's values type
 * @author Juan Pablo García Plaza Pérez (jgarciapft@alumnos.unex.es)
 */
public interface IDictionaryMarshaller<K, V> {

    /**
     * Serialize a dictionary to an output file
     *
     * @param dictionary Dictionary to be serialized
     * @param outFile    Output file
     */
    void marshall(Map<K, V> dictionary, File outFile);

}
