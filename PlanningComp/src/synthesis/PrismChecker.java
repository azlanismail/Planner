package synthesis;

import java.io.*;
import parser.Values;
import parser.ast.*;
import prism.*;

public class PrismChecker {

      public static void main(String[] args) {
           // TODO Auto-generated method stub
          new PrismChecker().go();
     }
     

      public void go()
     {
           try {
              PrismLog mainLog = new PrismFileLog("stdout");
              Prism prism = new Prism(mainLog , mainLog );
              prism.initialise();
            
              
              //load the model with a constant value
              Values v = new Values();
              v.setValue( "CYCLEMAX" , 2);
              ModulesFile modulesFile = prism.parseModelFile( new File("./Prismfiles/mainmodel_v11.smg" ));
              modulesFile .setUndefinedConstants(v);
              
              //load the property
              PropertiesFile propertiesFile = prism.parsePropertiesFile(modulesFile , new File("./Prismfiles/prop200815.props"));
              propertiesFile.setUndefinedConstants( null);
              Model model = prism.buildModel(modulesFile);
            //  Result result = prism.modelCheck( model, propertiesFile , propertiesFile.getProperty(0));
            //  System.out.println(result.getResult());
              System.out.println("testing");
              
              //write the outcomes into a file
              File f = new File("./myfile.txt");
              if(f.createNewFile())
                  System.out.println("Success!");
              else
                  System.out.println("Error, file already exists.");
              
              int expT = 1; 
              prism.exportStateRewardsToFile(model, expT, f);
          }
           catch (FileNotFoundException e ) {
              System. out .println("Error: " + e.getMessage());
              System. exit(1);
          }
           catch (PrismException e ) {
              System. out .println("Error: " + e.getMessage());
              System. exit(1);
          }
           catch(IOException e) {
              // ioe.printStackTrace();
               System. out .println("Error: " + e.getMessage());
               System. exit(1);
          }
     }
}
