import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

public class BSplineFrame extends JFrame {
    BSplineFrame() {
        this.setDefaultCloseOperation(EXIT_ON_CLOSE);
        this.setExtendedState(this.getExtendedState() | JFrame.MAXIMIZED_BOTH);
        this.setTitle("B-Spline demo");

        SmartCanvas smartCanvas = new SmartCanvas();
        smartCanvas.addGLEventListener(smartCanvas);
        smartCanvas.addMouseListener(smartCanvas);
        smartCanvas.addMouseMotionListener(smartCanvas);

        JLabel labelDegree = new JLabel("B-Spline's degree:");
        JSlider sliderDegree = new JSlider(1, 5, 3);
        sliderDegree.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                smartCanvas.setDegree(sliderDegree.getValue());
                smartCanvas.display();
            }
        });

        JLabel labelSmoothing = new JLabel("B-Spline's smoothing level:");
        JSlider sliderSmoothing = new JSlider(10, 1000, 50);
        sliderSmoothing.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                smartCanvas.setSmoothing(sliderSmoothing.getValue());
                smartCanvas.display();
            }
        });

        JPanel panelProps = new JPanel();
        panelProps.setLayout(new BoxLayout(panelProps, BoxLayout.Y_AXIS));
        panelProps.add(labelDegree);
        panelProps.add(sliderDegree);
        panelProps.add(labelSmoothing);
        panelProps.add(sliderSmoothing);

        JSplitPane splitPane = new JSplitPane();
        splitPane.setLeftComponent(panelProps);
        splitPane.setRightComponent(smartCanvas);

        this.add(splitPane);

        this.setVisible(true);
    }
}
