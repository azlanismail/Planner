/**
 * Author@Azlan Ismail
 */
package synthesis;

import java.io.*;

import parser.Values;
import parser.ast.*;
import prism.*;
import explicit.*;
import explicit.Model;
import explicit.rewards.*;
import simulator.*;


public class PrismChecker {

      public static void main(String[] args) {
           // TODO Auto-generated method stub
    	  PrismChecker pc = new PrismChecker();
          pc.synthesis();
     }
     
      public void synthesis()
     {
           try {
              PrismLog mainLog = new PrismFileLog("./myLog.txt");
              PrismSettings ps = new PrismSettings();
              PrismExplicit prismEx = new PrismExplicit(mainLog, ps);
              Prism prism = new Prism(mainLog , mainLog );
              prism.initialise();

              //load the model with the required constant value
              Values v = new Values();
              v.setValue("CYCLEMAX", 2);
              ModulesFile modulesFile = prism.parseModelFile(new File("./Prismfiles/mainmodel_v11.smg"));
              modulesFile.setUndefinedConstants(v);
              
              //load the property
              PropertiesFile propertiesFile = prism.parsePropertiesFile(modulesFile, new File("./Prismfiles/prop270815.props"));
              propertiesFile.setUndefinedConstants(null);
              
              //build and check the model
              SimulatorEngine simEngine = new SimulatorEngine(prism);
              Model model = prismEx.buildModel(modulesFile, simEngine);
              Result result = prismEx.modelCheck(model, modulesFile, propertiesFile , propertiesFile.getProperty(0));
              
              
              //get the outcomes
              System.out.println("The result is "+result.getResult());
              System.out.println(result.getCounterexample());
              System.out.println("Number of num states :"+model.getNumStates());
              
              //construct the rewards
              ConstructRewards csr = new ConstructRewards(mainLog);
              RewardStruct rw = new RewardStruct();
              SMGRewards smgr = csr.buildSMGRewardStructure((SMG)model, rw, v);
              System.out.println("The reward is "+smgr.getStateReward(3));
              
              // write the outcomes into a file
              File f = new File("./myfile.txt");
                  if(f.createNewFile())
                    System.out.println("Success!");
                  else
                     System.out.println("Error, file already exists.");
             
             //generate the path
               GenerateSimulationPath simPath = new GenerateSimulationPath(simEngine, mainLog);
               String details = "time=100";
               simPath.generateSimulationPath(modulesFile, null, details, 10, f);
                 
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
      
}//end of class
