package quarantine.covid19.util


import java.io.File;
import java.io.FileOutputStream;


object IOUtil {

  
	  
  def writeToFile(bytes: Array[Byte], filePath: String) = {
    
    val file = new File(filePath)
		val fos = new FileOutputStream(file);
    
      if (!file.exists()) {
	     file.createNewFile();
	  }
    
    fos.write(bytes)
    fos.flush()
    fos.close()
  }
}