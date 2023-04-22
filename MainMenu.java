/*
Each line is separated by new line ('\n')
*/
import java.util.ArrayList;
import java.util.HashMap;
import java.util.AbstractMap;
import java.util.Map;
import java.util.*;
import java.nio.file.Files;
import java.nio.file.Paths;

class MainMenu {
    public static void main(String[] args) {
        //Main loop
        //Interpreter interp = new Interpreter("sds");
        // Tokenize tokenizer = new Tokenize("//test program\nset r0 to 13\nset r1 to r0\nadd r0 to r1\nprintln r1\nend");
    
        // //Tokenize
        // ArrayList<Map.Entry<Tokenize.TOKENS, String>> tokens = tokenizer.tokenize();

        // //Parsing phase
        // Parser parser = new Parser(tokens);

        // parser.parse();

        Interpreter interp = new Interpreter("maintest.sail2023");
        interp.execute();
    }
}

//=== Interpreter class ===
class Interpreter {
    public String file;
    public Interpreter(String file) {
        this.file = file;
    }

    //Execute the file
    public void execute() {
        String code = "";
        //Read the code
        try {
            code = new String(Files.readAllBytes(Paths.get(file)));
        } catch (Exception e) {
            e.printStackTrace();
        }

        Tokenize tokenizer = new Tokenize(code);
    
        //Tokenize
        ArrayList<Map.Entry<Tokenize.TOKENS, String>> tokens = tokenizer.tokenize();

        //Parsing phase
        Parser parser = new Parser(tokens);

        parser.parse();

        //parser.debug();

        // Tokenize t = new Tokenize("3 > 2 && (2 < 1 || 4 > 3)");
        // Parser parser = new Parser(t.tokenize());

        // Boolean res = parser.parseTokensReturnBool(t.tokenize());
        // System.out.println(res);
    }
}

//Tokenizer
public class Tokenize {
    //Initialize the string to tokenize
    String str;

    //When tokenizing, set into enum values later stored into a hashmap
    public enum TOKENS {
        PLUS, MINUS, TIMES, DIVIDE,
        NUMERICAL, IDENTIFIER,
        O_BRACKET, C_BRACKET,
        NEWLINE, GREATER, SMALLER,
        GREATER_EQ, SMALLER_EQ, EQUALS,
        STRING_LITERAL,
        //Reserved keywords string values
        TO, FROM, DO,
        SET, ADD, SUBTRACT,
        PRINT, PRINTLN,
        IF, ENDIF, AND, OR,
        WHILE, ENDWHILE,
        //Misc
        END, COMMENT, EOF
    }

    public Tokenize(String s) {
        str = s;
    }

    //Function to easily get the tokens from keywords
    private TOKENS checkKeywords(String valCheck) {
        //Create a map for the reversed keywords
        Map<String, TOKENS> tokenTypesKeywods = new HashMap<>();
        tokenTypesKeywods.put("set", TOKENS.SET);
        tokenTypesKeywods.put("add", TOKENS.ADD);
        tokenTypesKeywods.put("subtract", TOKENS.SUBTRACT);
        tokenTypesKeywods.put("to", TOKENS.TO);
        tokenTypesKeywods.put("do", TOKENS.DO);
        tokenTypesKeywods.put("from", TOKENS.FROM);
        tokenTypesKeywods.put("println", TOKENS.PRINTLN);
        tokenTypesKeywods.put("print", TOKENS.PRINT);
        tokenTypesKeywods.put("if", TOKENS.IF);
        tokenTypesKeywods.put("endif", TOKENS.ENDIF);
        tokenTypesKeywods.put("while", TOKENS.WHILE);
        tokenTypesKeywods.put("endwhile", TOKENS.ENDWHILE);
        tokenTypesKeywods.put("end", TOKENS.END);

        for (Map.Entry<String, TOKENS> entry : tokenTypesKeywods.entrySet()) {
            if (entry.getKey().equals(valCheck))
                return entry.getValue();
        }
        return TOKENS.IDENTIFIER; //IDENTIFIER represent variable names or any other keyword tokens
    }

