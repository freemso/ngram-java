package ngram;

import com.alibaba.fastjson.annotation.JSONField;

import java.io.Serializable;
import java.util.*;

/**
 * Created by freemso on Jul 09, 2017.
 */
public class LanguageModel implements Serializable {
    public static final int K = 5; // as the discount threshold in Kartz backoff
    public static final String START = ":S"; // The sentence start symbol

    @JSONField(name="N", ordinal=1)
    private int n; // as in n-gram

    @JSONField(name="ITEM_MAP", ordinal=2)
    private HashMap<String, Item> itemMap = new HashMap<>();

    @JSONField(name="TRAIN_WORD_COUNT", ordinal=3)
    private double trainingWordsCount = 0;

    @JSONField(name="MODEL_MAP", ordinal=4)
    private Map<Integer, NgramModel> modelMap = new HashMap<>();

    @JSONField(name="NGRAM_TREE", ordinal=5)
    private NgramTree ngramTree = new NgramTree();


    public LanguageModel(int n) {
        this.n = n;
        this.itemMap.put(START, new Item(START));
    }

    public LanguageModel() {
    }

    public Map<Integer, NgramModel> getModelMap() {
        return modelMap;
    }

    public void setModelMap(Map<Integer, NgramModel> modelMap) {
        this.modelMap = modelMap;
    }

    public int getN() {
        return n;
    }

    public void setN(int n) {
        this.n = n;
    }

    public NgramTree getNgramTree() {
        return ngramTree;
    }

    public void setNgramTree(NgramTree ngramTree) {
        this.ngramTree = ngramTree;
    }

    public HashMap<String, Item> getItemMap() {
        return itemMap;
    }

    public void setItemMap(HashMap<String, Item> itemMap) {
        this.itemMap = itemMap;
    }

    public double getTrainingWordsCount() {
        return trainingWordsCount;
    }

    public void setTrainingWordsCount(double trainingWordsCount) {
        this.trainingWordsCount = trainingWordsCount;
    }

    public NgramModel ngramModel(int n) {
        if (modelMap.containsKey(n)) {
            return modelMap.get(n);
        } else {
            NgramModel ngramModel = new NgramModel();
            modelMap.put(n, ngramModel);
            return ngramModel;
        }
    }

    public Iterator<NgramModel> modelIterator() {
        return modelMap.values().iterator();
    }

    public Item convert2Item(String itemStr) {
        if (itemMap.containsKey(itemStr)) {
            return itemMap.get(itemStr);
        } else {
            Item item = new Item(itemStr);
            itemMap.put(itemStr, item);
            return item;
        }
    }

    public void train(HashSet<ArrayList<String>> samples) {

        for (ArrayList<String> sample : samples) {
            // Add each group of n words to the n-gram counter, e.g., ...
            // [:S :S :S w1] w2 w3 w4 w5 w6
            // :S [:S :S w1 w2] w3 w4 w5 w6
            // :S :S [:S w1 w2 w3] w4 w5 w6
            // :S :S :S [w1 w2 w3 w4] w5 w6
            // :S :S :S w1 [w2 w3 w4 w5] w6
            // :S :S :S w1 w2 [w3 w4 w5 w6]
            Item[] items = new Item[n];
            for (int i = 0; i < n; i++) {
                items[i] = convert2Item(START);
            }
            for (String word : sample) {
                System.arraycopy(items, 1, items, 0, n - 1);
                items[n-1] = convert2Item(word);

                // Count the training word number
                trainingWordsCount += 1;

                // Insert the words into the counter and receive count for this ngram
                ngramTree.insert(this, items);
            }
        }

        // Build up all Ngram model
        Iterator<NgramModel> modelIterator = this.modelIterator();
        while (modelIterator.hasNext()) {
            NgramModel model = modelIterator.next();
            model.buildModel();
        }
    }

