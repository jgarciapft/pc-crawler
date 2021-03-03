package es.unex.giiis.ribw.jgarciapft.utils;

import es.unex.giiis.ribw.jgarciapft.Config;

import java.io.File;

import static es.unex.giiis.ribw.jgarciapft.Config.ACCEPTED_FILE_EXTENSIONS_REGEXP;

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
    public static String getFileExtension(File file) {

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
     * @see Config#ACCEPTED_FILE_EXTENSIONS_REGEXP
     */
    public static boolean fileEndsWithAcceptedExtension(File file) {

        String fileExt = getFileExtension(file);

        // Checks it's an accepted extension matching the statically specified regular expression

        if (fileExt != null) return fileExt.matches(ACCEPTED_FILE_EXTENSIONS_REGEXP);
        else return false;
    }
}
