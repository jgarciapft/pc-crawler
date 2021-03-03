package es.unex.giiis.ribw.jgarciapft;

import static es.unex.giiis.ribw.jgarciapft.Config.ACCEPTED_FILE_EXTENSIONS_REGEXP;
import static es.unex.giiis.ribw.jgarciapft.Config.TOKEN_DELIMITERS;

/**
 * RIBW 2020/21 - ACTIVIDAD 2: Inicio de un PC-Crawler
 *
 * @author Juan Pablo García Plaza Pérez (jgarciapft@alumnos.unex.es)
 */
public class Main {

    public static void main(String[] args) {

        // Should provide the path to the root resource from which start crawling, either a directory or a file

        if (args.length < 1 || args[0].equals("--help")) {
            showHelp();
            return;
        }

        Crawler crawler = new Crawler(); // Crawler with default loading, saving and printing strategies

        // PARSE PROVIDED ARGUMENTS

        boolean shouldLoadSavedIndex = args[0].equals("-I"); // arg1 = Load an already built index
        String rootPath = args[args.length - 1]; // The root path will always be at the end of the args array

        // CRAWLER OPERATION

        // Load an already built inverted index if requested, otherwise build one

        if (shouldLoadSavedIndex) {
            crawler.loadSerializedDictionary();
        } else {
            try {
                crawler.initialiseThesauri(); // Only load the thesauri to build a new inverted index
                crawler.buildInvertedIndex(rootPath);
            } catch (IllegalStateException e) {
                System.err.println("[ERROR] " + e.getMessage());
                return;
            }
        }

        // Print the built (or loaded) inverted index
        crawler.printTokenDictionary();

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
                "of each indexed token (see TOKEN DELIMITERS). This crawler uses a pair of thesauri (regular and stopwords thesaurus) to filter which tokens" +
                "it will index\n" +
                "\n" +
                "SEE ALSO\n" +
                "\n" +
                "\tACCEPTED FILE EXTENSIONS: " + ACCEPTED_FILE_EXTENSIONS_REGEXP + "\n" +
                "\tTOKEN DELIMITERS: " + TOKEN_DELIMITERS + "\n"
        );
    }
}
