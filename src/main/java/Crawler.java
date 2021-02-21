/**
 * RIBW 2020/21 - ACTIVIDAD 2: Inicio de un PC-Crawler
 * <p>
 * Esta es una implementación iterativa de un crawler de archivos de texto pensado para operar en sistemas de archivos
 * locales. A partir de una ruta (absoluta o relativa a la ruta donde se encuentra este programa, dependiente del SO)
 * el crawler explorará en profundidad la jerarquía de carpetas en busca de archivos de texto compatbibles
 * (ver ACCEPTED_FILE_EXTENSIONS) e indexará su contenido. Al final de la ejecución imprime por pantalla la frencuencia
 * con la que aparece cada término y exporta el resultado como un objeto Java serializado 'CRAWLERINDEX.idx'. Con el
 * argumento opcional -I es posible cargar un resultado exportado y saltarse la exploración de la jerarquía de carpetas.
 * Por supuesto, los resultados serán válidos solo si no se ha modificado el contenido de la jerarquía de carpetas.
 * Cada archivo se divide en términos según los carácteres separadores de texto (ver TOKEN_DELIMITERS).
 * <p>
 * Para contabilizar la frecuencia de cada término se emplea un montículo ordenado lexicográficamente por el término
 * (java.util.TreeMap). Cada entrada almacena el término y su frencuencia.
 * <p>
 * El algoritmo de búsqueda iterativo está implementado con una cola FIFO (clase java.util.LinkedList) de ficheros.
 * Cada archivo puede ser una carpeta o un documento. Si es una carpeta, entonces la explora y añade todos sus archivos
 * a la cola. El proceso se repite  hasta que se hayan explorado todas las carpetas y documentos.
 * <p>
 * Cada archivo se procesa línea a línea, y cada línea pasa por un proceso de normalización antes de ser dividida en
 * términos. 1) Aplicar normalización Unicode Normalization Form D (NFD, Canonical Decomposition) para generar una
 * representación de caracteres no ASCII equivalente dentro del juego de caracteres ASCII. Esto incluye caracteres
 * acentuados y 'ñ'. 2) Convertir todas las letras a minúsculas. Los términos "casa" y "Casa" se consideran iguales.
 * <p>
 * DELIMITADORES DE TÉRMINOS:
 * * Signos de puntuación, aplicables a ctado -> [ESPACIO].,:;!¡¿?\/
 * * Caracteres comúnmente utilizados en lenguajes de programación para separar entidades -> ()[]{}\t|"#*-+=
 * <p>
 * He considerado que los caracteres "guion" (-) y "barra baja" (_) no deben delimitar términos porque en ocasiones son
 * utilizados para unir palabras (Ej: pre-procesar, TOKEN_DELIMITERS)
 * <p>
 * EXTENSIONES SOPORTADAS: txt|java|c|cpp|py
 *
 * <p>
 * Autor: Juan Pablo García Plaza Pérez (jgarciapft@alumnos.unex.es)
 */

import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.text.Normalizer;
import java.util.*;

public class Crawler {

    // RegEx specifying file extensions that a detected file should match in order to be processed by this crawler
    private static final String ACCEPTED_FILE_EXTENSIONS = "txt|java|c|cpp|py";
    // Which characters delimits tokens in accepted files processed by this crawler
    private static final String TOKEN_DELIMITERS = " .,:;!¡¿?\\/()[]{}\t|\"#*-+="; // Excludes (-) and (_)
    // Name of the file containing the serialized token frequency dictionary
    public static final String SERIALIZED_DICTIONARY_FILENAME = "CRAWLERINDEX.idx";

    public static void main(String[] args) {

        // Should provide the path to the root resource from which start crawling, either a directory or a file

        if (args.length < 1 || args[0].equals("--help")) {
            showHelp();
            return;
        }

        // Invoke the crawler main event loop. The root path will always be at the end of the args array

        exploreRoot(args[args.length - 1], args[0].equals("-I"));

    }

