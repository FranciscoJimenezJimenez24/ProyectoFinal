package jimenezfranciscoProyectoFinal;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.crypto.Cipher;

public class Servidor {

	private static final Logger LOGGER = Logger.getLogger(Servidor.class.getName());
	
	private static String url="jdbc:mysql://localhost/gestionCuentas";
	private static String usuario="root";
	private static String clave="1234";
	private static Connection conexion;
	private static int id_usuario=0;
	
	static {
		try {
			conexion=DriverManager.getConnection(url,usuario,clave);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

    public void main() {
        LOGGER.setLevel(Level.ALL);

        try {
            FileHandler fileHandler = new FileHandler("logger.log", true);
            fileHandler.setFormatter(new SimpleFormatter());
            LOGGER.addHandler(fileHandler);
        } catch (IOException e) {
            e.printStackTrace();
        }

        try {
            while (true) {
            	System.out.println("El servidor esperando....");
            	LOGGER.log(Level.FINE, "El servidor esperando");
                ServerSocket serverSocket = new ServerSocket(2026);
                Socket socket = serverSocket.accept();
                checkAuthetication(socket);
                opciones(socket);

                socket.close();
                serverSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private boolean ingresarDinero(int dinero,int id_cuenta) {
    	String sql="UPDATE cuentas SET saldo=saldo+? WHERE id_cuenta=?";
    	try (PreparedStatement statement=conexion.prepareStatement(sql)){
    		statement.setInt(1, dinero);
    		statement.setInt(2, id_cuenta);
    		if (statement.executeUpdate()==1) {
    			modificateMovimientos(dinero,id_cuenta);
    			return true;
    		}
    		statement.close();
    	} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
    	}
    	return false;
    }
    
    private boolean retirarDinero(int dinero,int id_cuenta) {
    	String sql="UPDATE cuentas SET saldo=saldo-? WHERE id_cuenta=?";
    	try (PreparedStatement statement=conexion.prepareStatement(sql)){
    		statement.setInt(1, dinero);
    		statement.setInt(2, id_cuenta);
    		if (statement.executeUpdate()==1) {
    			modificateMovimientos(-dinero,id_cuenta);
    			return true;
    		}
    		statement.close();
    	} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
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
			e.printStackTrace();
		}
    }
    
    private ArrayList<String> showMovimientos(int id_cuenta){
    	String sql="SELECT * FROM (SELECT * FROM movimientos WHERE id_cuenta=? ORDER BY fecha DESC LIMIT 15) sub ORDER BY fecha ASC";
    	int id_movimiento;
    	double dinero_movido;
    	Timestamp timestamp;
    	ArrayList<String> listaMovimientos=new ArrayList<String>();
    	try (PreparedStatement statement=conexion.prepareStatement(sql)){
    		statement.setInt(1,id_cuenta);
    		ResultSet result=statement.executeQuery();
    		if (result.next()) {
    			while (result.next()) {
        			id_movimiento=result.getInt("id_movimiento");
        			dinero_movido=result.getDouble("dinero_movido");
        			timestamp=result.getTimestamp("fecha");
        			listaMovimientos.add("Id: "+id_movimiento+", Dinero movido: "+dinero_movido+", Fecha: "+timestamp+", Cuenta: "+id_cuenta);
        		}
    		}
    	} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	return listaMovimientos;
    }
    
    private boolean sendFicheroMovimientos(int id_cuenta) {
    	String sql="SELECT * FROM movimientos where id_cuenta=?";
    	int id_movimiento;
    	double dinero_movido;
    	Timestamp timestamp;
    	ArrayList<String> listaMovimientos=new ArrayList<String>();
    	try (PreparedStatement statement=conexion.prepareStatement(sql)){
    		statement.setInt(1,id_cuenta);
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
			e.printStackTrace();
		}
    	System.out.println(listaMovimientos.size());
    	File f=new File("fichero.txt");
    	try {
			FileWriter writer=new FileWriter(f);
			for (String movimiento : listaMovimientos) {
				writer.write(movimiento+"\n");
			}
			writer.close();
			return true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	return false;
    }
    
    private void checkAuthetication(Socket socket) {
		try {
			BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
	        PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);

	        // Leer el usuario
	        String usuario = reader.readLine().trim();
	        if (getUsuario(usuario)) {
	            writer.println("+OK user");
	            boolean check=false;
	            while (!check) {
	            	String contrasena = reader.readLine().trim();
	            	if (checkContrasena(contrasena,usuario)) {
	            		check=true;
	            		writer.println("+OK pass");
	            	}else { 
	            		writer.println("ERR pass");
	            	}
	            }
	        }else {
	        	writer.println("ERR user");
	        	boolean check=false;
	        	while (!check) {
	            	String user = new String(socket.getInputStream().readAllBytes()).trim();
	            	if (getUsuario(user)) {
	            		check=true;
	            		writer.println("+OK user");
	            	}else {
	            		writer.println("ERR user");
	            	}
	            	
	            }
	        }
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        id_usuario=getIdUsuario(usuario,socket);
    }
 
    private void opciones(Socket socket) throws IOException {
    	BufferedReader reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
        PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
        
        String opcion="";
        while (opcion!="quit") {
        opcion = reader.readLine().trim();
        LOGGER.log(Level.FINE, "El servidor recibe el comando");
        opcion = opcion.toLowerCase();
        
        
        	if (opcion.startsWith("ing")) {
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
                    	writer.println("ERR");
                    }
                }
            }else if (opcion.startsWith("ret")) {
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
                    	writer.println("ERR"); 
                    }
                }
            }else if (opcion.startsWith("mov")) {
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
                	writer.println("No hay ningún movimiento en esta cuenta");
                }
            }else if (opcion.startsWith("sendmov")) {
            	String numeroStr = opcion.substring(8).trim();
            	int id_cuenta = Integer.parseInt(numeroStr);
            	if (sendFicheroMovimientos(id_cuenta)) {
            		KeyPair key=generarClave();
            		PublicKey publicKey=obtenerClavePublica(key);
            		try {
    					cifrarArchivo("fichero.txt","movimientosEncriptados.txt",publicKey);
    				} catch (Exception e) {
    					// TODO Auto-generated catch block
    					e.printStackTrace();
    				}
            		String filePath = "movimientosEncriptados.txt";
                    File file = new File(filePath);
                    String fileName = file.getName();
                    writer.println(fileName);

                    // Enviar el contenido del archivo
                    FileInputStream fileInputStream = new FileInputStream(file);
                    byte[] buffer = new byte[1024];
                    int bytesRead;
                    while ((bytesRead = fileInputStream.read(buffer)) != -1) {
                    	String line = new String(buffer, 0, bytesRead);
                        writer.println(line);
                    }
            		writer.println("Se envio el fichero");
            	}else {
            		writer.println("ERR"); 
            	}
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
			e.printStackTrace();
		}
    	return id_usuario;
    }
    
