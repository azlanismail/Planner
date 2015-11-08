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
	Model model;
	Result result, resultSMG;
	Strategy strategy;
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
	
	public void initialisePrism() throws PrismException {
		prism.initialise();
	}
	
	public void setConstantsProbe(int probeId) {
		vm.addValue("CUR_PROBE", probeId);
	}
	
	public void setConstantsServiceType(int serviceId) {
		vm.addValue("SS_TY", serviceId);
	}
	
	public void setConstantsforTesting() {
		setConstantsProbe(1);
		setConstantsServiceType(1);
	}
	
	public void setConstantsforModel(String inFile) throws PrismLangException, FileNotFoundException {
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
    * Objective: To export the synthesize strategy into an external file
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
     * Objective: To get the decision state by referring to the transition data 
     * @return
     * @throws FileNotFoundException
     */
    public int getDecisionState() throws FileNotFoundException{
    	
    	//read from transition file
    	Scanner read = new Scanner(new BufferedReader(new FileReader(transPath)));
		//read.useDelimiter(",");
		int inData = -1;
		int decState = -1;
		
		//skip the first line
		read.nextLine();
		
		while (read.hasNextLine()) {
		   	//read the first four numbers
			//keep the first number as the potential decision state
			inData = read.nextInt(); read.nextInt(); read.nextInt(); read.nextInt();
			
			//check if the fifth element has a label as follow:
			if (read.hasNext("refreshAllService") || read.hasNext("refreshServiceType") || read.hasNext("refreshServiceType"))
			{
				//take the potential decision state as the right one
				decState = inData;
				break;
			}
        }
		read.close();
		if (decState == -1) throw new IllegalArgumentException("Invalid decision state");
		System.out.println("Decision state is:"+decState);
    	return decState;
    }
    /**
     * Objective: To get the best action from .adv file
     * @return
     * @throws FileNotFoundException
     */
    public int getAdaptStrategyfromFile() throws FileNotFoundException
    {    	
    	int choice = 0;
    	int decState = getDecisionState();
    	
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
     * Objective: To generate the adaptation plan
     */
    public void generatePlan() 
    {
    	 //initialise the prism
    	 try {
			initialisePrism();
		} catch (PrismException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
    	//assign constants values to the model 
    	try {
 			modulesFile.setUndefinedConstants(vm);
 		} catch (PrismLangException e) {
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
        
        try {
			exportTrans();
		} catch (PrismException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        //strategy related process
       	exportStrategy(expStratPath);
    }//end of synthesis
    
	public void synthesisforTesting() 
    {
          
    	 //initialise the prism
    	 try {
			initialisePrism();
		} catch (PrismException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

    	 //read constants from file
    	// try {
		//	setConstantsforModel(modelConstPath);
		//	setConstantsforProperty(propConstPath);
		//} catch (PrismLangException | FileNotFoundException e) {
		//	// TODO Auto-generated catch block
		//	e.printStackTrace();
		//}
        
    	//assign constants values to the model 
    	try {
 			modulesFile.setUndefinedConstants(vm);
 		} catch (PrismLangException e) {
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
	    //plan.synthesisforTesting();
 		try {
			System.out.println("Decision state is "+plan.getDecisionState());
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
 	}
     
}
