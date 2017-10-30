import java.io.File;

public class JackAnalyzer {
    public static void main(String[] args) {
        // check for proper usage
        if (args.length != 1) {
            System.out.println("IMPROPER USAGE!");
            System.out.println("CORRECT USAGE: JackAnalyzer inLocation");
            System.out.println("where inLocation is the name of a folder of .jack files or an individual .jack file");
            System.exit(1);
        }

        // compile input file or all files in input file directory
        final File inFile = new File(args[0]);
        if (inFile.isDirectory()) {
            for (final File thisInFile : inFile.listFiles()) {
                createOutFileAndCompile(thisInFile);
            }
        }
        else {
            createOutFileAndCompile(inFile);
        }

    }

    /**
     * Creates an out file and compiles the .jack in file into it.
     * @param inFile - the file to compile
     */
    private static void createOutFileAndCompile(File inFile) {
        String inFileName = inFile.getName();
        String outFileName = inFileName.substring(0, inFileName.indexOf(".jack")) + ".xml";

        File outFile = new File(outFileName);
        try {
            outFile.createNewFile();
        } catch(Exception e) {
            System.out.println("ERROR CREATING FILE " + outFileName);
        }
        compileFile(inFile, outFile);
    }

    /**
     * Compiles an individual .jack file to a .vm file
     * @param inFile - the file to read .jack code from
     * @param outFile - the file to write the compiled .vm code to
     */
    private static void compileFile(File inFile, File outFile) {

    }
}
