package ngram;

import com.alibaba.fastjson.annotation.JSONField;

import java.io.Serializable;
import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by freemso on Jul 09, 2017.
 */
public class NgramModel implements Serializable {
    @JSONField(name="GOOD_TURING_NORM_MAP")
    private Map<Double, Double> goodTuringNormMap = new HashMap<>();
    @JSONField(name="KARTZ_DISCOUNT_MAP")
    private Map<Double, Double> kartzDiscountMap = new HashMap<>();
    @JSONField(name="NGRAM_COUNT_RECORD")
    private Map<Double, Double> ngramCountRecord = new HashMap<>();  // To memorize the number of ngrams with certain count

    public NgramModel() {
    }

    public void setGoodTuringNormMap(Map<Double, Double> goodTuringNormMap) {
        this.goodTuringNormMap = goodTuringNormMap;
    }

    public void setKartzDiscountMap(Map<Double, Double> kartzDiscountMap) {
        this.kartzDiscountMap = kartzDiscountMap;
    }

    public void setNgramCountRecord(Map<Double, Double> ngramCountRecord) {
        this.ngramCountRecord = ngramCountRecord;
    }

    public void recordInsertion(double oldCount) {
        if (oldCount > 0) {
            ngramCountRecord.put(oldCount,
                    ngramCountRecord.getOrDefault(oldCount, 0.0) - 1);
        }
        ngramCountRecord.put(oldCount + 1,
                ngramCountRecord.getOrDefault(oldCount + 1, 0.0) + 1);
    }

    public Map<Double, Double> getNgramCountRecord() {
        return ngramCountRecord;
    }

    public void buildModel() {
        for (Double rawCount :
                this.ngramCountRecord.keySet()) {
            double numOfCount = ngramCountRecord.getOrDefault(rawCount, 0.0);
            double numOfCountPlus = ngramCountRecord.getOrDefault(rawCount + 1, 0.0);
            if (numOfCount == 0) {
                goodTuringNormMap.put(rawCount, rawCount + 1);
            } else if (numOfCountPlus == 0) {
                goodTuringNormMap.put(rawCount, rawCount);
            } else {
                double normCount = (rawCount + 1) * numOfCountPlus / numOfCount;
                goodTuringNormMap.put(rawCount, normCount);
            }
        }
        goodTuringNormMap.put(0.0, goodTuringNormMap.getOrDefault(1.0, 0.1));

        for (Double rawCount :
                this.ngramCountRecord.keySet()) {
            double normCount = goodTuringNormMap.getOrDefault(rawCount, 0.0);
            double temp = (LanguageModel.K + 1)
                    * ngramCountRecord.getOrDefault(LanguageModel.K + 1.0, 0.1)
                    / ngramCountRecord.getOrDefault(1.0, 0.1);
            assert temp != 1.0 && temp != 0.0;
            double discount = (normCount - temp * rawCount) / (1 - temp);
            kartzDiscountMap.put(rawCount, discount);
        }
    }

    public Map<Double, Double> getGoodTuringNormMap() {
        return goodTuringNormMap;
    }

    public Map<Double, Double> getKartzDiscountMap() {
        return kartzDiscountMap;
    }
}
