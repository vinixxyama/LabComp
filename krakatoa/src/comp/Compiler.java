// Vinicius Yamamoto    490105


package comp;

import ast.*;
import lexer.*;
import java.io.*;
import java.util.*;

import com.sun.org.apache.xalan.internal.xsltc.compiler.util.VoidType;

public class Compiler {
	// compile must receive an input with an character less than
	// p_input.lenght
	public Program compile(char[] input, PrintWriter outError) {

		ArrayList<CompilationError> compilationErrorList = new ArrayList<>();
		signalError = new ErrorSignaller(outError, compilationErrorList);
		symbolTable = new SymbolTable();
		lexer = new Lexer(input, signalError);
		signalError.setLexer(lexer);

		Program program = null;
		lexer.nextToken();
		program = program(compilationErrorList);
		return program;
	}

	private Program program(ArrayList<CompilationError> compilationErrorList) {
		// Program ::= KraClass { KraClass }
		ArrayList<MetaobjectCall> metaobjectCallList = new ArrayList<>();
		ArrayList<KraClass> kraClassList = new ArrayList<>();
		Program program = new Program(kraClassList, metaobjectCallList, compilationErrorList);
		try {
			while ( lexer.token == Symbol.MOCall ) {
				metaobjectCallList.add(metaobjectCall());
			}
			kraClassList.add(classDec());
			while ( lexer.token == Symbol.CLASS )
				kraClassList.add(classDec());
			if(symbolTable.getInGlobal("Program") == null) {
				signalError.showError("Source code without a class 'Program'");
			}
			if ( lexer.token != Symbol.EOF ) {
				signalError.showError("End of file expected");
			}
		}
		 catch( CompilerError e) {
	            // if there was an exception, there is a compilation signalError
	        }
	        catch ( RuntimeException e ) {
	            e.printStackTrace();
	        }
	        return program;
	}

	/**  parses a metaobject call as <code>{@literal @}ce(...)</code> in <br>
     * <code>
     * @ce(5, "'class' expected") <br>
     * clas Program <br>
     *     public void run() { } <br>
     * end <br>
     * </code>
     * 
	   
	 */
	@SuppressWarnings("incomplete-switch")
	private MetaobjectCall metaobjectCall() {
		String name = lexer.getMetaobjectName();
		lexer.nextToken();
		ArrayList<Object> metaobjectParamList = new ArrayList<>();
		if ( lexer.token == Symbol.LEFTPAR ) {
			// metaobject call with parameters
			lexer.nextToken();
			while ( lexer.token == Symbol.LITERALINT || lexer.token == Symbol.LITERALSTRING ||
					lexer.token == Symbol.IDENT ) {
				switch ( lexer.token ) {
				case LITERALINT:
					metaobjectParamList.add(lexer.getNumberValue());
					break;
				case LITERALSTRING:
					metaobjectParamList.add(lexer.getLiteralStringValue());
					break;
				case IDENT:
					metaobjectParamList.add(lexer.getStringValue());
				}
				lexer.nextToken();
				if ( lexer.token == Symbol.COMMA ) 
					lexer.nextToken();
				else
					break;
			}
			if ( lexer.token != Symbol.RIGHTPAR ) 
				signalError.showError("')' expected after metaobject call with parameters");
			else
				lexer.nextToken();
		}
		if ( name.equals("nce") ) {
			if ( metaobjectParamList.size() != 0 )
				signalError.showError("Metaobject 'nce' does not take parameters");
		}
		else if ( name.equals("ce") ) {
			if ( metaobjectParamList.size() != 3 && metaobjectParamList.size() != 4 )
				signalError.showError("Metaobject 'ce' take three or four parameters");
			if ( !( metaobjectParamList.get(0) instanceof Integer)  )
				signalError.showError("The first parameter of metaobject 'ce' should be an integer number");
			if ( !( metaobjectParamList.get(1) instanceof String) ||  !( metaobjectParamList.get(2) instanceof String) )
				signalError.showError("The second and third parameters of metaobject 'ce' should be literal strings");
			if ( metaobjectParamList.size() >= 4 && !( metaobjectParamList.get(3) instanceof String) )  
				signalError.showError("The fourth parameter of metaobject 'ce' should be a literal string");
			
		}
			
		return new MetaobjectCall(name, metaobjectParamList);
	}

