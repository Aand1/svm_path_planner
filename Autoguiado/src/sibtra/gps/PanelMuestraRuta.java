package sibtra.gps;

import java.awt.BasicStroke;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.geom.GeneralPath;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;

/**
 * Clase amiga de {@link MiraObstaculo} que muestar graficamente resultado de sus cálculos.
 * Ponemos eje X vertical hacia arriba (Norte) y eje Y horizontal a la izda (Oeste)
 * @author alberto
 */
public class PanelMuestraRuta extends JPanel implements MouseListener, GpsEventListener, ActionListener {
	
	/** Tamaño (en pixeles) de los ejes a pintar en el panel */
	protected static final int TamEjes = 50;
	
	/** Panel de la gráfica */
	JPanel JPanelGrafico;
	
	/**
	 * Coordenadas de la esquina superior izquierda.
	 * En unidades mundo real.
	 */
	private Point2D esqSI;
	
	/**
	 * Coordenadas de la esquina superior izquierda.
	 * En unidades mundo real.
	 */
	private Point2D esqID;

	/** Banderín para indicar que hay que recalcular las esquinas */
	private boolean restaurar;

	/** Evento cuando se pulsó el ratón, se necesita para hacer los cálculos al soltar y hacer el zoom */
	private MouseEvent evenPulsa;
	/** Evento cuando se pulsó el ratón con el SHIFT, establece la posición deseada */
	private MouseEvent evenPos;


	/** Si ya hay datos que representar (posición y barrido) */
	private boolean hayBarrido;

	/** Largo del coche en metros */
	protected double largoCoche=2;
	/** ancho del coche en metros */
	protected double anchoCoche = 1;

	private Ruta RU;

	private JComboBox jcbEscalas;
	String[] escalasS={ "0.5 m","1 m","2 m", "5 m","10 m","20 m","50 m", "100 m", "500 m" };
	double[] escalasD={  0.5,    1,    2   ,  5   , 10   , 20   , 50   ,  100   ,  500    };  

	private boolean escalado;


	/** Número de pixeles en pantalla que representa la escala seleccionada */
	private static final int pixelEscala=100;
	
	/**
	 * Convierte punto en el mundo real a punto en la pantalla. 
	 * EN VERTICAL EL EJE X hacia la izquierda el eje Y. 
	 * @param pt punto del mundo real
	 * @return punto en pantalla
	 */
	private Point2D.Double point2Pixel(Point2D pt) {
		return point2Pixel(pt.getX(), pt.getY()); 
	}

	
	/**
	 * Convierte punto en el mundo real a punto en la pantalla.
	 *  EJE X vertical hacia arriba, Eje Y horizontal hacia la izquierda
	 * @param x coordenada X del punto
	 * @param y coordenada Y del punto
	 * @return punto en pantalla
	 */
	private Point2D.Double point2Pixel(double x, double y) {
		return new Point2D.Double(
				(esqSI.getY()-y)*JPanelGrafico.getWidth()/(esqSI.getY()-esqID.getY())
				,
				JPanelGrafico.getHeight()-((x-esqID.getX())*JPanelGrafico.getHeight()/(esqSI.getX()-esqID.getX()))
				);
	}

	/** @see #point2Pixel(double, double) */
	private Point2D point2Pixel(double[] ds) {
		return point2Pixel(ds[0], ds[1]);
	}

	/** @see #point2Pixel(double, double) */
	private Point2D.Double pixel2Point(Point2D px) {
		return pixel2Point(px.getX(), px.getY());
	}
	
	
	/**	
	 * Dado pixel de la pantalla determina a que punto del mundo real corresponde. 
	 * @param x coordenada x del pixel
	 * @param y coordenada y del pixel
	 * @return punto en el mundo real
	 */
    private Point2D.Double pixel2Point(double x, double y) {
		return new Point2D.Double(
				(JPanelGrafico.getHeight()-y)*(esqSI.getX()-esqID.getX())/JPanelGrafico.getHeight() + esqID.getX()
				,
				esqSI.getY()-x*(esqSI.getY()-esqID.getY())/JPanelGrafico.getWidth()
				 );
    }


