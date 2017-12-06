// Vinicius Yamamoto    490105

package ast;

import java.util.ArrayList;

import lexer.Symbol;

public class MethodDec {
	public MethodDec(String name, Type returnType, Symbol qualifier, ArrayList<Statement> stlist, ParamList plista) {
		this.name = name;
		this.returnType = returnType;
		this.qualifier = qualifier;
		this.stlist = stlist;
		this.plista = plista;
	}
	public ArrayList <Statement> getStlist(){
		return stlist;
	}
	public Symbol getQualifier() {
		return qualifier;
	}
	public String getName() {
		return name;
	}
	public Type getReturnType() {
		return returnType;
	}
	public ParamList getParametros() {
		return plista;
	}
	public void genKra(PW pw) {
		for(int i=0;i<stlist.size();i++) {
			if (stlist.get(i) instanceof StmtVar) {
				stlist.get(i).genKra(pw);
			}else if (stlist.get(i) instanceof Stmtlocadec) {
				stlist.get(i).genKra(pw);
			}else if(stlist.get(i) instanceof Ifstatement) {
				pw.add();
				stlist.get(i).genKra(pw);
				pw.sub();
			}else if(stlist.get(i) instanceof WhileStatement) {
				pw.printIdent("");
				stlist.get(i).genKra(pw);
			}else if(stlist.get(i) instanceof WriteStatement) {
				pw.printIdent("");
				stlist.get(i).genKra(pw);
			}else if(stlist.get(i) instanceof WritelnStatement) {
				pw.printIdent("");
				stlist.get(i).genKra(pw);
			}else if(stlist.get(i) instanceof ReadStatement) {
				pw.printIdent("");
				stlist.get(i).genKra(pw);
			}else if(stlist.get(i) instanceof ReturnStatement) {
				pw.printIdent("");
				stlist.get(i).genKra(pw);
			}
		}
	}
	private Symbol qualifier;
	private String name;
	private Type returnType;
	private ArrayList<Statement> stlist;
	private ParamList plista;
}
