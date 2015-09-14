package synthesis;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

import explicit.PrismExplicit;
import parser.PrismParser;
import parser.Values;
import parser.ast.ModulesFile;
import parser.ast.PropertiesFile;
import prism.Prism;
import prism.PrismException;
import prism.PrismFileLog;
import prism.PrismLangException;
import prism.PrismLog;
import prism.PrismSettings;

public class SMGParser {

	private static PrismParser thePrismParser = null;
	private static boolean prismParserInUse = false;
	
	public static void main(String[] args) {
		// TODO Auto-generated method stub
		 SMGParser pc = new SMGParser();
         pc.synthesis();
	}
	
	  public void synthesis()
	     {
	           try {
	              PrismLog mainLog = new PrismFileLog("./myLog.txt");
	              PrismSettings ps = new PrismSettings();
	              PrismExplicit prism = new PrismExplicit(mainLog, ps);
	              
	              //load the model with a constant value
	              ModulesFile modulesFile = mySMGParser(new File("./Prismfiles/smg_example.prism"));
	              System.out.println("The begin column is"+modulesFile.getBeginColumn());
	            
	          }
	           catch (FileNotFoundException e ) {
	              System.out.println("Error: " + e.getMessage());
	              System. exit(1);
	          }
	           catch (PrismLangException e ) {
		              System.out.println("Error: " + e.getMessage());
		              System. exit(1);
		      }
	           catch (PrismException e ) {
	              System.out.println("Error: " + e.getMessage());
	              System. exit(1);
	          }
	           catch(IOException e) {
	              // ioe.printStackTrace();
	               System.out.println("Error: " + e.getMessage());
	               System. exit(1);
	          }
	     }//end of synthesis

	  public ModulesFile mySMGParser(File file) throws FileNotFoundException, PrismLangException
	     {
	    	  FileInputStream strModel;
	    	  PrismParser prismParser;
	    	  ModulesFile modulesFile = null;

	    	  // open file
	    	  strModel = new FileInputStream(file);
	    	  try {
	    		  // obtain exclusive access to the prism parser
	    		  // (don't forget to release it afterwards)
	    		  prismParser = getPrismParser();
	    		  try {
	    			  // parse file
	    			  modulesFile = prismParser.parseModulesFile(strModel, null);
	    		  } finally {
	    			  // release prism parser
	    			  releasePrismParser();
	    		  }
	    	  } catch (InterruptedException ie) {
	    		  throw new PrismLangException("Concurrency error in parser");
	    	  }

	    	  modulesFile.tidyUp();

	    	  return modulesFile;
	     }//end of my SMG Parser
	  
	  /**
		 * Get (exclusive) access to the PRISM parser.
		 */
		public static PrismParser getPrismParser() throws InterruptedException
		{
			// Note: this mutex mechanism is based on public domain code by Doug Lea
			if (Thread.interrupted())
				throw new InterruptedException();
			// this code is synchronized on the whole Prism class
			// (because this is a static method)
			synchronized (SMGParser.class) {
				try {
					// wait until parser is free
					while (prismParserInUse) {
						SMGParser.class.wait();
					}
					// lock parser
					prismParserInUse = true;
					// return parser, creating anew if necessary
					if (thePrismParser == null)
						thePrismParser = new PrismParser();
					return thePrismParser;
				} catch (InterruptedException e) {
					SMGParser.class.notify();
					throw e;
				}
			}
		}

		/**
		 * Release (exclusive) access to the PRISM parser.
		 */
		public static synchronized void releasePrismParser()
		{
			prismParserInUse = false;
			Prism.class.notify();
		}
}
