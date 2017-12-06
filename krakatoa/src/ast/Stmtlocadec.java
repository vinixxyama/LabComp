// Vinicius Yamamoto    490105

package ast;

public class Stmtlocadec extends Statement {
	public Stmtlocadec(Expr e, String op, Expr e2) {
		this.e = e;
		this.e2 = e2;
		this.op = op;
	}
	@Override
	public void genC(PW pw) {
		// TODO Auto-generated method stub
	}
	@Override
	public void genKra(PW pw) {
		pw.add();
		if(op.equals("=")) {
			if(e instanceof VariableExpr) {
				pw.printIdent("");
				e.genKra(pw);
			}else {
				e.genKra(pw);
			}
			pw.print(" "+ op +" ");
			if(e2 instanceof ExprClass) {
				pw.print("new ");
				e2.genKra(pw);
			}else if(e2 instanceof LiteralInt){
				e2.genKra(pw);
			}else if(e2 instanceof LiteralBoolean) {
				e2.genKra(pw);
			}else if(e2 instanceof LiteralString) {
				e2.genKra(pw);
			}else if(e2 instanceof VariableExpr) {
				e2.genKra(pw);
			}else if(e2 instanceof CompositeExpr) {
				e2.genKra(pw);
			}else if(e2 instanceof UnaryExpr) {
				e2.genKra(pw);
			}else if(e2 instanceof ParenthesisExpr) {
				e2.genKra(pw);
			}else if(e2 instanceof PrimaryExpr) {
				pw.sub();
				pw.sub();
				e2.genKra(pw);
				pw.add();
				pw.add();
			}else if(e2 instanceof NullExpr) {
				e2.genKra(pw);
			}
			pw.println(";");
		}else{
			if(e instanceof PrimaryExpr) {
				e.genKra(pw);
				pw.println(";");
			}
			
		}
		pw.sub();
	}
	
	private Expr e;
	private Expr e2;
	private String op;
}
