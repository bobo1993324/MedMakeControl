import java.awt.BorderLayout;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;

import net.miginfocom.swing.MigLayout;

public class RunFrame extends JFrame {

	/**
	 * 
	 */
	private static final long serialVersionUID = -1871977801569770023L;
	private JPanel contentPane;
	private JTextField textField;
	private JTextField textField_1;
	private JTextField textField_2;
	private Timer tempTimer;
	private Timer fortyTimer;
	private JScrollPane scrollPane;
	private JTextArea textArea;
	protected boolean statusThreadIsRunning;
	protected boolean tempThreadIsRunning;

	// record temperature
	public Timer recordTimer = new Timer();
	public DrawLineGraph drawLineGraph;

	/**
	 * Create the frame.
	 */
	public RunFrame() {
		addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosed(WindowEvent e) {
				Run.abort();
				drawLineGraph.close();
				if (MainFrame.hasSerial)
					tempTimer.cancel();
				fortyTimer.cancel();
			}
		});
		setTitle("Run");
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 365, 277);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(new BorderLayout(0, 0));

		scrollPane = new JScrollPane();
		textArea = new JTextArea();
		textArea.setEditable(false);
		scrollPane.setViewportView(textArea);
		contentPane.add(scrollPane);

		JPanel panel = new JPanel();
		contentPane.add(panel, BorderLayout.EAST);
		panel.setLayout(new MigLayout("", "[][75.00px,grow]",
				"[][][][][40.00][-28.00px:n][-25.00px:n]"));

		JLabel lblT = new JLabel("T1");
		panel.add(lblT, "cell 0 0,alignx trailing");

		textField = new JTextField();
		textField.addKeyListener(new KeyAdapter() {
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER && !MainFrame.hasSerial)
					Run.opTemp(0, Run.WRITE,
							Double.parseDouble(textField.getText()));
			}
		});
		textField.setEditable(false);
		panel.add(textField, "cell 1 0,growx");
		textField.setColumns(10);

		JLabel lblT_1 = new JLabel("T2");
		panel.add(lblT_1, "cell 0 1,alignx trailing");

		textField_1 = new JTextField();
		textField_1.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER && !MainFrame.hasSerial)
					Run.opTemp(1, Run.WRITE,
							Double.parseDouble(textField_1.getText()));
			}
		});
		textField_1.setEditable(false);
		panel.add(textField_1, "cell 1 1,growx");
		textField_1.setColumns(10);

		JLabel lblT_2 = new JLabel("T3");
		panel.add(lblT_2, "cell 0 2,alignx trailing");

		textField_2 = new JTextField();
		textField_2.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER && !MainFrame.hasSerial)
					Run.opTemp(2, Run.WRITE,
							Double.parseDouble(textField_2.getText()));

			}
		});
		textField_2.setEditable(false);
		panel.add(textField_2, "cell 1 2,growx");
		textField_2.setColumns(10);

		JButton btnNewButton_1 = new JButton("Resume");
		btnNewButton_1.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				Run.isWaiting = false;
			}
		});

		panel.add(btnNewButton_1, "cell 0 5 2 1,alignx center,aligny bottom");

		JButton btnNewButton = new JButton("Abort");
		btnNewButton.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				Run.abort();
			}
		});
		panel.add(btnNewButton, "cell 0 6 2 1,alignx center,aligny bottom");

		if (MainFrame.hasSerial) {
			tempTimer = new Timer();
			tempTimer.scheduleAtFixedRate(new TimerTask() {
				public void run() {
					textField.setText(new Double(Run.temperature[0]).toString());
					textField_1.setText(new Double(Run.temperature[1])
							.toString());
					textField_2.setText(new Double(Run.temperature[2])
							.toString());
				}
			}, 500, 500);
		}

		fortyTimer = new Timer();
		fortyTimer.scheduleAtFixedRate(new TimerTask() {
			public void run() {
				Runnable doScroll = new Runnable() {
					public void run() {
						scrollPane.getVerticalScrollBar().setValue(
								scrollPane.getVerticalScrollBar().getMaximum());
					}
				};

				if (Run.statusSb.toString() != textArea.getText()) {
					textArea.setText(Run.statusSb.toString());
					SwingUtilities.invokeLater(doScroll);
				}

			}
		}, 500, 500);

		this.setVisible(true);
		if (!MainFrame.hasSerial) {
			this.textField.setEditable(true);
			this.textField_1.setEditable(true);
			this.textField_2.setEditable(true);
		}
	}

	public void init2() {
		drawLineGraph = new DrawLineGraph(this);
		recordTimer.scheduleAtFixedRate(new TimerTask() {
			@Override
			public void run() {
				drawLineGraph.update(Run.temperature[2]);
			}
		}, 3000, 3000);
	}
}
