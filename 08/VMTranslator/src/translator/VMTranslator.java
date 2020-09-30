package translator;
/*
 * Created by Nolva on 2020/9/21.
 */

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * ��ָ����VM�ļ���������VM�ļ���Ŀ¼ת��Ϊ����.asm�ļ�
 * reference: jahnagoldman
 */

public class VMTranslator {
    public static void main(String[] args) {

//        ����ļ���
        String outputFileName = null;
//        ������Ҫ����Ϊ.asm��.vm�ļ�
        ArrayList<Parser> files2parse = new ArrayList<>();

        // �����в����Ĵ�����
        if (args.length != 1) {
            printCommandLineErrorAndExit();
        }

//        ����ṩ�������в�������ȷ��������Ч���ļ�����Ŀ¼
        File file = new File(args[0]);
//        �����в���·����3�����: 1.�����ڣ�2.�ļ���3.Ŀ¼
//        �ļ��Ƿ����
        boolean exists = file.exists();
//        �Ƿ�Ϊ������ļ�
        boolean isFile = file.isFile();
//        �Ƿ�ΪĿ¼
        boolean isDirectory = file.isDirectory();

//        1.������
        if (!exists) {
            System.err.println(args[0] + " is not a valid file or path");
            System.exit(1);

//        2.����.vm�ļ�
        } else if (isFile && args[0].endsWith(".vm")) {
            Parser parser = getParser(file);
            String fileName = args[0].substring(0, args[0].indexOf(".vm"));
            parser.setFileName(fileName);
            files2parse.add(parser);
            outputFileName = fileName + ".asm";

//        3.Ŀ¼��ɨ��Ŀ¼�����е�.vm�ļ�
        } else if (isDirectory) {
//            ��·���µ������ļ�ʵ����ΪFile���󲢷���File����
            File[] files = file.listFiles();
            assert files != null;

//            ��������.vm�ļ�����ʼ��Parser����fileName����(�޺�׺)�������뵽Parser����������
            for (File f : files) {
                if (f.getName().endsWith(".vm")) {
                    Parser parser = getParser(f);
                    String fileName = f.getName().substring(0, f.getName().indexOf(".vm"));
                    parser.setFileName(fileName);
                    files2parse.add(parser);
                }
            }

//            ���Ŀ¼������.vm�ļ������׳������˳�
            if (files2parse.size() == 0) {
                System.err.println("No .vm files to parse in " + args[0]);
                System.exit(1);
            }

            outputFileName = file.getAbsolutePath() + "/" + file.getName() + ".asm"; // output fileName is dir name + .asm

//        4. �������������
        } else {
            printCommandLineErrorAndExit();
        }

//        ʵ����CodeWriter����
        PrintWriter printWriter = null;
        try {
            assert outputFileName != null;
            printWriter = new PrintWriter(outputFileName);
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        CodeWriter codeWriter = new CodeWriter(printWriter);

//        initializedָʾ.asm�ļ��Ƿ���д�������������
        boolean initialized = false;

//        ����Parser(ÿ��.vm�ļ�)��CodeWriterд��
        for (Parser fileToParse : files2parse) {

//            ���ļ���ͷֻ��дһ����������
            if (!initialized) {
                codeWriter.writerInit();
                initialized = true;
            }
            
//            ���õ�ǰ���ڽ������ļ����ļ���
            codeWriter.setFileName(fileToParse.getFileName());

            while (fileToParse.hasMoreCommand()) {
                fileToParse.advance();
                fileToParse.skipBlanks();
                if (fileToParse.Length() == 0) continue;

                Parser.Command command = fileToParse.CommandType();

                switch (command) {
                    case C_POP:
                    case C_PUSH:
                        codeWriter.writePushAndPop(fileToParse.CommandType(), fileToParse.arg1(), fileToParse.arg2());
                        break;
                    case C_ARITHMETIC:
                        codeWriter.writeArithmetic(fileToParse.command());
                        break;
                    case C_LABEL:
                        codeWriter.writeLabel(fileToParse.arg1());
                        break;
                    case C_GOTO:
                        codeWriter.writeGoto(fileToParse.arg1());
                        break;
                    case C_IF:
                        codeWriter.writeIf(fileToParse.arg1());
                        break;
                    case C_FUNCTION:
                        codeWriter.writeFunction(fileToParse.arg1(), fileToParse.arg2());
                        break;
                    case C_RETURN:
                        codeWriter.writeReturn();
                        break;
                    case C_CALL:
                        codeWriter.writeCall(fileToParse.arg1(), fileToParse.arg2());
                        break;
                    default:
                        break;
                }

            }
            fileToParse.close();
        }
        codeWriter.close();
    }

    private static Parser getParser(File file) {
        Parser parser = null;
        try {
            parser = new Parser(new Scanner(new FileReader(file)));
        } catch (IOException e) {
            System.out.println(e.getMessage());
        }
        return parser;
    }

    private static void printCommandLineErrorAndExit() {
        System.err.println("usage: java translator/VMTranslator <fileName.vm>");
        System.err.println("OR");
        System.err.println("java translator/VMTranslator <directory>");
        System.exit(1);
    }
}
