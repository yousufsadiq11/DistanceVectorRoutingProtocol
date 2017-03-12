import java.net.*;
import java.util.*;
import java.util.regex.*;

class Server extends Thread 
{
	// Initializing sockets and variables
	private int portNumberValue;
	private String fileName;
	protected DatagramSocket serverSocketObj = null;
	private boolean flagCheck = true;
	Hashtable<String, Double> neighboring_pairs_map = null;
	ArrayList<String> neighbors_list = null;
	Hashtable<String, Vector<DistanceVectorProtocolModelClass>> currentNetworkHostName = null;
	
	// Initializing variables through constructor
	public Server(int portNumberValue, String fileName,
			Hashtable<String, Double> neighboring_pairs_map,
			ArrayList<String> neighbors_list,
			Hashtable<String, Vector<DistanceVectorProtocolModelClass>> currentNetworkHostValue)
			throws Exception 
	{
		super("DVServerThread");
		this.portNumberValue = portNumberValue;
		this.fileName = fileName;
		serverSocketObj = new DatagramSocket(portNumberValue);
		this.neighboring_pairs_map = neighboring_pairs_map;
		this.neighbors_list = neighbors_list;
		this.currentNetworkHostName = currentNetworkHostValue;
	}

	public void run() 
	{
		// Infinite Loop
		while (flagCheck) 
		{
			try 
				{
				// Reading from server
				byte[] received_server_data = new byte[32768];
				// Received packet from the server
				DatagramPacket received_server_dataGram = new DatagramPacket(
						received_server_data, received_server_data.length);
				serverSocketObj.receive(received_server_dataGram);
				// Parsing the received data to string
				String receivedString = new String(received_server_dataGram.getData(),
						0, received_server_dataGram.getLength());
				// Converting into desired printable format
				String receivedRegexDataValue = "(?:([A-Za-z\\s]+node ))([A-Za-z\\d\\.]+)(?:([A-Za-z\\s]+node ))([A-Za-z\\d\\.]+):(?:([A-Za-z\\s]+node ))([A-Za-z\\d\\.]+)(?:([A-Za-z\\s]+)+)([\\d.]+)";
				Pattern receivedPatternDataValue = Pattern.compile(
						receivedRegexDataValue, Pattern.CASE_INSENSITIVE
								| Pattern.DOTALL);
				LinkedList<RouteEstimator> routeList = new LinkedList<RouteEstimator>();
				// Obtain data line by line
				String[] linesSeparator = receivedString.split("\\n");
				// Adding data from each line along with cost to routeList
				for (int i = 0; i < linesSeparator.length; i++) 
				{
					Matcher matchedDataValue = receivedPatternDataValue.matcher(linesSeparator[i]);
					// Initializing variables
					String startNode = null;
					String destinationNode = null;
					String throughNode = null;
					String costValue = null;
					if (matchedDataValue.find()) 
					{
						startNode = matchedDataValue.group(2);
						destinationNode = matchedDataValue.group(4);
						throughNode = matchedDataValue.group(6);
						costValue = matchedDataValue.group(8);
					}
					// Adding data to list
					routeList.add(new RouteEstimator(startNode, throughNode, destinationNode, Double
							.parseDouble(costValue)));
				}
				LinkedList<RouteEstimator> receivedListDataValues = routeList;
				// Checking for updated data in the network
				Hashtable<String, Vector<DistanceVectorProtocolModelClass>> updatedDataValue = currentNetworkHostName;
				// Splitting dat from file Name
				String[] hostFileNameWithoutExtension = fileName.split("\\.dat");
				String startIndexValue = hostFileNameWithoutExtension[0];
				// Traversing through all the received data
				for (int i = 0; i < receivedListDataValues.size(); i++) 
				{
					RouteEstimator temp_obj = receivedListDataValues.get(i);
					// Updating the Cost
					if (updatedDataValue.containsKey(startIndexValue + "-"
							+ temp_obj.getDestinationNodeValue())) 
					{
						// Getting the updated data
						Vector<DistanceVectorProtocolModelClass> distanceVectorObj = updatedDataValue
								.get(startIndexValue + "-"
										+ temp_obj.getDestinationNodeValue());
						int current_index_value = neighbors_list.indexOf(temp_obj
								.getSourceNodeValue());
						double updatedCostValue = neighboring_pairs_map.get(startIndexValue
								+ "-" + temp_obj.getSourceNodeValue())
								+ temp_obj.getCostBetweenNodesValue();
						// Checking if the old cost is greater than obtained new cost
						if (distanceVectorObj.get(current_index_value).getCostValue() > updatedCostValue) 
						{
							distanceVectorObj.get(current_index_value).setCost(
									updatedCostValue);
						}
					} 
					else 
					{
						// If erroneous data is received then jump by one iteration
						if (startIndexValue.equals(temp_obj.getDestinationNodeValue())) 
						{
							continue;
						}
						// Adding the new destination Node
						else 
						{
							Vector<DistanceVectorProtocolModelClass> newNodeObj = new Vector<DistanceVectorProtocolModelClass>();
							// Adding new node and setting cost to max value i.e
							// 99999
							for (int k = 0; k < neighbors_list.size(); k++) 
							{
								newNodeObj.add(new DistanceVectorProtocolModelClass(startIndexValue,
										neighbors_list.get(k), temp_obj
												.getDestinationNodeValue(), 99999.0));
							}
							int current_index_value = neighbors_list.indexOf(temp_obj
									.getSourceNodeValue());
							// Replacing with new value
							newNodeObj.set(current_index_value, new DistanceVectorProtocolModelClass(
									startIndexValue, temp_obj.getSourceNodeValue(), temp_obj
											.getDestinationNodeValue(),
									neighboring_pairs_map.get(startIndexValue + "-"
											+ temp_obj.getSourceNodeValue())
											+ temp_obj.getCostBetweenNodesValue()));
							currentNetworkHostName.put(startIndexValue + "-"
									+ temp_obj.getDestinationNodeValue(), newNodeObj);
						}
					}
				}
			}
			// Throwing Exceptions
			catch (Exception e) 
			{
				e.printStackTrace();
			}
		}
		// Closing the server socket
		serverSocketObj.close();
	}
}

class RouteEstimator {
	private String sourceNodeValue;
	private String destinationNodeValue;
	private String throughNodeValue;
	private double costBetweenNodesValue;

	// Constructor for initialization of parameters
	public RouteEstimator(String input_source_node_value, String input_through_node_value,
			String input_destination_node_value, double input_cost_value) {
		sourceNodeValue = input_source_node_value;
		throughNodeValue = input_through_node_value;
		destinationNodeValue = input_destination_node_value;
		this.costBetweenNodesValue = input_cost_value;
	}
	// Getters and Setters
	public String getSourceNodeValue() {
		return sourceNodeValue;
	}

	public String getThroughNodeValue() {
		return throughNodeValue;
	}

	public String getDestinationNodeValue() {
		return destinationNodeValue;
	}

	public double getCostBetweenNodesValue() {
		return costBetweenNodesValue;
	}

	public void setCostBetweenNodesToValue(double updatedCostValue) {
		costBetweenNodesValue = updatedCostValue;
	}
}