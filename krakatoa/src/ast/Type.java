// Vinicius Yamamoto    490105

package ast;

abstract public class Type {

    public Type( String name ) {
        this.name = name;
    }

    public static Type booleanType = new TypeBoolean();
    public static Type intType = new TypeInt();
    public static Type stringType = new TypeString();
    public static Type voidType = new TypeVoid();
    public static Type undefinedType = new TypeUndefined();
 

    public String getName() {
        return name;
    }

    abstract public String getCname();

    private String name;

	public boolean isCompatible(Type other) {
		if(this == booleanType) {
			return other == booleanType;
		}else if(this == intType){
			return other == intType;
		}else if(this == stringType) {
			return other == stringType;
		}else if(this == voidType) {
			return false;
		}else if(this instanceof KraClass) {
			return this == other || ((KraClass) this ).isSubclassOf(other);
		}
		else {
			return false;
		}
	}
}