    /**
     * Perform a full depth search for files starting from the given root path and exploring all sub-directories
     *
     * @param rootPath               The starting point in the system's filesystem
     * @param loadDictionaryFromFile If the token dictionary should be loaded from the default exported file
     */
    private static void exploreRoot(String rootPath, boolean loadDictionaryFromFile) {

        // FIFO list of captured files
        LinkedList<File> documentsQueue = new LinkedList<>();
        /* Ordered dictionary of tokens. Entries store the frequency of appearance of each token inside files within the
         * provided root hierarchy. Entries are ordered following lexicographically order of the tokens */
        Map<String, Object> tokenFrequencyDictionary =
                loadDictionaryFromFile ? loadSerializedDictionary() : new TreeMap<>();

        // Guard against any error while loading the dictionary (when indicated). Create an empty dictionary

        if (loadDictionaryFromFile && tokenFrequencyDictionary == null) tokenFrequencyDictionary = new TreeMap<>();

        // If no exported dictionary file is being used, build one

        if (!loadDictionaryFromFile) {

            // Add the root element to the file's queue

            File currentFile = new File(rootPath);
            documentsQueue.add(currentFile);

            /*
             * Iterative implementation of the directory hierarchy search algorithm using a FIFO queue.
             * The whole hierarchy is searched for files starting from the given root path and exploring all sub-directories.
             * The files will be processed in the order presented to the crawler by the filesystem.
             */

            while ((currentFile = documentsQueue.pollLast()) != null) {

                // CASE 1 - Current file can't be processed. The file either doesn't exist or can't be read

                if (!currentFile.exists() || !currentFile.canRead()) {
                    System.err.println("Unable to read (" + currentFile + ")");
                }

                // CASE 2 - Current file is a directory. Add its contents to the queue and continue with the next iteration

                else if (currentFile.isDirectory()) {

                    File[] childrenFiles = currentFile.listFiles(); // Retrieve all the files within the directory

                    // Check the directory isn't empty before adding the retrieved files to the queue

                    if (childrenFiles != null && childrenFiles.length > 0)
                        documentsQueue.addAll(Arrays.asList(childrenFiles));

                }

                // CASE 3 - Current file is actually a readable file. Break down the file into tokens and index them

                else if (currentFile.isFile()) try {

                    // Check that the current file can be processed by this crawler

                    if (fileEndsWithAcceptedExtension(currentFile)) {

                        BufferedReader bufferedReader = new BufferedReader(new FileReader(currentFile));
                        String line;

                        // Read the file line by line, normalize it and break it down into tokens at delimiters

                        while ((line = bufferedReader.readLine()) != null) {

                            /*
                             * Normalize each line before breaking it into tokens. The normalization pipeline first applies
                             * Unicode Normalization Form D (NFD, Canonical Decomposition) to get an equivalent representation
                             * of non-ASCII characters in the ASCII character set, then performs a lowercase replacement
                             *
                             * More info about Unicode Normalization within the Java JDK:
                             *
                             * https://stackoverflow.com/questions/4122170/java-change-%C3%A1%C3%A9%C5%91%C5%B1%C3%BA-to-aeouu
                             * https://docs.oracle.com/javase/8/docs/api/java/text/Normalizer.Form.html#NFD
                             * https://docs.oracle.com/javase/tutorial/i18n/text/normalizerapi.html
                             */

                            String normalisedLine = Normalizer.normalize(line, Normalizer.Form.NFD)
                                    // Remove the non-ASCII characters produced as a result of NFD Normalization
                                    .replaceAll("[^\\p{ASCII}]", "")
                                    .toLowerCase(Locale.ROOT);

                            // Break down the normalized line into tokens using the statically specified token delimiter list
                            StringTokenizer tokenizer = new StringTokenizer(normalisedLine, TOKEN_DELIMITERS);

                            /*
                             * Each token either creates a new entry with frequency 1 in the dictionary if it wasn't already
                             * included, or increments its frequency count by 1
                             */

                            while (tokenizer.hasMoreTokens()) {

                                String currentToken = tokenizer.nextToken();

                                if (tokenFrequencyDictionary.containsKey(currentToken)) {
                                    // Increment frequency in 1
                                    tokenFrequencyDictionary.compute(currentToken,
                                            (token, tokenFreqObj) -> new Integer(((Integer) tokenFreqObj).intValue() + 1));
                                } else {
                                    // Create new token entry keeping the order of the entries
                                    tokenFrequencyDictionary.put(currentToken, new Integer(1));
                                }
                            }
                        }

                        bufferedReader.close();

                    } else {
                        System.out.println("[INFO] Invalid extension (" + getFileExtension(currentFile) + "). Skipping (" + currentFile + ")");
                    }
                } catch (FileNotFoundException e) {
                    System.err.println("[ERROR] The file (" + currentFile + ") disappeared before it could be processed");
                } catch (IOException e) {
                    e.printStackTrace();
                }

            }
        }

        // After all files have been processed print the token dictionary

        System.out.println("\n--- TOKEN DICTIONARY (Token, Frequency) ----------------------------------------------------------------\n");
        for (Map.Entry<String, Object> dictionaryEntry : tokenFrequencyDictionary.entrySet())
            System.out.printf("\t%s : %s\n", StringUtils.rightPad(dictionaryEntry.getKey(), 25), dictionaryEntry.getValue());
        System.out.println("\n--------------------------------------------------------------------------------------------------------\n");

        // Export the built token frequency dictionary

        System.out.println("[INFO] Exporting the built token dictionary");
        exportSerializedDictionary(tokenFrequencyDictionary);

    }

