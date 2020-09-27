package translator;
/*
 * Created by Nolva on 2020/9/21.
 */

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Scanner;

public class VMTranslator {
    public static void main(String[] args) {

//        ������Ҫ����Ϊ.asm��.vm�ļ���Ϊ�ս��д�����
        ArrayList<Parser> files2parse = new ArrayList<>();

//        �ж��������Ƿ���ȷ
        if (args.length != 1) {
            printCommandLineErrorAndExit();
        }

//        ����ṩ�������в�������ȷ��������Ч���ļ�����Ŀ¼
//        �������в����½�File����
        File file = new File(args[0]);
//        ����ļ��Ƿ����
        boolean exists = file.exists();
//        ����Ƿ�ΪĿ¼
        boolean isDirectory = file.isDirectory();
//        ����Ƿ�Ϊ������ļ�
        boolean isFile = file.isFile();

//        �ļ�������
        if (!exists) {
            System.err.println(args[0] + "is not a valid file or path !");
            System.exit(1);

//        ������.vm�ļ�
        } else if (isFile && args[0].endsWith(".vm")) {
//            ��ȡParser���󲢳�ʼ���ļ�������ȡָ�����(·��+�ļ���)
            Parser parser = getParser(file);
            String fileName = args[0].substring(0, args[0].indexOf(".vm"));
            parser.setFileName(fileName);

            String outputFile = fileName + ".asm";
//            ����.vm�ļ�ֱ�ӷ����.asm�ļ�
            writeVM2ASM(outputFile, parser);

//        һ��Ŀ¼��ɨ�赱�е�����.vm�ļ�
        } else if (isDirectory) {
//            ��ָ�����arg[0]ӳ��·���µ������ļ�ת����File���󣬲���File[]����洢
            File[] files = file.listFiles();
            assert files != null;
//            ���������ļ����ҳ�.vm�ļ������н���(Parser)
            for (File f : files) {
                if (f.getName().endsWith(".vm")) {
                    Parser parser = getParser(f);
                    String fileName = f.getName().substring(0, f.getName().indexOf(".vm"));
                    parser.setFileName(fileName);
                    String outputFile = file.getAbsolutePath() + "/" + fileName + ".asm";
                    writeVM2ASM(outputFile, parser);
                    files2parse.add(parser);
                }
            }

//            ����ṩ��Ŀ¼������.vm�ļ������׳������˳�
            if (files2parse.size() == 0) {
                System.err.println("No .vm files to parse in " + args[0]);
                System.exit(1);
            }

        } else {
            printCommandLineErrorAndExit();
        }
    }

    /**
     * ��װд���ӦHack�����뵽����ļ��Ĳ��������ڶ�����Parser�͵�һ��codeWriter�������
     * @param output ����.vm�ļ�
     */
    private static void writeVM2ASM(String output, Parser parser) {
        CodeWriter codeWriter = null;
        try (PrintWriter writer = new PrintWriter(output)) {
            codeWriter = new CodeWriter(writer);
            codeWriter.setFileName(parser.getFileName());
            while (parser.hasMoreCommand()) {
                parser.advance();
                parser.skipBlanks();
                if (parser.Length() == 0) continue;

                if (parser.CommandType() == Parser.Command.C_PUSH || parser.CommandType() == Parser.Command.C_POP) {
                    codeWriter.writePushAndPop(parser.CommandType(), parser.arg1(), parser.arg2());
                } else if (parser.CommandType() == Parser.Command.C_ARITHMETIC) {
                    codeWriter.writeArithmetic(parser.command());
                }
                writer.close();
            }
        } catch (FileNotFoundException e) {
            System.err.println(e.getMessage());
        } finally {
            parser.close();
            assert codeWriter != null;
            codeWriter.close();
        }

    }

    /**
     * ����File�ļ����󴴽�Parser����
     * @param file �����ļ���������File����
     * @return Parser����
     */
    private static Parser getParser(File file) {
        Parser parser = null;
        try {
            parser = new Parser(new Scanner(new FileReader(file)));
        } catch (FileNotFoundException e) {
            System.out.println(e.getMessage());
        }
        return parser;
    }

    /**
     * ��������е��÷����˳�
     */
    private static void printCommandLineErrorAndExit() {
        System.err.println("usage: java VMTranslator/VMTranslator <filename.vm>");
        System.err.println("OR");
        System.err.println("java VMTranslator/VMTranslator <directory>");
        System.exit(1);
    }
}