	private KraClass classDec() {
		// Note que os métodos desta classe não correspondem exatamente às
		// regras
		// da gramática. Este método classDec, por exemplo, implementa
		// a produção KraClass (veja abaixo) e partes de outras produções.

		/*
		 * KraClass ::= ``class'' Id [ ``extends'' Id ] "{" MemberList "}"
		 * MemberList ::= { Qualifier Member } 
		 * Member ::= InstVarDec | MethodDec
		 * InstVarDec ::= Type IdList ";" 
		 * MethodDec ::= Qualifier Type Id "("[ FormalParamDec ] ")" "{" StatementList "}" 
		 * Qualifier ::= [ "static" ]  ( "private" | "public" )
		 */
		if ( lexer.token != Symbol.CLASS ) signalError.showError("'class' expected");
		lexer.nextToken();
		if ( lexer.token != Symbol.IDENT )
			signalError.show(ErrorSignaller.ident_expected);
		String className = lexer.getStringValue();
		this.currentClass = new KraClass(className);
		symbolTable.putInGlobal(className, currentClass);
		lexer.nextToken();
		if ( lexer.token == Symbol.EXTENDS ) {
			lexer.nextToken();
			if ( lexer.token != Symbol.IDENT )
				signalError.show(ErrorSignaller.ident_expected);
			String superclassName = lexer.getStringValue();
			if(superclassName.equals(className)) {
				signalError.showError("Class '"+ className +"' is inheriting from itself");
			}
			
			KraClass superclass = symbolTable.getInGlobal(superclassName);
			if(superclass == null) {
				signalError.showError("Class '"+ superclassName +"' does not exist");
			}else {
				currentClass.setSuperclass(superclass);
			}
			lexer.nextToken();
		}
		if ( lexer.token != Symbol.LEFTCURBRACKET )
			signalError.showError("{ expected", true);
		lexer.nextToken();
		while (lexer.token == Symbol.PRIVATE || lexer.token == Symbol.PUBLIC) {
			Symbol qualifier;
			switch (lexer.token) {
			case PRIVATE:
				lexer.nextToken();
				qualifier = Symbol.PRIVATE;
				break;
			case PUBLIC:
				lexer.nextToken();
				qualifier = Symbol.PUBLIC;
				break;
			default:
				signalError.showError("private, or public expected");
				qualifier = Symbol.PUBLIC;
			}
			Type t = type();
			if ( lexer.token != Symbol.IDENT )
				signalError.showError("Identifier expected");
			String name = lexer.getStringValue();
			lexer.nextToken();
			
			if ( lexer.token == Symbol.LEFTPAR ) {
				if(name.equals("run") && qualifier == Symbol.PRIVATE) {
					signalError.showError("Method 'run' of class 'Program' cannot be private");
				}else{
					if(name.equals("run") && t != Type.voidType && currentClass.getCname().equals("Program")) {
						signalError.showError("Method 'run' of class 'Program' with a return value type different from 'void'");
					}
					methodDec(t, name, qualifier);
				}
			}else if ( qualifier != Symbol.PRIVATE ) {
				signalError.showError("Attempt to declare a public instance variable");
			}else {
				instanceVarDec(t, name);
			}
		}
		if ( lexer.token != Symbol.RIGHTCURBRACKET )
			signalError.showError("public/private or \"}\" expected");
		
		if(currentClass.getName().equals("Program") && currentClass.searchPublicMethod("run") == null)
			signalError.showError("Method 'run' was not found in class 'Program'.");
		
		lexer.nextToken();
		return currentClass;
	}

	private void instanceVarDec(Type type, String name) {
		// InstVarDec ::= [ "static" ] "private" Type IdList ";"
		InstanceVariable iv = new InstanceVariable(name, type);
		if(currentClass.getInstanceVariableList().getInstanceVar(name) == null){
			currentClass.getInstanceVariableList().addElement(iv);
		}else {
			signalError.showError("Variable '"+ name +"' is being redeclared");
		}
		
		while (lexer.token == Symbol.COMMA) {
			lexer.nextToken();
			if ( lexer.token != Symbol.IDENT )
				signalError.showError("Identifier expected");
			String variableName = lexer.getStringValue();
			lexer.nextToken();
		}
		if ( lexer.token != Symbol.SEMICOLON )
			signalError.show(ErrorSignaller.semicolon_expected);
		lexer.nextToken();
	}

	private void methodDec(Type type, String name, Symbol qualifier) {
		/*
		 * MethodDec ::= Qualifier Return Id "("[ FormalParamDec ] ")" "{"
		 *                StatementList "}"
		 */
		ArrayList<Statement> stlist = new ArrayList<>();
		ParamList plista = null;
		this.currentMethod = new MethodDec(name, type, qualifier, stlist, plista);
		if(currentClass.getSuperclass() != null) {
			if(currentClass.getSuperclass().searchPublicMethod(name) != null) {
				if(currentClass.getSuperclass().searchPublicMethod(name).getReturnType().getName() != currentMethod.getReturnType().getName()) {
					signalError.showError("Method '"+ name +"' of subclass '"+ currentClass.getCname() +"' has a signature different from method inherited from superclass '"+ currentClass.getSuperclass().getCname() +"'" );
				}
			}
		}
		
		if(currentClass.searchPublicMethod(name) != null) {
			signalError.showError("Method '"+ name +"' is being redeclared");
		}
		if(currentClass.getInstanceVariableList().getInstanceVar(name) != null) {
			signalError.showError("Method '"+ name +"' has name equal to an instance variable");
		}
		lexer.nextToken();
		if ( lexer.token != Symbol.RIGHTPAR ) {
			if(name.equals("run") && currentClass.getCname().equals("Program")) {
				signalError.showError("Method 'run' of class 'Program' cannot take parameters");
			}else {
				plista = formalParamDec();
				if(currentClass.getSuperclass() != null) {
					if(currentClass.getSuperclass().searchPublicMethod(name) != null) {
						MethodDec mtd = currentClass.getSuperclass().searchPublicMethod(name);
						ArrayList<Variable> av1 = mtd.getParametros().getarray();
						ArrayList<Variable> av2 = plista.getarray();
						for(int i = 0;i<av1.size();i++) {
							if(av1.get(i).getType() != av2.get(i).getType()) {
								signalError.showError("Method '"+ name +"' is being redefined in subclass '"+ currentClass.getCname() +"' with a signature different from the method of superclass '"+ currentClass.getSuperclass().getCname() +"'");
							}
						}
					}
				}
			}
		}
		if(currentClass.getSuperclass() != null) {
			MethodDec mtd = currentClass.getSuperclass().searchPublicMethod(name);
			if(mtd != null) {
				
			}
		}
		this.currentClass.addMethod(currentMethod);//CAUSA RUNTIME EM ALGUM LUGAR DESCOBRIR DEPOIS
		if ( lexer.token != Symbol.RIGHTPAR ) signalError.showError(") expected");
		lexer.nextToken();
		if ( lexer.token != Symbol.LEFTCURBRACKET ) signalError.showError("{ expected");
		lexer.nextToken();
		if(lexer.token == Symbol.BREAK) {
			stlist = statementList(type, name, 9);		
		}else {
			stlist = statementList(type, name, 0);
		}
		this.currentClass.remMethod(currentMethod);
		if ( lexer.token != Symbol.RIGHTCURBRACKET ) signalError.showError("} expected");
		symbolTable.removeLocalIdent();
		lexer.nextToken();
		this.currentMethod = new MethodDec(name, type, qualifier, stlist, plista);
		if(currentClass.getSuperclass() != null) {
			if(currentClass.getSuperclass().searchPublicMethod(name) != null) {
				if(currentClass.getSuperclass().searchPublicMethod(name).getReturnType().getName() != currentMethod.getReturnType().getName()) {
					signalError.showError("Method '"+ name +"' of subclass '"+ currentClass.getCname() +"' has a signature different from method inherited from superclass '"+ currentClass.getSuperclass().getCname() +"'" );
				}
			}
		}
		this.currentClass.addMethod(this.currentMethod);
		this.currentMethod = null;
	}

