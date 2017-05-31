import java.util.*;
public class Parser {
	// tok is global to all these parsing methods;
	// scan just calls the scanner's scan method and saves the result in tok.
	private Token tok; // the current token
	private SymbolTable t = new SymbolTable();    
	
	public class Symbol {
            private String sym;
            public Symbol(String s) {
                this.sym = s;
            }
            String getSym() {
                return sym;
            }		
            public Boolean checkSym(String s){
                if (this.getSym().equals(s))
                {
                    return true;
                }
                else
                {
                    return false;
                }
            }
	}
	
	public class SymbolTable {

        private Stack<ArrayList<Symbol>> stack;
        private int scope;
	
        public SymbolTable(){
		scope = -1;
		stack = new Stack<ArrayList<Symbol>>();
            }
	
            public void pushTable() {
            	scope++;
				ArrayList<Symbol> newBlock = new ArrayList<Symbol>();
				stack.push(newBlock);
            }
	
            public void popTable() {
				scope--;
				stack.pop();
				System.out.println("}");
            }
	
        public int getScope() {
		return scope;
            }
	
        public boolean newSymbol(String s) {
		Symbol temp = expBlock(s);
		if(temp == null) {
			Symbol var = new Symbol(s);
			stack.peek().add(var);
			return true;	
			
		}
		System.err.println(" redeclaration of variable " + s);
		return false;
        }
	
            public Symbol explore(String s) {
		ListIterator<ArrayList<Symbol>> temp = stack.listIterator(stack.size());
		Symbol n;
		while(temp.hasPrevious()) {
			ArrayList<Symbol> temp2 = temp.previous();
			n = null;
			for(Symbol temp3 : temp2) {
				if( temp3.checkSym(s) == true ) {
					return temp3;
				}
			}
		}
		n = null;
		return n;
        }
	
        public Symbol expBlock(String s) {
		Symbol n = null;
		ArrayList<Symbol> temp = stack.peek();
		for (Symbol x : temp){
			if (x.checkSym(s) == true)
			{
				return x;
			}
		}
		return n;
            }
	
        public Symbol expScope(String s, int x) {
		int y = 0;
		Symbol n = null;
		if(x > scope)
			return n;
		if(x != -1)
			y = scope - x;
		ArrayList<Symbol> temp = stack.elementAt(y);
		for(Symbol temp2 : temp) {		
			if( temp2.checkSym(s) == true ) {
				return temp2;
			}
		}
		return n;
            }
	}
	private void scan() {
            tok = scanner.scan();
	}
	
	private Scan scanner;
	Parser(Scan scanner) {
            this.scanner = scanner;
            scan();
            program();
            if( tok.kind != TK.EOF )
                    parse_error("junk after logical end of program");
	}
	

	private void program() {  
            initProgram();
            block();
	}

	private void block(){
            t.pushTable();
            declaration_list();
            statement_list();
            t.popTable();
	}

	private void declaration_list() {
            // below checks whether tok is in first set of declaration.
            // here, that's easy since there's only one token kind in the set.
            // in other places, though, there might be more.
            // so, you might want to write a general function to handle that.
            while( is(TK.DECLARE) ) {
                    declaration();
            }
	}
	
	private void statement_list() {
            while( is(TK.TILDE) || is(TK.ID) || is(TK.PRINT) || is(TK.IF) | is(TK.DO) ) {
                statement();
            }
	}

	private void declaration() {
            System.out.print("int ");
            byte f = 1;
            mustbe(TK.DECLARE);
            if( is(TK.ID) ) {
                if( t.expBlock(tok.string) != null ) {
                    f = 0; 
                    System.err.println("redeclaration of variable " + tok.string);
                }     
                else {
					String b = t.getScope() + tok.string;
					System.out.print("x_" + b);
                    t.newSymbol(tok.string);
                }
            }
            mustbe(TK.ID);
            while( is(TK.COMMA) ) {
                mustbe(TK.COMMA);
                if( is(TK.ID) ) {
                        if( t.expBlock(tok.string) != null ) {
                            System.err.println("redeclaration of variable " + tok.string);
			    f = 0;
                        }
                        else {
                            t.newSymbol(tok.string);
                            if (f == 1) {
                                System.out.print(", ");   
                            }
							String b = t.getScope() + tok.string;
                            System.out.print("x_" + b); 
                        }
                }
                mustbe(TK.ID);
            }
            System.out.println(";");
	}
	
	private void statement() {
            if ( is(TK.TILDE) || is(TK.ID) ) {
                assignment();
            } 
            else if ( is(TK.PRINT) ) {
                print();
            }
            else if ( is(TK.DO) ) {
                do1();
            }
            else if ( is(TK.IF) ) {
                if1();
            }
			else
				parse_error("statement");
	}    
	