    //This will return a list of tokens
    public ArrayList<Map.Entry<TOKENS, String>> tokenize() {
        //Initialize the list of tokens
        ArrayList<Map.Entry<TOKENS, String>> tokens = new ArrayList<>();

        //Create a map for the token types
        Map<Character, TOKENS> tokenTypes = new HashMap<>();
        tokenTypes.put('+', TOKENS.PLUS);
        tokenTypes.put('-', TOKENS.MINUS);
        tokenTypes.put('*', TOKENS.TIMES);
        tokenTypes.put('/', TOKENS.DIVIDE);
        tokenTypes.put('(', TOKENS.O_BRACKET);
        tokenTypes.put(')', TOKENS.C_BRACKET);
        tokenTypes.put('<', TOKENS.SMALLER);
        tokenTypes.put('>', TOKENS.GREATER);
        tokenTypes.put('\n', TOKENS.NEWLINE);

        //Store index
        int index = 0;

        //Must read each character, for this a while loop will make it easier within the tokenization
        //process so we can check the next tokens
        while (index < str.length()) {
            //Read char at index
            char ch = str.charAt(index);

            //Check if token is a number value
            if (Character.isDigit(ch) || ch == '-') {
                //Init numeric value string
                StringBuilder val = new StringBuilder();
                if (ch == '-') {
                    val.append(ch);
                    index++;
                    //Check if the next value after the negative sign is numerical
                    if (!Character.isDigit(str.charAt(index)))
                        throw new RuntimeException("Error: Invalid negative value parsed in");
                    
                    ch = str.charAt(index);
                }
                //Traverse
                while (Character.isDigit(ch)) {
                    val.append(ch);
                    index++;
                    //If reach to the end of the string break the loop
                    if (index >= str.length())
                        break;
                    ch = str.charAt(index);
                }
                //Append the token to the ArrayList
                tokens.add(new AbstractMap.SimpleEntry<>(TOKENS.NUMERICAL, val.toString()));
            }
            //Check for string values
            if (Character.isLetter(ch)) {
                //String
                StringBuilder val = new StringBuilder();
                //Traverse
                while (Character.isLetter(ch) || Character.isDigit(ch)) {
                    val.append(ch);
                    index++;
                    //Make sure to break at end of string
                    if (index >= str.length())
                        break;
                    ch = str.charAt(index);
                }
                
                tokens.add(new AbstractMap.SimpleEntry<>(checkKeywords(val.toString()), val.toString()));
            }
            //AND bool
            else if (ch == '&' && index + 1 < str.length() && str.charAt(index + 1) == '&') {
                index += 2;
                tokens.add(new AbstractMap.SimpleEntry<>(TOKENS.AND, "&&"));
            }
            //OR bool
            else if (ch == '|' && index + 1 < str.length() && str.charAt(index + 1) == '|') {
                index += 2;
                tokens.add(new AbstractMap.SimpleEntry<>(TOKENS.OR, "||"));
            }
            //EQUALS bool
            else if (ch == '=' && index + 1 < str.length() && str.charAt(index + 1) == '=') {
                index += 2;
                tokens.add(new AbstractMap.SimpleEntry<>(TOKENS.EQUALS, "=="));
            }
            //Comments
            else if (ch == '/' && index + 1 < str.length() && str.charAt(index + 1) == '/') {
                while (ch != '\n') {
                    index++;
                    if (index >= str.length())
                        break;
                    ch = str.charAt(index);
                }
                //Don't add as a token for the parser for comments
                continue;
            }
            else if (ch == '\'') {
                //String literal
                StringBuilder val = new StringBuilder();
                boolean isEscapeChar = false;
                index++;
                ch = str.charAt(index);
                //Traverse until the end of the string literal
                while (ch != '\'' || isEscapeChar) {
                    isEscapeChar = ch == '\\' && !isEscapeChar ? true : false;
                    val.append(ch);
                    index++;
                    if (index >= str.length())
                        throw new RuntimeException("Error: String literal is not terminated");
                    ch = str.charAt(index);
                }
                index++;
                tokens.add(new AbstractMap.SimpleEntry<>(TOKENS.STRING_LITERAL, val.toString()));
            }
            //Singular character identifiers
            else if (tokenTypes.containsKey(ch)) {
                //Get the token type from the map
                TOKENS token = tokenTypes.get(ch);
                tokens.add(new AbstractMap.SimpleEntry<>(token, Character.toString(ch)));
                //Increment index
                index++;
            } else {
                //Invalid character, skip it
                index++;
            }
        }

        //End of file
        tokens.add(new AbstractMap.SimpleEntry<>(TOKENS.EOF, ""));

        return tokens;
    }
}

