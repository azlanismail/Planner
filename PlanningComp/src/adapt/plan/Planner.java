package adapt.plan;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Random;
import java.util.Scanner;

import explicit.Model;
import explicit.PrismExplicit;
import explicit.SMGModelChecker;
import parser.Values;
import parser.ast.ModulesFile;
import parser.ast.PropertiesFile;
import prism.Prism;
import prism.PrismException;
import prism.PrismFileLog;
import prism.PrismLangException;
import prism.PrismLog;
import prism.PrismSettings;
import prism.Result;
import simulator.SimulatorEngine;
import strat.Strategy;


public class Planner {

	//Classes from Prism-games
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
	PrismSettings ps;
	
	//Defining File Inputs/Outputs
	String logPath = "./myLog.txt";
	String laptopPath = "C:/Users/USER/git/Planner/PlanningComp/";
	String desktopPath = "H:/git/Planner/PlanningComp/";
	String linuxPath = "/home/azlani/git/Planner/PlanningComp/";
	String mainPath = laptopPath;
	String modelPath = mainPath+"Prismfiles/teleAssistanceAdapt_v5.smg";
	String propPath = mainPath+"Prismfiles/propTeleAssistance.props";
	String modelConstPath = mainPath+"IOFiles/ModelConstants.txt";
	String propConstPath = mainPath+"IOFiles/PropConstants.txt";
	String stratPath1 = mainPath+"IOFiles/strategyInitial";
	String transPath1 = mainPath+"IOFiles/transitionInitial";
	String stratPath2 = mainPath+"IOFiles/strategy";
	String transPath2 = mainPath+"IOFiles/transition";

	//Defining parameters to the stochastic-games model
	String md_probe = "CUR_PROBE";
	String md_maxCS = "MAX_CS";
	String md_maxRT = "MAX_RT";
	String md_maxFR = "MAX_FR";
	//String md_goalTQ = "GOAL_TQ";
	String md_goalTY = "GOAL_TY";
	String md_serviceType = "SV_TY";
	String md_serviceFailedId = "SV_FAIL_ID";
	String md_delay = "CUR_DELAY";
	String md_maxDelay = "MAX_DELAY";
	String md_minDelay = "MIN_DELAY";
	
	String md_retry = "RETRY";

	//utility-based decision making
	String md_wg_cs = "WG_CS"; 
	String md_wg_rt = "WG_RT";  
	String md_wg_fr = "WG_FR"; 
	
	//Defining properties for the planner
	private int stage;
	
	public Planner(int sg) {
		this.stage = sg;
		initiatePlanner();
		initializeServiceProfile();
		setDelay();
	}
	
