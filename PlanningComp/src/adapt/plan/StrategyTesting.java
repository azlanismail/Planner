package adapt.plan;

import java.io.FileNotFoundException;

public class StrategyTesting {

	public static void main(String[] args) throws FileNotFoundException {
		// TODO Auto-generated method stub
		Planner plan = new Planner();
		System.out.println("Obtained strategy is "+plan.getStrategy());
	}

}
