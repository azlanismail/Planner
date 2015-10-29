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



public class Planner {

	PrismLog mainLog;
	PrismSettings ps;
	PrismExplicit prismEx;
	Prism prism;
	Values vm, vp;
	ModulesFile modulesFile;
	PropertiesFile propertiesFile;
	SimulatorEngine simEngine;
	Model model, builtStra;
	Result result;
	ConstructRewards csr;
	RewardStruct rw;
	SMGRewards smgr;
	Strategy stra;
	
	String logPath = "./myLog.txt";
	String modelPath = "C:\\Users\\USER\\git\\Planner\\PlanningComp\\Prismfiles\\teleAssistance.smg";
	String propPath = "C:\\Users\\USER\\git\\Planner\\PlanningComp\\Prismfiles\\propTeleAssistance.props";
	String modelConstPath = "C:\\Users\\USER\\git\\Planner\\PlanningComp\\IOFiles\\ModelConstants.txt";
	String propConstPath = "C:\\Users\\USER\\git\\Planner\\PlanningComp\\IOFiles\\PropConstants.txt";
	String expStratPath = "C:\\Users\\USER\\git\\Planner\\PlanningComp\\IOFiles\\strategy.txt";
	String transPath = "C:\\Users\\USER\\git\\Planner\\PlanningComp\\IOFiles\\transition.txt";

		
	public Planner()
	{
		
		mainLog = new PrismFileLog(logPath);
        ps = new PrismSettings();
        prismEx = new PrismExplicit(mainLog, ps);
        prism = new Prism(mainLog , mainLog );
        
        //for assigning values of constants
    	vm = new Values();
    	vp = new Values();
        			
    	//for parsing model file
    	try {
			modulesFile = prism.parseModelFile(new File(modelPath));
		} catch (FileNotFoundException | PrismLangException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	//for parsing property model
    	try {
			propertiesFile = prism.parsePropertiesFile(modulesFile, new File(propPath));
		} catch (FileNotFoundException | PrismLangException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	//for building and checking the model
    	simEngine = new SimulatorEngine(prism);
   	  
	}
	
	public void initialisePrism() throws PrismException
	{
		prism.initialise();
	}
	
	public void setConstantsforModel(String inFile) throws PrismLangException, FileNotFoundException
	{
		Scanner readMod = new Scanner(new BufferedReader(new FileReader(inFile)));
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
		
		readMod.close();
	}
	
	public void setConstantsforProperty(String inFile) throws PrismLangException, FileNotFoundException
	{
		Scanner readProp = new Scanner(new BufferedReader(new FileReader(inFile)));
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
    	System.out.println("The current state (from simEngine) is :"+simEngine.getCurrentState());
        System.out.println("The number of choice (from simEngine) is :"+simEngine.getNumChoices());
        System.out.println("The number of transition (from simEngine) is :"+simEngine.getNumTransitions());
        System.out.println("The transition list (from simEngine) is:"+simEngine.getTransitionList() );
    }
	
    
    public void outcomefromModelChecking()
    {
    	 System.out.println("The result from model checking is :"+result.getResult());
    }
    
    public void outcomefromModelBuilding()
    {
    	System.out.println("Number of states (Model Building) :"+model.getNumStates());
    	System.out.println("Number of transitions (Model Building) :"+model.getNumTransitions());
    	for(int i=0; i < model.getNumStates(); i++){
    		System.out.println("Number of choice (Model Building) for state :"+i+ " is :"+model.getNumChoices(i));
    	}
    	
    }
    
    public void outcomefromRewards()
    {
    	System.out.println("The reward at initial state is :"+smgr.getStateReward(0));
    }
    
    /**
     * This function is used to build strategy based on Memoryless Deterministic Strategy
     * @throws PrismException
     * @throws InvalidStrategyStateException 
     */
    public void buildStrategy() throws PrismException, InvalidStrategyStateException
    {  
		
    	//for (int i=0; i < model.getNumStates(); i++)
       // {
    		int[] ch = new int[model.getNumChoices(4)];
    		stra = new MemorylessDeterministicStrategy(ch);
    		stra.init(0);
    	//	System.out.println("state 1 = " + 3 + " strategy :"+stra.getNextMove(1));
    		//stra.updateMemory(model.getNumChoices(2), 2);
    		//System.out.println("state 2 = " + 3 + " strategy :"+stra.getNextMove(2));
    		//stra.updateMemory(0, 0);
    		//
        	builtStra = stra.buildProduct(model);
       // }            	
    }
    
    public void exportTrans(String transPath) throws PrismException
    {
    	File transFile = new File(transPath);
    	builtStra.exportToPrismExplicitTra(transFile);
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

    /**
     * This function is used to provide the required strategy to the adaptation engine
     * @return
     */
    public int getStrategy()
    {
    	int choice = 0;
    	//extract from adv file
    	//perhaps i should only read the state where the adaptation action is selected
    	return choice;
    }
    
    private void readTransition(String transPath) throws FileNotFoundException
    {
    	Scanner readMod = new Scanner(new BufferedReader(new FileReader(transPath)));
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
		
		readMod.close();
    }
    
    
	public void synthesis() 
    {
          
    	 //initialise the prism
    	 try {
			initialisePrism();
		} catch (PrismException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

    	 //read constants 
    	 try {
			setConstantsforModel(modelConstPath);
			setConstantsforProperty(propConstPath);
		} catch (PrismLangException | FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
         
         //build and check the model
         try {
			buildModelbyPrismEx();
			checkModelbyPrismEx();
		} catch (PrismException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
         
      //   try {
		//	buildRewards();
	//	} catch (PrismException e) {
			// TODO Auto-generated catch block
	//		e.printStackTrace();
	//	}
         
       //  outcomefromRewards();
         
         outcomefromModelBuilding();
         outcomefromModelChecking();
         
         //get the outcomes
       //  try {
		//	outcomefromSimEngine();
		//} catch (PrismException e) {
			// TODO Auto-generated catch block
		//	e.printStackTrace();
		//}
                 
                      
         try {
			buildStrategy();
		} catch (PrismException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidStrategyStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
         
         exportStrategy(expStratPath);
         
         try {
			exportTrans(transPath);
		} catch (PrismException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
         
         try {
			outcomefromStrategyGeneration();
		} catch (InvalidStrategyStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
    	//String logPath = "./myLog.txt";
 	    //String modelPath = "./Prismfiles/smg_example.prism";
 	    //String propPath = "./Prismfiles/smg_example.props";
 	    //String modelConstPath = "./IOFiles/ModelConstants.txt";
 	    //String propConstPath = "./IOFiles/PropConstants.txt";
 	    //String expStratPath = "./IOFiles/strategy.txt";
 	    
 	    
 		Planner plan = new Planner();
	    plan.synthesis();
			

         
 	}
}
