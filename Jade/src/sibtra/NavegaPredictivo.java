/* */

package sibtra;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;
import javax.swing.SpinnerNumberModel;

import sibtra.controlcarro.ControlCarro;
import sibtra.controlcarro.PanelCarro;
import sibtra.gps.GPSConnectionTriumph;
import sibtra.gps.GPSData;
import sibtra.gps.PanelGPSTriumph;
import sibtra.gps.PanelGrabarRuta;
import sibtra.gps.PanelMuestraRuta;
import sibtra.gps.Ruta;
import sibtra.imu.AngulosIMU;
import sibtra.imu.ConexionSerialIMU;
import sibtra.imu.PanelMuestraAngulosIMU;
import sibtra.lms.BarridoAngular;
import sibtra.lms.LMSException;
import sibtra.lms.ManejaLMS;
import sibtra.log.PanelLoggers;
import sibtra.predictivo.Coche;
import sibtra.predictivo.ControlPredictivo;
import sibtra.predictivo.PanelMuestraPredictivo;
import sibtra.rfyruta.MiraObstaculo;
import sibtra.rfyruta.PanelMiraObstaculo;
import sibtra.rfyruta.PanelMiraObstaculoSubjetivo;
import sibtra.util.EligeSerial;
import sibtra.util.UtilCalculos;

/**
 * Para realizar la navegación controlando el coche con @link {@link ControlPredictivo}
 *  detectando obstáculos con el RF.
 * @author alberto
 *
 */
public class NavegaPredictivo implements ActionListener {
	

	//constantes para el cácluo ***********************************************************************
    /** Milisegundos del ciclo */
    private static final long periodoMuestreoMili = 200;
    private static final double COTA_ANGULO = Math.toRadians(30);
    /** Si la velocidad es más baja que este umbral se deja de mandar comandos al volante*/
	private static final double umbralMinimaVelocidad = 0.2;
	/** Distancia a la que el coche empieza a frenar si se encuentra un obstáculo a 
	 * menos de esa distancia*/
	private static final double distanciaSeguridad = 10;
	/** Distancia a la que idealmente se detendrá el coche del obstáculo*/
	private static final double margenColision = 3;
	
	/** distancia maxima entre puntos sucesivos de Tr */
    private double distMaxTr = 0.1;        

	
	//Campos de interacción con los dispositivos ******************************************************
    private ConexionSerialIMU conIMU=null;
    private GPSConnectionTriumph conGPS=null;
    private ManejaLMS manLMS=null;
    private ControlCarro contCarro=null;

	//Campos relacionados con los cálculos ************************************************************
    /** Ruta con la que se está trabajando. Mientras sea ==null significa que no se ha cargado ruta*/
    private Ruta rutaEspacial=null;
	/** Variable que indicará cuando se quiere navegar */
	private boolean navegando=false;

	double[][] Tr = null;
    private MiraObstaculo mi;
    private double desMag;
    Coche modCoche;
    ControlPredictivo cp;
    protected double distRF = 80;
    /** Regula la velocidad que se resta a la consigna principal de velocidad por 
     * errores en la orientación*/
	private double gananciaVel = 2;
	/** Cuando se manda a frenar la función {@link buscaPuntoFrenado} devuelve en esta variable 
	 * el punto que se encuentra a la distancia de frenado*/
	private int puntoFrenado=-1;
	private GPSData centroToTr;
	/** Regula la velocidad que se resta a la consigna principal de velocidad por 
     * errores en la posición lateral*/
	private double gananciaLateral=1;
	/** Pendiente de la rampa de frenado para la parada total */
	private double pendienteFrenado=1.0;

    
    //Campos relacionados con la representación gráfica ****************************************************
	/** La ventana principal ocupará casi toda la pantalla grande */
	private JFrame ventanaPrincipal=null;
	/** Ocupará toda la pantalla pequeña (táctil) */
	private JFrame ventadaPeque=null;

    private PanelGPSTriumph pgt;
    private PanelMuestraAngulosIMU pmai;
    private PanelMiraObstaculo pmo;
	private PanelMiraObstaculoSubjetivo pmoS;
	private PanelMuestraRuta pmr;
	private PanelLoggers pmLog;
	private PanelGrabarRuta panGrabar;
    private JFileChooser fc;

