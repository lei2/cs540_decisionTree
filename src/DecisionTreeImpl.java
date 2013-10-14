import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Fill in the implementation details of the class DecisionTree using this file.
 * Any methods or secondary classes that you want are fine but we will only
 * interact with those methods in the DecisionTree framework.
 * 
 * You must add code for the 4 methods specified below.
 * 
 * See DecisionTree for a description of default methods.
 */
public class DecisionTreeImpl extends DecisionTree {

	private List<Integer> validAttributeList;
	
	private MDecTreeNode decisionTree; 
	private List<String> attributeList;
	
	/**
	 * Answers static questions about decision trees.
	 */
	DecisionTreeImpl() {
		// no code necessary
		// this is void purposefully
	}

	/**
	 * Build a decision tree given only a training set.
	 * 
	 * @param train
	 *            the training set
	 */
	DecisionTreeImpl(DataSet train) {
		// this.validAttributeList=new ArrayList<Integer>();
		// for (int i =0 ; i <train.instances.get(0).attributes.size(); i ++){
		// this.validAttributeList.add(new Integer(i));
		// }
		// distriAnalysis(train);
		// recursive method to build the tree
		if (train.instances.isEmpty()) {
			System.out.println("there is no training data.");
			System.exit(1);
		} else {
			attributeList=new ArrayList<String>();
			attributeList.add("Status of existing checking account");
			attributeList.add("Credit history");
			attributeList.add("Purpose");
			attributeList.add("Savings account/bonds");
			attributeList.add("Duration in month");
			attributeList.add("Credit amount");
			attributeList.add("foreign worker");			
			this.validAttributeList=new ArrayList<Integer>();
			for (int i=0; i<7;i++){
				this.validAttributeList.add(i);
			}
			// by default, the child's label is its parents label, attribute is null. the child is not a leaf
			MDecTreeNode rootNode = new MDecTreeNode(
					plurality(train.instances),
					null,
					"ROOT", false,-1);
			buildDecisionTree(train.instances,
					validAttributeList, rootNode);
			this.decisionTree=rootNode;
		}
	}

	/**
	 * recursively build the decision tree
	 * 
	 * @param instances
	 * @param attributes
	 * @param parentInstances
	 * @param parentAtt
	 */
	private void buildDecisionTree(List<Instance> instances,
			List<Integer> attributes, MDecTreeNode curNode) {
		// if empty, return plurality value of parents
		if (instances.isEmpty()) {
			//label is parent's label by default
			//acknowledge leaf
			curNode.terminal=true;
			curNode.children=null;
			return;
		} else if (allSameLabel(instances)) {
			//acknowledge leaf, change label
			curNode.terminal=true;
			curNode.children=null;
			curNode.label=instances.get(0).label;
			return;
		} else if (attributes.isEmpty()) {
			//acknowledge leaf, change label
			curNode.terminal=true;
			curNode.children=null;
			curNode.label=plurality(instances);
		} else { //curNode should not be leaf. construct its subtree
			String curLabel=plurality(instances);
			curNode.label=curLabel;
			//find the attribute index in attributes list with max mutual information
			int curAttr = importance(attributes, instances);
			curNode.attribute=this.attributeList.get(attributes.get(curAttr));
			curNode.attrIndex=attributes.get(curAttr);
			// for that attribute, create a map with all possible outcomes of
			// that attribute. Associate items with such outcomes
			Map<String, List<Instance>> attMap = new HashMap<String, List<Instance>>();
			for (Instance item : instances) {
				//get the attribute value for curAttr
				String mAtt = trueAtt(item, attributes.get(curAttr), instances);
				if (attMap.containsKey(mAtt)) {
					List<Instance> itemList = attMap.get(mAtt);
					itemList.add(item);
				} else {
					List<Instance> newList = new ArrayList<Instance>();
					newList.add(item);
					attMap.put(mAtt, newList);
				}
			}
			for (Map.Entry<String, List<Instance>> mEntry : attMap.entrySet()) {
				// construct a child new node for each answer
				// by default, the child's label is its parents label, attribute is null. the child is not a leaf
				MDecTreeNode childNode = new MDecTreeNode(
						curLabel,
						null,
						mEntry.getKey(), false,-1);
				//a deep copy of attributes
				List<Integer> childAttributes=new ArrayList<Integer>(attributes);
				//does it affect original list? DEBUG
				childAttributes.remove(curAttr);
				curNode.addChild(childNode);
				buildDecisionTree(mEntry.getValue(), childAttributes, childNode);
			}
		}

	}

