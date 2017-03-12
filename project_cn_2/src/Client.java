import java.io.*;
import java.net.*;
import java.util.*;

class Client extends Thread 
{
	// Initializing the variables
	private int portNumberValue;
	private String fileName;
	// Client Socket
	private DatagramSocket clientSocketObj = null;
	private int iteration_count = 0;
	private int neighbours_Count = 0;
	Hashtable<String, Double> neighbor_pairs_map = null;
	// List for neighboring nodes
	ArrayList<String> neighbor_nodes_list = null;
	Hashtable<String, Vector<DistanceVectorProtocolModelClass>> currentNetworkHostName = null;

	// Constructor for initializing parameters
	public Client(int portNumberValue, String fileName,
			Hashtable<String, Double> neighbor_pairs_map,
			ArrayList<String> neighbor_nodes_list,
			Hashtable<String, Vector<DistanceVectorProtocolModelClass>> currentNetworkHostName)
			throws IOException 
	{
		super("DVClientThread");
		this.portNumberValue = portNumberValue;
		this.fileName = fileName;
		clientSocketObj = new DatagramSocket();
		this.neighbor_pairs_map = neighbor_pairs_map;
		this.neighbor_nodes_list = neighbor_nodes_list;
		this.currentNetworkHostName = currentNetworkHostName;
	}

	public void run() 
	{
		// Infinite loop
		while (true) 
		{
			try 
			{
				// Initializing Host Name
				String currentHostName = InetAddress.getLocalHost().getHostName();
				iteration_count++;
				// Printing the iteration count on the console
				System.out.println("\nDistance Vector Algorithm Running At " + currentHostName + " \nIteration number:  "
						+ iteration_count + "\n");
			} 
			catch (UnknownHostException u) 
			{
				u.printStackTrace();
			}
			try 
			{
				DatagramPacket clientPacketDatagram = null;
				// sending updated data
				byte[] send_buffer_obj = new byte[32768];
				String tempString = "";
				// Reading newly received information
				if (iteration_count == 1) 
				{
					ReadingDataFromHost(fileName);
					tempString = routingTableCalculator(currentNetworkHostName);
					// sending all the data from the list
					for (int n = 0; n < neighbor_nodes_list.size(); n++) 
					{
						send_buffer_obj = tempString.getBytes();
						InetAddress Next_node_address = InetAddress
								.getByName(neighbor_nodes_list.get(n) + ".dat");
						clientPacketDatagram = new DatagramPacket(send_buffer_obj,
								send_buffer_obj.length, Next_node_address,
								portNumberValue);
						clientSocketObj.send(clientPacketDatagram);
					}
					// Displaying updated info on the console
					String info_data = printResult(tempString);
					System.out.println("Info has been Sent to the following "
							+ neighbor_nodes_list.size() + " neighbors" + "\n"
							+ info_data);
				}
				// Info for new file
				else if (iteration_count > 1) 
				{
					boolean flag_variable = false;
					// For a new file name
					// Printing the updated cost between the corresponding nodes
					LinkedList<String> newNodeList = null;
					// Stream readers for reading data
					// Initializing them
					FileInputStream c_file_reader_obj = null;
					InputStreamReader c_input_reader_obj = null;
					BufferedReader buffer_read_obj = null;
					// Result List
					LinkedList<String> resultList = null;
					try 
					{
						// Reading data from file
						c_file_reader_obj = new FileInputStream(fileName);
						c_input_reader_obj = new InputStreamReader(c_file_reader_obj);
						buffer_read_obj = new BufferedReader(c_input_reader_obj);
						resultList = new LinkedList<String>();
						String splitString;
						// Splitting by spaces and eliminating .dat extension
						while ((splitString = buffer_read_obj.readLine()) != null) 
						{
							String[] hostFile = fileName.split("\\.dat");
							String[] words = splitString.split(" ");
							// Adding it to result list
							if (words.length == 2) 
							{
								resultList.add(hostFile[0] + "-" + words[0] + " "
										+ Double.parseDouble(words[1]));
							}
						}
						newNodeList = resultList;
					}
					// Throwing corresponding exceptions
					catch (FileNotFoundException fnf) 
					{
						fnf.printStackTrace();
					} 
					catch (IOException io) 
					{
						io.printStackTrace();
					}
					// Closing all the streams
					finally 
					{
						try 
						{
							buffer_read_obj.close();
							c_input_reader_obj.close();
							c_file_reader_obj.close();
						} 
						catch (IOException ioe) 
						{
							ioe.printStackTrace();
						}
					}
					// for all the nodes comparing costs and displaying on the console
						for (int i = 0; i < newNodeList.size(); i++) 
						{
						String[] newNodeCost = newNodeList.get(i).split(" ");
						// Comparing costs and displaying on the console
						if (neighbor_pairs_map.get(newNodeCost[0]) != Double
								.parseDouble(newNodeCost[1])) 
						{
							flag_variable = true;
							System.out.println("for the following nodes "
									+ neighbor_pairs_map.get(newNodeCost[0]));
							System.out.println(Double
									.parseDouble(newNodeCost[1]));
							System.out.println("Cost is changed to "
									+ flag_variable);
							neighbor_pairs_map.remove(newNodeCost[0]);
							neighbor_pairs_map.put(newNodeCost[0], Double
									.parseDouble(newNodeCost[1]));
						}
					}

					if (flag_variable) 
					{
						neighbor_nodes_list = new ArrayList<String>();
						// Reading data from new host
						ReadingDataFromHost(fileName);
						tempString = routingTableCalculator(currentNetworkHostName);
						for (int n = 0; n < neighbor_nodes_list.size(); n++) 
						{
							send_buffer_obj = tempString.getBytes();
							InetAddress address_inet = InetAddress
									.getByName(neighbor_nodes_list.get(n) + ".dat");
							clientPacketDatagram = new DatagramPacket(send_buffer_obj,
									send_buffer_obj.length, address_inet, portNumberValue);
							clientSocketObj.send(clientPacketDatagram);
						}
						// printing data on the console
						String simpleString = printResult(tempString);
						System.out.println("Info has been Sent to "
								+ neighbor_nodes_list.size() + " neighbors" + ":\n"
								+ simpleString);
						// Updating the flag to false
						flag_variable = false;
					} 
					else 
					{
						// Passing data to routing table for updation
						tempString = routingTableCalculator(currentNetworkHostName);
						for (int n = 0; n < neighbor_nodes_list.size(); n++)
						{
							send_buffer_obj = tempString.getBytes();
							InetAddress address_inet = InetAddress
									.getByName(neighbor_nodes_list.get(n) + ".dat");
							clientPacketDatagram = new DatagramPacket(send_buffer_obj,
									send_buffer_obj.length, address_inet, portNumberValue);
							clientSocketObj.send(clientPacketDatagram);
						}
						String simpleString = printResult(tempString);
						System.out.println("Info has been sent to "
								+ neighbor_nodes_list.size() + " neighbors" + ":\n"
								+ simpleString);
					}
				}

				// delay of 15 seconds
				try {
					Thread.sleep(15000);
				} 
				catch (Exception e) 
				{
					e.printStackTrace();
				}
			} 
			catch (Exception e) 
			{
				e.printStackTrace();

			}
		}

	}