	private void initiatePlanner(){
		mainLog = new PrismFileLog(logPath);
        prism = new Prism(mainLog , mainLog);
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
		System.out.println("Received service type is :"+typeId);
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
	
	public void setConstantsUtilWeight(double wgCS, double wgRT, double wgFR) {
		vm.setValue(md_wg_cs, wgCS);
		vm.setValue(md_wg_rt, wgRT);
		vm.setValue(md_wg_fr, wgFR);
	}
	
	public void setConstantsRetry(int r) {
		vm.setValue(md_retry, r);
	}
	
	
	public void initializeServiceProfile(){
		//set the service profiles for alarm service
 		setConstantsServiceProfile(1, 11, 4.0, 0.11);
 		setConstantsServiceProfile(2, 9, 12.0, 0.04);
		setConstantsServiceProfile(3, 3, 2.0, 0.18);
		
		//set the service profiles for medical analysis service
		setConstantsServiceProfile(4,22,4.0,0.12);
		setConstantsServiceProfile(5,27,14.0,0.07);
		setConstantsServiceProfile(6,31,2.15,0.18);
		setConstantsServiceProfile(7,29,7.3,0.25);
		setConstantsServiceProfile(8,20,11.9,0.05);
		
		//set the service profiles for drug service
		setConstantsServiceProfile(9,1,2,0.01);
		setConstantsServiceProfile(10,1,2,0.01);
		
	}
	
	public void setConstantsServiceProfile(int i, int rt, double cs, double fr) {
		String type = null;
		int id = 0;
		if(i <= 3) {
			type = "ALARM";
			id = i;
		}
		else if (i <= 8){
			type = "MEDIC";
			if (i == 4) id = 1;
			if (i == 5) id = 2;
			if (i == 6) id = 3;
			if (i == 7) id = 4;
			if (i == 8) id = 5;
		}
		else if (i <= 10){
			type = "DRUG";
			if (i == 9) id = 1;
			if (i == 10) id = 2;
			
		}
		else
			System.err.println("Parameters are not matched with the parameters in the model");
		
		String md_sv_id = "SV_"+type+""+id+"_ID";
		String md_sv_rt = "SV_"+type+""+id+"_RT";
		String md_sv_cs = "SV_"+type+""+id+"_CS";
		String md_sv_fr = "SV_"+type+""+id+"_FR";
		
		vm.setValue(md_sv_id, i); 
		vm.setValue(md_sv_rt, rt);
		vm.setValue(md_sv_cs, cs); 
		vm.setValue(md_sv_fr, fr);
	}
	
	public void setDelay(){
		Random rand = new Random();
		int maxDelay = 5;
		int minDelay = 0;
		int delay = rand.nextInt(maxDelay - minDelay + 1) + minDelay;
		System.out.println("Delay :"+delay);
		vm.setValue(md_delay, delay);
		vm.setValue(md_maxDelay, maxDelay);
		vm.setValue(md_minDelay, minDelay);
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
	public void setConstantsTesting(int goalType, int probe, int type, int id, int maxRT, double maxCS, double maxFR, int r) {
		setConstantsGoalType(goalType);
		setConstantsProbe(probe);
		setConstantsServiceType(type);
		setConstantsFailedServiceId(id);
		setConstantsMaxResponseTime(maxRT);
		setConstantsMaxCost(maxCS);
		setConstantsMaxFailureRate(maxFR);
		setConstantsRetry(r);
	}
	
	/**
	 * It is used in QoS requirement classes and to assign parameters for the initial stage planning
	 * @param goalType
	 * @param probe
	 * @param type
	 * @param id
	 */
	public void setConstantsParams(int goalType, int probe, String type, int id, int r, double wcs, double wrt, double wfr) {
		setConstantsGoalType(goalType);
		setConstantsProbe(probe);
		setServiceType(type);
		setConstantsFailedServiceId(id);
		setConstantsRetry(r);
		setConstantsUtilWeight(wcs, wrt, wfr);
		//setConstantsMaxResponseTime(maxRT);
		//setConstantsMaxCost(maxCS);
		//setConstantsMaxFailureRate(maxFR);
	}
	
//	public void setConstantsforModel(String inFile) throws PrismLangException, FileNotFoundException {
//		Scanner readMod = new Scanner(new BufferedReader(new FileReader(inFile)));
//		//read.useDelimiter(",");
//		String param = null;
//		int val = -1;
//		int count = 0;
//		while (readMod.hasNext()) {
//			 param = readMod.next();
//			 val = Integer.parseInt(readMod.next());
//			 vm.addValue(param, val);
//			 count++;
//        }
//		if (count > 0)
//			modulesFile.setUndefinedConstants(vm);
//		else
//			modulesFile.setUndefinedConstants(null);
//		
//		readMod.close();
//	}
	
//	public void setConstantsforProperty(String inFile) throws PrismLangException, FileNotFoundException
//	{
//		Scanner readProp = new Scanner(new BufferedReader(new FileReader(inFile)));
//		String param = null;
//		int val = -1;
//		int count = 0;
//		while (readProp.hasNext()) {
//			 param = readProp.next();
//			 val = Integer.parseInt(readProp.next());
//			 vp.addValue(param, val);
//			 count++;
//        }
//		if (count > 0)
//			propertiesFile.setUndefinedConstants(vp);
//		else
//			propertiesFile.setUndefinedConstants(null);
//	}

	public void buildModelbyPrismEx() throws PrismException
	{
		 model = prismEx.buildModel(modulesFile, simEngine);
	}
	
	public void checkModelbyPrismEx() throws PrismLangException, PrismException
	{
		smc.setModulesFileAndPropertiesFile(modulesFile, propertiesFile);
		smc.setGenerateStrategy(true);
		
		if(vm.getIntValueOf(md_goalTY) == 0) {
			System.out.println("Planning is based on minimizing the cost");
			resultSMG = smc.check(model, propertiesFile.getProperty(0));
		}
		if(vm.getIntValueOf(md_goalTY) == 1) {
			System.out.println("Planning is based on minimizing the failure rate");
			resultSMG = smc.check(model, propertiesFile.getProperty(1));
		}
		if(vm.getIntValueOf(md_goalTY) == 2) {
			System.out.println("Planning is based on minimizing the response time");
			resultSMG = smc.check(model, propertiesFile.getProperty(2));
		}
		if(vm.getIntValueOf(md_goalTY) == 3) {
			System.out.println("Planning is based on utility function");
			resultSMG = smc.check(model, propertiesFile.getProperty(3));
		}
		//if(vm.getIntValueOf(md_goalTY) == 4) {
		//	System.out.println("Planning is based on multiobjective");
		//	resultSMG = smc.check(model, propertiesFile.getProperty(4));
		//}
//		if(vm.getIntValueOf(md_goalTY) == 4) {
//			System.out.println("Planning is based on maximizing the cost");
//			resultSMG = smc.check(model, propertiesFile.getProperty(4));
//		}
//		if(vm.getIntValueOf(md_goalTY) == 5) {
//			System.out.println("Planning is based on maximizing the failure rate");
//			resultSMG = smc.check(model, propertiesFile.getProperty(5));
//		}
//		if(vm.getIntValueOf(md_goalTY) == 6) {
//			System.out.println("Planning is based on maximizing the response time");
//			resultSMG = smc.check(model, propertiesFile.getProperty(6));
//		}
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
    }
       
     
    /**
     * Objective: It extracts the transitions which have been synthesized
     * @throws PrismException
     */
    public void exportTrans() throws PrismException
    {
    	if (this.stage == 0){
    		File transFile1 = new File(transPath1);
    		model.exportToPrismExplicitTra(transFile1);
    	}else{
    		File transFile2 = new File(transPath2);
    		model.exportToPrismExplicitTra(transFile2);
    	}
    	
    }
    
    
   /**
    * Objective: To export the synthesize strategy into an external file
    * @param straFile
    */
    public void exportStrategy()
    {
    	//assign the pointer from SMGModelChecker to strategy
    	strategy = smc.getStrategy();
    	
    	if (this.stage == 0) {
    	//export to .adv file
    	strategy.exportToFile(stratPath1);
    	}else {
    		strategy.exportToFile(stratPath2);
    	}
    }
    
       
    
   
    
    public int getServiceIdfromLabel(String label){
    	int serviceId = -1;
    	
    	//map the selected action from strategy and transition
    	
    	try {
    		//in the case of retry
    		if (label.equalsIgnoreCase("Retry")) serviceId = vm.getIntValueOf(md_serviceFailedId);
    			
			if (label.equalsIgnoreCase("MedicalService1")) serviceId = 4;
			if (label.equalsIgnoreCase("MedicalService2")) serviceId = 5;
			if (label.equalsIgnoreCase("MedicalService3")) serviceId = 6;
			if (label.equalsIgnoreCase("MedicalService4")) serviceId = 7;
			if (label.equalsIgnoreCase("MedicalService5")) serviceId = 8;
			
			if (label.equalsIgnoreCase("AlarmService1")) serviceId = 1;
			if (label.equalsIgnoreCase("AlarmService2")) serviceId = 2;
			if (label.equalsIgnoreCase("AlarmService3")) serviceId = 3;
			
			if (label.equalsIgnoreCase("DrugService1")) serviceId = 9;
			if (label.equalsIgnoreCase("DrugService2")) serviceId = 9;
		} catch (PrismLangException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	
    	return serviceId;
    }
    
    
    public int getDecisionStateM1() throws FileNotFoundException
    {
    	//read from transition file
    	Scanner read;
    	
    	if (this.stage ==0)
    		read = new Scanner(new BufferedReader(new FileReader(transPath1)));
    	else
    		read = new Scanner(new BufferedReader(new FileReader(transPath2)));
		//read.useDelimiter(",");
		int prevState = -1;
		int curState = -1;
		int decState = -1;
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
    
    
    public int getDecisionStateM2() throws FileNotFoundException
    {
    	//read from transition file
    	Scanner read;
    	
    	if (this.stage ==0)
    		read = new Scanner(new BufferedReader(new FileReader(transPath1)));
    	else
    		read = new Scanner(new BufferedReader(new FileReader(transPath2)));
		//read.useDelimiter(",");
		int prevState = -1;
		int curState = -1;
		int decState = -1;
		//skip the first line
		read.nextLine();
		
		//read four items
		
		//if the fifth item is not null then its the solution
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
    
    public int getAdaptStrategyfromAdv() throws IllegalArgumentException, FileNotFoundException
    {   
    	//==============Get the decision strategy
    	int decState = getDecisionStateM1();
		
    	//========get the strategy
    	int choice = 0;
    	//int decState = getDecisionState();
    	
    	//Read from the exported strategy
    	Scanner readS;
    	if (this.stage ==0)
    		readS = new Scanner(new BufferedReader(new FileReader(stratPath1)));
    	else
    		readS = new Scanner(new BufferedReader(new FileReader(stratPath2)));
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
		Scanner readL;
		if (this.stage ==0)
    		readL = new Scanner(new BufferedReader(new FileReader(transPath1)));
    	else
    		readL = new Scanner(new BufferedReader(new FileReader(transPath2)));
		int curState = -1;
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
    public void generate() 
    {        
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
    
     
     public static void main(String[] args) {
 		// TODO Auto-generated method stub

    	//0-means the initial stage
    	//1-means the adaptation stage
    	int stage = 0;
 		Planner plan = new Planner(stage); 
 		
 		//set the service profiles for alarm service
 		plan.setConstantsServiceProfile(1, 11, 4.0, 0.11);
 		plan.setConstantsServiceProfile(2, 9, 12.0, 0.04);
		plan.setConstantsServiceProfile(3, 3, 2.0, 0.18);
		
		//set the service profiles for medical analysis service
		plan.setConstantsServiceProfile(4,22,4.0,0.12);
		plan.setConstantsServiceProfile(5,27,14.0,0.07);
		plan.setConstantsServiceProfile(6,31,2.15,0.18);
		plan.setConstantsServiceProfile(7,29,7.3,0.25);
		plan.setConstantsServiceProfile(8,20,11.9,0.05);
		
		//set the service profiles for drug service
		plan.setConstantsServiceProfile(9,1,2,0.01);
		plan.setConstantsServiceProfile(10,1,2,0.01);
				
 		Random rand = new Random();
 		int serviceType = -1;
 		int cycle = 200;
 		int goalType = 3;
 		int retry = 1;
 		long time[] = new long[cycle];
 		TimeMeasure tm = new TimeMeasure();
 		
 		for (int i=0; i < cycle; i++)
 	    {
 			tm.start();
 			System.out.println("number of cycle :"+i);
 			serviceType = rand.nextInt(3);
 			//serviceType = 0;
 			plan.setConstantsTesting(goalType,2,serviceType,-1,26,20,0.7, retry);
 			
 			if (goalType == 3) {
 				plan.setConstantsUtilWeight(0.3, 0.3, 0.4);
 			}
 	    
 			plan.generate();
	  
 			try {
 				plan.getAdaptStrategyfromAdv();
 			} 
 			catch (IllegalArgumentException e) {
 				e.printStackTrace();
 			}
 			catch (FileNotFoundException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 				System.err.println("something not right");
 			}
 			tm.stop();
 			time[i] = tm.getDuration();
 	    }
 		
 		long total = 0;
 		for(int k=0; k < cycle; k++)
 			total +=time[k];
 		
 		long avg = (total/cycle);
 		System.out.println("The average time is "+avg);
 	}
     
}
