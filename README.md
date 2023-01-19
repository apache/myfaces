# Apache MyFaces Core
[![Build Status](https://github.com/apache/myfaces/workflows/MyFaces%20CI/badge.svg)](https://github.com/apache/myfaces/actions/workflows/myfaces-ci.yml)
[![License](https://img.shields.io/badge/License-Apache%202.0-blue.svg)](https://opensource.org/licenses/Apache-2.0)
[![Build Status ASF](https://ci-builds.apache.org/buildStatus/icon?subject=ASF-Build&job=MyFaces%2FMyFaces+Pipeline%2Fmain)](https://ci-builds.apache.org/job/MyFaces/job/MyFaces%20Pipeline/job/main/)

Apache's implementation of the JavaServer Faces (JSF) and Jakarta Faces specification

## Branches

### Main

Jakarta Faces 4.0 implementation  
Based on the refactored 2.3-next codebase

### 3.0.x

Jakarta Faces 3.0 implementation  
It's equals to JSF 2.3 but with "jakarta.faces" packages and constants instead of "javax.faces"

### 2.3-next

(Almost) JavaServer Faces 2.3 implementation  
Completely refactored codebase compared to 2.3, also providing a Quarkus extension  
2.3-next equals the JSF 2.3 API but delegates @ManagedBeans to CDI; ManagedBeans configured via XML are completely ignored. The implementation of the old FacesEL (javax.faces.el.*) also has been completely removed.

### 2.3.x

JavaServer Faces 2.3 implementation


## Minimum Requirements (main / 2.3-next)

- Java 1.8
- Servlet 3.0
- EL 2.2
- CDI 1.2
- JSTL 1.2 (optional)
- BV 1.1 (optional)

Servlet 4.0 will enable JSF to serve resources via HTTP/2 push.

## Installation

mvn clean install

## Usage

### Dependency
```xml
<dependency>
    <groupId>org.apache.myfaces.core</groupId>
    <artifactId>myfaces-api</artifactId>
    <version>2.3-next-M3</version>
</dependency>
<dependency>
    <groupId>org.apache.myfaces.core</groupId>
    <artifactId>myfaces-impl</artifactId>
    <version>2.3-next-M3</version>
</dependency>
```

### web.xml
```xml
<?xml version="1.0" encoding="UTF-8"?>
<web-app xmlns="http://xmlns.jcp.org/xml/ns/javaee"
         xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
         xsi:schemaLocation="http://xmlns.jcp.org/xml/ns/javaee http://xmlns.jcp.org/xml/ns/javaee/web-app_3_1.xsd"
         version="3.1">

    <servlet>
        <servlet-name>Faces Servlet</servlet-name>
        <servlet-class>javax.faces.webapp.FacesServlet</servlet-class>
    </servlet>
    <servlet-mapping>
        <servlet-name>Faces Servlet</servlet-name>
        <url-pattern>*.xhtml</url-pattern>
    </servlet-mapping>

</web-app>
```

### index.xhtml
```xml
<!DOCTYPE html>
<html xmlns="http://www.w3.org/1999/xhtml"
  xmlns:f="http://xmlns.jcp.org/jsf/core"
  xmlns:h="http://xmlns.jcp.org/jsf/html">

  <h:head>

  </h:head>

  <h:body>

    Hello World!

  </h:body>
</html>
```

## Quarkus extension

Since 2.3-next a Quarkus extension is available. A sample project can be found here: https://github.com/apache/myfaces/blob/main/extensions/quarkus/showcase/

Uber-JARs are not supported by design

### Differences to a normal servlet container
- You need to put your views under src/main/resources/META-INF/resources as Quarkus doesn't create a WAR and src/main/webapp is ignored!
- Session replication / passivation / clustering is not supported yet by Quarkus
