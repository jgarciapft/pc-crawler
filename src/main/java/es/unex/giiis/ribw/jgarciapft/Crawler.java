package es.unex.giiis.ribw.jgarciapft;

import es.unex.giiis.ribw.jgarciapft.loaders.IDictionaryLoader;
import es.unex.giiis.ribw.jgarciapft.loaders.InverseThesaurusLoader;
import es.unex.giiis.ribw.jgarciapft.loaders.SerializedIndexLoader;
import es.unex.giiis.ribw.jgarciapft.loaders.ThesaurusLoader;
import es.unex.giiis.ribw.jgarciapft.marshallers.IDictionaryMarshaller;
import es.unex.giiis.ribw.jgarciapft.marshallers.IndexFileMarshaller;
import es.unex.giiis.ribw.jgarciapft.printers.ConsolePrinter;
import es.unex.giiis.ribw.jgarciapft.printers.IDictionaryPrinter;
import es.unex.giiis.ribw.jgarciapft.utils.FileExtensionUtils;
import es.unex.giiis.ribw.jgarciapft.utils.NormalizationUtils;

import java.io.*;
import java.util.*;

import static es.unex.giiis.ribw.jgarciapft.Config.*;

/**
 * PC-Crawler implementation
 *
 * @author Juan Pablo García Plaza Pérez (jgarciapft@alumnos.unex.es)
 */
public class Crawler {

    /* Ordered dictionary of tokens. Entries store the frequency of appearance of each token inside files within the
     * provided root hierarchy. Entries are ordered following lexicographically order of the tokens */
    private Map<String, Object> tokenFrequencyDictionary;
    // Dictionary of tokens that will be indexed. Tokens outside this collections won't be indexed
    private Map<String, Object> thesaurus;
    // Dictionary of tokens that won't be indexed (a.k.a stopwords). Used to accelerate token filtering
    private Map<String, Object> inverseThesaurus;

    // Token dictionary loading strategy
    private IDictionaryLoader<String, Object> dictionaryLoader;
    // Thesaurus loading strategy
    private IDictionaryLoader<String, Object> thesaurusLoader;
    // Inverse thesaurus loading strategy
    private IDictionaryLoader<String, Object> inverseThesaurusLoader;
    // Token dictionary serializer strategy
    private IDictionaryMarshaller<String, Object> dictionaryMarshaller;
    // Token dictionary printing strategy
    private IDictionaryPrinter<String, Object> dictionaryPrinter;

    /**
     * Instantiates a crawler with an empty token dictionary and default loading, saving and printing strategies
     */
    public Crawler() {
        tokenFrequencyDictionary = new TreeMap<>();
        thesaurus = new TreeMap<>();
        inverseThesaurus = new TreeMap<>();

        dictionaryLoader = new SerializedIndexLoader();
        thesaurusLoader = new ThesaurusLoader();
        inverseThesaurusLoader = new InverseThesaurusLoader();
        dictionaryMarshaller = new IndexFileMarshaller();
        dictionaryPrinter = new ConsolePrinter();
    }

    /**
     * Instantiates a crawler with an empty token dictionary and specific loading, saving and printing strategies
     *
     * @param dictionaryLoader       Loading strategy
     * @param thesaurusLoader
     * @param inverseThesaurusLoader
     * @param dictionaryMarshaller   Saving strategy
     * @param dictionaryPrinter      Printing strategy
     */
    public Crawler(IDictionaryLoader<String, Object> dictionaryLoader,
                   IDictionaryLoader<String, Object> thesaurusLoader,
                   IDictionaryLoader<String, Object> inverseThesaurusLoader,
                   IDictionaryMarshaller<String, Object> dictionaryMarshaller,
                   IDictionaryPrinter<String, Object> dictionaryPrinter) {
        tokenFrequencyDictionary = new TreeMap<>();
        thesaurus = new TreeMap<>();
        inverseThesaurus = new TreeMap<>();

        this.dictionaryLoader = dictionaryLoader;
        this.thesaurusLoader = thesaurusLoader;
        this.inverseThesaurusLoader = inverseThesaurusLoader;
        this.dictionaryMarshaller = dictionaryMarshaller;
        this.dictionaryPrinter = dictionaryPrinter;
    }

    /**
     * Loads into memory the thesaurus and inverse thesaurus from the designed default source files. This method should
     * be called before attempting to build an index, otherwise, it will throw an exception
     *
     * @see Crawler#buildInvertedIndex(String)
     * @see Config#DEFAULT_THESAURUS_PATH
     * @see Config#DEFAULT_INVERSE_THESAURUS_PATH
     */
    public void initialiseThesauri() {

        // Attempt to load both thesauri from the designed default source files

        Map<String, Object> loadedThesaurus = thesaurusLoader.load(new File(DEFAULT_THESAURUS_PATH));
        Map<String, Object> loadedInverseThesaurus = inverseThesaurusLoader.load(new File(DEFAULT_INVERSE_THESAURUS_PATH));

        // Guard against any error while loading the thesaurus

        if (loadedThesaurus != null)
            thesaurus = loadedThesaurus;

        // Guard against any error while loading the inverse thesaurus

        if (loadedThesaurus != null)
            inverseThesaurus = loadedInverseThesaurus;
    }

