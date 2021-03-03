package es.unex.giiis.ribw.jgarciapft.loaders;

import java.io.File;
import java.util.Map;

/**
 * Specifies a loading strategy to load a dictionary (java.util.Map)
 *
 * @param <K> Dictionary's keys type
 * @param <V> Dictionary's values type
 * @author Juan Pablo García Plaza Pérez (jgarciapft@alumnos.unex.es)
 */
public interface IDictionaryLoader<K, V> {

    /**
     * Loads a dictionary from a source file
     *
     * @param source Material to build the dictionary
     * @return The loaded dictionary, or null if something went wrong
     */
    Map<K, V> load(File source);

}
