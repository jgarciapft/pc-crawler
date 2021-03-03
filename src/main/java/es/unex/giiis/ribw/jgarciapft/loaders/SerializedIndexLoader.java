package es.unex.giiis.ribw.jgarciapft.loaders;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;
import java.util.Map;
import java.util.TreeMap;

/**
 * Deserializes a serialized token dictionary from a source file
 *
 * @author Juan Pablo García Plaza Pérez (jgarciapft@alumnos.unex.es)
 */
public class SerializedIndexLoader implements IDictionaryLoader<String, Object> {

    /**
     * Deserializes a serialized token dictionary from a source file
     *
     * @return The imported token dictionary or null if an error arose during deserialization
     */
    @SuppressWarnings("unchecked")
    @Override
    public Map<String, Object> load(File marshalledTokenDictionaryFile) {

        Map<String, Object> loadedDictionary = null;

        // Check that the exported file exists and is indeed a readable file

        if (marshalledTokenDictionaryFile != null && marshalledTokenDictionaryFile.exists() &&
                marshalledTokenDictionaryFile.isFile() && marshalledTokenDictionaryFile.canRead()) {

            // Load the token dictionary from the file

            try {
                ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream(marshalledTokenDictionaryFile));
                loadedDictionary = (TreeMap<String, Object>) objectInputStream.readObject();
                objectInputStream.close();
            } catch (Exception e) {
                System.err.println("[ERROR] An error occurred loading the serialized token dictionary from a file");
                e.printStackTrace();
                return null;
            }
        }

        return loadedDictionary;
    }

}
