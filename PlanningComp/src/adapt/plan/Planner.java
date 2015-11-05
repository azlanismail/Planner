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
import explicit.SMGModelChecker;
import explicit.STPG;
import explicit.STPGModelChecker;
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
	PrismExplicit prismEx;
	Prism prism;
	Values vm, vp;
	ModulesFile modulesFile;
	PropertiesFile propertiesFile;
	SimulatorEngine simEngine;
	Model model, builtStra;
	Result result, resultSTPG, resultSMG;
	ConstructRewards csr;
	RewardStruct rw;
	SMGRewards smgr;
	Strategy strategy, stra, stra2;
	SMGModelChecker smc;
	
	String logPath = "./myLog.txt";
	String laptopPath = "C:\\Users\\USER\\";
	String desktopPath = "H:\\";
	String mainPath = laptopPath;
	String modelPath = mainPath+"git\\Planner\\PlanningComp\\Prismfiles\\teleAssistance.smg";
	String propPath = mainPath+"git\\Planner\\PlanningComp\\Prismfiles\\propTeleAssistance.props";
	String modelConstPath = mainPath+"git\\Planner\\PlanningComp\\IOFiles\\ModelConstants.txt";
	String propConstPath = mainPath+"git\\Planner\\PlanningComp\\IOFiles\\PropConstants.txt";
	String expStratPath = mainPath+"git\\Planner\\PlanningComp\\IOFiles\\strategy";
	String transPath = mainPath+"git\\Planner\\PlanningComp\\IOFiles\\transition";
	
		
	public Planner()
	{
		
		mainLog = new PrismFileLog(logPath);
        prism = new Prism(mainLog , mainLog );
        prismEx = new PrismExplicit(prism.getMainLog(), prism.getSettings());
        
    	//for parsing model and property file
    	try {
			modulesFile = prism.parseModelFile(new File(modelPath));
			propertiesFile = prism.parsePropertiesFile(modulesFile, new File(propPath));
		} catch (FileNotFoundException | PrismLangException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	//for assigning values of constants
    	vm = new Values();
    	vp = new Values();			
    	
    	//for building and checking the model
    	simEngine = new SimulatorEngine(prism);
    	
    	//I need to access SMGModelChecker directly to manipulate the strategy
    	smc = new SMGModelChecker();
	}
	
	public void initialisePrism() throws PrismException
	{
		prism.initialise();
	}
	
	public void setConstantsforProbe(int probeId) {
		vm.addValue("CUR_PROBE", probeId);
		try {
			modulesFile.setUndefinedConstants(vm);
		} catch (PrismLangException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
		// result = prismEx.modelCheck(model, modulesFile, propertiesFile , propertiesFile.getProperty(0));
		smc.setModulesFileAndPropertiesFile(modulesFile, propertiesFile);
		smc.setGenerateStrategy(true);
		resultSMG = smc.check(model, propertiesFile.getProperty(0)); 
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
    	 System.out.println("The result from model checking (SMG) is :"+ resultSMG.getResultString());
    	 System.out.println("The outcome of the strategy is :"+smc.getStrategy());
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
     * Objective: It extracts the transitions which have been synthesized
     * @throws PrismException
     */
    public void exportTrans() throws PrismException
    {
    	File transFile = new File(transPath);
    	model.exportToPrismExplicitTra(transFile);
    }
    
    
   /**
    * Objective: to export the synthesize strategy into an external file
    * @param straFile
    */
    public void exportStrategy(String straFile)
    {
    	//assign the pointer from SMGModelChecker to strategy
    	strategy = smc.getStrategy();
    	
    	//export to .adv file
    	strategy.exportToFile(straFile);
    }
    
     
    
    /**
     * Objective: It is used to provide the required strategy to the adaptation engine
     * @return
     * @throws FileNotFoundException
     */
    public int getAdaptStrategyfromFile() throws FileNotFoundException
    {    	
    	int choice = 0;
    	int decState = 4;
    	
    	//Read from the exported strategy
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
         
                      
         outcomefromModelBuilding();
         outcomefromModelChecking();
                          
        //strategy related process
         try {
			exportTrans();
			exportStrategy(expStratPath);
			getAdaptStrategyfromFile();
			
		} catch (PrismException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
         
    }//end of synthesis
     
	
     public void display(){
   	  System.out.println("Calling from prism");
     }
     
     
     public static void main(String[] args) {
 		// TODO Auto-generated method stub

 		Planner plan = new Planner();
	    plan.synthesis();
 	}
     
}
