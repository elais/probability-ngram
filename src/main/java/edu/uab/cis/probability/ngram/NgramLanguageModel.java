package edu.uab.cis.probability.ngram;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
/**
 * A probabilistic n-gram language model.
 *
 * @param <T>
 *          The type of items in the sequences over which the language model
 *          estimates probabilities.
 */
public class NgramLanguageModel<T> {

  enum Smoothing {
    /**
     * Do not apply smoothing. An n-gram w<sub>1</sub>,...,w<sub>n</sub> will
     * have its joint probability P(w<sub>1</sub>,...,w<sub>n</sub>) estimated
     * as #(w<sub>1</sub>,...,w<sub>n</sub>) / N, where N indicates the total
     * number of all 1-grams observed during training.
     *
     * Note that we have defined only the joint probability of an n-gram here.
     * Deriving the conditional probability from the definition above is left as
     * an exercise.
     */
    NONE,

    /**
     * Apply Laplace smoothing. An n-gram w<sub>1</sub>,...,w<sub>n</sub> will
     * have its conditional probability
     * P(w<sub>n</sub>|w<sub>1</sub>,...,w<sub>n-1</sub>) estimated as (1 +
     * #(w<sub>1</sub>,...,w<sub>n</sub>)) / (V +
     * #(w<sub>1</sub>,...,w<sub>n-1</sub>)), where # indicates the number of
     * times an n-gram was observed during training and V indicates the number
     * of <em>unique</em> 1-grams observed during training.
     *
     * Note that Laplace smoothing defines only the conditional probability of
     * an n-gram, not the joint probability.
     */
    LAPLACE
  }

  enum Representation {
    /**
     * Calculate probabilities in the normal range, [0,1].
     */
    PROBABILITY,
    /**
     * Calculate log-probabilities instead of probabilities. In every case where
     * probabilities would have been multiplied, take advantage of the fact that
     * log(P(x)*P(y)) = log(P(x)) + log(P(y)) and add log-probabilities instead.
     * This will improve efficiency since addition is faster than
     * multiplication, and will avoid some numerical underflow problems that
     * occur when taking the product of many small probabilities close to zero.
     */
    LOG_PROBABILITY
  }

  /**
   * Creates an n-gram language model.
   *
   * @param n
   *          The number of items in an n-gram.
   * @param representation
   *          The type of representation to use for probabilities.
   * @param smoothing
   *          The type of smoothing to apply when estimating probabilities.
   */
  private int ngram_size;
  private Representation representation;
  private Smoothing smoothing;
  private int training_size;
  public NgramLanguageModel(int n, Representation representation, Smoothing smoothing) {
    // TODO
    this.ngram_size = n;
    this.representation = representation;
    this.smoothing = smoothing;
  }

  /**
   * Trains the language model with the n-grams from a sequence of items.
   *
   * This typically involves collecting counts of n-grams that occurred in the
   * sequence.
   *
   * @param sequence
   *          The sequence on which the model should be trained.
   */
  private Map<String, Double> ngrams;
  private Map<String, Double> ngram_prob;
  private Set uniqueNgrams;
  private List ngram_list;
  public void train(List<T> sequence) {
    List<T> s = sequence;

    training_size = sequence.size();
    uniqueNgrams = new HashSet(sequence);
    ngram_list = new ArrayList<List>();
    ngrams = new HashMap<String, Double>();
    ngram_prob = new HashMap<String, Double>();
    for(int n = 1; n <= ngram_size; n++)
      createNgrams(n, sequence).forEach(e -> ngram_list.add(e));
    ngram_list.forEach(e -> ngrams.put(e.toString(),
            (double)Collections.frequency(ngram_list, e)));
    ngram_list.forEach(e -> ngram_prob.put(e.toString(),
            (double)Collections.frequency(ngram_list, e)/training_size));
  }

  /**
   * Return the estimated n-gram probability of the sequence:
   *
   * P(w<sub>0</sub>,...,w<sub>k</sub>) = ∏<sub>i=0,...,k</sub>
   * P(w<sub>i</sub>|w<sub>i-n+1</sub>, w<sub>i-n+2</sub>, ..., w<sub>i-1</sub>)
   *
   * For example, a 3-gram language model would calculate the probability of the
   * sequence [A,B,B,C,A] as:
   *
   * P(A,B,B,C,A) = P(A)*P(B|A)*P(B|A,B)*P(C|B,B)*P(A|B,C)
   *
   * The exact calculation of the conditional probabilities in this expression
   * depends on the smoothing method. See {@link Smoothing}.
   *
   * The result is in the range [0,1] with {@link Representation#PROBABILITY}
   * and in the range (-∞,0] with {@link Representation#LOG_PROBABILITY}.
   *
   * @param sequence
   *          The sequence of items whose probability is to be estimated.
   * @return The estimated probability of the sequence.
   */
  private List sub;
  private List<Double> conditionals;
  private double product;
  public double probability(List<T> sequence) {
    // TODO
    sub = new ArrayList<T>();
    conditionals = new ArrayList<Double>(sequence.size());
    //System.out.println(sequence);
    for(int i = 0; i < sequence.size(); i++){
      if(i < ngram_size){
        sub = sequence.subList(0,i + 1);
      } else{
        sub = sequence.subList(i - this.ngram_size + 1, i+1);
      }
      if(this.smoothing == smoothing.NONE){
        if(i == 0){
          //System.out.println(ngram_prob.get(sub));
          conditionals.add(ngram_prob.getOrDefault(sub.toString(),0.0));
        }
        else{
          conditionals.add(noSmoothing(sub.toString(), sub.subList(0, sub.size()-1).toString()));
        }
      }
      else
        if(i == 0)
          conditionals.add((1 + ngrams.getOrDefault(sub.toString(), 0.0))/
                           ((double)uniqueNgrams.size() +
                            training_size));
        else
          conditionals.add(laplaceSmoothing(sub.toString(), sub.subList(0, sub.size()-1).toString(), sequence));

    }

    if(this.representation == representation.PROBABILITY){
      product = 1.0;
      for(double n : conditionals)
        product *= n;
      return product;
    }
    else{
      product = 0.0;
      for(double n : conditionals)
        product += Math.log(n);
      return product;
    }
  }

  private List ng;
  public List createNgrams(int n, List<T> s){
    ng = new ArrayList<List>();
    for(int i = 0; i < s.size() - n + 1; i++)
      ng.add(concat(s, i, i + n));
    return ng;
  }

  private List sb;
  public List concat(List<T> words, int start, int end){
    sb = new ArrayList();
    for(int i = start; i < end; i++)
      sb.add(words.get(i));
    return sb;
  }


  public double noSmoothing(String a, String b){
    return (ngram_prob.getOrDefault(a,0.0)/
            ngram_prob.getOrDefault(b,1.0));
  }

  public double laplaceSmoothing(String a, String b, List<T> V){
    return ((1.0 + ngrams.getOrDefault(a, 0.0))/
            ((double)uniqueNgrams.size() + ngrams.getOrDefault(b, 0.0)));
  }

}
