package edu.uab.cis.probability.ngram;

import java.util.Collection;
import java.util.List;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.HashMap;
import java.lang.Math;
import java.util.Iterator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
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
  private Map<String, Double> ngram_prob;
  private List<String> ngram_list;
  public void train(List<T> sequence) {
    String s = sequence.stream().map(Object::toString)
                        .collect(Collectors.joining(", ")).replaceAll(", ", "");
    
    //System.out.println(s);
    this.ngram_list = new ArrayList<String>();
    for(int n = 1; n <= this.ngram_size; n++){
      for(String ngram : createNgrams(n, s))
        ngram_list.add(ngram);
    }
    this.ngrams = new HashMap<String, Double>();
    
    for(String ngram : ngram_list){
      this.ngrams.put(ngram, 0.0);
    }
    for(String ngram: ngram_list)
      this.ngrams.put(ngram, this.ngrams.get(ngram) + 1.0);
    
    this.ngram_prob = new HashMap<String, Double>();
    Iterator<String> keySetIterator = this.ngrams.keySet().iterator();
    while(keySetIterator.hasNext()){
      String key = keySetIterator.next();
      this.ngram_prob.put(key, this.ngrams.get(key)/sequence.size());
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
    String s = sequence.stream().map(Object::toString)
                        .collect(Collectors.joining(", ")).replaceAll(", ", "");
    String sub = "";
    List<Double> conditionals = new ArrayList<Double>();
    for(int i = 0; i < s.length(); i++){
      if(i < this.ngram_size){
        sub = s.substring(0,i + 1);
      } else{
        sub = s.substring(i - this.ngram_size + 1, i+1);
      }
      if(this.smoothing == smoothing.NONE){
        if(i == 0)
          conditionals.add(noSmoothing(s.substring(i, i+1), sub.substring(0, sub.length()) ));
        else{
          conditionals.add(noSmoothing(s.substring(i, i+1), sub.substring(0, sub.length()-1) ));
        }
      }
      else
        if(i == 0)
          conditionals.add(laplaceSmoothing(sub, sub.substring(0, sub.length()), sequence));
        else
          conditionals.add(laplaceSmoothing(sub, sub.substring(0, sub.length()), sequence));

    }
    System.out.println(conditionals);

    if(this.representation == representation.PROBABILITY){
      double product = 1;
      for(double n : conditionals)
        product *= n;
      return product;
    }
    else{
      double logstuff = 0.0;
      for(double n : conditionals)
        logstuff += Math.log(n);
      return logstuff;
    }
  }
  

  public List<String> createNgrams(int n, String str){
    List<String> ngrams = new ArrayList<String>();
    String[] words = str.split("");
    for (int i = 1; i < words.length - n + 1; i++)
      ngrams.add(concat(words, i, i+n));
    return ngrams;
  }
  
  public static String concat(String[] words, int start, int end){
    StringBuilder sb = new StringBuilder();
    for(int i = start; i < end; i++)
      sb.append((i > start ? "" : "") + words[i]);
    return sb.toString();
  }
  
  public double noSmoothing(String a, String b){
    return ((this.ngram_prob.get(a) * this.ngram_prob.get(b))/this.ngram_prob.get(b));
  }
  
  private Set uniqueNgrams;
  public double laplaceSmoothing(String a, String b, List<T> V){
    uniqueNgrams = new HashSet(V);
    return ((1.0 + this.ngrams.getOrDefault(a, 0.0))/
            ((double)uniqueNgrams.size() + this.ngrams.getOrDefault(b, 0.0)));
  }
 
}