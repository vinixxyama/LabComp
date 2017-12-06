// Vinicius Yamamoto    490105

package ast;

public class Ifstatement extends Statement{
	public Ifstatement(Expr e, Statement st) {
		this.st = st;
		this.e = e;
	}
	public Ifstatement(Expr e, Statement ifstmt, Statement elsestmt) {
		this.st = ifstmt;
		this.st2 = elsestmt;
		this.e = e;
	}
	@Override
	public void genC(PW pw) {
		
	}

	@Override
	public void genKra(PW pw) {
		pw.printIdent("if(");
		if(e instanceof PrimaryExpr) {
			pw.sub();
			pw.sub();
			e.genKra(pw);
			pw.add();
			pw.add();
		}else if(e instanceof CompositeExpr){
			e.genKra(pw);
		}else{
			e.genKra(pw);
		}
		pw.println("){");
		if(st instanceof CompositeStatement) {
			st.genKra(pw);
		}
		if(st2 != null) {
			pw.printlnIdent("}else{");
			pw.printIdent("");
			st2.genKra(pw);
		}
		pw.printlnIdent("}");
	}

	private Expr e;
	private Statement st;
	private Statement st2;
}
