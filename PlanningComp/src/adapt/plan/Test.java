package adapt.plan;

import java.io.File;
import java.io.FileNotFoundException;

import explicit.DTMCSimple;
import parser.ast.ModulesFile;
import prism.ExplicitModel2MTBDD;
import prism.Model;
import prism.Modules2MTBDD;
import prism.Prism;
import prism.PrismException;
import prism.PrismLangException;
import prism.PrismLog;

public class Test {
	



		public static DTMCSimple dtmc = new DTMCSimple();
		
		static PrismLog l1;
		static PrismLog l2;
		
		public static ModulesFile mdf = new ModulesFile();
		public static Prism prism = new Prism(null,null);
		public static ExplicitModel2MTBDD emodel = new ExplicitModel2MTBDD(prism);
		public static Modules2MTBDD smodel;
		
		public static void main(String[] args) throws PrismException {
			// TODO Auto-generated method stub
			
			File file = new File("H:/git/Planner/PlanningComp/Prismfiles/m1.pm");
			
			try {
				mdf = prism.parseModelFile(file);
				System.out.println(mdf.toString());
				smodel = new Modules2MTBDD(prism,mdf);
				System.out.println(smodel.translate().getModuleName(0));
			} catch (FileNotFoundException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (PrismLangException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
			
		
	}
}