	private void print() {
        mustBePrint(TK.PRINT, "printf(\"%d\\n\", ");
        expr();
        System.out.println(");");
    }
	
	private void assignment() {
            ref_id();
            mustBePrint(TK.ASSIGN, " = ");
            expr();
            System.out.println(";");
	}    

	private void ref_id() {
            int s = -5; 
			int n = 1;
            byte f = 0;
            if( is(TK.TILDE) ) {
                    f = 1;
                    mustbe(TK.TILDE);
                    if( ! is(TK.NUM) ) {
							n = 0;
                            s = -1;
                    }
                    else {
                        s = Integer.parseInt(tok.string);
                        mustbe(TK.NUM);
						n = t.getScope();
                        if(s != 0)
                            n = n - s;		    
                    }
            }
            if( is(TK.ID) ) {
                if (s == -5){ 
					String b = tok.string + " is an undeclared variable on line " + tok.lineNumber ;
                    if( t.explore(tok.string) == null ) {
                        System.err.println(b);
                        System.exit(1);    						
                    }
                }
                else if (s != -5) {

                    if( t.expScope(tok.string, s) == null ) {
                        if(s != -1) 
							System.err.println("no such variable ~" + s + tok.string +
                                    " on line " + tok.lineNumber);
                        else
                            System.err.println("no such variable ~" + tok.string +
                                    " on line " + tok.lineNumber);
                        System.exit(1);																						
                    }
                }

                if(f == 0) {
					int i = 0;
                    while(true) {    
                        if(t.expScope(tok.string, i) == null)
                            i++;
                        else
                            break;
                    }
                    n = t.getScope() - i;
                }
            }
			String b = n + tok.string;
            System.out.print("x_" + b);
            mustbe(TK.ID);
	}
	
	private void initProgram(){
            System.out.println("#include <stdio.h>");
            System.out.println("int main(){ ");
	}
	
	
		
        private void do1() {
            mustBePrint(TK.DO, "while( 0 >= (");
            guarded_command();
            mustbe(TK.ENDDO);
        }
			
        private void if1() { 
            mustBePrint(TK.IF, "if( 0 >= (");
            guarded_command();
            while( is(TK.ELSEIF) ) {
                mustBePrint(TK.ELSEIF, "else if( 0 >= (");
                guarded_command();
            }
            if( is(TK.ELSE) ) {
                mustBePrint(TK.ELSE, "else {");
                block();
            }
            mustbe(TK.ENDIF);
        }
		
		private void guarded_command() {            
            expr();
            mustBePrint(TK.THEN, ")){");
            block();
        }    
					
        private void expr() {  
            term();          
            while ( is(TK.PLUS) || is(TK.MINUS) ) {
                addOp();
                term();
            }
        }

        private void term() {  
            factor();
            while( is(TK.TIMES) || is(TK.DIVIDE) ) {
                multOp();
                factor();
            }
        }		
		
		private void factor() {
            if( is(TK.LPAREN) ) {
                mustBePrint(TK.LPAREN, "(");
                expr();
                mustBePrint(TK.RPAREN, ")");
            }
            else if( is(TK.TILDE) || is(TK.ID) )
                ref_id();
            else if( is(TK.NUM) ){
                mustBePrint(TK.NUM, tok.string);
            }
			else
				parse_error("factor");
        }

        private void addOp() {  
            if( is(TK.PLUS) ){
				mustBePrint(TK.PLUS, " + ");
            }
            else if( is(TK.MINUS ) ){
				mustBePrint(TK.MINUS, " - ");
            }
			else
				parse_error("addOp");
        }
							
        private void multOp() {
            if( is(TK.TIMES) ){
                scan();
                System.out.print(" * ");
            }
            else if( is(TK.DIVIDE ) ){
				System.out.print("/");
                scan();
            }
			else
				parse_error("multOp");
        }		
									
        private void mustBePrint(TK tk, String s){
			if( ! is(tk) ) {
                System.err.println( "mustbe: want " + tk + ", got " +
                        tok);
                parse_error( "missing token (mustbe)" );
            }
            scan();
			System.out.print(s);
		}
										
        // is current token what we want?
        private boolean is(TK tk) {
            return tk == tok.kind;
        }
        // ensure current token is tk and skip over it.
        private void mustbe(TK tk) {
            if( ! is(tk) ) {
                System.err.println( "mustbe: want " + tk + ", got " +
                        tok);
                parse_error( "missing token (mustbe)" );
            }
            scan();
        }

        private void parse_error(String msg) {
            System.err.println( "can't parse: line "
                    + tok.lineNumber + " " + msg );
            System.exit(1);
        }
}