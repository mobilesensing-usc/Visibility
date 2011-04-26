package com.usc.resl.visibility;


import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;


public class WeatherXMLHandler extends DefaultHandler{

     // ===========================================================
     // Fields
     // ===========================================================
     
     private boolean tag_temperature = false;
     private boolean tag_humidity = false;
     private boolean tag_visibility = false;
     
     private XMLParsedDataSet myXMLParsedDataSet = new XMLParsedDataSet();
     String temperature=new String();
     String humidity=new String();
     String visibility=new String();
     int i=0,j=0;
     // ===========================================================
     // Getter & Setter
     // ===========================================================

     public XMLParsedDataSet getParsedData() {
          return this.myXMLParsedDataSet;
     }

     // ===========================================================
     // Methods
     // ===========================================================
     @Override
     public void startDocument() throws SAXException {
          this.myXMLParsedDataSet = new XMLParsedDataSet();
     }

     @Override
     public void endDocument() throws SAXException {
          // Nothing to do
     }

     /** Gets be called on opening tags like:
      * <tag>
      * Can provide attribute(s), when xml was like:
      * <tag attribute="attributeValue">*/
     @Override
     public void startElement(String namespaceURI, String localName,
               String qName, Attributes atts) throws SAXException {
          if (localName.equals("visibility_mi")) {
               this.tag_visibility = true;
          }else if (localName.equals("temp_f")) {
               this.tag_temperature = true;
          }else if (localName.equals("relative_humidity")) {
               this.tag_humidity = true;
          }          
     }
     
     /** Gets be called on closing tags like:
      * </tag> */
     @Override
     public void endElement(String namespaceURI, String localName, String qName)
               throws SAXException {
          if (localName.equals("visibility_mi")) {
               this.tag_visibility = false;
          }else if (localName.equals("temp_f")) {
               this.tag_temperature = false;
          }else if (localName.equals("relative_humidity")) {
               this.tag_humidity = false;
          }
     }
     
     /** Gets be called on the following structure:
      * <tag>characters</tag> */
     @Override
    public void characters(char ch[], int start, int length) {
          if(this.tag_humidity){
        	  humidity=new String(ch,start,length);
        	  myXMLParsedDataSet.setHumidity(humidity);
          }
          if(this.tag_temperature){        	 
        	  temperature=new String(ch,start,length);
        	  myXMLParsedDataSet.setTemperature(temperature);
          }
          if(this.tag_visibility){
        	  visibility=new String(ch,start,length);
        	  myXMLParsedDataSet.setVisibility(visibility);
          }
   }
}