	private void ReadingDataFromHost(String fileName)
	{
		// Initializing variables
		FileInputStream inputStreamData_obj = null;
		InputStreamReader readerData_obj = null;
		BufferedReader buffered_reader_obj = null;
		try 
		{
			// Reading data from file path
			inputStreamData_obj = new FileInputStream(fileName);
			readerData_obj = new InputStreamReader(inputStreamData_obj);
			buffered_reader_obj = new BufferedReader(readerData_obj);
			String temporary_str;
			int countValue = 0;
			// Retrieving data till end of line and eliminating space and dat
			// extension
			while ((temporary_str = buffered_reader_obj.readLine()) != null)
			{
				String[] hostFileNameWithoutExtension = fileName.split("\\.dat");
				String[] hostFileWordsSplitted = temporary_str.split(" ");
				// Checking if the count of words is two
				if (hostFileWordsSplitted.length == 2) 
				{
					neighbor_nodes_list.add(hostFileWordsSplitted[0]);
					neighbor_pairs_map.put(
							hostFileNameWithoutExtension[0] + "-" + hostFileWordsSplitted[0], Double
									.parseDouble(hostFileWordsSplitted[1]));
				}
				countValue++;
			}
			// Eliminating .dat extension and spaces line by line
			inputStreamData_obj = new FileInputStream(fileName);
			readerData_obj = new InputStreamReader(inputStreamData_obj);
			buffered_reader_obj = new BufferedReader(readerData_obj);
			while ((temporary_str = buffered_reader_obj.readLine()) != null) 
			{
				String[] hostFileNameWithoutExtension = fileName.split("\\.dat");
				String[] hostFilewordsAfterSplitting = temporary_str.split(" ");
				Vector<DistanceVectorProtocolModelClass> LinebyLine = new Vector<DistanceVectorProtocolModelClass>();
				// Checking if number of words in a file is '2'
				if (hostFilewordsAfterSplitting.length == 2) 
				{
					Iterator<String> list_Iterator = neighbor_nodes_list.iterator();
					while (list_Iterator.hasNext()) 
					{
						String nextElementValue = list_Iterator.next();
						// add cost if the element is reachable through node
						if (nextElementValue.equals(hostFilewordsAfterSplitting[0])) 
						{
							LinebyLine.add(new DistanceVectorProtocolModelClass(hostFileNameWithoutExtension[0],
									nextElementValue, hostFilewordsAfterSplitting[0], Double
											.parseDouble(hostFilewordsAfterSplitting[1])));
						}
						// Else adding max cost i.e 99999
						else 
						{
							LinebyLine.add(new DistanceVectorProtocolModelClass(hostFileNameWithoutExtension[0],
									nextElementValue, hostFilewordsAfterSplitting[0], 99999));
						}
					}
					// Adding data line by line
					currentNetworkHostName.put(hostFileNameWithoutExtension[0] + "-"
							+ hostFilewordsAfterSplitting[0], LinebyLine);
				}
				// Printing number of neighbors corresponding to the node
				else if (hostFilewordsAfterSplitting.length == 1) 
				{
					neighbours_Count = Integer.parseInt(hostFilewordsAfterSplitting[0]);
					System.out.println("Neighbours Count is " + neighbours_Count);
				}
			}
		}
		// Throwing corresponding exceptions
		catch (FileNotFoundException f)
		{
			f.printStackTrace();
		} 
		catch (IOException io)
		{
			io.printStackTrace();
		}
		// Closing all the streams
		finally 
		{
			try
			{
				buffered_reader_obj.close();
				readerData_obj.close();
				inputStreamData_obj.close();
			}
			catch (IOException ioe)
			{
				ioe.printStackTrace();
			}
		}

	}