    //TODO poner acciones para todo
	private Action actGrabarRuta;
	private Action actPararGrabarRuta;
	private Action actNavegar;
	private Action actFrenar;
    
//    JCheckBox jcbNavegando;
    SpinnerNumberModel spFrenado;
    JSpinner jsDistFrenado;
    private PanelMuestraPredictivo pmp;
    private PanelCarro pmCoche;
    private JCheckBox jcbUsarRF;
	private SpinnerNumberModel spGananciaVel;
	private JSpinner jsGananciaVel;
	//Items del menu
	private JMenuItem miCargar;
	private JMenuItem miSalir;
	private JProgressBar jpbTRF;
	private JTabbedPane tbPanelDecho;
	private JTabbedPane tbPanelIzdo;

	/** Se le han de pasar los 3 puertos series para: IMU, GPS, RF y Coche (en ese orden)*/
    public NavegaPredictivo(String[] args) {
        if (args == null || args.length < 4) {
            System.err.println("Son necesarios 4 argumentos con los puertos seriales");
            System.exit(1);
        }
        //inicializamos modelo del coche
        modCoche = new Coche();

        //conexión de la IMU
        System.out.println("Abrimos conexión IMU en "+args[1]);
        conIMU = new ConexionSerialIMU();
        if (!conIMU.ConectaPuerto(args[1], 5)) {
            System.err.println("Problema en conexión serial con la IMU");
            System.exit(1);
        }

        //comunicación con GPS
        System.out.println("Abrimos conexión GPS en "+args[0]);
        try {
            conGPS = new GPSConnectionTriumph(args[0]);
        } catch (Exception e) {
            System.err.println("Problema a crear GPSConnection:" + e.getMessage());
            System.exit(1);
        }
        if (conGPS == null) {
            System.err.println("No se obtuvo GPSConnection");
            System.exit(1);
        }
        conGPS.setCsIMU(conIMU);


        //Conectamos a RF
        System.out.println("Abrimos conexión LMS en "+args[2]);
        try {
            manLMS = new ManejaLMS(args[2]);
            manLMS.setDistanciaMaxima(80);
//            manLMS.setResolucionAngular((short)100);
            manLMS.CambiaAModo25();

        } catch (LMSException e) {
            System.err.println("No fue posible conectar o configurar RF");
        }

        //Conectamos Carro
        System.out.println("Abrimos conexión al Carro en "+args[3]);
        contCarro = new ControlCarro(args[3]);

        if (contCarro.isOpen() == false) {
            System.err.println("No se obtuvo Conexion al Carro");            
        }
        conGPS.setCsCARRO(contCarro);

        //elegir fichero
        fc = new JFileChooser(new File("./Rutas"));


        //Definición de los elementos gráficos =============================================
        actGrabarRuta=new AbstractAction("Grabar Ruta") {
            public void actionPerformed(ActionEvent e) {
            	pmr.setRuta(conGPS.getBufferRutaEspacial());
            	actGrabarRuta.setEnabled(false);
            	actPararGrabarRuta.setEnabled(true);
            }
        };
        actGrabarRuta.setEnabled(true); //inicialmente activada
        actPararGrabarRuta=new AbstractAction("Parar Grabar Ruta") {
            public void actionPerformed(ActionEvent e) {
            	pmr.setRuta(conGPS.getBufferEspacial()); 
            	actGrabarRuta.setEnabled(true);
            	actPararGrabarRuta.setEnabled(false);
            	
            }
        };
        actNavegar=new AbstractAction("Navegar") {
        	public void actionPerformed(ActionEvent e) {
        		if (rutaEspacial==null) { //por si las moscas
        			putValue(SELECTED_KEY, false);
        			setEnabled(false);
        		} else if ((Boolean)getValue(SELECTED_KEY)) {
        			navegando=true;
        			actGrabarRuta.setEnabled(false); //no se puede grabar si estamos navegando
        			actFrenar.setEnabled(true); //se puede frenar cuando se está navegando
        			cp.iniciaNavega();
        		} else {
        			//se desactivo Navegando
        			navegando=false;
        			actGrabarRuta.setEnabled(true); //ahora podemos grabar
        			actFrenar.setEnabled(false); //no se puede frenar cuando se está navegando
        			contCarro.stopControlVel(); //Paramos el control del carro
        		}
        	}
        };
		actNavegar.putValue(Action.SELECTED_KEY, false);
		if(rutaEspacial==null) actNavegar.setEnabled(false);

        
        actFrenar=new AbstractAction("Frenar") {
        	public void actionPerformed(ActionEvent e) {
        		if((Boolean)getValue(SELECTED_KEY)){
        			// Se acaba de seleccionar
        			double distFrenado = spFrenado.getNumber().doubleValue();
        			puntoFrenado = buscaPuntoFrenado(distFrenado);
        			jsDistFrenado.setEnabled(false);
        		}else{
        			// Se acaba de desactivar
        			jsDistFrenado.setEnabled(true);
        			puntoFrenado = -1;
        		}
        	}
        };
		actFrenar.putValue(Action.SELECTED_KEY, false);
		actFrenar.setEnabled(false); //se activa cuando se está navegando

        ventanaPrincipal=new JFrame("Navega Predictivo");
        
    	{   //Parte baja de la ventana principal
    		JPanel jpSur = new JPanel(new FlowLayout(3));
    		ventanaPrincipal.getContentPane().add(jpSur, BorderLayout.SOUTH);

    		//Checkbox para navegar
    		JCheckBox jcbNavegando = new JCheckBox(actNavegar);
    		jpSur.add(jcbNavegando);
    		//Checkbox para frenar
    		JCheckBox jcbFrenando = new JCheckBox(actFrenar);
    		jpSur.add(jcbFrenando);
    		//Spinner para fijar la distancia de frenado
    		double value = 5;
    		double min = 1;
    		double max = 50;
    		double step = 0.1;
    		spFrenado = new SpinnerNumberModel(value,min,max,step);
    		jsDistFrenado = new JSpinner(spFrenado);
    		jpSur.add(jsDistFrenado);
    		// Spinner para fijar la ganancia del cálculo d la consigna de Velocidad
    		jpSur.add(new JLabel("Ganancia Velocidad"));
    		spGananciaVel = new SpinnerNumberModel(2,0.1,20,0.1);
    		jsGananciaVel = new JSpinner(spGananciaVel);
    		jpSur.add(jsGananciaVel);

    		//Checkbox para detectar con RF
    		jcbUsarRF = new JCheckBox("Usar RF");
    		jcbUsarRF.setSelected(false);
    		if(rutaEspacial==null) jcbUsarRF.setEnabled(false);
    		jpSur.add(jcbUsarRF);
    		
    		//barra de progreso para tiempo RF
    		jpbTRF=new JProgressBar(0,100);
    		jpbTRF.setOrientation(JProgressBar.HORIZONTAL);
    		jpbTRF.setValue(0);
    		jpSur.add(new JLabel("Tiempo RF"));
    		jpSur.add(jpbTRF);
    	}
    	
    	//Solapas del lado izquierdo ===============================================
        tbPanelIzdo=new JTabbedPane();
    	//Panel datos numéricos se colacará a la izda del split panel

        //Panel del GPS
        pgt = new PanelGPSTriumph(conGPS);
        pgt.actualizaGPS(new GPSData());
        tbPanelIzdo.add("GPS",new JScrollPane(pgt));

        //Panel del Coche
        pmCoche=new PanelCarro(contCarro);
        tbPanelIzdo.add("Coche",new JScrollPane(pmCoche));

        //Panel de la Imu
        pmai = new PanelMuestraAngulosIMU();
        pmai.actualizaAngulo(new AngulosIMU(0, 0, 0, 0));
        tbPanelIzdo.add("IMU",new JScrollPane(pmai));
        
        panGrabar=new PanelGrabarRuta(conGPS,actGrabarRuta,actPararGrabarRuta);
        panGrabar.setEnabled(true);
        tbPanelIzdo.add("Grabar",panGrabar);

        //Panel con solapas para la parte derecha de la ventana principal =========================
        //  contendrá las gráficas.
        tbPanelDecho=new JTabbedPane();


        //añadimos los paneles a las solapasprotected
        pmr = new PanelMuestraRuta(conGPS.getBufferEspacial());
        conGPS.addGpsEventListener(pmr);
        tbPanelDecho.add("Ruta",pmr);
        pmp = new PanelMuestraPredictivo(null,null);
        tbPanelDecho.add("Predictivo",pmp);
        pmo = new PanelMiraObstaculo(null);
        tbPanelDecho.add("Obstaculo", pmo);
        {
        	short distMaxRF=80; //valor por defecto
        	try {
        		distMaxRF=(short)manLMS.getDistanciaMaxima();
            	pmoS=new PanelMiraObstaculoSubjetivo(mi,distMaxRF);
    			pmoS.setZona(manLMS.recibeZona((byte)0, true));
    			pmoS.setZona(manLMS.recibeZona((byte)1, true));
    			pmoS.setZona(manLMS.recibeZona((byte)2, true));
        	} catch (LMSException e) {
        		System.err.println("Problema al obtener distancia maxima o zonas en RF");
        	}
        }
        tbPanelDecho.add("Subjetivo",pmoS);
        //Loggers en solapa con scroll panel
        pmLog=new PanelLoggers();
        tbPanelDecho.add("Loggers",new JScrollPane(pmLog));

        
        
//        tbPanelDecho.setPreferredSize(new Dimension(500,600));
//        tbPanelDecho.setMinimumSize(new Dimension(100,600));

    	//split panel en el centro de la ventana principal
        JSplitPane splitPanel=new JSplitPane(JSplitPane.HORIZONTAL_SPLIT
//        		,false  //si al mover la barra componentes se refrescan continuamente
        		,true  //si al mover la barra componentes se refrescan continuamente
        		,tbPanelIzdo
        		,tbPanelDecho
        );

        ventanaPrincipal.getContentPane().add(splitPanel, BorderLayout.CENTER);

        {	//barra de menu
        	JMenuBar barra=new JMenuBar();
        	//menu de archivo
        	JMenu menuArchivo=new JMenu("Fichero");
        	barra.add(menuArchivo);
        	
        	miCargar=new JMenuItem("Cargar Ruta");
        	miCargar.addActionListener(this);
        	menuArchivo.add(miCargar);
        	
        	menuArchivo.addSeparator(); //separador =============================
        	miSalir=new JMenuItem("Salir");
        	miSalir.addActionListener(this);
        	menuArchivo.add(miSalir);
        	
        	//menu de Acciones
        	JMenu menuAcciones=new JMenu("Acciones");
        	menuAcciones.add(new JCheckBoxMenuItem(actNavegar));
        	menuAcciones.add(new JCheckBoxMenuItem(actFrenar));
        	menuAcciones.addSeparator();
        	menuAcciones.add(new JMenuItem(actGrabarRuta));
        	menuAcciones.add(new JMenuItem(actPararGrabarRuta));
        	barra.add(menuAcciones);
        	
        	ventanaPrincipal.setJMenuBar(barra); //ponemos barra en la ventana
        }
        
        //Mostramos la ventana principal con el tamaño y la posición deseada
        ventanaPrincipal.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        ventanaPrincipal.setUndecorated(true);
        ventanaPrincipal.pack();
        ventanaPrincipal.setVisible(true);

//        splitPanel.setDividerLocation(0.50); //La mitad para cada uno
        splitPanel.setDividerLocation(500); //Ajustamos para que no aparezca la barra a la dercha

        //La ventana Secundaria
        ventadaPeque=new JFrame("VERDINO");
        {
        	JPanel central=new JPanel();
        	central.setLayout(new BoxLayout(central,BoxLayout.PAGE_AXIS));
        	//Checkbox para navegar
        	JCheckBox jcbNavegandoP = new JCheckBox(actNavegar);
        	jcbNavegandoP.setFont(jcbNavegandoP.getFont().deriveFont(80.0f));
        	central.add(jcbNavegandoP);
        	//Checkbox para frenar
        	JCheckBox jcbFrenandoP = new JCheckBox(actFrenar);
        	jcbFrenandoP.setFont(jcbNavegandoP.getFont().deriveFont(80.0f));
        	central.add(jcbFrenandoP);
        	
        	JButton jbSaca=new JButton("Dimesiones");
        	jbSaca.addActionListener(this);
        	central.add(jbSaca);
        	
        	ventadaPeque.add(central);
        }
        ventadaPeque.setUndecorated(true); //para que no aparezcan el marco
        ventadaPeque.pack();
        ventadaPeque.setVisible(true);
        
    	//Tread para refrescar los paneles de la ventana
        Thread thRefresco = new Thread() {
        	/** Milisegundos del periodo de actualización */
        	private long milisPeriodo=500;

            public void run() {
    			setName("Refresco Numeros");
        		while (true){
//        			pgt.setEnabled(true);
        			//GPS
        			pgt.actualizaGPS(conGPS.getPuntoActualTemporal());
        			pgt.repinta();
        			//IMU
    				pmai.actualizaAngulo(conIMU.getAngulo());
    				pmai.repinta();
    				//Coche
    				pmCoche.actualizaCarro();
    				pmCoche.repinta();
    				
    				//Loggers
    				pmLog.repinta();

    				try{Thread.sleep(milisPeriodo);} catch (Exception e) {}	
        		}
            }
        };
        thRefresco.start();
        
        //thread para refrescar ventana del RF y calcular distancia al obstaculo
        Thread thRF = new Thread() {
    		public void run() {
    			setName("Recibe RF");
    			BarridoAngular ba=null;
    			while (true) {
    				long t0=System.currentTimeMillis();
    				ba=manLMS.esperaNuevoBarrido(ba);
    				long dt=System.currentTimeMillis()-t0;
    				jpbTRF.setValue((int)dt);
//    				if(dt>32) {
//    					System.err.println("\nNuevo barrido tardó "+dt);
//    				}
    				GPSData pa = conGPS.getPuntoActualTemporal();                            
    	            double[] ptoAct=null;
    	            double angAct=Double.NaN;
    	            if(pa!=null) {
    	             ptoAct= new double[2];
    	             ptoAct[0]=pa.getXLocal(); ptoAct[1]=pa.getYLocal();
    	             angAct = Math.toRadians(pa.getAngulosIMU().getYaw()) + desMag;
    	            }
    				if (mi != null && ptoAct!=null) {
    					//calculamos distancia a obstáculo más cercano
    					distRF = mi.masCercano(ptoAct, angAct, ba);
    				} else {
    					//ponemos posición y barrido ya que no se puede tomar de otro sitio
    					if(ptoAct!=null) pmo.setPosicionYawBarrido(ptoAct, angAct, ba);
//    					System.out.println("Barrido:"+ba);
    					pmoS.setBarrido(ba);
    				}
    				//actualizamos paneles aunque no haya mi
    				pmo.actualiza();
    				pmoS.actualiza();
    			}
    		}
    	};
    	try {
    		//TODO Para acortar barrido
    		manLMS.pideBarridoContinuo((short)10, (short)170, (short)1);
    		while(!manLMS.yaRecibiendoBarridoContinuo())
    			try {Thread.sleep(1000);} catch (Exception e) {}
    	} catch (LMSException e) {
            System.err.println("No fue posible Arrancar barrido continuo RF");
            System.exit(1);
    	}
    	System.out.println("Comenzó recepción barrido continuo. Lanzamos en thread para el RF");
    	thRF.start();

    	// ponemos popups en las tabs
    	tbPanelDecho.addMouseListener(new MouseAdapter() {
    		public void mousePressed(MouseEvent me)
    		{
    			maybeShowPopup(me);
    		}

    		public void mouseReleased(MouseEvent me)
    		{
    			maybeShowPopup(me);
    		}
    	});
    	tbPanelIzdo.addMouseListener(new MouseAdapter() {
    		public void mousePressed(MouseEvent me)
    		{
    			maybeShowPopup(me);
    		}

    		public void mouseReleased(MouseEvent me)
    		{
    			maybeShowPopup(me);
    		}
    	});

        //Fijamos su tamaño y posición
        ventanaPrincipal.setBounds(0, 384, 1024, 742);
        //fijamos su tamaño y posición
        ventadaPeque.setBounds(0, 0, 640, 384);

    }

