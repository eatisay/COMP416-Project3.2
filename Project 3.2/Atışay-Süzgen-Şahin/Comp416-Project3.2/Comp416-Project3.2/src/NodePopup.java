import org.jdesktop.swingx.HorizontalLayout;
import org.jdesktop.swingx.JXLabel;
import org.jdesktop.swingx.VerticalLayout;

import javax.swing.*;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Hashtable;

public class NodePopup extends JFrame {

    private Node n;
    private JFrame frame;
    private JPanel panel;

    public  NodePopup(Node n){
        this.n=n;

        frame = new JFrame("Output Window for Router #"+n.getNodeID());
        panel = new JPanel();

        frame.setSize(600,400);

        JLabel firstComponent = new JLabel("Current state for router "+n.getNodeID()+" at round "+ModivSim.getInstance().getRound());

        JXLabel secondComponent = new JXLabel(visualizeDistanceTable(n.getDistanceTable()));
        secondComponent.setLineWrap(true);


        JXLabel thirdComponent = new JXLabel(visualizeVectorDistance(n.getForwardingTable(),n.getDistanceVector()));
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

        pack();
    }

    public String visualizeDistanceTable(Hashtable<Integer, Hashtable<Integer, Integer>> distanceTable){
        StringBuilder totalString=new StringBuilder();

        totalString.append("Distance Table: "+ System.lineSeparator());


        StringBuilder firstRow=new StringBuilder("Dst|  \t");
        //buraya baslilklar gelecek

        ArrayList<Integer> keyList = Collections.list(distanceTable.keys());
        StringBuilder headerRow=new StringBuilder();
        for (int i=0;i<keyList.size();i++){
            headerRow.append(keyList.get(i) + "\t");

        }

        firstRow.append(headerRow);
        firstRow.append("\n");

        distanceTable.forEach((dest, via_costTable) -> {
            StringBuilder eachRow=new StringBuilder();
            if (dest != 0){



                eachRow.append("nbr "+dest + "\t");

                via_costTable.forEach((via, cost) -> {

                    eachRow.append(cost + "\t");


                });;

                firstRow.append(eachRow);
                firstRow.append("\n");

            }

        });;

        totalString.append(firstRow);

        return totalString.toString();


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

    public JFrame refresh(){
        frame.getContentPane().removeAll();
        frame.repaint();

        return this.frame;
    }


}
