// Sint.java
// Interpreter for S
package interpreter;
import java.util.Scanner;

public class Sint {
    static Scanner sc = new Scanner(System.in);
    static State state = new State();

    State Eval(Command c, State state) { 
	if (c instanceof Decl) {
	    Decls decls = new Decls();
	    decls.add((Decl) c);
	    return allocate(decls, state);
	}
	
	if (c instanceof Stmt)
	    return Eval((Stmt) c, state); 
		
	    throw new IllegalArgumentException("no command");
    }
  
    State Eval(Stmt s, State state) {
        if (s instanceof Empty) 
	        return Eval((Empty)s, state);
        if (s instanceof Assignment)  
	        return Eval((Assignment)s, state);
        if (s instanceof If)  
	        return Eval((If)s, state);
        if (s instanceof While)  
	        return Eval((While)s, state);
        if (s instanceof Stmts)  
	        return Eval((Stmts)s, state);
	    if (s instanceof Let)  
	        return Eval((Let)s, state);
	    if (s instanceof Read)  
	        return Eval((Read)s, state);
	    if (s instanceof Print)  
	        return Eval((Print)s, state);
        if (s instanceof Call) 
	        return Eval((Call)s, state);
	    if (s instanceof Return) 
	        return Eval((Return)s, state);
	    if (s instanceof For)
	    	return Eval((For)s, state);
	    if (s instanceof Dowhile)
	    	return Eval((Dowhile)s,state);
        throw new IllegalArgumentException("no statement");
    }

    State Eval(Empty s, State state) {
        return state;
    }
  
    State Eval(Assignment a, State state) {
        Value v = V(a.expr, state);
	    return state.set(a.id, v);
    }

    State Eval(Read r, State state) {
	    int i = sc.nextInt();
	    state.set(r.id, new Value(i));
	/*
        if (r.id.type == Type.INT) {
	        int i = sc.nextInt();
	        state.set(r.id, new Value(i));
	    } 

	    // input integer, boolean, string value 
	*/
	    return state;
    }

    State Eval(Print p, State state) {
	    System.out.println(V(p.expr, state));
        return state; 
    }
  
    State Eval(Stmts ss, State state) {
        for (Stmt s : ss.stmts) {
            state = Eval(s, state);
            if (s instanceof Return)  
                return state;
        }
        return state;
    }
  
    State Eval(If c, State state) {
        if (V(c.expr, state).boolValue( ))
            return Eval(c.stmt1, state);
        else
            return Eval(c.stmt2, state);
    }
 
    State Eval(While l, State state) {
        if (V(l.expr, state).boolValue( ))
            return Eval(l, Eval(l.stmt, state));
        else 
	        return state;
    }
    
    State Eval(Dowhile l, State state) {
    	state = Eval(l.stmt, state);
    	Eval(new While(l.expr,l.stmt),state);
        return state;
    }
    
    State Eval(For l, State state) {
    	//For(d,e,id,e2,s)
    	State s =allocate(l.decls,state);
    	for(;V(l.expr,state).boolValue();) {
    		Eval(l.stmt,state);
    		Assignment as = new Assignment(l.id,l.expr2);
    		s = Eval(as,s);
    	}
    	return free(l.decls,s);
    }

    State Eval(Let l, State state) {
        State s = allocate(l.decls, state);
        s = Eval(l.stmts,s);
	    return free(l.decls, s);
    }
    
    State allocate(Decl d, State state) {
    	state.push(d.id,(Value)d.expr);
    	return state;
    }
    
    State allocate (Decls ds, State state) {
    //
    // add entries for declared variables on the state
    //
    	if (ds != null) {
    		for(Decl d:ds) {
    			//decl의 아이디와 변수를 State에 추가 해야 함.
    			state.push(d.id,(Value)d.expr);
    		}
    	}    	
    	return state;
    }
    
    State free(Decl d,State state) {
    	state.pop();
    	return state;
    }

    State free (Decls ds, State state) {
    //
    // free the entries for declared variables from the state
    //
    	if (ds != null) {
    		for(Decl d:ds) {
    			state.pop();
    		}
    	}
        return state;
    }

