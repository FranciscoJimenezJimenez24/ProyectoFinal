package jimenezfranciscoProyectoFinal;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.InputStream;
import java.io.PrintWriter;

import java.net.Socket;
import java.security.InvalidKeyException;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.NoSuchPaddingException;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.SSLServerSocket;

import java.util.concurrent.Semaphore;


public class Servidor {

	private static final Logger LOGGER = Logger.getLogger(Servidor.class.getName());
	
	private static final int MAX_CLIENTES = 5;
    private static final Semaphore SEMAFORO = new Semaphore(MAX_CLIENTES);
	private static String url="jdbc:mysql://143.47.48.57:3306/gestionCuentas";
	private static String usuario="paquito";
	private static String clave="Paco@1234";
	private static Connection conexion;
	private static int id_usuario=0;
	private static ExecutorService executor;
	private static boolean user=false;
	private static boolean password=false;
	private static boolean cuenta=false;
	private static boolean yourCount=false;
	
	static {
		try {
			conexion=DriverManager.getConnection(url,usuario,clave);
			executor = Executors.newCachedThreadPool();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			LOGGER.log(Level.SEVERE, "Error al establecer la conexión con la base de datos", e);
		}
	}
	
	private void logger() {
	 	try {
	        FileHandler fileHandler = new FileHandler("logger.log", true);
	        fileHandler.setFormatter(new SimpleFormatter());
	        LOGGER.addHandler(fileHandler);
        } catch (IOException e) {
        	LOGGER.log(Level.SEVERE, "Error al configurar el manejador de archivos del logger", e);
        }
	}
	
    public void main() throws InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException {
        LOGGER.setLevel(Level.ALL);
        logger();
       
        SSLContext sslContext = getSSLContext();

        SSLServerSocketFactory sslServerSocketFactory = sslContext.getServerSocketFactory();
        SSLServerSocket serverSocket = null; // Declarar fuera del bloque try
        try {
            serverSocket = (SSLServerSocket) sslServerSocketFactory.createServerSocket(2026);
            System.out.println("El servidor esperando....");
            LOGGER.log(Level.FINE, "El servidor esperando");
            while (true) {
                SEMAFORO.acquire();
                Socket socket = serverSocket.accept();
                executor.submit(new Client(socket));
            }
        } catch (IOException e) {
        	LOGGER.log(Level.SEVERE, "Error de entrada/salida en el servidor", e);
        } catch (InterruptedException e) {
        	LOGGER.log(Level.SEVERE, "Hilo interrumpido en el servidor", e);
        } finally {
            if (serverSocket != null) {
                try {
                    serverSocket.close();
                } catch (IOException e) {
                	LOGGER.log(Level.SEVERE, "Error al cerrar el socket del servidor", e);
                }
            }
        }
    }

