# Assignment: Probability: N-gram Language Model

The goal of this assignment is to give you some experience implementing a probabilistic model. It assumes you have already familiarized yourself with Git and Maven in a previous assignment.

## Get a copy of the assignment template

1. Fork the repository https://git.cis.uab.edu/cs-460/probability-ngram.

2. Give your instructor access to your fork. **I cannot grade your assignment unless you complete this step.**

## Compile and test the code

1.  Compile the code. Run the following command:

        mvn clean compile

    Everything should compile and you should see a message like:

        [INFO] ------------------------------------------------------------------------
        [INFO] BUILD SUCCESS
        [INFO] ------------------------------------------------------------------------

2.  Test the code. Run the following command:

        mvn clean test

    The tests should fail, and you should see a message like:

        Failed tests:
          testCharacter2gram(edu.uab.cis.probability.ngram.NgramLanguageModelTest): expected:<0.008999999999999998> but was:<0.0>
          testInteger4gramLogprobLaplace(edu.uab.cis.probability.ngram.NgramLanguageModelTest): expected:<-5.9881387101384> but was:<0.0>
          testCharacter2gramLaplace(edu.uab.cis.probability.ngram.NgramLanguageModelTest): expected:<0.012794668887963346> but was:<0.0>

        Tests run: 3, Failures: 3, Errors: 0, Skipped: 0

        [INFO] ------------------------------------------------------------------------
        [INFO] BUILD FAILURE
        [INFO] ------------------------------------------------------------------------

    Note the `clean`, which ensures that Maven alone (not your development environment) is compiling your code.

## Implement your part of the code

Your task is to implement the methods marked `TODO` in the class `edu.uab.cis.probability.ngram.NgramLanguageModel`. All your code should go into `NgramLanguageModel.java`, and **you should not modify any other files**.

*   `probability(List<T>)`: Return the estimated n-gram probability of the sequence, calculated as P(w<sub>0</sub>,...,w<sub>k</sub>) = ∏<sub>i=0,...,k</sub> P(w<sub>i</sub>|w<sub>i-n+1</sub>, w<sub>i-n+2</sub>, ..., w<sub>i-1</sub>).
    For example, a 3-gram language model would calculate the probability of the sequence `[A,B,B,C,A]` as `P(A,B,B,C,A) = P(A)*P(B|A)*P(B|A,B)*P(C|B,B)*P(A|B,C)`.
    *   The exact calculation of the conditional probabilities in this expression depends on the smoothing method:
        *   If the smoothing method is `NONE`, then an n-gram w<sub>1</sub>,...,w<sub>n</sub> will have its joint probability P(w<sub>1</sub>,...,w<sub>n</sub>) estimated as #(w<sub>1</sub>,...,w<sub>n</sub>) / N, where N indicates the total number of all 1-grams observed during training. Note that we have defined only the joint probability of an n-gram here. You will have to derive the conditional probability from this definition and your knowledge of probability theory.
        *   If the smoothing method is `LAPLACE`, then an n-gram w<sub>1</sub>,...,w<sub>n</sub> will have its conditional probability P(w<sub>n</sub>|w<sub>1</sub>,...,w<sub>n-1</sub>) estimated as (1 + #(w<sub>1</sub>,...,w<sub>n</sub>)) / (V + #(w<sub>1</sub>,...,w<sub>n-1</sub>)), where # indicates the number of times an n-gram was observed during training and V indicates the number of *unique* 1-grams observed during training. Note that Laplace smoothing directly defines the conditional probability of an n-gram.
    *   The calculation also depends on the representation selected:
        *   If the representation is `PROBABILITY`, then you should calculate probabilities as normal, resulting in numbers in the range `[0,1]`.
        *   If the representation is `LOG_PROBABILITY`, then you should calculate log-probabilities instead of probabilities. In every case where probabilities would have been multiplied, take advantage of the fact that `log(P(x)*P(y)) = log(P(x)) + log(P(y))` and add log-probabilities instead. This will improve efficiency since addition is faster than multiplication, and will avoid some numerical underflow problems that occur when taking the product of many small probabilities close to zero. Your log-probabilities should be in the range `(-∞,0]`.

* `train(List<T>)`: Train the language model with the n-grams from a sequence of items. Collect whatever statistics you need from the n-grams in the training sequence to be able to predict probabilities later in `probability(List<T>)`.

* `NgramLanguageModel(int, Representation, Smoothing)`: Creates an n-gram language model, specifying the representation and smoothing to be used by `probability(List<T>)`.

## Test your code

1.  Re-run the tests:

        mvn clean test

    You should now see a message like:

        [INFO] ------------------------------------------------------------------------
        [INFO] BUILD SUCCESS
        [INFO] ------------------------------------------------------------------------

    Your code is now passing the tests that were given to you. This is a good sign, but note that **a successful `mvn test` does not guarantee you full credit on an assignment**. I will run extra tests on your code when grading it.

## Submit your assignment

1.  To submit your assignment, make sure that you have pushed all of your changes to your repository at `git.cis.uab.edu`.

2.  I will inspect the date of your last push to your `git.cis.uab.edu` repository. If it is after the deadline, your submission will be marked as late. So please **do not push changes to `git.cis.uab.edu` after the assignment deadline** unless you intend to submit a late assignment.
