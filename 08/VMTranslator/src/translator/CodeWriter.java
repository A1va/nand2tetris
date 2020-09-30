package translator;

import java.io.PrintWriter;

/**
 * ��VMָ����Hack�����룬��д����Ӧ��.asm����ļ�
 */
class CodeWriter {

    private PrintWriter writer;
//    ��ǰ�������.vm�ļ�
    private String fileName;
//    αָ��: ���ַ�֧��ǩ������
    private int branchIndex;
//    αָ��: ���ַ��ص�ַ�ı�ǩ����
    private int returnIndex;

    CodeWriter(PrintWriter writer) {
        this.writer = writer;
        branchIndex = 0;
        returnIndex = 0;
    }

//    ���÷�������ĵ�.vm�ļ����ļ���(��ȡ���ļ���)
    void setFileName(String fileName) {
        this.fileName = fileName.substring(fileName.lastIndexOf("/") + 1);
    }

    void writerInit() {
//        SP=256
        writer.println("@256");
        writer.println("D=A");
        writer.println("@SP");
        writer.println("M=D");
//        call Sys.init
        writeCall("Sys.init", 0);
    }

    /**
     * ��дִ��Label����Ļ��ָ��
     * @param label ��ǩ
     */
    void writeLabel(String label) {
        writer.println("(" + label + ")");
    }

    /**
     * ��дgoto����Ļ��ָ��
     * @param label goto label�еı�ǩ
     */
    void writeGoto(String label) {
        writer.println("@" + label);
        writer.println("0;JMP");
    }

    /**
     * ��дif-goto����Ļ��ָ��
     * @param label @label
     */
    void writeIf(String label) {
        decAndPopTopStack();
        writer.println("@" + label);
        writer.println("D;JNE");
    }

    /**
     * call f n
     * @param functionName ������
     * @param numArgs ������������
     */
    void writeCall(String functionName, int numArgs) {
        String returnAdd = uniqueReturnAdd();

//        push return-address
        writer.println("@" + returnAdd);
        writer.println("D=A");
        PushD2StackAndInc();

//        push LCL, ARG, THIS, THAT
        writeMappedPush("@LCL");
        writeMappedPush("@ARG");
        writeMappedPush("@THIS");
        writeMappedPush("@THAT");

//        reset ARG: ARG = (SP-n-5)
        writer.println("@SP");
        writer.println("D=M");
        writer.println("@" + numArgs);
        writer.println("D=D-A");
        writer.println("@5");
        writer.println("D=D-A");
        writer.println("@ARG");
        writer.println("M=D");

//        reset LCL: LCL = SP
        writer.println("@SP");
        writer.println("D=M");
        writer.println("@LCL");
        writer.println("M=D");
//        goto F
        writeGoto(functionName);

//        Ϊ���ص�ַ����һ����ǩ
        writer.println("(" + returnAdd + ")");
    }

    void writeReturn() {
//        FRAME = LCL
        writer.println("@LCL");
        writer.println("D=M");
        writer.println("@FRAME");
        writer.println("M=D");
        writer.println("@5");
        writer.println("A=D-A");
        writer.println("D=M");
        writer.println("@RET");
        writer.println("M=D");
        writePushAndPop(Parser.Command.C_POP, "argument", 0);
//        SP = ARG + 1
        writer.println("@ARG");
        writer.println("D=M");
        writer.println("@SP");
        writer.println("M=D+1");
//        THAT = *(FRAME - 1)  �ָ�caller��THAT��ָ��
        restoreCallerPointer("@THAT");
//        THIS = *(FRAME - 2) �ָ�caller��THIS��ָ��
        restoreCallerPointer("@THIS");
//        ARG = *(FRAME - 3)
        restoreCallerPointer("@ARG");
//        LCL = *(FRAME - 4)
        restoreCallerPointer("@LCL");
        writer.println("@RET");
        writer.println("A=M");
        writer.println("0;JMP");
    }