    // Sacado de http://forums.sun.com/thread.jspa?forumID=257&threadID=372811
    private void maybeShowPopup(final MouseEvent me)
    {
    	JTabbedPane pest;
    	if (me.isPopupTrigger() 
    			&& (pest=(JTabbedPane)me.getSource()).getTabCount()>0
    			&& (pest.getSelectedIndex()<pest.getTabCount())
    	) {
    		JPopupMenu popup = new JPopupMenu();
    		JMenuItem item = new JMenuItem("Cambia de pestaña de lado");
    		popup.add(item);
    		item.addActionListener(new ActionListener() {
    			public void actionPerformed(ActionEvent e)
    			{
    				JTabbedPane tabbed = (JTabbedPane)me.getSource();
    				int i = tabbed.getSelectedIndex();
    				if(i>=tabbed.getTabCount()) return;
    				if(tabbed==tbPanelDecho) {
    					tbPanelIzdo.add(tabbed.getTitleAt(i),tabbed.getComponent(i));
    				} else {
    					tbPanelDecho.add(tabbed.getTitleAt(i),tabbed.getComponent(i));
    				}
    				//NO hace falta borrarla ??
    				//						tabbed.remove(i);
    			}
    		});
    		popup.show(me.getComponent(), me.getX(), me.getY());
    	}
    }    


