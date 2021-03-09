package es.unex.giiis.ribw.jgarciapft.marshallers;

import es.unex.giiis.ribw.jgarciapft.IDocumentCatalogue;
import es.unex.giiis.ribw.jgarciapft.InvertedFile;
import es.unex.giiis.ribw.jgarciapft.Occurrences;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.Map;

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
     * @param invertedIndex            The dictionary of tokens to export to a file
     * @param documentIdentifierMapper Document identifier mapper to be serialized
     * @param outFile                  Where to write the serialized token dictionary
     */
    @Override
    public void marshall(Map<String, Occurrences> invertedIndex, IDocumentCatalogue documentIdentifierMapper, File outFile) {

        // Create an intermediate in memory inverted file representation (POJO) to be serialized

        InvertedFile invertedFile = new InvertedFile(invertedIndex, documentIdentifierMapper);

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
