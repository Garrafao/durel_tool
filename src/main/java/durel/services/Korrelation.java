package durel.services;

import java.util.*;

/**
 * Compute Spearman's rank correlation coefficient of the gold standard annotation of the tutorial and the user's annotations.
 */
public class Korrelation {

    // List of gold standard for tutorial annotation
    ArrayList<Integer[]> gold;

    // List of annotations for tutorial of a certain user
    ArrayList<Integer[]> annotator;

    public Korrelation (ArrayList<Integer[]> gold, ArrayList<Integer[]> annotator) {
        this.gold = gold;
        this.annotator = annotator;
    }

    /**
     * Returns Spearman's rank correlation coefficient of the annotations a user made and the given annotations for the tutorial.
     * @return Spearman's rank correlation coefficient
     */
    public double computeCorrelation () {

        // sort gold standard by annotation (ascending)
        this.gold = sortByJudgment(this.gold);
        // sort user's annotations by annotation (ascending)
        this.annotator = sortByJudgment(this.annotator);

        // add ranks to gold standards
        ArrayList<Double[]> goldRanked = rank(this.gold);
        // add ranks to user's annotations
        ArrayList<Double[]> annotatorRanked = rank(this.annotator);
        // merge the two lists
        ArrayList<Double[]> totalRanked = merge(goldRanked, annotatorRanked);

        /* Now we have our X and Y data ranked.
         * What's left is to compute some statistics on the rankings.
         * More specifically, we need the standard deviation of the rankings of X and Y as well as the covariance between them.
         * With that calculated, rho = cov / (stdX* stdY).
         */
        // compute the mean of x
        double meanX = 0;
        for (Double[] rank : totalRanked) {
            meanX += rank[0];
        }
        meanX /= totalRanked.size();

        // compute the mean of y
        double meanY = 0;
        for (Double[] rank : totalRanked) {
            meanY += rank[1];
        }
        meanY /= totalRanked.size();

        double stddevX = 0.0f;
        double stddevY = 0.0f;
        double cov = 0.0f;

        // In this loop we compute the standard deviations and the covariance on the ranks
        for (Double[] rank : totalRanked) {
            double first = rank[0] - meanX;
            double second = rank[1] - meanY;

            stddevX += first * first;
            stddevY += second * second;

            cov += first * second;
        }

        stddevX /= totalRanked.size();
        stddevY /= totalRanked.size();
        cov /= totalRanked.size();

        stddevX = Math.sqrt(stddevX) ;
        stddevY = Math.sqrt(stddevY) ;

        /* we return the rho coefficient */
        return cov / (stddevX*stddevY) ;
    }

    /**
     * Takes given List of Integer (that must be able to be cast to Integers) and sorts them in ascending order.
     * @param list  List of Integer Arrays, that contain an annotation.
     * @return      Given List, but sorted in ascending order.
     */
    public ArrayList<Integer[]> sortByJudgment(ArrayList<Integer[]> list) {
        ArrayList<Integer[]> sortedList = new ArrayList<>();
        while (!list.isEmpty()) {
            int highestValue = -1;
            int highestIndex = -1;
            for (Integer[] annotation : list) {
                if (annotation[2] > highestValue) {
                    highestValue = annotation[2];
                    highestIndex = list.indexOf(annotation);
                }
            }
            sortedList.add(list.get(highestIndex));
            list.remove(highestIndex);
        }
        return sortedList;
    }

    /**
     * Takes given List and adds ranks, according to Spearman's correlation.
     * @param list  List of String Arrays, sorted in ascending order. The String Arrays must contain the annotation, that can be cast to Integer.
     * @return      Given list of String Arrays, but each String Array is extended by a rank.
     */
    public ArrayList<Double[]> rank (ArrayList<Integer[]> list) {
        ArrayList<Double[]> result = new ArrayList<>();
        ArrayList<Integer> sameRanks = new ArrayList<>();

        // start with index 0
        int k = 0;

        // while k is inside list bound, iterate
        while (k < list.size()) {

            // take value of first rank and compare values to all next values, add all those with the same value
            int value = list.get(k)[2];
            sameRanks.add(k + 1);
            k++;
            for (int i = k + 1; i < list.size(); i++) {
                if (list.get(i)[2] == value) {
                    sameRanks.add(i+1);
                    k++;
                }
                if (list.get(i)[2] < value) break;
            }
            double averageRank = sameRanks.stream().mapToInt(i -> i).average().orElse(0.0);
            for (int sameRank : sameRanks) {
                result.add(new Double[]{Double.valueOf(list.get(sameRank - 1)[0]), averageRank});
            }
            sameRanks.clear();
        }
    return result;
    }

    /**
     * Takes two Lists of String Arrays and merges them by same sentence ID pairs.
     *
     * @param goldRanked        Gold standard annotations with added ranks.
     * @param annotatorRanked   User's annotations with added ranks.
     * @return                  Merged lists with both annotations and ranks, merged by same sentence id pairs.
     */
    public ArrayList<Double[]> merge (ArrayList<Double[]> goldRanked, ArrayList<Double[]> annotatorRanked) {
        ArrayList<Double[]> result = new ArrayList<>();

        for (Double[] goldRank : goldRanked) {
            for (Double[] annotatorRank : annotatorRanked) {
                if (goldRank[0].equals(annotatorRank[0])) {
                    Double[] total = {goldRank[1], annotatorRank[1]};
                    result.add(total);
                    break;
                }
            }
        }
        return result;
    }
}
