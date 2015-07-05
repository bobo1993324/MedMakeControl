import gnu.io.CommPort;
import gnu.io.CommPortIdentifier;
import gnu.io.SerialPort;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.EventQueue;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Enumeration;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

public class MainFrame {

	public static int debugLevel = 0;
	public JFrame frmportscontrol;
	static boolean hasSerial = false;
	static boolean hasTempSerial = false;
	static CommPort tempCP, fortyPortsCP;
	static OutputStream tempOut;
	static InputStream tempIn;
	static OutputStream fortyPortsOut;
	static RunFrame rf;
	final JTextArea txtrOpenSettemp = new JTextArea();

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					MainFrame window = new MainFrame();
					window.frmportscontrol.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 * 
	 * @wbp.parser.entryPoint
	 */
	public MainFrame() {
		initialize();
		testSerial();
		Configure.readAll();
		if (Configure.currentFilePath!=null && (new File(Configure.currentFilePath).exists())) {
			try {
				txtrOpenSettemp
						.setText(Lib.readFile(Configure.currentFilePath));
			} catch (IOException e1) {

				e1.printStackTrace();
			}
		}
	}

	/**
	 * Find out whether temp serial port are connected, and is there a serial
	 * port for control circuit.
	 */
	private void testSerial() {
		System.out.println(System.getProperty("java.library.path"));
		Enumeration<?> emunPort = CommPortIdentifier.getPortIdentifiers();
		int numberOfPorts = 0;
		boolean findTmp = false;
		while (emunPort.hasMoreElements()) {
			CommPortIdentifier pi = (CommPortIdentifier) emunPort.nextElement();
			try {
				CommPort commPort = pi.open("40PortsControl", 2000);
				if (commPort instanceof SerialPort) {
					SerialPort serialPort = (SerialPort) commPort;
					// only accept USB to Serial
					// if(serialPort.getName().indexOf("USB")==-1){
					// continue;
					// }
					System.out.println("find Port: " + serialPort.getName());
					serialPort.setSerialPortParams(9600, SerialPort.DATABITS_8,
							SerialPort.STOPBITS_1, SerialPort.PARITY_NONE);
					InputStream in = serialPort.getInputStream();
					OutputStream out = serialPort.getOutputStream();
					byte[] ba = { (byte) 129, (byte) 129, 82, 0, 0, 0, 83, 0 };
					out.write(ba);
					Thread.currentThread();
					Thread.sleep(300);
					while (in.available() != 0)
						in.read();
					out.write(ba);
					Thread.currentThread();
					Thread.sleep(300);
					if (in.available() != 0) {
						while (in.available() != 0)
							in.read();
						Configure.COMMT = serialPort.getName();
						findTmp = true;
						System.out.println("temp serial's name is: "
								+ serialPort.getName());
					} else {
						Configure.COMMList.add(serialPort.getName());
					}
					numberOfPorts++;
				}
				commPort.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		System.out.println(numberOfPorts + " ports found");
		if (findTmp) {
			JOptionPane.showMessageDialog(frmportscontrol, "find temp and "
					+ (numberOfPorts - 1) + " comms");
			hasSerial = true;
			hasTempSerial = true;
		} else if (numberOfPorts > 0) {
			// Disable timer and just to for ports
			String msg = "No temp found, just go with port control.";
			System.out.println(msg);
			JOptionPane.showMessageDialog(frmportscontrol, msg);
			hasSerial = true;
			hasTempSerial = false;
		} else {
			System.out.println("No serial port found.");
			JOptionPane.showMessageDialog(frmportscontrol, "No serial port found.");
			hasSerial = false;
			hasTempSerial = false;
		}
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frmportscontrol = new JFrame();
		frmportscontrol.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				System.out.println("releasing Serial");
				try {
					if (tempOut != null)
						tempOut.close();
					if (fortyPortsOut != null)
						fortyPortsOut.close();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		});
		frmportscontrol.setTitle("40PortsControl");
		frmportscontrol.setBounds(100, 100, 406, 300);
		frmportscontrol.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmportscontrol.getContentPane().setLayout(new BorderLayout(0, 0));

		JToolBar toolBar = new JToolBar();
		toolBar.setFloatable(false);
		frmportscontrol.getContentPane().add(toolBar, BorderLayout.NORTH);

		JScrollPane scrollPane = new JScrollPane();
		frmportscontrol.getContentPane().add(scrollPane, BorderLayout.CENTER);
		scrollPane.setViewportView(txtrOpenSettemp);

		JButton btnCheckerr = new JButton("CheckErr");
		btnCheckerr.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				CheckErr ce = new CheckErr(0);
				if (!ce.checkIfRight(txtrOpenSettemp.getText(),
						Configure.getCurrentFileName() + ':'))
					JOptionPane.showMessageDialog((Component) e.getSource(),
							ce.sb.toString());
				else
					JOptionPane.showMessageDialog((Component) e.getSource(),
							"There is no errors");
			}
		});

		JButton btnOpenFile = new JButton("Open File");
		btnOpenFile.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				JFileChooser chooser = new JFileChooser();
				try {
					chooser.setCurrentDirectory(new File(Configure.getCurrentFileParent()));
				} catch (Exception e1) {
					// directory setting doesn't exist, use default
				}
				int returnVal = chooser.showOpenDialog(frmportscontrol);
				if (returnVal == JFileChooser.APPROVE_OPTION) {
					Configure.setCurrentFilePath(chooser.getSelectedFile()
							.getPath());
					try {
						txtrOpenSettemp.setText(Lib.readFile(chooser
								.getSelectedFile().getPath()));
					} catch (IOException e1) {
						e1.printStackTrace();
					}
				}
			}
		});
		toolBar.add(btnOpenFile);
		toolBar.add(btnCheckerr);

		JButton btnRun = new JButton("Run");
		btnRun.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (MainFrame.hasSerial && !Lib.openCOMMs()) {
					JOptionPane.showMessageDialog((Component) e.getSource(),
							"error opening ports");
					return;
				}
				CheckErr ce = new CheckErr(0);
				if (ce.checkIfRight(txtrOpenSettemp.getText(),
						Configure.getCurrentFileName() + ':')
						&& !Run.isRunning) {
					rf = new RunFrame();
					rf.init2();
					new Run().runCode(txtrOpenSettemp.getText());
				} else {
					JOptionPane.showMessageDialog((Component) e.getSource(),
							ce.sb.toString());
				}
			}
		});
		toolBar.add(btnRun);

		JButton btnConfigureComm = new JButton("Configure");
		btnConfigureComm.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
			}
		});
		btnConfigureComm.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				ConfigureDialog cf;
				cf = new ConfigureDialog();
				if (!hasSerial) {
					cf.setAvailableCOMM40(new String[] { "TTY1", "TTY2" });
				} else {
					cf.setAvailableCOMM40(Configure.COMMList.toArray(new String[0]));
				}
				if (Configure.COMM40 != null) {
					cf.setCOMM40(Configure.COMM40);
				}
				cf.setGraphLength(Configure.getGraphLength());
				cf.pack();
				cf.setVisible(true);
				if (cf.result == "OK") {
					if(cf.getCOMM40()!=null)
						Configure.setCOMM40(cf.getCOMM40());
					Configure.setGraphLength(cf.getGraphLength());
				}
				System.out
						.println("COMM 40 now changes to " + Configure.COMM40);
			}
		});
		toolBar.add(btnConfigureComm);
	}
}