//=== Parsing ===
public class Parser {
    ArrayList<Map.Entry<Tokenize.TOKENS, String>> tokens;
    int currentTokenIndex;
    Map<String, Integer> variables; //The set of variables

    //Parser requires the tokens
    public Parser(ArrayList<Map.Entry<Tokenize.TOKENS, String>> tokens) {
        this.tokens = tokens;
        currentTokenIndex = 0;
        variables = new HashMap<>();
    }

    //Parse in
    public void parse() {
        while (currentTokenIndex < tokens.size()) {
            if (tokens.get(currentTokenIndex).getKey() == Tokenize.TOKENS.END)
                break;
            parseStatement();
        }
    }

    private void parseStatement() {
        if (match(Tokenize.TOKENS.SET)) {
            parseAssignment();
        } 
        else if (match(Tokenize.TOKENS.PRINTLN)) {
            parsePrint(true);
        }
        else if (match(Tokenize.TOKENS.PRINT)) {
            parsePrint(false);
        }
        else if (match(Tokenize.TOKENS.ADD)) {
            parseAddition();
        }
        else if (match(Tokenize.TOKENS.IF)) {
            //Format of IF statement: if <condition> do ... endif
            parseIfStatement();
        }
        else if (match(Tokenize.TOKENS.WHILE)) {
            //Format of WHILE statement: while <condition> do ... endwhile
            parseWhileStatement();
        }
        else if (match(Tokenize.TOKENS.NEWLINE)) {
            currentTokenIndex++; //Means we ignore
        }
        else {
            error("Expected assignment or print statement");
        }
    }

    //Debug by printing tokens
    public void debug() {
        System.out.println("Number of tokens: " + tokens.size());
        for (Map.Entry<Tokenize.TOKENS, String> token : tokens) {
           System.out.println(token.getKey() + " " + token.getValue());
        }   
    }

    //When we want to parse in a statement we must consume the tokens and check if
    //it is within the correct format, if it is then we increment and continue
    private Map.Entry<Tokenize.TOKENS, String> consume(Tokenize.TOKENS expected) {
        if (currentTokenIndex >= tokens.size()) {
            error("Unexpected end of input");
        }
        Map.Entry<Tokenize.TOKENS, String> currentToken = tokens.get(currentTokenIndex);
        if (currentToken.getKey() != expected) {
            error("Expected " + expected + ", but got " + currentToken.getKey());
        }
        currentTokenIndex++;
        return currentToken;
    }

    //This is for variable assignment
    private void parseAssignment() {
        consume(Tokenize.TOKENS.SET);
        String identifier = consume(Tokenize.TOKENS.IDENTIFIER).getValue();
        consume(Tokenize.TOKENS.TO);

        //Value to assign (can be an identifier or numerical)
        int value = 0; //Integer.parseInt(consume(Tokenize.TOKENS.NUMERICAL).getValue());

        //If numerical
        if (tokens.get(currentTokenIndex).getKey() == Tokenize.TOKENS.NUMERICAL)
            value = Integer.parseInt(consume(Tokenize.TOKENS.NUMERICAL).getValue());
        //If keyword
        else if (tokens.get(currentTokenIndex).getKey() == Tokenize.TOKENS.IDENTIFIER) {
            String val = consume(Tokenize.TOKENS.IDENTIFIER).getValue();
            if (!variables.containsKey(val)) {
                error("Variable '" + val + "' has not been assigned a value");
            }
            value = variables.get(val);
        }

        //Generate an AST node for the assignment statement (Debugging)
        variables.put(identifier, value);
        //System.out.println("Assign " + identifier + " = " + value);
    }

