package es.unex.giiis.ribw.jgarciapft.printers;

import org.apache.commons.lang3.StringUtils;

import java.util.Map;

/**
 * Prints a token frequency dictionary to the console (standard output)
 *
 * @author Juan Pablo García Plaza Pérez (jgarciapft@alumnos.unex.es)
 */
public class ConsolePrinter implements IDictionaryPrinter<String, Object> {

    /**
     * Print a token frequency dictionary to the console (standard output)
     *
     * @param tokenDictionary The token frequency dictionary to be printed
     */
    @Override
    public void print(Map<String, Object> tokenDictionary) {
        System.out.println("\n--- TOKEN DICTIONARY (Token, Frequency) ----------------------------------------------------------------\n");
        for (Map.Entry<String, Object> dictionaryEntry : tokenDictionary.entrySet())
            System.out.printf("\t%s : %s\n", StringUtils.rightPad(dictionaryEntry.getKey(), 25), dictionaryEntry.getValue());
        System.out.println("\n--------------------------------------------------------------------------------------------------------\n");
    }

}