    /**
     * Extract the extension from a file. A file extension is the last sequence of characters that result from dividing
     * a file name at each dot (.)
     *
     * @param file Input file
     * @return Input file's file extension, or null if it isn't a file or the file doesn't exists.
     */
    private static String getFileExtension(File file) {

        // Check that the input file exists and is indeed a file, not a directory

        if (file.exists() && file.isFile()) {

            String[] split = file.getName().split("\\."); // Split the file name at each dot (.)
            return split[split.length - 1]; // Return the last split

        } else {
            return null;
        }
    }

    /**
     * @param file Input file
     * @return True if the file ends with an accepted extension, false otherwise
     * @see Crawler#ACCEPTED_FILE_EXTENSIONS
     */
    private static boolean fileEndsWithAcceptedExtension(File file) {

        String fileExt = getFileExtension(file);

        // Checks it's an accepted extension matching the statically specified regular expression

        if (fileExt != null) return fileExt.matches(ACCEPTED_FILE_EXTENSIONS);
        else return false;
    }

    /**
     * Serializes the token dictionary and exports it to a file so it can be restored. If the file already exists it's
     * overwritten
     *
     * @param tokenDictionary The dictionary of tokens to export
     * @see Crawler#SERIALIZED_DICTIONARY_FILENAME
     */
    private static void exportSerializedDictionary(Map<String, Object> tokenDictionary) {
        try {
            ObjectOutputStream objectOutputStream =
                    new ObjectOutputStream(new FileOutputStream(SERIALIZED_DICTIONARY_FILENAME));
            objectOutputStream.writeObject(tokenDictionary);
            objectOutputStream.close();
        } catch (Exception e) {
            System.err.println("[ERROR] Something went wrong serializing and exporting the token dictionary");
            e.printStackTrace();
        }
    }

    /**
     * Deserializes a token dictionary from the expected exported file. The file should be located in the current directory
     *
     * @return The imported token dictionary or null if an error arose during deserialization
     * @see Crawler#SERIALIZED_DICTIONARY_FILENAME
     */
    @SuppressWarnings("unchecked")
    private static Map<String, Object> loadSerializedDictionary() {

        Map<String, Object> loadedDictionary = null;
        File expectedFile = new File(SERIALIZED_DICTIONARY_FILENAME);

        // Check that the exported file exists and is indeed a readable file

        if (expectedFile.exists() && expectedFile.isFile() && expectedFile.canRead()) {

            // Load the token dictionary from the file

            try {
                ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream(SERIALIZED_DICTIONARY_FILENAME));
                loadedDictionary = (TreeMap<String, Object>) objectInputStream.readObject();
                objectInputStream.close();
            } catch (Exception e) {
                System.err.println("[ERROR] An error occurred loading the token dictionary from a file");
                e.printStackTrace();
                return null;
            }
        }

        return loadedDictionary;

    }

    /**
     * Print to standard out this program's help info
     */
    private static void showHelp() {
        System.out.println("USAGE\n" +
                "\n" +
                "\t(1) pc-crawler [-I] root-path\n" +
                "\t(2) pc-crawler --help\n" +
                "\n" +
                "SYNOPSIS\n" +
                "\n" +
                "\t* root-path: It can either expressed as a relative or full path (according to your OS), and it can either be a directory hierarchy or a file\n" +
                "\t* -I: Load an already built index 'CRAWLERINDEX.idx' located at the specified root\n" +
                "\t* --help: Invoke this help\n" +
                "\n" +
                "DESCRIPTION\n" +
                "\n" +
                "This is an iterative implementation of a crawler of text files intended to operate on local filesystems. This crawler will attempt to explore\n" +
                "a given path and index the content of files with a compatible file extension (see ACCEPTED FILE EXTENSIONS), then it will show the frequency\n" +
                "of each indexed token (see TOKEN DELIMITERS)\n" +
                "\n" +
                "SEE ALSO\n" +
                "\n" +
                "\tACCEPTED FILE EXTENSIONS: " + ACCEPTED_FILE_EXTENSIONS + "\n" +
                "\tTOKEN DELIMITERS: " + TOKEN_DELIMITERS + "\n"
        );
    }

}
