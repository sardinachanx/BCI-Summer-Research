package datacollect;

import libsvm.svm;
import libsvm.svm_model;
import libsvm.svm_node;
import libsvm.svm_parameter;
import libsvm.svm_problem;

public class NSClassifier {
	
	private static final int TOTAL_CLASSES = 2;
	
	private svm_model model;
	
	public NSClassifier(int[] labels, double[][] trainingData){
		this(createSvmProblem(labels, trainingData));
	}
	
	private NSClassifier(svm_problem problem){
		svm_parameter param = getSvmParam();
		model = createSvmModel(problem, param);
	}
	
	private static svm_model createSvmModel(svm_problem problem, svm_parameter param){
		return svm.svm_train(problem, param);
	}
	
	private static svm_problem createSvmProblem(int[] labels, double[][] data){
		if(labels.length != data.length){
			return null;
		}
		svm_problem problem = new svm_problem();
		problem.l = labels.length;
		problem.y = new double[labels.length];
		problem.x = new svm_node[labels.length][];
		for(int i = 0; i < labels.length; i++){
			double[] row = data[i];
			svm_node[] nodes = new svm_node[row.length];
			for(int j = 0; j < row.length; j++){
				svm_node node = new svm_node();
				node.index = j;
				node.value = row[j];
				nodes[j] = node;
			}
			problem.x[i] = nodes;
			problem.y[i] = labels[i];
		}
		return problem;
	}
	
	private static svm_parameter getSvmParam(){
		svm_parameter param = new svm_parameter();
		param.probability = 1;
		param.gamma = 0.5;
		param.nu = 0.5;
		param.C = 1;
		param.svm_type = svm_parameter.C_SVC;
		param.kernel_type = svm_parameter.LINEAR;
		param.eps = 0.001;
		return param;
	}
	
	public double evaluate(double[] point){
		svm_node[] nodes = new svm_node[point.length - 1];
		for(int i = 0; i < point.length; i++){
			svm_node node = new svm_node();
			node.index = i;
			node.value = point[i];
			nodes[i] = node;
		}
		
		int[] labels = new int[TOTAL_CLASSES];
		svm.svm_get_labels(model, labels);
		
		double[] estimates = new double[TOTAL_CLASSES];
		double estimated = svm.svm_predict_probability(model, nodes, estimates);
		for (int i = 0; i < TOTAL_CLASSES; i++){
	        System.out.print("(" + labels[i] + ":" + estimates[i] + ")");
	    }
	    System.out.println("(Actual:" + point[0] + " Prediction:" + estimated + ")");            

	    return estimated;
	}
	
	public double[] evaluate(double[][] points){
		double[] result = new double[points.length];
		for(int i = 0; i < points.length; i++){
			result[i] = evaluate(points[i]);
		}
		return result;
	}
	
	public void updateSvmModel(int[] labels, double[][] trainingData){
		createSvmModel(createSvmProblem(labels, trainingData), getSvmParam());
	}
}
