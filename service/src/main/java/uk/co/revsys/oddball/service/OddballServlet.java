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
import uk.co.revsys.oddball.cases.StringCase;
import uk.co.revsys.oddball.rules.Opinion;
import uk.co.revsys.resource.repository.ResourceRepository;
import uk.co.revsys.resource.repository.model.Directory;
import uk.co.revsys.resource.repository.model.Resource;

public class OddballServlet extends HttpServlet {

	private Oddball oddball;
	private ResourceRepository resourceRepository;
	private JadeViewResolver viewResolver;

	@Override
	public void init() throws ServletException {
		WebApplicationContext webApplicationContext = WebApplicationContextUtils.getWebApplicationContext(getServletContext());
		this.resourceRepository = webApplicationContext.getBean(ResourceRepository.class);
		this.oddball = new Oddball(resourceRepository);
		this.viewResolver = webApplicationContext.getBean(JadeViewResolver.class);
	}

	@Override
	protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		try {
			resp.setHeader("Access-Control-Allow-Origin", "*");
                        String actionStr = req.getParameter("action");
                        String caseStr = req.getParameter("case");
                        String ruleSet = req.getParameter("ruleSet");
                        boolean goodRequest = false;
                        if ((actionStr!=null) && ruleSet!=null && (actionStr.equals("clear"))){
                            oddball.clearRuleSet(ruleSet);
                            goodRequest = true;
                        }
                        if (caseStr!=null && ruleSet!=null){
                            Opinion op = oddball.assessCase(ruleSet, new StringCase(caseStr));
                            resp.getWriter().write(op.getLabel());
                            goodRequest = true;
                        } 
                        if (!goodRequest) {
                            resp.getWriter().write("Specify parameters ruleSet and case or action");
                        }
        		resp.setContentType(MediaType.TEXT_PLAIN.toString());
		} catch (Exception ex) {
			throw new ServletException(ex);
		}
	}

	@Override
	protected void doOptions(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		resp.setHeader("Access-Control-Allow-Origin", "*");
		resp.setHeader("Access-Control-Allow-Methods", "GET");
		super.doOptions(req, resp);
	}

}