    /**
     * Constructor 
     * @param miObs Obejto {@link MiraObstaculo} del cual se obtendrá toda la información.
     */
	public PanelMuestraRuta(Ruta rupas) {
		RU=rupas;
		setLayout(new BorderLayout(3,3));
		esqID=new Point2D.Double();
		esqSI=new Point2D.Double();
		restaurar=true;	//se actualizan las esquinas la primera vez

		//Primero el Panel
		JPanelGrafico=new JPanel() {
			protected void paintComponent(Graphics g0) {
				Graphics2D g=(Graphics2D)g0;
				super.paintComponent(g);
				if(restaurar) {
					//restauramos las esquinas
					double minCX=RU.getPunto(0).getXLocal();
					double minCY=RU.getPunto(0).getYLocal();
					double maxCX=minCX;
					double maxCY=minCY;
					for(int i=1; i<RU.getNumPuntos(); i++) {
						if(RU.getPunto(i).getXLocal()<minCX) minCX=RU.getPunto(i).getXLocal();
						if(RU.getPunto(i).getXLocal()>maxCX) maxCX=RU.getPunto(i).getXLocal();
						if(RU.getPunto(i).getYLocal()<minCY) minCY=RU.getPunto(i).getYLocal();
						if(RU.getPunto(i).getYLocal()>maxCY) maxCY=RU.getPunto(i).getYLocal();
					}
					//Usamos mismo factor en ambas direcciones y centramos.
					double Dx=(maxCX-minCX);
					double Dy=(maxCY-minCY);
					double f=escalasD[jcbEscalas.getSelectedIndex()]/pixelEscala;
//					double fx=Dx/getHeight();
//					double fy=Dy/getWidth();
//					double f=(fx>fy)?fx:fy;
					double lx=f*getHeight();
					double ly=f*getWidth();
					esqSI.setLocation(minCX+Dx+(lx-Dx)/2,minCY+Dy+(ly-Dy)/2);
					esqID.setLocation(minCX-(lx-Dx)/2,minCY-(ly-Dy)/2);

					restaurar=false;
					escalado=true;
				}
				if (escalado) {
					//pintamos referencia de escala
					g.setColor(Color.WHITE);
					float pxSep=20;
					float altE=4;
					float xCentE=getWidth()-pixelEscala/2-pxSep;
					float yCentE=getHeight()-pxSep;
					GeneralPath gpE=new GeneralPath(GeneralPath.WIND_EVEN_ODD,4);
					gpE.moveTo(xCentE-pixelEscala/2, yCentE-altE);
					gpE.lineTo(xCentE-pixelEscala/2, yCentE);
					gpE.lineTo(xCentE+pixelEscala/2, yCentE);
					gpE.lineTo(xCentE+pixelEscala/2, yCentE-altE);
					g.draw(gpE);
					g.drawString((String)jcbEscalas.getSelectedItem(), xCentE, yCentE-altE);
				}
				{//Pintamos  ejes en 0,0
					g.setColor(Color.WHITE);
					Point2D.Double pxCentro=point2Pixel(0.0,0.0);
					GeneralPath ejes=new GeneralPath(GeneralPath.WIND_EVEN_ODD,3);
					ejes.moveTo((float)pxCentro.getX()-TamEjes, (float)pxCentro.getY());
					ejes.lineTo((float)pxCentro.getX(), (float)pxCentro.getY());
					ejes.lineTo((float)pxCentro.getX(), (float)pxCentro.getY()-TamEjes);
					g.draw(ejes);
					g.drawString("N",(float)pxCentro.getX()+3,(float)pxCentro.getY()-TamEjes+12);
					g.drawString("W",(float)pxCentro.getX()-TamEjes,(float)pxCentro.getY()-3);
				}
				{
					//pintamos el trayecto
					g.setStroke(new BasicStroke());
					g.setColor(Color.YELLOW);
					GeneralPath gpTr= 
						new GeneralPath(GeneralPath.WIND_EVEN_ODD, RU.getNumPuntos());

					Point2D.Double px=point2Pixel(RU.getPunto(0).getXLocal(),RU.getPunto(0).getYLocal());
					gpTr.moveTo((float)px.getX(),(float)px.getY());
					for(int i=1; i<RU.getNumPuntos(); i++) {
						px=point2Pixel(RU.getPunto(i).getXLocal(),RU.getPunto(i).getYLocal());
						//Siguientes puntos son lineas
						gpTr.lineTo((float)px.getX(),(float)px.getY());
					}

					g.draw(gpTr);
				}
				
				if(RU.getNumPuntos()>2)	{
					//Posición y orientación del coche
					g.setStroke(new BasicStroke(3));
					g.setPaint(Color.GRAY);
					g.setColor(Color.GRAY);
					double xl=RU.getUltimoPto().getXLocal();
					double yl=RU.getUltimoPto().getYLocal();
					double yaw=Math.atan2(yl-RU.getPunto(RU.getNumPuntos()-2).getYLocal()
							, xl-RU.getPunto(RU.getNumPuntos()-2).getXLocal());
					double[] esqDD={xl+anchoCoche/2*Math.sin(yaw)
							,yl-anchoCoche/2*Math.cos(yaw) };
					double[] esqDI={xl-anchoCoche/2*Math.sin(yaw)
							,yl+anchoCoche/2*Math.cos(yaw) };
					double[] esqPD={esqDD[0]-largoCoche*Math.cos(yaw)
							,esqDD[1]-largoCoche*Math.sin(yaw) };
					double[] esqPI={esqDI[0]-largoCoche*Math.cos(yaw)
							,esqDI[1]-largoCoche*Math.sin(yaw) };
					Point2D pxDD=point2Pixel(esqDD);
					Point2D pxDI=point2Pixel(esqDI);
					Point2D pxPD=point2Pixel(esqPD);
					Point2D pxPI=point2Pixel(esqPI);
					GeneralPath coche=new GeneralPath();
					coche.moveTo((float)pxDD.getX(),(float)pxDD.getY());
					coche.lineTo((float)pxPD.getX(),(float)pxPD.getY());
					coche.lineTo((float)pxPI.getX(),(float)pxPI.getY());
					coche.lineTo((float)pxDI.getX(),(float)pxDI.getY());
					coche.closePath();
					g.fill(coche);
					g.draw(coche);
				}

			}
		};
		JPanelGrafico.setMinimumSize(new Dimension(400,400));
		JPanelGrafico.setSize(new Dimension(400,400));
		JPanelGrafico.setBackground(Color.BLACK);
		JPanelGrafico.addMouseListener(this);
		add(JPanelGrafico,BorderLayout.CENTER);
		
		{
			JPanel jpSur=new JPanel();
			
			jpSur.add(new JLabel("Escala"));
			jcbEscalas=new JComboBox(escalasS);
			jcbEscalas.setSelectedIndex(4);
			jcbEscalas.addActionListener(this);
			jpSur.add(jcbEscalas);
			
			
			add(jpSur,BorderLayout.SOUTH);
			
		}
		
	}

	
    /**
     * Doble click del boton 1 vuelve a la presentación normal
     */
	public void mouseClicked(MouseEvent even) {
		if(even.getButton()!=MouseEvent.BUTTON1 || even.getClickCount()!=2)
			return;
		System.out.println("Clickeado Boton "+even.getButton()
				+" en posición: ("+even.getX()+","+even.getY()+") "
				+even.getClickCount()+" veces");
		restaurar=true;
		JPanelGrafico.repaint();
	}
	
