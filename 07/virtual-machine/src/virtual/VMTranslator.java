package virtual;
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

//        ����ļ�
        String outputFile = null;
//        ������Ҫ����Ϊ.asm��.vm�ļ�
        ArrayList<Parser> files2parse = new ArrayList<>();
        ArrayList<CodeWriter> codeWriters = new ArrayList<>();

//        �ж��������Ƿ���ȷ
        if (args.length != 1) {
            printCommandLineErrorAndExit();
        }

//        ����ṩ�������в�������ȷ��������Ч���ļ�����Ŀ¼
//        �������в����½�File����
        final File file = new File(args[0]);
//        ����ļ��Ƿ����
        final boolean exists = file.exists();
//        ����Ƿ�ΪĿ¼
        final boolean isDirectory = file.isDirectory();
//        ����Ƿ�Ϊ������ļ�
        final boolean isFile = file.isFile();

//        �ļ�������
        if (!exists) {
            System.err.println(args[0] + "is not a valid file or path !");
            System.exit(1);

//        ������.vm�ļ�
        } else if (isFile && args[0].endsWith(".vm")) {
//            ��ȡParser���󲢳�ʼ���ļ�������ȡָ�����(·��+�ļ���)
            final Parser parser = getParser(file);
            final String fileName = args[0].substring(0, args[0].indexOf(".vm"));
            parser.setFileName(fileName);

//            ��Parser����洢��ArrayList<Parser>����
            files2parse.add(parser);
            outputFile = fileName + ".asm";

//        һ��Ŀ¼��ɨ�赱�е�����.vm�ļ�
        } else if (isDirectory) {
//            files�洢��ָ�����arg[0]ӳ��·���µ������ļ�
            final File[] files = file.listFiles();
            assert files != null;

//            ���������ļ����ҳ�.vm�ļ������н���(Parser)
            for (final File f : files) {
                if (f.getName().endsWith(".vm")) {
                    final Parser parser = getParser(f);
                    final String fileName = f.getName().substring(0, f.getName().indexOf(".vm"));
                    parser.setFileName(fileName);
                    files2parse.add(parser);
                }
            }

//            ����ṩ��Ŀ¼������.vm�ļ������׳������˳�
            if (files2parse.size() == 0) {
                System.err.println("No .vm files to parse in " + args[0]);
                System.exit(1);
            }
//            ����ļ���·��+���ƣ��˴�ֻ�ܽ�Ŀ¼������.vm�ļ����뵽һ��.asm�ļ�
            outputFile = file.getAbsolutePath() + "/" + file.getName() + ".asm";

        } else {
            printCommandLineErrorAndExit();
        }
//        ʵ����codeWriter���󣬶�����ļ�д���Ӧ��Hack���ָ��
        PrintWriter printWriter = null;
        try {
            assert outputFile != null;
            printWriter = new PrintWriter(outputFile);
        } catch (FileNotFoundException e) {
            System.out.println(e.getMessage());
        }
        final CodeWriter codeWriter = new CodeWriter(printWriter);

//        ����Parser�������飺ÿ��.vm�ļ�
        for (final Parser file2parse : files2parse) {
//            ���õ�ǰ���ڽ������ļ����ļ���
            codeWriter.setFileName(file2parse.getFileName());

//            ��������ɨ�衢�����հ�
            while (file2parse.hasMoreCommand()) {
                file2parse.advance();
                file2parse.skipBlanks();
                if (file2parse.Length() == 0)
                    continue;

//                �����"push" OR "pop"ָ��
                if (file2parse.CommandType() == Parser.Command.C_PUSH
                        || file2parse.CommandType() == Parser.Command.C_POP) {
//                    д��"push" OR "pop"ָ���Ӧ�Ļ��ָ����� (ָ�����ͣ�ָ���ӵĵ�һ���������ڶ�������)
                    codeWriter.writePushAndPop(file2parse.CommandType(), file2parse.arg1(),
                            file2parse.arg2());

//                    ���������ָ��
                } else if (file2parse.CommandType() == Parser.Command.C_ARITHMETIC) {
//                    ����������ָ��
                    codeWriter.writeArithmetic(file2parse.command());
                }
            }
            file2parse.close();
        }
        codeWriter.close();
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
