
public class Matrix4Generator {

	private static char   MAKE_UPPER  = 0b11011111;
	private static char[] AXIS_LABELS = { 'x', 'y', 'z' };

	/////////// UTILS ///////////
	
	public static final String[] MULTIPLY = {
		"%m[0] * %0 + %m[4] * %1 + %m[8] * %2 + %m[12] * %3",
		"%m[1] * %0 + %m[5] * %1 + %m[9] * %2 + %m[13] * %3",
		"%m[2] * %0 + %m[6] * %1 + %m[10] * %2 + %m[14] * %3",
		"%m[3] * %0 + %m[7] * %1 + %m[11] * %2 + %m[15] * %3",
		"%m[0] * %4 + %m[4] * %5 + %m[8] * %6 + %m[12] * %7",
		"%m[1] * %4 + %m[5] * %5 + %m[9] * %6 + %m[13] * %7",
		"%m[2] * %4 + %m[6] * %5 + %m[10] * %6 + %m[14] * %7",
		"%m[3] * %4 + %m[7] * %5 + %m[11] * %6 + %m[15] * %7",
		"%m[0] * %8 + %m[4] * %9 + %m[8] * %10 + %m[12] * %11",
		"%m[1] * %8 + %m[5] * %9 + %m[9] * %10 + %m[13] * %11",
		"%m[2] * %8 + %m[6] * %9 + %m[10] * %10 + %m[14] * %11",
		"%m[3] * %8 + %m[7] * %9 + %m[11] * %10 + %m[15] * %11",
		"%m[0] * %12 + %m[4] * %13 + %m[8] * %14 + %m[12] * %15",
		"%m[1] * %12 + %m[5] * %13 + %m[9] * %14 + %m[13] * %15",
		"%m[2] * %12 + %m[6] * %13 + %m[10] * %14 + %m[14] * %15",
		"%m[3] * %12 + %m[7] * %13 + %m[11] * %14 + %m[15] * %15"
	};

	public static String[][] generateDirectModification(String[] multiplier, String prefix) {
		String[][] result = new String[2][16];
		// Extract values
		for (int i = 0; i < 16; ++i) {
			if (multiplier[i].matches("-?[\\w\\d.\\[\\]]*"))
				result[0][i] = null;
			else {
				result[0][i] = multiplier[i];
				multiplier[i] = "m" + i;
			}
		}
		// Construct the product
		for (int i = 0; i < 16; ++i) {
			result[1][i] = MULTIPLY[i].replace("%m", prefix);
			for (int j = 0; j < 16; ++j)
				result[1][i] = result[1][i].replaceAll("%" + j + "(?!\\d)", multiplier[j]);
			// Clean the product
			result[1][i] = result[1][i]
				.replaceAll(prefix + "\\[\\d\\d?\\] \\* 0", "")
				.replaceAll(prefix + "\\[(\\d\\d?)\\] \\* 1", prefix + "[$1]")
				.replaceAll("  \\+", "");
			if (result[1][i].startsWith(" + "))
				result[1][i] = result[1][i].substring(3);
			if (result[1][i].endsWith(" + "))
				result[1][i] = result[1][i].substring(0, result[1][i].length() - 3);
		}
		return result;
	}
	
	public static String generateDMMethod(String[] multiplier) {
		StringBuilder method = new StringBuilder();
		String[][] modifier = generateDirectModification(multiplier, "m");
		for (int i = 0; i < 16; ++i) {
			if (modifier[0][i] != null)
				method.append("\tauto m")
				      .append(i + (i < 10 ? " " : ""))
				      .append(" = ")
				      .append(modifier[0][i])
				      .append(";\n");
		}
		method.append("\tT newM[] = {\n");
		for (int i = 0; i < 16; ++i) {
			if (!modifier[1][i].matches("m\\[" + i + "]")) {
				method.append("\t\t")
				      .append(modifier[1][i]);
				if (i < 15)
					method.append(',');
				method.append('\n');
			}
			else
				modifier[1][i] = null;
		}
		method.append("\t};\n\t");
		int newMCounter = -1;
		for (int i = 0; i < 16; ++i) {
			if (modifier[1][i] == null)
				method.append("/**** m[")
				      .append(i + (i < 10 ? " " : ""))
				      .append("] ****/");
			else
				method.append("m[")
				      .append(i + (i < 10 ? " " : ""))
				      .append("] = newM[")
				      .append(++newMCounter + (newMCounter < 10 ? " " : ""))
				      .append("];");
			if (i > 0 && i % 4 == 3)
				method.append("\n\t");
			else
				method.append(' ');
		}
		return method.append("return *this;\n").toString();
	}
	
	public static String generateDMFunction(String[] multiplier) {
		StringBuilder method = new StringBuilder();
		String[][] modifier = generateDirectModification(multiplier, "matrix.m");
		for (int i = 0; i < 16; ++i) {
			if (modifier[0][i] != null)
				method.append("\tauto m")
				      .append(i + (i < 10 ? " " : ""))
				      .append(" = ")
				      .append(modifier[0][i])
				      .append(";\n");
		}
		method.append("\treturn new T[4 * 4] {\n");
		for (int i = 0; i < 16; ++i) {
			method.append("\t\t")
			      .append(modifier[1][i]);
			if (i < 15)
				method.append(',');
			method.append('\n');
		}
		return method.append("\t};\n").toString();
	}
	
	public static String generateDMTargetFunction(String[] multiplier) {
		StringBuilder method = new StringBuilder();
		String[][] modifier = generateDirectModification(multiplier, "matrix.m");
		for (int i = 0; i < 16; ++i) {
			if (modifier[0][i] != null)
				method.append("\tauto m")
				      .append(i + (i < 10 ? " " : ""))
				      .append(" = ")
				      .append(modifier[0][i])
				      .append(";\n");
		}
		for (int i = 0; i < 16; ++i) {
			method.append("\ttarget.m[")
			      .append(i)
			      .append("] = ")
			      .append(modifier[1][i])
			      .append(";\n");
		}
		return method.append("\treturn target;\n").toString();
	}
	
	/////////// TRANSLATES ///////////
	