    //Parse in addition
    private void parseAddition() {
        consume(Tokenize.TOKENS.ADD);

        //Value to assign (can be an identifier or numerical)
        int value = 0;
        //If numerical
        if (tokens.get(currentTokenIndex).getKey() == Tokenize.TOKENS.NUMERICAL)
            value = Integer.parseInt(consume(Tokenize.TOKENS.NUMERICAL).getValue());
        //If keyword
        else if (tokens.get(currentTokenIndex).getKey() == Tokenize.TOKENS.IDENTIFIER) {
            String val = consume(Tokenize.TOKENS.IDENTIFIER).getValue();
            if (!variables.containsKey(val)) {
                error("Variable '" + val + "' has not been assigned a value");
            }
            value = variables.get(val);
        }

        consume(Tokenize.TOKENS.TO);

        //Get variable to add
        String added = consume(Tokenize.TOKENS.IDENTIFIER).getValue();
        if (!variables.containsKey(added)) {
            error("Variable '" + added + "' has not been assigned a value");
        }

        //Set the value within the variables hashmap
        variables.put(added, variables.get(added) + value);
    }

    //Parses either print or println
    private void parsePrint(boolean newLine) {
        // consume(Tokenize.TOKENS.PRINTLN);
        // String stdout = consume(Tokenize.TOKENS.IDENTIFIER).getValue();

        // if (!variables.containsKey(stdout)) {
        //     error("Variable '" + stdout + "' has not been assigned a value");
        // }

        // System.out.println(variables.get(stdout));

        consume(newLine ? Tokenize.TOKENS.PRINTLN : Tokenize.TOKENS.PRINT);

        List<String> output = new ArrayList<>();
        while (true) {
            Tokenize.TOKENS token = tokens.get(currentTokenIndex).getKey();
            if (token == Tokenize.TOKENS.STRING_LITERAL) {
                output.add(consume(Tokenize.TOKENS.STRING_LITERAL).getValue());
            } 
            else if (token == Tokenize.TOKENS.IDENTIFIER) {
                String varName = consume(Tokenize.TOKENS.IDENTIFIER).getValue();
                if (!variables.containsKey(varName)) {
                    error("Error: Variable '" + varName + "' has not been assigned a value");
                }
                output.add(variables.get(varName).toString());
            }
            else if (token == Tokenize.TOKENS.PLUS) {
                consume(Tokenize.TOKENS.PLUS);
            }
            else {
                break;
            }
        }
       System.out.print(newLine ? String.join("", output) + "\n" : String.join("", output));
    }

    private Boolean checkIfCondition(Tokenize.TOKENS toks) {
        return toks == Tokenize.TOKENS.NUMERICAL || toks == Tokenize.TOKENS.GREATER ||
            toks == Tokenize.TOKENS.SMALLER || toks == Tokenize.TOKENS.GREATER_EQ || 
            toks == Tokenize.TOKENS.SMALLER_EQ || toks == Tokenize.TOKENS.AND || 
            toks == Tokenize.TOKENS.OR || toks == Tokenize.TOKENS.O_BRACKET || 
            toks == Tokenize.TOKENS.C_BRACKET || toks == Tokenize.TOKENS.IDENTIFIER ||
            toks == Tokenize.TOKENS.EQUALS;
    }

    //== If statement ==
    private void parseIfStatement() {
        consume(Tokenize.TOKENS.IF);

        //Parse condition
        ArrayList<Map.Entry<Tokenize.TOKENS, String>> condition = new ArrayList<>();

        //Construct condition
        while (tokens.get(currentTokenIndex).getKey() != Tokenize.TOKENS.DO) {
            Map.Entry<Tokenize.TOKENS, String> toks = tokens.get(currentTokenIndex);
            if (toks.getKey() == Tokenize.TOKENS.EOF || toks.getKey() == Tokenize.TOKENS.ENDIF)
                error("Error: Improper if condition initalization");

            if (checkIfCondition(toks.getKey())) {
                condition.add(toks);
                currentTokenIndex++;
            } else
                error("Error: Improper if condition statement");
        }
        consume(Tokenize.TOKENS.DO);

        //Obtain condition
        Boolean ifCondition = parseTokensReturnBool(condition);
        //If the condition is true, then we parse the statement
        while (tokens.get(currentTokenIndex).getKey() != Tokenize.TOKENS.ENDIF) {
            if (ifCondition)
                parseStatement();
            else
                currentTokenIndex++;
        }
        consume(Tokenize.TOKENS.ENDIF);
    }