	private SSLContext getSSLContext() {
		SSLContext sslContext;
        try {
            sslContext = SSLContext.getInstance("TLS");
            KeyManagerFactory keyManagerFactory = KeyManagerFactory.getInstance(KeyManagerFactory.getDefaultAlgorithm());
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());

            // Cargar el almacén de claves y el almacén de confianza (truststore) con tus certificados
            char[] keystorePassword = "llaves".toCharArray();
            char[] truststorePassword = "confio".toCharArray();
            KeyStore keyStore = KeyStore.getInstance("JKS");
            KeyStore trustStore = KeyStore.getInstance("JKS");

            try (InputStream keyStoreStream = new FileInputStream("llaves.jks");
                 InputStream trustStoreStream = new FileInputStream("confio.jks")) {
                keyStore.load(keyStoreStream, keystorePassword);
                trustStore.load(trustStoreStream, truststorePassword);
            }

            keyManagerFactory.init(keyStore, keystorePassword);
            trustManagerFactory.init(trustStore);

            sslContext.init(keyManagerFactory.getKeyManagers(), trustManagerFactory.getTrustManagers(), null);
        } catch (NoSuchAlgorithmException | KeyStoreException | CertificateException | IOException | UnrecoverableKeyException | KeyManagementException e) {
        	LOGGER.log(Level.SEVERE, "Error al configurar el contexto SSL", e);
            return null;
        }
		return sslContext;
	}
    
    private class Client implements Runnable {
        private Socket socket;

        public Client(Socket socket) {
            this.socket = socket;
        }

        @Override
        public void run() {
            try {
                checkAuthetication(socket);
                opciones(socket);
            } catch (IOException | NoSuchAlgorithmException | NoSuchPaddingException | InvalidKeyException  e ) {
            	LOGGER.log(Level.SEVERE, "Error en la ejecución del cliente", e);
            } catch (Exception ex) {
            	ex.printStackTrace();
            } finally {
            	SEMAFORO.release();
                try {
                    socket.close();
                } catch (IOException e) {
                	LOGGER.log(Level.SEVERE, "Error al cerrar el socket del cliente", e);
                }
            }
        }
    }

    private boolean ingresarDinero(int dinero,int id_cuenta) {
    	cuenta=countExist(id_cuenta);
        yourCount=isYourCount(id_cuenta);
    	String sql="UPDATE cuentas SET saldo=saldo+? WHERE id_cuenta=?";
    	if (yourCount) {
    		try (PreparedStatement statement=conexion.prepareStatement(sql)){
        		statement.setInt(1, dinero);
        		statement.setInt(2, id_cuenta);
        		if (statement.executeUpdate()==1) {
        			LOGGER.log(Level.INFO, "Se ingresó correctamente el dinero en la cuenta con ID: " + id_cuenta);
        			modificateMovimientos(dinero,id_cuenta);
        			return true;
        		}
        		statement.close();
        	} catch (SQLException e) {
    			// TODO Auto-generated catch block
        		LOGGER.log(Level.SEVERE, "Error al intentar ingresar dinero en la cuenta con ID: " + id_cuenta, e);
    			e.printStackTrace();
        	}
    	}
    	
    	return false;
    }
    
    private boolean retirarDinero(int dinero,int id_cuenta) {
    	cuenta=countExist(id_cuenta);
        yourCount=isYourCount(id_cuenta);
    	String sql="UPDATE cuentas SET saldo=saldo-? WHERE id_cuenta=?";
    	if (yourCount) {
    		try (PreparedStatement statement=conexion.prepareStatement(sql)){
        		statement.setInt(1, dinero);
        		statement.setInt(2, id_cuenta);
        		if (statement.executeUpdate()==1) {
        			LOGGER.log(Level.INFO, "Se retiró correctamente el dinero de la cuenta con ID: " + id_cuenta);
        			modificateMovimientos(-dinero,id_cuenta);
        			return true;
        		}
        		statement.close();
        	} catch (SQLException e) {
    			// TODO Auto-generated catch block
        		LOGGER.log(Level.SEVERE, "Error al intentar retirar dinero de la cuenta con ID: " + id_cuenta, e);
    			e.printStackTrace();
        	}
    	}
    	return false;
    }
    
    private void modificateMovimientos(int dinero,int id_cuenta) {
    	String sqlNumMov="UPDATE cuentas SET num_movimientos=num_movimientos+1 WHERE id_cuenta=?";
		try (PreparedStatement statementNumMov=conexion.prepareStatement(sqlNumMov)){
			statementNumMov.setInt(1, id_cuenta);
			statementNumMov.executeUpdate();
			statementNumMov.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			LOGGER.log(Level.SEVERE, "Error al intentar modificar los movimientos de la cuenta con ID: " + id_cuenta, e);
			e.printStackTrace();
		}
		String sqlMov="INSERT INTO movimientos (dinero_movido,fecha,id_cuenta) VALUES (?,?,?)";
		LocalDateTime now =LocalDateTime.now();
		Timestamp timestamp = Timestamp.valueOf(now);
		try (PreparedStatement statementMov=conexion.prepareStatement(sqlMov)){
			statementMov.setInt(1, dinero);
			statementMov.setTimestamp(2, timestamp);
			statementMov.setInt(3, id_cuenta);
			statementMov.executeUpdate();
			statementMov.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			LOGGER.log(Level.SEVERE, "Error al intentar insertar un movimiento en la cuenta con ID: " + id_cuenta, e);
			e.printStackTrace();
		}
    }
    
    private boolean countExist(int id_cuenta) {
    	String sqlCuenta="SELECT id_cuenta FROM cuentas WHERE id_cuenta=?";
    	try (PreparedStatement statement=conexion.prepareStatement(sqlCuenta)){
    		statement.setInt(1, id_cuenta);
    		 ResultSet result = statement.executeQuery();
             if (result.next()) {
            	 return true;
             }
    	} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	return false;
    }
    
    private boolean isYourCount(int id_cuenta) {
    	String sqlCuenta="SELECT id_cuenta FROM cuentas WHERE id_cuenta=? AND id_usuario=?";
    	try (PreparedStatement statement=conexion.prepareStatement(sqlCuenta)){
    		statement.setInt(1, id_cuenta);
    		statement.setInt(2, id_usuario);
    		 ResultSet result = statement.executeQuery();
             if (result.next()) {
            	 return true;
             }
    	} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	return false;
    }
    
    private ArrayList<String> showMovimientos(int id_cuenta) {
        String sql = "SELECT * FROM (SELECT * FROM movimientos WHERE id_cuenta=? ORDER BY fecha DESC LIMIT 10) sub ORDER BY fecha ASC";
        cuenta=countExist(id_cuenta);
        yourCount=isYourCount(id_cuenta);
        int id_movimiento;
        double dinero_movido;
        Timestamp timestamp;
        
        ArrayList<String> listaMovimientos = new ArrayList<String>();
        if (yourCount) {
        	try (PreparedStatement statement = conexion.prepareStatement(sql)) {
                statement.setInt(1, id_cuenta);
                ResultSet result = statement.executeQuery();
                if (result.next()) {
                	while (result.next()) {
                        id_movimiento = result.getInt("id_movimiento");
                        dinero_movido = result.getDouble("dinero_movido");
                        timestamp = result.getTimestamp("fecha");
                        listaMovimientos.add("Id: " + id_movimiento + ", Dinero movido: " + dinero_movido + ", Fecha: " + timestamp + ", Cuenta: " + id_cuenta);
                    }
                }
            } catch (SQLException e) {
                LOGGER.log(Level.SEVERE, "Error al intentar obtener los movimientos de la cuenta con ID: " + id_cuenta, e);
                e.printStackTrace();
            }
        }
        
        return listaMovimientos;
    }
    
    private boolean sendMovimientos(int id_cuenta) {
    	cuenta=countExist(id_cuenta);
        yourCount=isYourCount(id_cuenta);
        if (yourCount) {
        	String sql="SELECT * FROM movimientos where id_cuenta=?";
        	int id_movimiento;
        	double dinero_movido;
        	Timestamp timestamp;
        	ArrayList<String> listaMovimientos=new ArrayList<String>();
        	try (PreparedStatement statement=conexion.prepareStatement(sql)){
        		statement.setLong(1,id_cuenta);
        		ResultSet result=statement.executeQuery();
    			if (result.next()) {
    				while (result.next()) {
    					id_movimiento=result.getInt("id_movimiento");
    	    			dinero_movido=result.getDouble("dinero_movido");
    	    			timestamp=result.getTimestamp("fecha");
    	    			listaMovimientos.add("Id: "+id_movimiento+", Dinero movido: "+dinero_movido+", Fecha: "+timestamp+", Cuenta: "+id_cuenta);
    				}
        		}else {
        			return false;
        		}
        	} catch (SQLException e) {
    			// TODO Auto-generated catch block
        		LOGGER.log(Level.SEVERE, "Error al intentar obtener los movimientos para enviar el fichero de la cuenta con ID: " + id_cuenta, e);
    			e.printStackTrace();
    		}
        	File f=new File("fichero.txt");
        	try {
    			FileWriter writer=new FileWriter(f);
    			for (String movimiento : listaMovimientos) {
    				writer.write(movimiento+"\n");
    			}
    			writer.close();
    			LOGGER.log(Level.INFO, "Se creó correctamente el fichero de movimientos para la cuenta con ID: " + id_cuenta);
    			return true;
    		} catch (IOException e) {
    			// TODO Auto-generated catch block
    			LOGGER.log(Level.SEVERE, "Error al intentar crear el fichero de movimientos para la cuenta con ID: " + id_cuenta, e);
    			e.printStackTrace();
    		}
        }
    	return false;
    }
    
    private void checkAuthetication(Socket socket) {
    	String usuario = "";
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
	        PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
	        while (!getUsuario(usuario)) {
	        	usuario=reader.readLine().trim();
	        	if (getUsuario(usuario)) {
	        		writer.println("+OK user");
	            	String contrasena = reader.readLine().trim();
	            	if (checkContrasena(contrasena,usuario)) {
	            		writer.println("+OK pass");
	            		if (contrasena.equalsIgnoreCase("contrasena1")) {
	            			password=true;
	            		}else {
	            			password=false;
	            		}
	            	}else { 
	            		usuario="";
	            		writer.println("ERR pass");
	            	}
	            	
	        	}else {
	        		writer.println("ERR user");
	        	}
	            
	        }
		} catch (IOException e) {
			// TODO Auto-generated catch block
			LOGGER.log(Level.SEVERE, "Error de entrada/salida al autenticar el usuario", e);
			e.printStackTrace();
		}
		if (usuario.equalsIgnoreCase("usuario1")) {
			user=true;
		}else {
			user=false;
		}
        id_usuario=getIdUsuario(usuario,socket);
    }
    
    private void options(Socket socket,String opcion,PrintWriter writer) {
    	if (opcion.startsWith("ing")) {
    		ing(socket,opcion,writer);
    	}else if (opcion.startsWith("ret")) {
    		ret(socket,opcion,writer);
    	}else if (opcion.startsWith("mov")) {
    		mov(opcion,writer);
    	}else if (opcion.startsWith("sendmov")) {
    		sendmov(socket,opcion,writer);
    	}else if (opcion.equalsIgnoreCase("seecounts") && user && password) {
    		writer.println(mostrarDineroActual());
    	}else if (opcion.equalsIgnoreCase("quit")) {
        	writer.println("Hasta luego"); 
        }else {
        	writer.println("Ese comando es incorrecto"); 
        }
    }
    
    private void sendmov(Socket socket,String opcion,PrintWriter writer) {
    	String numeroStr = opcion.substring(8).trim();
    	int id_cuenta = Integer.parseInt(numeroStr);
    	if (sendMovimientos(id_cuenta)) {
    		try {
    		    OutputStream outputStream = socket.getOutputStream();
    		    String nombreArchivo = "fichero.txt";
    		    File archivo = new File(nombreArchivo);
    		    BufferedReader br = new BufferedReader(new FileReader(archivo));
    		    String linea;
    		    while ((linea = br.readLine()) != null) {
    		        outputStream.write(linea.getBytes());
    		        outputStream.write("\n".getBytes());
    		    }
    		    writer.println("FIN_MOVIMIENTOS");
    		    br.close();
    		    outputStream.flush();
    		} catch (Exception e) {
    		    LOGGER.log(Level.SEVERE, "Error al leer el fichero de movimientos", e);
    		    e.printStackTrace();
    		}
    		
    	}else {
    		if (!yourCount && cuenta) {
        		writer.println("No tiene acceso a esa cuenta");
        	}else if (!cuenta){
        		writer.println("Esa cuenta no existe");
        	}
    	}
    }
    	
    
    private void mov(String opcion,PrintWriter writer) {
    	String numeroStr = opcion.substring(4).trim();  // Obtener la subcadena a partir del segundo carácter
        int id_cuenta = Integer.parseInt(numeroStr);
        ArrayList<String> listaMovimientos=showMovimientos(id_cuenta);
        String movimientos="";
        if (listaMovimientos.size()>0) {
        	for (String movimiento : listaMovimientos) {
				movimientos+=movimiento+"\n";
			}
        	writer.println(movimientos);
        }else {
        	if (!yourCount && cuenta) {
        		writer.println("No tiene acceso a esa cuenta");
        	}else if (!cuenta){
        		writer.println("Esa cuenta no existe");
        	}else {
        		writer.println("No hay ningún movimiento en esta cuenta");
        	}
        	
        }
    }
    
    private void ret(Socket socket,String opcion,PrintWriter writer) {
    	Pattern pattern = Pattern.compile("<(\\d+)>\\s+(\\d+)");
        Matcher matcher = pattern.matcher(opcion);
        if (matcher.find()) {
            String valor1 = matcher.group(1);
            String valor2 = matcher.group(2);

            int dinero = Integer.parseInt(valor1);
            int id_cuenta = Integer.parseInt(valor2);

            if (retirarDinero(dinero, id_cuenta)) {
            	writer.println("+OK Retirada dinero");
            }else {
            	if (!yourCount && cuenta) {
            		writer.println("No tiene acceso a esa cuenta");
            	}else if (!cuenta){
            		writer.println("Esa cuenta no existe");
            	}
            }
        }else {
        	writer.println("Comando incorrecto");
        }
    }
    
    private void ing(Socket socket,String opcion, PrintWriter writer) {
    	Pattern pattern = Pattern.compile("<(\\d+)>\\s+(\\d+)");
        Matcher matcher = pattern.matcher(opcion);
        if (matcher.find()) {
            String valor1 = matcher.group(1);
            String valor2 = matcher.group(2);

            int dinero = Integer.parseInt(valor1);
            int id_cuenta = Integer.parseInt(valor2);

            if (ingresarDinero(dinero, id_cuenta)) {
            	writer.println("+OK Ingreso dinero");
            }else {
            	if (!yourCount && cuenta) {
            		writer.println("No tiene acceso a esa cuenta");
            	}else if (!cuenta){
            		writer.println("Esa cuenta no existe");
            	}
            }
        }else {
        	writer.println("Comando incorrecto");
        }
    }
 
    private void opciones(Socket socket) throws IOException, NoSuchAlgorithmException, NoSuchPaddingException, InvalidKeyException {
    	BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
        
        String opcion="";
        while (!opcion.equalsIgnoreCase("quit")) {
        	opcion = reader.readLine().trim();
        	try {
    		    LOGGER.log(Level.FINE, "El servidor recibe el comando");
    		    opcion = opcion.toLowerCase();
    		    options(socket,opcion,writer);
        	} catch (IllegalArgumentException e) {
        		writer.println("Ese comando es incorrecto"); 
        	}
		    
        }
    }
    
    private boolean getUsuario(String usuario) {
    	String sql="SELECT * FROM usuarios WHERE usuario=?";
    	try (PreparedStatement statement=conexion.prepareStatement(sql)){
    		statement.setString(1, usuario);
    		ResultSet result=statement.executeQuery();
    		if (result.next()) {
    			return true;
    		}
    		result.close();
    		statement.close();
    	} catch (SQLException e) {
			// TODO Auto-generated catch block
    		LOGGER.log(Level.SEVERE, "Error al obtener el usuario de la base de datos", e);
			e.printStackTrace();
		}
    	return false;
    }
    
    private boolean checkContrasena(String contrasena, String usuario) {
    	String sql="SELECT * FROM usuarios WHERE usuario=? AND contrasena=?";
    	try (PreparedStatement statement=conexion.prepareStatement(sql)){
    		statement.setString(1, usuario);
    		statement.setString(2, contrasena);
    		ResultSet result=statement.executeQuery();
    		if (result.next()) {
    			return true;
    		}
    		result.close();
    		statement.close();
    	} catch (SQLException e) {
			// TODO Auto-generated catch block
    		LOGGER.log(Level.SEVERE, "Error al comprobar la contraseña del usuario en la base de datos", e);
			e.printStackTrace();
		}
    	return false;
    }
    
    private int getIdUsuario(String usuario,Socket socket) {
    	String sql="SELECT id_usuario FROM usuarios WHERE usuario=?";
    	int id_usuario=0;
    	try (PreparedStatement statement=conexion.prepareStatement(sql)){
    		statement.setString(1, usuario);
    		ResultSet result=statement.executeQuery();
    		if (result.next()) {
    			id_usuario=result.getInt("id_usuario");
    		}
    		result.close();
    		statement.close();
    	} catch (SQLException e) {
			// TODO Auto-generated catch block
    		LOGGER.log(Level.SEVERE, "Error al obtener el ID del usuario de la base de datos", e);
			e.printStackTrace();
		}
    	return id_usuario;
    }
    
    public static String mostrarDineroActual() {
    	String sql="SELECT SUM(saldo) FROM cuentas";
    	String lista="";
    	try (PreparedStatement statement=conexion.prepareStatement(sql)){
    		ResultSet result=statement.executeQuery();
    		if (result.next()) {
    			int saldoTotal=result.getInt(1);
    			lista="El saldo total de las cuentas es de "+saldoTotal+"€\n";
    		}
    	} catch (SQLException e) {
			// TODO Auto-generated catch block
    		LOGGER.log(Level.SEVERE, "Error al obtener el saldo total de las cuentas de la base de datos", e);
			e.printStackTrace();
		}
    	return lista;
    }
    
}
