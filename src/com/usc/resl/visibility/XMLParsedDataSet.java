package com.usc.resl.visibility;

public class XMLParsedDataSet{
     private String temperature = null;
     private String humidity = null;
     private String visibility = null;
     public String getTemperature() {
          return temperature;
     }
     public void setTemperature(String temperature) {
          this.temperature = temperature;
     }
     public String getHumidity() {
          return humidity;
     }
     public void setHumidity(String humidity) {
          this.humidity=humidity;
     }
     public String getVisibility(){
    	 return visibility;
     }
     public void setVisibility(String visibility){
    	 this.visibility=visibility;
     }
}
