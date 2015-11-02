package adapt.plan;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Scanner;

import explicit.Distribution;
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
import strat.Strategies;
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
	Strategy stra, stra2;
	
	String logPath = "./myLog.txt";
	String modelPath = "C:\\Users\\USER\\git\\Planner\\PlanningComp\\Prismfiles\\teleAssistance.smg";
	String propPath = "C:\\Users\\USER\\git\\Planner\\PlanningComp\\Prismfiles\\propTeleAssistance.props";
	String modelConstPath = "C:\\Users\\USER\\git\\Planner\\PlanningComp\\IOFiles\\ModelConstants.txt";
	String propConstPath = "C:\\Users\\USER\\git\\Planner\\PlanningComp\\IOFiles\\PropConstants.txt";
	String expStratPath = "C:\\Users\\USER\\git\\Planner\\PlanningComp\\IOFiles\\strategy.adv";
	String expStratPath2 = "C:\\Users\\USER\\git\\Planner\\PlanningComp\\IOFiles\\strategy2.adv";
	String transPath = "C:\\Users\\USER\\git\\Planner\\PlanningComp\\IOFiles\\transition";

		
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
		// prismEx.exportTransToFile(arg0, arg1, arg2, arg3);
		 //only for DTMCs/CTMCs
		 //prismEx.doSteadyState(model);
		
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
     * Objective: It is used to build strategy based on Memoryless Deterministic Strategy
     * @throws PrismException
     * @throws InvalidStrategyStateException 
     */
    public void buildStrategy() throws PrismException, InvalidStrategyStateException
    {  
    	//get all choices for each state
		int[] ch = new int[model.getNumStates()];
    	for (int i=0; i < model.getNumStates(); i++)
        	ch[i] = model.getNumChoices(i)-1;
    	
    	//create the strategy
    	stra = new MemorylessDeterministicStrategy(ch);
//    	stra.init(0);
//    	Distribution dist = new Distribution();
//    	for (int i=0; i < model.getNumStates(); i++)
//    	{
//    		dist = stra.getNextMove(i);
//    		
//    		stra.updateMemory(ch[i], i);
//    	}
    	
		System.out.println("The memory size is :"+stra.getMemorySize());
		System.out.println("The type of the model is :"+model.getClass());
		stra.init(0);
    	//	System.out.println("state 1 = " + 3 + " strategy :"+stra.getNextMove(4));
    		//stra.updateMemory(model.getNumChoices(2), 2);
    		//System.out.println("state 2 = " + 3 + " strategy :"+stra.getNextMove(2));
    		//stra.updateMemory(0, 0);
    		//
        	builtStra = stra.buildProduct(model);
       // }            	
    }
    
    /**
     * Objective: It extracts all the possible transitions (before synthesizing)
     * @throws PrismException
     */
    public void exportTrans() throws PrismException
    {
    	File transFile = new File(transPath);
    	builtStra.exportToPrismExplicitTra(transFile);
    	//model.exportToPrismExplicit(transPath);
    }
    
    
    /**
     * Objective: to export the synthesize strategy into an external file
     * @param straFile1
     * @param straFile2
     * @throws  
     */
    public void exportStrategy(String straFile1, String straFile2)
    {
    	stra.exportToFile(straFile1);
    	stra2 = Strategies.loadStrategyFromFile(straFile1);
    	stra2.exportToFile(straFile2);
    	//mdstrat = mdstrat2 = null;
    }
    
   
    public void outcomefromStrategyGeneration() throws InvalidStrategyStateException
    {
    	  System.out.println("current memory element : "+stra.getCurrentMemoryElement());
          System.out.println("Strategy description : "+stra.getStateDescription());
    }

    
    /**
     * Objective: It is used to provide the required strategy to the adaptation engine
     * @return
     * @throws FileNotFoundException
     */
    public int getStrategy() throws FileNotFoundException
    {
    	//this function needs to be tested....
    	//so far, its reasonably ok.
    	
    	int choice = 0;
    	int decState = 4;
    	Scanner read = new Scanner(new BufferedReader(new FileReader(expStratPath)));
		//read.useDelimiter(",");
		int inData = -1;
		
		//need to skip the first two lines
		read.nextLine(); read.nextLine();
		while (read.hasNextLine()) {
			 inData = read.nextInt();
			 //find the decision state
			 if (inData == decState){
				 //pick up the selected choice
				 choice = read.nextInt();
				 break;
			 }
        }
		read.close();
		System.out.println("Obtained strategy is "+choice);
    	return choice;
    }
    
    /**
     * Objective: Read data from the transition file which is useful to map with the strategy file.
     * @param transPath
     * @throws FileNotFoundException
     */
    private void readTransition(String transPath) throws FileNotFoundException
    {
    	Scanner readMod = new Scanner(new BufferedReader(new FileReader(transPath)));
		//read.useDelimiter(",");
		String param = null;
		int val = -1;
		int count = 0;
		while (readMod.hasNext()) {
			 param = readMod.next();
			 val = readMod.nextInt();
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
                 
        //strategy related process
         try {
        	 
			buildStrategy();
			exportStrategy(expStratPath, expStratPath2);
			outcomefromStrategyGeneration();
			getStrategy();
		} catch (PrismException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidStrategyStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
         try {
			exportTrans();
		} catch (PrismException e) {
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

 		Planner plan = new Planner();
	    plan.synthesis();
			

         
 	}
}
