package uk.co.revsys.oddball.service;

import de.neuland.jade4j.spring.view.JadeViewResolver;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.IOUtils;
import org.json.JSONObject;
import org.springframework.http.MediaType;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.servlet.View;
import uk.co.revsys.oddball.Oddball;

public class OddballServlet extends HttpServlet {

	private Oddball oddball;
	private JadeViewResolver viewResolver;
	private DiskFileItemFactory factory;
	private ServletFileUpload upload;

	@Override
	public void init() throws ServletException {
		WebApplicationContext webApplicationContext = WebApplicationContextUtils.getWebApplicationContext(getServletContext());
		this.oddball = webApplicationContext.getBean(Oddball.class);
		this.viewResolver = webApplicationContext.getBean(JadeViewResolver.class);
		this.factory = new DiskFileItemFactory();
		this.upload = new ServletFileUpload(factory);
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		try {
			resp.setHeader("Access-Control-Allow-Origin", "*");
			String filePath = req.getRequestURI().substring((req.getContextPath() + req.getServletPath()).length() + 1);
			if (filePath.isEmpty() || filePath.endsWith("/")) {
				List<Directory> directories = resourceRepository.listDirectories(filePath);
				List<Resource> resources = resourceRepository.listResources(filePath);
				Map<String, Object> model = new HashMap<String, Object>();
				model.put("directories", directories);
				model.put("resources", resources);
				String viewType = req.getParameter("view");
				if (viewType != null && viewType.equals("json")) {
					resp.getWriter().write(new JSONObject(model).toString());
					resp.setContentType(MediaType.APPLICATION_JSON.toString());
				} else {
					String parentPath = filePath;
					if (!parentPath.isEmpty()) {
						parentPath = parentPath.substring(0, parentPath.length() - 1);
						parentPath = req.getContextPath() + "/" + parentPath.substring(0, parentPath.lastIndexOf("/") + 1);
					} else {
						parentPath = req.getContextPath() + "/";
					}
					model.put("parentPath", parentPath);
					View view = viewResolver.resolveViewName("./resources.jade", Locale.getDefault());
					view.render(model, req, resp);
				}
			} else {
				String resourcePath = filePath.substring(0, filePath.lastIndexOf("/"));
				String resourceName = filePath.substring(filePath.lastIndexOf("/") + 1);
				InputStream resourceStream = resourceRepository.read(new Resource(resourcePath, resourceName));
				IOUtils.copy(resourceStream, resp.getOutputStream());
			}
		} catch (Exception ex) {
			throw new ServletException(ex);
		}
	}

	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		try {
			resp.setHeader("Access-Control-Allow-Origin", "*");
			String filePath = req.getRequestURI().substring((req.getContextPath() + req.getServletPath()).length() + 1);
			List<FileItem> items = upload.parseRequest(req);
			for (FileItem item : items) {
				if (!item.isFormField()) {
					String resourcePath;
					String resourceName;
					if (filePath.endsWith("/")) {
						resourcePath = filePath.substring(0, filePath.lastIndexOf("/"));
						resourceName = item.getName();
					} else {
						resourcePath = filePath.substring(0, filePath.lastIndexOf("/"));
						resourceName = filePath.substring(filePath.lastIndexOf("/") + 1);
					}
					Resource resource = new Resource(resourcePath, resourceName);
					InputStream uploadStream = item.getInputStream();
					resourceRepository.write(resource, uploadStream);
					uploadStream.close();
					resp.getWriter().write(resource.getName() + " uploaded successfully");
				}
			}
		} catch (FileUploadException ex) {
			throw new ServletException(ex);
		}
	}

	@Override
	protected void doDelete(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		System.out.println("doDelete");
		resp.setHeader("Access-Control-Allow-Origin", "*");
		String filePath = req.getRequestURI().substring((req.getContextPath() + req.getServletPath()).length() + 1);
		String resourcePath = filePath.substring(0, filePath.lastIndexOf("/"));
		String resourceName = filePath.substring(filePath.lastIndexOf("/") + 1);
		resourceRepository.delete(new Resource(resourcePath, resourceName));
		resp.getWriter().write(resourceName + " deleted successfully");
	}

	@Override
	protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.setHeader("Access-Control-Allow-Origin", "*");
		resp.setHeader("Access-Control-Allow-Methods", "GET,PUT,POST,DELETE");
		super.doOptions(req, resp);
	}

}
