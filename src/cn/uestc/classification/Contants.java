package cn.sumatrae.classification;

public class Contants {
    //Graphviz2.38 file path
    public static final String PIC_UTIL_PATH = "D:/Graphviz2.38/bin/dot.exe ";
    //trainSet
    public static final String DATA_PATH = "data\\weather.arff";
    //output filePath of model
    public static final String OUTPUT_PATH = DATA_PATH + "_output.xml";
    //public static final String LABEL_MARK = "#";
    //delimiter of reading 
    public static final String DATA_DELIM = ",";
    public static final String ATTRIBUTE_DELIM = ", ";

    public static final String DATA_FORMAT = "@attribute(.*)[{](.*?)[}]";

    public static final String OUTPUT_ENCODING = "UTF-8";
    public static final String PREDICT_FILE_PATH = DATA_PATH;
    public static final String VISUALIZATION_FILE = DATA_PATH + "_visual.txt";
    public static final String VISUALIZTION_PIC = DATA_PATH + "_pic.png";

    public static final int SELECTION_INFORMATION_GAIN = 1;
    public static final int SELECTION_ENTROPY = 2;
    public static final int SELECTION_GINI = 3;
}
