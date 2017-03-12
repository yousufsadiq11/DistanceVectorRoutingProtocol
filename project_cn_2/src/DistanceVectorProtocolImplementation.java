
import java.util.*;

public class DistanceVectorProtocolImplementation {

	public static void main(String[] args) throws Exception {
		// Initializing array list and hash tables to store the status of network and neighbors list
		Hashtable<String, Double> input_neighbors_of_node = new Hashtable<String, Double>();
		ArrayList<String> neighbors_list = new ArrayList<String>();
		Hashtable<String, Vector<DistanceVectorProtocolModelClass>> presentNetworkHost = new Hashtable<String, Vector<DistanceVectorProtocolModelClass>>();
		// initializing port numbers and file name
		int portNumber = 0;
		String fileName = null;
		try {
			// checking if input has sufficient parameters to run the program
			if (args.length != 2) {
				System.out.println("Input should include both port number and file path as parameters");
				System.out.println("Please enter in the following format");
				System.out.println("java filename.java <Port Number> <File path>");
				System.exit(-1);
			} else {
				// Parsing args[0] to integer value
				portNumber = Integer.parseInt(args[0]);
				// Condition to check whether input port number is not in the following range
				if (portNumber <= 1023) {
					System.out
							.println("Please specify the port numbers beyond the range 0 - 1023");
					System.exit(-1);
				}
				// Initializing file name variable with args[1]
				fileName = args[1];
			}

		} catch (Exception e) {
			System.out.println(e);
		}
		// Calling server and client start methods
		new Server(portNumber, fileName, input_neighbors_of_node, neighbors_list,
				presentNetworkHost).start();
		new Client(portNumber, fileName, input_neighbors_of_node, neighbors_list,
				presentNetworkHost).start();
	}
}