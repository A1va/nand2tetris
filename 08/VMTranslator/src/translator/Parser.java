package translator;
/*
 * Created by Nolva on 2020/9/14.
 */

import java.util.Scanner;

/**
 * ����.vm�ļ�����װ���������ķ��ʲ���
 * ��ȡvmָ�������Ȼ��Ϊ�����������ṩ����ķ������
 * �Ƴ����������пո��ע��
 */
class Parser {

//    �ļ�ɨ��
    private Scanner scanner;
//    ��ǰ����
    private String current;
//    ���ڱ�������.vm�ļ�����
    private String fileName;

//    ����ָ��
    private final String[] ARITHMETIC_CMD = {"add", "sub", "neg", "eq", "gt", "lt", "and", "or", "not"};

    Parser(Scanner scanner) {
        this.scanner = scanner;
    }

//    �Ƿ���ָ��
    boolean hasMoreCommand() {
        return scanner.hasNextLine();
    }

//    ��ǰ
    void advance() {
        current = scanner.nextLine();
    }

    /**
     * vm command type
     */
    enum Command {
        C_ARITHMETIC,  // ������������
        C_PUSH,
        C_POP,
        C_LABEL,
        C_GOTO,
        C_IF,
        C_FUNCTION,
        C_RETURN,
        C_CALL
    }

//    �������еĿհ�
    void skipBlanks() {
        if (current.contains("//")){
            current = current.substring(0, current.indexOf("//"));
        }
//        ���������ո�eg: push constant 7�����, pushconstant7--�޷��ָ�ָ���������Խ��Ĵ���
//        current = current.replace(" ", "");
        current = current.replace("\n", "");
        current = current.replace("\t", "");
    }

//    ��ǰָ���
    int Length() {
        return current.length();
    }

    /**
     * ���ֳ�9��ָ�
     * chap7: ����"push", "pop", arithmetic cmd
     * @return ָ������commandType
     */
    Command CommandType() {
        if (current.startsWith("push")) {
            return Command.C_PUSH;
        } else if (current.startsWith("pop")) {
            return Command.C_POP;
        } else if (isArithmeticCmd()){
            return Command.C_ARITHMETIC;
        } else if (current.startsWith("label")) {
            return Command.C_LABEL;
        } else if (current.startsWith("goto")) {
            return Command.C_GOTO;
        } else if (current.startsWith("if-goto")) {
            return Command.C_IF;
        } else if (current.startsWith("function")) {
            return Command.C_FUNCTION;
        } else if (current.startsWith("return")) {
            return Command.C_RETURN;
        } else if (current.startsWith("call")){
            return Command.C_CALL;
        }else {
            return null;
        }
    }

    /**
     * ר������ָ��C_ARITHMETIC����
     * @return ������ָ��
     */
    String command() {
        return current.split("\\s+")[0];
    }

    /**
     * forbid C_RETURN
     * @return ���ص�ǰָ���һ������segment
     */
    String arg1() {
        return current.split("\\s+")[1];
    }

    /**
     * only for C_PUSH, C_POP, C_FUNCTION, C_CALL
     * @return ���ص�ǰָ��ĵڶ�������, segment����
     */
    int arg2() {
        if (CommandType() == Command.C_PUSH ||
                CommandType() == Command.C_POP ||
                CommandType() == Command.C_FUNCTION ||
                CommandType() == Command.C_CALL){
            return Integer.parseInt(current.split("\\s+")[2]);
        } else return -1;
    }

    void setFileName(String fileName) {
        this.fileName = fileName;
    }

    String getFileName() {
        return fileName;
    }

    void close() {
        scanner.close();
    }

    /**
     * ��������ָ��ARITHMETIC_CMD�������ǰָ��Ŀ�ʼ��������һ���ͷ���true
     * @return �Ƿ�Ϊ����ָ��
     */
    private boolean isArithmeticCmd() {
        for (String cmd : ARITHMETIC_CMD) {
            if (current.startsWith(cmd)) return true;
        }
        return false;
    }
}
