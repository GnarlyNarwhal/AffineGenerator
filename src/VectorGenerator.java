
public class VectorGenerator {

	private static char   MAKE_UPPER  = 0b11011111;
	private static char[] AXIS_LABELS = { 'x', 'y', 'z', 'w', 's', 't', 'u', 'v' };
	
	/////////// GETTERS ///////////
	
	public static String getMethods(int dimensions, int[] permutation) {
		StringBuilder method = new StringBuilder("GenoVector<")
			.append(permutation.length)
			.append(", T> get");
		for (int i = 0; i < permutation.length; ++i)
			method.append((char) (AXIS_LABELS[permutation[i]] & MAKE_UPPER));
		method.append("() const {\n\treturn { ");
		for (int i = 0; i < permutation.length; ++i) {
			method.append("v[")
			      .append(permutation[i])
			      .append(']');
			if (i < permutation.length - 1)
				method.append(", ");
		}
		return method.append(" };\n}\n\n").toString();
	}

	/////////// SETTERS ///////////
	
	public static String setMethods(int dimensions, int[] permutation) {
		if (!Utils.isUnique(permutation))
			return null;
		StringBuilder method = new StringBuilder();
		if (Utils.isOrdered(permutation)) {
			method.append("GenoVector<")
			      .append(dimensions)
			      .append(", T> & set");
			for (int i = 0; i < permutation.length; ++i)
				method.append((char) (AXIS_LABELS[permutation[i]] & MAKE_UPPER));
			method.append('(');
			for (int i = 0; i < permutation.length; ++i) {
				method.append("T ")
				      .append(AXIS_LABELS[permutation[i]]);
				if (i < permutation.length - 1)
					method.append(", ");
			}
			method.append(") {\n");
			for (int i = 0; i < permutation.length; ++i) {
				method.append("\tv[")
				      .append(permutation[i])
				      .append("] = ")
				      .append(AXIS_LABELS[permutation[i]])
				      .append(";\n");
			}
			method.append("\treturn *thUtils.is;\n}\n\n");
		}
		if (permutation.length > 1) {
			method.append("GenoVector<")
			      .append(dimensions)
			      .append(", T> & set");
			for (int i = 0; i < permutation.length; ++i)
				method.append((char) (AXIS_LABELS[permutation[i]] & MAKE_UPPER));
			method.append("(const GenoVector<")
			      .append(permutation.length)
			      .append(", T> & set) {\n");
			for (int i = 0; i < permutation.length; ++i)
				method.append("\tv[")
				      .append(permutation[i])
				      .append("] = set.v[")
				      .append(i)
				      .append("];\n");
			method.append("\treturn *thUtils.is;\n}\n\n");
		}
		return method.toString();
	}
	
	public static String setFunctions(int dimensions, int[] permutation) {
		if (!Utils.isUnique(permutation) || permutation.length == dimensions)
			return null;
		StringBuilder method = new StringBuilder();
		if (Utils.isOrdered(permutation)) {
			method.append("template<typename T>\nGenoVector<")
			      .append(dimensions)
			      .append(", T> set");
			for (int i = 0; i < permutation.length; ++i)
				method.append((char) (AXIS_LABELS[permutation[i]] & MAKE_UPPER));
			method.append("(const GenoVector<")
			      .append(dimensions)
			      .append(", T> & vector");
			for (int i = 0; i < permutation.length; ++i)
				method.append(", T ")
				      .append(AXIS_LABELS[permutation[i]]);
			method.append(") {\n\treturn {\n");
			for (int i = 0; i < dimensions; ++i) {
				method.append("\t\t");
				int index = Utils.contains(permutation, i); 
				if (index > -1)
					method.append(AXIS_LABELS[permutation[index]]);
				else
					method.append("vector.v[")
					      .append(i)
					      .append(']');
				if (i < dimensions - 1)
					method.append(',');
				method.append('\n');
			}
			method.append("\t};\n}\n\n");
		}
		if (permutation.length > 1) {
			method.append("template<typename T>\nGenoVector<")
			      .append(dimensions)
			      .append(", T> set");
			for (int i = 0; i < permutation.length; ++i)
				method.append((char) (AXIS_LABELS[permutation[i]] & MAKE_UPPER));
			method.append("(const GenoVector<")
			      .append(dimensions)
			      .append(", T> & vector, const GenoVector<")
			      .append(permutation.length)
			      .append(", T> & set) {\n\treturn {\n");
			for (int i = 0; i < dimensions; ++i) {
				method.append("\t\t");
				int index = Utils.contains(permutation, i);
				if (index > -1)
					method.append("   set.v[")
					      .append(index)
					      .append(']');
				else
					method.append("vector.v[")
					      .append(i)
					      .append(']');
				if (i < dimensions - 1)
					method.append(',');
				method.append('\n');
			}
			method.append("\t};\n}\n\n");
		}
		return method.toString();
	}
	
