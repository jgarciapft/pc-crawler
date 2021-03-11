package es.unex.giiis.ribw.jgarciapft;

import es.unex.giiis.ribw.jgarciapft.loaders.*;
import es.unex.giiis.ribw.jgarciapft.marshallers.IInvertedIndexMarshaller;
import es.unex.giiis.ribw.jgarciapft.marshallers.InvertedIndexMarshaller;
import es.unex.giiis.ribw.jgarciapft.utils.FileExtensionUtils;
import es.unex.giiis.ribw.jgarciapft.utils.NormalizationUtils;
import org.apache.tika.Tika;
import org.apache.tika.exception.TikaException;
import org.apache.tika.metadata.Metadata;
import org.apache.tika.parser.ParseContext;
import org.apache.tika.parser.Parser;
import org.apache.tika.sax.BodyContentHandler;
import org.xml.sax.SAXException;

import java.io.*;
import java.util.*;

import static es.unex.giiis.ribw.jgarciapft.Config.*;

/**
 * PC-Crawler implementation
 *
 * @author Juan Pablo García Plaza Pérez (jgarciapft@alumnos.unex.es)
 */
public class Crawler {

    // Path to the folder hierarchy that is the starting point for this crawler (absolute or relative)
    private final String rootPath;
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
     *
     * @param rootPath The folder hierarchy that is the starting point for this crawler
     */
    public Crawler(String rootPath) {
        this.rootPath = rootPath;

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
     * The expected file name is specified in the global properties and it should be located directly at the root of
     * the folder hierarchy
     *
     * @see Config#INVERTED_FILE_FILENAME
     */
    public void loadInvertedFile() {

        // The expected URL of the inverted file based on the root path
        String expectedInvertedFileURL = rootPath + File.separator + INVERTED_FILE_FILENAME;

        System.out.println("[INFO] Attempting to load an inverted file from (" + expectedInvertedFileURL + ")");

        InvertedFile loadedInvertedFile = invertedFileLoader.load(new File(expectedInvertedFileURL));

        // Guard against any error while loading the inverted file

        if (loadedInvertedFile != null) {
            invertedIndex = loadedInvertedFile.getInvertedIndex();
            documentCatalog = loadedInvertedFile.getDocumentIdentifierMapper();
        } else {
            System.err.println("[ERROR] Couldn't load the inverted file. This crawler will build a new inverted index");
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

                if (childrenFiles != null && childrenFiles.length > 0) {
                    System.out.printf("[FOLDER] Queuing %d file(s) from (%s)\n", childrenFiles.length, currentFile.getAbsolutePath());
                    documentsQueue.addAll(Arrays.asList(childrenFiles));
                }
            }

            // CASE 3 - Current file is actually a readable file and not an inverted file

            else if (currentFile.isFile() && !currentFile.getName().equals(INVERTED_FILE_FILENAME)) try {

                System.out.printf("[DOCUMENT] %s (%s)\n", currentFile.getName(), currentFile.getAbsolutePath());

                // Add this document to the document catalogue and get its corresponding ID
                int currentDocumentID = documentCatalog.addDocument(currentFile.getAbsolutePath());

                // Get the file's nature

                if (FileExtensionUtils.isTextualFile(currentFile)) {

                    BufferedReader bufferedReader = new BufferedReader(new FileReader(currentFile));
                    String line;

                    // Read the file line by line and index each one

                    while ((line = bufferedReader.readLine()) != null)
                        indexTextualContent(line, currentDocumentID);

                    bufferedReader.close();

                } else if (FileExtensionUtils.tikaHasFittingParser(currentFile)) {

                    indexWithTikaParser(currentFile, currentDocumentID);

                } else {

                    indexWithTikaAutoParser(currentFile, currentDocumentID);

                }
            } catch (FileNotFoundException e) {
                System.err.println("[ERROR] The file (" + currentFile + ") disappeared before it could be processed");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        // Serialize the built inverted index

        createInvertedFile();

    }

    private void indexTextualContent(String content, int documentID) {

        // Normalize the textual content before breaking it into tokens
        String normalizedContent = NormalizationUtils.normalizeStringNFD(content);

        // Break down the normalized content into tokens using the statically specified token delimiter list
        StringTokenizer tokenizer = new StringTokenizer(normalizedContent, TOKEN_DELIMITERS);

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
                    invertedIndex.get(currentToken).computeOccurrenceInDocument(documentID);
                } else {
                    // Create new entry for this new token and delegate frequency calculation
                    invertedIndex.put(currentToken, new Occurrences(documentID));
                }
            }
        }
    }

    private void indexWithTikaParser(File file, int documentID) {

        FileInputStream fileInputStream = null;

        Class<Parser> tikaParserClass = FileExtensionUtils.tikaParserForFile(file);

        try {

            BodyContentHandler textualContentHandler = new BodyContentHandler();
            Metadata metadata = new Metadata();
            fileInputStream = new FileInputStream(file);
            ParseContext parseContext = new ParseContext();

            Parser tikaParser = tikaParserClass.newInstance();

            tikaParser.parse(fileInputStream, textualContentHandler, metadata, parseContext);

            indexTextualContent(textualContentHandler.toString(), documentID);

        } catch (TikaException e) {

            indexWithTikaAutoParser(file, documentID);

        } catch (InstantiationException | IllegalAccessException | IOException | SAXException e) {
            e.printStackTrace();
        } finally {
            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void indexWithTikaAutoParser(File file, int documentID) {

        Tika tikaAutoParser = new Tika();

        try {

            indexTextualContent(tikaAutoParser.parseToString(file), documentID);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (TikaException e) {
            e.printStackTrace();
        }

    }

    /**
     * @return If thesauri are properly loaded, that is, they hold at least 1 entry each
     */
    private boolean areThesauriLoaded() {
        return thesaurus.size() > 0 && inverseThesaurus.size() > 0;
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
     * Serialize the built inverted index using the strategy specified by {@link Crawler#invertedIndexMarshaller} to
     * create an inverted file
     */
    private void createInvertedFile() {

        String invertedFileURL = rootPath + File.separator + INVERTED_FILE_FILENAME;

        System.out.println("[INFO] Creating inverted file at (" + invertedFileURL + ")");

        invertedIndexMarshaller.marshall(invertedIndex, documentCatalog, new File(invertedFileURL));
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
