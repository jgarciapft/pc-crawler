package es.unex.giiis.ribw.jgarciapft.printers;

import java.util.Map;

/**
 * Specifies a strategy to print a dictionary
 *
 * @param <K> Dictionary's keys type
 * @param <V> Dictionary's values type
 * @author Juan Pablo García Plaza Pérez (jgarciapft@alumnos.unex.es)
 */
public interface IDictionaryPrinter<K, V> {

    /**
     * Execute the printing strategy
     *
     * @param dictionary Dictionary to be printed somewhere
     */
    void print(Map<K, V> dictionary);

}
