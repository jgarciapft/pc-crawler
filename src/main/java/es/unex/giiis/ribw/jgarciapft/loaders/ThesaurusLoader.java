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
 * Loads a thesaurus into a dictionary from a file. Each token is separated by either a comma (,) or a semicolon (;).
 * The source file can include one line comments starting with #. Each line is normalized using
 * Unicode Normalization Form D (NFD, Canonical Decomposition)
 *
 * @author Juan Pablo García Plaza Pérez (jgarciapft@alumnos.unex.es)
 */
public class ThesaurusLoader implements IDictionaryLoader<String, Object> {

    private static final String COMMENT_PREFIX = "#"; // One line comments
    private static final String TOKEN_DELIMITERS = ";,"; // Token delimiters

    /**
     * @param thesaurusFile The source file to build the thesaurus
     * @return A dictionary representing the built thesaurus
     */
    @Override
    public Map<String, Object> load(File thesaurusFile) {

        Map<String, Object> thesaurus = new TreeMap<>();

        // Check the source file is indeed a file and can be read

        if (thesaurusFile != null && thesaurusFile.exists() && thesaurusFile.isFile() && thesaurusFile.canRead()) {

            try {

                BufferedReader bufferedReader = new BufferedReader(new FileReader(thesaurusFile));
                String line;

                // Read the source file line by line, divide it into tokens and store them (ignoring synonyms and compound words)

                while ((line = bufferedReader.readLine()) != null) {

                    if (line.startsWith(COMMENT_PREFIX)) continue; // Ignore one line comments

                    // Normalize each line before breaking it into tokens
                    String normalizedLine = NormalizationUtils.normalizeStringNFD(line);

                    StringTokenizer tokenizer = new StringTokenizer(normalizedLine, TOKEN_DELIMITERS);

                    // Store each token in the thesaurus

                    while (tokenizer.hasMoreTokens()) {

                        String currentToken = tokenizer.nextToken().trim();

                        // Check it isn't a compound word (contains a whitespace)

                        if (!currentToken.contains(" "))
                            thesaurus.put(currentToken, null);
                    }
                }

                bufferedReader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            return null;
        }

        return thesaurus;
    }

}