	public static String translateMake(int[] permutation) {
		if (!Utils.isUnique(permutation))
			return null;
		StringBuilder method = new StringBuilder();
		if (Utils.isOrdered(permutation)) {
			method.append("static GenoMatrix<4, 4, T> makeTranslate");
			for (int i = 0; i < permutation.length; ++i)
				method.append((char) (AXIS_LABELS[permutation[i]] & MAKE_UPPER));
			method.append('(');
			for (int i = 0; i < permutation.length; ++i) {
				method.append("T translate")
			          .append((char) (AXIS_LABELS[permutation[i]] & MAKE_UPPER));
				if (i < permutation.length - 1)
					method.append(", ");
			}
			method.append(") {\n\treturn new T[4 * 4] {\n\t\t1, 0, 0, 0,\n\t\t0, 1, 0, 0,\n\t\t0, 0, 1, 0,\n\t\t")
			      .append(Utils.contains(permutation, 0) != -1 ? "translateX, " : "0, ")
			      .append(Utils.contains(permutation, 1) != -1 ? "translateY, " : "0, ")
			      .append(Utils.contains(permutation, 2) != -1 ? "translateZ, " : "0, ")
			      .append("1\n\t};\n}\n\n");
		}
		if (permutation.length > 1) {
			int indexX = Utils.contains(permutation, 0);
			int indexY = Utils.contains(permutation, 1);
			int indexZ = Utils.contains(permutation, 2);
			method.append("static GenoMatrix<4, 4, T> makeTranslate");
			for (int i = 0; i < permutation.length; ++i)
				method.append((char) (AXIS_LABELS[permutation[i]] & MAKE_UPPER));
			method.append("(const GenoVector<")
			      .append(permutation.length)
			      .append(", T> & translate) {\n\treturn new T[4 * 4] {\n\t\t1, 0, 0, 0,\n\t\t0, 1, 0, 0,\n\t\t0, 0, 1, 0,\n\t\t")
			      .append(indexX != -1 ? "translate.v[" + indexX + "], " : "0, ")
			      .append(indexY != -1 ? "translate.v[" + indexY + "], " : "0, ")
			      .append(indexZ != -1 ? "translate.v[" + indexZ + "], " : "0, ")
			      .append("1\n\t};\n}\n\n");
		}
		return method.toString();
	}
	
	public static String translateSet(int[] permutation) {
		if (!Utils.isUnique(permutation))
			return null;
		StringBuilder method = new StringBuilder();
		if (Utils.isOrdered(permutation)) {
			method.append("GenoMatrix<4, 4, T> & setTranslate");
			for (int i = 0; i < permutation.length; ++i)
				method.append((char) (AXIS_LABELS[permutation[i]] & MAKE_UPPER));
			method.append('(');
			for (int i = 0; i < permutation.length; ++i) {
				method.append("T translate")
			          .append((char) (AXIS_LABELS[permutation[i]] & MAKE_UPPER));
				if (i < permutation.length - 1)
					method.append(", ");
			}
			method.append(") {\n\tm[0 ] = 1;\n\tm[1 ] = 0;\n\tm[2 ] = 0;\n\tm[3 ] = 0;\n\tm[4 ] = 0;\n\tm[5 ] = 1;\n\tm[6 ] = 0;\n\tm[7 ] = 0;\n\tm[8 ] = 0;\n\tm[9 ] = 0;\n\tm[10] = 1;\n\tm[11] = 0;")
			      .append(Utils.contains(permutation, 0) != -1 ? "\n\tm[12] = translateX;" : "\n\tm[12] = 0;")
			      .append(Utils.contains(permutation, 1) != -1 ? "\n\tm[13] = translateY;" : "\n\tm[13] = 0;")
			      .append(Utils.contains(permutation, 2) != -1 ? "\n\tm[14] = translateZ;" : "\n\tm[14] = 0;")
			      .append("\n\tm[15] = 1;\n\treturn *this;\n}\n\n");
		}
		if (permutation.length > 1) {
			int indexX = Utils.contains(permutation, 0);
			int indexY = Utils.contains(permutation, 1);
			int indexZ = Utils.contains(permutation, 2);
			method.append("GenoMatrix<4, 4, T> & setTranslate");
			for (int i = 0; i < permutation.length; ++i)
				method.append((char) (AXIS_LABELS[permutation[i]] & MAKE_UPPER));
			method.append("(const GenoVector<")
			      .append(permutation.length)
			      .append(", T> & translate) {\n\tm[0 ] = 1;\n\tm[1 ] = 0;\n\tm[2 ] = 0;\n\tm[3 ] = 0;\n\tm[4 ] = 0;\n\tm[5 ] = 1;\n\tm[6 ] = 0;\n\tm[7 ] = 0;\n\tm[8 ] = 0;\n\tm[9 ] = 0;\n\tm[10] = 1;\n\tm[11] = 0;")
			      .append(indexX != -1 ? "\n\tm[12] = translate.v[" + indexX + "];" : "\n\tm[12] = 0;")
			      .append(indexY != -1 ? "\n\tm[13] = translate.v[" + indexY + "];" : "\n\tm[13] = 0;")
			      .append(indexZ != -1 ? "\n\tm[14] = translate.v[" + indexZ + "];" : "\n\tm[14] = 0;")
			      .append("\n\tm[15] = 1;\n\treturn *this;\n}\n\n");
		}
		return method.toString();
	}
	