	private Statement localDec() {
		ArrayList<Variable> varlist = new ArrayList<>();
		StmtVar svar= null;
		Type type = type();
		if ( lexer.token != Symbol.IDENT ) signalError.showError("Identifier expected");
		Variable v = new Variable(lexer.getStringValue(), type);
		Variable avar = this.symbolTable.getInLocal(v.getName()); 
		if(avar == null){
			this.symbolTable.putInLocal(v.getName(),v);
			varlist.add(v);
			lexer.nextToken();
			while (lexer.token == Symbol.COMMA) {
				lexer.nextToken();
				if ( lexer.token != Symbol.IDENT )
					signalError.showError("Identifier expected");
				v = new Variable(lexer.getStringValue(), type);
				this.symbolTable.putInLocal(v.getName(),v);
				varlist.add(v);
				lexer.nextToken();
			}
			if(lexer.token != Symbol.SEMICOLON) {
				signalError.showError("Missing ';'", true);
			}
		}else{
			signalError.showError("Variable "+v.getName()+" is being redeclared");
		}
		svar = new StmtVar(varlist);
		return svar;
	}

	private ParamList formalParamDec() {
		// FormalParamDec ::= ParamDec { "," ParamDec }
		ParamList plist = new ParamList();
		plist.addElement(paramDec());
		while (lexer.token == Symbol.COMMA) {
			lexer.nextToken();
			plist.addElement(paramDec());
		}
		return plist;
	}

	private Parameter paramDec() {
		// ParamDec ::= Type Id

		Type t = type();
		if ( lexer.token != Symbol.IDENT ) signalError.showError("Identifier expected");
			
		Parameter p = new Parameter(lexer.getStringValue(), t);
		this.symbolTable.putInLocal(p.getName(), p);
		lexer.nextToken();
		return p;
	}

	private Type type() {
		// Type ::= BasicType | Id
		Type result;

		switch (lexer.token) {
		case VOID:
			result = Type.voidType;
			break;
		case INT:
			result = Type.intType;
			break;
		case BOOLEAN:
			result = Type.booleanType;
			break;
		case STRING:
			result = Type.stringType;
			break;
		case IDENT:
			// # corrija: faça uma busca na TS para buscar a classe
			// IDENT deve ser uma classe.
			if(symbolTable.getInGlobal(lexer.getStringValue()) == null) {
				signalError.showError("Class does not exist");
			}
			result = symbolTable.getInGlobal(lexer.getStringValue());;
			break;
		default:
			signalError.showError("Type expected");
			result = Type.undefinedType;
		}
		lexer.nextToken();
		return result;
	}

	private CompositeStatement compositeStatement(Type type, String name, int op) {
		CompositeStatement cpst;		
		lexer.nextToken();
		cpst = new CompositeStatement(statementList(type, name, op));
		if ( lexer.token != Symbol.RIGHTCURBRACKET )
			signalError.showError("} expected");
		else
			lexer.nextToken();
		return cpst;
	}

	private ArrayList<Statement> statementList(Type type, String name, int op) {
		// CompStatement ::= "{" { Statement } "}"
		ArrayList<Statement> stlist = new ArrayList<>();
		Symbol tk;
		int flagreturn = 0;
		// statements always begin with an identifier, if, read, write, ...
		while ((tk = lexer.token) != Symbol.RIGHTCURBRACKET && tk != Symbol.ELSE){
			if(op == 0 && lexer.token == Symbol.BREAK) {
				op = 9;
			}
			stlist.add(statement(type, name, op));
		}
		return stlist;
	}

	private Statement statement(Type type, String name, int qual) {
		/*
		 * Statement ::= Assignment ``;'' | IfStat |WhileStat | MessageSend
		 *                ``;'' | ReturnStat ``;'' | ReadStat ``;'' | WriteStat ``;'' |
		 *               ``break'' ``;'' | ``;'' | CompStatement | LocalDec
		 */
		switch (lexer.token) {
		case THIS:
			return assignExprLocalDec();
		case IDENT:
			return assignExprLocalDec();
		case SUPER:
			return assignExprLocalDec();
		case INT:
			return assignExprLocalDec();
		case BOOLEAN:
			return assignExprLocalDec();
		case STRING:
			return assignExprLocalDec();
		case ASSERT:
			assertStatement();
			break;
		case RETURN:
			return returnStatement();
		case READ:
			return readStatement();
		case WRITE:
			return writeStatement();
		case WRITELN:
			return writelnStatement();
		case IF:
			return ifStatement(type, name);
		case BREAK:
			if(qual != 9) {
				breakStatement();
			}else{
				signalError.showError("'break' statement found outside a 'while' statement");
			}
			break;
		case WHILE:
			return whileStatement(type, name);
		case SEMICOLON:
			nullStatement();
			break;
		case LEFTCURBRACKET:
			return compositeStatement(type, name, qual);
		default:
			signalError.showError("Statement expected!!!");
		}
		
		return null;
	}