    public void SacaDimensiones() {
    	//vemos tamaños de panel predictivo
    	System.out.println("Predictivo:");
    	for(int i=0; i<pmp.getComponentCount(); i++) {
    		Component ca=pmp.getComponent(i);
    		System.out.println("Componente "+i
    				+ "  Clase :"+ca.getClass().getName()
    				+ " Size="+ca.getSize()
    				+ "  Minimo="+ca.getMinimumSize()
    				+ "  Maximo="+ca.getMaximumSize()
    				+ "  Preferido="+ca.getPreferredSize()
    		);
    	}
    	//vemos tamaños de panel
    	System.out.println("Obstaculos:");
    	for(int i=0; i<pmo.getComponentCount(); i++) {
    		Component ca=pmo.getComponent(i);
    		System.out.println("Componente "+i
    				+ "  Clase :"+ca.getClass().getName()
    				+ " Size="+ca.getSize()
    				+ "  Minimo="+ca.getMinimumSize()
    				+ "  Maximo="+ca.getMaximumSize()
    				+ "  Preferido="+ca.getPreferredSize()
    		);
    	}
    	//vemos tamaños de panel
    	System.out.println("Subjetivo:");
    	for(int i=0; i<pmoS.getComponentCount(); i++) {
    		Component ca=pmoS.getComponent(i);
    		System.out.println("Componente "+i
    				+ "  Clase :"+ca.getClass().getName()
    				+ " Size="+ca.getSize()
    				+ "  Minimo="+ca.getMinimumSize()
    				+ "  Maximo="+ca.getMaximumSize()
    				+ "  Preferido="+ca.getPreferredSize()
    		);
    	}
    }
    
