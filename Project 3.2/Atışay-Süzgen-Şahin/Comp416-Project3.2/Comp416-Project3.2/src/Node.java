import org.jdesktop.swingx.JXLabel;
import org.jdesktop.swingx.VerticalLayout;

import javax.swing.*;
import java.util.*;
import java.util.Timer;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.IntStream;
import static java.util.Comparator.comparingInt;

/***
 * This class symbols the routers as nodes.
 * Class is implemented as thread to have better simulation of the autonomous behaviors.
 * Fields:
 * nodeID: Id of the node
 * linkCost: Costs of the links to the direct neighbors
 * linkBandwitdh: Holds the bandwidth
 * distanceTable: The calculated distances to the destinations in the topology via its neighbors
 * converged: Indicates whether there is updates anymore or not
 * neighbourIDList: IDs of the neighbor nodes
 * distanceVector: Structure that shows the shortest distance to the destinations in the topology
 * popup: Windows to be filled by the Node
 * linkBandwithOccupation: avaiblibility of the links to the direct neighbors
 * tasks: Packet flows assigned to the Node
 * waitingQueue: Packets that could not forwarded properly
 * dynamicLink: Shows if there is nay dynamic link
 * rndBool: Randomization criteria for the dynamic link
 * dynamiclinkTo: The predecessors of the dynamic links
 */
public class Node extends Thread{

    private int nodeID;
    private Hashtable<Integer, Integer> linkCost = new Hashtable<>();
    private Hashtable<Integer, Integer> linkBandwidth = new Hashtable<>();
    private Hashtable<Integer, Hashtable<Integer, Integer>> distanceTable = new Hashtable<>();
    private boolean converged;
    private ArrayList<Integer> neighbourIDList;
    private Hashtable<Integer, Integer> distanceVector;
    private NodePopup popup;
    private Hashtable<Integer, Boolean> linkBandwidthOccupation = new Hashtable<>(); //true if link is occupied
    private ArrayList<Packet> tasks;
    private Queue<Packet> waitingQueue= new LinkedList<Packet>();
    private boolean dynamicLink;
    private boolean rndBool;
    private ArrayList<Integer> dynamicLinkTo= new ArrayList<>();

    /***
     * Constructor of the Node. Where the explained fields above are assigned.
     * @param nodeID
     * @param linkCost
     * @param linkBandwidth
     */
    public Node(int nodeID, Hashtable<Integer, Integer> linkCost,  Hashtable<Integer, Integer> linkBandwidth) {
        this.nodeID=nodeID;
        this.linkCost=linkCost;
        this.linkBandwidth =linkBandwidth;
        this.neighbourIDList=new ArrayList<>();
        this.rndBool=false;
        this.dynamicLink=false;
        this.waitingQueue= new LinkedList<Packet>();
        tasks=new ArrayList<Packet>();
        this.distanceVector=new Hashtable<>();
        linkCost.forEach((mainKey, mainValue) -> {
            this.neighbourIDList.add(mainKey);
            linkBandwidthOccupation.put(mainKey,false);
        });
        int rndCost= new Random().nextInt(10)+1;
        linkCost.forEach((key,value)->{
            if (value==998){
                this.dynamicLink=true;
                this.dynamicLinkTo.add(key);
                linkCost.replace(key,rndCost);
                System.out.println(linkCost.get(key));
            }
        });
        Hashtable<Integer, Integer> linkCostCopy = linkCost;
        linkCostCopy.put(nodeID, 0);
        linkCostCopy.forEach((mainKey, mainValue) -> {
            Hashtable<Integer, Integer> tempRHS = new Hashtable<>();
            linkCostCopy.forEach((tempKey, tempValue) -> {
                if(tempKey== mainKey){
                    tempRHS.put(tempKey,tempValue);
                }else{
                    tempRHS.put(tempKey,999);
                }
            });
            distanceTable.put(mainKey,tempRHS);
        });
        this.converged=false;
        popup=new NodePopup(this);
    }

    /***
     * Main function of the thread. Routers are checks their tasks, whether there is any packet to be sent or not.
     */
    public void run(){
        while (true){
             if (!tasks.isEmpty()){
                 ModivSim.getInstance().fillPopup(this.nodeID + " has task");
                 if (waitingQueue.isEmpty()) System.out.println("I have task");
                flow(tasks.get(0));
            } else {
                 try {
                     Thread.sleep(0);
                 } catch (InterruptedException e) {
                     e.printStackTrace();
                 }
             }
        }
    }

