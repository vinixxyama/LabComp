// Vinicius Yamamoto    490105

package ast;

public class ExprClass extends Expr {
	public ExprClass(KraClass aClass) {
		this.aClass = aClass;
	}
	@Override
	public void genC(PW pw, boolean putParenthesis) {
		// TODO Auto-generated method stub
	}
	@Override
	public Type getType() {
		return aClass;
	}
	
	public void genKra(PW pw) {
		pw.print(aClass.getCname()+"()");
	}
	private KraClass aClass;
}
