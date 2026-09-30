package flag;

import com.jogamp.opengl.GL;
import com.jogamp.opengl.GL2;
import com.jogamp.opengl.GLAutoDrawable;
import com.jogamp.opengl.GLCapabilities;
import com.jogamp.opengl.GLEventListener;
import com.jogamp.opengl.GLProfile;
import com.jogamp.opengl.awt.GLCanvas;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import java.awt.Dimension;

/**
 * Bandera de Sudáfrica (proporción 3:2) dibujada con JOGL (OpenGL fijo).
 * Sistema de coordenadas: 900 x 600 unidades, origen abajo-izquierda.
 */
public class BanderaSudafrica implements GLEventListener {

    static final double W = 900, H = 600;
    static final double XJ = 330;        // x donde se une la "Y" con la franja horizontal
    static final double VERDE_H = 60;    // semiancho de la banda verde (1/5 de la altura en total)
    static final double BLANCO_H = 100;  // semiancho verde + bordes blancos (1/3 de la altura)
    static final double BORDE_AMARILLO = 40;

    // Colores oficiales
    static final float[] ROJO     = {224/255f, 60/255f, 49/255f};
    static final float[] AZUL     = {0/255f, 20/255f, 137/255f};
    static final float[] VERDE    = {0/255f, 119/255f, 73/255f};
    static final float[] AMARILLO = {255/255f, 184/255f, 28/255f};
    static final float[] BLANCO   = {1f, 1f, 1f};
    static final float[] NEGRO    = {0f, 0f, 0f};

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            GLProfile perfil = GLProfile.get(GLProfile.GL2);
            GLCapabilities caps = new GLCapabilities(perfil);
            caps.setSampleBuffers(true);
            caps.setNumSamples(4);
            GLCanvas canvas = new GLCanvas(caps);
            canvas.addGLEventListener(new BanderaSudafrica());
            canvas.setPreferredSize(new Dimension(900, 600));

            JFrame ventana = new JFrame("Bandera de Sudáfrica - JOGL");
            ventana.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            ventana.getContentPane().add(canvas);
            ventana.pack();
            ventana.setLocationRelativeTo(null);
            ventana.setVisible(true);
        });
    }

    @Override
    public void init(GLAutoDrawable d) {
        GL2 gl = d.getGL().getGL2();
        gl.glClearColor(0.1f, 0.1f, 0.1f, 1f);
        gl.glEnable(GL.GL_MULTISAMPLE);
    }

    @Override
    public void reshape(GLAutoDrawable d, int x, int y, int w, int h) {
        GL2 gl = d.getGL().getGL2();
        // Mantiene la proporción 3:2 (con franjas oscuras si la ventana cambia)
        double escala = Math.min(w / W, h / H);
        int vw = (int) (W * escala), vh = (int) (H * escala);
        gl.glViewport((w - vw) / 2, (h - vh) / 2, vw, vh);
        gl.glMatrixMode(GL2.GL_PROJECTION);
        gl.glLoadIdentity();
        gl.glOrtho(0, W, 0, H, -1, 1);
        gl.glMatrixMode(GL2.GL_MODELVIEW);
        gl.glLoadIdentity();
    }

    @Override
    public void display(GLAutoDrawable d) {
        GL2 gl = d.getGL().getGL2();
        gl.glClear(GL.GL_COLOR_BUFFER_BIT);

        // 1) Fondo: rojo arriba, azul abajo
        poligono(gl, ROJO, 0, H / 2, W, H / 2, W, H, 0, H);
        poligono(gl, AZUL, 0, 0, W, 0, W, H / 2, 0, H / 2);

        // 2) "Y" blanca (borde) y "Y" verde encima
        dibujarY(gl, BLANCO, BLANCO_H);
        dibujarY(gl, VERDE, VERDE_H);

        // 3) Triángulo amarillo (borde) y triángulo negro
        double[] borde = bordeInterior(VERDE_H);   // {x del vértice, y en x=0}
        double xm = borde[0], yTop = borde[1];
        poligono(gl, AMARILLO, 0, yTop, xm, H / 2, 0, H - yTop);

        // Borde interior: desplazar el lado superior hacia adentro
        double dx = xm, dy = H / 2 - yTop, len = Math.hypot(dx, dy);
        dx /= len; dy /= len;
        double nx = dy, ny = -dx;                  // normal hacia el interior
        double ax = BORDE_AMARILLO * nx, ay = yTop + BORDE_AMARILLO * ny;
        double yb = ay + (-ax / dx) * dy;          // y en x = 0
        double xb = ax + ((H / 2 - ay) / dy) * dx; // x en y = H/2
        poligono(gl, NEGRO, 0, yb, xb, H / 2, 0, H - yb);

        gl.glFlush();
    }

    /** Dibuja la "Y" horizontal-tumbada con semiancho h (perpendicular al eje). */
    private void dibujarY(GL2 gl, float[] color, double h) {
        double L = Math.hypot(XJ, H / 2);
        double ux = XJ / L, uy = -(H / 2) / L;   // dirección del brazo superior
        double nx = (H / 2) / L, ny = XJ / L;    // normal (lado exterior)
        double c0y = H;                          // el brazo parte de la esquina (0, H)

        // Borde exterior corta la línea y = H/2 + h
        double ox = h * nx, oy = c0y + h * ny;
        double t1 = (H / 2 + h - oy) / uy;
        double p1x = ox + t1 * ux, p1y = H / 2 + h;

        // Borde interior corta el eje y = H/2
        double ix = -h * nx, iy = c0y - h * ny;
        double t2 = (H / 2 - iy) / uy;
        double mx = ix + t2 * ux, my = H / 2;

        double k = 200; // prolongación fuera de la bandera (se recorta con el viewport)
        double sox = ox - k * ux, soy = oy - k * uy;
        double six = ix - k * ux, siy = iy - k * uy;

        // Brazo superior
        poligono(gl, color, sox, soy, p1x, p1y, mx, my, six, siy);
        // Brazo inferior (simétrico)
        poligono(gl, color, sox, H - soy, p1x, H - p1y, mx, my, six, H - siy);
        // Cuña central
        poligono(gl, color, p1x, p1y, mx, my, p1x, H - p1y);
        // Franja horizontal hasta el borde derecho
        poligono(gl, color, p1x, H / 2 - h, W + 50, H / 2 - h, W + 50, H / 2 + h, p1x, H / 2 + h);
    }

    /** Devuelve {x del vértice donde se juntan los bordes interiores, y del borde interior en x=0}. */
    private double[] bordeInterior(double h) {
        double L = Math.hypot(XJ, H / 2);
        double ux = XJ / L, uy = -(H / 2) / L;
        double nx = (H / 2) / L, ny = XJ / L;
        double ix = -h * nx, iy = H - h * ny;
        double tm = (H / 2 - iy) / uy;
        double xm = ix + tm * ux;
        double t0 = (0 - ix) / ux;
        double y0 = iy + t0 * uy;
        return new double[]{xm, y0};
    }

    /** Polígono convexo con color sólido. pts = x1,y1,x2,y2,... */
    private void poligono(GL2 gl, float[] c, double... pts) {
        gl.glColor3f(c[0], c[1], c[2]);
        gl.glBegin(GL2.GL_POLYGON);
        for (int i = 0; i < pts.length; i += 2) {
            gl.glVertex2d(pts[i], pts[i + 1]);
        }
        gl.glEnd();
    }

    @Override
    public void dispose(GLAutoDrawable d) { }
}
