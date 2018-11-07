import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.serializer.SerializerFeature;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import ngram.Item;
import ngram.LanguageModel;

import java.io.*;
import java.util.*;

/**
 * Created by freemso on Jul 16, 2017.
 */
public class SanityTest {
    public static void main(String[] args) {
        if (args.length != 4) {
            System.err.println("You must supply 4 arguments:\n(1) Data directory\n" +
                    "(2) An integer n > 1\n(3) Evaluation split rate(e.g. 0.75)\n" +
                    "(4) Evaluation round number(e.g. 10)");
            System.exit(1);
        }
        File dataDir = new File(args[0]);
        if (!dataDir.isDirectory()) {
            System.err.println("The first argument must be a directory!");
            System.exit(1);
        }
        int n = Integer.parseInt(args[1]);

        double rate = Double.parseDouble(args[2]);

        int round = Integer.parseInt(args[3]);

        Parser parser = new Parser();

        List<File> dataFiles = Arrays.asList(dataDir.listFiles());
        int trainDataSize = (int) (dataFiles.size() * rate);

        // Parse the data
        List<HashSet<ArrayList<String>>> examples = parser.parse(dataFiles);

        double totalAccuracy = 0;
        for (int i = 0; i < round; i++) {
            System.out.println("=======================Round #"+(i+1)+"=========================");
            // Shuffle and split the data
            Collections.shuffle(examples);
            HashSet<ArrayList<String>> trainingExamples = new HashSet<>();
            HashSet<ArrayList<String>> testExamples = new HashSet<>();
            for (int j = 0; j < trainDataSize; j++) {
                trainingExamples.addAll(examples.get(j));
            }
            for (int j = trainDataSize; j < examples.size(); j++) {
                testExamples.addAll(examples.get(j));
            }

            // Train the model
            System.out.println("Training...");
            LanguageModel languageModel = new LanguageModel(n);
            languageModel.train(trainingExamples);
            System.out.println("Serializing...");
//            String modelJson = JSON.toJSONString(languageModel,
//                    SerializerFeature.WriteClassName,
//                    SerializerFeature.QuoteFieldNames,
//                    SerializerFeature.PrettyFormat,
//                    SerializerFeature.WriteMapNullValue
//            );
//            Gson gson = new GsonBuilder().setPrettyPrinting().create();
//            String modelJson = gson.toJson(languageModel);
//            System.out.println(modelJson);
//            System.out.println("Deserializing...");
//            LanguageModel lm = JSON.parseObject(modelJson, LanguageModel.class);
//            LanguageModel lm = gson.fromJson(modelJson, LanguageModel.class);

            String modelFile = "model/lm.model";
            saveModel(languageModel, modelFile);

            System.out.println("Deserializing...");

            LanguageModel lm = loadModel(modelFile);

            // Evaluate the model
            double accuracy = lm.evaluate(testExamples);
            System.out.println("Accuracy: "+accuracy);
            totalAccuracy += accuracy;
        }

        System.out.format("Average accuracy of %d rounds: %f", round, totalAccuracy / round);
    }

    public static void saveModel(LanguageModel model, String filename) {
        try {
            // Serialize data object to a file
            ObjectOutputStream out = new ObjectOutputStream(
                    new FileOutputStream(filename));
            out.writeObject(model);
            out.close();
        } catch (IOException e) {
            System.err.println("Save error");
            e.printStackTrace();
        }
    }

    public static LanguageModel loadModel(String filename) {
        LanguageModel model = null;
        try {
            FileInputStream door = new FileInputStream(filename);
            ObjectInputStream reader = new ObjectInputStream(door);
            model = (LanguageModel) reader.readObject();
            reader.close();
        } catch (Exception e) {
            System.err.println("Load error");
            e.printStackTrace();
        }
        return model;
    }
}
