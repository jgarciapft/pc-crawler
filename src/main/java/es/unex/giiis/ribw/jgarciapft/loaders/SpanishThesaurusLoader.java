package es.unex.giiis.ribw.jgarciapft.loaders;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;

public class SpanishThesaurusLoader implements IDictionaryLoader<String, Object> {

    private static final String COMMENT_PREFIX = "#";
    private static final String TOKEN_DELIMITERS = ";,";

    @Override
    public Map<String, Object> load(File spanishThesaurusFile) {

        Map<String, Object> spanishThesaurus = new TreeMap<>();

        if (spanishThesaurusFile != null && spanishThesaurusFile.exists() &&
                spanishThesaurusFile.isFile() && spanishThesaurusFile.canRead()) {

            try {

                BufferedReader bufferedReader = new BufferedReader(new FileReader(spanishThesaurusFile));
                String line;

                while ((line = bufferedReader.readLine()) != null) {

                    if (line.startsWith(COMMENT_PREFIX)) continue;

                    StringTokenizer tokenizer = new StringTokenizer(line, TOKEN_DELIMITERS);

                    while (tokenizer.hasMoreTokens()) {

                        String currentToken = tokenizer.nextToken().trim();

                        if (!currentToken.contains(" "))
                            spanishThesaurus.put(currentToken, null);
                    }
                }

                bufferedReader.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        } else {
            return null;
        }

        return spanishThesaurus;
    }

}
