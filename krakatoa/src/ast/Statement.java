// Vinicius Yamamoto    490105

package ast;

abstract public class Statement {
	abstract public void genC(PW pw);
	
	abstract public void genKra(PW pw);
}
