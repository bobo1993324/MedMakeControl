import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.Enumeration;

class Lib {
	public static String readFile(String path) throws IOException {
			BufferedReader in = new BufferedReader(new FileReader(path));
			String s;
			StringBuffer sb = new StringBuffer();
			while ((s = in.readLine()) != null)
				sb.append(s + "\n");
			in.close();
			return sb.toString(); 
	}

	public static void test40COMM(String s) {
		if(!open40COMM(s)){
			System.out.println("open Ports failed");
			return;
		}
		Run.openPort(1);
		try {
			Thread.sleep(1000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		Run.closePort(1);
		closeCOMMs();
	}
	
	private static boolean open40COMM(String s) {
		Enumeration<?> emunPort = CommPortIdentifier.getPortIdentifiers();
		while (emunPort.hasMoreElements()) {
			CommPortIdentifier pi = (CommPortIdentifier) emunPort.nextElement();
			try {
				CommPort commPort = pi.open("40PortsControl", 2000);
				if (commPort instanceof SerialPort) {
					SerialPort serialPort = (SerialPort) commPort;
					if (serialPort.getName().equals(Configure.COMM40)) {
						serialPort.setSerialPortParams(9600,
								SerialPort.DATABITS_8, SerialPort.STOPBITS_1,
								SerialPort.PARITY_NONE);
						MainFrame.fortyPortsOut = serialPort.getOutputStream();
						MainFrame.fortyPortsCP=commPort;
						return true;
					}
				}
				commPort.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	public static boolean openCOMMs() {
		Enumeration<?> emunPort = CommPortIdentifier.getPortIdentifiers();
		boolean findTmp = false, find40 = false;
		while (emunPort.hasMoreElements()) {
			CommPortIdentifier pi = (CommPortIdentifier) emunPort.nextElement();
			try {
				CommPort commPort = pi.open("40PortsControl", 2000);
				if (commPort instanceof SerialPort) {
					SerialPort serialPort = (SerialPort) commPort;
					if (serialPort.getName().equals(Configure.COMM40)) {
						serialPort.setSerialPortParams(9600,
								SerialPort.DATABITS_8, SerialPort.STOPBITS_1,
								SerialPort.PARITY_NONE);
						MainFrame.fortyPortsOut = serialPort.getOutputStream();
						MainFrame.fortyPortsCP=commPort;
						find40 = true;
					}
					if(serialPort.getName().equals(Configure.COMMT)){
						serialPort.setSerialPortParams(9600,
								SerialPort.DATABITS_8, SerialPort.STOPBITS_1,
								SerialPort.PARITY_NONE);
						MainFrame.tempCP=commPort;
						MainFrame.tempOut = serialPort.getOutputStream();
						MainFrame.tempIn=serialPort.getInputStream();
						findTmp = true;
					}
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		System.out.println(Configure.COMM40);
		System.out.println(findTmp + " "+ find40);
		if (find40) {
			return true;
		} else {
			closeCOMMs();
			return false;
		}
	}
	
	public static void closeCOMMs(){
		try{
		if(MainFrame.tempIn != null){
			MainFrame.tempIn.close();
			MainFrame.tempOut.close();
			MainFrame.tempCP.close();
		}
		if(MainFrame.fortyPortsOut != null){
			MainFrame.fortyPortsOut.close();
			MainFrame.fortyPortsCP.close();
		}
		}catch(IOException e ){
			e.printStackTrace();
		}
	
	}
	
}