package ngram;

import com.alibaba.fastjson.annotation.JSONField;

import java.io.Serializable;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by freemso on Jul 09, 2017.
 */
public class NgramTree implements Serializable {
    @JSONField(name="NODE_MAP")
    private Map<Item, NgramNode> nodeMap = new HashMap<>();

    public NgramTree() {
    }

    public Map<Item, NgramNode> getNodeMap() {
        return nodeMap;
    }

    public void setNodeMap(Map<Item, NgramNode> nodeMap) {
        this.nodeMap = nodeMap;
    }

    public void insert(LanguageModel lm, Item[] ngram) {
        NgramNode node;
        Item item = ngram[0];
        if (nodeMap.containsKey(item)) {
            node = nodeMap.get(item);
        } else {
            node = new NgramNode(1);
            nodeMap.put(item, node);
        }
        node.insert(lm, Arrays.copyOfRange(ngram, 1, ngram.length));
    }

    public double count(Item[] ngram) {
        NgramNode node;
        Item item = ngram[0];
        if (nodeMap.containsKey(item)) {
            node = nodeMap.get(item);
            return node.count(Arrays.copyOfRange(ngram, 1, ngram.length));
        } else {
            return 0.0;
        }
    }
}