	public static String setTargetFunctions(int dimensions, int[] permutation) {
		if (!Utils.isUnique(permutation) || permutation.length == dimensions)
			return null;
		StringBuilder method = new StringBuilder();
		if (Utils.isOrdered(permutation)) {
			method.append("template<typename T>\nGenoVector<")
			      .append(dimensions)
			      .append(", T> set");
			for (int i = 0; i < permutation.length; ++i)
				method.append((char) (AXIS_LABELS[permutation[i]] & MAKE_UPPER));
			method.append("(const GenoVector<")
			      .append(dimensions)
			      .append(", T> & vector, ");
			for (int i = 0; i < permutation.length; ++i) {
				method.append("T ")
				      .append(AXIS_LABELS[permutation[i]])
				      .append(", ");
			}
			method.append("GenoVector<")
			      .append(dimensions)
			      .append(", T> & target) {\n");
			for (int i = 0; i < dimensions; ++i) {
				method.append("\ttarget.v[")
				      .append(i)
				      .append("] = ");
				int index = Utils.contains(permutation, i); 
				if (index > -1)
					method.append(AXIS_LABELS[permutation[index]]);
				else
					method.append("vector.v[")
					      .append(i)
					      .append("]");
				method.append(";\n");
			}
			method.append("\treturn target;\n}\n\n");
		}
		if (permutation.length > 1) {
			method.append("template<typename T>\nGenoVector<")
			      .append(dimensions)
			      .append(", T> set");
			for (int i = 0; i < permutation.length; ++i)
				method.append((char) (AXIS_LABELS[permutation[i]] & MAKE_UPPER));
			method.append("(const GenoVector<")
			      .append(dimensions)
			      .append(", T> & vector, const GenoVector<")
			      .append(permutation.length)
			      .append(", T> & set, GenoVector<")
			      .append(dimensions)
			      .append(", T> & target) {\n");
			for (int i = 0; i < dimensions; ++i) {
				method.append("\ttarget.v[")
				      .append(i)
				      .append("] = ");
				int index = Utils.contains(permutation, i);
				if (index > -1)
					method.append("   set.v[")
					      .append(index)
					      .append(']');
				else
					method.append("vector.v[")
					      .append(i)
					      .append(']');
				method.append(";\n");
			}
			method.append("\treturn target;\n}\n\n");
		}
		return method.toString();
	}

	/////////// TRANSLATES ///////////
	
