import java.awt.*;
import java.awt.event.*;
import javax.swing.*;
import javax.swing.JFileChooser;
import java.beans.*;
import java.io.*;
import java.util.Scanner;
import java.util.Random;
import java.nio.charset.*;
import java.nio.file.*;
import java.util.zip.*;

public class EncryptGUI extends JPanel
	implements ActionListener
{
	private JTextArea log, path;
	private JButton open, enc, dec;
	private JFileChooser fc;
	private File file;
	private JProgressBar progressBar;
	private Task task;
	private AESEncryption crypto = new AESEncryption();
	private static AliasPassword aliasPassword = new AliasPassword();

	public EncryptGUI(){
		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		
		JPanel panel = new JPanel();
		panel.setLayout(new GridLayout(1,2,1,1));
		JButton open = new JButton("Open File...");
		open.addActionListener(this);
		panel.add(open);
		path = new JTextArea(1,10);
		path.setEditable(false);
		panel.add(path);
		add(panel);
		add(Box.createRigidArea(new Dimension(0,5)));
		
		JPanel buttons = new JPanel();
		buttons.setLayout(new GridLayout(1,2,1,1));
		enc = new JButton("Encrypt");
		enc.addActionListener(new EncryptActionListener());
		enc.setEnabled(false);
		buttons.add(enc);
		dec = new JButton("Decrypt");
		dec.addActionListener(new DecryptActionListener());
		dec.setEnabled(false);
		buttons.add(dec);
		add(buttons);
		
		log = new JTextArea(10,20);
        log.setMargin(new Insets(10,10,10,10));
        log.setEditable(false);
        JScrollPane logScrollPane = new JScrollPane(log);
		add(Box.createRigidArea(new Dimension(0,5)));
		add(logScrollPane);
		
		add(Box.createRigidArea(new Dimension(0,5)));
		progressBar = new JProgressBar(0, 100);
        progressBar.setValue(0);
        progressBar.setStringPainted(true);
		add(progressBar);
		
		fc = new JFileChooser();
		TextFilter textFilter = new TextFilter();
		ZipFilter zipFilter = new ZipFilter();
		fc.addChoosableFileFilter(textFilter);
		fc.addChoosableFileFilter(zipFilter);
		fc.setFileFilter(textFilter);
		fc.setAcceptAllFileFilterUsed(false);
	}

	public void actionPerformed(ActionEvent e) {
		int val = fc.showOpenDialog(EncryptGUI.this);

		if (val == JFileChooser.APPROVE_OPTION) {
			file = fc.getSelectedFile();
			log.append("Opening: " + file.getName() + ".\n");
			path.setText(file.getPath());
			crypto.setSourcePath(file.getParent());
			enc.setEnabled(true);
			if(file.getName().substring(0,3).equals("enc")) {
				dec.setEnabled(true);
			}
		} else {
			log.append("Open command cancelled by user.\n");
		}
	}

	private class Task extends SwingWorker<Void, Void> {
		private String type;
		
		public void setType(String t) {
			type = t;
		}
		
		public Void doInBackground() {
			Random rand = new Random();
			int progress = 0;
			setProgress(0);
			while (progress < 100) {
				try {
					Thread.sleep(rand.nextInt(1000));
				} catch(InterruptedException e) {}
				progress += rand.nextInt(10);
				setProgress(Math.min(progress,100));
			}
			return null;
		}
	
		public void done() {
			Toolkit.getDefaultToolkit().beep();
			setCursor(null);
			log.append("Done!\n");
			if (type == "Encrypt") {
				log.append("Encrypting: complete.\nFile 'enc-" + file.getName() + ".zip' created.\n");
			}
			if (type == "Decrypt") {
				log.append("Decrypting: complete.\nFile 'dec-" + file.getName().replace("enc-","").replace(".zip","") + "' created.\n");
			}
		}
	}
	
	private class EncryptActionListener implements ActionListener, PropertyChangeListener {
		public void actionPerformed(ActionEvent a){
			log.append("Encrypting: " +file.getName() + ".\n");
			setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			task = new Task();
			task.setType("Encrypt");
			task.addPropertyChangeListener(this);
			task.execute();
			crypto.setPassword(aliasPassword.returnPassword());
			crypto.setAlias(aliasPassword.returnAlias());
			deleteReturns(file);
			Path plainText = Paths.get(file.getParent()+"\\trans-"+file.getName());
			Path cipherText = Paths.get(file.getParent()+"\\enc-"+file.getName());
			Charset charset = Charset.forName("UTF-8");
			try {
				BufferedReader reader = Files.newBufferedReader(plainText, charset);
				BufferedWriter writer = Files.newBufferedWriter(cipherText, charset);
				String line = null;
				while ((line = reader.readLine()) != null) {
					String encryptedLine = crypto.encrypt(line);
					writer.write(encryptedLine, 0, encryptedLine.length());
				}
				reader.close();
				writer.close();
			} catch (Exception e) {
				log.append("Error occurred.\n");
				e.printStackTrace();
			}
			
			try {
				FileOutputStream fos = new FileOutputStream(file.getParent()+"\\enc-"+file.getName()+".zip");
				ZipOutputStream zos = new ZipOutputStream(fos);
				addToZip("iv.txt",zos);
				addToZip("keystore.jceks",zos);
				addToZip("enc-"+file.getName(),zos);
				zos.flush();
				zos.close();
				fos.flush();
				fos.close();
				crypto.deleteFile("iv.txt");
				crypto.deleteFile("keystore.jceks");
				crypto.deleteFile("enc-"+file.getName());
				crypto.deleteFile("trans-"+file.getName());
			} catch (Exception e){
				e.printStackTrace();
			}
		}
		
		public void propertyChange(PropertyChangeEvent e) {
			if ("progress" == e.getPropertyName()) {
				int progress = (Integer) e.getNewValue();
				progressBar.setValue(progress);
			}
		}
		
		public void addToZip(String fileName, ZipOutputStream zos) {
			try {
				File f = new File(file.getParent()+"//"+fileName);
				FileInputStream fis = new FileInputStream(f);
				ZipEntry zipEntry = new ZipEntry(fileName);
				zos.putNextEntry(zipEntry);
				byte[] bytes = new byte[1024];
				int length;
				while ((length = fis.read(bytes)) > 0) {
					zos.write(bytes, 0, length);
				}
				zos.closeEntry();
				fis.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		
		public void deleteReturns(File f) {
			Path originalText = Paths.get(f.getAbsolutePath());
			Path transformedText = Paths.get(f.getParent() + "//trans-" + f.getName());
			Charset charset = Charset.forName("UTF-8");
			try {
				BufferedReader reader = Files.newBufferedReader(originalText, charset);
				BufferedWriter writer = Files.newBufferedWriter(transformedText, charset);
				String line = null;
				while ((line = reader.readLine()) != null) {
					line = (line + " ").replace("/n"," ").replace("/r"," ");
					writer.write(line, 0, line.length());
				}
				reader.close();
				writer.close();
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	private class DecryptActionListener implements ActionListener, PropertyChangeListener {
		public void actionPerformed(ActionEvent a){
			log.append("Decrypting: " +file.getName() + ".\n");
			setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
			task = new Task();
			task.setType("Decrypt");
			task.addPropertyChangeListener(this);
			task.execute();
			crypto.setPassword(aliasPassword.returnPassword());
			crypto.setAlias(aliasPassword.returnAlias());
			try {
				String outputFolder = file.getParent();
				unzip(file.getParent() + "//" + file.getName(), outputFolder);
			} catch (Exception e) {
				e.printStackTrace();
			}

			Path cipherText = Paths.get(file.getParent()+"\\"+file.getName().replace(".zip",""));
			Path decryptedText = Paths.get(file.getParent()+"\\dec-"+file.getName().replace("enc-","").replace(".zip",""));
			Charset charset = Charset.forName("UTF-8");
			try {
				BufferedReader reader = Files.newBufferedReader(cipherText, charset);
				BufferedWriter writer = Files.newBufferedWriter(decryptedText, charset);
				String line = null;
				while ((line = reader.readLine()) != null) {
					String decryptedLine = crypto.decrypt(line);
					writer.write(decryptedLine, 0, decryptedLine.length());
				}
				reader.close();
				writer.close();
			} catch (Exception e) {
				log.append("Error occurred.\n");
				e.printStackTrace();
			}
			crypto.deleteFile(file.getName().replace(".zip",""));
		}
		
		public void propertyChange(PropertyChangeEvent e) {
			if ("progress" == e.getPropertyName()) {
				int progress = (Integer) e.getNewValue();
				progressBar.setValue(progress);
			}
		}
		
		public void unzip(String zipFile, String outputFolder) {
			byte[] buffer = new byte[1024];
			try {
				FileInputStream fis = new FileInputStream(zipFile);
				ZipInputStream zis = new ZipInputStream(fis);
				ZipEntry entry = zis.getNextEntry();
				while (entry != null) {
					String fileName = outputFolder + "\\" + entry.getName();
					File outFile = new File(fileName);
					FileOutputStream fos = new FileOutputStream(outFile);
					int length;
					while ((length = zis.read(buffer)) > 0) {
						fos.write(buffer, 0, length);
					}
					fos.flush();
					fos.close();
					entry = zis.getNextEntry();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public static void displayGUI() {
		JFrame window = new JFrame("Encrypt GUI");
		window.setBounds(300,300,300,300);
		window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		JComponent contentPane = new EncryptGUI();
		contentPane.setOpaque(true);
		window.setContentPane(contentPane);
		window.pack();
		window.setVisible(true);
		window.setResizable(false);
		displayAliasPassword();
	}
	
	public static void displayAliasPassword() {
		JFrame frame = new JFrame("Enter alias and password.");
		frame.setBounds(300,300,300,300);
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		JComponent contentPane = aliasPassword;
		contentPane.setOpaque(true);
		frame.setContentPane(contentPane);
		frame.pack();
		frame.setVisible(true);
		frame.setResizable(false);
		frame.setAlwaysOnTop(true);
	}
	
	public static void main(String[] args){
		 javax.swing.SwingUtilities.invokeLater(new Runnable() {
            public void run() {
                displayGUI();
            }
        });
	}
}