import java.io.File;

public class CompilationEngine2 {
    private JackTokenizer tokenizer;
    private String xml = "";

    public CompilationEngine2(File inFile, File outFile) throws Exception {
        // create a tokenizer object
        tokenizer = new JackTokenizer(inFile);

        // at this level of the program, we are outside all class declarations,
        // so if the tokenizer has more tokens, it had better be a class declaration, so compile the class
        while (tokenizer.hasMoreTokens()) {
           compileClass();
        }
    }

    /**
     * Recursively compiles an entire class.
     * @precondition - tokenizer advanced to 'class' token at beginning of class declaration
     * @postcondition - tokenizer advanced past } token ending class
     * @throws Exception
     */
    public void compileClass() throws Exception {
        addToXml("<class><keyword>class</keyword>");

        addTokenIdentifierToXml(true);
        ensureSymbolValueAndAddXml('{', true);

        // for every block of code until we reach the } ending the class:
        ensureMoreTokensAndAdvance();
        while (!(tokenizer.tokenType().equals("SYMBOL") && tokenizer.symbol() == '}')) {
            ensureTokenType("KEYWORD", false);
            switch (tokenizer.keyWord()) {
                case "METHOD":
                case "FUNCTION":
                case "CONSTRUCTOR":
                    compileSubroutine();
                    break;
                case "FIELD":
                case "STATIC":
                    compileClassVarDec();
                    break;
                default:
                    throw new Exception("Error: unexpected token found where one of the following must be: 'method', 'function', 'constructor', 'field', or 'static' (or } to end class).");
            }
        }

        ensureSymbolValueAndAddXml('}');

        ensureMoreTokensAndAdvance();

        addToXml("</class>");
    }

    /**
     * @precondition - tokenizer advanced to 'method', 'function', or 'constructor' keyword starting subroutine dec
     * @postcondition - tokenizer advanced immediately past } closing subroutine dec
     * @throws Exception
     */
    public void compileSubroutine() throws Exception {
        addToXml("<subroutineDec><keyword>" + tokenizer.keyWord().toLowerCase() + "</keyword>");

        // subroutine dec parentheticals
        addTokenIdentifierToXml(true);
        ensureSymbolValueAndAddXml('(', true);
        compileParameterList();
        ensureSymbolValueAndAddXml(')');

        // subroutine body
        ensureSymbolValueAndAddXml('{', true);
        compileStatements();
        ensureSymbolValueAndAddXml('}');

        ensureMoreTokensAndAdvance();
        addToXml("</subroutineDec>");
    }

    /**
     * @precondition - tokenizer advanced to ( starting parameter list
     * @postcondition - tokenizer advanced to ) closing parameter list
     * @throws Exception
     */
    public void compileParameterList() throws Exception {
        addToXml("<parameterList>");

        ensureTokenType("SYMBOL", true);

        while (tokenizer.symbol() != ')') {
            addTokenTypeToXML(true);
            addTokenIdentifierToXml(true);
            ensureTokenType("SYMBOL", true);
            if (tokenizer.symbol() != ',' && tokenizer.symbol() != ')') {
                throw new Exception("UNEXPECTED TOKEN: was expecting ',' or ')' but found " + tokenizer.symbol());
            }
        }

        addToXml("</parameterList>");
    }

    /**
     * Tokenizer index should be on the first token of this term once this function is entered.
     * @postcondition: advances tokenizer to first token after expression compiled
     * @throws Exception
     */
    public void compileExpression() throws Exception {
        addToXml("<expression>");

        compileTerm();

        while (tokenizer.tokenType().equals("SYMBOL")) {
            String symbolName;

            switch (tokenizer.symbol()) {
                case '&':
                    symbolName = "&amp;";
                    break;
                case '<':
                    symbolName = "&lt;";
                    break;
                case '>':
                    symbolName = "&gt;";
                    break;
                case '+':
                case '-':
                case '*':
                case '/':
                case '|':
                case '=':
                    symbolName = "" + tokenizer.symbol();
                    break;
                default:
                    throw new Exception("ERROR: unexpected symbol found in expression where the only symbol allowed is a binary operator");
            }

            addToXml("<symbol>" + symbolName + "</symbol>");
            ensureMoreTokensAndAdvance();
        }


        addToXml("</expression>");
    }