	private Statement assertStatement() {
		lexer.nextToken();
		int lineNumber = lexer.getLineNumber();
		Expr e = expr();
		if ( e.getType() != Type.booleanType )
			signalError.showError("boolean expression expected");
		if ( lexer.token != Symbol.COMMA ) {
			this.signalError.showError("',' expected after the expression of the 'assert' statement");
		}
		lexer.nextToken();
		if ( lexer.token != Symbol.LITERALSTRING ) {
			this.signalError.showError("A literal string expected after the ',' of the 'assert' statement");
		}
		String message = lexer.getLiteralStringValue();
		lexer.nextToken();
		if ( lexer.token == Symbol.SEMICOLON )
			lexer.nextToken();
		
		return new StatementAssert(e, lineNumber, message);
	}

	/*
	 * retorne true se 'name' é uma classe declarada anteriormente. É necessário
	 * fazer uma busca na tabela de símbolos para isto.
	 */
	private boolean isType(String name) {
		return this.symbolTable.getInGlobal(name) != null;
	}

	/*
	 * AssignExprLocalDec ::= Expression [ ``$=$'' Expression ] | LocalDec
	 */
	private Statement assignExprLocalDec() {
		Statement s = null;
		String op = null;
		if ( lexer.token == Symbol.INT || lexer.token == Symbol.BOOLEAN
				|| lexer.token == Symbol.STRING ||
				// token é uma classe declarada textualmente antes desta
				// instrução
				(lexer.token == Symbol.IDENT && isType(lexer.getStringValue()) && lexer.proximo() != 0) ) {
			/*
			 * uma declaração de variável. 'lexer.token' é o tipo da variável
			 * 
			 * AssignExprLocalDec ::= Expression [ ``$=$'' Expression ] | LocalDec 
			 * LocalDec ::= Type IdList ``;''
			 */
			s = localDec();
		}else {
			/*
			 * AssignExprLocalDec ::= Expression [ ``$=$'' Expression ]
			 */
			Expr e = expr();
			if ( lexer.token == Symbol.ASSIGN ) {
				lexer.nextToken();
				op = "=";
				if(e != null) {
					if((e.getType() == Type.intType || e.getType() == Type.stringType || e.getType() == Type.booleanType) && lexer.token == Symbol.NEW) {
						signalError.showError("Type error: type of the left-hand side of the assignment is a basic type and the type of the right-hand side is a class");
					}
				}
				Expr e2 = expr();
				if(e2 != null) {
					if(!(e.getType() instanceof KraClass) && !(e2.getType() instanceof KraClass)) {
						if(e.getType() != e2.getType()) {
							signalError.showError("Type error: value of the right-hand side is not subtype of the variable of the left-hand side.");
						}
					}
					if(e.getType() instanceof KraClass) {
						if(e2.getType() == Type.intType || e2.getType() == Type.booleanType || e2.getType() == Type.stringType) {
							signalError.showError("Type error: the type of the expression of the right-hand side is a basic type and the type of the variable of the left-hand side is a class");
						}
					}
					if(e.getType() instanceof KraClass && e2.getType() instanceof KraClass) {
						KraClass aux1 = symbolTable.getInGlobal(e.getType().getCname());
						KraClass aux2 = symbolTable.getInGlobal(e2.getType().getCname());
						if(aux1.getSuperclass() != null) {
							if(aux2.getCname().equals(aux1.getSuperclass().getCname())) {
								signalError.showError("Type error: type of the right-hand side of the assignment is not a subclass of the left-hand side");
							}
						}
					}
				}
				
				if ( lexer.token != Symbol.SEMICOLON )
					signalError.showError("';' expected", true);
				else
					lexer.nextToken();
				return new Stmtlocadec(e, op, e2);
			}else if(e instanceof PrimaryExpr) {
				return new Stmtlocadec(e, ".", null);
			}else if(lexer.token == Symbol.IDENT) {
				return null;
			}
		}
		return s;
	}

	private ExprList realParameters() {
		ExprList anExprList = null;

		if ( lexer.token != Symbol.LEFTPAR ) signalError.showError("( expected");
		lexer.nextToken();
		if ( startExpr(lexer.token) ) anExprList = exprList();
		if ( lexer.token != Symbol.RIGHTPAR ) signalError.showError(") expected");
		lexer.nextToken();
		return anExprList;
	}

	private WhileStatement whileStatement(Type type, String name) {

		lexer.nextToken();
		if ( lexer.token != Symbol.LEFTPAR ) signalError.showError("( expected");
		lexer.nextToken();
		Expr e = expr();
		if(e.getType() != Type.booleanType ){
			signalError.showError("boolean expression expected");
		}
		if ( lexer.token != Symbol.RIGHTPAR ) signalError.showError(") expected");
		lexer.nextToken();
		Statement s = statement(type, name, 1);
		
		return new WhileStatement(e, s);
	}

