package es.unex.giiis.ribw.jgarciapft;

import es.unex.giiis.ribw.jgarciapft.loaders.*;
import es.unex.giiis.ribw.jgarciapft.marshallers.IInvertedIndexMarshaller;
import es.unex.giiis.ribw.jgarciapft.marshallers.InvertedIndexMarshaller;
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

    /* Ordered dictionary of tokens (inverted index). Entries store the frequency of appearance of each token inside
    files within the provided root hierarchy, globally and per document. Entries are ordered following
    lexicographically order of the tokens */
    private Map<String, Occurrences> invertedIndex;
    // A structure to map documents to a unique identifier
    private IDocumentCatalogue documentCatalog;
    // Dictionary of tokens that will be indexed. Tokens outside this collections won't be indexed
    private Map<String, Object> thesaurus;
    // Dictionary of tokens that won't be indexed (a.k.a stopwords). Used to accelerate token filtering
    private Map<String, Object> inverseThesaurus;

    /* A cached representation of the built (or loaded) inverted index managed by this crawler (see InvertedIndex)
    It purpose is to avoid instantiation of new InveredIndex objects when the inverted index hasn't changed */
    private InvertedIndex _cached_invertedIndex;

    // Inverted file loading strategy
    private IInvertedFileLoader invertedFileLoader;
    // Thesaurus loading strategy
    private IDictionaryLoader<String, Object> thesaurusLoader;
    // Inverse thesaurus loading strategy
    private IDictionaryLoader<String, Object> inverseThesaurusLoader;
    // Inverted file creation strategy
    private IInvertedIndexMarshaller invertedIndexMarshaller;

    /**
     * Instantiates a crawler with an empty token dictionary and default loading, saving and printing strategies
     * and default document's catalogue implementation
     */
    public Crawler() {
        invertedIndex = new TreeMap<>();
        documentCatalog = new DocumentsLUT();
        thesaurus = new TreeMap<>();
        inverseThesaurus = new TreeMap<>();

        _cached_invertedIndex = null;

        invertedFileLoader = new InvertedFileLoader();
        thesaurusLoader = new ThesaurusLoader();
        inverseThesaurusLoader = new InverseThesaurusLoader();
        invertedIndexMarshaller = new InvertedIndexMarshaller();
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
     * Load an already built index saved as a serialized representation of the token dictionary (inverted file).
     * The input file is specified in the global properties
     *
     * @see Config#INVERTED_FILE_FILENAME
     */
    public void loadInvertedFile() {

        System.out.println("[INFO] Loading inverted file");

        InvertedFile loadedInvertedFile = invertedFileLoader.load(new File(INVERTED_FILE_FILENAME));

        // Guard against any error while loading the inverted file

        if (loadedInvertedFile != null) {
            invertedIndex = loadedInvertedFile.getInvertedIndex();
            documentCatalog = loadedInvertedFile.getDocumentIdentifierMapper();
        }
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

                    // Register the current document with the document's catalogue and get its ID

                    int currentDocumentID = documentCatalog.addDocument(currentFile.getAbsolutePath());

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

                                // The token is considered in the thesaurus, so process it

                                if (invertedIndex.containsKey(currentToken)) {
                                    // Delegate frequency calculation
                                    invertedIndex.get(currentToken).computeOccurrenceInDocument(currentDocumentID);
                                } else {
                                    // Create new entry for this new token and delegate frequency calculation
                                    invertedIndex.put(currentToken, new Occurrences(currentDocumentID));
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

        // Serialize the built inverted index

        marshallTokenDictionary();

    }

    /**
     * @return A complete representation of the inverted index built by this crawler that can be used to query terms.
     * This object shouldn't be modified
     */
    public InvertedIndex exportInvertedIndex() {

        /* The cached representation should be updated if the current one is null or the inverted index or the
        document's catalogue has changed */

        if (_cached_invertedIndex == null ||
                !_cached_invertedIndex.getInvertedIndex().equals(invertedIndex) ||
                !_cached_invertedIndex.getDocumentIdentifierMapper().equals(documentCatalog)) {

            _cached_invertedIndex = new InvertedIndex(invertedIndex, documentCatalog);
        }

        return _cached_invertedIndex;
    }

    /**
     * @return If thesauri are properly loaded, that is, they hold at least 1 entry each
     */
    private boolean areThesauriLoaded() {
        return thesaurus.size() > 0 && inverseThesaurus.size() > 0;
    }

    /**
     * Serialize the built inverted index using the strategy specified by {@link Crawler#invertedIndexMarshaller}
     */
    private void marshallTokenDictionary() {
        System.out.println("[INFO] Exporting the built inverted index");
        invertedIndexMarshaller.marshall(invertedIndex, documentCatalog, new File(INVERTED_FILE_FILENAME));
    }

    public IInvertedFileLoader getInvertedFileLoader() {
        return invertedFileLoader;
    }

    public void setInvertedFileLoader(IInvertedFileLoader invertedFileLoader) {
        this.invertedFileLoader = invertedFileLoader;
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

    public IInvertedIndexMarshaller getInvertedIndexMarshaller() {
        return invertedIndexMarshaller;
    }

    public void setInvertedIndexMarshaller(IInvertedIndexMarshaller invertedIndexMarshaller) {
        this.invertedIndexMarshaller = invertedIndexMarshaller;
    }
}