	public static String translateMethods(int[] permutation) {
		if (!Utils.isUnique(permutation))
			return null;
		StringBuilder method = new StringBuilder();
		if (Utils.isOrdered(permutation)) {
			method.append("GenoMatrix<4, 4, T> & translate");
			for (int i = 0; i < permutation.length; ++i)
				method.append((char) (AXIS_LABELS[permutation[i]] & MAKE_UPPER));
			method.append('(');
			for (int i = 0; i < permutation.length; ++i) {
				method.append("T translate")
			          .append((char) (AXIS_LABELS[permutation[i]] & MAKE_UPPER));
				if (i < permutation.length - 1)
					method.append(", ");
			}
			String[] multiplier = new String[] {
				"1", "0", "0", "0",
				"0", "1", "0", "0",
				"0", "0", "1", "0",
				Utils.contains(permutation, 0) != -1 ? "translateX" : "0", Utils.contains(permutation, 1) != -1 ? "translateY" : "0", Utils.contains(permutation, 2) != -1 ? "translateZ" : "0", "1"
			};
			method.append(") {\n")
			      .append(generateDMMethod(multiplier))
			      .append("}\n\n");
		}
		if (permutation.length > 1) {
			int indexX = Utils.contains(permutation, 0);
			int indexY = Utils.contains(permutation, 1);
			int indexZ = Utils.contains(permutation, 2);
			method.append("GenoMatrix<4, 4, T> & translate");
			for (int i = 0; i < permutation.length; ++i)
				method.append((char) (AXIS_LABELS[permutation[i]] & MAKE_UPPER));
			String[] multiplier = new String[] {
					"1", "0", "0", "0",
					"0", "1", "0", "0",
					"0", "0", "1", "0",
					indexX != -1 ? "translate.v[" + indexX + ']': "0", indexY != -1 ? "translate.v[" + indexY + ']' : "0", indexZ != -1 ? "translate.v[" + indexZ + ']' : "0", "1"
				};
			method.append("(const GenoVector<")
			      .append(permutation.length)
			      .append(", T> & translate) {\n")
			      .append(generateDMMethod(multiplier))
			      .append("}\n\n");
		}
		return method.toString();
	}
	
	public static String translateFunctions(int[] permutation) {
		if (!Utils.isUnique(permutation))
			return null;
		StringBuilder method = new StringBuilder();
		if (Utils.isOrdered(permutation)) {
			method.append("template <typename T>\nGenoMatrix<4, 4, T> translate");
			for (int i = 0; i < permutation.length; ++i)
				method.append((char) (AXIS_LABELS[permutation[i]] & MAKE_UPPER));
			method.append("(const GenoMatrix<4, 4, T> & matrix");
			for (int i = 0; i < permutation.length; ++i) {
				method.append(", T translate")
			          .append((char) (AXIS_LABELS[permutation[i]] & MAKE_UPPER));
			}
			String[] multiplier = new String[] {
				"1", "0", "0", "0",
				"0", "1", "0", "0",
				"0", "0", "1", "0",
				Utils.contains(permutation, 0) != -1 ? "translateX" : "0", Utils.contains(permutation, 1) != -1 ? "translateY" : "0", Utils.contains(permutation, 2) != -1 ? "translateZ" : "0", "1"
			};
			method.append(") {\n")
			      .append(generateDMFunction(multiplier))
			      .append("}\n\n");
		}
		if (permutation.length > 1) {
			int indexX = Utils.contains(permutation, 0);
			int indexY = Utils.contains(permutation, 1);
			int indexZ = Utils.contains(permutation, 2);
			method.append("template <typename T>\nGenoMatrix<4, 4, T> translate");
			for (int i = 0; i < permutation.length; ++i)
				method.append((char) (AXIS_LABELS[permutation[i]] & MAKE_UPPER));
			String[] multiplier = new String[] {
				"1", "0", "0", "0",
				"0", "1", "0", "0",
				"0", "0", "1", "0",
				indexX != -1 ? "translate.v[" + indexX + ']': "0", indexY != -1 ? "translate.v[" + indexY + ']' : "0", indexZ != -1 ? "translate.v[" + indexZ + ']' : "0", "1"
			};
			method.append("(const GenoMatrix<4, 4, T> & matrix, const GenoVector<")
			      .append(permutation.length)
			      .append(", T> & translate) {\n")
			      .append(generateDMFunction(multiplier))
			      .append("}\n\n");
		}
		return method.toString();
	}
	
	public static String translateTargetFunctions(int[] permutation) {
		if (!Utils.isUnique(permutation))
			return null;
		StringBuilder method = new StringBuilder();
		if (Utils.isOrdered(permutation)) {
			method.append("template <typename T>\nGenoMatrix<4, 4, T> & translate");
			for (int i = 0; i < permutation.length; ++i)
				method.append((char) (AXIS_LABELS[permutation[i]] & MAKE_UPPER));
			method.append("(const GenoMatrix<4, 4, T> & matrix");
			for (int i = 0; i < permutation.length; ++i) {
				method.append(", T translate")
			          .append((char) (AXIS_LABELS[permutation[i]] & MAKE_UPPER));
			}
			String[] multiplier = new String[] {
				"1", "0", "0", "0",
				"0", "1", "0", "0",
				"0", "0", "1", "0",
				Utils.contains(permutation, 0) != -1 ? "translateX" : "0", Utils.contains(permutation, 1) != -1 ? "translateY" : "0", Utils.contains(permutation, 2) != -1 ? "translateZ" : "0", "1"
			};
			method.append(", GenoMatrix<4, 4, T> & target) {\n")
			      .append(generateDMTargetFunction(multiplier))
			      .append("}\n\n");
		}
		if (permutation.length > 1) {
			int indexX = Utils.contains(permutation, 0);
			int indexY = Utils.contains(permutation, 1);
			int indexZ = Utils.contains(permutation, 2);
			method.append("template <typename T>\nGenoMatrix<4, 4, T> & translate");
			for (int i = 0; i < permutation.length; ++i)
				method.append((char) (AXIS_LABELS[permutation[i]] & MAKE_UPPER));
			String[] multiplier = new String[] {
				"1", "0", "0", "0",
				"0", "1", "0", "0",
				"0", "0", "1", "0",
				indexX != -1 ? "translate.v[" + indexX + ']': "0", indexY != -1 ? "translate.v[" + indexY + ']' : "0", indexZ != -1 ? "translate.v[" + indexZ + ']' : "0", "1"
			};
			method.append("(const GenoMatrix<4, 4, T> & matrix, const GenoVector<")
			      .append(permutation.length)
			      .append(", T> & translate, GenoMatrix<4, 4, T> & target) {\n")
			      .append(generateDMTargetFunction(multiplier))
			      .append("}\n\n");
		}
		return method.toString();
	}
	
	/////////// ROTATES ///////////
	
