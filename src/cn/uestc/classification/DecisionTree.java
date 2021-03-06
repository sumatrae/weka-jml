package cn.uestc.classification;

import java.io.*;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.dom4j.Document;
import org.dom4j.DocumentHelper;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;
import org.dom4j.io.SAXReader;
import org.dom4j.io.XMLWriter;


public class DecisionTree {
    private ArrayList<Attribute> attributes = new ArrayList<Attribute>();
    private int type;
    private int labelIndex;
    private ArrayList<ArrayList<String>> dataLines = new ArrayList<ArrayList<String>>();        //maintain data of every line
    // output decision tree by type of XML
    private Document xmldoc;
    private Element root;


    public DecisionTree() {
        this.xmldoc = DocumentHelper.createDocument();
        this.root = xmldoc.addElement("root");
        this.root.addElement(this.getClass().getSimpleName()).addAttribute("value", "null");
        this.type = Contants.SELECTION_INFORMATION_GAIN;
    }

    public DecisionTree(int type) {
        super();
        this.type = type;
    }

    /**
     * getters and setters
     */
    public int getLabelIndex() {
        return this.labelIndex;
    }

    public ArrayList<Attribute> getAttributes() {
        return this.attributes;
    }

    public ArrayList<ArrayList<String>> getDataLines() {
        return this.dataLines;
    }

    public void setLabelIndex(int index) {
        this.labelIndex = index;
    }

    public int getType() {
        return this.type;
    }

    /**
     * get value of data(x,y)
     */
    public String getDataLinesXY(int x, int y) {
        if (x < this.getDataLines().size()) {
            ArrayList<String> thisLine = this.getDataLines().get(x);
            if (y < thisLine.size()) {
                return thisLine.get(y);
            }
        }
        System.out.println("out-of-bounds access");
        return null;
    }


    public static void main(String[] args) throws Exception {
        DecisionTree decisionTree = new DecisionTree();
        //train
        decisionTree.run(Contants.DATA_PATH, -1);
        //save result of training
        decisionTree.printTreeXML(Contants.OUTPUT_PATH, true);

        //predict
        decisionTree.predict(Contants.PREDICT_FILE_PATH, Contants.OUTPUT_PATH);
    }

    /**
     * use XML model to predict after training
     */
    private void predict(String predictFilePath, String xmlModelPath) throws Exception {
        //ensure training model file exist
        File file = new File(xmlModelPath);
        if (!file.exists() || !file.isFile()) {
            System.out.println("wrong xml file");
            System.exit(-1);
        }
        //ensure predicting file exist
        File toPredictFile = new File(predictFilePath);
        if (!toPredictFile.exists() || !toPredictFile.isFile()) {
            System.out.println("wrong predicting file");
            System.exit(-1);
        }
        //read xml model file
        SAXReader saxReader = new SAXReader();
        Document document = saxReader.read(xmlModelPath);
        //get label of root
        Element treeRoot = document.getRootElement().element(this.getClass().getSimpleName());
        BufferedReader bufferederReader = new BufferedReader(new FileReader(toPredictFile));
        //read by line
        String line = bufferederReader.readLine();

        ArrayList<String> nameList = new ArrayList<String>();
        while (line != null) {
            if (line.startsWith("@attribute")) {
                //record all names of attribute
                nameList.add(line.split(" ")[1]);
            } else if (line.startsWith("@data")) {
                line = bufferederReader.readLine();
                while (line != null) {
                    System.out.println(line);
                    String[] dataLine = line.split(Contants.DATA_DELIM);
                    String answer = this.findAnswer(dataLine, treeRoot, nameList);
                    System.out.println("predict  = " + answer);
                    line = bufferederReader.readLine();
                }
            }
            line = bufferederReader.readLine();
        }

    }

    /**
     * predict by model
     */
    private String findAnswer(String[] dataLine, Element treeRoot, ArrayList<String> nameList) {
        int i = 0;
        Element tmp = null;
        boolean find = false;
        List<Element> childs = treeRoot.elements();
        while (childs.size() > 0) {
            for (Element e : childs) {
//                System.out.println(e.attribute("value").getValue());
                String namexml = e.getName();
                int index = nameList.indexOf(namexml);
                String dataxml = dataLine[index];
                if (e.attribute("value").getValue().equals(dataxml)) {
                    find = true;
                    childs = e.elements();
                    tmp = e;
                    i++;
                    break;
                }
            }
            if (!find) {
                System.out.println("no find !");
                System.exit(-1);
            }
        }
//        System.out.println("answer = " + tmp.getData());
        return tmp.getData().toString();
    }


