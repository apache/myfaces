package org.apache.myfaces.core.integrationtests.ajax.test1Protocol;

import org.apache.myfaces.core.integrationtests.ajax.test1Protocol.jsfxmlnodes.Changes;
import org.apache.myfaces.core.integrationtests.ajax.test1Protocol.jsfxmlnodes.ErrorResponse;
import org.apache.myfaces.core.integrationtests.ajax.test1Protocol.jsfxmlnodes.PartialResponse;
import org.apache.myfaces.core.integrationtests.ajax.test1Protocol.jsfxmlnodes.Update;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * @author Werner Punz (latest modification by $Author$)
 * @version $Revision$ $Date$
 */
public class JSF21Simulation extends HttpServlet {

    static int TIMEOUT_REQS = 0;
    static int QUEUESIZE_REQS = 0;

    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        process(request, response);
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        process(request, response);
    }

    private void process(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        response.setContentType("text/xml;charset=UTF-8");

        PrintWriter out = response.getWriter();
        try {
            org.apache.myfaces.core.integrationtests.ajax.test1Protocol.jsfxmlnodes.PartialResponse root = new PartialResponse();
            if (request.getParameter("op") != null && request.getParameter("op").equals("timeout")) {
                renderTimeout(out, root);
            } else if (request.getParameter("op") != null && request.getParameter("op").equals("cleardelay")) {
                TIMEOUT_REQS = 0;
                Changes changes = new Changes(root);
                changes.addChild(new Update(changes, "delayoutput", "<div id='delayoutput'>Number of requests so far " + TIMEOUT_REQS + "  </div>"));
                root.addElement(changes);
                out.println(root.toString());
                out.flush();
            } else if (request.getParameter("op") != null && request.getParameter("op").equals("delay")) {
                TIMEOUT_REQS++;
                Changes changes = new Changes(root);
                changes.addChild(new Update(changes, "delayoutput", "<div id='delayoutput'>Number of requests so far " + TIMEOUT_REQS + "  </div>"));
                root.addElement(changes);
                out.println(root.toString());
                out.flush();
            } else if (request.getParameter("op") != null && request.getParameter("op").equals("queuesize")) {
                QUEUESIZE_REQS++;
                Changes changes = new Changes(root);
                changes.addChild(new Update(changes, "queuesizeoutput", "<div id='queuesizeoutput'>Number of requests so far " + QUEUESIZE_REQS + "  </div>"));
                root.addElement(changes);
                sleep(300);
                out.println(root.toString());
                out.flush();
            } else if (request.getParameter("op") != null && request.getParameter("op").equals("pps")) {
                QUEUESIZE_REQS++;
                Changes changes = new Changes(root);
                boolean validPPS = request.getParameter("ppsControl") != null && request.getParameter("queuesizecontrol") == null;
                String validPPSString = (validPPS)? "is a valid partial page submit" : "is a full post submit";
                changes.addChild(new Update(changes, "ppsoutput", "<div id='ppsoutput'>" + validPPSString + "  </div>"));
                root.addElement(changes);
                out.println(root.toString());
                out.flush();
            } else if (request.getParameter("op") != null && request.getParameter("op").equals("pps2")) {
                QUEUESIZE_REQS++;
                Changes changes = new Changes(root);
                boolean validPPS = request.getParameter("ppsControl3") != null && request.getParameter("queuesizecontrol") == null;
                String validPPSString = (validPPS)? "is a valid partial page submit" : "is a full post submit";
                changes.addChild(new Update(changes, "ppsoutput2", "<div id='ppsoutput2'>" + validPPSString + "  </div>"));
                root.addElement(changes);
                out.println(root.toString());
                out.flush();
            }




        } finally {
            out.close();

        }

    }

    private void renderTimeout(PrintWriter out, PartialResponse root) {
        sleep(3000);

        root.addElement(new ErrorResponse(root, "This error should be displayed only if you run the long running request", "NoTrigger"));
        out.println(root.toString());
        out.flush();

    }

    private void sleep(int len) {
        try {
            Thread.sleep(len);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

}
