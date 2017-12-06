// Vinicius Yamamoto    490105

package ast;

public class WhileStatement extends Statement{
	public WhileStatement(Expr e, Statement s) {
		this.expr = e;
		this.statement = s;
	}
	public Expr getExpr() {
		return expr;
	}
	public Statement getStatement() {
		return statement;
	}
	@Override
	public void genC(PW pw) {
	}
	@Override
	public void genKra(PW pw) {
		pw.printIdent("while ( ");
		expr.genKra(pw);
		pw.println(" ){");
		pw.add();
		if(this.statement instanceof CompositeStatement) {
			pw.add();
			this.statement.genKra(pw);
			pw.sub();
		}
		pw.printlnIdent("}");
		pw.sub();
	}
	private Expr expr;
	private Statement statement;
}
