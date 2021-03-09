package es.unex.giiis.ribw.jgarciapft;

import static es.unex.giiis.ribw.jgarciapft.Config.ACCEPTED_FILE_EXTENSIONS_REGEXP;
import static es.unex.giiis.ribw.jgarciapft.Config.TOKEN_DELIMITERS;

/**
 * RIBW 2020/21 - PC-Crawler: Parte 3
 *
 * @author Juan Pablo García Plaza Pérez (jgarciapft@alumnos.unex.es)
 */
public class Main {

    public static void main(String[] args) {

        // Should provide 1 arg: the path to the root resource from which start crawling or the help invocation arg

        if (args.length < 1 || args[0].equals("--help")) {
            showHelp();
            return;
        }

        Crawler pcCrawler = new Crawler(); // PC Crawler initialised with defaults

        // PARSE PROVIDED ARGUMENTS

        boolean shouldLoadInvertedFile = args[0].equals("-I"); // arg1 = Load an already built inverted file
        String rootPath = args[args.length - 1]; // The root path will always be at the end of the args array

        // CRAWLER OPERATION

        // Load an already built inverted index if requested, otherwise build one

        if (shouldLoadInvertedFile) {
            pcCrawler.loadInvertedFile();
        } else {
            try {
                pcCrawler.initialiseThesauri(); // Only load the thesauri to build a new inverted index
                pcCrawler.buildInvertedIndex(rootPath);
            } catch (IllegalStateException e) {
                System.err.println("[ERROR] " + e.getMessage());
                return;
            }
        }

        CrawlerCLI crawlerCLI = new CrawlerCLI(pcCrawler.exportInvertedIndex()); // The CLI manager to interact with the user

        // OPEN AN INTERACTIVE CLI TO QUERY THE BUILT (OR LOADED) INVERTED INDEX

        crawlerCLI.interactiveCLI();

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
                "a given path and index the content of files with a compatible file extension (see ACCEPTED FILE EXTENSIONS), storing the total and partial frequencies\n" +
                "of each indexed token (see TOKEN DELIMITERS). The result is the construction of an inverted index associated with the given path\n" +
                "\n" +
                "This crawler uses a pair of thesauri (regular and stopwords thesaurus) to filter which tokens it will index\n" +
                "\n" +
                "SEE ALSO\n" +
                "\n" +
                "\tACCEPTED FILE EXTENSIONS: " + ACCEPTED_FILE_EXTENSIONS_REGEXP + "\n" +
                "\tTOKEN DELIMITERS: " + TOKEN_DELIMITERS + "\n"
        );
    }
}
