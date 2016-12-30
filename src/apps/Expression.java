package apps;

import java.io.IOException;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.StringTokenizer;

import structures.Stack;

public class Expression {

	/**
	 * Expression to be evaluated
	 */
	String expr;                
    
	/**
	 * Scalar symbols in the expression 
	 */
	ArrayList<ScalarSymbol> scalars;   
	
	/**
	 * Array symbols in the expression
	 */
	ArrayList<ArraySymbol> arrays;
    
	/**
	 * Positions of opening brackets
	 */
	ArrayList<Integer> openingBracketIndex; 
    
	/**
	 * Positions of closing brackets
	 */
	ArrayList<Integer> closingBracketIndex; 

    /**
     * String containing all delimiters (characters other than variables and constants), 
     * to be used with StringTokenizer
     */
    public static final String delims = " \t*+-/()[]";
    
    /**
     * Initializes this Expression object with an input expression. Sets all other
     * fields to null.
     * 
     * @param expr Expression
     */
    public Expression(String expr) {
        this.expr = expr;
        scalars = null;
        arrays = null;
        openingBracketIndex = null;
        closingBracketIndex = null;
    }

    /**
     * Matches parentheses and square brackets. Populates the openingBracketIndex and
     * closingBracketIndex array lists in such a way that closingBracketIndex[i] is
     * the position of the bracket in the expression that closes an opening bracket
     * at position openingBracketIndex[i]. For example, if the expression is:
     * <pre>
     *    (a+(b-c))*(d+A[4])
     * </pre>
     * then the method would return true, and the array lists would be set to:
     * <pre>
     *    openingBracketIndex: [0 3 10 14]
     *    closingBracketIndex: [8 7 17 16]
     * </pe>
     * 
     * See the FAQ in project description for more details.
     * 
     * @return True if brackets are matched correctly, false if not
     */
    public boolean isLegallyMatched() {
    	openingBracketIndex = new ArrayList<Integer>();
    	closingBracketIndex = new ArrayList<Integer>();
    	int oI = 0;  				//opening index
    	int cI = 0;					//closing index
    	for (int i = 0; i < expr.length(); i++){
    		if (expr.charAt(i) == '(' || expr.charAt(i) == '['){
    			openingBracketIndex.add(oI, i);
    			oI++;
    		}
    		else if (expr.charAt(i) == ')' || expr.charAt(i) == ']'){
    			closingBracketIndex.add(cI, i);
    			cI++;
    		}
    		else{
    			//continue
    		}
    	}
    	if (openingBracketIndex.size() != closingBracketIndex.size()){
    		return false;
    	}
    	int index = 0;
    	while(oI < openingBracketIndex.size()){
    		if (expr.charAt(openingBracketIndex.get(index)) == '('){
    			if (expr.charAt(closingBracketIndex.get(index)) != ')'){
    				return false;
    			}
    			else {
    				index++;
    			}
    		}
    		else if (expr.charAt(openingBracketIndex.get(index)) == '['){
    			if(expr.charAt(closingBracketIndex.get(index)) != ']'){
    				return false;
    			}
    			else{
    				index++;
    			}
    		}
    		else{
    			//continue
    		}
    	}
    	return true;
    }