	public static String rotateMake(int[] permutation) {
		if (!Utils.isUnique(permutation))
			return null;
		StringBuilder method = new StringBuilder();
		String[][] coefficients = Utils.getCoefficients(permutation);
		if (Utils.isOrdered(permutation)) {
			method.append("static GenoMatrix<4, 4, T> makeRotate");
			for (int i = 0; i < permutation.length; ++i)
				method.append((char) (AXIS_LABELS[permutation[i]] & MAKE_UPPER));
			method.append('(');
			for (int i = 0; i < permutation.length; ++i) {
				method.append("T rotate")
			          .append((char) (AXIS_LABELS[permutation[i]] & MAKE_UPPER));
				if (i < permutation.length - 1)
					method.append(", ");
			}
			method.append(") {\n")
			      .append(Utils.requiredTrig(permutation)
			               .replace("%0", "rotateX")
			               .replace("%1", "rotateY")
			               .replace("%2", "rotateZ"))
			      .append("\treturn new T[4 * 4] {\n\t\t")
			      .append(coefficients[0][0])
			      .append(", ")
			      .append(coefficients[1][0])
			      .append(", ")
			      .append(coefficients[2][0])
			      .append(", 0,\n\t\t")
			      .append(coefficients[0][1])
			      .append(", ")
			      .append(coefficients[1][1])
			      .append(", ")
			      .append(coefficients[2][1])
			      .append(", 0,\n\t\t")
			      .append(coefficients[0][2])
			      .append(", ")
			      .append(coefficients[1][2])
			      .append(", ")
			      .append(coefficients[2][2])
			      .append(", 0,\n\t\t0, 0, 0, 1\n\t};\n}\n\n");
		}
		if (permutation.length > 1) {
			method.append("static GenoMatrix<4, 4, T> makeRotate");
			for (int i = 0; i < permutation.length; ++i)
				method.append((char) (AXIS_LABELS[permutation[i]] & MAKE_UPPER));
			method.append("(const GenoVector<")
			      .append(permutation.length)
			      .append(", T> & rotate) {\n")
			      .append(Utils.requiredTrig(permutation)
		                   .replace("%0", "rotate.v[" + Utils.contains(permutation, 0) + ']')
		                   .replace("%1", "rotate.v[" + Utils.contains(permutation, 1) + ']')
		                   .replace("%2", "rotate.v[" + Utils.contains(permutation, 2) + ']'))
			      .append("\treturn new T[4 * 4] {\n\t\t")
			      .append(coefficients[0][0])
			      .append(", ")
			      .append(coefficients[1][0])
			      .append(", ")
			      .append(coefficients[2][0])
			      .append(", 0,\n\t\t")
			      .append(coefficients[0][1])
			      .append(", ")
			      .append(coefficients[1][1])
			      .append(", ")
			      .append(coefficients[2][1])
			      .append(", 0,\n\t\t")
			      .append(coefficients[0][2])
			      .append(", ")
			      .append(coefficients[1][2])
			      .append(", ")
			      .append(coefficients[2][2])
			      .append(", 0,\n\t\t0, 0, 0, 1\n\t};\n}\n\n");
		}
		return method.toString();
	}
	
	public static String rotateSet(int[] permutation) {
		if (!Utils.isUnique(permutation))
			return null;
		StringBuilder method = new StringBuilder();
		String[][] coefficients = Utils.getCoefficients(permutation);
		if (Utils.isOrdered(permutation)) {
			method.append("GenoMatrix<4, 4, T> & setRotate");
			for (int i = 0; i < permutation.length; ++i)
				method.append((char) (AXIS_LABELS[permutation[i]] & MAKE_UPPER));
			method.append('(');
			for (int i = 0; i < permutation.length; ++i) {
				method.append("T rotate")
			          .append((char) (AXIS_LABELS[permutation[i]] & MAKE_UPPER));
				if (i < permutation.length - 1)
					method.append(", ");
			}
			method.append(") {\n")
			      .append(Utils.requiredTrig(permutation)
			               .replace("%0", "rotateX")
			               .replace("%1", "rotateY")
			               .replace("%2", "rotateZ"))
			      .append("\tm[0 ] = ")
			      .append(coefficients[0][0])
			      .append(";\n\tm[1 ] = ")
			      .append(coefficients[1][0])
			      .append(";\n\tm[2 ] = ")
			      .append(coefficients[2][0])
			      .append(";\n\tm[3 ] = 0;\n\tm[4 ] = ")
			      .append(coefficients[0][1])
			      .append(";\n\tm[5 ] = ")
			      .append(coefficients[1][1])
			      .append(";\n\tm[6 ] = ")
			      .append(coefficients[2][1])
			      .append(";\n\tm[7 ] = 0;\n\tm[8 ] = ")
			      .append(coefficients[0][2])
			      .append(";\n\tm[9 ] = ")
			      .append(coefficients[1][2])
			      .append(";\n\tm[10] = ")
			      .append(coefficients[2][2])
			      .append(";\n\tm[11] = 0;\n\tm[12] = 0;\n\tm[13] = 0;\n\tm[14] = 0;\n\tm[15] = 1;\n\treturn *this;\n}\n\n");
		}
		if (permutation.length > 1) {
			method.append("GenoMatrix<4, 4, T> & setRotate");
			for (int i = 0; i < permutation.length; ++i)
				method.append((char) (AXIS_LABELS[permutation[i]] & MAKE_UPPER));
			method.append("(const GenoVector<")
			      .append(permutation.length)
			      .append(", T> & rotate) {\n")
			      .append(Utils.requiredTrig(permutation)
		                   .replace("%0", "rotate.v[" + Utils.contains(permutation, 0) + ']')
		                   .replace("%1", "rotate.v[" + Utils.contains(permutation, 1) + ']')
		                   .replace("%2", "rotate.v[" + Utils.contains(permutation, 2) + ']'))
			      .append("\tm[0 ] = ")
			      .append(coefficients[0][0])
			      .append(";\n\tm[1 ] = ")
			      .append(coefficients[1][0])
			      .append(";\n\tm[2 ] = ")
			      .append(coefficients[2][0])
			      .append(";\n\tm[3 ] = 0;\n\tm[4 ] = ")
			      .append(coefficients[0][1])
			      .append(";\n\tm[5 ] = ")
			      .append(coefficients[1][1])
			      .append(";\n\tm[6 ] = ")
			      .append(coefficients[2][1])
			      .append(";\n\tm[7 ] = 0;\n\tm[8 ] = ")
			      .append(coefficients[0][2])
			      .append(";\n\tm[9 ] = ")
			      .append(coefficients[1][2])
			      .append(";\n\tm[10] = ")
			      .append(coefficients[2][2])
			      .append(";\n\tm[11] = 0;\n\tm[12] = 0;\n\tm[13] = 0;\n\tm[14] = 0;\n\tm[15] = 1;\n\treturn *this;\n}\n\n");
		}
		return method.toString();
	}
	