    /***
     * From the distance table, the forwarding table is obtained. In order to do that, least costed paths are found from the distance table
     * for each destination.
     * @return destination and two following best alternative hop nodes are shown in the structure.
     */
    public Hashtable<Integer, int[]> getForwardingTable(){
        Hashtable<Integer, int[]> forwardingTable = new Hashtable<>();
        distanceTable.forEach((keyAsID, valueAsTable) -> {
            int[] tempEntry = new int[2];
            List<Integer> keyList = Collections.list(valueAsTable.keys());
            List<Integer> valueList = Collections.list(valueAsTable.elements());
            for (int i=0;i < keyList.size();i++){
                if (valueList.get(i)==999){
                    keyList.remove(i);
                    valueList.remove(i);
                }
            }
            int firstMinIndex = IntStream.range(0,valueList.size()).boxed()
                    .min(comparingInt(valueList::get))
                    .get();
            tempEntry[0]=keyList.get(firstMinIndex);
            keyList.remove(firstMinIndex);
            valueList.remove(firstMinIndex);
            if(keyList.size() > 0){
                int secondMinIndex = IntStream.range(0,valueList.size()).boxed()
                        .min(comparingInt(valueList::get))
                        .get();
                tempEntry[1]=keyList.get(secondMinIndex);
            }
            forwardingTable.put(keyAsID,tempEntry);
        });
        return forwardingTable;
    }

    /***
     * The message containing the distance vector of the neighbor is analyzed in thar function.
     * In addition, the obtained destination that is not familiar with this node is also added to the possible destinations.
     * The obtained distance vector is put to the distance table's appropriate index.
     * Then, the  Dx = [Dx(y): y є N ] is implemented.
     * After that process, old version and modified version is comprised. If they are not equal, the update is signalled.
     * @param m
     */
    public synchronized void receiveUpdate(Message m){

        ModivSim.getInstance().fillPopup(this.nodeID+" has received message from " + m.getSenderID());
        AtomicBoolean b= new AtomicBoolean(false);
        int viaNode= m.getSenderID();
        Hashtable<Integer, Hashtable<Integer, Integer>> olddistanceTable = distanceTable;
        Hashtable<Integer, Integer> distVector=m.getDistanceVectorEstimates();
        Hashtable<Integer, Integer> tmpSet=new Hashtable<Integer, Integer>();
        tmpSet.put(nodeID,999);
        for (Integer nID:this.neighbourIDList){
            tmpSet.put(nID,999);
        }
        distVector.forEach((dst,cst)->{
            if (!distanceTable.keySet().contains(dst)){
                distanceTable.put(dst,tmpSet);
            }
        });
        int directLinkCost=distanceTable.get(viaNode).get(viaNode);
        distanceTable.forEach((dest, via_costTable) -> {
            via_costTable.forEach((via, cost) ->
            {
                if (via.equals(viaNode)){
                    if(distVector.containsKey(dest)){
                        int tmp=directLinkCost+distVector.get(dest);
                        if (tmp<cost) {
                            distanceTable.get(dest).replace(via,tmp);
                            b.set(true);
                        }
                    }
                }
            }
            );
        });
        this.distTableToString();
        if (b.get()) ModivSim.getInstance().fillPopup(this.nodeID+" has updated its distance table");
    }

    /***
     * This function holds the responsiblity for the distribution of the information.
     * First, it checks for the dynamic links and re-assigns if needed.
     * After taking the neighbor nodes from the ModivSim, it recalculates its distance vector and make comparison of it with the
     * previous distance table from the global field. If there is any change, the distanceVector fields is updated and
     * for every neighbor, the message containing the source, neighborID, updated distance vector and bandwitdh created and sent.
     * Followingly, every direct neighbors receiveUpdate functions is called for the message created and returns true.
     * If no change is observed, it returns false.
     * @return boolean indicates whether there is update or not.
     */
    public synchronized boolean sendUpdate(){
        if (dynamicLink){
            this.rndBool= new Random().nextBoolean();
            if (rndBool){
                int rndCost= new Random().nextInt(10)+1;
                for(Integer i: dynamicLinkTo){
                    distanceTable.get(i).replace(i,rndCost);
                }
            }
        }
        ArrayList<Node> NeighbourList;
        NeighbourList= ModivSim.getInstance().returnNeighbor(neighbourIDList);
        /*
        for (Node neighbour: NeighbourList){
            Message tempMessage=new Message(this.getDistanceVector(),linkBandwith,nodeID,neighbour.nodeID);
            neighbour.receiveUpdate(tempMessage);
        }
         */
        Hashtable<Integer, Integer> distVector = this.getDistanceVector();
        if (this.distanceVector.equals(distVector)) converged=true;
        else this.distanceVector=distVector;
        if(!converged){
            for (Node neighbour: NeighbourList){
                Message tempMessage=new Message(distVector, linkBandwidth,nodeID,neighbour.nodeID);
                ModivSim.getInstance().fillPopup(this.nodeID+" has sent message to " + tempMessage.getReceiverID());
                ModivSim.getInstance().fillPopup(" Content of the message is the new distance vector of " + this.nodeID + ": " + distVector.toString());
                neighbour.receiveUpdate(tempMessage);
            }
            return true;
        }else{
            return false;
        }
    }

