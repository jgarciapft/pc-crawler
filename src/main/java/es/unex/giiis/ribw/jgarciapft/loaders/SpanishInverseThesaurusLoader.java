package es.unex.giiis.ribw.jgarciapft.loaders;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;

public class SpanishInverseThesaurusLoader implements IDictionaryLoader<String, Object> {

    private static final String STOPWORD_DELIMITER = " ";

    @Override
    public Map<String, Object> load(File spanishInverseThesaurusFile) {

        Map<String, Object> spanishInverseThesaurus = new TreeMap<>();

        if (spanishInverseThesaurusFile != null && spanishInverseThesaurusFile.exists() &&
                spanishInverseThesaurusFile.isFile() && spanishInverseThesaurusFile.canRead()) {

            try {

                BufferedReader bufferedReader = new BufferedReader(new FileReader(spanishInverseThesaurusFile));
                String line;

                if ((line = bufferedReader.readLine()) != null) {

                    StringTokenizer tokenizer = new StringTokenizer(line, STOPWORD_DELIMITER);

                    while (tokenizer.hasMoreTokens())
                        spanishInverseThesaurus.put(tokenizer.nextToken(), null);
                }

                bufferedReader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            return null;
        }

        return spanishInverseThesaurus;
    }

}