	public static String translateMethods(int dimensions, int[] permutation) {
		if (!Utils.isUnique(permutation))
			return null;
		StringBuilder method = new StringBuilder();
		if (Utils.isOrdered(permutation)) {
			method.append("GenoVector<")
			      .append(dimensions)
			      .append(", T> & translate");
			for (int i = 0; i < permutation.length; ++i)
				method.append((char) (AXIS_LABELS[permutation[i]] & MAKE_UPPER));
			method.append('(');
			for (int i = 0; i < permutation.length; ++i) {
				method.append("T translate")
				      .append((char) (AXIS_LABELS[permutation[i]] & MAKE_UPPER));
				if (i < permutation.length - 1)
					method.append(", ");
			}
			method.append(") {\n");
			for (int i = 0; i < permutation.length; ++i) {
				method.append("\tv[")
				      .append(permutation[i])
				      .append("] += translate")
				      .append((char) (AXIS_LABELS[permutation[i]] & MAKE_UPPER))
				      .append(";\n");
			}
			method.append("}\n\n");
		}
		if (permutation.length > 1) {
			method.append("GenoVector<")
		      .append(dimensions)
		      .append(", T> & translate");
			for (int i = 0; i < permutation.length; ++i)
				method.append((char) (AXIS_LABELS[permutation[i]] & MAKE_UPPER));
			method.append("(const GenoVector<")
			      .append(permutation.length)
			      .append(", T> & translate) {\n");
			for (int i = 0; i < permutation.length; ++i) {
				method.append("\tv[")
				      .append(permutation[i])
				      .append("] += translate.v[")
				      .append(i)
				      .append("];\n");
			}
			method.append("}\n\n");
		}
		return method.toString();
	}
	
	public static String translateFunctions(int dimensions, int[] permutation) {
		if (!Utils.isUnique(permutation))
			return null;
		StringBuilder method = new StringBuilder();
		if (Utils.isOrdered(permutation)) {
			method.append("template<typename T>\nGenoVector<")
			      .append(dimensions)
			      .append(", T> translate");
			for (int i = 0; i < permutation.length; ++i)
				method.append((char) (AXIS_LABELS[permutation[i]] & MAKE_UPPER));
			method.append("(const GenoVector<")
			      .append(dimensions)
			      .append(", T> & vector");
			for (int i = 0; i < permutation.length; ++i)
				method.append(", T translate")
				      .append((char) (AXIS_LABELS[permutation[i]] & MAKE_UPPER));
			method.append(") {\n\treturn {\n");
			for (int i = 0; i < dimensions; ++i) {
				method.append("\t\tvector.v[")
				      .append(i)
				      .append(']');
				int index = Utils.contains(permutation, i);
				if (index > -1)
					method.append(" + translate")
					      .append((char) (AXIS_LABELS[permutation[index]] & MAKE_UPPER));
				if (i < dimensions - 1)
					method.append(',');
				method.append('\n');
			}
			method.append("\t};\n}\n\n");
		}
		if (permutation.length > 1) {
			method.append("template<typename T>\nGenoVector<")
		      .append(dimensions)
		      .append(", T> translate");
			for (int i = 0; i < permutation.length; ++i)
				method.append((char) (AXIS_LABELS[permutation[i]] & MAKE_UPPER));
			method.append("(const GenoVector<")
			      .append(dimensions)
			      .append(", T> & vector, const GenoVector<")
			      .append(permutation.length)
			      .append(", T> & translate) {\n\treturn {\n");
			for (int i = 0; i < dimensions; ++i) {
				method.append("\t\tvector.v[")
				      .append(i)
				      .append(']');
				int index = Utils.contains(permutation, i);
				if (index > -1)
					method.append(" + translate.v[")
					      .append(index)
					      .append(']');
				if (i < dimensions - 1)
					method.append(',');
				method.append('\n');
			}
			method.append("\t};\n}\n\n");
		}
		return method.toString();
	}
	
