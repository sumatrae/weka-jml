package cn.uestc.clustering;

import java.io.*;
import java.util.*;

import cn.uestc.util.Utility;

public class DBSCAN {
    private static List<List<Point>> resultList = new ArrayList<List<Point>>();

    private static void display() {
        int index = 1;
//		for (Iterator<List<Point>> it = resultList.iterator(); it.hasNext();)
        for (List<Point> lst : resultList) {
            if (lst.isEmpty()) {
                continue;
            }
            System.out.println("-----" + index + "cluster-----");

            for (Point p : lst) {
                System.out.println(p.print());
            }
            index++;
        }
    }

    private static void applyDbscan() {
        try {
            List<Point> pointsList = Utility.getPointsList();

            for (Point p : pointsList) {
                if (!p.isClassed()) {
                    List<Point> tmpLst = new ArrayList<Point>();
                    int e = 5;
                    int minp = 3;
                    if ((tmpLst = Utility.isKeyPoint(pointsList, p, e, minp)) != null) {
                        Utility.setListClassed(tmpLst);
                        resultList.add(tmpLst);
                    }
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    private static void getResult() {

        applyDbscan();

        int length = resultList.size();
        for (int i = 0; i < length; ++i) {
            for (int j = i + 1; j < length; ++j) {
                if (Utility.mergeList(resultList.get(i), resultList.get(j))) {
                    resultList.get(j).clear();
                }
            }
        }
    }

    public static void main(String[] args) {
        getResult();
        display();
    }
}