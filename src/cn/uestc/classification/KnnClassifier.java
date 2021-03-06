package cn.uestc.classification;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;

public class KnnClassifier {
    ArrayList<Data> trainSet;
    ArrayList<Data> testSet;
    int k;

    public static void main(String args[]) {
        KnnClassifier knn = new KnnClassifier("data/train.txt", 1);
        knn.predict("data/test.txt");

    }

    public KnnClassifier(String filePath, int k) {
        trainSet = loadDataSet(filePath);

        this.k = k;
    }

    public ArrayList<Data> loadDataSet(String filePath) {
        ArrayList<Data> dataSet = new ArrayList<Data>();
        File file = new File(filePath);
        FileReader fr;
        try {
            fr = new FileReader(file);
            BufferedReader bis = new BufferedReader(fr);
            String line = null;
            while ((line = bis.readLine()) != null) {
                String[] str = line.trim().split(",");
                double[] data = new double[str.length - 1];
                for (int i = 0; i < data.length; i++) {
                    data[i] = Double.parseDouble(str[i]);
                }
                Data dataObject = new Data(data, str[str.length - 1]);
                dataSet.add(dataObject);
            }
            bis.close();

        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return dataSet;

    }

    public void predict(String testPath) {
        testSet = this.loadDataSet(testPath);
        int correct = 0;
        for (int n = 0; n < testSet.size(); n++) {
            Data data = testSet.get(n);
            String predict = classify(data);
            System.out.println("Predicted (True) " + predict + "(" + data.label + ")");
            if (predict.equals(data.label)) {
                correct++;
            }
        }
        System.out.println("Accuracy: " + correct * 100.0 / testSet.size() + "%");

    }

    public String classify(Data test) {
        double[] distances = new double[trainSet.size()];

        for (int n = 0; n < trainSet.size(); n++) {
            distances[n] = getDist(test, trainSet.get(n));
        }
        HashMap<String, Integer> voteMap = new HashMap<String, Integer>();

        for (int n = 0; n < k; n++) {
            double min = Integer.MAX_VALUE;
            int minIndex = -1;
            for (int m = 0; m < trainSet.size(); m++) {
                if (distances[m] < min) {
                    min = distances[m];
                    minIndex = m;
                }
            }
            if (minIndex == -1) {
                System.out.println("error");
                System.exit(0);
            }
            distances[minIndex] = Integer.MAX_VALUE;
            if (voteMap.containsKey(trainSet.get(minIndex).label)) {
                voteMap.put(trainSet.get(minIndex).label,
                        voteMap.get(trainSet.get(minIndex).label) + 1);
            } else {
                voteMap.put(trainSet.get(minIndex).label, 1);
            }
        }

        Iterator<String> i = voteMap.keySet().iterator();
        String predict = null;
        int maxVote = 0;
        while (i.hasNext()) {
            String key = i.next();
            if (voteMap.get(key) > maxVote) {
                maxVote = voteMap.get(key);
                predict = key;
            }
        }
        return predict;
    }

    private double getDist(Data test, Data data) {
        double sum = 0;
        for (int n = 0; n < test.data.length; n++) {
            sum += (test.data[n] - data.data[n]) * (test.data[n] - data.data[n]);
        }
        return Math.sqrt(sum);
    }

    private class Data {
        double[] data;
        String label;

        Data(double[] data, String label) {
            this.data = data;
            this.label = label;
        }
    }


}
