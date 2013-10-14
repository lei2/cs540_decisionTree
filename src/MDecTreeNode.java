public class MDecTreeNode extends DecTreeNode{
	public int attrIndex;
	
	MDecTreeNode(String _label, String _attribute,
			String _parentAttributeValue, boolean _terminal, int attrIndex) {
		super(_label, _attribute, _parentAttributeValue, _terminal);
		this.attrIndex=attrIndex;
	}
}
