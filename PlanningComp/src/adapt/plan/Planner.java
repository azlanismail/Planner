package adapt.plan;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Scanner;

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
	protected Values vm, vp;
	protected ModulesFile modulesFile;
	protected PropertiesFile propertiesFile;
	protected SimulatorEngine simEngine;
	protected Model model;
	protected Result result;
	protected ConstructRewards csr;
	protected RewardStruct rw;
	protected SMGRewards smgr;
	protected Strategy stra;
	
	Scanner readMod, readProp;
		
	public Planner() throws FileNotFoundException, PrismException  
	{
		mainLog = new PrismFileLog("./myLog.txt");
        ps = new PrismSettings();
        prismEx = new PrismExplicit(mainLog, ps);
        prism = new Prism(mainLog , mainLog );
        
        //for assigning values of constants
    	vm = new Values();
    	vp = new Values();
    	
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
	public void setConstantsforModel(String inFile) throws PrismLangException, FileNotFoundException
	{
		readMod = new Scanner(new BufferedReader(new FileReader(inFile)));
		//read.useDelimiter(",");
		String param = null;
		int val = -1;
		int count = 0;
		while (readMod.hasNext()) {
			 param = readMod.next();
			 val = Integer.parseInt(readMod.next());
			 vm.addValue(param, val);
			 count++;
        }
		if (count > 0)
			modulesFile.setUndefinedConstants(vm);
		else
			modulesFile.setUndefinedConstants(null);
	}
	
	public void setConstantsforProperty(String inFile) throws PrismLangException, FileNotFoundException
	{
		readProp = new Scanner(new BufferedReader(new FileReader(inFile)));
		String param = null;
		int val = -1;
		int count = 0;
		while (readProp.hasNext()) {
			 param = readProp.next();
			 val = Integer.parseInt(readProp.next());
			 vp.addValue(param, val);
			 count++;
        }
		if (count > 0)
			propertiesFile.setUndefinedConstants(vp);
		else
			propertiesFile.setUndefinedConstants(null);
	}

	public void buildModelbyPrismEx() throws PrismException
	{
		 model = prismEx.buildModel(modulesFile, simEngine);
	}
	
	public void buildRewards() throws PrismException
	{
		//construct the rewards
	    csr = new ConstructRewards(mainLog);
	    rw = new RewardStruct();
	    smgr = csr.buildSMGRewardStructure((SMG)model, rw, vp);
	}
	
	public void checkModelbyPrismEx() throws PrismLangException, PrismException
	{
		 result = prismEx.modelCheck(model, modulesFile, propertiesFile , propertiesFile.getProperty(0));
	}
	
    
    
    public void outcomefromSimEngine() throws PrismException
    {
    	System.out.println("The current state is :"+simEngine.getCurrentState());
        System.out.println("The number of choice is :"+simEngine.getNumChoices());
    }
	
    public void outcomefromModelChecking()
    {
    	 System.out.println("The result is :"+result.getResult());
    }
    
    public void outcomefromModelBuilding()
    {
    	System.out.println("Number of states :"+model.getNumStates());
    }
    
    public void outcomefromRewards()
    {
    	System.out.println("The reward at initial state is :"+smgr.getStateReward(0));
    }
    
    
    public void buildStrategy() throws PrismException
    {
    	//get the number of choice from the simulator
    	int numChoice = simEngine.getNumChoices();
        int[] ch = new int[numChoice];
        stra = new MemorylessDeterministicStrategy(ch);
    	stra.buildProduct(model);
    }
    
    public void exportStrategy(String straFile)
    {
    	stra.exportToFile(straFile);
    }
    
    public void outcomefromStrategyGeneration() throws InvalidStrategyStateException
    {
    	  System.out.println("current memory element : "+stra.getCurrentMemoryElement());
          System.out.println("Strategy description : "+stra.getStateDescription());
          System.out.println("get next move : "+stra.getNextMove(0));
    }

	public void synthesis() throws PrismException, InvalidStrategyStateException, FileNotFoundException
    {
          
    	 //initialise the prism
    	 initialisePrism();

    	 //read constants 
    	 setConstantsforModel("./IOFiles/ModelConstants.txt");
    	 setConstantsforProperty("./IOFiles/PropConstants.txt");
         
         //build and check the model
         buildModelbyPrismEx();           
         checkModelbyPrismEx();
         
         buildRewards();
         outcomefromRewards();
         
         //get the outcomes
         outcomefromSimEngine();
         outcomefromModelBuilding();
         outcomefromModelChecking();
         
                      
         buildStrategy();
         exportStrategy("./IOFiles/strategy.txt");
         
         outcomefromStrategyGeneration(); 
        
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
 				
 		Planner plan;
 		
		try {
			
			plan = new Planner();
			plan.synthesis();
			
		} catch (FileNotFoundException e ) {
            System.out.println("Error: " + e.getMessage());
            System. exit(1);
        }
         catch (PrismException e ) {
            System.out.println("Error: " + e.getMessage());
            System. exit(1);
        }
         catch (InvalidStrategyStateException e) {
      	  System.out.println("Error: " + e.getMessage());
      	  System. exit(1);
		}
         
 	}
}