	private Ifstatement ifStatement(Type type, String name) {

		lexer.nextToken();
		if ( lexer.token != Symbol.LEFTPAR ) signalError.showError("( expected");
		lexer.nextToken();
		Expr e = expr();
		if(e.getType() != Type.booleanType ){
			signalError.showError("boolean expression expected");
		}
		if(e instanceof CompositeExpr) {
			if(((CompositeExpr) e).getleft().getType() != ((CompositeExpr) e).getright().getType() ) {
				if(((CompositeExpr) e).getleft().getType() instanceof KraClass && ((CompositeExpr) e).getright().getType() instanceof KraClass) {
					KraClass akra = symbolTable.getInGlobal(((CompositeExpr) e).getleft().getType().getCname());
					KraClass akra2 = symbolTable.getInGlobal(((CompositeExpr) e).getright().getType().getCname());
					if(akra.getSuperclass() != null ) {
						if(!akra.getSuperclass().getCname().equals(akra2.getCname())) {
							signalError.showError("Incompatible types cannot be compared with '"+ ((CompositeExpr) e).getoper() +"' because the result will always be 'false' ");
						}
					}else if(akra2.getSuperclass() != null ) {
						if(!akra2.getSuperclass().getCname().equals(akra.getCname())) {
							signalError.showError("Incompatible types cannot be compared with '"+ ((CompositeExpr) e).getoper() +"' because the result will always be 'false' ");
						}
					}else {
						signalError.showError("Incompatible types cannot be compared with '"+ ((CompositeExpr) e).getoper() +"' because the result will always be 'false' ");
					}
				}
			}
		}
		if ( lexer.token != Symbol.RIGHTPAR ) signalError.showError(") expected");
		lexer.nextToken();
		Statement ifstm = statement(type, name, 2);
		Ifstatement ifstmt = new Ifstatement(e, ifstm);
		if ( lexer.token == Symbol.ELSE ) {
			lexer.nextToken();
			ifstmt = new Ifstatement(e, ifstm, statement(type, name, 2));
		}
		return ifstmt;
	}

	private ReturnStatement returnStatement() {

		lexer.nextToken();
		Expr e = expr();
		if(this.currentMethod.getReturnType() == Type.voidType) {
			this.signalError.showError("This method cannot return a value");
		}
		if(e != null && e.getType() != null) {
			if(!e.getType().isCompatible(this.currentMethod.getReturnType())) {
				this.signalError.showError("This expression is not compatible with the method return type");
			}
		}
		if ( lexer.token != Symbol.SEMICOLON )
			signalError.show(ErrorSignaller.semicolon_expected);
		if(lexer.token == Symbol.SEMICOLON){
			lexer.nextToken();
		}
		return new ReturnStatement(e);
	}

	private ReadStatement readStatement() {
		ReadStatement rd = null;
		ArrayList<Expr> elist = new ArrayList<>();
		lexer.nextToken();
		if ( lexer.token != Symbol.LEFTPAR ) signalError.showError("( expected");
		lexer.nextToken();
		if(lexer.token != Symbol.IDENT && lexer.token != Symbol.THIS) signalError.showError("command 'read' expects a variable");
		while (true) {
			Expr e = expr();
			if(e.getType() == Type.booleanType) {
				signalError.showError("Command 'read' does not accept 'boolean' variables");
			}
			if(e.getType() != Type.intType && e.getType() != Type.stringType) {
				signalError.showError("'int' or 'String' expression expected");
			}
			elist.add(e);
			if ( lexer.token == Symbol.COMMA )
				lexer.nextToken();
			else
				break;
		}

		rd = new ReadStatement(elist);
		if ( lexer.token != Symbol.RIGHTPAR ) signalError.showError(") expected");
		lexer.nextToken();
		if ( lexer.token != Symbol.SEMICOLON )
			signalError.show(ErrorSignaller.semicolon_expected);
		lexer.nextToken();
		return rd;
	}

	private WriteStatement writeStatement() {
		
		ExprList exlist = null;
		ArrayList<Expr> arlist = new ArrayList<>();
		lexer.nextToken();
		if ( lexer.token != Symbol.LEFTPAR ) signalError.showError("( expected");
		lexer.nextToken();
		exlist = exprList();
		arlist = exlist.getArray();
		for(int i=0;i<arlist.size();i++) {
			if(arlist.get(i) != null) {	
				if(arlist.get(i).getType() == Type.booleanType) {
					signalError.showError("Command 'write' does not accept 'boolean' expressions");
				}
				if(arlist.get(i).getType() instanceof KraClass) {
					signalError.showError("Command 'write' does not accept objects");
				}
			}
		}
		WriteStatement wst = new WriteStatement(exlist);
		if ( lexer.token != Symbol.RIGHTPAR ) signalError.showError(") expected");
		lexer.nextToken();
		if ( lexer.token != Symbol.SEMICOLON )
			signalError.show(ErrorSignaller.semicolon_expected);
		lexer.nextToken();
		return wst;
	}

	private WritelnStatement writelnStatement() {
		
		ExprList exlist = null;
		ArrayList<Expr> arlist = new ArrayList<>();
		lexer.nextToken();
		if ( lexer.token != Symbol.LEFTPAR ) signalError.showError("( expected");
		lexer.nextToken();
		exlist = exprList();
		arlist = exlist.getArray();
		for(int i=0;i<arlist.size();i++) {
			if(arlist.get(i) != null) {	
				if(arlist.get(i).getType() == Type.booleanType) {
					signalError.showError("Command 'write' does not accept 'boolean' expressions");
				}
				if(arlist.get(i).getType() instanceof KraClass) {
					signalError.showError("Command 'write' does not accept objects");
				}
			}
		}
		WritelnStatement wlnst = new WritelnStatement(exlist);
		if ( lexer.token != Symbol.RIGHTPAR ) signalError.showError(") expected");
		lexer.nextToken();
		if ( lexer.token != Symbol.SEMICOLON )
			signalError.show(ErrorSignaller.semicolon_expected);
		lexer.nextToken();
		return wlnst;
	}

