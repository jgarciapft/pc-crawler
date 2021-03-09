package es.unex.giiis.ribw.jgarciapft.printers;

import es.unex.giiis.ribw.jgarciapft.InvertedIndex;

/**
 * Specifies a strategy to print an inverted index
 *
 * @author Juan Pablo García Plaza Pérez (jgarciapft@alumnos.unex.es)
 */
public interface IInvertedIndexPrinter {

    /**
     * Execute the printing strategy
     *
     * @param invertedIndex Inverted index to be printed somewhere
     */
    void print(InvertedIndex invertedIndex);

}
