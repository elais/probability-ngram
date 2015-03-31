package edu.uab.cis.probability.ngram;

import static com.google.common.collect.Lists.charactersOf;

import java.util.Arrays;

import org.junit.Assert;
import org.junit.Test;

import edu.uab.cis.probability.ngram.NgramLanguageModel.Representation;
import edu.uab.cis.probability.ngram.NgramLanguageModel.Smoothing;

public class NgramLanguageModelTest {

  @Test(timeout = 10000)
  public void testCharacter2gram() {
    NgramLanguageModel<Character> model =
        new NgramLanguageModel<>(2, Representation.PROBABILITY, Smoothing.NONE);
    model.train(charactersOf("babbaaaa"));
    Assert.assertEquals((5.0 / 8.0) * (3.0 / 5.0) * (3.0 / 5.0) * (3.0 / 5.0) * (1.0 / 5.0)
        * (1.0 / 3.0), model.probability(charactersOf("aaaabb")), 1e-10);
  }

  @Test(timeout = 10000)
  public void testCharacter2gramLaplace() {
    NgramLanguageModel<Character> model =
        new NgramLanguageModel<>(2, Representation.PROBABILITY, Smoothing.LAPLACE);
    model.train(charactersOf("babbaaaa"));
    Assert.assertEquals((6.0 / 10.0) * (4.0 / 7.0) * (4.0 / 7.0) * (4.0 / 7.0) * (2.0 / 7.0)
        * (2.0 / 5.0), model.probability(charactersOf("aaaabb")), 1e-10);
  }

  @Test(timeout = 10000)
  public void testInteger4gramLogprobLaplace() {
    NgramLanguageModel<Integer> model =
        new NgramLanguageModel<>(4, Representation.LOG_PROBABILITY, Smoothing.LAPLACE);
    model.train(Arrays.asList(1, 0, 0, 1, 1, 1, 0, 0, 1, 0, 1, 0, 1, 0, 0, 0));
    Assert.assertEquals(
        Math.log((8.0 / 18.0) * (6.0 / 9.0) * (4.0 / 7.0) * (2.0 / 5.0) * (1.0 / 3.0) * (1.0 / 3.0)
            * (1.0 / 3.0)),
        model.probability(Arrays.asList(1, 0, 0, 0, 0, 0, 1)),
        1e-10);
  }

  @Test(timeout = 10000)
  public void testCharacterNotInTraining() {
    NgramLanguageModel<Character> model =
        new NgramLanguageModel<>(1, Representation.PROBABILITY, Smoothing.NONE);
    model.train(charactersOf("abccabbacbaba"));
    Assert.assertEquals(0, model.probability(charactersOf("d")), 1e-10);
  }

  @Test(timeout = 10000)
  public void testCharacterNotInTrainingLaplace() {
    NgramLanguageModel<Character> model =
        new NgramLanguageModel<>(1, Representation.PROBABILITY, Smoothing.LAPLACE);
    model.train(charactersOf("abccabbacbaba"));
    Assert.assertEquals((1.0/16.0), model.probability(charactersOf("d")), 1e-10);
  }

  @Test(timeout = 10000)
  public void testCharacter3gram() {
    NgramLanguageModel<Character> model =
        new NgramLanguageModel<>(3, Representation.PROBABILITY, Smoothing.NONE);
    model.train(charactersOf("abccabbacbaba"));
    Assert.assertEquals(0.0, model.probability(charactersOf("abbca")), 1e-10);
  }

  @Test(timeout = 10000)
  public void testCharacter3gramLaplace() {
    NgramLanguageModel<Character> model =
        new NgramLanguageModel<>(3, Representation.PROBABILITY, Smoothing.LAPLACE);
    model.train(charactersOf("abccabbacbaba"));
    Assert.assertEquals(
        (6.0 / 16.0) * (4.0 / 8.0) * (2.0 / 6.0) * (1.0 / 4.0) * (1.0 / 4.0),
        model.probability(charactersOf("abbca")), 1e-10);
  }
//  @Test(timeout = 10000)
//  public void testString4gramLaplace() {
//    NgramLanguageModel<String> model =
//        new NgramLanguageModel<>(4, Representation.PROBABILITY, Smoothing.LAPLACE);
//    model.train(Arrays.asList("this", "is", "a", "sentence", "and", "my", "dog", "is", "named", "Newton", "he", "is", "a", "good", "dog", "jk"));
//    Assert.assertEquals((2.0/28.0) * (2.0/13.0) * (2.0/13.0) * (2.0/13.0) * (1.0/13.0),
//        model.probability(Arrays.asList("my dog is named Charlie")),
//        1e-10);
//  }
}
