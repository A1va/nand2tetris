
/*
 * Created by Nolva on 2020/9/8.
 */

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.PrintWriter;
import java.util.Scanner;

/**
 * Compilation: javac Assembler.Assembler.java
 * Execution: java Assembler.Assembler filename.asm
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
        if (args.length != 1 || isValidFilename(args[0])) {
            System.err.println("usage: java Assembler.Assembler filename.asm");
            System.exit(1);
        } else {
            filename = args[0];
        }

//        ʵ����Parser����
        Parser parser = null;
        parser = getParser(filename, parser);

//        ��ǰ��������ص��ĵ�ַ
//        int currentRomAddress = -1;

//        ���������(.hack�ļ�)
        String outputFile = filename.substring(0, filename.indexOf(".asm")) + ".hack";
        PrintWriter writer = null;
        try {
            writer = new PrintWriter(outputFile);
        } catch (FileNotFoundException e) {
            System.out.println(e.getMessage());
            e.printStackTrace();
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
                    String binary = Code.binary(getInt(parser.symbol()));
                    assert writer != null;
                    writer.print("0" + binary);
                    break;
            }
//            ������д��һ������Ķ����ƴ���
            if (parser.hasMoreCommands())
                writer.println("");
        }
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
            e.printStackTrace();
        }
        return parser;
    }

//    ����������ļ����ǲ��ǿɽ��ܵ����룬��.asm�ļ�
    private static boolean isValidFilename(String filename) {
        return filename.endsWith(".asm");
    }

    private static int getInt(String input){
        return Integer.parseInt(input);
    }

}
