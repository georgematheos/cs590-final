import java.util.HashMap;

public class SymbolTable {
    private HashMap<String, SymbolInfo> classSymbols;
    private HashMap<String, SymbolInfo> subroutineSymbols;

    private HashMap<String, Integer> segmentNumbers;

    public SymbolTable() {
        classSymbols = new HashMap<String, SymbolInfo>();
        segmentNumbers = new HashMap<String, Integer>();

        segmentNumbers.put("STATIC", 0);
        segmentNumbers.put("FIELD", 0);
    }

    public void startSubroutine() {
        // reset subroutine symbols
        subroutineSymbols = new HashMap<String, SymbolInfo>();

        segmentNumbers.put("ARG", 0);
        segmentNumbers.put("VAR", 0);
    }

    public void define(String name, String type, String kind) throws Exception {
        switch (kind.toUpperCase()) {
            case "STATIC":
            case "FIELD":
                classSymbols.put(name, new SymbolInfo(type, kind.toLowerCase(), segmentNumbers.get(kind.toUpperCase())));
                segmentNumbers.put(kind.toUpperCase(), segmentNumbers.get(kind.toUpperCase()) + 1);
                break;
            case "ARG":
            case "VAR":
                subroutineSymbols.put(name, new SymbolInfo(type, kind.toLowerCase(), segmentNumbers.get(kind.toUpperCase())));
                segmentNumbers.put(kind.toUpperCase(), segmentNumbers.get(kind.toUpperCase()) + 1);
                break;
            default:
                new Exception("Unexpected value for 'kind' passed into define function: " + kind);
                break;
        }

        printSymbolTables();
    }

    public int varCount(String kind) {
        return segmentNumbers.get(kind.toUpperCase());
    }

    public String kindOf(String name) {
        if (subroutineSymbols.containsKey(name)) {
            return subroutineSymbols.get(name).kind;
        }
        else {
            System.out.println("NAME: " + name);
            return classSymbols.get(name).kind;
        }
    }

    public String typeOf(String name) {
        if (subroutineSymbols.containsKey(name)) {
            return subroutineSymbols.get(name).type;
        }
        else {
            return classSymbols.get(name).type;
        }
    }

    public int indexOf(String name) {
        if (subroutineSymbols.containsKey(name)) {
            return subroutineSymbols.get(name).number;
        }
        else {
            return classSymbols.get(name).number;
        }
    }

    private void printSymbolTables() {
        System.out.println("CLASS-SCOPED SYMBOLS:");
        System.out.println(classSymbols);
        System.out.println("SUBROUTINE-SCOPED SYMBOLS:");
        System.out.println(subroutineSymbols);
    }

    public static String convertSegmentName(String name) throws Exception {
        name = name.toUpperCase();
        switch (name) {
            case "ARG":
                return "argument";
            case "VAR":
                return "local";
            case "STATIC":
                return "static";
            case "FIELD":
                return "this";
            case "argument":
                return "ARG";
            case "local":
                return "LOCAL";
            case "static":
                return "STATIC";
            case "this":
                return "FIELD";
            default:
                throw new Exception("Unrecognized segment name: " + name);
        }
    }
}