	public static String translateTargetFunctions(int dimensions, int[] permutation) {
		if (!Utils.isUnique(permutation))
			return null;
		StringBuilder method = new StringBuilder();
		if (Utils.isOrdered(permutation)) {
			method.append("template<typename T>\nGenoVector<")
			      .append(dimensions)
			      .append(", T> translate");
			for (int i = 0; i < permutation.length; ++i)
				method.append((char) (AXIS_LABELS[permutation[i]] & MAKE_UPPER));
			method.append("(const GenoVector<")
			      .append(dimensions)
			      .append(", T> & vector");
			for (int i = 0; i < permutation.length; ++i)
				method.append(", T translate")
				      .append((char) (AXIS_LABELS[permutation[i]] & MAKE_UPPER));
			method.append(", GenoVector<")
			      .append(dimensions)
			      .append(", T> & target) {\n");
			for (int i = 0; i < dimensions; ++i) {
				method.append("\ttarget.v[")
				      .append(i)
				      .append("] = vector.v[")
				      .append(i)
				      .append(']');
				int index = Utils.contains(permutation, i);
				if (index > -1)
					method.append(" + translate")
					      .append((char) (AXIS_LABELS[permutation[index]] & MAKE_UPPER));
				method.append(";\n");
			}
			method.append("\treturn target;\n}\n\n");
		}
		if (permutation.length > 1) {
			method.append("template<typename T>\nGenoVector<")
			      .append(dimensions)
			      .append(", T> translate");
			for (int i = 0; i < permutation.length; ++i)
				method.append((char) (AXIS_LABELS[permutation[i]] & MAKE_UPPER));
			method.append("(const GenoVector<")
			      .append(dimensions)
			      .append(", T> & vector, const GenoVector<")
			      .append(permutation.length)
			      .append(", T> & translate, GenoVector<")
			      .append(dimensions)
			      .append(", T> & target) {\n");
			for (int i = 0; i < dimensions; ++i) {
				method.append("\ttarget.v[")
				      .append(i)
				      .append("] = vector.v[")
				      .append(i)
				      .append(']');
				int index = Utils.contains(permutation, i);
				if (index > -1)
					method.append(" + translate.v[")
					      .append(index)
					      .append(']');
				method.append(";\n");
			}
			method.append("\treturn target;\n}\n\n");
		}
		return method.toString();
	}

	/////////// SCALES ///////////
	
	public static String scaleMethods(int dimensions, int[] permutation) {
		if (!Utils.isUnique(permutation))
			return null;
		StringBuilder method = new StringBuilder();
		if (permutation.length > 1 && Utils.isOrdered(permutation)) {
			method.append("GenoVector<")
		      .append(dimensions)
		      .append(", T> & scale");
			for (int i = 0; i < permutation.length; ++i)
				method.append((char) (AXIS_LABELS[permutation[i]] & MAKE_UPPER));
			method.append("(T scale) {\n");
			for (int i = 0; i < permutation.length; ++i) {
				method.append("\tv[")
				      .append(permutation[i])
				      .append("] *= scale;\n");
			}
			method.append("}\n\n");
		}
		if (Utils.isOrdered(permutation)) {
			method.append("GenoVector<")
			      .append(dimensions)
			      .append(", T> & scale");
			for (int i = 0; i < permutation.length; ++i)
				method.append((char) (AXIS_LABELS[permutation[i]] & MAKE_UPPER));
			method.append('(');
			for (int i = 0; i < permutation.length; ++i) {
				method.append("T scale")
				      .append((char) (AXIS_LABELS[permutation[i]] & MAKE_UPPER));
				if (i < permutation.length - 1)
					method.append(", ");
			}
			method.append(") {\n");
			for (int i = 0; i < permutation.length; ++i) {
				method.append("\tv[")
				      .append(permutation[i])
				      .append("] *= scale")
				      .append((char) (AXIS_LABELS[permutation[i]] & MAKE_UPPER))
				      .append(";\n");
			}
			method.append("}\n\n");
		}
		if (permutation.length > 1) {
			method.append("GenoVector<")
		      .append(dimensions)
		      .append(", T> & scale");
			for (int i = 0; i < permutation.length; ++i)
				method.append((char) (AXIS_LABELS[permutation[i]] & MAKE_UPPER));
			method.append("(const GenoVector<")
			      .append(permutation.length)
			      .append(", T> & scale) {\n");
			for (int i = 0; i < permutation.length; ++i) {
				method.append("\tv[")
				      .append(permutation[i])
				      .append("] *= scale.v[")
				      .append(i)
				      .append("];\n");
			}
			method.append("}\n\n");
		}
		return method.toString();
	}
	