    public double unsmoothedProb(Item[] words) {
        double count = ngramTree.count(words);
        Item[] context = Arrays.copyOfRange(words, 0, words.length-1);
        double contextCount = ngramTree.count(context);
        if (count > 0) {
            return count / contextCount;
        } else {
            return 0.0;
        }
    }

    public double addOneSmoothedProb(Item[] words) {
        double count = ngramTree.count(words);
        Item[] context = Arrays.copyOfRange(words, 0, words.length-1);
        double contextCount = ngramTree.count(context);
        return (count + 1.0) / (contextCount + itemMap.size());
    }

    public double goodTuringSmoothedProb(Item[] words) {
        Map<Double, Double> goodTuringNormMap = this.ngramModel(n).getGoodTuringNormMap();
        double count = ngramTree.count(words);
        double normedCount = goodTuringNormMap.getOrDefault(count, 1.0);
        return normedCount / trainingWordsCount;
    }

    public double kartzSmoothedProb(Item[] words) {
        int n = words.length;
        if (n == 1) {
            double count = ngramTree.count(words);
            return count / trainingWordsCount;
        } else {
            Map<Double, Double> kartzDiscountMap = this.ngramModel(n).getKartzDiscountMap();
            double count = ngramTree.count(words);
            if (count > 0) {
                Item[] context = Arrays.copyOfRange(words, 0, words.length-1);
                double contextCount = ngramTree.count(context);
                if (count > K) {
                    return count / contextCount;
                } else {
                    return kartzDiscountMap.getOrDefault(count, 0.0) / contextCount;
                }
            } else {
                double sum1 = 0.0;
                for (Item item : itemMap.values()) {
                    Item[] words2 = Arrays.copyOfRange(words, 0, words.length);
                    Item[] context = Arrays.copyOfRange(words, 0, words.length-1);
                    words2[words2.length-1] = item;
                    double count2 = ngramTree.count(words2);
                    double contextCount = ngramTree.count(context);
                    if (count2 > 0) {
                        sum1 += kartzDiscountMap.getOrDefault(count, 0.0) / contextCount;
                    }
                }
                double beta = 1 - sum1;
                double sum2 = 0.0;
                for (Item item : itemMap.values()) {
                    Item[] words2 = Arrays.copyOfRange(words, 0, words.length);
                    Item[] nngram = Arrays.copyOfRange(words, 1, words.length);
                    words2[words2.length-1] = item;
                    double count2 = ngramTree.count(words2);
                    if (count2 == 0) {
                        sum2 += kartzSmoothedProb(nngram);
                    }
                }
                assert sum2 > 0;
                double alpha = beta / sum2;

                return alpha * kartzSmoothedProb(Arrays.copyOfRange(words, 1, words.length));
            }
        }
    }


    public Item generateNextWord(Item[] context) {
        // Find the max prob of the possible ngrams
        Item mostProbWord = null;
        double maxProb = -1;
        for (Item item : itemMap.values()) {
            Item[] words = new Item[n];
            System.arraycopy(context, 0, words, 0, context.length);
            words[n-1] = item;
            double thisProb = goodTuringSmoothedProb(words);
            if (thisProb > maxProb) {
                maxProb = thisProb;
                mostProbWord = item;
            }
        }
        return mostProbWord;
    }


    public double evaluate(Set<ArrayList<String>> testSamples) {
        double totalCount = 0;
        double correctCount = 0;
        for (ArrayList<String> words : testSamples) {
            Item[] context = new Item[n-1];
            // Fill up the words array with START symbols
            for (int i = 0; i < context.length; i++) {
                context[i] = convert2Item(START);
            }

            // For every word in the sentence
            for (String word : words) {
                // Generate a new word based on context
                Item predict = generateNextWord(context);
                totalCount++;
                if (predict.equals(word)) {
                    correctCount++;
                } else {
//                    System.out.println(predict.toString() + " --> " + item.toString());
                }
                // Update context with the new word
                System.arraycopy(context, 1, context, 0, context.length-1);
                context[n-2] = convert2Item(word);
            }
        }
        return correctCount/totalCount;
    }
}

