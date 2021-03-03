package es.unex.giiis.ribw.jgarciapft.utils;

import java.text.Normalizer;
import java.util.Locale;

/**
 * String normalization utilities
 *
 * @author Juan Pablo García Plaza Pérez (jgarciapft@alumnos.unex.es)
 */
public class NormalizationUtils {

    /**
     * Normalize each line before breaking it into tokens. The normalization pipeline first applies
     * Unicode Normalization Form D (NFD, Canonical Decomposition) to get an equivalent representation
     * of non-ASCII characters in the ASCII character set, then performs a lowercase replacement
     * <p>
     * More info about Unicode Normalization within the Java JDK:
     * <p>
     * https://stackoverflow.com/questions/4122170/java-change-%C3%A1%C3%A9%C5%91%C5%B1%C3%BA-to-aeouu
     * https://docs.oracle.com/javase/8/docs/api/java/text/Normalizer.Form.html#NFD
     * https://docs.oracle.com/javase/tutorial/i18n/text/normalizerapi.html
     *
     * @param inputString String to be normalized
     * @return Normalized string
     */
    public static String normalizeStringNFD(String inputString) {
        return Normalizer.normalize(inputString, Normalizer.Form.NFD)
                // Remove the non-ASCII characters produced as a result of NFD Normalization
                .replaceAll("[^\\p{ASCII}]", "")
                .toLowerCase(Locale.ROOT);
    }

}
