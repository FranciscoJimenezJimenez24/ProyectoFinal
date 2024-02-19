package jimenezfranciscoProyectoFinal;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Servidor {

	private static final Logger LOGGER = Logger.getLogger(Servidor.class.getName());
	
	private static String url="jdbc:mysql://localhost/gestionCuentas";
	private static String usuario="root";
	private static String clave="1234";
	private static Connection conexion;
	private static int id_usuario=0;

    public void main(String[] args) {
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
    
    private boolean retirarDinero(int dinero,int id_cuenta) {
    	String sql="UPDATE cuentas SET saldo=saldo-? WHERE id_cuenta=?";
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
    
    private void modificateMovimientos(int dinero,int id_cuenta) {
    	String sqlNumMov="UPDATE cuentas SET num_movimientos=num_movimientos+1 WHERE id_cuenta=?";
		try (PreparedStatement statementNumMov=conexion.prepareStatement(sqlNumMov)){
			statementNumMov.setInt(1, id_cuenta);
			statementNumMov.executeUpdate();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String sqlMov="INSERT INTO movimientos (dinero_movido,fecha,id_cuenta) VALUES (?,?,?)";
		LocalDateTime now =LocalDateTime.now();
		Timestamp timestamp = Timestamp.valueOf(now);
		try (PreparedStatement statementMov=conexion.prepareStatement(sqlMov)){
			if (dinero>=0) {
				statementMov.setInt(1, dinero);
			}else {
				statementMov.setInt(1, -dinero);
			}
			statementMov.setTimestamp(2, timestamp);
			statementMov.setInt(3, id_cuenta);
			statementMov.executeUpdate();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    private String showMovimientos(int id_cuenta){
    	String sql="SELECT * FROM movimientos"
    	return "";
    }
    
    private void checkAuthetication(Socket socket) {
    	String usuario="";
		try {
			usuario = new String(socket.getInputStream().readAllBytes()).trim();
			PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
	        if (getUsuario(usuario)) {
	            writer.println("+OK user");
	            boolean check=false;
	            while (!check) {
	            	String contrasena = new String(socket.getInputStream().readAllBytes()).trim();
	            	if (checkContrasena(usuario,contrasena)) {
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
        String opcion = new String(socket.getInputStream().readAllBytes()).trim();
        LOGGER.log(Level.FINE, "El servidor recibe el comando");
        opcion = opcion.toLowerCase();
        PrintWriter writer = new PrintWriter(socket.getOutputStream(), true);
        Pattern pattern = Pattern.compile("<(\\d+)>\\s+(\\d+)");
        Matcher matcher = pattern.matcher(opcion);
        if (opcion.startsWith("ing")) {
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
            int numero = Integer.parseInt(numeroStr);
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
    	String sql="SELECT * FROM usuarios WHERE usuario=?, contrasena=?";
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
    
    

}