	public static String rotateMethods(int[] permutation) {
		if (!Utils.isUnique(permutation))
			return null;
		StringBuilder method = new StringBuilder();
		String[][] coefficients = Utils.getCoefficients(permutation);
		if (Utils.isOrdered(permutation)) {
			method.append("GenoMatrix<4, 4, T> & rotate");
			for (int i = 0; i < permutation.length; ++i)
				method.append((char) (AXIS_LABELS[permutation[i]] & MAKE_UPPER));
			method.append('(');
			for (int i = 0; i < permutation.length; ++i) {
				method.append("T rotate")
			          .append((char) (AXIS_LABELS[permutation[i]] & MAKE_UPPER));
				if (i < permutation.length - 1)
					method.append(", ");
			}
			String[] multiplier = new String[] {
				coefficients[0][0], coefficients[1][0], coefficients[2][0], "0",
				coefficients[0][1], coefficients[1][1], coefficients[2][1], "0",
				coefficients[0][2], coefficients[1][2], coefficients[2][2], "0",
				"0", "0", "0", "1"
			};
			method.append(") {\n")
			      .append(Utils.requiredTrig(permutation)
			               .replace("%0", "rotateX")
			               .replace("%1", "rotateY")
			               .replace("%2", "rotateZ"))
			      .append(generateDMMethod(multiplier))
			      .append("}\n\n");
		}
		if (permutation.length > 1) {
			method.append("GenoMatrix<4, 4, T> & rotate");
			for (int i = 0; i < permutation.length; ++i)
				method.append((char) (AXIS_LABELS[permutation[i]] & MAKE_UPPER));
			String[] multiplier = new String[] {
					coefficients[0][0], coefficients[1][0], coefficients[2][0], "0",
					coefficients[0][1], coefficients[1][1], coefficients[2][1], "0",
					coefficients[0][2], coefficients[1][2], coefficients[2][2], "0",
					"0", "0", "0", "1"
				};
			method.append("(const GenoVector<")
			      .append(permutation.length)
			      .append(", T> & rotate) {\n")
			      .append(Utils.requiredTrig(permutation)
		                   .replace("%0", "rotate.v[" + Utils.contains(permutation, 0) + ']')
		                   .replace("%1", "rotate.v[" + Utils.contains(permutation, 1) + ']')
		                   .replace("%2", "rotate.v[" + Utils.contains(permutation, 2) + ']'))
			      .append(generateDMMethod(multiplier))
			      .append("}\n\n");
		}
		return method.toString();
	}
	
	public static String rotateFunctions(int[] permutation) {
		if (!Utils.isUnique(permutation))
			return null;
		StringBuilder method = new StringBuilder();
		String[][] coefficients = Utils.getCoefficients(permutation);
		if (Utils.isOrdered(permutation)) {
			method.append("template <typename T>\nGenoMatrix<4, 4, T> rotate");
			for (int i = 0; i < permutation.length; ++i)
				method.append((char) (AXIS_LABELS[permutation[i]] & MAKE_UPPER));
			method.append("(const GenoMatrix<4, 4, T> & matrix");
			for (int i = 0; i < permutation.length; ++i) {
				method.append(", T rotate")
			          .append((char) (AXIS_LABELS[permutation[i]] & MAKE_UPPER));
			}
			String[] multiplier = new String[] {
				coefficients[0][0], coefficients[1][0], coefficients[2][0], "0",
				coefficients[0][1], coefficients[1][1], coefficients[2][1], "0",
				coefficients[0][2], coefficients[1][2], coefficients[2][2], "0",
				"0", "0", "0", "1"
			};
			method.append(") {\n")
			      .append(Utils.requiredTrig(permutation)
			               .replace("%0", "rotateX")
			               .replace("%1", "rotateY")
			               .replace("%2", "rotateZ"))
			      .append(generateDMFunction(multiplier))
			      .append("}\n\n");
		}
		if (permutation.length > 1) {
			method.append("template <typename T>\nGenoMatrix<4, 4, T> & rotate");
			for (int i = 0; i < permutation.length; ++i)
				method.append((char) (AXIS_LABELS[permutation[i]] & MAKE_UPPER));
			String[] multiplier = new String[] {
					coefficients[0][0], coefficients[1][0], coefficients[2][0], "0",
					coefficients[0][1], coefficients[1][1], coefficients[2][1], "0",
					coefficients[0][2], coefficients[1][2], coefficients[2][2], "0",
					"0", "0", "0", "1"
				};
			method.append("(const GenoMatrix<4, 4, T> & matrix, const GenoVector<")
			      .append(permutation.length)
			      .append(", T> & rotate) {\n")
			      .append(Utils.requiredTrig(permutation)
		                   .replace("%0", "rotate.v[" + Utils.contains(permutation, 0) + ']')
		                   .replace("%1", "rotate.v[" + Utils.contains(permutation, 1) + ']')
		                   .replace("%2", "rotate.v[" + Utils.contains(permutation, 2) + ']'))
			      .append(generateDMFunction(multiplier))
			      .append("}\n\n");
		}
		return method.toString();
	}
	
