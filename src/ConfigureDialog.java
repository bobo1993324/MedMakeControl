import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;

import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import javax.swing.DefaultComboBoxModel;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JSpinner;


public class ConfigureDialog extends JDialog {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private final JPanel contentPanel = new JPanel();
	JComboBox comboBox = new JComboBox();
	JSpinner spinner = new JSpinner();
	protected String result;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			ConfigureDialog dialog = new ConfigureDialog();
			dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
			dialog.setVisible(true);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Create the dialog.
	 */
	public ConfigureDialog() {
		setModal(true);
		setBounds(100, 100, 450, 184);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.NORTH);
		contentPanel.setLayout(new FlowLayout(FlowLayout.CENTER, 5, 5));
		
		Box verticalBox = Box.createVerticalBox();
		contentPanel.add(verticalBox);
		
		Box horizontalBox_1 = Box.createHorizontalBox();
		verticalBox.add(horizontalBox_1);
		{
			JLabel lblComm = new JLabel("COMM40");
			horizontalBox_1.add(lblComm);
		}
		{
			Component horizontalStrut = Box.createHorizontalStrut(30);
			horizontalBox_1.add(horizontalStrut);
		}
		horizontalBox_1.add(comboBox);
		{
			Component horizontalStrut = Box.createHorizontalStrut(30);
			horizontalBox_1.add(horizontalStrut);
		}
		{
			JButton btnTest = new JButton("test");
			horizontalBox_1.add(btnTest);
			
			Box horizontalBox_2 = Box.createHorizontalBox();
			verticalBox.add(horizontalBox_2);
			{
				JLabel lblGraphMaxTime = new JLabel("graph max time");
				horizontalBox_2.add(lblGraphMaxTime);
			}
			
			horizontalBox_2.add(spinner);
			btnTest.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseClicked(MouseEvent e) {
					Lib.test40COMM((String)comboBox.getSelectedItem());
				}
			});
		}
		{
			JPanel buttonPane = new JPanel();
			buttonPane.setLayout(new FlowLayout(FlowLayout.RIGHT));
			getContentPane().add(buttonPane, BorderLayout.SOUTH);
			{
				JButton okButton = new JButton("OK");
				okButton.addMouseListener(new MouseAdapter() {
					@Override
					public void mouseClicked(MouseEvent e) {
						result="OK";
						setVisible(false);
					}
				});
				okButton.setActionCommand("OK");
				buttonPane.add(okButton);
				getRootPane().setDefaultButton(okButton);
			}
			{
				JButton cancelButton = new JButton("Cancel");
				cancelButton.addMouseListener(new MouseAdapter() {
					@Override
					public void mouseClicked(MouseEvent e) {
						result="Cancel";
						setVisible(false);
					}
				});
				cancelButton.setActionCommand("Cancel");
				buttonPane.add(cancelButton);
			}
		}
	}
	
	public void setAvailableCOMM40(String[] sa){
		for(int i=0;i<sa.length;i++)
			this.comboBox.addItem(sa[i]);
	}
	
	public boolean setCOMM40(String string){
		for(int i=0;i<this.comboBox.getItemCount();i++){
			if(string.equals(comboBox.getItemAt(i))){
				System.out.println("find "+string+" at "+i);
				comboBox.setSelectedIndex(i);
				return true;
			}
		}
		return false;
	}

	public String getCOMM40() {
		return (String) comboBox.getSelectedItem();
	}

	public void setGraphLength(int graphLength) {
		spinner.setValue(graphLength);
	}

	public int getGraphLength() {
		return (Integer)spinner.getValue();
	}
}
