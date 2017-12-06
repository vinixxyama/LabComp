// Vinicius Yamamoto    490105

package ast;

public class Variable {

    public Variable( String name, Type type ) {
        this.name = name;
        this.type = type;
    }

    public String getName() { 
        return name; 
    }

    public Type getType() {
        return type;
    }
    
    public void getKra(PW pw) {
		pw.print(type.getName() + " " + name);	
	}

    protected String name;
    protected Type type;
}