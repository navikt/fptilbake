package no.nav.foreldrepenger.tilbakekreving.overvåkning.metrikker;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.function.Function;

/** Generer alle kombinasjoner av angitte input data. */
class CombineLists<T> {

    private Map<String, Collection<T>> namedVectors;

    CombineLists(Map<String, Collection<T>> namedVectors) {
        this.namedVectors = new TreeMap<>(namedVectors);
    }

    List<Map<String, T>> toMap() {
        return toMap(v -> v);
    }

    <R> List<Map<String, R>> toMap(Function<T, R> valueConverter) {
        List<String> keys = new ArrayList<>();
        List<Collection<T>> vectors = new ArrayList<>();
        for (var e : namedVectors.entrySet()) {
            // sikre lik indeks
            keys.add(e.getKey());
            vectors.add(e.getValue());
        }
        var rows = permutations(vectors);

        List<Map<String, R>> result = new ArrayList<>();
        for (var row : rows) {
            Map<String, R> mapRow = new LinkedHashMap<>(); // bevarer rekkefølge
            for (int i = 0; i < keys.size(); i++) {
                mapRow.put(keys.get(i), valueConverter.apply(row.get(i)));
            }
            result.add(mapRow);
        }
        return result;
    }

    List<List<T>> permutations(List<Collection<T>> vectors) {
        if (vectors == null || vectors.isEmpty()) {
            return Collections.emptyList();
        } else {
            List<List<T>> res = new LinkedList<>();
            permutationsImpl(vectors, res, 0, new LinkedList<T>());
            return res;
        }
    }

    /** Recursive implementation for {@link #permutations(List, Collection)} */
    private static <T> void permutationsImpl(List<Collection<T>> vectors, List<List<T>> resultMatrix, int d, List<T> currentVector) {
        // if depth equals number of original collections, final reached, add and return
        if (d == vectors.size()) {
            resultMatrix.add(currentVector);
            return;
        }

        // iterate from current collection and copy 'current' element N times, one for each element
        Collection<T> currentCollection = vectors.get(d);
        for (T element : currentCollection) {
            List<T> copy = new LinkedList<>(currentVector);
            copy.add(element);
            permutationsImpl(vectors, resultMatrix, d + 1, copy);
        }
    }

}
