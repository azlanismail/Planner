package adapt.plan;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.List;
import java.util.Random;
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
	String modelPath = mainPath+"git\\Planner\\PlanningComp\\Prismfiles\\teleAssistance_v4.smg";
	String propPath = mainPath+"git\\Planner\\PlanningComp\\Prismfiles\\propTeleAssistance.props";
	String modelConstPath = mainPath+"git\\Planner\\PlanningComp\\IOFiles\\ModelConstants.txt";
	String propConstPath = mainPath+"git\\Planner\\PlanningComp\\IOFiles\\PropConstants.txt";
	String stratPath = mainPath+"git\\Planner\\PlanningComp\\IOFiles\\strategy";
	String transPath = mainPath+"git\\Planner\\PlanningComp\\IOFiles\\transition";
	
	//important parameters to the model
	String md_probe = "CUR_PROBE";
	String md_maxCS = "MAX_CS";
	String md_maxRT = "MAX_RT";
	String md_maxFR = "MAX_FR";
	String md_goalTY = "GOAL_TY";
	String md_serviceType = "SV_TY";
	String md_serviceFailedId = "SV_FAIL_ID";
	int index = 0;
	String type = null;
	String md_sv_id = "SV_"+type+""+index+"_ID";
	String md_sv_rt = "SV_"+type+""+index+"_RT";
	String md_sv_cs = "SV_"+type+""+index+"_CS";
	String md_sv_fr = "SV_"+type+""+index+"_FR";

		
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
		vm.setValue(md_probe, probeId);
	}
	
	public void setConstantsGoalType(int goalType) {
		vm.setValue(md_goalTY, goalType);
	}
	
	public void setServiceType(String serviceType) {
		System.out.println("Type detected and sent to the model is :"+serviceType);
    	if (serviceType.equalsIgnoreCase("MedicalAnalysisService"))
    		setConstantsServiceType(0);
    	if (serviceType.equalsIgnoreCase("AlarmService"))
    		setConstantsServiceType(1);
    	if (serviceType.equalsIgnoreCase("DrugService"))
    		setConstantsServiceType(2);
	}
	
	public void setConstantsServiceType(int typeId) {
		System.out.println("Received service type is +"+typeId);
		vm.setValue(md_serviceType, typeId);
	}
	
	public void setConstantsFailedServiceId(int serviceId) {
		vm.setValue(md_serviceFailedId, serviceId);
	}
	
	public void setConstantsMaxCost(double maxCS) {
		vm.setValue(md_maxCS, maxCS);
	}
	
	public void setConstantsMaxResponseTime(int maxRT) {
		vm.setValue(md_maxRT, maxRT);
	}
	
	public void setConstantsMaxFailureRate(double maxFR) {
		vm.setValue(md_maxFR, maxFR);
	}
	
	public void setConstantsServiceProfile(int i, int rt, double cs, double fr) {
		
		if (i <= 3) {
			index = i; 
			type = "ALARM";
		}
		else{
			index = i;
			type = "MEDIC";
		}
	
		vm.setValue(md_sv_id, i); vm.setValue(md_sv_rt, rt);
		vm.setValue(md_sv_cs, cs); vm.setValue(md_sv_fr, fr);
	}
	
	/**
	 * 
	 * @param goalType
	 * @param probe
	 * @param type
	 * @param id
	 * @param maxRT
	 * @param maxFR
	 */
	public void setConstantsTesting(int goalType, int probe, int type, int id, int maxRT, double maxCS, double maxFR) {
		setConstantsGoalType(goalType);
		setConstantsProbe(probe);
		setConstantsServiceType(type);
		setConstantsFailedServiceId(id);
		setConstantsMaxResponseTime(maxRT);
		setConstantsMaxCost(maxCS);
		setConstantsMaxFailureRate(maxFR);
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
		
		//property 0 - find the minimum response time
		//property 1 - find the minimum cost
		if(vm.getIntValueOf(md_goalTY) == 0) {
			System.out.println("synthesis is based on minimum cost");
			resultSMG = smc.check(model, propertiesFile.getProperty(0));
		}
		if(vm.getIntValueOf(md_goalTY) == 1) {
			System.out.println("synthesis is based on reliability");
			resultSMG = smc.check(model, propertiesFile.getProperty(1));
		}
		if(vm.getIntValueOf(md_goalTY) == 2) {
			System.out.println("synthesis is based on minimum response time");
			resultSMG = smc.check(model, propertiesFile.getProperty(2));
		}
		
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
    	// System.out.println("The result from model checking (SMG) is :"+ resultSMG.getResultString());
    	// System.out.println("The outcome of the strategy is :"+smc.getStrategy());
    }
    
    public void outcomefromModelBuilding()
    {
    	System.out.println("Number of states (Model Building) :"+model.getNumStates());
    	System.out.println("Number of transitions (Model Building) :"+model.getNumTransitions());
    //	for(int i=0; i < model.getNumStates(); i++){
    //		System.out.println("Number of choice (Model Building) for state :"+i+ " is :"+model.getNumChoices(i));
    //	}	
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
    public void exportStrategy()
    {
    	//assign the pointer from SMGModelChecker to strategy
    	strategy = smc.getStrategy();
    	
    	//export to .adv file
    	strategy.exportToFile(stratPath);
    	
    	//System.out.println("State Description ");
    }
    
    /**
     * Objective: To get the decision state by referring to the transition data 
     * @return
     * @throws FileNotFoundException
     */
    public int getDecisionState() throws IllegalArgumentException, FileNotFoundException{
    	
    	//read from transition file
    	Scanner read = new Scanner(new BufferedReader(new FileReader(transPath)));
		//read.useDelimiter(",");
		int prevState = -1;
		int curState = -1;
		int decState = -1;
		int count = 0;
		//skip the first line
		read.nextLine();
		prevState = read.nextInt();
		read.nextLine();
		//System.out.println("prev state is "+prevState);
		while (read.hasNextLine()) {
		   	curState = read.nextInt();
		   //	System.out.println("current state is "+curState);
			if (prevState == curState) {
				decState = curState;
				break;
			}
			else {
				prevState = curState;
			}
			read.nextLine();
			
        }
		read.close();
		
		if (decState == -1) throw new IllegalArgumentException("Invalid decision state");
		System.out.println("Decision state is :"+decState);
		
		
    	return decState;
    }
    
    /**
     * Objective: To get the decision state by referring to the transition data 
     * @return
     * @throws FileNotFoundException
     */
    public int getDecisionState_old() throws FileNotFoundException{
    	
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
    
    public String getActionLabel(int decState, int decAction) throws FileNotFoundException {
    	//read from transition file
    	Scanner read = new Scanner(new BufferedReader(new FileReader(transPath)));
		//read.useDelimiter(",");
    	int curState = -1;
    	int curAction = -1;
    	String label = null;
    	boolean status = false;
		//skip the first line
		read.nextLine();
		while (read.hasNextLine()) {
		   	curState = read.nextInt();
		   //	System.out.println("current state is "+curState);
			if (curState == decState) {
				curAction = read.nextInt();
				if (curAction == decAction) {
					//skip two columns
					read.nextInt(); read.nextInt();
					label = read.next();
					status = true;
				}
			}
			if (status == true) break;
			read.nextLine();
        }
		read.close();
		System.out.println("Label is :"+label);
    	return label;
    }
    
    public int getServiceIdfromLabel(String label){
    	int serviceId = -1;
    	
    	//map the selected action from strategy and transition
    	
    	try {
    		
    		//in the case of retry
    		if (label.equalsIgnoreCase("Retry")) serviceId = vm.getIntValueOf(md_serviceFailedId);
    		
    		//in the case of medical service
			//if ((vm.getIntValueOf(md_serviceType) == 0) && label.equalsIgnoreCase("MedicalService1")) serviceId = 4;
			//if ((vm.getIntValueOf(md_serviceType) == 0) && label.equalsIgnoreCase("MedicalService2")) serviceId = 5;
			//if ((vm.getIntValueOf(md_serviceType) == 0) && label.equalsIgnoreCase("MedicalService3")) serviceId = 6;
			//if ((vm.getIntValueOf(md_serviceType) == 0) && label.equalsIgnoreCase("MedicalService4")) serviceId = 7;
			//if ((vm.getIntValueOf(md_serviceType) == 0) && label.equalsIgnoreCase("MedicalService5")) serviceId = 8;
			
			if (label.equalsIgnoreCase("MedicalService1")) serviceId = 4;
			if (label.equalsIgnoreCase("MedicalService2")) serviceId = 5;
			if (label.equalsIgnoreCase("MedicalService3")) serviceId = 6;
			if (label.equalsIgnoreCase("MedicalService4")) serviceId = 7;
			if (label.equalsIgnoreCase("MedicalService5")) serviceId = 8;
			
			
			//in the case of alarm service
			//if ((vm.getIntValueOf(md_serviceType) == 1) && label.equalsIgnoreCase("AlarmService1")) serviceId = 1;
			//if ((vm.getIntValueOf(md_serviceType) == 1) && label.equalsIgnoreCase("AlarmService2")) serviceId = 2;
			//if ((vm.getIntValueOf(md_serviceType) == 1) && label.equalsIgnoreCase("AlarmService3")) serviceId = 3;
		
			if (label.equalsIgnoreCase("AlarmService1")) serviceId = 1;
			if (label.equalsIgnoreCase("AlarmService2")) serviceId = 2;
			if (label.equalsIgnoreCase("AlarmService3")) serviceId = 3;
		
			//in the case of drug service
			//if ((vm.getIntValueOf(md_serviceType) == 2) && label.equalsIgnoreCase("DrugService1")) serviceId = 9;
			//if ((vm.getIntValueOf(md_serviceType) == 2) && label.equalsIgnoreCase("DrugService2")) serviceId = 9;
			
			if (label.equalsIgnoreCase("DrugService1")) serviceId = 9;
			if (label.equalsIgnoreCase("DrugService2")) serviceId = 9;
			
			
		} catch (PrismLangException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	return serviceId;
    }
    
    
    /**
     * Objective: To get the best action from .adv file
     * @return
     * @throws FileNotFoundException
     */
    public int getAdaptStrategyfromFile_old() throws FileNotFoundException
    {    	
    	int choice = 0;
    	int decState = getDecisionState();
    	
    	//Read from the exported strategy
    	Scanner read = new Scanner(new BufferedReader(new FileReader(stratPath)));
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
		String label = getActionLabel(decState,choice);
		System.out.println("Service id is "+getServiceIdfromLabel(label));
		return getServiceIdfromLabel(label);
    }
    
    public int getAdaptStrategyfromFile() throws IllegalArgumentException, FileNotFoundException
    {   
    	//==============Get the decision strategy
    	//read from transition file
    	Scanner read = new Scanner(new BufferedReader(new FileReader(transPath)));
		//read.useDelimiter(",");
		int prevState = -1;
		int curState = -1;
		int decState = -1;
		int count = 0;
		//skip the first line
		read.nextLine();
		prevState = read.nextInt();
		read.nextLine();
		//System.out.println("prev state is "+prevState);
		while (read.hasNextLine()) {
		   	curState = read.nextInt();
		   //	System.out.println("current state is "+curState);
			if (prevState == curState) {
				decState = curState;
				break;
			}
			else {
				prevState = curState;
			}
			read.nextLine();
			
        }
		read.close();
		
		if (decState == -1) throw new IllegalArgumentException("Invalid decision state");
		System.out.println("Decision state is :"+decState);
		
    	//========get the strategy
    	int choice = 0;
    	//int decState = getDecisionState();
    	
    	//Read from the exported strategy
    	Scanner readS = new Scanner(new BufferedReader(new FileReader(stratPath)));
		//read.useDelimiter(",");
		int inData = -1;
		
		//need to skip the first two lines
		readS.nextLine(); readS.nextLine();
		while (readS.hasNextLine()) {
			 inData = readS.nextInt();
			 //find the decision state
			 if (inData == decState){
				 //pick up the selected choice
				 choice = readS.nextInt();
				 break;
			 }
        }
		readS.close();
		System.out.println("Obtained strategy is "+choice);
		
		//===========get the label======================
		Scanner readL = new Scanner(new BufferedReader(new FileReader(transPath)));
		curState = -1;
		int decAction = choice;
    	int curAction = -1;
    	String label = null;
    	boolean status = false;
		//skip the first line
		readL.nextLine();
		while (readL.hasNextLine()) {
		   	curState = readL.nextInt();
		   //	System.out.println("current state is "+curState);
			if (curState == decState) {
				curAction = readL.nextInt();
				if (curAction == decAction) {
					//skip two columns
					readL.nextInt(); readL.nextInt();
					label = readL.next();
					status = true;
				}
			}
			if (status == true) break;
			readL.nextLine();
        }
		readL.close();
		System.out.println("Label is :"+label);
		
		//==========get the id
		System.out.println("Service id is "+getServiceIdfromLabel(label));
		return getServiceIdfromLabel(label);
    }
   
    /**
     * Objective: To generate the adaptation plan
     */
    public void initialPlan() 
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
       	exportStrategy();
       	
    }//end of synthesis
    
    /**
     * Objective: To generate the adaptation plan
     */
    public void adaptPlan() 
    {
    	 //initialise the prism
    	// try {
		//	initialisePrism();
		//} catch (PrismException e) {
			// TODO Auto-generated catch block
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
        
        try {
			exportTrans();
		} catch (PrismException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        //strategy related process
       	exportStrategy();
       	
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
			exportStrategy();
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
 		Random rand = new Random();
 		int serviceType = -1;
 		for (int i=0; i < 100; i++)
 	    {
 			System.out.println("number of cycle :"+i);
 			serviceType = rand.nextInt(2);
 			plan.setConstantsTesting(1,-1,serviceType,-1,26,20,0.7);
 	    
 			plan.adaptPlan();
	  
 			try {
 				//plan.getDecisionState();
 				plan.getAdaptStrategyfromFile();
 			} 
 			catch (IllegalArgumentException e) {
 				e.printStackTrace();
 			}
 			catch (FileNotFoundException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 				System.err.println("something not right");
 			}
 	    }
 	}
     
}
