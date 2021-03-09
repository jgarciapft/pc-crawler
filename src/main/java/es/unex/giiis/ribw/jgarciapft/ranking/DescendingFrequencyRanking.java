package es.unex.giiis.ribw.jgarciapft.ranking;

import java.util.Map;

/**
 * Rank query results by descending (partial) frequency order. The document with the higher partial frequency will
 * appear first. In case of a tie whichever result got processed first will come before
 *
 * @author Juan Pablo García Plaza Pérez (jgarciapft@alumnos.unex.es)
 */
public class DescendingFrequencyRanking implements RankingCriterion {

    @SuppressWarnings("ComparatorMethodParameterNotUsed")
    @Override
    public int compare(Map.Entry<Integer, Integer> entry1, Map.Entry<Integer, Integer> entry2) {
        /* Note. This never returns 0 cause it will cause results to be discarded if they are being stored inside
        collections where uniqueness restrictions apply */
        return entry2.getValue() > entry1.getValue() ? 1 : -1;
    }

}
