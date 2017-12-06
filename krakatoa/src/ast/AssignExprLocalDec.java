// Vinicius Yamamoto	490105
package ast;

public class AssignExprLocalDec extends Expr{
	public AssignExprLocalDec(Expr e, String op, Expr e2) {
		this.e = e;
		this.e2 = e2;
		this.op = op;
	}
	
	@Override
	public void genC(PW pw, boolean putParenthesis) {
	}
	
	public void genKra(PW pw) {
		e.genKra(pw);
		e2.genKra(pw);
	}
	
	@Override
	public Type getType() {
		return null;
	}
	private Expr e;
	private String op; 
	private Expr e2;
}
