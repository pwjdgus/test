package interpreter;

//Parser.java
//Parser for language S

public class Parser {
	 Token token;          // current token 
	 Lexer lexer;
	 String funId = "";
	
	 public Parser(Lexer scan) { 
	     lexer = scan;		  
	     token = lexer.getToken(); // get the first token
	 }
	
	 private String match(Token t) {
	     String value = token.value();
	     if (token == t)
	         token = lexer.getToken();
	     else
	         error(t);
	     return value;
	 }
	
	 private void error(Token tok) {
	     System.err.println("Syntax error: " + tok + " --> " + token);
	     token=lexer.getToken();
	 }
	
	 private void error(String tok) {
	     System.err.println("Syntax error: " + tok + " --> " + token);
	     token=lexer.getToken();
	 }
	
	 public Command command() {
	 // <command> ->  <decl> | <function> | <stmt>
		    if (isType()) {
		        Decl d = decl();
		        return d;
		    }
	
		    if (token != Token.EOF) {
		        Stmt s = stmt();
	         return s;
		    }
		    return null;
	 }
	 
	 private Decl decl() {
		 // <decl>  -> <type> id [=<expr>]; 
		     Type t = type();
			    String id = match(Token.ID);
			    Decl d = null;
			    if(token == Token.LBRACKET) {
			    	match(Token.LBRACKET);
			    	Value v=literal();
			    	int arrsize=v.intValue();
			    	match(Token.RBRACKET);
			    	d=new Decl(id,t,arrsize);
			    }
			    else if (token == Token.ASSIGN) {
			        match(Token.ASSIGN);
			        Expr e = expr();
			        d = new Decl(id, t, e);
			    }
			    
			    else 
			    	d = new Decl(id, t);
		
			    match(Token.SEMICOLON);
			    return d;
		 }
	
	 private Decls decls () {
	 // <decls> -> {<decl>}
	     Decls ds = new Decls ();
		    while (isType()) {
		        Decl d = decl();
		        ds.add(d);
		    }
	     return ds;
	 }
	
	 private Type type () {
	 // <type>  ->  int | bool | void | string 
	     Type t = null;
	     switch (token) {
		    case INT:
	         t = Type.INT; break;
	     case BOOL:
	         t = Type.BOOL; break;
	     case VOID:
	         t = Type.VOID; break;
	     case STRING:
	         t = Type.STRING; break;
	     default:
		        error("int | bool | void | string");
		    }
	     match(token);
	     return t;       
	 }
	
	 private Stmt stmt() {
	 // <stmt> -> <stmts> | <assignment> | <ifStmt> | <whileStmt> | ...
	     Stmt s = new Empty();
	     switch (token) {
		    case SEMICOLON:
	         match(token.SEMICOLON); return s;
	     case LBRACE:			
		        match(Token.LBRACE);		
	         s = stmts();
	         match(Token.RBRACE);	
		        return s;
	     case IF: 	// if statement 
	         s = ifStmt(); return s;
	     case WHILE:      // while statement 
	         s = whileStmt(); return s;
	     case DO:
	    	 s = doWhileStmt(); return s;
	     case FOR:
	    	 s=forStmt(); return s;
	     case ID:	// assignment
	         s = assignment(); return s;
	    case LET:	// let statement 
         s = letStmt(); return s;
	    case READ:	// read statement 
         s = readStmt(); return s;
	    case PRINT:	// print statment 
         s = printStmt(); return s;
	     default:  
		        error("Illegal stmt"); return null; 
		    }
	 }
	
	 private Stmts stmts () {
	 // <stmts> -> {<stmt>}
	     Stmts ss = new Stmts();
		    while((token != Token.RBRACE) && (token != Token.END))
		        ss.stmts.add(stmt()); 
	     return ss;
	 }
	
	 private Let letStmt () {
		 // <letStmt> -> let <decls> in <stmts> end
		 match(Token.LET);	
	     Decls ds = decls();
		 match(Token.IN);
	     Stmts ss = stmts();
	     match(Token.END);	
	     match(Token.SEMICOLON);
	     return new Let(ds, null, ss);
	 }
	
	 private Read readStmt() {
		 // <readStmt> -> read id;
		 match(Token.READ);
		 Identifier id = new Identifier(match(Token.ID));
		 match(Token.SEMICOLON);
		 return new Read(id);
	 }
	
	 private Print printStmt() {
		 // <printStmt> -> print <expr>;
		 match(Token.PRINT);
		 Expr e = expr();
		 match(Token.SEMICOLON);
		 return new Print(e);
	 }
	
	 private Stmt assignment() {
	 // <assignment> -> id = <expr>;   
	     Identifier id = new Identifier(match(Token.ID));
	     
	     if(token==Token.LBRACKET) {
	            match(Token.LBRACKET);
	            Expr index = expr();
	            match(Token.RBRACKET);
	            Array arr = new Array(id,index);
	            match(Token.ASSIGN);
	            Expr e = expr();
	            match(Token.SEMICOLON);
	            return new Assignment(arr,e);
	        }
	     
	     match(Token.ASSIGN);
	     Expr e = expr();
	     match(Token.SEMICOLON);
	     return new Assignment(id, e);
	 }
	
	 private If ifStmt () {
	 // <ifStmt> -> if (<expr>) then <stmt> [else <stmt>]
	     match(Token.IF);
		    match(Token.LPAREN);
	     Expr e = expr();
		    match(Token.RPAREN);
	     match(Token.THEN);
	     Stmt s1 = stmt();
	     Stmt s2 = new Empty();
	     if (token == Token.ELSE){
	         match(Token.ELSE); 
	         s2 = stmt();
	     }
	     return new If(e, s1, s2);
	 }
	