    /**
     * Tokenizer index should be on the first token of this term once this function is entered.
     * @postcondition: advances tokenizer to first token after term compiled
     * @throws Exception
     */
    public void compileTerm() throws Exception {
        addToXml("<term>");

        // see what the first token is
        switch (tokenizer.tokenType()) {

            case "SYMBOL":
                // if the symbol is a unary operation, add the symbol to the xml
                // then advance and compile the term it is operating on
                if (tokenizer.symbol() == '-' || tokenizer.symbol() == '~') {
                    addToXml("<symbol>" + tokenizer.symbol() + "</symbol>");
                    ensureMoreTokensAndAdvance();
                    compileTerm();
                }
                // the only other token that may start a term is a ( open parenthesis
                else {
                    ensureSymbolValueAndAddXml('(');
                    compileExpression();
                    ensureSymbolValueAndAddXml(')');
                    ensureMoreTokensAndAdvance();
                    break;
                }
            case "INT_CONST":
                addToXml("<integerConstant>" + tokenizer.intVal() + "</integerConstant>");
                ensureMoreTokensAndAdvance();
                break;
            case "STRING_CONST":
                addToXml("<stringConstant>" + tokenizer.stringVal() + "</stringConstant>");
                ensureMoreTokensAndAdvance();
                break;
            case "IDENTIFIER":
                addTokenIdentifierToXml(false);

                // if it is an identifier, compile possible . class membership notation and [] array indexing notation
                ensureMoreTokensAndAdvance();
                if (tokenizer.tokenType().equals("SYMBOL") && (tokenizer.symbol() == '[' || tokenizer.symbol() == '.')) {
                    compileArrayIndexingAndClassMembershipNotation();
                }
                break;
            case "KEYWORD":
                // only the following keywords are allowed
                switch (tokenizer.keyWord()) {
                    case "true":
                    case "false":
                    case "null":
                    case "this":
                        addToXml("<keyword>" + tokenizer.keyWord() + "</keyword>");
                        ensureMoreTokensAndAdvance();
                        break;

                    default:
                        throw new Exception("ERROR: Unexpected keyword in expression: " + tokenizer.keyWord());
                }
                break;
        }

        addToXml("</term>");
    }

    /**
     * @precondition: tokenizer is advanced to { symbol starting statements
     * @postcondition: tokenizer is advanced to first token after } closing statements
     * @throws Exception
     */
    public void compileStatements() throws Exception {
        addToXml("<statements>");

        ensureMoreTokensAndAdvance();

        // repeat until we reach the } closing the statements segment
        while (!(tokenizer.tokenType().equals("SYMBOL") && tokenizer.symbol() == '}')) {
            // each statement should start with a keyword and be one of the types of keywords enumerated below
            ensureTokenType("KEYWORD");
            switch (tokenizer.keyWord()) {
                case "LET":
                    break;
                case "IF":
                    compileIf();
                    break;
                case "VAR":
                    break;
                case "WHILE":
                    break;
                case "DO":
                    break;
                case "RETURN":
                    break;
                default:
                    throw new Exception("ERROR: invalid keyword at beginning of statement in a subroutine declaration.");
            }
        }

        ensureMoreTokensAndAdvance();

        addToXml("</statements>");
    }