	/**
	 * get a particular attribute of a certain node
	 * @param item
	 * @param curAttr, the index of attribute insterested in.
	 * @param instances
	 * @return
	 */
	private String trueAtt(Instance item, int curAttr, List<Instance> instances) {
		if (0==instances.size()){
			System.out.println("ERROR: zero number array of instances passed into truAtt");
			System.exit(1);
		}
		String result=null;
		if (4==curAttr || 5 ==curAttr){
			//discretize
			List<Integer> valueList=new ArrayList<Integer>();
			for (Instance eachItem: instances){
				valueList.add(Integer.valueOf(eachItem.attributes.get(curAttr)));
			}
			//find the max and min
			int max=Integer.MIN_VALUE;
			int min=Integer.MAX_VALUE;
			for (int index=0;index<valueList.size();index++){
				int value=valueList.get(index);
				if (value>max){
					max=value;
				}
				if (value < min){
					min=value;
				}
			}
			double midPoint=0.5*(max+min);
			int curValue=Integer.valueOf(item.attributes.get(curAttr));
			if (curValue>midPoint){
				return "A";
			} else {
				return "B";
			}
		} else {
			result=item.attributes.get(curAttr);
		}
		return result;
	}

	/**
	 * find the most important attribute given the set of points
	 * 
	 * @param attributes
	 * @param instances
	 * @return
	 */
	private int importance(List<Integer> attributes, List<Instance> instances) {
		if (instances.isEmpty()) {
			System.out.println("0 instances passed into importance method.");
			System.exit(1);
		}
		// find the attribute that minimize the H(Y|X)
		int minIndex = -1;
		// loose upper bound. Entropy should be in [0,1]
		double minValue = 2.0;
		for (int index = 0; index < attributes.size(); index++) {
			// for each attribute, create a map with all possible outcomes of
			// that attribute. Associate items with such outcomes
			Map<String, List<Instance>> attMap = new HashMap<String, List<Instance>>();
			for (Instance item : instances) {
				String mAtt = trueAtt(item, attributes.get(index),instances);
				if (attMap.containsKey(mAtt)) {
					List<Instance> itemList = attMap.get(mAtt);
					itemList.add(item);
				} else {
					List<Instance> newList = new ArrayList<Instance>();
					newList.add(item);
					attMap.put(mAtt, newList);
				}
			}
			// find H(Y|X)
			double condEntropy = 0.0;
			for (Map.Entry<String, List<Instance>> mEntry : attMap.entrySet()) {
				// probability of X=v
				int numInstances = mEntry.getValue().size();
				if (0!=numInstances){
					// value of H(Y|X=v)
					double condEntropyV = entropy(mEntry.getValue());
					double pValue = (double) numInstances
							/ (double) instances.size();
					condEntropy += pValue * condEntropyV;
				} else {
					System.out.println("0 instances for attribute value::  attribute: "+attributes.get(index)+" attribute value:"+mEntry.getKey());
				}
			}
			if (condEntropy < minValue) {
				minValue = condEntropy;
				minIndex = index;
			}
		}
		return minIndex;
	}

	/**
	 * find the entropy given a list of instances
	 * 
	 * @return
	 */
	private double entropy(List<Instance> instances) {
		if (instances.isEmpty()) {
			System.out.println("0 instances passed into entropy method.");
			System.exit(1);
		}
		Map<String, Integer> labelMap = new HashMap<String, Integer>();
		for (int i = 0; i < instances.size(); i++) {
			Instance mItem = instances.get(i);
			if (labelMap.containsKey(mItem.label)) {
				int oldValue = labelMap.get(mItem.label);
				labelMap.put(mItem.label, oldValue + 1);
			} else {
				labelMap.put(mItem.label, 1);
			}
		}
		double tentropy = 0.0;
		for (Map.Entry<String, Integer> mEntry : labelMap.entrySet()) {
			int numInstances = mEntry.getValue();
			// probability of that value
			double pValue = (double) numInstances / (double) instances.size();
			tentropy += -pValue * (Math.log(pValue) / Math.log(2.0));
		}
		return tentropy;
	}

