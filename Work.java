import javax.swing.*;
import javax.swing.text.*;

import java.awt.*;
import java.io.*;
import java.awt.event.*;
import java.util.Hashtable;
import java.util.Scanner;


public class Work extends JFrame
{	
	
	public static void main(String args[])
	{
		File file = new File("TextDoc.txt");
		try {
			Scanner scanner = new Scanner(file);
			
			while( scanner.hasNextLine() )
			{
				System.out.println(scanner.nextLine());
			}
			
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
	}
	
}
