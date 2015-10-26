package synthesis;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Scanner;

public class InputFileTest {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		Scanner read = null;
		try 
		{
			read = new Scanner(new BufferedReader(new FileReader("./IOFiles/ModelConstants.txt")));
			 String param = null;
			 int val = -1;
			 
			 while (read.hasNext()) {
				 param = read.next();
				 val = Integer.parseInt(read.next());
				 System.out.println("The param "+param);
				 System.out.println("The value "+val);
	            }
		

		} catch (IOException e) {
			e.printStackTrace();
		} 
	}
}