    /**
     * @precondition: tokenizer is advanced to 'do' keyword starting do statement
     * @postcondition: tokenizer is advanced to first token after ; closing do statement
     * @throws Exception
     */
    public void compileDo() throws Exception {
        addToXml("<doStatement><keyword>do</keyword>");

        // the first token should be a class/object identifier for function or class/obj with function to call
        addTokenIdentifierToXml(true);

        // next token is either . [in Object.function()] or ( [in function()]
        ensureTokenType("SYMBOL", true);
        if (tokenizer.symbol() == '.') {
            addToXml("<symbol>.</symbol>");
            // add function identifier to xml
            addTokenIdentifierToXml(true);

            ensureMoreTokensAndAdvance();
        }

        ensureSymbolValueAndAddXml('(');
        ensureMoreTokensAndAdvance();

        compileExpressionList();

        ensureSymbolValueAndAddXml(')', false);
        ensureSymbolValueAndAddXml(';', true);

        addToXml("</doStatement>");
    }

    /**
     * @precondition: tokenizer is advanced to first token in expression list or the ) immediatly after it
     * @postcondition: tokenizer is advanced to the ) immediately after the expression list
     * @throws Exception
     */
    public void compileExpressionList() throws Exception {
        addToXml("<expressionList>");
        while (!(tokenizer.tokenType().equals("SYMBOL") && tokenizer.symbol() == ')')) {
            compileExpression();
            ensureSymbolValueAndAddXml(',', true);
            ensureMoreTokensAndAdvance();
        }
        addToXml("</expressionList>");
    }

    /**
     * @precondition: tokenizer is advanced to the 'let' token beginning let statement
     * @postcondition: tokenizer is advanced to the token immediately after the ; ending let statmeent
     * @throws Exception
     */
    public void compileLet() throws Exception {
        addToXml("<letStatement><keyword>let</keyword>");

        // this statement looks like:
        // let IDENTIFIER||this([EXPRESSION])? (.IDENTIFIER) = EXPRESSION;

        ensureMoreTokensAndAdvance();
        if (tokenizer.tokenType().equals("KEYWORD")) {
            if (tokenizer.keyWord() != "THIS") {
                throw new Exception("ERROR: unexpected keyword " + tokenizer.keyWord() + " found where an identifier or the 'this' keyword was expected.");
            }

            addToXml("<keyword>this</keyword>");
        }
        else {
            addTokenIdentifierToXml(false);
        }

        ensureTokenType("SYMBOL", true);

        if (tokenizer.tokenType().equals("SYMBOL") && (tokenizer.symbol() == '[' || tokenizer.symbol() == '.')) {
            compileArrayIndexingAndClassMembershipNotation();
        }

        addToXml("</letStatement>");
    }

    /**
     * @precondition - tokenizer is advanced to first token which is either a . opening class membership notation or [ opening array indexing notation
     * @postcondition - tokenizer is advanced past last ] closing array indexing
     * (multidimensional array indexing supported: eg. array[2][4])
     * @throws Exception
     */
    private void compileArrayIndexingAndClassMembershipNotation() throws Exception {
        // check whether we have [] array indexing syntax
        // or . syntax to access class elements
        // repeat this until we pass all such constructions
        while (tokenizer.tokenType().equals("SYMBOL") && (tokenizer.symbol() == '[' || tokenizer.symbol() == '.')) {
            if (tokenizer.symbol() == '.') {
                addToXml("<symbol>.</symbol>");
                addTokenIdentifierToXml(true);
                ensureMoreTokensAndAdvance();
            }
            else if (tokenizer.symbol() == '[') {
                addToXml("<symbol>[</symbol>");
                while (tokenizer.tokenType().equals("SYMBOL") && tokenizer.symbol() == '[') {
                    ensureMoreTokensAndAdvance();
                    compileExpression();
                    ensureSymbolValueAndAddXml(']');
                    ensureMoreTokensAndAdvance();
                }
            }
        }
    }

    /**
     * @precondition - tokenizer is advanced to 'return' token starting the return statement
     * @postcondition - tokenizer is advanced to first token after ; ending return statement
     * @throws Exception
     */
    public void compileReturn() throws Exception {
        addToXml("<returnStatement><keyword>return</keyword>");
        ensureMoreTokensAndAdvance();
        // check if we just have the semicolon immediately
        if (tokenizer.tokenType().equals("SYMBOL")  && tokenizer.symbol() == ';') {
            addToXml("<symbol>;</symbol>");
            ensureMoreTokensAndAdvance();
        }
        else {
            compileExpression();
            ensureSymbolValueAndAddXml(';');
            ensureMoreTokensAndAdvance();
        }

        addToXml("</returnStatement>");
    }

