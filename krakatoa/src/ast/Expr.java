// Vinicius Yamamoto    490105

package ast;

abstract public class Expr {
    abstract public void genC( PW pw, boolean putParenthesis );
      // new method: the type of the expression
    abstract public Type getType();
    
	public void genKra(PW pw) {
		// TODO Auto-generated method stub
	}
}