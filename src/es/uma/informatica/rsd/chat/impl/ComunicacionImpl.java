package es.uma.informatica.rsd.chat.impl;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.MulticastSocket;
import java.net.NetworkInterface;
import java.net.SocketAddress;
import java.net.UnknownHostException;

import es.uma.informatica.rsd.chat.ifaces.Comunicacion;
import es.uma.informatica.rsd.chat.ifaces.Controlador;
import es.uma.informatica.rsd.chat.impl.DialogoPuerto.PuertoAlias;

/// Clase a implementar 
public class ComunicacionImpl implements Comunicacion {
	private Controlador con;
	private MulticastSocket socket;
	private InetAddress localIP;
	private PuertoAlias user;


	@Override
	public void crearSocket(PuertoAlias pa) {
		try {
			user = pa;
			socket = new MulticastSocket(new InetSocketAddress(InetAddress.getByName("0.0.0.0"), user.puerto));
			localIP = InetAddress.getByName("192.168.1.101");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void setControlador(Controlador c) {
		con = c;
	}

	@Override
	public void runReceptor() {
		while (true) {
			byte[] msg = new byte[10000];
			DatagramPacket packet = new DatagramPacket(msg, msg.length);
			try {
				socket.receive(packet);
			} catch (IOException e) {
				e.printStackTrace();
			}

			String content = "";
			try {
				content = new String(msg, "UTF-8");
			} catch (UnsupportedEncodingException e) {
				e.printStackTrace();
			}

			String[] message = content.split("!");
			try {
				if (!InetAddress.getByName(message[0]).isMulticastAddress()) {
					con.mostrarMensaje(new InetSocketAddress(packet.getAddress(), packet.getPort()), message[1], message[2]);
				} else if (!packet.getAddress().equals(localIP)) {
					con.mostrarMensaje(new InetSocketAddress(InetAddress.getByName(message[0]), packet.getPort()), message[1], message[2]);
				}
			} catch (UnknownHostException e) {
				e.printStackTrace();
			}

		}
	}

	@Override
	public void envia(InetSocketAddress sa, String mensaje) {

		mensaje = "!"+user.alias+"!"+mensaje;

		if (sa.getAddress().isMulticastAddress()) {
			mensaje = sa.getAddress().toString().split("/")[1]+ mensaje;
		}

		byte[] msg = null;
		try {
			msg = mensaje.getBytes("UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}


		try {
			socket.send(new DatagramPacket(msg, msg.length, sa.getAddress(), sa.getPort()));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void joinGroup(InetAddress multi) {
		try {
			socket.joinGroup(multi);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void leaveGroup(InetAddress multi) {
		try {
			socket.leaveGroup(multi);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
