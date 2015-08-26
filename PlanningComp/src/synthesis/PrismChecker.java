package synthesis;

import java.io.*;
import parser.Values;
import parser.ast.*;
import prism.*;

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
              Prism prism = new Prism(mainLog , mainLog );
              prism.initialise();
              
              
              //load the model with a constant value
              Values v = new Values();
              v.setValue("x" , 2);
              ModulesFile modulesFile = prism.parseModelFile( new File("./Prismfiles/dice.pm"));
              modulesFile .setUndefinedConstants(null);
              
              //load the property
              PropertiesFile propertiesFile = prism.parsePropertiesFile(modulesFile , new File("./Prismfiles/dice.pctl"));
              propertiesFile.setUndefinedConstants(v);
              Model model = prism.buildModel(modulesFile);
              Result result = prism.modelCheck( model, propertiesFile , propertiesFile.getProperty(0));
              System.out.println(result.getResult());
              System.out.println("testing");
              
              // write the outcomes into a file
              File f = new File("./myfile.txt");
                  if(f.createNewFile())
                    System.out.println("Success!");
                  else
                     System.out.println("Error, file already exists.");
              
              int expT = 1; 
              prism.exportStateRewardsToFile(model, expT, f);
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
     }
}
