package es.unex.giiis.ribw.jgarciapft.loaders;

import es.unex.giiis.ribw.jgarciapft.InvertedFile;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;

/**
 * Deserializes an inverted file from a source file
 *
 * @author Juan Pablo García Plaza Pérez (jgarciapft@alumnos.unex.es)
 */
public class InvertedFileLoader implements IInvertedFileLoader {

    /**
     * Deserializes a serialized token dictionary from a source file
     *
     * @return The imported token dictionary or null if an error arose during deserialization
     */
    public InvertedFile load(File marshalledInvertedFile) {

        InvertedFile loadedInvertedFile = null;

        // Check that the exported file exists and is indeed a readable file

        if (marshalledInvertedFile != null && marshalledInvertedFile.exists() &&
                marshalledInvertedFile.isFile() && marshalledInvertedFile.canRead()) {

            // Load the inverted file from the source file

            try {
                ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream(marshalledInvertedFile));
                loadedInvertedFile = (InvertedFile) objectInputStream.readObject();
                objectInputStream.close();
            } catch (Exception e) {
                System.err.println("[ERROR] An error occurred loading the serialized inverted file from a file");
                e.printStackTrace();
                return null;
            }
        }

        return loadedInvertedFile;
    }

}
