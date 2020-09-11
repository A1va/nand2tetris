package Assembler;
/*
 * Created by Nolva on 2020/9/6.
 */

import java.util.Scanner;

/**
 * Parser ��װ���������ķ��ʲ�����
 *    ʵ�ֹ��ܣ�
 *      1.��ȡ����������������н���;
 *      (*)2.�ṩ��������ʻ������ɷ�(��ͷ���)���ķ���;
 *      3.ȥ�����еĿո��ע��.
 *
 * ����Assembler.java
 */
class Parser {

//    �洢�����ļ�/��������Ϊ�﷨������׼��
    private Scanner scanner;
//    �洢��ǰ������
    private String current;

    /**
     * ���캯������ �����ļ�/������ �洢��Scanner����
     * @param scanner
     */
    Parser(Scanner scanner) {
        this.scanner = scanner;
    }

    /**
     * �ж����뵱���Ƿ�����һ��
     * @return  ��:true ; û��:false
     */
    boolean hasMoreCommands() {
        return scanner.hasNextLine();
    }

    /**
     * �������ж�ȡ��һ��(��)������䵱��"��ǰ����"��
     * ����hasNextCommands()Ϊtrueʱ�����ܵ��ô˷�����
     *
     * �� next(): ��ȡ��һ����Ч�ַ��������ո�ͽ�������Ҫ������ȡ��Ҫ����ѭ��
     * nextLine(): ��ȡһ���е��ַ������������з��ͽ�����(�ɵõ��հ�)
     */
    void advance() {
        this.current = scanner.nextLine();
    }

    /**
     * �������е�ע�ͺͿհ�
     */
    void skipSpacesAndComments() {
//        �����ע��
        if (current.contains("//")) {
//            ���� "//"ע�ͺ�����������ݣ�ɾ��ע��
            current = current.substring(0, current.indexOf("//"));
        }
//        ɾ���ո�
        current = current.replace(" ", "");
//        ɾ��Tab
        current = current.replace("\t", "");
    }

    /**
     * ���ص�ǰָ��ĳ���
     * @return
     */
    int currentLength() {
        return current.length();
    }

    /**
     * ָ������
     */
    public enum commandType {
        A_COMMAND,  // ��@Xxx�е�Xxx�Ƿ��Ż�ʮ��������ʱ
        C_COMMAND,  // ���� dest=comp;jump
        L_COMMAND   // (α����), ��(Xxx)�е�Xxx�Ƿ���ʱ
    }

    /**
     * ���ݵ�ǰָ��Ŀ�ʼ�ַ����жϵ�ǰָ�������
     * @return commandType: A/C/L_COMMAND
     */
    commandType CommandType() {

//        ��@Xxx�е�Xxx�Ƿ��Ż�ʮ��������ʱ, ΪA-ָ��.
        if (current.startsWith("@")) {
            return commandType.A_COMMAND;

//        ��(Xxx)�е�Xxx�Ƿ���ʱ, Ϊαָ��.   eg:(END)
        } else if (current.startsWith("(")) {
            return commandType.L_COMMAND;

//        dest=comp;jump, C-ָ��.
        } else {
            return commandType.C_COMMAND;
        }
    }

    /**
     * ��ָ�����"="��˵��ָ���dest���comp��   eg:dest=comp(;jump)
     * ��"="�ָ�Ϊ�����ַ�������ȡ[0](��һ��)
     * @return C-ָ���dest�� �� null
     */
    String dest() {
        if (current.contains("=")) {
            return current.split("=")[0];
        } else {
            return "";
        }
    }

    /**
     * ��C-ָ����comp��ض�����
     *     eg:comp;jump, dest=comp, dest=comp;jump
     *
     * ��ָ�����"="����"="��";"�ָ�Ϊ��(��)���ַ�������ȡ[1](�ڶ���)
     * ��ָ�����"="����";"�ָ�Ϊ�����ַ�������ȡ[0](��һ��)
     * @return C-ָ���comp��
     */
    String comp() {
        if (current.contains("=")) {
            return current.split("[=;]")[1];
        } else {
            return current.split(";")[0];
        }
    }

    /**
     * ��C-ָ�����";"��˵��ָ���jump��   eg:dest=comp;jump, comp;jump
     * ��";"�ָ�Ϊ�����ַ�������ȡ[1](�ڶ���)
     * @return C-ָ���jump�� �� null
     */
    String jump(){
        if (current.contains(";")){
            return current.split(";")[1];
        } else {
            return "";
        }
    }

    /**
     * ����"@Value", "@Variable", "(Symbol)"�﷨����ַ���
     * @return ����Value��Variable��Symbol �� null
     */
    String symbol() {
        if (current.startsWith("@")) {
//            Value | Variable
            return current.substring(1);
        } else if (current.startsWith("(")) {
//            Symbol
            return current.substring(1, current.indexOf(")"));
        } else {
            return null;
        }
    }

    /**
     * �ͷ���Դ
     */
    void close() {
        if (scanner != null)
            scanner.close();
    }

}
