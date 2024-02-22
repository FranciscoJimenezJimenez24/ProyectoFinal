package jimenezfranciscoProyectoFinal;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class prueba {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String opcion = "mov 1";
	    opcion = opcion.toLowerCase();

	    if (opcion.startsWith("mov")) {
	    	String numeroStr = opcion.substring(4).trim();  // Obtener la subcadena a partir del segundo carácter
            int id_cuenta = Integer.parseInt(numeroStr);
            System.out.println(id_cuenta);
	    }
	}

}