	private void breakStatement() {
		lexer.nextToken();
		if ( lexer.token != Symbol.SEMICOLON )
			signalError.show(ErrorSignaller.semicolon_expected);
		lexer.nextToken();
	}

	private void nullStatement() {
		lexer.nextToken();
	}

	private ExprList exprList() {
		// ExpressionList ::= Expression { "," Expression }

		ExprList anExprList = new ExprList();
		Expr e = expr();
		/*if(e != null) {
			if(e.getType() == Type.booleanType) {
				signalError.showError("Command 'write' does not accept 'boolean' expressions");
			}
		}*/
		anExprList.addElement(e);
		while (lexer.token == Symbol.COMMA) {
			lexer.nextToken();
			anExprList.addElement(expr());
		}
		return anExprList;
	}

	private Expr expr() {

		Expr left = simpleExpr();
		Symbol op = lexer.token;
		if ( op == Symbol.EQ || op == Symbol.NEQ || op == Symbol.LE
				|| op == Symbol.LT || op == Symbol.GE || op == Symbol.GT ) {
			lexer.nextToken();
			Expr right = simpleExpr();
			left = new CompositeExpr(left, op, right);
		}
		
		return left;
	}

	private Expr simpleExpr() {
		Symbol op;

		Expr left = term();
		while ((op = lexer.token) == Symbol.MINUS || op == Symbol.PLUS || op == Symbol.OR) {
			if(left != null) {
				if(left.getType() == Type.booleanType && op == Symbol.PLUS) {
					signalError.showError("type boolean does not support operation '+'");
				}
				lexer.nextToken();
				Expr right = term();
				if(left.getType() != right.getType()) {
					if(left.getType() == Type.intType) {
						signalError.showError("operator '+' of 'int' expects an 'int' value");
					}
				}
				left = new CompositeExpr(left, op, right);
			}else {
				lexer.nextToken();
			}
		}
		return left;
	}

	private Expr term() {
		Symbol op;

		Expr left = signalFactor();
		while ((op = lexer.token) == Symbol.DIV || op == Symbol.MULT
				|| op == Symbol.AND) {
			lexer.nextToken();
			Expr right = signalFactor();
			left = new CompositeExpr(left, op, right);
		}
		return left;
	}

	private Expr signalFactor() {
		Symbol op;
		if ( (op = lexer.token) == Symbol.PLUS || op == Symbol.MINUS ) {
			lexer.nextToken();
			return new SignalExpr(op, factor());
		}
		else
			return factor();
	}