    /**
     * @precondition - tokenizer is advanced to 'while' token starting while statement
     * @postcondition - tokenizer is advanced immediately past } token ending while statement
     * @throws Exception
     */
    public void compileWhile() throws Exception {
        addToXml("<whileStatement><keyword>while</keyword>");

        ensureSymbolValueAndAddXml('(', true);
        compileExpression();
        ensureSymbolValueAndAddXml(')', true);

        ensureSymbolValueAndAddXml('{', true);
        compileStatements();
        ensureSymbolValueAndAddXml('}', true);

        addToXml("</whileStatement>");
    }

    /**
     * @precondition - tokenizer advanced to 'var' keyword starting var dec
     * @postcondition - tokenizer advanced past ; at end of statement
     * @throws Exception
     */
    public void compileVarDec() throws Exception {
        addToXml("<varDec><keyword>var</keyword>");

        ensureMoreTokensAndAdvance();
        compileVarDecList();

        addToXml("</varDec>");
    }

    /**
     * @precondition - tokenizer advanced to 'field' or 'static' keyword starting var dec
     * @postcondition - tokenizer advanced past ; at end of statement
     * @throws Exception
     */
    public void compileClassVarDec() throws Exception {
        addToXml("<classVarDec></keyword>" + tokenizer.keyWord().toLowerCase() + "</keyword>");

        compileVarDecList();

        addToXml("</classVarDec>");
    }

    /**
     * Compiles a list of variables being declared, ex. var String a, b, c;
     * @precondition - tokenizer advanced to type term at beginning of list
     * @postconditino - tokenizer advanced past ; at end of statement
     * @throws Exception
     */
    private void compileVarDecList() throws Exception {
        // multiple vars might be declared (ex. field int x, int y;) so loop until we reach a semicolon
        boolean moreVariablesBeingDeclared = true;

        // the type of the variable being declared
        addTokenTypeToXML(false);

        while (moreVariablesBeingDeclared) {
            addTokenIdentifierToXml(true);

            ensureTokenType("SYMBOL", true);
            addToXml("<symbol>");
            if (tokenizer.symbol() == ';') {
                addToXml(";");
                moreVariablesBeingDeclared = false;
            }
            else if (tokenizer.symbol() != ',') throw new Exception("Unexpected symbol " + tokenizer.symbol() + " where ';' or ',' was expected.");
            else {
                addToXml(",");
            }
            addToXml("</symboL>");
        }
    }

    /**
     * @precondition: tokenizer is advanced to 'if' keyword starting if statement
     * @postcondition: tokenizer is advanced to first token after } closing if statement
     * @throws Exception
     */
    public void compileIf() throws Exception {
        addToXml("<ifStatement><keyword>if</keyword>");

        // should open parenthesis for condition here
        ensureSymbolValueAndAddXml('(', true);
        // expression of the condition
        compileExpression();
        // close condition
        ensureSymbolValueAndAddXml(')', false);

        // compile if body
        ensureSymbolValueAndAddXml('{', true);
        compileStatements();
        ensureSymbolValueAndAddXml('}', false);

        // check if there is an else statement
        ensureMoreTokensAndAdvance();
        if (tokenizer.tokenType().equals("KEYWORD") && tokenizer.keyWord().equals("ELSE")) {
            ensureSymbolValueAndAddXml('{', true);
            compileStatements();
            ensureSymbolValueAndAddXml('}', false);

            ensureMoreTokensAndAdvance();
        }

        addToXml("</ifStatement>");
    }

    /**
     * Throws an error if there are no more tokens on the tokenizer.
     * @throws Exception
     */
    private void ensureMoreTokens() throws Exception {
        if (!tokenizer.hasMoreTokens()) {
            throw new Exception("More tokens were expected but none were found.");
        }
    }

