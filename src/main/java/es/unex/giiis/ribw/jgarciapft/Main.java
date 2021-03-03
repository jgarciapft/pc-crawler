package es.unex.giiis.ribw.jgarciapft;

import static es.unex.giiis.ribw.jgarciapft.Config.ACCEPTED_FILE_EXTENSIONS_REGEXP;
import static es.unex.giiis.ribw.jgarciapft.Config.TOKEN_DELIMITERS;

/**
 * RIBW 2020/21 - ACTIVIDAD 2: Inicio de un PC-Crawler
 * <p>
 * Esta es una implementación iterativa de un crawler de archivos de texto pensado para operar en sistemas de archivos
 * locales. A partir de una ruta (absoluta o relativa a la ruta donde se encuentra este programa, dependiente del SO)
 * el crawler explorará en profundidad la jerarquía de carpetas en busca de archivos de texto compatbibles
 * (ver ACCEPTED_FILE_EXTENSIONS) e indexará su contenido. Al final de la ejecución imprime por pantalla la frencuencia
 * con la que aparece cada término y exporta el resultado como un objeto Java serializado 'CRAWLERINDEX.idx'. Con el
 * argumento opcional -I es posible cargar un resultado exportado y saltarse la exploración de la jerarquía de carpetas.
 * Por supuesto, los resultados serán válidos solo si no se ha modificado el contenido de la jerarquía de carpetas.
 * Cada archivo se divide en términos según los carácteres separadores de texto (ver TOKEN_DELIMITERS).
 * <p>
 * Para contabilizar la frecuencia de cada término se emplea un montículo ordenado lexicográficamente por el término
 * (java.util.TreeMap). Cada entrada almacena el término y su frencuencia.
 * <p>
 * El algoritmo de búsqueda iterativo está implementado con una cola FIFO (clase java.util.LinkedList) de ficheros.
 * Cada archivo puede ser una carpeta o un documento. Si es una carpeta, entonces la explora y añade todos sus archivos
 * a la cola. El proceso se repite  hasta que se hayan explorado todas las carpetas y documentos.
 * <p>
 * Cada archivo se procesa línea a línea, y cada línea pasa por un proceso de normalización antes de ser dividida en
 * términos. 1) Aplicar normalización Unicode Normalization Form D (NFD, Canonical Decomposition) para generar una
 * representación de caracteres no ASCII equivalente dentro del juego de caracteres ASCII. Esto incluye caracteres
 * acentuados y 'ñ'. 2) Convertir todas las letras a minúsculas. Los términos "casa" y "Casa" se consideran iguales.
 * <p>
 * DELIMITADORES DE TÉRMINOS:
 * * Signos de puntuación, aplicables a ctado -> [ESPACIO].,:;!¡¿?\/
 * * Caracteres comúnmente utilizados en lenguajes de programación para separar entidades -> ()[]{}\t|"#*-+=
 * <p>
 * He considerado que los caracteres "guion" (-) y "barra baja" (_) no deben delimitar términos porque en ocasiones son
 * utilizados para unir palabras (Ej: pre-procesar, TOKEN_DELIMITERS)
 * <p>
 * EXTENSIONES SOPORTADAS: txt|java|c|cpp|py
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

        boolean shouldLoadSavedDictionary = args[0].equals("-I"); // arg1 = Load an already built index
        String rootPath = args[args.length - 1]; // The root path will always be at the end of the args array

        // CRAWLER OPERATION

        // Load an already built dictionary if requested
        if (shouldLoadSavedDictionary) crawler.loadSerializedDictionary();

        // Invoke the crawler's main event loop. Inverted index building
        crawler.buildIndex(rootPath);

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
                "of each indexed token (see TOKEN DELIMITERS)\n" +
                "\n" +
                "SEE ALSO\n" +
                "\n" +
                "\tACCEPTED FILE EXTENSIONS: " + ACCEPTED_FILE_EXTENSIONS_REGEXP + "\n" +
                "\tTOKEN DELIMITERS: " + TOKEN_DELIMITERS + "\n"
        );
    }
}
