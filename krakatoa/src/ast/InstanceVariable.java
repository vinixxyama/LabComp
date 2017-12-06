// Vinicius Yamamoto    490105

package ast;

public class InstanceVariable extends Variable {

    public InstanceVariable( String name, Type type ) {
        super(name, type);
    }
    
    public String getName() {
    	return name;
    }
    
    public Type getType() {
    	return type;
    }
}