public class MDecTreeNode extends DecTreeNode{
	public int attrIndex;
	public double midPoint;
	
	MDecTreeNode(String _label, String _attribute,
			String _parentAttributeValue, boolean _terminal, int attrIndex) {
		super(_label, _attribute, _parentAttributeValue, _terminal);
		this.attrIndex=attrIndex;
		this.midPoint=-1;
	}
	
	/**
	 * make a deep copy given such node
	 * @param node
	 */
	MDecTreeNode(MDecTreeNode node) {
		super(node.label, node.attribute, node.parentAttributeValue, node.terminal);
		this.attrIndex=node.attrIndex;
		this.midPoint=node.midPoint;
		//if it is not leaf node, deep copy every child
		if (!node.terminal){
			for (DecTreeNode child:node.children){
				MDecTreeNode mNode=(MDecTreeNode)child;
				MDecTreeNode copyNode=new MDecTreeNode(mNode);
				this.children.add(copyNode);
			}			
		}
	}
}