	/*
	 * Factor ::= BasicValue | "(" Expression ")" | "!" Factor | "null" |
	 *      ObjectCreation | PrimaryExpr
	 * 
	 * BasicValue ::= IntValue | BooleanValue | StringValue 
	 * BooleanValue ::=  "true" | "false" 
	 * ObjectCreation ::= "new" Id "(" ")" 
	 * PrimaryExpr ::= "super" "." Id "(" [ ExpressionList ] ")"  | 
	 *                 Id  |
	 *                 Id "." Id | 
	 *                 Id "." Id "(" [ ExpressionList ] ")" |
	 *                 Id "." Id "." Id "(" [ ExpressionList ] ")" |
	 *                 "this" | 
	 *                 "this" "." Id | 
	 *                 "this" "." Id "(" [ ExpressionList ] ")"  | 
	 *                 "this" "." Id "." Id "(" [ ExpressionList ] ")"
	 */
	private Expr factor() {
		Expr anExpr;
		ExprList exprList;
		PrimaryExpr prexpr;
		String messageName, id;
		char op;
		switch (lexer.token) {
		// IntValue
		case LITERALINT:
			return literalInt();
			// BooleanValue
		case FALSE:
			lexer.nextToken();
			return LiteralBoolean.False;
			// BooleanValue
		case TRUE:
			lexer.nextToken();
			return LiteralBoolean.True;
			// StringValue
		case LITERALSTRING:
			String literalString = lexer.getLiteralStringValue();
			lexer.nextToken();
			return new LiteralString(literalString);
			// "(" Expression ")" |
		case LEFTPAR:
			lexer.nextToken();
			anExpr = expr();
			if ( lexer.token != Symbol.RIGHTPAR ) signalError.showError(") expected");
			lexer.nextToken();
			return new ParenthesisExpr(anExpr);

			// "null"
		case NULL:
			lexer.nextToken();
			return new NullExpr();
			// "!" Factor
		case NOT:
			lexer.nextToken();
			anExpr = expr();
			return new UnaryExpr(anExpr, Symbol.NOT);
			// ObjectCreation ::= "new" Id "(" ")"
		case NEW:
			/*
			 * person = new Person(); -- classname
			 */
			lexer.nextToken();
			if ( lexer.token != Symbol.IDENT )
				signalError.showError("Identifier expected");

			String className = lexer.getStringValue();
			KraClass aClass = this.symbolTable.getInGlobal(className);
			if(aClass == null) {
				signalError.showError("Class '"+className+"' does not exist");
			}
			lexer.nextToken();
			if ( lexer.token != Symbol.LEFTPAR ) signalError.showError("( expected");
			lexer.nextToken();
			if ( lexer.token != Symbol.RIGHTPAR ) signalError.showError(") expected");
			lexer.nextToken();
			/*
			 * return an object representing the creation of an object
			 */
			return new ExprClass(aClass);
			/*
          	 * PrimaryExpr ::= "super" "." Id "(" [ ExpressionList ] ")"  | 
          	 *                 Id  |
          	 *                 Id "." Id | 
          	 *                 Id "." Id "(" [ ExpressionList ] ")" |
          	 *                 Id "." Id "." Id "(" [ ExpressionList ] ")" |
          	 *                 "this" | 
          	 *                 "this" "." Id | 
          	 *                 "this" "." Id "(" [ ExpressionList ] ")"  | 
          	 *                 "this" "." Id "." Id "(" [ ExpressionList ] ")"
			 */
		case SUPER:
			// "super" "." Id "(" [ ExpressionList ] ")"
			lexer.nextToken();
			op = 's';
			if ( lexer.token != Symbol.DOT ) {
				signalError.showError("'.' expected");
			}
			else
				lexer.nextToken();
			if ( lexer.token != Symbol.IDENT )
				signalError.showError("Identifier expected");
			messageName = lexer.getStringValue();
			if(currentClass.getSuperclass() != null) {
				KraClass spclass = symbolTable.getInGlobal(currentClass.getSuperclass().getCname());   
				while(spclass != null) {
					if(spclass != null) {
						MethodDec amethod = spclass.searchPublicMethod(messageName);
						if(amethod != null) {
							if(amethod.getQualifier() != Symbol.PUBLIC) {
								signalError.showError("Method '"+ amethod.getName() +"' was not found in the public interface of '"+ spclass.getCname() +"' or its superclasses");
							}
						}
						if(spclass.searchPublicMethod(messageName) != null) {
							break;
						}
					}
					if(spclass.getSuperclass() != null) {
						spclass = spclass.getSuperclass();	
					}else {
						signalError.showError("Method '"+ messageName +"' was not found in superclass '"+ currentClass.getCname() +"' or its superclasses");
					}
				}
			}else {
				signalError.showError("'super' used in class '"+ currentClass.getCname() +"' that does not have a superclass");
			}
			/*
			 * para fazer as conferências semânticas, procure por 'messageName'
			 * na superclasse/superclasse da superclasse etc
			 */
			lexer.nextToken();
			exprList = realParameters();
			return new PrimaryExpr(messageName, null, exprList, op, null, true);
		case IDENT:
			/*
          	 * PrimaryExpr ::=  
          	 *                 Id  |
          	 *                 Id "." Id | 
          	 *                 Id "." Id "(" [ ExpressionList ] ")" |
          	 *                 Id "." Id "." Id "(" [ ExpressionList ] ")" |
			 */
			Type tipo = null;
			String firstId = lexer.getStringValue();
			op = 'i';
			lexer.nextToken();
			if ( lexer.token != Symbol.DOT ) {
				Variable avar = this.symbolTable.getInLocal(firstId);
					if(avar == null) {
							this.signalError.showError("identifier '"+firstId+"' was not declared");
					}
				// Id
				// retorne um objeto da ASA que representa um identificador
				VariableExpr ve = new VariableExpr(avar);
				return ve;
			}
			else { // Id "."
				lexer.nextToken(); // coma o "."
				if ( lexer.token != Symbol.IDENT ) {
					signalError.showError("Identifier expected");
				}
				else {
					// Id "." Id
					lexer.nextToken();
					id = lexer.getStringValue();
					Variable avar = symbolTable.getInLocal(id);
					if(avar == null) {
						avar = currentClass.getInstanceVariableList().getInstanceVar(id);
					}
					if(avar != null) {
						tipo = avar.getType();
					}
					prexpr = new PrimaryExpr(firstId, id, op, tipo, false);
					if ( lexer.token == Symbol.DOT ) {
						// Id "." Id "." Id "(" [ ExpressionList ] ")"
						/*
						 * se o compilador permite variáveis estáticas, é possível
						 * ter esta opção, como
						 *     Clock.currentDay.setDay(12);
						 * Contudo, se variáveis estáticas não estiver nas especificações,
						 * sinalize um erro neste ponto.
						 */
						lexer.nextToken();
						if ( lexer.token != Symbol.IDENT )
							signalError.showError("Identifier expected");
						messageName = lexer.getStringValue();
						lexer.nextToken();
						exprList = this.realParameters();
					}
					else if ( lexer.token == Symbol.LEFTPAR ) {
						// Id "." Id "(" [ ExpressionList ] ")"
						avar = this.symbolTable.getInLocal(firstId); 
						if(avar == null) {
							this.signalError.showError("identifier '"+firstId+"' was not declared");
						}
						Type typeVar = avar.getType();
						if(!(typeVar instanceof KraClass)) {
							this.signalError.showError("Attempt to call a method on a variable of a basic type");
						}
						KraClass classVar = (KraClass ) typeVar;
						
						//method is ID
						MethodDec amethod = classVar.searchPublicMethod(id);
						if(amethod == null) {
							if(classVar.getSuperclass() == null) {
								signalError.showError("Method '"+id+"' is not a public method of '"+classVar.getName()+"' which is the type of '" +firstId+"'");
							}else {
								KraClass spclass = classVar;
								while(spclass.searchPublicMethod(id) == null) {
									if(spclass.getSuperclass() != null) {
										spclass = spclass.getSuperclass();
									}else {
										signalError.showError("Method '"+id+"' is not a public method of '"+classVar.getName()+"' which is the type of '" +firstId+"'");	
									}
								}
							}
						}else {
							if(amethod.getQualifier() != Symbol.PUBLIC) {
								signalError.showError("Method '"+ amethod.getName() +"' was not found in the public interface of '"+ classVar.getCname() +"' or its superclasses");
							}
						}
						exprList = this.realParameters();
						/*
						 * para fazer as conferências semânticas, procure por
						 * método 'ident' na classe de 'firstId'
						 */
						avar = symbolTable.getInLocal(firstId);
						if(avar == null) {
							avar = currentClass.getInstanceVariableList().getInstanceVar(firstId);
						}
						KraClass akra = symbolTable.getInGlobal(avar.getType().getCname());
						if(akra.searchPublicMethod(id) != null) {
							tipo = akra.searchPublicMethod(id).getReturnType();
						}
						return new PrimaryExpr(firstId, id, exprList, op, tipo, true);
					}
					else {
						// retorne o objeto da ASA que representa Id "." Id
						return prexpr;
					}
				}
			}
			break;
		case THIS:
			/*
			 * Este ':' trata os seguintes casos: 
          	 * PrimaryExpr ::= 
          	 *                 "this" | 
          	 *                 "this" "." Id | 
          	 *                 "this" "." Id "(" [ ExpressionList ] ")"  | 
          	 *                 "this" "." Id "." Id "(" [ ExpressionList ] ")"
			 */
			op = 't';
			tipo = null;
			lexer.nextToken();
			if ( lexer.token != Symbol.DOT ) {
				// only 'this'
				// retorne um objeto da ASA que representa 'this'
				// confira se não estamos em um método estático
				return null;
			}
			else {
				lexer.nextToken();
				if ( lexer.token != Symbol.IDENT )
					signalError.showError("Identifier expected");
				id = lexer.getStringValue();
				lexer.nextToken();
				// já analisou "this" "." Id
				if ( lexer.token == Symbol.LEFTPAR ) {
					// "this" "." Id "(" [ ExpressionList ] ")"
					/*
					 * Confira se a classe corrente possui um método cujo nome é
					 * 'ident' e que pode tomar os parâmetros de ExpressionList
					 */
					MethodDec mtd = currentClass.searchPublicMethod(id);
					if(mtd == null) {
						if(currentClass.getSuperclass() != null) {
							if(currentClass.getSuperclass().searchPublicMethod(id) == null) {
								signalError.showError("Method '"+ id +"' was not found in class '"+ currentClass.getCname() +"' or its superclasses");
							}
						}else {
							signalError.showError("Method '"+ id +"' was not found in class '"+ currentClass.getCname() +"' or its superclasses");
						}
					}else {
						tipo = mtd.getReturnType();
					}
					exprList = this.realParameters();
					if(currentClass.searchPublicMethod(id) != null) {
						if(currentClass.searchPublicMethod(id).getParametros() != null) {
							ArrayList<Variable> avarlist = currentClass.searchPublicMethod(id).getParametros().getarray();
							ArrayList<Expr> exlist = exprList.getArray();
							for(int i=0;i < avarlist.size();i++) {
								if(exlist.get(i).getType() instanceof KraClass && avarlist.get(i).getType() instanceof KraClass) {
									KraClass aux = symbolTable.getInGlobal(exlist.get(i).getType().getCname());
									if(!(aux.getCname().equals(avarlist.get(i).getType().getCname()))) {
										if(aux.getSuperclass() != null) {
											if(!(aux.getSuperclass().getCname().equals(avarlist.get(i).getType().getCname()))){
												signalError.showError("Type error: the type of the real parameter is not subclass of the type of the formal parameter ");
											}
										}else{
											signalError.showError("Type error: the type of the real parameter is not subclass of the type of the formal parameter ");
										}
									}
								}
							}
						}
					}
					return new PrimaryExpr(id, null, exprList, op, tipo, true);
				}
				else if ( lexer.token == Symbol.DOT ) {
					// "this" "." Id "." Id "(" [ ExpressionList ] ")"
					lexer.nextToken();
					if ( lexer.token != Symbol.IDENT )
						signalError.showError("Identifier expected");
					String secid = lexer.getStringValue();
					lexer.nextToken();
					exprList = this.realParameters();
					Variable avar = symbolTable.getInLocal(id);
					if(avar == null) {
						avar = currentClass.getInstanceVariableList().getInstanceVar(id);
					}
					KraClass akra = symbolTable.getInGlobal(avar.getType().getCname());
					if(akra.searchPublicMethod(secid) != null) {
						tipo = akra.searchPublicMethod(secid).getReturnType();
					}
					return prexpr = new PrimaryExpr(id, secid, op, tipo, false);
				}else{
					// retorne o objeto da ASA que representa "this" "." Id
					/*
					 * confira se a classe corrente realmente possui uma
					 * variável de instância 'ident'
					 */
					Variable avar = symbolTable.getInLocal(id);
					if(avar == null) {
						avar = currentClass.getInstanceVariableList().getInstanceVar(id);
					}
					tipo = avar.getType();
					return prexpr = new PrimaryExpr(id, op, tipo, false);
				}
			}
		default:
			signalError.showError("Expression expected");
		}
		return null;
	}

	private LiteralInt literalInt() {
		// the number value is stored in lexer.getToken().value as an object of
		// Integer.
		// Method intValue returns that value as an value of type int.
		int value = lexer.getNumberValue();
		lexer.nextToken();
		return new LiteralInt(value);
	}

	private static boolean startExpr(Symbol token) {

		return token == Symbol.FALSE || token == Symbol.TRUE
				|| token == Symbol.NOT || token == Symbol.THIS
				|| token == Symbol.LITERALINT || token == Symbol.SUPER
				|| token == Symbol.LEFTPAR || token == Symbol.NULL
				|| token == Symbol.IDENT || token == Symbol.LITERALSTRING;

	}

	private SymbolTable		symbolTable;
	private Lexer			lexer;
	private ErrorSignaller	signalError;
	private MethodDec currentMethod;
	private KraClass currentClass;
}