	public static String rotateTargetFunctions(int[] permutation) {
		if (!Utils.isUnique(permutation))
			return null;
		StringBuilder method = new StringBuilder();
		String[][] coefficients = Utils.getCoefficients(permutation);
		if (Utils.isOrdered(permutation)) {
			method.append("template <typename T>\nGenoMatrix<4, 4, T> & rotate");
			for (int i = 0; i < permutation.length; ++i)
				method.append((char) (AXIS_LABELS[permutation[i]] & MAKE_UPPER));
			method.append("(const GenoMatrix<4, 4, T> & matrix");
			for (int i = 0; i < permutation.length; ++i) {
				method.append(", T rotate")
			          .append((char) (AXIS_LABELS[permutation[i]] & MAKE_UPPER));
			}
			String[] multiplier = new String[] {
				coefficients[0][0], coefficients[1][0], coefficients[2][0], "0",
				coefficients[0][1], coefficients[1][1], coefficients[2][1], "0",
				coefficients[0][2], coefficients[1][2], coefficients[2][2], "0",
				"0", "0", "0", "1"
			};
			method.append(", GenoMatrix<4, 4, T> & target) {\n")
			      .append(Utils.requiredTrig(permutation)
			               .replace("%0", "rotateX")
			               .replace("%1", "rotateY")
			               .replace("%2", "rotateZ"))
			      .append(generateDMTargetFunction(multiplier))
			      .append("}\n\n");
		}
		if (permutation.length > 1) {
			method.append("template <typename T>\nGenoMatrix<4, 4, T> & rotate");
			for (int i = 0; i < permutation.length; ++i)
				method.append((char) (AXIS_LABELS[permutation[i]] & MAKE_UPPER));
			String[] multiplier = new String[] {
					coefficients[0][0], coefficients[1][0], coefficients[2][0], "0",
					coefficients[0][1], coefficients[1][1], coefficients[2][1], "0",
					coefficients[0][2], coefficients[1][2], coefficients[2][2], "0",
					"0", "0", "0", "1"
				};
			method.append("(const GenoMatrix<4, 4, T> & matrix, const GenoVector<")
			      .append(permutation.length)
			      .append(", T> & rotate, GenoMatrix<4, 4, T> & target) {\n")
			      .append(Utils.requiredTrig(permutation)
		                   .replace("%0", "rotate.v[" + Utils.contains(permutation, 0) + ']')
		                   .replace("%1", "rotate.v[" + Utils.contains(permutation, 1) + ']')
		                   .replace("%2", "rotate.v[" + Utils.contains(permutation, 2) + ']'))
			      .append(generateDMTargetFunction(multiplier))
			      .append("}\n\n");
		}
		return method.toString();
	}
	
	/////////// SCALES ///////////
	
	public static String scaleMake(int[] permutation) {
		if (!Utils.isUnique(permutation))
			return null;
		StringBuilder method = new StringBuilder();
		if (permutation.length > 1 && Utils.isOrdered(permutation)) {
			method.append("static GenoMatrix<4, 4, T> makeScale");
			for (int i = 0; i < permutation.length; ++i)
				method.append((char) (AXIS_LABELS[permutation[i]] & MAKE_UPPER));
			method.append("(T scale) {\n\treturn new T[4 * 4] {\n\t\t")
			      .append(Utils.contains(permutation, 0) != -1 ? "scale" : "1")
			      .append(", 0, 0, 0,\n\t\t0, ")
			      .append(Utils.contains(permutation, 1) != -1 ? "scale" : "1")
			      .append(", 0, 0,\n\t\t0, 0, ")
			      .append(Utils.contains(permutation, 2) != -1 ? "scale" : "1")
			      .append(", 0,\n\t\t0, 0, 0, 1\n\t};\n}\n\n");
		}
		if (Utils.isOrdered(permutation)) {
			method.append("static GenoMatrix<4, 4, T> makeScale");
			for (int i = 0; i < permutation.length; ++i)
				method.append((char) (AXIS_LABELS[permutation[i]] & MAKE_UPPER));
			method.append('(');
			for (int i = 0; i < permutation.length; ++i) {
				method.append("T scale")
			          .append((char) (AXIS_LABELS[permutation[i]] & MAKE_UPPER));
				if (i < permutation.length - 1)
					method.append(", ");
			}
			method.append(") {\n\treturn new T[4 * 4] {\n\t\t")
			      .append(Utils.contains(permutation, 0) != -1 ? "scaleX" : "1")
			      .append(", 0, 0, 0,\n\t\t0, ")
			      .append(Utils.contains(permutation, 1) != -1 ? "scaleY" : "1")
			      .append(", 0, 0,\n\t\t0, 0, ")
			      .append(Utils.contains(permutation, 2) != -1 ? "scaleZ" : "1")
			      .append(", 0,\n\t\t0, 0, 0, 1\n\t};\n}\n\n");
		}
		if (permutation.length > 1) {
			int indexX = Utils.contains(permutation, 0);
			int indexY = Utils.contains(permutation, 1);
			int indexZ = Utils.contains(permutation, 2);
			method.append("static GenoMatrix<4, 4, T> makeScale");
			for (int i = 0; i < permutation.length; ++i)
				method.append((char) (AXIS_LABELS[permutation[i]] & MAKE_UPPER));
			method.append("(const GenoVector<")
			      .append(permutation.length)
			      .append(", T> & scale) {\n\treturn new T[4 * 4] {\n\t\t")
			      .append(indexX != -1 ? "scale.v[" + indexX + "]": "1")
			      .append(", 0, 0, 0,\n\t\t0, ")
			      .append(indexY != -1 ? "scale.v[" + indexY + "]": "1")
			      .append(", 0, 0,\n\t\t0, 0, ")
			      .append(indexZ != -1 ? "scale.v[" + indexZ + "]": "1")
			      .append(", 0,\n\t\t0, 0, 0, 1\n\t};\n}\n\n");
		}
		return method.toString();
	}
	
