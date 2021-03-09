package es.unex.giiis.ribw.jgarciapft.ranking;

import java.util.Comparator;
import java.util.Map;

/**
 * Marker interface to alias the map entry comparator used as a ranking criterion to rank queries
 * @author Juan Pablo García Plaza Pérez (jgarciapft@alumnos.unex.es)
 */
public interface RankingCriterion extends Comparator<Map.Entry<Integer, Integer>> {

}
