package es.unex.giiis.ribw.jgarciapft.marshallers;

import java.io.File;
import java.io.FileOutputStream;
import java.io.ObjectOutputStream;
import java.util.Map;

/**
 * Serializes the token dictionary and exports it to a file so it can be restored
 *
 * @author Juan Pablo García Plaza Pérez (jgarciapft@alumnos.unex.es)
 */
public class IndexFileMarshaller implements IDictionaryMarshaller<String, Object> {

    /**
     * Serializes the token dictionary and exports it to a file so it can be restored. If the file already exists it's
     * overwritten
     *
     * @param tokenDictionary The dictionary of tokens to export to a file
     * @param outFile         Where to write the serialized token dictionary
     */
    @Override
    public void marshall(Map<String, Object> tokenDictionary, File outFile) {
        try {
            ObjectOutputStream objectOutputStream =
                    new ObjectOutputStream(new FileOutputStream(outFile));
            objectOutputStream.writeObject(tokenDictionary);
            objectOutputStream.close();
        } catch (Exception e) {
            System.err.println("[ERROR] Something went wrong serializing and exporting the token dictionary");
            e.printStackTrace();
        }
    }

}
