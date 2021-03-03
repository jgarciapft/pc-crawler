package es.unex.giiis.ribw.jgarciapft.loaders;

import es.unex.giiis.ribw.jgarciapft.utils.NormalizationUtils;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;

/**
 * Loads an inverse thesaurus from a file, that is, a thesaurus of tokens not to be indexed (a.k.a stopword).
 * All stopwords are specified in one line and they are separated by a whitespace. Each line is normalized using
 * Unicode Normalization Form D (NFD, Canonical Decomposition)
 *
 * @author Juan Pablo García Plaza Pérez (jgarciapft@alumnos.unex.es)
 */
public class InverseThesaurusLoader implements IDictionaryLoader<String, Object> {

    private static final String STOPWORD_DELIMITER = " ";

    /**
     * @param inverseThesaurusFile The source file to build the inverse thesaurus
     * @return A dictionary representing the built inverse thesaurus
     */
    @Override
    public Map<String, Object> load(File inverseThesaurusFile) {

        Map<String, Object> inverseThesaurus = new TreeMap<>();

        // Check the source file is indeed a file and can be read

        if (inverseThesaurusFile != null && inverseThesaurusFile.exists() &&
                inverseThesaurusFile.isFile() && inverseThesaurusFile.canRead()) {

            try {

                BufferedReader bufferedReader = new BufferedReader(new FileReader(inverseThesaurusFile));
                String line;

                // Attempt to read the only line of the source file and decompose it into stopwords

                if ((line = bufferedReader.readLine()) != null) {

                    // Normalize each line before breaking it into tokens
                    String normalizedLine = NormalizationUtils.normalizeStringNFD(line);

                    StringTokenizer tokenizer = new StringTokenizer(normalizedLine, STOPWORD_DELIMITER);

                    // Store each stopword in the inverse thesaurus

                    while (tokenizer.hasMoreTokens())
                        inverseThesaurus.put(tokenizer.nextToken(), null);
                }

                bufferedReader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            return null;
        }

        return inverseThesaurus;
    }

}
