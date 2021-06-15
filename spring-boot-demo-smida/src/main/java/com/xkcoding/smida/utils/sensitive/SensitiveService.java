package com.xkcoding.smida.utils.sensitive;

import com.alibaba.fastjson.JSON;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * dfa实现敏感词过滤
 *
 * @author Created by YangYifan on 2021/6/15.
 */
@Service
public class SensitiveService implements InitializingBean {

    /**
     * 敏感词数据
     */
    private static final SensitiveData sensitiveData = new SensitiveData(false);

    /**
     * 初始化敏感词库
     *
     * @param words
     */
    private void initSensitiveWords(List<String> words) {
        for (String word : words) {
            char[] cArr = word.toCharArray();
            SensitiveData curData = sensitiveData;
            for (int i = 0; i < cArr.length; i++) {
                boolean isEnd = i == cArr.length - 1;
                char c = cArr[i];
                SensitiveData subData = curData.getSub(c);
                if (null == subData) {
                    curData.putSub(new SensitiveData(c, isEnd));
                } else {
                    subData.setIsEnd(isEnd);
                }
                curData = curData.getSub(c);
            }
        }
    }

    /**
     * 返回是否包含敏感词
     * @param txt
     * @return
     */
    public boolean hasSensitiveWords(String txt) {
        char[] cArr = txt.toCharArray();
        for (int i = 0; i < cArr.length; i++) {
            if (check(cArr, i) > 0) {
                return true;
            }
        }
        return false;
    }

    /**
     * 返回包含的所有敏感词
     * @param txt
     * @return
     */
    public List<String> findAllSensitiveWords(String txt) {
        List<String> res = new ArrayList<>();
        char[] cArr = txt.toCharArray();
        for (int i = 0; i < cArr.length; ) {
            int len;
            if ((len = check(cArr, i)) > 0) {
                res.add(txt.substring(i, i + len));
                i += len;
            } else {
                i++;
            }
        }
        return res;
    }

    /**
     * 检查敏感词
     *
     * @param cArr 传入的文本字符数组
     * @param ind  开始从数组的ind下标检测
     * @return 返回检测到的敏感词的长度
     */
    private Integer check(char[] cArr, int ind) {
        int count = 0;
        SensitiveData cur = sensitiveData;
        for (int i = ind; i < cArr.length; i++) {
            SensitiveData sub = cur.getSub(cArr[i]);
            if (null == sub) {
                return 0;
            }
            count++;
            cur = sub;
            if (sub.isEnd()) {
                return count;
            }
        }
        return count;
    }


    /**
     * 敏感词数据类
     * 共-产-党
     * |
     * -主-义
     * c:对应节点的字符
     * subMap 节点后跟随的后续数据
     * isEnd 为true时说明已到敏感词最后一个字
     */
    private static class SensitiveData {
        private char c;
        private Map<Character, SensitiveData> subMap;
        private boolean isEnd;

        SensitiveData(char c, boolean isEnd) {
            this.c = c;
            subMap = new HashMap<>(1);
            this.isEnd = isEnd;
        }

        SensitiveData(boolean isEnd) {
            subMap = new HashMap<>(10);
            this.isEnd = isEnd;
        }

        public SensitiveData getSub(Character ch) {
            return subMap.get(ch);
        }

        public char getChar() {
            return c;
        }

        public boolean isEnd() {
            return isEnd;
        }

        private void setIsEnd(boolean f) {
            if (isEnd == false) {
                this.isEnd = f;
            }
        }

        public void putSub(SensitiveData sensitiveData) {
            SensitiveData s;
            if (null != (s = subMap.get(sensitiveData.getChar())) && s.isEnd == true) {
                sensitiveData.setIsEnd(true);
            }
            subMap.put(sensitiveData.getChar(), sensitiveData);
        }
    }


    @Override
    public void afterPropertiesSet() throws Exception {
        List<String> arr = Arrays.asList("基金", "股票", "比特币", "共产党", "共产主义");
        initSensitiveWords(arr);
        System.out.println(JSON.toJSONString(sensitiveData));
    }
}