    /**
     * Método para decidir la consigna de velocidad para cada instante.
     * Se tiene en cuenta el error en la orientación y el error lateral para reducir la 
     * consigna de velocidad. 
     * @return
     */
    public double calculaConsignaVel(){
        double consigna = 0;
        double velocidadMax = 2.5;
        double VelocidadMinima = 1;
        double refVelocidad;
        double errorOrientacion;      
        double errorLateral;
        int indMin = UtilCalculos.indiceMasCercano(Tr,modCoche.getX(),modCoche.getY());
        double dx = Tr[indMin][0]-modCoche.getX();
        double dy = Tr[indMin][1]-modCoche.getY();
        errorLateral = Math.sqrt(dx*dx + dy*dy);
//        errorOrientacion = cp.getOrientacionDeseada() - modCoche.getTita();
        errorOrientacion = Tr[indMin][2] - modCoche.getTita();
//        System.out.println("Error en la orientación "+errorOrientacion);
        if (Tr[indMin][3]>velocidadMax){
            refVelocidad = velocidadMax;
        }else
            refVelocidad = Tr[indMin][3]; 
        consigna = refVelocidad - Math.abs(errorOrientacion)*gananciaVel - Math.abs(errorLateral)*gananciaLateral;        
/*      Solo con esta condición el coche no se detiene nunca,aunque la referencia de la 
 * 		ruta sea cero*/
//        if (consigna <= 1){
//            consigna = 1;
        if (consigna <= VelocidadMinima && refVelocidad >= VelocidadMinima){
        /*Con esta condición se contempla el caso de que la consigna sea < 0*/
            consigna = VelocidadMinima;
        }else if (consigna <= VelocidadMinima && refVelocidad <= VelocidadMinima)
        /* De esta manera si la velocidad de la ruta disminuye hasta cero el coche se 
        detiene, en vez de seguir a velocidad mínima como ocurría antes. En este caso también
        está contemplado el caso de que la consigna sea < 0*/
        	consigna = refVelocidad;
        return consigna; 
     }
    /**
     * Recorre la trayectoria desde el punto más cercano al coche y mide la distancia de 
     * frenado. Devuelve el índice del punto que se encuentra a esa distancia
     * @param distFrenado Distancia en metros a la que se desea que el coche se detenga
     * @return Índice del punto que se encuentra a la distancia de frenado
     */
    public int buscaPuntoFrenado(double distFrenado){
    	int indCercano = UtilCalculos.indiceMasCercano(Tr, modCoche.getX(),modCoche.getY());
    	double dist = 0;
    	int i = 0;    	
    	for (i=indCercano;dist<distFrenado;i++){
    		double dx=Tr[i][0]-Tr[(i+1)%Tr.length][0];
            double dy=Tr[i][1]-Tr[(i+1)%Tr.length][1];
            dist = dist + Math.sqrt(dx*dx+dy*dy);
    	}
    	return i;
    }
    /**
     * Calcula la distancia a la que se encuentra el punto en el que se quiere que el coche
     * se detenga
     * @param puntoFrenado Índice del punto de la trayectoria donde se desea que el coche se pare
     * @return Distancia en metros a la que se encuentra el punto en el que se desea que el
     * coche se detenga
     */
    public double mideDistanciaFrenado(int puntoFrenado){
    	double distFrenado=0;
    	int indCercano = UtilCalculos.indiceMasCercano(Tr, modCoche.getX(),modCoche.getY());    	   	
    	for (int i=indCercano;i<puntoFrenado;i++){
    		double dx=Tr[i][0]-Tr[(i+1)%Tr.length][0];
            double dy=Tr[i][1]-Tr[(i+1)%Tr.length][1];
            distFrenado = distFrenado + Math.sqrt(dx*dx+dy*dy);
    	}
    	return distFrenado;
    }
    /**
     * calcula la rampa decreciente de consignas de velocidad para realizar el frenado
     * del coche
     * @param velocidadActual Velocidad instantanea en metros por segundo del coche 
     * @param distFrenado Distancia en metros a la que se desea que el coche se detenga
     * @param numPuntos cantidad de puntos del perfil. Coincidirá con el horizonte de predicción
     * en el caso de que el perfil de velocidad sea la entrada para el controlador predictivo
     * @param T Periodo de muestreo del sistema
     * @return Perfil de velocidad de frenado
     */
    public double[] calculaPerfilVelocidad(double velocidadActual,double distFrenado,int numPuntos,double T){
    	double pendiente = -velocidadActual/distFrenado;
    	double c = -pendiente*distFrenado;
    	double t = T;
    	double[] perfilVelocidad= new double[numPuntos];
    	for(int i=0;i<numPuntos+1;i++){    		
    		perfilVelocidad[i] = pendiente*t + c;
    		t = t + T;
    	}    	
    	return perfilVelocidad;
    }
    public double calculaPerfilVelocidad(double velocidadActual,double distFrenado,double T){
    	double pendiente = -velocidadActual/distFrenado;
    	double c = -pendiente*distFrenado;
    	double consignaVelocidad = pendiente*T + c;       	
    	return consignaVelocidad;
    }
    
    
    /** Metodo para cargar una ruta de fichero */
    protected void CargarRuta() {
    	//necestamos leer archivo con la ruta
    	int devuelto = fc.showOpenDialog(ventanaPrincipal);
    	if (devuelto != JFileChooser.APPROVE_OPTION) {
    		//no se quiso seleccionar ruta
    		rutaEspacial=null;
    		pmp.setControlPyRuta(null, null);
            pmo.setMiraObstaculo(null);
            pmoS.setMiraObstaculo(null);
            actNavegar.setEnabled(false);
            jcbUsarRF.setEnabled(false);

    		return;
    	}

    	conGPS.loadRuta(fc.getSelectedFile().getAbsolutePath());
    	if((rutaEspacial=conGPS.getRutaEspacial())==null) {
    		JOptionPane.showMessageDialog(ventanaPrincipal,
    				"No se cargó ruta adecuadamente de ese fichero",
    				"Error",
    				JOptionPane.ERROR_MESSAGE);
    		return;
    	}
    	//tenemos ruta != null
        desMag = rutaEspacial.getDesviacionM();
        System.out.println("Usando desviación magnética " + Math.toDegrees(desMag));

        // MOstrar coodenadas del centro del sistema local
        centroToTr = rutaEspacial.getCentro();
        System.out.println("centro de la Ruta Espacial " + centroToTr);
        //Rellenamos la trayectoria con la nueva versión de toTr,que 
        //introduce puntos en la trayectoria de manera que la separación
        //entre dos puntos nunca sea mayor de la distMax
        Tr = rutaEspacial.toTr(distMaxTr);


        System.out.println("Longitud de la trayectoria=" + Tr.length);

        mi = new MiraObstaculo(Tr,rutaEspacial.esRutaCerrada());
//        mi.nuevaPosicion(); 
        pmo.setMiraObstaculo(mi);
        pmoS.setMiraObstaculo(mi);
        //Inicializamos modelos predictivos
        cp = new ControlPredictivo(modCoche, Tr, 13, 4, 2.0, (double) periodoMuestreoMili / 1000);
        pmp.setControlPyRuta(cp, rutaEspacial); //nuevos valores en panel predictivo
        actNavegar.setEnabled(true);
        jcbUsarRF.setEnabled(true);
    }
    