	public static String scaleFunctions(int dimensions, int[] permutation) {
		if (!Utils.isUnique(permutation))
			return null;
		StringBuilder method = new StringBuilder();
		if (permutation.length > 1 && Utils.isOrdered(permutation)) {
			method.append("template<typename T>\nGenoVector<")
			      .append(dimensions)
			      .append(", T> scale");
			for (int i = 0; i < permutation.length; ++i)
				method.append((char) (AXIS_LABELS[permutation[i]] & MAKE_UPPER));
			method.append("(const GenoVector<")
			      .append(dimensions)
			      .append(", T> & vector, T scale) {\n\treturn {\n");
			for (int i = 0; i < dimensions; ++i) {
				method.append("\t\tvector.v[")
				      .append(i)
				      .append(']');
				int index = Utils.contains(permutation, i);
				if (index > -1)
					method.append(" * scale");
				if (i < dimensions - 1)
					method.append(',');
				method.append('\n');
			}
			method.append("\t};\n}\n\n");
		}
		if (Utils.isOrdered(permutation)) {
			method.append("template<typename T>\nGenoVector<")
			      .append(dimensions)
			      .append(", T> scale");
			for (int i = 0; i < permutation.length; ++i)
				method.append((char) (AXIS_LABELS[permutation[i]] & MAKE_UPPER));
			method.append("(const GenoVector<")
			      .append(dimensions)
			      .append(", T> & vector");
			for (int i = 0; i < permutation.length; ++i)
				method.append(", T scale")
				      .append((char) (AXIS_LABELS[permutation[i]] & MAKE_UPPER));
			method.append(") {\n\treturn {\n");
			for (int i = 0; i < dimensions; ++i) {
				method.append("\t\tvector.v[")
				      .append(i)
				      .append(']');
				int index = Utils.contains(permutation, i);
				if (index > -1)
					method.append(" * scale")
					      .append((char) (AXIS_LABELS[permutation[index]] & MAKE_UPPER));
				if (i < dimensions - 1)
					method.append(',');
				method.append('\n');
			}
			method.append("\t};\n}\n\n");
		}
		if (permutation.length > 1) {
			method.append("template<typename T>\nGenoVector<")
		      .append(dimensions)
		      .append(", T> scale");
			for (int i = 0; i < permutation.length; ++i)
				method.append((char) (AXIS_LABELS[permutation[i]] & MAKE_UPPER));
			method.append("(const GenoVector<")
			      .append(dimensions)
			      .append(", T> & vector, const GenoVector<")
			      .append(permutation.length)
			      .append(", T> & scale) {\n\treturn {\n");
			for (int i = 0; i < dimensions; ++i) {
				method.append("\t\tvector.v[")
				      .append(i)
				      .append(']');
				int index = Utils.contains(permutation, i);
				if (index > -1)
					method.append(" * scale.v[")
					      .append(index)
					      .append(']');
				if (i < dimensions - 1)
					method.append(',');
				method.append('\n');
			}
			method.append("\t};\n}\n\n");
		}
		return method.toString();
	}
	
