package com.example.demo;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.Map;

public class JettyReverseProxy {

    private static ApplicationService applicationService;

    public static void main(String[] args) throws Exception {
        applicationService = new ApplicationService();
        Server server = new Server(8080);

        ServletContextHandler context = new ServletContextHandler(ServletContextHandler.SESSIONS);
        context.setContextPath("/");
        server.setHandler(context);

        context.addServlet(new ServletHolder(new ProxyServlet()), "/app/*");
        context.addServlet(new ServletHolder(new AdminServlet()), "/admin");

        server.start();
        server.join();
    }

    public static void addAlias(String alias, int port) {
        applicationService.getRunningApplications().put(alias, port);
    }

    @WebServlet(name = "ProxyServlet", urlPatterns = {"/app/*"})
    public static class ProxyServlet extends HttpServlet {
        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            String pathInfo = req.getPathInfo();
            Integer port = applicationService.getRunningApplications().get(pathInfo);

            if (port != null) {
                String url = "http://localhost:" + port + req.getRequestURI();

                HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
                connection.setRequestMethod(req.getMethod());

                int responseCode = connection.getResponseCode();
                resp.setStatus(responseCode);
                connection.getInputStream().transferTo(resp.getOutputStream());
            } else {
                resp.sendError(HttpServletResponse.SC_NOT_FOUND, "No mapping found for " + pathInfo);
            }
        }
    }

    @WebServlet(name = "AdminServlet", urlPatterns = {"/admin"})
    public static class AdminServlet extends HttpServlet {
        @Override
        protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            StringBuilder responseContent = new StringBuilder("<html><body>");
            responseContent.append("<h1>Running Applications</h1>");
            responseContent.append("<table border='1'><tr><th>Alias</th><th>Port</th><th>Action</th></tr>");

            for (Map.Entry<String, Integer> entry : applicationService.getRunningApplications().entrySet()) {
                String alias = entry.getKey();
                Integer port = entry.getValue();

                responseContent.append("<tr>")
                        .append("<td>").append(alias).append("</td>")
                        .append("<td>").append(port).append("</td>")
                        .append("<td>")
                        .append("<a href='/admin/stop?port=").append(port).append("'>Stop</a>")
                        .append("</td>")
                        .append("</tr>");
            }

            responseContent.append("</table>");
            responseContent.append("</body></html>");

            resp.setContentType("text/html");
            resp.getWriter().write(responseContent.toString());
        }

        @Override
        protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
            String action = req.getParameter("action");
            int port = Integer.parseInt(req.getParameter("port"));

            if ("stop".equals(action)) {
                String result = applicationService.stopApplication(port);
                resp.getWriter().write(result);
            }
        }
    }
}
