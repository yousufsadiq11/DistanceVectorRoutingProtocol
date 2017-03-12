

class DistanceVectorProtocolModelClass {	
	private String sourceNodeName;
	private String throughNodeName;
	private String destinationNodeName;
	private double costValue;
	
	// complete constructor
	public DistanceVectorProtocolModelClass(String sourceNode, String throughNode, String destinationNode, double costValue) {
		// s's neighbors
		this.sourceNodeName = sourceNode;
		this.throughNodeName = throughNode;
		this.destinationNodeName = destinationNode;
		this.costValue = costValue;
	}

	public String getSourceNodeName() {
		return sourceNodeName;
	}
	public String getThroughNodeName() {
		return throughNodeName;
	}
	public String getDestinationNodeName() {
		return destinationNodeName;
	}
	public double getCostValue() {
		return costValue;
	}
	public void setCost(double newCost) {
		costValue = newCost;
	}
}