    public void run(String dataPath, int labelIndex) throws Exception {
        //read trainSet 
        ArrayList<Integer> numOfDataLineList = this.loadData(dataPath, labelIndex);
        LinkedList<Integer> indexs = new LinkedList<Integer>();
        for (int i = 0; i < this.getAttributes().size(); i++) {
            if (i != this.getLabelIndex()) {
                indexs.add(i);
            }
        }
        //build decision tree
        this.buildDT(this.getClass().getSimpleName(), "null", numOfDataLineList, indexs);
    }


    public ArrayList<Integer> loadData(String dataPath, int labelIndex) throws Exception {
        ArrayList<Integer> numLineList = new ArrayList<Integer>();
        File file = new File(dataPath);
        if (!file.exists()) {
            System.out.println("filePath don't exist: " + dataPath);
            System.exit(-1);
        } else if (!file.isFile()) {
            System.out.println("file don't exist：" + dataPath);
            System.exit(-1);
        }
        BufferedReader bufferedReader =
                new BufferedReader(new FileReader(file));
        //read by line
        String line = bufferedReader.readLine();
        int lineNum = -1;
        Pattern pattern = Pattern.compile(Contants.DATA_FORMAT);
        Matcher matcher = null;
        while (line != null) {
            matcher = pattern.matcher(line);
            if (matcher.find()) {
                String attributeName = matcher.group(1).trim();
                Attribute attribute = new Attribute(attributeName);

                String[] values = matcher.group(2).split(Contants.ATTRIBUTE_DELIM);
                attribute.setValueList(new ArrayList<String>(Arrays.asList(values)));
                this.getAttributes().add(attribute);
            } else if (line.startsWith("@data")) {
                if (labelIndex == -1) {
                    this.setLabelIndex(this.getAttributes().size() - 1);
                } else if (labelIndex < this.getAttributes().size() && labelIndex > -1) {
                    this.setLabelIndex(labelIndex);
                } else {
                    System.out.println("label wrong");
                    System.exit(-1);
                }

                this.getAttributes().get(this.getLabelIndex()).setIsLabel();
                line = bufferedReader.readLine();
                while (line != null) {
                    String[] dataLine = line.split(Contants.DATA_DELIM);
                    this.getDataLines().add(
                            new ArrayList<String>(Arrays.asList(dataLine)));
                    lineNum++;
                    numLineList.add(lineNum);
                    line = bufferedReader.readLine();
                }
            }
            line = bufferedReader.readLine();
        }
        bufferedReader.close();
        //System.out.println("data has been loaded, total: " + this.getDataLines().size() + "row,  attribute size: " + this.getAttributes().size() + "" + lineNum);
        return numLineList;
    }

    /**
     * get entropy
     */
    public double getEntropy(int[] arr) {
        double entropy = 0.0;
        int sum = 0;
        for (int i = 0; i < arr.length; i++) {
            entropy += -1 * arr[i] * Math.log(arr[i] + Double.MIN_VALUE) / Math.log(2);
            sum += arr[i];
        }
        entropy += sum * Math.log(sum + Double.MIN_VALUE) / Math.log(2);
        entropy /= sum;
        return entropy;
    }

    /**
     * whether is subset divided
     */
    public boolean isClear(ArrayList<Integer> subset) {
        String value = this.getDataLinesXY(subset.get(0), this.getLabelIndex());
        for (int i = 1; i < subset.size(); i++) {
            String next = this.getDataLinesXY(subset.get(i), this.getLabelIndex());
            if (!value.equals(next))
                return false;
        }
        return true;
    }

    /**
     * calculate entropy accroding index attribute
     */
    public double calNodeEntropy(ArrayList<Integer> subset, int index) {
        int sum = subset.size();
        double entropy = 0.0;
        int size = this.getAttributes().get(index).getValueList().size();
        int sizeLabel = this.getAttributes().get(this.getLabelIndex()).getValueList().size();
        int[][] info = new int[size][];
        for (int i = 0; i < info.length; i++) {
            info[i] = new int[sizeLabel];
        }

        int[] count = new int[size];
        for (int i = 0; i < sum; i++) {
            int n = subset.get(i);
            String nodeValue = this.getDataLinesXY(n, index);
            int nodeIndex = this.getAttributes().get(index).getValueList().indexOf(nodeValue);
            count[nodeIndex]++;
            String decvalue = this.getDataLinesXY(n, this.getLabelIndex());
            int labelIndex = this.getAttributes().get(this.getLabelIndex()).getValueList().indexOf(decvalue);
            info[nodeIndex][labelIndex]++;
        }
        for (int i = 0; i < info.length; i++) {
            entropy += getEntropy(info[i]) * count[i] / sum;
        }
        //System.out.println(entropy);
        return entropy;
    }