	public static String scaleTargetFunctions(int dimensions, int[] permutation) {
		if (!Utils.isUnique(permutation))
			return null;
		StringBuilder method = new StringBuilder();
		if (permutation.length > 1 && Utils.isOrdered(permutation)) {
			method.append("template<typename T>\nGenoVector<")
			      .append(dimensions)
			      .append(", T> scale");
			for (int i = 0; i < permutation.length; ++i)
				method.append((char) (AXIS_LABELS[permutation[i]] & MAKE_UPPER));
			method.append("(const GenoVector<")
			      .append(dimensions)
			      .append(", T> & vector, T scale, GenoVector<")
			      .append(dimensions)
			      .append(", T> & target) {\n");
			for (int i = 0; i < dimensions; ++i) {
				method.append("\ttarget.v[")
				      .append(i)
				      .append("] = vector.v[")
				      .append(i)
				      .append(']');
				int index = Utils.contains(permutation, i);
				if (index > -1)
					method.append(" * scale");
				method.append(";\n");
			}
			method.append("\treturn target;\n}\n\n");
		}
		if (Utils.isOrdered(permutation)) {
			method.append("template<typename T>\nGenoVector<")
			      .append(dimensions)
			      .append(", T> scale");
			for (int i = 0; i < permutation.length; ++i)
				method.append((char) (AXIS_LABELS[permutation[i]] & MAKE_UPPER));
			method.append("(const GenoVector<")
			      .append(dimensions)
			      .append(", T> & vector");
			for (int i = 0; i < permutation.length; ++i)
				method.append(", T scale")
				      .append((char) (AXIS_LABELS[permutation[i]] & MAKE_UPPER));
			method.append(", const GenoVector<")
			      .append(dimensions)
			      .append(", T> & target) {\n");
			for (int i = 0; i < dimensions; ++i) {
				method.append("\ttarget.v[")
				      .append(i)
				      .append("] = vector.v[")
				      .append(i)
				      .append(']');
				int index = Utils.contains(permutation, i);
				if (index > -1)
					method.append(" * scale")
					      .append((char) (AXIS_LABELS[permutation[index]] & MAKE_UPPER));
				method.append(";\n");
			}
			method.append("\treturn target;\n}\n\n");
		}
		if (permutation.length > 1) {
			method.append("template<typename T>\nGenoVector<")
			      .append(dimensions)
			      .append(", T> scale");
			for (int i = 0; i < permutation.length; ++i)
				method.append((char) (AXIS_LABELS[permutation[i]] & MAKE_UPPER));
			method.append("(const GenoVector<")
			      .append(dimensions)
			      .append(", T> & vector, const GenoVector<")
			      .append(permutation.length)
			      .append(", T> & scale, GenoVector<")
			      .append(dimensions)
			      .append(", T> & target) {\n");
			for (int i = 0; i < dimensions; ++i) {
				method.append("\ttarget.v[")
				      .append(i)
				      .append("] = vector.v[")
				      .append(i)
				      .append(']');
				int index = Utils.contains(permutation, i);
				if (index > -1)
					method.append(" * scale.v[")
					      .append(index)
					      .append(']');
				method.append(";\n");
			}
			method.append("\treturn target;\n}\n\n");
		}
		return method.toString();
	}

	/////////// 3D ROTATIONS ///////////
	
	public static String[] constructRotations(String[][] coefficients) {
		String[] ret = new String[3];
		for (int i = 0; i < 3; ++i) {
			boolean keep = false;
			for (int j = 0; j < 3; ++j) {
				if ((i == j && !coefficients[i][j].equals("1")) || (i != j && !coefficients[i][j].equals("")))
					keep = true;
			}
			if (keep) {
				ret[i] = "";
				for (int j = 0; j < 3; ++j) {
					if (coefficients[i][j].startsWith("-"))
						ret[i] += "%" + j + " * (-" + coefficients[i][j].substring(2) + " )";
					else
						ret[i] += "%" + j + " * ( " + coefficients[i][j] + " )";
					if (j < 2)
						ret[i] += " + ";
				}
				ret[i] = ret[i]
					.replaceAll(" \\* \\( 1 \\)", "")
					.replaceAll("%[012] \\* \\( 0\\)", "")
					.replaceAll("\\+  \\+", "\\+");
				if (ret[i].startsWith(" + "))
					ret[i] = ret[i].substring(3);
				if (ret[i].endsWith(" + "))
					ret[i] = ret[i].substring(0, ret[i].length() - 3);
				ret[i] = ret[i].replaceAll("\\+ -", "- ");
			}
		}
		boolean negativeFirst = false;
		for (int i = 0; i < 3 && !negativeFirst; ++i)
			if (ret[i] != null && ret[i].startsWith("-"))
				negativeFirst = true;
		if (negativeFirst)
			for (int i = 0; i < 3; ++i)
				if (ret[i] != null && !ret[i].startsWith("-"))
					ret[i] = ' ' + ret[i];
		return ret;
	}
	
