package ngram;

import com.alibaba.fastjson.annotation.JSONField;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by freemso on Jul 09, 2017.
 */
public class NgramNode implements Serializable {
    @JSONField(name="LEVEL", ordinal = 1)
    private double level;
    @JSONField(name="COUNT", ordinal = 2)
    private double count = 0;
    @JSONField(name="CHILDREN_MAP", ordinal = 3)
    private Map<Item, NgramNode> childrenMap = new HashMap<>();

    public NgramNode(double level) {
        this.level = level;
    }

    public NgramNode() {}

    public double getCount() {
        return count;
    }

    public void setCount(double count) {
        this.count = count;
    }

    public double getLevel() {
        return level;
    }

    public void setLevel(double level) {
        this.level = level;
    }

    public Map<Item, NgramNode> getChildrenMap() {
        return childrenMap;
    }

    public void setChildrenMap(Map<Item, NgramNode> childrenMap) {
        this.childrenMap = childrenMap;
    }

    public void insert(LanguageModel lm, Item[] ngram) {
        NgramModel ngramModel = lm.ngramModel((int)(level));
        ngramModel.recordInsertion(this.count);
        this.count++;

        // Recursive base case
        if (ngram.length > 0) {
            NgramNode nextNode;
            Item nextItem = ngram[0];
            if (childrenMap.containsKey(nextItem)) {
                nextNode = childrenMap.get(nextItem);
            } else {
                nextNode = new NgramNode(this.level+1);
                childrenMap.put(nextItem, nextNode);
            }
            nextNode.insert(lm, Arrays.copyOfRange(ngram, 1, ngram.length));
        }
    }

    public double count(Item[] ngram) {
        if (ngram.length == 0) {
            return count;
        } else {
            NgramNode nextNode;
            Item nextItem = ngram[0];
            if (childrenMap.containsKey(nextItem)) {
                nextNode = childrenMap.get(nextItem);
                return nextNode.count(Arrays.copyOfRange(ngram, 1, ngram.length));
            } else {
                return 0.0;
            }
        }
    }


}