    /**
     * function f k
     * @param functionName ������f
     * @param numLocals �ֲ������ĸ���k
     */
    void writeFunction(String functionName, int numLocals) {
//        declare label
        writer.println("(" + functionName + ")");
//        set aside locations for local vars
        for (int i = 0; i < numLocals; i++) {
            writePushAndPop(Parser.Command.C_PUSH, "constant", 0);
        }
    }

    private String uniqueReturnAdd() {
        return "return-address" + "_" + (returnIndex++);
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
     * segment(local��, argument��, this/that��)��RAM�д洢���Ƕ�Ӧ�εĻ���ַbase
     * (temp��, pointer��, static��)�����ε�ֱֵ��ӳ�䵽RAM
     * @param command ָ������(Push/Pop)
     * @param segment ָ���ĵ�һ��������segment(constant��, local��, argument��...)
     * @param index ��Ӧsegment�ε�����
     */
    void writePushAndPop(Parser.Command command, String segment, int index) {

        switch (segment) {
            case "constant":
                if (command == Parser.Command.C_PUSH) {
//                    @decimal
                    writer.println("@" + index);
//                    �洢��D�Ĵ���
                    writer.println("D=A");
//                    (PUSH)��D�Ĵ�����ֵ�洢��SP��ָ����ڴ浥Ԫ��SP++
                    PushD2StackAndInc();
                }
                break;
            case "local":
                if (command == Parser.Command.C_PUSH) {
                    writeBasePush("@LCL", index);
                } else if (command == Parser.Command.C_POP) {
                    writeBasePop("@LCL", index);
                }
                break;
            case "argument":
                if (command == Parser.Command.C_PUSH) {
                    writeBasePush("@ARG", index);
                } else if (command == Parser.Command.C_POP) {
                    writeBasePop("@ARG", index);
                }
                break;
            case "this":
                if (command == Parser.Command.C_PUSH) {
                    writeBasePush("@THIS", index);
                } else if (command == Parser.Command.C_POP) {
                    writeBasePop("@THIS", index);
                }
                break;
            case "that":
                if (command == Parser.Command.C_PUSH) {
                    writeBasePush("@THAT", index);
                } else if (command == Parser.Command.C_POP) {
                    writeBasePop("@THAT", index);
                }
                break;
            case "temp":
                if (command == Parser.Command.C_PUSH) {
                    int address = 5 + index;
                    String location = "@" + address;
                    writeMappedPush(location);
                } else if (command == Parser.Command.C_POP) {
                    int address = 5 + index;
                    String location = "@" + address;
                    writeMappedPop(location);
                }
                break;
            case "pointer":
                if (command == Parser.Command.C_PUSH) {
                    if (index == 0) {
                        writeMappedPush("@THIS");
                    } else if (index == 1) {
                        writeMappedPush("@THAT");
                    }
                } else if (command == Parser.Command.C_POP) {
                    if (index == 0) {
                        writeMappedPop("@THIS");
                    } else if (index == 1) {
                        writeMappedPop("@THAT");
                    }
                }
                break;
            case "static":
                String location = "@" + fileName + "_" + index;
                if (command == Parser.Command.C_PUSH) {
                    writeMappedPush(location);
                } else if (command == Parser.Command.C_POP) {
                    writeMappedPop(location);
                }
                break;
        }
    }

    /**
     * temp: stored in RAM locations 5 to 12 (8��)
     * addr = (5+i), SP--, *addr = *SP
     * @param location ֱ��ӳ����RAM�Ķε�ַ
     */
    private void writeMappedPop(String location) {
        decAndPopTopStack();
        writer.println(location);
        writer.println("M=D");
    }

    /**
     * temp: stored in RAM locations 5 to 12 (8��)
     * ѹ���ջ������ֱ��ӳ�䵽RAM
     * ֱ�ӽ�ֵ�洢��D�Ĵ�������PushD2StackAndInc
     * addr = (5+i), *SP = *addr, SP++
     * @param location @(5+index)
     */
    private void writeMappedPush(String location) {
        writer.println(location);
//        D=M, ӳ���ַM�洢��D�Ĵ���
        writer.println("D=M");
        PushD2StackAndInc();
    }

    /**
     * д���ɻ���ַ������(base+i)������Popָ��
     * addr = segmentPointer + i, SP--, *addr = *SP
     * @param segmentPointer segment�ε�ָ��
     * @param index segment����
     */
    private void writeBasePop(String segmentPointer, int index) {
//        addr = segmentPointer + i����ȡaddr���洢��R13
        writer.println(segmentPointer);
        writer.println("D=M");
        writer.println("@" + index);
        writer.println("D=D+A");
        writer.println("@R13");
        writer.println("M=D");
//        SP--, *addr = *SP
        decAndPopTopStack();
        writer.println("@R13");
        writer.println("A=M");
        writer.println("M=D");
    }

    /**
     * ѹ���ջ�Բ���ָ������λ���ַ�ķ���
     * д���ɻ���ַ������(base+i)������Pushָ��
     * addr = segmentPointer + i, *SP = *addr, SP++
     * @param segmentPointer segment�ε�ָ��
     * @param index segment����
     */
    private void writeBasePush(String segmentPointer, int index) {
//        ��(segmentPointer+i)��ָ����ڴ浥Ԫ��ֵ(*addr)�洢��D�Ĵ���
        writer.println(segmentPointer);
        writer.println("D=M");
        writer.println("@" + index);
        writer.println("A=D+A");
        writer.println("D=M");

        PushD2StackAndInc();
    }

    /**
     * @param operator write ��֧
     */
    private void writeLogical(String operator) {
        decAndPopTopStack();
        writer.println("A=A-1");
        writer.println("D=M-D");
        writer.println("@" + fileName + "_TRUE" + branchIndex);
        writer.println("D;" + operator);
        writer.println("@SP");
        writer.println("A=M-1");
        writer.println("M=0");
        writer.println("@" + fileName + "_CONTINUE" + branchIndex);
        writer.println("0;JMP");
        writer.println("(" + fileName + "_TRUE" + branchIndex + ")");
        writer.println("@SP");
        writer.println("A=M-1");
        writer.println("M=-1");
        writer.println("(" + fileName + "_CONTINUE" + branchIndex + ")");
        branchIndex++;
    }

    /**
     * (PUSH)��D�Ĵ�����ֵ�洢��SP��ָ����ڴ浥Ԫ��SP++
     * �ȸ�ֵ��������
     */
    private void PushD2StackAndInc() {
        writePointerVal();
        writeInc();
    }

    /**
     * һԪ���߼�ָ�M�Ĵ����洢��(SP-1)��ָ����ڴ浥Ԫ��ֵ
     * SP-1: A=M-1����δ������M�Ĵ���
     */
    private void writeUnaryArithmetic() {
        writer.println("@SP");
        writer.println("A=M-1");
    }

    /**
     * ��Ԫ���߼�ָ�D�Ĵ����洢��(SP--)��ָ����ڴ浥Ԫ��ֵ��M�Ĵ����洢��(SP-1)��ָ����ڴ浥Ԫ��ֵ
     * ����D��ȡ��SP--ָ���ֵ��M��ȡ��SP-1ָ���ֵ���ȴ���һ��������ָ��
     * SP--: M=M-1��SP�Լ�
     */
    private void writeBinaryArithmetic() {
        decAndPopTopStack();
//        writeUnaryArithmetic();
        writer.println("A=A-1");
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
    private void decAndPopTopStack() {
        writer.println("@SP");
        writer.println("AM=M-1");
        writer.println("D=M");
    }

    private void restoreCallerPointer(String dest) {
        writer.println("@FRAME");
        writer.println("AM=M-1");
//        writer.println("D=M");
//        writer.println("A=D-" + index);
        writer.println("D=M");
        writer.println(dest);
        writer.println("M=D");
    }

    /**
     * ��SP��ָ����ڴ浥Ԫ��ֵ(D�Ĵ�����ֵ)
     */
    private void writePointerVal() {
        writer.println("@SP");
        writer.println("A=M");
        writer.println("M=D");
    }

    void close() {
        writer.close();
    }
}
