package Assembler;
/*
 * Created by Nolva on 2020/9/8.
 */

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.Scanner;

/**
 * Compilation: javac Assembler/*.java
 * Execution: java packageName/className path/filename.asm
 * Dependencies: SymbolTable.java, Code.java, Parser.java
 *
 * Takes a Hack assembly program via a file of .asm format,
 * and produces a text file of .hack format, containing the
 * translated Hack machine code.
 */
public class Assembler {

    public static void main(String[] args) {

        String filename = null;

//        ���������в���
        if (args.length != 1 || !isValidFileName(args[0])) {
            System.err.println("usage: java packageName/className path/fileName.asm");
            System.exit(1);
        } else {
            filename = args[0];
        }

//        ʵ����Parser����
        Parser parser = null;
        parser = getParser(filename, parser);

//        ��һ�α���: �������ű�
        SymbolTable symbolTable = new SymbolTable();
//        ��ǰ��������ص��ĵ�ַ
        int currentRomAddress = -1;

        while (parser.hasMoreCommands()) {
            parser.advance();
            parser.skipSpacesAndComments();
            if (parser.currentLength() == 0) continue;

//            ��һ�α���ֻ����L_COMMAND����ӵ�symbol table
            Parser.commandType commandType = parser.CommandType();
            if (commandType == Parser.commandType.L_COMMAND) {
                symbolTable.addEntry(parser.symbol(), currentRomAddress + 1);
            } else if (commandType == Parser.commandType.A_COMMAND || commandType == Parser.commandType.C_COMMAND){
                currentRomAddress++;
            }
        }
        parser.close();

//        ʵ����Parser����
        parser = getParser(filename, parser);
//        ���������(.hack�ļ�)
        String outputFile = filename.substring(0, filename.indexOf(".asm")) + ".hack";
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(outputFile);
        } catch (FileNotFoundException e) {
            System.out.println(e.getMessage());
        }

        while (parser.hasMoreCommands()) {
            parser.advance();
            parser.skipSpacesAndComments();
            if (parser.currentLength() == 0) continue;

            Parser.commandType commandType = parser.CommandType();

            switch (commandType) {
                case C_COMMAND:
                    String comp = Code.comp(parser.comp());
                    String dest = Code.dest(parser.dest());
                    String jump = Code.jump(parser.jump());
                    assert writer != null;
                    writer.print("111" + comp + dest + jump);
                    break;
                case L_COMMAND:
                    continue;
                case A_COMMAND:
                    String binary = Code.binary(getInt(parser.symbol(), symbolTable));
                    assert writer != null;
                    writer.print("0" + binary);
                    break;
            }
//            ������д��һ������Ķ����ƴ���
            if (parser.hasMoreCommands())
                writer.println("");
        }
//        �ر���Դ
        if (writer != null){
            writer.close();
        }
        parser.close();
    }

    private static Parser getParser(String filename, Parser parser) {

        try {
            parser = new Parser(new Scanner(new FileReader(new File(filename))));
        } catch (FileNotFoundException e) {
            System.out.println(e.getMessage());
        }
        return parser;
    }

//    ����������ļ����ǲ��ǿɽ��ܵ����룬��.asm�ļ�
    private static boolean isValidFileName(String filename) {
        return filename.endsWith(".asm");
    }

//    ��A_COMMAND�ķ��Ż�����ת��Ϊint����
    private static int getInt(String input, SymbolTable symbolTable){
        try {
//            ��input�Ƿ��Ż�����ʱ��ת��Ϊint������
            return Integer.parseInt(input);
        } catch (NumberFormatException e) {  // ��input�ǳ���
//            �����Ŵ����ڷ��ű�����input���ض�Ӧ�ĵ�ַ
            if (symbolTable.contains(input)) {
                return symbolTable.getAddress(input);
            } else {
//                �����ǵ�һ�������ı�����������뵽���ű�
                int address = symbolTable.getNextAddAndIncrement();
                symbolTable.addEntry(input, address);
                return address;
            }
        }
    }

}
