// Vinicius Yamamoto    490105

package ast;

public class PrimaryExpr extends Expr{
	
	
	public PrimaryExpr(String id, char op, Type tipo, boolean n) {
		this.id = id;
		this.op = op;
		this.tipo = tipo;
		this.n = n;
	}
	
	public PrimaryExpr(String id, String secid, char op, Type tipo, boolean n) {
		this.id = id;
		this.secid = secid;
		this.op = op;
		this.tipo = tipo;
		this.n = n;
	}
	
	public PrimaryExpr(String id, String secid, ExprList exlist, char op, Type tipo, boolean n) {
		this.id = id;
		this.secid = secid;
		this.exlist = exlist;
		this.op = op;
		this.tipo = tipo;
		this.n = n;
	}
	
	public void genKra(PW pw) {
		if(op == 't') {
			pw.printIdent("this."+ id);
			if(n == true) {
				pw.print("(");
				if(exlist != null) {
					exlist.genKra(pw);
				}
				pw.print(")");
			}
			if(secid != null) {
				pw.print("."+secid);
				pw.print("(");
				if(exlist != null) {
					exlist.genKra(pw);
				}
				pw.print(")");
			}
		}else if(op == 's') {
			pw.printIdent("super.");
			pw.print(id);
			pw.print("(");
			if(exlist != null) {
				exlist.genKra(pw);
			}
			pw.print(")");
		}else {
			pw.printIdent(id +"."+ secid + "(");
			if(exlist != null) {
				exlist.genKra(pw);
			}
			pw.print(")");
		}
	}
	
	@Override
	public void genC(PW pw, boolean putParenthesis) {
	}

	@Override
	public Type getType() {
		return tipo;
	}
	
	private boolean n;
	private String id;
	private String secid;
	private char op;
	private Type tipo;
	private ExprList exlist;
}