    /**
     * Populates the scalars and arrays lists with symbols for scalar and array
     * variables in the expression. For every variable, a SINGLE symbol is created and stored,
     * even if it appears more than once in the expression.
     * At this time, the constructors for ScalarSymbol and ArraySymbol
     * will initialize values to zero and null, respectively.
     * The actual values will be loaded from a file in the loadSymbolValues method.
     */
    public void buildSymbols() {
    	StringTokenizer tokenizer = new StringTokenizer(expr, delims, true); //returns delims as tokens
    	String[] tokens = new String[expr.length()];
    	arrays = new ArrayList<ArraySymbol>();
    	scalars = new ArrayList<ScalarSymbol>();
    	int i = 0;
    	while (tokenizer.hasMoreTokens()){
    		tokens[i] = tokenizer.nextToken();
    		i++;
    	}
    	for (int j = 0; j < tokens.length; j++){
    		if (isArrayVar(j, tokens)){	
    			if (!arrayVarExists(tokens[j])){									//variable already exists in arraylist
    				arrays.add(new ArraySymbol(tokens[j])); 							
    			}
    		}
    		else if (isScalarVar(j, tokens)) {
    			if (!scalarVarExists(tokens[j])){									//variable already exists in arraylist
    				scalars.add(new ScalarSymbol(tokens[j])); 
    			}	
    		}
    	}
    	//printArrays();
    	//printScalars();
    }
    
    /**
     * Loads values for symbols in the expression
     * 
     * @param sc Scanner for values input
     * @throws IOException If there is a problem with the input 
     */
    public void loadSymbolValues(Scanner sc) 
    throws IOException {
        while (sc.hasNextLine()) {
            StringTokenizer st = new StringTokenizer(sc.nextLine().trim());
            int numTokens = st.countTokens();
            String sym = st.nextToken();
            ScalarSymbol ssymbol = new ScalarSymbol(sym);
            ArraySymbol asymbol = new ArraySymbol(sym);
            int ssi = scalars.indexOf(ssymbol);
            int asi = arrays.indexOf(asymbol);
            if (ssi == -1 && asi == -1) {
            	continue;
            }
            int num = Integer.parseInt(st.nextToken());
            if (numTokens == 2) { // scalar symbol
                scalars.get(ssi).value = num;
            } else { // array symbol
            	asymbol = arrays.get(asi);
            	asymbol.values = new int[num];
                // following are (index,val) pairs
                while (st.hasMoreTokens()) {
                    String tok = st.nextToken();
                    StringTokenizer stt = new StringTokenizer(tok," (,)");
                    int index = Integer.parseInt(stt.nextToken());
                    int val = Integer.parseInt(stt.nextToken());
                    asymbol.values[index] = val;              
                }
            }
        }
    }
    
    /**
     * Evaluates the expression, using RECURSION to evaluate subexpressions and to evaluate array 
     * subscript expressions. (Note: you can use one or more private helper methods
     * to implement the recursion.)
     * 
     * @return Result of evaluation
     */
    public float evaluate() {
    		Stack<String> operators = new Stack<String>();
    		Stack<Float> operands = new Stack<Float>();
    		Expression copy = new Expression(copy(expr));
    		copy.isLegallyMatched();
    		copy.scalars = scalars;
    		copy.arrays = arrays;
    		return eval(copy.expr, operators, operands);
    }
    	

    /**
     * Utility method, prints the symbols in the scalars list
     */
    public void printScalars() {
        for (ScalarSymbol ss: scalars) {
            System.out.println(ss);
        }
    }
    
    /**
     * Utility method, prints the symbols in the arrays list
     */
    public void printArrays() {
    	for (ArraySymbol as: arrays) {
    		System.out.println(as);
    	}
    }
    
    private boolean onlyNumbers(String s){
    	if (s == null){
    		return false;
    	}
    	if (s.matches("[0-9]+") || s.contains(".")){
    		return true;
    	}
    	else{
    		return false;
    	}
    }
    
    private boolean scalarVarExists(String name){
    	for (ScalarSymbol ss: scalars){
    		if (ss.name.equals(name)){
    			return true;
    		}
    	}
    	return false;
    }
    
    private boolean arrayVarExists(String name){
    	for (ArraySymbol as: arrays){
    		if (as.name.equals(name)){
    			return true;
    		}
    	}
    	return false;
    }
    
    
    private boolean hasOnlyLetters(String name) {
    	if (name == null){
    		return false;
    	}
        return name.matches("[a-zA-Z]+");
    }
    
