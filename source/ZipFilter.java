import java.io.File;
import javax.swing.filechooser.*;

public class ZipFilter extends FileFilter {
	public boolean accept(File f){
		if (f.isDirectory()){
			return true;	
		}
		String ext = getExtension(f);
		if (ext.equals("zip")){
			return true;
		}
		return false;
	}
		
	public static String getExtension(File f) {
		String ext = "";
		String fileName = f.getName();
		int i = fileName.lastIndexOf('.');
		if (i > 0) {
			ext = fileName.substring(i+1);
		}
		return ext;
	}
		
	public String getDescription(){
		return "Compressed Files (*.zip)";
	}
}