package es.unex.giiis.ribw.jgarciapft.utils;

import es.unex.giiis.ribw.jgarciapft.Config;
import org.apache.tika.parser.Parser;

import java.io.File;
import java.util.Locale;

import static es.unex.giiis.ribw.jgarciapft.Config.TEXTUAL_FILE_EXTENSIONS_REGEXP;
import static es.unex.giiis.ribw.jgarciapft.Config.TIKA_PARSERS;

/**
 * Utility class to deal with file extensions
 *
 * @author Juan Pablo García Plaza Pérez (jgarciapft@alumnos.unex.es)
 */
public class FileExtensionUtils {

    /**
     * Extract the extension from a file. A file extension is the last sequence of characters that result from dividing
     * a file name at each dot (.)
     *
     * @param file Input file
     * @return Input file's file extension, or null if it isn't a file or the file doesn't exists.
     */
    public static String extractExtension(File file) {

        // Check that the input file exists and is indeed a file, not a directory

        if (file.exists() && file.isFile()) {

            // Split the file name at each dot (.)

            String[] split = file.getName()
                    .toLowerCase(Locale.ROOT)
                    .split("\\.");

            return split[split.length - 1]; // Return the last split

        } else {
            return null;
        }
    }

    /**
     * @param file Input file
     * @return If the file's contents are text based, non-binary
     * @see Config#TEXTUAL_FILE_EXTENSIONS_REGEXP
     */
    public static boolean isTextualFile(File file) {

        String fileExt = extractExtension(file);

        // Check if the file can be recognized as a textual file by its file extension

        if (fileExt != null) return fileExt.matches(TEXTUAL_FILE_EXTENSIONS_REGEXP);
        else return false;
    }

    /**
     * @param file A structured or semi-structured file
     * @return If the provided structured file can be analysed with a concrete Tika parser
     * @see Config#TIKA_PARSERS
     */
    public static boolean tikaHasFittingParser(File file) {
        return TIKA_PARSERS.containsKey(extractExtension(file));
    }

    /**
     * @param file A structured or semi-structured file
     * @return A fitting Tika parser for the input file
     * @see Config#TIKA_PARSERS
     */
    @SuppressWarnings("unchecked")
    public static Class<Parser> tikaParserForFile(File file) {
        return (Class<Parser>) TIKA_PARSERS.get(extractExtension(file));
    }

}