    Value binaryOperation(Operator op, Value v1, Value v2) {
        check(!v1.undef && !v2.undef,"reference to undef value");
	    switch (op.val) {
	    case "+":
	    	if(v1.type() == Type.STRING && v2.type() == Type.STRING) {
	    		return new Value(v1.stringValue()+v2.stringValue());
	    	}
            return new Value(v1.intValue() + v2.intValue());
        case "-": 
            return new Value(v1.intValue() - v2.intValue());
        case "*": 
            return new Value(v1.intValue() * v2.intValue());
        case "/": 
            return new Value(v1.intValue() / v2.intValue());
	//
	// relational operations 
	//

    //
	// logical operations
	//
        //정수, 스트링, 관계 연산, 부울값의 논리 연산.
        case ">":
        	return new Value(v1.intValue() > v2.intValue());
        case ">=":
        	return new Value(v1.intValue() >= v2.intValue());
        case "<":
        	return new Value(v1.intValue() < v2.intValue());
        case "<=":
        	return new Value(v1.intValue() <= v2.intValue());
        case "==":
        	return new Value(v1.intValue() == v2.intValue());
        case "!=":
        	return new Value(v1.intValue() != v2.intValue());

	    default:
	        throw new IllegalArgumentException("no operation");
	    }
    } 
    
    Value unaryOperation(Operator op, Value v) {
        check( !v.undef, "reference to undef value");
	    switch (op.val) {
        case "!": 
            return new Value(!v.boolValue( ));
        case "-": 
            return new Value(-v.intValue( ));
        default:
            throw new IllegalArgumentException("no operation: " + op.val); 
        }
    } 

    static void check(boolean test, String msg) {
        if (test) return;
        System.err.println(msg);
    }

    Value V(Expr e, State state) {
    	
        if (e instanceof Value) 
            return (Value) e;

        if (e instanceof Identifier) {
	        Identifier v = (Identifier) e;
            return (Value)(state.get(v));
	    }

        if (e instanceof Binary) {
            Binary b = (Binary) e;
            Value v1 = V(b.expr1, state);
            Value v2 = V(b.expr2, state);
            return binaryOperation (b.op, v1, v2); 
        }

        if (e instanceof Unary) {
            Unary u = (Unary) e;
            Value v = V(u.expr, state);
            return unaryOperation(u.op, v); 
        }

        if (e instanceof Call) 
    	    return V((Call)e, state);  
        throw new IllegalArgumentException("no operation");
    }

    public static void main(String args[]) {
	    if (args.length == 0) {
	        Sint sint = new Sint(); 
			Lexer.interactive = true;
            System.out.println("Language S Interpreter 2.0");
            System.out.print(">> ");
	        Parser parser  = new Parser(new Lexer());

	        do { // Program = Command*
	            if (parser.token == Token.EOF)
		            parser.token = parser.lexer.getToken();
	       
	            Command command=null;
                try {
	                command = parser.command();
                    // command.display(0);    // display AST    
	                // System.out.println("\nType checking...");
                    //command.type = TypeChecker.Check(command); 
                    // System.out.println("\nType: "+ command.type); 
                } catch (Exception e) {
                    System.out.println(e);
		            System.out.print(">> ");
                    continue;
                }

	            if (command.type != Type.ERROR) {
                    // System.out.println("\nInterpreting..." );
                    try {
                        state = sint.Eval(command, state);
                    } catch (Exception e) {
                         System.err.println(e);  
                    }
				    // System.out.println("\nFinal State");
                    // state.display( );
                }
		    System.out.print(">> ");
	        } while (true);
	    }
        else {
	        System.out.println("Begin parsing... " + args[0]);
	        Command command = null;
	        Parser parser  = new Parser(new Lexer(args[0]));
	        Sint sint = new Sint();

	        do {	// Program = Command*
	            if (parser.token == Token.EOF)
                    break;
	         
                try {
		            command = parser.command();
		             //  command.display(0);      // display AST
		             // System.out.println("\nType checking..." + args[0]);
                     // command.type = TypeChecker.Check(command);    
                     // System.out.println("\nType: "+ command.type);  
                } catch (Exception e) {
                    System.out.println(e);
                    continue;
                }

	            if (command.type!=Type.ERROR) {
                    System.out.println("\nInterpreting..." + args[0]);
                    try {
                        state = sint.Eval(command, state);
                    } catch (Exception e) {
                        System.err.println(e);  
                    }
					// System.out.println("\nFinal State");
                    // state.display( );
                }
	        } while (command != null);
        }        
    }
}