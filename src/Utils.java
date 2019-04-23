public class Utils {

	public interface VectorMethod {
		public String method(int dimensions, int[] permutation);
	}
	
	public interface Method3D {
		public String method(int[] permutation);
	}
	
	public static int numPermutations(int vectorDimensions, int methodDimensions) {
		int ret = 1;
		for (int i = 0; i < methodDimensions; ++i)
			ret *= vectorDimensions;
		return ret;
	}
	
	public static int[] getPermutation(int permutationNumber, int vectorDimensions, int methodDimensions) {
		int[] permutation = new int[methodDimensions];
		for (int i = methodDimensions - 1; i > -1; --i) {
			permutation[i] = permutationNumber % vectorDimensions;
			permutationNumber /= vectorDimensions;
		}
		return permutation;
	}
	
	public static boolean isOrdered(int[] permutation) {
		for (int i = 1; i < permutation.length; ++i)
			if (permutation[i] <= permutation[i - 1])
				return false;
		return true;
	}
	
	public static boolean isUnique(int[] permutation) {
		for (int i = 0; i < permutation.length - 1; ++i)
			for (int j = i + 1; j < permutation.length; ++j)
				if (permutation[i] == permutation[j])
					return false;
		return true;
	}
	
	public static int contains(int[] permutation, int search) {
		for (int i = 0; i < permutation.length; ++i)
			if (permutation[i] == search)
				return i;
		return -1;
	}
	
	public static String feedPermutations(int vectorDimensions, int methodDimensions, VectorMethod methodType) {
		StringBuilder result = new StringBuilder();
		for (int i = 1; i <= methodDimensions; ++i) {
			int numPermutations = numPermutations(vectorDimensions, i);
			for (int j = 0; j < numPermutations; ++j) {
				int[] permutation = getPermutation(j, vectorDimensions, i);
				String method = methodType.method(vectorDimensions, permutation);
				if (method != null)
					result.append(method);
			}
		}
		return result.toString();
	}
	
	public static String feedPermutations(Method3D methodType) {
		StringBuilder result = new StringBuilder();
		for (int i = 1; i <= 3; ++i) {
			int numPermutations = numPermutations(3, i);
			for (int j = 0; j < numPermutations; ++j) {
				int[] permutation = getPermutation(j, 3, i);
				String method = methodType.method(permutation);
				if (method != null)
					result.append(method);
			}
		}
		return result.toString();
	}

	/////////// 3D ROTATIONS ///////////
	
	public static final String[] TRIG_FUNCTIONS = new String[] {
		"\tauto sinX = sin(%0);\n\tauto cosX = cos(%0);\n",
		"\tauto sinY = sin(%1);\n\tauto cosY = cos(%1);\n",
		"\tauto sinZ = sin(%2);\n\tauto cosZ = cos(%2);\n"
	};
	
	public static final String[][][] ROTATIONS = new String[][][] {
	{    // ROTATE X
		{ "1",  "0",     "0"    },
		{ "0",  "cosX", "-sinX" },
		{ "0",  "sinX",  "cosX" }
	}, { // ROTATE Y
		{  "cosY", "0", "sinY" },
		{  "0",    "1", "0"    },
		{ "-sinY", "0", "cosY" }
	}, { // ROTATE Z
		{ "cosZ", "-sinZ", "0" },
		{ "sinZ",  "cosZ", "0" },
		{ "0",     "0",    "1" }
	}};
	
	public interface RotateMethod {
		public String method(int[] permutation);
	}
	
	public static String requiredTrig(int[] permutation) {
		StringBuilder trig = new StringBuilder();
		for (int i = 0; i < 3; ++i)
			if (contains(permutation, i) != -1)
				trig.append(TRIG_FUNCTIONS[i]);
		return trig.toString();
	}
	
	public static String cleanSign(String string) {
		int numNegatives = 0;
		for (int i = 0; i < string.length(); ++i)
			if (string.charAt(i) == '-')
				++numNegatives;
		return (numNegatives % 2 == 0 ? "" : "- ") + string.replace("-", "");
	}
	
	public static String[][] multiplyMatrices(String[][] left, String[][] right) {
		String[][] product = new String[left.length][left.length];
		for (int i = 0; i < left.length; ++i) {
			for (int j = 0; j < left.length; ++j) {
				product[i][j] = "";
				for (int k = 0; k < left.length; ++k) {
					StringBuilder dot = new StringBuilder();
					String[] u = left [i][k].split("\\s*\\+\\s*");
					String[] v = right[k][j].split("\\s*\\+\\s*");
					for (int a = 0; a < u.length; ++a) {
						for (int b = 0; b < v.length; ++b) {
							dot.append(u[a])
							   .append(" * ")
							   .append(v[b]);
						}
						if (a < u.length - 1)
							dot.append(" + ");
					}
					product[i][j] += dot.toString();
					if (k < 2)
						product[i][j] += " + ";
				}
			}
		}
		return product;
	}
	
	public static String[][] getCoefficients(int[] permutation) {
		String[][] coefficients = new String[3][3];
		for (int i = permutation.length - 1; i > -1; --i) {
			// Copy the matrix in directly
			if (i == permutation.length - 1) {
				for (int j = 0; j < 9; ++j)
					coefficients[j / 3][j % 3] = ROTATIONS[permutation[i]][j / 3][j % 3];
			}
			// Multiply the matrix
			else
				coefficients = multiplyMatrices(coefficients, ROTATIONS[permutation[i]]);
		}
		// Clean the matrix
		for (int i = 0; i < 9; ++i) {
			String[] split = coefficients[i / 3][i % 3].split(" \\+ ");
			coefficients[i / 3][i % 3] = "";
			boolean notFirst = false;
			for (int j = 0; j < split.length; ++j) {
				if (!split[j].contains("0")) {
					split[j] = cleanSign(split[j]
						.replaceAll(" \\* 1", "")
						.replaceAll("1 \\* ", "")
					);
					if (notFirst) {
						if (split[j].charAt(0) == '-')
							split[j] = ' ' + split[j];
						else
							split[j] = " + " + split[j];
					}
					coefficients[i / 3][i % 3] += split[j];
					notFirst = true;
				}
			}
		}
		for (int i = 0; i < 9; ++i)
			if (coefficients[i / 3][i % 3].equals(""))
				coefficients[i / 3][i % 3] = "0";
		for (int i = 0; i < 9; ++i)
			if (coefficients[i / 3][i % 3].startsWith("-")) 
				coefficients[i / 3][i % 3] = "-" + coefficients[i / 3][i % 3].substring(2);
		return coefficients;
	}
}