	public static String scaleSet(int[] permutation) {
		if (!Utils.isUnique(permutation))
			return null;
		StringBuilder method = new StringBuilder();
		if (permutation.length > 1 && Utils.isOrdered(permutation)) {
			method.append("GenoMatrix<4, 4, T> & setScale");
			for (int i = 0; i < permutation.length; ++i)
				method.append((char) (AXIS_LABELS[permutation[i]] & MAKE_UPPER));
			method.append("(T scale) {\n\t")
			      .append(Utils.contains(permutation, 0) != -1 ? "m[0 ] = scale" : "m[0 ] = 1")
			      .append(";\n\tm[1 ] = 0;\n\tm[2 ] = 0;\n\tm[3 ] = 0;\n\tm[4 ] = 0;\n\t")
			      .append(Utils.contains(permutation, 1) != -1 ? "m[5 ] = scale" : "m[5 ] = 1")
			      .append(";\n\tm[6 ] = 0;\n\tm[7 ] = 0;\n\tm[8 ] = 0;\n\tm[9 ] = 0;\n\t")
			      .append(Utils.contains(permutation, 2) != -1 ? "m[10] = scale" : "m[10] = 1")
			      .append(";\n\tm[11] = 0;\n\tm[12] = 0;\n\tm[13] = 0;\n\tm[14] = 0;\n\tm[15] = 1;\n\treturn *this;\n}\n\n");
		}
		if (Utils.isOrdered(permutation)) {
			method.append("GenoMatrix<4, 4, T> & setScale");
			for (int i = 0; i < permutation.length; ++i)
				method.append((char) (AXIS_LABELS[permutation[i]] & MAKE_UPPER));
			method.append('(');
			for (int i = 0; i < permutation.length; ++i) {
				method.append("T scale")
			          .append((char) (AXIS_LABELS[permutation[i]] & MAKE_UPPER));
				if (i < permutation.length - 1)
					method.append(", ");
			}
			method.append(") {\n\t")
			      .append(Utils.contains(permutation, 0) != -1 ? "m[0 ] = scaleX" : "m[0 ] = 1")
			      .append(";\n\tm[1 ] = 0;\n\tm[2 ] = 0;\n\tm[3 ] = 0;\n\tm[4 ] = 0;\n\t")
			      .append(Utils.contains(permutation, 1) != -1 ? "m[5 ] = scaleY" : "m[5 ] = 1")
			      .append(";\n\tm[6 ] = 0;\n\tm[7 ] = 0;\n\tm[8 ] = 0;\n\tm[9 ] = 0;\n\t")
			      .append(Utils.contains(permutation, 2) != -1 ? "m[10] = scaleZ" : "m[10] = 1")
			      .append(";\n\tm[11] = 0;\n\tm[12] = 0;\n\tm[13] = 0;\n\tm[14] = 0;\n\tm[15] = 1;\n\treturn *this;\n}\n\n");
		}
		if (permutation.length > 1) {
			int indexX = Utils.contains(permutation, 0);
			int indexY = Utils.contains(permutation, 1);
			int indexZ = Utils.contains(permutation, 2);
			method.append("GenoMatrix<4, 4, T> & setScale");
			for (int i = 0; i < permutation.length; ++i)
				method.append((char) (AXIS_LABELS[permutation[i]] & MAKE_UPPER));
			method.append("(const GenoVector<")
			      .append(permutation.length)
			      .append(", T> & scale) {\n\t")
			      .append(indexX != -1 ? "m[0 ] = scale.v[" + indexX + "]" : "m[0 ] = 1")
			      .append(";\n\tm[1 ] = 0;\n\tm[2 ] = 0;\n\tm[3 ] = 0;\n\tm[4 ] = 0;\n\t")
			      .append(indexY != -1 ? "m[5 ] = scale.v[" + indexY + "]" : "m[5 ] = 1")
			      .append(";\n\tm[6 ] = 0;\n\tm[7 ] = 0;\n\tm[8 ] = 0;\n\tm[9 ] = 0;\n\t")
			      .append(indexZ != -1 ? "m[10] = scale.v[" + indexZ + "]" : "m[10] = 1")
			      .append(";\n\tm[11] = 0;\n\tm[12] = 0;\n\tm[13] = 0;\n\tm[14] = 0;\n\tm[15] = 1;\n\treturn *this;\n}\n\n");
		}
		return method.toString();
	}
	
	public static String scaleMethods(int[] permutation) {
		if (!Utils.isUnique(permutation))
			return null;
		StringBuilder method = new StringBuilder();
		if (permutation.length > 1 && Utils.isOrdered(permutation)) {
			method.append("GenoMatrix<4, 4, T> & scale");
			for (int i = 0; i < permutation.length; ++i)
				method.append((char) (AXIS_LABELS[permutation[i]] & MAKE_UPPER));
			String[] multiplier = new String[] {
				Utils.contains(permutation, 0) != -1 ? "scale": "1", "0", "0", "0",
				"0", Utils.contains(permutation, 1) != -1 ? "scale" : "1", "0", "0",
				"0", "0", Utils.contains(permutation, 2) != -1 ? "scale" : "1", "0",
				"0", "0", "0", "1"
			};
			method.append("(T scale) {\n")
			      .append(generateDMMethod(multiplier))
			      .append("}\n\n");
		}
		if (Utils.isOrdered(permutation)) {
			method.append("GenoMatrix<4, 4, T> & scale");
			for (int i = 0; i < permutation.length; ++i)
				method.append((char) (AXIS_LABELS[permutation[i]] & MAKE_UPPER));
			method.append('(');
			for (int i = 0; i < permutation.length; ++i) {
				method.append("T scale")
			          .append((char) (AXIS_LABELS[permutation[i]] & MAKE_UPPER));
				if (i < permutation.length - 1)
					method.append(", ");
			}
			String[] multiplier = new String[] {
				Utils.contains(permutation, 0) != -1 ? "scaleX": "1", "0", "0", "0",
				"0", Utils.contains(permutation, 1) != -1 ? "scaleY" : "1", "0", "0",
				"0", "0", Utils.contains(permutation, 2) != -1 ? "scaleZ" : "1", "0",
				"0", "0", "0", "1"
			};
			method.append(") {\n")
			      .append(generateDMMethod(multiplier))
			      .append("}\n\n");
		}
		if (permutation.length > 1) {
			int indexX = Utils.contains(permutation, 0);
			int indexY = Utils.contains(permutation, 1);
			int indexZ = Utils.contains(permutation, 2);
			method.append("GenoMatrix<4, 4, T> & scale");
			for (int i = 0; i < permutation.length; ++i)
				method.append((char) (AXIS_LABELS[permutation[i]] & MAKE_UPPER));
			String[] multiplier = new String[] {
				indexX != -1 ? "scale.v[" + indexX + ']': "1", "0", "0", "0",
				"0", indexY != -1 ? "scale.v[" + indexY + ']' : "1", "0", "0",
				"0", "0", indexZ != -1 ? "scale.v[" + indexZ + ']' : "1", "0",
				"0", "0", "0", "1"
			};
			method.append("(const GenoVector<")
			      .append(permutation.length)
			      .append(", T> & scale) {\n")
			      .append(generateDMMethod(multiplier))
			      .append("}\n\n");
		}
		return method.toString();
	}
	
