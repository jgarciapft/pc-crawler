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
import java.nio.charset.StandardCharsets;
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
    It purpose is to avoid instantiation of new InvertedIndex objects when the inverted index hasn't changed */
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
            documentCatalog = loadedInvertedFile.getDocumentCatalogue();
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

                // Get the file's nature: 1) text based, 2) structured analysable by a concrete Tika parser 3) other structured type

                if (FileExtensionUtils.isTextualFile(currentFile)) { // 1) Textual file

                    BufferedReader bufferedReader = new BufferedReader(new FileReader(currentFile, StandardCharsets.UTF_8));
                    String line;

                    // Read the file line by line and index each one

                    while ((line = bufferedReader.readLine()) != null)
                        indexTextualContent(line, currentDocumentID);

                    bufferedReader.close();

                } else if (FileExtensionUtils.tikaHasFittingParser(currentFile)) { // 2) structured file analysable by a concrete Tika parser

                    indexWithTikaParser(currentFile, currentDocumentID);

                } else { // 3) Other type of structured file without a concrete Tika parser, use the automatic Tika parser

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

    /**
     * Feed non-structured text based content to the inverted index. The content is normalized and filtered with both a
     * thesaurus and an inverse thesaurus before it is indexed.
     *
     * @param content    The textual content to be indexed
     * @param documentID The identifier of the document which holds the provided content
     */
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

    /**
     * Wrapper call around {@link Crawler#indexTextualContent(String, int)} to extract the textual information from a
     * structured or semi-structured file using a concrete Tika parser, then it is indexed. If the guessed Tika parser
     * fails then Tika's automatic parser detection is leveraged via {@link Crawler#indexWithTikaAutoParser(File, int)}
     *
     * @param file       Input structured or semi-structured file
     * @param documentID The identifier associated with the input file
     * @see Config#TIKA_PARSERS
     */
    private void indexWithTikaParser(File file, int documentID) {

        FileInputStream fileInputStream = null;

        // Get the fitting Tika parser class for the input file

        Class<Parser> tikaParserClass = FileExtensionUtils.tikaParserForFile(file);

        try {

            BodyContentHandler textualContentHandler = new BodyContentHandler();
            Metadata metadata = new Metadata();
            fileInputStream = new FileInputStream(file);
            ParseContext parseContext = new ParseContext();

            // Instantiate the fitting Tika parser from the retrieved class

            Parser tikaParser = tikaParserClass.newInstance();
            System.out.printf("\t[INFO] With Tika parser => %s (%s)\n", tikaParserClass.getSimpleName(), tikaParserClass.toString());

            // Get the textual content from the file using the Tika parser

            tikaParser.parse(fileInputStream, textualContentHandler, metadata, parseContext);

            // Index the textual content

            indexTextualContent(textualContentHandler.toString(), documentID);

        } catch (TikaException e) { // On failure use the automatic parser detection as a fallback mechanism

            System.out.println("\t[WARNING] The chosen Tika parser failed. Using fallback Tika Automatic Parser");
            indexWithTikaAutoParser(file, documentID);

        } catch (InstantiationException | IllegalAccessException | IOException | SAXException e) {
            e.printStackTrace();
        } finally {

            // Close the input file stream

            if (fileInputStream != null) {
                try {
                    fileInputStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    /**
     * Wrapper call around {@link Crawler#indexTextualContent(String, int)} to extract the textual information from a
     * structured or semi-structured file when no specific Tika parser is available, then it is indexed
     *
     * @param file       Input structured or semi-structured file
     * @param documentID The identifier associated with the input file
     */
    private void indexWithTikaAutoParser(File file, int documentID) {

        Tika tikaAutoParser = new Tika();
        System.out.println("\t[INFO] With Tika Automatic Parser");

        try {

            // Attempt to extract the textual content, delegating on Tika to decide the best parser, and indexing it

            indexTextualContent(tikaAutoParser.parseToString(file), documentID);

        } catch (IOException e) {
            e.printStackTrace();
        } catch (TikaException e) {
            System.err.println("[ERROR] The file cannot interpreted by Tika. Ignoring it");
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
                !_cached_invertedIndex.getDocumentCatalogue().equals(documentCatalog)) {

            _cached_invertedIndex = new InvertedIndex(invertedIndex, documentCatalog);
        }

        return _cached_invertedIndex;
    }

    /**
     * Serialize the built inverted index using the strategy specified by {@link Crawler#invertedIndexMarshaller} to
     * create an inverted file. If the provided root path is a file then the inverted file will be stored next to this
     * executable
     */
    private void createInvertedFile() {

        // Calculated path to the inverted file
        File invertedFile = new File(rootPath + File.separator + INVERTED_FILE_FILENAME);

        // If the provided root path is a file then store the inverted file next to this executable

        if (new File(rootPath).isFile())
            invertedFile = new File(INVERTED_FILE_FILENAME);

        System.out.println("[INFO] Creating inverted file at (" + invertedFile.getAbsolutePath() + ")");

        invertedIndexMarshaller.marshall(exportInvertedIndex(), invertedFile);
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
