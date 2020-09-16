import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

// parameters names
// filePath

public class PythonScript {
	private String script;
	int indent;
	List<Parameter> parameters;

	public PythonScript(String s) {
		SimpleDateFormat formatter = new SimpleDateFormat("HH:mm:ss z', 'dd MMMM yyyy");
		script = "# Python script generated by MOSES at " + formatter.format(new Date(System.currentTimeMillis()));
		newLine();
		script += "# " + s;
		newLine();
		newLine();

		parameters = new ArrayList<Parameter>();
		indent = 0;
	}

	public void newLine() {
		script += "\r\n";
	}

	public void addScript(String s) {
		for (int i = 1; i <= indent; i++)
			script += "\t";
		script += s;
		newLine();
	}

	public String getScript() {
		return script;
	}

	public void addCommnet(String s) {
		BufferedReader reader = new BufferedReader(new StringReader(s));

		String line = null;
		try {
			while ((line = reader.readLine()) != null) {
				addScript("# " + line);
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static String addString(String s) {
		return "'" + s + "'";
	}

	public void importModule(String moduleName) {
		addScript("import " + moduleName);
	}

	public void importModule(String moduleName, String alternativeName) {
		addScript("import " + moduleName + " as " + alternativeName);
	}

	public void importModuleFrom(String moduleName, String moduleParent) {
		addScript("from " + moduleParent + " import " + moduleName);
	}

	public static String print(String s) {
		return callFunction("print", Arrays.asList(s, "flush = True"));
	}

	public void addParameter(String parameterName, String parameterType, String parameterValue) {
		parameters.add(new Parameter(parameterName, parameterType, parameterValue));
	}

	public void addParameter(List<Parameter> parameterList) {
		for (Parameter parameter : parameterList)
			addParameter(parameter.getName(), parameter.getType(), parameter.getValue());
	}

	public List<Parameter> getParameters() {
		return parameters;
	}

	public static String setValue(String leftName, String rightName) {
		return leftName + " = " + rightName;
	}

	public static String setValue(List<String> leftNames, String rightName) {
		String script = "";

		for (int index = 0; index < leftNames.size(); index++) {
			script += leftNames.get(index);

			if (index < leftNames.size() - 1)
				script += ", ";
		}

		script += " = " + rightName;
		return script;
	}

	public String createParameterDictionary() {
		List<Pair<String, String>> dictionaryElements = new ArrayList<Pair<String, String>>();

		for (int index = 0; index < parameters.size(); index++) {
			Parameter parameter = parameters.get(index);

			dictionaryElements
					.add(new Pair<>(parameter.getName(), parameter.getType() + "(" + "sys.argv[" + (index + 1) + "])"));
		}

		return createDictionary("parameters", dictionaryElements);
	}

	public static String createDictionary(String dictionaryName, List<Pair<String, String>> elements) {
		String script = dictionaryName + " = dict(";

		for (int index = 0; index < elements.size(); index++) {
			Pair<String, String> element = elements.get(index);

			script += setValue(element.getL(), element.getR());

			if (index < elements.size() - 1)
				script += ", ";
		}

		script += ")";

		return script;
	}

	public static String callFunction(String functionName, List<String> inputList) {
		String script = functionName + "(";
		for (int index = 0; index < inputList.size(); index++) {
			script += inputList.get(index);

			if (index < inputList.size() - 1)
				script += ", ";
		}

		script += ")";

		return script;
	}

	public static String callFunction(String functionName, String input) {
		return functionName + "(" + input + ")";
	}

	public static String callFunctionWithResult(String resultName, String functionName, List<String> inputList) {
		return setValue(resultName, callFunction(functionName, inputList));
	}

	public static String callFunctionWithResult(List<String> resultName, String functionName, List<String> inputList) {
		return setValue(resultName, callFunction(functionName, inputList));
	}

	public static String makeSaveList(List<Pair<String, String>> elements) {
		String script = "";

		for (int index = 0; index < elements.size(); index++) {
			Pair<String, String> element = elements.get(index);

			script += "'" + element.getL() + "' : " + element.getR();

			if (index < elements.size() - 1)
				script += ", ";
		}

		return "{" + script + "}";
	}

	public void startFor(String var, String start, String stop, String step) {
		addScript("for " + var + " in range(" + start + ", " + stop + ", " + step + "):");
		indent++;
	}

	public void startFor(String var, String stop) {
		addScript("for " + var + " in range(" + stop + "):");
		indent++;
	}

	public void stopFor() {
		indent--;
	}

	public void startIf(String cond) {
		addScript("if(" + cond + "):");
		indent++;
	}

	public void stopIf() {
		indent--;
	}

	public void startWhile(String cond) {
		addScript("while(" + cond + "):");
		indent++;
	}

	public void stopWhile() {
		indent--;
	}

	public void startElse() {
		addScript("else:");
		indent++;
	}

	public void stopElse() {
		indent--;
	}

	public void startElif(String cond) {
		addScript("elif(" + cond + "):");
		indent++;
	}

	public void stopElif() {
		indent--;
	}

	public static String andCondition(List<String> conditionList) {
		String script = "";

		for (int index = 0; index < conditionList.size(); index++) {
			script += conditionList.get(index);

			if (index < conditionList.size() - 1)
				script += " and ";
		}

		return script;

	}

	public void startFunction(String name, List<String> parameters) {
		addScript("def " + name + "(" + String.join(", ", parameters) + "):");
		indent++;
	}

	public void stopFunction() {
		indent--;
	}
}