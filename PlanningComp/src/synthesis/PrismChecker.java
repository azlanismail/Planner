/**
 * Author@Azlan Ismail
 */
package synthesis;

import java.io.*;

import parser.PrismParser;
import parser.Values;
import parser.ast.*;
import prism.*;
import explicit.*;
import explicit.Model;

public class PrismChecker {

      public static void main(String[] args) {
           // TODO Auto-generated method stub
    	  PrismChecker pc = new PrismChecker();
          pc.synthesis();
     }
     
      public PrismChecker()
      {
    	  
      }

      public void synthesis()
     {
           try {
              PrismLog mainLog = new PrismFileLog("./myLog.txt");
              PrismSettings ps = new PrismSettings();
              PrismExplicit prism = new PrismExplicit(mainLog, ps);
              //prism.initialise();
              
              
              //load the model with a constant value
              Values v = new Values();
              v.setValue("x" , 2);
              ModulesFile modulesFile = prism. parseModelFile( new File("./Prismfiles/smg_example.prism"));
              ModulesFile modulesFile = mySMGParser(new File("./Prismfiles/smg_example.prism"));
              modulesFile .setUndefinedConstants(null);
              
              
              //load the property
              PropertiesFile propertiesFile = prism.parsePropertiesFile(modulesFile , new File("./Prismfiles/smg_example.props"));
              propertiesFile.setUndefinedConstants(v);
      
              explicit.Model model = (explicit.Model)prism.buildModelExplicit(modulesFile);
             // Result result = prism. modelCheck( model, propertiesFile , propertiesFile.getProperty(0));
             // System.out.println(result.getResult());
              System.out.println("Number of num states :"+model.getNumStates());
              
              // write the outcomes into a file
              File f = new File("./myfile.txt");
                  if(f.createNewFile())
                    System.out.println("Success!");
                  else
                     System.out.println("Error, file already exists.");
              
             // int expT = 1; 
             // prism.exportStateRewardsToFile(model, expT, f);
              //   prism.exportPRISMModel(f);
          }
           catch (FileNotFoundException e ) {
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
      
      public ModulesFile mySMGParser(File file) throws FileNotFoundException,
		PrismLangException
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
    			  modulesFile = prismParser.parseModulesFile(strModel, typeOverride);
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
}//end of class