    //While statement
     private void parseWhileStatement() {
        consume(Tokenize.TOKENS.WHILE);

        //Parse condition
        ArrayList<Map.Entry<Tokenize.TOKENS, String>> condition = new ArrayList<>();

        //Construct condition
        while (tokens.get(currentTokenIndex).getKey() != Tokenize.TOKENS.DO) {
            Map.Entry<Tokenize.TOKENS, String> toks = tokens.get(currentTokenIndex);
            if (toks.getKey() == Tokenize.TOKENS.EOF || toks.getKey() == Tokenize.TOKENS.ENDWHILE)
                error("Error: Improper if condition initalization");

            if (checkIfCondition(toks.getKey())) {
                condition.add(toks);
                currentTokenIndex++;
            } else
                error("Error: Improper if condition statement");
        }
        consume(Tokenize.TOKENS.DO);

        //If the condition is true, then we parse the statement
        Boolean whileCondition = parseTokensReturnBool(new ArrayList<>(condition));
        while (whileCondition) {
            int whileIndex = currentTokenIndex;
            while (tokens.get(currentTokenIndex).getKey() != Tokenize.TOKENS.ENDWHILE)
                parseStatement();
            
            whileCondition = parseTokensReturnBool(new ArrayList<>(condition));
            if (whileCondition) //Jump to block if still true
                currentTokenIndex = whileIndex;
        }
        consume(Tokenize.TOKENS.ENDWHILE);
    }

    //This is so we can parse in conditional statements (if, while, for etc)
    public Boolean parseTokensReturnBool(ArrayList<Map.Entry<Tokenize.TOKENS, String>> tokens) {
        //Initialize the operator stack
        List<Object> stack = new ArrayList<>();

        //Parse the tokens
        while (!tokens.isEmpty()) {
            Map.Entry<Tokenize.TOKENS, String> token = tokens.remove(0); //Pops the stack and get return

            if (token.getKey() == Tokenize.TOKENS.NUMERICAL)
                stack.add(Integer.parseInt(token.getValue()));
            else if (token.getKey() == Tokenize.TOKENS.IDENTIFIER) { //For variables
                String varName = token.getValue();
                if (!variables.containsKey(varName)) {
                    error("Error: Variable '" + varName + "' has not been assigned a value");
                }
                stack.add(variables.get(varName));
            }
            else if (token.getKey() == Tokenize.TOKENS.SMALLER || token.getKey() == Tokenize.TOKENS.GREATER
                    || token.getKey() == Tokenize.TOKENS.EQUALS) {
                int left = (Integer)stack.remove(stack.size() - 1);
                int right = Integer.parseInt(tokens.remove(0).getValue());

                stack.add(token.getKey() == Tokenize.TOKENS.SMALLER ? left < right :
                          token.getKey() == Tokenize.TOKENS.GREATER ? left > right : left == right);
            }
            else if (token.getKey() == Tokenize.TOKENS.AND || token.getKey() == Tokenize.TOKENS.OR) {
                if (stack.size() >= 1 && tokens.size() >= 1) {
                    boolean left  = (Boolean)stack.remove(stack.size() - 1);
                    Boolean right = parseTokensReturnBool(tokens);

                    stack.add(token.getKey() == Tokenize.TOKENS.AND ? left && right : left || right);
                }
            }
            else if (token.getKey() == Tokenize.TOKENS.O_BRACKET)
                stack.add(parseTokensReturnBool(tokens));
            else if (token.getKey() == Tokenize.TOKENS.C_BRACKET)
                break;
        }

        return stack.isEmpty() ? null : (Boolean)stack.remove(stack.size() - 1);
    }

    //Match
    private boolean match(Tokenize.TOKENS expected) {
        if (currentTokenIndex >= tokens.size()) {
            return false;
        }
        return tokens.get(currentTokenIndex).getKey() == expected;
    }

    //Throws exception when there is any sort of interpreted error
    private void error(String message) {
        throw new RuntimeException(message);
    }
}