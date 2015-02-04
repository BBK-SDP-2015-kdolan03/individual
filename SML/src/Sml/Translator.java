package Sml;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.IllegalAccessException;

/*
 * The translator of a <b>S</b><b>M</b>al<b>L</b> program.
 */
public class Translator {

	// word + line is the part of the current line that's not yet processed
	// word has no whitespace
	// If word and line are not empty, line begins with whitespace
	private String line = "";
	private Labels labels; // The labels of the program being translated
	private ArrayList<Instruction> program; // The program to be created
	private String fileName; // source file of SML code

	private static final String SRC = "src";

	public Translator(String fileName) {
		this.fileName = SRC + "/" + fileName;
	}

	// translate the small program in the file into lab (the labels) and
	// prog (the program)
	// return "no errors were detected"
	public boolean readAndTranslate(Labels lab, ArrayList<Instruction> prog) {

		try (Scanner sc = new Scanner(new File(fileName))) {
			// Scanner attached to the file chosen by the user
			labels = lab;
			labels.reset();
			program = prog;
			program.clear();

			try {
				line = sc.nextLine();
			} catch (NoSuchElementException ioE) {
				return false;
			}

			// Each iteration processes line and reads the next line into line
			while (line != null) {
				// Store the label in label
				String label = scan();

				if (label.length() > 0) {
					Instruction ins = getInstruction(label);
					if (ins != null) {
						labels.addLabel(label);
						program.add(ins);
					}
					else{return false;}	
				}

				try {
					line = sc.nextLine();
				} catch (NoSuchElementException ioE) {
					return true;		// End of file found, was originally false
				}
			}
		} catch (IOException ioE) {
			System.out.println("File: IO error " + ioE.getMessage());
			return false;
		}
		return true;
	}

	// line should consist of an MML instruction, with its label already
	// removed. Translate line into an instruction with label label
	// and return the instruction
	public Instruction getInstruction(String label) {
		
		if (line.equals(""))
			return null;

		String ins = scan();
			
		String subclass = "Sml." + Character.toUpperCase(ins.charAt(0)) + ins.substring(1, 3) + "Instruction";
		return reflectionInstruction(label, subclass);	
	}
	
	/**
	 * 
	 * @param label
	 * @param className
	 * @return
	 */
	
	private Instruction reflectionInstruction(String label, String className)
	{	
		try {
			Class<?> c = Class.forName(className);
			try {
				Constructor<?> cons[] = c.getConstructors();
				
				// Assumes that the second constructor takes the parameters
				
				if (cons.length < 2)	// Missing constructor
					return null;
				
				/*
				 * Read the parameter types. Allows for a parameter-less
				 * instruction, E.g NOP
				 */
						
		        Class<?> params[] = cons[1].getParameterTypes();
		        if (params.length < 1)
		        	return null;
		        
		        /*
		        * Create an Object array, of params.length size,
		        * and initialise the first element to label
		        */
		        
		        final Object[] args = new Object[params.length];
		        args[0] = label;
		       		        
		        /*
		        * Parse the constructors parameters, starting at the second,
		        * as the first i the label
		        */

		        for (int p = 1; p < params.length; ++p)
		        {  	   	    
		        	switch(params[p].getTypeName())		// Expects only String or Integer
		        	{
		        		case "java.lang.String":
		        			args[p] = scan();	
		        			break;
		        			
		        		case "int":
		        			args[p] = scanInt();
		        			break;
		        			
		        		default:
		        			System.err.println("Error: Unknown parameter type");
		        			return null;
		        	}
		        }
		        
		        try
		        {
		        	/* 
		        	 * Construct a new instance of the xxxInstruction class
		        	 * using the Object array to initialise the constructor
		        	 */
		        	return (Instruction) cons[1].newInstance(args);
		        }
		        catch (InstantiationException|IllegalAccessException|IllegalArgumentException|InvocationTargetException e)
		        {
		        	e.printStackTrace();
		        }
		        
			} catch ( SecurityException e) {
				System.err.println(e.getCause());
				e.printStackTrace();
			}
		} catch (ClassNotFoundException e) {
			System.err.println("Class: "+className+" not found");
			e.printStackTrace();
		}
		
		return null;
	}
	

	/*
	 * Return the first word of line and remove it from line. If there is no
	 * word, return ""
	 */
	private String scan() {
		line = line.trim();
		if (line.length() == 0)
			return "";

		int i = 0;
		while (i < line.length() && line.charAt(i) != ' ' && line.charAt(i) != '\t') {
			i = i + 1;
		}
		String word = line.substring(0, i);
		line = line.substring(i);
		return word;
	}

	// Return the first word of line as an integer. If there is
	// any error, return the maximum int
	private int scanInt() {
		String word = scan();
		if (word.length() == 0) {
			return Integer.MAX_VALUE;
		}

		try {
			return Integer.parseInt(word);
		} catch (NumberFormatException e) {
			return Integer.MAX_VALUE;
		}
	}
}