/*
Each line is separated by new line ('\n')
*/
import java.util.ArrayList;
import java.util.HashMap;
import java.util.AbstractMap;
import java.util.Map;

class MainMenu {
    public static void main(String[] args) {
        //Main loop
        //Interpreter interp = new Interpreter("sds");
        Tokenize tokenizer = new Tokenize("set r0 to 13\nset r1 to r0\nadd r0 to r1\nprintln r1\nend");
    
        //Tokenize
        ArrayList<Map.Entry<Tokenize.TOKENS, String>> tokens = tokenizer.tokenize();

        //Parsing phase
        Parser parser = new Parser(tokens);

        parser.parse();
    }
}

//=== Interpreter class ===
class Interpreter {
    public String file;
    public Interpreter(String file) {
        this.file = file;
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
        NEWLINE,
        //Reserved keywords string values
        TO, FROM,
        SET, ADD, SUBTRACT,
        PRINT, PRINTLN,
        //Misc
        END, COMMENT
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
        tokenTypesKeywods.put("from", TOKENS.FROM);
        tokenTypesKeywods.put("println", TOKENS.PRINTLN);
        tokenTypesKeywods.put("print", TOKENS.PRINT);
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
        tokenTypes.put('\n', TOKENS.NEWLINE);

        //Store index
        int index = 0;

        //Must read each character, for this a while loop will make it easier within the tokenization
        //process so we can check the next tokens
        while (index < str.length()) {
            //Read char at index
            char ch = str.charAt(index);

            //Check if token is a number value
            if (Character.isDigit(ch)) {
                //Init numeric value string
                StringBuilder val = new StringBuilder();
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

        // //Print each token value (for debugging)
        // for (Map.Entry<TOKENS, String> t : tokens) {
        //     System.out.println(t.getKey().name() + " " + t.getValue());
        // }

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
            parsePrintln();
        }
        else if (match(Tokenize.TOKENS.ADD)) {
            parseAddition();
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
            value = variables.get(val);
        }

        //Generate an AST node for the assignment statement (Debugging)
        variables.put(identifier, value);
        System.out.println("Assign " + identifier + " = " + value);
    }

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

    private void parsePrintln() {
        consume(Tokenize.TOKENS.PRINTLN);
        String stdout = consume(Tokenize.TOKENS.IDENTIFIER).getValue();

        if (!variables.containsKey(stdout)) {
            error("Variable '" + stdout + "' has not been assigned a value");
        }

        System.out.println(variables.get(stdout));
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