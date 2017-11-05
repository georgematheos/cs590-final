import java.io.File;
import java.io.FileNotFoundException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
//Pavan Garidipuri
//CS590 JackTokenizer

public class JackTokenizer {


    public final static String KEYWORD = "KEYWORD";
    public final static String SYMBOL = "SYMBOL";
    public final static String IDENTIFIER = "IDENTIFIER";
    public final static String INT_CONST = "INT_CONST";
    public final static String STRING_CONST = "STRING_CONST";


    private Scanner scanner;
    private String currentToken;
    private String currentTokenType;
    private int pointer;
    private ArrayList<String> tokens;


    private static Pattern tokenPatterns;
    private static String keyWordReg;
    private static String symbolReg;
    private static String intReg;
    private static String strReg;
    private static String idReg;

    private static HashMap<String,String> keyWordMap = new HashMap<String, String>();
    private static HashSet<Character> opSet = new HashSet<Character>();

    static {

        keyWordMap.put("class","CLASS");
        keyWordMap.put("constructor","CONSTRUCTOR");
        keyWordMap.put("function","FUNCTION");
        keyWordMap.put("method","METHOD");
        keyWordMap.put("field","FIELD");
        keyWordMap.put("static","STATIC");
        keyWordMap.put("var","VAR");
        keyWordMap.put("int","INT");
        keyWordMap.put("char","CHAR");
        keyWordMap.put("boolean","BOOLEAN");
        keyWordMap.put("void","VOID");
        keyWordMap.put("true","TRUE");
        keyWordMap.put("false","FALSE");
        keyWordMap.put("null","NULL");
        keyWordMap.put("this","THIS");
        keyWordMap.put("let","LET");
        keyWordMap.put("do","DO");
        keyWordMap.put("if","IF");
        keyWordMap.put("else","ELSE");
        keyWordMap.put("while","WHILE");
        keyWordMap.put("return","RETURN");

        opSet.add('+');opSet.add('-');opSet.add('*');opSet.add('/');opSet.add('&');opSet.add('|');
        opSet.add('<');opSet.add('>');opSet.add('=');
    }


    public static void main(String[] args)
    {
        new JackTokenizer(new File("JackFile.jack"));

    }

    public JackTokenizer(File inFile) {

        try {

            scanner = new Scanner(inFile);
            String preprocessed = "";
            String line = "";

            while(scanner.hasNext()){

                line = noComments(scanner.nextLine()).trim();

                if (line.length() > 0) {
                    preprocessed += line + "\n";
                }
            }

            preprocessed = noBlockComments(preprocessed).trim();

            //init all regex
            initRegs();

            Matcher m = tokenPatterns.matcher(preprocessed);
            tokens = new ArrayList<String>();
            pointer = 0;

            while (m.find()){

                tokens.add(m.group());

            }

        } catch (FileNotFoundException e) {


        }

        currentToken = "";
        currentTokenType = null;

    }


    private void initRegs(){

        keyWordReg = "";

        for (String seg: keyWordMap.keySet()){

            keyWordReg += seg + "|";

        }

        symbolReg = "[\\&\\*\\+\\(\\)\\.\\/\\,\\-\\]\\;\\~\\}\\|\\{\\>\\=\\[\\<]";
        //System.out.println(symbolReg);
        intReg = "[0-9]+";
        //System.out.println(intReg);
        strReg = "\"[^\"\n]*\"";
        //System.out.println(strReg);
        idReg = "[\\w_]+";

        tokenPatterns = Pattern.compile(keyWordReg + symbolReg + "|" + intReg + "|" + strReg + "|" + idReg);
    }



    public boolean hasMoreTokens() {
        return pointer < tokens.size();
    }


    public void advance(){

        if (hasMoreTokens()) {
            currentToken = tokens.get(pointer);
            pointer++;
        }else {
            throw new IllegalStateException("No more tokens");
        }


        if (currentToken.matches(keyWordReg)){
            currentTokenType = KEYWORD;
        }else if (currentToken.matches(symbolReg)){
            currentTokenType = SYMBOL;
        }else if (currentToken.matches(intReg)){
            currentTokenType = INT_CONST;
        }else if (currentToken.matches(strReg)){
            currentTokenType = STRING_CONST;
        }else if (currentToken.matches(idReg)){
            currentTokenType = IDENTIFIER;
        }else {

            throw new IllegalArgumentException("Unknown token:" + currentToken);
        }

    }

    public String getCurrentToken() {
        return currentToken;
    }

    /**
     * Returns the type of the current token
     */
    public String tokenType(){

        return currentTokenType;
    }

    /**
     * Returns the keyword which is the current token
     * Should be called only when tokeyType() is KEYWORD
     */
    public String keyWord(){

        if (currentTokenType.equals(KEYWORD)){

            return keyWordMap.get(currentToken);

        }else {
            throw new IllegalStateException("Current token is not a keyword!");
        }
    }

    /**
     * Returns the character which is the current token
     * should be called only when tokenType() is SYMBOL
     * return if current token is not a symbol return \0
     */
    public char symbol(){

        if (currentTokenType.equals(SYMBOL)){

            return currentToken.charAt(0);

        }else{
            throw new IllegalStateException("Current token is not a symbol!");
        }
    }

    /**
     * Return the identifier which is the current token
     * should be called only when tokenType() is IDENTIFIER
     */
    public String identifier(){

        if (currentTokenType.equals(IDENTIFIER)){

            return currentToken;

        }else {
            throw new IllegalStateException("Current token is not an identifier!");
        }
    }

    /**
     * Returns the integer value of the current token
     * should be called only when tokenType() is INT_CONST
     */
    public int intVal(){

        if(currentTokenType.equals(INT_CONST)) {

            return Integer.parseInt(currentToken);
        }else {
            throw new IllegalStateException("Current token is not an integer constant!");
        }
    }

    /**
     * Returns the string value of the current token
     * without the double quotes
     * should be called only when tokenType() is STRING_CONST
     */
    public String stringVal(){

        if (currentTokenType.equals(STRING_CONST)) {

            return currentToken.substring(1, currentToken.length() - 1);

        }else {
            throw new IllegalStateException("Current token is not a string constant!");
        }
    }

    /**
     * move pointer back
     */
    public void pointerBack(){

        if (pointer > 1) {
            pointer-=2;
        }
        advance();

    }

    /**
     * return if current symbol is a op
     */
    public boolean isOp(){
        return opSet.contains(symbol());
    }

    /**
     * Delete comments(String after "//") from a String
     */
    public static String noComments(String strIn){

        int position = strIn.indexOf("//");

        if (position != -1){

            strIn = strIn.substring(0, position);

        }

        return strIn;
    }

    /**
     * Delete spaces from a String
     */
    public static String noSpaces(String strIn){
        String result = "";

        if (strIn.length() != 0){

            String[] segs = strIn.split(" ");

            for (String s: segs){
                result += s;
            }
        }

        return result;
    }

    /*
     * delete block comment
     */
    public static String noBlockComments(String strIn){

        int startIndex = strIn.indexOf("/*");

        if (startIndex == -1) return strIn;

        String result = strIn;

        int endIndex = strIn.indexOf("*/");

        while(startIndex != -1){

            if (endIndex == -1){

                return strIn.substring(0,startIndex - 1);

            }
            result = result.substring(0,startIndex) + result.substring(endIndex + 2);

            startIndex = result.indexOf("/*");
            endIndex = result.indexOf("*/");
        }

        return result;
    }
}