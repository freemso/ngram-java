package ngram;

import com.alibaba.fastjson.annotation.JSONField;

import java.io.Serializable;

/**
 * Created by freemso on Jul 16, 2017.
 */
public class Item implements Serializable {
    @JSONField(name="NAME")
    private String name;

    public Item(String name) {
        this.name = name;
    }

    public Item() {}

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String toString() {
        return this.name;
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) return true;
        if (o == null) return false;
        if (o instanceof Item) {
            Item that = (Item) o;
            return that.name.equals(this.name);
        }
        if (o instanceof String) {
            String that = (String) o;
            return that.equals(this.toString());
        }
        return false;
    }

    @Override
    public int hashCode() {
        return name.hashCode();
    }
}