	public static String scaleFunctions(int[] permutation) {
		if (!Utils.isUnique(permutation))
			return null;
		StringBuilder method = new StringBuilder();
		if (permutation.length > 1 && Utils.isOrdered(permutation)) {
			method.append("template <typename T>\nGenoMatrix<4, 4, T> scale");
			for (int i = 0; i < permutation.length; ++i)
				method.append((char) (AXIS_LABELS[permutation[i]] & MAKE_UPPER));
			String[] multiplier = new String[] {
				Utils.contains(permutation, 0) != -1 ? "scale": "1", "0", "0", "0",
				"0", Utils.contains(permutation, 1) != -1 ? "scale" : "1", "0", "0",
				"0", "0", Utils.contains(permutation, 2) != -1 ? "scale" : "1", "0",
				"0", "0", "0", "1"
			};
			method.append("(const GenoMatrix<4, 4, T> & matrix, T scale) {\n")
			      .append(generateDMFunction(multiplier))
			      .append("}\n\n");
		}
		if (Utils.isOrdered(permutation)) {
			method.append("template <typename T>\nGenoMatrix<4, 4, T> scale");
			for (int i = 0; i < permutation.length; ++i)
				method.append((char) (AXIS_LABELS[permutation[i]] & MAKE_UPPER));
			method.append("(const GenoMatrix<4, 4, T> & matrix");
			for (int i = 0; i < permutation.length; ++i) {
				method.append(", T scale")
			          .append((char) (AXIS_LABELS[permutation[i]] & MAKE_UPPER));
			}
			String[] multiplier = new String[] {
				Utils.contains(permutation, 0) != -1 ? "scaleX": "1", "0", "0", "0",
				"0", Utils.contains(permutation, 1) != -1 ? "scaleY" : "1", "0", "0",
				"0", "0", Utils.contains(permutation, 2) != -1 ? "scaleZ" : "1", "0",
				"0", "0", "0", "1"
			};
			method.append(") {\n")
			      .append(generateDMFunction(multiplier))
			      .append("}\n\n");
		}
		if (permutation.length > 1) {
			int indexX = Utils.contains(permutation, 0);
			int indexY = Utils.contains(permutation, 1);
			int indexZ = Utils.contains(permutation, 2);
			method.append("template <typename T>\nGenoMatrix<4, 4, T> scale");
			for (int i = 0; i < permutation.length; ++i)
				method.append((char) (AXIS_LABELS[permutation[i]] & MAKE_UPPER));
			String[] multiplier = new String[] {
				indexX != -1 ? "scale.v[" + indexX + ']': "1", "0", "0", "0",
				"0", indexY != -1 ? "scale.v[" + indexY + ']' : "1", "0", "0",
				"0", "0", indexZ != -1 ? "scale.v[" + indexZ + ']' : "1", "0",
				"0", "0", "0", "1"
			};
			method.append("(const GenoMatrix<4, 4, T> & matrix, const GenoVector<")
			      .append(permutation.length)
			      .append(", T> & scale) {\n")
			      .append(generateDMFunction(multiplier))
			      .append("}\n\n");
		}
		return method.toString();
	}
	
	public static String scaleTargetFunctions(int[] permutation) {
		if (!Utils.isUnique(permutation))
			return null;
		StringBuilder method = new StringBuilder();
		if (permutation.length > 1 && Utils.isOrdered(permutation)) {
			method.append("template <typename T>\nGenoMatrix<4, 4, T> & scale");
			for (int i = 0; i < permutation.length; ++i)
				method.append((char) (AXIS_LABELS[permutation[i]] & MAKE_UPPER));
			String[] multiplier = new String[] {
				Utils.contains(permutation, 0) != -1 ? "scale": "1", "0", "0", "0",
				"0", Utils.contains(permutation, 1) != -1 ? "scale" : "1", "0", "0",
				"0", "0", Utils.contains(permutation, 2) != -1 ? "scale" : "1", "0",
				"0", "0", "0", "1"
			};
			method.append("(const GenoMatrix<4, 4, T> & matrix, T scale, GenoMatrix<4, 4, T> & target) {\n")
			      .append(generateDMTargetFunction(multiplier))
			      .append("}\n\n");
		}
		if (Utils.isOrdered(permutation)) {
			method.append("template <typename T>\nGenoMatrix<4, 4, T> & scale");
			for (int i = 0; i < permutation.length; ++i)
				method.append((char) (AXIS_LABELS[permutation[i]] & MAKE_UPPER));
			method.append("(const GenoMatrix<4, 4, T> & matrix");
			for (int i = 0; i < permutation.length; ++i) {
				method.append(", T scale")
			          .append((char) (AXIS_LABELS[permutation[i]] & MAKE_UPPER));
			}
			String[] multiplier = new String[] {
				Utils.contains(permutation, 0) != -1 ? "scaleX": "1", "0", "0", "0",
				"0", Utils.contains(permutation, 1) != -1 ? "scaleY" : "1", "0", "0",
				"0", "0", Utils.contains(permutation, 2) != -1 ? "scaleZ" : "1", "0",
				"0", "0", "0", "1"
			};
			method.append(", GenoMatrix<4, 4, T> & target) {\n")
			      .append(generateDMTargetFunction(multiplier))
			      .append("}\n\n");
		}
		if (permutation.length > 1) {
			int indexX = Utils.contains(permutation, 0);
			int indexY = Utils.contains(permutation, 1);
			int indexZ = Utils.contains(permutation, 2);
			method.append("template <typename T>\nGenoMatrix<4, 4, T> & scale");
			for (int i = 0; i < permutation.length; ++i)
				method.append((char) (AXIS_LABELS[permutation[i]] & MAKE_UPPER));
			String[] multiplier = new String[] {
				indexX != -1 ? "scale.v[" + indexX + ']': "1", "0", "0", "0",
				"0", indexY != -1 ? "scale.v[" + indexY + ']' : "1", "0", "0",
				"0", "0", indexZ != -1 ? "scale.v[" + indexZ + ']' : "1", "0",
				"0", "0", "0", "1"
			};
			method.append("(const GenoMatrix<4, 4, T> & matrix, const GenoVector<")
			      .append(permutation.length)
			      .append(", T> & scale, GenoMatrix<4, 4, T> & target) {\n")
			      .append(generateDMTargetFunction(multiplier))
			      .append("}\n\n");
		}
		return method.toString();
	}
}
