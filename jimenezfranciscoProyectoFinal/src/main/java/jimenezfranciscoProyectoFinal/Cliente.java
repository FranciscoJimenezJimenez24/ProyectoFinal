package jimenezfranciscoProyectoFinal;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;

public class Cliente {

	public static void main(String[] args) {
		// TODO Auto-generated method stub
		SSLSocket socket_cliente = null;
        try {
            SSLContext context = SSLContext.getInstance("TLS");
            context.init(null, trustAllCertificates(), null);

            socket_cliente = (SSLSocket) context.getSocketFactory().createSocket("localhost", 2026);

            // Realizar operaciones con el socket_cliente aquí

        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (socket_cliente != null) {
                try {
                    socket_cliente.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
	}
	
	private static TrustManager[] trustAllCertificates() {
        return new TrustManager[]{
            new X509TrustManager() {
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return null;
                }

                public void checkClientTrusted(java.security.cert.X509Certificate[] certs, String authType) {
                }

                public void checkServerTrusted(java.security.cert.X509Certificate[] certs, String authType) {
                }
            }
        };
    }

}