	/**
	 * check whether all instances have the same label
	 * 
	 * @param instances
	 * @return
	 */
	private boolean allSameLabel(List<Instance> instances) {
		if (instances.isEmpty()) {
			System.out.println("0 instances passed into allSameLabel method.");
			System.exit(1);
		}
		String label = instances.get(0).label;
		for (int i = 0; i < instances.size(); i++) {
			if (!instances.get(i).label.equals(label))
				return false;
		}
		return true;
	}

	/**
	 * return the majority vote of label in a list
	 * 
	 * @param instances
	 * @return the label with most items
	 */
	private String plurality(List<Instance> instances) {
		if (0==instances.size()){
			System.out.print("ERROR: zero-sized array passed into pularity function\n");
			System.exit(1);
			return null;
		} else {
			Map<String, Integer> mLabel = new HashMap<String, Integer>();
			for (Instance item : instances) {
				if (mLabel.containsKey(item.label)) {
					int oldValue = mLabel.get(item.label);
					mLabel.put(item.label, oldValue + 1);
				} else {
					mLabel.put(item.label, 1);
				}
			}
			TreeMap<Integer, String> sortedMap = new TreeMap<Integer, String>(
					Collections.reverseOrder());
			for (Map.Entry<String, Integer> mItem : mLabel.entrySet()) {
				sortedMap.put(mItem.getValue(), mItem.getKey());
			}
			String resultLabel=sortedMap.firstEntry().getValue();
			int firKey=sortedMap.firstKey();
			//remove max label
			sortedMap.remove(firKey);
			if (sortedMap.isEmpty()){
				//tie condition
				return "1";
			}
			return resultLabel;
		}
	}


	/**
	 * Build a decision tree given a training set then prune it using a tuning
	 * set.
	 * 
	 * @param train
	 *            the training set
	 * @param tune
	 *            the tuning set
	 */
	DecisionTreeImpl(DataSet train, DataSet tune) {

		// TODO: add code here

	}

	@Override
	/**
	 * Evaluates the learned decision tree on a test set.
	 * @return the label predictions for each test instance 
	 * 	according to the order in data set list
	 */
	public String[] classify(DataSet test) {
		String[] result = new String[test.instances.size()];
		for (int index=0;index<result.length;index++){
			Instance item = test.instances.get(index);
			String label = getClassification(this.decisionTree,item);
			result[index]=label;
		}
		return result;
	}

	/**
	 * get classification given an item
	 * @param decisionTree
	 * @param item
	 * @return
	 */
	private String getClassification(MDecTreeNode decisionTree, Instance item) {
		MDecTreeNode curNode=decisionTree;
		while (!curNode.terminal){
			int attrIndex=curNode.attrIndex;
			String itemAttr=item.attributes.get(attrIndex);
			boolean find=false;
			for (DecTreeNode curNodeChild: curNode.children){
				if (curNodeChild.parentAttributeValue.equals(itemAttr)){
					curNode=(MDecTreeNode)curNodeChild;
					find=true;
					break;
				}
			}
			if (false==find){
//				System.out.println(item.toString());
			}
		}
		return curNode.label;
	}

	@Override
	/**
	 * Prints the tree in specified format. It is recommended, but not
	 * necessary, that you use the print method of DecTreeNode.
	 * 
	 * Example:
	 * Root {Existing checking account?}
	 *   A11 (2)
	 *   A12 {Foreign worker?}
	 *     A71 {Credit Amount?}
	 *       A (1)
	 *       B (2)
	 *     A72 (1)
	 *   A13 (1)
	 *   A14 (1)
	 *         
	 */
	public void print() {
		this.decisionTree.print(0);
	}

}

/**
 * helper class to get all information about every attribute
 * 
 * @author junjue
 * 
 */
class AttriStat {
	// number of answers to a question
	public List<String> choices;
	// how many items are there for each answer
	public List<Integer> instanceNum;

	// label string

	public AttriStat() {
		this.choices = new ArrayList<String>();
		this.instanceNum = new ArrayList<Integer>();
	}

	/**
	 * 
	 * @return the total number of instances
	 */
	public int getTotalInstanceNum() {
		int sum = 0;
		for (Integer eachNum : this.instanceNum) {
			sum += eachNum;
		}
		return sum;
	}
}
