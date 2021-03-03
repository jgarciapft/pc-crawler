package es.unex.giiis.ribw.jgarciapft;

/**
 * Public global configuration parameters
 *
 * @author Juan Pablo García Plaza Pérez (jgarciapft@alumnos.unex.es)
 */
public final class Config {

    // RegEx specifying file extensions that a detected file should match in order to be processed by this crawler
    public static final String ACCEPTED_FILE_EXTENSIONS_REGEXP = "txt|java|c|cpp|py";

    // Which characters delimits tokens in accepted files processed by this crawler
    public static final String TOKEN_DELIMITERS = " .,:;!¡¿?\\/()[]{}\t|\"#*-+="; // Excludes (-) and (_)

    // Name of the file containing the serialized token frequency dictionary
    public static final String SERIALIZED_TOKEN_DICTIONARY_FILENAME = "CRAWLERINDEX.idx";

    // Path to the location of the default thesaurus
    public static final String DEFAULT_THESAURUS_PATH = "resources/Thesaurus_es_ES.txt";

    // Path to the location of the default inverse thesaurus
    public static final String DEFAULT_INVERSE_THESAURUS_PATH = "resources/stopwords_es.txt";

}
