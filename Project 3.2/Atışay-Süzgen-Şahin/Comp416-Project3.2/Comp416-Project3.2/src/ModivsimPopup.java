import org.jdesktop.swingx.JXLabel;
import org.jdesktop.swingx.VerticalLayout;

import javax.swing.*;
import java.awt.*;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;

public class ModivsimPopup extends JFrame {
    private static StringBuilder popupText=new StringBuilder();
    private JFrame frame = new JFrame();
    private JPanel panel = new JPanel();

    public  ModivsimPopup(){

        frame = new JFrame("Output Window for Modified DVR Simulator");
        JScrollPane scrollBar=new JScrollPane(panel,JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

        frame.add(scrollBar);

        frame.setSize(400,200);

        panel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));


        //frame.add(panel);
        frame.setVisible(true);

        frame.show();


        pack();
    }


    public void clearModivsimPopup(){
        frame.getContentPane().removeAll();
        frame.repaint();

    }
    public void addComponentToModivSim(String str){

        frame.getContentPane().removeAll();
        frame.repaint();
        JScrollPane scrollBar=new JScrollPane(panel,JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);

        frame.add(scrollBar);

        JXLabel maComponent=new JXLabel(str);

        panel.add(maComponent);

        panel.setBorder(BorderFactory.createEmptyBorder(10,10,10,10));

        panel.setLayout(new VerticalLayout());

        //frame.add(panel);


        frame.setVisible(true);
        //frame.setLocationRelativeTo(null);
        frame.show();

        pack();

    }
}