    /**
     * Sólo nos interesan pulsaciones del boton 1  para hacer zoom.
     * @see #mouseReleased(MouseEvent)
     */
	public void mousePressed(MouseEvent even) {
		evenPulsa=null;
		if(even.getButton()==MouseEvent.BUTTON1) {
			Point2D.Double ptPulsa=pixel2Point(even.getX(),even.getY());
			System.out.println("Pulsado Boton "+even.getButton()
					+" en posición: ("+even.getX()+","+even.getY()+")"
					+"  ("+ptPulsa.getX()+","+ptPulsa.getY()+")  "
					+" distancia: "+ptPulsa.distance(new Point2D.Double(0,0))
			);
			evenPulsa = even;
			return;
		}
	}

	/**
     * Sólo nos interesan pulsaciones del boton 1 para hacer zoom.
     * Termina el trabajo empezado en {@link #mousePressed(MouseEvent)}
     */
	public void mouseReleased(MouseEvent even) {
		if(even.getButton()==MouseEvent.BUTTON1) {
			System.out.println("Soltado Boton "+even.getButton()
					+" en posición: ("+even.getX()+","+even.getY()+")");
			if(evenPulsa!=null) {
				System.out.println("Soltado Boton "+even.getButton()
						+" en posición: ("+even.getX()+","+even.getY()+")");
				//Creamos rectángulo si está suficientemente lejos
				if(even.getX()-evenPulsa.getX()>50 
						&& even.getY()-evenPulsa.getY()>50) {
					//como se usan las esquinas actuales para calcular las nuevas sólo podemos modificarlas 
					// después
					Point2D.Double nuevaEsqSI=pixel2Point( new Point2D.Double(evenPulsa.getX(),evenPulsa.getY()) );
					esqID.setLocation(pixel2Point( new Point2D.Double(even.getX(),even.getY()) ));
					esqSI.setLocation(nuevaEsqSI);
					escalado=false;
					JPanelGrafico.repaint();
					System.out.println("Puntos:  SI ("+ esqSI.getX()+ ","+esqSI.getY() +") "
							+"  ID ("+esqID.getX()+","+esqID.getY()+")"
							+"   ("+JPanelGrafico.getWidth()+","+JPanelGrafico.getHeight()+")"
					);
				}
			}
		}
	}