    private boolean hasPrecedence(String current, String other){
    		//if (current.equals("+") || current.equals("-")){
    			
    			//if (other.equals("*") || other.equals("/")){
    			//	return false;
    			//}
    		//}
    	
    	if (other.equals("*") || other.equals("/")){
				return false;
			}
    	return true;
    	//return true;
    	
    	
    }
    
    private boolean isScalarVar(int index, String[] tokens){
    	if (!hasOnlyLetters(tokens[index])){
    		return false;
    	}
    	if (index + 1 < tokens.length - 1){
    		if (tokens[index + 1] != null){
    			if (tokens[index + 1].equals("[")){
    			return false;
    			}	
    		}
    			
    	}
    	return true;
    }
    
    private boolean isArrayVar(int index, String[] tokens){
    	if (!hasOnlyLetters(tokens[index])){
    		return false;
    	}
    	if (index + 1 < tokens.length - 1 ){
    		if (tokens[index + 1] != null){
    			if (tokens[index + 1].equals("[")){
    				return true;
    			}
    		}
    	}
    	return false;
    }
    
    private float getScalarVal(String token){
    	for (int i = 0; i < scalars.size(); i++){
    		if (scalars.get(i).name.equals(token)){
    			return (float)scalars.get(i).value;
    		}
    	}
    	return 0;
    	
    }
    
    private float getArrayVal(String token, int index){
    	for (int i = 0; i < arrays.size(); i++){
    		if (arrays.get(i).name.equals(token)){
    			return (float)arrays.get(i).values[index];
    		}
    	}
    	return 0;
    }
    
    private String copy(String s){
    	String c = "";
		for (int i = 0; i < s.length(); i++){
			c = c + s.charAt(i);
		}
		return c;
    }
    
