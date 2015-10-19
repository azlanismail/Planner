package adapt.plan;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import explicit.Model;
import explicit.PrismExplicit;
import explicit.SMG;
import explicit.rewards.ConstructRewards;
import explicit.rewards.SMGRewards;
import parser.Values;
import parser.ast.ModulesFile;
import parser.ast.PropertiesFile;
import parser.ast.RewardStruct;
import prism.Prism;
import prism.PrismException;
import prism.PrismFileLog;
import prism.PrismLangException;
import prism.PrismLog;
import prism.PrismSettings;
import prism.Result;
import simulator.SimulatorEngine;
import strat.InvalidStrategyStateException;
import strat.MemorylessDeterministicStrategy;
import strat.Strategy;
import synthesis.PrismChecker;

public class Planner {

	protected PrismLog mainLog;
	protected PrismSettings ps;
	protected PrismExplicit prismEx;
	protected Prism prism;
	protected Values v;
	protected ModulesFile modulesFile;
	protected PropertiesFile propertiesFile;
	protected SimulatorEngine simEngine;
	protected Model model;
	protected Result result;
	
		
	public Planner() throws FileNotFoundException, PrismLangException  
	{
		mainLog = new PrismFileLog("./myLog.txt");
        ps = new PrismSettings();
        prismEx = new PrismExplicit(mainLog, ps);
        prism = new Prism(mainLog , mainLog );
        
        //for assigning values of constants
    	v = new Values();
    	
    	//for parsing model file
    	modulesFile = prism.parseModelFile(new File("./Prismfiles/smg_example.prism"));
    	
    	//for parsing property model
    	propertiesFile = prism.parsePropertiesFile(modulesFile, new File("./Prismfiles/smg_example.props"));
    	
    	//for building and checking the model
    	simEngine = new SimulatorEngine(prism);
    	  
	}
	
	public void initialisePrism() throws PrismException
	{
		prism.initialise();
	}
	
	public void setConstantsforModel() throws PrismLangException
	{
		 v.addValue("CYCLEMAX", 2);
         v.addValue("TEST", 2);
         modulesFile.setUndefinedConstants(v);
	}
	
	public void setConstantsforProperty() throws PrismLangException
	{
		 propertiesFile.setUndefinedConstants(null);
	}
	
	public void buildModelbyPrismEx() throws PrismException
	{
		 model = prismEx.buildModel(modulesFile, simEngine);
	}
	
	public void checkModelbyPrismEx() throws PrismLangException, PrismException
	{
		 result = prismEx.modelCheck(model, modulesFile, propertiesFile , propertiesFile.getProperty(0));
	}
	
    public void synthesis()
    {
          try {
        	 //initialise the prism
        	 initialisePrism();

        	 setConstantsforModel();
        	 setConstantsforProperty();
             
             //build and check the model
             buildModelbyPrismEx();           
             checkModelbyPrismEx();
             
             //get the outcomes
             System.out.println("The current state is :"+simEngine.getCurrentState());
             System.out.println("The number of choice is :"+simEngine.getNumChoices());
             System.out.println("The result is :"+result.getResult());
             System.out.println("The result of counter example is :"+result.getCounterexample());
             System.out.println("Number of states :"+model.getNumStates());
             
             //construct the rewards
             ConstructRewards csr = new ConstructRewards(mainLog);
             RewardStruct rw = new RewardStruct();
             SMGRewards smgr = csr.buildSMGRewardStructure((SMG)model, rw, v);
             //How to set 
             System.out.println("The reward is :"+smgr.getStateReward(0));
             
             // open a file for writing the outcomes
             File f = new File("./myfile.txt");
           //      if(f.createNewFile())
           //        System.out.println("Success!");
           //      else
          //          System.out.println("Error, file already exists.");
            
            //generate the path
              //GenerateSimulationPath simPath = new GenerateSimulationPath(simEngine, mainLog);
              //String details = "time=100";
              //simPath.generateSimulationPath(modulesFile, null, details, 10, f);
              
            
              //get the number of choice from the simulator
              int numChoice = simEngine.getNumChoices();
              int[] ch = new int[numChoice];
              Strategy straAdapt = new MemorylessDeterministicStrategy(ch);
              straAdapt.buildProduct(model);
              straAdapt.exportToFile("./myfile.txt");
         
              System.out.println("current memory element : "+straAdapt.getCurrentMemoryElement());
              System.out.println("Strategy description : "+straAdapt.getStateDescription());
              System.out.println("get next move : "+straAdapt.getNextMove(0));
            
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
              System.out.println("Error: " + e.getMessage());
              System. exit(1);
         } 
          catch (InvalidStrategyStateException e) {
       	  System.out.println("Error: " + e.getMessage());
       	  System. exit(1);
		}
    }//end of synthesis
     
     public void display(){
   	  System.out.println("Calling from prism");
     }
     
     public static void main(String[] args) {
 		// TODO Auto-generated method stub
 		
 		//receive data from the analyzer
 		//a) cost/benefits data - to support reward calculation in the prism model
 		//b) latency data - to support the environment player
 		//c) adaptation goals - to support the am player
 		//d) configuration data - to support the am player
 			
 	
 	
 		
 		Planner plan = new Planner();
         plan.synthesis();
         
 	}

}
