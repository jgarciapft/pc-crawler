package es.unex.giiis.ribw.jgarciapft.marshallers;

import es.unex.giiis.ribw.jgarciapft.InvertedFile;
import es.unex.giiis.ribw.jgarciapft.InvertedIndex;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;

/**
 * Serializes an inverted index and exports it to a file (inverted file) so it can be restored
 *
 * @author Juan Pablo García Plaza Pérez (jgarciapft@alumnos.unex.es)
 */
public class InvertedIndexMarshaller implements IInvertedIndexMarshaller {

    /**
     * Serializes the inverted index (token dictionary) and exports it to a file so it can be restored.
     * If the file already exists it's overwritten
     *
     * @param invertedIndex The inverted index to be serialized
     * @param outFile       Where to write the serialized token dictionary
     */
    @Override
    public void marshall(InvertedIndex invertedIndex, File outFile) {

        // Create an inverted file POJO from an InvertedIndex transactional object

        InvertedFile invertedFile = new InvertedFile(invertedIndex);

        // Serialize the POJO representation

        try {
            ObjectOutputStream objectOutputStream = new ObjectOutputStream(new FileOutputStream(outFile));
            objectOutputStream.writeObject(invertedFile);
            objectOutputStream.close();
        } catch (Exception e) {
            System.err.println("[ERROR] Something went wrong serializing and exporting the inverted index");
            e.printStackTrace();
        }
    }

}
