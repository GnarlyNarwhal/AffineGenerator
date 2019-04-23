import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

public class Main {
	
	public static void main(String[] args) {
		String methods = Matrix4Generator.shearTargetFunctions();
		try {
			FileWriter output = new FileWriter(new File("output.txt"));
			output.write(methods);
			output.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
