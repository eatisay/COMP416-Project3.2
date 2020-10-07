import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

/***
 * Main class hold the responsibilities of the simulation.
 * Main fields:
 * period: An integer indicating the duration of the periods of the simulation
 * nodeList: List of nodes taken by the input topology
 * flowS: Flows that will sent through by the flows.txt
 * msPP: Popup window to be filled
 * packSent: Boolean indicates whether the packets are sent to routers or not
 * round: Number of rounds in the simulation
 * sysConverged: Boolean indicates whether the system is converged or not
 */
public class ModivSim {

    int period;
    private static ModivSim single_instance=null;
    private List<Node> nodeList = new ArrayList<>();
    private LinkedHashMap<String, int[]> flowS;
    private ModivsimPopup msPP=new ModivsimPopup();
    public boolean packSent;
    public boolean sentA;
    public boolean sentB;
    public boolean sentC;
    public int round;
    public boolean sysConverged;
    private ModivSim()
    {
    }
    private ModivSim(int period)
    {
        this.period=period;
    }

    /***
     * Function to obtain the instance
     * @return
     */
    public static ModivSim getInstance() {
        if (single_instance == null) {
            single_instance = new ModivSim();
        }
        return single_instance;
    }
    public static ModivSim getInstance(int period) {
        if (single_instance == null) {
            single_instance = new ModivSim(period);
        }
        return single_instance;
    }

    /***
     * Function that searches for the direct neighbor nodes of the given node.
     * It takes to IDs of neighbors as list from the given node and searches for those IDs in the nodeList.
     * @param nIDs, Id of the node that will be queried
     * @return list of neighbor nodes of the given ID
     */
    public ArrayList<Node> returnNeighbor(ArrayList<Integer> nIDs){
        ArrayList<Node> nList = new ArrayList<>();
        for (Integer ID: nIDs){
            for (Node n: nodeList){
                if(ID==n.getNodeID()) nList.add(n);
            }
        }
        return nList;
    }

    /***
     * Function to fill the popup.
     * @param str String that is wanted to be prompted.
     */
    public void fillPopup(String str){
        this.msPP.addComponentToModivSim(str);
    }

    /***
     * Getter for the round of the simulation.
     * @return round
     */

    public int getRound() {
        return round;
    }

    /***
     * Function to check the durability of the nodes.
     * Basically, it takes the nodes' covergence situations and have and operation on them.
     * @return check, the current situation by means of convergence
     */
    private boolean convergenceCheck() {
        boolean check=true;
        for (Node n: nodeList){
            boolean tmpB=n.isConverged();
            check=check && tmpB;
        }
        return check;
    }

    /***
     * Getter for the nodeList
     * @return nodeList
     */
    public List<Node> getNodeList() {
        return nodeList;
    }

    /***
     * Analog demo of the flows in the flow.txt to see if the system works.
     * It is a dummy function that is not using the thread functions of the nodes.
     * @param flowname
     */
    private void demoFlow(String flowname){
        int[] flow= flowS.get(flowname);
        ArrayList<Integer> traceRoute= new ArrayList<>();
        int srcNode=flow[0];
        int dstNode=flow[1];
        int flowSize=flow[2];
        int cost=0;
        ArrayList<Integer> bandwith= new ArrayList<>();
        int currentNode= srcNode;
        traceRoute.add(currentNode);
        while(!(currentNode==dstNode)){
            Node curNodeN= nodeList.get(currentNode);
            int[] possibleHops;
            possibleHops=curNodeN.getForwardingTable().get(dstNode);
            for(int hops : possibleHops){
                currentNode=hops;
                traceRoute.add(currentNode);
                break;
            }
        }
        for (int i: traceRoute){
            System.out.println(i+"->");
        }
    }

