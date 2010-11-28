package org.pullback

import java.io._ ;
import javax.servlet._ ;
import javax.servlet.http._ ;

abstract class ScalaHttpServlet extends HttpServlet {

  val contentType = "text/html";

  def doGetXML( req:HttpServletRequest ):scala.xml.Node ;

  final override def doGet( req:HttpServletRequest, res:HttpServletResponse ):Unit = {
    res.setContentType("text/html");
    val out:PrintWriter = res.getWriter();
    out.print( doGetXML( req ).toString() );
  }

  override def init():Unit = super.init();
  override def init(sc:javax.servlet.ServletConfig):Unit = super.init(sc);
  override def service(req:javax.servlet.http.HttpServletRequest,
              resp:javax.servlet.http.HttpServletResponse) = 
                super.service(req, resp):Unit ;
                            
  override def service(req:javax.servlet.ServletRequest,
              resp:javax.servlet.ServletResponse):Unit =
                super.service(req,resp);
}