    /**
     * Load an already built index saved as a serialized representation of the token dictionary. The input file is
     * specified in the global properties
     *
     * @see Config#SERIALIZED_TOKEN_DICTIONARY_FILENAME
     */
    public void loadSerializedDictionary() {

        System.out.println("[INFO] Loading serialized token frequency dictionary");

        Map<String, Object> loadedTokenDictionary = dictionaryLoader.load(new File(SERIALIZED_TOKEN_DICTIONARY_FILENAME));

        // Guard against any error while loading the dictionary

        if (loadedTokenDictionary != null)
            tokenFrequencyDictionary = loadedTokenDictionary;
    }

    /**
     * Perform a full depth search for files starting from the given root path and exploring all sub-directories to
     * build an inverted index. The index holds the frequency of each token
     *
     * @param rootPath The starting point in the system's filesystem
     * @throws IllegalStateException If the thesaurus, inverse thesaurus or both aren't loaded
     */
    public void buildInvertedIndex(String rootPath) throws IllegalStateException {

        // Check the thesauri are loaded before building the index

        if (!areThesauriLoaded())
            throw new IllegalStateException("The thesaurus, inverse thesaurus or both aren't loaded. Load them first " +
                    "before attempting to build an inverted index");

        LinkedList<File> documentsQueue = new LinkedList<>(); // FIFO list of captured files to be processed

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
                System.err.println("[ERROR] Unable to read (" + currentFile + ")");
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

                if (FileExtensionUtils.fileEndsWithAcceptedExtension(currentFile)) {

                    BufferedReader bufferedReader = new BufferedReader(new FileReader(currentFile));
                    String line;

                    // Read the file line by line, normalize it and break it down into tokens at delimiters

                    while ((line = bufferedReader.readLine()) != null) {

                        // Normalize each line before breaking it into tokens
                        String normalizedLine = NormalizationUtils.normalizeStringNFD(line);

                        // Break down the normalized line into tokens using the statically specified token delimiter list
                        StringTokenizer tokenizer = new StringTokenizer(normalizedLine, TOKEN_DELIMITERS);

                        /*
                         * Each token either creates a new entry with frequency 1 in the dictionary if it wasn't already
                         * included, or increments its frequency count by 1
                         */

                        while (tokenizer.hasMoreTokens()) {

                            String currentToken = tokenizer.nextToken();

                            /* Filter tokens. A token that appears in the inverse thesaurus (a stopword) can be discarded.
                             If it can't be discarded, check if it's present in the thesaurus */

                            if (!inverseThesaurus.containsKey(currentToken) && thesaurus.containsKey(currentToken)) {

                                // The token is inside the thesaurus, process it

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
                    }

                    bufferedReader.close();

                } else {
                    System.out.println("[INFO] Invalid extension (" + FileExtensionUtils.getFileExtension(currentFile) + ")." +
                            " Skipping (" + currentFile + ")");
                }
            } catch (FileNotFoundException e) {
                System.err.println("[ERROR] The file (" + currentFile + ") disappeared before it could be processed");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Serialize the built token dictionary

        marshallTokenDictionary();

    }

    /**
     * Execute the printing strategy specified by {@link Crawler#dictionaryPrinter}
     */
    public void printTokenDictionary() {
        dictionaryPrinter.print(tokenFrequencyDictionary);
    }

    /**
     * @return If thesauri are properly loaded, that is, they hold at least 1 entry each
     */
    private boolean areThesauriLoaded() {
        return thesaurus.size() > 0 && inverseThesaurus.size() > 0;
    }

    public IDictionaryLoader<String, Object> getDictionaryLoader() {
        return dictionaryLoader;
    }

    public void setDictionaryLoader(IDictionaryLoader<String, Object> dictionaryLoader) {
        this.dictionaryLoader = dictionaryLoader;
    }

    public IDictionaryLoader<String, Object> getThesaurusLoader() {
        return thesaurusLoader;
    }

    public void setThesaurusLoader(IDictionaryLoader<String, Object> thesaurusLoader) {
        this.thesaurusLoader = thesaurusLoader;
    }

    public IDictionaryLoader<String, Object> getInverseThesaurusLoader() {
        return inverseThesaurusLoader;
    }

    public void setInverseThesaurusLoader(IDictionaryLoader<String, Object> inverseThesaurusLoader) {
        this.inverseThesaurusLoader = inverseThesaurusLoader;
    }

    public IDictionaryMarshaller<String, Object> getDictionaryMarshaller() {
        return dictionaryMarshaller;
    }

    public void setDictionaryMarshaller(IDictionaryMarshaller<String, Object> dictionaryMarshaller) {
        this.dictionaryMarshaller = dictionaryMarshaller;
    }

    public IDictionaryPrinter<String, Object> getDictionaryPrinter() {
        return dictionaryPrinter;
    }

    public void setDictionaryPrinter(IDictionaryPrinter<String, Object> dictionaryPrinter) {
        this.dictionaryPrinter = dictionaryPrinter;
    }

    /**
     * Serialize the built token dictionary using the strategy specified by {@link Crawler#dictionaryMarshaller}
     */
    private void marshallTokenDictionary() {
        System.out.println("[INFO] Exporting the built token frequency dictionary");
        dictionaryMarshaller.marshall(tokenFrequencyDictionary, new File(SERIALIZED_TOKEN_DICTIONARY_FILENAME));
    }
}
