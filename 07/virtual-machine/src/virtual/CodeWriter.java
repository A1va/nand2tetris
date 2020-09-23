package virtual;

import java.io.PrintWriter;

/**
 * ��VMָ����Hack�����룬��д����Ӧ��.asm����ļ�
 */
class CodeWriter {

    private PrintWriter writer;
//    ��ǰ�������.vm�ļ�
    private String fileName;
//    �߼���������������
    private int logicalIndex;

//    Ϊд������ļ���׼��
    CodeWriter(PrintWriter writer) {
        this.writer = writer;
        logicalIndex = 0;
    }

//    ��ʼ�����µ�.vm�ļ�
    void setFileName(String fileName) {
        this.fileName = fileName.substring(fileName.lastIndexOf("/") + 1);
    }

    /**
     * ��������������������Ӧ�Ļ��д������ļ�
     * @param command ����ָ��
     */
    void writeArithmetic(String command) {
        switch (command){
            case "add":
                writeBinaryArithmetic();
                writer.println("M=M+D");
                break;
            case "sub":
                writeBinaryArithmetic();
                writer.println("M=M-D");
                break;
            case "neg":
                writeUnaryArithmetic();
                writer.println("M=-M");
                break;
            case "eq":
                writeLogical("JEQ");
                break;
            case "gt":
                writeLogical("JGT");
                break;
            case "lt":
                writeLogical("JLT");
                break;
            case "and":
                writeBinaryArithmetic();
                writer.println("M=M&D");
                break;
            case "or":
                writeBinaryArithmetic();
                writer.println("M=M|D");
                break;
            case "not":
                writeUnaryArithmetic();
                writer.println("M=!M");
                break;
        }
    }

    /**
     * д��(Push/Pop)ָ���Ӧ�Ļ��ָ��
     * @param command ָ������(Push/Pop)
     * @param segment ָ���ĵ�һ��������segment(constant��, local��, argument��...)
     * @param index ��Ӧsegment�ε�����
     */
    void writePushAndPop(Parser.Command command, String segment, int index) {

        switch (segment) {
            case "constant":
                if (command == Parser.Command.C_PUSH) {
                    writer.println("@" + index);
                    writer.println("D=A");
                    getPointerVal();
                    writeInc();
                }
                break;
            case "local":
//                TODO complete local segment and other commands
                break;
        }
    }

    /**
     * @param operator write ��֧
     */
    private void writeLogical(String operator) {
        decAndGetTopStack();
        writer.println("A=A-1");
        writer.println("D=M-D");
        writer.println("@" + fileName + "_TRUE" + logicalIndex);
        writer.println("D;" + operator);
        writer.println("@SP");
        writer.println("A=M-1");
        writer.println("M=0");
        writer.println("@" + fileName + "_CONTINUE" + logicalIndex);
        writer.println("0;JMP");
        writer.println("(" + fileName + "_TRUE" + logicalIndex + ")");
        writer.println("@SP");
        writer.println("A=M-1");
        writer.println("M=-1");
        writer.println("(" + fileName + "_CONTINUE" + logicalIndex + ")");
        logicalIndex++;
    }

    /**
     * һԪ���߼�ָ��
     */
    private void writeUnaryArithmetic() {
        writer.println("@SP");
        writer.println("A=M-1");
    }

    /**
     * ��Ԫ���߼�ָ��
     */
    private void writeBinaryArithmetic() {
        decAndGetTopStack();
        writeUnaryArithmetic();
    }

    /**
     * SP����
     */
    private void writeInc() {
        writer.println("@SP");
        writer.println("M=M+1");
    }

    /**
     * SP�Լ�����ȡջ��Ԫ��
     */
    private void decAndGetTopStack() {
        writer.println("@SP");
        writer.println("AM=M-1");
        writer.println("D=M");
    }

    /**
     * ��ȡSPָ���ֵ
     */
    private void getPointerVal() {
        writer.println("@SP");
        writer.println("A=M");
        writer.println("M=D");
    }

    void close() {
        writer.close();
    }
}
