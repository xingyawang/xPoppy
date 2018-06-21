package net.n3.nanoxml;
import java.io.IOException;


public class XMLParserFactory
{

   public static final String DEFAULT_CLASS = "net.n3.nanoxml.StdXMLParser";
   public static final String CLASS_KEY = "net.n3.nanoxml.XMLParser";


   public static IXMLParser createDefaultXMLParser()
      throws ClassNotFoundException,
             InstantiationException,
             IllegalAccessException
   {
      String className = System.getProperty(XMLParserFactory.CLASS_KEY,
                                            XMLParserFactory.DEFAULT_CLASS);
      return XMLParserFactory.createXMLParser(className,
                                              new StdXMLBuilder());
   }

   public static IXMLParser createDefaultXMLParser(IXMLBuilder builder)
      throws ClassNotFoundException,
             InstantiationException,
             IllegalAccessException
   {
      String className = System.getProperty(XMLParserFactory.CLASS_KEY,
                                            XMLParserFactory.DEFAULT_CLASS);
      return XMLParserFactory.createXMLParser(className, builder);
   }

   public static IXMLParser createXMLParser(String      className,
                                            IXMLBuilder builder)
      throws ClassNotFoundException,
             InstantiationException,
             IllegalAccessException
   {
      Class cls = Class.forName(className);
      IXMLParser parser = (IXMLParser) cls.newInstance();
      parser.setBuilder(builder);
      parser.setValidator(new NonValidator());
      return parser;
   }
}
