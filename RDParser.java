package interpreter;
///*package web1;
//import java.io.*;
//
///**
// * Recursive Descent Parser of simple expression
// * 
// * EBNF of our grammar 
// * <command> -> <expr>\n 
// * <expr> -> <term> {+ <term>}
// *        -> <bexp> {& <bexp> | | <bexp>} | !<expr> | true | false 
// * <bexp> -> <aexp> [<relop> <aexp>]
// * <relop> -> == , != , <. > , <=, >=
// * 
// * <term> -> <factor> {* <factor>}
// * <factor> -> <number> | (<expr>)
// * <number> -> <digit>{<digit>}
// * <digit> -> 0|1|2|3|4|5|6|7|8|9
// * 
// * 
// * @author Jangwu Jo
// *
// */
//public class RDParser {
//	static int MAX = 987654321;
//	static Boolean is_relop = false;
//	static Boolean is_expr = false;
//	static Boolean is_cal = false;
//	static Boolean is_minus=false;
//	static Boolean is_digit=false;
//	int token, ch;
//	private PushbackInputStream input;
//	
//	RDParser(PushbackInputStream is) {
//        input = is;
//    }
//    
//    void error( ) {
//        System.out.printf("parse error : %d\n", token);
//        System.exit(1);
//    }
//
//    void command( ) {
//    /* command -> expr '\n' */
//        int val = expr();
//        if (token == '\n') {
//        	/* end the parse and print the result */
//        	System.out.println("good syntax");
//        	if(is_cal && val == MAX) {
//        		// 연산이 정수 , 불리언식 일 경우.
//        		System.out.println("정의되지 않음");
//        	}
//        	else if(is_expr){
//        		//불리언 식일 때,
//        		if(is_minus) {
//        			System.out.println("정의되지 않음");
//        			}
//        		else if(val == 1) {
//        			System.out.println("true");
//        		}
//        		else if(val==0){
//        			System.out.println("false");
//        		}
//        		else {
//        			System.out.println("정의되지 않음");
//        		}
//        	}
//        	else {
//        		System.out.println("값은 " + val+"입니다.");
//        	}
//        }
//        else error();
//    }
//
//    int match(int c) { 
//        int val = token;
//    	if (token == c) 
//        	token = getToken();
//        else error();
//        return val;
//    }
//    
//    int expr() {
//    /* expr -> term { '+' term } */
//    	int val = 0;
//    	is_expr = false;
//    	//expr이 true이면 불리언식,
//    	//false 이면 산술
//    	if(token=='!') {
//    		match('!');
//    		is_digit=false;
//    		val = expr();
//    		is_expr = true;
//    		if(val==1 && !is_digit) {
//    			return 0;
//    		}
//    		else if(val==0 && !is_digit) {
//    			return 1;
//    		}
//    		else {
//    			//정의 할 수 없음을 표현해야 함.
//    			//is_expr이 true 이고, 값이 임의의 정수를
//    			//리턴하면 될까..?
//    			return MAX;
//    		}
//    	}
//    	else if(token=='t') {
//    		is_expr = true;
//    		match('t');match('r');match('u');match('e');
//    		return 1;
//    	}
//    	else if(token=='f') {
//    		is_expr=true;
//    		match('f');match('a');match('l');match('s');match('e');
//    		return 0;
//    	}
//        else {
//        	//relop 이 돌아간다면 
//        	//값은 true , false 값이 여야 함.
//        	is_relop=false;
//        	val = bexp();
//        	Boolean is_val = false;
//        	if(is_relop) {
//        		is_val = true;
//        		is_expr = true;
//        	}
//        	
//        	if(token=='&' || token=='|') {
//	        	is_relop = false;
//	        	is_expr = true;
//        		int c=0;
//	        	while(token=='&' || token=='|') {
//	        		if(token=='&') {
//	        			c = match('&');
//	        		}
//	        		else {
//	        			c = match('|');
//	        		}
//	        		int b = bexp();
//	        		if(val == MAX && is_cal) {
//	        			//이미 정의 할 수 없는 값이라면
//	        			//연산을 진행할 필요 없음.
//	        			val = MAX;
//	        		}
//	        		else if(is_val && is_relop) {
//	        			//1. 둘 다 불리언 식일 경우.
//	        			if(c=='&') {
//	        				val = val&b;
//	        			}
//	        			else val = val|b;
//	        		}else if((is_val && !is_relop) && (!is_val && is_relop)) {//한 쪽만 불리언 식일 경우.
//	        			is_cal = true;
//	        			val = MAX;
//	        		}
//	        		else { // 3. 둘 다 정수를 연산 한 경우.
//	        			//다만 중간에 &,| 기호가 있어서 정의 할 수 없음.
//	        			is_cal = true;
//	        			val = MAX;
//	        		}
//	        	}
//        	}
//        }
//    	return val;
//    }
//    int bexp() {
//    	is_digit = false;
//    	int a = aexp();
//    	Boolean is_cal_possible=false;
//    	if(is_digit) {
//    		//숫자라면 계산이 가능하다.
//    		is_cal_possible=true;
//    	}
//    	if(token=='=' || token=='!' || token=='<' || token=='>') {
//    		// == != < > <= >=
//    		int cal = relop();
//    		is_relop = true;
//    		//== 1
//        	//!= 0
//        	//<= 2,3
//        	//>= 4,5
//    		is_digit = false;
//    		int b = aexp();
//    		if(is_digit!=is_cal_possible) {
//    			is_cal = true;
//    			return MAX;
//    		}
//    		switch(cal) {
//    		case 1:
//    			if(a==b) return 1;
//    			else return 0;
//    		case 0:
//    			if(a!=b) return 1;
//    			else return 0;
//    		case 2:
//    			if(a<b) return 1;
//    			else return 0;
//    		case 3:
//    			if(a<=b) return 1;
//    			else return 0;
//    		case 4:
//    			if(a>b) return 1;
//    			else return 0;
//    		case 5:
//    			if(a>=b) return 1;
//    			else return 0;
//    		}
//    	}	
//    	return a;
//    }
//    
//    int relop() {
//    	//== 1
//    	//!= 0
//    	//<= 2,3
//    	//>= 4,5
//    	if(token=='=') {
//    		//같다.
//    		match('=');match('=');
//    		return 1;
//    	}
//    	else if(token=='!') {
//    		//같지 않다.
//    		match('!'); match('=');
//    		return 0;
//    	}
//    	else if(token=='<') {
//    		match('<');
//    		if(token=='=') {
//    			match('=');
//    			return 3;
//    		}
//    		return 2;
//    	}
//    	else if(token == '>') {
//    		match('>');
//    		if(token=='=') {
//    			match('=');
//    			return 5;
//    		}
//    		return 4;
//    	}
//    	return -1;
//    }
//    
//    int aexp() {
//    	//덧셈 또는 뺄셈을 연산 후에 값을 리턴
//    	int a = term();
//    	while(token=='+' || token=='-') {
//    		int cal = 0;
//    		if(token=='+') cal = match('+');
//    		else cal =match('-');
//    		int b = term();
//    		if(cal=='+') a+=b;
//    		else a-=b;
//    	}
//    	return a;
//    }
//    
//    int term( ) {
//    /* term -> factor { '*' factor } */
//       //곱셈이나 나눗셈을 연산 후에 값을 리턴.
//    	
//    	int a = factor();
//       while (token == '*' || token=='/') {
//           int cal=0;
//    	   if(token=='*') {
//        	   cal = match('*');
//           }
//           else {
//        	   cal =match('/');
//           }
//           int b = factor();
//           if(cal=='*') {
//        	   a *=b;
//           }
//           else a/=b;
//       }
//       return a;
//    }
//
//    int factor() {
//    /* factor -> '(' expr ')' | number */
//       int val = 0;
//       is_minus = false;
//       if(token=='-') {
//    		match('-');
//    		is_minus=true;
//    	}
//    	if (token == '(') {
//    		match('(');
//            val = expr();
//            match(')');
//        }
//        else {
//            val = number(); 
//        }
//    	if(is_minus){
//    		val *=-1;
//    	}
//    	return val;
//    }
//
//    int number() {
//    	is_digit=true;
//    /* number -> digit{digit} */
//    	//digit에서 숫자 1을 아스키 값인 49로 넘겨 줌
//    	//여기서 문자열로 다 더해서, 숫자로 바꿔서 리턴.
//    	String val = Integer.toString(digit()-48);
//    	while (Character.isDigit(token)) {
//    		val+=(digit()-48);
//    	}
//    	return Integer.parseInt(val);
//    }
//    
//    int digit() {
//    /* digit -> 0|1|...|9 */	
//    	int val=0;
//    	if (Character.isDigit(token))
//    		val = match(token);
//    	else
//    		error();
//    	return val;
//    }
//    
//	int getToken() {
//        while(true) {
//            try  {
//	            ch = input.read();
//                if (ch == ' ' || ch == '\t' || ch == '\r') ;
//                else 
//                	return ch;
//	        } catch (IOException e) {
//                System.err.println(e);
//            }
//        }
//    }
//        
//    void parse( ) {
//        token = getToken(); // get the first character
//        command();          // call the parsing command
//    }
//    
//	public static void main(String[] args) { 
//		RDParser parser = new RDParser(new PushbackInputStream(System.in));
//        while(true) {
//            System.out.print(">> ");
//            parser.parse();
//        }
//    }
//
//}