	public static String rotateMethods(int[] permutation) {
		if (!Utils.isUnique(permutation))
			return null;
		StringBuilder method = new StringBuilder();
		String[] rotations = constructRotations(Utils.getCoefficients(permutation));
		if (Utils.isOrdered(permutation)) {
			method.append("GenoVector<3, T> & rotate");
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
			                   .replace("%2", "rotateZ"));
			for (int i = 0; i < 3; ++i)
				if (rotations[i] != null)
					method.append("\tauto ")
					      .append(AXIS_LABELS[i])
					      .append(" = ")
					      .append(rotations[i]
				                 .replace("%0", "v[0]")
				                 .replace("%1", "v[1]")
				                 .replace("%2", "v[2]"))
					      .append(";\n");
			for (int i = 0; i < 3; ++i)
				if (rotations[i] != null)
					method.append("\tv[")
					      .append(i)
					      .append("] = ")
					      .append(AXIS_LABELS[i])
					      .append(";\n");
			method.append("\treturn *this;\n}\n\n");
		}
		if (permutation.length > 1) {
			method.append("GenoVector<3, T> & rotate");
			for (int i = 0; i < permutation.length; ++i)
				method.append((char) (AXIS_LABELS[permutation[i]] & MAKE_UPPER));
			method.append("(const GenoVector<")
			      .append(permutation.length)
			      .append(", T> & rotate) {\n")
			      .append(Utils.requiredTrig(permutation)
		                   .replace("%0", "rotate.v[" + Utils.contains(permutation, 0) + ']')
		                   .replace("%1", "rotate.v[" + Utils.contains(permutation, 1) + ']')
		                   .replace("%2", "rotate.v[" + Utils.contains(permutation, 2) + ']'));
			for (int i = 0; i < 3; ++i)
				if (rotations[i] != null)
					method.append("\tauto ")
					      .append(AXIS_LABELS[i])
					      .append(" = ")
					      .append(rotations[i]
					             .replace("%0", "v[0]")
					             .replace("%1", "v[1]")
					             .replace("%2", "v[2]"))
					      .append(";\n");
			for (int i = 0; i < 3; ++i)
				if (rotations[i] != null)
					method.append("\tv[")
					      .append(i)
					      .append("] = ")
					      .append(AXIS_LABELS[i])
					      .append(";\n");
			method.append("\treturn *this;\n}\n\n");
		}
		return method.toString();
	}
	
	public static String rotateFunctions(int[] permutation) {
		if (!Utils.isUnique(permutation))
			return null;
		StringBuilder method = new StringBuilder();
		String[] rotations = constructRotations(Utils.getCoefficients(permutation));
		if (Utils.isOrdered(permutation)) {
			method.append("template<typename T>\nGenoVector<3, T> & rotate");
			for (int i = 0; i < permutation.length; ++i)
				method.append((char) (AXIS_LABELS[permutation[i]] & MAKE_UPPER));
			method.append("(const GenoVector<3, T> & vector, ");
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
			                   .replace("%2", "rotateZ"));
			method.append("\treturn {\n");
			String padding = "";
			for (int i = 0; i < 3; ++i)
				if (rotations[i] != null && (rotations[i].startsWith(" ") || rotations[i].startsWith("-")))
					padding = " ";
			for (int i = 0; i < 3; ++i) {
				if (rotations[i] != null)
					method.append("\t\t")
					      .append(rotations[i]
				                 .replace("%0", "vector.v[0]")
				                 .replace("%1", "vector.v[1]")
				                 .replace("%2", "vector.v[2]"));
				else
					method.append("\t\t")
					      .append(padding)
					      .append("vector.v[")
					      .append(i)
					      .append("]");
				if (i < 2)
					method.append(',');
				method.append('\n');
			}
			method.append("\t};\n}\n\n");
		}
		if (permutation.length > 1) {
			method.append("template<typename T>\nGenoVector<3, T> & rotate");
			for (int i = 0; i < permutation.length; ++i)
				method.append((char) (AXIS_LABELS[permutation[i]] & MAKE_UPPER));
			method.append("(const GenoVector<3, T> & vector, const GenoVector<")
			      .append(permutation.length)
			      .append(", T> & rotate) {\n")
			      .append(Utils.requiredTrig(permutation)
		                   .replace("%0", "rotate.v[" + Utils.contains(permutation, 0) + ']')
		                   .replace("%1", "rotate.v[" + Utils.contains(permutation, 1) + ']')
		                   .replace("%2", "rotate.v[" + Utils.contains(permutation, 2) + ']'));
			method.append("\treturn {\n");
			for (int i = 0; i < 3; ++i) {
				if (rotations[i] != null)
					method.append("\t\t")
					      .append(rotations[i]
					             .replace("%0", "vector.v[0]")
					             .replace("%1", "vector.v[1]")
					             .replace("%2", "vector.v[2]"));
				if (i < 2)
					method.append(',');
				method.append('\n');
			}
			method.append("\t};\n}\n\n");
		}
		return method.toString();
	}
	
	public static String rotateTargetFunctions(int[] permutation) {
		if (!Utils.isUnique(permutation))
			return null;
		StringBuilder method = new StringBuilder();
		String[] rotations = constructRotations(Utils.getCoefficients(permutation));
		if (Utils.isOrdered(permutation)) {
			method.append("template<typename T>\nGenoVector<3, T> & rotate");
			for (int i = 0; i < permutation.length; ++i)
				method.append((char) (AXIS_LABELS[permutation[i]] & MAKE_UPPER));
			method.append("(const GenoVector<3, T> & vector, ");
			for (int i = 0; i < permutation.length; ++i) {
				method.append("T rotate")
				      .append((char) (AXIS_LABELS[permutation[i]] & MAKE_UPPER))
				      .append(", ");
			}
			method.append("GenoVector<3, T> & target) {\n")
			      .append(Utils.requiredTrig(permutation)
			                   .replace("%0", "rotateX")
			                   .replace("%1", "rotateY")
			                   .replace("%2", "rotateZ"));
			String padding = "";
			for (int i = 0; i < 3; ++i)
				if (rotations[i] != null && (rotations[i].startsWith(" ") || rotations[i].startsWith("-")))
					padding = " ";
			for (int i = 0; i < 3; ++i) {
				if (rotations[i] != null)
					method.append("\ttarget.v[")
					      .append(i)
					      .append("] = ")
					      .append(rotations[i]
				                 .replace("%0", "vector.v[0]")
				                 .replace("%1", "vector.v[1]")
				                 .replace("%2", "vector.v[2]"))
					      .append(";\n");
				else
					method.append("\ttarget.v[")
					      .append(i)
					      .append("] = ")
					      .append(padding)
					      .append("vector.v[")
					      .append(i)
					      .append("];\n");
			}
			method.append("\treturn target;\n}\n\n");
		}
		if (permutation.length > 1) {
			method.append("template<typename T>\nGenoVector<3, T> & rotate");
			for (int i = 0; i < permutation.length; ++i)
				method.append((char) (AXIS_LABELS[permutation[i]] & MAKE_UPPER));
			method.append("(const GenoVector<3, T> & vector, const GenoVector<")
			      .append(permutation.length)
			      .append(", T> & rotate, GenoVector<3, T> & target) {\n")
			      .append(Utils.requiredTrig(permutation)
		                   .replace("%0", "rotate.v[" + Utils.contains(permutation, 0) + ']')
		                   .replace("%1", "rotate.v[" + Utils.contains(permutation, 1) + ']')
		                   .replace("%2", "rotate.v[" + Utils.contains(permutation, 2) + ']'));
			for (int i = 0; i < 3; ++i) {
				if (rotations[i] != null)
					method.append("\ttarget.v[")
					      .append(i)
					      .append("] = ")
					      .append(rotations[i]
					             .replace("%0", "vector.v[0]")
					             .replace("%1", "vector.v[1]")
					             .replace("%2", "vector.v[2]"))
					      .append(";\n");
			}
			method.append("\treturn target;\n}\n\n");
		}
		return method.toString();
	}
}