    /** Metodo para terminar la ejecución */
    protected void Terminar() {
    	//TODO dejar todo en estado adecuado, desfrenar, etc. Apagar RF
		System.exit(0);
	}

    public void actionPerformed(ActionEvent e) {
		if(e.getSource() == spGananciaVel){
			gananciaVel = spGananciaVel.getNumber().doubleValue();
		}		
//		if (e.getSource() == jcbUsarRF){
//			if(!jcbUsarRF.isSelected()){
//				//Cuando se desactiva la checkbox del rangeFinder la distancia se
//				//se pone al máximo.
//				distRF = 80;
//			}
//		}
		if(e.getSource()==miSalir) {
			Terminar();
		}
		if(e.getSource()==miCargar) {
			CargarRuta();
		}
//		SacaDimensiones();
	}

    /** Método que ejecuta cada {@link #periodoMuestreoMili} bucle de control del coche mirando los obstáculos con el RF 
     */
    public void camina() {
        double velocidadActual;        
        Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
        long tSig;
        double consignaVelAnt = 0;
        while (true) {
            tSig = System.currentTimeMillis() + periodoMuestreoMili;
            //Calculamos el comando            	
            GPSData pa = conGPS.getPuntoActualTemporal();
            double[] ptoAct=null;
            double angAct=Double.NaN;
            if(pa!=null) {
             ptoAct= new double[2];
             ptoAct[0]=pa.getXLocal(); ptoAct[1]=pa.getYLocal();
             angAct = Math.toRadians(pa.getAngulosIMU().getYaw()) + desMag;
            }
            if (navegando && ptoAct!=null) { //sólo se debe activar si hay ruta 

                double volante = contCarro.getAnguloVolante();
                // Con esta linea realimentamos la información de los sensores al modelo
                // se puede incluir tambien la posicion del volante añadiendo el parámetro
                // volante a la invocación de setPostura
                //TODO Realimentar posición del volante
                modCoche.setPostura(ptoAct[0], ptoAct[1], angAct);
                double comandoVolante = cp.calculaComando();                
                if (comandoVolante > COTA_ANGULO) {
                    comandoVolante = COTA_ANGULO;
                }
                if (comandoVolante < -COTA_ANGULO) {
                    comandoVolante = -COTA_ANGULO;
                //System.out.println("Comando " + comandoVolante);
                }
                double consignaVelocidad;
                velocidadActual = contCarro.getVelocidadMS();
                //Cuando está casi parado no tocamos el volante
                if (velocidadActual >= umbralMinimaVelocidad)
                	contCarro.setAnguloVolante(-comandoVolante);
                
            	consignaVelocidad = calculaConsignaVel(); 
		System.out.println("Consigna de calcula: "+consignaVelocidad);
            	//Si se pulsa la checkbox de frenar
                if (puntoFrenado!=-1){
            		double distFrenado = mideDistanciaFrenado(puntoFrenado);
            		double velRampa=distFrenado*pendienteFrenado;
            		// Nos quedamos con la velocidad menor, la más restrictiva
            		consignaVelocidad=Math.min(consignaVelocidad, velRampa);
            		System.out.println("Punto frenado a "+distFrenado+" vel. rampa "+ velRampa);
                } 
                if (jcbUsarRF.isSelected() && !Double.isNaN(distRF) 
			&&(distRF <= distanciaSeguridad)){
                	// Si el RF detecta un obstáculo a menos de la dist de seguridad
                	double velRampa = (distRF-margenColision)*pendienteFrenado;
                	consignaVelocidad=Math.min(consignaVelocidad, velRampa);
                }
                if (consignaVelocidad-consignaVelAnt >=0.1){
                	consignaVelocidad = consignaVelAnt + 0.1;
                	System.out.println("Demasiado incremento en la consigna");
                }
                System.out.println(consignaVelocidad);
                consignaVelAnt = consignaVelocidad;
                contCarro.setConsignaAvanceMS(consignaVelocidad);			
                modCoche.calculaEvolucion(comandoVolante, velocidadActual, periodoMuestreoMili / 1000);
            } else {
            	//Le decimos almenos donde está el coche
            	if(ptoAct!=null) pmp.situaCoche(ptoAct[0], ptoAct[1], angAct);
            }
            pmp.actualiza();

            //esperamos hasta que hayan pasado miliSeg de ciclo.
            long msSobra = tSig - System.currentTimeMillis();
            if (msSobra < 0) {
                System.out.println("Sobra=" + msSobra);
            }
            while (System.currentTimeMillis() < tSig) {
                try {
                    Thread.sleep(tSig - System.currentTimeMillis());
                } catch (Exception e) {
                }
            }
        }

    }

    /**
     * @param args Seriales para IMU, GPS, RF y Carro. Si no se pasan de piden interactivamente.
     */
    public static void main(String[] args) {
        String[] puertos=null;
        if (args == null || args.length < 3) {
            //no se han pasado argumentos, pedimos los puertos interactivamente
            String[] titulos = {"GPS", "IMU", "RF", "Coche"};
            puertos = new EligeSerial(titulos).getPuertos();
            if (puertos == null) {
                System.err.println("No se asignaron los puertos seriales");
                System.exit(1);
            }
        } else {
            puertos = args;
        }
        NavegaPredictivo na = new NavegaPredictivo(puertos);
        na.camina();
    }


}