	/** NO hacemos nada */
	public void mouseEntered(MouseEvent arg0) {
		// No hacemos nada por ahora
		
	}

	/** NO hacemos nada */
	public void mouseExited(MouseEvent arg0) {
		// No hacemos nada por ahora
		
	}


	/**
	 * Acatualiza la presentación con los datos en {@link #MI}.
	 * Se debe invocar cuando {@link #MI} realiza un nuevo cálculo. 
	 */
	public void nuevoPunto() {
		restaurar=true;
		//programamos la actualizacion de la ventana
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				repaint();			
			}
		});
	}
	

	public void handleGpsEvent(GpsEvent ev) {
		nuevoPunto();
	}


	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent ae) {
		if(ae.getSource()==jcbEscalas) {
			//solo repintamos restaurando
			restaurar=true;
			JPanelGrafico.repaint();
		}
	}

	
	/**
	 * Programa para probar 
	 * @param args
	 */
	public static void main(String[] args) {
		
		double[][] rutaLLA={{ 28.481901 , -16.321980 , 613.100000}
		,{ 28.481901 , -16.321987 , 613.100000}
		,{ 28.481901 , -16.321993 , 613.100000}
		,{ 28.481901 , -16.322001 , 613.100000}
		,{ 28.481901 , -16.322006 , 613.200000}
		,{ 28.481901 , -16.322014 , 613.200000}
		,{ 28.481901 , -16.322020 , 613.200000}
		,{ 28.481901 , -16.322027 , 613.200000}
		,{ 28.481901 , -16.322033 , 613.200000}
		,{ 28.481901 , -16.322041 , 613.200000}
		,{ 28.481901 , -16.322046 , 613.200000}
		,{ 28.481901 , -16.322054 , 613.200000}
		,{ 28.481901 , -16.322060 , 613.300000}
		,{ 28.481901 , -16.322067 , 613.300000}
		,{ 28.481901 , -16.322073 , 613.200000}
		,{ 28.481901 , -16.322081 , 613.200000}
		,{ 28.481899 , -16.322086 , 613.200000}
		,{ 28.481899 , -16.322094 , 613.200000}
		,{ 28.481897 , -16.322100 , 613.200000}
		,{ 28.481895 , -16.322105 , 613.200000}
		,{ 28.481892 , -16.322111 , 613.200000}
		,{ 28.481888 , -16.322117 , 613.200000}
		,{ 28.481886 , -16.322121 , 613.200000}
		,{ 28.481880 , -16.322124 , 613.200000}
		,{ 28.481874 , -16.322128 , 613.400000}
		,{ 28.481869 , -16.322130 , 613.500000}
		,{ 28.481863 , -16.322132 , 613.500000}
		,{ 28.481855 , -16.322132 , 613.500000}
		,{ 28.481850 , -16.322132 , 613.600000}
		,{ 28.481840 , -16.322132 , 613.600000}
		,{ 28.481834 , -16.322132 , 613.700000}
		,{ 28.481827 , -16.322132 , 613.700000}
		,{ 28.481819 , -16.322132 , 613.700000}
		,{ 28.481812 , -16.322132 , 613.800000}
		,{ 28.481806 , -16.322132 , 613.800000}
		,{ 28.481796 , -16.322130 , 613.800000}
		,{ 28.481791 , -16.322130 , 613.900000}
		,{ 28.481781 , -16.322130 , 614.000000}
		,{ 28.481775 , -16.322130 , 614.000000}
		,{ 28.481768 , -16.322130 , 614.100000}
		,{ 28.481762 , -16.322130 , 614.200000}
		,{ 28.481752 , -16.322130 , 614.300000}
		,{ 28.481747 , -16.322130 , 614.400000}
		,{ 28.481737 , -16.322130 , 614.500000}
		,{ 28.481731 , -16.322128 , 614.500000}
		,{ 28.481724 , -16.322128 , 614.500000}
		,{ 28.481716 , -16.322128 , 614.500000}
		,{ 28.481709 , -16.322128 , 614.600000}
		,{ 28.481703 , -16.322128 , 614.600000}
		,{ 28.481693 , -16.322128 , 614.600000}
		,{ 28.481688 , -16.322128 , 614.700000}
		,{ 28.481678 , -16.322128 , 614.700000}
		,{ 28.481672 , -16.322128 , 614.700000}
		,{ 28.481663 , -16.322128 , 614.800000}
		,{ 28.481657 , -16.322128 , 614.800000}
		,{ 28.481647 , -16.322128 , 614.800000}
		,{ 28.481642 , -16.322128 , 614.800000}
		,{ 28.481634 , -16.322128 , 614.900000}
		,{ 28.481627 , -16.322128 , 614.900000}
		,{ 28.481619 , -16.322128 , 614.900000}
		,{ 28.481611 , -16.322130 , 614.900000}
		,{ 28.481604 , -16.322132 , 614.900000}
		,{ 28.481598 , -16.322134 , 615.000000}
		,{ 28.481590 , -16.322138 , 615.000000}
		,{ 28.481585 , -16.322140 , 615.000000}
		,{ 28.481579 , -16.322145 , 615.000000}
		,{ 28.481573 , -16.322151 , 615.000000}
		,{ 28.481569 , -16.322159 , 615.000000}
		,{ 28.481565 , -16.322165 , 615.100000}
		,{ 28.481564 , -16.322174 , 615.100000}
		,{ 28.481562 , -16.322180 , 615.100000}
		,{ 28.481560 , -16.322189 , 615.200000}
		,{ 28.481560 , -16.322195 , 615.200000}
		,{ 28.481560 , -16.322205 , 615.200000}
		,{ 28.481562 , -16.322212 , 615.100000}
		,{ 28.481564 , -16.322222 , 615.100000}
		,{ 28.481564 , -16.322227 , 615.100000}
		,{ 28.481567 , -16.322237 , 615.100000}
		,{ 28.481569 , -16.322241 , 615.100000}
		,{ 28.481573 , -16.322250 , 615.100000}
		,{ 28.481575 , -16.322256 , 615.000000}
		,{ 28.481581 , -16.322262 , 614.900000}
		,{ 28.481583 , -16.322268 , 614.900000}
		,{ 28.481588 , -16.322273 , 614.800000}
		,{ 28.481592 , -16.322279 , 614.800000}
		,{ 28.481598 , -16.322285 , 614.700000}
		,{ 28.481604 , -16.322289 , 614.700000}
		,{ 28.481609 , -16.322292 , 614.700000}
		,{ 28.481615 , -16.322294 , 614.700000}
		,{ 28.481623 , -16.322298 , 614.700000}
		,{ 28.481628 , -16.322300 , 614.700000}
		,{ 28.481636 , -16.322302 , 614.700000}
		,{ 28.481642 , -16.322302 , 614.700000}
		,{ 28.481651 , -16.322302 , 614.600000}
		,{ 28.481657 , -16.322304 , 614.600000}
		,{ 28.481667 , -16.322304 , 614.600000}
		,{ 28.481672 , -16.322302 , 614.600000}
		,{ 28.481682 , -16.322302 , 614.600000}
		,{ 28.481689 , -16.322302 , 614.600000}
		,{ 28.481699 , -16.322302 , 614.500000}
		,{ 28.481705 , -16.322302 , 614.500000}
		,{ 28.481714 , -16.322302 , 614.400000}
		,{ 28.481722 , -16.322302 , 614.300000}
		,{ 28.481731 , -16.322302 , 614.300000}
		,{ 28.481739 , -16.322302 , 614.200000}
		,{ 28.481749 , -16.322302 , 614.100000}
		,{ 28.481754 , -16.322302 , 614.100000}
		,{ 28.481766 , -16.322302 , 614.000000}
		,{ 28.481771 , -16.322302 , 613.900000}
		,{ 28.481783 , -16.322302 , 613.900000}
		,{ 28.481789 , -16.322302 , 613.800000}
		,{ 28.481800 , -16.322302 , 613.800000}
		,{ 28.481806 , -16.322302 , 613.800000}
		,{ 28.481815 , -16.322302 , 613.700000}
		,{ 28.481821 , -16.322302 , 613.700000}
		,{ 28.481831 , -16.322302 , 613.600000}
		,{ 28.481838 , -16.322302 , 613.600000}
		,{ 28.481848 , -16.322302 , 613.500000}
		,{ 28.481853 , -16.322302 , 613.500000}
		,{ 28.481861 , -16.322302 , 613.300000}
		,{ 28.481869 , -16.322302 , 613.200000}
		,{ 28.481876 , -16.322302 , 613.100000}
		,{ 28.481884 , -16.322304 , 613.100000}
		,{ 28.481892 , -16.322304 , 613.000000}
		,{ 28.481899 , -16.322304 , 613.000000}
		,{ 28.481907 , -16.322304 , 613.000000}
		,{ 28.481913 , -16.322304 , 613.000000}
		,{ 28.481922 , -16.322304 , 612.800000}
		,{ 28.481928 , -16.322304 , 612.800000}
		,{ 28.481936 , -16.322304 , 612.800000}
		,{ 28.481941 , -16.322304 , 612.700000}
		,{ 28.481951 , -16.322304 , 612.800000}
		,{ 28.481956 , -16.322304 , 612.800000}
		,{ 28.481966 , -16.322304 , 612.700000}
		,{ 28.481972 , -16.322304 , 612.600000}
		,{ 28.481981 , -16.322304 , 612.600000}
		,{ 28.481989 , -16.322304 , 612.500000}
		,{ 28.481998 , -16.322304 , 612.400000}
		,{ 28.482006 , -16.322302 , 612.400000}
		,{ 28.482016 , -16.322302 , 612.400000}
		,{ 28.482023 , -16.322302 , 612.300000}
		,{ 28.482033 , -16.322298 , 612.100000}
		,{ 28.482037 , -16.322296 , 611.900000}
		,{ 28.482044 , -16.322290 , 611.800000}
		,{ 28.482048 , -16.322285 , 611.600000}
		,{ 28.482050 , -16.322275 , 611.600000}
		,{ 28.482054 , -16.322269 , 611.700000}
		,{ 28.482054 , -16.322260 , 611.800000}
		,{ 28.482054 , -16.322252 , 611.900000}
		,{ 28.482054 , -16.322241 , 612.000000}
		,{ 28.482052 , -16.322233 , 612.100000}
		,{ 28.482050 , -16.322224 , 612.200000}
		,{ 28.482050 , -16.322216 , 612.200000}
		,{ 28.482050 , -16.322205 , 612.300000}
		,{ 28.482050 , -16.322197 , 612.400000}
		,{ 28.482050 , -16.322184 , 612.500000}
		,{ 28.482050 , -16.322176 , 612.400000}
		,{ 28.482048 , -16.322165 , 612.400000}
		,{ 28.482048 , -16.322155 , 612.400000}
		,{ 28.482048 , -16.322142 , 612.400000}
		,{ 28.482048 , -16.322134 , 612.400000}
		,{ 28.482048 , -16.322121 , 612.500000}
		,{ 28.482048 , -16.322113 , 612.400000}
		,{ 28.482048 , -16.322100 , 612.500000}
		,{ 28.482048 , -16.322092 , 612.600000}
		,{ 28.482048 , -16.322079 , 612.700000}
		,{ 28.482048 , -16.322071 , 612.800000}
		,{ 28.482046 , -16.322058 , 612.900000}
		,{ 28.482046 , -16.322050 , 612.900000}
		,{ 28.482046 , -16.322039 , 612.800000}
		,{ 28.482046 , -16.322031 , 612.700000}
		,{ 28.482044 , -16.322018 , 612.800000}
		,{ 28.482044 , -16.322010 , 612.900000}
		,{ 28.482044 , -16.321999 , 612.900000}
		,{ 28.482044 , -16.321991 , 612.900000}
		,{ 28.482044 , -16.321978 , 612.900000}
		,{ 28.482044 , -16.321970 , 612.900000}
		,{ 28.482044 , -16.321959 , 612.900000}
		,{ 28.482042 , -16.321951 , 612.800000}
		,{ 28.482042 , -16.321938 , 612.800000}
		,{ 28.482042 , -16.321930 , 612.800000}
		,{ 28.482040 , -16.321918 , 612.700000}
		,{ 28.482040 , -16.321909 , 612.700000}
		,{ 28.482040 , -16.321898 , 612.600000}
		,{ 28.482040 , -16.321890 , 612.600000}
		,{ 28.482038 , -16.321877 , 612.500000}
		,{ 28.482038 , -16.321869 , 612.400000}
		,{ 28.482038 , -16.321856 , 612.400000}
		,{ 28.482038 , -16.321848 , 612.400000}
		,{ 28.482037 , -16.321836 , 612.300000}
		,{ 28.482037 , -16.321827 , 612.400000}
		,{ 28.482037 , -16.321815 , 612.400000}
		,{ 28.482037 , -16.321808 , 612.400000}
		,{ 28.482037 , -16.321795 , 612.400000}
		,{ 28.482037 , -16.321787 , 612.300000}
		,{ 28.482037 , -16.321774 , 612.300000}
		,{ 28.482037 , -16.321766 , 612.300000}
		,{ 28.482035 , -16.321753 , 612.300000}
		,{ 28.482035 , -16.321743 , 612.300000}
		,{ 28.482037 , -16.321730 , 612.300000}
		,{ 28.482035 , -16.321722 , 612.200000}
		,{ 28.482035 , -16.321709 , 612.200000}
		,{ 28.482035 , -16.321701 , 612.200000}
		,{ 28.482035 , -16.321688 , 612.200000}
		,{ 28.482035 , -16.321678 , 612.100000}
		,{ 28.482035 , -16.321667 , 612.100000}
		,{ 28.482035 , -16.321657 , 612.000000}
		,{ 28.482035 , -16.321644 , 612.000000}
		,{ 28.482035 , -16.321634 , 612.000000}
		,{ 28.482033 , -16.321623 , 612.000000}
		,{ 28.482033 , -16.321613 , 611.900000}
		,{ 28.482031 , -16.321602 , 612.000000}
		,{ 28.482029 , -16.321594 , 612.000000}
		,{ 28.482025 , -16.321583 , 611.900000}
		,{ 28.482023 , -16.321577 , 611.900000}
		,{ 28.482018 , -16.321568 , 611.900000}
		,{ 28.482014 , -16.321562 , 611.900000}
		,{ 28.482006 , -16.321554 , 612.000000}
		,{ 28.482000 , -16.321552 , 612.100000}
		,{ 28.481991 , -16.321548 , 612.100000}
		,{ 28.481985 , -16.321547 , 612.100000}
		,{ 28.481977 , -16.321545 , 612.200000}
		,{ 28.481972 , -16.321545 , 612.200000}
		,{ 28.481962 , -16.321545 , 612.300000}
		,{ 28.481956 , -16.321545 , 612.300000}
		,{ 28.481949 , -16.321547 , 612.300000}
		,{ 28.481943 , -16.321548 , 612.300000}
		,{ 28.481936 , -16.321552 , 612.400000}
		,{ 28.481930 , -16.321556 , 612.500000}
		,{ 28.481922 , -16.321562 , 612.600000}
		,{ 28.481918 , -16.321568 , 612.600000}
		,{ 28.481911 , -16.321575 , 612.700000}
		,{ 28.481907 , -16.321581 , 612.700000}
		,{ 28.481903 , -16.321592 , 612.700000}
		,{ 28.481901 , -16.321600 , 612.700000}
		,{ 28.481901 , -16.321611 , 612.800000}
		,{ 28.481901 , -16.321619 , 612.700000}
		,{ 28.481901 , -16.321630 , 612.700000}
		,{ 28.481901 , -16.321638 , 612.700000}
		,{ 28.481901 , -16.321651 , 612.700000}
		,{ 28.481901 , -16.321659 , 612.700000}
		,{ 28.481901 , -16.321671 , 612.700000}
		,{ 28.481901 , -16.321680 , 612.700000}
		,{ 28.481901 , -16.321692 , 612.700000}
		,{ 28.481901 , -16.321701 , 612.800000}
		,{ 28.481901 , -16.321712 , 612.800000}
		,{ 28.481901 , -16.321720 , 612.800000}
		,{ 28.481901 , -16.321733 , 612.800000}
		,{ 28.481901 , -16.321741 , 612.800000}
		,{ 28.481901 , -16.321754 , 612.800000}
		,{ 28.481901 , -16.321762 , 612.900000}
		,{ 28.481901 , -16.321774 , 612.800000}
		,{ 28.481901 , -16.321783 , 612.800000}
		,{ 28.481901 , -16.321795 , 612.900000}
		,{ 28.481901 , -16.321802 , 612.900000}
		,{ 28.481901 , -16.321815 , 613.000000}
		,{ 28.481901 , -16.321823 , 613.000000}
		,{ 28.481901 , -16.321836 , 613.100000}
		,{ 28.481901 , -16.321844 , 613.100000}
		,{ 28.481901 , -16.321857 , 613.100000}
		,{ 28.481901 , -16.321865 , 613.100000}
		,{ 28.481901 , -16.321878 , 613.100000}
		,{ 28.481901 , -16.321886 , 613.100000}
		,{ 28.481901 , -16.321899 , 613.000000}
		,{ 28.481901 , -16.321907 , 613.000000}
		,{ 28.481903 , -16.321920 , 613.000000}
		,{ 28.481903 , -16.321928 , 613.000000}
		,{ 28.481903 , -16.321941 , 613.000000}
		};
		
		Ruta ru=new Ruta();
		GPSData nuevoPunto=new GPSData(rutaLLA[0]);
		ru.actualizaSistemaLocal(nuevoPunto);
		ru.add(ru.setCoordenadasLocales(nuevoPunto));
		
		
		JFrame ventana=new JFrame("Panel Muestra Ruta");
		ventana.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		PanelMuestraRuta pmr=new PanelMuestraRuta(ru);
		ventana.add(pmr);
		ventana.setSize(new Dimension(800,600));
		ventana.setVisible(true);

		//Espera inicial
		try {
			Thread.sleep(5000);
		} catch (Exception e) { }
		
		//Vamos añadiendo puntos poco a poco
		for(int i=1; i<rutaLLA.length; i++) {
			try {
				Thread.sleep(200);
			} catch (Exception e) { }
			ru.add(ru.setCoordenadasLocales((new GPSData(rutaLLA[i]).calculaECEF())));
			pmr.nuevoPunto();
			System.out.println("Añadido "+ru.getUltimoPto());
		}
		
	}



}
