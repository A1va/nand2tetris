package Assembler;
/*
 * Created by Nolva on 2020/9/8.
 */

import java.util.HashMap;

/**
 * ��Hack����������Ƿ�����ɶ�������
 * ����Assembler.java
 */
class Code {

   /**
    * ָ��comp�����Ƿ������������ʽ��ת����
    */
    private static final HashMap<String, String> compRegion = new HashMap<>(){{
        put("0",   "0101010");
        put("1",   "0111111");
        put("-1",  "0111010");
        put("D",   "0001100");
        put("A",   "0110000");
        put("M",   "1110000");
        put("!D",  "0001101");
        put("!A",  "0110011");
        put("!M",  "1110001");
        put("-D",  "0001111");
        put("-A",  "0110011");
        put("-M",  "1110011");
        put("D+1", "0011111");
        put("A+1", "0110111");
        put("M+1", "1110111");
        put("D-1", "0001110");
        put("A-1", "0110010");
        put("M-1", "1110010");
        put("D+A", "0000010");
        put("D+M", "1000010");
        put("D-A", "0010011");
        put("D-M", "1010011");
        put("A-D", "0000111");
        put("M-D", "1000111");
        put("D&A", "0000000");
        put("D&M", "1000000");
        put("D|A", "0010101");
        put("D|M", "1010101");
    }};

   /**
    * �Զ�������ʽ����comp���Ƿ�(7bits)
    * @param symbol
    * @return
    */
   static String comp(String symbol) {
       if (!compRegion.containsKey(symbol)) {
          throw new IllegalArgumentException("Invalid comp mnemonic: " + symbol);
       } else {
          return compRegion.get(symbol);
       }
   }

   /**
    * ���� �Ƿ� ��Ҫ�洢����ǰ�Ĵ�����binary(ָ���Ӧ�Ķ�������ʽ)���(1/0)
    * @param symbol dest�����Ƿ�
    * @param binary dest��Ķ����Ʊ�ʾ
    * @param register ���Ƿ���Ӧ��AMD�Ĵ���
    */
    private static void checkDest(String symbol, StringBuilder binary, String register) {
//      �ж������Ƿ���Ҫ�洢����ǰ�Ĵ���
        if (symbol.contains(register)) {
           binary.append("1");
        } else {
           binary.append("0");
        }
    }

   /**
    * �Զ�������ʽ����dest���Ƿ�(3bits)
    * @param symbol
    * @return
    */
    static String dest(String symbol) {
        StringBuilder binary = new StringBuilder();

        checkDest(symbol, binary, "A");
        checkDest(symbol, binary, "D");
        checkDest(symbol, binary, "M");

        return binary.toString();
    }

    /**
     * �Զ�������ʽ����jump���Ƿ�(3bits)
     * @param symbol
     * @return
     */
    static String jump(String symbol) {
        switch (symbol) {

            case "":
                return "000";
            case "JGT":
                return "001";
            case "JEQ":
                return "010";
            case "JGE":
                return "011";
            case "JLT":
                return "100";
            case "JNE":
                return "101";
            case "JLE":
                return "110";
            default:
                return "111";
        }
    }

    /**
     * ����15λ �Զ�������ʽ��ʾ�� ʮ���ƷǸ�����
     * @param decimal
     * @return
     */
    static String binary(int decimal) {

//        ��������2^15����������ʽ����15λ ���Ϲ淶
        if (decimal > 65535) {
            throw new IllegalArgumentException("Number too big to load into A-register");
        }

        StringBuilder binary = new StringBuilder();

//        ��ʮ����ת��Ϊ�����ƣ����洢��StringBuilder����
        while (decimal > 0){
            binary.append(decimal % 2);
            decimal /= 2;
        }

//        ��0��䣬ֱ����������Ϊ15λ
        while (binary.length() < 15) {
            binary.append("0");
        }

        binary.reverse();

        return binary.toString();
    }

}