    /**
     * build Decision tree
     */
    public void buildDT(String name, String value, ArrayList<Integer> subset,
                        LinkedList<Integer> subIndex) {

        Element ele = null;
        List<Element> list = root.selectNodes("//" + name);
        Iterator<Element> iter = list.iterator();
        while (iter.hasNext()) {
            ele = iter.next();
            if (ele.attributeValue("value").equals(value))
                break;
        }
        if (isClear(subset)) {
            ele.setText(this.getDataLinesXY(subset.get(0), this.getLabelIndex()));
            return;
        }

        int minIndex = -1;
        double minEntropy = Double.MAX_VALUE;
        for (int i = 0; i < subIndex.size(); i++) {
            if (i == this.getLabelIndex())
                continue;
            //calculate
            double entropy = calNodeEntropy(subset, subIndex.get(i));
            if (entropy < minEntropy) {
                minIndex = subIndex.get(i);
                minEntropy = entropy;
            }
        }
        String nodeName = this.getAttributes().get(minIndex).getAttributeName();
        subIndex.remove(new Integer(minIndex));
        //divide
        ArrayList<String> attValuesSet = this.getAttributes().get(minIndex).getValueList();
        for (String val : attValuesSet) {
            ele.addElement(nodeName).addAttribute("value", val);
            ArrayList<Integer> tmp = new ArrayList<Integer>();
            for (int i = 0; i < subset.size(); i++) {
                if (this.getDataLinesXY(subset.get(i), minIndex).equals(val)) {
                    tmp.add(subset.get(i));
                }
            }
            buildDT(nodeName, val, tmp, subIndex);
        }
    }

    /**
     * save result  by xml
     */
    public void printTreeXML(String filename, boolean draw) throws Exception {
        try {
            FileWriter fileWriter = new FileWriter(new File(filename));
            OutputFormat format = OutputFormat.createPrettyPrint();
            format.setEncoding(Contants.OUTPUT_ENCODING);
            XMLWriter output = new XMLWriter(fileWriter, format);
            output.write(xmldoc);
            output.close();
            //whether visual
            if (draw) {
                this.draw(xmldoc);
            }
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
    }

    /**
     * visualization
     */
    private void draw(Document xmldoc) throws Exception {
        //write to file
        OutputStreamWriter outputStreamWriter =
                new OutputStreamWriter(new FileOutputStream(Contants.VISUALIZATION_FILE));
        outputStreamWriter.write("digraph{\n");
        Element treeRoot = xmldoc.getRootElement().element(this.getClass().getSimpleName());

        System.out.println("=========draw========");
        int num = 0;
        while (treeRoot.elements().size() > 0) {
            List<Element> childs = treeRoot.elements();
            String nameValue = treeRoot.attribute("value").getValue();
            String name = treeRoot.getName();

            num++;
            for (Iterator iter = treeRoot.elementIterator(); iter.hasNext(); ) {

                Element e = (Element) iter.next();
                outputStreamWriter.write(name + "_" + nameValue + "->" + e.getName() + "_" + e.attribute("value").getValue() + "\n");
                System.out.print(name + "_" + nameValue + "->");
                if (e.elements().size() > 0) {
                    iter = e.elementIterator();
                    name = e.getName();
                    nameValue = e.attribute("value").getValue();
                } else {
                    String value = e.attribute("value").getValue();
                    String nametmp = e.getName();
                    outputStreamWriter.write(nametmp + "_" + value + "->" + e.getData() + "\n");
                    System.out.println(nametmp + "_" + value + "->" + e.getData());
                }
            }
            for (int i = 0; i < num; i++)
                treeRoot.remove(childs.get(i));
        }
        outputStreamWriter.write("}");
        outputStreamWriter.close();

        // call dot to plot
        String commandText = "C:/Program Files (x86)/Graphviz2.38/bin/dot.exe " + Contants.VISUALIZATION_FILE + " -Tpng -o "
                + Contants.VISUALIZTION_PIC;
        System.out.println(commandText);
        Runtime.getRuntime().exec(commandText);
    }


}
