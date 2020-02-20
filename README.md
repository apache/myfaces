# Apache MyFaces Core

Apache's implementation of the JavaServer Faces specification 

## Minimum Requirements (trunk / 2.3-next)

- Java 1.8
- Servlet 3.0
- EL 2.2
- CDI 1.2
- JSTL 1.2 (optional)
- BV 1.1 (optional)

Servlet 4.0 will enable JSF 2.3 to serve resources via HTTP/2 push.   

2.3-next equals the JSF 2.3 API but delegated @ManagedBeans to CDI. The implementation of the old FacesEL (javax.faces.el.*) has been completely removed.

## Installation

mvn clean install

## Usage

### Dependency
    ```xml
    <dependency>
        <groupId>org.apache.myfaces.core</groupId>
        <artifactId>myfaces-api</artifactId>
        <version>2.3-next-M1</version>
    </dependency>
    <dependency>
        <groupId>org.apache.myfaces.core</groupId>
        <artifactId>myfaces-impl</artifactId>
        <version>2.3-next-M1</version>
    </dependency>
    ```

### web.xml
    ```xml
    <servlet>
        <servlet-name>facesServlet</servlet-name>
        <servlet-class>javax.faces.webapp.FacesServlet</servlet-class>
    </servlet>
    <servlet-mapping>
        <servlet-name>facesServlet</servlet-name>
        <url-pattern>*.xhtml</url-pattern>
    </servlet-mapping>
    ```

### index.xhtml
    ```xml
    <!DOCTYPE html>
    <html xmlns="http://www.w3.org/1999/xhtml"
      xmlns:h="http://java.sun.com/jsf/html"
      xmlns:f="http://java.sun.com/jsf/core"
      xmlns:p="http://primefaces.org/ui">

      <h:head>

      </h:head>

      <h:body>

        Hello World!

      </h:body>
    </html>
    ```
