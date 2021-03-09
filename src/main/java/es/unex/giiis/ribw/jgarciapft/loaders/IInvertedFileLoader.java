package es.unex.giiis.ribw.jgarciapft.loaders;

import es.unex.giiis.ribw.jgarciapft.InvertedFile;

import java.io.File;

/**
 * Specifies a loading strategy to load into memory an inverted file (static representation of an inverted index)
 *
 * @author Juan Pablo García Plaza Pérez (jgarciapft@alumnos.unex.es)
 */
public interface IInvertedFileLoader {

    /**
     * @param invertedFile Source file
     * @return An inverted index contained within this POJO
     */
    InvertedFile load(File invertedFile);

}
