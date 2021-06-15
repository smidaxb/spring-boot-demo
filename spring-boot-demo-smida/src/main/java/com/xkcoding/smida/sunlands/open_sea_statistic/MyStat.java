package com.xkcoding.smida.sunlands.open_sea_statistic;

import org.springframework.util.StringUtils;

import java.io.*;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author Created by YangYifan on 2021/5/31.
 */
public class MyStat {
    public static void main(String[] args) throws Exception {
        File file1 = new File("D:\\1111.txt");
        LinkedHashMap<String, Integer> map1 = new LinkedHashMap<>();
        long nd = 1000 * 24 * 60 * 60;
        try (BufferedReader reader = new BufferedReader(new FileReader(file1))) {
            String s1 = "";
//            SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd HH:mm");
            while (!StringUtils.isEmpty(s1 = reader.readLine())) {
                String[] array = s1.split("\t");
//                Date t1 = sdf.parse(array[0]);
//                Date t2 = sdf.parse(array[1]);
//                int day = (int) ((t2.getTime() -t1.getTime())/nd);
//                if(day<0){
//                    System.out.println("111");
//                }
//                if(day<=90){
//                    if(map1.get(day)!=null&&map1.get(day) != 0){
//                        map1.put(day,map1.get(day)+1 );
//                    }else {
//                        map1.put(day,1 );
//                    }
//                }
                String key;
                Integer count;
                if (StringUtils.isEmpty(array[1].trim()) || (count = Integer.valueOf(array[1].trim())) == 0) {
                    key = "0条";
                } else {
                    Integer i = count / 50;
                    if (i < 10) {
                        key = i * 50 + "-" + (i + 1) * 50 + "条";
                    } else {
                        key = "大于500条";
                    }
                }
                if (map1.get(key) != null && map1.get(key) != 0) {
                    map1.put(key, map1.get(key) + 1);
                } else {
                    map1.put(key, 1);
                }
            }
        }
        LinkedHashMap<String, String> ans = new LinkedHashMap<>();
        for (Map.Entry<String, Integer> entry : map1.entrySet()) {
            String name = entry.getKey();
            Integer value = entry.getValue();
            ans.put(name, "" + value);

        }

        File file3 = new File("D:\\3.txt");
        file3.delete();
        file3.createNewFile();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file3))) {
            for (Map.Entry<String, String> entry : ans.entrySet()) {
                writer.write(entry.getKey());
                writer.newLine();
            }
            for (Map.Entry<String, String> entry : ans.entrySet()) {
                writer.write(entry.getValue());
                writer.newLine();
            }
        }
    }
}
