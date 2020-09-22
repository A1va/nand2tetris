package virtual;
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

    private Scanner scanner;
    private String current;
//    ���ڱ�������.vm�ļ�
    private String fileName;

    private final String[] ARITHMETIC_CMD = {"add", "sub", "neg", "eq", "gt", "lt", "and", "or", "not"};

    Parser(Scanner scanner) {
        this.scanner = scanner;
    }

    boolean hasMoreCommand() {
        return scanner.hasNextLine();
    }

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
//        C_LABEL,
//        C_GOTO,
//        C_IF,
        C_FUNCTION,
//        C_RETURN,
        C_CALL
    }

    void skipBlanks() {
        if (current.contains("//")){
            current = current.substring(0, current.indexOf("//"));
        }
        current = current.replace(" ", "");
        current = current.replace("\n", "");
        current = current.replace("\t", "");
    }

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
        } else {
            return null;
        }
    }

    /**
     * @return ������ָ��
     */
    String command() {
        return current.split("\\s+")[0];
    }

    /**
     * forbid C_RETURN
     * @return ���ص�ǰָ���һ������
     */
    String arg1() {
//        if (CommandType() == commandType.C_ARITHMETIC){
//            return current;
//        } else {
            return current.split("\\s+")[1];
//        }
    }

    /**
     * only for C_PUSH, C_POP, C_FUNCTION, C_CALL
     * @return ���ص�ǰָ��ĵڶ�������segment
     */
    int arg2() {
//        if (CommandType() == commandType.C_PUSH ||
//                CommandType() == commandType.C_POP ||
//                CommandType() == commandType.C_FUNCTION ||
//                CommandType() == commandType.C_CALL){
            return Integer.parseInt(current.split("\\s+")[2]);
//        } else return -1;
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
     * ��������ָ��ARITHMETIC_CMD�������ǰָ���������һ���ͷ���true
     * @return �Ƿ�Ϊ����ָ��
     */
    private boolean isArithmeticCmd() {
        for (String cmd : ARITHMETIC_CMD) {
            if (current.equals(cmd)) return true;
        }
        return false;
    }
}
