<?xml version="1.0" encoding="ISO-8859-1"?>
<!--
  - Stylesheet to expand xml entity references inline. 
  - 
  - This is a temporary workaround for a bug in the tlddoc application which means that
  - a (valid) xmlns attribute is rejected; tlddoc v1.3 only accepts input that (incorrectly)
  - puts the tld elements in no namespace. This stylesheet therefore generates a special
  - version of the tld for feeding to the tlddoc app, until such time as tlddoc is fixed.
  -->
<xsl:stylesheet version="1.1"
    xmlns:xsl="http://www.w3.org/1999/XSL/Transform">

  <xsl:output method="xml"
      encoding="ISO-8859-1"
      doctype-public="-//Sun Microsystems, Inc.//DTD JSP Tag Library 1.2//EN"
      doctype-system="http://java.sun.com/dtd/web-jsptaglibrary_1_2.dtd"
      indent="yes"/>

  <xsl:template match="*" priority="2">
    <!-- avoiding copy to get get rid of namespace attribute -->
    <xsl:element name="{name(.)}">
      <xsl:apply-templates select="@*|node()"/>
    </xsl:element>
  </xsl:template>

  <xsl:template match="@*|node()">
    <xsl:copy>
      <xsl:apply-templates select="@*|node()"/>
    </xsl:copy>
  </xsl:template>

</xsl:stylesheet>
