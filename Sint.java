package interpreter;

//Sint.java
//Interpreter for S
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
		if (s instanceof DoWhile)
			return Eval((DoWhile)s, state);
		if (s instanceof For)
			return Eval((For)s, state);
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
	    throw new IllegalArgumentException("no statement");
	}

	State Eval(Empty s, State state) {
		return state;
	}

	State Eval(Assignment a, State state) {
		if(a.ar != null) {
			//배열 이라면,
			//id, Value[]
			//ValueArray 갖고 오기.
			
			Value v = state.get(a.ar.id);
			Value[] varr = v.arrValue();
			
			//arr의 인덱스 참조하기.
			Value idxtemp = V(a.ar.expr,state);
			int idx = idxtemp.intValue();
			 
			//varr에 값을 다시 넣어주고, 스택에 넣어주기.
			varr[idx] = V(a.expr,state); 
			return state.set(a.ar.id, new Value(varr));
		}
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
	
	State Eval(DoWhile d, State state) {
		state = Eval(d.stmt, state);
		Eval(new While(d.expr,d.stmt),state);
		return state;
	}
	
	State Eval(For f, State state) {
		State s=allocate(f.decls,state);
		while(V(f.expr1, state).boolValue()) {
			Assignment as=new Assignment(f.id, f.expr2);
			Eval(f.stmt, state);
			s=Eval(as,state);
		}
		return free(f.decls, s);
	}
	
	State Eval(Let l, State state) {
		State s = allocate(l.decls, state);
		s = Eval(l.stmts,s);
		return free(l.decls, s);
	}

	State allocate (Decls ds, State state) {
		if(ds != null) {
			for(Decl d:ds) {
				if(d.arraysize!=0) {
					//만약 d가 배열이라면,
					//push에는 id, Value,
					Value[] v = new Value[d.arraysize];
					state.push(d.id,new Value(v));
					
				}
				else {
					state.push(d.id,V(d.expr, state));
				}
			}
		}
		return state;
	}

	State free (Decls ds, State state) {
		if(ds!=null) {
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
	    	return new Value(v1.intValue() + v2.intValue());
	    case "-": 
	    	return new Value(v1.intValue() - v2.intValue());
	    case "*": 
	    	return new Value(v1.intValue() * v2.intValue());
	    case "/":
	    	return new Value(v1.intValue() / v2.intValue());
	    case "==":
	    	if(v1.type==Type.STRING && v2.type==Type.STRING) return new Value(v1.stringValue().equals(v2.stringValue()));
	    	else if(v1.type==Type.INT && v2.type==Type.INT) return new Value(v1.intValue() == v2.intValue());
	    case "!=":
	    	if(v1.type==Type.STRING && v2.type==Type.STRING) return new Value(v1.stringValue().equals(v2.stringValue()));
	    	else if(v1.type==Type.INT && v2.type==Type.INT) return new Value(v1.intValue() != v2.intValue());
	    case "<":
	    	return new Value(v1.intValue() < v2.intValue());
	    case ">":
	    	return new Value(v1.intValue() > v2.intValue());
	    case "<=":
	    	return new Value(v1.intValue() <= v2.intValue());
	    case ">=":
	    	return new Value(v1.intValue() >= v2.intValue());
	    case "&":
	    	return new Value(v1.boolValue() && v2.boolValue());
	    case "|":
	    	return new Value(v1.boolValue() || v2.boolValue());
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
        
        if(e instanceof Array) {
        	// id[idx] = <expr>
        	//에서 id[idx]부분.
        	Array ar = (Array) e;
        	Value v = (Value)(state.get(ar.id));
        	//value(value[])
        	Value[] Varr = v.arrValue();
        	
        	//인덱스계산
        	Value idxtemp = V(ar.expr,state);
        	int idx = idxtemp.intValue();
        	
        	return Varr[idx];
        	
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
		            //command.type = TypeChecker.Check(command);    
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