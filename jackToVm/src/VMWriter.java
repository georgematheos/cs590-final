import java.io.File;

public class VMWriter {
    private String vmCode = "";

    public VMWriter(File outFile) {

    }

    /**
     * Writes the provided text to the vm code output.
     * @param text
     */
    private void write(String text) {
        vmCode += text;
    }

    // TODO: remove
    public void printVMCode() {
        System.out.println(vmCode);
    }

    public void writePush(String segment, int index) {
        write("push " + segment.toLowerCase() + " " + index + "\n");
    }

    public void writePop(String segment, int index) {
        write("pop " + segment.toLowerCase() + " " + index + "\n");
    }

    public void writeArithmetic(String command) {
        write(command + "\n");
    }

    public void writeLabel(String label) {
        write("label " + label + "\n");
    }

    public void writeGoto(String label) {
        write("goto " + label + "\n");
    }

    public void writeIf(String label) {
        write("if-goto " + label + "\n");
    }

    public void writeCall(String name, int nArg) {
        write("call " + name + " " + nArg + "\n");
    }

    public void writeFunction(String name, int nLocals) {
        write("function " + name + " " + nLocals + "\n");
    }

    public void writeReturn() {
        write("return");
    }

    public void close() {

    }
}
