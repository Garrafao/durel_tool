package durel.session;

import durel.domain.model.Instance;
import lombok.Getter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;

/**
 * Defines the sequence of annotations of a given word (base).
 */
@Getter
public class RandomAnnotationSequence {

    ArrayList<Integer[]> annotations;

    /**
     * Creates the annotation sequence given a list of sentence ids.
     * @param ids list of sentence ids.
     */
    public RandomAnnotationSequence(List<Integer> ids, long seed) {
        this.annotations = new ArrayList<>();

        // create the annotation sequence. Basically, find all the combinations of sentence pairs.
        for ( int i = 0 ; i < ids.size() - 1 ; ++i ) {
            for ( int j = i + 1 ; j < ids.size(); j++ ) {
                this.annotations.add(new Integer[]{ids.get(i), ids.get(j)});
            }
        }

        // Using always the same seed we are able to recreate always the same annotation sequence, which enables resuming
        // the annotation process.
        Random random = new Random(seed);

        // Shuffle the list.
        Collections.shuffle(this.annotations,random ) ;
    }

    /**
     * Creates the annotation sequence given a list of sentence ids.
     * @param instances list of instances.
     */
    public RandomAnnotationSequence(long seed, List<Instance> instances) {
        this.annotations = new ArrayList<>();

        // create the annotation sequence. Basically, find all the combinations of sentence pairs.
        for (Instance instance : instances) {
            final Integer[] array = instance.getUsePair().getSentenceIDs().toArray(Integer[]::new);
            this.annotations.add(array);
        }

        // Using always the same seed we are able to recreate always the same annotation sequence, which enables resuming
        // the annotation process.
        Random random = new Random(seed);

        // Shuffle the list.
        Collections.shuffle(this.annotations,random) ;
    }

    /**
     * Returns next sentence ids that should be annotated by the user.
     */
    public Integer[] next (int currentIndex) {
        return this.annotations.get(currentIndex);
    }
}