	private String routingTableCalculator(
			Hashtable<String, Vector<DistanceVectorProtocolModelClass>> distanceVectorList) 
	{
		Double maxValue = Double.MAX_VALUE;
		String resultString = "";
		// Traversing through the list of values
		for (Vector<DistanceVectorProtocolModelClass> dvIterator : distanceVectorList.values()) 
		{
			String sourceNodeValue = null;
			String throughNodeValue = null;
			String destinationNodeValue = null;
			double[] costsBetweenThem = new double[dvIterator.size()];
			for (int j = 0; j < dvIterator.size(); j++) 
			{
				costsBetweenThem[j] = dvIterator.get(j).getCostValue();
			}
			sourceNodeValue = dvIterator.get(minValueCalculator(costsBetweenThem)).getSourceNodeName();
			destinationNodeValue = dvIterator.get(minValueCalculator(costsBetweenThem)).getDestinationNodeName();
			throughNodeValue = dvIterator.get(minValueCalculator(costsBetweenThem)).getThroughNodeName();
			maxValue = dvIterator.get(minValueCalculator(costsBetweenThem)).getCostValue();
			// Concatenating result
			resultString += "shortest path from node " + sourceNodeValue + " to node "
					+ destinationNodeValue + ": the next hop is node " + throughNodeValue
					+ " and the cost is " + maxValue + "\n";
		}
		return resultString;
	}

	// for simple output purpose
	private String printResult(String inputNodeValue) 
	{
		String resultString = "";
		String[] linesSeparator = inputNodeValue.split("\\n");
		for (int i = 0; i < linesSeparator.length; i++) 
		{
			String[] arraySpaceSeparator = linesSeparator[i].split(" ");
			String[] startNodeValue = arraySpaceSeparator[4].split("\\.");
			String[] destinationNodeValue = arraySpaceSeparator[7].split("\\.");
			String[] throughNodeValue = arraySpaceSeparator[13].split("\\.");
			resultString += "Shortest Path from " + startNodeValue[0] + " to "
					+ destinationNodeValue[0] + " through " + throughNodeValue[0] + " "
					+ arraySpaceSeparator[18] + "\n";
		}
		return resultString;
	}

	private int minValueCalculator(double[] minimumValueObtained) 
	{
		double maxValue = Double.MAX_VALUE;
		for (int i = 0; i < minimumValueObtained.length; i++) 
		{
			if (maxValue > minimumValueObtained[i]) 
			{
				maxValue = minimumValueObtained[i];
			}
		}
		for (int i = 0; i < minimumValueObtained.length; i++) 
		{
			if (maxValue == minimumValueObtained[i]) 
			{
				return i;
			}
		}
		return -1;
	}
}