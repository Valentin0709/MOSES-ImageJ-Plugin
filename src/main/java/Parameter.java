public class Parameter {
	private String name, type, value;

	public Parameter(String n, String t, Object v) {
		name = n;
		type = t;
		value = String.valueOf(v);
	}

	public String getName() {
		return name;
	}

	public String getType() {
		return type;
	}

	public String getValue() {
		return value;
	}
}
