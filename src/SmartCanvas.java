
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.awt.GLCanvas;

import javax.swing.*;
import java.awt.*;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.ArrayList;

public class SmartCanvas extends GLCanvas implements MouseListener, MouseMotionListener, GLEventListener {
    private ArrayList<Point> points;
    private Point lastClickPoint = null;
    private ArrayList<Double> knots;
    private int degree = 3;
    private int smoothing = 50;

    private static final double THRESHOLD_DISTANCE = 32.0;

    SmartCanvas() {
        this.points = new ArrayList<>();
        this.lastClickPoint = null;
    }

    SmartCanvas(ArrayList<Point> points) {
        this.points = points;
        this.lastClickPoint = null;
    }

    public void setSmoothing(int smoothing) {
        this.smoothing = smoothing;
    }

    public void setDegree(int degree) {
        this.degree = degree;
    }

    private Point getNearestNodeInThresholdArea(Point searchPoint) {
        Point nearestPoint = getNearestNode(searchPoint);

        if (nearestPoint == null || searchPoint.distance(nearestPoint) > THRESHOLD_DISTANCE) {
            return null;
        }

        return nearestPoint;
    }

    private Point getNearestNode(Point searchPoint) {
        if (points.isEmpty()) {
            return null;
        }

        Point nearestPoint = points.get(0);
        for (Point point : points) {
            if (searchPoint.distance(point) < searchPoint.distance(nearestPoint)) {
                nearestPoint = point;
            }
        }

        return nearestPoint;
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        if (SwingUtilities.isLeftMouseButton(e)) {
            leftMouseButtonClicked(e);
        }

        if (SwingUtilities.isRightMouseButton(e)) {
            rightMouseButtonClicked(e);
        }
    }

    private void leftMouseButtonClicked(MouseEvent e) {
        createNode(e.getPoint());
    }

    private void createNode(Point point) {
        this.points.add(point);
        display();
    }

    private void rightMouseButtonClicked(MouseEvent e) {
        Point nearestPoint = getNearestNodeInThresholdArea(e.getPoint());

        if (nearestPoint != null) {
            removeNode(nearestPoint);
        }
    }

    private void removeNode(Point point) {
        this.points.remove(point);
        display();
    }

    @Override
    public void mousePressed(MouseEvent e) {
        this.lastClickPoint = e.getPoint();
    }

    @Override
    public void mouseReleased(MouseEvent e) {

    }

    @Override
    public void mouseDragged(MouseEvent e) {
        Point nearestPoint = getNearestNodeInThresholdArea(this.lastClickPoint);
        this.lastClickPoint = e.getPoint();

        if (nearestPoint != null) {
            this.points.set(this.points.indexOf(nearestPoint), e.getPoint());
            display();
        }
    }

    @Override
    public void init(GLAutoDrawable glAutoDrawable) {
        GL2 gl = glAutoDrawable.getGL().getGL2();
        gl.glClearColor(0.3f,0.3f,0.3f,1.0f);
        gl.glMatrixMode(GL2.GL_PROJECTION);
    }

    @Override
    public void display(GLAutoDrawable glAutoDrawable) {
        GL2 gl = glAutoDrawable.getGL().getGL2();
        gl.glClear(GL2.GL_COLOR_BUFFER_BIT | GL2.GL_DEPTH_BUFFER_BIT);
        drawNodes(gl);
        drawNodes2(gl);
        drawBSpline(gl);
    }

    private void drawNodes(GL2 gl) {
        gl.glLineWidth(4.0f);
        gl.glColor3d(0.4,0.4, 1.0);

        gl.glBegin(GL2.GL_LINE_STRIP);
        for (Point point : points) {
            gl.glVertex3d(point.x, point.y, -0.05);
        }

        gl.glEnd();
    }

    private void drawNodes2(GL2 gl) {
        gl.glColor3d(0.2,0.2, 1.0);
        gl.glPointSize(20.0f);

        gl.glBegin(GL2.GL_POINTS);

        for (Point point : points) {
            gl.glVertex2d(point.x, point.y);
        }

        gl.glEnd();
    }

    private ArrayList<Double> computeKnots(int n, int degree) {
        ArrayList<Double> knots = new ArrayList<>();

        for (int i = 0; i < degree; ++i) {
            knots.add(0.0);
        }

        for (int i = 0; i < n - degree + 1; ++i) {
            knots.add((double)i);
        }

        for (int i = 0; i < degree; ++i) {
            knots.add((double)n - degree);
        }

        return (knots);
    }

    private double N0(double t, int n) {
        if (knots.get(n) <= t && t < knots.get(n + 1)) {
            return 1.0;
        }

        return 0.0;
    }

    private double N(double t, int n, int degree) {
        if (degree == 0) {
            return N0(t, n);
        }

        double a = N(t, n, degree - 1);
        double b = N(t, n + 1, degree - 1);

        double c = 0.0;
        if (a != 0.0) {
            c = (t - knots.get(n)) / (knots.get(n + degree) - knots.get(n));
        }

        double d = 0.0;
        if (b != 0.0) {
            d = (knots.get(n + degree + 1) - t) / (knots.get(n + degree + 1) - knots.get(n + 1));
        }

        return (a * c + b * d);
    }

    private void drawBSpline(GL2 gl) {
        if (degree >= points.size()) {
            return;
        }

        gl.glBegin(GL2.GL_LINE_STRIP);

        gl.glColor3d(1.0, 1.0, 1.0);

        knots = computeKnots(points.size(), degree);
        double tMin = knots.get(0);
        double tMax = knots.get(knots.size() - 1);
        double tDiff = tMax - tMin;
        double tStep = tDiff / smoothing;

        for (double t = tMin; t <= tMax; t += tStep) {
            double x = 0.0;
            double y = 0.0;

            for (int i = 0; i < points.size(); ++i) {
                x += N(t, i, degree) * points.get(i).getX();
                y += N(t, i, degree) * points.get(i).getY();
            }

            gl.glVertex2d(x, y);
        }

        gl.glEnd();
    }

    @Override
    public void reshape(GLAutoDrawable glAutoDrawable, int x, int y, int w, int h) {
        GL2 gl = glAutoDrawable.getGL().getGL2();
        gl.glLoadIdentity();
        gl.glOrtho(0.0, getWidth(), getHeight(), 0.0, -1.0, 1.0);
    }


    @Override
    public void dispose(GLAutoDrawable glAutoDrawable) {

    }

    @Override
    public void mouseEntered(MouseEvent e) {
        // empty implementation
    }

    @Override
    public void mouseExited(MouseEvent e) {
        // empty implementation
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        // empty implementation
    }

}