    /***
     * Task, flow, is obtained from this function.
     * Taks are updated.
     * @param pack
     */
    public synchronized void getTask(Packet pack){
        tasks.add(pack);
    }

    /***
     * The main function that coordinates the packet flow.
     * First, after the proper prompt functons, it checks for the queue and updates the traceroute according to it.
     * Then, the flow part is split in half.
     * If the destination node is the current node, it calculates the time of the transmission, traceroute and prompts them.
     * Followingly, sets a Timer to timeOccupied to release the occupied links and prohibit the deadlock. It also removes the packet from the tasks.
     * If the current node is not the destination node, it controls its forwarding table. For the next hops, it checks whether the links
     * are avaible. If an avaible node is found, packet is assigned to next hop by calling the getTask function and according link signalled as
     * occupied. It also cehcks the bottleneck bandwidth of the packet and update it if the given link is narrower.
     * If on alternative is found, packet is added to the waitingQueue.
     * @param pack
     */
    public synchronized void flow(Packet pack){
        if (!waitingQueue.isEmpty()) {
            ModivSim.getInstance().fillPopup(waitingQueue.peek().getPacketName()+ " is in the queue in router " + this.nodeID);
            System.out.println(waitingQueue.peek().getPacketName()+ " is in the queue");
        }
            int srcID=pack.getFlowInfo()[0];
            int destID=pack.getFlowInfo()[1];
            int bndw=pack.getFlowInfo()[3];
            if (!waitingQueue.contains(pack)) pack.appendFlowRoute(this.nodeID);
            if (destID==this.nodeID){
                ModivSim.getInstance().fillPopup("Packet "+ pack.getPacketName() + "is arrived to the destination "+ this.nodeID);
                System.out.println("Packet arrived!");
                int timeOccupied=pack.getFlowInfo()[2]/pack.getFlowInfo()[3];
                ModivSim.getInstance().fillPopup("Packet "+ pack.getPacketName() + " took "+ timeOccupied+" seconds to arrive");
                System.out.println("It took "+ timeOccupied+" seconds to arrive");
                ModivSim.getInstance().fillPopup("Packet "+ pack.getPacketName()+ " through the route of "+ pack.getFlowRoute());
                System.out.println("!Packet "+ pack.getPacketName()+ " through the route of "+ pack.getFlowRoute());
                tasks.remove(pack);
                Timer t= new Timer();
                t.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        ArrayList<Integer> flowRoute=pack.getFlowRoute();
                        for(Integer nodeID: flowRoute){
                            int routePlace=flowRoute.indexOf(nodeID);
                            Node n=ModivSim.getInstance().getNodeList().get(nodeID);
                            if (routePlace==(flowRoute.size()-1)){
                                n.linkBandwidthOccupation.replace(flowRoute.get(routePlace-1),false);
                            }else{
                                ModivSim.getInstance().fillPopup("Link between " + n.nodeID + " and " + flowRoute.get(routePlace+1)+ "is not occupied now");
                                System.out.println("Links are not occupied now");
                                n.linkBandwidthOccupation.replace(flowRoute.get(routePlace+1),false);
                                if(routePlace!=0) n.linkBandwidthOccupation.replace(flowRoute.get(routePlace-1),false);
                            }
                        }
                    }
                },timeOccupied*1000);
            }else{
                int[] possibleHops;
                possibleHops=getForwardingTable().get(destID);
                for(int hops : possibleHops){
                    if(!linkBandwidthOccupation.get(hops)){
                        Node cur=ModivSim.getInstance().getNodeList().get(hops);
                        if(bndw>this.linkBandwidth.get(cur.nodeID)) pack.getFlowInfo()[3]=this.linkBandwidth.get(cur.nodeID);
                        cur.getTask(pack);
                        tasks.remove(pack);
                        linkBandwidthOccupation.replace(cur.nodeID,true);
                        if (waitingQueue.contains(pack)) waitingQueue.remove();
                        ModivSim.getInstance().fillPopup("Packet "+ pack.getPacketName()+ ": "+nodeID+ " -> "+ cur.nodeID);
                        System.out.println("Packet "+ pack.getPacketName()+ ": "+nodeID+ " -> "+ cur.nodeID);
                        break;
                    }
                    waitingQueue.add(pack);
                }
            }
    }
    //dest, cost
    public Hashtable<Integer, Integer> getDistanceVector(){
        Hashtable<Integer, Integer> distVector=new Hashtable<Integer, Integer>();
        for (Integer dest: this.distanceTable.keySet()){
            Hashtable<Integer, Integer> via_cost;
            via_cost=distanceTable.get(dest);
            int minK= Integer.MAX_VALUE;
            int minV= 0;
            //Map.Entry<Object, Integer> -->
            for(Map.Entry<Integer, Integer> x: via_cost.entrySet()){
                if(x.getValue() < minK){
                    minK= x.getValue(); // this is minumum cost
                    minV= x.getKey(); // this is minumum costed via-node
                }
            }
            distVector.put(dest,minK);
        }
        return distVector;
    }

    /**
     * Getter for the nodeID
     * @return nodeID
     */
    public int getNodeID() {
        return nodeID;
    }

    /***
     * Getter for the convergence
     * @return converged
     */
    public boolean isConverged() {
        return converged; }

    /***
     * ToString function for prompting the distance table in plain way.
     */
    public void distTableToString(){
        distanceTable.forEach((dest, via_costTable) -> {
            via_costTable.forEach((via, cost) ->
                    {
                       System.out.println("From "+ nodeID +", To "+ dest + ", via " + via +", cost is " +cost);
                    }
            );
        });;
    }

    /***
     * Getter for the distanceTable
     * @return distanceTable
     */
    public Hashtable<Integer, Hashtable<Integer, Integer>> getDistanceTable() {
        return distanceTable;
    }

    //Following functions are utility functions that is used to fill the UI, the NodePopup
    public void refreshPopup(){
        JFrame frame=this.popup.refresh();

        JPanel panel = new JPanel();

        frame.setSize(600,400);

        JLabel firstComponent = new JLabel("Current state for router "+getNodeID()+" at round "+ModivSim.getInstance().getRound());

        JXLabel secondComponent = new JXLabel(visualizeDistanceTable(getDistanceTable()));
        secondComponent.setLineWrap(true);


        JXLabel thirdComponent = new JXLabel(visualizeVectorDistance(getForwardingTable(),getDistanceVector()));
        thirdComponent.setLineWrap(true);


        panel.add(firstComponent);
        panel.add(Box.createVerticalStrut(50));
        panel.add(secondComponent);
        panel.add(Box.createVerticalStrut(50));
        panel.add(thirdComponent);

        panel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        panel.setLayout(new VerticalLayout());

        frame.add(panel);


        frame.setVisible(true);
        frame.setLocationRelativeTo(null);
        frame.show();



    }
    public String visualizeDistanceTable(Hashtable<Integer, Hashtable<Integer, Integer>> distanceTable){
        StringBuilder totalString=new StringBuilder();

        totalString.append("Distance Table: "+ System.lineSeparator());


        StringBuilder firstRow=new StringBuilder("Dst|  \t");
        ArrayList<Integer> keyList = Collections.list(distanceTable.keys());
        StringBuilder headerRow=new StringBuilder();
        for (int i=0;i<keyList.size();i++){
            headerRow.append(keyList.get(i) + "\t");

        }

        firstRow.append(headerRow);
        firstRow.append("\n");

//-----------------------------------------------------------------------------------------------------

        ArrayList<Hashtable<Integer,Integer>> firstList = Collections.list(distanceTable.elements());
        Hashtable<Integer,Integer> deneme=firstList.get(0);
        List<Integer> smallList=Collections.list(deneme.keys());


        for (Integer keyIndex : smallList){

            StringBuilder rowWise=new StringBuilder("nbr"+keyIndex+ "\t");

            for (int i=0;i<firstList.size();i++){

                Hashtable<Integer,Integer> temptable=firstList.get(i);
                rowWise.append(temptable.get(keyIndex)+"\t");
            }
            firstRow.append(rowWise+"\n");

        }
        return firstRow.toString();
    }
    public String visualizeVectorDistance(Hashtable<Integer, int[]> forwardingTable, Hashtable<Integer, Integer> distanceVector){

        StringBuilder totalString=new StringBuilder();

        totalString.append("Distance Vector: "+ System.lineSeparator());

        StringBuilder firstRow=new StringBuilder("Dst|  \t");
        StringBuilder headerRow=new StringBuilder();
        distanceVector.forEach((via, cost) -> {

            headerRow.append(via + "\t");
        });;
        firstRow.append(headerRow);
        firstRow.append("\n");
        totalString.append(firstRow);

        StringBuilder secondRow=new StringBuilder("cost \t");

        distanceVector.forEach((via, cost) -> {

            secondRow.append(cost + "\t");
        });;

        secondRow.append("\n");
        totalString.append(secondRow);

        StringBuilder thirdRow=new StringBuilder("route \t");

        forwardingTable.forEach((via, route) -> {

            thirdRow.append(route[0] + "\t");
        });;

        thirdRow.append("\n");
        totalString.append(thirdRow);


        return totalString.toString();
    }
}