	 private While whileStmt () {
	 // <whileStmt> -> while (<expr>) <stmt>
		 match(Token.WHILE);
		 match(Token.LPAREN);
		 Expr e = expr();
		 match(Token.RPAREN);
		 Stmt s = stmt();
	     return new While(e,s);
	 }
	 
	 private DoWhile doWhileStmt() {
		 // <doWhileStmt> -> do<stmt> while(<expr>);
		 match(Token.DO);
		 Stmt s=stmt();
		 match(Token.WHILE);
		 match(Token.LPAREN);
		 Expr e=expr();
		 match(Token.RPAREN);
		 match(Token.SEMICOLON);
		 return new DoWhile(s,e);
	 }
	 
	 private For forStmt() {
		 match(Token.FOR);
		 match(Token.LPAREN);
		 Decls d = decls();
		 Expr e1=expr();
		 match(Token.SEMICOLON);
		 Identifier id = new Identifier(match(Token.ID));
	     match(Token.ASSIGN);
	     Expr e2=expr();
		 match(Token.RPAREN);
		 Stmt stmt=stmt();
		 return new For(d, e1, id, e2, stmt);
	 }
	 
	 private Expr expr () {
	 // <expr> -> <bexp> {& <bexp> | '|'<bexp>} | !<expr>
	     switch (token) {
		    case NOT:
		        Operator op = new Operator(match(token));
		        Expr e = expr();
		        return new Unary(op, e);
	     }
	     Expr e = bexp();
	     while(token==Token.AND || token==Token.OR) {
	    	 Operator op=new Operator(match(token));
	    	 Expr b=bexp();
	    	 e=new Binary(op,e,b);
	     }
	     return e;
	 }
	
	 private Expr bexp() {
	     // <bexp> -> <aexp> [ (< | <= | > | >= | == | !=) <aexp> ] | true | false
		 switch (token) {
		     case TRUE:
		         match(Token.TRUE);
		         return new Value(true);
		     case FALSE:
		         match(Token.FALSE);
		         return new Value(false);
	     }
	     Expr e = aexp();
	     if(token==Token.EQUAL || token==Token.NOTEQ || token==Token.LT || token==Token.GT || token==Token.LTEQ || token==Token.GTEQ) {
	    	 Operator op=new Operator(match(token));
	    	 Expr b=aexp();
	    	 e=new Binary(op,e,b);
	     }
	     return e;
	 }
	
	 private Expr aexp () {
	     // <aexp> -> <term> { + <term> | - <term> }
	     Expr e = term();
	     while (token == Token.PLUS || token == Token.MINUS) {
	         Operator op = new Operator(match(token));
	         Expr t = term();
	         e = new Binary(op, e, t);
	     }
	     return e;
	 }
	
	 private Expr term () {
	     // <term> -> <factor> { * <factor> | / <factor>}
	     Expr t = factor();
	     while (token == Token.MULTIPLY || token == Token.DIVIDE) {
	         Operator op = new Operator(match(token));
	         Expr f = factor();
	         t = new Binary(op, t, f);
	     }
	     return t;
	 }
	
	 private Expr factor() {
	     // <factor> -> [-](id | <call> | literal | '('<aexp> ')')
	     Operator op = null;
	     if (token == Token.MINUS) 
	         op = new Operator(match(Token.MINUS));
	
	     Expr e = null;
	     switch(token) {
	     case ID:
	         Identifier v = new Identifier(match(Token.ID));
	         e = v;
	         if(token==Token.LBRACKET) {
	        	 match(Token.LBRACKET);
	        	 Expr e2=expr();
	        	 match(Token.RBRACKET);
	        	 e=new Array(v,e2);
	         }
	         break;
	     case NUMBER: case STRLITERAL: 
	         e = literal();
	         break; 
	     case LPAREN: 
	         match(Token.LPAREN); 
	         e = expr();       
	         match(Token.RPAREN);
	         break; 
	     default: 
	         error("Identifier | Literal"); 
	     }
	
	     if (op != null)
	         return new Unary(op, e);
	     else return e;
	 }
	
	
	 private Value literal( ) {
	     String s = null;
	     switch (token) {
	     case NUMBER:
	         s = match(Token.NUMBER);
	         return new Value(Integer.parseInt(s));
	     case STRLITERAL:
	         s = match(Token.STRLITERAL);
	         return new Value(s);
	     }
	     throw new IllegalArgumentException( "no literal");
	 }
	
	 private boolean isType( ) {
	     switch(token) {
	     case INT: case BOOL: case STRING: 
	         return true;
	     default: 
	         return false;
	     }
	 }
	 
	 public static void main(String args[]) {
		 Parser parser;
	     Command command = null;
		 	if (args.length == 0) {
		 		System.out.print(">> ");
		        Lexer.interactive = true;
		        parser  = new Parser(new Lexer());
		        do {
		            if (parser.token == Token.EOF) 
			        parser.token = parser.lexer.getToken();
	
	             try {
	                 command = parser.command();
			            if (command != null) command.display(0);    // display AST
	             } catch (Exception e) {
	                 System.err.println(e);
	             }
			        System.out.print("\n>> ");
		        } while(true);
		    }
	 	else {
		        System.out.println("Begin parsing... " + args[0]);
		        parser  = new Parser(new Lexer(args[0]));
		        do {
		            if (parser.token == Token.EOF) 
	                 break;
	
	             try {
			             command = parser.command();
			             if (command != null) command.display(0);      // display AST
	             } catch (Exception e) {
	                 System.err.println(e); 
	             }
		        } while (command != null);
		    }
	 } //main
} // Parser