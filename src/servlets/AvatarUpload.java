package servlets;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

@MultipartConfig
@WebServlet("/avatar")
public class AvatarUpload extends HttpServlet{
	
	@Override
	public void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException, ServletException {
		String userID = getCookieValue("userID", request);
		
		Part filePart = request.getPart("file");
		String filename = getFilename(filePart);
		InputStream fileContent = filePart.getInputStream();
		
		String path = String.format("%s:%d", request.getServerName(), request.getServerPort());
		OutputStream outFile = new FileOutputStream(new File(request.getServletContext().getRealPath("/img/avatars/" + userID + ".jpg")));
		int read = 0;
		byte[] bytes = new byte[1024];
	 
		while ((read = fileContent.read(bytes)) != -1) {
			outFile.write(bytes, 0, read);
		}
		fileContent.close();
		outFile.flush();
		outFile.close();
		
	}
	
	private static String getFilename(Part part) {
	    for (String cd : part.getHeader("content-disposition").split(";")) {
	        if (cd.trim().startsWith("filename")) {
	            String filename = cd.substring(cd.indexOf('=') + 1).trim().replace("\"", "");
	            return filename.substring(filename.lastIndexOf('/') + 1).substring(filename.lastIndexOf('\\') + 1); // MSIE fix.
	        }
	    }
	    return null;
	}
	
	private String getCookieValue(String name, HttpServletRequest request) {
		String s = "";
		Cookie[] cookies = request.getCookies();
		if(cookies != null) {
			for(int i = 0; i < cookies.length; i++) {
				if(name.equals(cookies[i].getName())) {
					s = cookies[i].getValue();
				}
			}
		}
		return s;
		
	}

}
