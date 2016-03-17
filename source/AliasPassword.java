import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class AliasPassword extends JPanel implements ActionListener {
	
	private JLabel alias, pswrd;
	private JTextField user;
	private JPasswordField pass;
	private JButton button;
	private char[] password = null;
	private String userName = null;
	private static String type;
	
	public AliasPassword() {
		JPanel userPanel = new JPanel();
		alias = new JLabel("Alias:");
		userPanel.add(alias);
		user = new JTextField(10);
		userPanel.add(user);
		add(userPanel);
		JPanel passPanel = new JPanel();
		pswrd = new JLabel("Password:");
		passPanel.add(pswrd);
		pass = new JPasswordField(10);
		passPanel.add(pass);
		add(passPanel);
		button = new JButton("Enter");
		button.addActionListener(this);
		add(button);
	}
	
	public void actionPerformed(ActionEvent e) {
		userName = user.getText();
		password = pass.getPassword();
		//System.out.println(returnAlias());
		//System.out.println(returnPassword());
		JFrame frame = (JFrame) SwingUtilities.getWindowAncestor(this);
		frame.dispose();
	}
	
	public String returnPassword(){
		return new String(password);
	}
	
	public void setType(String t){
		type = t;
	}
	
	public String getType(){
		return type;
	}
	
	public String returnAlias(){
		return userName;
	}
	
	/*public static void main(String[] args) {
		String msg;
		if (type.equals("ENCRYPT"))
		msg = "Set alias and password";
		else msg = "Enter alias and password";
		JFrame window = new JFrame(msg);
		window.setBounds(300,300,300,300);
		window.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		JComponent contentPane = new AliasPassword();
		contentPane.setOpaque(true);
		window.setContentPane(contentPane);
		window.pack();
		window.setVisible(true);
		window.setResizable(false);
	}*/
}