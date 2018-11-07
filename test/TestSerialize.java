import com.alibaba.fastjson.JSON;
import ngram.Item;

/**
 * Created by freemso on Jul 18, 2017.
 */
public class TestSerialize {
    public static void main(String[] args) {
        TestA t = new TestA(1, 2.0);
        String a = JSON.toJSONString(t);
        System.out.println(a);

        TestA t2 = JSON.parseObject(a, TestA.class);

        System.out.println(t2.a + t2.b);
    }

    static class TestA {
        public int a;
        public double b;

        public TestA(int a, double b) {
            this.a = a;
            this.b = b;
        }

        public TestA() {
        }

        public int getA() {
            return a;
        }

        public void setA(int a) {
            this.a = a;
        }

        public double getB() {
            return b;
        }

        public void setB(double b) {
            this.b = b;
        }
    }
}