    public KeyPair generarClave() {
		KeyPairGenerator generator;
		try {
			generator = KeyPairGenerator.getInstance("RSA");
			generator.initialize(8192);
			KeyPair pair = generator.generateKeyPair();
			return pair;
		} catch (NoSuchAlgorithmException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}
    
  	//obtiene la clave publica
  	private PublicKey obtenerClavePublica(KeyPair pair) {
  		PublicKey publicKey = pair.getPublic();
  		return publicKey;
  	}
    
  	private void cifrarArchivo(String fichero, String ficheroEncriptado, PublicKey publicKey) throws Exception {
        byte[] contenido = leerFichero(fichero);
        byte[] contenidoCifrado = cifrarConClavePublica(contenido, publicKey);
        escribirFichero(ficheroEncriptado, contenidoCifrado);
    }
  	
  	private void escribirFichero(String fichero, byte[] contenido) throws Exception {
        try (FileOutputStream f = new FileOutputStream(fichero)) {
            f.write(contenido);
        }
    }
  	
 // Cifrar con la clave pública
    private byte[] cifrarConClavePublica(byte[] contenido, PublicKey clavePublica) throws Exception {
        Cipher cifrador = Cipher.getInstance("RSA");
        cifrador.init(Cipher.ENCRYPT_MODE, clavePublica);
        return cifrador.doFinal(contenido);
    }
    
 // Leer contenido de un fichero y devolverlo como un array de bytes
    private byte[] leerFichero(String fichero) throws Exception {
        File archivo = new File(fichero);
        byte[] contenido = new byte[(int) archivo.length()];

        try (FileInputStream f = new FileInputStream(archivo)) {
            f.read(contenido);
        }

        return contenido;
    }
    

}
