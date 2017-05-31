import java.util.ArrayList;
import java.util.Stack;
import java.util.ListIterator;
import java.util.Iterator;

public class Parser {


	// tok is global to all these parsing methods;
	// scan just calls the scanner's scan method and saves the result in tok.
	private Token tok; // the current token
	private SymbolTable t = new SymbolTable();
	public class SymbolTable{
		private Stack<ArrayList<Symbol>> myStack;
		private int scope;

		public class Symbol{
			private String sym;
			public Symbol(String s){
				this.sym = s;
			}
			public String getSym(){
				return sym;
			}
			public Boolean checkSym(String s){
				if (this.getSym().equals(s))
				{
					return true;
				}
				else
					return false;
			}
		}
		
		public SymbolTable(){
			scope = -1;
			myStack = new Stack<ArrayList<Symbol>>();
		}
		
		public int getScope(){
			return scope;
		}
		
		public void pushTable(){
			scope++;
			ArrayList<Symbol> temp = new ArrayList<Symbol>();
			myStack.push(temp);
		}
		
		public void popTable(){
			scope--;
			myStack.pop();
		}
		
		public boolean newSymbol(String s){
			Symbol x = expBlock(s);
			if (x == null){
				Symbol y = new Symbol(s);
				myStack.peek().add(y);
				return true;
			}
			System.err.println("redeclaration of variable " + s);
			return false;
		}
		
		public Symbol explore(String s){
			Symbol n;
			ListIterator<ArrayList<Symbol>> temp = myStack.listIterator(myStack.size());
			while(temp.hasPrevious()){
				n = null;
				ArrayList<Symbol> temp2 = temp.previous();
				for (Symbol x : temp2){
					if (x.checkSym(s) == true)
					{
						return x;
					}
				}
			}
			n = null;
			return n;
		}
		
		public Symbol expBlock(String s){
			Symbol n = null;
			ArrayList<Symbol> temp = myStack.peek();
			for (Symbol x : temp){
				if (x.checkSym(s) == true)
				{
					return x;
				}
			}
			return n;
		}
		
		public Symbol expScope(String s, int x){
			int y = 0;
			Symbol n = null;
			if(x > this.getScope())
			{
				return n;
			}
			if (x != 0)
			{
				y = this.getScope() - x;
			}
			ArrayList<Symbol> temp = myStack.elementAt(y);
			for (Symbol z : temp){
				if (z.checkSym(s) == true)
				{
					return z;
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

	private void declaration() {
		mustbe(TK.DECLARE);
		if(is(TK.ID)){
			if(t.expBlock(tok.string) != null)
			{
				vError(tok.string);
				
			}
			else
			{
				t.newSymbol(tok.string);
			}
		}
		mustbe(TK.ID);
		while( is(TK.COMMA) ) {
			mustbe(TK.COMMA);
			if (is(TK.ID))
			{
				if(t.expBlock(tok.string) != null)
				{
					vError(tok.string);
				}
				else
				{
					t.newSymbol(tok.string);
				}
			}
			mustbe(TK.ID);
		}
	}

	private void statement_list() {
		while( is(TK.DO) || is(TK.ID) || is(TK.IF) || is(TK.TILDE) || is(TK.PRINT) ) {
			statement();
		}
	}
	
	private void statement() {
		if ( is(TK.TILDE) || is(TK.ID) ) {
			assignment();
		}
		else if ( is(TK.PRINT) ) {
			print();
		}
		else if ( is(TK.IF) ) {
			if1();
		}
		else if ( is(TK.DO) ) {
			do1();
		}
		else
			parse_error("statement");												
	}    
	
	private void print() {
		mustbe(TK.PRINT);
		expr();
	}
	
	private void assignment() {
		ref_id();
		mustbe(TK.ASSIGN);
		expr();
	}    

	private void ref_id() {
		int s = -5;
		if( is(TK.TILDE) ) {
			mustbe(TK.TILDE);
			if( ! is(TK.NUM))
				{
					s = 0;
				}
			else
			{
				s = Integer.parseInt(tok.string);
				mustbe(TK.NUM);
				if(s == 0)
				{
					s = -5;
				}
				
			}	
		}
		if (is(TK.ID))
		{
			if (s == -5)
			{
				if (t.explore(tok.string) == null)
				{
					sError(tok.string + " is an undeclared variable on line "
						+ tok.lineNumber);
					System.exit(1);
				}
			}
			else if(s != -5)
			{
				
				if (t.expScope(tok.string, s) == null)
				{
					if (s != 0)
					{
						sError("no such variable ~" + s + tok.string +
							" on line " + tok.lineNumber);
					}
					else
					{
						sError("no such variable ~" + tok.string +
							" on line " + tok.lineNumber);
					}
				}
			}
		}
		mustbe(TK.ID);
	}
	
	private void do1() {
		mustbe(TK.DO);
		guarded_command();
		mustbe(TK.ENDDO);
	}
	
	private void if1() { 
		mustbe(TK.IF);           
		guarded_command();
		while( is(TK.ELSEIF) ) {
			mustbe(TK.ELSEIF);
			guarded_command();
		}
		if( is(TK.ELSE) ) {
			mustbe(TK.ELSE);
			block();
		}
		mustbe(TK.ENDIF);
	}
	
	private void guarded_command() {            
		expr();
		mustbe(TK.THEN);
		block();
	}   
	
	private void expr() {  
		term();          
		while ( is(TK.PLUS) || is(TK.MINUS) ) {
			addop();
			term();
		}
	}
	
	private void term() {  
		factor();
		while( is(TK.TIMES) || is(TK.DIVIDE) ) {
			multop();
			factor();
		}
	}		
	
	private void factor() {  
		if( is(TK.LPAREN) ) {
			mustbe(TK.LPAREN);
			expr();
			mustbe(TK.RPAREN);
		}
		else if( is(TK.TILDE) || is(TK.ID) )
			ref_id();
		else if( is(TK.NUM) )
			mustbe(TK.NUM);
		else
			parse_error("factor");
	}

	private void addop() {  
		if( is(TK.PLUS) || is(TK.MINUS))
			scan();
		else
			parse_error("addop");
	}
	
	private void multop() {  
		if( is(TK.TIMES) || is(TK.DIVIDE))
			scan();
		else
			parse_error("multop");				
	}

	private void vError(String s){
		System.err.println("redeclaration of variable " + s);
	}
	
	private void sError(String s){
		System.err.println(s);
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