    /**
     * Syntactic sugar to ensure there are more tokens and then if there are, advance to next token.
     * @throws Exception
     */
    private void ensureMoreTokensAndAdvance() throws Exception {
        ensureMoreTokens();
        tokenizer.advance();
    }



    /**
     * Throws an error if the type of the current token is not that type provided to the function.
     * If checkNextToken is true, this ensures there is another token, advances to it, and then performs the check.
     * If it is false (or not included), checks this token.
     * @param type
     * @param checkNextToken
     * @throws Exception
     */
    private void ensureTokenType(String type, boolean checkNextToken) throws Exception {
        if (checkNextToken) {
            ensureMoreTokensAndAdvance();
        }

        if (!tokenizer.tokenType().equals(type)) {
            throw new Exception("Invalid token type. Token must be of type " + type + " but is of type " + tokenizer.tokenType());
        }
    }

    /**
     * Throws an error if the type of the current token is not that type provided to the function.
     * @param type
     * @throws Exception
     */
    private void ensureTokenType(String type) throws Exception {
        ensureTokenType(type, false);
    }

    /**
     * Add the next token, (which is a token identifying a type -- eg. int, char, Bicycle [a class]) to the XML code.
     * @throws Exception
     */
    private void addTokenTypeToXML(boolean moveToNextToken) throws Exception {
        if (moveToNextToken) {
            ensureMoreTokensAndAdvance();
        }

        // next token should either be a keyword, like int or boolean, or an identifier of a user-defined type
        switch (tokenizer.tokenType()) {
            case "KEYWORD":
                switch (tokenizer.keyWord()) {
                    case "INT":
                    case "BOOLEAN":
                    case "CHAR":
                        addToXml("<keyword>" + tokenizer.keyWord().toLowerCase() + "</keyword>");
                        break;
                    default: // if it is a keyword but not one of the enumerated options, throw an error
                        throw new Exception("SYNTAX ERROR: invalid keyword in field variable declaration.");
                }
                break;
            case "IDENTIFIER":
                addToXml("<identifier>" + tokenizer.identifier() + "</identifier>");
                break;
            default:
                throw new Exception("A token which wa not an identifier or a keyword was found in a location where a type was expected, and a type may only be an identifier or a keyword.");
        }
    }

    /**
     * Adds a token--either this current token, if moveToNextToken is false, or the next one, if moveToNextToken is true,
     * which is an identifier, to XML.  Makes sure it is an identifer.
     * @param moveToNextToken
     * @throws Exception
     */
    public void addTokenIdentifierToXml(boolean moveToNextToken) throws Exception {
        // varaible identifier
        ensureTokenType("IDENTIFIER", moveToNextToken);
        addToXml("<identifier>" + tokenizer.identifier() + "</identifier>");
    }

    /**
     * Adds current token, which 
     * @throws Exception
     */
    public void addTokenIdentifierToXml() throws Exception {
        addTokenIdentifierToXml(false);
    }

    /**
     * Ensures that either the current or next token is a symbol with a specific value, and adds it to xml.
     * @param symbolValue
     * @param moveToNextToken
     * @throws Exception
     */
    private void ensureSymbolValueAndAddXml(char symbolValue, boolean moveToNextToken) throws Exception {
        ensureTokenType("SYMBOL", moveToNextToken);
        if (tokenizer.symbol() != symbolValue) {
            throw new Exception("ERROR: " + symbolValue + " symbol expected but " + tokenizer.symbol() + " found.");
        }
        addToXml("<symbol>" + symbolValue + "</symbol>");
    }

    private void ensureSymbolValueAndAddXml(char symbolValue) throws Exception {
        ensureSymbolValueAndAddXml(symbolValue, false);
    }

    /**
     * Adds the provided text to the xml document.
     * @param text
     */
    private void addToXml(String text) {
        xml += text;
        System.out.println(xml);
    }

}
