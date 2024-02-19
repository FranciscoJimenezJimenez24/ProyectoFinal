package jimenezfranciscoProyectoFinal;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class prueba {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		String opcion = "ing <300> 1";
	    opcion = opcion.toLowerCase();

	    if (opcion.startsWith("ing")) {
	        Pattern pattern = Pattern.compile("<(\\d+)>\\s+(\\d+)");
	        Matcher matcher = pattern.matcher(opcion);

	        if (matcher.find()) {
	            String valor1 = matcher.group(1);
	            String valor2 = matcher.group(2);

	            int numero1 = Integer.parseInt(valor1);
	            int numero2 = Integer.parseInt(valor2);
	            
	            System.out.println("Dinero: "+numero1+", cuenta: "+numero2);
	        }
	    }
	}

}