    /***
     * Simulation takes place in this function.
     * It reads the input.txt and parses the given information. Followingly to that parsing operation, instantiates the nodes defined.
     * It reads the flow.txt and holds them in the flowS.
     * After the inputting process, it starts every node thread and initialized the fields such as round, packSent, sysConverged.
     * Sets timer to have to simulation in periodic manner.
     * In every period, if the system is ont converged, nodes in the nodeList called for their update function and round is incremented.
     * If the system is converged, flows are routed. In that process, pack from the flows are parsed and null list of traceroute instantiated as
     * Packet class.
     * In the very end of the periods, systems again checked for the convergence.
     * @throws FileNotFoundException
     */
    public void run() throws FileNotFoundException {
        String currentDirectory = System.getProperty("user.dir") + "\\src\\input";
        System.out.println(currentDirectory);
        this.fillPopup(currentDirectory);
        File[] files = new File(currentDirectory).listFiles();
        for (File inputFile : files) {
            if (inputFile.isDirectory()) {
                System.out.println("Illegal input");
            }
            else if (inputFile.getName().equals("input.txt")){
                List<String> listOfInputs = new ArrayList<>();
                try (BufferedReader br = new BufferedReader(new FileReader(inputFile))) {
                    listOfInputs = br.lines().collect(Collectors.toList());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                for (String input: listOfInputs){
                    System.out.println(input);
                    String[] inputAsArray=input.split(",\\(");
                    String nodeID=inputAsArray[0];
                    Hashtable<Integer,Integer> linkCost = new Hashtable<Integer,Integer>();
                    Hashtable<Integer,Integer> linkBandwidth = new Hashtable<Integer,Integer>();
                    int index=0;
                    for (String str : inputAsArray){
                        if (index>0){
                            str=str.replace(")", "");
                            String[] nodeN= str.split(",");
                            System.out.println(str);
                            if(nodeN[1].equals("x")){
                                linkCost.put(Integer.parseInt(nodeN[0]),998); //998 denotes dynamic
                                System.out.println(nodeN[1]);
                            }else{
                                linkCost.put(Integer.parseInt(nodeN[0]),Integer.parseInt(nodeN[1]));
                            }
                            linkBandwidth.put(Integer.parseInt(nodeN[0]),Integer.parseInt(nodeN[2]));
                        }
                        index++;
                    }
                    index=0;
                    Node x= new Node(Integer.parseInt(nodeID), linkCost,linkBandwidth);
                    this.nodeList.add(x);
                }
            }
            else if (inputFile.getName().equals("flow.txt")){
                flowS= new LinkedHashMap<>();
                List<String> listOfInputs = new ArrayList<>();
                try (BufferedReader br = new BufferedReader(new FileReader(inputFile))) {
                    listOfInputs = br.lines().collect(Collectors.toList());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                for (String input: listOfInputs){
                    System.out.println(input);
                    String[] inputAsArray=input.split(", ");
                    String flowName=inputAsArray[0];
                    int srcNode=Integer.parseInt(inputAsArray[1]);
                    int dstNode=Integer.parseInt(inputAsArray[2]);
                    int flowSize=Integer.parseInt(inputAsArray[3]);
                    int[] flowDetails= new int[]{srcNode,dstNode,flowSize,999}; //999 means it is the first observer of the packet
                    flowS.put(flowName,flowDetails);

                }
                flowS.keySet();
            }
        }
        nodeList.forEach(node -> {
            node.start();
        });
        round=0;
        packSent=false;
        sentA=false;
        sentB=false;
        sentC=false;
        final int[] packIndex = {0};
        System.out.println(round);
        sysConverged=false;
        Timer t = new Timer();
        t.schedule(new TimerTask() {
            @Override
            public void run() {
                if (!sysConverged){
                    fillPopup(round+ "");
                    System.out.println(round);
                    for (Node node:nodeList){
                        node.sendUpdate();
                        node.refreshPopup();
                    }
                    round++;
                }

                else
                    {
                    System.out.println("System is converged at round: "+ round);
                        if (!packSent){
                            if(packIndex[0] <flowS.size()){
                                int i=0;
                                for(Map.Entry<String, int[]> x: flowS.entrySet()){
                                    if (i== packIndex[0]){
                                        String key = x.getKey();
                                        int[] value = x.getValue();
                                        String packetName= key;
                                        int[] pack=value;
                                        ArrayList<Integer> flowRoute=new ArrayList<>();
                                        System.out.println("Sent to " + nodeList.get(pack[0]).getNodeID());
                                        System.out.println("Package " + key + " is sent to " + nodeList.get(pack[0]).getNodeID());
                                        fillPopup("Package " + key + " is sent to " + nodeList.get(pack[0]).getNodeID());
                                        Packet p=new Packet(packetName,pack,flowRoute);
                                        nodeList.get(pack[0]).getTask(p);
                                        packIndex[0]++;
                                        break;
                                    }
                                    i++;
                                }
                            }
                        }
                }
                sysConverged=convergenceCheck();
            }
        }, 0, period*1000); // 5 seconds
    }


}
