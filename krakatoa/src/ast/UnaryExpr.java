// Vinicius Yamamoto    490105

package ast;

import lexer.*;

public class UnaryExpr extends Expr {

	public UnaryExpr(Expr expr, Symbol op) {
		this.expr = expr;
		this.op = op;
	}

	@Override
	public void genC(PW pw, boolean putParenthesis) {
		switch (op) {
		case PLUS:
			pw.print("+");
			break;
		case MINUS:
			pw.print("-");
			break;
		case NOT:
			pw.print("!");
			break;
		default:
			pw.print(" internal error at UnaryExpr::genC");

		}
		expr.genC(pw, false);
	}
	
	public void genKra(PW pw) {
		pw.print(op.toString());
		expr.genKra(pw);
	}
	@Override
	public Type getType() {
		return expr.getType();
	}

	private Expr	expr;
	private Symbol	op;
}
