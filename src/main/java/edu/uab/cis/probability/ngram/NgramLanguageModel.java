package edu.uab.cis.probability.ngram;

import java.util.List;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.HashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
  public void train(List<T> sequence) {
    // TODO
    String s = sequence.toArray(new String[sequence.size()]).toString();
    List<String> ngram_list = createNgrams(this.ngram_size, s);
    this.ngrams = new HashMap<String, Double>();
    for(String ngram : ngram_list)
      this.ngrams.putIfAbsent(ngram, 0.0);
    for(String key : this.ngrams.keySet()){
      Pattern p = Pattern.compile(key);
      Matcher m = p.matcher(s);
      while(m.find()){
        this.ngrams.put(key, this.ngrams.get(key) + 1.0);
      }
      this.ngrams.put(key, (Double)this.ngrams.get(key)/s.length());
    }
    



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
  public double probability(List<T> sequence) {
    // TODO
    return 0.0;
  }
  

  public List<String> createNgrams(int n, String str){
    List<String> ngrams = new ArrayList<String>();
    String[] words = str.split("");
    for (int i = 0; i < words.length - n + 1; i++)
      ngrams.add(concat(words, i, i+n));
    return ngrams;
  }
  
  public static String concat(String[] words, int start, int end){
    StringBuilder sb = new StringBuilder();
    for(int i = start; i < end; i++)
      sb.append((i > start ? "" : "") + words[i]);
    return sb.toString();
  }
}