This directory contains files that are included into TLD files via XML
entity references. These declare JSP attributes so that multiple tags
which accept the same attributes can share their declarations.

The following naming standard is followed:

faces_*     Attributes defined in the f: namespace
ui_*        Attributes which map to a property on a class in package 
            javax.faces.component (but not javax.faces.component.html)
html_*      Attributes for components in the h: namespace which map to
            properties on classes in javax.faces.component.html.
standard_*  Attributes that are specific to a particular component, or a file
            containing references to a mix of the above entities. For a
            component whose tagname is fooBar, the filename is always of form
            standard_foo_bar_attributes.xml.
validator_* Attributes on validator classes.