    private float eval(String e, Stack<String> operators, Stack<Float> operands){
    	
    	StringTokenizer tokenizer = new StringTokenizer(e, delims, true);
    	String[] tokens = new String[e.length()];
    	int j = 0;
    	while (tokenizer.hasMoreTokens()){ //parses expr into tokens and stores in an array
    		
    		tokens[j] = tokenizer.nextToken();
    		j++;
    	}
    	for (int i = 0; i < tokens.length; i++){
    		if (tokens[i] == null || tokens[i].equals("")){
    			break;
    		}
    		if (onlyNumbers(tokens[i])){ //int or float1 + 1
    			operands.push(Float.parseFloat(tokens[i]));
    			
    		}
    		else if (isScalarVar(i, tokens)){
    			operands.push(getScalarVal(tokens[i]));
    		}
    		else if (isArrayVar(i, tokens)){
    			String name = tokens[i];
    			Stack<String> openB = new Stack<String>();
    			int closingIndex = 0;
    			for(int k = i + 1; k <= tokens.length; k++){ 
    				if (tokens[k].equals("[")){
    					openB.push("[");
    				}
    				if (tokens[k].equals("]")){
    					openB.pop();
    					if (openB.size() == 0){
    						closingIndex = k;
    						break;
    					}
    				}
    			}
    			while (i < closingIndex){
    				i++;
    			}
    			
    			operands.push(getArrayVal(name, (int)eval(e.substring(1 + openingB(e), closingB(e)), new Stack<String>(), new Stack<Float>())));
    			e = e.substring(closingB(e) + 1, e.length());
    		}
    		else if (tokens[i].equals("(")){
    			
    			Stack<String> openP = new Stack<String>();
    			int closingIndex = 0;
    			for(int k = i; k <= tokens.length; k++){ 
    				if (tokens[k].equals("(")){
    					openP.push("(");
    				}
    				if (tokens[k].equals(")")){
    					openP.pop();
    					if (openP.size() == 0){
    						closingIndex = k;
    						break;
    					}
    				}
    			}
    			while (i < closingIndex){
    				i++;
    			}
        		
        		operands.push(eval(e.substring(1 + openingP(e), closingP(e)), new Stack<String>(), new Stack<Float>()));
        		e = e.substring(closingP(e) + 1, e.length());
    		}
    		
    		else if (tokens[i].equals(" ") || tokens[i].equals("/t")){
    			
    		}
    		else{
    			if (operators.size() == 0){
    				operators.push(tokens[i]);
    			}
    			else if (hasPrecedence(tokens[i], operators.peek())){
    				operators.push(tokens[i]);
    			}
    			else {
    				
    				if (operators.peek().equals("*")){
    					operands.push((operands.pop()) * (operands.pop()));  
    					operators.pop();
    					operators.push(tokens[i]);
    				}
    				else if (operators.peek().equals("/")){
    					operands.push((1 / operands.pop()) * (operands.pop()));  
    					operators.pop();
    					operators.push(tokens[i]);
    				
    				}
    			}
    		}	
    	}
    	while (operators.size() > 0){
    		
			if (operators.peek().equals("*")){
				String tempOperator = operators.pop();
				if (operators.size() >= 1 && operators.peek().equals("/")){
					float tempOperand = operands.pop();
					operands.push((1 / operands.pop()) * operands.pop());
					operators.pop();
					operators.push(tempOperator);
					operands.push(tempOperand);
				}
				else{
					operators.push(tempOperator);
					operands.push(operands.pop() * (operands.pop())); 
					operators.pop();
				}
			}
			else if (operators.peek().equals("/")){
				String tempOperator = operators.pop();
				if (operators.size() >= 1 && operators.peek().equals("/")){
					float tempOperand = operands.pop();
					operands.push((1 / operands.pop()) * operands.pop());
					operators.pop();
					operators.push(tempOperator);
					operands.push(tempOperand);
				}
				else{
					operators.push(tempOperator);
					operands.push((1 / operands.pop()) * (operands.pop()));  
					operators.pop();
				}
			}
			else if (operators.peek().equals("+")){
				String tempOperator = operators.pop();
				if (operators.size() >= 1 && operators.peek().equals("-")){
					float tempOperand = operands.pop();
					operands.push((0 - operands.pop()) + operands.pop());
					operators.pop();
					operators.push(tempOperator);
					operands.push(tempOperand);
				}
				else{
					operators.push(tempOperator);
					operands.push(operands.pop() + (operands.pop()));  
					operators.pop();
				}
			}
			else {
				operands.push((0 - operands.pop()) + (operands.pop()));  
				operators.pop();
			}
		}
    	
    	return (operands.pop());
    }
    
    private int openingP(String expr){
    	int index = 0;
    	for (int i = 0; i < expr.length(); i++){
    		if (expr.charAt(i) == '('){
    			index = i;
    			break;
    		}
    	}
    	return index;
    }
    private int openingB(String expr){
    	int index = 0;
    	for (int i = 0; i < expr.length(); i++){
    		if (expr.charAt(i) == '['){
    			index = i;
    			break;
    		}
    	}
    	return index;
    }
    
    private int closingP(String expr){
    	Stack<Bracket> s = new Stack<Bracket>();
    	int index = 0;
    	
    	
    	for (int i = 0; i <= expr.length(); i++){
    		
    		if (expr.charAt(i) == '('){
    			s.push(new Bracket('(', i));
    		}
    		if (expr.charAt(i) == ')'){
    			s.pop();
    			index = i;
    			if (s.size() == 0){
        			break;
        		}
    		}
    		
    	}
    	return index;
    }
    
    private int closingB(String expr){
    	Stack<Bracket> s = new Stack<Bracket>();
    	int index = 0;
    	
    	
    	for (int i = 0; i <= expr.length(); i++){
    		
    		if (expr.charAt(i) == '['){
    			s.push(new Bracket('[', i));
    		}
    		if (expr.charAt(i) == ']'){
    			s.pop();
    			index = i;
    			if (s.size() == 0){
        			break;
        		}
    		}
    		
    	}
    	return index;
    }

}
