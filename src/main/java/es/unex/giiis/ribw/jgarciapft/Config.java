package es.unex.giiis.ribw.jgarciapft;

import org.apache.tika.parser.Parser;
import org.apache.tika.parser.asm.ClassParser;
import org.apache.tika.parser.epub.EpubParser;
import org.apache.tika.parser.html.HtmlParser;
import org.apache.tika.parser.image.ImageParser;
import org.apache.tika.parser.microsoft.OfficeParser;
import org.apache.tika.parser.microsoft.ooxml.OOXMLParser;
import org.apache.tika.parser.mp3.Mp3Parser;
import org.apache.tika.parser.mp4.MP4Parser;
import org.apache.tika.parser.odf.OpenDocumentParser;
import org.apache.tika.parser.pdf.PDFParser;
import org.apache.tika.parser.rtf.RTFParser;
import org.apache.tika.parser.xml.XMLParser;

import java.util.Map;
import java.util.TreeMap;

/**
 * Public global configuration parameters
 *
 * @author Juan Pablo García Plaza Pérez (jgarciapft@alumnos.unex.es)
 */
public final class Config {

    // RegEx specifying which file extensions correspond to text based files
    public static final String TEXTUAL_FILE_EXTENSIONS_REGEXP = "txt|c|h|cpp|hpp|py|cs|js|sql";

    // Which characters delimits tokens in accepted files processed by this crawler
    public static final String TOKEN_DELIMITERS = " .,:;!¡¿?\\/()[]{}\t|\"#*-+="; // Excludes (-) and (_)

    // Name of the hidden file that holds an inverted file built by this crawler for some folder hierarchy
    public static final String INVERTED_FILE_FILENAME = ".PCCRAWLER.idx";

    // Path to the location of the default thesaurus
    public static final String DEFAULT_THESAURUS_PATH = "resources/Thesaurus_es_ES.txt";

    // Path to the location of the default inverse thesaurus
    public static final String DEFAULT_INVERSE_THESAURUS_PATH = "resources/stopwords_es.txt";

    // Catalogue of file extensions and their respective Tika parser classes
    public static final Map<String, Class<? extends Parser>> TIKA_PARSERS = new TreeMap<>();

    static {

        // Populate the catalogue of parsers

        TIKA_PARSERS.put("xml", XMLParser.class);
        TIKA_PARSERS.put("html", HtmlParser.class);
        TIKA_PARSERS.put("doc", OfficeParser.class);
        TIKA_PARSERS.put("xls", OfficeParser.class);
        TIKA_PARSERS.put("ppt", OfficeParser.class);
        TIKA_PARSERS.put("docx", OOXMLParser.class);
        TIKA_PARSERS.put("xlsx", OOXMLParser.class);
        TIKA_PARSERS.put("pptx", OOXMLParser.class);
        TIKA_PARSERS.put("odf", OpenDocumentParser.class);
        TIKA_PARSERS.put("pdf", PDFParser.class);
        TIKA_PARSERS.put("epub", EpubParser.class);
        TIKA_PARSERS.put("rtf", RTFParser.class);
        TIKA_PARSERS.put("mp3", Mp3Parser.class);
        TIKA_PARSERS.put("jpg", ImageParser.class);
        TIKA_PARSERS.put("jpeg", ImageParser.class);
        TIKA_PARSERS.put("png", ImageParser.class);
        TIKA_PARSERS.put("gif", ImageParser.class);
        TIKA_PARSERS.put("bmp", ImageParser.class);
        TIKA_PARSERS.put("mp4", MP4Parser.class);
        TIKA_PARSERS.put("java", ClassParser.class);
    }

}
